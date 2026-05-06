package com.versus.api.auth;

import com.versus.api.auth.domain.RefreshToken;
import com.versus.api.auth.dto.AuthResponse;
import com.versus.api.auth.dto.RegisterRequest;
import com.versus.api.auth.repo.RefreshTokenRepository;
import com.versus.api.common.exception.ApiException;
import com.versus.api.common.exception.ErrorCode;
import com.versus.api.users.Role;
import com.versus.api.users.domain.User;
import com.versus.api.users.repo.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AuthService")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UserRepository users;
    @Mock
    RefreshTokenRepository refreshTokens;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    JwtService jwt;

    @InjectMocks
    AuthService authService;

    @DisplayName("Registro de usuario")
    @Nested
    class Register {

        private static final String USERNAME = "player1";
        private static final String EMAIL = "player1@versus.com";
        private static final String PASSWORD = "Segura123!";
        private static final String HASHED_PW = "$2a$10$mockedhash";
        private static final String ACCESS_TOKEN = "mocked.access.token";
        private static final String REFRESH_TOKEN = "mocked.refresh.token";
        private static final String REFRESH_HASH = "mockedrefreshhash";

        private RegisterRequest validRequest() {
            return new RegisterRequest(USERNAME, EMAIL, PASSWORD);
        }

        private void stubHappyPath() {
            when(users.existsByEmail(EMAIL)).thenReturn(false);
            when(users.existsByUsername(USERNAME)).thenReturn(false);
            when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PW);
            when(users.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(UUID.randomUUID());
                return u;
            });
            when(jwt.generateAccessToken(any())).thenReturn(ACCESS_TOKEN);
            when(jwt.generateRefreshToken(any())).thenReturn(REFRESH_TOKEN);
            when(jwt.hash(REFRESH_TOKEN)).thenReturn(REFRESH_HASH);
            when(jwt.getRefreshExpiry()).thenReturn(604800L);
        }

        @DisplayName("Camino feliz: registro exitoso con datos válidos")
        @Test
        void caminoFeliz_devuelveAuthResponseCompleta() {
            stubHappyPath();

            AuthResponse res = authService.register(validRequest());

            assertThat(res.accessToken()).isEqualTo(ACCESS_TOKEN);
            assertThat(res.refreshToken()).isEqualTo(REFRESH_TOKEN);
            assertThat(res.user().username()).isEqualTo(USERNAME);
            assertThat(res.user().role()).isEqualTo("PLAYER");
            assertThat(res.user().id()).isNotNull();
        }

        @DisplayName("Registro con email duplicado lanza conflicto")
        @Test
        void emailDuplicado_lanzaConflict() {
            when(users.existsByEmail(EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRequest()))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode())
                            .isEqualTo(ErrorCode.CONFLICT));
        }

        @DisplayName("Registro con username duplicado lanza conflicto")
        @Test
        void usernameDuplicado_lanzaConflict() {
            when(users.existsByEmail(EMAIL)).thenReturn(false);
            when(users.existsByUsername(USERNAME)).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRequest()))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode())
                            .isEqualTo(ErrorCode.CONFLICT));
        }

        @DisplayName("El refresh token se persiste con los campos correctos")
        @Test
        void caminoFeliz_persisteRefreshTokenConCamposCorrectos() {
            stubHappyPath();

            authService.register(validRequest());

            ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
            verify(refreshTokens).save(captor.capture());

            RefreshToken saved = captor.getValue();
            assertThat(saved.getRevoked()).isFalse();
            assertThat(saved.getTokenHash()).isEqualTo(REFRESH_HASH);
            assertThat(saved.getUserId()).isNotNull();
            assertThat(saved.getExpiresAt()).isAfter(Instant.now());
        }

        @DisplayName("El usuario guardado tiene role PLAYER")
        @Test
        void usuarioGuardado_tieneRolePlayer() {
            stubHappyPath();

            authService.register(validRequest());

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(users).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo(Role.PLAYER);
        }

        @DisplayName("El usuario guardado tiene isActive true")
        @Test
        void usuarioGuardado_tieneIsActiveTrue() {
            stubHappyPath();

            authService.register(validRequest());

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(users).save(captor.capture());
            assertThat(captor.getValue().getIsActive()).isTrue();
        }

        @DisplayName("La contraseña se guarda hasheada, no en plano")
        @Test
        void contrasena_seGuardaHasheadaNoEnPlano() {
            stubHappyPath();

            authService.register(validRequest());

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(users).save(captor.capture());
            assertThat(captor.getValue().getPasswordHash())
                    .isEqualTo(HASHED_PW)
                    .isNotEqualTo(PASSWORD);
        }

        @DisplayName("Usuario sin avatar tiene avatarUrl null sin lanzar excepción")
        @Test
        void usuarioSinAvatar_avatarUrlEsNullSinExcepcion() {
            stubHappyPath();

            AuthResponse res = authService.register(validRequest());

            assertThat(res.user().avatarUrl()).isNull();
        }

        @DisplayName("Si email y username ambos están duplicados, se lanza conflicto por email primero")
        @Test
        void emailYUsernameAmbosDuplicados_lanzaConflictPorEmailPrimero() {
            when(users.existsByEmail(EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRequest()))
                    .isInstanceOf(ApiException.class)
                    .satisfies(ex -> assertThat(((ApiException) ex).getCode())
                            .isEqualTo(ErrorCode.CONFLICT));

            verify(users, never()).existsByUsername(any());
        }

        @DisplayName("Los tokens de acceso y refresh son distintos")
        @Test
        void tokens_accessYRefreshSonDistintos() {
            stubHappyPath();

            AuthResponse res = authService.register(validRequest());

            assertThat(res.accessToken()).isNotEqualTo(res.refreshToken());
        }
    }

    class Login {

    }

}
