package com.smartcampus.dao;

import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Nuwanka Fernando
 */
public class MockDatabase {

    public static final List<Room> ROOMS = new ArrayList<>();
    public static final List<Sensor> SENSORS = new ArrayList<>();
    public static final Map<String, List<SensorReading>> READINGS = new HashMap<>();

    static {

        // Initialise SENOSRS
        SENSORS.add(new Sensor("Temp-001", "Temperature", "ACTIVE", 30.0, "LEC-301"));
        SENSORS.add(new Sensor("Temp-002", "CO2", "MAINTENANCE", 30.0, "LEC-301"));
        SENSORS.add(new Sensor("Temp-003", "Temperature", "ACTIVE", 30.0, "LEC-301"));

        List<String> lec301Sensors = new ArrayList<>();
        lec301Sensors.add("Temp-001");
        lec301Sensors.add("Temp-002");

        // Initialise ROOMS
        ROOMS.add(new Room("LEC-301", "Lecture Room 301", 200, lec301Sensors));
        ROOMS.add(new Room("LEC-302", "Lecture Room 301", 30, new ArrayList<>()));

        // Initialise READINGS
        READINGS.put("Temp-001", new ArrayList<>());
        READINGS.get("Temp-001").add(new SensorReading(10.0));
        READINGS.get("Temp-001").add(new SensorReading(20.0));
    }

}
