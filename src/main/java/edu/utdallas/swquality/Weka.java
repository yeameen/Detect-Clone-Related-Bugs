package edu.utdallas.swquality;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author Chowdhury Ashabul Yeameen
 *         <p/>
 *         This is an attempt to test how many original bugs are in the harmful class; what is recall+accuracy on original bugs
 */
public class Weka {

    private static int INDEX_ORIGINAL = 19;
    private static String[] inputFiles = {
            "conservative.arff",
            "bug_check.arff",
            "bug_check_smell.arff"
    };

    public static void main(String[] args) {

        for (String inputFile : inputFiles) {

            try {
                BufferedReader reader = new BufferedReader(
                        new FileReader(inputFile));

                Instances trainingData = new Instances(reader);
                reader.close();

                if (trainingData.classIndex() == -1) {
                    trainingData.setClassIndex(trainingData.numAttributes() - 1);
                }

                // Classifier
                FilteredClassifier classifier = new FilteredClassifier();
                MultilayerPerceptron nn = new MultilayerPerceptron();
                NaiveBayes nb = new NaiveBayes();

                // not using multilayerperceptron as it is susceptible to overfitting
//                classifier.setClassifier(nn);

                classifier.setClassifier(nb);

                // This is necessary to remove the attribute which contains original classification so that it has
                // no impact on the performance
                Remove removeFilter = new Remove();
                removeFilter.setAttributeIndices((INDEX_ORIGINAL + 1) + "");
                classifier.setFilter(removeFilter);

                // Here build and test on same set of data
                classifier.buildClassifier(trainingData);

                int totalBugs = 0;
                int classifiedBugs = 0;
                for (int i = 0; i < trainingData.numInstances(); i++) {
                    String originalClassification = trainingData.instance(i).toString(INDEX_ORIGINAL);
                    double pred = classifier.classifyInstance(trainingData.instance(i));

                    if (originalClassification.equalsIgnoreCase("BUG")) {
                        totalBugs++;
                        if (trainingData.classAttribute().value((int) pred).equalsIgnoreCase("HARMFUL")) {
                            classifiedBugs++;
                        }
                    }

//                    System.out.print(i + " " + originalClassification + ": actual " + trainingData.classAttribute().value((int) trainingData.instance(i).classValue()));
//                    System.out.println(", predicted " + trainingData.classAttribute().value((int) pred));
                }

                // compare with original
                Evaluation eval = new Evaluation(trainingData);
                eval.evaluateModel(classifier, trainingData);
                System.out.println("\n\n" + inputFile);
                System.out.println(eval.toSummaryString("Results\n=======\n", false));

                System.out.println(eval.toMatrixString());

                System.out.println("Total classified bug: " + classifiedBugs);
                System.out.println("Total misclassified bug: " + (totalBugs - classifiedBugs));
                System.out.println("Number of bugs in original: " + (double) totalBugs / trainingData.numInstances() * 100);

                System.out.println("True positive: " + (int) eval.numTruePositives(0));
                System.out.println("False positive: " + (int) eval.numFalsePositives(0));

                int totalOnHarmful = (int) (eval.numTruePositives(0) + eval.numFalsePositives(0));
                System.out.println("Number of bugs in harmful: " + (double) classifiedBugs / totalOnHarmful * 100);


            } catch (Exception e) {
                System.out.println("File not found");
                return;
            }
        }
    }
}
