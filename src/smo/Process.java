package smo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

public class Process extends Element {
    protected final Deque<Task> queue = new ArrayDeque<>();
    protected final ArrayList<Channel> channels = new ArrayList<>();
    protected int failures = 0;
    protected int maxQueueSize = Integer.MAX_VALUE;
    protected double meanQueue = 0.0;
    protected double workTime = 0.0;
    protected double totalLeaveTime = 0.0;
    protected double previousLeaveTime = 0.0;

    protected static class Channel {
        private Task currentTask = null;
        private double tNext = Double.MAX_VALUE;

        public int getState() {
            return currentTask == null ? 0 : 1;
        }

        public Task getCurrentTask() {
            return currentTask;
        }

        public void setCurrentTask(Task currentTask) {
            this.currentTask = currentTask;
        }

        public double getTNext() {
            return tNext;
        }

        public void setTNext(double tNext) {
            this.tNext = tNext;
        }
    }

    public Process(String name, double delayMean) {
        super(name, delayMean);
        channels.add(new Channel());
    }

    public Process(String name, double delayMean, double delayDev) {
        super(name, delayMean, delayDev);
        channels.add(new Channel());
    }

    public void initializeChannelsWithTasks(int tasksNum) {
        tasksNum = Math.min(tasksNum, channels.size());
        for (int i = 0; i < tasksNum; i++) {
            channels.get(i).setCurrentTask(new Task(0.0));
            channels.get(i).setTNext(super.getTCurr() + super.getDelay());
        }
    }

    @Override
    public void inAct(Task task) {
        Channel freeChannel = getFreeChannel();
        if (freeChannel != null) {
            freeChannel.setCurrentTask(task);
            freeChannel.setTNext(super.getTCurr() + super.getDelay());
        } else {
            if (queue.size() < getMaxQueueSize()) {
                queue.add(task);
            } else {
                failures++;
            }
        }
    }

    @Override
    public void outAct() {
        processCurrentTasks();
        startNextTasks();
    }

    protected void processCurrentTasks() {
        ArrayList<Channel> channelsWithMinTNext = getChannelsWithMinTNext();
        for (Channel channel : channelsWithMinTNext) {
            Task task = channel.getCurrentTask();

            Route nextRoute = getNextRoute(task);
            if (nextRoute.isBlocked(task)) {
                continue;
            }

            if (nextRoute.getElement() != null) {
                task.setTimeOut(super.getTCurr());
                nextRoute.getElement().inAct(task);
            }

            channel.setCurrentTask(null);
            channel.setTNext(Double.MAX_VALUE);

            changeQuantity(1);
            totalLeaveTime += super.getTCurr() - previousLeaveTime;
            previousLeaveTime = super.getTCurr();
        }
    }

    protected void startNextTasks() {
        Channel freeChannel = getFreeChannel();
        while (!queue.isEmpty() && freeChannel != null) {
            Task task = queue.poll();
            freeChannel.setCurrentTask(task);
            freeChannel.setTNext(super.getTCurr() + super.getDelay());
            freeChannel = getFreeChannel();
        }
    }

    @Override
    public int getState() {
        int state = 0;
        for (Channel channel : channels) {
            state |= channel.getState();
        }
        return state;
    }

    @Override
    public double getTNext() {
        double tNext = Double.MAX_VALUE;
        for (Channel channel : channels) {
            if (channel.getTNext() < tNext) {
                tNext = channel.getTNext();
            }
        }
        return tNext;
    }

    @Override
    public void setTNext(double tNext) {
        double previousTNext = getTNext();
        for (Channel channel : channels) {
            if (channel.getTNext() == previousTNext) {
                channel.setTNext(tNext);
            }
        }
    }

    protected ArrayList<Channel> getChannelsWithMinTNext() {
        ArrayList<Channel> channelsWithMinTNext = new ArrayList<>();
        double minTNext = Double.MAX_VALUE;
        for (Channel channel : channels) {
            if (channel.getTNext() < minTNext) {
                minTNext = channel.getTNext();
            }
        }
        for (Channel channel : channels) {
            if (channel.getTNext() == minTNext) {
                channelsWithMinTNext.add(channel);
            }
        }
        return channelsWithMinTNext;
    }

    protected Channel getFreeChannel() {
        for (Channel channel : channels) {
            if (channel.getState() == 0) {
                return channel;
            }
        }
        return null;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public int getFailures() {
        return failures;
    }

    public double getMeanQueue() {
        return meanQueue;
    }

    public double getWorkTime() {
        return workTime;
    }

    @Override
    public void doStatistics(double delta) {
        super.doStatistics(delta);
        meanQueue += queue.size() * delta;
        workTime += getState() * delta;
    }

    @Override
    public void printInfo() {
        System.out.println(getName() +
                " state = " + getState() +
                " quantity = " + getQuantity() +
                " tnext = " + getTNext() +
                " failures = " + failures +
                " queue size = " + queue.size()
        );
    }

    public int getQueueSize() {
        return queue.size();
    }
}