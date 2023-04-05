package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * @author Rahul
 *
 */

@Entity
@Proxy(lazy=false)
public class ChargebackComment implements Serializable{

	private static final long serialVersionUID = 7625357795853259376L;
	@Id
	@Column(nullable = false,unique = true)
	private String commentId;
	@CreationTimestamp
	private Date createDate;
	@UpdateTimestamp
	private Date updateDate;
	private String commentSenderEmailId;
	private String documentId;
	@Column(length = 65535,columnDefinition="Text")
	private String commentBody;
	private String imageFileName;
	@Transient
	private String commentcreateDate;
	

	
	
	
	
	public String getImageFileName() {
		return imageFileName;
	}
	public void setImageFileName(String imageFileName) {
		this.imageFileName = imageFileName;
	}
	public String getCommentId() {
		return commentId;
	}
	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	public String getCommentSenderEmailId() {
		return commentSenderEmailId;
	}
	public void setCommentSenderEmailId(String commentSenderEmailId) {
		this.commentSenderEmailId = commentSenderEmailId;
	}
	public String getCommentBody() {
		return commentBody;
	}
	public void setCommentBody(String commentBody) {
		this.commentBody = commentBody;
	}	
	public String getDocumentId() {
		return documentId;
	}
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
	public String getCommentcreateDate() {
		return commentcreateDate;
	}
	public void setCommentcreateDate(String commentcreateDate) {
		this.commentcreateDate = commentcreateDate;
	}
	
}
