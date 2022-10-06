package com.github.truongbb.genetictimetablealgorithmversion2;

import com.github.truongbb.genetictimetablealgorithmversion2.schedule.TimeTableScheduler;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@AllArgsConstructor
@SpringBootApplication
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GeneticTimeTableAlgorithmVersion2Application implements CommandLineRunner {

    TimeTableScheduler timeTableScheduler;

    public static void main(String[] args) {
        SpringApplication.run(GeneticTimeTableAlgorithmVersion2Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        timeTableScheduler.generateTimeTable();
    }

}
