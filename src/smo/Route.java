package smo;

public class Route {
    private final Element element;
    private int priority = 0;
    private double probability = 1.0;
    private Block block = null;

    public Route(Element element) {
        this.element = element;
    }

    public Route(Element element, Block block) {
        this.element = element;
        this.block = block;
    }

    public Route(Element element, double probability) {
        this.element = element;
        this.probability = probability;
    }

    public Route(Element element, double probability, int priority) {
        this.element = element;
        this.probability = probability;
        this.priority = priority;
    }

    public Route(Element element, double probability, int priority, Block block) {
        this.element = element;
        this.probability = probability;
        this.priority = priority;
        this.block = block;
    }

    public boolean isBlocked(Task task) {
        if (block == null) {
            return false;
        }
        try {
            return block.call(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    public interface Block {
        Boolean call(Task task);
    }

    public Element getElement() {
        return element;
    }

    public int getPriority() {
        return priority;
    }

    public double getProbability() {
        return probability;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

}