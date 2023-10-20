package interfaces.eo16_mx;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import io.restassured.response.Response;
import modelo.BaseExecution;
import util.GlobalVariables;
import util.IntegrationServerUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.ApiMethodsUtil;

public class ATC_FT_006_EO16_MX_AdmAvante_ActualizacionDeUsuarioExepcionDeServicio extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_006_EO16_MX_AdmAvante_ActualizacionDeUsuarioExepcionDeServicio_test (HashMap <String, String> data) throws Exception{
		/*
		 * Utileria*********************************************************************/
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		
		/*
		 * Variables********************************************************************/
		String queryEjecucion = "SELECT * \r\n"
				+ "FROM  TPEUSER.WM_TRANSACTION_EO16_MX \r\n"
				+ "WHERE OPERATION='UpdUserPass' \r\n"
				+ "ORDER BY END_DT DESC";
		
		String queryWMerror = "SELECT * \r\n"
				+ "FROM  WMLOG.WM_LOG_ERROR_TPE \r\n"
				+ "WHERE FOLIO= '%s' \r\n"
				+ "AND TPE_TYPE='EO16_MX' \r\n"
				+ "ORDER BY ERROR_DATE DESC";
		
		String uri = String.format(data.get("uri"), data.get("host"), data.get("port"));
		ApiMethodsUtil api = new ApiMethodsUtil(uri);
		//RestAssured.baseURI = "https://www.postman-echo.com";
		String requestBody = data.get("json");
		
		String responseCode = data.get("code");
		String statusToValidate = data.get("status");
		
		
		
	/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
		
//		/****************************************************************************************************************************************
//		 * Paso 0
//		 * **************************************************************************************************************************************/
//		addStep("Comprobar status adapter");
//				 u = new SeleniumUtil(new ChromeTest(), true);
//				IntegrationServerUtil iu = new IntegrationServerUtil(u, data.get("IS_USER"), PasswordUtil.decryptPassword(data.get("IS_PASS")), data.get("IS_IP"));
//				iu.changeStatusAdapter(data.get("IS_ADAPTER_NAME"), data.get("IS_STATUS").equalsIgnoreCase("ON"));
//				testCase.addScreenShotCurrentStep(u, "Estatus adapter");
//		 			
		
		/*********************************Paso 1****************************************/
		addStep("Comprobar status adapter");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		IntegrationServerUtil iu = new IntegrationServerUtil(u, data.get("IS_USER"), PasswordUtil.decryptPassword(data.get("IS_PASS")), data.get("IS_IP"));
		iu.changeStatusAdapter(data.get("IS_ADAPTER_NAME"), data.get("IS_STATUS").equalsIgnoreCase("ON"));
		testCase.addScreenShotCurrentStep(u, "Estatus adapter");
		
		/*********************************Paso 2****************************************/
		addStep("Capturar en POSTMAN Username y Password en la pestaña de Authorization.");
		
		testCase.addTextEvidenceCurrentStep("Username: " + data.get("username"));
		testCase.addTextEvidenceCurrentStep("Password: " + data.get("password"));
		testCase.addBoldTextEvidenceCurrentStep("Se capturan datos con exito");
		
		boolean conexion = true;
		
		assertTrue("Se ingresa exitosamente.", conexion);
		/*********************************Paso 3****************************************/
		addStep("Irse a la pestaña de Body y capturar los parametros necesarios para la solicitud de Actualizacion de Contraseña.");
		
		Response request = api.postRequestMethod(uri, requestBody);
		System.out.println(request);
		
		
		// PASOS SOLO DE REPORTE
		//datos utilizados en la prueba
		testCase.addBoldTextEvidenceCurrentStep("Request");
		testCase.addBoldTextEvidenceCurrentStep("Metodo: POST");
		testCase.addTextEvidenceCurrentStep(uri);
		testCase.addTextEvidenceCurrentStep(requestBody);			
			
		//respuesta del request
		testCase.addBoldTextEvidenceCurrentStep("Response");
		testCase.addTextEvidenceCurrentStep("Status code: " + request.statusLine());
		testCase.addTextEvidenceCurrentStep(request.asPrettyString());			
		
		boolean validateStatus = responseCode.equals(Integer.toString(request.getStatusCode()));
		System.out.println("VALIDACION DE STATUS = " + validateStatus);
		assertFalse(!validateStatus, "La ejecuciï¿½n de la interfaz no fue exitosa");
		
		/*********************************Paso 4****************************************/
		addStep("Ejecutar la siguiente sentencia para validar el registro de la ejecuciï¿½n de la interfaz EO16_MX "
				+ "en  la BD de WM FCWM6QA.FEMCOM.NET.");
		
		SQLResult executionRegister = dbPos.executeQuery(queryEjecucion);
		System.out.println(queryEjecucion);
		
		boolean validateExecutionRegister = executionRegister.isEmpty();
		String status = "";
		String folio = "";
		if (!validateExecutionRegister) {
			status = executionRegister.getData(0, "STATUS");
			folio = executionRegister.getData(0, "FOLIO");
			testCase.addQueryEvidenceCurrentStep(executionRegister);
		}else {
			testCase.addQueryEvidenceCurrentStep(executionRegister);
		}
		assertEquals(statusToValidate, status, "El estatus de ejecucion es diferente a E");
		/****************************************Paso 5***********************************/
		addStep("Ejecutar la siguiente sentencia para validar el registro del log del error de desconexiï¿½n de WM para el "
				+ "cï¿½digo obtenido 500 en  la BD de WM FCWM6QA.FEMCOM.NET.");
		String WMerrorFormat = String.format(queryWMerror, folio);
		System.out.println(WMerrorFormat);
		SQLResult WMerror = dbPos.executeQuery(WMerrorFormat);
		
		boolean validateWMerror = WMerror.isEmpty();
		if(!validateWMerror) {
			testCase.addQueryEvidenceCurrentStep(WMerror);
		}else {
			testCase.addQueryEvidenceCurrentStep(WMerror);
		}
		assertFalse(validateWMerror, "No se obtuvieron registros del Log de error");
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
		return "ATC-FT-018-Realizar una Actualizacion de Contraseña a un Usuario, Excepcion en el servicio";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Dora Elia Reyes Obeso";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_003_EO16_Avante_ActualizarContraseñaExcepcion";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
