package verstatmod;

import smo.Task;
import smo.Process;

public class Process5SolvingBreakdown extends Process {
    private Process searchProcess = null;
    private int interrupts = 0;

    public Process5SolvingBreakdown(String name, double delayMean, double delayDev) {
        super(name, delayMean, delayDev);
    }

    public void setSearchProcess(Process searchProcess) {
        this.searchProcess = searchProcess;
    }

    public int getInterrupts() {
        return interrupts;
    }

    @Override
    public void inAct(Task task) {
        Channel freeChannel = getFreeChannel();
        if (freeChannel != null) {
            freeChannel.setCurrentTask(task);
            if (searchProcess.getState() == 0){
                freeChannel.setTNext(super.getTCurr() + super.getDelay());
            }else{
                double planned = super.getTCurr() + super.getDelay();
                freeChannel.setTNext(planned + 3);
                interrupts++;
                System.out.println("INTERRUPTION IN BREAKDOWN" + " planned: " + planned + " fact: " + freeChannel.getTNext());
            }

        } else {
            if (queue.size() < getMaxQueueSize()) {
                queue.add(task);
            } else {
                failures++;
            }
        }
    }

    @Override
    protected void startNextTasks() {
        Channel freeChannel = getFreeChannel();
        while (!queue.isEmpty() && freeChannel != null) {
            Task task = queue.poll();
            freeChannel.setCurrentTask(task);
            if (searchProcess.getState() == 0){
                freeChannel.setTNext(super.getTCurr() + super.getDelay());
            }else{
                double planned = super.getTCurr() + super.getDelay();
                freeChannel.setTNext(planned + 3);
                interrupts++;
                System.out.println("INTERRUPTION IN BREAKDOWN" + " planned: " + planned + " fact: " + freeChannel.getTNext());
            }
            freeChannel = getFreeChannel();
        }
    }
}
