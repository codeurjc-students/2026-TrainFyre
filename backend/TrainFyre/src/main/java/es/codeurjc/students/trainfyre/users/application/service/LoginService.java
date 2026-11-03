package es.codeurjc.students.trainfyre.users.application.service;

import es.codeurjc.students.trainfyre.security.domain.AccessToken;
import es.codeurjc.students.trainfyre.security.domain.AccessTokenIssuer;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.users.application.port.in.LoginUseCase;
import es.codeurjc.students.trainfyre.users.application.port.in.command.LoginCommand;
import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import es.codeurjc.students.trainfyre.users.domain.InvalidCredentialsException;
import es.codeurjc.students.trainfyre.users.domain.User;
import org.jmolecules.architecture.cqrs.CommandHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class LoginService implements LoginUseCase {

    private final UserPort userPort;
    private final PasswordEncoder passwordEncoder;
    private final AccessTokenIssuer accessTokenIssuer;
    private final String unknownUserPasswordHash;

    public LoginService(UserPort userPort, PasswordEncoder passwordEncoder, AccessTokenIssuer accessTokenIssuer) {
        this.userPort = userPort;
        this.passwordEncoder = passwordEncoder;
        this.accessTokenIssuer = accessTokenIssuer;
        this.unknownUserPasswordHash = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @CommandHandler
    @Override
    public AccessToken execute(LoginCommand input) {

        Optional<User> candidate = userPort.findByUsername(input.username());

        String storedHash = candidate.map(User::getPassword).orElse(unknownUserPasswordHash);
        boolean passwordMatches = passwordEncoder.matches(input.password(), storedHash);

        User user = candidate
                .filter(found -> passwordMatches)
                .orElseThrow(InvalidCredentialsException::new);

        return accessTokenIssuer.issue(new AuthenticatedUser(user.getId(), user.getRoles()));
    }
}
