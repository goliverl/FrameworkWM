package integrationServer.scripts;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class scriptsIS  extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void test(HashMap<String, String> data) throws Exception {
	//utileria 
	SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
	PakageManagment pok = new PakageManagment(u, testCase);
	 
	String user = data.get("user");
	String ps = PasswordUtil.decryptPassword( data.get("ps"));
	String server = data.get("server");
	String con ="http://"+user+":"+ps+"@"+server;
	
	// cargar el integration server 
	
	addStep("Se iniciala la carga de integration Server");
	
	String contra =   "http://"+user+":"+ps+"@"+server+":5555";


	u.get(contra);
	
//	pok.clickManagment();
//	
////	pok.selectInterface(data.get(""));
//	pok.selectInterface("FEMSA_PR35");
//	pok.clickBrowserService();
////	pok.selectService(data.get(""));
//	pok.selectService("PR35.Pub:run");
//	pok.clickTestRun();
//	pok.clickSelectNoInput();
	
	pok.runIntefaceWM("FEMSA_PR35","PR35.Pub:run", null);
	
	
	}
	
	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
