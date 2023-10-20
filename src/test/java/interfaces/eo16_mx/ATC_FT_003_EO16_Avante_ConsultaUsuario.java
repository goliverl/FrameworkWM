package interfaces.eo16_mx;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.validation.constraints.AssertTrue;

import org.apache.commons.codec.binary.Base64;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import modelo.BaseExecution;
import util.ApiUtil;
import util.GlobalVariables;
import util.IntegrationServerUtil;
import utils.ApiMethodsUtil;
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
public class ATC_FT_003_EO16_Avante_ConsultaUsuario  extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_EO16_Avante_ConsultaUsuario_Test(HashMap<String, String> data) throws Exception {
	
		testCase.setProject_Name("Administración de Identidades - Avante"); 
		testCase.setPrerequisites("1. Para poder hacer la asignación de roles y accesos debe de existir una matriz configurada en IDM.\r\n"
				+ "2. Contar con un usuario previamente dado de alta y activo en EBS 12.2.4.\r\n"
				+ "3. Contar con acceso a la BD FCWM6QA.FEMCOM.NET.");
		testCase.setTest_Description(data.get("desc"));
		
		/*
		 * Utilerías
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
	
		/*
		 * Variables
		 *********************************************************************/
		
		String tdcQueryValidate = "SELECT * FROM  POSUSER.WM_TRANSACTION_EO16_MX WHERE OPERATION='GetUser' AND creation_date >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')";

		String expectedCode = data.get("code");
		String expectedMessage = data.get("mensaje");
		
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
				assertEquals(actualResponseMessage, expectedMessage); 
				
				
//				RequestSpecification request = RestAssured.given();
//				// el usuario y contraseña estan separados por :
//				String credentials = data.get("username")+":"+data.get("password");
//				//String credentials  = "postman:password";
//		
//				// se usa la clase base64 para encriptar los datos de arriba y que no se vayan directo a la network
//				//transformandolo en bytes para encriptarla
//				byte [] encodedCredentials = Base64.encodeBase64(credentials.getBytes());
//				//pasar a string para pasarla al request header
//				String encodedCredentialsString = new String(encodedCredentials);
//				//pasar las credenciales al header de postman Authorization
//				request.header("Authorization","Basic "+encodedCredentialsString);
//				//Pasar el body en este caso vacio para el caso de prueba
//				String body = data.get("json");
//				//crear el request y mandarlo al server
//				request.header("Content-type", "application/json");
//				
//				Response response = request.body(body).get("");
//				System.out.println("Response status code is: " + response.getStatusCode());
//				response.prettyPrint();		
//				
//				// PASOS SOLO DE REPORTE
//				//datos utilizados en la prueba
//				testCase.addTextEvidenceCurrentStep(data.get("uri"));
//				testCase.addTextEvidenceCurrentStep(data.get("json"));
//				
//				testCase.addBoldTextEvidenceCurrentStep("Se muestra la respuesta del request enviado.");		
//			
//				//respuesta del request
//				testCase.addTextEvidenceCurrentStep(response.prettyPrint());		
//				testCase.addTextEvidenceCurrentStep("Status code: " + Integer.toString(response.getStatusCode()));		
		
				
//				boolean validateStatus = responseCode.equals(Integer.toString(response.getStatusCode()));
//				System.out.println("VALIDACION DE STATUS = " + validateStatus);
//				assertFalse(!validateStatus, "La ejecución de la interfaz no fue exitosa");
	
		
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
		return "ATC_FT_002_EO16_Avante_ConsultaUsuario_Test";
	}

	@Override
	public String setTestDescription() {
		return "ATC-FT-008-Realizar una Consulta de Usuario, Exitosa";
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
	
//	public static void main(String[] args) throws Exception {
//		String uri = "https://httpbin.org/post";
//		String json = "{\r\n"
//				+ "		\"NOMBRE_USUARIO\": \"IDMUSER01\"\r\n"
//				+ "}";
//		String user = "test";
//		String pass = "test";
//		
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//		
//		HttpResponse respuesta = ApiUtil.sendPostBasicAuth(
//				"https://httpbin.org/post", 
//				"{\r\n"
//				+ "		\"NOMBRE_USUARIO\": \"IDMUSER01\"\r\n"
//				+ "}",
//				"test", 
//				"test");
//		
//		String actualResponseCode = String.valueOf(respuesta.getStatusLine().getStatusCode());
//		String actualResponseMessage = EntityUtils.toString(respuesta.getEntity());
//		
//		System.out.println("codigo de respuesta actual: " + actualResponseCode);
//		System.out.println("respuesta mensaje actual: " + actualResponseMessage);
//		
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//		//Gil?
////		ApiMethodsUtil api = new ApiMethodsUtil(uri);
////		String credentials = user+":"+pass;
////		String body1 = json;
////		
////		String BasicAuth = String.valueOf(api.BasicAuthRequest(uri, credentials));
////		
////		Response JsonRequest= api.getRequestMethod(body1);
////		
////		String JsonResponse =String.valueOf(JsonRequest.asPrettyString());
////		String StatusCode = String.valueOf(JsonRequest.getStatusCode());
////		
////		System.out.println("JsonResponse: \n"+JsonResponse);
////		System.out.println("StatusCode: \n"+StatusCode);
//	
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//		//Luis
////		RequestSpecification request = RestAssured.given();
////		// el usuario y contraseña estan separados por :
////		String credentials = user+":"+pass;
////		//String credentials  = "postman:password";
////
////		// se usa la clase base64 para encriptar los datos de arriba y que no se vayan directo a la network
////		//transformandolo en bytes para encriptarla
////		byte [] encodedCredentials = Base64.encodeBase64(credentials.getBytes());
////		//pasar a string para pasarla al request header
////		String encodedCredentialsString = new String(encodedCredentials);
////		//pasar las credenciales al header de postman Authorization
////		request.header("Authorization","Basic "+encodedCredentialsString);
////		//Pasar el body en este caso vacio para el caso de prueba
////		String body = json;
////		//crear el request y mandarlo al server
////		request.header("Content-type", "application/json");
////		
////		Response response = request.body(body).get("/basic-auth");
////		System.out.println("Response status code is: " + response.getStatusCode());
////		response.prettyPrint();		
////		
////		// PASOS SOLO DE REPORTE
////		//datos utilizados en la prueba
////			
////	
////		//respuesta del request
////		System.out.println(response.prettyPrint());
////		System.out.println("Status code: " + Integer.toString(response.getStatusCode()));
//	
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//		//Oliver
////		RequestSpecification request = RestAssured.given().auth().basic(user, pass)
////				.header("Content-Type", "application/json").body(json);
////		request.log().uri();
////		request.log().method();
////		request.log().headers();
////		request.log().body();
////	
////		Response response = request.request(Method.POST);
////		
////		// PASOS SOLO DE REPORTE
////		//datos utilizados en la prueba
////						
////			
////		//respuesta del request
////		System.out.println("Status code: " + response.statusLine());
////		System.out.println(response.asPrettyString());	
//		
////		System.out.println(PasswordUtil.decryptPassword("A5544A354006EE384994CACA76D6289B"));
////		System.out.println(PasswordUtil.decryptPassword("92A9B1C1830A9C104662425C28C91152761EE7F4954ACC6256E7B2BD21B2D0AC6717993EFECB5015E24E9A5CE874293F"));
//		//QAVIEW
//		//10.184.48.217:1535/FCWM6QA.FEMCOM.NET
//		//Passw0rd#20
//		
//		//DB_HOST_FCWMQA_NUEVA - 10.184.48.216:1535/fcwm6QA.femcom.net
//		
//		
//	}
	
}
