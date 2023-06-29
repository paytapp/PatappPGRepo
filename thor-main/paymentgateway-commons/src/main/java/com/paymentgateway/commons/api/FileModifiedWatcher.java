package com.paymentgateway.commons.api;

import java.io.File;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.FileSystemResource;

import com.paymentgateway.commons.util.PropertiesManager;

public class FileModifiedWatcher {
	private static File file;
	private static int pollingInterval;
	private static Timer fileWatcher;
	private static long lastReadTimeStamp = 0L;
	private static final String ymlFileLocation = System.getenv("PG_PROPS");
	private final static YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
	private static Logger logger = LoggerFactory.getLogger(FileModifiedWatcher.class.getName());

	public static boolean init(String _file, int _pollingInterval) {
		file = new File(_file);
		pollingInterval = _pollingInterval; // In seconds

		watchFile();

		return true;
	}

	private static void watchFile() {
		if (null == fileWatcher) {

			logger.info("Started file watch service for application.yml");
			fileWatcher = new Timer();

			fileWatcher.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {

					// Changed by shaiwal to always update the yml file without comparing with
					// history
					// This way all servers will have updated files even if it was updated on only
					// one server on production

					if (true) {

						logger.info("File modified , using updated values for application.yml");

						YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
						yaml.setResources(new FileSystemResource(System.getenv("PG_PROPS")+"application.yml"));
						Properties configProperty = yaml.getObject();
						Set<Object> keys = configProperty.keySet();
						
						for (Object key : keys) {
							PropertiesManager.propertiesMap.put((String)key, configProperty.getProperty((String) key));
						}


					}

					lastReadTimeStamp = System.currentTimeMillis();
				}
			}, 0, 1000 * pollingInterval);
		}

	}

}