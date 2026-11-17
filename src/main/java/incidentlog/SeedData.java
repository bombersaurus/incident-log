package incidentlog;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Fills an empty database with made-up users and incidents when the app
 * starts. Everyone's password is "password". This is demo data only.
 */
@Component
public class SeedData implements ApplicationRunner {

    private final UserRepository users;
    private final IncidentRepository incidents;
    private final BCryptPasswordEncoder passwordEncoder;
    private final boolean enabled;

    public SeedData(UserRepository users, IncidentRepository incidents,
                    BCryptPasswordEncoder passwordEncoder,
                    @Value("${app.seed:true}") boolean enabled) {
        this.users = users;
        this.incidents = incidents;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled || !users.isEmpty()) {
            return;
        }
        String passwordHash = passwordEncoder.encode("password");
        users.add("sam", passwordHash, "supervisor");
        users.add("alex", passwordHash, "staff");
        users.add("priya", passwordHash, "staff");
        long alex = users.findByUsername("alex").orElseThrow().id();
        long priya = users.findByUsername("priya").orElseThrow().id();

        List<IncidentForm> alexIncidents = List.of(
                new IncidentForm("2026-10-28T21:40", "North entrance", "Refused entry", "2",
                        "Guest arrived with a ticket for a different date. Refused entry and sent to the box office.", ""),
                new IncidentForm("2026-10-28T22:15", "Level 2 concourse", "Disorder", "3",
                        "Two guests arguing in the bar queue. Separated them and both calmed down. No further action.", "BC-1042"),
                new IncidentForm("2026-11-07T19:30", "North entrance", "Refused entry", "2",
                        "Screening flagged a bag. Guest had a glass bottle, which was binned. Guest then let in.", ""),
                new IncidentForm("2026-11-07T23:10", "Block 112", "Ejection", "4",
                        "Guest threw a drink at the row in front after two warnings. Ejected through the east exit with a second responder.", "BC-1101"),
                new IncidentForm("2026-11-14T22:30", "Floor standing", "Disorder", "5",
                        "Fight between two groups near the sound desk. Four responders attended. Two guests ejected, one treated for a cut hand.", "BC-1130"),
                new IncidentForm("2026-11-15T18:55", "Box office", "Other", "1",
                        "Guest lost their phone in the queue. Logged with lost property.", ""));
        List<IncidentForm> priyaIncidents = List.of(
                new IncidentForm("2026-11-02T20:05", "Block 104", "Medical", "4",
                        "Guest fainted during the support act. Medics called on the radio and took over within three minutes.", "BC-1077"),
                new IncidentForm("2026-11-02T22:50", "Service yard", "Other", "1",
                        "Delivery driver couldn't find the loading bay. Walked him to bay 3.", ""),
                new IncidentForm("2026-11-08T21:20", "Merchandise stand", "Theft", "3",
                        "Staff reported a hoodie taken from the stand. Guest stopped at the exit and returned it. Details passed to the supervisor.", "BC-1109"),
                new IncidentForm("2026-11-14T20:45", "Level 1 toilets", "Medical", "2",
                        "Guest feeling unwell. Sat with them until a friend arrived. Medics not needed.", ""));

        alexIncidents.forEach(incident -> incidents.add(incident, alex));
        priyaIncidents.forEach(incident -> incidents.add(incident, priya));
    }
}
