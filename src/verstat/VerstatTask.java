package verstat;
import smo.Task;

public class VerstatTask extends Task {
    private boolean isInterrupted;
    private double fullTime;
    private double leftTime;

    public VerstatTask(double timeIn) {
        super(timeIn);
        this.isInterrupted = false;
        this.fullTime = 0.0;
        this.leftTime = 0.0;
    }

    public void markInterrupted() {
        this.isInterrupted = true;
    }

    public boolean isInterrupted() {
        return isInterrupted;
    }

    public double getFullTime() {
        return fullTime;
    }

    public double getLeftTime() {
        return leftTime;
    }

    public void setFullTime(double fullTime) {
        this.fullTime = fullTime;
    }

    public void setLeftTime(double leftTime) {
        this.leftTime = leftTime;
    }
}