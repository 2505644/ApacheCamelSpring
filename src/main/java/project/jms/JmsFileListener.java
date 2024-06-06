package project.jms;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.BytesMessage;

@Component
public class JmsFileListener {

    private static Logger LOGGER = LogManager.getLogger(JmsFileListener.class);

    final String queue = "empty";

    @JmsListener(destination = queue)
    public void receiveXmlFile(BytesMessage bytesMessage) {
        try {
            byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(bytes);
            String out = new String(bytes);
            LOGGER.info(
                    String.format(" Queue = %s; File contains = %s", queue, out)
            );
        } catch (Exception e) {
            LOGGER.error("Error in JmsFileListener class!", e);
        }
    }
}
