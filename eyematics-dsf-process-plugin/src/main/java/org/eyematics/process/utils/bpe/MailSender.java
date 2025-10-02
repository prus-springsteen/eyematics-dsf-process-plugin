package org.eyematics.process.utils.bpe;

import dev.dsf.bpe.v1.service.MailService;
import org.hl7.fhir.r4.model.Task;


public class MailSender {

    public static boolean sendError(MailService mailService, Task task, String process, String taskName, String status, String errorMessage) {
        String subject = "Error in process '" + process + "'";
        StringBuilder message = new StringBuilder();
        message.append("an error occurred by processing '");
        message.append(taskName);
        message.append("' with following message: \n\n");
        message.append(errorMessage);
        return sendInfo(mailService, task, status, subject, message.toString());
    }

    public static boolean sendInfo(MailService mailService, Task task, String status, String subject, String mailContent) {
        StringBuilder message = new StringBuilder();
        message.append("Dear Admin, \n\n");
        message.append(mailContent);
        message.append("\n\n");
        message.append("Task Id: ");
        message.append(task.getId());
        message.append("\n");
        message.append("Requester: ");
        message.append(task.getRequester().getIdentifier().getValue());
        message.append("\n");
        message.append("Status: ");
        message.append(status);
        try {
            mailService.send(subject, message.toString());
        } catch (Exception e) {
            return false;
        }
        return  true;
    }
}
