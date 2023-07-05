package com.paymentgateway.phonePe;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "payment_response")
//, schema = "paytapp")
public class PaymentResponse {
	
    private int id;
    private boolean success;
    private String code;
    private String message;
    private String merchantId;
    private String merchantTransactionId;
    private String transactionId;
    private String type;
    private String url;
    private String method;
    private Transaction transactionReference;
    private Date createdDate;
    private String createdBy;
    private Date updatedDate;
    private String updatedBy;


	public PaymentResponse() {
		super();
	}

	public PaymentResponse(int id, boolean success, String code, String message, String merchantId,
			String merchantTransactionId, String transactionId, String type, String url, String method,
			//Transaction transactionReference,
			Date createdDate, String createdBy, Date updatedDate, String updatedBy) {
		super();
		this.id = id;
		this.success = success;
		this.code = code;
		this.message = message;
		this.merchantId = merchantId;
		this.merchantTransactionId = merchantTransactionId;
		this.transactionId = transactionId;
		this.type = type;
		this.url = url;
		this.method = method;
//		this.transactionReference = transactionReference;
		this.createdDate = createdDate;
		this.createdBy = createdBy;
		this.updatedDate = updatedDate;
		this.updatedBy = updatedBy;
	}

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
 
    @Column(name = "transactionId")
	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
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

	@Column(name = "type")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(name = "url")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Column(name = "method")
	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
	
	@OneToOne(mappedBy = "paymentResponseIdRef")
	public Transaction getTransactionReference() {
		return transactionReference;
	}

	public void setTransactionReference(Transaction transactionReference) {
		this.transactionReference = transactionReference;
	}



}
