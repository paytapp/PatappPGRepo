package com.paymentgateway.commons.user;

import java.util.ArrayList;
import java.util.List;

public enum PermissionType {

	LOGIN(1, "login", false, false, false, false, 0), 
	CREATEUSER(3, "create", false, false, false, false, 0),
	DELETEUSER(4, "delete", false, false, false, false, 0), 
	VOID_REFUND(8, "Void/Refund", true, false, false, false, 0),
	CREATE_INVOICE(9, "Create Invoice", true, true, true, true, 0),
	VIEW_INVOICE(10, "View Invoice", true, true, true, true, 0),
	VIEW_REMITTANCE(11, "View Remittance", true, false, false, false, 0),
	// CREATE_TICKET(12, "Create Ticket", true, true, 0),
	VIEW_MERCHANT_SETUP(13, "View MerchantSetup", true, false, false, false, 0),
	CREATE_MAPPING(14, "Create Mapping", true, false, false, false, 0),
	VIEW_MERCHANT_BILLING(28, "View Merchant Billing", true, false, false, false, 0),
	VIEW_SEARCH_PAYMENT(16, "View SearchPayment", true, true, true, true, 0),
	CREATE_BATCH_OPERATION(18, "Create BatchOperation", true, false, false, false, 0),
	FRAUD_PREVENTION(19, "Fraud Prevention", true, false, false, false, 0),
	CREATE_BULK_EMAIL(20, "Create BulkEmail", true, false, false, false, 0),
	VIEW_CHARGEBACK(21, "ChargeBack", true, true, false, false, 0),

	SMART_ROUTER(22, "Smart Router", true, false, false, false, 0),
	CREATE_SURCHARGE(23, "Create Surcharge", true, false, false, false, 0),
	CREATE_TDR(24, "Create TDR", true, false, false, false, 0),
	CREATE_SERVICE_TAX(25, "Create Service Tax", true, false, false, false, 0),
	CREATE_MERCHANT_MAPPING(26, "Create Merchant Mapping", true, false, false, false, 0),
	CREATE_RESELLER_MAPPING(27, "Create Reseller Mapping", true, false, false, false, 0),
	MERCHANT_EDIT(28, "Edit Merchant Details", true, false, false, false, 2),
	VIEW_TRANSACTION_REPORTS(29, "View Transaction Reports", true, false, true, true, 0),
	VIEW_REPORTS(30, "View Reports", true, false, false, false, 0),
	VIEW_ACCOUNT_AND_FINANCE_REPORTS(31, "View Accounts and Finance Reports", true, false, false, false, 0),
	VIEW_ANALYTICS(32, "View Analytics", true, false, false, false, 0),
	NODAL_PAYOUTS(33, "Nodal Payouts", true, false, false, false, 0),
	REFRESH_DATA(34, "Settlement Data Refresh", true, false, false, false, 0),
	AGENT_SEARCH(35, "Agent Search", true, true, true, true, 0),
	VIEW_SURCHARGE(36, "View Surcharge", true, false, false, false, 0),
	REVIEW_MPA(37, "Review MPA", true, false, false, false, 0),
	APPROVE_MPA(38, "Approve MPA", true, false, false, false, 0),
	MERCHANT_VIEW(39, "View Merchant Details", true, false, false, false, 0),
	FILLING_MPA_FORM(40, "MPA Form Filling", true, false, false, false, 0),
	CREATE_BULK_USER(41, "Create Bulk User", true, false, false, false, 0),
	VIEW_CONFIGURATION(42, "View Configuration", true, false, false, false, 0),
	SCHEDULER(43, "Scheduler Jobs", true, false, false, false, 0),
	RESELLER_SETUP(44, "Reseller Setup", true, false, false, false, 0),
	VIEW_RESELLER_REVENUE(45, "View Reseller Revenue", true, false, false, false, 0),
	MANAGE_AGENT(46, "Manage Agent", true, false, false, false, 0),
	BOOKING_RECORD(47, "Booking Report", false, false, true, true, 0),
	DISBURSEMENT(48, "Disbursement", true, false, false, false, 0),
	CREATE_MPA(49, "Create MPA", true, true, true, false, 0),
	CUSTOM_CAPTURE_REPORT(50, "Custom Capture Report", false, false, true, true, 0),
	VENDOR_REPORT(51, "Vendor Report", true, true, true, true, 0),
	SUB_USER_ALL(52, "Sub User All", false, false, false, false, 0),
	SUB_USER_SELF(53, "Sub User Self", false, false, false, false, 0),

