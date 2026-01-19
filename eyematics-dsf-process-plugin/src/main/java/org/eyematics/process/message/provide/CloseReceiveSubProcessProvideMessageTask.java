package org.eyematics.process.message.provide;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsGenericStatus;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.eyematics.process.utils.message.CloseProcessAbstractTaskMessageSend;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Stream;


public class CloseReceiveSubProcessProvideMessageTask extends CloseProcessAbstractTaskMessageSend {

    private static final Logger logger = LoggerFactory.getLogger(CloseReceiveSubProcessProvideMessageTask.class);

    public CloseReceiveSubProcessProvideMessageTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api, dataSetStatusGenerator);
    }

    @Override
    protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution,
                                                                           Variables variables) {
        logger.info("-> Preparing the output to close a receive process");
        Task startTask = variables.getStartTask();
        return this.dataSetStatusGenerator.transformOutputToInputComponent(startTask,
                EyeMaticsGenericStatus.getTypeSystem(),
                EyeMaticsGenericStatus.getTypeCode());
    }
}
