package ru.stepev.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ru.stepev.dao.ClassroomDao;
import ru.stepev.dao.CourseDao;
import ru.stepev.dao.DailyScheduleDao;
import ru.stepev.dao.GroupDao;
import ru.stepev.dao.LectureDao;
import ru.stepev.dao.TeacherDao;
import ru.stepev.exception.ClassroomHasNotQuiteCapacity;
import ru.stepev.exception.ClassroomIsNotFreeException;
import ru.stepev.exception.EntityNotFoundException;
import ru.stepev.exception.FildsNotFoundException;
import ru.stepev.exception.GroupCanNotStudyCourseException;
import ru.stepev.exception.GroupIsNotFreeException;
import ru.stepev.exception.TeacherIsNotFreeException;
import ru.stepev.exception.TecherIsNotAbleTheachCourseException;
import ru.stepev.model.Classroom;
import ru.stepev.model.Course;
import ru.stepev.model.Group;
import ru.stepev.model.Lecture;
import ru.stepev.model.Teacher;

@Component
@Slf4j
public class LectureService {

	private LectureDao lectureDao;
	private DailyScheduleDao dailyScheduleDao;
	private CourseDao courseDao;
	private ClassroomDao classroomDao;
	private GroupDao groupDao;
	private TeacherDao teacherDao;

	@Value("${durationOfLecture}")
	private int durationOfLecture;

	public LectureService(DailyScheduleDao dailyScheduleDao, CourseDao courseDao, ClassroomDao classroomDao,
			GroupDao groupDao, TeacherDao teacherDao, LectureDao lectureDao) {
		this.dailyScheduleDao = dailyScheduleDao;
		this.courseDao = courseDao;
		this.classroomDao = classroomDao;
		this.groupDao = groupDao;
		this.teacherDao = teacherDao;
		this.lectureDao = lectureDao;
	}

	public void add(Lecture lecture) {
		try {
			checkAllFieldsOfLectureForExist(lecture);
			isClassroomFree(lecture);
			isGroupFree(lecture);
			isTeacherFree(lecture);
			isTeacherCanTeachCourse(lecture.getTeacher(), lecture.getCourse());
			isGroupCanStudyCourse(lecture.getGroup(), lecture.getCourse());
			isClassroomHasQuiteCapacity(lecture.getClassRoom(), lecture.getGroup());
			isLectureExist(lecture);
			log.warn("Lecture with time {} was not created", lecture.getTime());
		} catch (EntityNotFoundException e) {
			lectureDao.create(lecture);
			log.debug("Lecture with time {} was created", lecture.getTime());
		}
	}

	public void checkAllFieldsOfLectureForExist(Lecture lecture) {
		boolean dailyScheduleExist = dailyScheduleDao.findById(lecture.getDailyScheduleId()).isPresent();
		boolean courseExist = courseDao.findById(lecture.getCourse().getId()).isPresent();
		boolean classroomExist = classroomDao.findById(lecture.getClassRoom().getId()).isPresent();
		boolean groupExist = groupDao.findById(lecture.getGroup().getId()).isPresent();
		boolean teacherExist = teacherDao.findById(lecture.getTeacher().getId()).isPresent();

		if (!(dailyScheduleExist && courseExist && classroomExist && groupExist && teacherExist)) {
			throw new FildsNotFoundException(
					String.format("Some filds of lecture doesn't exist!"));
		} 
	}

	public void isLectureExist(Lecture lecture) {
		if (lectureDao.findById(lecture.getId()).isEmpty()) {
			throw new EntityNotFoundException(String.format("Lecture with ID %s doesn't exist", lecture.getId()));
		}
	}

	public void update(Lecture lecture) {
		isLectureExist(lecture);
		isGroupFree(lecture);
		checkAllFieldsOfLectureForExist(lecture);
		isClassroomFree(lecture);
		isTeacherFree(lecture);
		isTeacherCanTeachCourse(lecture.getTeacher(), lecture.getCourse());
		isGroupCanStudyCourse(lecture.getGroup(), lecture.getCourse());
		isClassroomHasQuiteCapacity(lecture.getClassRoom(), lecture.getGroup());
		lectureDao.update(lecture);
		log.debug("Lecture with time {} was updated", lecture.getTime());

	}

	public void delete(Lecture lecture) {
		isLectureExist(lecture);
		lectureDao.delete(lecture.getId());
		log.debug("Lecture with time {} was deleted", lecture.getTime());

	}

	public Optional<Lecture> getById(int lectureId) {
		return lectureDao.findById(lectureId);
	}

	public List<Lecture> getByDailyScheduleId(int dailyScheduleId) {
		return lectureDao.findByDailyScheduleId(dailyScheduleId);
	}

	public List<Lecture> getAll() {
		return lectureDao.findAll();
	}

	private void isGroupFree(Lecture lecture) {
		if (lectureDao.findByDailyScheduleIdAndTimeAndGroupId(lecture.getDailyScheduleId(), lecture.getTime(),
				lecture.getTime().plusMinutes(durationOfLecture), lecture.getGroup().getId()).isPresent()) {
			throw new GroupIsNotFreeException(String.format("Group name %s is not free", lecture.getGroup().getName()));
		}
	}

	private void isClassroomFree(Lecture lecture) {
		if (lectureDao.findByDailyScheduleIdAndTimeAndClassroomId(lecture.getDailyScheduleId(), lecture.getTime(),
				lecture.getTime().plusMinutes(durationOfLecture), lecture.getClassRoom().getId()).isPresent()) {
			throw new ClassroomIsNotFreeException(
					String.format("Classroom with address %s is not free", lecture.getClassRoom().getAddress()));
		}
	}

	private void isTeacherFree(Lecture lecture) {
		if (lectureDao.findByDailyScheduleIdAndTimeAndTeacherId(lecture.getDailyScheduleId(), lecture.getTime(),
				lecture.getTime().plusMinutes(durationOfLecture), lecture.getTeacher().getId()).isPresent()) {
			throw new TeacherIsNotFreeException(
					String.format("Teacher name %s is not free", lecture.getTeacher().getLastName()));
		}
	}

	private void isClassroomHasQuiteCapacity(Classroom classRoom, Group group) {
		if (classRoom.getCapacity() < group.getStudents().size()) {
			throw new ClassroomHasNotQuiteCapacity(
					String.format("Classroom with address %s doesn't have quite capacity", classRoom.getAddress()));
		}
	}

	private void isGroupCanStudyCourse(Group group, Course course) {
		if (groupDao.findByGroupIdAndCourseId(group.getId(), course.getId()).isEmpty()) {
			throw new GroupCanNotStudyCourseException(
					String.format("Group with name %s can't study course %s", group.getName(), course.getName()));
		}
	}

	private void isTeacherCanTeachCourse(Teacher teacher, Course course) {
		if (!teacher.getCourses().contains(course)) {
			throw new TecherIsNotAbleTheachCourseException(
					String.format("Teacher name %s can't teach course %s", teacher.getLastName(), course.getName()));
		}
	}
}
