<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<link rel="stylesheet" type="text/css" href="../css/popup.css" />
<!-- Modal -->
<div class="modal-dialog">

	<!-- Modal content-->
	<div class="modal-content">
		<div class="modal-body">
			<table class="table98">
				<tr>
					<td colspan="5" align="center" valign="middle" class="headgpop"
						height="25">Transaction <s:property value="status" /></td>
				</tr>
				<tr>
					<td colspan="5" height="5" align="center" valign="top"></td>
				</tr>
				<tr>
					<td colspan="5" align="center" valign="top"><table
							cellpadding="0" class="product-spec table98">
							<tr>
								<td colspan="2" align="left" valign="middle" bgcolor="#ccdce7"
									class="newpopuphead" style="background: #ccdce7">Customer
									Details</td>
							</tr>
							<tr>
								<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Customer
										Name</strong></td>
								<td bgcolor="#FFFFFF"><div id="param-custName">
										<s:property value="%{aaData.custName}" />
									</div></td>
							</tr>
							<tr>
								<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Phone</strong></td>
								<td bgcolor="#FFFFFF"><div id="param-custPhone">
										<s:property value="%{aaData.custPhone}" />
									</div></td>
							</tr>
							<tr>
								<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Email</strong></td>
								<td bgcolor="#FFFFFF"><span class="bluetext"><s:property
											value="custEmail" /></span></td>
							</tr>
							<s:if test="%{transactionAuthenticationFlag == true}">
								<tr>
									<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Customer
											Authentication</strong></td>
									<td bgcolor="#FFFFFF"><s:property
											value="%{aaData.internalTxnAuthentication}" /></td>
								</tr>
							</s:if>
							<tr>
								<td colspan="2" align="left" valign="middle"
									class="newpopuphead" style="background: #ccdce7">Transaction
									Details</td>
							</tr>
							<tr>
								<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Amount</strong></td>
								<td bgcolor="#FFFFFF"><s:property value="amount" />-<s:property
										value="currency" /></td>
							</tr>
							<tr>
								<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Date
										& Time</strong></td>
								<td bgcolor="#FFFFFF"><s:property value="datefrom" /></td>
							</tr>
							<tr>
								<td width="32%" align="left" valign="middle" bgcolor="#FFFFFF"><strong>Status</strong></td>
								<td width="68%" bgcolor="#FFFFFF"><s:property
										value="status" />
									<s:if test="%{pgTxnMessage != null && pgTxnMessage != ''}">&nbsp;(<s:property
											value="pgTxnMessage" />)</s:if></td>
							</tr>
							
							
							<s:if test="%{status == 'Denied due to fraud'}">
							<tr>
								<td width="32%" align="left" valign="middle" bgcolor="#FFFFFF"><strong>Fraud Type</strong></td>
								<td width="68%" bgcolor="#FFFFFF">
								<s:property value="responseMsg"/></td>
							</tr></s:if>
							
							
							<tr>
								<td width="32%" align="left" valign="middle" bgcolor="#FFFFFF"><strong>Transaction
										ID</strong></td>
								<td width="68%" bgcolor="#FFFFFF"><s:property
										value="transactionId" /> <s:property value="txnId" /></td>
							</tr>
							<tr>
								<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Payment
										Method</strong></td>
								<td bgcolor="#FFFFFF"><s:property value="paymentMethod" />&nbsp;(<s:property
										value="mopType" />)</td>
							</tr>
							<tr>
								<s:if test="%{paymentMethod == 'Debit Card' || paymentMethod == 'Credit Card'}">
								<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Card
										Issuer Info</strong></td>
									<td bgcolor="#FFFFFF"><s:if
											test="%{internalCardIssusserBank !=null}">
											<s:property value="internalCardIssusserBank" />
											<s:if test="%{internalCardIssusserCountry !=null}">&nbsp;(<s:property
													value="internalCardIssusserCountry" />)</s:if>
										</s:if>
										<s:else>N/A</s:else></td>
								</s:if>
							</tr>
							<s:if test="%{showRequestFlag == true}">
								<tr>
									<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Request
											Parameter</strong></td>
									<td bgcolor="#FFFFFF"><div class="scrollpera">
											<s:property value="internalRequestDesc" />
										</div></td>
								</tr>
							</s:if>
							<tr>
								<td colspan="2" align="left" valign="middle"
									class="newpopuphead" style="background: #ccdce7">Order
									Details</td>
							</tr>
							<tr>
								<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Product
										Description</strong></td>
								<td bgcolor="#FFFFFF"><s:property value="productDesc" /></td>
							</tr>
							<tr>
								<td align="left" valign="middle" bgcolor="#FFFFFF"><strong>Order
										ID</strong></td>
								<td bgcolor="#FFFFFF"><s:property value="orderId" /></td>
							</tr>
						</table></td>
				</tr>
				<tr>
					<td align="center" valign="top" height="5"></td>
				</tr>
				<tr>
					<td align="center" valign="top">
						<table class="table98">
							<tr>
								<td width="33%" align="center" valign="top" bgcolor="#FFFFFF">
									<table class="orgco1 table98">
										<tr>
											<th align="left" valign="middle">Billing Address</th>
										</tr>
										<tr>
											<td height="90" align="left" valign="top"><div
													id="param-custStreetAddress1">
													<p>
														<s:property value="%{aaData.custStreetAddress1}" />
												</div>
												<div id="param-custStreetAddress2">
													<p>
														<s:property value="%{aaData.custStreetAddress2}" />
												</div>
												<div id="param-custCity">
													<p>
														<s:property value="%{aaData.custCity}" />
												</div>
												<div id="param-custState">
													<p>
														<s:property value="%{aaData.custState}" />
												</div>
												<div id="param-custCountry">
													<p>
														<s:property value="%{aaData.custCountry}" />
												</div>
												<div id="param-custZip">
													<p>
														<s:property value="%{aaData.custZip}" />
												</div></td>
										</tr>
									</table>
								</td>
								<td width="33%" align="left" valign="top" bgcolor="#FFFFFF"><table
										class="orgco1 table98">
										<tr>
											<th align="left" valign="middle">Shipping Address</th>
										</tr>
										<tr>
											<td height="90" align="left" valign="top"><p>
												<div id="param-custShipStreetAddress1">
													<p>
														<s:property value="%{aaData.custShipStreetAddress1}" />
												</div>
												<div id="param-custShipStreetAddress2">
													<p>
														<s:property value="%{aaData.custShipStreetAddress2}" />
												</div>
												<div id="param-custShipCity">
													<p>
														<s:property value="%{aaData.custShipCity}" />
												</div>
												<div id="param-custShipState">
													<p>
														<s:property value="%{aaData.custShipState}" />
												</div>
												<div id="param-custShipCountry">
													<p>
														<s:property value="%{aaData.custShipCountry}" />
												</div>
												<div id="param-custShipZip">
													<p>
														<s:property value="%{aaData.custShipZip}" />
												</div></td>
										</tr>
									</table></td>
								<td width="33%" align="right" valign="top" bgcolor="#FFFFFF"><table
										class="orgco1 table98">
										<tr>
											<th align="left" valign="middle">Cardholder Details</th>
										</tr>
										<tr>
											<td height="90" align="left" valign="top" bgcolor="#FFFFFF"><p>
												<div id="param-cardNumber">
													<p>
														<s:property value="%{cardNumber}" />
												</div>
												<div id="param-custName">
													<p>
														<s:property value="%{aaData.custName}" />
												</div>
												<div>
													<s:property value="paymentMethod" />
													&nbsp;(
													<s:property value="mopType" />
													)
												</div></td>
										</tr>
									</table></td>
							</tr>
							<tr>
								<td colspan="3"><div class="button-position">
										<button class="popup_close closepopupbtn"></button>
									</div></td>
							</tr>

						</table>
					</td>
				</tr>
			</table>
		</div>
	</div>
</div>