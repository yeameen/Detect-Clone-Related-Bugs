package edu.utdallas.swquality.util;

import au.com.bytecode.opencsv.CSVReader;
import edu.utdallas.swquality.Category;
import edu.utdallas.swquality.CloneProperties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility functions for generating arff file
 */
public class ArffUtil {

    private static final int INDEX_FEATURE_NAME = 1;

    /**
     * Reads feature data from CSV, populate data structures and return total number of features
     *
     * @param inputFileName     Input CSV Filename
     * @param examples
     * @param featureMap
     * @return                  Total number of features read
     * @throws IOException
     */

    public static int readCSV(String inputFileName, Map<Integer, CloneProperties> examples, Map<Integer, String> featureMap) throws IOException {
        int index = 0;
        CSVReader reader = new CSVReader(new FileReader(new File(inputFileName)));

        if(examples == null) {
            examples = new TreeMap<Integer, CloneProperties>();
        }

        if(featureMap == null) {
            featureMap = new TreeMap<Integer, String>();
        }

        // Discard the heading lines
        reader.readNext();
        reader.readNext();

        String[] lineArrayInput;

        // Iterate over data dimension
        while ((lineArrayInput = reader.readNext()) != null) {
            String featureName = lineArrayInput[INDEX_FEATURE_NAME];

            // If any empty line
            if (featureName == null || featureName.isEmpty()) {
                continue;
            }

            featureMap.put(index, featureName);

            for (Category category : Category.values()) {
                String[] items = lineArrayInput[category.getIndex()].split("[,\\s]+");
                for (String item : items) {
                    if (item.trim().length() == 0) {
                        continue;
                    }
                    int exampleNumber = Integer.parseInt(item.trim());
                    CloneProperties cloneProperties = examples.get(exampleNumber);
                    if (cloneProperties == null) {
                        cloneProperties = new CloneProperties();
                        cloneProperties.setCategory(category);
                    }
                    cloneProperties.addDiffProperties(index);
                    examples.put(exampleNumber, cloneProperties);
                }
            }
            index++;
        }

        return index;
    }

}
