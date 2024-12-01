package com.aguas.srv_leakdetection.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aguas.srv_leakdetection.model.PressureReading;

public interface PressureReadingRepository extends JpaRepository<PressureReading, Long> {
}