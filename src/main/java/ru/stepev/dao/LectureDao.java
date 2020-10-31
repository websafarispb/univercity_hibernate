package ru.stepev.dao;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import ru.stepev.model.Lecture;

public interface LectureDao {

	public void create(Lecture lecture);

	public void update(Lecture lecture);

	public void delete(int lectureId);

	public Optional<Lecture> findById(int lectureId);

	public List<Lecture> findByDailyScheduleId(int dailyScheduleId);

	public List<Lecture> findAll();
	
	public Optional<Lecture> findByDailyScheduleIdAndTimeAndGroupId(int dailyScheduleId, LocalTime startTime, LocalTime finishTime, int groupId);

	public Optional<Lecture> findByDailyScheduleIdAndTimeAndClassroomId(int dailyScheduleId, LocalTime startTime, LocalTime finishTime, int classroomId);

	public Optional<Lecture> findByDailyScheduleIdAndTimeAndTeacherId(int dailyScheduleId, LocalTime startTime, LocalTime finishTime, int teacherId);
}
