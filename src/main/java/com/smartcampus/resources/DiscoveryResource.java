package com.smartcampus.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Nuwanka Fernando - Part 1: Question 1
 *
 */
//Root discovery endpoint for the Smart Campus API.
@Path("/")
public class DiscoveryResource {

    // Handles GET requests to the API root.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {

        // Maintain ordered JSON structure
        Map<String, Object> info = new LinkedHashMap<>();

        // API metadata
        info.put("api", "Smart Campus Sensor & Room Management API");
        info.put("version", "1.0");
        info.put("contact", "suvin.20232498@iit.ac.lk");
        info.put("description", "RESTful API for managing campus rooms and IoT sensors.");

        // Resource endpoints for client navigation
        Map<String, String> links = new LinkedHashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");

        // Attach links to response
        info.put("resources", links);

        // Return HTTP 200 OK with JSON body
        return Response.ok(info).build();
    }
}
