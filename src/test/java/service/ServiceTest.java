package service;

import domain.Grade;
import domain.Homework;
import domain.Pair;
import domain.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import repository.GradeXMLRepository;
import repository.HomeworkXMLRepository;
import repository.StudentXMLRepository;
import validation.GradeValidator;
import validation.HomeworkValidator;
import validation.StudentValidator;
import validation.Validator;

import java.util.Objects;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

public class ServiceTest {
    private Validator<Student> studentValidator;
    private Validator<Homework> homeworkValidator;
    private Validator<Grade> gradeValidator;

    private StudentXMLRepository fileRepository1;
    private HomeworkXMLRepository fileRepository2;
    private GradeXMLRepository fileRepository3;

    private Service service;

    @BeforeEach
    public void setUp() throws Exception {
        studentValidator = new StudentValidator();
        homeworkValidator = new HomeworkValidator();
        gradeValidator = new GradeValidator();

        fileRepository1 = new StudentXMLRepository(studentValidator, "students.xml");
        fileRepository2 = new HomeworkXMLRepository(homeworkValidator, "homework.xml");
        fileRepository3 = new GradeXMLRepository(gradeValidator, "grades.xml");

        service = new Service(fileRepository1, fileRepository2, fileRepository3);
    }

    @Test
    public void findAllStudents() {
        assertEquals(fileRepository1.findAll(), service.findAllStudents());
    }

    @Test
    public void testSaveGradeNewGradeAdded() {
        int delivered = 8;
        double baseGrade = 4;
        double gradeValue = baseGrade;
        String idStudent = "2";
        String idHomework = "3";
        String feedback = "Good job";
        int deadline = fileRepository2.findOne(idHomework).getDeadline();
        fileRepository3.delete(new Pair<>(idStudent, idHomework));

        if (delivered - deadline > 2) {
            gradeValue = 1;
        } else {
            gradeValue = baseGrade - 2.5 * (delivered - deadline);
        }

        Iterable<Grade> grades = service.findAllGrades();

        int numberOfGradesBefore = Math.toIntExact(StreamSupport.stream(grades.spliterator(), false).count());
        service.saveGrade(idStudent, idHomework, baseGrade, delivered, feedback);
        int numberOfGradesAfter = Math.toIntExact(StreamSupport.stream(grades.spliterator(), false).count());

        Grade savedGrade = StreamSupport.stream(service.findAllGrades().spliterator(), false).filter(g -> Objects.equals(g.getID(), new Pair<>(idStudent, idHomework))).findFirst().orElse(null);

//        assertNotNull(savedGrade);
        assertTrue(savedGrade != null);

        double finalGradeValue = gradeValue;
        assertAll("Should check if length was increased by 1 and each field match one by one",
                () -> assertEquals(numberOfGradesBefore + 1, numberOfGradesAfter),
                () -> assertEquals(idStudent, savedGrade.getID().getObject1()),
                () -> assertEquals(idHomework, savedGrade.getID().getObject2()),
                () -> assertEquals(finalGradeValue, savedGrade.getGrade()),
                () -> assertEquals(feedback, savedGrade.getFeedback()),
                () -> assertEquals(delivered, savedGrade.getDeliveryWeek())
        );
    }

    @Test
    public void testSaveGradeAlreadyExists() {
        Grade grade = new Grade(new Pair<>("2", "1"), 5, 5, "None");
        service.saveGrade("2", "1", 5, 5, "None");
        Integer returnedValue = service.saveGrade("2", "1", 5, 5, "None");
        assertNotEquals(1, returnedValue);
    }

    @Test
    public void testDeleteStudentWithNullId() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteStudent(null));
    }

    @Test
    public void testDeleteStudentWithGivenId() {
        Student student = new Student("55", "John", 452);
        fileRepository1.save(student);
        int numberOfStudentsBefore = Math.toIntExact(StreamSupport.stream(service.findAllStudents().spliterator(), false).count());
        service.deleteStudent("55");
        int numberOfStudentsAfter = Math.toIntExact(StreamSupport.stream(service.findAllStudents().spliterator(), false).count());

        assertEquals(numberOfStudentsBefore - 1, numberOfStudentsAfter);
        assertFalse(StreamSupport.stream(service.findAllStudents().spliterator(), false).anyMatch(s -> s.equals(student)));
    }

    @ParameterizedTest
    @ValueSource(ints = {6, 7, 8, 9, 7})
    public void testUpdateHomeWorkValidWeeks(int startDate) {
        String id = "3";
        Homework hw = fileRepository2.findOne(id);
        assertNotNull(hw);
        service.updateHomework(id, hw.getDescription(), hw.getDeadline(), startDate);
        Homework modifiedHomework = StreamSupport.stream(service.findAllHomework().spliterator(), false).filter(homework -> homework.getID().equals(id)).findFirst().orElse(null);
        assertEquals(startDate, Objects.requireNonNull(modifiedHomework).getStartline());
    }
}