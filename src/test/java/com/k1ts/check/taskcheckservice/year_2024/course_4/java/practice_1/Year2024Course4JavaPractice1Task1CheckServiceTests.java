package com.k1ts.check.taskcheckservice.year_2024.course_4.java.practice_1;

import com.k1ts.check.taskcheckservice.DefaultTaskCheckServiceTests;
import com.k1ts.check.taskcheckservice.TaskCheckService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class Year2024Course4JavaPractice1Task1CheckServiceTests extends DefaultTaskCheckServiceTests {
    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        taskCheckService = applicationContext.getBean("year-2024-course-4-java-practice-1-task-1", TaskCheckService.class);

        addCorrectTestCase("""
                import java.util.*;
                import java.text.SimpleDateFormat;
                
                public class DateApp {
                         public static void main(String[] args) {
                             Date date = new Date();
                             System.out.println(new SimpleDateFormat("yyyy/MM/dd").format(date));
                         }
                     }
                """);

        addIncorrectTestCase("""
                import java.util.*;
                import java.text.SimpleDateFormat;
                
                public class DateApp {
                         public static void main(String[] args) {
                             Date date = new Date();
                             System.out.println(new SimpleDateFormat("yyyy/m/dd").format(date));
                         }
                     }
                """);

        addIncorrectTestCase("""
                import java.util.*;
                import java.text.SimpleDateFormat;
                
                public class DateApp {
                         public static void main(String[] args) {
                             Date date = new Date();
                             System.out.println(date);
                         }
                     }
                """);
    }
}
