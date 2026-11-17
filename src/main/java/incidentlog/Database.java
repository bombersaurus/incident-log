package incidentlog;

import javax.sql.DataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

/** Creates the tables from tables.sql when the app starts, if they are missing. */
@Component
public class Database {

    public Database(DataSource dataSource) {
        ResourceDatabasePopulator tables =
                new ResourceDatabasePopulator(new ClassPathResource("tables.sql"));
        tables.execute(dataSource);
    }
}
