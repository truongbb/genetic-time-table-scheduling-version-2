package com.github.truongbb.genetictimetablealgorithmversion2.schedule;


import com.github.truongbb.genetictimetablealgorithmversion2.config.TimeTableConfiguration;
import com.github.truongbb.genetictimetablealgorithmversion2.dto.Chromosome;
import com.github.truongbb.genetictimetablealgorithmversion2.dto.Gene;
import com.github.truongbb.genetictimetablealgorithmversion2.entity.Clazz;
import com.github.truongbb.genetictimetablealgorithmversion2.entity.LessonSlot;
import com.github.truongbb.genetictimetablealgorithmversion2.entity.Subject;
import com.github.truongbb.genetictimetablealgorithmversion2.entity.Teacher;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Component
public class TimeTableScheduler {

    List<Teacher> teachers;

    List<Subject> subjects;

    List<Clazz> clazzes;

    List<Chromosome> population;

    TimeTableConfiguration config;


    /**
     * 0. Init data (lấy data từ DB hoặc file)
     * <p>
     * 1. Generate initial population
     * <p>
     * 2. Loop:
     * 2.1 fitness evaluation
     * 2.2 selection - https://en.wikipedia.org/wiki/Selection_(genetic_algorithm)
     * 2.3 crossover - https://en.wikipedia.org/wiki/Crossover_(genetic_algorithm)
     * 2.4 mutation - https://en.wikipedia.org/wiki/Mutation_(genetic_algorithm) https://vi.wikipedia.org/wiki/%C4%90%E1%BB%99t_bi%E1%BA%BFn_sinh_h%E1%BB%8Dc
     */
    public void generateTimeTable() {

        // init data

        // generate initial population
        generateInitialPopulation();
        // fitness evaluation
        this.fitnessEvaluation();
        for (int i = 1; i <= this.config.getGenerationNumber(); i++) {
            List<Chromosome> newPopulation = new ArrayList<>();

            /**
             * SELECTION - chọn lọc theo tỉ lệ elitism
             *
             * lấy mặc định n = population.size()/elitismRate cá thể có điểm fitness tốt nhất cho vào quần thể mới
             *  nhằm mục đích quần thể mới luôn chứa những cá thể tốt nhất của quần thể cũ.
             *
             * còn lại (population.size() - n) cá thể cần thêm vào quần thể mới nữa
             *  --> số cá thể này được tạo ra bằng cách cho đi lai chéo và đột biến
             */
            int eliteEntityNumber = this.population.size() / this.config.getElitesRate();
            for (int j = 0; j < eliteEntityNumber; j++) {
                newPopulation.add(this.population.get(j));
            }

            while (newPopulation.size() <= this.config.getPopulationSize()) {
                // crossover - recombination
                Chromosome child;
                int fatherIndex = rouletteWheelSelection();
                Chromosome father = this.population.get(fatherIndex);
                double randomCrossoverRate = new Random().nextDouble();
                if (randomCrossoverRate < this.config.getCrossOverRate()) {
                    // Select 2 parents by seed selection
                    int motherIndex = -1;
                    do {
                        motherIndex = rouletteWheelSelection();
                    } while (motherIndex == fatherIndex);
                    Chromosome mother = this.population.get(motherIndex);

                    child = twoPointsCrossover(father, mother);
                } else {
                    // Select an individual randomly from the current population
                    child = father;
                }

                // mutation
                swapMutation(child);
                newPopulation.add(child);
            }
            // fitness evaluation
            this.fitnessEvaluation();
        }

    }

