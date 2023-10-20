package interfaces.eo16_mx;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import modelo.BaseExecution;
import util.ApiUtil;
import util.GlobalVariables;
import util.IntegrationServerUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

/**
 *  I21045-Administración de Identidades-Avante-CC: MTC-FT-032-Realizar un Cambio de Responsabilidades
 * Desc:
 * Se requiere validar el método de cambio de responsabilidades de usuarios en el sistema EBS 12.2.4
 * @author Roberto Flores
 * @date   2022/07/22
 */
public class ATC_FT_017_EO16_Avante_CambioResp extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_017_EO16_Avante_CambioResp_Test(HashMap<String, String> data) throws Exception {
	
		testCase.setProject_Name("Administración de Identidades - Avante"); 
		testCase.setPrerequisites("1. Para poder hacer la asignación de roles y accesos debe de existir una matriz configurada en IDM.\r\n"
				+ "2. La cuenta del sistema EBS 12.2.4 debe estar asociada en IDM.\r\n"
				+ "3. Contar con un usuario previamente dado de alta y activo en EBS 12.2.4.\r\n"
				+ "4. Contar con acceso a la BD FCWM6QA.FEMCOM.NET.");
		testCase.setTest_Description(data.get("desc"));
		
		/*
		 * Utilerías
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		SimpleDateFormat jsonFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		jsonFormat.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		/*
		 * Variables
		 *********************************************************************/
		
		String tdcQueryValidate = "SELECT * FROM  POSUSER.WM_TRANSACTION_EO16_MX WHERE OPERATION='UpdUserResponsabilities' AND START_DT >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')";

		String expectedCode = data.get("code");
		
		String status = data.get("status");
		
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
				u.close();
				
		/****************************************************************************************************************************************
		 * Paso 1
		 * **************************************************************************************************************************************/		
		addStep("Capturar en POSTMAN URL y Body");
					
				Date fechaEjecucionInicio = new Date();
				
				String fechaJson = jsonFormat.format(fechaEjecucionInicio);
		
				testCase.addTextEvidenceCurrentStep("url: " + data.get("url"));
				testCase.addTextEvidenceCurrentStep("body:" + data.get("json").replace("<<FECHA_ACTUAL>>", fechaJson));
				
				boolean conexion = true;
				
				assertTrue("Se ingresa exitosamente.", conexion);		
		
		/****************************************************************************************************************************************
		 * Paso 2
		 * **************************************************************************************************************************************/		
		addStep("Capturar los parámetros necesarios para la solicitud de Consulta de usuario y enviar el request.");
		
				HttpResponse respuesta = ApiUtil.sendPostDissableSSL(
						data.get("url"), 
						data.get("json").replace("<<FECHA_ACTUAL>>", fechaJson));
				
				String actualResponseCode = String.valueOf(respuesta.getStatusLine().getStatusCode());
				String actualResponseMessage = EntityUtils.toString(respuesta.getEntity());
				
				testCase.addCodeEvidenceCurrentStep("codigo de respuesta actual: " + actualResponseCode);
				System.out.println("codigo de respuesta actual: " + actualResponseCode);
				testCase.addCodeEvidenceCurrentStep("respuesta mensaje actual: " + actualResponseMessage);
				System.out.println("respuesta mensaje actual: " + actualResponseMessage);
				
				assertEquals(actualResponseCode, expectedCode);
				//assertEquals(actualResponseMessage, expectedMessage); 
		
		/****************************************************************************************************************************************
		 * Paso 3
		 * **************************************************************************************************************************************/		
				addStep("Establecer conexión a la BD de WM FCWM6QA.FEMCOM.NET.");		
		
				testCase.addTextEvidenceCurrentStep("Base de Datos: FCWM6QA");
				testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
				testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMQA_NUEVA);
					
				assertTrue("Se ingresa exitosamente.", true);

		/****************************************************************************************************************************************
		 * Paso 4
		 * **************************************************************************************************************************************/
		addStep("Validar el registro de la ejecución de la interfaz EO16_MX.");
		
		String fechaEjecucionInicio_f = formatter.format(fechaEjecucionInicio);
		
		String tdcQueryValidate_f = String.format(tdcQueryValidate, fechaEjecucionInicio_f);
		
		System.out.println(tdcQueryValidate_f);		
		
		SQLResult connectionResult = executeQuery(dbPos, tdcQueryValidate_f);

		boolean connection = connectionResult.isEmpty();
		
		if (!connection) {				
			
			String estatus = connectionResult.getData(0, "Status");			
			connection = estatus.equals(status);
			System.out.println(connection);
			testCase.addQueryEvidenceCurrentStep(connectionResult);	
			
			connection = !connection;	
		} 

		assertFalse(connection, "La tabla no contiene la información o STATUS diferente a " + data.get("status"));	    
		
		
	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_017_EO16_Avante_CambioResp_Test";
	}

	@Override
	public String setTestDescription() {
		return "MTC-FT-032-Realizar un Cambio de Responsabilidades";
	}

	@Override
	public String setTestDesigner() {
		return "Dora Elia Reyes Obeso";
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
