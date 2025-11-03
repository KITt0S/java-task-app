package com.k1ts.check.taskcheckservice.year_2024.course_2.oop_2.practice_1;

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
        taskCheckService = applicationContext.getBean("year-2024-course-2-oop_2-practice-1-task-1", TaskCheckService.class);

        addCorrectTestCase("""
                interface Printable {
                                
                    void print();
                }
                            
                class Document implements Printable {
                            
                    @Override
                    public void print() {
                        System.out.println("Документ виведено на друк");
                    }
                }
                            
                public class Program {
                            
                    public static void main(String[] args) {
                        Printable document = new Document();
                        document.print();
                    }
                }
                """);

        addIncorrectTestCase("""
                interface Printable {
                                
                    void print();
                }
                            
                class Document implements Printable {
                            
                    @Override
                    public void print() {
                        System.out.println("Документ виведено на друк");
                    }
                }
                            
                public class Program {
                            
                    public static void main(String[] args) {
                        System.out.println("Документ виведено на друк");
                    }
                }
                """);

        addIncorrectTestCase("""
                interface Printable {
                                
                    void print();
                }
                            
                class Document implements Printable {
                            
                    @Override
                    public void print() {
                        System.out.println("Документ надруковано");
                    }
                }
                            
                public class Program {
                            
                    public static void main(String[] args) {
                        Printable document = new Document();
                        document.print();
                    }
                }
                """);
    }
}
