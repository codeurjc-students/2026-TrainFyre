package es.codeurjc.students.trainfyre.users.domain;

import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAccessPolicyTest {

    @Test
    void ownerCanManageOwnUser() {
        UUID id = UUID.randomUUID();
        var requester = new AuthenticatedUser(id, Set.of(Role.USER));

        assertThatCode(() -> UserAccessPolicy.assertCanManage(requester, id))
                .doesNotThrowAnyException();
    }

    @Test
    void userCannotManageAnotherUser() {
        var requester = new AuthenticatedUser(UUID.randomUUID(), Set.of(Role.USER));

        assertThatThrownBy(() -> UserAccessPolicy.assertCanManage(requester, UUID.randomUUID()))
                .isInstanceOf(UserAccessDeniedException.class);
    }
}
