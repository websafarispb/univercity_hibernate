package ru.stepev.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ru.stepev.dao.DailyScheduleDao;
import ru.stepev.dao.GroupDao;
import ru.stepev.exception.EntityAlreadyExistException;
import ru.stepev.exception.EntityNotFoundException;
import ru.stepev.model.DailySchedule;
import ru.stepev.model.Group;

@Component
@Slf4j
public class DailyScheduleService {

	private DailyScheduleDao dailyScheduleDao;
	private GroupDao groupDao;

	public DailyScheduleService(DailyScheduleDao dailyScheduale, GroupDao groupDao) {
		this.dailyScheduleDao = dailyScheduale;
		this.groupDao = groupDao;
	}

	public void add(DailySchedule dailySchedule) {
		verifyDailyScheduleNotExist(dailySchedule);
		dailyScheduleDao.create(dailySchedule);
		log.debug("DailySchedule with date {} was created", dailySchedule.getDate());

	}

	public void update(DailySchedule dailySchedule) {
		verifyDailyScheduleExist(dailySchedule);
		dailyScheduleDao.update(dailySchedule);
		log.debug("DailySchedule with date {} was updated", dailySchedule.getDate());
	}

	public void delete(DailySchedule dailySchedule) {
		verifyDailyScheduleExist(dailySchedule);
		dailyScheduleDao.delete(dailySchedule.getId());
		log.debug("Delete DailySchedule with date {}  was deleted", dailySchedule.getDate());
	}

	public Optional<DailySchedule> getById(int scheduleId) {
		return dailyScheduleDao.findById(scheduleId);
	}

	public Optional<DailySchedule> getByDate(LocalDate date) {
		return dailyScheduleDao.findByDate(date);
	}

	public List<DailySchedule> getAll() {
		return dailyScheduleDao.findAll();
	}

	public List<DailySchedule> getAllByDatePeriod(LocalDate firstDate, LocalDate lastDate) {
		return dailyScheduleDao.findAllByDatePeriod(firstDate, lastDate);
	}

	public List<DailySchedule> getScheduleForTeacher(int teacherId, LocalDate firstDate, LocalDate lastDate) {
		return dailyScheduleDao.findByTeacherIdAndPeriodOfTime(teacherId, firstDate, lastDate);
	}

	public List<DailySchedule> getScheduleForStudent(int studentId, LocalDate firstDate, LocalDate lastDate) {
		Group group = groupDao.findByStudentId(studentId).get();
		return dailyScheduleDao.findByGroupAndPeriodOfTime(group, firstDate, lastDate);
	}

	public void verifyDailyScheduleNotExist(DailySchedule dailySchedule) {
		if (dailyScheduleDao.findByDate(dailySchedule.getDate()).isPresent()) {
			throw new EntityAlreadyExistException(
					String.format("DailySchedule with date %s already exist", dailySchedule.getDate()));
		}
	}

	public void verifyDailyScheduleExist(DailySchedule dailySchedule) {
		if (dailyScheduleDao.findByDate(dailySchedule.getDate()).isEmpty()) {
			throw new EntityNotFoundException(String.format("DailySchedule with date %s doesn't exist", dailySchedule.getDate()));
		}
	}

	public int getNumberOfItems() {
		return dailyScheduleDao.findNumberOfItems();
	}

	public List<DailySchedule> getAndSortByDate(int numberOfItems, int offset) {
		return dailyScheduleDao.findAndSortByDate( numberOfItems,  offset);
	}

	public List<DailySchedule> getAndSortById(int numberOfItems, int offset) {
		return dailyScheduleDao.getAndSortById(numberOfItems, offset);
	}
}
