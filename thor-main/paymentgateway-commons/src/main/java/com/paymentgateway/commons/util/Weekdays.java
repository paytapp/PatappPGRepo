package com.paymentgateway.commons.util;

public enum Weekdays {
	
	MONDAY		("MON", "1"),
	TUESDAY		("TUE", "2"),
	WEDNESDAY	("WED", "3"),
	THRUSDAY	("THU", "4"),
	FRIDAY		("FRI", "5"),
	SATURDAY	("SAT", "6"),
	SUNDAY		("SUN", "7");
	
	
	private final String name;
	private final String code;

	private Weekdays(String name, String code){
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public String getCode(){
		return code;
	}
	
	public static Weekdays getInstance(String name){
		Weekdays[] weekdays = Weekdays.values();
		for(Weekdays weekday : weekdays){
			if(weekday.getName().equals(name)){
				return weekday;
			}
		}		
		return null;
	}
	
	public static String getdayName(String dayCode){
		Weekdays weekday = Weekdays.getday(dayCode);		
		if(weekday == null) {
			return "";
		}
		return weekday.getName();
	}
	
	public static String getdayCode(String dayName){
		Weekdays weekday = Weekdays.getday(dayName);		
		if(weekday == null) {
			return "";
		}
		return weekday.getCode();
	}

	public static Weekdays getday(String dayCode){
		Weekdays daysObj = null;
		if(null!=dayCode){
			for(Weekdays day:Weekdays.values()){
				if(dayCode.equals(day.getCode().toString())){
					daysObj=day;
					break;
				}
			}
		}
		return daysObj;
	}	
	public static Weekdays getInstanceIgnoreCase(String name){
		Weekdays[] weekDays = Weekdays.values();
		for(Weekdays day : weekDays){
			if(day.getName().equalsIgnoreCase(name)){
				return day;
			}
		}		
		return null;
	}
	
	public static Weekdays getDayInstance(String name){
		Weekdays[] weekDays = Weekdays.values();
		for(Weekdays day : weekDays){
			if(day.toString().equals(name)){
				return day;
			}
		}		
		return null;
	}
	
	

}
