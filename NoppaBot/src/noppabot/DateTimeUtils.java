/*
 * Created on 10.12.2014
 * @author verkel
 */
package noppabot;

import java.time.*;
import java.util.Calendar;

public class DateTimeUtils {
	private DateTimeUtils() {
	}
	
	/**
	 * Convert minutes and hours from the calendar to a Task scheduling pattern
	 */
	public static String toSchedulingPattern(Calendar time) {
		return String.format("%s %s * * *", time.get(Calendar.MINUTE), time.get(Calendar.HOUR_OF_DAY));
	}
	
	public static String toSchedulingPattern(LocalDateTime time) {
		return toSchedulingPattern(time.toLocalTime());
	}
	
	public static String toSchedulingPattern(LocalTime time) {
		return String.format("%d %d * * *", time.getMinute(), time.getHour());
	}
	
}
