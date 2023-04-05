/**
 * For accessing scheduler job table created in DB
 */
package com.paymentgateway.commons.dao;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.user.SchedulerJobs;
import com.paymentgateway.commons.util.JobFrequency;
import com.paymentgateway.commons.util.JobTimeFactory;
import com.paymentgateway.commons.util.JobType;

/**
 * @author Amitosh Aanand
 *
 */
@Service
@SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
public class SchedulerJobsDao extends HibernateAbstractDao {

	@Autowired
	JobTimeFactory time;

	public SchedulerJobsDao() {
		super();
	}

	public void create(SchedulerJobs schedulerJobs) throws DataAccessLayerException {
		super.save(schedulerJobs);
	}

	public void update(SchedulerJobs schedulerJobs) throws DataAccessLayerException {
		super.saveOrUpdate(schedulerJobs);
	}

	public void delete(SchedulerJobs schedulerJobs) throws DataAccessLayerException {
		super.delete(schedulerJobs);
	}

	public List<SchedulerJobs> fetchActiveJobs(String qurery) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<Object[]> res = null;
		try {
			Query query = session.createNativeQuery(qurery);
			res = query.getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		List<SchedulerJobs> list = new ArrayList<SchedulerJobs>();
		Iterator it = res.iterator();
		while (it.hasNext()) {
			Object[] line = (Object[]) it.next();
			SchedulerJobs sj = new SchedulerJobs();
			sj.setJobId(((BigInteger) line[0]).longValue());
			sj.setJobType(JobType.valueOf((String) line[1]).getName());
			sj.setJobTime((String) line[2]);
			sj.setJobFrequency(JobFrequency.valueOf((String) line[3]).getName());
			sj.setJobDetails((String) line[4]);
			list.add(sj);
		}
		while (list.remove(null))
			;
		return list;
	}

	public SchedulerJobs fetchActiveJobsByJobId(String jobId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		SchedulerJobs schedulerJob = null;
		try {
			schedulerJob = (SchedulerJobs) session
					.createQuery("from SchedulerJobs SJ where SJ.jobStatus=true and SJ.jobId='" + jobId + "'")
					.uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return schedulerJob;
	}

	public boolean checkExistingJob(String jobType, String jobTime, String jobFrequency, String jobParams) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		SchedulerJobs schedulerJob = null;
		try {
			schedulerJob = (SchedulerJobs) session.createQuery(
					"from SchedulerJobs SJ where SJ.jobStatus=true and SJ.jobType='" + jobType + "'  and SJ.jobTime='"
							+ jobTime + "' and jobFrequency='" + jobFrequency + "' and jobParams='" + jobParams + "'")
					.uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		if (schedulerJob == null) {
			return false;
		}
		return true;
	}

	public List<SchedulerJobs> fetchActiveJobsAtCurrentMoment() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<SchedulerJobs> schedulerJobsList = null;
		try {
			schedulerJobsList = session.createQuery("from SchedulerJobs SJ where SJ.jobStatus=true " + "and jobTime='"
					+ new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()) + "'").getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return schedulerJobsList;
	}

	public void rescheduleJob(Long jobId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			SchedulerJobs schedulerJobsFromDb = session.load(SchedulerJobs.class, jobId);
			schedulerJobsFromDb.setUpdatedDate(new Date());
			schedulerJobsFromDb.setUpdatedBy("Scheduler");
			if (Integer.parseInt(time.formatJobFrequency(schedulerJobsFromDb.getJobFrequency())) > 0) {
				schedulerJobsFromDb.setJobTime(time.newJobTimeByFrequency(schedulerJobsFromDb.getJobTime(),
						schedulerJobsFromDb.getJobFrequency()));
			} else {
				schedulerJobsFromDb.setJobStatus(false);
			}
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}

	public void createJob(String jobType, String jobTime, String jobFrequency, String jobDetails, String jobParams,
			String emailId) {
		SchedulerJobs schedulerJobs = new SchedulerJobs();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		schedulerJobs.setJobType(JobType.valueOf(jobType).getCode());
		schedulerJobs.setJobTime(jobTime);
		schedulerJobs.setJobFrequency(JobFrequency.valueOf(jobFrequency).getCode());
		schedulerJobs.setJobDetails(jobDetails);
		schedulerJobs.setJobStatus(true);
		schedulerJobs.setJobParams(jobParams);
		schedulerJobs.setCreatedBy(emailId);
		schedulerJobs.setCreatedDate(new Date());
		try {
			session.save(schedulerJobs);
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}

	public void upadteJob(String jobId, String jobTime, String jobFrequency, String jobDetails, String jobParams,
			String emailId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			SchedulerJobs schedulerJobsFromDb = session.load(SchedulerJobs.class, Long.parseLong(jobId));
			if (StringUtils.isNotBlank(jobTime)) {
				schedulerJobsFromDb.setJobTime(jobTime);
			}
			if (StringUtils.isNotBlank(jobFrequency)) {
				schedulerJobsFromDb.setJobFrequency(JobFrequency.valueOf(jobFrequency).getCode());
			}
			if (StringUtils.isNotBlank(jobDetails)) {
				schedulerJobsFromDb.setJobDetails(jobDetails);
			}
			if (StringUtils.isNotBlank(jobParams)) {
				schedulerJobsFromDb.setJobParams(jobParams);
			}
			schedulerJobsFromDb.setUpdatedBy(emailId);
			schedulerJobsFromDb.setUpdatedDate(new Date());
			schedulerJobsFromDb.setJobStatus(true);
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}

	public void deleteJob(String jobId, String emailId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			SchedulerJobs schedulerJobsFromDb = session.load(SchedulerJobs.class, Long.parseLong(jobId));
			schedulerJobsFromDb.setUpdatedDate(new Date());
			schedulerJobsFromDb.setUpdatedBy(emailId);
			schedulerJobsFromDb.setJobStatus(false);
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}
}
