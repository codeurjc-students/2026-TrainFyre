package es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web;

import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import es.codeurjc.students.trainfyre.users.application.service.CreateUserService;
import es.codeurjc.students.trainfyre.users.application.service.GetUserByIdService;
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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
@Import({
        CreateUserService.class,
        GetUserByIdService.class
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

}
