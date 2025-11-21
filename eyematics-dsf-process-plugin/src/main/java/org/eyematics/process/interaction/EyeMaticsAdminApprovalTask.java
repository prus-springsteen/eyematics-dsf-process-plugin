package org.eyematics.process.interaction;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.DefaultUserTaskListener;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.eyematics.process.utils.bpe.MailSender;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

public class EyeMaticsAdminApprovalTask extends DefaultUserTaskListener {

    private static final Logger logger = LoggerFactory.getLogger(EyeMaticsAdminApprovalTask.class);
    private final ProcessPluginApi api;

    public EyeMaticsAdminApprovalTask(ProcessPluginApi api) {
        super(api);
        this.api = api;
    }

    @Override
    protected void beforeQuestionnaireResponseCreate(DelegateTask userTask, QuestionnaireResponse beforeCreate) {
        Variables variables = this.api.getVariables(userTask.getExecution());
        String reqOrg = variables.getStartTask().getRequester().getIdentifier().getValue();
        String question = "The organization " + reqOrg + " is requesting data from the EyeMatics Core Dataset. " +
                "Please approve this request.\n" +
                "Choose \"Yes\" if the data should be sent, or choose \"No\" if it should not be sent.";
        Optional<QuestionnaireResponse.QuestionnaireResponseItemComponent> displayItem = beforeCreate.getItem().stream()
                .filter(i -> i.getLinkId().equals("binary-question")).findFirst();
        displayItem.ifPresent(questionnaireResponseItemComponent
                        -> questionnaireResponseItemComponent.setText(question));
    }

    @Override
    protected void afterQuestionnaireResponseCreate(DelegateTask userTask, QuestionnaireResponse afterCreate) {
        Variables variables = this.api.getVariables(userTask.getExecution());
        String url = this.api.getQuestionnaireResponseHelper().getLocalVersionlessAbsoluteUrl(afterCreate);
        String subject = "Admin Approval for EyeMatics Data";
        String message = "please approve the data transfer to the requesting organisation (see below). \n" +
                "For this, please answer the questionnaire provided at the following link: \n\n"
                + url;
        MailSender.sendInfo(this.api.getMailService(),
                variables.getStartTask(),
                "-",
                subject,
                message);
    }
}
