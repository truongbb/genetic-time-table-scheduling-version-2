package com.github.truongbb.genetictimetablealgorithmversion2.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Teacher implements Cloneable {

    Long id;

    String name;

    List<Subject> availableSubjects; // những môn giáo viên này có thể dạy

    Integer maxTeachingLessonPerWeek; // số tiết tối đa có thể dạy trong tuần (custom)

    Clazz headClazz;

    Integer lessonLeftPerWeek; // số tiết còn lại trống --> temporary only

    @Override
    protected Teacher clone() throws CloneNotSupportedException {
        Teacher clone = (Teacher) super.clone();
        List<Subject> sub = new ArrayList<>();
        this.getAvailableSubjects().forEach(s -> {
            try {
                sub.add(s.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });
        clone.setAvailableSubjects(sub);
        clone.setHeadClazz(ObjectUtils.isEmpty(this.getHeadClazz()) ? null : this.getHeadClazz().clone());
        return clone;
    }
}
