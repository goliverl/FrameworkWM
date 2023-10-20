package om;

import java.util.HashMap;
import modelo.TestCase;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;

public class PR24 {
	

	HashMap<String, String> data;
	TestCase testCase;
	SQLUtil db;
	
	public PR24(HashMap<String, String> data, TestCase testCase,SQLUtil db) {
		this.data = data;
		this.testCase = testCase;
		this.db = db;
		
		// utileria :::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("password"));
		String server = data.get("server");
	
	/// generacion de constructor 
		
	
	/// ingresar 
		

			
			u.get("http://"+user+":"+ps+"@"+server+":5555");
			
				
		
		
	}
	
	

}
