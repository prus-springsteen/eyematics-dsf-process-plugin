package org.eyematics.process.message.receive;

import dev.dsf.bpe.v1.ProcessPluginApi;
import dev.dsf.bpe.v1.activity.AbstractTaskMessageSend;
import dev.dsf.bpe.v1.variables.Variables;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.eyematics.process.constant.EyeMaticsConstants;
import org.eyematics.process.utils.generator.DataSetStatusGenerator;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.stream.Stream;

public class AcknowledgeReceivedMessageTask extends AbstractTaskMessageSend {

    private static final Logger logger = LoggerFactory.getLogger(AcknowledgeReceivedMessageTask.class);
    private final DataSetStatusGenerator dataSetStatusGenerator;

    public AcknowledgeReceivedMessageTask(ProcessPluginApi api, DataSetStatusGenerator dataSetStatusGenerator) {
        super(api);
        this.dataSetStatusGenerator = dataSetStatusGenerator;
    }

    @Override
    protected Stream<Task.ParameterComponent> getAdditionalInputParameters(DelegateExecution execution, Variables variables) {
        logger.info("-> something to send");
        return Stream.of(this.dataSetStatusGenerator.createDataSetStatusInput(EyeMaticsConstants.CODESYSTEM_DATA_SET_STATUS_VALUE_RECEIPT_OK, EyeMaticsConstants.CODESYSTEM_GENERIC_DATA_SET_STATUS,
                EyeMaticsConstants.CODESYSTEM_DATA_TRANSFER_VALUE_DATA_SET_STATUS, null));
    }
}