	// New PermissionsType added by Amitosh

	PERFORMANCE_REPORT(54, "Performance Report", true, false, false, false, 1),
	REVENUE_REPORT(55, "Revenue Report", true, false, false, false, 1),

	USER_REGISTRATION(56, "User Registration", true, false, false, false, 2),
	ADD_BULK_USER(57, "Add Bulk Users", true, false, false, false, 2),
	MERCHANT_ACCOUNT(58, "Merchant Account", true, false, false, false, 2),
	MERCHANT_UNDERWRITING(59, "Merchant Underwriting", true, false, false, false, 2),
	MERCHANT_LIST_FOR_MPA(60, "Merchant List For MPA", true, false, false, false, 2),
	USER_STATUS(127, "User Status", true, false, false, false, 2),
	CMS(128, "CMS", true, false, false, false, 2),
	USER_SETTING(141, "User Setting", true, false, false, false, 2),
	
	MERCHANT_MAPPING(61, "Merchant Mapping", true, false, false, false, 3),
	PAYMENT_OPTIONS(62, "Payment Options", true, false, false, false, 3),
	SUF_DETAILS(63, "SUF Details", true, false, false, false, 3),
	DISCOUNT_DETAILS(64, "Discount Details", true, false, false, false, 3),
	DISPATCH_DETAILS(65, "Dispatch Details", true, false, false, false, 3),
	CHARGING_PLATFORM(66, "Charging Platform", true, false, false, false, 3),
	BULK_UPDATE_CHARGES(67, "Bulk Update Charges", true, false, false, false, 3),
	MAILER(68, "Mailer", true, false, false, false, 3),
	CONFIGURE_SMART_ROUTER(69, "Configure Smart Router", true, false, false, false, 3),
	PENDING_REQUESTS(70, "Pending Requests", true, false, false, false, 3),
	PAYOUT_MAPPING(142, "Payout Mapping", true, false, false, false, 3),

	RESELLER_ACCOUNT(71, "Reseller Account", true, false, false, false, 4),
	RESELLER_CHARGES_UPDATE(72, "Reseller Charges Update", true, false, false, false, 4),
	RESELLER_MERCANT_LIST(73, "Reseller Merchant List", true, false, false, false, 4),
	RESELLER_REVENUE_REPORT(74, "Reseller Revenue Report", true, false, false, false, 4),

	VIEW_SMART_ROUTER_CONFIGURATION(75, "View Smart Router Configuration", true, false, false, false, 5),
	VIEW_CHARGING_DETAILS_CONFIGURATION(76, "View Charging Details Configuration", true, false, false, false, 5),
	VIEW_RESELLER_CHARGES_CONFIGURATIONS(77, "View Reseller Charges Configuration", true, false, false, false, 5),
	VIEW_PRODUCTIONS_DETAILS_CONFIGURATIONS(78, "View Production Details Configurations", true, false, false, false, 5),

	SEARCH_TRANSACTION(79, "Search Transaction", true, false, false, false, 6),
	DOWNLOAD_TRANSACTION(80, "Download Transaction", true, false, false, false, 6),
	QUICK_SEARCH(136, "Quick Search", true, false, false, false, 23),

