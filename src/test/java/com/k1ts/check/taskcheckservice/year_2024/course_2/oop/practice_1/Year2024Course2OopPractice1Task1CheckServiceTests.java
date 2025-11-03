package com.k1ts.check.taskcheckservice.year_2024.course_2.oop.practice_1;

import com.k1ts.check.taskcheckservice.DefaultTaskCheckServiceTests;
import com.k1ts.check.taskcheckservice.TaskCheckService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
public class Year2024Course2OopPractice1Task1CheckServiceTests extends DefaultTaskCheckServiceTests {

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        taskCheckService = applicationContext.getBean("year-2024-course-2-oop-practice-1-task-1", TaskCheckService.class);

        addCorrectTestCase("""
                public class JavaIsFunClass {
                    public static void main(String[] args) {
                        System.out.println("Java — це весело!");
                    }
                }
                """);

        addIncorrectTestCase("""
                public class JavaIsFunClass {
                    public static void main(String[] args) {
                        System.out.println("");
                    }
                }
                """);

        addIncorrectTestCase("""
                public class JavaIsFunClass {
                    public static void main(String[] args) {
                        System.out.println(2024);
                    }
                }
                """);
    }
}
