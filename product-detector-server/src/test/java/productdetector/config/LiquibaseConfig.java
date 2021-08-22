package productdetector.config;

import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ActiveProfiles("test")
public class LiquibaseConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(LiquibaseConfig.class);

    @Autowired
    private DataSource dataSource;

    @Bean
    public SpringLiquibase liquibase() {
        LOGGER.info("Initializing Liquibase.");

        // If the database is out of date, this will update it to the latest schema.

        SpringLiquibase liquidbase = new SpringLiquibase();
        liquidbase.setChangeLog("classpath:db/changelog/db.changelog-master.xml");
        liquidbase.setDataSource(dataSource);
        Map<String, String> params = new HashMap<>();
        params.put("verbose", "true");
        liquidbase.setChangeLogParameters(params);
        liquidbase.setShouldRun(true);

        return liquidbase;
    }
}