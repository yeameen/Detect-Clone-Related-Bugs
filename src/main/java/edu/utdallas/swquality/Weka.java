package edu.utdallas.swquality;

import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

/**
 * @author Chowdhury Ashabul Yeameen
 * <p>
 * This is an attempt to test how many original bugs are in the harmful class; what is recall+accuracy on original bugs
 * </p>
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
                }

                System.out.println("\n" + inputFile + "\n");

                Evaluation eval = new Evaluation(trainingData);

                // Cross validation result
                System.out.println("Cross Validation:\n");
                eval.crossValidateModel(classifier, trainingData, 10, new Random(1));
                System.out.println(eval.toSummaryString("Summary:", false));
                System.out.println(eval.toMatrixString());

                // compare with original
                System.out.println("Validating on training:\n");
                eval = new Evaluation(trainingData);
                eval.evaluateModel(classifier, trainingData);

                System.out.println(eval.toSummaryString("Summary:", false));
                System.out.println(eval.toMatrixString());

                System.out.format("Accuracy on Harmful: %.2f\n", eval.truePositiveRate(0) * 100);
                System.out.format("Accuracy on Benign: %.2f\n", eval.truePositiveRate(1) * 100);
                System.out.println();

                System.out.println("Total classified bug: " + classifiedBugs);
                System.out.println("Total misclassified bug: " + (totalBugs - classifiedBugs));
                System.out.format("Ratio of bugs in original: %.2f %n", 100.0 * totalBugs / trainingData.numInstances());


                int totalOnHarmful = (int) (eval.numTruePositives(0) + eval.numFalsePositives(0));
                double precision = 100.0 * classifiedBugs / totalOnHarmful;
                double recall = 100.0 * classifiedBugs / totalBugs;

                System.out.format("Ratio of bugs in harmful (Precision): %.2f %n", precision);
                System.out.format("Recall: %.2f %n", recall);
                System.out.format("F-score: %.2f\n", 2* precision * recall / (precision + recall));
                System.out.println("-------------------------------------------------------------------------------------------------------------------");

                // NOTE: f-score is not appropriate here
//                System.out.format("F-score: %.2f %n", 2 * precision * recall / (precision + recall));

            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
