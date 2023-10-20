package interfaces.pe2;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import om.PE2;

import static org.testng.Assert.*;
import static util.RequestUtil.getSimpleDataXml;

/**
 *  Prueba de regresion para comprobar la no afectacion en la funcionalidad 
 *  principal de transaccionalidad de la interface FEMSA_PE2 al ser migrada 
 *  de WM9.9 a WM10.5 para una transaccion exitosa de pago con tarjeta
 * 
 * @cp MTC-FT-005 PE2_REG Transaccion exitosa de pago con tarjeta
 * @reviewer Gilberto Martinez
 * @date 2023/15/02
 */


public class ATC_FT_012_PE2TransaccionExitosaPagoTarjeta extends BaseExecution {
	@Test (dataProvider = "data-provider")
	public void ATC_FT_012_PE2TransaccionExitosaPagoTarjeta_test (HashMap<String, String> data) throws Exception {
		/*
		 * Utilerias
		 ************************************************************************/
		utils.sql.SQLUtil dbFCTDCQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA, GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA); //NUEVA
		utils.sql.SQLUtil dbFCTDCQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA_QRO, GlobalVariables.DB_USER_FCTDCQA_QRO, GlobalVariables.DB_PASSWORD_FCTDCQA_QRO); //NUEVA
		utils.sql.SQLUtil dbFCSWQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		utils.sql.SQLUtil dbFCSWQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO, GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		utils.sql.SQLUtil dbFCWMLTAEQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QAVIEW);
		utils.sql.SQLUtil dbFCWMLTAEQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO, GlobalVariables.DB_USER_FCWMLTAEQA_QA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		
		PE2 pe2Util = new PE2(data, testCase, null);
		
		/*
		 *Variables
		 ************************************************************************/
		String queryPayment = "SELECT WM_CODE, CREATION_DATE, PLAZA, TIENDA, AFFILIATION, AMOUNT, SW_AUTH_CODE\r\n"
				+ "FROM TPEUSER.TDC_TRANSACTION\r\n"
				+ "WHERE FOLIO= '%s'";
		
		String queryTransaction = "SELECT AUTH_ID_RES, FOLIO, RESP_CODE, SW_CODE, COUNTER, MTI, RESP_CODE, AUTH_ID_RES, APPLICATION\r\n"
				+ "FROM SWUSER.TPE_SW_TLOG \r\n"
				+ "WHERE FOLIO = '%s'";
		
		String querySecurityLog = "SELECT APPLICATION, OPERATION, ENTITY, SOURCE, PLAZA, TIENDA, AMOUNT, CREATION_DATE\r\n"
				+ "FROM WMLOG.SECURITY_SESSION_LOG\r\n"
				+ "WHERE APPLICATION like 'PE2'\r\n"
				+ "AND CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "AND FOLIO = '%s'\r\n"
				+ "ORDER BY CREATION_DATE DESC";
		
		String queryError = "SELECT *\r\n"
				+ "FROM ( SELECT ERROR_ID, ERROR_DATE, ERROR_CODE, DESCRIPTION, MESSAGE FROM\r\n"
				+ "WMLOG.WM_LOG_ERROR_TPE TPE\r\n"
				+ "WHERE TPE_TYPE like 'PE2'\r\n"
				+ "AND ERROR_DATE >= TRUNC(SYSDATE) AND FOLIO = '%s')\r\n"
				+ "WHERE ROWNUM <= 5";
		
		String folio = "";
		String wmCode = "";
		String authWmCode = "";
		String authAction = "";
		String ackWmCode = "";
		String ackAction = "";
		String creationDate = "";
		String codeToValidate = data.get("wmCodeFolio"); //100
		String codeAuth = data.get("wmCodeAuth"); //000
		String codeToValidateAck = data.get("wmCodeACK"); //101
		String responseRunGetFolio = "";
		String responseRunGetAuth = "";
		String responseRunGetAck = "";
		
		/******************************************Paso 1*************************************/
		addStep("Solicitar un folio, invocando el servicio **runGetFolio** de la Interface PE2 para una transaccion exitosa de pago con tarjeta");
		
		
		responseRunGetFolio = pe2Util.ejecutarRunGetFolio();
		System.out.println(responseRunGetFolio);
		
		folio = getSimpleDataXml(responseRunGetFolio, "folio");
		wmCode = getSimpleDataXml(responseRunGetFolio, "wmCode");
		creationDate = getSimpleDataXml(responseRunGetFolio, "creationDate");
		
		System.out.println("Folio: " + folio +" wmCode: " + wmCode + " Creation Date: " + creationDate);
		assertEquals(wmCode, codeToValidate, "Se obtiene WmCode diferente al esperado");
		
		/******************************************Paso 2*************************************/
		addStep("Solicitar autorizacion de pago con tarjeta invocando el servicio **runGetAuth** de la interface PE2");
		
		responseRunGetAuth = pe2Util.ejecutarRunGetAuth(folio, creationDate, 0);
		System.out.println("ResposeAuth: \r\n " + responseRunGetAuth);
		
		authWmCode = getSimpleDataXml(responseRunGetAuth, "wmCode");
		authAction = getSimpleDataXml(responseRunGetAuth, "action");
		
		System.out.println("wmCode: " + authWmCode + " Action: " + authAction);
		assertEquals(authWmCode, codeAuth, "Se obtiene WmCode diferente al esperado");
		
		/******************************************Paso 3*************************************/
		addStep("Solicitar confirmacion ACK del pago con tarjeta desde un navegador invocando servicio **runGetAuthAck**");
		
		responseRunGetAck = pe2Util.ejecutarRunGetAck(folio, creationDate);
		System.out.println(responseRunGetAck);
		
		ackWmCode = getSimpleDataXml(responseRunGetAck, "wmCode");
		ackAction = getSimpleDataXml(responseRunGetAck, "action");
		
		System.out.println("ack Code: " + ackWmCode + " ack Action: " + ackAction);
		assertEquals(ackWmCode, codeToValidateAck, "Se obtiene WmCode diferente al esperado");
		
		/******************************************Paso 4*************************************/
		addStep("Validar que la transaccion  de pago de tarjeta en la tabla TDC_TRANSACTION de la BD  **FCTDCQA**");
		
		String queryPaymentFormat = String.format(queryPayment, folio);
		System.out.println(queryPaymentFormat);
		
		SQLResult paymentSite1 = executeQuery(dbFCTDCQA_MTY,queryPaymentFormat);
		Thread.sleep(50000);
		SQLResult paymentSite2 = executeQuery(dbFCTDCQA_QRO,queryPaymentFormat);
		
		boolean validatePayment = paymentSite1.isEmpty();
		boolean validatePayment1 = paymentSite2.isEmpty();
		
		if(!validatePayment) {
			wmCode = paymentSite1.getData(0, "WM_CODE");
			authWmCode = paymentSite1.getData(0, "SW_AUTH_CODE");
			System.out.println("WM_CODE: "+ wmCode +" SW_AUTH_CODE: " + authWmCode);
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(paymentSite1);
		}else {
			wmCode = paymentSite2.getData(0, "WM_CODE");
			authWmCode = paymentSite2.getData(0, "SW_AUTH_CODE");
			System.out.println("WM_CODE: "+ wmCode +" SW_AUTH_CODE: " + authWmCode);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(paymentSite2);
		}
		assertFalse(validatePayment && validatePayment1,"No se obtuvo registro de transaccion de pago con tarjeta");
		
		/******************************************Paso 5*************************************/
		addStep("Validar que se encuentre el registro de la transaccion");
		
		String queryTransFormat = String.format(queryTransaction, folio);
		System.out.println(queryTransFormat);
		
		SQLResult transactionSite1 = executeQuery(dbFCSWQA, queryTransFormat);
		Thread.sleep(50000);
		SQLResult transactionSite2 = executeQuery(dbFCSWQA_QRO, queryTransFormat);
		boolean validateTransSite1 = transactionSite1.isEmpty();
		boolean validateTransSite2 = transactionSite2.isEmpty();
		String mti = "";
		String respCode = "";
		if(!validateTransSite1) {
			folio = transactionSite1.getData(0, "FOLIO");
			mti = transactionSite1.getData(0, "MTI");
			respCode = transactionSite1.getData(0, "RESP_CODE");
			authWmCode = transactionSite1.getData(0, "AUTH_ID_RES");
			System.out.println("Folio: " + folio);
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(transactionSite1);
		}else {
			folio = transactionSite2.getData(0, "FOLIO");
			mti = transactionSite2.getData(0, "MTI");
			respCode = transactionSite2.getData(0, "RESP_CODE");
			authWmCode = transactionSite2.getData(0, "AUTH_ID_RES");
			System.out.println("Folio: " + folio);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(transactionSite2);
		}
		assertFalse(validateTransSite1 && validateTransSite2, "No se encontro registro de transaccion");
		/******************************************Paso 6*************************************/
		addStep("Validar que se este registrando el securityLog");
		
		String securityFormat = String.format(querySecurityLog, folio);
		System.out.println(securityFormat);
		
		SQLResult securityLogSite1 = executeQuery(dbFCWMLTAEQA_MTY, securityFormat);
		Thread.sleep(50000);
		SQLResult securityLogSite2 = executeQuery(dbFCWMLTAEQA_QRO, securityFormat);
		
		boolean validateSecurityLogSite1 = securityLogSite1.isEmpty();
		boolean validateSecurityLogSite2 = securityLogSite2.isEmpty();
		
		if(!validateSecurityLogSite1) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(securityLogSite1);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(securityLogSite2);
		}
			
		assertFalse(validateSecurityLogSite1 && validateSecurityLogSite2, "La transaccion no fue registrada en el security Log");
		/******************************************Paso 7*************************************/
		addStep("Validar que no se encuentren registros de error de la PE2");
		String errorFormat = String.format(queryError, folio);
		SQLResult errorLogSite1 = executeQuery(dbFCWMLTAEQA_MTY, errorFormat);
		Thread.sleep(50000);
		SQLResult errorLogSite2 = executeQuery(dbFCWMLTAEQA_QRO, errorFormat);
		
		boolean validateErrorLogSite1 = errorLogSite1.isEmpty();
		boolean validateErrorLogSite2 = errorLogSite2.isEmpty();
		if (!validateErrorLogSite1) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(errorLogSite1);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(errorLogSite2);
		}
		assertTrue(validateErrorLogSite1 && validateErrorLogSite2, "Se obtuvieron registros de error");
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
		return " Prueba de regresion para comprobar la no afectacion en la funcionalidad "
				+ "principal de transaccionalidad de la interface FEMSA_PE2 al ser migrada "
				+ "de WM9.9 a WM10.5 para una transaccion exitosa de pago con tarjeta";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_012_PE2TransaccionExitosaPagoTarjeta_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
