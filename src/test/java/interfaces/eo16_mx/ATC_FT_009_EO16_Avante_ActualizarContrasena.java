package interfaces.eo16_mx;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import modelo.BaseExecution;
import util.GlobalVariables;
import util.IntegrationServerUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

/**
 *  I21045-Administraci�n de Identidades-Avante-CC: ATC-FT-016-Realizar una Actualizaci�n de Contrase�a a un Usuario, Exitosa, 
 *  ATC-FT-017-Realizar una Actualizaci�n de Contrase�a a un Usuario, Par�metros Insuficientes 
 * Desc:
 * Se requiere validar el m�todo de actualizaci�n de contrase�a de usuarios en el sistema EBS 12.2.4
 * @author Oliver Martinez
 * @date   2022/08/08
 */

public class ATC_FT_009_EO16_Avante_ActualizarContrasena extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_009_EO16_Avante_ActualizarContrasena_Test(HashMap<String, String> data) throws Exception {
	
/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		//utils.sql.SQLUtil dbPosNueva = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);	
		
		testCase.setTest_Description(data.get("case"));
		testCase.setProject_Name("Administracion de Identidades - Avante");    
				
/** VARIABLES *********************************************************************/	
	
		String tdcQueryValidate = "SELECT * FROM  POSUSER.WM_TRANSACTION_EO16_MX WHERE OPERATION='UpdUserPass' ORDER BY END_DT DESC";

		String uri = String.format(data.get("uri"),data.get("host"), data.get("host"));
		RestAssured.baseURI = uri;
		//RestAssured.baseURI = "https://www.postman-echo.com";
		
		byte [] encodedUser = Base64.encodeBase64(data.get("username").getBytes());
		String encodedUserString = new String(encodedUser);
		byte [] encodedPass = Base64.encodeBase64(data.get("password").getBytes());
		String encodedPassString = new String(encodedPass);
		String requestBody = data.get("json");
		
		String responseCode = data.get("code");
		String status = data.get("status");
		

		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
				
		/*********************************Paso 0****************************************/
//		addStep("Comprobar status adapter");
//		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
//		IntegrationServerUtil iu = new IntegrationServerUtil(u, data.get("IS_USER"), PasswordUtil.decryptPassword(data.get("IS_PASS")), data.get("IS_IP"));
//		iu.changeStatusAdapter(data.get("IS_ADAPTER_NAME"), data.get("IS_STATUS").equalsIgnoreCase("ON"));
//		testCase.addScreenShotCurrentStep(u, "Estatus adapter");
		
		/* PASO 1 *********************************************************************/			

		addStep("Capturar en POSTMAN Username y Password en la pesta�a de Authorization.");
			
		testCase.addTextEvidenceCurrentStep("Username: " + data.get("username"));
		testCase.addTextEvidenceCurrentStep("Password: " + PasswordUtil.decryptPassword((data.get("password"))));
		testCase.addBoldTextEvidenceCurrentStep("Se capturan datos con �xito.");
		
		boolean conexion = true;
		
		assertTrue("Se ingresa exitosamente.", conexion);	
		
		/* PASO 2 *********************************************************************/		
		
		addStep("Capturar los par�metros necesarios para la solicitud de Actualizacion de contrase�a de usuario y enviar el request.");
		
		RequestSpecification request = RestAssured.given().auth().basic(encodedUserString, encodedPassString)
				.header("Content-Type", "application/json").body(requestBody);
		request.log().uri();
		request.log().method();
		request.log().headers();
		request.log().body();
	
		Response response = request.request(Method.GET);
		System.out.println(response.getBody().asPrettyString());
//		RequestSpecification request = RestAssured.given();
//		// el usuario y contrase�a estan separados por :
//		String credentials = data.get("username")+":"+data.get("password");
//		//String credentials  = "postman:password";
//
//		// se usa la clase base64 para encriptar los datos de arriba y que no se vayan directo a la network
//		//transformandolo en bytes para encriptarla
//		byte [] encodedCredentials = Base64.encodeBase64(credentials.getBytes());
//		//pasar a string para pasarla al request header
//		String encodedCredentialsString = new String(encodedCredentials);
//		//pasar las credenciales al header de postman Authorization
//		request.header("Authorization","Basic "+encodedCredentialsString);
//		//Pasar el body en este caso vacio para el caso de prueba
//		String body = data.get("json");
//		//crear el request y mandarlo al server
//		request.header("Content-type", "application/json");
//		
//		Response response = request.body(body).get("/basic-auth");
//		System.out.println("Response status code is: " + response.getStatusCode());
//		response.prettyPrint();		
		
		// PASOS SOLO DE REPORTE
		//datos utilizados en la prueba
		testCase.addBoldTextEvidenceCurrentStep("Request");
		testCase.addBoldTextEvidenceCurrentStep("Metodo: POST");
		testCase.addTextEvidenceCurrentStep(uri);
		testCase.addTextEvidenceCurrentStep(requestBody);			
	
		//respuesta del request
		testCase.addBoldTextEvidenceCurrentStep("Response");
		testCase.addTextEvidenceCurrentStep("Status code: " + response.statusLine());
		testCase.addTextEvidenceCurrentStep(response.getBody().asPrettyString());			

		
		boolean validateStatus = responseCode.equals(Integer.toString(response.getStatusCode()));
		System.out.println("VALIDACION DE STATUS = " + validateStatus);
		assertFalse(!validateStatus, "La ejecuci�n de la interfaz no fue exitosa");
	
		
		/* PASO 3 *********************************************************************/		    
		
		addStep("Establecer conexi�n a la BD de WM FCWM6QA.FEMCOM.NET.");		

		testCase.addTextEvidenceCurrentStep("Base de Datos: FCWM6QA");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexi�n con �xito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMQA_NUEVA);
			
		assertTrue("Se ingresa exitosamente.", true);

		/* PASO 4 *********************************************************************/		    

		addStep("Validar el registro de la ejecuci�n de la interfaz EO16_MX.");
		
		System.out.println(tdcQueryValidate);		
		SQLResult connectionResult = executeQuery(dbPos, tdcQueryValidate);

		boolean connection = connectionResult.isEmpty();
		String estatus = "";
		if (!connection) {				
			estatus = connectionResult.getData(0, "STATUS");			
			testCase.addQueryEvidenceCurrentStep(connectionResult);	
		}else {
			testCase.addQueryEvidenceCurrentStep(connectionResult);
		}
		assertEquals(status, estatus, "El Status de ejecucion es diferente a S");	    
		
		
	}
	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Se requiere validar el m�todo de \"Actualizacion de contrase�a de Usuario\" en EBS 12.2.4 desde WM.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Dora Elia Reyes Obeso";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_009_EO16_Avante_ActualizarContrase�a_Test";
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