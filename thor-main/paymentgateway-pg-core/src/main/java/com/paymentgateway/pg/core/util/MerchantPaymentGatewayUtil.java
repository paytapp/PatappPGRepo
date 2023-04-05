package com.paymentgateway.pg.core.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.SystemConstants;

@Component
public class MerchantPaymentGatewayUtil {
	
	private static Logger logger =  LoggerFactory.getLogger(MerchantPaymentGatewayUtil.class.getName());
	public static final String ALGO = "AES";

	public static String encrypt(String data, String key, String iv) {
		try {
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGO);
			IvParameterSpec ivKey = new IvParameterSpec(iv.getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8));
			Cipher cipher = Cipher.getInstance(ALGO + "/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivKey);

			byte[] encValue = cipher.doFinal(data.getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8));

			Base64.Encoder base64Encoder = Base64.getEncoder().withoutPadding();
			String base64EncodedData = base64Encoder.encodeToString(encValue);
			System.out.println("encdata = " + base64EncodedData );
			base64EncodedData = URLEncoder.encode(base64EncodedData, SystemConstants.DEFAULT_ENCODING_UTF_8);
			return base64EncodedData;
		} catch (NoSuchAlgorithmException noSuchAlgorithmException) {

			noSuchAlgorithmException.printStackTrace();
		} catch (NoSuchPaddingException noSuchPaddingException) {

			noSuchPaddingException.printStackTrace();
		} catch (InvalidKeyException invalidKeyException) {

			invalidKeyException.printStackTrace();
		} catch (IllegalBlockSizeException illegalBlockSizeException) {

			illegalBlockSizeException.printStackTrace();
		} catch (BadPaddingException badPaddingException) {

			badPaddingException.printStackTrace();
		} catch (InvalidAlgorithmParameterException invalidAlgorithmParameterException) {

			invalidAlgorithmParameterException.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return null;
	} 

	public static String decrypt(String data, String key, String iv) {
		try {
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGO);
			IvParameterSpec ivKey = new IvParameterSpec(iv.getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8));
			Cipher cipher = Cipher.getInstance(ALGO + "/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivKey);
			byte[] decodedData = Base64.getDecoder().decode(data);
			byte[] decValue = cipher.doFinal(decodedData);

			return new String(decValue);


		} catch (NoSuchAlgorithmException noSuchAlgorithmException) {

			noSuchAlgorithmException.printStackTrace();
		} catch (NoSuchPaddingException noSuchPaddingException) {

			noSuchPaddingException.printStackTrace();
		} catch (InvalidKeyException invalidKeyException) {

			invalidKeyException.printStackTrace();
		} catch (IOException ioException) {

			ioException.printStackTrace();
		} catch (InvalidAlgorithmParameterException invalidAlgorithmParameterException) {

			invalidAlgorithmParameterException.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	
	
	public static String decryptPaymentGateway(String requestStringEnc, String key, String iv) throws SystemException{
		try {
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGO);
			IvParameterSpec ivKey = new IvParameterSpec(iv.getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8));
			Cipher cipher = Cipher.getInstance(ALGO + "/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, ivKey);			
			byte[] decValue = cipher.doFinal(Hex.decodeHex(requestStringEnc.toCharArray()));

			String decrypt =  new String(decValue);

			return decrypt;

		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException |
				InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | DecoderException decryptionException ) {
				logger.error("Error decrypting CRIS request" + decryptionException);
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,"Error decrypting cris request");
		}
	}
	
	public static String encryptPaymentGateway(String requestStringDec, String key, String iv) throws SystemException{
		try {
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGO);
			IvParameterSpec ivKey = new IvParameterSpec(iv.getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8));
			Cipher cipher = Cipher.getInstance(ALGO + "/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivKey);			
			
			byte[] encrypted = cipher.doFinal(requestStringDec.getBytes());
			String encryptedString = new String(Hex.encodeHex(encrypted));

            return encryptedString;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException |
				InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException  decryptionException ) {
				logger.error("Error decrypting CRIS request" , decryptionException);
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,"Error decrypting cris request");
		}
	}
	//test main method
	public static void main(String[] args) {
		/*String value= "merchantCode=1000180416134234|reservationId=1050180108163751|txnAmount=10.00|currencyType=INR|appCode=ET|pymtMode=Internet|txnDate=20180108|securityId=CRIS|RU=http://localhost:8080/pgui/jsp/response.jsp";
		//String[] splitted = value.split("\\|");
		String hexa = null;
		try {
			String checksum = Hasher.getHash(value);
			System.out.println(checksum);
		} catch (SystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String data = "merchantCode=1000180416134234|reservationId=LPAY1528434634125|txnAmount=100|currencyType=INR|appCode=ET|pymtMode=Internet|txnDate=20180108|securityId=CRIS|RU=http://localhost:8080/pgui/jsp/response.jsp|checkSum=3A2A6AEFCC25952E31818B62817AF805EB9CE289698911EE247F7ABB172CE5F8";
		String key= "ACME-1234ACME-12ACME-1234ACME-12";
		String iv="4e5Wa71fYoT7MFEX";
		String encrypt = encrypt(data, key, iv);
		System.out.println("Encrypted String= " + encrypt);
		try {
			 hexa = Hex.encodeHexString(encrypt.getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8));
			System.out.println("Hex= " + hexa);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	//	String dvalue = "lgPA6uRWJlLAlHOdJ5orxoxBwlyLteZ9v22W7IuN2bQ/ZPT5P6QCpoVaf4KAAyfyoi+XgtfaVnG3unXuADOGb3uRNPM/pwLbI8mNGH4DuBkmQwxa8d+2cPpRtrdbP2uNIHzJBmglNrpONtoEsxg6Ln/sx7bn9Gq+c7iZikI/g/MRYyKM5zioXRVfsZQ4GVgGVaqgrgUlnZbZFuDAYurJtOG7kR+QDK0jpQAwx3Gg8lA=lgPA6uRWJlLAlHOdJ5orxoxBwlyLteZ9v22W7IuN2bQ/ZPT5P6QCpoVaf4KAAyfyoi+XgtfaVnG3unXuADOGb3uRNPM/pwLbI8mNGH4DuBkmQwxa8d+2cPpRtrdbP2uNIHzJBmglNrpONtoEsxg6Ln/sx7bn9Gq+c7iZikI/g/MRYyKM5zioXRVfsZQ4GVgGVaqgrgUlnZbZFuDAYurJtOG7kR+QDK0jpQAwx3Gg8lA=";
	//	byte[] decode = null;
		try {
			String key= "ACME-1234ACME-12ACME-1234ACME-12";
			String iv="4e5Wa71fYoT7MFEX";
			String	decodeString=null;
		//	String hex= "2532423734692532466b6a387366387176253246675870555149666c6454444f4d616e747975534e4148315a524d68416c4c396f3636646a59774c59326d78466455435a684a366a56707866546c396e45754c733150654c435a457071784c713330376a2532466139494d7a6559253246555a6666786d635a795168253242433261706d545a4b4d6b3446536e64385a68613250313578456d7a66764e334a4652366c736a34664362457672666b543275496544516e774258737671324244614857597076253246532532426143654e4a626e253242646a6d576f79666954437a62735462646e6772635335346d50756c624f4741594373695351414a6e704c4367253242656553524579706b7763576f5731733756556c464d366f51314c53645a6d5543253242373051494c76394d387a7072447459787463614f7849396247454d48416e734e62574d31675557785647506473736e632532466d38335a304772517873387278374974785a53713042364233656d6b31694a377974503645674838";
			String inp = "merchantCode=1001380707182450|reservationId=130720181809240|bankTxnId=1011380717143644|txnAmount=1.23|currencyType=INR";
			//byte[] decodedData = hex.getBytes();
			String enc = encryptPaymentGateway(inp, key, iv);
				
				
			System.out.println("encrypted String= " + enc.toUpperCase());
		} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}

}
