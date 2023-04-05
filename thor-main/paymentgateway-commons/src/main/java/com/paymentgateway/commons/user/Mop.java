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

import com.paymentgateway.commons.util.MopType;


@Entity
public class Mop implements Serializable {
	
	private static final long serialVersionUID = 3427412715511705729L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	
	@Enumerated(EnumType.STRING)
	private MopType mopType;
			
	@OneToMany(targetEntity=MopTransaction.class,fetch = FetchType.EAGER,cascade = CascadeType.ALL,orphanRemoval=true)
	private Set<MopTransaction> mopTransactionTypes = new HashSet<MopTransaction>();
	
	public Mop(){
		
	}
	
	public Mop(MopType mopType){
		this.mopType = mopType;
	}

	public MopType getMopType() {
		return mopType;
	}

	public void setMopType(MopType mopType) {
		this.mopType = mopType;
	}
	
	public Set<MopTransaction> getMopTransactionTypes() {
		return mopTransactionTypes;
	}

	public void setMopTransactionTypes(Set<MopTransaction> mopTransactionTypes) {
		this.mopTransactionTypes = mopTransactionTypes;
	}
	
	public void addMopTransaction(MopTransaction mopTransaction) {
		mopTransactionTypes.add(mopTransaction);
	}

	public void removeTransactionType(MopTransaction mopTransaction) {
		mopTransactionTypes.remove(mopTransaction);
	}
}
