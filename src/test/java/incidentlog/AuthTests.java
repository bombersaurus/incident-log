package incidentlog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class AuthTests extends TestBase {

    @Test
    void pagesNeedALogin() throws Exception {
        mvc.perform(get("/")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/incidents/new")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/audit")).andExpect(redirectedUrl("/login"));
    }

    @Test
    void wrongPasswordIsRefused() throws Exception {
        String html = mvc.perform(post("/login")
                        .param("username", "alex")
                        .param("password", "not-the-password"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        assertThat(html).contains("Wrong username or password.");
    }

    @Test
    void unknownUserGetsTheSameMessage() throws Exception {
        String html = mvc.perform(post("/login")
                        .param("username", "nobody")
                        .param("password", "password"))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        assertThat(html).contains("Wrong username or password.");
    }

    @Test
    void loginThenLogout() throws Exception {
        MockHttpSession session = login("alex");
        mvc.perform(get("/").session(session)).andExpect(status().isOk());

        mvc.perform(post("/logout").session(session)).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/").session(session)).andExpect(redirectedUrl("/login"));
    }
}
