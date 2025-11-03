package com.k1ts.check.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CheckRequest {
    private String token;

    private int year;

    private int courseId;

    private int subjectId;

    private int practiceId;

    private int taskId;

    private String code;
}
