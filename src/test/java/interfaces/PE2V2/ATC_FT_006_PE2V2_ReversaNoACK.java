package interfaces.PE2V2;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.util.HashMap;

import org.testng.annotations.Test;

import io.restassured.response.Response;
import modelo.BaseExecution;
import om.PE2;
import util.GlobalVariables;
import utils.ApiMethodsUtil;
import utils.sql.SQLResult;

public class ATC_FT_006_PE2V2_ReversaNoACK extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_006_PE2V2_ReversaNoACK_test (HashMap <String, String> data) throws Exception{
		/*
		 * Utileria***************************************************/
		utils.sql.SQLUtil dbFCTDCQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA, GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		utils.sql.SQLUtil dbFCSWQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		
		PE2 pe2Util = new PE2(data, testCase, null);
		/*
		 * Variables**************************************************/
		String queryTransactionRegistered = "SELECT tdc.wm_code, tdc.* "
				+ "FROM TPEUSER.TDC_TRANSACTION tdc "
				+ "WHERE FOLIO = '%s' "
				+ "AND CREATION_DATE >= TRUNC(SYSDATE)";
		
		String queryReverseRegistered = "SELECT * "
				+ "FROM TPEUSER.TDC_REVERSE "
				+ "WHERE FOLIO = '%s'"
				+ "AND CREATION_DATE >= TRUNC(SYSDATE)";
		
		String queryTransaction = "SELECT tl.auth_id_res, tl.resp_code, tl.sw_code, tl.counter, tl.* \r\n"
				+ "FROM SWUSER.TPE_SW_TLOG  tl \r\n"
				+ "WHERE creation_date >= TRUN(SYSDATE)\r\n"
				+ "AND folio = '%s'";
		
		String queryTransactionError = "SELECT *\r\n"
				+ "FROM WMLOG.WM_LOG_ERROR_TPE TPE\r\n"
				+ "WHERE ERROR_DATE >= TRUNC(SYSDATE)\r\n"
				+ "AND TPE_TYPE like '%PE2V2%'\r\n"
				+ "AND ROWNUM <= 100\r\n"
				+ "ORDER BY error_date  DESC";
		
		String getFolioUri = String.format(data.get("getFolio"), data.get("host"));
		testCase.setTest_Description(data.get("name") + " - " + data.get("desc"));
		ApiMethodsUtil api = new ApiMethodsUtil(getFolioUri);
		String getFolioParams = pe2Util.runGetFolioParams();
		/****************************Paso 1***************************/
		addStep("Solicitar un folio desde el navegador o desde alguna herramienta como  Soap UI o postman, "
				+ "invocando el servicio runGetFolio de la Interface PE2V2 del sitie <Nombre_SITE>");
		
		Response requestGetFolio =  api.getRequestMethod(getFolioUri + getFolioParams);
		System.out.println(getFolioUri + getFolioParams);
		String folio = "";
		String folioWmCode = "";
		String creationDate = "";
		if(requestGetFolio.getStatusCode() == Integer.parseInt(data.get("statusCode"))) {
			String getFolioBody = requestGetFolio.getBody().asPrettyString();
			System.out.println(getFolioBody);
			folioWmCode = getSimpleDataXml(getFolioBody, "WM_CODE");
			creationDate = getSimpleDataXml(getFolioBody, "CreationDate");
			testCase.addTextEvidenceCurrentStep("Request");
			testCase.addTextEvidenceCurrentStep("Metodo: GET");
			testCase.addTextEvidenceCurrentStep(getFolioUri + getFolioParams);
			testCase.addTextEvidenceCurrentStep("Response");
			testCase.addTextEvidenceCurrentStep("Status Code: " + requestGetFolio.getStatusLine());
			testCase.addTextEvidenceCurrentStep(getFolioBody);
			
			assertEquals(folioWmCode, data.get("folioWmCode"), "Se obtiene WM_CODE diferente al esperado");
		}
		assertEquals(requestGetFolio.getStatusCode(), Integer.parseInt(data.get("statusCode")), "Se obtiene estatus diferente a 200");
		
		/****************************Paso 2***************************/
		addStep("Solicitar autorización de <tipo_transaccion> desde un navegador o desde la herramienta Soap UI, "
				+ "invocando el servicio runGetAuth de la interface PE2v2 del site <Nombre_SITE>");
		
		String getAuthUri = String.format(data.get("getAuth"), data.get("host"));
		String getAuthParams = pe2Util.runGetAuthParams(folio, creationDate);
		Response requestGetAuth = api.getRequestMethod(getAuthUri + getAuthParams);
		System.out.println(getAuthUri + getAuthParams);
		String authWmCode = "";
		if(requestGetAuth.getStatusCode() == Integer.parseInt(data.get("statusCode"))) {
			String getAuthBody = requestGetAuth.getBody().asPrettyString();
			authWmCode = getSimpleDataXml(getAuthBody, "WM_CODE");
			testCase.addTextEvidenceCurrentStep("Request");
			testCase.addTextEvidenceCurrentStep("Metodo: GET");
			testCase.addTextEvidenceCurrentStep(getFolioUri + getFolioParams);
			testCase.addTextEvidenceCurrentStep("Response");
			testCase.addTextEvidenceCurrentStep("Status Code: " + requestGetAuth.getStatusLine());
			testCase.addTextEvidenceCurrentStep(getAuthBody);
			
			assertEquals(authWmCode, data.get("authWmCode"), "Se obtiene WM_CODE diferente al esperado");
		}
		assertEquals(requestGetAuth.getStatusCode(), Integer.parseInt(data.get("statusCode")), "Se obtiene estatus diferente a 200");
		
