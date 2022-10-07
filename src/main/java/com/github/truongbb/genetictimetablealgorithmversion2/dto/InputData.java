package com.github.truongbb.genetictimetablealgorithmversion2.dto;

import com.github.truongbb.genetictimetablealgorithmversion2.entity.Clazz;
import com.github.truongbb.genetictimetablealgorithmversion2.entity.Subject;
import com.github.truongbb.genetictimetablealgorithmversion2.entity.Teacher;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InputData implements Serializable {

    List<Teacher> teachers;

    List<Subject> subjects;

    List<Clazz> clazzes;

}
