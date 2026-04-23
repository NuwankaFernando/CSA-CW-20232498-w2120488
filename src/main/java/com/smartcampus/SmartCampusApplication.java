package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 *
 * @author Nuwanka Fernando - Part 1: Question 1
 *
 */
// Configures the base URI for the Smart Campus REST API.
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    // No additional configuration required for basic JAX-RS setup
}
