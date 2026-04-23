package com.smartcampus.repository;

import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryDataStore {
    // Thread-safe maps for in-memory storage. 
    // In JAX-RS, Resource classes are instantiated per-request by default.
    // Using a static ConcurrentHashMap ensures data persists and is thread-safe across multiple concurrent requests.
    
    // Key: Room ID
    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    
    // Key: Sensor ID
    private static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    
    // Key: Sensor ID, Value: List of Readings
    private static final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    public static Map<String, Room> getRooms() {
        return rooms;
    }

    public static Map<String, Sensor> getSensors() {
        return sensors;
    }

    public static Map<String, List<SensorReading>> getSensorReadings() {
        return sensorReadings;
    }
    
    // Helper to initialize some dummy data if needed, or simply start empty.
    static {
        // Initialize with default room for testing
        // rooms.put("LIB-301", new Room("LIB-301", "Library Quiet Study", 50));
    }
}