	SALE_CAPTURE_REPORT(81, "Sale Capture", true, false, false, false, 7),
	REFUND_CAPTURE_REPORT(82, "Refund Capture", true, false, false, false, 7),
	SETTLED_REPORT(128, "Settled", true, false, false, false, 7),
	DOWNLOAD_REPORT(83, "Download Report", true, false, false, false, 7),
	PAYMENT_ADVISE_REPORT(84, "Payment Advice", true, false, true, true, 7),
	CUSTOM_CAPTURE(85, "Custom Capture Report", true, false, false, false, 7),
	BOOKING_RECORD_REPORT(86, "Booking Record", true, false, false, false, 7),
	BOOKING_REPORT_REPORT(87, "Booking Report", true, false, false, false, 7),
	ECOLLECTION_REPORT(88, "eCollection Report", true, false, true, true, 7),
	SUMMARY_REPORT(89, "Summary Report", true, false, false, false, 7),
	DOWNLOAD_SUMMARY_REPORT(90, "Download Summary", true, false, false, false, 7),
	SETTLED_BOOKINGS_REPORT(91, "Settled Bookings", true, false, false, false, 7),
	REFUND_REPORT_REPORT(92, "Refund Report", true, false, false, false, 7),
	ACQUIRER_MPR_REPORT(93, "Acquirer MPR", true, false, false, false, 7),
	REFUND_SUMMARY_REPORT(94, "Refund Summary", true, false, false, false, 7),
	MIS_REPORT(95, "MIS Report", true, false, false, false, 7),
	BANK_EXCEPTION_REPORT(96, "Bank Exception", true, false, false, false, 7),
	MERCHNAT_EXCEPTION_REPORT(97, "Merchant Exception", true, false, false, false, 7),
	GSTR_SALE_REPORT(98, "GSTR Sale", true, false, false, false, 7),
	CUSTOMER_QR_REPORT(135, "Static UPI QR Report", true, false, true, true, 7),
	NET_SETTLED_REPORT(137, "Net Settled Report", true, false, true, true, 7),
	ECOLLECTION_TRANSACTION(139, "eCollection Transaction", true, false, true, true, 7),
	ECOLLECTION_VALIST(140, "Virtual Account List", true, false, true, true, 7),

	PRODUCT_REPORT(99, "Product Report", true, false, false, false, 8),
	PAYOUT_REPORT(100, "Payout Report", true, false, false, false, 8),

	INVOICE_PAYMENTS(101, "Invoice Payments", true, false, false, false, 9),
	BULK_INVOICE(102, "Bulk Invoice", true, false, false, false, 9),
	QUICK_PAYMENT_SEARCH(103, "Quick Payment Search", true, false, false, false, 9),
	BULK_INVOICE_SEARCH(104, "Bulk Invoice Search", true, false, false, false, 9),

	SCHOOL_FEE_MANAGER(105, "School Fee Manager", true, false, false, false, 10),

	BIN_RANGES(106, "BIN Ranges", true, false, false, false, 11),
	EMI_BIN_RANGES(107, "EMI BIN Ranges", true, false, false, false, 11),
	HOTEL_INVENTORY(108, "Hotel Inventory", true, false, false, false, 11),

	IMPS_TRANSFER(109, "IMPS Transfer", false, false, false, false, 12),
	MERCHANT_INITIATED_DIRECT(133, "Payout", true, false, true, true, 12),
	ADD_BENEFICIARY(134, "Add Beneficiary", true, false, false, false, 12),
	NODAL_TRANSFER(110, "Nodal Transfer", true, false, false, false, 12),
	

	FPS_CONFIGURATION(111, "FPS Configuration", true, false, false, false, 13),

	ADD_SUB_ADMIN(112, "Add Sub Admin", true, false, false, false, 14),
	SUB_ADMIN_LIST(113, "Sub Admin List", true, false, false, false, 14),
	SUB_MERCHANT_LIST(114, "Sub Merchant List", true, false, false, false, 14),
	ADD_SUB_USER(115, "Add Sub User", true, false, false, false, 14),
	SUB_USER_LIST(116, "Sub User List", true, false, false, false, 14),
	ADD_AGENT(117, "Add Agent", true, false, false, false, 14),
	AGENT_LIST(118, "Agent List", true, false, false, false, 14),
	ASSIGN_MPA(119, "Assign MPA", true, false, false, false, 14),
	KHADI_VENDOR_LIST(120, "Khadi Vendor List", true, false, false, false, 14),
	ADD_KHADI_VENDOR(121, "Add Khadi Vendor", true, false, false, false, 14),
	ADD_ACQUIRER(122, "Add Acquirer", true, false, false, false, 15),
	ACQUIRER_LIST(123, "Acquirer List", true, false, false, false, 15),
	ACQUIRER_OPERATIONS(138, "Acquirer Operations", true, false, false, false, 15),
	EMI(124, "EMI", true, false, false, false, 16),
	AGNET_SEARCH(125, "Agent Search", true, false, false, false, 17),
	CHARGEBACK(126, "View ChargeBack", true, true, true, true, 18),
	FRAUD_ANALYTICS(127, "Fraud Analytics", true, false, false, false, 13),
	FRAUD_RULE_DOWNLOAD(143, "Download Fraud Rule", true, false, false, false, 13),
	
