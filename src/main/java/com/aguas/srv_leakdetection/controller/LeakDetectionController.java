package com.aguas.srv_leakdetection.controller;

import com.aguas.srv_leakdetection.model.LeakDetection;
import com.aguas.srv_leakdetection.model.PressureReading;
import com.aguas.srv_leakdetection.service.LeakDetectionService;
import com.aguas.srv_leakdetection.mapper.LeakDetectionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class LeakDetectionController {

    private static final Logger log = LoggerFactory.getLogger(LeakDetectionController.class);

    @Autowired
    private LeakDetectionService leakDetectionService;

    @KafkaListener(topics = "pressure-readings", groupId = "leak-detection-group")
    public void consumePressureReading(PressureReading reading) {
        log.info("Received pressure reading: {}", reading);
        LeakDetection leakDetection = LeakDetectionMapper.toLeakDetection(reading, 0.0);
        leakDetectionService.processPressureReading(leakDetection);
        log.info("Processed pressure reading successfully");
    }
}