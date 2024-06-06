package project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import project.db.H2Repo;

import java.util.concurrent.TimeUnit;

@Configuration
@SpringBootApplication(scanBasePackages = "project")
public class Application implements ApplicationRunner {

    @Autowired
    private H2Repo h2Repo;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        TimeUnit.SECONDS.sleep(2);
        h2Repo.showDataInH2Db();
        h2Repo.clearH2Db();
        System.exit(0);
    }
}
