package com.aguas.srv_leakdetection.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aguas.srv_leakdetection.model.LeakDetection;

public interface PressureReadingRepository extends JpaRepository<LeakDetection, Long> {
}