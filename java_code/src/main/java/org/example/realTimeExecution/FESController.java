package org.example.realTimeExecution;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Gestiona la comunicación serie con el dispositivo de estimulación funcional (FES).
 * Permite abrir la conexión, enviar comandos de configuración y controlar el inicio o
 * parada de la estimulación sobre distintos canales.
 */
public class FESController {
    private SerialPort serialPort;
    private String portName;
    private static final int BAUD_RATE = 9600;

    /**
     * Crea un controlador asociado a un nombre de puerto.
     *
     * @param portName identificador del puerto serie donde está conectado el FES
     */
    public FESController(String portName) {

        this.portName = portName;
    }

    /**
     * Abre el puerto configurando los parámetros de comunicación.
     *
     * @return {@code true} si el puerto se abre correctamente; {@code false} en caso contrario.
     */
    public boolean connect() {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(BAUD_RATE); //velocidad de transmisionde bits
        serialPort.setNumDataBits(8); //8 bits de datos por cada paquete de comunicaion
        serialPort.setNumStopBits(1); //1 bit de parada para idnicar el final de un paquete de datos
        serialPort.setParity(SerialPort.NO_PARITY); //sin paridad (no se detectan errores en la transmisión

        if (serialPort.openPort()) {
            System.out.println("Conectado al dispositivo FES en " + portName);
            return true;
        } else {
            System.out.println("Error al conectar con el dispositivo " + portName);
            return false;
        }
    }


