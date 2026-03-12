package back.sw.domain.post.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
class PostIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 게시글_작성_목록_상세_삭제_성공_흐름() throws Exception {
        String accessToken = registerAndLogin("postuser1@univ.ac.kr", "20251001", "postnick1");

        MvcResult createResult = createPost(
                accessToken,
                "FREE",
                "첫 게시글",
                "게시글 내용",
                List.of(
                        createImagePart("first.png", "file-one"),
                        createImagePart("second.png", "file-two")
                )
        );

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
        assertEquals(2, detailBody.get("data").get("imageUrls").size());
        assertTrue(detailBody.get("data").get("imageUrls").get(0).asText().startsWith("/uploads/"));
        assertTrue(detailBody.get("data").get("imageUrls").get(1).asText().startsWith("/uploads/"));

        mockMvc.perform(
                delete("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk());

        mockMvc.perform(
                get("/api/v1/posts/{postId}", postId)
        ).andExpect(status().isNotFound());
    }

    @Test
    void 게시글_작성_요청은_인증_토큰이_없으면_401() throws Exception {
        MockMultipartHttpServletRequestBuilder requestBuilder = multipart("/api/v1/posts")
                .file(createPostPart("FREE", "무토큰 작성", "인증 필요"));

        mockMvc.perform(requestBuilder)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 게시글_작성_요청은_리프레시토큰이면_401() throws Exception {
        mockMvc.perform(
                post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "postuser15@univ.ac.kr",
                                "studentNumber", "20251025",
                                "password", "password1234",
                                "nickname", "postnick15"
                        )))
        ).andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "email", "postuser15@univ.ac.kr",
                                        "password", "password1234"
                                )))
                ).andExpect(status().isOk())
                .andReturn();

        String refreshToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("data")
                .get("refreshToken")
                .asText();

        MockMultipartHttpServletRequestBuilder requestBuilder = multipart("/api/v1/posts")
                .file(createPostPart("FREE", "리프레시 토큰", "인증 실패"))
                .header("Authorization", "Bearer " + refreshToken);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 비작성자_삭제_요청은_403() throws Exception {
        String writerToken = registerAndLogin("postuser2@univ.ac.kr", "20251002", "postnick2");
        String otherToken = registerAndLogin("postuser3@univ.ac.kr", "20251003", "postnick3");

        MvcResult createResult = createPost(
                writerToken,
                "QNA",
                "삭제권한 테스트",
                "작성자만 삭제 가능",
                List.of()
        );

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
    void 게시글_수정_성공() throws Exception {
        String writerToken = registerAndLogin("postuser9@univ.ac.kr", "20251009", "postnick9");
        int postId = objectMapper.readTree(
                createPost(writerToken, "FREE", "수정 전 제목", "수정 전 내용", List.of())
                        .getResponse()
                        .getContentAsString()
        ).get("data").get("postId").asInt();

        mockMvc.perform(
                patch("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + writerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "수정 후 제목",
                                "content", "수정 후 내용"
                        )))
        ).andExpect(status().isOk());

        MvcResult detailResult = mockMvc.perform(get("/api/v1/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode detailBody = objectMapper.readTree(detailResult.getResponse().getContentAsString());
        assertEquals("수정 후 제목", detailBody.get("data").get("title").asText());
        assertEquals("수정 후 내용", detailBody.get("data").get("content").asText());
    }

    @Test
    void 비작성자_게시글_수정_요청은_403() throws Exception {
        String writerToken = registerAndLogin("postuser10@univ.ac.kr", "20251010", "postnick10");
        String otherToken = registerAndLogin("postuser11@univ.ac.kr", "20251021", "postnick11");
        int postId = objectMapper.readTree(
                createPost(writerToken, "QNA", "수정 권한", "작성자만 가능", List.of())
                        .getResponse()
                        .getContentAsString()
        ).get("data").get("postId").asInt();

        mockMvc.perform(
                patch("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "해킹 제목",
                                "content", "해킹 내용"
                        )))
        ).andExpect(status().isForbidden());
    }

    @Test
    void 삭제된_게시글_수정_요청은_404() throws Exception {
        String writerToken = registerAndLogin("postuser12@univ.ac.kr", "20251022", "postnick12");
        int postId = objectMapper.readTree(
                createPost(writerToken, "FREE", "삭제될 게시글", "삭제 후 수정 시도", List.of())
                        .getResponse()
                        .getContentAsString()
        ).get("data").get("postId").asInt();

        mockMvc.perform(
                delete("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + writerToken)
        ).andExpect(status().isOk());

        mockMvc.perform(
                patch("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + writerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "수정 시도",
                                "content", "수정 시도 내용"
                        )))
        ).andExpect(status().isNotFound());
    }

    @Test
    void 게시글_수정_요청시_빈_제목은_400() throws Exception {
        String writerToken = registerAndLogin("postuser13@univ.ac.kr", "20251023", "postnick13");
        int postId = objectMapper.readTree(
                createPost(writerToken, "FREE", "정상 제목", "정상 내용", List.of())
                        .getResponse()
                        .getContentAsString()
        ).get("data").get("postId").asInt();

        mockMvc.perform(
                patch("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + writerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", " ",
                                "content", "수정 내용"
                        )))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void 게시글_수정_요청시_내용_누락은_400() throws Exception {
        String writerToken = registerAndLogin("postuser14@univ.ac.kr", "20251024", "postnick14");
        int postId = objectMapper.readTree(
                createPost(writerToken, "QNA", "질문", "내용", List.of())
                        .getResponse()
                        .getContentAsString()
        ).get("data").get("postId").asInt();

        mockMvc.perform(
                patch("/api/v1/posts/{postId}", postId)
                        .header("Authorization", "Bearer " + writerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "제목만 존재"
                        )))
        ).andExpect(status().isBadRequest());
    }

    @Test
    void 게시글_목록은_최신순_정렬() throws Exception {
        String accessToken = registerAndLogin("postuser4@univ.ac.kr", "20251004", "postnick4");

        createPost(accessToken, "FREE", "오래된 글", "first", List.of());
        createPost(accessToken, "FREE", "최신 글", "second", List.of());

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

    @Test
    void 게시글_작성시_이미지_6개_업로드면_400() throws Exception {
        String accessToken = registerAndLogin("postuser5@univ.ac.kr", "20251005", "postnick5");

        MockMultipartHttpServletRequestBuilder requestBuilder = multipart("/api/v1/posts")
                .file(createPostPart("FREE", "이미지 제한", "6개 업로드 테스트"))
                .header("Authorization", "Bearer " + accessToken);

        for (int i = 0; i < 6; i++) {
            requestBuilder.file(createImagePart("img-" + i + ".png", "content-" + i));
        }

        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());
    }

    @Test
    void 모집게시판_작성시_모집정보가_없으면_400() throws Exception {
        String accessToken = registerAndLogin("postuser6@univ.ac.kr", "20251006", "postnick6");

        MockMultipartHttpServletRequestBuilder requestBuilder = multipart("/api/v1/posts")
                .file(createPostPart("PROJECT_RECRUIT", "모집글", "모집 내용", null))
                .header("Authorization", "Bearer " + accessToken);

        mockMvc.perform(requestBuilder).andExpect(status().isBadRequest());
    }

    @Test
    void 자유게시판_작성시_모집정보를_보내면_400() throws Exception {
        String accessToken = registerAndLogin("postuser7@univ.ac.kr", "20251007", "postnick7");

        MockMultipartHttpServletRequestBuilder requestBuilder = multipart("/api/v1/posts")
                .file(createPostPart(
                        "FREE",
                        "자유글",
                        "내용",
                        Map.of(
                                "capacity", 4,
                                "contactMethod", "오픈채팅",
                                "deadline", "2026-03-20"
                        )
                ))
                .header("Authorization", "Bearer " + accessToken);

        mockMvc.perform(requestBuilder).andExpect(status().isBadRequest());
    }

    @Test
    void 모집게시글_작성후_상세조회시_모집정보가_노출된다() throws Exception {
        String accessToken = registerAndLogin("postuser8@univ.ac.kr", "20251008", "postnick8");

        MvcResult createResult = createPost(
                accessToken,
                "STUDY_RECRUIT",
                "스터디 모집",
                "자바 스터디",
                List.of(),
                Map.of(
                        "capacity", 6,
                        "contactMethod", "이메일",
                        "deadline", "2026-03-25"
                )
        );

        int postId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("data")
                .get("postId")
                .asInt();

        MvcResult detailResult = mockMvc.perform(get("/api/v1/posts/{postId}", postId))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode detailBody = objectMapper.readTree(detailResult.getResponse().getContentAsString());
        JsonNode recruitment = detailBody.get("data").get("recruitment");
        assertEquals(6, recruitment.get("capacity").asInt());
        assertEquals("이메일", recruitment.get("contactMethod").asText());
        assertEquals("OPEN", recruitment.get("recruitStatus").asText());
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

    private MvcResult createPost(
            String accessToken,
            String boardType,
            String title,
            String content,
            List<MockMultipartFile> images
    ) throws Exception {
        return createPost(accessToken, boardType, title, content, images, null);
    }

    private MvcResult createPost(
            String accessToken,
            String boardType,
            String title,
            String content,
            List<MockMultipartFile> images,
            Map<String, Object> recruitment
    ) throws Exception {
        MockMultipartHttpServletRequestBuilder requestBuilder = multipart("/api/v1/posts")
                .file(createPostPart(boardType, title, content, recruitment))
                .header("Authorization", "Bearer " + accessToken);

        for (MockMultipartFile image : images) {
            requestBuilder.file(image);
        }

        return mockMvc.perform(requestBuilder)
                .andExpect(status().isCreated())
                .andReturn();
    }

    private MockMultipartFile createPostPart(String boardType, String title, String content) throws Exception {
        return createPostPart(boardType, title, content, null);
    }

    private MockMultipartFile createPostPart(
            String boardType,
            String title,
            String content,
            Map<String, Object> recruitment
    ) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("boardType", boardType);
        payload.put("title", title);
        payload.put("content", content);
        if (recruitment != null) {
            payload.put("recruitment", recruitment);
        }

        return new MockMultipartFile(
                "post",
                "post.json",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(payload)
        );
    }

    private MockMultipartFile createImagePart(String fileName, String content) {
        return new MockMultipartFile(
                "images",
                fileName,
                MediaType.IMAGE_PNG_VALUE,
                content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
