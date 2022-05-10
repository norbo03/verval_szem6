package service;

import domain.Grade;
import domain.Homework;
import domain.Pair;
import domain.Student;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import repository.GradeXMLRepository;
import repository.HomeworkXMLRepository;
import repository.StudentXMLRepository;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ServiceMockTest {
    private static StudentXMLRepository studentMockRepository;
    private static HomeworkXMLRepository homeworkMockRepository;
    private static GradeXMLRepository gradeMockRepository;

    private static Service service;

    @BeforeAll
    static void init() {
        studentMockRepository = mock(StudentXMLRepository.class);
        homeworkMockRepository = mock(HomeworkXMLRepository.class);
        gradeMockRepository = mock(GradeXMLRepository.class);

        service = new Service(studentMockRepository, homeworkMockRepository, gradeMockRepository);
    }

    @Test
    public void testSaveGradeNewGradeAdded() {
        int delivered = 8;
        double baseGrade = 4;
        double gradeValue = baseGrade;
        String idStudent = "2";
        String idHomework = "3";
        String feedback = "Good job";
        int deadline = 9;
        gradeValue = baseGrade - 2.5 * (delivered - deadline);

        Grade grade = new Grade(new Pair<>(idStudent, idHomework), gradeValue, delivered, feedback);

        when(gradeMockRepository.save(grade)).thenReturn(grade);
        when(studentMockRepository.findOne(idStudent)).thenReturn(new Student(idStudent, "Aladar", 542));
        when(homeworkMockRepository.findOne(idHomework)).thenReturn(new Homework(idHomework, "GUI", 9, 6));

        int returnValue = service.saveGrade(idStudent, idHomework, baseGrade, delivered, feedback);
        assertEquals(1, returnValue);
    }

    @Test
    public void testDeleteStudentWithGivenId() {
        Student student = new Student("3", "Aladar", 542);
        when(studentMockRepository.delete(Mockito.anyString())).thenReturn(student);

        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String randomString = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        int returnValue = service.deleteStudent(randomString);
        assertEquals(1, returnValue);
    }

    @Test
    public void findAllStudents() {
        when(studentMockRepository.findAll()).thenReturn(new ArrayList<>());
        service.findAllStudents();
        verify(studentMockRepository, times(1)).findAll();
    }

}