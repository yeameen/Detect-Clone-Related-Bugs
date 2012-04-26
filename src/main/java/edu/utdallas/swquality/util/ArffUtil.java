package edu.utdallas.swquality.util;

import au.com.bytecode.opencsv.CSVReader;
import edu.utdallas.swquality.Category;
import edu.utdallas.swquality.CloneProperties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Utility functions for generating arff file
 */
public class ArffUtil {

    private static final int INDEX_FEATURE_NUMBER = 0;
    private static final int INDEX_FEATURE_NAME = 1;
    private static final int DISTANCE_THRESHOLD_LOW = 50;
    private static final int DISTANCE_THRESHOLD_HIGH = 200;

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
            String featureNumber = lineArrayInput[INDEX_FEATURE_NUMBER];
            String featureName = lineArrayInput[INDEX_FEATURE_NAME];

            // If any empty line
            if (featureNumber == null || featureNumber.isEmpty() || featureName == null || featureName.isEmpty()) {
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
         *  @param examples where features will be stored
         *  @throws IOException on FileRead
         *
        * */
    public static void readDejavuOutput(String inputFileName, Map<Integer, CloneProperties> examples) throws IOException {
        Scanner dejavuInputScanner;
        dejavuInputScanner = new Scanner(new FileReader(new File(inputFileName)));

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
                CloneProperties cloneProperties = examples.get(cloneGroup);
                if (cloneProperties == null) {
                    cloneProperties = new CloneProperties();
                }

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
                cloneProperties.setDistance(normalizeDistance(dist));
                cloneProperties.setTextSim(txtSim);

                // read until find buggy differences



                examples.put(cloneGroup, cloneProperties);
                index = dejavuInputScanner.nextLine().indexOf("Potentially buggy differences:");
                while(index < 0)
                {
                    index = dejavuInputScanner.nextLine().indexOf("Potentially buggy differences:");
                }
                //start buggy difference..
                lineInput = dejavuInputScanner.nextLine();
//                System.out.println(cloneGroup + ": " + lineInput);
                index = lineInput.indexOf("-------------------------------------------------------------------------");
                Vector<String> codes = new Vector<String>();
                Vector<String> changes = new Vector<String>();
                while(index < 0 )
                {
                    index = lineInput.indexOf("-------------------------------------------------------------------------");
                    if(index > 0)
                    {
                        break;
                    }
                    while(lineInput.equals(""))
                    {
                        lineInput = dejavuInputScanner.nextLine();
                    }
                    codes.add(lineInput);
                    changes.add(dejavuInputScanner.nextLine());
                    lineInput = dejavuInputScanner.nextLine();
                }
                System.out.println("\t\t\tthis is for clone group " + cloneGroup);
                extractFeatures(codes, changes, cloneProperties);
            }

        }

        dejavuInputScanner.close();
    }

    private static float normalizeDistance(int distance)
    {
        float result;
        if(distance < DISTANCE_THRESHOLD_LOW)
        {
            result = 0;
        }
        else if(distance > DISTANCE_THRESHOLD_HIGH)
        {
            result = 1;
        }
        else
        {
            result = (float)(distance - DISTANCE_THRESHOLD_LOW) / (DISTANCE_THRESHOLD_HIGH - DISTANCE_THRESHOLD_LOW);
        }

        return result;


    }

    private static void extractFeatures(Vector<String> codes, Vector<String> changes, CloneProperties cloneProperties)
    {
        //We place our extraction logic here...

        if(codes.size()!=changes.size())
        {
            System.err.println("UNEXPECTED CASE: codes and changes are of different size");
        }


        //we extract changes from the changed lines
        String changeSeq;
        String codeSeq;
        Vector<String> codeChanges;
        for(int i = 0 ; i < codes.size(); i++)
        {
            //for each changes
            codeChanges = new Vector<String>();

            changeSeq = changes.get(i);
            codeSeq = codes.get(i);
            if(changeSeq.length() > 0)
            {
                String changed = "";
                for(int j = 0 ; j < changeSeq.length(); ++j)
                {
                    char changeChar = changeSeq.charAt(j);
                    if(changeChar == '-' || changeChar == '^')
                    {
                       changed += codeSeq.charAt(j) ;
                    }
                    else
                    {
                        if(!changed.equals(""))
                        {
                            codeChanges.add(changed);
                        }
                        changed = "";
                    }
                }
                if(!changed.equals(""))
                {
                    codeChanges.add(changed);
                }

                //first apply easy filters that can be found only by examining changed segments,
                if(codeSeq.contains("return")   && !codeChanges.contains("return"))
                {
                    addValueToDiffpropertiesInCloneProperties(cloneProperties,7);
                }

                Iterator<String> iterator = codeChanges.iterator();
                //apply filter to each changed "words"
                while(iterator.hasNext())
                {
                    String changedCode = iterator.next();

                    if(changedCode.contains("return"))  // introduction of new return
                    {
                        addValueToDiffpropertiesInCloneProperties(cloneProperties,2);
                    }
                    if(changedCode.contains("&&")   || changedCode.contains("||")  ) //if change contains && or || which means it probably contain
                    {
                        addValueToDiffpropertiesInCloneProperties(cloneProperties,1);
                        addValueToDiffpropertiesInCloneProperties(cloneProperties,12); // and it will also be change in condition
                    }
                    if(changedCode.contains("==")  ) //adding more check for change in logical operator
                    {
                        addValueToDiffpropertiesInCloneProperties(cloneProperties,12); // and it will also be change in condition
                    }
                    else if(codeSeq.contains("=")  ) // if an assignment is found
                    {
                        addValueToDiffpropertiesInCloneProperties(cloneProperties,8); // and it will also be change in condition
                    }
                    if(changedCode.contains("!")   && changedCode.contains("(") && changedCode.contains(")")) //inversion
                    {
                        addValueToDiffpropertiesInCloneProperties(cloneProperties,3);
                    }
                    if(changedCode.contains("if")   || changedCode.contains("for")   || changedCode.contains("while")  )
                    {
                        addValueToDiffpropertiesInCloneProperties(cloneProperties,15);
                    }
                    //else for that '(' will not be part of if-statement or for-statement, but of function...
                    else if(changedCode.contains("(")   || codeSeq.contains(changedCode + "(")  )//if the change itself is function or just functionname changed
                    {
                        addValueToDiffpropertiesInCloneProperties(cloneProperties,4);
                    }
                    if(changedCode.contains("->")  ) //if it is pointer function call,
                    {
                        addValueToDiffpropertiesInCloneProperties(cloneProperties,5);
                    }
                    if(changedCode.contains("NS_RELEASE")   || changedCode.contains("free")    || changedCode.contains("NS_Free")   ) //if it is resource allocation/release function
                    {
                        addValueToDiffpropertiesInCloneProperties(cloneProperties,6);
                    }
                }
            }
        }
    }

    private static void addValueToDiffpropertiesInCloneProperties(CloneProperties cloneProperties, int value)
    {
       if(cloneProperties.getDiffProperties().contains(value))
       {
           return;
       }
        cloneProperties.getDiffProperties().add(6);
    }

}
