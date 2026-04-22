/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.exceptions;

/* - Part 5: Advanced Error Handling, Exception Mapping & Logging (30 Marks)
   - 3. State Constraint (403 Forbidden) (5 Marks) */
public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String sensorId) {
        super("Sensor '" + sensorId + "' is under MAINTENANCE.");
    }

}
