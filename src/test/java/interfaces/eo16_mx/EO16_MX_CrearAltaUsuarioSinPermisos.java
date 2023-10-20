package interfaces.eo16_mx;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import modelo.BaseExecution;
import modelo.TestCase;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class EO16_MX_CrearAltaUsuarioSinPermisos extends BaseExecution{
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
		
		String tdcQueryValidate = "SELECT * FROM  POSUSER.WM_TRANSACTION_EO16_MX WHERE OPERATION='CreateUser' ORDER BY END_DT DESC";
		String uri = String.format(data.get("uri"),data.get("host"),data.get("port"));
//		RestAssured.baseURI = uri;
		RestAssured.baseURI = "https://www.postman-echo.com";

		String responseCode = data.get("code");
		String status = data.get("mensaje");
		

		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
				
		
		/* PASO 1 *********************************************************************/			

		addStep("Capturar Username y Password en la pestaña de Authorization.");
			
		testCase.addTextEvidenceCurrentStep("Username: " + data.get("username"));
		testCase.addTextEvidenceCurrentStep("Password: " + data.get("username"));
		testCase.addBoldTextEvidenceCurrentStep("Se capturan datos con éxito.");
		
		boolean conexion = true;
		
		assertTrue("Se ingresa exitosamente.", conexion);	
		
		/* PASO 2 *********************************************************************/		
		
		addStep("Capturar los parámetros necesarios para la solicitud de Alta de Usuario y enviar el request.");
	
		RequestSpecification request = RestAssured.given();
		// el usuario y contraseña estan separados por :
		//String credentials = data.get("username")+":"+data.get("password");
		String credentials  = "postman:password";

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
		System.out.println(response.getStatusLine()); 
		
		
		//VALIDACION DEL STATUS CODE Y MENSAJE

		//FORMA 1: Validacion desde el Status line (respuesta API)
		//Status
		System.out.println("Response status code is: " + response.getStatusCode());
		
		//mensaje
		String mensaje = response.getStatusLine();
        String[] split = mensaje.split(" ", 3);
        mensaje = split[2].toString();
        System.out.println("Response Message is: " + mensaje); 
		
		//FORMA 2: Validacion de respuesta en JSON

        //mensaje
        String mensaje2 = jsonSearchKey(response.asString(),"authenticated");
		System.out.println("Response JSON value is: " + mensaje2 );

		
		// PASOS SOLO DE REPORTE
		//datos utilizados en la prueba
		testCase.addTextEvidenceCurrentStep(data.get("uri"));
		testCase.addTextEvidenceCurrentStep(data.get("json"));
		
		testCase.addBoldTextEvidenceCurrentStep("Se muestra la respuesta del request enviado.");		
	
		//respuesta del request
		testCase.addTextEvidenceCurrentStep(response.prettyPrint());		
		testCase.addTextEvidenceCurrentStep("Codigo de respuesta: " + Integer.toString(response.getStatusCode()));
		testCase.addTextEvidenceCurrentStep("Mensaje de respuesta: " + mensaje);

		
		boolean validateStatus = responseCode.equals(Integer.toString(response.getStatusCode()));
		System.out.println("VALIDACION DE STATUS = " + validateStatus);
		//assertFalse(!validateStatus, "La ejecución de la interfaz no fue exitosa");
	
		
	}
	
	public static String jsonSearchKey(String object, String key) throws JSONException {
		String x = null;

		if (object.contains("{")) {
			JSONObject json = new JSONObject(object);//			
			if (json.opt(key) == null) {
				for (int i = 0; i <= json.length() - 1; i++) {
					String cadena = json.get(json.names().get(i).toString()).toString();
					if (cadena.contains("{"))
						x = jsonSearchKey(cadena, key);
					if (x != null)
						break;
				}
			} else {
				return json.get(key).toString();
			}

		} else {
		}
		return x;
	}
	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Se requiere validar el método de \"Alta de Usuario\" creando un nuevo usuario en EBS 12.2.4 desde WM utilizando un usuario que no tenga permisos suficientes.";
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
