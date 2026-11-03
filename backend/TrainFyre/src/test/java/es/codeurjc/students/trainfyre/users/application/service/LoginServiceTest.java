package es.codeurjc.students.trainfyre.users.application.service;

import es.codeurjc.students.trainfyre.security.domain.AccessToken;
import es.codeurjc.students.trainfyre.security.domain.AccessTokenIssuer;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import es.codeurjc.students.trainfyre.users.application.port.in.command.LoginCommand;
import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import es.codeurjc.students.trainfyre.users.domain.InvalidCredentialsException;
import es.codeurjc.students.trainfyre.users.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    private static final String DUMMY_HASH = "dummy-hash";

    @Mock
    private UserPort userPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccessTokenIssuer accessTokenIssuer;

    private LoginService service;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(any())).thenReturn(DUMMY_HASH);
        service = new LoginService(userPort, passwordEncoder, accessTokenIssuer);
    }

    @Test
    void issuesTokenForValidCredentials() {
        UUID id = UUID.randomUUID();
        User user = User.reconstitute(id, 0L, "alice", "stored-hash", Set.of(Role.USER), List.of());
        AccessToken token = new AccessToken("jwt", Duration.ofMinutes(15));

        when(userPort.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", "stored-hash")).thenReturn(true);
        when(accessTokenIssuer.issue(new AuthenticatedUser(id, Set.of(Role.USER)))).thenReturn(token);

        assertThat(service.execute(new LoginCommand("alice", "correct-password"))).isEqualTo(token);
    }

    @Test
    void rejectsWrongPassword() {
        User user = User.reconstitute(UUID.randomUUID(), 0L, "alice", "stored-hash", Set.of(Role.USER), List.of());

        when(userPort.findByUsername("alice")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "stored-hash")).thenReturn(false);

        assertThatThrownBy(() -> service.execute(new LoginCommand("alice", "wrong-password")))
                .isInstanceOf(InvalidCredentialsException.class);

        verifyNoInteractions(accessTokenIssuer);
    }

    @Test
    void rejectsUnknownUserAfterHashingAgainstDummyHash() {
        when(userPort.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(new LoginCommand("ghost", "any-password")))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(passwordEncoder).matches("any-password", DUMMY_HASH);
        verifyNoInteractions(accessTokenIssuer);
    }
}
