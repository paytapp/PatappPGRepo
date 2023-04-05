/*
 * package com.paymentgateway.commons.user;
 * 
 * import java.io.Serializable; import java.math.BigDecimal; import
 * java.util.Date;
 * 
 * import javax.persistence.Entity; import javax.persistence.EnumType; import
 * javax.persistence.Enumerated; import javax.persistence.GeneratedValue; import
 * javax.persistence.GenerationType; import javax.persistence.Id; import
 * javax.persistence.Table;
 * 
 * import org.hibernate.annotations.Proxy;
 * 
 * import com.paymentgateway.commons.util.TDRStatus;
 * 
 * @Entity
 * 
 * @Proxy(lazy= false)
 * 
 * @Table public class ServiceTax implements Serializable{
 * 
 * //service tax private static final long serialVersionUID =
 * 6451344616066902378L;
 * 
 * @Id
 * 
 * @GeneratedValue(strategy=GenerationType.AUTO) private Long id;
 * 
 * @Enumerated(EnumType.STRING) private TDRStatus status;
 * 
 * private String businessType; private BigDecimal serviceTax; private Date
 * createdDate; private Date updatedDate; private String requestedBy; private
 * String processedBy;
 * 
 * public Long getId() { return id; } public void setId(Long id) { this.id = id;
 * }
 * 
 * public BigDecimal getServiceTax() { return serviceTax; } public void
 * setServiceTax(BigDecimal serviceTax) { this.serviceTax = serviceTax; } public
 * Date getCreatedDate() { return createdDate; } public void setCreatedDate(Date
 * createdDate) { this.createdDate = createdDate; } public Date getUpdatedDate()
 * { return updatedDate; } public void setUpdatedDate(Date updatedDate) {
 * this.updatedDate = updatedDate; } public TDRStatus getStatus() { return
 * status; } public void setStatus(TDRStatus status) { this.status = status; }
 * public String getBusinessType() { return businessType; } public void
 * setBusinessType(String businessType) { this.businessType = businessType; }
 * public String getRequestedBy() { return requestedBy; } public void
 * setRequestedBy(String requestedBy) { this.requestedBy = requestedBy; } public
 * String getProcessedBy() { return processedBy; } public void
 * setProcessedBy(String processedBy) { this.processedBy = processedBy; }
 * 
 * }
 */