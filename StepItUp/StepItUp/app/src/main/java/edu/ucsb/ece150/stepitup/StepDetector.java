package edu.ucsb.ece150.stepitup;
import java.util.ArrayList;
public class StepDetector {
    private static final int WINDOW_SIZE = 1000;
    private ArrayList<Double> sampleList = new ArrayList<>();
    private double threshold = 10.0;
    public StepDetector() { }

    public boolean detectStep(float x, float y, float z) {
        double sample = Math.sqrt(x*x + y*y + z*z);

        sampleList.add(sample);
        if (sampleList.size() > WINDOW_SIZE) {
            sampleList.remove(0);
        }
        double average = calculateAverage(sampleList);
        if (sample > average + threshold) {
            return true;
        } else {
            return false;
        }
    }
    private double calculateAverage(ArrayList<Double> values) {
        double sum = 0.0;
        if (!values.isEmpty()) {
            for (Double value : values) {
                sum += value;
            }
            return sum / values.size();
        }
        return sum;
    }
}
