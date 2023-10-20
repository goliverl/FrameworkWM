package interfaces.PE2V2;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.util.HashMap;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import modelo.BaseExecution;
import om.PE2V2;
import util.GlobalVariables;
import utils.sql.SQLResult;
/**
*
* PE2V2:MTC-FT-052 Realizar inicializacion de llaves
* Desc:
* Realizar inicializacion de llaves exitosa
* 
* @author Gilberto Martinez
* @date   2022/11/08
* 
* Mtto:
* @author Jose Onofre
* @date 02/27/2023
*/
public class ATC_FT_052_PE2V2_RealizarInicializacionDeLlaves extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_052_PE2V2_RealizarInicializacionDeLlaves_test(HashMap<String, String> data)
			throws Exception {
		/*
		 * Utilerías
		 ************************************************************************/
		utils.sql.SQLUtil dbFCTDCQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA,
				GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA); // NUEVA
		utils.sql.SQLUtil dbFCTDCQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA_QRO,
				GlobalVariables.DB_USER_FCTDCQA_QRO, GlobalVariables.DB_PASSWORD_FCTDCQA_QRO); // NUEVA
		utils.sql.SQLUtil dbFCSWQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA,
				GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
//		utils.sql.SQLUtil dbFCSWQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO, GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		utils.sql.SQLUtil dbFCWMLTAEQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA,
				GlobalVariables.DB_USER_FCWMLTAEQA_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAEQA);
