package com.versus.api.game;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.versus.api.questions.QuestionStatus;
import com.versus.api.questions.QuestionType;
import com.versus.api.questions.domain.Question;
import com.versus.api.questions.domain.QuestionOption;
import com.versus.api.questions.repo.QuestionRepository;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class SingleplayerGameIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private QuestionRepository questions;

    private MockMvc mvc;

    @PostConstruct
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(springSecurityFilterChain)
                .build();
    }

    @Test
    void registeredUserStartsSurvivalAndAnswersCorrectly() throws Exception {
        Question question = binaryQuestion();
        UUID correctOptionId = question.getOptions().getFirst().getId();
        String accessToken = register("survival_user", "survival_user@versus.com");

        MvcResult startResult = mvc.perform(post("/api/game/survival/start")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").isNotEmpty())
                .andExpect(jsonPath("$.question.type").value("BINARY"))
                .andReturn();

        JsonNode startJson = mapper.readTree(startResult.getResponse().getContentAsString());
        String sessionId = startJson.get("sessionId").asText();
        String questionId = startJson.get("question").get("id").asText();

        mvc.perform(post("/api/game/survival/answer")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sessionId":"%s","questionId":"%s","optionId":"%s"}
                                """.formatted(sessionId, questionId, correctOptionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.livesRemaining").value(3))
                .andExpect(jsonPath("$.lifeDelta").value(0))
                .andExpect(jsonPath("$.streak").value(1))
                .andExpect(jsonPath("$.scoreDelta").value(50))
                .andExpect(jsonPath("$.gameOver").value(false));
    }

    @Test
    void registeredUserStartsPrecisionAndAnswersExactly() throws Exception {
        numericQuestion();
        String accessToken = register("precision_user", "precision_user@versus.com");

        MvcResult startResult = mvc.perform(post("/api/game/precision/start")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").isNotEmpty())
                .andExpect(jsonPath("$.question.type").value("NUMERIC"))
                .andReturn();

        JsonNode startJson = mapper.readTree(startResult.getResponse().getContentAsString());
        String sessionId = startJson.get("sessionId").asText();
        String questionId = startJson.get("question").get("id").asText();

        mvc.perform(post("/api/game/precision/answer")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sessionId":"%s","questionId":"%s","value":100}
                                """.formatted(sessionId, questionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lifeDelta").value(5))
                .andExpect(jsonPath("$.livesRemaining").value(105))
                .andExpect(jsonPath("$.deviation").value(0.0))
                .andExpect(jsonPath("$.deviationPercent").value(0.0))
                .andExpect(jsonPath("$.gameOver").value(false));
    }

    private String register(String username, String email) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s","password":"secret123"}
                                """.formatted(username, email)))
                .andExpect(status().isCreated())
                .andReturn();
        return mapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken")
                .asText();
    }

    private Question binaryQuestion() {
        Question question = Question.builder()
                .text("Who has more seeded goals?")
                .type(QuestionType.BINARY)
                .category("football")
                .status(QuestionStatus.ACTIVE)
                .scrapedAt(Instant.now())
                .build();
        question.getOptions().add(QuestionOption.builder()
                .question(question)
                .text("Correct Player")
                .isCorrect(true)
                .build());
        question.getOptions().add(QuestionOption.builder()
                .question(question)
                .text("Incorrect Player")
                .isCorrect(false)
                .build());
        return questions.saveAndFlush(question);
    }

    private Question numericQuestion() {
        return questions.saveAndFlush(Question.builder()
                .text("What is the exact seeded value?")
                .type(QuestionType.NUMERIC)
                .category("geography")
                .status(QuestionStatus.ACTIVE)
                .scrapedAt(Instant.now())
                .unit("points")
                .correctValue(new BigDecimal("100"))
                .tolerancePercent(new BigDecimal("5"))
                .build());
    }
}
