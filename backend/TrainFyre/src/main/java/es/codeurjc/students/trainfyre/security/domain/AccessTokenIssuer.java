package es.codeurjc.students.trainfyre.security.domain;

import org.jmolecules.architecture.hexagonal.SecondaryPort;

@SecondaryPort
public interface AccessTokenIssuer {

    AccessToken issue(AuthenticatedUser user);
}
