package adapterAutomatationIS.scripts;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.Test;

import adapterAutomatationIS.om.IntegrationServerView;
import adapterAutomatationIS.om.IntegrationServerInicio;
import adapterAutomatationIS.om.IntegrationServerList;
import modelo.BaseExecution;
import utils.EmailUtil;

import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
 
public class ATC_FT_001 extends BaseExecution {
	
	
	@Test(dataProvider = "data-provider")
	public void test(HashMap<String, String> data) throws Exception {
		
		//utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		IntegrationServerInicio si = new IntegrationServerInicio (u);
		IntegrationServerList   sl = new IntegrationServerList(u ,testCase);
		IntegrationServerView sd = new IntegrationServerView(u);
		EmailUtil mail = new EmailUtil();
		
 
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("password"));
		
		
		
	addStep("cargar is ");
	

	u.get("http://"+user+":"+ps+"@10.184.80.20:5555");
	

	
		testCase.addScreenShotCurrentStep(u, "carga is");
		
		
	testCase.passStep();
	
	testCase.nextStep("Se realiza el clic en el modulo de adapters ");
	
		u.hardWait(5);
		si.clickAdapter();
	
	testCase.passStep();
	
	testCase.nextStep("Se obtiene el error de los adapter que no esten habilitados ");
	
		HashMap<String, String> errors = sl.getErrors();
		System.out.println("errors");
		
		
	
		
		
	
		
		testCase.validateStep(errors.isEmpty());
		
	testCase.nextStep("fin");
	
	u.close();
	
	
	testCase.passStep();

		
		
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "El script buscara un adapter que este estatus NO y este " + 
				"  almacene el error mostrado";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "adapters";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}


















	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

}
