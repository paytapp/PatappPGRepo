package com.paymentgateway.phonePe;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "status_check_response", schema = "paytapp")
public class StatusCheckResponse {
	
	
    private int id;
    private boolean success;
    private String code;
    private String message;
    private String merchantId;
    private String merchantTransactionId;
    private String transactionId;
    private BigDecimal amount;
    private String state;
    private String responseCode;
    private String type;
    private String utr;
    private String cardType;
    private String pgTransactionId;
    private String bankTransactionId;
    private String pgAuthorizationCode;
    private String arn;
    private String bankId;
    private String pgServiceTransactionId;
    private Transaction transactionReference;
    private Date createdDate;
    private String createdBy;
    private Date updatedDate;
    private String updatedBy;

    
//	public StatusCheckResponse(int id, boolean success, String code, String message, String merchantId,
//			String merchantTransactionId, String transactionId, BigDecimal amount, String state, String responseCode,
//			String type, String utr, String cardType, String pgTransactionId, String bankTransactionId,
//			String pgAuthorizationCode, String arn, String bankId, String pgServiceTransactionId,
//			Transaction transactionReference, Date createdDate, String createdBy, Date updatedDate, String updatedBy) {
//		super();
//		this.id = id;
//		this.success = success;
//		this.code = code;
//		this.message = message;
//		this.merchantId = merchantId;
//		this.merchantTransactionId = merchantTransactionId;
//		this.transactionId = transactionId;
//		this.amount = amount;
//		this.state = state;
//		this.responseCode = responseCode;
//		this.type = type;
//		this.utr = utr;
//		this.cardType = cardType;
//		this.pgTransactionId = pgTransactionId;
//		this.bankTransactionId = bankTransactionId;
//		this.pgAuthorizationCode = pgAuthorizationCode;
//		this.arn = arn;
//		this.bankId = bankId;
//		this.pgServiceTransactionId = pgServiceTransactionId;
//		this.transactionReference = transactionReference;
//		this.createdDate = createdDate;
//		this.createdBy = createdBy;
//		this.updatedDate = updatedDate;
//		this.updatedBy = updatedBy;
//	}
//	
//	public StatusCheckResponse() {
//		super();
//	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "success", nullable = false)
    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Column(name = "code", nullable = false)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(name = "message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Column(name = "merchantId", nullable = false)
    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    @Column(name = "merchantTransactionId")
    public String getMerchantTransactionId() {
        return merchantTransactionId;
    }

    public void setMerchantTransactionId(String merchantTransactionId) {
        this.merchantTransactionId = merchantTransactionId;
    }

    @Column(name = "amount")
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Column(name = "state")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Column(name = "responseCode")
    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(name = "utr")
    public String getUtr() {
        return utr;
    }

    public void setUtr(String utr) {
        this.utr = utr;
    }

    @Column(name = "cardType")
    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    @Column(name = "pgTransactionId")
    public String getPgTransactionId() {
        return pgTransactionId;
    }

    public void setPgTransactionId(String pgTransactionId) {
        this.pgTransactionId = pgTransactionId;
    }

    @Column(name = "bankTransactionId")
    public String getBankTransactionId() {
        return bankTransactionId;
    }

    public void setBankTransactionId(String bankTransactionId) {
        this.bankTransactionId = bankTransactionId;
    }

    @Column(name = "pgAuthorizationCode")
    public String getPgAuthorizationCode() {
        return pgAuthorizationCode;
    }

    public void setPgAuthorizationCode(String pgAuthorizationCode) {
        this.pgAuthorizationCode = pgAuthorizationCode;
    }

    @Column(name = "arn")
    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    @Column(name = "bankId")
    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    @Column(name = "pgServiceTransactionId")
    public String getPgServiceTransactionId() {
        return pgServiceTransactionId;
    }

    public void setPgServiceTransactionId(String pgServiceTransactionId) {
        this.pgServiceTransactionId = pgServiceTransactionId;
    }
    
	@Column(name = "created_date")
	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	@Column(name = "created_by")
	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@Column(name = "updated_date")
	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	@Column(name = "updated_by")
	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	@Column(name = "transactionId")
	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	
	@OneToOne(mappedBy = "statusCheckResponseIdRef")
	public Transaction getTransactionReference() {
		return transactionReference;
	}

	public void setTransactionReference(Transaction transactionReference) {
		this.transactionReference = transactionReference;
	}
    
    

}

