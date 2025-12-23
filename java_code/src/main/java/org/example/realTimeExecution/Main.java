package org.example.realTimeExecution;

import org.example.auxiliar.Utilities;
import org.example.config.RuntimeConfig;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Ejecuta el flujo completo de calibración y estimulación en tiempo real:
 * abre los puertos de los sensores y del estimulador, registra las orientaciones
 * iniciales, activa cada canal y guarda las mediciones antes y durante la estimulación.
 * Mantiene el mismo orden de operaciones y tiempos de espera definidos en la
 * implementación original.
 *
 * Precondiciones operativas:
 * <ul>
 *     <li>Los puertos definidos en {@link RuntimeConfig} deben existir y estar libres.</li>
 *     <li>El usuario proporciona por consola frecuencia, ancho de pulso e intensidad válidos.</li>
 * </ul>
 */
public class Main {
    /**
     * Punto de entrada de la aplicación de ejecución en tiempo real.
     */
    public static void main(String[] args) {
        RuntimeConfig runtimeConfig = RuntimeConfig.defaultConfig();

        SerialReaderQuaternion handReader = new SerialReaderQuaternion(runtimeConfig.getHandImuPort());
        SerialReaderQuaternion armReader = new SerialReaderQuaternion(runtimeConfig.getArmImuPort());
        FESController fesController = new FESController(runtimeConfig.getFesPort());

        System.out.println("Configuración parametros de la estimulación \n");
        double frequency = Utilities.readDouble("Establezca la frecuencia: ");
        double pulseWidth = Utilities.readDouble("Establezca el ancho de pulso: ");
        double amplitude = Utilities.readDouble("Establezca la intensidad de corriente: ");

        if (handReader.openPort() && armReader.openPort() && fesController.connect()) {

            for(int channel = runtimeConfig.getChannelPulseStart(); channel <= runtimeConfig.getChannelPulseEnd(); channel++) {
                fesController.setPulseWidth(channel, pulseWidth);
                fesController.setCurrent(channel, amplitude);
            }
            fesController.setFrequency(frequency);


            System.out.println("Encendiendo fuente de alimentacion.......");
            fesController.powerOn();

            for(int i = runtimeConfig.getStimulationLoopStartIndex(); i < runtimeConfig.getStimulationLoopEndIndex(); i++) {

                System.out.println("\n----------- Canal " + (i + 1) + " -----------");

                //1. Tomar medidas iniciales
                handReader.clearData();
                armReader.clearData();

                System.out.println("Tomando mediadas inciales del canal " + (i + 1) + " antes de la estimulación");

                //creo hilos para los dos IMUs
                Thread handThread = new Thread(handReader);
                Thread armThread = new Thread(armReader);
                //inicio ambos hilos
                handThread.start();
                armThread.start();

                try {
                    // Esperar a que ambos hilos terminen antes de continuar
                    handThread.join();
                    armThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Quaternion Qglobal1 = handReader.getMeanQuartenion();
                Quaternion Qglobal2 = armReader.getMeanQuartenion();

                Coord Qglobal1_euler= Qglobal1.toEulerAngles();
                Coord Qglobal2_euler= Qglobal2.toEulerAngles();



                // Imprimir los valores medios obtenidos
                //System.out.println("Mean quartenion for the initial hand position:");
                //System.out.printf("W: %.2f, X: %.2f, Y: %.2f, Z: %.2f\n", Qglobal1.getW(), Qglobal1.getX(), Qglobal1.getY(), Qglobal1.getZ());
                System.out.println("Mean euler angle for the initial hand position:");
                System.out.printf("X=%.2f°, Y=%.2f°, Z=%.2f°\n", Qglobal1_euler.getX(), Qglobal1_euler.getY(), Qglobal1_euler.getZ());

                //System.out.println("Mean quartenion for the initial arm position:");
                //System.out.printf("W: %.2f, X: %.2f, Y: %.2f, Z: %.2f\n", Qglobal2.getW(), Qglobal2.getX(), Qglobal2.getY(), Qglobal2.getZ());
                System.out.println("Mean euler angle for the initial arm position:");
                System.out.printf("X=%.2f°, Y=%.2f°, Z=%.2f°\n", Qglobal2_euler.getX(), Qglobal2_euler.getY(), Qglobal2_euler.getZ());

                saveData(runtimeConfig.getInitialAnglesFilePrefix() + (i + 1) + ".txt", handReader, armReader, Qglobal1, Qglobal2, Qglobal1_euler, Qglobal2_euler, null, null);
                saveDataToPlot(runtimeConfig.getInitialAnglesPlotPrefix() + (i + 1) + ".csv", handReader, armReader);


                //2.Activar canal i
                int[][] mask = new int[runtimeConfig.getMaskLength()][2];
                for (int j = 0; j < mask.length; j++) {
                    mask[j][0] = 0; //resto de canales a 0
                    mask[j][1] = 0;
                }
                mask[i][0] = 1; // Solo el canal actual
                mask[i][1] = 0;

                //3. Mandar máscara
                fesController.setMask(mask);


                //4. Activar la estimulacion
                System.out.println("Iniciando estimulación en el canal " + (i + 1));
                fesController.startStimulation();

                try {
                    Thread.sleep(runtimeConfig.getStimulationDelayMillis()); //esperar un segundo dos segundos de tomar las nuevas mediciones
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Tomando medidas durante le estimulación en el canal " + (i + 1) + "..............");

                //5. Grabar durante la estimulacion
                handReader.clearData();
                armReader.clearData();

                //Volver a iniciar los hilos
                handThread = new Thread(handReader);
                armThread = new Thread(armReader);

                handThread.start();
                armThread.start();

                //6. Para estimulacion y reset la mascara a 0
                fesController.stopStimulation();
                for (int j = 0; j < mask.length; j++) mask[j][0] = 0; // Resetear máscara
                fesController.setMask(mask);

                try {
                    // Esperar a que ambos hilos terminen antes de continuar
                    handThread.join();
                    armThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Quaternion Q1 = handReader.getMeanQuartenion();
                Quaternion Q2 = armReader.getMeanQuartenion();

                Coord Q1_euler = Q1.toEulerAngles();
                Coord Q2_euler = Q2.toEulerAngles();

                Q1 = calculateRotation(Qglobal1,Q1);
                Q2 = calculateRotation(Qglobal2,Q2);

                // Imprimir los valores medios obtenidos
                //System.out.println("Mean quartenions for the hand during FES:");
                //System.out.printf("W: %.2f, X: %.2f, Y: %.2f, Z: %.2f\n", Q1.getW(), Q1.getX(), Q1.getY(), Q1.getZ());
                System.out.println("Mean euler angle for the hand during FES:");
                System.out.printf("X=%.2f°, Y=%.2f°, Z=%.2f°\n", Q1_euler.getX(), Q1_euler.getY(), Q1_euler.getZ());


                //System.out.println("Mean quartenions for the arm during FES:");
                //System.out.printf("W: %.2f, X: %.2f, Y: %.2f, Z: %.2f\n", Q2.getW(), Q2.getX(), Q2.getY(), Q2.getZ());
                System.out.println("Mean euler angle for the arm during FES:");
                System.out.printf("X=%.2f°, Y=%.2f°, Z=%.2f°\n", Q2_euler.getX(), Q2_euler.getY(), Q2_euler.getZ());


                //7. Esperar 3 segundos
                try {
                    Thread.sleep(runtimeConfig.getRestDelayMillis());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                //Calcular rotaciones y guardar datos
                Quaternion Q1_2 = calculateRotation(Q2, Q1);
                Coord rotationAngles = Q1_2.toEulerAngles();

                Quaternion Q2_calibrated = calculateRotation(Qglobal2, Q2);
                Coord pronSupAngles = Q2_calibrated.toEulerAngles();
                saveData(runtimeConfig.getFinalAnglesFilePrefix() + (i + 1) + ".txt", handReader, armReader, Q1, Q2, Q1_euler,Q2_euler, rotationAngles, pronSupAngles);
                saveDataToPlot(runtimeConfig.getFinalAnglesPlotPrefix() + (i + 1) + ".csv", handReader, armReader);


                System.out.println("Channel  " + (i + 1) + ":");
                System.out.println("Hand rotation relative to the arm at the channel " + (i+1));
                /*System.out.printf("Quaternion: W=%.4f, X=%.4f, Y=%.4f, Z=%.4f\n",
                        Q1_2.getW(), Q1_2.getX(),
                        Q1_2.getY(), Q1_2.getZ());*/
                System.out.printf("Euler Angles: X=%.2f°, Y=%.2f°, Z=%.2f°\n",
                        rotationAngles.getX(), rotationAngles.getY(), rotationAngles.getZ());


                System.out.println("Arm rotation at the channel " + (i+1));
                /*System.out.printf("Quaternion: W=%.4f, X=%.4f, Y=%.4f, Z=%.4f\n",
                        Q2_calibrated.getW(), Q2_calibrated.getX(),
                        Q2_calibrated.getY(), Q2_calibrated.getZ());*/
                System.out.printf("Euler Angles: X=%.2f°, Y=%.2f°, Z=%.2f°\n",
                        pronSupAngles.getX(), pronSupAngles.getY(), pronSupAngles.getZ());

                handReader.clearData();
                armReader.clearData();


            }
            System.out.println("Apagando fuente de alimentación......");
            fesController.powerOff();;
            handReader.closePort();
            armReader.closePort();
            fesController.disconnect();


        } else {
            System.out.println("Error al abrir los puertos.");
        }
    }

    /**
     * Guarda en un archivo de texto las muestras completas y los ángulos medios de mano y brazo.
     */
    public static void saveData(String fileName, SerialReaderQuaternion handReader, SerialReaderQuaternion armReader, Quaternion handMean, Quaternion armMean, Coord handMeanEuler, Coord armMeanEuler, Coord rotationAngle, Coord pronSupAngle) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            if(rotationAngle != null && pronSupAngle != null){
                writer.write("Euler Angles for hand rotation relative to Arm):");
                writer.write(String.format("Mean values: X: %.2f, Y: %.2f, Z: %.2f\n", rotationAngle.getX(), rotationAngle.getY(), rotationAngle.getZ()));

                writer.write("Euler Angles for arm pronation-supination rotation):");
                writer.write(String.format("Mean values: X: %.2f, Y: %.2f, Z: %.2f\n", pronSupAngle.getX(), pronSupAngle.getY(), pronSupAngle.getZ()));
            }
            writer.write("Hand data:\n");
            //writer.write("Mean quaternion for the hand position:\n");
            //writer.write(String.format("W: %.2f, X: %.2f, Y: %.2f, Z: %.2f\n", handMean.getW(), handMean.getX(), handMean.getY(), handMean.getZ()));

            writer.write("Mean euler angle for the hand position:");
            writer.write(String.format("X=%.2f°, Y=%.2f°, Z=%.2f°\n", handMeanEuler.getX(), handMeanEuler.getY(), handMeanEuler.getZ()));

            for(Quaternion q : handReader.getData()){
                Coord euler = q.toEulerAngles();
                writer.write(String.format("Roll (x-axis): %.2f°, Pitch (y-axis): %.2f°, Yaw (z-axis): %.2f°\n", euler.getX(), euler.getY(), euler.getZ()));
            }


            writer.write("\nArm data:\n");
            //writer.write("Mean quaternion for the arm position:\n");
            //writer.write(String.format("W: %.2f, X: %.2f, Y: %.2f, Z: %.2f\n", armMean.getW(), armMean.getX(), armMean.getY(), armMean.getZ()));

            writer.write("Mean euler angle for the arm position:");
            writer.write(String.format("X=%.2f°, Y=%.2f°, Z=%.2f°\n", armMeanEuler.getX(), armMeanEuler.getY(),armMeanEuler.getZ()));

            for(Quaternion q : armReader.getData()){
                Coord euler = q.toEulerAngles();
                writer.write(String.format("Roll (x-axis): %.2f°, Pitch (y-axis): %.2f°, Yaw (z-axis): %.2f°\n", euler.getX(), euler.getY(), euler.getZ()));
            }
            //System.out.println("Data saved to " + fileName);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Exporta datos de ambas IMU en un CSV con columnas listas para graficar roll, pitch y yaw.
     */
    public static void saveDataToPlot(String simpleFileName, SerialReaderQuaternion handReader, SerialReaderQuaternion armReader) {
        try (BufferedWriter simpleWriter = new BufferedWriter(new FileWriter(simpleFileName))) {

            // Escribir la cabecera del archivo simplificado
            simpleWriter.write("timestamp roll_hand pitch_hand yaw_hand roll_arm pitch_arm yaw_arm\n");

            // Guardar las muestras de la mano y del brazo en el mismo archivo
            saveSimplifiedSampleData(simpleWriter, handReader, armReader);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Escribe en el {@link BufferedWriter} una fila por muestra sincronizando mano y brazo.
     */
    private static void saveSimplifiedSampleData(BufferedWriter br, SerialReaderQuaternion handReader, SerialReaderQuaternion armReader) throws IOException {
        long startTime = System.currentTimeMillis();  // Marca de tiempo de inicio

        // Suponemos que ambos lectores tienen el mismo número de datos, de lo contrario necesitaríamos manejar el caso donde uno tiene más datos que el otro
        int handDataSize = handReader.getData().size();
        int armDataSize = armReader.getData().size();
        int dataSize = Math.min(handDataSize, armDataSize);  // Aseguramos que no tratemos de acceder a datos fuera de rango

        for (int i = 0; i < dataSize; i++) {
            // Convertir el cuaternión de la mano a ángulos de Euler
            Quaternion handQuaternion = handReader.getData().get(i);
            Coord handEuler = handQuaternion.toEulerAngles();

            // Convertir el cuaternión del brazo a ángulos de Euler
            Quaternion armQuaternion = armReader.getData().get(i);
            Coord armEuler = armQuaternion.toEulerAngles();

            long currentTime = System.currentTimeMillis();
            double time = (currentTime - startTime) / 1000.0; // Convertir a segundos

            // Escribir los datos simplificados en el archivo CSV
            br.write(String.format("%.4f %.2f %.2f %.2f %.2f %.2f %.2f\n",
                    time, handEuler.getX(), handEuler.getY(), handEuler.getZ(), armEuler.getX(), armEuler.getY(), armEuler.getZ()));
        }
    }
    /**
     * Calcula la rotación relativa entre una medida y su referencia global.
     *
     * @param Qglobal cuaternión de referencia.
     * @param Qmean   cuaternión medido.
     * @return rotación relativa {@code Qmean * inverse(Qglobal)}.
     */
    public static Quaternion calculateRotation(Quaternion Qglobal, Quaternion Qmean) {
        return Qmean.multiplication((Qglobal.inversion()));

    }

}
