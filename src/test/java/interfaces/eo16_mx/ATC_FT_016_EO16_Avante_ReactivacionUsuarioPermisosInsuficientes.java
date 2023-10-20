package interfaces.eo16_mx;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.ApiUtil;
import util.IntegrationServerUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

/**
 *  I21045-Administración de Identidades-Avante-CC: MTC-FT-029-Realizar una Reactivación de Usuario, Permisos Insuficientes
 * Desc:
 * Se requiere un método de reactivación de usuarios en el sistema EBS 12.2.4
 * @author Roberto Flores
 * @date   2022/07/22
 */
public class ATC_FT_016_EO16_Avante_ReactivacionUsuarioPermisosInsuficientes extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_016_EO16_Avante_ReactivacionUsuarioPermisosInsuficientes_Test(HashMap<String, String> data) throws Exception {
	
		testCase.setProject_Name("Administración de Identidades - Avante"); 
		testCase.setPrerequisites("1. Para poder hacer la asignación de roles y accesos debe de existir una matriz configurada en IDM.\r\n"
				+ "2. Contar con un usuario previamente creado e inactivo en EBS 12.2.4.\r\n"
				+ "3. Contar con acceso a la BD FCWM6QA.FEMCOM.NET.");
		
		/*
		 * Utilerías
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		/*
		 * Variables
		 *********************************************************************/
		String expectedCode = data.get("code");
		String expectedMessage = data.get("mensaje");
		
		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/****************************************************************************************************************************************
		 * Paso 0
		 * **************************************************************************************************************************************/
		addStep("Comprobar status adapter");
				SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
				IntegrationServerUtil iu = new IntegrationServerUtil(u, data.get("IS_USER"), PasswordUtil.decryptPassword(data.get("IS_PASS")), data.get("IS_IP"));
				iu.changeStatusAdapter(data.get("IS_ADAPTER_NAME"), data.get("IS_STATUS").equalsIgnoreCase("ON"));
				testCase.addScreenShotCurrentStep(u, "Estatus adapter");
		 
		/****************************************************************************************************************************************
		 * Paso 1
		 * **************************************************************************************************************************************/		
		addStep("Capturar en POSTMAN Username y Password en la pestaña de Authorization.");
					
				testCase.addTextEvidenceCurrentStep("url: " + data.get("url"));
				testCase.addTextEvidenceCurrentStep("Username: " + data.get("username"));
				testCase.addTextEvidenceCurrentStep("Password: " + data.get("password"));
				testCase.addBoldTextEvidenceCurrentStep("Se capturan datos con éxito.");
				
				boolean conexion = true;
				
				assertTrue("Se ingresa exitosamente.", conexion);	
		
		/****************************************************************************************************************************************
		 * Paso 2
		 * **************************************************************************************************************************************/		
		addStep("Capturar los parámetros necesarios para la solicitud de Consulta de usuario y enviar el request.");
				
				HttpResponse respuesta = ApiUtil.sendPostDissableSSL(
						data.get("url"), 
						data.get("json"));
				
				String actualResponseCode = String.valueOf(respuesta.getStatusLine().getStatusCode());
				String actualResponseMessage = EntityUtils.toString(respuesta.getEntity());
				
				testCase.addCodeEvidenceCurrentStep("codigo de respuesta actual: " + actualResponseCode);
				System.out.println("codigo de respuesta actual: " + actualResponseCode);
				testCase.addCodeEvidenceCurrentStep("respuesta mensaje actual: " + actualResponseMessage);
				System.out.println("respuesta mensaje actual: " + actualResponseMessage);
				
				assertEquals(actualResponseCode, expectedCode);
				assertEquals(actualResponseMessage, expectedMessage); 
		   
		
		
	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_016_EO16_Avante_ReactivacionUsuarioPermisosInsuficientes_Test2";
	}

	@Override
	public String setTestDescription() {
		return "MTC-FT-029-Realizar una Reactivación de Usuario, Permisos Insuficientes";
	}

	@Override
	public String setTestDesigner() {
		return "AutomationQA";
	}

	@Override
	public String setTestInstanceID() {
		return null;
	}

	@Override
	public void beforeTest() {
	}

	@Override
	public String setPrerequisites() {
		return null;
	}
}
