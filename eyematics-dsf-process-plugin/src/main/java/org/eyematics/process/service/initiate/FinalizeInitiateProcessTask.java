package org.eyematics.process.service.initiate;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractServiceDelegate;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FinalizeInitiateProcessTask extends AbstractServiceDelegate {

    private static final Logger logger = LoggerFactory.getLogger(FinalizeInitiateProcessTask.class);
    private final DataSetStatusGenerator dataSetStatusGenerator;

    public FinalizeInitiateProcessTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api);
        this.dataSetStatusGenerator = dataSetStatusGenerator;
    }

    @Override
    protected void doExecute(DelegateExecution delegateExecution, Variables variables) throws BpmnError, Exception {
        logger.info("-> Summing up the process in an Output...");
        logger.info("-> Tasks: {}", variables.getTasks());
        //Task startTask = variables.getStartTask();
        //List<Task.TaskOutputComponent> taskOutputComponentList = startTask.getOutput();
        //for (Task.TaskOutputComponent t : taskOutputComponentList) logger.info("-> Summing up the process in an Output\n -> {}", t.getType().getCodingFirstRep().getSystem());
        //startTask.getOutput().clear();
    }
}
