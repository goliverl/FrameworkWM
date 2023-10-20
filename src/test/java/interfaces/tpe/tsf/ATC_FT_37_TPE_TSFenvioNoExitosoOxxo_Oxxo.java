package interfaces.tpe.tsf;

import java.util.HashMap;

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

public class ATC_FT_37_TPE_TSFenvioNoExitosoOxxo_Oxxo extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_37_TPE_TSFenvioNoExitosoOxxo_Oxxo_test (HashMap<String, String> data) throws Exception {
		
		/**
		 * Proyecto: Servicios Electronicos (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-037-C2 Transacción NO exitosa de Envio de Dinero de OXXO a OXXO por SYC.
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
		String queryTransaction = "SELECT APPLICATION, ENTITY, OPERATION, SOURCE,TIENDA, PLAZA, FOLIO,\r\n"
				+ "WM_CODE, WM_DESC\r\n"
				+ "FROM TPEUSER.TPE_FR_TRANSACTION\r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE) AND APPLICATION ='TSF' AND TIENDA = '50VNL' AND FOLIO IN ('%s', '%s', '%s', '%s')\r\n"
				+ "ORDER BY CREATION_DATE DESC";
		
		String queryError = "SELECT ERROR_ID, FOLIO, ERROR_DATE, ERROR_CODE,\r\n"
				+ "DESCRIPTION, MESSAGE\r\n"
				+ "FROM WMLOG.WM_LOG_ERROR_TPE\r\n"
				+ "WHERE TPE_TYPE = 'TPE.TSF' AND ERROR_DATE >= TRUNC(SYSDATE)AND\r\n"
				+ "FOLIO IN ('%s', '%s', '%s', '%s')\r\n"
				+ "ORDER BY ERROR_DATE DESC";
		
		String wmCode = "";
		String folio1, folio2, folio3, folio4 = "";
		String creationDate = "";
		String responseEjecutarQRY01pt2 = "";
		String responseEjecutarQRY01 = "";
		String responseEjecutarQRY02 = "";
		String responseEjecutarTRN01 = "";
		String responseEjecutarTRN02 = "";
		String codeToValidate = "101";
		String codeToValidate1 = "100";
		
		SoftAssert softAssert = new SoftAssert();
		/***************************************Paso 1********************************************/
		addStep("Ejecutar el servicio de la consulta de Tabuladores para la TPE_TSF del server QA8");
		
		responseEjecutarQRY01pt2 = tpeTSF.ejecutarQRY01pt2();
		System.out.println(responseEjecutarQRY01pt2);
		
		wmCode = RequestUtil.getWmCodeXml(responseEjecutarQRY01pt2);
		folio1 = RequestUtil.getFolioXml(responseEjecutarQRY01pt2);
		System.out.println("WMCODE: " + wmCode + " FOLIO: " + folio1);
		
		softAssert.assertEquals(wmCode, codeToValidate, "Paso 1: Se obtiene WmCode diferente al esperado");
		
		/***************************************Paso 2********************************************/
		addStep("Ejecutar el servicio de la consulta de comisión  por el respectivo monto a enviar para TPE_TSF del server QA8");
		
		responseEjecutarQRY01 = tpeTSF.ejecutarQRY01();
		System.out.println(responseEjecutarQRY01);
		
		wmCode = RequestUtil.getWmCodeXml(responseEjecutarQRY01);
		folio2 = RequestUtil.getFolioXml(responseEjecutarQRY01);
		System.out.println("WMCODE: " + wmCode + " FOLIO: " + folio2);
		
		softAssert.assertEquals(wmCode, codeToValidate, "Paso 2: Se obtiene WmCode diferente al esperado");
		
		/***************************************Paso 3********************************************/
		addStep("Ejecutar la consulta para validar el beneficiario para la TPE_TSF del server QA8");
		
		responseEjecutarQRY02 = tpeTSF.ejecutarQRY02();
		System.out.println(responseEjecutarQRY02);
		
		wmCode = RequestUtil.getWmCodeXml(responseEjecutarQRY02);
		folio3 = RequestUtil.getFolioXml(responseEjecutarQRY02);
		System.out.println("WMCODE: " + wmCode + " FOLIO: " + folio3);
		
		softAssert.assertEquals(wmCode, codeToValidate, "Paso 3: Se obtiene WmCode diferente al esperado");
		
		/***************************************Paso 4********************************************/
		addStep("Ejecutar la siguiente consulta para la  solicitud de Folio de la TPE_TSF del server QA8");
		
		responseEjecutarTRN01 = tpeTSF.ejecutarTRN01();
		System.out.println(responseEjecutarTRN01);
		
		wmCode = RequestUtil.getWmCodeXml(responseEjecutarTRN01);
		folio4 = RequestUtil.getFolioXml(responseEjecutarTRN01);
		creationDate = RequestUtil.getCreationDate(responseEjecutarTRN01);
		System.out.println("WMCODE: " + wmCode + " FOLIO: " + folio4 + " CREATIONDATE: " + creationDate);
		
		softAssert.assertEquals(wmCode, codeToValidate1, "Paso 4: Se obtiene WmCode diferente al esperado");
		
		/***************************************Paso 5********************************************/
		addStep("Ejecutar el request para la solicitud de autorización del envío de dinero de la TPE_TSF del server QA8 ");
		
		responseEjecutarTRN02 = tpeTSF.ejecutarTRN02(folio1, creationDate);
		System.out.println(responseEjecutarTRN02);
		
		wmCode = RequestUtil.getWmCodeXml(responseEjecutarTRN02);
		System.out.println("WMCODE: " + wmCode);
		softAssert.assertEquals(wmCode, codeToValidate, "Paso 5: Se obtiene WmCode diferente al esperado");
		
		/***************************************Paso 6********************************************/
		addStep("Validar registro de transaccion en la BD * FCTPEQA* en site 1 y site 2");
		
		String transactionFormat = String.format(queryTransaction, folio1, folio2, folio3, folio4);
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
		assertFalse(validateTransaction, "No se obtiene registro de transaccion");
		
		/***************************************Paso 7********************************************/
		addStep(" Ejecutar la siguiente consulta en la base de datos *FCWMLTAEQA* buscando que no se encuentren registros de error de la TSF");
		
		String errorFormat = String.format(queryError, folio1, folio2, folio3, folio4);
		System.out.println(errorFormat);
		SQLResult tsfErrorSite1 = executeQuery(dbFCWMLTAEQA_MTY, errorFormat);
		Thread.sleep(50000);
		SQLResult tsfErrorSite2 = executeQuery(dbFCWMLTAEQA_QRO, errorFormat);
		
		boolean validateError = tsfErrorSite1.isEmpty();
		if(!validateError) {
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
		assertTrue(validateError, "Se obtienen registros de error");
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
		return "Construida. MTC-FT-037 Transacción NO exitosa de Envío de Dinero de OXXO a OXXO por SYC";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_37_TPE_TSFenvioNoExitosoOxxo_Oxxo_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
