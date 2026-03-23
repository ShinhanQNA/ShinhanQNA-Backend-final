package back.sw.domain.like.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LikeIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 같은_사용자_재요청시_좋아요가_토글된다() throws Exception {
        String writerToken = registerAndLogin("likewriter1@univ.ac.kr", "20253001", "likewriter1");
        String likerToken = registerAndLogin("likeuser1@univ.ac.kr", "20253002", "likeuser1");
        int postId = createPost(writerToken, "FREE", "좋아요 토글", "본문", List.of());

        MvcResult firstLikeResult = mockMvc.perform(
                        post("/api/v1/posts/{postId}/likes", postId)
                                .header("Authorization", "Bearer " + likerToken)
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode firstLikeBody = objectMapper.readTree(firstLikeResult.getResponse().getContentAsString());
        assertEquals(true, firstLikeBody.get("data").get("liked").asBoolean());
        assertEquals(1, firstLikeBody.get("data").get("likeCount").asInt());

        MvcResult secondLikeResult = mockMvc.perform(
                        post("/api/v1/posts/{postId}/likes", postId)
                                .header("Authorization", "Bearer " + likerToken)
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode secondLikeBody = objectMapper.readTree(secondLikeResult.getResponse().getContentAsString());
        assertEquals(false, secondLikeBody.get("data").get("liked").asBoolean());
        assertEquals(0, secondLikeBody.get("data").get("likeCount").asInt());
    }

    @Test
    void 서로_다른_사용자_좋아요는_누적되고_상세_집계와_일치한다() throws Exception {
        String writerToken = registerAndLogin("likewriter2@univ.ac.kr", "20253003", "likewriter2");
        String liker1Token = registerAndLogin("likeuser2@univ.ac.kr", "20253004", "likeuser2");
        String liker2Token = registerAndLogin("likeuser3@univ.ac.kr", "20253005", "likeuser3");
        int postId = createPost(writerToken, "QNA", "좋아요 누적", "본문", List.of());

        mockMvc.perform(
                post("/api/v1/posts/{postId}/likes", postId)
                        .header("Authorization", "Bearer " + liker1Token)
        ).andExpect(status().isOk());

        MvcResult secondLikeResult = mockMvc.perform(
                        post("/api/v1/posts/{postId}/likes", postId)
                                .header("Authorization", "Bearer " + liker2Token)
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode secondLikeBody = objectMapper.readTree(secondLikeResult.getResponse().getContentAsString());
        assertEquals(2, secondLikeBody.get("data").get("likeCount").asInt());

        MvcResult detailResult = mockMvc.perform(
                        get("/api/v1/posts/{postId}", postId)
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode detailBody = objectMapper.readTree(detailResult.getResponse().getContentAsString());
        assertEquals(2, detailBody.get("data").get("likeCount").asInt());
    }

    @Test
    void 삭제된_게시글에는_좋아요를_등록할_수_없다() throws Exception {
        String writerToken = registerAndLogin("likewriter3@univ.ac.kr", "20253006", "likewriter3");
        String likerToken = registerAndLogin("likeuser4@univ.ac.kr", "20253007", "likeuser4");
        int postId = createPost(writerToken, "FREE", "삭제 후 좋아요", "본문", List.of());

        mockMvc.perform(
                delete("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + writerToken)
        ).andExpect(status().isOk());

        mockMvc.perform(
                post("/api/v1/posts/{postId}/likes", postId)
                        .header("Authorization", "Bearer " + likerToken)
        ).andExpect(status().isNotFound());
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
}
