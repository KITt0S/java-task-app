package com.k1ts.check;

import com.k1ts.check.request.CheckRequest;
import com.k1ts.check.request.CheckResponse;
import com.k1ts.check.taskcheckservice.TaskCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CheckService {
    private final CheckFactory checkFactory;

    @Value("${token}")
    private String apiToken;

    public CheckResponse check(CheckRequest request) {
        if (isInvalidToken(request.getToken())) {
            return CheckResponse.failed(CheckResponse.Error.invalidToken);
        }

        Optional<TaskCheckService> taskCheckService = checkFactory.getTaskCheckService(
                request.getYear(),
                request.getCourseId(),
                request.getSubjectId(),
                request.getPracticeId(),
                request.getTaskId());

        if (taskCheckService.isEmpty()) {
            return CheckResponse.failed(CheckResponse.Error.noSuchTaskService);
        }

        return taskCheckService.get().check(request.getCode());
    }

    private boolean isInvalidToken(String token) {
        return !apiToken.equals(token);
    }
}
