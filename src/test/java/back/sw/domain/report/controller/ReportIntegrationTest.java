package back.sw.domain.report.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReportIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 게시글_신고는_등록되고_중복_신고는_409() throws Exception {
        String writerToken = registerAndLogin("reportwriter1@univ.ac.kr", "20255001", "reportwriter1");
        String reporterToken = registerAndLogin("reportuser1@univ.ac.kr", "20255002", "reportuser1");
        int postId = createPost(writerToken, "FREE", "신고 대상", "본문", List.of());

        mockMvc.perform(
                post("/api/v1/posts/{postId}/reports", postId)
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "reason", "SPAM",
                                "description", "도배성 게시물"
                        )))
        ).andExpect(status().isCreated());

        mockMvc.perform(
                post("/api/v1/posts/{postId}/reports", postId)
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "reason", "SPAM",
                                "description", "중복 신고"
                        )))
        ).andExpect(status().isConflict());
    }

    @Test
    void 댓글_신고는_description_없이도_성공하고_자동블라인드되지_않는다() throws Exception {
        String writerToken = registerAndLogin("reportwriter2@univ.ac.kr", "20255003", "reportwriter2");
        String commenterToken = registerAndLogin("reportcomment1@univ.ac.kr", "20255004", "reportcomment1");
        String reporterToken = registerAndLogin("reportuser2@univ.ac.kr", "20255005", "reportuser2");
        int postId = createPost(writerToken, "QNA", "댓글 신고 대상", "본문", List.of());
        int commentId = createComment(postId, commenterToken, "신고 대상 댓글");

        mockMvc.perform(
                post("/api/v1/comments/{commentId}/reports", commentId)
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "reason", "ABUSE"
                        )))
        ).andExpect(status().isCreated());

        MvcResult postDetailResult = mockMvc.perform(
                        get("/api/v1/posts/{postId}", postId)
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode postDetailBody = objectMapper.readTree(postDetailResult.getResponse().getContentAsString());
        assertEquals("댓글 신고 대상", postDetailBody.get("data").get("title").asText());

        MvcResult commentListResult = mockMvc.perform(
                        get("/api/v1/posts/{postId}/comments", postId)
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode commentListBody = objectMapper.readTree(commentListResult.getResponse().getContentAsString());
        assertEquals("신고 대상 댓글", commentListBody.get("data").get("items").get(0).get("content").asText());
    }

    private String registerAndLogin(String email, String studentNumber, String nickname) throws Exception {
        mockMvc.perform(
                post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "studentNumber", studentNumber,
                                "password", "password1234",
                                "nickname", nickname
                        )))
        ).andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "email", email,
                                        "password", "password1234"
                                )))
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return loginBody.get("data").get("accessToken").asText();
    }

    private int createPost(
            String accessToken,
            String boardType,
            String title,
            String content,
            List<MockMultipartFile> images
    ) throws Exception {
        MockMultipartHttpServletRequestBuilder requestBuilder = multipart("/api/v1/posts")
                .file(createPostPart(boardType, title, content))
                .header("Authorization", "Bearer " + accessToken);

        for (MockMultipartFile image : images) {
            requestBuilder.file(image);
        }

        MvcResult createResult = mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode createBody = objectMapper.readTree(createResult.getResponse().getContentAsString());
        return createBody.get("data").get("postId").asInt();
    }

    private MockMultipartFile createPostPart(String boardType, String title, String content) throws Exception {
        return new MockMultipartFile(
                "post",
                "post.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(Map.of(
                        "boardType", boardType,
                        "title", title,
                        "content", content
                ))
        );
    }

    private int createComment(int postId, String accessToken, String content) throws Exception {
        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/posts/{postId}/comments", postId)
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("content", content)))
                ).andExpect(status().isCreated())
                .andReturn();

        JsonNode createBody = objectMapper.readTree(createResult.getResponse().getContentAsString());
        return createBody.get("data").get("commentId").asInt();
    }
}
