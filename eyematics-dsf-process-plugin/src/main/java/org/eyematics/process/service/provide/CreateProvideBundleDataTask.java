package org.eyematics.process.service.provide;

import org.eyematics.process.constant.EyeMaticsConstants;
import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class CreateProvideBundleDataTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(CreateProvideBundleDataTask.class);

    public CreateProvideBundleDataTask(ProcessPluginApi api) {
        super(api);
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> something to bundle");
        // Binary too large ... which possibilities are provided by dsf?
        Bundle b = variables.getResource(EyeMaticsConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET);
        logger.info("-> Amount of Items: {}", b.getEntry().size());
        Bundle toStore = new Bundle();
        for (int i = 0; i < 2; i++) toStore.addEntry(b.getEntry().get(i));

        //toStore.addEntry(b.getEntry().get(1));
        variables.setResource(EyeMaticsConstants.BPMN_PROVIDE_EXECUTION_VARIABLE_DATA_SET, toStore);
    }
}
