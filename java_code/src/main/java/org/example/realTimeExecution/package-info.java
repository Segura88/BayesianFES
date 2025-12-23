/**
 * Paquete responsable de la ejecución en tiempo real del sistema de estimulación funcional.
 * <p>
 * Incluye utilidades para:
 * <ul>
 *   <li>Leer en tiempo real los cuaterniones enviados por los sensores IMU mediante {@link org.example.realTimeExecution.SerialReaderQuaternion}.</li>
 *   <li>Controlar el dispositivo de estimulación a través del puerto serie con {@link org.example.realTimeExecution.FESController}.</li>
 *   <li>Realizar la secuencia completa de medición y estimulación descrita en {@link org.example.realTimeExecution.Main}.</li>
 *   <li>Representar orientaciones mediante {@link org.example.realTimeExecution.Quaternion} y ángulos de Euler con {@link org.example.realTimeExecution.Coord}.</li>
 * </ul>
 * </p>
 */
package org.example.realTimeExecution;
