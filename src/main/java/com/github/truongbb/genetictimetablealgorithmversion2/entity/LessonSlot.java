package com.github.truongbb.genetictimetablealgorithmversion2.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LessonSlot {

    Integer day;

    Integer lessonSlotOrder;

    Clazz clazz;

    Subject subject;

    Teacher teacher;

}
