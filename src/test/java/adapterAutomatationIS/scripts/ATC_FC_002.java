package adapterAutomatationIS.scripts;

import java.util.HashMap;

import org.testng.annotations.Test;

import adapterAutomatationIS.om.IntegrationServerView;
import adapterAutomatationIS.om.IntegrationServerInicio;
import adapterAutomatationIS.om.IntegrationServerList;
import modelo.BaseExecution;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class ATC_FC_002 extends BaseExecution {
 
	
	
	@Test(dataProvider = "data-provider")
	public void test(HashMap<String, String> data) throws Exception {
		
		//utileria 
		
				SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
				IntegrationServerInicio si = new IntegrationServerInicio (u);
				IntegrationServerList   sl = new IntegrationServerList(u,testCase);
				IntegrationServerView sv = new IntegrationServerView(u);
				
				String user = data.get("user");
				String ps = PasswordUtil.decryptPassword( data.get("Password"));
				
				
	testCase.nextStep("cargar is ");
			
				
	System.out.println("http://"+user+":"+ps+"@10.184.80.20:5555");
	u.get("http://"+user+":"+ps+"@10.184.80.20:5555");
				
	
				
	testCase.passStep();
			
	testCase.nextStep("Se realiza el clic en el modulo de adapters ");
			
				u.hardWait(5);
				si.clickAdapter();
			
	testCase.passStep();
	
	testCase.nextStep("Dar click en la opción de VIEW");
	
				sl.clickView();
				
	
	testCase.passStep();
	
	testCase.nextStep("fin");
	u.wait(100);
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
		return "Se ejecutara el script el cual obtendrá la información de los adapters habilitados ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "Recolección de informacion de Adapters";
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
