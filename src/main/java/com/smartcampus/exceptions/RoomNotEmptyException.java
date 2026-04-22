/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.exceptions;

/* - Part 5: Advanced Error Handling, Exception Mapping & Logging (30 Marks)
 - 1. Resource Conflict (409) (5 Marks) */
public class RoomNotEmptyException extends RuntimeException {

    public RoomNotEmptyException(String roomId) {
        super("Room '" + roomId + "' cannot be deleted: it still has active sensors assigned.");
    }
}