	MSEDCL(129, "MSEDCL", true, false, false, false, 19),
	ENACH_REPORT(130, "eNACH Report", true, false, true, true, 20),
	MONTHLY_INVOICE(132, "Monthly Invoice", true, false, false, false, 11),
	ACCOUNT_VERIFICATION(133, "Account Verification", true, false, true, true, 21),
	UPLOAD_TXN(145, "Upload TXN", true, false, false, false, 11),
	VPA_VERIFICATION(134, "VPA Verification", true, false, true, true, 21),
	ALLOW_EPOS(135, "Allow ePOS", true, false, true, true, 22),
	UPI_AUTOPAY_REPORT(136, "UPI AutoPay Report", true, false, true, true, 20);

	private final String permission;
	private final int id;
	private final boolean isInternal;
	private final boolean isInternalValue;
	private final boolean isExternal;
	private final boolean isExternalValue;
	private final int permissionCategory;

	private PermissionType(int id, String permission, boolean isInternal, boolean isInternalValue, boolean isExternal,
			boolean isExternalValue, int permissionCategory) {
		this.id = id;
		this.permission = permission;
		this.isInternal = isInternal;
		this.isInternalValue = isInternalValue;
		this.isExternal = isExternal;
		this.isExternalValue = isExternalValue;
		this.permissionCategory = permissionCategory;
	}

	public int getId() {
		return id;
	}

	public String getPermission() {
		return permission;
	}

	public boolean isInternal() {
		return isInternal;
	}

	public boolean isInternalValue() {
		return isInternalValue;
	}

	public boolean isExternal() {
		return isExternal;
	}

	public boolean isExternalValue() {
		return isExternalValue;
	}

	public int getPermissionCategory() {
		return permissionCategory;
	}

	public static List<PermissionType> getPermissionType() {
		List<PermissionType> permissionTypes = new ArrayList<PermissionType>();
		for (PermissionType permissionType : PermissionType.values()) {
			if (permissionType.isInternal())
				permissionTypes.add(permissionType);
		}
		return permissionTypes;
	}

	public static PermissionType getInstanceFromName(String code) {
		PermissionType[] permissionTypes = PermissionType.values();
		for (PermissionType permissionType : permissionTypes) {
			if (String.valueOf(permissionType.getPermission()).toUpperCase().equals(code.toUpperCase())) {
				return permissionType;
			}
		}
		return null;
	}

	public static List<PermissionType> getSubAcquirerPermissionType() {
		List<PermissionType> permissionTypes = new ArrayList<PermissionType>();
		for (PermissionType permissionType : PermissionType.values()) {
			if (permissionType.isInternalValue())
				permissionTypes.add(permissionType);
		}
		return permissionTypes;
	}

	public static List<PermissionType> getSubUserPermissionType() {
		List<PermissionType> permissionTypes = new ArrayList<PermissionType>();
		for (PermissionType permissionType : PermissionType.values()) {
			if (permissionType.isExternal())
				permissionTypes.add(permissionType);
		}
		return permissionTypes;
	}

	public static List<PermissionType> getSubUserPermissionTypeForSuperMerchant() {
		List<PermissionType> permissionTypes = new ArrayList<PermissionType>();
		for (PermissionType permissionType : PermissionType.values()) {
			if (permissionType.isExternalValue())
				permissionTypes.add(permissionType);
		}
		return permissionTypes;
	}

	public static List<PermissionType> getSubUserAccessPrevilageType() {
		List<PermissionType> permissionTypes = new ArrayList<PermissionType>();
		permissionTypes.add(PermissionType.SUB_USER_ALL);
		permissionTypes.add(PermissionType.SUB_USER_SELF);
		return permissionTypes;
	}

	public static List<PermissionType> getSubAdminPermissionTypeByCategory(int Category) {
		List<PermissionType> permissionTypes = new ArrayList<PermissionType>();
		for (PermissionType permissionType : PermissionType.values()) {
			if (permissionType.isInternal() && permissionType.permissionCategory == Category)
				permissionTypes.add(permissionType);
		}
		return permissionTypes;
	}
	
	public static List<PermissionType> getResellerPermissionTypeByCategory(int Category) {
		List<PermissionType> permissionTypes = new ArrayList<PermissionType>();
		for (PermissionType permissionType : PermissionType.values()) {
			if (permissionType.isInternal() && permissionType.permissionCategory == Category)
				permissionTypes.add(permissionType);
		}
		return permissionTypes;
	}

}
