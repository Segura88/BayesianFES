# BayesianFES

BayesianFES implements the Bayesian algorithm used to predict stimulation pad patterns for a surface FES (sFES) grid and to log motion data from inertial sensors. The repository contains two main parts:

- **Java (`java_code/`)**
  - **`simulation/`**: runs the Bayesian update step over a grid of pads using movement and observation models to generate CSV logs for later analysis.
  - **`realTimeExecution/`**: captures quaternion data from two IMUs, drives the TEREFES stimulator, and stores both raw samples and averaged orientations during calibration and stimulation.
  - **`auxiliar/`**: shared utilities for file IO and console prompts.
- **MATLAB scripts (`matlab_scripts/`)**: plotting and metrics utilities; each script has a companion `_README.md` next to it.

## System architecture
- **Acquisition (IMU)**: `realTimeExecution.SerialReaderQuaternion` reads quaternions from the hand and arm IMUs over serial ports, averages samples, and exports TXT/CSV logs.
- **Bayesian filter**: `simulation.Simulation` orchestrates the displacement model (`DisplacementModel`), the observation model (`ObservationModel`), and pad state to compute predicted and corrected probabilities per grid pad.
- **Control (FES)**: `realTimeExecution.FESController` opens the stimulator serial port, configures pulse parameters, applies channel masks, and starts/stops stimulation.
- **Logging**:
  - Simulation mode uses `io.ResultWriter` to emit CSV files containing the Bayesian step results.
  - Real-time tools write TXT and CSV files with quaternion samples and Euler angles for each stimulation channel.

## How to run
### Simulation
1. Place the subject-specific files in the working directory:
   - `initialK_values_<subject>.csv` for initial probabilities.
   - `Kstable_<subject>.csv` for observation likelihoods.
2. Instantiate `simulation.Simulation` with the movement threshold, probability floor, and subject ID.
3. For each movement angle (degrees), call `runStep(subject, angleDiff)` to obtain a `SimulationResult`.
4. Pass the returned `SimulationResult` to `io.ResultWriter.writeSimulationResults(...)` to create the CSV (same format as the original implementation).

### Real-time execution
1. Connect both IMUs and the TEREFES stimulator to the configured serial ports.
2. Run `realTimeExecution.Main` and enter the requested pulse parameters (frequency, pulse width, amplitude).
3. The program will iterate through channels 16–32, capturing initial orientations, stimulating the selected channel, and logging measurements before/during stimulation.
4. Outputs are written to the configured paths in `config.RuntimeConfig` (TXT logs with mean angles and per-sample Euler angles plus CSV files for plotting).

## Output formats
### Simulation CSV
- Header: `PadID,InitialProb,Displacement,PredictedProb,CorrectedProb`
- One row per pad, followed by a blank line and a `TopPad1,TopPad2,TopPad3` section listing the IDs with the highest corrected probabilities.
- Column meaning (per pad row):
  - **PadID**: 1-based index of the pad within the grid (derived from the current `GridConfig`).
  - **InitialProb**: prior probability loaded from `initialK_values_<subject>.csv` before any movement.
  - **Displacement**: signed arc length (centimeters) produced by the movement angle and pad radius.
  - **PredictedProb**: probability after the displacement model redistributes mass across the movement region.
  - **CorrectedProb**: posterior probability after applying the observation likelihoods for the requested angle.

### Real-time logs
- TXT files include the mean quaternion/Euler angles for each IMU and the per-sample Euler angles (roll, pitch, yaw).
- CSV files created by `saveDataToPlot` and `SerialReaderQuaternion.saveDataToPlot` contain columns: `timestamp roll_hand pitch_hand yaw_hand roll_arm pitch_arm yaw_arm`.

## Package structure
- `config`: default grid and runtime parameters (`GridConfig`, `RuntimeConfig`).
- `io`: result persistence for the simulation (`ResultWriter`).
- `model`: DTOs such as `BayesStepResult`.
- `simulation`: Bayesian grid update models (`Simulation`, `DisplacementModel`, `ObservationModel`, `Pad`).
- `realTimeExecution`: IMU acquisition, quaternion utilities, and FES control (`Main`, `SerialReaderQuaternion`, `Quaternion`, `FESController`, `Coord`).
- `verification`: baseline checks to ensure CSV layout compatibility (`BaselineCheck`).

## Differences vs memoria
The TFG memory referenced in project notes is not included in this repository, so this documentation reflects the observed code behavior and default configurations present in the source.
