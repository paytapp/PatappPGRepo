package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;

@Entity
@Proxy(lazy=false)@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Roles implements Serializable {

	private static final long serialVersionUID = 3527144404701118357L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;	
	private String name;

	@OneToMany(targetEntity=Permissions.class,fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	private Set<Permissions> permissions = new HashSet<Permissions>(); 

	public Roles(){
	}

	public Roles(long id, String name){
		this.id = id;
		this.name = name;
	}

		
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void addPermission(Permissions permission){
		this.permissions.add(permission);
	}

	
	public Set<Permissions> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<Permissions> permissions) {
		this.permissions = permissions;
	} 
}
