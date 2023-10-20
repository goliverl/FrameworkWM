package interfaces.tpe_ftc;

import static org.testng.Assert.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import integrationServer.om.PakageManagment;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_002_TPE_FTC_TransaccionExitosadeActivacionTarjetaSpin extends BaseExecution {
	
	
	/**
	 * TPE_FTC: MTC-FT-063 Transaccion exitosa  de activacion de tarjeta SPIN
	 * Desc:
	 * Prueba de regresión para comprobar la no afectación en la funcionalidad principal 
	 * de activación de tarjetas SPIN de la interface TPE_FTC al ser migradas de WM9.9 a WM10.5.
	 * 
	 * @author Jose Onofre
	 * @date 02/22/2023
	 */
	
	
	private static final Object WmCodigo = null;
	private static final Object Wmfolio = null;
	private SQLUtil db;

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_TPE_FTC_transaccionexitosadeactivaciontarjetaspin_test(HashMap<String, String> data)
			throws Exception {

		/**
		 * UTILERIA
		 *********************************************************************/

		utils.sql.SQLUtil dbTran = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCACQA, GlobalVariables.DB_USER_FCACQA,
				GlobalVariables.DB_PASSWORD_FCACQA);
		// utils.sql.SQLUtil dbLog = new
		// utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA,GlobalVariables.DB_USER_FCWMLTAEQA,
		// GlobalVariables.DB_PASSWORD_FCWMLTAEQA);

		/**
		 * VARIABLES
		 *********************************************************************/
		String tdcPaso6 = " SELECT WM_CODE, ENTITY,OPERATION,PROC_CODE,CREATION_DATE\r\n"
				+ "FROM TPEUSER.CTR_SE_TRANSACTION \r\n"
				+ "WHERE CREATION_DATE>=TRUNC(SYSDATE)\r\n"
				+ "AND ROWNUM <= 10";

		String error = "SELECT error_id, folio, error_date, severity, error_type, error_code, description \n"
				+ "FROM WMLOG.WM_LOG_ERROR_TPE\n" + "WHERE TPE_TYPE LIKE = 'FTC' \n"
				+ "AND ERROR_DATE >= trunc(SYSDATE) \n" + "ORDER BY ERROR_DATE DESC";

		LocalDateTime now = LocalDateTime.now();
		String pvDate = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String adDate = pvDate.substring(0, 8);
		testCase.setTest_Description(data.get("id")+ data.get("descripcion"));
		String wmCodeToValidate = "100";
		String wmCodeToValidate2 = "101";
		String wmCodeToValidate3 = "100";
		String wmCodeToValidate4 = "000";
		String wmCodeToValidate5 = "101";
		
		SoftAssert softAssert = new SoftAssert();
		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		// **************************************Paso 1
		// *******************************************************************

		String uri = data.get("url");
		String param1 = data.get("param1");
		System.out.println("Paso 1: " + uri);

		String formParam1 = String.format(param1, pvDate, adDate);
		RestAssured.baseURI = uri;
		RequestSpecification requestFolio = RestAssured.given().queryParam("jsonIn", formParam1)
				.relaxedHTTPSValidation();
		requestFolio.log().method();
		requestFolio.log().uri();
		requestFolio.log().params();
		
		Response response = requestFolio.request(Method.POST);
		String responseBody = response.getBody().asPrettyString();
		
		JSONObject jsonResponseBody = new JSONObject(responseBody);
		JSONObject doc = jsonResponseBody.getJSONObject("TPEDoc");
		JSONObject header = doc.getJSONObject("header");
		JSONObject responseWmCode = doc.getJSONObject("response");
		JSONObject wmCodeJson = responseWmCode.getJSONObject("wmCode");
		String wmCode = wmCodeJson.getString("value");
		
		String folio = header.getString("folio");
		String creationDate = header.getString("creationDate");
		System.out.println("Status:" + response.statusLine());
		System.out.println("WmCode value:" + wmCode);
		System.out.println("Ahi va la respuesta: \n"+responseBody);
		
		softAssert.assertEquals(wmCode, wmCodeToValidate, "Paso 1: No se obtuvo el WmCode esperado");
		
		// Agregar request a la evidencia
		/**testCase.addBoldTextEvidenceCurrentStep("Request: ");
		testCase.addBoldTextEvidenceCurrentStep("Metodo: POST");
		testCase.addTextEvidenceCurrentStep(uri + "jsonIn=" + formParam1);
		// Agregar response a la evidencia
		testCase.addBoldTextEvidenceCurrentStep("Status");
		testCase.addBoldTextEvidenceCurrentStep(response.statusLine());
		testCase.addBoldTextEvidenceCurrentStep("Response: ");
		testCase.addTextEvidenceCurrentStep(responseBody.toString());
*/
		// *****************************************Paso 2
		// ************************************************************
		
		 
		String param2 = data.get("param2");
		System.out.println("Paso 2: \n"+uri);
		String formParam2 = String.format(param2, folio, pvDate, adDate, creationDate);
		RestAssured.baseURI = uri;
		RequestSpecification requestFolioimagen = RestAssured.given().queryParam("jsonIn", formParam2)
				.relaxedHTTPSValidation();
		requestFolioimagen.log().method();
		requestFolioimagen.log().uri();
		requestFolioimagen.log().params();
		
		Response response2 = requestFolioimagen.request(Method.POST);
		String responseBody2 = response2.getBody().asPrettyString();
		
		JSONObject jsonResponseBody2 = new JSONObject(responseBody2);	
		JSONObject doc2 = jsonResponseBody2.getJSONObject("TPEDoc");	
		//JSONObject header2 = doc2.getJSONObject("header");
		JSONObject responseWmCode2 = doc2.getJSONObject("response");
		JSONObject wmCodeJson2 = responseWmCode2.getJSONObject("wmCode");
		String wmCode2 = wmCodeJson2.getString("value");
		
		//String folio2 = header2.getString("folio");
		//String creationDate2 = header2.getString("creationDate");
		
		System.out.println("Status:" + response2.statusLine());
		System.out.println(responseBody2); 
		
		softAssert.assertEquals(wmCode2, wmCodeToValidate2, "Paso 2: No se obtuvo el WmCode esperado");
		
		// Agregar request a la evidencia
		/**
		testCase.addBoldTextEvidenceCurrentStep("Request: ");
		testCase.addBoldTextEvidenceCurrentStep("Metodo: POST");
		testCase.addTextEvidenceCurrentStep(uri + "jsonIn=" + formParam1); // Agregar
		// response a la evidencia testCase.addBoldTextEvidenceCurrentStep("Status");
		testCase.addBoldTextEvidenceCurrentStep(response.statusLine());
		testCase.addBoldTextEvidenceCurrentStep("Response: ");
		testCase.addTextEvidenceCurrentStep(responseBody.toString());

		 */
		//*****************************************Paso 3

		 String param3 = data.get("param3"); 
		 System.out.println("Paso 3: \n"+uri); 
		 String formParam3 = String.format(param3, pvDate, adDate); 
		 RestAssured.baseURI = uri; 
		 RequestSpecification requestFoliosolicitud = RestAssured.given().queryParam("jsonIn",formParam3).relaxedHTTPSValidation(); 
		 requestFoliosolicitud.log().method();
		 requestFoliosolicitud.log().uri(); 
		 requestFoliosolicitud.log().params();
		 
		 Response response3 = requestFoliosolicitud.request(Method.POST); 
		 String responseBody3 = response3.getBody().asPrettyString(); 
		 JSONObject jsonResponseBody3 = new JSONObject(responseBody3); 
		 JSONObject doc3 = jsonResponseBody3.getJSONObject("TPEDoc"); 
		 JSONObject header3 = doc3.getJSONObject("header"); 
		 JSONObject responseWmCode3 = doc3.getJSONObject("response");
		 JSONObject wmCodeJson3 = responseWmCode3.getJSONObject("wmCode");
		 String wmCode3 = wmCodeJson3.getString("value");
		 
		 String folio3 = header3.getString("folio");
		 String creationDate3 = header3.getString("creationDate");
		 
		 System.out.println("Status:" + response3.statusLine());
		 System.out.println(responseBody3); 
		 
		 softAssert.assertEquals(wmCode3, wmCodeToValidate3, "Paso 3: No se obtuvo el WmCode esperado");
		 /**
		 //Agregar request a la evidencia
		 testCase.addBoldTextEvidenceCurrentStep("Request: ");
		 testCase.addBoldTextEvidenceCurrentStep("Metodo: POST");
		 testCase.addTextEvidenceCurrentStep(uri + "jsonIn=" + formParam1); //Agregar
		 //response a la evidencia testCase.addBoldTextEvidenceCurrentStep("Status");
		 testCase.addBoldTextEvidenceCurrentStep(response.statusLine());
		 testCase.addBoldTextEvidenceCurrentStep("Response: ");
		 testCase.addTextEvidenceCurrentStep(responseBody.toString());
		  */
		  
		 
		 //*****************************************Paso 4

		 
			String param4 = data.get("param4");
			System.out.println("Paso 4: \n"+uri);
			String formParam4 = String.format(param4, folio, pvDate, adDate, creationDate);
			RestAssured.baseURI = uri;
			RequestSpecification requestFolioactivacion = RestAssured.given().queryParam("jsonIn", formParam4)
					.relaxedHTTPSValidation();
			requestFolioactivacion.log().method();
			requestFolioactivacion.log().uri();
			requestFolioactivacion.log().params();
			
			Response response4 = requestFolioactivacion.request(Method.POST);
			String responseBody4 = response4.getBody().asPrettyString();
			JSONObject jsonResponseBody4 = new JSONObject(responseBody4);
			JSONObject doc4 = jsonResponseBody4.getJSONObject("TPEDoc");
			JSONObject header4 = doc4.getJSONObject("header");
			JSONObject responseWmCode4 = doc4.getJSONObject("response");
			JSONObject wmCodeJson4 = responseWmCode4.getJSONObject("wmCode");
			String wmCode4 = wmCodeJson4.getString("value");
			
			
			String folio4 = header.getString("folio");
			String creationDate4 = header.getString("creationDate");
			
			
			System.out.println("Status:" + response4.statusLine());
			System.out.println(responseBody4); 
			
			softAssert.assertEquals(wmCode4, wmCodeToValidate4, "Paso 4: No se obtuvo el WmCode esperado");
			
			/**
			// Agregar request a la evidencia
			testCase.addBoldTextEvidenceCurrentStep("Request: ");
			testCase.addBoldTextEvidenceCurrentStep("Metodo: POST");
			testCase.addTextEvidenceCurrentStep(uri + "jsonIn=" + formParam1); // Agregar
			// response a la evidencia testCase.addBoldTextEvidenceCurrentStep("Status");
			testCase.addBoldTextEvidenceCurrentStep(response.statusLine());
			testCase.addBoldTextEvidenceCurrentStep("Response: ");
			testCase.addTextEvidenceCurrentStep(responseBody.toString());
		  */
		  
		  //*****************************************Paso 5

		
			String param5 = data.get("param5");
			System.out.println("Paso 5: \n"+uri);
			String formParam5 = String.format(param5, folio, pvDate, adDate, creationDate);
			RestAssured.baseURI = uri;
			RequestSpecification requestFolioack = RestAssured.given().queryParam("jsonIn", formParam5)
					.relaxedHTTPSValidation();
			requestFolioack.log().method();
			requestFolioack.log().uri();
			requestFolioack.log().params();
			
			Response response5 = requestFolioack.request(Method.POST);
			String responseBody5 = response5.getBody().asPrettyString();
			JSONObject jsonResponseBody5 = new JSONObject(responseBody5);
			JSONObject doc5 = jsonResponseBody5.getJSONObject("TPEDoc");
			JSONObject header5 = doc5.getJSONObject("header");
			JSONObject responseWmCode5 = doc5.getJSONObject("response");
			JSONObject wmCodeJson5 = responseWmCode5.getJSONObject("wmCode");
			String wmCode5 = wmCodeJson5.getString("value");
			
			String folio5 = header.getString("folio");
			String creationDate5 = header.getString("creationDate");
			
			System.out.println("Status:" + response5.statusLine());
			System.out.println(responseBody5); 
			
			softAssert.assertEquals(wmCode5, wmCodeToValidate5, "Paso 5: No se obtuvo el WmCode esperado");
			
			/**
			// Agregar request a la evidencia
			testCase.addBoldTextEvidenceCurrentStep("Request: ");
			testCase.addBoldTextEvidenceCurrentStep("Metodo: POST");
			testCase.addTextEvidenceCurrentStep(uri + "jsonIn=" + formParam1); // Agregar
			// response a la evidencia testCase.addBoldTextEvidenceCurrentStep("Status");
			testCase.addBoldTextEvidenceCurrentStep(response.statusLine());
			testCase.addBoldTextEvidenceCurrentStep("Response: ");
			testCase.addTextEvidenceCurrentStep(responseBody.toString());
		 */

		// ********************************** Paso 6

		 addStep("Validar que se encuntre la informacion en la base de datos");
		  
		 String wmcode ;
		  
		 String FormatoPaso6 = String.format(tdcPaso6, Wmfolio);
		  
		 System.out.println(FormatoPaso6);
		  
		 SQLResult Paso1 = executeQuery(dbTran, FormatoPaso6);
		 
		 boolean ValidaPaso1 = Paso1.isEmpty(); if (!ValidaPaso1) {
		 
		 wmcode= Paso1.getData(0, "WM_CODE");
		 
		 boolean validacionCode = wmcode.equals(WmCodigo);
		 
		 if(!validacionCode) {
		 
		 testCase.validateStep(validacionCode); }
		 
		 testCase.addQueryEvidenceCurrentStep(Paso1); }
		 
		 System.out.println(ValidaPaso1);
		 
		 assertFalse(ValidaPaso1, "No se obtiene información de la consulta");


	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "JoseO@Hexaware.com";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
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
