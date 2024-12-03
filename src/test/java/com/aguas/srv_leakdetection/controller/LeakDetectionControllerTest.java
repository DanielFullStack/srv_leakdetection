package com.aguas.srv_leakdetection.controller;

import com.aguas.srv_leakdetection.model.PressureReading;
import com.aguas.srv_leakdetection.service.LeakDetectionService;
import com.aguas.srv_leakdetection.mapper.LeakDetectionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

public class LeakDetectionControllerTest {

    @Mock
    private LeakDetectionService leakDetectionService;

    @Mock
    private LeakDetectionMapper leakDetectionMapper;

    @InjectMocks
    private LeakDetectionController leakDetectionController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConsumePressureReading() {
        // Arrange
        PressureReading pressureReading = new PressureReading();
        pressureReading.setSensorId("1");
        pressureReading.setPressure(100.0);
        pressureReading.setReadingDateTime(LocalDateTime.now());

        // Act
        leakDetectionController.consumePressureReading(pressureReading);

        // Assert
        verify(leakDetectionService).processPressureReading(LeakDetectionMapper.toLeakDetection(pressureReading, 0.0));
    }
}
