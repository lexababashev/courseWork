package verstatmod;

import smo.Dispose;
import smo.Task;
import verstat.Process0WaitingTask;
import smo.Process;

public class Process0Mod extends Process0WaitingTask {
    private Process solvingBreakdown = null;
    private Dispose disposeSubcontractor = null;

    public Process0Mod(String name, double delayMean) {
        super(name, delayMean);
    }

    public void setSolvingBreakdown(Process solvingBreakdown) {
        this.solvingBreakdown = solvingBreakdown;
    }

    public void setDisposeSubcontractor(Dispose disposeSubcontractor) {
        this.disposeSubcontractor = disposeSubcontractor;
    }

    public void removeTasksForSubcontractor(int maxRemainingTasks, Dispose subcontractorDispose) {
        while (queue.size() > maxRemainingTasks) {
            Task task = queue.poll();
            if (task != null) {
                subcontractorDispose.inAct(task); // Передаємо завдання на субпідрядника
            }
        }
    }


    @Override
    public void inAct(Task task) {
    super.inAct(task);
        // Логіка активації очищення черги
        if (solvingBreakdown.getState() == 1) { // Якщо процес 5 активний (поломка)
            removeTasksForSubcontractor(3, disposeSubcontractor); // Тільки останні 3 завдання залишаються
        }
    }
}