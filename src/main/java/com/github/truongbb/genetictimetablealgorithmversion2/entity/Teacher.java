package com.github.truongbb.genetictimetablealgorithmversion2.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Teacher {

    Long id;

    String name;

    List<Subject> availableSubjects; // những môn giáo viên này có thể dạy

    Integer maxTeachingLessonPerWeek; // số tiết tối đa có thể dạy trong tuần (custom)

    Clazz headClazz;

}
