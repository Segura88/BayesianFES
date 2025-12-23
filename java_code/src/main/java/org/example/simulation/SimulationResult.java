package org.example.simulation;

import org.example.model.BayesStepResult;

import java.util.List;

/**
 * Immutable snapshot of a single simulation step containing all values needed to persist
 * the baseline CSV without performing any IO from {@link Simulation}.
 */
public class SimulationResult {
    private final String subject;
    private final double angleDiff;
    private final List<BayesStepResult> bayesStepResults;
    private final List<Pad> topPads;

    public SimulationResult(String subject,
                            double angleDiff,
                            List<BayesStepResult> bayesStepResults,
                            List<Pad> topPads) {
        this.subject = subject;
        this.angleDiff = angleDiff;
        this.bayesStepResults = bayesStepResults;
        this.topPads = topPads;
    }

    public String getSubject() {
        return subject;
    }

    public double getAngleDiff() {
        return angleDiff;
    }

    public List<BayesStepResult> getBayesStepResults() {
        return bayesStepResults;
    }

    public List<Pad> getTopPads() {
        return topPads;
    }
}
