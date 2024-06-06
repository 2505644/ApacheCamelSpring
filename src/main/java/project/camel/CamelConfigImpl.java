package project.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.springframework.context.annotation.Configuration;
import org.apache.activemq.ActiveMQXAConnectionFactory;

@Configuration
public class CamelConfigImpl extends CamelConfiguration {
    @Override
    protected void setupCamelContext(CamelContext camelContext) throws Exception {
        ActiveMQXAConnectionFactory connectionFactory = new ActiveMQXAConnectionFactory();
        connectionFactory.setBrokerURL("vm://localhost?broker.persistent=false&broker.useJmx=false&broker.useShutdownHook=false");
        JmsComponent answer = new JmsComponent();
        answer.setConnectionFactory(connectionFactory);
        camelContext.addComponent("jms", answer);
    }
}
