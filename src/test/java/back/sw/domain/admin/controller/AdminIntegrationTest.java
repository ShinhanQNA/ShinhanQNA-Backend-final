package back.sw.domain.admin.controller;

import back.sw.domain.member.entity.Member;
import back.sw.domain.member.entity.MemberRole;
import back.sw.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
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
class AdminIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 관리자만_신고목록을_조회할_수_있다() throws Exception {
        String adminEmail = "admin-list@univ.ac.kr";
        String adminToken = registerAndLogin(adminEmail, "20256001", "adminlist");
        promoteToAdmin(adminEmail);
        String writerToken = registerAndLogin("writer-list@univ.ac.kr", "20256002", "writerlist");
        String reporterToken = registerAndLogin("reporter-list@univ.ac.kr", "20256003", "reporterlist");

        int firstPostId = createPost(writerToken, "FREE", "신고 대상1", "본문1", List.of());
        mockMvc.perform(
                post("/api/v1/posts/{postId}/reports", firstPostId)
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "SPAM")))
        ).andExpect(status().isCreated());

        int secondPostId = createPost(writerToken, "FREE", "신고 대상2", "본문2", List.of());
        mockMvc.perform(
                post("/api/v1/posts/{postId}/reports", secondPostId)
                        .header("Authorization", "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("reason", "ABUSE")))
        ).andExpect(status().isCreated());

        MvcResult listResult = mockMvc.perform(
                        get("/api/v1/admin/reports/posts")
                                .param("page", "0")
                                .param("size", "1")
                                .header("Authorization", "Bearer " + adminToken)
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(listResult.getResponse().getContentAsString());
        assertEquals(1, body.get("data").get("items").size());
        assertEquals(0, body.get("data").get("page").asInt());
        assertEquals(1, body.get("data").get("size").asInt());
        assertEquals(2, body.get("data").get("totalElements").asInt());
        assertEquals(true, body.get("data").get("hasNext").asBoolean());

        mockMvc.perform(
                get("/api/v1/admin/reports/posts")
                        .header("Authorization", "Bearer " + reporterToken)
        ).andExpect(status().isForbidden());
    }

    @Test
    void 관리자는_게시글을_강제삭제할_수_있다() throws Exception {
        String adminEmail = "admin-post@univ.ac.kr";
        String adminToken = registerAndLogin(adminEmail, "20256004", "adminpost");
        promoteToAdmin(adminEmail);
        String writerToken = registerAndLogin("writer-post@univ.ac.kr", "20256005", "writerpost");

        int postId = createPost(writerToken, "QNA", "삭제 대상", "내용", List.of());

        mockMvc.perform(
                delete("/api/v1/admin/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + writerToken)
        ).andExpect(status().isForbidden());

        mockMvc.perform(
                delete("/api/v1/admin/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + adminToken)
        ).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/posts/{postId}", postId))
                .andExpect(status().isNotFound());
    }

    @Test
    void 관리자는_댓글을_강제삭제하고_집계를_맞춘다() throws Exception {
        String adminEmail = "admin-comment@univ.ac.kr";
        String adminToken = registerAndLogin(adminEmail, "20256006", "admincomment");
        promoteToAdmin(adminEmail);
        String writerToken = registerAndLogin("writer-comment@univ.ac.kr", "20256007", "writercomment");
        String commenterToken = registerAndLogin("commenter-comment@univ.ac.kr", "20256008", "commentercomment");

        int postId = createPost(writerToken, "FREE", "댓글 삭제 대상", "내용", List.of());
        int commentId = createComment(postId, commenterToken, "삭제될 댓글");

        mockMvc.perform(
                delete("/api/v1/admin/comments/{commentId}", commentId)
                        .header("Authorization", "Bearer " + commenterToken)
        ).andExpect(status().isForbidden());

        mockMvc.perform(
                delete("/api/v1/admin/comments/{commentId}", commentId)
                        .header("Authorization", "Bearer " + adminToken)
        ).andExpect(status().isOk());

        MvcResult commentList = mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode commentBody = objectMapper.readTree(commentList.getResponse().getContentAsString());
        assertEquals("삭제된 댓글입니다.", commentBody.get("data").get("items").get(0).get("content").asText());
        assertEquals(true, commentBody.get("data").get("items").get(0).get("deleted").asBoolean());

        MvcResult detail = mockMvc.perform(get("/api/v1/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode detailBody = objectMapper.readTree(detail.getResponse().getContentAsString());
        assertEquals(0, detailBody.get("data").get("commentCount").asInt());
    }

    private void promoteToAdmin(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow();
        ReflectionTestUtils.setField(member, "role", MemberRole.ADMIN);
        memberRepository.flush();
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
