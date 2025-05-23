package org.eyematics.process.service.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.ProvideConstants;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateDataBundleTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CreateDataBundleTask.class);

    public CreateDataBundleTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> something to bundle");
        Bundle b = variables.getResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET);
        logger.info("-> Amount of Items: {}", b.getEntry().size());
        Bundle toStore = new Bundle();
        for (int i = 0; i < b.getEntry().size(); i++) toStore.addEntry(b.getEntry().get(i));
        variables.setResource(ProvideConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET, toStore);
    }
}
