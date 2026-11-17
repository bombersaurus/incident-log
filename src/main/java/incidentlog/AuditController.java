package incidentlog;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.server.ResponseStatusException;

/** The audit page: every change made to any incident. Supervisors only. */
@Controller
public class AuditController {

    private final AuditRepository audit;

    public AuditController(AuditRepository audit) {
        this.audit = audit;
    }

    @GetMapping("/audit")
    public String auditPage(@RequestAttribute(LoginInterceptor.CURRENT_USER) User user,
                            Model model) {
        if (!user.isSupervisor()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        model.addAttribute("changes", audit.all());
        return "audit";
    }
}
