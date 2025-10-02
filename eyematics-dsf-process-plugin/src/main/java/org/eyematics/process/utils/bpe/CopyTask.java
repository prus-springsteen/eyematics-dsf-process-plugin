package org.eyematics.process.utils.bpe;

import org.hl7.fhir.r4.model.Task;


public class CopyTask {

    public static Task getTaskCopy(Task task) {
        return task.copy();
    }

}
