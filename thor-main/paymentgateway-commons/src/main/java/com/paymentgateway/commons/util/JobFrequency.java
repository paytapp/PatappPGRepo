/**
 * To control frequency of job repetition
 */
package com.paymentgateway.commons.util;

/**
 * @author Amitosh Aanand
 *
 */
public enum JobFrequency {
	
	ONCE			("ONCE", "Once"), 
	HALF_HOURLY		("HALF_HOURLY", "30 Mins"),
	HOURLY			("HOURLY", "Hourly"),
	DAILY			("DAILY", "Daily"), 
	WEEKLY			("WEEKLY", "Weekly"),
	MONTHLY			("MONTHLY", "Monthly");

	private final String code;
	private final String name;

	private JobFrequency(String code, String name) {
		this.name = name;
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static JobFrequency getInstancefromName(String name){
		JobFrequency[] jobFrequencies = JobFrequency.values();
		for(JobFrequency jobFrequency : jobFrequencies){
			if(jobFrequency.getName().equals(name)){
				return jobFrequency;
			}
		}		
		return null;
	}
}