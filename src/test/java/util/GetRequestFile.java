package util;

import java.io.IOException;
import java.util.HashMap;

import exceptions.ReempRequestException;
import utils.webmethods.ReadRequest;
 
public class GetRequestFile {

	public static final String BASE_PATH = System.getProperty("user.dir") + "\\Interfaces\\";
	
	public static String getRequestFile(String path, HashMap<String, String> reempData) throws ReempRequestException {
		
		String fullPath = BASE_PATH + path;
		
		String req = null;
		try {
			req = ReadRequest.replaceDataRequestFile(fullPath, reempData);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		
		return req;
	}
	
}
