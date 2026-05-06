package com.versus.api.common.exception;

import com.versus.api.common.dto.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler")
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock MethodArgumentNotValidException validationEx;
    @Mock BindingResult bindingResult;

    // ═══════════════════════════════════════════════════════════════════════
    // handleApi
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("handleApi")
    @Nested
    class HandleApi {

        @DisplayName("NOT_FOUND produce HTTP 404")
        @Test
        void notFound_retorna404() {
            ResponseEntity<ErrorResponse> res = handler.handleApi(ApiException.notFound("missing"));
            assertThat(res.getStatusCode().value()).isEqualTo(404);
        }

        @DisplayName("CONFLICT produce HTTP 409")
        @Test
        void conflict_retorna409() {
            ResponseEntity<ErrorResponse> res = handler.handleApi(ApiException.conflict("dup"));
            assertThat(res.getStatusCode().value()).isEqualTo(409);
        }

        @DisplayName("UNAUTHORIZED produce HTTP 401")
        @Test
        void unauthorized_retorna401() {
            ResponseEntity<ErrorResponse> res = handler.handleApi(ApiException.unauthorized("denied"));
            assertThat(res.getStatusCode().value()).isEqualTo(401);
        }

        @DisplayName("VALIDATION_ERROR produce HTTP 400")
        @Test
        void validationError_retorna400() {
            ResponseEntity<ErrorResponse> res = handler.handleApi(ApiException.validation("bad input"));
            assertThat(res.getStatusCode().value()).isEqualTo(400);
        }

        @DisplayName("Cuerpo contiene error, message y status correctos")
        @Test
        void cuerpo_contieneErrorMessageStatusCorrectos() {
            ResponseEntity<ErrorResponse> res = handler.handleApi(ApiException.notFound("item missing"));
            ErrorResponse body = res.getBody();
            assertThat(body).isNotNull();
            assertThat(body.error()).isEqualTo("NOT_FOUND");
            assertThat(body.message()).isEqualTo("item missing");
            assertThat(body.status()).isEqualTo(404);
        }

        @DisplayName("FORBIDDEN produce HTTP 403 con código FORBIDDEN en cuerpo")
        @Test
        void forbidden_retorna403_codigoForbidden() {
            ResponseEntity<ErrorResponse> res = handler.handleApi(ApiException.forbidden("no access"));
            assertThat(res.getStatusCode().value()).isEqualTo(403);
            assertThat(res.getBody().error()).isEqualTo("FORBIDDEN");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // handleValidation
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("handleValidation")
    @Nested
    class HandleValidation {

        @DisplayName("Produce HTTP 400")
        @Test
        void retorna400() {
            when(validationEx.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(
                    new FieldError("req", "email", "must not be blank")));
            ResponseEntity<ErrorResponse> res = handler.handleValidation(validationEx);
            assertThat(res.getStatusCode().value()).isEqualTo(400);
        }

        @DisplayName("Cuerpo contiene error=VALIDATION_ERROR")
        @Test
        void cuerpoErrorEsValidationError() {
            when(validationEx.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(
                    new FieldError("req", "username", "size must be between 3 and 64")));
            ResponseEntity<ErrorResponse> res = handler.handleValidation(validationEx);
            assertThat(res.getBody().error()).isEqualTo("VALIDATION_ERROR");
        }

        @DisplayName("Mensaje concatena todos los field errors con formato campo: mensaje")
        @Test
        void mensajeContieneFieldErrorsConcatenados() {
            when(validationEx.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(
                    new FieldError("req", "email", "must not be blank"),
                    new FieldError("req", "password", "size must be between 6 and 100")));
            ResponseEntity<ErrorResponse> res = handler.handleValidation(validationEx);
            assertThat(res.getBody().message())
                    .contains("email: must not be blank")
                    .contains("password: size must be between 6 and 100");
        }

        @DisplayName("Sin field errors produce mensaje vacío")
        @Test
        void sinFieldErrors_mensajeVacio() {
            when(validationEx.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of());
            ResponseEntity<ErrorResponse> res = handler.handleValidation(validationEx);
            assertThat(res.getBody().message()).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // handleForbidden
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("handleForbidden")
    @Nested
    class HandleForbidden {

        @DisplayName("Produce HTTP 403")
        @Test
        void retorna403() {
            ResponseEntity<ErrorResponse> res = handler.handleForbidden(new AccessDeniedException("no"));
            assertThat(res.getStatusCode().value()).isEqualTo(403);
        }

        @DisplayName("Mensaje fijo 'Access denied' independiente del mensaje de la excepción")
        @Test
        void mensajeFijoAccessDenied() {
            ResponseEntity<ErrorResponse> res = handler.handleForbidden(
                    new AccessDeniedException("mensaje interno no expuesto"));
            assertThat(res.getBody().message()).isEqualTo("Access denied");
            assertThat(res.getBody().error()).isEqualTo("FORBIDDEN");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // handleAuth
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("handleAuth")
    @Nested
    class HandleAuth {

        @DisplayName("Produce HTTP 401")
        @Test
        void retorna401() {
            ResponseEntity<ErrorResponse> res = handler.handleAuth(new BadCredentialsException("bad creds"));
            assertThat(res.getStatusCode().value()).isEqualTo(401);
        }

        @DisplayName("Propaga el mensaje de la excepción de autenticación")
        @Test
        void propagaMensajeDeLaExcepcion() {
            ResponseEntity<ErrorResponse> res = handler.handleAuth(new BadCredentialsException("Invalid credentials"));
            assertThat(res.getBody().message()).isEqualTo("Invalid credentials");
            assertThat(res.getBody().error()).isEqualTo("UNAUTHORIZED");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // handleGeneric
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("handleGeneric")
    @Nested
    class HandleGeneric {

        @DisplayName("Produce HTTP 500")
        @Test
        void retorna500() {
            ResponseEntity<ErrorResponse> res = handler.handleGeneric(new RuntimeException("boom"));
            assertThat(res.getStatusCode().value()).isEqualTo(500);
        }

        @DisplayName("Mensaje genérico oculta detalles internos")
        @Test
        void mensajeGenericoOcultaDetallesInternos() {
            ResponseEntity<ErrorResponse> res = handler.handleGeneric(
                    new RuntimeException("NullPointerException en línea 42"));
            assertThat(res.getBody().message()).isEqualTo("Internal server error");
            assertThat(res.getBody().error()).isEqualTo("INTERNAL_ERROR");
            assertThat(res.getBody().status()).isEqualTo(500);
        }
    }
}
