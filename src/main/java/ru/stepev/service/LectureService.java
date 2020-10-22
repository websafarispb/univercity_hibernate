package ru.stepev.service;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import ru.stepev.dao.ClassroomDao;
import ru.stepev.dao.CourseDao;
import ru.stepev.dao.DailyScheduleDao;
import ru.stepev.dao.GroupDao;
import ru.stepev.dao.LectureDao;
import ru.stepev.dao.TeacherDao;
import ru.stepev.model.Classroom;
import ru.stepev.model.Course;
import ru.stepev.model.Group;
import ru.stepev.model.Lecture;
import ru.stepev.model.Teacher;

@Component
public class LectureService {

	private LectureDao lectureDao;
	private DailyScheduleDao dailyScheduleDao;
	private CourseDao courseDao;
	private ClassroomDao classroomDao;
	private GroupDao groupDao;
	private TeacherDao teacherDao;

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
		if (!lectureIsExist(lecture) && isDataCorrect(lecture) && resorcesAreAvailable(lecture)
				&& isTeacherCanDoCourse(lecture.getTeacher(), lecture.getCourse())
				&& isGroupCanDoCourse(lecture.getGroup(), lecture.getCourse())
				&& isClassroomHasQuiteCapacity(lecture.getClassRoom(), lecture.getGroup())) {
			lectureDao.create(lecture);
		} else
			System.out.println("LECTURE HAS BEEN CREATED WRONG DATE!!!");
	}

	public boolean isClassroomHasQuiteCapacity(Classroom classRoom, Group group) {
		if (classRoom.getCapacity() >= group.getStudents().size())
			return true;
		else
			return false;
	}

	public boolean isGroupCanDoCourse(Group group, Course course) {
		if (group.getStudents().size() == 0)
			return false;
		else
			return group.getStudents().get(0).getCourses().contains(course);
	}

	public boolean isTeacherCanDoCourse(Teacher teacher, Course course) {
		return teacher.getCourses().contains(course);
	}

	public boolean isDataCorrect(Lecture lecture) {
		boolean dailyScheduleExist = dailyScheduleDao.findById(lecture.getDailyScheduleId()).isPresent();
		boolean courseExist = courseDao.findById(lecture.getCourse().getId()).isPresent();
		boolean classroomExist = classroomDao.findById(lecture.getClassRoom().getId()).isPresent();
		boolean groupExist = groupDao.findById(lecture.getGroup().getId()).isPresent();
		boolean teacherExist = teacherDao.findById(lecture.getTeacher().getId()).isPresent();
		return dailyScheduleExist && courseExist && classroomExist && groupExist && teacherExist;
	}

	public boolean resorcesAreAvailable(Lecture lecture) {
		List<Lecture> lectures = lectureDao.findByDailyScheduleId(lecture.getDailyScheduleId());
		if (lectures.size() == 0) {
			return true;
		} else {
			for (Lecture lectureDB : lectures) {
				LocalTime startTimeLectureDB = lectureDB.getTime();
				LocalTime finishTimeLectureDB = lectureDB.getTime().plusHours(1);
				LocalTime startTimeCreatedLecture = lecture.getTime();
				LocalTime finishTimeCreatedLecture = lecture.getTime().plusHours(1);

				Teacher teacherDB = lectureDB.getTeacher();
				Teacher teacherCreatedLecture = lecture.getTeacher();

				Group groupDB = lectureDB.getGroup();
				Group groupCreatedLecture = lecture.getGroup();

				Classroom classroomDB = lectureDB.getClassRoom();
				Classroom classroomCreateedLecture = lecture.getClassRoom();

				if (teacherDB.equals(teacherCreatedLecture) || groupDB.equals(groupCreatedLecture)
						|| classroomDB.equals(classroomCreateedLecture)) {
					if (startTimeCreatedLecture.isBefore(finishTimeLectureDB)
							&& finishTimeCreatedLecture.isAfter(startTimeLectureDB)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean lectureIsExist(Lecture lecture) {
		return lectureDao.findById(lecture.getId()).isPresent();
	}

	public void update(Lecture lecture) {
		if (lectureIsExist(lecture) && isDataCorrect(lecture) && resorcesAreAvailable(lecture)
				&& isTeacherCanDoCourse(lecture.getTeacher(), lecture.getCourse())
				&& isGroupCanDoCourse(lecture.getGroup(), lecture.getCourse())
				&& isClassroomHasQuiteCapacity(lecture.getClassRoom(), lecture.getGroup())) {
			lectureDao.update(lecture);
		}
	}

	public void delete(Lecture lecture) {
		if (lectureIsExist(lecture)) {
			lectureDao.delete(lecture.getId());
		}
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
}
