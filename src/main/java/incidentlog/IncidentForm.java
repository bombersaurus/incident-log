package incidentlog;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * What somebody typed into the incident form. Everything is text at this
 * point, exactly as the browser sent it. Call errors() before using it.
 */
public record IncidentForm(
        String occurredAt,
        String location,
        String category,
        String severity,
        String description,
        String bodycamRef) {

    public static final List<String> CATEGORIES =
            List.of("Disorder", "Ejection", "Medical", "Refused entry", "Theft", "Other");
    public static final List<String> SEVERITIES = List.of("1", "2", "3", "4", "5");

    /** An empty form for a new incident, with the time set to now. */
    public static IncidentForm blank() {
        String now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString();
        return new IncidentForm(now, "", CATEGORIES.get(0), "1", "", "");
    }

    /** A form filled in with an existing incident, ready to edit. */
    public static IncidentForm of(Incident incident) {
        return new IncidentForm(
                incident.occurredAt().truncatedTo(ChronoUnit.MINUTES).toString(),
                incident.location(),
                incident.category(),
                String.valueOf(incident.severity()),
                incident.description(),
                incident.bodycamRef());
    }

    /** Builds the form from the fields of a submitted request. */
    public static IncidentForm from(Map<String, String> params) {
        return new IncidentForm(
                field(params, "occurredAt"),
                field(params, "location"),
                field(params, "category"),
                field(params, "severity"),
                field(params, "description"),
                field(params, "bodycamRef"));
    }

    private static String field(Map<String, String> params, String name) {
        return params.getOrDefault(name, "").trim();
    }

    /** Everything wrong with the form. An empty list means it is fine to save. */
    public List<String> errors() {
        List<String> errors = new ArrayList<>();
        if (parsedOccurredAt() == null) {
            errors.add("Enter the date and time it happened.");
        }
        if (location.isEmpty()) {
            errors.add("Location is required.");
        }
        if (!CATEGORIES.contains(category)) {
            errors.add("Pick a category from the list.");
        }
        Integer level = parsedSeverity();
        if (level == null) {
            errors.add("Severity must be a number.");
        }
        return errors;
    }

    /** The time as a LocalDateTime, or null if the text is not a date and time. */
    public LocalDateTime parsedOccurredAt() {
        try {
            return LocalDateTime.parse(occurredAt);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** The severity as a number, or null if the text is not a whole number. */
    public Integer parsedSeverity() {
        try {
            return Integer.valueOf(severity);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
