/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.exceptions;

/* - Part 5: Advanced Error Handling, Exception Mapping & Logging (30 Marks)
 - 2. Dependency Validation (422 Unprocessable Entity) (10 Marks) */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String roomID) {
        super("No room with ID '" + roomID + "' does not exist.");
    }
}
