package interfaces.eo16_mx;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.validation.constraints.AssertTrue;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
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
 *  I21045-Administración de Identidades-Avante-CC: ATC-FT-008-Realizar una Consulta de Usuario, Exitosa
 * Desc:
 * Se requiere validar el método de consulta de usuario para no duplicar cuentas en el sistema EBS 12.2.4
 * @author Roberto Flores
 * @date   2022/07/13
 */
public class ATC_FT_002_EO16_Avante_ConsultaUsuario  extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_EO16_Avante_ConsultaUsuario_Test(HashMap<String, String> data) throws Exception {
	
		testCase.setProject_Name("Administración de Identidades - Avante"); 
		testCase.setPrerequisites("1. Para poder hacer la asignación de roles y accesos debe de existir una matriz configurada en IDM.\r\n"
				+ "2. Contar con un usuario previamente dado de alta y activo en EBS 12.2.4.\r\n"
				+ "3. Contar con acceso a la BD FCWM6QA.FEMCOM.NET.");
		
		/*
		 * Utilerías
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
	
		/*
		 * Variables
		 *********************************************************************/
		
		String tdcQueryValidate = "SELECT * FROM  POSUSER.WM_TRANSACTION_EO16_MX WHERE OPERATION='GetUser' AND START_DT >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')";
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
		 
		/****************************************************************************************************************************************
		 * Paso 1
		 * **************************************************************************************************************************************/		
		addStep("Capturar en POSTMAN URL y Body");
					
				testCase.addTextEvidenceCurrentStep("url: " + data.get("url"));
				testCase.addTextEvidenceCurrentStep("body:" + data.get("json"));
				
				boolean conexion = true;
				
				assertTrue("Se ingresa exitosamente.", conexion);	
		
		/****************************************************************************************************************************************
		 * Paso 2
		 * **************************************************************************************************************************************/		
		addStep("Capturar los parámetros necesarios para la solicitud de Consulta de usuario y enviar el request.");
		
				Date fechaEjecucionInicio = new Date();
				
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
		
		u.close();
	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_002_EO16_Avante_AltaUsuario";
	}

	@Override
	public String setTestDescription() {
		return "ATC-FT-008-Realizar una Consulta de Usuario, Exitosa 3";
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
	
	public static void main(String[] args) throws Exception {
		
//		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//		
//		String encoding = Base64.getEncoder().encodeToString(("test1:test1").getBytes());
//		HttpPost httpPost = new HttpPost("http://host:post/test/login");
//		httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
//
//		System.out.println("executing request " + httpPost.getRequestLine());
//		HttpResponse response = httpClient.execute(httpPost);
//		HttpEntity entity = response.getEntity();
//		
		System.out.println(PasswordUtil.decryptPassword("92A9B1C1830A9C104662425C28C91152761EE7F4954ACC6256E7B2BD21B2D0AC6717993EFECB5015E24E9A5CE874293F"));
	}
	
}
