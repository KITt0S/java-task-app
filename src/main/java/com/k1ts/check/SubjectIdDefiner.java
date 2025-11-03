package com.k1ts.check;

import org.springframework.stereotype.Component;

@Component
public class SubjectIdDefiner {

    public String getSubjectIdAsText(int courseId, int subjectId) {
        if (courseId == 2) {
            if (subjectId == 1) {
                return "java";
            } else if (subjectId == 2) {
                return "oop";
            } else if (subjectId == 3) {
                return "oop_2";
            }
        } else if (courseId == 3) {
            if (subjectId == 1) {
                return "cpp";
            }
        } else if (courseId == 4) {
            if (subjectId == 1) {
                return "java";
            }
        }

        return null;
    }
}
