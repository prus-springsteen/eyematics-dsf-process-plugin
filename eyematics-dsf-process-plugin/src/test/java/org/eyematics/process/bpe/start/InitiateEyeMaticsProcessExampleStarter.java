package org.eyematics.process.bpe.start;

import java.util.Date;
import java.util.UUID;
import org.eyematics.process.constant.InitiateConstants;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.eyematics.process.EyeMaticsProcessPluginDefinition;
import dev.dsf.bpe.start.ExampleStarter;
import dev.dsf.bpe.v1.constants.CodeSystems;
import dev.dsf.bpe.v1.constants.NamingSystems;


public class InitiateEyeMaticsProcessExampleStarter {

    private static final String DIC_URL = "https://dic-a/fhir";
    private static final String DIC_IDENTIFIER = "dic.a.test";


    public static void main(String[] args) throws Exception {
        ExampleStarter.forServer(args, DIC_URL).startWith(task());
    }

    private static Task task() {
        var def = new EyeMaticsProcessPluginDefinition();

        Task task = new Task();
        task.setIdElement(new IdType("urn:uuid:" + UUID.randomUUID().toString()));

        task.getMeta().addProfile(InitiateConstants.PROFILE_TASK_INITIATE_EYEMATICS_PROCESS_START + "|" + def.getResourceVersion());
        task.setInstantiatesCanonical(
                InitiateConstants.PROFILE_TASK_INITIATE_EYEMATICS_PROCESS_START_URI + "|" + def.getResourceVersion());
        task.setStatus(Task.TaskStatus.REQUESTED);
        task.setIntent(Task.TaskIntent.ORDER);
        task.setAuthoredOn(new Date());
        task.getRequester().setType(ResourceType.Organization.name())
                .setIdentifier(NamingSystems.OrganizationIdentifier.withValue(DIC_IDENTIFIER));
        task.getRestriction().addRecipient().setType(ResourceType.Organization.name())
                .setIdentifier(NamingSystems.OrganizationIdentifier.withValue(DIC_IDENTIFIER));
        task.addInput().setValue(new StringType(InitiateConstants.PROFILE_TASK_INITIATE_EYEMATICS_PROCESS_START_MESSAGE_NAME)).getType()
                .addCoding(CodeSystems.BpmnMessage.messageName());

        return task;
    }
}
