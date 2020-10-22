package ru.stepev.service;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import ru.stepev.dao.CourseDao;
import ru.stepev.dao.TeacherDao;
import ru.stepev.model.Course;
import ru.stepev.model.Student;
import ru.stepev.model.Teacher;

@Component
public class CourseService {

	private CourseDao courseDao;
	private TeacherDao teacherDao;

	public CourseService(CourseDao courseDao, TeacherDao teacherDao) {
		this.courseDao = courseDao;
		this.teacherDao = teacherDao;
	}

	public void add(Course course) {
		if (!isCourseExist(course) && resorcesAreAvailable(course)) {
			courseDao.create(course);
		}
	}

	private boolean resorcesAreAvailable(Course course) {
		List<Teacher> availableTeachers = teacherDao.findAll().stream().filter(t -> t.getCourses().contains(course))
				.collect(toList());
		if (availableTeachers.size() > 0)
			return true;
		else
			return false;
	}

	private boolean isCourseExist(Course course) {
		return courseDao.findById(course.getId()).isPresent();
	}

	public void update(Course course) {
		if (isCourseExist(course) && resorcesAreAvailable(course)) {
			courseDao.update(course);
		}
	}

	public void delete(Course course) {
		if (isCourseExist(course)) {
			courseDao.delete(course.getId());
		}
	}

	public Optional<Course> getById(int courseId) {
		return courseDao.findById(courseId);
	}

	public List<Course> getAll() {
		return courseDao.findAll();
	}

	public List<Course> getByTeacher(Teacher teacher) {
		return courseDao.findByTeacherId(teacher.getId());
	}

	public List<Course> getByStudent(Student student) {
		return courseDao.findByStudentId(student.getId());
	}

}
