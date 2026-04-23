package com.smartcampus.exceptions;

/**
 *
 * @author Nuwanka Fernando - Part 5: Question 3
 *
 */
// Exception thrown when an operation is attempted on a sensor that is currently
public class SensorUnavailableException extends RuntimeException {

    // Constructs a new SensorUnavailableException with a detail message.
    public SensorUnavailableException(String message) {
        super(message);
    }
}
