package com.paymentgateway.commons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileModifiedWatcher {
	private static int pollingInterval;
	private static Timer fileWatcher;
	private static long lastReadTimeStamp = 0L;
	private static Logger logger = LoggerFactory.getLogger(FileModifiedWatcher.class.getName());

	public static boolean init(String _file, int _pollingInterval) {
		pollingInterval = _pollingInterval; // In seconds
		watchFile();

		return true;
	}

	private static void watchFile() {
		if (null == fileWatcher) {

			logger.info("Started file watch service for RECON");
			fileWatcher = new Timer();

			fileWatcher.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {

					logger.info("Checking DestDir for changes...");
					if (true) {

						String dirPath = System.getenv("PG_PROPS") + "/RECON/DestDir";
						try (Stream<Path> filePathStream = Files.walk(Paths.get(dirPath))) {
							filePathStream.forEach(filePath -> {
								if (Files.isRegularFile(filePath)) {
									System.out.println(filePath);
								}
							});
						} catch (IOException e) {
							e.printStackTrace();
						}

					}

					lastReadTimeStamp = System.currentTimeMillis();
				}
			}, 0, 1000 * pollingInterval);
		}

	}

}