    /**
     * Enciende la fuente de alimentación número 2 del dispositivo FES.
     */
    public void powerOn() {
        String command = "on2\r"; //escribe en el dispostivo, refiriendose a la fuente de alimentacion 2, y la activa

        try {
            sendCommand(command);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Fuente de alimentación encendida");
    }

    /**
     * Apaga la fuente de alimentación número 2 del dispositivo FES.
     */
    public void powerOff() {
        String command = "off2\r"; //escribe en el dispostivo, refiriendose a la fuente de alimentacion 2, y la desactiva

        try {
            sendCommand(command);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Fuente de alimentación apagada");
    }

    /**
     * Inicia la estimulación sobre los canales activos de la máscara actual.
     */
    public void startStimulation() {
        String command = "s\r";
        try {
            sendCommand(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Estimulación iniciada");
    }

    /**
     * Detiene la estimulación en curso.
     */
    public void stopStimulation() {
        String command = "p\r";
        try {
            sendCommand(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Estimulación detenida");
    }

    /**
     * Cierra el puerto serie si está abierto.
     */
    public void disconnect() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Conexion cerrada");
        }
    }

    /**
     * Envía al FES la máscara de canales a activar.
     *
     * @param mask matriz de 32x2 donde {@code mask[j][0]} indica si el canal está activo y
     *             {@code mask[j][1]} el índice con el que se envía al dispositivo.
     */
    public void setMask(int[][] mask) {

        for (int j = 0; j < mask.length; j++) {
            if (mask[j][0] == 1) { // Canal activo
                String command = "e lc " + (j + 1) + " " + mask[j][1] + "\r";  // Canal y estado

                try {
                    System.out.println("Preparando comando: " + command);
                    sendCommand(command);

                    // Para depuración (opcional)
                    System.out.println("Comando enviado: " + command);
                    System.out.println("Canal " + (j + 1) + " añadido a la lista !!!\n");
                } catch (IOException e) {
                    throw new RuntimeException("Error enviando comando al canal " + j, e);
                }
            }
        }

    }

    /**
     * Configura la frecuencia global de estimulación.
     *
     * @param frequency frecuencia en Hz.
     * @throws IllegalArgumentException si la frecuencia no es positiva.
     */
    public void setFrequency(double frequency) {
        if (frequency <= 0) {
            throw new IllegalArgumentException("La frecuencia debe ser mayor que 0");
        }

        int timeMs = (int) (1.0 / frequency / 0.0005); // convierte Hz a tiempo en ms
        String command = "e tg " + timeMs + "\r";

        try {
            sendCommand(command);
            System.out.println("Frecuencia configurada: " + frequency + " Hz (tiempo: " + timeMs + ")");
        } catch (IOException e) {
            throw new RuntimeException("Error al enviar el comando de frecuencia", e);
        }
    }

    /**
     * Configura el ancho de pulso positivo para un canal específico.
     *
     * @param channel    número de canal dirigido.
     * @param pulseWidth ancho de pulso en microsegundos.
     * @throws IllegalArgumentException si el ancho de pulso es inferior al mínimo admitido.
     */
    public void setPulseWidth(int channel, double pulseWidth) {
        // Convierte el ancho de pulso (mínimo 27.6 ms, paso de 2.4 ms)
        int pulseValue = (int) ((pulseWidth - 27.6) / 2.4);

        if (pulseValue < 0) {
            throw new IllegalArgumentException("El ancho de pulso debe ser mayor o igual a 27.6 ms");
        }

        String command = "w " + channel + " tp " + pulseValue + "\r";

        try {
            sendCommand(command);
            System.out.println("Ancho de pulso positivo configurado para canal " + channel + ": " + pulseWidth + " ms (valor: " + pulseValue + ")");
        } catch (IOException e) {
            throw new RuntimeException("Error al enviar el comando de ancho de pulso", e);
        }
    }

    /**
     * Configura la corriente positiva para un canal específico.
     *
     * @param channel   número de canal dirigido.
     * @param amplitude amplitud en miliamperios.
     * @throws IllegalArgumentException si la amplitud es negativa.
     */
    public void setCurrent(int channel, double amplitude) {
        // Convierte la amplitud (paso de 0.78 mA)
        int currentValue = (int) (amplitude / 0.78);

        if (currentValue < 0) {
            throw new IllegalArgumentException("La amplitud debe ser mayor o igual a 0");
        }

        String command = "w " + channel + " ap " + currentValue + "\r";

        try {
            sendCommand(command);
            System.out.println("Corriente positiva configurada en canal " + channel + ": " + amplitude + " mA (valor: " + currentValue + ")");
        } catch (IOException e) {
            throw new RuntimeException("Error al enviar el comando de corriente", e);
        }
    }

    /**
     * Envía un comando textual al FES y muestra la respuesta devuelta por el dispositivo.
     *
     * @param command comando terminado en retorno de carro según el protocolo del FES.
     * @throws IOException si ocurre un problema al escribir por el puerto serie.
     */
    private void sendCommand(String command) throws IOException {
        serialPort.writeBytes(command.getBytes(), command.length()); //convierte el comando en un array de bits y lo envia por el puerto serie, indica la cantidad de bytes a enviar
        try {
            Thread.sleep(200); // Pequeña espera para recibir la respuesta
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        byte[] buffer = new byte[20];  // Tamaño del buffer mayor para que cubra cualquier respuesta posible
        int numBytes = serialPort.readBytes(buffer, buffer.length); // Lee los bytes desde el puerto serie

        if (numBytes > 0) {
            String response = new String(buffer, 0, numBytes, StandardCharsets.UTF_8).trim();  // Convierte bytes a string
            System.out.println("Respuesta: \"" + response + "\"");


        } else {
            System.out.println("No se recibió respuesta del dispositivo.");
        }
    }

    public static void main(String[] args) {
        String portName = "COM14";
        FESController fes = new FESController(portName);

        if (fes.connect()) {
            try {
                int pause = 100; // ms entre comandos

                double amplitud = 2.0;         // mA
                double anchoPulso = 250.0;     // µs
                double frecuencia = 30.0;      // Hz

                int ap = (int) (amplitud / 0.78);
                int tp = (int) ((anchoPulso - 27.6) / 2.4);
                int tg = (int) (1.0 / frecuencia / 0.0005);

                fes.powerOn();
                Thread.sleep(200);

                // Estimulación canal por canal
                for (int canal = 16; canal <= 32; canal++) {
                    System.out.println(">>> Estimulando canal " + canal);

                    // Configura amplitud y ancho de pulso para este canal
                    fes.sendCommand("w " + canal + " ap " + ap + "\r");
                    fes.sendCommand("w " + canal + " an " + ap + "\r");
                    fes.sendCommand("w " + canal + " tp " + tp + "\r");
                    fes.sendCommand("w " + canal + " tn " + tp + "\r");
                    Thread.sleep(pause);

                    // Establece lista de canales activos (solo este canal)
                    fes.sendCommand("e lc " + canal + " 0\r"); // índice 0
                    fes.sendCommand("e fl 0\r");              // fin de lista en índice 0
                    Thread.sleep(pause);

                    // Inicia estimulación
                    fes.startStimulation();
                    Thread.sleep(2000); // Estimula por 2 segundos

                    // Detiene estimulación antes del siguiente canal
                    fes.stopStimulation();
                    Thread.sleep(500); // Pausa entre canales
                }
                fes.powerOff();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                fes.disconnect();
            }
        } else {
            System.out.println("No se pudo conectar al dispositivo FES.");
        }
    }
}
