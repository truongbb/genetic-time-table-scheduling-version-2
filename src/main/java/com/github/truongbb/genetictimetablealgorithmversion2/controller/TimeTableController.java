package com.github.truongbb.genetictimetablealgorithmversion2.controller;


import com.github.truongbb.genetictimetablealgorithmversion2.dto.Chromosome;
import com.github.truongbb.genetictimetablealgorithmversion2.dto.Gene;
import com.github.truongbb.genetictimetablealgorithmversion2.dto.LessonKey;
import com.github.truongbb.genetictimetablealgorithmversion2.entity.Clazz;
import com.github.truongbb.genetictimetablealgorithmversion2.entity.LessonSlot;
import com.github.truongbb.genetictimetablealgorithmversion2.schedule.TimeTableScheduler;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/time-table")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TimeTableController {

    TimeTableScheduler timeTableScheduler;

    @GetMapping
    public String getTimeTable(ModelMap modelMap) {

        Chromosome entity = timeTableScheduler.getPopulation().get(0);
        System.out.println(entity.getFitness());
        Map<Clazz, Gene> chromosome = entity.getChromosome();
        ArrayList<Clazz> clazzes = new ArrayList<>(chromosome.keySet());
        List<LessonSlot> allLessonSlots = new ArrayList<>();

        for (Clazz clazz : clazzes) {
            Gene gene = chromosome.get(clazz);
            List<LessonSlot> lessonSlots = gene.getLessonSlots();
            allLessonSlots.addAll(lessonSlots);
        }

        Map<LessonKey, List<LessonSlot>> tempMap = allLessonSlots
                .stream()
                .collect(
                        Collectors.groupingBy(
                                sl -> new LessonKey(sl.getDay(), sl.getLessonSlotOrder()),
                                Collectors.toCollection(ArrayList::new)
                        )
                );

        tempMap.forEach((key, lessons) -> {
            for (int i = 0; i < lessons.size(); i++) {
                LessonSlot l1 = lessons.get(i);
                for (int j = i + 1; j < lessons.size(); j++) {
                    LessonSlot l2 = lessons.get(j);
                    if (!ObjectUtils.isEmpty(l1.getTeacher()) && !ObjectUtils.isEmpty(l2.getTeacher()) && l1.getTeacher().getId().equals(l2.getTeacher().getId())) {
                        l1.setDuplicated(true);
                        l2.setDuplicated(true);
                    }
                }
            }
        });

        Map<LessonKey, List<LessonSlot>> result = tempMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (oldValue, newValue) -> oldValue, LinkedHashMap::new)
                );

        result.forEach((key, value) -> value.sort((Comparator.comparing(o -> o.getClazz().getName()))));

        modelMap.addAttribute("timeTables", result);
        modelMap.addAttribute("allClasses", clazzes.stream().sorted(Comparator.comparing(Clazz::getName)).collect(Collectors.toList()));
        return "index";
    }

}
