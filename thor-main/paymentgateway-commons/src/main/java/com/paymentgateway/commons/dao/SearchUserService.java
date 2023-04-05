package com.paymentgateway.commons.dao;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.SearchUser;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Acquirer;
import com.paymentgateway.commons.util.Agent;
import com.paymentgateway.commons.util.SubAdmin;
import com.paymentgateway.commons.util.UserStatusType;

@Service
public class SearchUserService {
	
	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(SearchUserService.class.getName());
	public List<SearchUser> transactionList = new ArrayList<SearchUser>();

	public SearchUserService() {
	}

	public List<Merchants> getSubUsers(String parentPayId, UserStatusType status) throws SQLException, ParseException, SystemException {
		List<Merchants> subUser = new ArrayList<Merchants>();
		try {
			subUser = userDao.getSubUsers(parentPayId, status);
		} catch (Exception exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getResponseMessage());
		}

		return subUser;

	}

	public List<Merchants> getAcquirerSubUsers(String parentPayId)
			throws SQLException, ParseException, SystemException {
		List<Merchants> subUser = new ArrayList<Merchants>();
		try {
			subUser = userDao.getAcquirerSubUsers(parentPayId);
		} catch (Exception exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getResponseMessage());
		}

		return subUser;
	}	

	public List<SubAdmin> getAgentsList(String sessionPayId) throws SQLException, ParseException, SystemException {
		List<SubAdmin> agentList = new ArrayList<SubAdmin>();
		try {
			agentList = userDao.getUsers(sessionPayId);
		} catch (Exception exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return agentList;

	}
	
	public List<Acquirer> getAcquirersList(String sessionPayId) throws SQLException, ParseException, SystemException {
		List<Acquirer> acquirerList = new ArrayList<Acquirer>();
		try {
			acquirerList = userDao.getAcquirers();
		} catch (Exception exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return acquirerList;

	}
	
	
	public List<Agent> getnewAgentsList() throws SQLException, ParseException, SystemException {
		List<Agent> newagentList = new ArrayList<Agent>();
		try {
			newagentList = userDao.getAgent();
		} catch (Exception exception) {
			logger.error("Database error", exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, ErrorType.DATABASE_ERROR.getResponseMessage());
		}
		return newagentList;

	}
}
