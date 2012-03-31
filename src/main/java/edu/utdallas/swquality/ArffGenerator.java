package edu.utdallas.swquality;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

/**
 * Generates Arff file from CSV
 */
public class ArffGenerator {

    private static final int INDEX_FEATURE_NAME = 1;

    private static final String HEADING_BEGINNING = "@RELATION ";
    private static final String HEADING_DATA = "@DATA";
    private static final String DATA_BEGINNING = "{";
    private static final String DATA_ENDING = "}";
    private static final String SPACE_NEWLINE = "\n";

    private static final String FILE_CONFIGURATION = "data/output_classes.yml";

    public static void main(String[] args) throws IOException {
        if (args.length != 1 || args[0].length() == 0) {
            System.err.println("Usage: program <csv filename>");
            return;
        }

        // read configuration file
        Yaml yaml = new Yaml();
        Map<String, Map<String, ArrayList<String>>> classificationBoundaryMap = (Map<String, Map<String, ArrayList<String>>>) yaml.load(new FileInputStream(new File(FILE_CONFIGURATION)));

        for (String classificationBoundaryType : classificationBoundaryMap.keySet()) {

            File inputFile = new File(args[0]);
            CSVReader reader = new CSVReader(new FileReader(inputFile));

            // Discard the heading lines
            reader.readNext();
            String[] lineArrayInput = reader.readNext();

            Map<Integer, CloneProperties> examples = new TreeMap<Integer, CloneProperties>();
            Map<Integer, String> featureMap = new TreeMap<Integer, String>();
            int index = 0;

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

//            for (Integer exampleNumber : examples.keySet()) {
//                System.out.println(exampleNumber + ": " + examples.get(exampleNumber));
//            }

            // Create ARFF
            BufferedWriter writer = new BufferedWriter(new FileWriter(classificationBoundaryType + ".arff"));

            writer.write(HEADING_BEGINNING + "BugFind");
            writer.write(SPACE_NEWLINE);
            writer.write(SPACE_NEWLINE);

            // Write the attributes
            for (Integer featureNumber : featureMap.keySet()) {
                String attributeName = "\"" + featureNumber + " " + featureMap.get(featureNumber) + "\"";
                writer.write("@ATTRIBUTE\t" + attributeName + "\tinteger\n");
            }

            // 2.2.2 class attribute
            writer.write("@ATTRIBUTE\tclass\t");
            writer.write(DATA_BEGINNING);
            writer.write(StringUtils.join(classificationBoundaryMap.get(classificationBoundaryType).keySet(), ","));
            writer.write(DATA_ENDING);

            writer.write(SPACE_NEWLINE);
            writer.write(SPACE_NEWLINE);

            writer.write(HEADING_DATA);
            writer.write(SPACE_NEWLINE);
            for (Integer cloneNumber : examples.keySet()) {
                writer.write(DATA_BEGINNING);

                CloneProperties cloneProperties = examples.get(cloneNumber);
                for (Integer featureNumber : cloneProperties.getDiffProperties()) {
                    writer.write(featureNumber + " 1,");
                }

                // write classification
                // TODO: This is where you I play around
                String classification = cloneProperties.getCategory().toString();
                for (String name : classificationBoundaryMap.get(classificationBoundaryType).keySet()) {
                    Set<String> classes = new HashSet<String>(classificationBoundaryMap.get(classificationBoundaryType).get(name));
                    if(classes.contains(classification)) {
                        writer.write(index + " " + name.toUpperCase());
                    }
                }

                writer.write(DATA_ENDING);
                writer.write(SPACE_NEWLINE);
            }

            writer.close();
        }
    }
}
