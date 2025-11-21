package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.ProvideConstants;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EvaluateAdminApprovalTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(EvaluateAdminApprovalTask.class);

    public EvaluateAdminApprovalTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Evaluating admin approval.");
        QuestionnaireResponse userResponse = variables.getLatestReceivedQuestionnaireResponse();
        boolean answer = userResponse.getItem().stream().filter(item
                        -> item.getLinkId().equals("answer"))
                .map(item -> (BooleanType) item.getAnswerFirstRep()
                        .getValue()).findFirst().orElse(new BooleanType(false)).getValue();
        variables.setBoolean(ProvideConstants.BPMN_EXECUTION_VARIABLE_EYEMATICS_DATA_SET_ADMIN_APPROVED, answer);
    }
}
