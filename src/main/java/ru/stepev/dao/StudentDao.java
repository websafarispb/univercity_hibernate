package ru.stepev.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ru.stepev.dao.rowmapper.StudentRowMapper;
import ru.stepev.exceptions.ObjectNotFoundException;
import ru.stepev.model.Student;

@Component
public class StudentDao {
	private static final String GET_ALL = "SELECT * FROM students";
	private static final String CREATE_STUDENT_QUERY = "INSERT INTO students (personal_number, first_name, last_name, birthday, email, gender, address) VALUES ( ?, ?, ?, ?, ?, ?, ?)";
	private static final String ASSIGN_TO_COURSE = "INSERT INTO students_courses (student_id, course_id) VALUES (?, ?)";
	private static final String RESIGN_FROM_COURSE = "DELETE FROM students_courses WHERE student_id = ? AND course_id = ?";
	private static final String GET_STUDENT_ID = "SELECT * FROM students WHERE first_name = ? and last_name = ?";
	private static final String FIND_STUDENT_BY_ID = "SELECT * FROM students WHERE id = ?";
	private static final String DELETE_BY_STUDENT_ID = "DELETE FROM students WHERE id = ?";
	private static final String UPDATE_BY_STUDENT_ID = "UPDATE students SET first_name = ?, last_name = ?, birthday = ?, email = ?, gender= ?, address = ? WHERE id = ?";
	private static final String GET_STUDENT_BY_GROUP_ID = "SELECT * FROM students INNER JOIN students_groups ON  students.id = students_groups.student_id WHERE students_groups.group_id = ?";

	private StudentRowMapper studentRowMapper;
	private JdbcTemplate jdbcTemplate;

	public static <T> Predicate<T> not(Predicate<T> t) {
		return t.negate();
	}

	public StudentDao(JdbcTemplate jdbcTemplate, StudentRowMapper studentRowMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.studentRowMapper = studentRowMapper;
	}

	@Transactional
	public void create(Student student) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection.prepareStatement(CREATE_STUDENT_QUERY,
					Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, student.getPersonalNumber());
			statement.setString(2, student.getFirstName());
			statement.setString(3, student.getLastName());
			statement.setObject(4, student.getBirthday());
			statement.setString(5, student.getEmail());
			statement.setString(6, student.getGender().toString());
			statement.setString(7, student.getAddress());
			return statement;
		}, keyHolder);
		student.setId((int) keyHolder.getKeys().get("id"));
		student.getCourses().forEach(c -> jdbcTemplate.update(ASSIGN_TO_COURSE, student.getId(), c.getId()));
	}

	@Transactional
	public void update(Student student) {

		if (jdbcTemplate.update(UPDATE_BY_STUDENT_ID, student.getFirstName(), student.getLastName(),
				student.getBirthday(), student.getEmail(), student.getGender().toString(), student.getAddress(),
				student.getId()) == 0) {
			throw new ObjectNotFoundException(String.format("Student with Id = %d not found !!!", student.getId()));
		} else {

			Student updatedSudent = findById(student.getId());
			updatedSudent.getCourses().stream().filter(not(student.getCourses()::contains))
					.forEach(c -> jdbcTemplate.update(RESIGN_FROM_COURSE, student.getId(), c.getId()));
			student.getCourses().stream().filter(not(updatedSudent.getCourses()::contains))
					.forEach(c -> jdbcTemplate.update(ASSIGN_TO_COURSE, student.getId(), c.getId()));
		}
	}

	public void delete(int studentId) {
		if (jdbcTemplate.update(DELETE_BY_STUDENT_ID, studentId) == 0) {
			throw new ObjectNotFoundException(String.format("Student with Id = %d not found !!!", studentId));
		}
	}

	public Student findById(int studentId) {
		try {
			return jdbcTemplate.queryForObject(FIND_STUDENT_BY_ID, studentRowMapper, studentId);
		} catch (EmptyResultDataAccessException e) {
			throw new ObjectNotFoundException(String.format("Student with Id = %d not found !!!", studentId), e);
		}
	}

	public List<Student> findAll() {
		return jdbcTemplate.query(GET_ALL, studentRowMapper);
	}

	public List<Student> findByFirstAndLastNames(String firstName, String lastName) {
		Object[] objects = new Object[] { firstName, lastName };
		return jdbcTemplate.query(GET_STUDENT_ID, objects, studentRowMapper);
	}

	public List<Student> findByGroupId(int groupId) {
		return jdbcTemplate.query(GET_STUDENT_BY_GROUP_ID, studentRowMapper, groupId);
	}
}
