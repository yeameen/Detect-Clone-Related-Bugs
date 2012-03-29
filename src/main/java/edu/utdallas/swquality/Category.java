package edu.utdallas.swquality;

/**
 * Classification categories
 */
public enum Category {
    BUG("Bug", 4), SMELL("Smell", 5), STYLE("Style", 6), CHECK("Check", 7), FALSE("False", 8);

    private final String name;
    private final int index;

    Category(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

}