    // https://www.geeksforgeeks.org/mutation-algorithms-for-string-manipulation-ga/
    private void swapMutation(Chromosome entity) {
        Map<Clazz, Gene> chromosome = entity.getChromosome();

        List<Integer> takenClasses = new ArrayList<>();

        Set<Clazz> clazzes = chromosome.keySet();
        int totalClazz = clazzes.size();
        int numberOfClazzToMutate = new Random().nextInt((int) Math.round(totalClazz * this.config.getMutationRate()));
        numberOfClazzToMutate = numberOfClazzToMutate <= 0 ? 1 : numberOfClazzToMutate; // make sure there's always a gene to mutate
        for (int i = 0; i < numberOfClazzToMutate; i++) {
            Gene gene = null;
            do {
                int randomClazz = new Random().nextInt(totalClazz);
                if (takenClasses.contains(randomClazz)) {
                    continue;
                }
                takenClasses.add(randomClazz);
                Optional<Clazz> clazz = clazzes.stream().filter(c -> c.getId() == randomClazz).findFirst();
                if (!clazz.isPresent()) {
                    continue;
                }
                gene = chromosome.get(clazz.get());
                break;
            } while (true);

            int randomIndex1 = new Random().nextInt(this.config.getDayOfWeek() * this.config.getSlotOfDay());
            int randomIndex2 = -1;
            do {
                randomIndex2 = new Random().nextInt(this.config.getDayOfWeek() * this.config.getSlotOfDay());
            } while (randomIndex1 == randomIndex2);

            // mutation gene
            List<String> geneList = Arrays.asList(gene.getGene().split(";"));
            String geneContent1 = geneList.get(randomIndex1);
            String geneContent2 = geneList.get(randomIndex2);

            String slotContent1 = geneContent1.substring(4);
            String slotContent2 = geneContent2.substring(4);
            geneContent1 = geneContent1.substring(0, 4) + slotContent2;
            geneContent2 = geneContent2.substring(0, 4) + slotContent1;

            geneList.set(randomIndex1, geneContent1);
            geneList.set(randomIndex2, geneContent2);

            String geneStr = String.join(";", geneList);
            gene.setGene(geneStr);


            // mutation slot following gene before
            List<LessonSlot> slots = gene.getLessonSlots();

            LessonSlot slot1 = slots.get(randomIndex1);
            Optional<Subject> subjectOptional1 = this.subjects.stream().filter(s -> s.getId().equals(Long.valueOf(slotContent1.substring(0, 3)))).findFirst();
            slot1.setSubject(subjectOptional1.orElse(null));
            Optional<Teacher> teacherOptional1 = this.teachers.stream().filter(s -> s.getId().equals(Long.valueOf(slotContent1.substring(3)))).findFirst();
            slot1.setTeacher(teacherOptional1.orElse(null));

            LessonSlot slot2 = slots.get(randomIndex2);
            Optional<Subject> subjectOptional2 = this.subjects.stream().filter(s -> s.getId().equals(Long.valueOf(slotContent2.substring(0, 3)))).findFirst();
            slot2.setSubject(subjectOptional2.orElse(null));
            Optional<Teacher> teacherOptional2 = this.teachers.stream().filter(s -> s.getId().equals(Long.valueOf(slotContent2.substring(3)))).findFirst();
            slot2.setTeacher(teacherOptional2.orElse(null));

            gene.setLessonSlots(slots);
        }

        entity.setChromosome(chromosome);
    }

    private Chromosome twoPointsCrossover(Chromosome father, Chromosome mother) {
        int point1, point2;
        point1 = new Random().nextInt(this.population.size() - 3);
        do {
            point2 = new Random().nextInt(this.population.size() - 3);
        } while (point1 == point2);
        if (point1 > point2) {
            // swap point1 and point2
            int temp = point1;
            point1 = point2;
            point2 = temp;
        }

        Map<Clazz, Gene> fatherChromosome = father.getChromosome();
        Map<Clazz, Gene> motherChromosome = mother.getChromosome();

        Map<Clazz, Gene> child1Chromosome = new HashMap<>();
        Map<Clazz, Gene> child2Chromosome = new HashMap<>();

        ArrayList<Clazz> fatherKey = new ArrayList<>(fatherChromosome.keySet());
        ArrayList<Clazz> motherKey = new ArrayList<>(motherChromosome.keySet());

        for (int i = 0; i <= point1; i++) {
            Clazz fKey = fatherKey.get(i);
            Clazz mKey = motherKey.get(i);
            child1Chromosome.put(fKey, fatherChromosome.get(fKey));
            child2Chromosome.put(fKey, motherChromosome.get(mKey));
        }

        for (int i = point1 + 1; i <= point2; i++) {
            Clazz fKey = fatherKey.get(i);
            Clazz mKey = motherKey.get(i);
            child1Chromosome.put(fKey, motherChromosome.get(fKey));
            child2Chromosome.put(fKey, fatherChromosome.get(mKey));
        }

        for (int i = point2 + 1; i <= this.population.size() - 2; i++) {
            Clazz fKey = fatherKey.get(i);
            Clazz mKey = motherKey.get(i);
            child1Chromosome.put(fKey, fatherChromosome.get(fKey));
            child2Chromosome.put(fKey, motherChromosome.get(mKey));
        }

        Chromosome child1 = new Chromosome(child1Chromosome, this.config);
        Chromosome child2 = new Chromosome(child2Chromosome, this.config);

        return child1.getFitness() > child2.getFitness() ? child1 : child2;
    }

    private Chromosome uniformCrossover(Chromosome father, Chromosome mother) {
        Map<Clazz, Gene> fatherChromosome = father.getChromosome();
        Map<Clazz, Gene> motherChromosome = mother.getChromosome();

        ArrayList<Clazz> fatherKey = new ArrayList<>(fatherChromosome.keySet());
        ArrayList<Clazz> motherKey = new ArrayList<>(motherChromosome.keySet());

        Map<Clazz, Gene> child1Chromosome = new HashMap<>();
        Map<Clazz, Gene> child2Chromosome = new HashMap<>();

        int chromosomeGeneSize = fatherKey.size();

        for (int i = 0; i < chromosomeGeneSize; i++) {
            Clazz fKey = fatherKey.get(i);
            Clazz mKey = motherKey.get(i);

            if (i % 2 == 0) {
                child1Chromosome.put(mKey, motherChromosome.get(fKey));
                child2Chromosome.put(fKey, fatherChromosome.get(fKey));
            } else {
                child1Chromosome.put(fKey, fatherChromosome.get(fKey));
                child2Chromosome.put(mKey, motherChromosome.get(fKey));
            }
        }

        Chromosome child1 = new Chromosome(child1Chromosome, this.config);
        Chromosome child2 = new Chromosome(child2Chromosome, this.config);

        return child1.getFitness() > child2.getFitness() ? child1 : child2;
    }

