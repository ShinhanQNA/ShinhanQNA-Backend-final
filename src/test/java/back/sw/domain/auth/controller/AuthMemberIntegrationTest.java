package back.sw.domain.auth.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthMemberIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 회원가입_성공() throws Exception {
        MvcResult result = mockMvc.perform(
                        post("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "email", "user1@univ.ac.kr",
                                        "studentNumber", "20250001",
                                        "password", "password1234",
                                        "nickname", "dongbin1"
                                )))
                ).andExpect(status().isCreated())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals("201-1", body.get("resultCode").asText());
        assertEquals("dongbin1", body.get("data").get("nickname").asText());
    }

    @Test
    void 닉네임_중복이면_회원가입_실패() throws Exception {
        register("user2@univ.ac.kr", "20250002", "password1234", "dupNick");

        mockMvc.perform(
                        post("/api/v1/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "email", "user3@univ.ac.kr",
                                        "studentNumber", "20250003",
                                        "password", "password1234",
                                        "nickname", "dupNick"
                                )))
                ).andExpect(status().isBadRequest());
    }

    @Test
    void 로그인_재발급_로그아웃_흐름() throws Exception {
        register("user4@univ.ac.kr", "20250004", "password1234", "loginUser");

        MvcResult loginResult = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "email", "user4@univ.ac.kr",
                                        "password", "password1234"
                                )))
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginBody.get("data").get("accessToken").asText();
        String refreshToken = loginBody.get("data").get("refreshToken").asText();
        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "refreshToken", refreshToken
                                )))
                ).andExpect(status().isOk());

        mockMvc.perform(
                        post("/api/v1/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "refreshToken", refreshToken
                                )))
                ).andExpect(status().isOk());

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "refreshToken", refreshToken
                                )))
                ).andExpect(status().isUnauthorized());
    }

    @Test
    void 닉네임_수정_성공_및_중복_실패() throws Exception {
        register("user5@univ.ac.kr", "20250005", "password1234", "originNick");
        register("user6@univ.ac.kr", "20250006", "password1234", "occupiedNick");

        MvcResult loginResult = mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "email", "user5@univ.ac.kr",
                                        "password", "password1234"
                                )))
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode loginBody = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = loginBody.get("data").get("accessToken").asText();

        MvcResult patchResult = mockMvc.perform(
                        patch("/api/v1/members/me/nickname")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "nickname", "changedNick"
                                )))
                ).andExpect(status().isOk())
                .andReturn();

        JsonNode patchBody = objectMapper.readTree(patchResult.getResponse().getContentAsString());
        assertEquals("changedNick", patchBody.get("data").get("nickname").asText());

        mockMvc.perform(
                        patch("/api/v1/members/me/nickname")
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "nickname", "occupiedNick"
                                )))
                ).andExpect(status().isBadRequest());
    }

    @Test
    void 닉네임_수정은_잘못된_토큰_형식이면_401() throws Exception {
        register("user7@univ.ac.kr", "20250007", "password1234", "originNick7");

        mockMvc.perform(
                        patch("/api/v1/members/me/nickname")
                                .header("Authorization", "Token invalid")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of(
                                        "nickname", "changedNick7"
                                )))
                ).andExpect(status().isUnauthorized());
    }

    private void register(String email, String studentNumber, String password, String nickname) throws Exception {
        mockMvc.perform(
                post("/api/v1/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "studentNumber", studentNumber,
                                "password", password,
                                "nickname", nickname
                        )))
        ).andExpect(status().isCreated());
    }
}
