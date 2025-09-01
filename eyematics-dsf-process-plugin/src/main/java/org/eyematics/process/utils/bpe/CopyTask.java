package org.eyematics.process.utils.bpe;

import org.hl7.fhir.r4.model.Task;

public class CopyTask {
    public static Task getTaskCopy(Task task) {
        Task copyTask = new Task(task.getStatusElement(), task.getIntentElement());
        for (Task.TaskOutputComponent t : task.getOutput()) copyTask.addOutput(t);
        return copyTask;
    }
}
