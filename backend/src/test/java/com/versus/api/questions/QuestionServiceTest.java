package com.versus.api.questions;

import com.versus.api.common.exception.ApiException;
import com.versus.api.common.exception.ErrorCode;
import com.versus.api.questions.domain.Question;
import com.versus.api.questions.domain.QuestionOption;
import com.versus.api.questions.dto.QuestionBinaryResponse;
import com.versus.api.questions.dto.QuestionNumericResponse;
import com.versus.api.questions.dto.QuestionResponse;
import com.versus.api.questions.repo.QuestionRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("QuestionService")
@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock QuestionRepository repo;
    @InjectMocks QuestionService questionService;

    private static final UUID QID = UUID.fromString("aaaa0000-0000-0000-0000-000000000001");

    private Question activeBinaryQuestion() {
        QuestionOption optA = QuestionOption.builder().id(UUID.randomUUID()).text("Zaragoza").isCorrect(false).build();
        QuestionOption optB = QuestionOption.builder().id(UUID.randomUUID()).text("Barcelona").isCorrect(true).build();
        return Question.builder().id(QID).type(QuestionType.BINARY).status(QuestionStatus.ACTIVE)
                .text("Capital?").category("geo").options(List.of(optA, optB)).build();
    }

    private Question activeNumericQuestion() {
        return Question.builder().id(QID).type(QuestionType.NUMERIC).status(QuestionStatus.ACTIVE)
                .text("Population?").category("geo").unit("millions").build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getById
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("getById")
    @Nested
    class GetById {

        @DisplayName("Pregunta activa devuelve respuesta correcta")
        @Test
        void preguntaActiva_devuelveRespuesta() {
            when(repo.findById(QID)).thenReturn(Optional.of(activeBinaryQuestion()));
            QuestionResponse res = questionService.getById(QID);
            assertThat(res).isInstanceOf(QuestionBinaryResponse.class);
            assertThat(res.id()).isEqualTo(QID);
        }

        @DisplayName("Pregunta inactiva lanza NOT_FOUND")
        @Test
        void preguntaInactiva_lanzaNotFound() {
            Question inactive = Question.builder().id(QID).type(QuestionType.BINARY)
                    .status(QuestionStatus.PENDING_REVIEW).text("X").build();
            when(repo.findById(QID)).thenReturn(Optional.of(inactive));
            assertThatThrownBy(() -> questionService.getById(QID))
                    .isInstanceOf(ApiException.class)
                    .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(ErrorCode.NOT_FOUND));
        }

        @DisplayName("Pregunta no encontrada lanza NOT_FOUND")
        @Test
        void preguntaNoEncontrada_lanzaNotFound() {
            when(repo.findById(QID)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> questionService.getById(QID))
                    .isInstanceOf(ApiException.class)
                    .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(ErrorCode.NOT_FOUND));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getRandom
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("getRandom")
    @Nested
    class GetRandom {

        @DisplayName("Llama a findRandomActive con tipo y categoría")
        @Test
        void llamaFindRandomActiveConTipoYCategoria() {
            when(repo.findRandomActive("BINARY", "geo")).thenReturn(Optional.of(activeBinaryQuestion()));
            questionService.getRandom(QuestionType.BINARY, "geo");
            verify(repo).findRandomActive("BINARY", "geo");
        }

        @DisplayName("tipo=null pasa null al repositorio")
        @Test
        void tipoNull_pasaNullAlRepo() {
            when(repo.findRandomActive(null, null)).thenReturn(Optional.of(activeBinaryQuestion()));
            questionService.getRandom(null, null);
            verify(repo).findRandomActive(null, null);
        }

        @DisplayName("categoría blank se normaliza a null")
        @Test
        void categoriaBlank_normalizaANull() {
            when(repo.findRandomActive("BINARY", null)).thenReturn(Optional.of(activeBinaryQuestion()));
            questionService.getRandom(QuestionType.BINARY, "   ");
            verify(repo).findRandomActive("BINARY", null);
        }

        @DisplayName("Sin preguntas disponibles lanza NOT_FOUND")
        @Test
        void sinPreguntas_lanzaNotFound() {
            when(repo.findRandomActive(any(), any())).thenReturn(Optional.empty());
            assertThatThrownBy(() -> questionService.getRandom(QuestionType.BINARY, null))
                    .isInstanceOf(ApiException.class)
                    .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(ErrorCode.NOT_FOUND));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // findActiveQuestion
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("findActiveQuestion")
    @Nested
    class FindActiveQuestion {

        @DisplayName("Pregunta activa del tipo correcto la devuelve")
        @Test
        void preguntaActivaTipoCorecto_devuelve() {
            when(repo.findById(QID)).thenReturn(Optional.of(activeBinaryQuestion()));
            Question q = questionService.findActiveQuestion(QID, QuestionType.BINARY);
            assertThat(q.getId()).isEqualTo(QID);
        }

        @DisplayName("Tipo de pregunta incorrecto lanza VALIDATION_ERROR")
        @Test
        void tipoIncorrecto_lanzaValidation() {
            when(repo.findById(QID)).thenReturn(Optional.of(activeBinaryQuestion())); // BINARY
            assertThatThrownBy(() -> questionService.findActiveQuestion(QID, QuestionType.NUMERIC))
                    .isInstanceOf(ApiException.class)
                    .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
        }

        @DisplayName("Pregunta inactiva lanza NOT_FOUND")
        @Test
        void preguntaInactiva_lanzaNotFound() {
            Question inactive = Question.builder().id(QID).type(QuestionType.BINARY)
                    .status(QuestionStatus.PENDING_REVIEW).text("X").build();
            when(repo.findById(QID)).thenReturn(Optional.of(inactive));
            assertThatThrownBy(() -> questionService.findActiveQuestion(QID, QuestionType.BINARY))
                    .isInstanceOf(ApiException.class)
                    .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(ErrorCode.NOT_FOUND));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // getCategories
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("getCategories")
    @Nested
    class GetCategories {

        @DisplayName("Delega al repositorio con status ACTIVE")
        @Test
        void delegaAlRepositorioConStatusActive() {
            when(repo.findDistinctCategories(QuestionStatus.ACTIVE)).thenReturn(List.of("sport", "geo"));
            List<String> cats = questionService.getCategories();
            assertThat(cats).containsExactly("sport", "geo");
            verify(repo).findDistinctCategories(QuestionStatus.ACTIVE);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // toResponse
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("toResponse")
    @Nested
    class ToResponse {

        @DisplayName("Pregunta BINARY devuelve QuestionBinaryResponse")
        @Test
        void binary_devuelveQuestionBinaryResponse() {
            QuestionResponse res = questionService.toResponse(activeBinaryQuestion());
            assertThat(res).isInstanceOf(QuestionBinaryResponse.class);
        }

        @DisplayName("Pregunta NUMERIC devuelve QuestionNumericResponse con unit")
        @Test
        void numeric_devuelveQuestionNumericResponseConUnit() {
            QuestionResponse res = questionService.toResponse(activeNumericQuestion());
            assertThat(res).isInstanceOf(QuestionNumericResponse.class);
            assertThat(((QuestionNumericResponse) res).unit()).isEqualTo("millions");
        }

        @DisplayName("Opciones de pregunta BINARY ordenadas alfabéticamente")
        @Test
        void binary_opcionesOrdenadasAlfabeticamente() {
            QuestionBinaryResponse res = (QuestionBinaryResponse) questionService.toResponse(activeBinaryQuestion());
            // activeBinaryQuestion tiene "Zaragoza" y "Barcelona"; deben salir Barcelona primero
            assertThat(res.options().get(0).text()).isEqualTo("Barcelona");
            assertThat(res.options().get(1).text()).isEqualTo("Zaragoza");
        }

        @DisplayName("Tipo null lanza VALIDATION_ERROR")
        @Test
        void tipoNull_lanzaValidationError() {
            Question q = Question.builder().id(QID).type(null).text("X").build();
            assertThatThrownBy(() -> questionService.toResponse(q))
                    .isInstanceOf(ApiException.class)
                    .satisfies(e -> assertThat(((ApiException) e).getCode()).isEqualTo(ErrorCode.VALIDATION_ERROR));
        }

        @DisplayName("Id y texto se mapean correctamente en BINARY")
        @Test
        void binary_mapeoIdYTextoCorrectos() {
            QuestionBinaryResponse res = (QuestionBinaryResponse) questionService.toResponse(activeBinaryQuestion());
            assertThat(res.id()).isEqualTo(QID);
            assertThat(res.text()).isEqualTo("Capital?");
            assertThat(res.category()).isEqualTo("geo");
        }
    }
}
