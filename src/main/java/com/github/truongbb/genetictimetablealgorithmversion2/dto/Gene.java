package com.github.truongbb.genetictimetablealgorithmversion2.dto;

import com.github.truongbb.genetictimetablealgorithmversion2.entity.LessonSlot;
import com.github.truongbb.genetictimetablealgorithmversion2.util.StringUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Gene {

    /**
     * Một gene chứa thông tin thời khóa biểu của một lớp trong một tuần
     * --> một list các lesson slot
     * --> encode thông tin của list các lesson slot thành gene
     */

    List<LessonSlot> lessonSlots;


    /**
     * Gene sẽ biểu thị thông tin các tiết học trong 1 tuần của 1 lớp, có dạng:
     * AABBCCCDDD;AABBCCCDDD;AABBCCCDDD;....
     * <p>
     * Mỗi cụm ngăn cách nhau bởi dấu ";" sẽ là thông tin về 1 tiết học, bao gồm:
     * AA: thứ mấy, ví dụ thứ 2 là 02
     * BB: tiết mấy, ví dụ tiết 4 là 04
     * CCC: ID của môn học
     * DDD: id của giáo viên
     * <p>
     * Nếu bất kỳ thông tin nào trong các thông tin bên trên null sẽ được thể hiện trong gene bằng ký tự X.
     * Ví dụ: 0204XXX003 (môn học bị null, chưa xếp chẳng hạn)
     */
    String gene;

    // chuyển dữ liệu thường thành gene
    public void encode() {
        if (CollectionUtils.isEmpty(this.lessonSlots)) {
            return;

        }
        this.gene = this.lessonSlots
                .stream()
                .map(l -> StringUtil.convert2DigitsNumber((long) l.getDay())
                        + StringUtil.convert2DigitsNumber((long) l.getLessonSlotOrder())
                        + (ObjectUtils.isEmpty(l.getSubject().getId()) ? "XXX" : StringUtil.convert3DigitsNumber(l.getSubject().getId()))
                        + (ObjectUtils.isEmpty(l.getTeacher().getId()) ? "XXX" : StringUtil.convert3DigitsNumber(l.getTeacher().getId()))
                )
                .collect(Collectors.joining(";"));
    }

    public Gene(List<LessonSlot> lessonSlots) {
        this.lessonSlots = lessonSlots;
        this.encode();
    }

}
