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
public class LessonSlot implements Comparable<LessonSlot>, Cloneable {

    Integer day;

    Integer lessonSlotOrder;

    Clazz clazz;

    Subject subject;

    Teacher teacher;

    // for temporary only
    boolean isDuplicated;

    public LessonSlot(Integer day, Integer lessonSlotOrder, Clazz clazz, Subject subject, Teacher teacher) {
        this.day = day;
        this.lessonSlotOrder = lessonSlotOrder;
        this.clazz = clazz;
        this.subject = subject;
        this.teacher = teacher;
    }

    @Override
    public int compareTo(LessonSlot o) {
        return this.getClazz().getName().compareTo(o.getClazz().getName());
    }

    @Override
    public LessonSlot clone() throws CloneNotSupportedException {
        LessonSlot clone = (LessonSlot) super.clone();
        clone.setTeacher(this.teacher.clone());
        clone.setSubject(this.subject.clone());
        clone.setClazz(this.clazz.clone());
        return clone;
    }
}
