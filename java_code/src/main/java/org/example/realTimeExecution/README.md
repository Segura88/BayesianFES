# Package description

This package contains the Java code that reads data from two IMU devices (in quaternion format) via serial ports, controls the TEREFES stimulator, and saves the results to text and CSV files.
Classes FESController and SerialReader include a local main to test their performance separetly.

Before the execution, you must connect the Arduinos to the inertial sensors following the wiring configuration, and then connect the Arduinos to the PC. Connect also de stimulator to the PC.

## Dependencies
1. Java 19+ 
2. [jSerialComm](https://fazecast.github.io/jSerialComm/) 
   Add to your `pom.xml` (Maven) or `build.gradle` (Gradle):

   ```xml
   <dependency>
     <groupId>com.fazecast</groupId>
     <artifactId>jSerialComm</artifactId>
     <version>2.6.2</version>
   </dependency>

3. Ensure the `Utilities` class is included in your project.

## Configuration

1. Configuration of the serial ports in `Main.java.`
2. Set stimulation parameters in  `Main.java.`
3. Change output paths in `Main.java.` to save results for plotting in MATLAB
4. Baud Rate for `SerialReaderQuaternion` is 115200-8-1-N, change if needed.
5. Baud Rate for `FESController` is 9600-8-1-N, change if needed.

## Real-time predicted pattern logging

The current real-time execution code reads two IMU streams, controls the stimulator, and saves raw and averaged quaternion data
to TXT/CSV files (see the `saveData` and `saveDataToPlot` calls in `Main.java` and `SerialReaderQuaternion`). It does **not**
include any logic to run the Bayesian predictor or to persist a "predicted pattern" while stimulation is ongoing. Implementing
that capability would require adding the prediction routine to the real-time loop and writing its outputs to disk in parallel
with the existing angle logs.












