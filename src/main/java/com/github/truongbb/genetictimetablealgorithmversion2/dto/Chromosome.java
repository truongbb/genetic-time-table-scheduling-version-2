package com.github.truongbb.genetictimetablealgorithmversion2.dto;

import com.github.truongbb.genetictimetablealgorithmversion2.config.TimeTableConfiguration;
import com.github.truongbb.genetictimetablealgorithmversion2.entity.Clazz;
import com.github.truongbb.genetictimetablealgorithmversion2.entity.LessonSlot;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
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
        List<Clazz> clazzes = new ArrayList<>(chromosome.keySet());
        int totalClass = clazzes.size();

        final List<Long> takenClasses = new ArrayList<>();
        final List<Long> takenTeachers = new ArrayList<>();

        long countDuplicateTeacherSlot = 0;

        for (int k = 0; k < totalClass; k++) {
            Clazz clazz = clazzes.get(k);
            Gene gene = chromosome.get(clazz);
            takenClasses.add(clazz.getId());
            List<LessonSlot> lessonSlots = gene.getLessonSlots();
            for (int i = 2; i <= config.getDayOfWeek(); i++) { // duyệt qua các ngày
                for (int j = 1; j <= config.getSlotOfDay(); j++) { // duyệt qua các tiết
                    int finalI = i;
                    int finalJ = j;
                    LessonSlot lessonSlot = lessonSlots.stream().filter(l -> l.getDay() == finalI && l.getLessonSlotOrder() == finalJ).findFirst().orElse(null);
                    if (ObjectUtils.isEmpty(lessonSlot)) {
                        continue;
                    }
                    Long teacherId = lessonSlot.getTeacher().getId();
                    if (takenTeachers.contains(teacherId)) {
                        continue;
                    }
                    takenTeachers.add(teacherId);
                    List<LessonSlot> sameTimeSlots = lessonSlots
                            .stream()
                            .filter(slot -> slot.getDay() == finalI
                                    && slot.getLessonSlotOrder() == finalJ
                                    && slot.getClazz().getName().equals(clazz.getName())
                                    && takenClasses.contains(slot.getClazz().getId())
                            )
                            .collect(Collectors.toList());
                    countDuplicateTeacherSlot += sameTimeSlots.stream().filter(sl -> !ObjectUtils.isEmpty(sl.getTeacher()) && sl.getTeacher().getId().equals(lessonSlot.getTeacher().getId())).count();
                }
            }
        }
        double duplicateTeacherPercent = (double) countDuplicateTeacherSlot / (config.getDayOfWeek() * config.getSlotOfDay());

        // các tiêu chí khác

        this.setFitness(duplicateTeacherPercent);
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
