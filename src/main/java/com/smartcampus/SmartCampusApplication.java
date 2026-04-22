package com.smartcampus;

import com.smartcampus.resources.DiscoveryResource;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/* Part 1: Service Architecture & Setup (10 Marks) */
 /* 1. Project & ApplicationConfiguration (5 Marks) */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
//
//    @Override
//    public Set<Class<?>> getClasses() {
//        Set<Class<?>> classes = new HashSet<>();
//        classes.add(DiscoveryResource.class);
//        return classes;
//    }
}
