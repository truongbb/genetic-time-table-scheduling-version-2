package com.github.truongbb.genetictimetablealgorithmversion2.schedule;


import com.github.truongbb.genetictimetablealgorithmversion2.config.TimeTableConfiguration;
import com.github.truongbb.genetictimetablealgorithmversion2.constant.SpecialLesson;
import com.github.truongbb.genetictimetablealgorithmversion2.dto.Chromosome;
import com.github.truongbb.genetictimetablealgorithmversion2.dto.Gene;
import com.github.truongbb.genetictimetablealgorithmversion2.dto.InputData;
import com.github.truongbb.genetictimetablealgorithmversion2.entity.Clazz;
import com.github.truongbb.genetictimetablealgorithmversion2.entity.LessonSlot;
import com.github.truongbb.genetictimetablealgorithmversion2.entity.Subject;
import com.github.truongbb.genetictimetablealgorithmversion2.entity.Teacher;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimeTableScheduler {

    List<Teacher> teachers;

    List<Subject> subjects;

    List<Clazz> clazzes;

    List<Chromosome> population;

    final TimeTableConfiguration config;

    public TimeTableScheduler(TimeTableConfiguration config) {
        this.config = config;
    }

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
        this.initData();

        // generate initial population
        this.generateInitialPopulation();
        // fitness evaluation
        this.fitnessEvaluation();
//        for (int i = 1; i <= this.config.getGenerationNumber(); i++) {
//            List<Chromosome> newPopulation = new ArrayList<>();
//
//            /**
//             * SELECTION - chọn lọc theo tỉ lệ elitism
//             *
//             * lấy mặc định n = population.size()/elitismRate cá thể có điểm fitness tốt nhất cho vào quần thể mới
//             *  nhằm mục đích quần thể mới luôn chứa những cá thể tốt nhất của quần thể cũ.
//             *
//             * còn lại (population.size() - n) cá thể cần thêm vào quần thể mới nữa
//             *  --> số cá thể này được tạo ra bằng cách cho đi lai chéo và đột biến
//             */
//            int eliteEntityNumber = this.population.size() / this.config.getElitesRate();
//            for (int j = 0; j < eliteEntityNumber; j++) {
//                newPopulation.add(this.population.get(j));
//            }
//
//            while (newPopulation.size() <= this.config.getPopulationSize()) {
//                // crossover - recombination
//                Chromosome child;
//                int fatherIndex = this.rouletteWheelSelection();
//                Chromosome father = this.population.get(fatherIndex);
//                double randomCrossoverRate = new Random().nextDouble();
//                if (randomCrossoverRate < this.config.getCrossOverRate()) {
//                    // Select 2 parents by seed selection
//                    int motherIndex = -1;
//                    do {
//                        motherIndex = this.rouletteWheelSelection();
//                    } while (motherIndex == fatherIndex);
//                    Chromosome mother = this.population.get(motherIndex);
//
//                    child = this.twoPointsCrossover(father, mother);
//                } else {
//                    // Select an individual randomly from the current population
//                    child = father;
//                }
//
//                // mutation
//                this.swapMutation(child);
//                newPopulation.add(child);
//            }
//            // fitness evaluation
//            this.fitnessEvaluation();
//        }

    }

    private void initData() {
        Gson gson = new Gson();
        try (Reader reader = new FileReader("secondary-school-data.json")) {
            InputData inputData = gson.fromJson(reader, InputData.class);

            this.teachers = inputData.getTeachers();
            this.clazzes = inputData.getClazzes();
            this.subjects = inputData.getSubjects();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // https://www.geeksforgeeks.org/mutation-algorithms-for-string-manipulation-ga/
    private void swapMutation(Chromosome entity) {
        Map<Clazz, Gene> chromosome = entity.getChromosome();

        List<Integer> takenClasses = new ArrayList<>();

        Set<Clazz> clazzes = chromosome.keySet();
        int totalClazz = clazzes.size();
        int round = (int) Math.round(totalClazz * this.config.getMutationRate());
        int numberOfClazzToMutate = new Random().nextInt(round <= 0 ? 1 : round);
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
        int chromosomeSize = this.population.get(0).getChromosome().size();
        int point1, point2;
        point1 = new Random().nextInt(chromosomeSize - 3);
        do {
            point2 = new Random().nextInt(chromosomeSize - 3);
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

        for (int i = point2 + 1; i <= chromosomeSize - 2; i++) {
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

        Subject offLesson = this.subjects.stream().filter(s -> s.getName().equals(SpecialLesson.NGHI.value)).findFirst().orElse(null);

        while (this.population.size() < this.config.getPopulationSize()) {
            Chromosome entity = new Chromosome(); // 1 TKB trong danh sách
            Map<Clazz, Gene> chromosome = new HashMap<>();

            for (Clazz clazz : this.clazzes) {
                List<Subject> subjects = clazz.getSubjects();// danh sách các môn mà lớp này học
                List<LessonSlot> lessonSlots = new ArrayList<>(); // danh sách các tiết học của lớp này trong tuần

                // tìm giáo viên chủ nhiệm
                Teacher headTeacher = this.teachers.stream().filter(t -> t.getHeadClazz().getId().equals(clazz.getId())).findFirst().orElse(null);

                for (Subject subject : subjects) {

                    // xếp chào cờ và sinh hoạt lớp cho cô giáo chủ nhiệm
                    if (subject.getName().equals(SpecialLesson.CHAO_CO.value) || subject.getName().equals(SpecialLesson.SINH_HOAT_LOP.value)) {
                        do {
                            int day = new Random().nextInt(this.config.getDayOfWeek()) + 2; // random ra ngày học tiết này
                            int order = new Random().nextInt(this.config.getSlotOfDay()) + 1; // random ra số tiết cho lớp học môn này

                            // kiểm tra xem trong list slots ngày này tiết này có môn chưa, nếu chưa thì cho học, nếu có thì phải tìm tiếp
                            if (!ObjectUtils.isEmpty(lessonSlots)) {
                                boolean isNotAvailableSlot = lessonSlots
                                        .stream()
                                        .anyMatch(slot -> slot.getDay() == day && slot.getLessonSlotOrder() == order && slot.getSubject() != null);
                                if (isNotAvailableSlot) {
                                    continue;
                                }
                            }
                            LessonSlot lessonSlot = new LessonSlot(day, order, clazz, subject, headTeacher);
                            lessonSlots.add(lessonSlot);
                            break;
                        } while (true);
                    }

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
                                if (!ObjectUtils.isEmpty(lessonSlots)) {
                                    boolean isNotAvailableSlot = lessonSlots
                                            .stream()
                                            .anyMatch(slot -> slot.getDay() == day && slot.getLessonSlotOrder() == order && slot.getSubject() != null);
                                    if (isNotAvailableSlot) {
                                        continue;
                                    }
                                }

                                // nếu slot này trống, kiểm tra xem vào ngày đó, tiết đó, ở các lớp khác, thầy này đã dạy chưa
                                // nếu dạy rồi thì không xếp do trùng lịch, nếu chưa thì xếp lịch
//                                boolean isTaughtOtherSlot = chromosome.entrySet().stream().anyMatch(entry -> {
//                                    Clazz entryKey = entry.getKey();
//                                    Gene entryGene = entry.getValue();
//                                    if (!entryKey.getId().equals(clazz.getId())) {
//                                        return false;
//                                    }
//                                    List<LessonSlot> slots = entryGene.getLessonSlots();
//                                    if (CollectionUtils.isEmpty(slots)) {
//                                        return false;
//                                    }
//                                    return slots.stream().anyMatch(sl -> sl.getTeacher().getId().equals(teacher.getId()));
//                                });
//
//                                if (isTaughtOtherSlot) {
//                                    continue;
//                                }

                                LessonSlot lessonSlot = new LessonSlot(day, order, clazz, subject, teacher);
                                lessonSlots.add(lessonSlot);
                                break;
                            } while (true);
                        }
                        break;
                    } while (true);

                }
                if (lessonSlots.size() < this.config.getDayOfWeek() * this.config.getSlotOfDay()) {
                    for (int i = 2; i <= this.config.getDayOfWeek(); i++) {
                        for (int j = 1; j <= this.config.getSlotOfDay(); j++) {
                            int finalI = i;
                            int finalJ = j;
                            boolean isNotAvailableSlot = lessonSlots.stream().anyMatch(l -> l.getDay() == finalI && l.getLessonSlotOrder() == finalJ && l.getSubject() != null);
                            if (isNotAvailableSlot) {
                                continue;
                            }
                            LessonSlot lessonSlot = new LessonSlot(i, j, clazz, offLesson, headTeacher);
                            lessonSlots.add(lessonSlot);
                        }
                    }
                }
                Gene gene = new Gene(lessonSlots);
                chromosome.put(clazz, gene);
            }

            entity.setChromosome(chromosome);
            population.add(entity);
        }
    }


}
