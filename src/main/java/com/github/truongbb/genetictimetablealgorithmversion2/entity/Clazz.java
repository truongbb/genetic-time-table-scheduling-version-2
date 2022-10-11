package com.github.truongbb.genetictimetablealgorithmversion2.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Clazz implements Cloneable {

    Long id;

    String name;

    // danh sách môn học của lớp đó
    List<Subject> subjects;

    @Override
    protected Clazz clone() throws CloneNotSupportedException {
        Clazz clone = (Clazz) super.clone();
        List<Subject> sub = new ArrayList<>();
        this.getSubjects().forEach(s -> {
            try {
                sub.add(s.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        });
        clone.setSubjects(sub);
        return clone;
    }
}