    // selecting using Roulette Wheel Selection only from the best 10% chromosomes
    // https://en.wikipedia.org/wiki/Fitness_proportionate_selection
    // https://stackoverflow.com/a/391712/11827984
    public int rouletteWheelSelection() {
        int strongestChromosome = this.population.size() / this.config.getElitesRate();
        double randomDouble = new Random().nextDouble() * strongestChromosome;
        double currentFitnessSum = 0;
        int i = 0;

        while (currentFitnessSum <= randomDouble) {
            currentFitnessSum += this.population.get(i).getFitness();
            i++;
        }
        return --i;
    }

    // selecting using Elitism Selection (from best chromosomes only)
    public int bestParentSelection() {
        return new Random().nextInt(this.population.size() / this.config.getElitesRate());
    }

    public void fitnessEvaluation() {
        for (Chromosome chromosome : this.population) {
            chromosome.calculateFitness(this.config);
        }
        this.population.sort(Comparator.comparing(Chromosome::getFitness));
    }

    public void generateInitialPopulation() {
        this.population = new ArrayList<>(); // danh sách TKB

        while (this.population.size() < this.config.getPopulationSize()) {
            Chromosome entity = new Chromosome(); // 1 TKB trong danh sách
            Map<Clazz, Gene> chromosome = new HashMap<>();

            for (Clazz clazz : this.clazzes) {
                List<Subject> subjects = clazz.getSubjects();// danh sách các môn mà lớp này học
                List<LessonSlot> lessonSlots = new ArrayList<>(); // danh sách các tiết học của lớp này trong tuần

                for (Subject subject : subjects) {
                    Integer numberOfLessonPerWeek = subject.getNumberOfLessonPerWeek(); // thời lượng tiết 1 tuần
                    // đi tìm thầy có thể dạy môn này
                    List<Teacher> availableTeachers = this.teachers
                            .stream().filter(t -> t.getAvailableSubjects()
                                    .stream()
                                    .anyMatch(s -> s.getId().equals(subject.getId()))
                            )
                            .collect(Collectors.toList());
                    do {
                        int randomTeacherIndex = new Random().nextInt(availableTeachers.size()); // lấy giáo viên ngẫu nhiên
                        Teacher teacher = availableTeachers.get(randomTeacherIndex);
                        if (teacher.getMaxTeachingLessonPerWeek() < numberOfLessonPerWeek) { // nếu giáo viên này KHÔNG còn đủ tiết trống để dạy môn này
                            continue;
                        }
                        // tìm slot còn trống để dạy môn này
                        for (int k = 0; k < numberOfLessonPerWeek; k++) {
                            do {
                                int day = new Random().nextInt(this.config.getDayOfWeek()) + 2; // random ra ngày học tiết này
                                int order = new Random().nextInt(this.config.getSlotOfDay()) + 1; // random ra số tiết cho lớp học môn này

                                // kiểm tra xem trong list slots ngày này tiết này có môn chưa, nếu chưa thì cho học, nếu có thì phải tìm tiếp
                                boolean isAvailableSlot = lessonSlots
                                        .stream()
                                        .anyMatch(slot -> slot.getDay() == day && slot.getLessonSlotOrder() == order && slot.getSubject() != null);
                                if (!isAvailableSlot) {
                                    continue;
                                }

                                // nếu slot này trống, kiểm tra xem vào ngày đó, tiết đó, ở các lớp khác, thầy này đã dạy chưa
                                // nếu dạy rồi thì không xếp do trùng lịch, nếu chưa thì xếp lịch
                                boolean isTaughtOtherSlot = chromosome.entrySet().stream().anyMatch(entry -> {
                                    Clazz entryKey = entry.getKey();
                                    Gene entryGene = entry.getValue();
                                    if (!entryKey.getId().equals(clazz.getId())) {
                                        return false;
                                    }
                                    List<LessonSlot> slots = entryGene.getLessonSlots();
                                    if (CollectionUtils.isEmpty(slots)) {
                                        return false;
                                    }
                                    return slots.stream().anyMatch(sl -> sl.getTeacher().getId().equals(teacher.getId()));
                                });

                                if (isTaughtOtherSlot) {
                                    continue;
                                }

                                LessonSlot lessonSlot = new LessonSlot(day, order, clazz, subject, teacher);
                                lessonSlots.add(lessonSlot);
                                break;
                            } while (true);
                        }
                        break;
                    } while (true);

                }
                Gene gene = new Gene(lessonSlots);
                chromosome.put(clazz, gene);
            }

            entity.setChromosome(chromosome);
            population.add(entity);
        }
    }


}
