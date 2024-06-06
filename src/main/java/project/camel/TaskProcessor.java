package project.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class TaskProcessor implements Processor {
    private final static int BATCH_SIZE = 3;
    private final static String TXT = "txt";
    private final static String XML = "xml";
    private final static String UNDEFINED = "undefined";

    private long startTime;
    private Map<String, Integer> numberOfFilesByType;

    public TaskProcessor() {
        numberOfFilesByType = new HashMap<>();
        numberOfFilesByType.put(XML, 0);
        numberOfFilesByType.put(TXT, 0);
        numberOfFilesByType.put(UNDEFINED, 0);
    }

    private static Logger LOGGER = LogManager.getLogger(TaskProcessor.class);
    private int sumOfFiles = 0;

    public void process(Exchange exchange) throws Exception {
        startTime = System.nanoTime();
        messageExtensionHandler(exchange);
        sumOfFiles++;

        if (sumOfFiles % BATCH_SIZE == 0) {
            sendEmail(exchange);
        }

        if (LOGGER.isDebugEnabled()) {
            numberOfFilesByType.forEach((x, y) -> LOGGER.debug(
                    "Extension = " + x + " Count of files = " + y)
            );
        }
    }

    private void messageExtensionHandler(Exchange exchange) {
        String fileName = getFileName(exchange);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(fileName);
        }

        String extension = getExtension(fileName);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(extension);
        }

        if (extension.equals(TXT) || extension.equals(XML)) {
            increaseCountFileWithExtension(extension);
        } else {
            increaseCountFileWithExtension(UNDEFINED);
        }
    }

    private String getFileName(Exchange exchange) {
        return exchange
                .getMessage()
                .getHeader("CamelFileName")
                .toString();
    }

    private String getExtension(String filename) {
        String[] parsedBlocks = filename.split("\\.");
        return parsedBlocks[parsedBlocks.length - 1].toLowerCase();
    }

    void increaseCountFileWithExtension(String extension) {
        int buffer = numberOfFilesByType.get(extension);
        numberOfFilesByType.put(extension, ++buffer);
    }

    private void sendEmail(Exchange exchange) {
        String mailBody = getLetterBody(getDeltaTime());
        Message message = exchange.getOut();
        message.setHeader("host", "localhost");
        message.setHeader("to", "kashuba_an@magnit.ru");
        message.setHeader("From", "kashuba_an@magnit.ru");
        message.setHeader("Subject", "Count of files");
        message.setBody(mailBody);
    }

    private long getDeltaTime() {
        long currentTime = System.nanoTime();
        long deltaTime = currentTime - startTime;
        startTime = currentTime;
        return deltaTime;
    }

    private String getLetterBody(long deltaTime) {
        StringBuilder mailBody = new StringBuilder();
        mailBody.append("countTxt: ");
        mailBody.append(numberOfFilesByType.get(TXT));
        mailBody.append(System.getProperty("line.separator"));
        mailBody.append("countXml: ");
        mailBody.append(numberOfFilesByType.get(XML));
        mailBody.append(System.getProperty("line.separator"));
        mailBody.append("countOther: ");
        mailBody.append(numberOfFilesByType.get(UNDEFINED));
        mailBody.append(System.getProperty("line.separator"));
        mailBody.append("elapsed time: ");
        mailBody.append(deltaTime);
        mailBody.append(" nanoseconds");
        return mailBody.toString();
    }
}
