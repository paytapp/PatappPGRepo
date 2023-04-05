package com.example.demo;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.JobFrequency;

public class GeneralTest {

	public static void main(String[] args) throws ParseException {
		@SuppressWarnings("unused")
		User user = new UserDao().findPayId("1008001021232722");
		System.out.println("hahah");
	}
	
	public static String toLower(String vpa) {
		StringBuilder output = new StringBuilder(vpa);
		for (int i = 0; i < output.toString().length(); i++) {
		if (Character.isUpperCase(output.charAt(i))) {
				output.setCharAt(i, Character.toLowerCase(output.charAt(i)));
			}
		}
		return output.toString();
	}

	public static String newJobTime(String oldJobTime, String frequency) {
		oldJobTime += ":00";
		DateFormat oldFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return formatter.format(DateUtils.addMinutes((Date) oldFormatter.parse(oldJobTime),
					Integer.parseInt(formatJobFrequency(frequency))));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static boolean compareTime(String jobTime) {
		DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		try {
			Date date1 = formatter.parse(jobTime + ":00");
			Date date2 = formatter.parse(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()) + ":00");
			if (date1.compareTo(date2) > 0) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	private static String formatJobFrequency(String jobFrequency) {
		switch (JobFrequency.valueOf(jobFrequency)) {
		case ONCE:
			return "0";
		case HALF_HOURLY:
			return "30";
		case DAILY:
			return "1440";
		case WEEKLY:
			return "10080";
		case MONTHLY:
			return "43200";
		}
		return "0";
	}
}
