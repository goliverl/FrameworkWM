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

public class ATC_FT_004_PE2V2_EjecutarTransaccionExitosaDeDevolucion extends BaseExecution {
	/**
	 * PE2V2:MTC-FT-004 Ejecutar transaccion exitosa de la devolucion del pago con tarjeta usando conector actual
	 * MTC-FT-014 Realizar una devolucion usando nuevo conector
	 * Desc:
	 * Comprobar que las transacciones de una devolucion del pago con tarjeta con la versión 
	 * de la interface PE2v2 se realice exitosamente.
	 * 
	 * Mtto:
	 * @author Jose Onofre
	 * @date 02/27/2023
	 */
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_PE2V2_EjecutarTransaccionExitosaDeDevolucion_test (HashMap <String, String> data) throws Exception{
		/*
		 * Utileria******************************************************/
		//utils.sql.SQLUtil dbFCTDCQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA, GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		//utils.sql.SQLUtil dbFCTDCQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA_QRO, GlobalVariables.DB_USER_FCTDCQA_QRO, GlobalVariables.DB_PASSWORD_FCTDCQA_QRO);
		utils.sql.SQLUtil dbFCSWQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		utils.sql.SQLUtil dbFCWMLTAEQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QAVIEW);
		
		PE2 pe2Util = new PE2(data, testCase, null);
		/*
		 * Variables******************************************************/
		String querycorrectTransaction = "SELECT tdc.wm_code, tdc.* "
				+ "FROM TPEUSER.TDC_DEVOLUTION tdc "
				+ "WHERE FOLIO= '%s' "
				+ "AND CREATION_DATE >= TRUNC(SYSDATE)";
		
		String queryInvoiceRecord = "SELECT * "
				+ "FROM tpeuser.TDC_DEVOLUTION "
				+ "WHERE folio = '%s' AND status = 100";
		
		String queryInvoiceStatus = "SELECT * "
				+ "FROM tpeuser.tdc_devolution "
				+ "WHERE folio = %s"
				+ "AND wm_code = '101'";
		
		String queryTransactionRecord = "SELECT tl.auth_id_res, tl.resp_code, tl.sw_code, tl.counter, tl.* \r\n"
				+ "FROM SWUSER.TPE_SW_TLOG  tl \r\n"
				+ "WHERE creation_date >= TRUNC(SYSDATE)\r\n"
				+ "AND folio = '%s'";
		
		String queryErrorRecords = "SELECT *\r\n"
				+ "FROM WMLOG.WM_LOG_ERROR_TPE TPE\r\n"
				+ "WHERE TRUNC(ERROR_DATE) >= TRUNC(SYSDATE)\r\n"
				+ "AND TPE_TYPE like '%PE2V2%'\r\n"
				+ "AND ROWNUM <= 100\r\n"
				+ "ORDER BY error_date  DESC";
		
