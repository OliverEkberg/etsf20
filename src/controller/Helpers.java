package controller;

import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import database.User;

/**
 * Provides a set of helper methods that are to be used throughout the whole system.
 *
 * @author Oliver Ekberg
 */
public class Helpers {
	/**
	 * Computes the first (Monday) day of the week that contains dateInWeek.
	 * @param dateInWeek The date for which to get the start of the week.
	 * @return the start of the week.
	 */
	public static LocalDate getFirstDayOfWeek(LocalDate dateInWeek) {
		return dateInWeek.minusDays(dateInWeek.getDayOfWeek().getValue() - 1);
	}
	
	/**
	 * Computes the last (Sunday) day of the week that contains dateInWeek.
	 * @param dateInWeek The date for which to get the end of the week.
	 * @return the end of the week.
	 */
	public static LocalDate getLastDayOfWeek(LocalDate dateInWeek) {
		return dateInWeek.plusDays(7 - dateInWeek.getDayOfWeek().getValue());
	}
	
	/**
	 * Computes the week number given a date.
	 * @param dateInWeek The date for which to compute the week number.
	 * @return The week number.
	 */
	public static int getWeekNbr(LocalDate dateInWeek) {
		TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear(); 
		return dateInWeek.get(woy);
	}
	
	/**
	 * Sorts a list of Users alphabeticaly.
	 *  
	 * @param userList - The list to be sorted
	 * @return a sorted list of users.
	 */
	public static List<User> sortUserList(List<User> userList) { 

		Comparator<User> comparator = (u1, u2) -> u2.getUsername().compareTo(u1.getUsername());
		userList.sort(comparator);

		return userList;
	}
}
