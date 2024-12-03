package com.aguas.srv_leakdetection.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import com.aguas.srv_leakdetection.model.LeakDetection;
import com.aguas.srv_leakdetection.model.PressureReading;

public class LeakDetectionMapperTest {
    
    @Test
    void toLeakDetection_WhenValidPressureReading_ShouldMapCorrectly() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        PressureReading reading = new PressureReading(1L, "sensor1", 100.0, now);
        double variation = 5.0;

        // Act
        LeakDetection result = LeakDetectionMapper.toLeakDetection(reading, variation);

        // Assert
        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("sensor1", result.getSensorId());
        assertEquals(100.0, result.getPressure());
        assertEquals(5.0, result.getVariation());
        assertEquals(now, result.getReadingDateTime());
        assertNull(result.getVersion());
    }

    @Test
    void toLeakDetection_WhenNullPressureReading_ShouldReturnNull() {
        // Act
        LeakDetection result = LeakDetectionMapper.toLeakDetection(null, 5.0);

        // Assert
        assertNull(result);
    }

    @Test
    void toPressureReading_WhenValidLeakDetection_ShouldMapCorrectly() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        LeakDetection detection = new LeakDetection(1L, "sensor1", 100.0, 5.0, now, 1L);

        // Act
        PressureReading result = LeakDetectionMapper.toPressureReading(detection);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("sensor1", result.getSensorId());
        assertEquals(100.0, result.getPressure());
        assertEquals(now, result.getReadingDateTime());
    }

    @Test
    void toPressureReading_WhenNullLeakDetection_ShouldReturnNull() {
        // Act
        PressureReading result = LeakDetectionMapper.toPressureReading(null);

        // Assert
        assertNull(result);
    }
}
