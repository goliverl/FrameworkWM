package interfaces.pe2;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.openqa.selenium.support.ui.Wait;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import om.PE2;
import om.PE6;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

public class ATC_FT_014_PE2_PagoConPuntos_Credito  extends BaseExecution {
	
	/*
	 * 
	 * @cp MTC-FT-007 PE2_PTS Transaccion exitosa de pago con puntos con tarjeta de credito
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_014_PE2_PagoConPuntos_Credito_test(HashMap <String, String> data) throws Exception{
		
		/*
	 *Utilerias
	 ************************************************************************************************/
		utils.sql.SQLUtil dbFCTDCQA= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA, GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		utils.sql.SQLUtil dbFCTDCQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA_QRO, GlobalVariables.DB_USER_FCTDCQA_QRO, GlobalVariables.DB_PASSWORD_FCTDCQA_QRO); //NUEVA
		utils.sql.SQLUtil dbFCSWQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		utils.sql.SQLUtil dbFCSWQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO, GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		utils.sql.SQLUtil dbFCWMLTAEQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAEQA);
		utils.sql.SQLUtil dbFCWMLTAEQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO, GlobalVariables.DB_USER_FCWMLTAEQA_QRO, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		
		PE2 pe2Util = new PE2(data, testCase, null);
		PE6 pe6Util = new PE6(data, testCase, null);
		/*
		 * Variables
		 ********************************************************************************************/
		String queryTransaction = "SELECT TDC.APP, TDC.WM_CODE, TDC.FOLIO, TDC.CREATION_DATE, TDC.PLAZA, TDC.TIENDA, TDC.BANK, TDC.BIN, TDC.AMOUNT, TDC.SW_AUTH\r\n"
				+ "FROM TPEUSER.TDC_TRANSACTION TDC WHERE FOLIO= '%s' \r\n"
				+ "AND CREATION_DATE>= TRUNC(SYSDATE)";
		
		String queryRegistration = "SELECT TL.AUTH_ID_RES, TL.RESP_CODE, TL.SW_CODE, TL.COUNTER, TL.PLAZA, TL.TIENDA, TL.FOLIO, TL.AMOUNT, TL.CREATION_DATE\r\n"
				+ "FROM SWUSER.TPE_SW_TLOG tl\r\n"
				+ "WHERE CREATION_DATE>= TRUNC(SYSDATE)\r\n"
				+ "AND FOLIO = %s ";
		
		String queryError = "SELECT ERROR_ID, FOLIO, ERROR_DATE, SEVERITY, ERROR_TYPE, ERROR_CODE,\r\n"
				+ "DESCRIPTION, ACTION\r\n"
				+ "FROM WMLOG.WM_LOG_ERROR_TPE\r\n"
				+ "WHERE TPE_TYPE LIKE 'PE2' AND ERROR_DATE >= TRUNC(SYSDATE) AND FOLIO = %s \r\n"
				+ "ORDER BY ERROR_DATE DESC";
		
		String queryChannel = "SELECT * FROM ( SELECT APPLICATION, OPERATION, FOLIO, SOURCE, PLAZA, TIENDA, AMOUNT, CREATION_DATE\r\n"
				+ " FROM WMLOG.SECURITY_SESSION_LOG\r\n"
				+ " WHERE CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ " AND APPLICATION LIKE 'PE2'\r\n"
				+ " ORDER BY CREATION_DATE DESC)\r\n"
				+ "WHERE ROWNUM <= 3";
		
		String folio = "";
		String wmCode = "";
		String creationDate = "";
		String authAction = "";
		String authWmCode = "";
		String swAuth = "";
		String respCode = "";
		String codeTovalidate = "100";
		String codeTovalidate1 = "000";
		String codeTovalidate2 = "101";
		String responseRunGetFolio = "";
		String responseRunGetAuth = "";
		String resposeRunGetAuthAck = "";
		
		SoftAssert softAssert = new SoftAssert();
		/***************************************Paso 1***********************************************/
		addStep("Ejecutar el servicio **runGetFolio** desde el navegador  de la Interface PE2");

		responseRunGetFolio = pe2Util.ejecutarRunGetFolio();
		System.out.println(responseRunGetFolio);
		folio = getSimpleDataXml(responseRunGetFolio, "folio");
		wmCode = getSimpleDataXml(responseRunGetFolio, "wmCode");
		creationDate = getSimpleDataXml(responseRunGetFolio, "creationDate");
		
		System.out.println("Folio: " + folio + " wmCode: " + wmCode);
		softAssert.assertEquals(wmCode, codeTovalidate, "Paso 1: Se obtiene WmCode diferente al esperado");
		
		/****************************************Paso 2**********************************************/
		addStep("Ejecutar el servicio **runGetauth**  desde el navegador   de la interface PE2");
		
		responseRunGetAuth = pe2Util.ejecutarRunGetAuth(folio, creationDate, 0);
		System.out.println(responseRunGetAuth);
		
		folio = getSimpleDataXml(responseRunGetAuth, "folio");
		wmCode = getSimpleDataXml(responseRunGetAuth, "wmCode");
		
		System.out.println("Folio: " + folio + " wmCode: " + wmCode);
		softAssert.assertEquals(wmCode, codeTovalidate1, "Paso 2: Se obtiene WmCode diferente al esperado");
		
		/****************************************Paso 3***********************************************/
		addStep("Ejecutar el servicio  **RunGetauthack** desde un navegador de la interface PE6");
		
		resposeRunGetAuthAck = pe2Util.ejecutarRunGetAck(folio, creationDate);
		System.out.println(resposeRunGetAuthAck);
		
		authAction = getSimpleDataXml(resposeRunGetAuthAck, "action");
		authWmCode = getSimpleDataXml(resposeRunGetAuthAck, "wmCode");
		
		System.out.println("Action: " + authAction + " WmCode: " + authWmCode);
		softAssert.assertEquals(authWmCode, codeTovalidate2, "Paso 3: Se obtiene WmCode diferente al esperado");
		
		/****************************************Paso 4***********************************************/
		addStep("Validar que la transaccion se registro correctamente en la tabla TDC_TRANSACTION ");
		
		String transactionFormat = String.format(queryTransaction, folio);
		System.out.println(transactionFormat);
		
		SQLResult transactionSite1 = executeQuery(dbFCTDCQA, transactionFormat);
		Thread.sleep(50000);
		SQLResult transactionSite2 = executeQuery(dbFCTDCQA_QRO, transactionFormat);
		boolean validateTransaction = transactionSite1.isEmpty();
		if(!validateTransaction) {
			wmCode = transactionSite1.getData(0, "WM_CODE");
			swAuth = transactionSite1.getData(0, "SW_AUTH");
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(transactionSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(transactionSite2);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(transactionSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(transactionSite2);
		}
		assertFalse(validateTransaction, "La transaccion no se registro correctamente");
		
		/****************************************Paso 5***********************************************/
		addStep("Validar el registro  en la base de datos  *FCSWQA* ");
		
		String registrationFormat = String.format(queryRegistration, folio);
		System.out.println(registrationFormat);
		
		SQLResult registrationSite1 = executeQuery(dbFCSWQA_MTY, registrationFormat);
		Thread.sleep(50000);
		SQLResult registrationSite2 = executeQuery(dbFCSWQA_QRO, registrationFormat);
		
		boolean validateRegistration = registrationSite1.isEmpty();
		if(!validateRegistration) {
			folio = registrationSite1.getData(0, "FOLIO");
			respCode = registrationSite1.getData(0, "RESP_CODE");
			swAuth = registrationSite1.getData(0, "SW_CODE");
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(registrationSite1);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(registrationSite1);
		}
		assertFalse(validateRegistration);
		
		/****************************************Paso 6***********************************************/
		addStep("Validar que no se encuentren registros de error de la PE2");
		
		String errorFormat = String.format(queryError, folio);
		System.out.println(errorFormat);
		
		SQLResult errorLogSite1 = executeQuery(dbFCWMLTAEQA_MTY, errorFormat);
		Thread.sleep(50000);
		SQLResult errorLogSite2 = executeQuery(dbFCWMLTAEQA_QRO, errorFormat);
		
		boolean validateErrorLog = errorLogSite1.isEmpty();
		if (!validateErrorLog) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(errorLogSite1);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(errorLogSite1);
		}
		assertTrue(validateErrorLog, "Se encontraron registros de error");
		
		/****************************************Paso 7***********************************************/
		addStep("Verificar  que no  viajo por un canal seguro");
		
		SQLResult safeChannelSite1 = executeQuery(dbFCWMLTAEQA_MTY, queryChannel);
		Thread.sleep(50000);
		SQLResult safeChannelSite2 = executeQuery(dbFCWMLTAEQA_QRO, queryChannel);
		boolean validateSafeChannel = safeChannelSite1.isEmpty();
		if(!validateSafeChannel) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(safeChannelSite1);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(safeChannelSite1);
		}
		assertFalse(validateSafeChannel,  "No se obtuvieron registros");
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
		return "Transaccion exitosa de pago con puntos con tarjeta de credito";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_014_PE2_PagoConPuntos_Credito_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
}