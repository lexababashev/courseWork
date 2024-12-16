package smo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Element {
    private static int nextId = 0;
    private final int id;
    private final String name;

    private final ArrayList<Route> routes = new ArrayList<>();
    private String routing = "PRIORITY_CONDITION";

    private String distribution;
    private double delayMean;
    private double delayDev;

    private double tCurr;
    private double tNext;
    private int state = 0;
    private int quantity = 0;

    public Element(String name) {
        this.name = name;
        id = nextId;
        nextId++;
        tNext = Double.MAX_VALUE;
        tCurr = tNext;
        delayMean = 1.0;
        distribution = "NONE";
    }

    public Element(String name, double delayMean) {
        this.name = name;
        id = nextId;
        nextId++;
        tNext = 0.0;
        tCurr = tNext;
        this.delayMean = delayMean;
        distribution = "EXP";
    }

    public Element(String name, double delayMean, double delayDev) {
        this.name = name;
        id = nextId;
        nextId++;
        tNext = 0.0;
        tCurr = tNext;
        this.delayMean = delayMean;
        this.delayDev = delayDev;
        distribution = "NORM";
    }

    public double getDelay() {
        return switch (distribution) {
            case "EXP" -> FunRand.Exponential(delayMean);
            case "UNIF" -> FunRand.Uniform(delayMean, delayDev);
            case "NORM" -> FunRand.Normal(delayMean, delayDev);
            case "ERL" -> FunRand.Erlang(delayMean, delayDev);
            default -> delayMean;
        };
    }

    public Route getNextRoute(Task routedTask) {
        if (routes.isEmpty()) {
            return new Route(null);
        }
        return switch (routing) {
            case "PRIORITY_CONDITION" -> getNextRouteByPriority(routedTask);
            case "PROBABILITY" -> getNextRouteByProbability(routedTask);
            case "COMB" -> getNextRouteCombined(routedTask);
            default -> getNextRouteByPriority(routedTask);
        };
    }

    public void addRoutes(Route... routes) {
        this.routes.addAll(List.of(routes));
        this.routes.sort(Comparator.comparingInt(Route::getPriority).reversed());
    }

    private static ArrayList<Route> getUnblockedRoutes(ArrayList<Route> routes, Task routedTask) {
        ArrayList<Route> unblockedRoutes = new ArrayList<>();
        for (Route route : routes) {
            if (!route.isBlocked(routedTask)) {
                unblockedRoutes.add(route);
            }
        }
        return unblockedRoutes;
    }

    private Route getNextRouteByPriority(Task routedTask) {
        ArrayList<Route> unblockedRoutes = getUnblockedRoutes(routes, routedTask);
        if (unblockedRoutes.isEmpty()) {
            return routes.getFirst();
        }
        return unblockedRoutes.getFirst();
    }

    private static double[] getScaledProbabilities(ArrayList<Route> routes) {
        double[] probabilities = new double[routes.size()];
        for (int i = 0; i < routes.size(); i++) {
            probabilities[i] = routes.get(i).getProbability() + (i == 0 ? 0 : probabilities[i - 1]);
        }
        for (int i = 0; i < probabilities.length; i++) {
            probabilities[i] *= 1 / (probabilities[probabilities.length - 1]);
        }
        return probabilities;
    }

    private Route getNextRouteByProbability(Task routedTask) {
        ArrayList<Route> unblockedRoutes = getUnblockedRoutes(routes, routedTask);
        if (unblockedRoutes.isEmpty()) {
            return routes.getFirst();
        }
        double probability = Math.random();
        double[] scaledProbabilities = getScaledProbabilities(unblockedRoutes);
        for (int i = 0; i < scaledProbabilities.length; i++) {
            if (probability < scaledProbabilities[i]) {
                return unblockedRoutes.get(i);
            }
        }
        return unblockedRoutes.getLast();
    }

    private ArrayList<Route> findRoutesByPriority(int priority) {
        ArrayList<Route> routesByPriority = new ArrayList<>();
        for (Route route : routes) {
            if (route.getPriority() == priority) {
                routesByPriority.add(route);
            }
        }
        return routesByPriority;
    }

    private Route getNextRouteCombined(Task routedTask) {
        Route selectedRoute = null;

        for (Route route : routes) {
            if (!route.isBlocked(routedTask)) {
                selectedRoute = route;
                break;
            }
        }

        if (selectedRoute == null) {
            return routes.getFirst();
        }

        ArrayList<Route> samePriorityRoutes = findRoutesByPriority(selectedRoute.getPriority());
        double probability = Math.random();
        double[] scaledProbabilities = getScaledProbabilities(samePriorityRoutes);

        for (int i = 0; i < scaledProbabilities.length; i++) {
            if (probability < scaledProbabilities[i]) {
                selectedRoute = samePriorityRoutes.get(i);
                break;
            }
        }

        return selectedRoute;
    }

    public void inAct(Task task) {}

    public void outAct() {quantity++;}

    public double getTNext() {return tNext;}

    public void setTNext(double tNext) {this.tNext = tNext;}

    public double getTCurr() {return tCurr;}

    public void setTCurr(double tCurr) {this.tCurr = tCurr;}

    public int getId() {return id;}

    public void setRouting(String routing) {
        this.routing = routing;
    }

    public void setDistribution(String distribution) {this.distribution = distribution;}

    public double getDelayMean() {
        return delayMean;
    }

    public void setDelayMean(double delayMean) {
        this.delayMean = delayMean;
    }

    public double getDelayDev() {
        return delayDev;
    }

    public void setDelayDev(double delayDev) {
        this.delayDev = delayDev;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void changeQuantity(int delta) {
        this.quantity += delta;
    }

    public int getState() {return state;}

    public void setState(int state) {this.state = state;}

    public String getName() {return name;}

    public void printInfo() {
        System.out.println(name + " state = " + getState() + " quantity = " + getQuantity() + " tnext = " + getTNext());
    }

    public void printResult() {
        System.out.println(name + " quantity = " + getQuantity());
    }

    public void doStatistics(double delta) {}

}