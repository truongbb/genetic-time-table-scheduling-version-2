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
public class Subject implements Cloneable {

    Long id;

    String code;

    String name;

    Integer numberOfLessonPerWeek; // số tiết trong tuần

    // môn này có tiết thực hành hay không
    Boolean isLab;

    @Override
    protected Subject clone() throws CloneNotSupportedException {
        return (Subject) super.clone();
    }
}
