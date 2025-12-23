package org.example.io;

import org.example.model.BayesStepResult;
import org.example.simulation.Pad;
import org.example.simulation.SimulationResult;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

/**
 * Handles persistence of simulation results to CSV while keeping {@link org.example.simulation.Simulation}
 * free of IO concerns.
 */
public class ResultWriter {
    private static final String EXTERNAL_PATH = "C:\\Users\\alemo\\OneDrive\\Documentos\\CEU SAN PABLO\\QUINTO\\TFG\\Materiales\\Algoritmo";

    public void writeResults(SimulationResult simulationResult) {
        String subject = simulationResult.getSubject();
        double angleDiff = simulationResult.getAngleDiff();
        List<BayesStepResult> bayesStepResults = simulationResult.getBayesStepResults();
        List<Pad> topPads = simulationResult.getTopPads();
        int rows = bayesStepResults.size();

        String localFilename = String.format(Locale.US, "results_%s_angle_%.1f.csv", subject, angleDiff);
        String externalFilename = String.format(Locale.US, "%s\\results_%s_angle_%.1f.csv", EXTERNAL_PATH, subject, angleDiff);

        writeCsv(localFilename, bayesStepResults, rows, topPads);
        writeCsv(externalFilename, bayesStepResults, rows, topPads);
    }

    private void writeCsv(String filename, List<BayesStepResult> bayesStepResults, int rows, List<Pad> topPads) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("PadID,InitialProb,Displacement,PredictedProb,CorrectedProb");
            for (int i = 0; i < rows; i++) {
                BayesStepResult row = bayesStepResults.get(i);
                int padId = row.getPadId();
                double initP = row.getInitialProb();
                double disp = row.getDisplacement();
                double pPred = row.getPredictedProb();
                double pPost = row.getCorrectedProb();
                pw.printf(Locale.US, "%d,%.8f,%.8f,%.8f,%.8f%n", padId, initP, disp, pPred, pPost);
            }
            pw.println();
            pw.println("TopPad1,TopPad2,TopPad3");
            String[] topIds = {"", "", ""};
            for (int j = 0; j < topPads.size() && j < 3; j++) {
                topIds[j] = String.valueOf(topPads.get(j).getId());
            }
            pw.printf("%s,%s,%s%n", topIds[0], topIds[1], topIds[2]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
