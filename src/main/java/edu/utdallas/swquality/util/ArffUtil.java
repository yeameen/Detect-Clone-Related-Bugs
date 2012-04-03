package edu.utdallas.swquality.util;

import au.com.bytecode.opencsv.CSVReader;
import edu.utdallas.swquality.Category;
import edu.utdallas.swquality.CloneProperties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
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


        /**
        *  Reads output from dejavu and populate examples with similarity
         *
         *  @param inputFileName Input Dejavu File (expecting .txt format)
         *  @param examples
         *  @throws IOException
         *
        * */
    public static void readDejavuOutput(String inputFileName, Map<Integer, CloneProperties> examples) throws IOException {
        Scanner dejavuInputScanner = new Scanner(new FileReader(new File(inputFileName)));

        if(examples == null)
        {
            examples = new TreeMap<Integer, CloneProperties>();
        }

        String lineInput;
        String[] parsed;
        int cloneGroup; //same as exampleNumber in CSVReader.
        float txtSim;
        int dist;


        //we assume sequential scanning and proper ordering in file
        while(dejavuInputScanner.hasNext())
        {
            lineInput = dejavuInputScanner.nextLine();
            int index = lineInput.indexOf("Clone group ");
            if(index > 0)
            {
                parsed = lineInput.split(" ");
                cloneGroup = Integer.parseInt(parsed[3].substring(0, parsed[3].length()-1));

                //read until attributes shows,
                dejavuInputScanner.nextLine();
                lineInput = dejavuInputScanner.nextLine();
                parsed = lineInput.split(" ");

                txtSim = Float.parseFloat(parsed[4]);
                if(parsed[parsed.length-1].equals("inf"))
                {
                    dist = Integer.MAX_VALUE;
                }
                else
                {
                    dist = Integer.parseInt(parsed[parsed.length-1]);
                }
                //x
                // System.out.println(cloneGroup + " sim: " + txtSim + ", dist: " + dist);
                CloneProperties cloneProperties = examples.get(cloneGroup);
                if (cloneProperties == null) {
                    cloneProperties = new CloneProperties();
                }
                cloneProperties.setDistance(dist);
                cloneProperties.setTextSim(txtSim);
                examples.put(cloneGroup, cloneProperties);
            }

        }

        dejavuInputScanner.close();
    }

}
