package interfaces.tpe.tsf;

import java.util.HashMap;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import modelo.BaseExecution;
import util.GlobalVariables;
import util.RequestUtil;
import om.TPE_TSF;
import om.FCP_WMx86;

import utils.sql.SQLUtil;
import utils.password.GenerateEncryptionPassword;
import utils.password.PasswordUtil;
import utils.sql.SQLResult;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

public class ATC_FT_48_TPE_TSF_DispersionExitosaMoneyGramtest extends BaseExecution{
	@Test (dataProvider = "data-provider")
	public void ATC_FT_48_TPE_TSF_DispersionExitosaMoneyGramtest_test (HashMap<String, String> data) throws Exception{
		
		/**
		 * Proyecto: Servicios Electronicos (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-048-C2 Transaccion exitosa de Dispersion de Dinero de Money Gram
		 * @author 
		 * @date 
		 */
		
		/*
		 * Utileria*************************************************************************/
		utils.sql.SQLUtil dbFCTPE_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		utils.sql.SQLUtil dbFCTPE_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE_QRO, GlobalVariables.DB_USER_FCTPE_QRO, GlobalVariables.DB_PASSWORD_FCTPE_QRO);
		utils.sql.SQLUtil dbFCWMLTAEQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QAVIEW);
		utils.sql.SQLUtil dbFCWMLTAEQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO, GlobalVariables.DB_USER_FCWMLTAEQA_QA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		
		TPE_TSF tpeTSF = new TPE_TSF(data, testCase, null);
		FCP_WMx86 fcp = new FCP_WMx86(data, testCase, null);
		/*
		 * Variables************************************************************************/
		String queryTransaction = "SELECT * FROM (SELECT APPLICATION, ENTITY, OPERATION, FOLIO, CREATION_DATE,PLAZA, TIENDA, WM_CODE,\r\n"
				+ "WM_DESC\r\n"
				+ "FROM TPEUSER.TPE_FR_TRANSACTION WHERE APPLICATION= 'TSF' AND ENTITY = 'MGI' AND\r\n"
				+ "CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "ORDER BY CREATION_DATE DESC)\r\n"
				+ "WHERE ROWNUM <= 4";
		
		String querySafeChannel = "SELECT * FROM (SELECT APPLICATION, ENTITY, OPERATION, FOLIO, PLAZA, TIENDA,\r\n"
				+ "CREATION_DATE\r\n"
				+ "FROM WMLOG.SECURITY_SESSION_LOG WHERE APPLICATION ='TSF' AND ENTITY='MGI' AND\r\n"
				+ "CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "ORDER BY CREATION_DATE DESC)\r\n"
				+ "WHERE ROWNUM <=4";
		
		String queryError = "SELECT * FROM (SELECT TPE.ERROR_ID, TPE.FOLIO, TPE.CR_PLAZA, TPE.CR_TIENDA, TPE.ERROR_DATE, TPE.ERROR_CODE, TPE.DESCRIPTION\r\n"
				+ "FROM WMLOG.WM_LOG_ERROR_TPE TPE\r\n"
				+ "WHERE (TPE_TYPE like '%TSF%' OR TPE_TYPE like '%FR%') AND ERROR_DATE >= TRUNC(SYSDATE)\r\n"
				+ "ORDER BY error_date DESC)\r\n"
				+ "WHERE ROWNUM <=4";
		
		String wmCode = "";
		String folio, folio1, folio2, folio3= "";
		String creationDate = "";
		String responseQRY01 = "";
		String responseTRN01 = "";
		String responseTRN02 = "";
		String responseTRN03 = "";
		String codeToValidate = "101";
		String codeToValidate2 = "100";
		String codeToValidate3 = "000";
		
		SoftAssert softAssert = new SoftAssert();
		
		
		/***************************************Paso 1 *************************************/
		addStep("Ejecutar el servicio de la consulta de referencia de envío Money Gram para la interface TPE_TSF");
		
		responseQRY01 = tpeTSF.ejecutarQRY01_MG();
		System.out.println(responseQRY01);
		
		wmCode = RequestUtil.getWmCodeXml(responseQRY01);
		folio = RequestUtil.getFolioXml(responseQRY01);
		creationDate = RequestUtil.getCreationDate(responseQRY01);
		System.out.println("WMCODE: " + wmCode +  " Folio: " + folio + " CreationDate: "+ creationDate);
		softAssert.assertEquals(wmCode, codeToValidate, "Paso 1: Se obtuvo WmCode diferente al esperado");
		
		/***************************************Paso 2 *************************************/
		addStep("Ejecutar el servicio de solicitud de Folio para la transacción de Dispersión de Dinero de Money Gram de la interface TPE_TSF");
		
		responseTRN01 = tpeTSF.ejecutarTRN01();
		System.out.println(responseTRN01);

		folio1 = RequestUtil.getFolioXml(responseTRN01);
		wmCode = RequestUtil.getWmCodeXml(responseTRN01);
		creationDate = RequestUtil.getCreationDate(responseTRN01);
		System.out.println("FOLIO: " + folio1 + " wmCode:" + wmCode +" CreationDate: " + creationDate);
		
		softAssert.assertEquals(wmCode, codeToValidate2, "Paso 2: Se obtuvo WmCode diferente al esperado");
		
		/***************************************Paso 3 *************************************/
		addStep("Ejecutar el servicio de solicitud de autorización para la transacción de Dispersión de Dinero de Money Gram de la interface TPE_TSF");
		
		responseTRN02 = fcp.ejecutarTRN02_MG(folio1, creationDate);
		System.out.println(responseTRN02);
		
		wmCode = RequestUtil.getWmCodeXml(responseTRN02);
		folio2 = RequestUtil.getFolioXml(responseTRN02);
		creationDate = RequestUtil.getCreationDate(responseTRN02);
		System.out.println("WMCODE: " + wmCode + " Folio: " + folio2 + " CreationDate: " + creationDate);
		
		softAssert.assertEquals(wmCode, codeToValidate3, "Paso 3: Se obtuvo WmCode diferente al esperado");
		
		/***************************************Paso 4 *************************************/
		addStep("Ejecutar el servicio de solicitud de confirmación ACK de Dispersión de Dinero de Money Gram de la interface TPE_TSF");
		
		responseTRN03 = tpeTSF.ejecutarTRN03(folio2, creationDate);
		System.out.println(responseTRN03);
		
		wmCode = RequestUtil.getWmCodeXml(responseTRN03);
		folio3 = RequestUtil.getFolioXml(responseTRN03);
		creationDate = RequestUtil.getCreationDate(responseTRN03);
		System.out.println("WMCODE: " + wmCode + " Folio: " + folio3 + " CreationDate: " + creationDate);
		
		softAssert.assertEquals(wmCode, codeToValidate, "Paso 4: se obtuvo WmCode diferente al esperado");
		
		/***************************************Paso 5 *************************************/
		addStep("Consultar el registro de la transacción de Dispersión de Dinero de Money Gram en la tabla TPE_FR_TRANSACTION de la BD FCTPEQA");
		
		String transactionFormat = String.format(queryTransaction, folio, folio3);
		System.out.println(transactionFormat);
		
		SQLResult transactionSite1 = executeQuery(dbFCTPE_MTY, transactionFormat);
		Thread.sleep(50000);
		SQLResult transactionSite2 = executeQuery(dbFCTPE_QRO, transactionFormat);
		
		boolean validateTransaction = transactionSite1.isEmpty();
		if(!validateTransaction) {
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
		assertFalse(validateTransaction, "No se registro la transaccion de dispersion de dinero MoneyGram");
		
		/***************************************Paso 6 *************************************/
		addStep("Validar el registro de las invocaciones de la interface TPE_TSF en la tabla SECURITY_SESSION_LOG de la BD FCWMLTAQ.FEMCOM.NET");
		
		SQLResult safeChannelSite1 = executeQuery(dbFCWMLTAEQA_MTY, querySafeChannel);
		Thread.sleep(50000);
		SQLResult safeChannelSite2 = executeQuery(dbFCWMLTAEQA_QRO, querySafeChannel);
		
		System.out.println(queryError);
		
		boolean validateSafeChannel = safeChannelSite1.isEmpty();
		if(!validateSafeChannel) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(safeChannelSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(safeChannelSite2);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(safeChannelSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(safeChannelSite2);
		}
		assertFalse(validateSafeChannel, "No se obtuvo registro");
		
		/***************************************Paso 7 *************************************/
		addStep("Validar que no se registre errores de la interface TPE_TSF y Framework en la tabla WM_LOG_ERROR_TPE de la BD FCWMLTAQ.FEMCOM.NET, "
				+ "correspondientes a la transacción realizada de Dispersión de Dinero Money Gram");
		
		SQLResult dispersionError = executeQuery(dbFCWMLTAEQA_MTY, queryError);
		System.out.println(queryError);
		
		boolean validateError = dispersionError.isEmpty();
		if(!validateError) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(dispersionError);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(dispersionError);
		}
		assertTrue(validateError, "Se obtuvieron errores en la transaccion");
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
		return "Construida. FT-MTC-048 Transacción exitosa de Dispersión de Dinero de MoneyGram";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_48_TPE_TSF_DispersionExitosaMoneyGramtest_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
