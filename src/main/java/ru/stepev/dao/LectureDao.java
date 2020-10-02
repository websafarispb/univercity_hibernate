package ru.stepev.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import ru.stepev.dao.rowmapper.LectureRowMapper;
import ru.stepev.model.Group;
import ru.stepev.model.Lecture;

@Component
public class LectureDao {

	private static final String CREATE_LECTURE_QUERY = "INSERT INTO lectures (dailyschedule_id, local_date, local_time, course_id, classroom_id, group_id, teacher_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_BY_LECTURE_ID = "UPDATE lectures SET dailyschedule_id = ?, local_date = ?, local_time = ?, course_id = ?, classroom_id = ?, group_id = ?, teacher_id = ? WHERE id = ?";
	private static final String GET_ALL = "SELECT * FROM lectures";
	private static final String DELETE_LECTURE_BY_ID = "DELETE  FROM lectures WHERE id = ?";
	private static final String GET_BY_DATE = "SELECT * FROM lectures WHERE local_date = ?";
	private static final String FIND_LECTURE_BY_ID = "SELECT * FROM lectures WHERE id = ?";
	private static final String GET_LECTURE_ID = "SELECT * FROM lectures WHERE local_date = ? and group_id = ?";
	private static final String GET_LECTURE_TEACHER_ID = "SELECT * FROM lectures WHERE local_date = ? and teacher_id = ?";

	private LectureRowMapper lectureRowMapper;
	private JdbcTemplate jdbcTemplate;

	public LectureDao(JdbcTemplate jdbcTemplate, LectureRowMapper lectureRowMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.lectureRowMapper = lectureRowMapper;
	}

	public void create(Lecture lecture) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection.prepareStatement(CREATE_LECTURE_QUERY,
					Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, lecture.getDailyScheduleId());
			statement.setObject(2, lecture.getDate());
			statement.setObject(3, lecture.getTime());
			statement.setInt(4, lecture.getCourse().getId());
			statement.setInt(5, lecture.getClassRoom().getId());
			statement.setInt(6, lecture.getGroup().getId());
			statement.setInt(7, lecture.getTeacher().getId());
			return statement;
		}, keyHolder);
		lecture.setId((int) keyHolder.getKeys().get("id"));
	}

	public void update(Lecture lecture) {
		jdbcTemplate.update(UPDATE_BY_LECTURE_ID, lecture.getDailyScheduleId(), lecture.getDate(), lecture.getTime(),
				lecture.getCourse().getId(), lecture.getClassRoom().getId(), lecture.getGroup().getId(),
				lecture.getTeacher().getId(), lecture.getId());
	}

	public void delete(int lectureId) {
		jdbcTemplate.update(DELETE_LECTURE_BY_ID, lectureId);
	}

	public Lecture findById(int lectureId) {
		return this.jdbcTemplate.queryForObject(FIND_LECTURE_BY_ID, lectureRowMapper, lectureId);
	}

	public List<Lecture> findByDate(LocalDate date) {
		Object[] objects = new Object[] { date };
		return this.jdbcTemplate.query(GET_BY_DATE, objects, lectureRowMapper);
	}

	public List<Lecture> findAll() {
		return this.jdbcTemplate.query(GET_ALL, lectureRowMapper);
	}
	
	public List<Lecture> findByDateAndGroup(LocalDate date, Group group) {
		Object[] objects = new Object[] { date, group.getId() };
		return this.jdbcTemplate.query(GET_LECTURE_ID, objects, lectureRowMapper);
	}
	
	public List<Lecture> findLecturesByDateAndTeacherId(LocalDate date, int id) {
		Object[] objects = new Object[] { date, id };
		return this.jdbcTemplate.query(GET_LECTURE_TEACHER_ID, objects, lectureRowMapper);
	}
}
