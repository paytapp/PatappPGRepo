package com.paymentgateway.crypto.key;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.FieldType;

@Component
public class KeysDao {
	// All static fields
	private static Logger logger = LoggerFactory.getLogger(KeysDao.class.getName());
	private static final String driver = ConfigurationConstants.DB_DRIVER.getValue();
	private static final String url = ConfigurationConstants.DB_URL.getValue();
	private static final String user = ConfigurationConstants.DB_USER.getValue();
	private static final String password = ConfigurationConstants.DB_PASSWORD.getValue();

	// All private fields
	private Connection connection = null;
	private PreparedStatement statement = null;

	// Perform all DB related init/registration processes
	static {
		// Register JDBC driver
		try {
			Class.forName(driver);
		} catch (ClassNotFoundException classNotFoundException) {
			logger.error("JDBC Driver class not found, class = " + driver, classNotFoundException);
		}
	}

	public KeysDao() {
	}

	public void saveKey() {
	}

	public boolean insertKey(int keyId, String key) {
		try {
			PreparedStatement preparedStatement = getStatementForInsert();
			preparedStatement.setInt(1, keyId);
			preparedStatement.setString(2, key);

			if (1 != preparedStatement.executeUpdate()) {
				return false;
			}

		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}

		return true;
	}

	public String fetchKey(int keyId) {
		try {
			PreparedStatement preparedStatement = getStatementForSelect();
			preparedStatement.setInt(1, keyId);

			ResultSet resultSet = executeQuery(preparedStatement);

			if (null != resultSet && resultSet.next()) {
				return resultSet.getString("KEY_NAME");
			}
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}

		return null;
	}

	public String getQuery() {

		StringBuilder sqlQueryBuilder = new StringBuilder();
		sqlQueryBuilder.append("SELECT KEY_NAME FROM KEY_STORE WHERE ");
		sqlQueryBuilder.append(FieldType.KEY_ID.getName());
		sqlQueryBuilder.append(" = ?");

		return sqlQueryBuilder.toString();
	}

	public Connection getConnection() {
		if (null == connection) {
			try {
				connection = DriverManager.getConnection(url, user, password);
			} catch (SQLException sqlException) {
				sqlException.printStackTrace();
			}
		}

		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public PreparedStatement getStatementForSelect() {

		try {
			statement = getConnection().prepareStatement(getQuery());
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}

		return statement;
	}

	public String getInsertQuery() {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append("INSERT INTO KEY_STORE ( ");
		stringBuilder.append(FieldType.KEY_ID.getName());
		stringBuilder.append(" , KEY_NAME ) VALUES (?,?)");

		return stringBuilder.toString();
	}

	public PreparedStatement getStatementForInsert() {

		try {
			statement = getConnection().prepareStatement(getInsertQuery());
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}

		return statement;
	}

	public ResultSet executeQuery(PreparedStatement preparedStatement) {
		ResultSet resultSet = null;
		try {
			resultSet = preparedStatement.executeQuery();
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}

		return resultSet;
	}

	public ResultSet executeQuery() {
		ResultSet resultSet = null;
		try {
			resultSet = getStatementForSelect().executeQuery();
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}

		return resultSet;
	}

	public void setStatement(PreparedStatement statement) {
		this.statement = statement;
	}

	public static String getDriver() {
		return driver;
	}

	public static String getUrl() {
		return url;
	}

	public static String getUser() {
		return user;
	}

	public static String getPassword() {
		return password;
	}

}// end FirstExample