		String getFolioUri = String.format(data.get("getFolio"), data.get("host"));
		testCase.setTest_Description(data.get("name") + " - " + data.get("desc"));
		ApiMethodsUtil api = new ApiMethodsUtil(getFolioUri);
		String getFolioParams = pe2Util.runGetFolioParams();
		/********************************Paso 1***************************/
		addStep("- ? Solicitar un folio desde el navegador o desde la herramienta Soap UI, invocando el servicio "
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
		
		/********************************Paso 2***************************/
		addStep("Ejecutar la siguiente consulta para validar que la transaccion se registro correctamente en "
				+ "la tabla TDC_DEVOLUTION de la BD  **FCTDCQA**");
		/*
		String correctTransactionFormat = String.format(querycorrectTransaction, folio);
		SQLResult correctTransaction = executeQuery(dbFCTDCQA_MTY, correctTransactionFormat);
		System.out.println(correctTransactionFormat);
		boolean validateCorrectTransaction = correctTransaction.isEmpty();
		String wmCode = "";
		if(!validateCorrectTransaction) {
			wmCode = correctTransaction.getData(0, "WM_CODE");
			testCase.addQueryEvidenceCurrentStep(correctTransaction);
			
			assertEquals(wmCode, data.get("WM_CODE"), "Se obtiene WM_CODE diferente al esperado");
		}
		assertFalse(validateCorrectTransaction, "No se obtuvo registro de la transaccion");
		/********************************Paso 3***************************/
		addStep("Solicitar la __devolucion__ de una pago con tarjeta desde un navegador o desde la herramienta "
				+ "Soap UI, invocando el servicio **runGetDev** de la interface PE2V2 del site <Nombre_SITE>");
		
		String getDevUri = String.format(data.get("getDev"), data.get("host"));
		String getDevParams = pe2Util.runGetDevParams(folio, creationDate);
		Response requestGetDev = api.getRequestMethod(getDevUri + getDevParams);
		System.out.println(getDevUri + getDevParams);
		String devWmCode = "";
		if(requestGetDev.getStatusCode() == Integer.parseInt(data.get("statusCode"))) {
			String getDevBody = requestGetDev.getBody().asPrettyString();
			System.out.println(getDevBody);
			devWmCode = getSimpleDataXml(getDevBody, "WM_CODE");
			testCase.addTextEvidenceCurrentStep("Request");
			testCase.addTextEvidenceCurrentStep("Metodo: GET");
			testCase.addTextEvidenceCurrentStep(getDevUri + getDevParams);
			testCase.addTextEvidenceCurrentStep("Response");
			testCase.addTextEvidenceCurrentStep("Status Code: " + requestGetDev.getStatusLine());
			testCase.addTextEvidenceCurrentStep(getDevBody);
			
			assertEquals(devWmCode, data.get("devWmCode"), "Se obtiene WM_CODE diferente al esperado");
		}
		assertEquals(requestGetDev.getStatusCode() == Integer.parseInt(data.get("statusCode")), "Se obtiene estatus diferente a 200");
		/********************************Paso 4***************************/
		addStep("Validar que se ha creado un registro del folio en la tabla TDC_DEVOLUTION de TPEUSER, pasando "
				+ "como folio el que recibimos en la respuesta de la ejecución del servicio.");
		
		String invoiceRecordFormat = String.format(queryInvoiceRecord, folio);
		SQLResult invoiceRecord = executeQuery(dbFCWMLTAEQA_MTY, invoiceRecordFormat);
		System.out.println(invoiceRecordFormat);
		boolean validateInvoiceRecord = invoiceRecord.isEmpty();
		if(!validateInvoiceRecord) {
			testCase.addQueryEvidenceCurrentStep(invoiceRecord);
		}
		assertFalse(validateInvoiceRecord, "No se obtuvo registro de la transaccion");
		/********************************Paso 5***************************/
		addStep("Solicitar confirmacion ACK del pago en efectivo desde un navegador o desde "
				+ "la herramienta Soap UI, invocando el servicio **runGeDevtAck** de la interface PE2V2 del site <Nombre_SITE>");
		
		String getDevAckUri = String.format(data.get("getDevAck"), data.get("host"));
		String getDevAckParams = pe2Util.runGetDevAckParams(folio);
		Response requestGetDevAck = api.getRequestMethod(getDevAckUri + getDevAckParams);
		System.out.println(getDevAckUri + getDevAckParams);
		String ackWmCode = "";
		if(requestGetDevAck.getStatusCode() == Integer.parseInt(data.get("statusCode"))) {
			String getDevAckBody = requestGetDevAck.getBody().asPrettyString();
			ackWmCode = getSimpleDataXml(getDevAckBody, "WM_CODE");
			testCase.addTextEvidenceCurrentStep("Request");
			testCase.addTextEvidenceCurrentStep("Metodo: GET");
			testCase.addTextEvidenceCurrentStep(getDevAckUri + getDevAckParams);
			testCase.addTextEvidenceCurrentStep("Response");
			testCase.addTextEvidenceCurrentStep("Status Code: " + requestGetDevAck.getStatusLine());
			testCase.addTextEvidenceCurrentStep(getDevAckBody);
			
			assertEquals(ackWmCode, data.get("ackWmCode"), "Se obtiene WM_CODE diferente al esperado");
		}
		assertEquals(requestGetDevAck.getStatusCode() == Integer.parseInt(data.get("statusCode")), "Se obtiene estatus diferente a 200");
		/********************************Paso 6***************************/
		addStep("Validar que el estatus del folio ha sido actualizado en la tabla tdc_devolution de TPEUSER.");
		
		String invoiceStatusFormat = String.format(queryInvoiceStatus, folio);
		SQLResult invoiceStatus = executeQuery(dbFCWMLTAEQA_MTY, invoiceStatusFormat);
		System.out.println(invoiceStatusFormat);
		boolean validateInvoiceStatus = invoiceStatus.isEmpty();
		if(!validateInvoiceStatus) {
			testCase.addQueryEvidenceCurrentStep(invoiceStatus);
		}
		assertFalse(validateInvoiceStatus, "No se obtuvo registro del folio");
		/********************************Paso 7***************************/
		addStep("*Validar en la base de datos del **FCSWQA**, que se encuentre el registro de la "
				+ "transaccion utilizando la siguiente consulta:*");
		
		String transactionRecordFormat = String.format(queryTransactionRecord, folio);
		SQLResult transactionRecord = executeQuery(dbFCSWQA_MTY, transactionRecordFormat);
		System.out.println(transactionRecordFormat);
		boolean validateTransactionRecord = transactionRecord.isEmpty();
		if(!validateTransactionRecord) {
			testCase.addQueryEvidenceCurrentStep(transactionRecord);
		}
		assertFalse(validateTransactionRecord, "No se obtuvo registro de transaccion");
		/********************************Paso 8***************************/
		addStep("Validar en la base de datos **FCWMLTAQ** que no se encuentren registros de error de la PE2V2, "
				+ "utilizando la siguiente consulta:");
		
		SQLResult errorRecords = executeQuery(dbFCWMLTAEQA_MTY, queryErrorRecords);
		System.out.println(queryErrorRecords);
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
