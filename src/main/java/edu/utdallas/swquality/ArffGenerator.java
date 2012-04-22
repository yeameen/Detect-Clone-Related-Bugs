package edu.utdallas.swquality;

import edu.utdallas.swquality.util.ArffUtil;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;

/**
 * Generates Arff file from CSV
 */
public class ArffGenerator {

    private static final String HEADING_BEGINNING = "@RELATION ";
    private static final String HEADING_DATA = "@DATA";
    private static final String DATA_BEGINNING = "{";
    private static final String DATA_ENDING = "}";
    private static final String SPACE_NEWLINE = "\n";

    private static final String FILE_CONFIGURATION = "conf/output_classes.yml";

    public static void main(String[] args) throws IOException {
        if (args.length != 2 || args[0].length() == 0 || args[1].length() == 0) {
            System.err.println("Usage: program <csv filename> <dejavu filename>");
            return;
        }

        Map<Integer, CloneProperties> examples = new TreeMap<Integer, CloneProperties>();
        Map<Integer, String> featureMap = new TreeMap<Integer, String>();

        int classificationIndex = ArffUtil.readCSV(args[0], examples, featureMap);
        //TODO: pretty sure this is misnomer, but for now...
        ArffUtil.readDejavuOutput(args[1],examples);

        // read configuration file
        Yaml yaml = new Yaml();
        Map<String, Map<String, ArrayList<String>>> classificationBoundaryMap = (Map<String, Map<String, ArrayList<String>>>) yaml.load(new FileInputStream(new File(FILE_CONFIGURATION)));

        for (String classificationBoundaryType : classificationBoundaryMap.keySet()) {

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
            writer.write("@ATTRIBUTE\t\"TxtSim\"\tnumeric\n");
            writer.write("@ATTRIBUTE\t\"Distance\"\tnumeric\n");

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
                CloneProperties cloneProperties = examples.get(cloneNumber);

                // write classification
                Category classifiedCategory= cloneProperties.getCategory();
                if(classifiedCategory != null)
                {
                    writer.write(DATA_BEGINNING);

                    for (Integer featureNumber : cloneProperties.getDiffProperties()) {
                        writer.write(featureNumber + " 1,");
                    }

                    writer.write(classificationIndex + " " + examples.get(cloneNumber).getTextSim() + ",");
                    writer.write(classificationIndex+1 + " " + examples.get(cloneNumber).getDistance() + ",");
                    String classification = cloneProperties.getCategory().toString();
                    for (String name : classificationBoundaryMap.get(classificationBoundaryType).keySet()) {
                        Set<String> classes = new HashSet<String>(classificationBoundaryMap.get(classificationBoundaryType).get(name));
                        if(classes.contains(classification)) {
                            writer.write(classificationIndex+2 + " " + name.toUpperCase());
                        }
                    }
                    writer.write(DATA_ENDING);
                    writer.write(SPACE_NEWLINE);
                }


            }

            writer.close();
        }
    }
}
