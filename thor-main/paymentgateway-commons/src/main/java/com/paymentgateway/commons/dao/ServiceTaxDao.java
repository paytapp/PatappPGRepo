/*
 * package com.paymentgateway.commons.dao;
 * 
 * import java.math.BigDecimal; import java.util.ArrayList; import
 * java.util.Date; import java.util.List;
 * 
 * import org.hibernate.HibernateException; import
 * org.hibernate.ObjectNotFoundException; import org.hibernate.Session; import
 * org.hibernate.Transaction; import org.springframework.stereotype.Service;
 * 
 * import com.paymentgateway.commons.exception.DataAccessLayerException; import
 * com.paymentgateway.commons.user.ServiceTax; import
 * com.paymentgateway.commons.user.User; import
 * com.paymentgateway.commons.user.UserDao; import
 * com.paymentgateway.commons.util.BusinessType; import
 * com.paymentgateway.commons.util.TDRStatus;
 * 
 * @Service public class ServiceTaxDao extends HibernateAbstractDao {
 * 
 * public void create(ServiceTax serviceTax) throws DataAccessLayerException {
 * super.save(serviceTax); }
 * 
 * public BigDecimal getServiceTax(BusinessType businessType) { ServiceTax
 * serviceTax = null;
 * 
 * Session session = HibernateSessionProvider.getSession(); Transaction tx =
 * session.beginTransaction();
 * 
 * try { serviceTax = (ServiceTax) session
 * .createQuery("from ServiceTax ST where ST.businessType = :businessType and ST.status = 'ACTIVE' "
 * ) .setParameter("businessType",
 * businessType).setCacheable(true).uniqueResult(); tx.commit(); } catch
 * (ObjectNotFoundException objectNotFound) {
 * handleException(objectNotFound,tx); } catch (HibernateException
 * hibernateException) { handleException(hibernateException,tx); } finally {
 * autoClose(session); // return responseUser;; } return
 * serviceTax.getServiceTax();
 * 
 * }
 * 
 * public ServiceTax findServiceTax(String businessType) {
 * 
 * Session session = HibernateSessionProvider.getSession(); Transaction tx =
 * session.beginTransaction();
 * 
 * ServiceTax serviceTax = null; try { serviceTax = (ServiceTax) session
 * .createQuery("from ServiceTax ST where ST.businessType = :businessType and ST.status = 'ACTIVE' "
 * ) .setParameter("businessType",
 * businessType).setCacheable(true).uniqueResult(); tx.commit(); } catch
 * (ObjectNotFoundException objectNotFound) {
 * handleException(objectNotFound,tx); } catch (HibernateException
 * hibernateException) { handleException(hibernateException,tx); } finally {
 * autoClose(session); } return serviceTax;
 * 
 * }
 * 
 * public ServiceTax findPendingRequest(String businessType) { ServiceTax
 * serviceTax = null;
 * 
 * Session session = HibernateSessionProvider.getSession(); Transaction tx =
 * session.beginTransaction();
 * 
 * try { serviceTax = (ServiceTax) session
 * .createQuery("from ServiceTax ST where ST.businessType = :businessType and ST.status = 'PENDING' "
 * ) .setParameter("businessType",
 * businessType).setCacheable(true).uniqueResult(); tx.commit(); } catch
 * (ObjectNotFoundException objectNotFound) {
 * handleException(objectNotFound,tx); } catch (HibernateException
 * hibernateException) { handleException(hibernateException,tx); } finally {
 * autoClose(session); // return responseUser; } return serviceTax;
 * 
 * }
 * 
 * public BigDecimal findServiceTaxByPayId(String payId) { ServiceTax serviceTax
 * = null;
 * 
 * Session session = HibernateSessionProvider.getSession(); Transaction tx =
 * session.beginTransaction();
 * 
 * User user = new User(); UserDao userDao = new UserDao(); //user =
 * userDao.findPayId(payId); user = userDao.getUserClass(payId); String
 * businessType = user.getIndustryCategory(); try { serviceTax = (ServiceTax)
 * session
 * .createQuery("from ServiceTax ST where ST.businessType = :businessType and ST.status = 'ACTIVE' "
 * ) .setParameter("businessType",
 * businessType).setCacheable(true).uniqueResult(); tx.commit(); } catch
 * (ObjectNotFoundException objectNotFound) {
 * handleException(objectNotFound,tx); } catch (HibernateException
 * hibernateException) { handleException(hibernateException,tx); } finally {
 * autoClose(session); } return serviceTax.getServiceTax();
 * 
 * }
 * 
 * public BigDecimal findServiceTaxByPayId(String payId, String date) {
 * ServiceTax serviceTax = null; Session session =
 * HibernateSessionProvider.getSession(); Transaction tx =
 * session.beginTransaction();
 * 
 * User user = new User(); UserDao userDao = new UserDao(); //user =
 * userDao.findPayId(payId); user = userDao.getUserClass(payId); String
 * businessType = user.getIndustryCategory(); try { serviceTax = (ServiceTax)
 * session.createQuery(
 * "select * from servicetax ST  where (case when ST.updatedDate=ST.createdDate then :date between ST.createdDate and current_timestamp() else  ST.createdDate <= :date and st.updatedDate > :date end) and ST.businessType = :businessType "
 * ) .setParameter("businessType", businessType).setParameter("date",
 * date).setCacheable(true) .uniqueResult(); tx.commit(); } catch
 * (ObjectNotFoundException objectNotFound) {
 * handleException(objectNotFound,tx); } catch (HibernateException
 * hibernateException) { handleException(hibernateException,tx); } finally {
 * autoClose(session); } return serviceTax.getServiceTax();
 * 
 * }
 * 
 * @SuppressWarnings("unchecked") public void addOrUpdateServiceTax(String
 * businessType, BigDecimal tax) {
 * 
 * Session session = HibernateSessionProvider.getSession(); Transaction tx =
 * session.beginTransaction();
 * 
 * ServiceTax serviceTaxFromDb = new ServiceTax(); serviceTaxFromDb =
 * findServiceTax(businessType); Date currentDate = new Date();
 * 
 * if (serviceTaxFromDb != null) {
 * 
 * try { session = HibernateSessionProvider.getSession(); Long id =
 * serviceTaxFromDb.getId(); session.load(serviceTaxFromDb,
 * serviceTaxFromDb.getId()); ServiceTax serviceTaxDetails = (ServiceTax)
 * session.get(ServiceTax.class, id);
 * serviceTaxDetails.setStatus(TDRStatus.INACTIVE);
 * serviceTaxDetails.setUpdatedDate(currentDate);
 * session.update(serviceTaxDetails); tx.commit(); session.close();
 * 
 * } catch (HibernateException e) { e.printStackTrace(); } finally {
 * autoClose(session); } }
 * 
 * ServiceTax newServiceTax = new ServiceTax();
 * newServiceTax.setServiceTax(tax);
 * newServiceTax.setBusinessType(businessType);
 * newServiceTax.setStatus(TDRStatus.ACTIVE);
 * newServiceTax.setCreatedDate(currentDate);
 * newServiceTax.setUpdatedDate(currentDate);
 * 
 * create(newServiceTax);
 * 
 * }
 * 
 * @SuppressWarnings("unchecked") public List<ServiceTax> getServiceTaxList() {
 * Session session = HibernateSessionProvider.getSession(); Transaction tx =
 * session.beginTransaction(); List<ServiceTax> serviceTaxList = new
 * ArrayList<ServiceTax>(); serviceTaxList = null; try {
 * 
 * serviceTaxList =
 * session.createQuery("from ServiceTax ST where ST.status='ACTIVE'").
 * setCacheable(true) .getResultList(); tx.commit(); return serviceTaxList; }
 * catch (ObjectNotFoundException objectNotFound) {
 * handleException(objectNotFound,tx); } catch (HibernateException
 * hibernateException) { handleException(hibernateException,tx); } finally {
 * autoClose(session); } return serviceTaxList; }
 * 
 * @SuppressWarnings("unchecked") public List<ServiceTax>
 * getPendingServiceTaxList() { List<ServiceTax> serviceTaxList = new
 * ArrayList<ServiceTax>();
 * 
 * Session session = HibernateSessionProvider.getSession(); Transaction tx =
 * session.beginTransaction();
 * 
 * serviceTaxList = null; try {
 * 
 * serviceTaxList =
 * session.createQuery("from ServiceTax ST where ST.status='PENDING'").
 * setCacheable(true) .getResultList(); tx.commit(); return serviceTaxList; }
 * catch (ObjectNotFoundException objectNotFound) {
 * handleException(objectNotFound,tx); } catch (HibernateException
 * hibernateException) { handleException(hibernateException,tx); } finally {
 * autoClose(session); } return serviceTaxList; }
 * 
 * @SuppressWarnings("unchecked") public List<ServiceTax>
 * findServiceTaxForReport(String businessType, String date) { List<ServiceTax>
 * serviceTaxList = new ArrayList<ServiceTax>();
 * 
 * Session session = HibernateSessionProvider.getSession(); Transaction tx =
 * session.beginTransaction();
 * 
 * serviceTaxList = null;
 * 
 * try { serviceTaxList = session.createNativeQuery(
 * "select * from ServiceTax ST  where (case when ST.updatedDate=ST.createdDate then :date between ST.createdDate and current_timestamp() else  ST.createdDate <= :date and ST.updatedDate > :date end) and ST.businessType =:businessType"
 * , ServiceTax.class).setParameter("businessType",
 * businessType).setParameter("date", date) .getResultList(); tx.commit(); }
 * catch (ObjectNotFoundException objectNotFound) {
 * handleException(objectNotFound,tx); } catch (HibernateException
 * hibernateException) { handleException(hibernateException,tx); } finally {
 * autoClose(session); // return responseUser; } return serviceTaxList;
 * 
 * }
 * 
 * @SuppressWarnings("unchecked") public List<ServiceTax>
 * findServiceTaxForReportWithoutDate(String businessType, String date) {
 * List<ServiceTax> serviceTaxList = new ArrayList<ServiceTax>();
 * 
 * Session session = HibernateSessionProvider.getSession(); Transaction tx =
 * session.beginTransaction();
 * 
 * serviceTaxList = null;
 * 
 * try { serviceTaxList = session.createNativeQuery(
 * "select * from ServiceTax ST  where  ST.businessType =:businessType and ST.status = 'ACTIVE'"
 * , ServiceTax.class).setParameter("businessType", businessType)
 * .getResultList(); tx.commit(); } catch (ObjectNotFoundException
 * objectNotFound) { handleException(objectNotFound,tx); } catch
 * (HibernateException hibernateException) {
 * handleException(hibernateException,tx); } finally { autoClose(session); //
 * return responseUser; } return serviceTaxList;
 * 
 * }
 * 
 * }
 */