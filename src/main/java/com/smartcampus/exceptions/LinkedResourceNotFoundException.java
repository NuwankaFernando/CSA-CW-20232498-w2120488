package com.smartcampus.exceptions;

/**
 *
 * @author Nuwanka Fernando - Part 5: Question 2
 *
 */
// Exception thrown when a required linked resource
public class LinkedResourceNotFoundException extends RuntimeException {

    // Constructs a new LinkedResourceNotFoundException with a detail message.
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
