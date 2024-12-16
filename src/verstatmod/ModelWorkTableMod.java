package verstatmod;

import smo.*;
import verstat.ModelWorkTable;
import java.util.ArrayList;
import java.util.Objects;

public class ModelWorkTableMod extends ModelWorkTable {
    private int subTasks;
    private int totalTasks;

    public ModelWorkTableMod (Element...elements) {
        super(elements);
    }

    private double getSubTaskTotalTask() {
        ArrayList<Task> processedTasks = new ArrayList<>();
        ArrayList<Task> subcontractorTasks = new ArrayList<>();

        for (Element element : elements) {
            if (element instanceof Dispose d &&
                    (Objects.equals(d.getName(), "DISPOSE") ||
                            Objects.equals(d.getName(), "DISPOSE SUBCONTRACTOR"))) {
                processedTasks.addAll(d.getProcessedTasks());
            }

            if (element instanceof Dispose d &&
                    Objects.equals(d.getName(), "DISPOSE SUBCONTRACTOR")) {
                subcontractorTasks.addAll(d.getProcessedTasks());
            }
        }
        subTasks = subcontractorTasks.size(); totalTasks = processedTasks.size();
        if (processedTasks.isEmpty()) {
            return 0;
        }
        return (double) subcontractorTasks.size() / processedTasks.size();
    }

    public int getInterrupts(){
        int interrupts = 0;
        for (Element element : elements) {
            if (element instanceof Process5SolvingBreakdown p) {
                if (Objects.equals(p.getName(), "PROCESS 5")) {
                    interrupts = p.getInterrupts();
                }
            }
        }
        return interrupts;
    }

    public double getInterruptResolutionTime(){
        double totalSearchTime = getInterrupts() * 3;
        return totalSearchTime / getResolutionBreakDownTime();
    }

    @Override
    public void printResult() {
        super.printResult();
        System.out.println("\n-------------MOD RESULTS-------------");
        System.out.printf("3) prop of subcontractor tasks to total tasks: %.2f%%%n", getSubTaskTotalTask() * 100);
        System.out.printf("4) prop of interruption search time to resolution breakdown time: %.2f%%%n", getInterruptResolutionTime() * 100);
        System.out.println("\nsubcontractor tasks: " + subTasks);
        System.out.println("total system processed tasks: " + totalTasks);
        System.out.println("interrupts: " + getInterrupts());
        System.out.println("interrupts time: " + (getInterrupts() * 3));
    }
}