//		utils.sql.SQLUtil dbFCWMLTAEQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO, GlobalVariables.DB_USER_FCWMLTAEQA_QA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);

		PE2V2 pe2Util = new PE2V2(data, testCase, null);

		/*
		 * Variables
		 ************************************************************************/
		String queryPayment = "SELECT FOLIO,CREATION_DATE,PLAZA,TIENDA,CAJA,WM_CODE,TYPE_OPERATION,PV_DATE,AD_DATE\r\n "
				+ "FROM TPEUSER.TDC_INITIALIZATION_KEYS\r\n "
				+ "WHERE FOLIO= '%s' "
				+ "AND CREATION_DATE >= TRUNC(SYSDATE) "
				+ "AND ROWNUM = 1"
				+ "ORDER BY CREATION_DATE DESC";
		
		String queryTransaction = "SELECT tl.auth_id_res, tl.resp_code, tl.sw_code, tl.counter, tl.mti, tl.resp_code, tl.auth_id_res, tl.application, tl.folio\r\n "
				+ "FROM SWUSER.TPE_SW_TLOG tl\r\n "
				+ "WHERE FOLIO = '%s' "
				+ "AND CREATION_DATE >= TRUNC(SYSDATE) "
				+ "ORDER BY CREATION_DATE DESC";

		
		String queryError = "SELECT *\r\n "
				+ "FROM ( SELECT ERROR_ID,FOLIO, ERROR_DATE, ERROR_CODE, DESCRIPTION FROM\r\n "
				+ "WMLOG.WM_LOG_ERROR_TPE TPE\r\n "
				+ "WHERE TPE_TYPE like '%PE2V2%'\r\n "
				+ "AND ERROR_DATE >= TRUNC(SYSDATE) AND FOLIO = '%s')\r\n "
				+ "WHERE ROWNUM <= 5";
		
		String folio = "";
		String wmCode = "";
		String keyWmCode = "";
		String creationDate = "";
		String codeToValidate = data.get("wmCodeToValidateFolio");
		String wmCodeToValidateKey = data.get("wmCodeToValidateKey");
		String responseRunGetFolio = "";
		String responseRunGetInitialization = "";

		SoftAssert softAssert = new SoftAssert();
		/******************************************
		 * Paso 1
		 *************************************/
		addStep("Solicitar un folio, invocando el servicio runGetFolio de la Interface PE2V2");

		responseRunGetFolio = pe2Util.ejecutarRunGetFolio();
		System.out.println(responseRunGetFolio);

		folio = getSimpleDataXml(responseRunGetFolio, "folio");
		wmCode = getSimpleDataXml(responseRunGetFolio, "wmCode");
		creationDate = getSimpleDataXml(responseRunGetFolio, "creationDate");

		System.out.println("Folio: " + folio + " wmCode: " + wmCode + " Creation Date: " + creationDate);
		softAssert.assertEquals(wmCode, codeToValidate, "Paso 1: Se obtiene WmCode diferente al esperado");

		/******************************************
		 * Paso 2
		 *************************************/
		addStep("Realizar la solicitud de llaves utilizando request runGetInitialization :");

		responseRunGetInitialization = pe2Util.ejecutarRunGetInitialization(folio, creationDate);
		System.out.println("ResposeAuth" + responseRunGetInitialization);

		keyWmCode = getSimpleDataXml(responseRunGetInitialization, "wmCode");

		System.out.println("wmCode: " + keyWmCode);
		softAssert.assertEquals(keyWmCode, wmCodeToValidateKey, "Paso 2: Se obtiene WmCode diferente al esperado");

		/******************************************
		 * Paso 3
		 *************************************/
		addStep("Validar que la transacción  de pago de tarjeta en la tabla de la BD  **FCTDCQA**");

		String queryPaymentFormat = String.format(queryPayment, folio);
		System.out.println(queryPaymentFormat);

		SQLResult paymentSite1 = executeQuery(dbFCTDCQA_MTY, queryPaymentFormat);

		SQLResult paymentSite2 = executeQuery(dbFCTDCQA_QRO, queryPaymentFormat);

		boolean validatePayment = paymentSite1.isEmpty();
		String WM_CODE = "";
		if (!validatePayment) {
			WM_CODE = paymentSite1.getData(0, "WM_CODE");
			System.out.println("WM_CODE: " + WM_CODE);
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(paymentSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(paymentSite2);
			testCase.addBoldTextEvidenceCurrentStep("WM_CODE: " + WM_CODE);
		} else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(paymentSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(paymentSite2);
		}
		assertFalse(validatePayment, "No se obtuvo registro de transaccion de pago con tarjeta");
		assertEquals(wmCodeToValidateKey, WM_CODE);

		/******************************************
		 * Paso 4
		 *************************************/
		addStep("Validar en la base de datos del **FCSWQA**, que se encuentre el registro de la transaccion");

		String queryTransFormat = String.format(queryTransaction, folio);
		System.out.println(queryTransFormat);

		SQLResult transactionSite1 = executeQuery(dbFCSWQA, queryTransFormat);

		boolean validateTrans = transactionSite1.isEmpty();
		String mti = "";
		String respCode = "";
		String Folio = "";
		String authWmCode="";
		
		if (!validateTrans) {
			Folio = transactionSite1.getData(0, "FOLIO");
			mti = transactionSite1.getData(0, "MTI");
			respCode = transactionSite1.getData(0, "RESP_CODE");
			authWmCode = transactionSite1.getData(0, "AUTH_ID_RES");
			System.out.println(
					"Folio: " + folio + "\nmti: " + mti + "\nrespCode: " + respCode + "\nauthWmCode: " + authWmCode);
			testCase.addBoldTextEvidenceCurrentStep(
					"Folio: " + folio + "\nmti: " + mti + "\nrespCode: " + respCode + "\nauthWmCode: " + authWmCode);
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(transactionSite1);
		}
		assertFalse(validateTrans, "No se encontro registro de transaccion");
		assertEquals(folio, Folio);
		/******************************************
		 * Paso 5
		 *************************************/
		addStep("Validar que no se encuentren registros de error de la PE2V2");

		String errorFormat = String.format(queryError, folio);

		SQLResult errorLogSite1 = executeQuery(dbFCWMLTAEQA_MTY, errorFormat);

		boolean validateErrorLog = errorLogSite1.isEmpty();
		if (!validateErrorLog) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(errorLogSite1);
		}
		assertTrue(validateErrorLog, "Se obtuvieron registros de error");
		softAssert.assertAll();
		/******************************************
		 * Paso 6
		 *************************************/

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
		return "MTC-FT-052 Realizar inicializacion de llaves";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_052_PE2V2_RealizarInicializacionDeLlaves_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
