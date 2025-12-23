package org.example.simulation;

import org.example.auxiliar.Utilities;

import java.util.*;

import org.example.io.ResultWriter;
import org.example.model.BayesStepResult;
import java.util.stream.Collectors;

//Asigna probabilidades iniciales a los pads, gestiona la actualización del algoritmo
public class Simulation {
    private final List<Pad> pads;
    private final DisplacementModel disModel;
    private final ObservationModel obsModel;
    private final double movementThreshold;
    private final double probMin;

    private static final int NUM_COLS = 3;
    private static final int NUM_ROWS = 5;


    public Simulation(double movementThreshold, double probMin, String subject) {
        this.disModel = new DisplacementModel();
        this.obsModel = new ObservationModel();
        this.movementThreshold = movementThreshold;
        this.probMin = probMin;

        this.pads = new ArrayList<>();
        for (int id = 1; id <= NUM_COLS * NUM_ROWS; id++) {
            int padCol = (id - 1) / NUM_ROWS;
            double radius = calculateRadius(padCol);
            pads.add(new Pad(id, radius));

        }
        loadInitialProbs(subject);
        obsModel.loadkTable(subject);

    }

    public double calculateRadius(int col) {
        double forearmCircle = 18.0;
        double radiusCenter = forearmCircle / (2 * Math.PI); //2.86 cm

        if (col == 1) {
            return radiusCenter;
        } else {
            double offSet = 1.5;
            double rSide = Math.sqrt(Math.max(0.0, radiusCenter * radiusCenter - offSet * offSet)); //sqrt(2.86² − 1.5²) = 2.4cm
            return rSide;
        }

    }

    public void loadInitialProbs(String subject) {
        String fileName = "initialK_values_" + subject + ".csv";
        List<Double> initialProbs = Utilities.readProbabilities(fileName);
        for (int i = 0; i < pads.size(); i++) {
            pads.get(i).setInitialProb(initialProbs.get(i));
            pads.get(i).setProbability(initialProbs.get(i));
        }
    }


    public List<Pad> getRegion(Pad pad, double angleDiff, double movementThreshold) {
        double distance = pad.getDisplacementDistance();  // coge la distancia que se ha movido el pad en el que estamos
        double spacing = 1.5; //distancia de centro de un pad al centro de otro (1cm ancho pad + 0.5cm entre pads)

        //nuevo pad ideal en la piel a a partir del pad en el que estoy y el desplazamiento
        double padX = pad.getCol() * spacing;
        double padY = pad.getRow() * spacing;

        //asumo que el pad se mueve arriba o abajo al girar el brazo
        //si angleDiff>0 aumentamos Y en distance, x lo conrtario reducimos Y en distance.
        double newX = padX + distance;
        double newY = padY;
        // region formada x pads cuyo centro (x,y) esté a menos del threshold(ancho de pad) de la nueva localozacion del pad actual
        List<Pad> region = new ArrayList<>();
        for (Pad q : pads) {
            double qx = q.getCol() * spacing;
            double qy = q.getRow() * spacing;
            double dist = Math.hypot(qx - newX, qy - newY); //calculo distancia euclidia
            if (dist <= movementThreshold) {
                region.add(q);
            }
        }
        //si no hay region, devuelvo el propio pad
        if (region.isEmpty()) {
            region.add(pad);
        }
        return region;
    }

    public void updateProbsAfterMovement(double angleDiff) {
        int N = pads.size();

        //cojo prior probs
        double[] prior = new double[N];
        for (int i = 0; i < N; i++) {
            prior[i] = pads.get(i).getProbability();
        }

        disModel.upadteDisplacementDistance(angleDiff, pads);
        double[] pred = new double[N];
        Arrays.fill(pred, 0.0);

        for (int i = 0; i < N; i++) {
            Pad src = pads.get(i);
            List<Pad> region = getRegion(src, angleDiff, movementThreshold);
            double share = prior[i] / region.size(); //p⁺(xᵢ) / |Rᵢ|
            //prior[i] + prior[j]) / n
            for (Pad padInRegion : region) {
                int idx = padInRegion.getId() - 1;   //indice 0-based
                pred[idx] += share;
            }
        }

        double ep = 0.0001;
        for (int i = 0; i < N; i++) { //para los pads que estan fuera de la region la prob va a ser 0.0001
            if (pred[i] == 0.0) {
                pred[i] = ep;
            }
        }

        //normalizamos
        double sum = 0;
        for (double v : pred) sum += v;
        if (sum > 0) {
            for (int i = 0; i < N; i++) {
                pads.get(i).setProbability(pred[i] / sum);
            }
        } else {
            // fallback uniforme
            double u = 1.0 / N;
            for (Pad p : pads) p.setProbability(u);
        }
    }

