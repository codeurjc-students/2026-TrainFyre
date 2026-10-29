package es.codeurjc.students.trainfyre.users.infrastructure.adapter.in.web;

import es.codeurjc.students.trainfyre.users.application.port.out.UserPort;
import es.codeurjc.students.trainfyre.users.application.service.CreateUserService;
import es.codeurjc.students.trainfyre.users.domain.User;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(CreateUserService.class)
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
}
