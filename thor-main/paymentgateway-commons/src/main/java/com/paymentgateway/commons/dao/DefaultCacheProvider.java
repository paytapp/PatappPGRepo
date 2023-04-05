package com.paymentgateway.commons.dao;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class DefaultCacheProvider implements CacheProvider {

	Map<String, String> store = new ConcurrentHashMap<String, String>();

	public DefaultCacheProvider() {
	}

	public void put(String key, String value) {
		store.put(key, value);
	}

	public void putAll(Map<String, String> pairs) {
		store.putAll(pairs);
	}

	public String get(String key) {
		return store.get(key);
	}

	public Map<String, String> getAll() {
		return store;
	}

	public void clear() {
		store.clear();
	}

	public String remove(String key) {
		return store.remove(key);
	}
}
