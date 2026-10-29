package es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web;

import es.codeurjc.students.trainfyre.users.application.port.in.command.UpdateUserCommand;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@Import({
        CreateUserService.class,
        GetUserByIdService.class,
        UpdateUserService.class,
        DeleteUserService.class
})


class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private UserPort userPort;

    @Test
    void createShouldSaveUserAndReturnItsIdUsingRealService() throws Exception {
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "pablo",
                                  "password": "test-password"
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
        assertThat(savedUser.getPassword()).isEqualTo("test-password");
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
                subscriptions
        );

        when(userPort.findById(id, User.class)).thenReturn(user);

        String response = mockMvc.perform(get("/users/{id}", id)
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
                password = "updated-password";
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
                password = "updated-password";
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

        var command = new UpdateUserCommand(
                id,
                username,
                password,
                subscriptionsToAdd,
                subscriptionsToDelete
        );

        when(userPort.findById(id, User.class)).thenReturn(user);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        var userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userPort).findById(id, User.class);
        verify(userPort).save(userCaptor.capture());

        var savedUser = userCaptor.getValue();

        assertThat(savedUser.getId()).isEqualTo(id);
        assertThat(savedUser.getVersion()).isEqualTo(3L);
        assertThat(savedUser.getUsername()).isEqualTo(expectedUsername);
        assertThat(savedUser.getPassword()).isEqualTo(expectedPassword);
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

        mockMvc.perform(delete("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "id": "%s"
                            }
                            """.formatted(id)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(userPort).delete(id);
    }



}
