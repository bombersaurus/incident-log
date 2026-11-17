package incidentlog;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Makes the logged-in user available to every template as ${currentUser}. */
@ControllerAdvice
public class CurrentUserAdvice {

    @ModelAttribute(LoginInterceptor.CURRENT_USER)
    public User currentUser(HttpServletRequest request) {
        return (User) request.getAttribute(LoginInterceptor.CURRENT_USER);
    }
}
