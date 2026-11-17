package incidentlog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Shared setup for the tests. Every test starts with an empty in-memory
 * database holding three users: alex and priya (staff) and sam (supervisor).
 * MockMvc sends requests to the app without starting a real web server.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:tests;DB_CLOSE_DELAY=-1",
        "app.seed=false"})
abstract class TestBase {

    // Hashing is slow on purpose, so do it once and reuse it for every test.
    private static final String PASSWORD_HASH = new BCryptPasswordEncoder().encode("password");

    @Autowired
    private WebApplicationContext context;

    @Autowired
    protected JdbcTemplate jdbc;

    protected MockMvc mvc;

    @BeforeEach
    void startWithAnEmptyDatabase() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        jdbc.update("DELETE FROM audit_log");
        jdbc.update("DELETE FROM incidents");
        jdbc.update("DELETE FROM users");
        addUser("alex", "staff");
        addUser("priya", "staff");
        addUser("sam", "supervisor");
    }

    private void addUser(String username, String role) {
        jdbc.update("INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)",
                username, PASSWORD_HASH, role);
    }

    /** Logs in and returns the session to send with later requests. */
    protected MockHttpSession login(String username) throws Exception {
        MvcResult result = mvc.perform(post("/login")
                        .param("username", username)
                        .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    /**
     * A filled-in incident form, ready to post to the given address. Pass
     * pairs of field name and value to change fields, for example
     * incidentForm("/incidents/new", session, "severity", "4").
     */
    protected MockHttpServletRequestBuilder incidentForm(String url, MockHttpSession session,
                                                         String... changes) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("occurredAt", "2026-11-14T21:30");
        fields.put("location", "North entrance");
        fields.put("category", "Refused entry");
        fields.put("severity", "2");
        fields.put("description", "Guest refused entry, no ticket.");
        fields.put("bodycamRef", "");
        for (int i = 0; i < changes.length; i += 2) {
            fields.put(changes[i], changes[i + 1]);
        }
        MockHttpServletRequestBuilder request = post(url).session(session);
        fields.forEach((name, value) -> request.param(name, value));
        return request;
    }

    /** Saves a new incident and returns its id. */
    protected long makeIncident(MockHttpSession session, String... changes) throws Exception {
        MvcResult result = mvc.perform(incidentForm("/incidents/new", session, changes))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        String address = result.getResponse().getRedirectedUrl(); // such as /incidents/7
        return Long.parseLong(address.substring(address.lastIndexOf('/') + 1));
    }

    /** Opens a page, checks it loaded, and returns its HTML. */
    protected String page(MockHttpSession session, String url) throws Exception {
        return mvc.perform(get(url).session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    protected int count(String sql) {
        return jdbc.queryForObject(sql, Integer.class);
    }
}
