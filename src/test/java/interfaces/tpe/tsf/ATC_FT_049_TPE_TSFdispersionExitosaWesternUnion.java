package interfaces.tpe.tsf;

import java.util.HashMap;

import javax.validation.constraints.AssertFalse;
import javax.ws.rs.core.Request;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import modelo.BaseExecution;
import util.GlobalVariables;
import util.RequestUtil;
import om.FCP_WMx86;
import om.TPE_TSF;

import utils.sql.SQLUtil;
import utils.password.GenerateEncryptionPassword;
import utils.password.PasswordUtil;
import utils.sql.SQLResult;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

public class ATC_FT_049_TPE_TSFdispersionExitosaWesternUnion extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_049_TPE_TSFdispersionExitosaWesternUnion_test (HashMap<String, String> data) throws Exception{
		
		/**
		 * Proyecto: Servicios Electronicos (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-049-C2 Transaccion exitosa de Dispersion de Dinero de con Wester Union
		 * @author 
		 * @date 
		 */
		
		/*
		 * Utileria*********************************************************************/
		utils.sql.SQLUtil dbFCTPE_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		utils.sql.SQLUtil dbFCTPE_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE_QRO, GlobalVariables.DB_USER_FCTPE_QRO, GlobalVariables.DB_PASSWORD_FCTPE_QRO);
		utils.sql.SQLUtil dbFCWMLTAEQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QAVIEW);
		utils.sql.SQLUtil dbFCWMLTAEQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO, GlobalVariables.DB_USER_FCWMLTAEQA_QA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		
		TPE_TSF tpeTSF = new TPE_TSF(data, testCase, null);
		FCP_WMx86 fcp = new FCP_WMx86(data, testCase, null);
		/*
		 * Variables********************************************************************/
		String queryTransactioWU = "SELECT APPLICATION, ENTITY, OPERATION, FOLIO, CREATION_DATE, PLAZA, TIENDA, CAJA, WM_DESC\r\n"
				+ "FROM TPEUSER.TPE_FR_TRANSACTION\r\n"
				+ "WHERE APPLICATION= 'TSF' AND ENTITY = 'WU' AND FOLIO IN ('%s', '%s', '%s')\r\n"
				+ "ORDER BY CREATION_DATE DESC";
		
		String querySafeChannel = "SELECT * FROM (SELECT APPLICATION, ENTITY, OPERATION, SOURCE, FOLIO, PLAZA, TIENDA,CAJA, CREATION_DATE\r\n"
				+ "FROM WMLOG.SECURITY_SESSION_LOG\r\n"
				+ "WHERE APPLICATION ='TSF' AND ENTITY='WU' AND CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "ORDER BY CREATION_DATE DESC)\r\n"
				+ "WHERE ROWNUM <= 5";
		
		String queryError = "SELECT ERROR_ID, FOLIO, ERROR_DATE, SEVERITY, ERROR_TYPE, ERROR_CODE, DESCRIPTION \r\n"
				+ "FROM WMLOG.WM_LOG_ERROR_TPE TPE\r\n"
				+ "WHERE ERROR_DATE >= TRUNC(SYSDATE) AND FOLIO IN ('%s', '%s', '%s')\r\n"
				+ "ORDER BY error_date DESC";
		
		String wmCode = "";
		String folio, folio1, folio2, folio3, folio4 = "";
		String creationDate = "";
		String resposeQRY01 = "";
		String responseQR02 = "";
		String responseTRN01 = "";
		String responseTRN02 = "";
		String responseTRN03 = "";
		String codeToValidate = "101";
		String codeToValidate1 = "100";
		String codeTovalidate2 = "000";
		SoftAssert softAssert = new SoftAssert();
		/***********************************Paso 1**************************************/
		addStep(" Ejecutar el servicio de la consulta de referencia de envío Western Union para la interface TPE_TSF");
		
		resposeQRY01 = tpeTSF.ejecutarQRY01_WU1();
		System.out.println(resposeQRY01);
		
			wmCode = RequestUtil.getWmCodeXml(resposeQRY01);
			folio = RequestUtil.getFolioXml(resposeQRY01);
			creationDate = RequestUtil.getCreationDate(resposeQRY01);
			System.out.println("WMCODE: " + wmCode + " FOLIO: " + folio + " CreationDate: " + creationDate);
			softAssert.assertEquals(wmCode, codeToValidate, "Paso 1: Se obtuvo WmCode diferente al esperado");

		
		/***********************************Paso 2**************************************/
		addStep("Ejecutar el servicio de consulta de Dispersión de Dinero de Western Union de la interface TPE_TSF");
		
		responseQR02 = tpeTSF.ejecutarQRY02_WU(folio, creationDate);
		System.out.println(responseQR02);
		
			wmCode = RequestUtil.getWmCodeXml(responseQR02);
			folio1 = RequestUtil.getFolioXml(responseQR02);
			System.out.println("WMCODE: " + wmCode + " FOLIO: " + folio1);
			softAssert.assertEquals(wmCode, codeToValidate, "Paso 2: Se obtuvo WmCode diferente al esperado");
		
		/***********************************Paso 3**************************************/
		addStep("Ejecutar servicio de solicitud de folio para la transacción de Dispersión de Dinero de Western Union de la interface TPE_TSF");
		
		responseTRN01 = tpeTSF.ejecutarTRN01();
		System.out.println(responseTRN01);
		
			wmCode = RequestUtil.getWmCodeXml(responseTRN01);
			folio2 = RequestUtil.getFolioXml(responseTRN01);
			creationDate = RequestUtil.getCreationDate(responseTRN01);
			System.out.println("WMCODE: " + wmCode + " FOLIO: " + folio2 + " CreationDate: " + creationDate);
			softAssert.assertEquals(wmCode, codeToValidate1, "Paso 3: Se obtuvo WmCode diferente al esperado");
		
		/***********************************Paso 4**************************************/
		addStep("Ejecutar el servicio de solicitud de autorización de Dispersión de Dinero de Western Union de la interface TPE_TSF");
		
		responseTRN02 = fcp.ejecutarTRN02_WU(folio2, creationDate, folio);
		System.out.println(responseTRN02);
		
			wmCode = RequestUtil.getWmCodeXml(responseTRN02);
			folio3 = RequestUtil.getFolioXml(responseTRN02);
			creationDate = RequestUtil.getCreationDate(responseTRN02);
			System.out.println("WMCODE: " + wmCode + " FOLIO: " + folio3 + "CreationDate: " + creationDate);
			
			softAssert.assertEquals(wmCode, codeTovalidate2, "Paso 4: Se obtuvo WmCode diferente al esperado");
		
		/***********************************Paso 5**************************************/
		addStep("Ejecutar el servicio de solicitud de confirmación para la transacción de Dispersión "
				+ "de Dinero de Western Union de la interface TPE_TSF");
	
		responseTRN03 = tpeTSF.ejecutarTRN03(folio3, creationDate);
		System.out.println(responseTRN03);
		
			wmCode = RequestUtil.getWmCodeXml(responseTRN03);
			folio4 = RequestUtil.getFolioXml(responseTRN03);
			creationDate = RequestUtil.getCreationDate(responseTRN03);
			System.out.println("WMCODE: " + wmCode + " FOLIO: " + folio4 + "CreationDate: " + creationDate);
			
			softAssert.assertEquals(wmCode, codeToValidate, "Paso 5: Se obtiene el WmCode diferente al esperado");

		
		/***********************************Paso 6**************************************/
		addStep("Consultar el registro de la transacción de Dispersión de Dinero de Western Union "
				+ "en la tabla TPE_FR_TRANSACTION de la BD FCTPEQA");
		String transactionFormat = String.format(queryTransactioWU, folio, folio1, folio2);
		System.out.println(transactionFormat);
		
		SQLResult responseTransationWUSite1 = dbFCTPE_MTY.executeQuery(transactionFormat);
		Thread.sleep(50000);
		SQLResult responseTransationWUSite2 = dbFCTPE_QRO.executeQuery(transactionFormat);
		boolean validateTransaction = responseTransationWUSite1.isEmpty();
		if (!validateTransaction) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(responseTransationWUSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(responseTransationWUSite2);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(responseTransationWUSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(responseTransationWUSite2);
		}
		assertFalse(validateTransaction, "No se obtiene registro de la transaccion");
		
		/***********************************Paso 7**************************************/
		addStep("Validar el registro de las invocaciones de la interface TPE_TSF en la tabla SECURITY_SESSION_LOG "
				+ "de la BD FCWMLTAQ.FEMCOM.NET, debido a que la transacción no viajó por canal seguro");
		
		SQLResult responseSafeChannelSite1 = dbFCWMLTAEQA_MTY.executeQuery(querySafeChannel);
		Thread.sleep(50000);
		SQLResult responseSafeChannelSite2 = dbFCWMLTAEQA_QRO.executeQuery(querySafeChannel);
		System.out.println(querySafeChannel);
		boolean validateSafeChannel = responseSafeChannelSite1.isEmpty();
		if(!validateSafeChannel) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(responseSafeChannelSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(responseSafeChannelSite2);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(responseSafeChannelSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(responseSafeChannelSite2);
		}
		assertFalse(validateSafeChannel, "No se obtuvieron registros");
		
		/***********************************Paso 8**************************************/
		addStep("Validar que no se registre errores de la interface TPE_TSF y Framework en la tabla WM_LOG_ERROR_TPE "
				+ "de la BD FCWMLTAQ.FEMCOM.NET, correspondientes a la transacción realizada de Dispersión de Dinero Western Union");
		String errorFormat = String.format(queryError, folio, folio1, folio2);
		System.out.println(errorFormat);
		
		SQLResult responseErrorSite1 = dbFCWMLTAEQA_MTY.executeQuery(errorFormat);
		Thread.sleep(50000);
		SQLResult responseErrorSite2 = dbFCWMLTAEQA_QRO.executeQuery(errorFormat);
		boolean validateError = responseErrorSite1.isEmpty();
		if(!validateError) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(responseErrorSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(responseErrorSite2);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(responseErrorSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(responseErrorSite2);
		}
		assertFalse(validateError, "Se obtienen registros de error");//assertTrue
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
		return "Construida. MTC-FT-049 Transacción exitosa de Dispersión de Dinero de con Western Union";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_049_TPE_TSFdispersionExitosaWesternUnion_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
