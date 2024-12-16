package verstat;

import smo.Create;
import smo.Task;


public class CreateVerstatTasks extends Create {
    public CreateVerstatTasks(String name, double delay, double initialTNext) {
        super(name, delay, initialTNext);
    }

    @Override
    protected Task createTask() {
        return new VerstatTask(super.getTCurr());
    }
}
