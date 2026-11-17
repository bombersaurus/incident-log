package incidentlog;

import java.io.IOException;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Runs before every page except the login page. If nobody is logged in it
 * sends the browser to /login. Otherwise it loads the user and stores it on
 * the request as "currentUser" for controllers and templates to use.
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    public static final String CURRENT_USER = "currentUser";

    private final UserRepository users;

    public LoginInterceptor(UserRepository users) {
        this.users = users;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws IOException {
        Optional<User> user = loggedInUser(request);
        if (user.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false; // stop here, do not run the controller
        }
        request.setAttribute(CURRENT_USER, user.get());
        return true;
    }

    private Optional<User> loggedInUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return Optional.empty();
        }
        Object userId = session.getAttribute(LoginController.SESSION_USER_ID);
        if (userId == null) {
            return Optional.empty();
        }
        return users.findById((Long) userId);
    }
}
