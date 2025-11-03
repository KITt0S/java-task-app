package com.k1ts.check;

import com.k1ts.check.taskcheckservice.TaskCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CheckFactory {
    private final Map<String, TaskCheckService> taskCheckServiceMap;
    private final SubjectIdDefiner subjectIdDefiner;

    public Optional<TaskCheckService> getTaskCheckService(int year, int courseId, int subjectId, int practiceId, int taskId) {
        TaskCheckService taskCheckService = taskCheckServiceMap.get( "year-" + year +
                "-course-" + courseId +
                "-" + subjectIdDefiner.getSubjectIdAsText(courseId, subjectId) +
                "-practice-" + practiceId +
                "-task-" + taskId);

        if (taskCheckService == null) {
            return Optional.empty();
        }

        return Optional.of(taskCheckService);
    }
}
