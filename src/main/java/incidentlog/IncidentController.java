package incidentlog;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

/** Incident pages: list and search, add, view, edit. */
@Controller
public class IncidentController {

    private final IncidentRepository incidents;
    private final AuditRepository audit;

    public IncidentController(IncidentRepository incidents, AuditRepository audit) {
        this.incidents = incidents;
        this.audit = audit;
    }

    @GetMapping("/")
    public String list(@RequestParam(defaultValue = "") String q,
                       @RequestAttribute(LoginInterceptor.CURRENT_USER) User user,
                       Model model) {
        String query = q.trim();
        // Supervisors see everything. Staff only see what they reported.
        Long onlyReportedBy = user.isSupervisor() ? null : user.id();
        model.addAttribute("incidents", incidents.search(query, onlyReportedBy));
        model.addAttribute("query", query);
        return "index";
    }

    @GetMapping("/incidents/new")
    public String newIncidentPage(Model model) {
        return showForm(model, "New incident", IncidentForm.blank(), List.of());
    }

    @PostMapping("/incidents/new")
    public String create(@RequestParam Map<String, String> params,
                         @RequestAttribute(LoginInterceptor.CURRENT_USER) User user,
                         Model model,
                         HttpServletResponse response) {
        IncidentForm form = IncidentForm.from(params);
        List<String> errors = form.errors();
        if (!errors.isEmpty()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return showForm(model, "New incident", form, errors);
        }
        long id = incidents.add(form, user.id());
        return "redirect:/incidents/" + id;
    }

    @GetMapping("/incidents/{id}")
    public String detail(@PathVariable long id,
                         @RequestAttribute(LoginInterceptor.CURRENT_USER) User user,
                         Model model) {
        model.addAttribute("incident", load(id, user));
        model.addAttribute("history", audit.forIncident(id));
        return "detail";
    }

    @GetMapping("/incidents/{id}/edit")
    public String editPage(@PathVariable long id,
                           @RequestAttribute(LoginInterceptor.CURRENT_USER) User user,
                           Model model) {
        Incident incident = load(id, user);
        return showForm(model, "Edit incident " + id, IncidentForm.of(incident), List.of());
    }

    @PostMapping("/incidents/{id}/edit")
    public String edit(@PathVariable long id,
                       @RequestParam Map<String, String> params,
                       @RequestAttribute(LoginInterceptor.CURRENT_USER) User user,
                       Model model,
                       HttpServletResponse response) {
        load(id, user); // stops with a 404 if there is no such incident
        IncidentForm form = IncidentForm.from(params);
        List<String> errors = form.errors();
        if (!errors.isEmpty()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return showForm(model, "Edit incident " + id, form, errors);
        }
        incidents.update(id, form);
        logChanges(load(id, user), form, user);
        return "redirect:/incidents/" + id;
    }

    /** Finds one incident, or stops the request with a 404. */
    private Incident load(long id, User user) {
        return incidents.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private String showForm(Model model, String heading, IncidentForm form, List<String> errors) {
        model.addAttribute("heading", heading);
        model.addAttribute("form", form);
        model.addAttribute("errors", errors);
        model.addAttribute("categories", IncidentForm.CATEGORIES);
        model.addAttribute("severities", IncidentForm.SEVERITIES);
        return "form";
    }

    /** Adds an audit row for each field the form changed. */
    private void logChanges(Incident incident, IncidentForm form, User user) {
        logIfChanged(incident, user, "occurred at", incident.occurredAt(), form.parsedOccurredAt());
        logIfChanged(incident, user, "location", incident.location(), form.location());
        logIfChanged(incident, user, "category", incident.category(), form.category());
        logIfChanged(incident, user, "severity", incident.severity(), form.parsedSeverity());
        logIfChanged(incident, user, "description", incident.description(), form.description());
        logIfChanged(incident, user, "bodycam reference", incident.bodycamRef(), form.bodycamRef());
    }

    private void logIfChanged(Incident incident, User user, String field,
                              Object before, Object after) {
        if (!before.equals(after)) {
            audit.record(incident.id(), user.id(), field, before.toString(), after.toString());
        }
    }
}
