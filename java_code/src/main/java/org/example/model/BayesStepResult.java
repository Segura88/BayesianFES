package org.example.model;

/**
 * Immutable data transfer object for a single pad row in the Bayesian filtering step.
 */
public class BayesStepResult {
    private final int padId;
    private final double initialProb;
    private final double displacement;
    private final double predictedProb;
    private final double correctedProb;

    public BayesStepResult(int padId,
                           double initialProb,
                           double displacement,
                           double predictedProb,
                           double correctedProb) {
        this.padId = padId;
        this.initialProb = initialProb;
        this.displacement = displacement;
        this.predictedProb = predictedProb;
        this.correctedProb = correctedProb;
    }

    public int getPadId() {
        return padId;
    }

    public double getInitialProb() {
        return initialProb;
    }

    public double getDisplacement() {
        return displacement;
    }

    public double getPredictedProb() {
        return predictedProb;
    }

    public double getCorrectedProb() {
        return correctedProb;
    }
}
