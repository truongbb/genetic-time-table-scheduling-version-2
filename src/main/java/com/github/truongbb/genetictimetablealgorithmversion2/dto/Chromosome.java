package com.github.truongbb.genetictimetablealgorithmversion2.dto;

import com.github.truongbb.genetictimetablealgorithmversion2.config.TimeTableConfiguration;
import com.github.truongbb.genetictimetablealgorithmversion2.constant.SpecialLesson;
import com.github.truongbb.genetictimetablealgorithmversion2.entity.Clazz;
import com.github.truongbb.genetictimetablealgorithmversion2.entity.LessonSlot;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Chromosome {

    // danh sách thời khóa biểu của các lớp thì tạo thành thời khóa biểu toàn trường --> là một thực thể trong quần thể
//    List<Gene> genes;
    Map<Clazz, Gene> chromosome;


    double fitness;

    /**
     * tính điểm thích nghi của từng cá thể (trong quần thể), dựa vào các luật đặt ra cho TBK
     * <p>
     * == LUẬT CỨNG ==
     * - Giáo viên không trùng tiết
     * - Phòng học không trùng nhau
     * - Tiết chào cờ luôn là tiết 1 thứ hai (với hệ học sáng) hoặc tiết 5 thứ 2 (với hệ học chiều)
     * <p>
     * == LUẬT MỀM ==
     * - Các tiết giảng học/tránh vào cuối buổi: Các môn được cấu hình không học tiết cuối (như Thể dục) thì không cho học tiết cuối
     * - Hai tiết giảng xếp liền kề nhau: trong tuần có một ngày có 2 tiết Văn/Toán liên tiếp để làm bài kiểm tra
     * - Quy định học cách ngày giữa các tiết giảng, ví dụ môn Địa 1 tuần có 2 tiết sẽ có cách ngày, tránh học chung 1 ngày hoặc 2 ngày liền nhau
     * - Các tiết nghỉ cấu hình vào ngày nhất định hoặc không cấu hình (sẽ rơi vào tiết cuối của các ngày)
     * <p>
     * <p>
     * - Giảng viên thuê ngoài chỉ dạy được một vài ngày cố định trong tuần
     * - Ưu tiên giáo viên nhà xa, có con nhỏ tránh dạy tiết 1
     * - Check trùng phòng máy thực hành
     * - Ưu tiên dồn tiết, tránh bị cơi giờ cho giáo viên (ví dụ không bị dạy tiết 1, 5, nghỉ tiết 2, 3, 4)
     * - Linh động tiết Sinh hoạt lớp do có thể giáo viên chủ nhiệm bận vào tiết cuối cùng của ngày thứ 7
     *
     * @param config
     */
    public void calculateFitness(TimeTableConfiguration config) {
        // check trùng tiết của giáo viên
        double checkDuplicateTeacherLesson = checkDuplicateTeacherLesson(config);

        // các tiêu chí khác

        this.setFitness(1 - checkDuplicateTeacherLesson);
    }

    private double checkDuplicateTeacherLesson(TimeTableConfiguration config) {
        List<Clazz> clazzes = new ArrayList<>(chromosome.keySet());
        int totalClass = clazzes.size();

        List<LessonSlot> allSlots = chromosome.values().stream().map(Gene::getLessonSlots).flatMap(Collection::stream).collect(Collectors.toList());
        List<LessonSlot> slots = new ArrayList<>();
        allSlots.forEach(l -> {
            try {
                slots.add(l.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });

        final List<Long> takenClasses = new ArrayList<>();
        long countDuplicateTeacherSlot = 0;

        for (Clazz clazz : clazzes) {
            Gene gene = chromosome.get(clazz);
            takenClasses.add(clazz.getId());
            List<LessonSlot> lessonSlots = gene.getLessonSlots();
            for (int i = 2; i <= config.getDayOfWeek() + 1; i++) { // duyệt qua các ngày
                for (int j = 1; j <= config.getSlotOfDay(); j++) { // duyệt qua các tiết
                    int finalI = i;
                    int finalJ = j;
                    LessonSlot lessonSlot = lessonSlots
                            .stream()
                            .filter(l -> l.getDay() == finalI && l.getLessonSlotOrder() == finalJ
                                    && !l.getSubject().getName().equals(SpecialLesson.CHAO_CO.value)
                                    && !l.getSubject().getName().equals(SpecialLesson.SINH_HOAT_LOP.value)
                            )
                            .findFirst()
                            .orElse(null);
                    if (ObjectUtils.isEmpty(lessonSlot)) {
                        continue;
                    }
                    if (ObjectUtils.isEmpty(lessonSlot.getTeacher())) {
                        continue;
                    }
                    for (LessonSlot slot : slots) {
                        if (slot.getDay().equals(i) && slot.getLessonSlotOrder().equals(j)
                                && !ObjectUtils.isEmpty(slot.getTeacher())
                                && slot.getTeacher().getId().equals(lessonSlot.getTeacher().getId())
                                && !takenClasses.contains(slot.getClazz().getId())
                                && !slot.getSubject().getName().equals(SpecialLesson.CHAO_CO.value)
                                && !slot.getSubject().getName().equals(SpecialLesson.SINH_HOAT_LOP.value)
                        ) {
                            countDuplicateTeacherSlot++;
                            slot.setTeacher(null);
                        }
                    }
                }
            }
        }
        return (double) countDuplicateTeacherSlot / (config.getDayOfWeek() * config.getSlotOfDay() * totalClass);
    }

    public Chromosome(Map<Clazz, Gene> chromosome, TimeTableConfiguration config) {
        this.chromosome = chromosome;
        this.calculateFitness(config);
    }

    public void setChromosome(Map<Clazz, Gene> chromosome, TimeTableConfiguration config) {
        this.chromosome = chromosome;
        this.calculateFitness(config);
    }
}
