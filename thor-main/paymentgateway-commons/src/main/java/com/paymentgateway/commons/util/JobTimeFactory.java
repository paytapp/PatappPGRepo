/**
 * It can manage job timings for scheduler
 */
package com.paymentgateway.commons.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class JobTimeFactory {

	public String newJobTimeByFrequency(String oldJobTime, String frequency) {
		oldJobTime += ":00";
		DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		try {
			return formatter.format(DateUtils.addMinutes((Date) formatter.parse(oldJobTime),
					Integer.parseInt(formatJobFrequency(frequency)))).substring(0, 16);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String newJobTimeByMinutes(String oldJobTime, String minutes) {
		oldJobTime += ":00";
		DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		try {
			return formatter.format(DateUtils.addMinutes((Date) formatter.parse(oldJobTime),
					Integer.parseInt(minutes))).substring(0, 16);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String formatJobFrequency(String jobFrequency) {
		switch (JobFrequency.valueOf(jobFrequency)) {
		case ONCE:
			return "0";
		case HALF_HOURLY:
			return "30";
		case HOURLY:
			return "60";
		case DAILY:
			return "1440";
		case WEEKLY:
			return "10080";
		case MONTHLY:
			return "43200";
		default:
			return jobFrequency;
		}
	}

	private boolean isAfterTime(String jobTime) {
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

	public String manageJobTime(String jobTime) {
		if (StringUtils.isNotBlank(jobTime)) {
			if (isAfterTime(jobTime)) {
				return jobTime;
			} else {
				return newJobTimeByMinutes((new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date())), "2");
			}
		} else {
			return newJobTimeByMinutes((new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date())), "2");
		}
	}
}
