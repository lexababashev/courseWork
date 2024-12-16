package smo;
import java.util.ArrayList;
import java.util.Arrays;

public class Model {
    protected final ArrayList<Element> elements;
    protected double tCurr;
    protected double tNext;
    protected int nearestEvent;
    protected boolean isFirst = true;

    public Model(Element... elements) {
        this.elements = new ArrayList<>(Arrays.asList(elements));
        tNext = 0.0;
        tCurr = tNext;
        nearestEvent = 0;
    }

    public void simulate(double time) {
        while (tCurr < time) {
            tNext = Double.MAX_VALUE;
            for (Element element : elements) {
                if ((tCurr < element.getTNext() || isFirst) && element.getTNext() < tNext) {
                    tNext = element.getTNext();
                    nearestEvent = element.getId();
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

    protected void updateBlockedElements() {
        for (Element element : elements) {
            if (element.getTNext() <= tCurr) {
                element.setTNext(tNext);
            }
        }
    }

    public void printResult() {
        System.out.println("\n-------------RESULTS-------------");
        for (Element element : elements) {
            System.out.print("\n");
            element.printResult();

            if (element instanceof Process p) {
                System.out.println("average workload = " + p.getWorkTime() / tCurr);
                System.out.println("failure probability = " + p.getFailures() / (double) (p.getQuantity() + p.getFailures()) + "\n");
                System.out.println("average queue = " + p.getMeanQueue() / tCurr);
            }

        }
    }

    protected void doModelStatistics(double delta) {}
}