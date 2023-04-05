package com.paymentgateway.commons.dao;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.ConfigurationConstants;

@Component
public class DataAccessObject {

	public DataAccessObject() {
	}

	private static Logger logger = LoggerFactory.getLogger(DataAccessObject.class.getName());
	private static ComboPooledDataSource cpds = new ComboPooledDataSource();
	static {
		try {
			Class.forName(ConfigurationConstants.DB_DRIVER.getValue()
					.toString());

			cpds.setDriverClass(ConfigurationConstants.DB_DRIVER.getValue()
					.toString());
			// loads the jdbc driver
			cpds.setJdbcUrl(ConfigurationConstants.DB_URL.getValue().toString());
			cpds.setUser(ConfigurationConstants.DB_USER.getValue().toString());
			cpds.setPassword(ConfigurationConstants.DB_PASSWORD.getValue()
					.toString());

		} catch (ClassNotFoundException classNotFoundException) {
			logger.error("Can not find JDBC class", classNotFoundException);
		} catch (PropertyVetoException propertyVetoException) {
			logger.error("Error in C3P0 property file", propertyVetoException);
		}
	}

	public static Connection getBasicConnection() throws SQLException {
		return cpds.getConnection();
	}

	public static Connection getConnection() throws SystemException {
		Connection connection = null;
		try {
			logger.info("inside DAO class before invoking connection call");
			connection = getBasicConnection();
			logger.info("connection created");
		} catch (SQLException sqlException) {
			logger.error("Database exception", sqlException);
			throw new SystemException(ErrorType.DATABASE_ERROR, sqlException,
					"Error creating database connection!");
		}
		logger.info("return connection to service");
		return connection;
	}
}// end
