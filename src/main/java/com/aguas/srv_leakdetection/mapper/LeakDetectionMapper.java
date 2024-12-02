package com.aguas.srv_leakdetection.mapper;

import com.aguas.srv_leakdetection.model.LeakDetection;
import com.aguas.srv_leakdetection.model.PressureReading;

public class LeakDetectionMapper {

    /**
     * Mapeia um objeto PressureReading para LeakDetection.
     * @param reading o objeto PressureReading
     * @param variation a variação de pressão detectada
     * @return um objeto LeakDetection
     */
    public static LeakDetection toLeakDetection(PressureReading reading, double variation) {
        if (reading == null) {
            return null;
        }
        return new LeakDetection(
            null, // ID será gerado pelo banco de dados
            reading.getSensorId(),
            reading.getPressure(),
            variation,
            reading.getReadingDateTime(),
            null // Version será gerenciada pelo Hibernate
        );
    }

    /**
     * Mapeia um objeto LeakDetection para PressureReading.
     * @param detection o objeto LeakDetection
     * @return um objeto PressureReading
     */
    public static PressureReading toPressureReading(LeakDetection detection) {
        if (detection == null) {
            return null;
        }
        return new PressureReading(
            detection.getId(),
            detection.getSensorId(),
            detection.getPressure(),
            detection.getReadingDateTime()
        );
    }
}
