package incidentlog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class IncidentTests extends TestBase {

    @Test
    void createAndView() throws Exception {
        MockHttpSession alex = login("alex");
        long id = makeIncident(alex, "location", "Service yard");

        String html = page(alex, "/incidents/" + id);
        assertThat(html).contains("Service yard");
        assertThat(html).contains("14/11/2026 21:30");
    }

    @Test
    void staffOnlySeeTheirOwnInTheList() throws Exception {
        MockHttpSession alex = login("alex");
        MockHttpSession priya = login("priya");
        makeIncident(alex, "location", "Service yard");
        makeIncident(priya, "location", "Box office");

        String html = page(priya, "/");
        assertThat(html).contains("Box office");
        assertThat(html).doesNotContain("Service yard");
    }

    @Test
    void supervisorSeesEverything() throws Exception {
        MockHttpSession alex = login("alex");
        MockHttpSession sam = login("sam");
        long id = makeIncident(alex, "location", "Service yard");

        assertThat(page(sam, "/")).contains("Service yard");
        assertThat(page(sam, "/incidents/" + id)).contains("Service yard");
    }

    @Test
    void newestIncidentIsListedFirst() throws Exception {
        MockHttpSession alex = login("alex");
        makeIncident(alex, "occurredAt", "2026-11-15T20:00", "location", "Level 2 concourse");
        makeIncident(alex, "occurredAt", "2026-12-02T20:00", "location", "Service yard");
        makeIncident(alex, "occurredAt", "2026-10-28T20:00", "location", "Box office");

        String html = page(alex, "/");
        assertThat(html.indexOf("Service yard")).isLessThan(html.indexOf("Level 2 concourse"));
        assertThat(html.indexOf("Level 2 concourse")).isLessThan(html.indexOf("Box office"));
    }

    @Test
    void searchFindsMatchingIncidents() throws Exception {
        MockHttpSession alex = login("alex");
        makeIncident(alex, "location", "Service yard", "description", "Van blocking the gate.");
        makeIncident(alex, "location", "Box office", "description", "Lost phone.");

        String html = page(alex, "/?q=GATE");
        assertThat(html).contains("Service yard");
        assertThat(html).doesNotContain("Box office");
    }

    @Test
    void editChangesTheIncident() throws Exception {
        MockHttpSession alex = login("alex");
        long id = makeIncident(alex, "location", "Service yard");

        mvc.perform(incidentForm("/incidents/" + id + "/edit", alex, "location", "Loading bay 3"))
                .andExpect(status().is3xxRedirection());

        assertThat(page(alex, "/incidents/" + id)).contains("Loading bay 3");
    }

    @Test
    void badInputIsRejected() throws Exception {
        MockHttpSession alex = login("alex");

        mvc.perform(incidentForm("/incidents/new", alex, "severity", "9"))
                .andExpect(status().isBadRequest());
        mvc.perform(incidentForm("/incidents/new", alex, "description", "   "))
                .andExpect(status().isBadRequest());

        assertThat(count("SELECT COUNT(*) FROM incidents")).isZero();
    }

    @Test
    void descriptionIsShownAsTextNotHtml() throws Exception {
        MockHttpSession alex = login("alex");
        long id = makeIncident(alex, "description", "<script>alert(1)</script>");

        String html = page(alex, "/incidents/" + id);
        assertThat(html).doesNotContain("<script>alert(1)</script>");
        assertThat(html).contains("&lt;script&gt;");
    }

    @Test
    void staffCannotOpenSomeoneElsesIncident() throws Exception {
        MockHttpSession alex = login("alex");
        MockHttpSession priya = login("priya");
        long id = makeIncident(alex, "location", "Service yard");

        mvc.perform(get("/incidents/" + id).session(priya))
                .andExpect(status().isForbidden());
        mvc.perform(get("/incidents/" + id + "/edit").session(priya))
                .andExpect(status().isForbidden());
        mvc.perform(incidentForm("/incidents/" + id + "/edit", priya, "location", "Changed"))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchCopesWithAnApostrophe() throws Exception {
        MockHttpSession alex = login("alex");
        makeIncident(alex, "location", "Service yard",
                "description", "Driver couldn't find the loading bay.");

        String html = mvc.perform(get("/").session(alex).param("q", "couldn't"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(html).contains("Service yard");
    }

    @Test
    void searchCannotShowOtherPeoplesIncidents() throws Exception {
        MockHttpSession alex = login("alex");
        MockHttpSession priya = login("priya");
        makeIncident(alex, "location", "Service yard");

        String html = mvc.perform(get("/").session(priya).param("q", "%') OR 1=1 --"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(html).doesNotContain("Service yard");
    }

    @Test
    void missingIncidentIsNotFound() throws Exception {
        MockHttpSession alex = login("alex");
        mvc.perform(get("/incidents/999999").session(alex))
                .andExpect(status().isNotFound());
        mvc.perform(post("/incidents/999999/edit").session(alex))
                .andExpect(status().isNotFound());
    }
}
