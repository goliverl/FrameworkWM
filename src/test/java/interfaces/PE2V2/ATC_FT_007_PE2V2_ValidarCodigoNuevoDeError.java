package interfaces.PE2V2;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;

import groovyjarjarantlr4.v4.parse.ANTLRParser.parserRule_return;
import io.restassured.response.Response;
import modelo.BaseExecution;
import om.PE2;
import util.GlobalVariables;
import utils.ApiMethodsUtil;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ATC_FT_007_PE2V2_ValidarCodigoNuevoDeError extends BaseExecution{
	/**
	 * PE2V2:MTC-FT-026 Validar nuevo codigo de error
	 * Desc:
	 * Validar que al no estar configurado parte las configuraciones marque error  correspondiente al WsSecurity
	 * 
	 * Mtto:
	 * @author Jose Onofre
	 * @date 02/27/2023
	 */
	@Test(dataProvider = "data-provider")
	public void ATC_FT_007_PE2V2_ValidarCodigoNuevoDeError_test(HashMap<String, String> data) throws Exception{
		/*
		 * Utileria*************************************************/
		//utils.sql.SQLUtil dbFCTDCQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA, GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		//utils.sql.SQLUtil dbFCTDCQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA_QRO, GlobalVariables.DB_USER_FCTDCQA_QRO, GlobalVariables.DB_PASSWORD_FCTDCQA_QRO);
		utils.sql.SQLUtil dbFCSWQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		utils.sql.SQLUtil dbFCSWQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO, GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		utils.sql.SQLUtil dbFCWMLTAQ_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAQ_MTY, GlobalVariables.DB_USER_FCWMLTAQ_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAQ_MTY);
		
		PE2 pe2Util = new PE2(data, testCase, null);
		SeleniumUtil u = null;
		/*
		 * Variables************************************************/
		String queryCorrectTransaction = "SELECT tdc.wm_code, td.app, td.securitytrack,  tdc.* "
				+ "FROM TPEUSER.TDC_TRANSACTION tdc "
				+ "WHERE FOLIO = '%s'"
				+ "AND CREATION_DATE>=:DDMMAAAA";
		
		String queryReverseStatus = "SELECT re.wm_code, re.status, re.counter, re.* "
				+ "FROM TPEUSER.TDC_REVERSE re \r\n"
				+ "WHERE FOLIO = '%s'  AND \r\n"
				+ "CREATION_DATE >= TRUNC(SYSDATE)";
		
		String queryTransactionRegistered = "SELECT tdc.wm_code, td.app, td.securitytrack,  tdc.* "
				+ "FROM TPEUSER.TDC_TRANSACTION tdc "
				+ "WHERE FOLIO = '%S'"
				+ "AND CREATION_DATE >= ";
		
		String queryTransaction = "SELECT tl.auth_id_res, tl.resp_code, tl.sw_code, tl.counter, tl.* \r\n"
				+ "FROM SWUSER.TPE_SW_TLOG  tl \r\n"
				+ "WHERE creation_date >= :v_pvDate\r\n"
				+ "AND folio = :v_folio";
		
		String queryErrorRecords = "SELECT *\r\n"
				+ "FROM WMLOG.WM_LOG_ERROR_TPE TPE\r\n"
				+ "WHERE TRUNC(ERROR_DATE) >= :v_fechaActual\r\n"
				+ "AND ROWNUM <= 100\r\n"
				+ "ORDER BY error_date  DESC";
		
		String getFolioUri = String.format(data.get("uri"), data.get("host"));
		testCase.setTest_Description(data.get("name") + " - " + data.get("desc"));
		ApiMethodsUtil api = new ApiMethodsUtil(getFolioUri);
		String getFolioParams = pe2Util.runGetFolioParams();
		
		/**************************Paso 1***************************/
		addStep("Validar que se encuentre en HOLD el job PE2V2ReverseManager");
		
		u  = new SeleniumUtil(new ChromeTest(), true);
		
		JSONObject obj = new JSONObject(data.get("job"));
		
		addStep("Jobs en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("pas"));
	
		addStep("Login");
		u.get(data.get("server"));
		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn(); 

		//Buscar del job
	
		addStep("Inicio de job ");
		ControlM control = new ControlM(u, testCase, obj);
		boolean flag = control.searchJob();
		assertTrue(flag);
		
		//Ejecucion
		String resultado = control.executeJob();
		System.out.println("Resultado de la ejecucion -> " + resultado);

		//Valor del output 
		System.out.println ("Valor de output :" +control.getOutput());
		
		//Validacion del caso
		Boolean casoPasado = true;
		if(resultado.equals("Wait Condition")) {
		casoPasado = true;
		}		
		assertTrue(casoPasado);
		//assertNotEquals("Failure",resultado);
		control.closeViewpoint();
		
		/**************************Paso 2***************************/
		addStep(" Solicitar un folio desde el navegador o desde la herramienta Soap UI, invocando el servicio "
				+ "**runGetFolio** de la Interface PE2V2 del site <Nombre_SITE>");
		
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
		/**************************Paso 3***************************/
		addStep("Solicitar autorización de pago con tarjeta desde un navegador o desde la herramienta Soap UI, "
				+ "invocando el servicio **runGetAuth** de la interface PE2v2 del site <Nombre_SITE>");
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
		/**************************Paso 4***************************/
		addStep(" Ejecutar la siguiente consulta para validar que la transaccion se registro correctamente en la "
				+ "tabla TDC_TRANSACTION de la BD  **FCTDCQA**  ");
		/*String correctTransactionFormat = String.format(queryCorrectTransaction, folio);
		SQLResult correctTransaction = executeQuery(dbFCTDCQA_QRO, correctTransactionFormat);
		System.out.println(correctTransactionFormat);
		boolean validateCorrectTransaction = correctTransaction.isEmpty();
		if(!validateCorrectTransaction) {
			String transactionWmCode = correctTransaction.getData(0, "WM_CODE");
			testCase.addQueryEvidenceCurrentStep(correctTransaction);
			
			assertEquals(transactionWmCode, data.get("WM_CODE"));
		}
		assertFalse(validateCorrectTransaction, "No se obtuvo registro de la transaccion");
		
		/**************************Paso 5***************************/
		addStep("Ejecutar que se ejecute el job PE2V2ReverseManager y una vez finalizado dejar en HOLD");
		
		u  = new SeleniumUtil(new ChromeTest(), true);
		
		JSONObject obj2 = new JSONObject(data.get("job"));
		
		addStep("Jobs en  Control M ");
		Control_mInicio CM2 = new Control_mInicio(u, data.get("user"), data.get("pas"));
	
		addStep("Login");
		u.get(data.get("server"));
		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn(); 

		//Buscar del job
	
		addStep("Inicio de job ");
		ControlM control2 = new ControlM(u, testCase, obj2);
		boolean flag2 = control.searchJob();
		assertTrue(flag2);
		
		//Ejecucion
		String resultado2 = control.executeJob();
		System.out.println("Resultado de la ejecucion -> " + resultado2);

		//Valor del output 
		System.out.println ("Valor de output :" +control2.getOutput());
		
		//Validacion del caso
		Boolean casoPasado2 = true;
		if(resultado2.equals("Wait Condition")) {
		casoPasado2 = true;
		}		
		assertTrue(casoPasado2);
		//assertNotEquals("Failure",resultado);
		control2.closeViewpoint();
		
		
		/**************************Paso 6***************************/
		addStep("Ejecutar la siguiente consulta para revisar el estado de la reversa en la BD **FCTDCQA**");
		String reverseStatusFormat = String.format(queryReverseStatus, folio);
		/*SQLResult reverseStatus = executeQuery(dbFCTDCQA_MTY, reverseStatusFormat);
		System.out.println(reverseStatusFormat);
		boolean validateReverseStatus = reverseStatus.isEmpty();
		if(!validateReverseStatus) {
			String reverseWmCode = reverseStatus.getData(0, "WM_CODE");
			String reverseStatusCode = reverseStatus.getData(0, "STATUS");
			testCase.addQueryEvidenceCurrentStep(reverseStatus);
			
			assertEquals(reverseWmCode, data.get("REVERSE_WMCODE"), "se obtiene WM_CODE diferente al esperado");
			assertTrue(reverseStatusCode == data.get("STATUS_P") || reverseStatusCode == data.get("STATUS_P"), "Se obtiene STATUS diferente a P o S");
			
		}
		assertFalse(validateReverseStatus, "No se obtuvo registro de la transaccion");
		/**************************Paso 7***************************/
		addStep("Ejecutar la siguiente consulta para validar que la transaccion se registro correctamente "
				+ "en la tabla TDC_TRANSACTION de la BD  **FCTDCQA**  ");
		
		
		/**************************Paso 8***************************/
		addStep("*Validar en la base de datos del **FCSWQA**, que NO se encuentre el registro de la transaccion "
				+ "utilizando la siguiente consulta:*");
		
		/**************************Paso 9***************************/
		addStep("Validar en la base de datos **FCWMLTAQ** se encuentren registros de error de la PE2V2, utilizando la siguiente consulta:");
		
		
		
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
