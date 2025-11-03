package com.k1ts.check.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CheckResponse {
    private boolean success;

    private Error error;

    private List<String> successResult;

    private List<String> failedResult;

    private String codeResult;

    private String errorResult;

    private boolean taskPassed;

    public static CheckResponse failed(Error error) {
        return builder().success(false).error(error).build();
    }

    public enum Error {
        ok,
        noSuchTaskService,
        wrongCodeDetected,
        invalidToken
    }
}
