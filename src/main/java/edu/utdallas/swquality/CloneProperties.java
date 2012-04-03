package edu.utdallas.swquality;

import java.util.ArrayList;

/**
 * Each clone
 */
public class CloneProperties {
    ArrayList<Integer> diffProperties;
    Category category;
    float textSim;
    float distance;

    public CloneProperties() {
        diffProperties = new ArrayList<Integer>();
    }

    public ArrayList<Integer> getDiffProperties() {
        return diffProperties;
    }

    public void addDiffProperties(Integer featureNumber) {
        diffProperties.add(featureNumber);
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public float getTextSim() {
        return textSim;
    }

    public void setTextSim(float textSim) {
        this.textSim = textSim;
    }

    public float getDistance()
    {
        return this.distance;
    }

    public void setDistance(float distance)
    {
        this.distance = distance;
    }



    @Override
    public String toString() {
        return "Properties: " + diffProperties.toString() + ", Category: " + category.getName();
    }
}
