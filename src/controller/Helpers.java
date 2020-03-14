package controller;

import java.time.LocalDate;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class Helpers {
	public static LocalDate getFirstDayOfWeek(LocalDate dateInWeek) {
		return dateInWeek.minusDays(dateInWeek.getDayOfWeek().getValue() - 1);
	}
	public static LocalDate getLastDayOfWeek(LocalDate dateInWeek) {
		return dateInWeek.plusDays(7 - dateInWeek.getDayOfWeek().getValue());
	}
	public static int getWeekNbr(LocalDate dateInWeek) {
		TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear(); 
		return dateInWeek.get(woy);
	}
}
