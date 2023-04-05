package com.paymentgateway.commons.dao;

public class CacheProviderFactory {

	public CacheProviderFactory() {
	}

	public static CacheProvider getCacheProvider() {
		return new DefaultCacheProvider();
	}

}
