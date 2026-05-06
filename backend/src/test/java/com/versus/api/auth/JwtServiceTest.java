package com.versus.api.auth;

import com.versus.api.users.Role;
import com.versus.api.users.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtService")
class JwtServiceTest {

    // Secreto válido: >= 32 bytes (256 bits) requerido por HMAC-SHA256
    private static final String SECRET         = "test-secret-key-para-versus-api-junit-tests!!";
    private static final String SECRET_OTHER   = "other-secret-key-para-versus-api-junit-tests!!";
    private static final long   ACCESS_EXPIRY  = 900L;
    private static final long   REFRESH_EXPIRY = 604800L;

    private static final UUID   USER_ID   = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final String USERNAME  = "testplayer";
    private static final Role   ROLE      = Role.PLAYER;

    private final JwtService jwtService     = new JwtService(SECRET, ACCESS_EXPIRY, REFRESH_EXPIRY);
    private final JwtService otherService   = new JwtService(SECRET_OTHER, ACCESS_EXPIRY, REFRESH_EXPIRY);

    private User testUser() {
        return User.builder()
                .id(USER_ID).username(USERNAME).email("test@versus.com")
                .passwordHash("$2a$hash").role(ROLE).isActive(true)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("Constructor")
    @Nested
    class Constructor {

        @DisplayName("Secreto demasiado corto lanza WeakKeyException")
        @Test
        void secretoCorto_lanzaWeakKeyException() {
            // HMAC-SHA256 requiere mínimo 32 bytes; con menos JJWT lanza WeakKeyException
            assertThatThrownBy(() -> new JwtService("cortito", ACCESS_EXPIRY, REFRESH_EXPIRY))
                    .isInstanceOf(io.jsonwebtoken.security.WeakKeyException.class);
        }

        @DisplayName("Secreto de exactamente 32 bytes construye sin error")
        @Test
        void secreto32Bytes_construyeCorrectamente() {
            assertThatCode(() -> new JwtService("12345678901234567890123456789012", ACCESS_EXPIRY, REFRESH_EXPIRY))
                    .doesNotThrowAnyException();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // generateAccessToken
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("generateAccessToken")
    @Nested
    class GenerateAccessToken {

        @DisplayName("El subject es el userId del usuario")
        @Test
        void subject_esElUserIdDelUsuario() {
            String token = jwtService.generateAccessToken(testUser());
            Claims claims = jwtService.parse(token);
            assertThat(claims.getSubject()).isEqualTo(USER_ID.toString());
        }

        @DisplayName("Contiene el claim role correcto")
        @Test
        void contieneClaimRole() {
            String token = jwtService.generateAccessToken(testUser());
            assertThat(jwtService.parse(token).get("role", String.class)).isEqualTo(ROLE.name());
        }

        @DisplayName("Contiene el claim username correcto")
        @Test
        void contieneClaimUsername() {
            String token = jwtService.generateAccessToken(testUser());
            assertThat(jwtService.parse(token).get("username", String.class)).isEqualTo(USERNAME);
        }

        @DisplayName("Contiene claim type=access")
        @Test
        void contieneClaimTypeAccess() {
            String token = jwtService.generateAccessToken(testUser());
            assertThat(jwtService.parse(token).get("type", String.class)).isEqualTo("access");
        }

        @DisplayName("La expiración está a accessExpiry segundos en el futuro")
        @Test
        void expiracion_estaEnElFuturo() {
            Instant antes = Instant.now();
            String token = jwtService.generateAccessToken(testUser());
            Date exp = jwtService.parse(token).getExpiration();
            assertThat(exp.toInstant()).isAfter(antes.plusSeconds(ACCESS_EXPIRY - 2))
                                       .isBefore(antes.plusSeconds(ACCESS_EXPIRY + 2));
        }

        @DisplayName("Token no es nulo ni vacío")
        @Test
        void token_noEsNuloNiVacio() {
            assertThat(jwtService.generateAccessToken(testUser())).isNotBlank();
        }

        @DisplayName("Usuario con id=null lanza NullPointerException")
        @Test
        void usuarioConIdNull_lanzaNPE() {
            User sinId = User.builder().id(null).username(USERNAME)
                    .role(ROLE).isActive(true).build();
            assertThatThrownBy(() -> jwtService.generateAccessToken(sinId))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // generateRefreshToken
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("generateRefreshToken")
    @Nested
    class GenerateRefreshToken {

        @DisplayName("El subject es el userId del usuario")
        @Test
        void subject_esElUserIdDelUsuario() {
            String token = jwtService.generateRefreshToken(testUser());
            assertThat(jwtService.parse(token).getSubject()).isEqualTo(USER_ID.toString());
        }

        @DisplayName("Contiene claim type=refresh")
        @Test
        void contieneClaimTypeRefresh() {
            String token = jwtService.generateRefreshToken(testUser());
            assertThat(jwtService.parse(token).get("type", String.class)).isEqualTo("refresh");
        }

        @DisplayName("No contiene claim role (privacidad)")
        @Test
        void noContieneClaimRole() {
            String token = jwtService.generateRefreshToken(testUser());
            assertThat(jwtService.parse(token).get("role", String.class)).isNull();
        }

        @DisplayName("No contiene claim username (privacidad)")
        @Test
        void noContieneClaimUsername() {
            String token = jwtService.generateRefreshToken(testUser());
            assertThat(jwtService.parse(token).get("username", String.class)).isNull();
        }

        @DisplayName("Dos llamadas consecutivas producen tokens distintos (jti aleatorio)")
        @Test
        void dosLlamadas_producenTokensDistintos() {
            User user = testUser();
            String t1 = jwtService.generateRefreshToken(user);
            String t2 = jwtService.generateRefreshToken(user);
            assertThat(t1).isNotEqualTo(t2);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // validate
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("validate")
    @Nested
    class Validate {

        @DisplayName("Token de acceso válido retorna true")
        @Test
        void tokenAcceso_valido_retornaTrue() {
            assertThat(jwtService.validate(jwtService.generateAccessToken(testUser()))).isTrue();
        }

        @DisplayName("Token de refresco válido retorna true")
        @Test
        void tokenRefresh_valido_retornaTrue() {
            assertThat(jwtService.validate(jwtService.generateRefreshToken(testUser()))).isTrue();
        }

        @DisplayName("Token expirado retorna false sin lanzar excepción")
        @Test
        void tokenExpirado_retornaFalseSinExcepcion() {
            JwtService expirado = new JwtService(SECRET, -10L, REFRESH_EXPIRY);
            String token = expirado.generateAccessToken(testUser());
            assertThat(jwtService.validate(token)).isFalse();
        }

        @DisplayName("Token firmado con otra clave retorna false sin lanzar excepción")
        @Test
        void tokenOtraClave_retornaFalseSinExcepcion() {
            String foreign = otherService.generateAccessToken(testUser());
            assertThat(jwtService.validate(foreign)).isFalse();
        }

        @DisplayName("Cadena basura retorna false sin lanzar excepción")
        @Test
        void cadenaBasura_retornaFalseSinExcepcion() {
            assertThat(jwtService.validate("esto.no.es.un.jwt")).isFalse();
        }

        @DisplayName("null retorna false sin lanzar excepción")
        @Test
        void tokenNull_retornaFalseSinExcepcion() {
            assertThat(jwtService.validate(null)).isFalse();
        }

        @DisplayName("Cadena vacía retorna false sin lanzar excepción")
        @Test
        void cadenaVacia_retornaFalseSinExcepcion() {
            assertThat(jwtService.validate("")).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // parse
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("parse")
    @Nested
    class Parse {

        @DisplayName("Token válido retorna Claims con el subject correcto")
        @Test
        void tokenValido_retornaClaimsConSubjectCorrecto() {
            String token = jwtService.generateAccessToken(testUser());
            Claims claims = jwtService.parse(token);
            assertThat(claims.getSubject()).isEqualTo(USER_ID.toString());
        }

        @DisplayName("Token firmado con otra clave lanza JwtException")
        @Test
        void tokenOtraClave_lanzaJwtException() {
            String foreign = otherService.generateAccessToken(testUser());
            assertThatThrownBy(() -> jwtService.parse(foreign))
                    .isInstanceOf(JwtException.class);
        }

        @DisplayName("Token expirado lanza JwtException")
        @Test
        void tokenExpirado_lanzaJwtException() {
            JwtService expirado = new JwtService(SECRET, -10L, REFRESH_EXPIRY);
            String token = expirado.generateAccessToken(testUser());
            assertThatThrownBy(() -> jwtService.parse(token))
                    .isInstanceOf(JwtException.class);
        }

        @DisplayName("null lanza IllegalArgumentException")
        @Test
        void tokenNull_lanzaIllegalArgumentException() {
            assertThatThrownBy(() -> jwtService.parse(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // extractUserId
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("extractUserId")
    @Nested
    class ExtractUserId {

        @DisplayName("Token de acceso retorna el UUID correcto")
        @Test
        void tokenAcceso_retornaUuidCorrecto() {
            String token = jwtService.generateAccessToken(testUser());
            assertThat(jwtService.extractUserId(token)).isEqualTo(USER_ID);
        }

        @DisplayName("Token de refresco retorna el UUID correcto")
        @Test
        void tokenRefresh_retornaUuidCorrecto() {
            String token = jwtService.generateRefreshToken(testUser());
            assertThat(jwtService.extractUserId(token)).isEqualTo(USER_ID);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // extractRole
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("extractRole")
    @Nested
    class ExtractRole {

        @DisplayName("Token de acceso retorna el rol correcto")
        @Test
        void tokenAcceso_retornaRolCorrecto() {
            String token = jwtService.generateAccessToken(testUser());
            assertThat(jwtService.extractRole(token)).isEqualTo(ROLE.name());
        }

        @DisplayName("Token de refresco retorna null (sin claim role)")
        @Test
        void tokenRefresh_retornaNull() {
            // Los refresh tokens no incluyen el role por diseño.
            // Los llamadores de extractRole deben manejar null para evitar NPE.
            String token = jwtService.generateRefreshToken(testUser());
            assertThat(jwtService.extractRole(token)).isNull();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // hash
    // ═══════════════════════════════════════════════════════════════════════

    @DisplayName("hash")
    @Nested
    class Hash {

        @DisplayName("Mismo input produce siempre el mismo hash (determinismo)")
        @Test
        void mismoInput_mismoHash() {
            assertThat(jwtService.hash("cualquier.token"))
                    .isEqualTo(jwtService.hash("cualquier.token"));
        }

        @DisplayName("Inputs distintos producen hashes distintos")
        @Test
        void inputsDistintos_hashesDistintos() {
            assertThat(jwtService.hash("token.a")).isNotEqualTo(jwtService.hash("token.b"));
        }

        @DisplayName("El hash tiene exactamente 64 caracteres hexadecimales (SHA-256)")
        @Test
        void output_es64HexChars() {
            String h = jwtService.hash("cualquier.token");
            assertThat(h).hasSize(64).matches("[a-f0-9]+");
        }

        @DisplayName("Input vacío produce un hash válido sin lanzar excepción")
        @Test
        void inputVacio_producirHashSinExcepcion() {
            assertThatCode(() -> jwtService.hash("")).doesNotThrowAnyException();
            assertThat(jwtService.hash("")).hasSize(64);
        }

        @DisplayName("Input null lanza IllegalStateException con causa NPE (mensaje engañoso)")
        @Test
        void inputNull_lanzaIllegalStateException() {
            // BUG: el catch genérico de hash() envuelve la NPE en IllegalStateException
            // con mensaje "SHA-256 unavailable", que es incorrecto; la causa real es null input.
            assertThatThrownBy(() -> jwtService.hash(null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("SHA-256 unavailable")
                    .hasCauseInstanceOf(NullPointerException.class);
        }
    }
}
