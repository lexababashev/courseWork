package smo;

public class Create extends Element {
    private int failures = 0;

    public Create(String name, double delay, double initialTNext) {
        super(name, delay);
        super.setTNext(initialTNext);
    }

    protected Task createTask() {
        return new Task(super.getTCurr());
    }

    @Override
    public void outAct() {
        super.outAct();
        super.setTNext(super.getTCurr() + super.getDelay());
        Task createdTask = createTask();
        Route nextRoute = super.getNextRoute(createdTask);
        if (nextRoute.getElement() == null || nextRoute.isBlocked(createdTask)) {
            failures++;
        } else {
            nextRoute.getElement().inAct(createdTask);
        }
    }

    public int getFailures() {return failures;}

    public void setFailures (int failures) {this.failures = failures;}
}