package ru.stepev.model;

import java.time.LocalDate;
import java.util.List;

public class Teacher extends Person{

	public Teacher(String firstName, String lastName, LocalDate birthday, String email, Gender gender, String address,
			List<Course> courses) {
		
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthday = birthday;
		this.email = email;
		this.gender = gender;
		this.address = address;
		this.courses = courses;
	}

	public Teacher(int id, String firstName, String lastName, LocalDate birthday, String email, Gender gender, String address,
			List<Course> courses) {
		
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthday = birthday;
		this.email = email;
		this.gender = gender;
		this.address = address;
		this.courses = courses;
	}

	public Teacher(int id, String firstName, String lastName, String birthday, String email, String gender,
			String address) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.birthday = LocalDate.parse(birthday);
		this.email = email;
		this.gender = Gender.valueOf(gender);
		this.address = address;
		
	}
	
	@Override
	public String toString() {
		return "Teacher [firstName=" + firstName + ", lastName=" + lastName + "]";
	}
	
	
}
