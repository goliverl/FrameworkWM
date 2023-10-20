package interfaces.tpe.tsf;

import java.util.HashMap;

import javax.validation.constraints.AssertTrue;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import modelo.BaseExecution;
import util.GlobalVariables;
import util.RequestUtil;
import om.TPE_TSF;

import utils.sql.SQLUtil;
import utils.password.GenerateEncryptionPassword;
import utils.password.PasswordUtil;
import utils.sql.SQLResult;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

public class ATC_FT_36_TPE_TSFenvioDineroExitosoOxxo_Oxxo extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_36_TPE_TSFenvioDineroExitosoOxxo_Oxxo_test (HashMap <String, String> data) throws Exception {
		
		/**
		 * Proyecto: Servicios Electronicos (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-036-C2 Transaccion exitosa de Envio de Dinero de OXXO a OXXO por SYC.
		 * @author 
		 * @date 
		 */
		
		/*
		 **Utileria*******************************************************************************/
		utils.sql.SQLUtil dbFCTPE_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		utils.sql.SQLUtil dbFCTPE_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE_QRO, GlobalVariables.DB_USER_FCTPE_QRO, GlobalVariables.DB_PASSWORD_FCTPE_QRO);
		utils.sql.SQLUtil dbFCWMLTAEQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QAVIEW);
		utils.sql.SQLUtil dbFCWMLTAEQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO, GlobalVariables.DB_USER_FCWMLTAEQA_QA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		
		
		TPE_TSF tpeTSF = new TPE_TSF(data, testCase, null);
		/*
		 **Variables******************************************************************************/
		
		String queryTransfer = "SELECT OPERATION, WM_CODE, MTI, RESP_CODE, APPLICATION, ENTITY, FOLIO, WM_DESC\r\n"
				+ "FROM TPEUSER.TPE_FR_TRANSACTION \r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE) \r\n"
				+ "AND FOLIO IN ('%s', '%s', '%s', '%s') AND  APPLICATION = 'TSF' ORDER BY CREATION_DATE DESC";
		
		String queryChannel = "SELECT * FROM (SELECT APPLICATION, ENTITY, OPERATION, SOURCE, FOLIO, PLAZA, TIENDA, CREATION_DATE\r\n"
				+ "FROM WMLOG.SECURITY_SESSION_LOG\r\n"
				+ "WHERE APPLICATION like '%TSF%' AND ENTITY LIKE '%OXXO%'\r\n"
				+ "ORDER BY CREATION_DATE DESC)\r\n"
				+ "WHERE ROWNUM <=5";
		
		String queryError = "SELECT * FROM (SELECT ERROR_ID, FOLIO, CR_PLAZA, CR_TIENDA, ERROR_DATE, ERROR_TYPE, ERROR_CODE, DESCRIPTION\r\n"
				+ "FROM WMLOG.WM_LOG_ERROR_TPE\r\n"
				+ "WHERE TPE_TYPE LIKE '%TSF%'\r\n"
				+ "AND ERROR_DATE >= TRUNC(SYSDATE) ORDER BY ERROR_DATE DESC)\r\n"
				+ "WHERE ROWNUM <=5";
		
		String wmCode = "";
		String folio1, folio2, folio3, folio4;
		String creationDate = "";
		String responseEjecutarQRY01pt2 = ""; 
		String responseEjecutarQRY01 = "";
		String responseEjecutarQRY02 = "";
		String responseEjecutarTRN01 = "";
		String responseEjecutarTRN02 = "";
		String responseEjecutarTRN03 = "";
		String codeToValidate = "101";
		String codeToValidate1 = "100";
		String codeToValidate2 = "000";
		
		SoftAssert softAssert = new SoftAssert();
		/*****************************************Paso 1******************************************/
		addStep("Ejecutar el servicio de la consulta de Tabuladores para la TPE_TSF del server QA8");
		
		responseEjecutarQRY01pt2 = tpeTSF.ejecutarQRY01pt2();
		System.out.println(responseEjecutarQRY01pt2);
		
		wmCode = RequestUtil.getWmCodeXml(responseEjecutarQRY01pt2);
		folio1 = RequestUtil.getFolioXml(responseEjecutarQRY01pt2);
		System.out.println("WMCODE: " + wmCode + " FOLIO: " + folio1);
		
		softAssert.assertEquals(wmCode, codeToValidate, "Paso 1: Se obtiene WmCode diferente al esperado");
		//Arroja "wmCode value="199" desc="El envío no puede ser cobrado en esta tienda, favor de cobrarlo en cualquier otra" action=""/>"
		/*****************************************Paso 2******************************************/
		addStep("Ejecutar el servicio de la consulta de comisión por monto para la TPE_TSF del server QA8");
		
		responseEjecutarQRY01 = tpeTSF.ejecutarQRY01();
		System.out.println(responseEjecutarQRY01);
		
		wmCode = RequestUtil.getWmCodeXml(responseEjecutarQRY01);
		folio2 = RequestUtil.getFolioXml(responseEjecutarQRY01);
		System.out.println("WMCODE: " + wmCode + " FOLIO: " + folio2);
		
		softAssert.assertEquals(wmCode, codeToValidate, "Paso 2: Se obtiene WmCode diferente al esperado");
		
		/*****************************************Paso 3******************************************/
		addStep("Ejecutar el servicio de la consulta de beneficiario para la TPE_TSF del server QA8");
		
		responseEjecutarQRY02 = tpeTSF.ejecutarQRY02();
		System.out.println(responseEjecutarQRY02);
		
		wmCode = RequestUtil.getWmCodeXml(responseEjecutarQRY02);
		folio3 = RequestUtil.getFolioXml(responseEjecutarQRY02);
		System.out.println("WMCODE: " + wmCode + " FOLIO: " + folio3);
		
		softAssert.assertEquals(wmCode, codeToValidate, "Paso 3: Se obtiene WmCode diferente al esperado");
		
		/*****************************************Paso 4******************************************/
		addStep("Ejecutar el servicio de solicitud de Folio para envío de dinero de la TPE_TSF del server QA8");
		
		responseEjecutarTRN01 = tpeTSF.ejecutarTRN01();
		System.out.println(responseEjecutarTRN01);
		
		wmCode = RequestUtil.getWmCodeXml(responseEjecutarTRN01);
		folio4 = RequestUtil.getFolioXml(responseEjecutarTRN01);
		creationDate = RequestUtil.getCreationDate(responseEjecutarTRN01);
		System.out.println("WMCODE: " + wmCode + " FOLIO: " + folio4);
		
		softAssert.assertEquals(wmCode, codeToValidate1, "Paso 4: Se obtiene WmCode diferente al esperado");
		
		/*****************************************Paso 5******************************************/
		addStep("Ejecutar el servicio de solicitud de Autorización de envío de dinero de oxxo a oxxo de la TPE_TSF del servidor QA8");
		
		responseEjecutarTRN02 = tpeTSF.ejecutarTRN02(folio4, creationDate);
		System.out.println(responseEjecutarTRN02);
		
		wmCode = RequestUtil.getWmCodeXml(responseEjecutarTRN02);
		folio4 = RequestUtil.getFolioXml(responseEjecutarTRN02);
		creationDate = RequestUtil.getCreationDate(responseEjecutarTRN02);
		System.out.println("WMCODE: " + wmCode + " FOLIO: " + folio4 + " CreationDate: " + creationDate);
		
		softAssert.assertEquals(wmCode, codeToValidate2, "Paso 5: Se obtiene WmCode diferente al esperado");
		
		/*****************************************Paso 6******************************************/
		addStep("Ejecutar el servicio de solicitud de Confirmación ACK de envío de dinero de oxxo a oxxo de la TPE_TSF del servidor QA8");
		
		responseEjecutarTRN03 = tpeTSF.ejecutarTRN03(folio4, creationDate);
		System.out.println(responseEjecutarTRN03);
		
		wmCode = RequestUtil.getWmCodeXml(responseEjecutarTRN03);
		folio4 = RequestUtil.getFolioXml(responseEjecutarTRN03);
		System.out.println("WMCODE: " + wmCode + " FOLIO: " + folio4);
		
		softAssert.assertEquals(wmCode, codeToValidate, "Paso 6: Se obtiene WmCode diferente al esperado");
		
		/*****************************************Paso 7******************************************/
		addStep("Validar la Transacción exitosa de Envío de Dinero de OXXO a OXXO en la tabla TPE_FR_TRANSACTION  de la BD  **FCTPEQA** ");
		
		String transferFormat = String.format(queryTransfer, folio1, folio2, folio3, folio4);
		System.out.println(transferFormat);
		
		SQLResult succesTransactionSite1 = dbFCTPE_MTY.executeQuery(transferFormat);
		Thread.sleep(50000);
		SQLResult succesTransactionSite2 = dbFCTPE_QRO.executeQuery(transferFormat);
		
		boolean validateTransaction = succesTransactionSite1.isEmpty();
		if(!validateTransaction) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(succesTransactionSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(succesTransactionSite2);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(succesTransactionSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(succesTransactionSite2);
		}
		assertFalse(validateTransaction, "No se obtiene registro de transaccion");
		
		/*****************************************Paso 8******************************************/
		addStep("Validar que se hayan registrado las invocaciones a las interface TPE_TSF ya que no van por el canal seguro de F5");
		
		SQLResult safeChannelSite1 = dbFCWMLTAEQA_MTY.executeQuery(queryChannel);
		Thread.sleep(50000);
		SQLResult safeChannelSite2 = dbFCWMLTAEQA_QRO.executeQuery(queryChannel);
		
		boolean validateChannel = safeChannelSite1.isEmpty();
		if(!validateChannel) {
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
		assertFalse(validateChannel, "No se obtiene registro de invocaciones");
		
		/*****************************************Paso 9******************************************/
		addStep("Validar que no se encuentren registros de error de la TSF");
		
		SQLResult tsfErrorSite1 = dbFCWMLTAEQA_MTY.executeQuery(queryError);
		Thread.sleep(50000);
		SQLResult tsfErrorSite2 = dbFCWMLTAEQA_QRO.executeQuery(queryError);
		
		boolean validateTsfError = tsfErrorSite1.isEmpty();
		if(!validateTsfError) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(tsfErrorSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(tsfErrorSite2);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(tsfErrorSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(tsfErrorSite2);
		}
		assertTrue(validateTsfError, "Se obtienen registros de error");
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
		return "Construida. MTC-FT-036 Transacción exitosa de Envío de Dinero de OXXO a OXXO por SYC";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_36_TPE_TSFenvioDineroExitosoOxxo_Oxxo_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}	
}
