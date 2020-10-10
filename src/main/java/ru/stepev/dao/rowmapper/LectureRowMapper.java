package ru.stepev.dao.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import ru.stepev.dao.ClassroomDao;
import ru.stepev.dao.CourseDao;
import ru.stepev.dao.GroupDao;
import ru.stepev.dao.TeacherDao;
import ru.stepev.model.Lecture;

@Component
public class LectureRowMapper implements RowMapper<Lecture> {

	private CourseDao courseDao;
	private ClassroomDao classroomDao;
	private GroupDao groupDao;
	private TeacherDao teacherDao;
	
	public LectureRowMapper(CourseDao courseDao, ClassroomDao classroomDao, GroupDao groupDao, TeacherDao teacherDao) {
		this.courseDao = courseDao;
		this.classroomDao = classroomDao;
		this.groupDao = groupDao;
		this.teacherDao = teacherDao;
	}

	@Override
	public Lecture mapRow(ResultSet resultSet, int rowNum) throws SQLException {
		Lecture lecture = new Lecture();
		lecture.setId(resultSet.getInt("id"));
		lecture.setDailyScheduleId(resultSet.getInt("dailyschedule_id"));
		lecture.setTime(resultSet.getObject("local_time", LocalTime.class));
		courseDao.findById(resultSet.getInt("course_id")).ifPresent(lecture::setCourse);
		classroomDao.findById(resultSet.getInt("classroom_id")).ifPresent(lecture::setClassRoom);
		groupDao.findById(resultSet.getInt("group_id")).ifPresent(lecture::setGroup);
		teacherDao.findById(resultSet.getInt("teacher_id")).ifPresent(lecture::setTeacher);
		return lecture;
	}
}
