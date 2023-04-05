package com.paymentgateway.commons.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 	@author Shiva
 */
@Service
public class Base64EncodeDecode {
	
	private static Logger logger = LoggerFactory.getLogger(Base64EncodeDecode.class.getName());
	
	public String base64Encoder(File file) {
        String base64File = "";
        try (FileInputStream imageInFile = new FileInputStream(file)) {
            byte fileData[] = new byte[(int) file.length()];
            imageInFile.read(fileData);
            base64File = Base64.getEncoder().encodeToString(fileData);
        } catch (FileNotFoundException e) {
            logger.error("Exception caught while encoding into Base64, " , e);
        } catch (IOException e) {
            logger.error("Exception caught while encoding into Base64, " , e);
        } catch (Exception e) {
            logger.error("Exception caught while encoding into Base64, " , e);
        }
        return base64File;
    }

 

    public static File decoder(String base64Image,String userFileName) {
    	String fileName=userFileName;
    	File outputFile = new File(fileName);
        try (FileOutputStream imageOutFile = new FileOutputStream(fileName)) {
            byte[] imageByteArray = Base64.getDecoder().decode(base64Image);
            
            FileUtils.writeByteArrayToFile(outputFile, imageByteArray);
            
        } catch (FileNotFoundException e) {
            logger.error("Exception caught while encoding into Base64, " , e);
        } catch (IOException e) {
            logger.error("Exception caught while encoding into Base64, " , e);
        } catch (Exception e) {
            logger.error("Exception caught while encoding into Base64, " , e);
        }
        return outputFile;
    }

}
