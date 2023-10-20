package interfaces.tpe_ftc;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import modelo.BaseExecution;
import util.GlobalVariables;
//import utils.ApiMethodsUtil;
//import utils.webmethods.GetRequest;
//import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ATC_FT_001_TPE_FTC_VinculacionExitosaTarjetaSPIN extends BaseExecution{
	
	/**
	 * TPE_FTC: MTC-FT-064 Transaccion exitosa de vinculacion de tarjeta SPIN
	 * Desc:
	 * Prueba de regresión para comprobar la no afectación en la funcionalidad principal
	 *  de vinculación de tarjetas SPIN de la interface FEMSA_TPE_FTC 
	 *  al ser migradas de WM9.9 a WM10.5
	 * 
	 * @author Jose Onofre
	 * @date 02/22/2023
	 */
	
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_TPE_FTC_VinculacionExitosaTarjetaSPIN_test(HashMap <String, String> data) throws Exception {
		/*
		 * Utileria*************************************************************************/
		utils.sql.SQLUtil dbFCACQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCACQA, GlobalVariables.DB_USER_FCACQA, GlobalVariables.DB_PASSWORD_FCACQA);
		utils.sql.SQLUtil dbFCWMLTAEQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAQ_MTY);
		
		/*
		 * Variables************************************************************************/
		
		String queryTransaction = "SELECT APPLICATION, ENTITY, OPERATION, FOLIO, PLAZA, TIENDA, WM_CODE, WM_DESC\r\n"
				+ "FROM TPEUSER.CTR_SE_TRANSACTION\r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE) AND FOLIO='%s'";
		
		String queryError = "SELECT ERROR_ID,FOLIO,ERROR_DATE,SEVERITY,ERROR_TYPE,ERROR_CODE "
				+ "FROM WMLOG.WM_LOG_ERROR_TPE \r\n"
				+ "WHERE  TPE_TYPE LIKE '%FTC%' \r\n"
				+ "AND  ERROR_DATE >= TRUNC(SYSDATE) \r\n"
				+ "ORDER BY ERROR_DATE DESC";
		
		testCase.setTest_Description(data.get("id")+ data.get("descripcion"));
		LocalDateTime now = LocalDateTime.now();
		String pvDate = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		String adDate = pvDate.substring(0, 8);
		
		String wmCodeToValidate = "100";
		String wmCodeToValidate2 = "000";
		String wmCodeToValidate3 = "101";
		
		SoftAssert softAssert = new SoftAssert();
		
		/****************************************Paso 1*************************************/
		addStep("Ejecutar en la herramienta de postman el servicio de solicitud de Folio para la "
				+ "vinculación de tarjeta SPIN de la TPE_FTC del server QA8");
		System.out.println("Paso 1: ");
		
		
		String uri = data.get("baseuri");
		String param1 = data.get("param1");
		System.out.println(uri);
		String formParam1 = String.format(param1, pvDate, adDate);
		RestAssured.baseURI = uri;
		RequestSpecification requestFolio = RestAssured.given().queryParam("jsonIn", formParam1).relaxedHTTPSValidation();
		requestFolio.log().method();
		requestFolio.log().uri();
		requestFolio.log().params();
		
		Response response = requestFolio.request(Method.POST);
		String responseBody = response.getBody().asPrettyString();
		
		JSONObject jsonResponseBody = new JSONObject(responseBody);
		JSONObject doc = jsonResponseBody.getJSONObject("TPEDoc");
		JSONObject header = doc.getJSONObject("header");
		String folio = header.getString("folio");
		String creationDate = header.getString("creationDate");
		
		JSONObject responseWmCode = doc.getJSONObject("response");
		JSONObject wmCodeJson = responseWmCode.getJSONObject("wmCode");
		String wmCode = wmCodeJson.getString("value");
		
		System.out.println("Status:" + response.statusLine());
		System.out.println(responseBody);
		
		//Agregar request a la evidencia
		testCase.addBoldTextEvidenceCurrentStep("Request: ");
		testCase.addBoldTextEvidenceCurrentStep("Metodo: POST");
		testCase.addTextEvidenceCurrentStep(uri + "jsonIn=" + formParam1);
		//Agregar response a la evidencia
		testCase.addBoldTextEvidenceCurrentStep("Status");
		testCase.addBoldTextEvidenceCurrentStep(response.statusLine());
		testCase.addBoldTextEvidenceCurrentStep("Response: ");
		testCase.addTextEvidenceCurrentStep(responseBody.toString());
		
		softAssert.assertEquals(wmCode, wmCodeToValidate, "Paso 1: No se obtuvo el WmCode esperado");
		
		/****************************************Paso 2*************************************/
		addStep(" Ejecutar en la herramienta de postman el servicio de vinculación con los siguientes campos:\r\n"
				+ "folio: Folio antes recibido de TRN01");
		System.out.println("Paso 2: ");
		
		String param2 = data.get("param2");
		String formParam2 = String.format(param2, folio, pvDate, adDate, creationDate);
		RequestSpecification requestAuth = RestAssured.given().queryParam("jsonIn",formParam2).relaxedHTTPSValidation();
		requestAuth.log().method();
		requestAuth.log().uri();
		requestAuth.log().params();
		Response responseAuth = requestAuth.request(Method.POST);
		String responseBodyAuth = responseAuth.getBody().asPrettyString();
		
		JSONObject jsonResponseBodyAuth = new JSONObject(responseBodyAuth);
		JSONObject docAuth = jsonResponseBodyAuth.getJSONObject("TPEDoc");
		JSONObject jsonResponseAuth = docAuth.getJSONObject("response");
		JSONObject jsonWmCodeAuth = jsonResponseAuth.getJSONObject("wmCode");
		String wmCodeAuth = jsonWmCodeAuth.getString("value");
		
		
		System.out.println("Status:" + responseAuth.getStatusLine());
		System.out.println(responseBodyAuth);
		
		//Agregar request a la evidencia
		testCase.addBoldTextEvidenceCurrentStep("Request: ");
		testCase.addBoldTextEvidenceCurrentStep("Metodo: POST");
		testCase.addTextEvidenceCurrentStep(uri + "jsonIn=" +formParam2);
		//Agregar response a la evidencia
		testCase.addBoldTextEvidenceCurrentStep("Response: ");
		testCase.addBoldTextEvidenceCurrentStep("Status: " + responseAuth.statusLine());
		testCase.addTextEvidenceCurrentStep(responseBodyAuth.toString());
		
		softAssert.assertEquals(wmCodeAuth, wmCodeToValidate2, "Paso 2: No se obtuvo el WmCode esperado");
		
		/****************************************Paso 3*************************************/
		addStep(" Ejecutar en la herramienta de postman el json para realizar petición ACK TRN03 00 y verificar que conteste "
				+ "con un json indicando que la transacción fue exitosa.");
		System.out.println("Paso 3: ");
		
		String param3 = data.get("param3");
		String formParam3 = String.format(param3, folio, pvDate, adDate, creationDate);
		RequestSpecification requestAck = RestAssured.given().queryParam("jsonIn", formParam3).relaxedHTTPSValidation();
		requestAck.log().method();
		requestAck.log().uri();
		requestAck.log().params();
		
		Response responseAck = requestAck.request(Method.POST);
		String responseBodyAck = responseAck.getBody().asPrettyString();
		String statusAck = responseAck.getStatusLine();
		
		JSONObject jsonResponseBodyAck = new JSONObject(responseBodyAck);
		JSONObject docAck = jsonResponseBodyAck.getJSONObject("TPEDoc");
		
		JSONObject responseWmCodeAck = docAck.getJSONObject("response");
		JSONObject wmCodeJsonAck = responseWmCodeAck.getJSONObject("wmCode");
		String wmCodeAck = wmCodeJsonAck.getString("value");
		
		System.out.println("Status: " + statusAck);
		System.out.println(responseBodyAck);
		
		//Agregar request a la evidencia
		testCase.addBoldTextEvidenceCurrentStep("Request: ");
		testCase.addBoldTextEvidenceCurrentStep("Metodo:  POST");
		testCase.addTextEvidenceCurrentStep(uri + "jsonIn=" + formParam3 );
		//Agregar response a la evidencia
		testCase.addBoldTextEvidenceCurrentStep("Response: ");
		testCase.addBoldTextEvidenceCurrentStep("Status" + responseAck.getStatusLine());
		testCase.addTextEvidenceCurrentStep(responseBodyAck.toString());
		
		softAssert.assertEquals(wmCodeAck, wmCodeToValidate3, "Paso 3: No se obtuvo el WmCode esperado");
		
		/****************************************Paso 4*************************************/
		addStep("Ejecutar la siguiente consulta para validar la Transacción exitosa en la tabla "
				+ "CTR_SE_TRANSACTION  de la BD  **FCACQA** ");
		System.out.println("Paso 4: ");
		
		String formTransaction = String.format(queryTransaction, folio);
		System.out.println(formTransaction);
		
		SQLResult transaction = dbFCACQA.executeQuery(formTransaction);
		boolean validateTransaction = transaction.isEmpty();
		
		if(!validateTransaction) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1:");
			testCase.addQueryEvidenceCurrentStep(transaction);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1:");
			testCase.addQueryEvidenceCurrentStep(transaction);
		}
		assertFalse(validateTransaction, "No se realizo la transacción");
		
		/****************************************Paso 5*************************************/
		
		addStep("Ejecutar la siguiente consulta en la base de datos **FCWMLTAEQA** para validar que"
				+ " no se encuentren registros de error de la FTC");
		System.out.println("Paso 5: ");
		
		SQLResult errorSite1 = dbFCWMLTAEQA_MTY.executeQuery(queryError);
		System.out.println(queryError);
		
		boolean validateError = errorSite1.isEmpty();
		if(!validateError) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1:");
			testCase.addQueryEvidenceCurrentStep(errorSite1);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1:");
			testCase.addQueryEvidenceCurrentStep(errorSite1);
		}
		
		assertTrue(validateError, "Se obtuvieron registros de error");
		softAssert.assertAll();
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
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
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

}