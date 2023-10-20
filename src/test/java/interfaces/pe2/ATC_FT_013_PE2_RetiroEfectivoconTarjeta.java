package interfaces.pe2;

import java.util.HashMap;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import modelo.BaseExecution;
import om.PE2;
import util.GlobalVariables;
import utils.sql.SQLResult;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;


/*
 * 
 * @cp  MTC-FT-006 PE2_CAB Transaccion exitosa de retiro de efectivo con tarjeta
 * @author Oliver Martinez
 * @date 2022/09/16
 * 
 */


public class ATC_FT_013_PE2_RetiroEfectivoconTarjeta extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_013_PE2_RetiroEfectivoconTarjeta_test (HashMap <String, String> data) throws Exception {
		/*Utilerias*****************************************************************************************/
		utils.sql.SQLUtil dbFCTDCQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA, GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA); //NUEVA
		utils.sql.SQLUtil dbFCTDCQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA_QRO, GlobalVariables.DB_USER_FCTDCQA_QRO, GlobalVariables.DB_PASSWORD_FCTDCQA_QRO); //NUEVA
		utils.sql.SQLUtil dbFCWMLTAEQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QAVIEW);
		utils.sql.SQLUtil dbFCWMLTAEQA_QRO = new utils.sql.SQLUtil (GlobalVariables.DB_HOST_FCWMLTAEQA_QRO, GlobalVariables.DB_USER_FCWMLTAEQA_QA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		utils.sql.SQLUtil dbFCSWQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		utils.sql.SQLUtil dbFCSWQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO, GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		
		PE2 pe2Util = new PE2(data, testCase, null);
		
		/*Variables*****************************************************************************************/
		
		String regTransaction = "SELECT TDC.WM_CODE, TDC.APP, TDC.FOLIO, TDC.CREATION_DATE, TDC.PLAZA, TDC.TIENDA, TDC.AMOUNT, TDC.SW_AUTH_CODE\r\n"
				+ "FROM TPEUSER.TDC_TRANSACTION TDC\r\n"
				+ "WHERE FOLIO= '%s'\r\n"
				+ "AND CREATION_DATE >= TRUNC(SYSDATE)";
		
		String regTransaction2 = "SELECT TL.AUTH_ID_RES, TL.RESP_CODE, TL.SW_CODE, TL.COUNTER, TL.PLAZA, TL.TIENDA, TL.APPLICATION, TL.FOLIO\r\n"
				+ "FROM SWUSER.TPE_SW_TLOG tl\r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "AND FOLIO = '%s'";
		
		String errorQuery = "SELECT * FROM (SELECT ERROR_ID, FOLIO, ERROR_CODE, DESCRIPTION, MESSAGE, ACTION FROM WMLOG.WM_LOG_ERROR_TPE\r\n"
				+ " WHERE TPE_TYPE LIKE 'PE2' AND ERROR_DATE >= TRUNC (SYSDATE) AND FOLIO = '%s'\r\n"
				+ " ORDER BY ERROR_DATE DESC)\r\n"
				+ "WHERE ROWNUM <=5";
		
		String channelQuery = "SELECT APPLICATION, OPERATION, SOURCE, FOLIO, PLAZA, TIENDA, CREATION_DATE \r\n"
				+ "FROM WMLOG.SECURITY_SESSION_LOG\r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "AND APPLICATION LIKE 'PE2' AND FOLIO = '%s'\r\n"
				+ "ORDER BY CREATION_DATE DESC";
		
		String folio, folioAuth= "";
		String wmCode = "";
		String authWmCode = "";
		String responseRunGetFolio = "";
		String responseRunGetAuth = "";
		String responseRunGetAuthAck = "";
		String action = "";
		String creationDate = "";
		String swAuthCode = "";
		String respCode = "";
		String swAuth = "";
		String codeToValidate = "100";
		String codeToValidate1 = "000";
		String codeToValidate2 = "101";
		
		SoftAssert softAssert = new SoftAssert();
		/*******************************Paso 1**********************************************/
		addStep("Ejecutar el servicio  runGetFolio desde el navegador  de la Interface PE2.");
		
		responseRunGetFolio = pe2Util.ejecutarRunGetFolio();
		System.out.println(responseRunGetFolio);
		
		folio = getSimpleDataXml(responseRunGetFolio, "folio");
		wmCode =getSimpleDataXml(responseRunGetFolio, "wmCode");
		creationDate = getSimpleDataXml(responseRunGetFolio, "creationDate");
		assertEquals(wmCode, codeToValidate, "Se obtiene WmCode diferente al esperado");
		
		/*******************************Paso 2**********************************************/
		addStep("Ejecutar el servicio runGetauth desde el navegador  de la Interface PE2.");
		
		responseRunGetAuth = pe2Util.ejecutarRunGetAuth(folio, creationDate, 0);
		System.out.println(responseRunGetAuth);
		
		folioAuth = getSimpleDataXml(responseRunGetAuth, "folio");
		authWmCode = getSimpleDataXml(responseRunGetAuth, "wmCode");
		assertEquals(authWmCode, codeToValidate1, "Se obtiene WmCode diferente al esperado");
		
		/*******************************************Paso 3**********************************************/
		addStep("Ejecutar el servicio RunGetauthack desde el navegador  de la Interface PE6.");
		
		responseRunGetAuthAck = pe2Util.ejecutarRunGetAck(folio, creationDate);
		System.out.println(responseRunGetAuthAck);
		
		authWmCode = getSimpleDataXml(responseRunGetAuthAck, "wmCode");
		action = getSimpleDataXml(responseRunGetAuthAck, "action");
		assertEquals(authWmCode, codeToValidate2, "Se obtiene WmCode diferente al esperado");
		
		/*******************************************Paso 4**********************************************/
		
		addStep("Validar que la transaccion se registro correctamente en la tabla TDC_TRANSACTION.");
		
		String transactionFormat = String.format(regTransaction, folio);
		System.out.println(transactionFormat);
		
		SQLResult transactionSite1 = dbFCTDCQA_MTY.executeQuery(transactionFormat);
		Thread.sleep(50000);
		SQLResult transactionSite2 = dbFCTDCQA_QRO.executeQuery(transactionFormat);
		
		boolean validateTrans  = transactionSite1.isEmpty();
		if(!validateTrans) {
			wmCode = transactionSite1.getData(0, "Wm_code");
			swAuthCode = transactionSite1.getData(0, "Sw_auth_code");
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(transactionSite1);
		}else if (!transactionSite2.isEmpty()){
			wmCode = transactionSite2.getData(0, "Wm_code");
			swAuthCode = transactionSite2.getData(0, "Sw_auth_code");
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(transactionSite2);
		}
		assertFalse(validateTrans && transactionSite2.isEmpty(), "La transaccion no se registro correctamente");
		
		/*******************************************Paso 5**********************************************/
	
		addStep(" validar el registro  en la base de datos  FCSWQA.");
		
		String transactionFormat2 = String.format(regTransaction2, folio);
		System.out.println(transactionFormat2);
		
		SQLResult validateTrans2Site1 = dbFCSWQA_MTY.executeQuery(transactionFormat2);
		Thread.sleep(50000);
		SQLResult validateTrans2Site2 = dbFCSWQA_QRO.executeQuery(transactionFormat2);
		
		boolean hasData2 = validateTrans2Site1.isEmpty();
		if(!hasData2) {
			folio = validateTrans2Site1.getData(0, "FOLIO");
			respCode = validateTrans2Site1.getData(0, "RESP_CODE");
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(validateTrans2Site1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(validateTrans2Site2);
		}else if (!validateTrans2Site2.isEmpty()){
			folio = validateTrans2Site2.getData(0, "FOLIO");
			respCode = validateTrans2Site2.getData(0, "RESP_CODE");
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(validateTrans2Site2);
		}
		assertFalse(hasData2 && validateTrans2Site2.isEmpty(), "La transaccion no se registro correctamente");
		
		/*******************************************Paso 6**********************************************/
		
		addStep("Validar que no se encuentren registros de error de la PE2 en la tabla WM_LOG_ERROR_TPE.");
		
		String errorFormat = String.format(errorQuery , folio);

		SQLResult getErrorsS1 = dbFCWMLTAEQA_MTY.executeQuery(errorFormat);
		SQLResult getErrorsS2 = dbFCWMLTAEQA_QRO.executeQuery(errorFormat);
		System.out.println(errorFormat);
		boolean validateGetErrors = getErrorsS1.isEmpty();
		if (!validateGetErrors) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(getErrorsS1);
		}else if(!getErrorsS2.isEmpty()){
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(getErrorsS2);
		}
		assertTrue(validateGetErrors && getErrorsS2.isEmpty(), "Se encontraron registros de error");
		
		/*******************************************Paso 7**********************************************/
		
		addStep("Verificar  que no  viajo por un canal seguro.");

		String channelFormat = String.format(channelQuery, folio);
		
		SQLResult safeChannel = dbFCWMLTAEQA_MTY.executeQuery(channelFormat);
		System.out.println(channelFormat);
		
		boolean validateSafeChannel = safeChannel.isEmpty();
		if(!validateSafeChannel) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(safeChannel);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(safeChannel);
		}
		assertFalse(validateSafeChannel, "No se obtuvieron registros");
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
		return "Transaccion exitosa de retiro de efectivo con tarjeta";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_013_PE2_RetiroEfectivoconTarjeta_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
