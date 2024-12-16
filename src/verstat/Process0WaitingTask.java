package verstat;
import smo.Process;
import smo.Task;
import java.util.ArrayDeque;
import java.util.Deque;


public class Process0WaitingTask extends Process {
    public Process0WaitingTask(String name, double delayMean) {
        super(name, delayMean);
    }

    @Override
    public void inAct(Task task) {
        // When a task returns after interruption, it should be placed at the front of the queue
        if (task instanceof VerstatTask VerstatTask && VerstatTask.isInterrupted()) {
            Deque<Task> tempQueue = new ArrayDeque<>(queue);
            queue.clear();
            queue.add(task);
            queue.addAll(tempQueue);
        } else {
            // Normal queueing for new tasks if they are not interrupted
            super.inAct(task);
        }
    }
}
