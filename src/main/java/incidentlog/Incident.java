package incidentlog;

import java.time.LocalDateTime;

/** One row of the incidents table, plus the username of whoever reported it. */
public record Incident(
        long id,
        LocalDateTime occurredAt,
        String location,
        String category,
        int severity,
        String description,
        String bodycamRef,
        long reportedBy,
        String reporter,
        LocalDateTime createdAt) {

    /** The description with line breaks turned into br tags, for showing on a page. */
    public String descriptionHtml() {
        return description.replace("\n", "<br>");
    }
}
