package project.camel;

import project.db.H2Repo;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class TaskRoute extends RouteBuilder {

    private TaskProcessor taskProcessor;

    @Override
    public void configure() throws Exception {
        errorHandler(
                deadLetterChannel("project.jms:queue:invalid-queue"));

        from("file:data?noop=true")
                .streamCaching()
                .choice()
                .when(header("CamelFileName").endsWith(".xml"))
                .to("project.jms:queue:empty")
                .process(taskProcessor)
                .when(header("CamelFileName").endsWith(".txt"))
                .bean(H2Repo.class, "writeInDb")
                .to("project.jms:queue:empty")
                .process(taskProcessor)
                .otherwise()
                .process(taskProcessor)
                .throwException(new Exception(" Wrong extension !"))
                .end()
                .choice()
                .when(header("to").isNotNull())
                .to("smtp://localhost");
    }
}
