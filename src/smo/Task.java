package smo;

public class Task {
    private static int nextId = 0;
    private final int id;
    private final double timeIn;
    private double timeOut;

    public Task(double timeIn) {
        this.timeIn = timeIn;
        this.timeOut = timeIn;
        this.id = nextId;
        nextId++;
    }

    public int getId() {
        return id;
    }

    public double getTimeIn() {
        return timeIn;
    }

    public double getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(double timeOut) {
        this.timeOut = timeOut;
    }
}