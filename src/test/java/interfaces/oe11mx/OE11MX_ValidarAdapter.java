package interfaces.oe11mx;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.TPE_FAC;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;


public class OE11MX_ValidarAdapter  extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_OE11_MX_Valida_Adapter(HashMap<String, String> data) throws Exception {
		
		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCACQA, GlobalVariables.DB_USER_FCACQA, GlobalVariables.DB_PASSWORD_FCACQA);

		TPE_FAC facUtil = new TPE_FAC(data, testCase, db);
/**
* Variables ******************************************************************************************
* 
* 
*/			
		
		testCase.setProject_Name("Centralización Integral Cuentas por Pagar");
		testCase.setFullTestName("ATC_FT_001_OE11_MX_Valida_Adapter");		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 **************************	
		addStep("Ingresar al  Integration server http://"+data.get("server")+":5555/ "+data.get("QA")+" ");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		testCase.addScreenShotCurrentStep(u, "IS");
		testCase.addTextEvidenceCurrentStep("Se ingresa exitosamente.");
		   
		

//Paso 2 **************************	
		addStep("Dar clic en el menú Packages/Management");
		
		PakageManagment pok = new PakageManagment(u, testCase);
		String QA = data.get("QA");
		
//		if (QA == "INQA3") {
			// metodo para el server .15
			pok.clickpackage();
			testCase.addTextEvidenceCurrentStep("Se muestra la lista de Package.");
//		} 
//		else {
//			//metodo para los servers .13 y .14
//			pok.clickManagment();
//			testCase.addScreenShotCurrentStep(u, "selectionIter");
//		}
		
//Paso 3 **************************	
		addStep("Validar que se encuentre la interface "+data.get("interface")+"");
		boolean avinterfaz = false;
		String interfaceWM = data.get("interface");
		//Al ejecutar el metodo si tiene un error lo detiene y lo manda al catch
		try {
			pok.CXP(interfaceWM);
		}catch (Exception e) {//el catch cambia el valor del boleano para determinar que hubo un error
		 avinterfaz=true;
		}
		
		//si no se encuentra la interfaz no se agrega la imagen a la evidencia
		if(!avinterfaz) {
			testCase.addTextEvidenceCurrentStep("Se muestra la interfaz en el package list.");
		}
		assertFalse(avinterfaz, " - La interfaz"+data.get("interface")+" no se encuentra en el IS " );

		
//Paso 4 **************************	
		addStep("Dar clic sobre la Interfaz "+data.get("interface")+"");
		
		pok.CXPclic(interfaceWM);
		testCase.addTextEvidenceCurrentStep("Se muestra el detalle de cada uno de los servicios de la  Interface.");
		
//Paso 5 **************************
		addStep("Dar clic sobre Browse Services de la interfaz");
		
		pok.clickBrowserService();
		testCase.addScreenShotCurrentStep(u, "Browse Services");
		testCase.addTextEvidenceCurrentStep("Se muestra el listado servicios de la Interfaz.");
		
		
		u.close();
		
		
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

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar que la interface de envió de catálogos GL cuente con los adapters configurados correctamente" ;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "José Luis Flores";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_OE11_MX_Valida_Adapter";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
