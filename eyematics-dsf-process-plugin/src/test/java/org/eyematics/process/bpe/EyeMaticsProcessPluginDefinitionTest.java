package org.eyematics.process.bpe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import dev.dsf.bpe.v1.ProcessPluginDefinition;
import org.eyematics.process.EyeMaticsProcessPluginDefinition;
import org.eyematics.process.constant.InitiateConstants;
import org.eyematics.process.constant.ProvideConstants;
import org.eyematics.process.constant.ReceiveConstants;


public class EyeMaticsProcessPluginDefinitionTest {

    @Test
    public void testResourceLoading() {
        ProcessPluginDefinition definition = new EyeMaticsProcessPluginDefinition();
        Map<String, List<String>> resourcesByProcessId = definition.getFhirResourcesByProcessId();

        var initiate = resourcesByProcessId.get(InitiateConstants.PROCESS_NAME_FULL_EXECUTE_INITIATE_EYEMATICS_PROCESS);
        assertNotNull(initiate);
        assertEquals(8, initiate.stream().filter(this::exists).count());

        var provide = resourcesByProcessId.get(ProvideConstants.PROCESS_NAME_FULL_EXECUTE_PROVIDE_EYEMATICS_PROCESS);
        assertNotNull(provide);
        assertEquals(9, provide.stream().filter(this::exists).count());

        var receive = resourcesByProcessId.get(ReceiveConstants.PROCESS_NAME_FULL_EXECUTE_RECEIVE_EYEMATICS_PROCESS);
        assertNotNull(receive);
        assertEquals(13, receive.stream().filter(this::exists).count());
    }

    private boolean exists(String file) {
        return getClass().getClassLoader().getResourceAsStream(file) != null;
    }
}
