package incidentlog;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Logging in and out. Being logged in means the session holds a user id. */
@Controller
public class LoginController {

    /** The name the logged-in user's id is stored under in the session. */
    public static final String SESSION_USER_ID = "userId";

    private final UserRepository users;
    private final BCryptPasswordEncoder passwordEncoder;

    public LoginController(UserRepository users, BCryptPasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam(defaultValue = "") String username,
                        @RequestParam(defaultValue = "") String password,
                        HttpServletRequest request,
                        HttpServletResponse response,
                        Model model) {
        Optional<User> user = users.findByUsername(username.trim());

        // Same message for a wrong username and a wrong password, so the page
        // does not reveal which usernames exist.
        if (user.isEmpty() || !passwordEncoder.matches(password, user.get().passwordHash())) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            model.addAttribute("error", "Wrong username or password.");
            return "login";
        }

        // Throw away any session from before the login and start a new one.
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        request.getSession(true).setAttribute(SESSION_USER_ID, user.get().id());
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }
}