    //Filtra los pads que superen un umbral mínimo de probabilidad
    //Sobre ese conjuento, caclulo el baricentro (centro de masa) y selecciono los N pads más cercanos al centroide
    public List<Pad> selectPads(int topN) {

        List<Pad> filteredPads = pads.stream() //inicia flujo
                .filter(p -> p.getProbability() >= probMin) //aplica filtro que solo conserva la lista original de pads
                .collect(Collectors.toList()); //recoge el resultado en una nueva lista

        if (filteredPads.isEmpty()) {
            filteredPads = new ArrayList<>();
        }

        //Ccalculo el baricentro
        double sumProbs = 0.0;
        double sumX = 0.0;
        double sumY = 0.0;

        for (Pad p : filteredPads) { //por cada pad
            double prob = p.getProbability();
            sumProbs += prob;
            sumX += p.getCol() * prob;
            sumY += p.getRow() * prob;
        }
        //calcula coordenadas del centroide
        double centroidX = (sumProbs > 0) ? sumX / sumProbs : 0.0; //si condicion verdadera, coordenada  es la division de sumX  entre sumProbs
        double centroidY = (sumProbs > 0) ? sumY / sumProbs : 0.0; //si la condicion es falsa, coordenada es 0.0

        return filteredPads.stream()
                .sorted(Comparator.comparingDouble(p -> //calcula distancias euclideas entre el pad y el centroide
                        Math.hypot(p.getCol() - centroidX, p.getRow() - centroidY)
                ))
                .limit(topN) //deja pasar los top N pads más cercanos al centroide
                .collect(Collectors.toList());
    }

    public SimulationResult runStep(String subject, double angleDiff) {
        int N = pads.size();
        Map<Integer, Double> predictedProbs = new HashMap<>();

        updateProbsAfterMovement(angleDiff);
        for (Pad pad : pads) {
            predictedProbs.put(pad.getId(), pad.getProbability());
        }

        obsModel.applyCorrectionPhase(pads, angleDiff);

        List<BayesStepResult> bayesStepResults = new ArrayList<>(N);
        for (Pad pad : pads) {
            double predictedProb = predictedProbs.getOrDefault(pad.getId(), 0.0);
            BayesStepResult resultRow = new BayesStepResult(
                    pad.getId(),
                    pad.getInitialProb(),
                    pad.getDisplacementDistance(),
                    predictedProb,
                    pad.getProbability()
            );
            bayesStepResults.add(resultRow);
        }

        List<Pad> top3Pads = selectPads(3);

        return new SimulationResult(subject, angleDiff, bayesStepResults, top3Pads);
    }

    public static void main(String[] args) {
        String[] subjectNames = {"Subject1", "Subject2", "Subject3", "Subject4", "Subject5", "Subject6", "Subject7", "Subject8", "Subject9", "Subject10"};
        double movementThreshold = 1;
        double probMin = 0.05;
        double[] angles = {-90.0 ,10.0, 30.0, 45.0, 60.0, 90.0};
        ResultWriter resultWriter = new ResultWriter();

        for (String sub : subjectNames) {
            System.out.println("\n============================================");
            System.out.println("  Procesando: " + sub);
            System.out.println("============================================");

            Simulation s = new Simulation(movementThreshold, probMin, sub);

            for (double ang : angles) {
                s.loadInitialProbs(sub);
                System.out.printf("\n-- %s | Ángulo: %.1f° --\n", sub, ang);
                SimulationResult result = s.runStep(sub, ang);
                resultWriter.writeResults(result);
            }

        }
        //SIMULACION PARA UN SUJETO
        /*Simulation sim = new Simulation(1, 0.05,1);
        double[] angles = {10.0,30.0,45.0, 60.0, 90.0};

        for (double ang : angles) {
            sim.loadInitialProbs();
            System.out.println("\n--- Ángulo: " + ang + "° ---");
            sim.runStep(ang);
        }*/

    }
}