		/****************************Paso 3***************************/
		addStep("- Ejecutar servicio manualmente ó por medio job en el runReveseManager");
		
		
		/****************************Paso 4***************************/
		addStep("Ejecutar la siguiente consulta para validar que la transaccion se registro correctamente "
				+ "en la tabla TDC_TRANSACTION de la BD  **FCTDCQA**  ");
		
		String transactionRegisteredFormat = String.format(queryTransactionRegistered, folio);
		SQLResult transactionRegistered = executeQuery(dbFCTDCQA_MTY, transactionRegisteredFormat);
		System.out.println(transactionRegisteredFormat);
		boolean validateTransactionRegistered = transactionRegistered.isEmpty();
		if(!validateTransactionRegistered) {
			String wmCode = transactionRegistered.getData(0, "WM_CODE");
			String reversed = transactionRegistered.getData(0, "REVERSED");
			String swDevCode = transactionRegistered.getData(0, "SW_DEV_CODE");
			testCase.addQueryEvidenceCurrentStep(transactionRegistered);
			
			assertEquals(wmCode, data.get("WM_CODE"), "Se obtiene WM_CODE diferente al esperado");
			assertEquals(reversed, data.get("REVERSED"), "Se obtiene REVERSED diferente al esperado");
			assertEquals(swDevCode, data.get("SW_DEV_CODE"), "Se obtiene SW_DEV_CODE diferente al esperado");
		}
		assertFalse(validateTransactionRegistered, "No se obtuvo registro de la transaccion");
		/****************************Paso 5***************************/
		addStep("Ejecutar la siguiente consulta para validar que la transaccion se registro correctamente con "
				+ "la reversa en la tabla TDC_REVERSE de la BD: **FCTDCQA** con el esquema:**TPEUSER** ");
		
		String reverseRegisteredFormat = String.format(queryReverseRegistered, folio);
		SQLResult reverseRegistered = executeQuery(dbFCTDCQA_MTY, reverseRegisteredFormat);
		System.out.println(reverseRegisteredFormat);
		boolean validateReverseRegistered = reverseRegistered.isEmpty();
		if(!validateReverseRegistered) {
			String reversedWmCode = reverseRegistered.getData(0, "WM_CODE");
			String status = reverseRegistered.getData(0, "STATUS");
			String swReverseCode = reverseRegistered.getData(0, "SW_REVERSE_CODE");
			testCase.addQueryEvidenceCurrentStep(reverseRegistered);
			
			assertEquals(reversedWmCode, data.get("WM_CODE"), "Se obtiene WM_CODE diferente al esperado");
			assertEquals(status, data.get("STATUS"), "Se obtiene STATUS diferente al esperado");
			assertEquals(swReverseCode, data.get("SW_REVERSE_CODE"), "Se obtiene SW_REVERSE_CODE diferente al esperado");
		}
		assertFalse(validateReverseRegistered, "No se obtuvo registro de la transaccion");
		/****************************Paso 6***************************/
		addStep("Validar en la base de datos del **FCSWQA**, que se encuentre el registro de la transaccion "
				+ "utilizando la siguiente consulta:*");
		
		String transactionFormat = String.format(queryTransaction, folio);
		SQLResult transaction = executeQuery(dbFCSWQA_MTY, transactionFormat);
		System.out.println(transactionFormat);
		boolean validateTranasaction = transaction.isEmpty();
		if(!validateTranasaction) {
			String mti = transaction.getData(0, "MTI");
			String respCode = transaction.getData(0, "RESP_CODE");
			testCase.addQueryEvidenceCurrentStep(transaction);
			
			assertEquals(mti, data.get("MTI"), "Se obtiene MTI diferente al esperado");
			assertEquals(respCode, data.get("RESP_CODE"), "Se obtiene RESP_CODE diferente al esperado");
		}
		assertFalse(validateTranasaction, "No se obtuvo registro de la transaccion");
		/****************************Paso 6***************************/
		addStep("Validar en la base de datos **FCWMLTAQ** que no se encuentren registros de error de la PE2, "
				+ "utilizando la siguiente consulta:");
		
		SQLResult transactionError = executeQuery(dbFCSWQA_MTY, queryTransactionError);
		System.out.println(queryTransactionError);
		boolean validateTransactionError = transactionError.isEmpty();
		if(!validateTransactionError) {
			testCase.addQueryEvidenceCurrentStep(transactionError);
		}
		assertTrue(validateTransactionError, "Se obtuvieron regstros de error");
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
		return "Equipo automatizacion";
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
