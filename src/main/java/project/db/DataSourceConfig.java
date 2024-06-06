package project.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.annotation.PostConstruct;

@Configuration
@PropertySources({@PropertySource("classpath:dataCfg.properties")})
public class DataSourceConfig {

    private final static Logger LOGGER =
            LogManager.getLogger(DataSourceConfig.class);

    @Autowired
    private Environment env;

    @Bean
    @Qualifier("h2DataSource")
    public DriverManagerDataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));
        dataSource.setPassword(env.getProperty("spring.datasource.password"));
        return dataSource;
    }

    @Bean
    @Qualifier("JdbcTemplateForH2")
    JdbcTemplate getJdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource());
        return jdbcTemplate;
    }

    @PostConstruct
    @DependsOn("JdbcTemplateForH2")
    void createDb() {
        JdbcTemplate jdbcTemplate = getJdbcTemplate();
        String createTable1 = "CREATE TABLE msg (" +
                "    msg_id INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY," +
                "    message BLOB NOT NULL" +
                ")";
        String createTable2 = "CREATE TABLE headers (" +
                "    headers_id INTEGER NOT NULL," +
                "    FOREIGN KEY (headers_id) references msg(msg_id)," +
                "    head VARCHAR(256) NOT NULL" +
                ")";
        jdbcTemplate.execute(createTable1);
        jdbcTemplate.execute(createTable2);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Successful table creation");
        }
    }
}
