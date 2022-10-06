package com.github.truongbb.genetictimetablealgorithmversion2.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class TimeTableConfiguration {

    @Value("${application.genetic-algorithm-rate.cross-over-rate}")
    Double crossOverRate;

    @Value("${application.genetic-algorithm-rate.mutation-rate}")
    Double mutationRate;

    @Value("${application.genetic-algorithm-rate.selection-rate}")
    Double selectionRate;

    @Value("${application.genetic-algorithm-rate.elites-rate}")
    Integer elitesRate;

    @Value("${application.genetic-algorithm-rate.population-size}")
    Integer populationSize;

    @Value("${application.genetic-algorithm-rate.generation-number}")
    Integer generationNumber;

    @Value("${application.day-of-week}")
    Integer dayOfWeek;

    @Value("${application.slot-of-day}")
    Integer slotOfDay;

}
