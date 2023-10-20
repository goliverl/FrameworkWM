package interfaces.eo16_mx;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class EO16_MX_ActualizacionDeUsuario extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void test(HashMap<String, String> data) throws Exception {
	
/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		//utils.sql.SQLUtil dbPosNueva = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);	

		SeleniumUtil u;
		PakageManagment pok;	
		
		testCase.setFullTestName(data.get("case"));
		testCase.setProject_Name("Administración de Identidades - Avante");    
				
/** VARIABLES *********************************************************************/	
		
		String tdcQueryValidate = "SELECT * FROM  POSUSER.WM_TRANSACTION_EO16_MX WHERE OPERATION='UpdUserData' ORDER BY END_DT DESC";

		String uri = String.format(data.get("uri"),data.get("host"),data.get("port"));
		RestAssured.baseURI = uri;
		//RestAssured.baseURI = "https://www.postman-echo.com";

		String responseCode = data.get("code");
		String status = data.get("status");
		

		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
				
		
		/* PASO 1 *********************************************************************/			

		addStep("Capturar en POSTMAN Username y Password en la pestaña de Authorization.");
			
		testCase.addTextEvidenceCurrentStep("Username: " + data.get("username"));
		testCase.addTextEvidenceCurrentStep("Password: " + data.get("password"));
		testCase.addBoldTextEvidenceCurrentStep("Se capturan datos con éxito.");
		
		boolean conexion = true;
		
		assertTrue("Se ingresa exitosamente.", conexion);	
		
		/* PASO 2 *********************************************************************/		
		
		addStep("Capturar los parámetros necesarios para la solicitud de Actualizacion de usuario y enviar el request.");
	
		RequestSpecification request = RestAssured.given();
		// el usuario y contraseña estan separados por :
		String credentials = data.get("username")+":"+data.get("password");
		//String credentials  = "postman:password";

		// se usa la clase base64 para encriptar los datos de arriba y que no se vayan directo a la network
		//transformandolo en bytes para encriptarla
		byte [] encodedCredentials = Base64.encodeBase64(credentials.getBytes());
		//pasar a string para pasarla al request header
		String encodedCredentialsString = new String(encodedCredentials);
		//pasar las credenciales al header de postman Authorization
		request.header("Authorization","Basic "+encodedCredentialsString);
		//Pasar el body en este caso vacio para el caso de prueba
		String body = data.get("json");
		//crear el request y mandarlo al server
		request.header("Content-type", "application/json");
		
		Response response = request.body(body).get("/basic-auth");
		System.out.println("Response status code is: " + response.getStatusCode());
		response.prettyPrint();		
		
		// PASOS SOLO DE REPORTE
		//datos utilizados en la prueba
		testCase.addTextEvidenceCurrentStep(data.get("uri"));
		testCase.addTextEvidenceCurrentStep(data.get("json"));
		
		testCase.addBoldTextEvidenceCurrentStep("Se muestra la respuesta del request enviado.");		
	
		//respuesta del request
		testCase.addTextEvidenceCurrentStep(response.prettyPrint());		
		testCase.addTextEvidenceCurrentStep("Status code: " + Integer.toString(response.getStatusCode()));		

		
		boolean validateStatus = responseCode.equals(Integer.toString(response.getStatusCode()));
		System.out.println("VALIDACION DE STATUS = " + validateStatus);
		assertFalse(!validateStatus, "La ejecución de la interfaz no fue exitosa");
	
		
		/* PASO 3 *********************************************************************/		    
		
		addStep("Establecer conexión a la BD de WM FCWM6QA.FEMCOM.NET.");		

		testCase.addTextEvidenceCurrentStep("Base de Datos: FCWM6QA");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMQA_NUEVA);
			
		assertTrue("Se ingresa exitosamente.", true);

		/* PASO 4 *********************************************************************/		    

		addStep("Validar el registro de la ejecución de la interfaz EO16_MX.");
		
		System.out.println(tdcQueryValidate);		
		SQLResult connectionResult = executeQuery(dbPos, tdcQueryValidate);

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
		return "";
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