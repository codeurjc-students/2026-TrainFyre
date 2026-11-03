package es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web;

import es.codeurjc.students.trainfyre.security.domain.AccessTokenIssuer;
import es.codeurjc.students.trainfyre.security.domain.AuthenticatedUser;
import es.codeurjc.students.trainfyre.security.domain.Role;
import es.codeurjc.students.trainfyre.security.infrastructure.adapter.in.jwt.AuthenticatedUserJwtConverter;
import es.codeurjc.students.trainfyre.security.infrastructure.adapter.out.jwt.NimbusAccessTokenIssuer;
import es.codeurjc.students.trainfyre.security.infrastructure.config.JwtConfiguration;
import es.codeurjc.students.trainfyre.security.infrastructure.config.SecurityConfiguration;
import es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web.request.UpdateUserRequest;
import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import es.codeurjc.students.trainfyre.users.application.service.CreateUserService;
import es.codeurjc.students.trainfyre.users.application.service.GetUserByIdService;
import es.codeurjc.students.trainfyre.users.application.service.UpdateUserService;
import es.codeurjc.students.trainfyre.users.application.service.DeleteUserService;
import es.codeurjc.students.trainfyre.users.domain.AlertSubscription;
import es.codeurjc.students.trainfyre.users.domain.User;
import es.codeurjc.students.trainfyre.users.domain.UserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@Import({
        CreateUserService.class,
        GetUserByIdService.class,
        UpdateUserService.class,
        DeleteUserService.class,
        UserExceptionHandler.class,
        SecurityConfiguration.class,
        JwtConfiguration.class,
        NimbusAccessTokenIssuer.class,
        AuthenticatedUserJwtConverter.class
})


