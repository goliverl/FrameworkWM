package interfaces.eo16_mx;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import io.restassured.response.Response;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.selenium.SeleniumUtil;
import utils.ApiMethodsUtil;

/**
 *  I21045-Administración de Identidades-Avante-CC: ATC-FT-013-Realizar una Actualización de Usuario, Permisos Insuficientes
 * Desc:
 * Se requiere validar el método de consulta de usuario para no duplicar cuentas en el sistema EBS 12.2.4
 * @author Luis Jasso
 * @date   2022/07/13
 */

public class ATC_FT_007_EO16_MX_AdmAvante_ActualizacionDeUsuarioPermisosInsuficentes extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_007_EO16_MX_AdmAvante_ActualizacionDeUsuarioPermisosInsuficentes_test(HashMap<String, String> data) throws Exception {
	
/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		//utils.sql.SQLUtil dbPosNueva = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);	

		
		testCase.setFullTestName(data.get("case"));
		testCase.setProject_Name("Administración de Identidades - Avante");    
				
/** VARIABLES *********************************************************************/	
		
		String tdcQueryValidate = "SELECT * FROM  POSUSER.WM_TRANSACTION_EO16_MX WHERE OPERATION='UpdUserData' ORDER BY END_DT DESC";

		String uri = String.format(data.get("uri"),data.get("host"),data.get("port"));
		ApiMethodsUtil api = new ApiMethodsUtil(uri);
		//RestAssured.baseURI = "https://www.postman-echo.com";
		String requestBody = data.get("json");
		String responseCode = data.get("code");
		String status = data.get("status");
		

		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
			
//		/****************************************************************************************************************************************
//		 * Paso 0
//		 * **************************************************************************************************************************************/
//		addStep("Comprobar status adapter");
//				 u = new SeleniumUtil(new ChromeTest(), true);
//				IntegrationServerUtil iu = new IntegrationServerUtil(u, data.get("IS_USER"), PasswordUtil.decryptPassword(data.get("IS_PASS")), data.get("IS_IP"));
//				iu.changeStatusAdapter(data.get("IS_ADAPTER_NAME"), data.get("IS_STATUS").equalsIgnoreCase("ON"));
//				testCase.addScreenShotCurrentStep(u, "Estatus adapter");
		
				
		
		/* PASO 1 *********************************************************************/			

		addStep("Capturar en POSTMAN Username y Password en la pestaña de Authorization.");
			
		testCase.addTextEvidenceCurrentStep("Username: " + data.get("username"));
		testCase.addTextEvidenceCurrentStep("Password: " + data.get("password"));
		testCase.addBoldTextEvidenceCurrentStep("Se capturan datos con éxito.");
		
		boolean conexion = true;
		
		assertTrue("Se ingresa exitosamente.", conexion);	
		
		/* PASO 2 *********************************************************************/		
		
		addStep("Capturar los parámetros necesarios para la solicitud de Actualizacion de usuario y enviar el request.");
		System.out.println(uri);
		Response request = api.postRequestMethod(uri, requestBody);
		System.out.println(request.asPrettyString());
		// PASOS SOLO DE REPORTE
		//datos utilizados en la prueba
		testCase.addBoldTextEvidenceCurrentStep("Request");
		testCase.addBoldTextEvidenceCurrentStep("Metodo: POST");
		testCase.addTextEvidenceCurrentStep(uri);
		testCase.addTextEvidenceCurrentStep(requestBody);
		
		testCase.addBoldTextEvidenceCurrentStep("Se muestra la respuesta del request enviado.");		
	
		//respuesta del request
		testCase.addBoldTextEvidenceCurrentStep("Response");
		testCase.addTextEvidenceCurrentStep("Status code: " + request.statusLine());
		testCase.addTextEvidenceCurrentStep(request.asPrettyString());	

		
		boolean validateStatus = responseCode.equals(Integer.toString(request.getStatusCode()));
		System.out.println("VALIDACION DE STATUS = " + validateStatus);
		assertFalse(!validateStatus, "La ejecución de la interfaz no fue exitosa");
	
		
	}
	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Se requiere validar el método de \"Actualizacion de Usuario\" en EBS 12.2.4 desde WM.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Dora Elia Reyes Obeso";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_007_EO16_MX_AdmAvante_ActualizacionDeUsuarioPermisosInsuficentes_tes";
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