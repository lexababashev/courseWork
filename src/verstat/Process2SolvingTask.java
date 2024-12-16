package verstat;

import smo.Task;
import smo.Process;

public class Process2SolvingTask extends Process {

    private Process occurrenceBreakProcess = null; // Процес 4

    public Process2SolvingTask(String name, double delayMean, double delayDev) {
        super(name, delayMean, delayDev);
    }

    public void setOccurrenceBreakProcess(Process occurrenceBreakProcess) {
        this.occurrenceBreakProcess = occurrenceBreakProcess;
    }

    @Override
    public void inAct(Task task) {
        if(task instanceof VerstatTask VerstatTask){
            if (VerstatTask.isInterrupted()) {
                Channel freeChannel = getFreeChannel();
                if (freeChannel != null) {
                    freeChannel.setCurrentTask(VerstatTask);
                    double tnext = super.getTCurr() + VerstatTask.getLeftTime();
                    if(tnext < occurrenceBreakProcess.getTNext()){
                        freeChannel.setTNext(tnext);
                    }else{
                        VerstatTask.setLeftTime(tnext - occurrenceBreakProcess.getTNext());
                        freeChannel.setTNext(occurrenceBreakProcess.getTNext()); // + 0.00000001
                    }

                } else if (queue.size() < getMaxQueueSize()) {
                    queue.add(VerstatTask);
                } else {
                    failures++;
                }
            } else {
                // Звичайна логіка для нових завдань
                Channel freeChannel = getFreeChannel();
                if (freeChannel != null) {
                    freeChannel.setCurrentTask(VerstatTask);

                    double fullTimeToFinishTask  = super.getDelay();
                    VerstatTask.setFullTime(fullTimeToFinishTask);

                    double tnext = super.getTCurr() + fullTimeToFinishTask;
                    if(tnext < occurrenceBreakProcess.getTNext()){
                        freeChannel.setTNext(tnext);
                    }else{
                        VerstatTask.markInterrupted();
                        VerstatTask.setLeftTime(tnext - occurrenceBreakProcess.getTNext());
                        freeChannel.setTNext(occurrenceBreakProcess.getTNext());
                    }

                } else {
                    if (queue.size() < getMaxQueueSize()) {
                        queue.add(VerstatTask);
                    } else {
                        failures++;
                    }
                }
            }
        }
    }

    @Override
    protected void startNextTasks() {
        Channel freeChannel = getFreeChannel();
        while (!queue.isEmpty() && freeChannel != null) {
            Task task = queue.poll();

            if (task instanceof VerstatTask VerstatTask) {
                if (VerstatTask.isInterrupted()) {

                    freeChannel.setCurrentTask(VerstatTask);
                    double tnext = super.getTCurr() + VerstatTask.getLeftTime();
                    if (tnext < occurrenceBreakProcess.getTNext()) {
                        freeChannel.setTNext(tnext);
                    } else {
                        VerstatTask.setLeftTime(tnext - occurrenceBreakProcess.getTNext());
                        freeChannel.setTNext(occurrenceBreakProcess.getTNext());
                    }

                } else {
                    // Звичайна логіка для нових завдань
                    freeChannel.setCurrentTask(VerstatTask);

                    double fullTimeToFinishTask = super.getDelay();
                    VerstatTask.setFullTime(fullTimeToFinishTask);

                    double tnext = super.getTCurr() + fullTimeToFinishTask;
                    if (tnext < occurrenceBreakProcess.getTNext()) {
                        freeChannel.setTNext(tnext);
                    } else {
                        VerstatTask.markInterrupted();
                        VerstatTask.setLeftTime(tnext - occurrenceBreakProcess.getTNext());
                        freeChannel.setTNext(occurrenceBreakProcess.getTNext());
                    }

                }
            }
            freeChannel = getFreeChannel();
        }
    }


    @Override
    public void printInfo() {
        super.printInfo();
        for (int i = 0; i < channels.size(); i++) {
            Channel channel = channels.get(i);
            Task task = channel.getCurrentTask();
            if (task == null) {
                System.out.println("Channel " + i + ": No current task.");
            } else {
                System.out.println("Channel " + i + ": Current task - " + task.getId());
                if (task instanceof VerstatTask VerstatTask) {
                    System.out.println(
                            "Interrupted: " + VerstatTask.isInterrupted() +
                            "  Left time: " + VerstatTask.getLeftTime() +
                            "  Full time: " + VerstatTask.getFullTime());
                }
            }
        }
    }
}
