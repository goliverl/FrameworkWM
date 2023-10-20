package test;

import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;

import io.restassured.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class Prueba {
	
	  Class<? extends Prueba> d = this.getClass();
	  String c = d.getName();

	public static void main(String[] args) throws JSONException {

//	    ArrayList<String> user = new ArrayList<String>();
//	    ArrayList<ArrayList<String>> user2 = new ArrayList<ArrayList<String>>();
//
//	    Class thisClass = new Object(){}.getClass();
//	    String className = thisClass.getEnclosingClass().getSimpleName();
//	    String methodName = thisClass.getEnclosingMethod().getName();
//	    
//	    user.add("hola");
//	    user.add("hola2");
//	    user.add("hola3");
//	    user.add("hola4");
//	    
//	    user2.add(user);
//		user.clear();
//		user.add("holarr");
//	    user.add("hola2");
//	    user.add("hola3");
//	    user.add("hola4");
//	    
//	    
	   
	    System.out.println(PasswordUtil.decryptPassword("DE1DE48F7BC10667961DF95A7AC53E2F"));
	  
	    
	
		//SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
	   // u.get("http://10.184.56.194:18080/ControlM/");

	    

	}

}