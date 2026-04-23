package com.smartcampus.filters;

import java.io.IOException;
import java.util.logging.Logger;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Nuwanka Fernando - Part 5: Question 5
 *
 */
// Logging filter for tracking API requests and responses.
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    // Logger instance for recording request and response details
    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    // Intercepts incoming HTTP requests.
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        // Log incoming request details
        LOGGER.info("--- Incoming Request ---");
        LOGGER.info("Method: " + requestContext.getMethod());
        LOGGER.info("URI: " + requestContext.getUriInfo().getAbsolutePath());
    }

    // Intercepts outgoing HTTP responses.
    @Override
    public void filter(ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) throws IOException {

        // Log outgoing response status
        LOGGER.info("--- Outgoing Response ---");
        LOGGER.info("Status: " + responseContext.getStatus());
    }
}
