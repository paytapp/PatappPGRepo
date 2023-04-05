package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Proxy;

import com.paymentgateway.commons.util.PaymentType;

@Entity
@Proxy(lazy=false)
public class Payment implements Serializable {
	
	private static final long serialVersionUID = -5338518560081770107L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@Enumerated(EnumType.STRING)
	private PaymentType paymentType;
	
	@OneToMany(targetEntity=Mop.class,fetch = FetchType.EAGER,cascade = CascadeType.ALL,orphanRemoval=true)
	private Set<Mop> mops = new HashSet<Mop>();

	public Payment(){
		
	}
	
	public PaymentType getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}

	public Set<Mop> getMops() {
		return mops;
	}

	public void setMops(Set<Mop> mops) {
		this.mops = mops;
	}
	
	public void addMop(Mop mop){
		mops.add(mop);
	}
	
	public void removeMop(Mop mop){
		mops.remove(mop);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
