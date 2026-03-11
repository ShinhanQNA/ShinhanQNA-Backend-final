package back.sw.domain.post.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 게시글_작성_목록_상세_삭제_성공_흐름() throws Exception {
        String accessToken = registerAndLogin("postuser1@univ.ac.kr", "20251001", "postnick1");

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/posts")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "boardType", "FREE",
                                        "title", "첫 게시글",
                                        "content", "게시글 내용"
                                )))
                ).andExpect(status().isCreated())
                .andReturn();

        JsonNode createBody = objectMapper.readTree(createResult.getResponse().getContentAsString());
        int postId = createBody.get("data").get("postId").asInt();

        MvcResult listResult = mockMvc.perform(
                        get("/api/v1/posts")
                                .param("boardType", "FREE")
                                .param("page", "0")
                                .param("size", "20")
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode listBody = objectMapper.readTree(listResult.getResponse().getContentAsString());
        assertEquals("첫 게시글", listBody.get("data").get("items").get(0).get("title").asText());
        assertEquals(1, listBody.get("data").get("totalElements").asInt());

        MvcResult detailResult = mockMvc.perform(
                        get("/api/v1/posts/{postId}", postId)
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode detailBody = objectMapper.readTree(detailResult.getResponse().getContentAsString());
        assertEquals("익명", detailBody.get("data").get("authorName").asText());

        mockMvc.perform(
                delete("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk());

        mockMvc.perform(
                get("/api/v1/posts/{postId}", postId)
        ).andExpect(status().isNotFound());
    }

    @Test
    void 비작성자_삭제_요청은_403() throws Exception {
        String writerToken = registerAndLogin("postuser2@univ.ac.kr", "20251002", "postnick2");
        String otherToken = registerAndLogin("postuser3@univ.ac.kr", "20251003", "postnick3");

        MvcResult createResult = mockMvc.perform(
                        post("/api/v1/posts")
                                .header("Authorization", "Bearer " + writerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "boardType", "QNA",
                                        "title", "삭제권한 테스트",
                                        "content", "작성자만 삭제 가능"
                                )))
                ).andExpect(status().isCreated())
                .andReturn();

        int postId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data")
                .get("postId")
                .asInt();

        mockMvc.perform(
                delete("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + otherToken)
        ).andExpect(status().isForbidden());
    }

    @Test
    void 게시글_목록은_최신순_정렬() throws Exception {
        String accessToken = registerAndLogin("postuser4@univ.ac.kr", "20251004", "postnick4");

        createPost(accessToken, "FREE", "오래된 글", "first");
        createPost(accessToken, "FREE", "최신 글", "second");

        MvcResult listResult = mockMvc.perform(
                        get("/api/v1/posts")
                                .param("boardType", "FREE")
                                .param("page", "0")
                                .param("size", "20")
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode listBody = objectMapper.readTree(listResult.getResponse().getContentAsString());
        assertEquals("최신 글", listBody.get("data").get("items").get(0).get("title").asText());
        assertEquals("오래된 글", listBody.get("data").get("items").get(1).get("title").asText());
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

    private void createPost(String accessToken, String boardType, String title, String content) throws Exception {
        mockMvc.perform(
                post("/api/v1/posts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "boardType", boardType,
                                "title", title,
                                "content", content
                        )))
        ).andExpect(status().isCreated());
    }
}