class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private AccessTokenIssuer accessTokenIssuer;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserPort userPort;

    private String bearer(UUID userId) {
        var user = new AuthenticatedUser(userId, Set.of(Role.USER));
        return "Bearer " + accessTokenIssuer.issue(user).value();
    }

    @Test
    void createShouldSaveUserAndReturnItsIdUsingRealService() throws Exception {
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "pablo",
                                  "password": "test-password-123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID returnedId = objectMapper.readValue(response, UUID.class);

        var userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userPort).save(userCaptor.capture());

        var savedUser = userCaptor.getValue();

        assertThat(returnedId).isNotNull();
        assertThat(savedUser.getId()).isEqualTo(returnedId);
        assertThat(savedUser.getUsername()).isEqualTo("pablo");
        assertThat(passwordEncoder.matches("test-password-123", savedUser.getPassword())).isTrue();
        assertThat(savedUser.getRoles()).containsExactly(Role.USER);
        assertThat(savedUser.getVersion()).isNull();
        assertThat(savedUser.getAlertSubscriptions()).isEmpty();
    }

    @ParameterizedTest(name = "[{index}] subscriptions={0}")
    @ValueSource(ints = {0, 1, 2})
    void getByIdShouldReturnUserDetailsUsingRealService(int subscriptionCount) throws Exception {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        var start = ZonedDateTime.parse(
                "2026-10-19T08:00:00+02:00[Europe/Madrid]"
        );

        var subscriptions = new ArrayList<AlertSubscription>();

        for (int i = 0; i < subscriptionCount; i++) {
            subscriptions.add(new AlertSubscription(
                    Set.of((long) i + 1, (long) i + 10),
                    start.plusHours(i * 3L),
                    start.plusHours(i * 3L + 2),
                    EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                    start.plusMonths(i + 1L)
            ));
        }

        var user = User.reconstitute(
                id,
                3L,
                "pablo",
                "encoded-password",
                Set.of(Role.USER),
                subscriptions
        );

        when(userPort.findById(id, User.class)).thenReturn(user);

        String response = mockMvc.perform(get("/users/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(id))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.username").value("pablo"))
                .andExpect(jsonPath("$.alertSubscriptions").isArray())
                .andExpect(jsonPath("$.alertSubscriptions.length()")
                        .value(subscriptionCount))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.version").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var expectedDetails = new UserDetails("pablo", subscriptions);

        assertThat(objectMapper.readTree(response))
                .isEqualTo(objectMapper.readTree(
                        objectMapper.writeValueAsString(expectedDetails)
                ));

        verify(userPort).findById(id, User.class);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @ValueSource(strings = {
            "updated username",
            "updated password",
            "added subscriptions",
            "removed subscription",
            "removed all subscriptions",
            "combined changes",
            "updated username with empty subscription lists"
    })
    void updateShouldSaveExpectedChangesUsingRealService(
            String scenario
    ) throws Exception {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        var first = updateSubscription(1L);
        var second = updateSubscription(2L);
        var third = updateSubscription(3L);
        var fourth = updateSubscription(4L);

        var user = User.reconstitute(
                id,
                3L,
                "original-username",
                "original-password",
                Set.of(Role.USER),
                List.of(first, second)
        );

        String username = null;
        String password = null;
        List<AlertSubscription> subscriptionsToAdd = null;
        List<AlertSubscription> subscriptionsToDelete = null;

        String expectedUsername = "original-username";
        String expectedPassword = "original-password";
        List<AlertSubscription> expectedSubscriptions = List.of(first, second);

        switch (scenario) {
            case "updated username" -> {
                username = "updated-username";
                expectedUsername = username;
            }
            case "updated password" -> {
                password = "updated-password-123";
                expectedPassword = password;
            }
            case "added subscriptions" -> {
                subscriptionsToAdd = List.of(third, fourth);
                expectedSubscriptions = List.of(first, second, third, fourth);
            }
            case "removed subscription" -> {
                subscriptionsToDelete = List.of(first);
                expectedSubscriptions = List.of(second);
            }
            case "removed all subscriptions" -> {
                subscriptionsToDelete = List.of(first, second);
                expectedSubscriptions = List.of();
            }
            case "combined changes" -> {
                username = "updated-username";
                password = "updated-password-123";
                subscriptionsToAdd = List.of(third, fourth);
                subscriptionsToDelete = List.of(first);

                expectedUsername = username;
                expectedPassword = password;
                expectedSubscriptions = List.of(second, third, fourth);
            }
            case "updated username with empty subscription lists" -> {
                username = "updated-username";
                subscriptionsToAdd = List.of();
                subscriptionsToDelete = List.of();

                expectedUsername = username;
            }
            default -> throw new AssertionError(
                    "Unexpected scenario: " + scenario
            );
        }

        var request = new UpdateUserRequest(
                username,
                password,
                subscriptionsToAdd,
                subscriptionsToDelete
        );

        when(userPort.findById(id, User.class)).thenReturn(user);

        mockMvc.perform(put("/users/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        var userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userPort).findById(id, User.class);
        verify(userPort).save(userCaptor.capture());

        var savedUser = userCaptor.getValue();

        assertThat(savedUser.getId()).isEqualTo(id);
        assertThat(savedUser.getVersion()).isEqualTo(3L);
        assertThat(savedUser.getUsername()).isEqualTo(expectedUsername);
        if (password == null) {
            assertThat(savedUser.getPassword()).isEqualTo(expectedPassword);
        } else {
            assertThat(passwordEncoder.matches(password, savedUser.getPassword())).isTrue();
        }
        assertThat(savedUser.getAlertSubscriptions())
                .containsExactlyElementsOf(expectedSubscriptions);
    }

    private AlertSubscription updateSubscription(long lineId) {
        var start = ZonedDateTime.parse("2026-10-19T08:00:00Z");

        return new AlertSubscription(
                Set.of(lineId),
                start,
                start.plusHours(2),
                EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                start.plusMonths(1)
        );
    }

    @Test
    void deleteShouldDeleteUserAndReturnNoContentUsingRealService()
            throws Exception {
        UUID id = UUID.fromString(
                "123e4567-e89b-12d3-a456-426614174000"
        );

        mockMvc.perform(delete("/users/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(id)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userPort).delete(id);
    }

    @Test
    void requestsWithoutTokenAreUnauthorized() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/users/{id}", id))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userPort);
    }

    @Test
    void requestsWithMalformedTokenAreUnauthorized() throws Exception {
        mockMvc.perform(get("/users/{id}", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userPort);
    }

    @Test
    void userCannotReadAnotherUser() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();

        mockMvc.perform(get("/users/{id}", owner)
                        .header(HttpHeaders.AUTHORIZATION, bearer(attacker)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userPort);
    }

    @Test
    void userCannotUpdateAnotherUser() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();

        mockMvc.perform(put("/users/{id}", owner)
                        .header(HttpHeaders.AUTHORIZATION, bearer(attacker))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "hijacked-name"
                                }
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userPort);
    }

    @Test
    void userCannotDeleteAnotherUser() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID attacker = UUID.randomUUID();

        mockMvc.perform(delete("/users/{id}", owner)
                        .header(HttpHeaders.AUTHORIZATION, bearer(attacker)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userPort);
    }

    @Test
    void updateWithoutModificationsIsABadRequest() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/users/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userPort);
    }

    @Test
    void createRejectsWeakPassword() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "pablo",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userPort);
    }

    @ParameterizedTest(name = "[{index}] authenticatedUserId={0}")
    @ValueSource(strings = {
            "123e4567-e89b-12d3-a456-426614174000",
            "123e4567-e89b-12d3-a456-426614174001"
    })
    void updateMeShouldUpdateAuthenticatedUserUsingRealService(
            String authenticatedUserId
    ) throws Exception {
        UUID id = UUID.fromString(authenticatedUserId);

        var subscription = updateSubscription(1L);

        var user = User.reconstitute(
                id,
                3L,
                "original-username",
                "original-password",
                Set.of(Role.USER),
                List.of(subscription)
        );

        var request = new UpdateUserRequest(
                "updated-username",
                null,
                null,
                null
        );

        when(userPort.findById(id, User.class)).thenReturn(user);

        mockMvc.perform(put("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        var userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userPort).findById(id, User.class);
        verify(userPort).save(userCaptor.capture());

        var savedUser = userCaptor.getValue();

        assertThat(savedUser.getId()).isEqualTo(id);
        assertThat(savedUser.getVersion()).isEqualTo(3L);
        assertThat(savedUser.getUsername()).isEqualTo("updated-username");
        assertThat(savedUser.getPassword()).isEqualTo("original-password");
        assertThat(savedUser.getRoles()).containsExactly(Role.USER);
        assertThat(savedUser.getAlertSubscriptions())
                .containsExactly(subscription);
    }

    @Test
    void updateMeWithoutTokenIsUnauthorized() throws Exception {
        var request = new UpdateUserRequest(
                "updated-username",
                null,
                null,
                null
        );

        mockMvc.perform(put("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userPort);
    }

    @Test
    void updateMeWithMalformedTokenIsUnauthorized() throws Exception {
        var request = new UpdateUserRequest(
                "updated-username",
                null,
                null,
                null
        );

        mockMvc.perform(put("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userPort);
    }

    @Test
    void updateMeWithoutModificationsIsABadRequest() throws Exception {
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        mockMvc.perform(put("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userPort);
    }


    @ParameterizedTest(name = "[{index}] authenticatedUserId={0}")
    @ValueSource(strings = {
            "123e4567-e89b-12d3-a456-426614174000",
            "123e4567-e89b-12d3-a456-426614174001"
    })
    void getMeShouldReturnAuthenticatedUserDetailsUsingRealService(
            String authenticatedUserId
    ) throws Exception {
        UUID id = UUID.fromString(authenticatedUserId);

        var subscriptions = List.of(updateSubscription(1L));

        var user = User.reconstitute(
                id,
                3L,
                "pablo",
                "encoded-password",
                Set.of(Role.USER),
                subscriptions
        );

        when(userPort.findById(id, User.class)).thenReturn(user);

        String response = mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(id))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.username").value("pablo"))
                .andExpect(jsonPath("$.alertSubscriptions").isArray())
                .andExpect(jsonPath("$.alertSubscriptions.length()").value(1))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.roles").doesNotExist())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.version").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var expectedDetails = new UserDetails("pablo", subscriptions);

        assertThat(objectMapper.readTree(response))
                .isEqualTo(objectMapper.readTree(
                        objectMapper.writeValueAsString(expectedDetails)
                ));

        verify(userPort).findById(id, User.class);
    }

    @Test
    void getMeWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/users/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userPort);
    }

    @Test
    void getMeWithMalformedTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userPort);
    }


}
