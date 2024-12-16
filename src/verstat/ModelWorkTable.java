package verstat;

import smo.Dispose;
import smo.Element;
import smo.Task;
import smo.Model;
import smo.Process;
import java.util.ArrayList;
import java.util.Objects;

public class ModelWorkTable extends Model {
    private double totalSetupTime;
    private double totalProcessingTime;

    public ModelWorkTable(Element...elements) {
        super(elements);
        this.totalSetupTime = 0;
        this.totalProcessingTime = 0;
    }

    // Inner class to encapsulate task processing statistics
    private static class TaskStatistics {
        final double avgTimeInSystem;
        final double minTimeInSystem;
        final double maxTimeInSystem;
        final double standardDeviation;
        final int totalProcessedTasks;

        TaskStatistics(double avgTimeInSystem, double minTimeInSystem,
                       double maxTimeInSystem, double standardDeviation,
                       int totalProcessedTasks) {
            this.avgTimeInSystem = avgTimeInSystem;
            this.minTimeInSystem = minTimeInSystem;
            this.maxTimeInSystem = maxTimeInSystem;
            this.standardDeviation = standardDeviation;
            this.totalProcessedTasks = totalProcessedTasks;
        }
    }

    // Method to calculate detailed task processing statistics
    private TaskStatistics calculateTaskStatistics() {
        ArrayList<Task> processedTasks = new ArrayList<>();
        for (Element element : elements) {
            if (element instanceof Dispose d && Objects.equals(d.getName(), "DISPOSE")) {
                processedTasks.addAll(d.getProcessedTasks());
            }
        }

        if (processedTasks.isEmpty()) {
            return new TaskStatistics(0, 0, 0, 0, 0);
        }

        double avgTimeInSystem = processedTasks.stream()
                .mapToDouble(task -> task.getTimeOut() - task.getTimeIn())
                .average()
                .orElse(0.0);

        double minTimeInSystem = processedTasks.stream()
                .mapToDouble(task -> task.getTimeOut() - task.getTimeIn())
                .min()
                .orElse(0.0);

        double maxTimeInSystem = processedTasks.stream()
                .mapToDouble(task -> task.getTimeOut() - task.getTimeIn())
                .max()
                .orElse(0.0);

        // Обчислення стандартного відхилення
        double variance = processedTasks.stream()
                .mapToDouble(task -> Math.pow((task.getTimeOut() - task.getTimeIn()) - avgTimeInSystem, 2))
                .average()
                .orElse(0.0);
        double standardDeviation = Math.sqrt(variance);

        return new TaskStatistics(
                avgTimeInSystem,
                minTimeInSystem,
                maxTimeInSystem,
                standardDeviation,
                processedTasks.size()
        );
    }

   @Override
    public void simulate(double time) {
        while (tCurr < time) {
            tNext = Double.MAX_VALUE;
            nearestEvent = -1;

            for (Element element : elements) {
                if ((tCurr < element.getTNext() || isFirst) && element.getTNext() <= tNext) {
                    if (element.getTNext() == tNext) {
                        if (element.getName().equals("PROCESS 4") &&
                                elements.get(nearestEvent).getName().equals("PROCESS 2")) {
                            nearestEvent = element.getId();
                        }
                    } else {
                        tNext = element.getTNext();
                        nearestEvent = element.getId();
                    }
                }
            }

            updateBlockedElements();

            System.out.println("\nEvent in " + elements.get(nearestEvent).getName() + ", tNext = " + tNext + ", tCurr = " + tCurr);

            double delta = tNext - tCurr;
            doModelStatistics(delta);
            for (Element element : elements) {
                element.doStatistics(delta);
            }

            tCurr = tNext;
            for (Element element : elements) {
                element.setTCurr(tCurr);
            }

            elements.get(nearestEvent).outAct();
            for (Element element : elements) {
                if (element.getTNext() == tCurr) {
                    element.outAct();
                }
            }

            isFirst = false;

            for (Element element : elements) {
                element.printInfo();
            }
        }
        printResult();
    }


    private double getAverageTaskTimeInSystem() {
        ArrayList<Task> processedTasks = new ArrayList<>();
        for (Element element : elements) {
            if (element instanceof Dispose d && Objects.equals(d.getName(), "DISPOSE")) {
                processedTasks.addAll(d.getProcessedTasks());
            }
        }
        if (processedTasks.isEmpty()) {
            return 0;
        }
        double totalTasksTimeInSystem = 0;
        for (Task task : processedTasks) {
            totalTasksTimeInSystem += task.getTimeOut() - task.getTimeIn();
        }
        return totalTasksTimeInSystem / processedTasks.size();
    }

    // Calculate workload for setup and solving processes
    private double workloadSetupSolving(){
        double settingAndSolvingWorkTime = 0;
        for (Element element : elements) {
            if (element instanceof Process p && (Objects.equals(element.getName(), "PROCESS 1") || Objects.equals(element.getName(), "PROCESS 2"))) {
                settingAndSolvingWorkTime += p.getWorkTime();
            }
        }
        return (settingAndSolvingWorkTime / tCurr);
    }

    public double getResolutionBreakDownTime(){
        double result = 0;
        for (Element element : elements) {
            if (element instanceof Process p) {
                if (Objects.equals(p.getName(), "PROCESS 5")) {
                    result = p.getWorkTime();
                }
            }
        }
        return result;
    }

    @Override
    public void printResult() {
        System.out.println("\n-------------RESULTS-------------");

        // Task statistics
        TaskStatistics taskStats = calculateTaskStatistics();

        System.out.printf("1) worktable workload: %.2f%%%n", workloadSetupSolving() * 100);
        System.out.printf("2) average task time in system: %.2f hours%n", getAverageTaskTimeInSystem());

        System.out.printf("task statistics:%n");
        System.out.printf("   - minimum time in system: %.2f hours%n", taskStats.minTimeInSystem);
        System.out.printf("   - average time in system: %.2f hours%n", taskStats.avgTimeInSystem);
        System.out.printf("   - maximum time in system: %.2f hours%n", taskStats.maxTimeInSystem);
        System.out.printf("   - standard deviation: %.2f hours%n", taskStats.standardDeviation);
        System.out.printf("   - total tasks processed: %d%n", taskStats.totalProcessedTasks);

        // setup and processing time
        for (Element element : elements) {
            if (element instanceof Process p) {
                if (Objects.equals(p.getName(), "PROCESS 1")) {
                    totalSetupTime = p.getWorkTime();
                } else if (Objects.equals(p.getName(), "PROCESS 2")) {
                    totalProcessingTime = p.getWorkTime();
                }
            }
        }

        System.out.printf("total solving time:%n");
        System.out.printf("   - total setup time: %.2f hours%n", totalSetupTime);
        System.out.printf("   - total processing time: %.2f hours%n", totalProcessingTime);
        System.out.printf("   - total solving breakdown time: %.2f hours%n", getResolutionBreakDownTime());
        System.out.printf("   - waisting time with upset work: %.2f hours%n", tCurr-getResolutionBreakDownTime()-totalProcessingTime-totalSetupTime);
        System.out.printf("   - total simulation time: %.2f hours%n", tCurr);
    }
}
