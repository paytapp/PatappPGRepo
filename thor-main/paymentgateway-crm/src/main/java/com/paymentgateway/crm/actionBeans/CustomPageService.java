package com.paymentgateway.crm.actionBeans;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CustomPage;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class CustomPageService {

	private static Logger logger = LoggerFactory.getLogger(CustomPageService.class.getName());
	
	@Autowired
	private UserDao userDao;
	
	public void saveImage(CustomPage custom) {
		String sourceFile = null;
	//	sessionUser = (User) sessionMap.get(Constants.USER.values());
	//	User merchant = userDao.findPayId(payId);
		try {
			
			String location = PropertiesManager.propertiesMap.get(Constants.CUSTOM_PAGE_LOGO_LOCATION.getValue()) + custom.getPayId();
			
			if(custom.getMerchantLogoImage() != null) {
				sourceFile = "logo.png";
				File destFile = new File(location, sourceFile);
				FileUtils.copyFile(custom.getMerchantLogoImage(), destFile);
			}else if(custom.getBackGroundImage() != null) {
				sourceFile = "texture-01.png";
				File destFile = new File(location, sourceFile);
				FileUtils.copyFile(custom.getBackGroundImage(), destFile);
			}else if(custom.getMerchantBannerImage() != null) {
				sourceFile = "banner.png";
				File destFile = new File(location, sourceFile);
				FileUtils.copyFile(custom.getMerchantBannerImage(), destFile);
			}
			
			String root = FileSystems.getDefault().getPath("").toAbsolutePath().toString();
			String newDirectory = root.replace("bin", "webapps");
			Path filePath = Paths.get(newDirectory, "crm", "image");
			
			File sourceFilePath = new File(filePath.toString());
			File[] files = sourceFilePath.listFiles();
			int count = 0;
			
			for(File file : files) {
				
				if(file.toString().contains("loader.gif")) {
					count++;
					sourceFile = "loader.gif";
					File destFile = new File(location, sourceFile);
					FileUtils.copyFile(file, destFile);
				}
				if(file.toString().contains("PaymentGateway3.png")) {
					count++;
					sourceFile = "powered-by.png";
					File destFile = new File(location, sourceFile);
					FileUtils.copyFile(file, destFile);
				}
				if(file.toString().contains("PaymentGateway.png") && StringUtils.isNotEmpty(custom.getPaymentGatewayLogoFlag()) && custom.getPaymentGatewayLogoFlag().equalsIgnoreCase("on")) {
                    sourceFile = "PaymentGateway.png";
                    File destFile = new File(location, sourceFile);
                    FileUtils.copyFile(file, destFile);
                }
				if(file.toString().contains("texture-01.png") && custom.getBackGroundImage() != null) {
					count++;
					sourceFile = "texture-01.png";
					File destFile = new File(location, sourceFile);
					FileUtils.copyFile(file, destFile);
				}
				if(file.toString().contains("icon-upload.png")) {
					count++;
					sourceFile = "icon-upload.png";
					File destFile = new File(location, sourceFile);
					FileUtils.copyFile(file, destFile);
				}
				if(count == 5)
					break;
			}
		} catch (Exception e) {
			logger.error("Exception while saving logoImage to location : " , e);
		}
	}
	
	
	public File writeToFile(String fileContent, String fileName) {

		File file = new File(fileName);
		
		try {
			file.createNewFile();
			OutputStream outputStream = new FileOutputStream(file.getAbsoluteFile());
			Writer writer = new OutputStreamWriter(outputStream);
			writer.write(fileContent);
			writer.close();
			
		} catch (IOException e) {
			logger.error("Exception while writeToFile method is called : " , e);
		}
		return file;
	}
	public InputStream createZipFile(File file, String zipFileName, String payId) throws Exception {
		
		InputStream inputfileStream = null;
		String location = PropertiesManager.propertiesMap.get(Constants.CUSTOM_PAGE_TNC_FILES_LOCATION.getValue()) + payId;
		try {
			String root = FileSystems.getDefault().getPath("").toAbsolutePath().toString();
			String newDirectory = root.replace("bin", "webapps");
			Path filePath = Paths.get(newDirectory, "crm", "customPage");
			
			
			FileOutputStream fos = new FileOutputStream(zipFileName);
		    ZipOutputStream zos = new ZipOutputStream(fos);
		    
		    File sourceFilePath = new File(location);
			File[] TnCFiles = sourceFilePath.listFiles();
			
		    createZipOfAllDirectories(zos, new File(filePath.toString()), null, file, TnCFiles);
			
			
			File zipFile = new File(zipFileName);
			inputfileStream = new FileInputStream(zipFile);
			zos.flush();
		    fos.flush();
		    zos.close();
		    fos.close();
		} catch (IOException e) {
			logger.error("Exception while writeToFile method is called : " , e);
		}

		return inputfileStream;
	}
	public void uploadfilesInImagefolder(CustomPage custom) {
		String location = PropertiesManager.propertiesMap.get(Constants.CUSTOM_PAGE_LOGO_LOCATION.getValue()) + custom.getPayId();
		try {
			String root = FileSystems.getDefault().getPath("").toAbsolutePath().toString();
			String newDirectory = root.replace("bin", "webapps");
			Path fileRootPath = Paths.get(newDirectory, "crm", "customPage","images");
			File rootDirectory = new File(fileRootPath.toString());
			
			File sourceFilePath = new File(location);
			File[] files = sourceFilePath.listFiles();
			
			for(File file : files) {
				if(!rootDirectory.exists())
					rootDirectory.mkdirs();
				
				File destFile = new File(rootDirectory, file.getName());
				FileUtils.copyFile(file, destFile);
			}
		} catch (IOException e) {
			logger.error("Exception cought while uploading image files in server : " , e);
		}
	}
	public String createHtmlForm(CustomPage custom) {
		
		String contactNumberArray[] = null;
		String contactEmailArray[] = null;
		if(custom.getContactPhone()!=null) {
			contactNumberArray = custom.getContactPhone().split(",");
		}
		if(custom.getContactEmail()!=null) {
			contactEmailArray = custom.getContactEmail().split(",");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>");
		sb.append("<html lang=\"en\">");
		sb.append("<head>");
		sb.append("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>");
		sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
		sb.append("<title>");
		sb.append(custom.getPageTitle());
		sb.append("</title>");
		sb.append("<link rel=\"shortcut icon\" href=\"../images/favicon.ico\" type=\"images/x-icon\">");
		sb.append("<link rel=\"stylesheet\" href=\"../customPage/css/bootstrap.min.css\">");
		sb.append("<link rel=\"stylesheet\" href=\"../customPage/css/common-style.css\">");
		sb.append("<link rel=\"stylesheet\" href=\"../customPage/css/loader-animation.css\">");
		sb.append("<link href=\"https://fonts.googleapis.com/css?family=Source+Sans+Pro:400,600,700&display=swap\" rel=\"stylesheet\">");
		sb.append("</head>");
		
		sb.append("<style>");
			sb.append(createCSSStyle(custom));
		sb.append("</style>");
		
//		sb.append("<script>");
//			sb.append(createScriptJS(custom));
//		sb.append("</script>");
		
		sb.append("<body class=\"position-relative text-grey-dark section-body ");
		if(custom.getBackGroundImage() != null) {
			sb.append("isBgActive \">");
		}else {
			sb.append("\">");
		}
			sb.append("<header class=\"py-10 position-relative z-index-2 box-shadow-primary\" header-bg-color \">");
				sb.append("<div class=\"container\">");
					sb.append("<div class=\"row d-flex\">");
						sb.append("<div class=\"logo col-md-3\">");
							sb.append("<img src=\"../customPage/images/logo.png\" alt=\"\" class=\"d-block\" height=\"100\">");
						sb.append("</div>");
						sb.append("<div class=\"col-12 col-md-6 mt-10 text-center d-flex flex-column justify-content-center\">");
//							sb.append("<h2 class=\"font-size-22 font-weight-bold mb-10\">");
//								sb.append("Hotel Booking Portal");
//							sb.append("</h2>");
							sb.append("<span class=\"font-size-18 font-weight-medium\">");
//								sb.append("Government of Punjab Initiative");
								sb.append(custom.getMerchantSlogan());
							sb.append("</span>");
						sb.append("</div>");
						if(StringUtils.isNotEmpty(custom.getPaymentGatewayLogoFlag()) && custom.getPaymentGatewayLogoFlag().equalsIgnoreCase("on")) {
							sb.append("<div class=\"col-6 col-md-3 d-flex align-items-center justify-content-end\">");
								sb.append("<img src=\"../customPage/images/PaymentGateway.png\" alt=\"\" class=\"img-fluid\">");
							sb.append("</div>");
						}
					sb.append("</div>");
				sb.append("</div>");
			sb.append("</header>");
			
			sb.append("<section class=\"position-relative z-index-2 heading-style\">");
			sb.append("<div class=\"container\">");
				sb.append("<div class=\"row my-30 flex-column-reverse flex-lg-row\">");
//					sb.append("<div class=\"col-lg-6 mt-20 mt-lg-0 text-justify\">");
//						sb.append("<h2 class=\"font-weight-medium font-size-22 text-primary heading-border-bottom mb-10\">");
//							if(custom.getHeader() != null)
//								sb.append(custom.getHeader());
//							else
//								sb.append("About Us");
//						sb.append("</h2>");
//	//about content
//						sb.append("<p>While there are many sports programmes dedicated to children - whether in local residential vicinities, schools or in sports academies - their focus is mainly on the physical aspect of sport. Aiming for excellence and on hard labour. These programmes focus on the children who show potential, who have skill and who work hard.</p>" + 
//								" <p>The Art of Sport is for the rest of us; for those children who used to love to play and run about but for whatever reason, stopped. It is for those children who spend a lot of time in their heads, ignoring their bodies and wasting the potential strength and courage that resides in them.</p>" + 
//								" <p>We focus on the overall development of young children through sport and group therapy. By overall development we mean guiding them to be more self-aware of their physical, mental and emotional capacities and limitations.</p>");
//						if(custom.getSubHeader1() != null) {
//							sb.append("<h2 class=\"font-weight-medium font-size-22 text-primary heading-border-bottom mb-10\">");
//							sb.append(custom.getSubHeader1());
//							sb.append("</h2><p>");
//							sb.append(custom.getParagraph1());
//							sb.append("</p>");
//						}
//						if(custom.getSubHeader2() != null) {
//							sb.append("<h2 class=\"font-weight-medium font-size-20 text-primary mb-10\">");
//							sb.append(custom.getSubHeader2());
//							sb.append("</h2><p>");
//							sb.append(custom.getParagraph2());
//							sb.append("</p>");
//						}
//						if(custom.getSubHeader3() != null) {
//							sb.append("<h2 class=\"font-weight-medium font-size-20 text-primary mb-10\">");
//							sb.append(custom.getSubHeader3());
//							sb.append("</h2><p>");
//							sb.append(custom.getParagraph3());
//							sb.append("</p>");
//						}
//	//banner image
//						sb.append("<div class=\"mt-20 pt-20 border-top-grey-lightest\">");
//							sb.append("<img src=\"../customPage/images/banner.jpg\" alt=\"\" class=\"img-fluid\">");
//						sb.append("</div>");
//					sb.append("</div>");
					sb.append("<div class=\"col-lg-6\">");
					sb.append(custom.getAboutContent());
					sb.append("</div>");
					sb.append("<div class=\"col-lg-6\">");
						sb.append("<div class=\"form-box form-bg-color box-shadow-primary border-primary p-30 bg-grey-lightest\">");
	// payment details lebel
							sb.append("<h3 class=\"font-weight-medium font-size-22 text-primary heading-border-bottom mb-20\">Payment Details</h3>");
							sb.append("<form method=\"POST\" action=\"https://www.PaymentGateway.com/pgui/jsp/paymentrequest\" id=\"paymentDetail\" class=\"row\" autocomplete=\"off\">");
								sb.append("<div class=\"col-12 col-sm-6 col-lg-12\">");
									sb.append("<div class=\"form-group row\">");
	// lebel name								
										sb.append("<label for=\"name\" class=\"col-12 col-lg-4 col-form-label pt-0 pt-sm-5 font-size-12 font-size-lg-14 font-weight-medium d-lg-flex align-items-lg-center\">");
										sb.append("Name ");
//										sb.append(custom.getFormDetailsName());
										sb.append("<span class=\"text-red ml-5\">*</span></label>");
										sb.append("<div class=\"col-12 col-lg-8\">");
											sb.append("<input type=\"text\" name=\"CUST_SHIP_NAME\" id=\"name\" class=\"form-control input-box\" placeholder=\"");
											sb.append("Name");
											//sb.append(custom.getFormDetailsName());
											sb.append("\" oninput=\"onlyAlpha(this)\">");
										sb.append("</div>");
									sb.append("</div>");
								sb.append("</div>");
								
								sb.append("<div class=\"col-12 col-sm-6 col-lg-12\">");
									sb.append("<div class=\"form-group row\">");
	                                	sb.append("<label for=\"phone\" class=\"col-12 col-lg-4 col-form-label pt-0 pt-sm-5 font-size-12 font-size-lg-14 font-weight-medium d-lg-flex align-items-lg-center\">");
	                                		sb.append("Phone Number ");
	                         //       		sb.append(custom.getFormDetailsPhone());
	                                	sb.append("<span class=\"text-red ml-5\">*</span></label>");
		                                sb.append("<div class=\"col-12 col-lg-8\">");
	                                    	sb.append("<input type=\"text\" name=\"CUST_PHONE\" id=\"phone\" class=\"form-control input-box\" maxlength=\"10\" placeholder=\"");
	                          //          	sb.append(custom.getFormDetailsPhone());
	                                    	sb.append("Phone Number");
	                                    	sb.append("\" oninput=\"onlyNumberInput(this);\" required>");
	                                    sb.append("</div>");
	                                sb.append("</div>");
	                            sb.append("</div>");
		
								sb.append("<div class=\"col-12 col-sm-6 col-lg-12\">");
									sb.append("<div class=\"form-group row\">");
										// lebel email								
										sb.append("<label for=\"email\" class=\"col-12 col-lg-4 col-form-label pt-0 pt-sm-5 font-size-12 font-size-lg-14 font-weight-medium d-lg-flex align-items-lg-center\">");
											sb.append("Email Id ");
										sb.append("<span class=\"text-red ml-5\">*</span></label>");
										sb.append("<div class=\"col-12 col-lg-8\">");
											sb.append("<input type=\"text\" name=\"CUST_EMAIL\" id=\"email\" class=\"form-control input-box\" placeholder=\"");
												sb.append("Email Id");
											sb.append("\" onkeypress=\"validateEmail(this, event);\" oninput=\"validateEmail(this, event);\" required>");
										sb.append("</div>");
									sb.append("</div>");
								sb.append("</div>");
								
								if(custom.getFormInputFields() != null) {
									sb.append(custom.getFormInputFields());
								}
								
								sb.append("<div class=\"col-12 col-sm-6 col-lg-12\">");
									sb.append("<div class=\"form-group row\">");
										// lebel amount
										sb.append("<label for=\"amount\" class=\"col-12 col-lg-4 col-form-label pt-0 pt-sm-5 font-size-12 font-size-lg-14 font-weight-medium d-lg-flex align-items-lg-center\">");
										sb.append("Amount ");
										sb.append("<span class=\"text-red ml-5\">*</span></label>");
										sb.append("<div class=\"col-12 col-lg-8\">");
											sb.append("<input type=\"text\" id=\"amount\" name=\"TEMP_AMOUNT\" class=\"form-control\" oninput=\"onlyNumericKey(this, event, 2);\" placeholder=\"");
											sb.append("Amount");
											sb.append("\">");
										sb.append("</div>");
									sb.append("</div>");
								sb.append("</div>");
		
								sb.append("<input type=\"hidden\" name=\"AMOUNT\" id=\"finalAmount\" class=\"input-box\">");
								sb.append("<input type=\"hidden\" name=\"ORDER_ID\" id=\"orderId\" class=\"input-box\">");
								sb.append("<input type=\"hidden\" name=\"PAY_ID\" id=\"payId\" value=\"");
								sb.append("8503500623150723");
								// sb.append(custom.getPayId());
								sb.append("\" class=\"input-box\">");
								sb.append("<input type=\"hidden\" id=\"tempPayId\" value=\"1111111111111111\">");
								sb.append("<input type=\"hidden\" name=\"CURRENCY_CODE\" value=\"356\" class=\"input-box\">");
								sb.append("<input type=\"hidden\" name=\"TXNTYPE\" value=\"SALE\" class=\"input-box\">");
								sb.append("<input type=\"hidden\" name=\"RETURN_URL\" value=\"https://www.PaymentGateway.com/pgui/jsp/response.jsp\" class=\"input-box\">");
								sb.append("<input type=\"hidden\" name=\"HASH\" id=\"hashKey\">");
								// pay now
								sb.append("<div class=\"d-flex justify-content-center col-12\">");
									sb.append("<button type=\"submit\" class=\"btn btn-primary\" id=\"btn-submit\">");
									sb.append("Pay Now");
									//sb.append(custom.getFormDetailsPayButton());
									sb.append("</button>");
								sb.append("</div>");
							sb.append("</form>");
						sb.append("</div>");
						// Address block		
						sb.append("<div class=\"address-box pt-20 mt-20\">");
							sb.append("<div class=\"row\">");
								sb.append("<div class=\"col-sm-4 col-md-2\">");
									sb.append("<label class=\"address-label position-relative d-inline-block d-sm-block font-weight-medium pr-10 pr-xl-0 mb-0\">");
									//sb.append(custom.getContactAddress());
									sb.append("Address");
									sb.append("</label>");
								sb.append("</div>");
								sb.append("<div class=\"col-sm-8 col-md-10\">");
									sb.append("<address class=\"mb-0\">");
		
//									sb.append("<strong>The Art Of Sport</strong><br>" + 
//											" B-5/28, Safdarjung Enclave, Opp, Deer Park, Safdarjung Enclave,<br>" + 
//											" New Delhi, Delhi 110029<br>" + 
//											" <strong class=\"font-weight-medium\">Call Us (From 10:30 a.m - 6:30 p.m)</strong>");
									sb.append(custom.getContactAddress());
									sb.append("</address>");
								sb.append("</div>");
							sb.append("</div>");
							sb.append("<div class=\"row\">");
								sb.append("<div class=\"col-sm-4 col-md-2\">");
									sb.append("<label class=\"address-label position-relative d-inline-block d-sm-block font-weight-medium pr-10 pr-xl-0 mb-0\">");
									//sb.append(custom.getContactEmail());
									sb.append("Email");
									sb.append("</label>");
								sb.append("</div>");
								sb.append("<div class=\"col-sm-8 col-md-10 contact-mail\">");
				//					sb.append("contact@taos.in");
				//					sb.append(custom.getContactEmail());
				//					sb.append("contact@taos.in");
									for(String email : contactEmailArray) {
										sb.append("<a href=\"mailto:");
										sb.append(email);
										sb.append("\" class=\"text-primary\">");
										sb.append(email);
										sb.append("</a>");
//										if(contactEmailArray.length > 1) {
//											sb.append(" | ");
//										}
									}
									
								sb.append("</div>");
							sb.append("</div>"); //row
							sb.append("<div class=\"row\">");
								sb.append("<div class=\"col-sm-4 col-md-2\">");
									sb.append("<label class=\"address-label position-relative d-inline-block d-sm-block font-weight-medium pr-10 pr-xl-0 mb-0\">");
									//sb.append(custom.getContactPhone());
									sb.append("Phone");
									sb.append("</label>");
								sb.append("</div>");
								sb.append("<div class=\"col-sm-8 col-md-10 contact-number\">");
								for(String contactNumber : contactNumberArray) {
									sb.append("<a href=\"tel:0");
									sb.append(contactNumber);
									sb.append("\" class=\"text-primary\"> ");
									sb.append(contactNumber);
									sb.append("</a>");
//									if(contactNumberArray.length > 1) {
//										sb.append(" | ");
//									}
								}
//									sb.append("<a href=\"tel:0");
//									sb.append(custom.getContactPhone());
//									sb.append("\" class=\"text-primary\">+91 ");
//									sb.append(custom.getContactPhone());
//									sb.append("</a>");
								sb.append("</div>");
							sb.append("</div>"); //row
							sb.append("<div class=\"row\">");
								sb.append("<div class=\"col-sm-4 col-md-2\">");
									sb.append("<label class=\"address-label position-relative d-inline-block d-sm-block font-weight-medium pr-10 pr-xl-0 mb-0\">");
									sb.append("Website");
									sb.append("</label>");
								sb.append("</div>");
								sb.append("<div class=\"col-sm-8 col-md-10\">");
									sb.append("<a href=\"");
//									sb.append("www.amarjyotischool.co.in");
									sb.append(custom.getContactWebsite());
									sb.append("\" class=\"text-primary\" target=\"_blank\">");
//									sb.append("www.amarjyotischool.co.in");
									sb.append(custom.getContactWebsite());
								sb.append("</a></div>");
							sb.append("</div>"); //row
						sb.append("</div>");
					sb.append("</div>");
				sb.append("</div>");
			sb.append("</div>");
			sb.append("</section>");
			// footer block		
			sb.append("<footer class=\"bg-grey-lightest footer-bg-color box-shadow-primary border-top-grey-lightest py-20 position-relative z-index-2 font-size-14 text-center text-md-left\">");
			sb.append("<div class=\"container\">");
				sb.append("<div class=\"row\">");
					sb.append("<div class=\"col-md-4 mb-10 mb-md-0 d-flex flex-column\">");
						sb.append("<p class=\"mb-0\"><strong>");
							sb.append("All Domestic and International Cards Accepted");
						sb.append("</strong></p>");
						sb.append("<p class=\"mt-5 mb-0 font-weight-medium\">");
							sb.append("Associating Partner: AMI Merchants Services");
						sb.append("</p>");
						sb.append("<p class=\"mt-5 mb-0\">");
							sb.append("<span class=\"font-weight-medium\">");
								sb.append("Contact:");
							sb.append("</span>");
							sb.append("<a href=\"#\" class=\"text-primary\">");
							sb.append("<i class=\"fa fa-mobile-alt\"></i>");
								sb.append("+91 99999-04121");
							sb.append("</a>");
						sb.append("</p>");
					sb.append("</div>");
	//--------------					
					sb.append("<div class=\"col-md-8 text-md-right\">");
						sb.append("<p class=\"m-0\">Powered by <img src=\"../customPage/images/powered-by.png\" alt=\"\" height=\"16\" class=\"ml-5\"></p>");
						sb.append("<p class=\"m-0 footer-link\">");
	//					sb.append("<a href=\"termCondition.pdf\" target=\"_blank\" class=\"text-primary mr-10 d-inline-block\">");
							sb.append(custom.getFooterTnCLink());
	//					sb.append("</a> |");
//						sb.append(" <a href=\"privacyPlicy.pdf\" target=\"_blank\" class=\"text-primary ml-10 d-inline-block\">");
//							sb.append("Privacy Policy");
//						sb.append("</a>");
						sb.append("</p>");
						sb.append("<address class=\"mb-0 mt-5\">");
							sb.append("<strong class=\"font-weight-medium\">PaymentGateway Solution Pvt Ltd,</strong> 1F CS-06, Ansal Plaza, Vaishali, Ghaziabad- 201010");
						sb.append("</address>");
						sb.append("<p class=\"mb-0 mt-5\">");
							sb.append("<a href=\"tel:09167348029\" class=\"text-primary mr-10 d-inline-block\"><i class=\"fa fa-whatsapp text-success mr-5\"></i> +91 91673-48029</a>");
							sb.append("<a href=\"tel:01204344884\" class=\"text-primary mr-10 d-inline-block\"><i class=\"fa fa-phone rotate-90 mr-5\"></i> 0120 434 4884</a>");
							sb.append("<a href=\"mailto:support@paymentgateway.com\" class=\"text-primary d-inline-block\"><i class=\"fa fa-envelope mr-5\"></i> support@paymentgateway.com</a>");
						sb.append("</p>");
					sb.append("</div>");
				sb.append("</div>");
			sb.append("</div>");
			sb.append("</footer>");
			
			sb.append("<script src=\"js/jquery-3.4.1.min.js\"></script>");
			sb.append("<script src=\"js/jquery.validate.min.js\"></script>");
			sb.append("<script src=\"js/Sha256.js\"></script>");
			sb.append("<script src=\"js/decimalLimit1.js\"></script>");
			sb.append("<script src=\"js/common.js\"></script>");
			
			sb.append("<script src=\"../customPage/js/jquery-3.4.1.min.js\"></script>");
			sb.append("<script src=\"../customPage/js/jquery.validate.min.js\"></script>");
			sb.append("<script src=\"../customPage/js/Sha256.js\"></script>");
			sb.append("<script src=\"../customPage/js/decimalLimit.js\"></script>");
			sb.append("<script src=\"../customPage/js/common.js\"></script>");
			sb.append("<script src=\"../customPage/js/script.js\"></script>");
		
			sb.append("</body> </html>");
		
		return sb.toString();
		
	}
	
//	public String createScriptJS(CustomPage custom) {
//		StringBuilder JSString = new StringBuilder();
//		
//		if(custom.isAllowInvoiceFlag()) {
//		
//			// REMOVE ERROR MESSAGE LABEL
//			JSString.append("var removeLabelMsg = function(that) {\r\n" + 
//					"    var elementId = that.getAttribute(\"id\");\r\n" + 
//					"    var _parent = that.closest(\".form-group\");\r\n" + 
//					"    _parent.classList.remove(\"upload--success\");\r\n" + 
//					"    _parent.classList.remove(\"upload--error\");\r\n" + 
//					"    _querySelector('label[data-id=\"'+ elementId +'\"]').innerHTML = \"\";\r\n" + 
//					"}");
//			// DISPLAY ERROR MESSAGE LABEL
//			JSString.append("var displayLabelMsg = function(obj) {\r\n" + 
//					"    var elementId = obj.element.getAttribute(\"id\");\r\n" + 
//					"    var _parent = obj.element.closest(\".form-group\");\r\n" + 
//					"    _querySelector('label[data-id=\"'+ elementId +'\"]').innerHTML = obj.message;\r\n" + 
//					"\r\n" + 
//					"    if(obj.fileName !== \"\" && obj.fileName !== undefined) {\r\n" + 
//					"        _id(\"filename\").innerHTML = obj.fileName;\r\n" + 
//					"    } else {\r\n" + 
//					"        _id(\"filename\").innerHTML = \"No file selected.\";\r\n" + 
//					"    }\r\n" + 
//					"    obj.element.focus();\r\n" + 
//					"\r\n" + 
//					"    _parent.setAttribute(\"data-id\", obj.statusClass);\r\n" + 
//					"}");
//			// VALIDATE UPLOADED FILE EXTENSION AND SIZE
//			JSString.append("var validateFileUpload = function(that) {\r\n" + 
//					"    var _val = that.value;\r\n" + 
//					"        \r\n" + 
//					"    if(_val !== \"\") {\r\n" + 
//					"        var _files = that.files;\r\n" + 
//					"        var fileName = _files[0].name;\r\n" + 
//					"        var fileExtension = fileName.split('.').pop().toLowerCase();\r\n" + 
//					"        var _size = _files[0].size / 1024;\r\n" + 
//					"        var _maxSize = 1024 * 2;\r\n" + 
//					"\r\n" + 
//					"        if(_size > _maxSize) {\r\n" + 
//					"            displayLabelMsg({\r\n" + 
//					"                element: that,\r\n" + 
//					"                message: \"File too large. Upload less than \"+ _maxSize / 1024 +\" mb\",\r\n" + 
//					"                fileName: fileName,\r\n" + 
//					"                statusClass: \"upload--error\"\r\n" + 
//					"            });\r\n" + 
//					"            return false;\r\n" + 
//					"        } else if(fileExtension == \"\" \r\n" + 
//					"            || fileExtension == \"jpg\" \r\n" + 
//					"            || fileExtension == \"png\" \r\n" + 
//					"            || fileExtension == \"pdf\" \r\n" + 
//					"            || fileExtension == \"doc\" \r\n" + 
//					"            || fileExtension == \"docx\") {\r\n" + 
//					"                displayLabelMsg({\r\n" + 
//					"                    element: that,\r\n" + 
//					"                    message: \"Invoice has been uploaded!\",\r\n" + 
//					"                    fileName: fileName,\r\n" + 
//					"                    statusClass: \"upload--success\"\r\n" + 
//					"                });\r\n" + 
//					"            return true;\r\n" + 
//					"        } else {\r\n" + 
//					"            displayLabelMsg({\r\n" + 
//					"                element: that,\r\n" + 
//					"                message: \"Invalid file format\",\r\n" + 
//					"                fileName: fileName,\r\n" + 
//					"                statusClass: \"upload--error\"\r\n" + 
//					"            });\r\n" + 
//					"            return false;\r\n" + 
//					"        }\r\n" + 
//					"    } else {\r\n" + 
//					"        displayLabelMsg({\r\n" + 
//					"            element: that,\r\n" + 
//					"            message: \"Please upload invoice file\",\r\n" + 
//					"            fileName: fileName,\r\n" + 
//					"            statusClass: \"upload--error\"\r\n" + 
//					"        });\r\n" + 
//					"        return false;\r\n" + 
//					"    }\r\n" + 
//					"}");
//		}
//		
//			JSString.append("$(\"document\").ready(function() {");
//			JSString.append("$(\"#paymentDetail\").validate({");
//				JSString.append("rules: {");
//					JSString.append("CUST_ID: \"");
//						JSString.append("required");
//					JSString.append("\", ");
//					JSString.append("CUST_SHIP_NAME: \"");
//						JSString.append("required");
//					JSString.append("\", ");
//					JSString.append("CUST_PHONE: { ");
//						JSString.append("required: ");
//							JSString.append(true);
//						JSString.append(", minlength: 10,},");
//					JSString.append("CUST_EMAIL: {");
//						JSString.append("required: ");
//							JSString.append(true);
//						JSString.append(",email: true},");
//					JSString.append("TEMP_AMOUNT: \"");
//						JSString.append("required");
//				JSString.append("\"},");
//				
//				JSString.append("messages: {");
//					JSString.append("CUST_ID: \"");
//						JSString.append("Please enter booking reference");
//					JSString.append("\",CUST_SHIP_NAME: \"");
//						JSString.append("Please enter your name");
//					JSString.append("\",CUST_PHONE: {required: \"");
//						JSString.append("Please enter your phone number");
//					JSString.append("\"},CUST_EMAIL: {required: \"");
//						JSString.append("Please enter your email id");
//					JSString.append("\"},TEMP_AMOUNT: \"");
//						JSString.append("Please enter amount");
//				JSString.append("\"},");
//			
//			JSString.append("submitHandler: function(form) {updateOrderId();updateAmount();genrateHash(form);loaderAction(\"show\");$(\"#loaderText\").removeClass(\"d-none\");form.submit();}");
//			JSString.append("});");
//			JSString.append("});");
//		
//		return JSString.toString();
//	}
	
	public String createCSSStyle(CustomPage custom) {
		StringBuilder cssString = new StringBuilder();
		
		cssString.append("@media (min-width: 992px) {");
			cssString.append("body.isBgActive::after,");
			cssString.append("body.isBgActive::before {");
				cssString.append("content: \"\";");
				cssString.append("height: 100%;");
				cssString.append("right: 0;");
				cssString.append("top: 0;");
				cssString.append("width: 380px;");
				cssString.append("position: fixed;}");
			cssString.append("body.isBgActive::after {");
				cssString.append("background: url(\"../images/texture-01.png\");}");
		cssString.append("}");
		
		cssString.append("body { height: 100vh; }");
		cssString.append(".heading-border-bottom {position: relative;padding-bottom: 12px;}");
		cssString.append(".heading-border-bottom::after {height: 3px; width: 50px;background: var(--color-primary);content: \"\";position: absolute;bottom: 0;left: 0;}");
		cssString.append("input::-webkit-outer-spin-button,input::-webkit-inner-spin-button {-webkit-appearance: none;margin: 0;}");
		cssString.append("input[type=number] { -moz-appearance: textfield; }");
		cssString.append(".custom-control-inline { line-height: 22px; }.custom-control-inline:not(:last-child) { margin-right: 20px; }.custom-control-inline:last-child { margin-right: 0; }");
		cssString.append(".address-box .address-label::after {content: \":\";position: absolute;right: 0;}");
		cssString.append("label.error {color: var(--color-red);margin-bottom: 0px !important;font-size: 12px !important;position: absolute;bottom: -16px;}");
		cssString.append(".form-control.error {border-color: var(--color-red);}");
		cssString.append("#loaderText h3 {font-weight: 300;font-size: 21px; margin-bottom: 5px;}");
		
		cssString.append(".header-bg-color {background-color: ");
			cssString.append(custom.getHeaderBackgroundColor());
		cssString.append(" !important;}");
		
		cssString.append(".heading-style h1, .heading-style h2, .heading-style h3, .heading-style h4, .heading-style h5, .heading-style h6{color: ");
			cssString.append(custom.getHeadingColor()); //heasding color
		cssString.append(" !important;font-size: ");
			cssString.append(custom.getHeadingFontSize()); //heasding font size
		cssString.append(" !important}");
		
		cssString.append(".heading-style h1:after, .heading-style h2:after, .heading-style h3:after, .heading-style h4:after, .heading-style h5:after, .heading-style h6:after{background-color: ");
			cssString.append(custom.getHeadingBackgroundColor());
		cssString.append(" !important;}");//heading bg clr
		cssString.append(".footer-bg-color{background-color: ");
			cssString.append(custom.getFooterBackgroundColor());
		cssString.append(" !important;}");
		cssString.append(".form-bg-color{background-color: ");
			cssString.append(custom.getFormBackgroundColor());
		cssString.append(" !important;}");
		cssString.append(".section-body{color: ");
			cssString.append(custom.getParagraphColor());
		cssString.append(" !important;}");//paragraph color
		
		
		return cssString.toString();
	}
	
	public static void createZipOfAllDirectories(ZipOutputStream zos, File fileToZip, String parrentDirectoryName, File htmlFile, File[] TnCFiles) throws Exception {
	    if (fileToZip == null || !fileToZip.exists()) {
	        return;
	    }

	    String zipEntryName = fileToZip.getName();
	    if (parrentDirectoryName!=null && !parrentDirectoryName.isEmpty()) {
	        zipEntryName = parrentDirectoryName + "/" + fileToZip.getName();
	    }
	    
	    if(TnCFiles != null) {
	    	for(File tncpFile : TnCFiles) {
	    		createZipOfAllDirectories(zos, tncpFile, zipEntryName,null, null);
	    	}
	    }
	    
	    if(htmlFile != null) {
	    	createZipOfAllDirectories(zos, htmlFile, zipEntryName,null, null);
	    }
	    if (fileToZip.isDirectory()) {
	    	logger.error("Root Directory : " + zipEntryName);
	        for (File file : fileToZip.listFiles()) {
	        	createZipOfAllDirectories(zos, file, zipEntryName,null, null);
	        }
	    } else {
	    	logger.error("   File in Directory : " + zipEntryName);
	        byte[] buffer = new byte[1024];
	        FileInputStream fis = new FileInputStream(fileToZip);
	        zos.putNextEntry(new ZipEntry(zipEntryName));
	        int length;
	        while ((length = fis.read(buffer)) > 0) {
	            zos.write(buffer, 0, length);
	        }
	        zos.closeEntry();
	        fis.close();
	    }
	}
	
	public String uploadTnCFile(String filename, File file, String destPath) throws SystemException {
		
		File destFile = new File(destPath, filename);

		try {
			FileUtils.copyFile(file, destFile);

		} catch (Exception exception) {
			logger.error("Exception Occured in Uploading file", exception);
		}

		return "SUCCESS";

	}
}
