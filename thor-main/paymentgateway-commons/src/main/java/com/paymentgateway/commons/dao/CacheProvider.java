package com.paymentgateway.commons.dao;

import java.util.Map;

public interface CacheProvider {
	public void put(String key, String value);

	public void putAll(Map<String, String> pairs);

	public String get(String key);

	public Map<String, String> getAll();

	public void clear();

	public String remove(String key);
}
