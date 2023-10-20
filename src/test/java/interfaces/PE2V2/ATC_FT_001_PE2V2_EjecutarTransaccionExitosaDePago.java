package interfaces.PE2V2;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import io.restassured.response.Response;
import modelo.BaseExecution;
import om.PE2;
import util.GlobalVariables;
import utils.ApiMethodsUtil;
import utils.sql.SQLResult;

import static util.RequestUtil.getSimpleDataXml;

public class ATC_FT_001_PE2V2_EjecutarTransaccionExitosaDePago extends BaseExecution{
	
	/**
	 * PE2V2: MTC-FT-001 Ejecutar transacción exitosa de pago con puntos usando conector actual
	 * Desc:
	 * Comprobar que las transacciones de pago con puntos exitosa, ejecutadas desde la interface PE2v2 son procesadas correctamente.
	 * 
	 * Mtto:
	 * @author Jose Onofre
	 * @date 02/27/2023
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PE2V2_EjecutarTransaccionExitosaDePago_test (HashMap<String, String> data) throws Exception {
		/*
		 * Utileria******************************************************/
		utils.sql.SQLUtil dbFCTDCQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA, GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		utils.sql.SQLUtil dbFCTDCQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA_QRO, GlobalVariables.DB_USER_FCTDCQA_QRO, GlobalVariables.DB_PASSWORD_FCTDCQA_QRO);
		utils.sql.SQLUtil dbFCSWQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		utils.sql.SQLUtil dbFCSWQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO, GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		utils.sql.SQLUtil dbFCWMLTAQ_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAQ_MTY, GlobalVariables.DB_USER_FCWMLTAQ_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAQ_MTY);
		
		PE2 pe2Util = new PE2(data, testCase, null);
		/*
		 * Variables*****************************************************/
		String queryCorrectRegister = "SELECT WM_CODE, SW_AUTH_CODE,FOLIO, CREATION_DATE, PLAZA, TIENDA\r\n"
				+ "FROM TPEUSER.TDC_TRANSACTION\r\n"
				+ "WHERE FOLIO = '%s' \r\n"
				+ "AND CREATION_DATE >= TRUNC(SYSDATE)";
		
		String queryCorrectTransaction = "SELECT RESP_CODE, SW_CODE, PLAZA, TIENDA, FOLIO, MTI\r\n"
				+ "FROM SWUSER.TPE_SW_TLOG\r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "AND FOLIO = '%s'";
		
		String queryErrorRecords = "SELECT ERROR_ID, FOLIO, ERROR_DATE, ERROR_TYPE, ERROR_CODE, DESCRIPTION\r\n"
				+ "FROM WMLOG.WM_LOG_ERROR_TPE\r\n"
				+ "WHERE ERROR_DATE >= TRUNC(SYSDATE)\r\n"
				+ "AND FOLIO = '%s'\r\n"
				+ "AND TPE_TYPE like '%s'\r\n"
				+ "AND ROWNUM <= 100\r\n"
				+ "ORDER BY ERROR_DATE  DESC";
		
		String getFolioUri = String.format(data.get("getFolio"), data.get("host"));
		
		ApiMethodsUtil api = new ApiMethodsUtil(getFolioUri);
		String getFolioParams = pe2Util.runGetFolioParams();
		
		testCase.setTest_Description(data.get("description"));
		/******************************Paso 1****************************/
		addStep("Solicitar un folio desde el navegador o desde la herramienta Soap UI, invocando "
				+ "el servicio runGetFolio de la Interface PE2V2 del sitie <Nombre_SITE>");
		
		Response requestGetFolio =  api.getRequestMethod(getFolioUri + getFolioParams);
		System.out.println(getFolioUri + getFolioParams);
		String folio = "";
		String folioWmCode = "";
		String creationDate = "";
		if(requestGetFolio.getStatusCode() == Integer.parseInt(data.get("statusCode"))) {
			String getFolioBody = requestGetFolio.getBody().asPrettyString();
			System.out.println(getFolioBody);
			folio = getSimpleDataXml(getFolioBody, "folio");
			folioWmCode = getSimpleDataXml(getFolioBody, "wmCode");
			creationDate = getSimpleDataXml(getFolioBody, "creationDate");
			testCase.addTextEvidenceCurrentStep("Request");
			testCase.addTextEvidenceCurrentStep("Metodo: GET");
			testCase.addTextEvidenceCurrentStep(getFolioUri + getFolioParams);
			testCase.addTextEvidenceCurrentStep("Response");
			testCase.addTextEvidenceCurrentStep("Status Code: " + requestGetFolio.getStatusLine());
			testCase.addTextEvidenceCurrentStep(getFolioBody);
			
			assertEquals(folioWmCode, data.get("folioWmCode"), "Se obtiene WM_CODE diferente al esperado");
		}
		assertEquals(requestGetFolio.getStatusCode(), Integer.parseInt(data.get("statusCode")), "Se obtiene estatus diferente a 200");
		
		/******************************Paso 2****************************/
		addStep("Solicitar autorización de la compra con tarjeta desde un navegador o desde la herramienta"
				+ " Soap UI, invocando el servicio runGetAuth de la interface PE2v2 del site <Nombre_SITE>");
		String getAuthUri = String.format(data.get("getAuth"), data.get("host"));
		String getAuthParams = pe2Util.runGetAuthParams(folio, creationDate);
		Response requestGetAuth = api.getRequestMethod(getAuthUri + getAuthParams);
		System.out.println(getAuthUri + getAuthParams);
		String authWmCode = "";
		if(requestGetAuth.getStatusCode() == Integer.parseInt(data.get("statusCode"))) {
			String getAuthBody = requestGetAuth.getBody().asPrettyString();
			authWmCode = getSimpleDataXml(getAuthBody, "wmCode");
			testCase.addTextEvidenceCurrentStep("Request");
			testCase.addTextEvidenceCurrentStep("Metodo: GET");
			testCase.addTextEvidenceCurrentStep(getAuthUri + getAuthParams);
			testCase.addTextEvidenceCurrentStep("Response");
			testCase.addTextEvidenceCurrentStep("Status Code: " + requestGetAuth.getStatusLine());
			testCase.addTextEvidenceCurrentStep(getAuthBody);
			
			assertEquals(authWmCode, data.get("authWmCode"), "Se obtiene WM_CODE diferente al esperado");
		}
		assertEquals(requestGetAuth.getStatusCode(), Integer.parseInt(data.get("statusCode")), "Se obtiene estatus diferente a 200");
		/******************************Paso 3****************************/
		addStep("Solicitar confirmacion ACK de la compra con tarjeta desde un navegador o desde la herramienta Soap UI, "
				+ "invocando el servicio **runGetAuth** de la interface PE2V2 del site <Nombre_SITE>");
		String getAckUri = String.format(data.get("getAuthAck"), data.get("host"));
		String getAuthAckParams = pe2Util.runGetAckParams(folio, creationDate);
		Response requestGetAuthAck = api.getRequestMethod(getAckUri + getAuthAckParams);
		System.out.println(getAckUri + getAuthParams);
		String ackWmCode = "";
		if(requestGetAuthAck.getStatusCode() == Integer.parseInt(data.get("statusCode"))) {
			String getAuthAckBody = requestGetAuthAck.getBody().asPrettyString();
			ackWmCode = getSimpleDataXml(getAuthAckBody, "wmCode");
			testCase.addTextEvidenceCurrentStep("Request");
			testCase.addTextEvidenceCurrentStep("Metodo: GET");
			testCase.addTextEvidenceCurrentStep(getAckUri + getAuthAckParams);
			testCase.addTextEvidenceCurrentStep("Response");
			testCase.addTextEvidenceCurrentStep("Status Code: " + requestGetAuthAck.getStatusLine());
			testCase.addTextEvidenceCurrentStep(getAuthAckBody);
			
			assertEquals(ackWmCode, data.get("ackWmCode"), "Se obtiene WM_CODE diferente al esperado");
		}
		assertEquals(requestGetAuthAck.getStatusCode(), Integer.parseInt(data.get("statusCode")), "Se obtiene estatus diferente a 200");
		
		/******************************Paso 4****************************/
		addStep("Ejecutar la siguiente consulta para validar que la transacción se registró correctamente en la "
				+ "tabla TDC_TRANSACTION de la BD  **FCTDCQA**  ");
		
		String correctRegisterFormat = String.format(queryCorrectRegister, folio);
		System.out.println(correctRegisterFormat);
		SQLResult correctRegisterS1 = executeQuery(dbFCTDCQA_MTY, correctRegisterFormat);
		SQLResult correctRegisterS2 = executeQuery(dbFCTDCQA_QRO, correctRegisterFormat);
		boolean validateCorrectRegister = correctRegisterS1.isEmpty();
		String wmCode = "";
		String swAuthCode = "";
		if(!validateCorrectRegister) {
			wmCode = correctRegisterS1.getData(0, "WM_CODE");
			swAuthCode = correctRegisterS1.getData(0, "SW_AUTH_CODE");
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(correctRegisterS1);
			
			assertEquals(wmCode, data.get("WM_CODE"), "Se obtiene WM_CODE diferente al esperado");
			assertEquals(swAuthCode, data.get("SW_AUTH_CODE"), "Se obtiene SW_AUTH_CODE diferente al esperado");
		}else if (!correctRegisterS2.isEmpty()){
			wmCode = correctRegisterS2.getData(0, "WM_CODE");
			swAuthCode = correctRegisterS2.getData(0, "SW_AUTH_CODE");
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(correctRegisterS2);
			
			assertEquals(wmCode, data.get("WM_CODE"), "Se obtiene WM_CODE diferente al esperado");
			assertEquals(swAuthCode, data.get("SW_AUTH_CODE"), "Se obtiene SW_AUTH_CODE diferente al esperado");
		}
		assertFalse(validateCorrectRegister && correctRegisterS2.isEmpty(), "No se obtuvo registro de la transaccion");
		/******************************Paso 5****************************/
		addStep("*Validar en la base de datos del **FCSWQA**, que se encuentre el registro de la transaccion utilizando la siguiente consulta:*");
		
		String CorrectTransactionFormat = String.format(queryCorrectTransaction, folio);
		System.out.println(CorrectTransactionFormat);
		SQLResult correctTransactionS1 = executeQuery(dbFCSWQA_MTY, CorrectTransactionFormat);
		SQLResult correctTransactionS2 = executeQuery(dbFCSWQA_QRO, CorrectTransactionFormat);
		boolean validateCorrectTransaction = correctTransactionS1.isEmpty();
		String mti = "";
		String respCode = "";
		if(!validateCorrectTransaction) {
			mti = correctTransactionS1.getData(0, "MTI");
			respCode = correctTransactionS1.getData(0, "RESP_CODE");
			testCase.addQueryEvidenceCurrentStep(correctTransactionS1);
			
			assertEquals(mti, data.get("MTI"), "Se obtiene WM_CODE diferente al esperado");
			assertEquals(respCode, data.get("RESP_CODE"), "Se obtiene SW_AUTH_CODE diferente al esperado");
		}else if (!correctTransactionS2.isEmpty()) {
			mti = correctTransactionS2.getData(0, "MTI");
			respCode = correctTransactionS2.getData(0, "RESP_CODE");
			testCase.addQueryEvidenceCurrentStep(correctTransactionS2);
			
			assertEquals(mti, data.get("MTI"), "Se obtiene WM_CODE diferente al esperado");
			assertEquals(respCode, data.get("RESP_CODE"), "Se obtiene SW_AUTH_CODE diferente al esperado");
		}
		assertFalse(validateCorrectTransaction && correctTransactionS2.isEmpty(), "No se obtuvo registro de la transaccion");
		
		/******************************Paso 6****************************/
		addStep("Validar en la base de datos **FCWMLTAQ** que no se encuentren registros de error de la PE2V2, "
				+ "utilizando la siguiente consulta:");
		String errorRecordsFormat = String.format(queryErrorRecords, "%PE2V2%",folio);
		SQLResult errorRecords = executeQuery(dbFCWMLTAQ_MTY, errorRecordsFormat);
		System.out.println(errorRecordsFormat);
		boolean validateErrorRecords = errorRecords.isEmpty();
		if(!validateErrorRecords) {
			testCase.addQueryEvidenceCurrentStep(errorRecords);
		}
		assertTrue(validateErrorRecords, "Se obtuvieron registros de error");
		
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
		return "ATC_FT_001_PE2V2_EjecutarTransaccionExitosaDePago_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
