package model.utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logger {

	private String title;
	
	public Logger(String title) {
		this.title  = title;
	}
	
	public void log(String message) {
		System.out.println(getDatetime() + "  |  [" + this.title + "] " + message);
	}
	
	public void debug(String message) {
		System.out.println(getDatetime() + "  |  [*--DEBUG--*][" + this.title + "] " + message);
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	private String getDatetime() {
		return  "" + LocalDate.now() + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
	}
	
}
