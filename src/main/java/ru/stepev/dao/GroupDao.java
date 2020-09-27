package ru.stepev.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import ru.stepev.dao.rowmapper.GroupRowMapper;
import ru.stepev.model.Group;
import ru.stepev.model.Student;

@Component
public class GroupDao {

	private static final String GET_ALL_GROUPS = "SELECT * FROM GROUPS ";
	private static final String CREATE_GROUP_QUERY = "INSERT INTO groups ( group_name) VALUES ( ?)";
	private static final String ASSIGN_STUDENT = "INSERT INTO students_groups (student_id, group_id) VALUES (?, ?)";
	private static final String RESIGN_FROM_GROUP = "DELETE FROM students_groups WHERE student_id = ? AND group_id = ?";
	private static final String GET_BY_STUDENT_ID = "SELECT groups.id, groups.group_name "
			+ " FROM groups INNER JOIN students_groups  " + " ON students_groups.group_id = groups.id "
			+ " WHERE students_groups.student_id = ?";
	private static final String UPDATE_BY_GROUP_ID = "UPDATE groups SET group_name = ? WHERE id = ?";
	private static final String DELETE_GROUP_BY_ID = "DELETE FROM groups WHERE id = ?";
	private static final String FIND_GROUP_BY_ID = "SELECT * FROM groups WHERE id = ?";

	private GroupRowMapper groupRowMapper;
	private JdbcTemplate jdbcTemplate;

	@Autowired
	public GroupDao(JdbcTemplate jdbcTemplate, GroupRowMapper groupRowMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.groupRowMapper = groupRowMapper;
	}

	public void create(Group group) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection.prepareStatement(CREATE_GROUP_QUERY,
					Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, group.getName());
			return statement;
		}, keyHolder);
		group.setId((int) (keyHolder.getKeys().get("id")));
		group.getStudents().stream().forEach(s -> jdbcTemplate.update(ASSIGN_STUDENT, s.getId(), group.getId()));
	}

	public void update(Group group) {
		Group updatedGroup = findById(group.getId());
		jdbcTemplate.update(UPDATE_BY_GROUP_ID, group.getName(), group.getId());
		updatedGroup.getStudents().stream().filter(s -> !group.getStudents().contains(s))
				.forEach(s -> jdbcTemplate.update(RESIGN_FROM_GROUP, s.getId(), group.getId()));
		
		group.getStudents().stream().filter(s -> !updatedGroup.getStudents().contains(s))
				.forEach(s -> jdbcTemplate.update(ASSIGN_STUDENT, s.getId(), group.getId()));
	}

	public void delete(int groupId) {
		jdbcTemplate.update(DELETE_GROUP_BY_ID, groupId);
	}

	public Group findById(int groupId) {
		return this.jdbcTemplate.queryForObject(FIND_GROUP_BY_ID, groupRowMapper, groupId);
	}

	public List<Group> findAll() {
		return this.jdbcTemplate.query(GET_ALL_GROUPS, groupRowMapper);
	}

	public List<Group> findByStudentId(int student_id) {
		Object[] objects = new Object[] { student_id };
		return this.jdbcTemplate.query(GET_BY_STUDENT_ID, objects, groupRowMapper);
	}
	
	public void assignToStudents(Group group, List<Student> students) {
		for (Student student : students) {
			jdbcTemplate.update(ASSIGN_STUDENT, student.getId(), group.getId());
		}
	}

}
