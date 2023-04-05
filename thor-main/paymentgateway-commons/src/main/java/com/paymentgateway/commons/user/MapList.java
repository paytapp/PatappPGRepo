package com.paymentgateway.commons.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class MapList {

	private List<MerchantMopPopulator> mmpList = new ArrayList<MerchantMopPopulator>();
	private List<MerchantCurrencyPopulator> mcpList = new ArrayList<MerchantCurrencyPopulator>();
	public List<MerchantMopPopulator> getMmpList() {
		return mmpList;
	}
	public void setMmpList(List<MerchantMopPopulator> mmpList) {
		this.mmpList = mmpList;
	}
	public List<MerchantCurrencyPopulator> getMcpList() {
		return mcpList;
	}
	public void setMcpList(List<MerchantCurrencyPopulator> mcpList) {
		this.mcpList = mcpList;
	}
}
