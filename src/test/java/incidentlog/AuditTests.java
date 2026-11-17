package incidentlog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class AuditTests extends TestBase {

    @Test
    void auditPageIsForSupervisorsOnly() throws Exception {
        MockHttpSession alex = login("alex");
        MockHttpSession sam = login("sam");

        mvc.perform(get("/audit").session(alex)).andExpect(status().isForbidden());
        mvc.perform(get("/audit").session(sam)).andExpect(status().isOk());
    }

    @Test
    void anEditIsLoggedWithBeforeAndAfter() throws Exception {
        MockHttpSession alex = login("alex");
        long id = makeIncident(alex, "severity", "2");

        mvc.perform(incidentForm("/incidents/" + id + "/edit", alex, "severity", "4"))
                .andExpect(status().is3xxRedirection());

        // Exactly one row: only the severity changed, from 2 to 4.
        assertThat(count("SELECT COUNT(*) FROM audit_log")).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM audit_log WHERE field = 'severity'"
                + " AND old_value = '2' AND new_value = '4'")).isEqualTo(1);

        String html = page(alex, "/incidents/" + id);
        assertThat(html).contains("<td>severity</td>");
    }
}
