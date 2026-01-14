package com.api.medicine.application.jobs;

import org.springframework.stereotype.Component;
import com.api.medicine.domain.interfaces.MedicineForecastRepository;

@Component
public class ForecastScheduler {

    private final MedicineForecastRepository forecastRepository;

    public ForecastScheduler(MedicineForecastRepository forecastRepository) {
        this.forecastRepository = forecastRepository;
    }

    // Planned usage for scheduled jobs
}
