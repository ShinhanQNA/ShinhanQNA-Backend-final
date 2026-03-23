package back.sw.domain.comment.controller;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommentIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 댓글_작성_조회시_익명번호와_작성자배지가_정책대로_노출된다() throws Exception {
        String writerToken = registerAndLogin("commentwriter1@univ.ac.kr", "20251011", "writer1");
        String otherToken = registerAndLogin("commentuser1@univ.ac.kr", "20251012", "user1");

        int postId = createPost(writerToken, "FREE", "댓글 테스트", "본문", List.of());

        createComment(postId, writerToken, "원글 작성자 첫 댓글");
        createComment(postId, otherToken, "다른 사용자 댓글");
        createComment(postId, writerToken, "원글 작성자 두 번째 댓글");

        MvcResult listResult = mockMvc.perform(
                        get("/api/v1/posts/{postId}/comments", postId)
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode listBody = objectMapper.readTree(listResult.getResponse().getContentAsString());
        JsonNode items = listBody.get("data").get("items");

        assertEquals(3, items.size());
        assertEquals("원글 작성자 두 번째 댓글", items.get(0).get("content").asText());
        assertEquals("익명1", items.get(0).get("anonymousLabel").asText());
        assertEquals(true, items.get(0).get("isPostAuthor").asBoolean());
        assertEquals("익명2", items.get(1).get("anonymousLabel").asText());
        assertEquals(false, items.get(1).get("isPostAuthor").asBoolean());
        assertEquals("익명1", items.get(2).get("anonymousLabel").asText());
        assertEquals(true, items.get(2).get("isPostAuthor").asBoolean());
    }

    @Test
    void 댓글_삭제시_내용숨김과_게시글_댓글수_정합성_유지() throws Exception {
        String writerToken = registerAndLogin("commentwriter2@univ.ac.kr", "20251013", "writer2");
        String otherToken = registerAndLogin("commentuser2@univ.ac.kr", "20251014", "user2");

        int postId = createPost(writerToken, "QNA", "댓글 삭제", "본문", List.of());
        int commentId = createComment(postId, otherToken, "삭제될 댓글");

        mockMvc.perform(
                delete("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId)
                        .header("Authorization", "Bearer " + otherToken)
        ).andExpect(status().isOk());

        MvcResult postDetailResult = mockMvc.perform(
                        get("/api/v1/posts/{postId}", postId)
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode postDetailBody = objectMapper.readTree(postDetailResult.getResponse().getContentAsString());
        assertEquals(0, postDetailBody.get("data").get("commentCount").asInt());

        MvcResult commentListResult = mockMvc.perform(
                        get("/api/v1/posts/{postId}/comments", postId)
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode commentListBody = objectMapper.readTree(commentListResult.getResponse().getContentAsString());
        JsonNode firstComment = commentListBody.get("data").get("items").get(0);
        assertEquals("삭제된 댓글입니다.", firstComment.get("content").asText());
        assertEquals(true, firstComment.get("deleted").asBoolean());
    }

    @Test
    void 비작성자_댓글_삭제_요청은_403() throws Exception {
        String writerToken = registerAndLogin("commentwriter3@univ.ac.kr", "20251015", "writer3");
        String otherToken = registerAndLogin("commentuser3@univ.ac.kr", "20251016", "user3");

        int postId = createPost(writerToken, "FREE", "권한 테스트", "본문", List.of());
        int commentId = createComment(postId, writerToken, "작성자 댓글");

        mockMvc.perform(
                delete("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId)
                        .header("Authorization", "Bearer " + otherToken)
        ).andExpect(status().isForbidden());
    }

    @Test
    void 댓글_수정_성공() throws Exception {
        String writerToken = registerAndLogin("commentwriter4@univ.ac.kr", "20251017", "writer4");
        int postId = createPost(writerToken, "FREE", "댓글 수정 테스트", "본문", List.of());
        int commentId = createComment(postId, writerToken, "수정 전 댓글");

        mockMvc.perform(
                patch("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId)
                        .header("Authorization", "Bearer " + writerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "수정 후 댓글")))
        ).andExpect(status().isOk());

        MvcResult listResult = mockMvc.perform(
                        get("/api/v1/posts/{postId}/comments", postId)
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode items = objectMapper.readTree(listResult.getResponse().getContentAsString())
                .get("data")
                .get("items");
        assertEquals("수정 후 댓글", items.get(0).get("content").asText());
    }

    @Test
    void 비작성자_댓글_수정_요청은_403() throws Exception {
        String writerToken = registerAndLogin("commentwriter5@univ.ac.kr", "20251018", "writer5");
        String otherToken = registerAndLogin("commentuser5@univ.ac.kr", "20251019", "user5");
        int postId = createPost(writerToken, "FREE", "댓글 수정 권한", "본문", List.of());
        int commentId = createComment(postId, writerToken, "작성자 댓글");

        mockMvc.perform(
                patch("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "변경 시도")))
        ).andExpect(status().isForbidden());
    }

    @Test
    void 삭제된_댓글_수정_요청은_400이고_내용은_유지된다() throws Exception {
        String writerToken = registerAndLogin("commentwriter6@univ.ac.kr", "20251020", "writer6");
        int postId = createPost(writerToken, "FREE", "삭제된 댓글 수정", "본문", List.of());
        int commentId = createComment(postId, writerToken, "삭제될 댓글");

        mockMvc.perform(
                delete("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId)
                        .header("Authorization", "Bearer " + writerToken)
        ).andExpect(status().isOk());

        mockMvc.perform(
                patch("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId)
                        .header("Authorization", "Bearer " + writerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", "수정 시도")))
        ).andExpect(status().isBadRequest());

        MvcResult listResult = mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode firstComment = objectMapper.readTree(listResult.getResponse().getContentAsString())
                .get("data")
                .get("items")
                .get(0);
        assertEquals("삭제된 댓글입니다.", firstComment.get("content").asText());
        assertEquals(true, firstComment.get("deleted").asBoolean());
    }

    @Test
    void 댓글_수정_요청시_빈_내용은_400() throws Exception {
        String writerToken = registerAndLogin("commentwriter7@univ.ac.kr", "20251025", "writer7");
        int postId = createPost(writerToken, "FREE", "댓글 빈값 검증", "본문", List.of());
        int commentId = createComment(postId, writerToken, "원본 댓글");

        mockMvc.perform(
                patch("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId)
                        .header("Authorization", "Bearer " + writerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("content", " ")))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void 댓글_수정_요청시_내용_누락은_400() throws Exception {
        String writerToken = registerAndLogin("commentwriter8@univ.ac.kr", "20251026", "writer8");
        int postId = createPost(writerToken, "FREE", "댓글 누락 검증", "본문", List.of());
        int commentId = createComment(postId, writerToken, "원본 댓글");

        mockMvc.perform(
                patch("/api/v1/posts/{postId}/comments/{commentId}", postId, commentId)
                        .header("Authorization", "Bearer " + writerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of()))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void 대댓글_작성_성공() throws Exception {
        String writerToken = registerAndLogin("commentwriter9@univ.ac.kr", "20251027", "writer9");
        String replierToken = registerAndLogin("commentuser9@univ.ac.kr", "20251028", "user9");
        int postId = createPost(writerToken, "FREE", "대댓글 테스트", "본문", List.of());
        int parentCommentId = createComment(postId, writerToken, "부모 댓글");

        createComment(postId, replierToken, "대댓글", parentCommentId);

        MvcResult listResult = mockMvc.perform(get("/api/v1/posts/{postId}/comments", postId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode items = objectMapper.readTree(listResult.getResponse().getContentAsString())
                .get("data")
                .get("items");

        assertEquals(2, items.size());
        assertEquals("대댓글", items.get(0).get("content").asText());
        assertEquals("부모 댓글", items.get(1).get("content").asText());
    }

    @Test
    void 다른_게시글_댓글을_부모로_대댓글_요청시_404() throws Exception {
        String writerToken = registerAndLogin("commentwriter10@univ.ac.kr", "20251029", "writer10");
        String replierToken = registerAndLogin("commentuser10@univ.ac.kr", "20251030", "user10");

        int firstPostId = createPost(writerToken, "FREE", "첫 게시글", "본문1", List.of());
        int secondPostId = createPost(writerToken, "FREE", "둘째 게시글", "본문2", List.of());
        int foreignParentCommentId = createComment(secondPostId, writerToken, "다른 글 댓글");

        mockMvc.perform(
                post("/api/v1/posts/{postId}/comments", firstPostId)
                        .header("Authorization", "Bearer " + replierToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "content", "대댓글 시도",
                                "parentId", foreignParentCommentId
                        )))
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

    private int createComment(int postId, String accessToken, String content) throws Exception {
        return createComment(postId, accessToken, content, null);
    }

    private int createComment(int postId, String accessToken, String content, Integer parentId) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("content", content);
        if (parentId != null) {
            payload.put("parentId", parentId);
        }

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/posts/{postId}/comments", postId)
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload))
                ).andExpect(status().isCreated())
                .andReturn();

        JsonNode createBody = objectMapper.readTree(createResult.getResponse().getContentAsString());
        return createBody.get("data").get("commentId").asInt();
    }
}
