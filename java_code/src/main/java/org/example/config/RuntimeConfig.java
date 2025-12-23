package org.example.config;

/**
 * Holds default runtime parameters for real-time execution and data capture.
 * <p>
 * All fields are immutable to avoid accidental drift between the stimulation
 * loop and the IMU sampling routines. The defaults mirror the current values
 * used in the real-time workflow so behavior remains unchanged unless an
 * explicit configuration instance is provided by the caller.
 */
public final class RuntimeConfig {
    private final String handImuPort;
    private final String armImuPort;
    private final String fesPort;
    private final int channelPulseStart;
    private final int channelPulseEnd;
    private final int stimulationLoopStartIndex;
    private final int stimulationLoopEndIndex;
    private final int maskLength;
    private final int stimulationDelayMillis;
    private final int restDelayMillis;
    private final int imuReadDurationMillis;
    private final String initialAnglesFilePrefix;
    private final String finalAnglesFilePrefix;
    private final String initialAnglesPlotPrefix;
    private final String finalAnglesPlotPrefix;
    private final String imuDataFolder;
    private final String initialAnglesPlotFile;

    private RuntimeConfig(String handImuPort,
                          String armImuPort,
                          String fesPort,
                          int channelPulseStart,
                          int channelPulseEnd,
                          int stimulationLoopStartIndex,
                          int stimulationLoopEndIndex,
                          int maskLength,
                          int stimulationDelayMillis,
                          int restDelayMillis,
                          int imuReadDurationMillis,
                          String initialAnglesFilePrefix,
                          String finalAnglesFilePrefix,
                          String initialAnglesPlotPrefix,
                          String finalAnglesPlotPrefix,
                          String imuDataFolder,
                          String initialAnglesPlotFile) {
        this.handImuPort = handImuPort;
        this.armImuPort = armImuPort;
        this.fesPort = fesPort;
        this.channelPulseStart = channelPulseStart;
        this.channelPulseEnd = channelPulseEnd;
        this.stimulationLoopStartIndex = stimulationLoopStartIndex;
        this.stimulationLoopEndIndex = stimulationLoopEndIndex;
        this.maskLength = maskLength;
        this.stimulationDelayMillis = stimulationDelayMillis;
        this.restDelayMillis = restDelayMillis;
        this.imuReadDurationMillis = imuReadDurationMillis;
        this.initialAnglesFilePrefix = initialAnglesFilePrefix;
        this.finalAnglesFilePrefix = finalAnglesFilePrefix;
        this.initialAnglesPlotPrefix = initialAnglesPlotPrefix;
        this.finalAnglesPlotPrefix = finalAnglesPlotPrefix;
        this.imuDataFolder = imuDataFolder;
        this.initialAnglesPlotFile = initialAnglesPlotFile;
    }

    /**
     * Provides the default runtime configuration matching the current behavior.
     *
     * @return configuration with default ports, timings, and file locations
     */
    public static RuntimeConfig defaultConfig() {
        return new RuntimeConfig(
                "COM10",
                "COM13",
                "COM14",
                16,
                32,
                15,
                31,
                32,
                2000,
                3000,
                7000,
                "initialAngles_Channel_",
                "finalAngles_Channel_",
                "C:\\Users\\alemo\\IdeaProjects\\getIMU\\initialAngles_Channel_",
                "C:\\Users\\alemo\\IdeaProjects\\getIMU\\finalAngles_Channel_",
                "C:/Users/alemo/IdeaProjects/getIMU/data/",
                "C:\\Users\\alemo\\IdeaProjects\\getIMU\\initial_angles.csv"
        );
    }

    public String getHandImuPort() {
        return handImuPort;
    }

    public String getArmImuPort() {
        return armImuPort;
    }

    public String getFesPort() {
        return fesPort;
    }

    /**
     * First channel index stimulated in the sweep.
     *
     * @return inclusive starting channel
     */
    public int getChannelPulseStart() {
        return channelPulseStart;
    }

    /**
     * Last channel index stimulated in the sweep.
     *
     * @return inclusive ending channel
     */
    public int getChannelPulseEnd() {
        return channelPulseEnd;
    }

    /**
     * First index of the loop that iterates over stimulation channels for data capture.
     *
     * @return inclusive starting index for the stimulation loop
     */
    public int getStimulationLoopStartIndex() {
        return stimulationLoopStartIndex;
    }

    /**
     * Last index of the loop that iterates over stimulation channels for data capture.
     *
     * @return inclusive ending index for the stimulation loop
     */
    public int getStimulationLoopEndIndex() {
        return stimulationLoopEndIndex;
    }

    /**
     * Number of positions in the stimulation mask that are iterated when applying the channel window.
     *
     * @return mask size in positions
     */
    public int getMaskLength() {
        return maskLength;
    }

    /**
     * Delay applied before starting stimulation, matching the existing timing assumptions.
     *
     * @return delay in milliseconds prior to triggering the stimulator
     */
    public int getStimulationDelayMillis() {
        return stimulationDelayMillis;
    }

    /**
     * Delay applied after stimulation to allow the subject to rest.
     *
     * @return rest interval in milliseconds between channel iterations
     */
    public int getRestDelayMillis() {
        return restDelayMillis;
    }

    /**
     * Duration of IMU sampling during each calibration or stimulation window.
     *
     * @return sampling duration in milliseconds
     */
    public int getImuReadDurationMillis() {
        return imuReadDurationMillis;
    }

    public String getInitialAnglesFilePrefix() {
        return initialAnglesFilePrefix;
    }

    public String getFinalAnglesFilePrefix() {
        return finalAnglesFilePrefix;
    }

    public String getInitialAnglesPlotPrefix() {
        return initialAnglesPlotPrefix;
    }

    public String getFinalAnglesPlotPrefix() {
        return finalAnglesPlotPrefix;
    }

    public String getImuDataFolder() {
        return imuDataFolder;
    }

    public String getInitialAnglesPlotFile() {
        return initialAnglesPlotFile;
    }
}
