package com.k1ts.check.taskcheckservice;

import com.k1ts.check.CheckResult;
import com.k1ts.check.request.CheckResponse;

import java.util.regex.Pattern;

public abstract class DefaultAbstractTaskCheckService implements TaskCheckService {

    @Override
    public CheckResponse check(String code) {
        if (Pattern.compile(".*?System\\.getenv.*?").matcher(code).find()) {
            return CheckResponse.failed(CheckResponse.Error.wrongCodeDetected);
        }

        if (Pattern.compile(".*?(Class\\.forName|\\.invoke|ClassLoader).*?").matcher(code).find()) {
            return CheckResponse.failed(CheckResponse.Error.wrongCodeDetected);
        }

        if (Pattern.compile(".*?ClassLoader.*?").matcher(code).find()) {
            return CheckResponse.failed(CheckResponse.Error.wrongCodeDetected);
        }

        if (Pattern.compile(".*?ProcessBuilder.*?").matcher(code).find()) {
            return CheckResponse.failed(CheckResponse.Error.wrongCodeDetected);
        }

        if (Pattern.compile(".*?\\bProcess\\b.*?").matcher(code).find()) {
            return CheckResponse.failed(CheckResponse.Error.wrongCodeDetected);
        }

        if (Pattern.compile(".*?\\bRuntime\\b.*?").matcher(code).find()) {
            return CheckResponse.failed(CheckResponse.Error.wrongCodeDetected);
        }

        if (Pattern.compile(".*?(SELECT|INSERT|UPDATE|DELETE|DROP|ALTER).*?").matcher(code).find()) {
            return CheckResponse.failed(CheckResponse.Error.wrongCodeDetected);
        }
        
        CheckResult checkResult = doCheck(code);

        boolean taskPassed = !checkResult.getSuccessResult().isEmpty() && checkResult.getFailedResult().isEmpty();

        return CheckResponse
                .builder()
                .success(true)
                .error(CheckResponse.Error.ok)
                .successResult(checkResult.getSuccessResult())
                .failedResult(checkResult.getFailedResult())
                .taskPassed(taskPassed)
                .codeResult(checkResult.getConsoleOutput())
                .build();
    }

    public abstract CheckResult doCheck(String code);
}
