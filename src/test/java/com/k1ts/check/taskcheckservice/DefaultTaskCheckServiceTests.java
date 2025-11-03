package com.k1ts.check.taskcheckservice;

import com.k1ts.check.request.CheckResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.Set;

@SpringBootTest
public class DefaultTaskCheckServiceTests {

    protected TaskCheckService taskCheckService;

    private final Set<String> correctTestCases = new HashSet<>();
    private final Set<String> incorrectTestCases = new HashSet<>();

    @Test
    public void testCorrectTestCases() {
        for (String correctTestCase : correctTestCases) {
            CheckResponse response = taskCheckService.check(correctTestCase);

            Assertions.assertFalse(response.getSuccessResult().isEmpty(), response.getFailedResult().toString());
            Assertions.assertTrue(response.getFailedResult().isEmpty(), response.getFailedResult().toString());
        }
    }

    @Test
    public void testIncorrectTestCases() {
        for (String incorrectTestCase : incorrectTestCases) {
            CheckResponse response = taskCheckService.check(incorrectTestCase);

            Assertions.assertFalse(response.getFailedResult().isEmpty());
        }
    }

    public void addCorrectTestCase(String correctTestCase) {
        correctTestCases.add(correctTestCase);
    }

    public void addIncorrectTestCase(String incorrectTestCase) {
        incorrectTestCases.add(incorrectTestCase);
    }
}
