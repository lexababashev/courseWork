import smo.*;
import smo.Process;
import verstat.*;
import verstatmod.ModelWorkTableMod;
import verstatmod.Process0Mod;
import verstatmod.Process5SolvingBreakdown;

public class Main {
    public static void main(String[] args) {
        //worktable();
        worktablemod();
    }

    public static void worktable(){
        CreateVerstatTasks create = new CreateVerstatTasks("CREATE", 1, 0.0);
        Process0WaitingTask waitingTask = new Process0WaitingTask("PROCESS 0", 0);
        Process setupTask = new Process("PROCESS 1", 0.2, 0.5);
        Process2SolvingTask solvingTask = new Process2SolvingTask("PROCESS 2", 0.5, 0.1);
        Dispose dispose = new Dispose("DISPOSE");


        create.setDistribution("EXP");
        waitingTask.setDistribution("NONE");
        setupTask.setDistribution("UNIF");
        solvingTask.setDistribution("NORM");



        Process waitingBreakdown = new Process("PROCESS 3", 0);
        Process occurrenceBreakdown = new Process("PROCESS 4", 20, 2.0);
        Process solvingBreakdown = new Process("PROCESS 5", 3, 0.75);


        waitingBreakdown.setDistribution("NONE");
        occurrenceBreakdown.setDistribution("NORM");
        solvingBreakdown.setDistribution("ERL");



        solvingTask.setOccurrenceBreakProcess(occurrenceBreakdown);
        waitingBreakdown.initializeChannelsWithTasks(1);



        //маршрут виконання
        create.addRoutes(
                new Route(waitingTask)
        );
        waitingTask.addRoutes(
                new Route(setupTask, (Task task) -> setupTask.getState() == 1 || solvingTask.getState() == 1 || solvingBreakdown.getState() == 1)
        );
        setupTask.addRoutes(
                new Route(solvingTask)
        );
        solvingTask.addRoutes(
                new Route(dispose, 1, 1),
                new Route(waitingTask, 1, 2, (Task task) -> solvingBreakdown.getState() == 0)
        );

        //маршрут поломки
        waitingBreakdown.addRoutes(
                new Route(occurrenceBreakdown, (Task task) -> solvingTask.getState() == 0)
        );
        occurrenceBreakdown.addRoutes(
                new Route(solvingBreakdown, 1, 1),
                new Route(waitingBreakdown, 1, 2, (Task task) -> setupTask.getState() == 0)
        );
        solvingBreakdown.addRoutes(new Route(waitingBreakdown));



        ModelWorkTable model = new ModelWorkTable(
                create, waitingTask, setupTask, solvingTask,
                dispose, waitingBreakdown, occurrenceBreakdown, solvingBreakdown
        );
        model.simulate(1000);
    }

    public static void worktablemod(){
        CreateVerstatTasks create = new CreateVerstatTasks("CREATE", 1, 0.0);
        Process0Mod waitingTask = new Process0Mod("PROCESS 0", 0);
        Dispose disposeSubcontractor = new Dispose("DISPOSE SUBCONTRACTOR");
        Process setupTask = new Process("PROCESS 1", 0.2, 0.5);
        Process2SolvingTask solvingTask = new Process2SolvingTask("PROCESS 2", 0.5, 0.1);
        Dispose dispose = new Dispose("DISPOSE");


        create.setDistribution("EXP");
        waitingTask.setDistribution("NONE");
        setupTask.setDistribution("UNIF");
        solvingTask.setDistribution("NORM");

        Process waitingBreakdown = new Process("PROCESS 3", 0);
        Process occurrenceBreakdown = new Process("PROCESS 4", 20, 2.0);
        Process5SolvingBreakdown solvingBreakdown = new Process5SolvingBreakdown("PROCESS 5", 3, 0.75);


        waitingBreakdown.setDistribution("NONE");
        occurrenceBreakdown.setDistribution("NORM");
        solvingBreakdown.setDistribution("ERL");


        Create createInterrupt = new Create("CREATE INTERRUPTS", 10, 0.0);
        Process search = new Process("PROCESS 6", 3);
        Dispose happenedInterrupts = new Dispose("DISPOSE INTERRUPTS");



        createInterrupt.setDistribution("EXP");
        search.setDistribution("NONE");

        waitingTask.setDisposeSubcontractor(disposeSubcontractor);
        waitingTask.setSolvingBreakdown(solvingBreakdown);
        waitingBreakdown.initializeChannelsWithTasks(1);
        solvingTask.setOccurrenceBreakProcess(occurrenceBreakdown);
        solvingBreakdown.setSearchProcess(search);


        //маршрут виконання
        create.addRoutes(
                new Route(waitingTask)
        );
        waitingTask.addRoutes(
                new Route(
                        setupTask, 1, 1,
                        (Task task) -> setupTask.getState() == 1 || solvingTask.getState() == 1 || solvingBreakdown.getState() == 1),
                new Route(
                        disposeSubcontractor, 1, 2,
                        (Task task) -> !(waitingTask.getQueueSize() > 3 && solvingBreakdown.getState() == 1))
        );
        setupTask.addRoutes(
                new Route(solvingTask)
        );
        solvingTask.addRoutes(
                new Route(dispose, 1, 1),
                new Route(waitingTask, 1, 2, (Task task) -> solvingBreakdown.getState() == 0)
        );


        //маршрут переривання на пошук запчастин
        createInterrupt.addRoutes(
                new Route(search)
        );
        search.addRoutes(
                new Route(happenedInterrupts)
        );

        //маршрут поломки
        waitingBreakdown.addRoutes(
                new Route(occurrenceBreakdown, (Task task) -> solvingTask.getState() == 0)
        );
        occurrenceBreakdown.addRoutes(
                new Route(solvingBreakdown, 1, 1),
                new Route(waitingBreakdown, 1, 2, (Task task) -> setupTask.getState() == 0)
        );
        solvingBreakdown.addRoutes(new Route(waitingBreakdown));




        ModelWorkTableMod model = new ModelWorkTableMod(
                create,waitingTask, disposeSubcontractor,  setupTask, solvingTask,
                dispose, waitingBreakdown, occurrenceBreakdown, solvingBreakdown,
                createInterrupt, search, happenedInterrupts
        );
        model.simulate(1000);
    }
}