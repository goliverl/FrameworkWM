package interfaces.PE2V2;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import modelo.BaseExecution;
import om.PE2V2;
import util.GlobalVariables;
import utils.sql.SQLResult;
/**
*
* PE2V2:MTC-FT-027 CP044 Compras Menores de 250 pagando con Tarjeta fisica debito/credito
* Desc:
* Regresion con XPOS
* Realizar una compra menor a 250, pagando con una tarjeta de credito /debito
* 
* @author Gilberto Martinez
* @date   2022/10/20
* 
* Mtto:
* @author Jose Onofre
* @date 02/27/2023
*/
public class ATC_FT_027_PE2V2_ComprasMenoresDe250PagandoConTarjetaFisica extends BaseExecution{

	@Test (dataProvider = "data-provider")
	public void ATC_FT_027_PE2V2_ComprasMenoresDe250PagandoConTarjetaFisica_test (HashMap<String, String> data) throws Exception {
		/*
		 * Utilerías
		 ************************************************************************/
		utils.sql.SQLUtil dbFCTDCQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA, GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA); //NUEVA
		utils.sql.SQLUtil dbFCTDCQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA_QRO, GlobalVariables.DB_USER_FCTDCQA_QRO, GlobalVariables.DB_PASSWORD_FCTDCQA_QRO); //NUEVA
		utils.sql.SQLUtil dbFCSWQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
//		utils.sql.SQLUtil dbFCSWQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO, GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		utils.sql.SQLUtil dbFCWMLTAEQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAEQA);
//		utils.sql.SQLUtil dbFCWMLTAEQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO, GlobalVariables.DB_USER_FCWMLTAEQA_QA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		
		PE2V2 pe2Util = new PE2V2(data, testCase, null);
		
		/*
		 *Variables
		 ************************************************************************/
		String queryPayment = "SELECT tdc.wm_code , tdc.folio, tdc.creation_date, tdc.plaza, tdc.tienda, tdc.affiliation, tdc.amount, tdc.sw_auth_code\r\n "
				+ "FROM TPEUSER.TDC_TRANSACTION tdc\r\n "
				+ "WHERE FOLIO= %s "
				+ "AND CREATION_DATE >= TRUNC(SYSDATE) "
				+ "ORDER BY CREATION_DATE DESC";

		String queryTransaction = "SELECT tl.auth_id_res, tl.resp_code, tl.sw_code, tl.counter, tl.mti, tl.resp_code, tl.auth_id_res, tl.application, tl.folio\r\n "
				+ "FROM SWUSER.TPE_SW_TLOG tl\r\n "
				+ "WHERE FOLIO = %s "
				+ "AND CREATION_DATE >= TRUNC(SYSDATE) "
				+ "ORDER BY CREATION_DATE DESC";

		
		String queryError = "SELECT *\r\n "
				+ "FROM ( SELECT ERROR_ID,FOLIO, ERROR_DATE, ERROR_CODE, DESCRIPTION FROM\r\n "
				+ "WMLOG.WM_LOG_ERROR_TPE TPE\r\n "
				+ "WHERE TPE_TYPE like '%PE2V2%'\r\n "
				+ "AND ERROR_DATE >= TRUNC(SYSDATE) AND FOLIO = '%s')\r\n "
				+ "WHERE ROWNUM <= 5";
		
		String channelQuery = "SELECT APPLICATION, OPERATION, SOURCE, FOLIO, PLAZA, TIENDA, CREATION_DATE \r\n"
				+ "FROM WMLOG.SECURITY_SESSION_LOG\r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "AND APPLICATION LIKE 'PE2V2' AND FOLIO = '%s'\r\n"
				+ "ORDER BY CREATION_DATE DESC";
		
		
	
		String folio = data.get("folio");
		String wmCode = "";
		String authWmCode = "";

		testCase.setTest_Description(data.get("Name"));
		
		SoftAssert softAssert = new SoftAssert();
		/******************************************Paso 1*************************************/
		addStep("Solicitar a XPOS realizar una compra con una tarjeta");
	
		testCase.addBoldTextEvidenceCurrentStep("Se realiza correctamente la transaccion");
		
		/******************************************Paso 2*************************************/
	
		addStep("Validar que la transacción  de pago de tarjeta en la tabla TDC_TRANSACTION de la BD  **FCTDCQA**");
	
		String queryPaymentFormat = String.format(queryPayment, folio);
		System.out.println(queryPaymentFormat);
		
		SQLResult paymentSite1 = executeQuery(dbFCTDCQA_MTY,queryPaymentFormat);

		SQLResult paymentSite2 = executeQuery(dbFCTDCQA_QRO,queryPaymentFormat);
		
		boolean validatePayment = paymentSite1.isEmpty();
		
		if(!validatePayment) {
			wmCode = paymentSite1.getData(0, "WM_CODE");
			authWmCode = paymentSite1.getData(0, "SW_AUTH_CODE");
			System.out.println("WM_CODE: "+ wmCode +" SW_AUTH_CODE: " + authWmCode);
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(paymentSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(paymentSite2);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(paymentSite1);
			testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
			testCase.addQueryEvidenceCurrentStep(paymentSite2);
		}
		assertFalse(validatePayment,"No se obtuvo registro de transaccion de pago con tarjeta");
		
		/******************************************Paso 3*************************************/
		addStep("Validar en la base de datos del **FCSWQA**, que se encuentre el registro de la transaccion");

		String queryTransFormat = String.format(queryTransaction, folio);
		System.out.println(queryTransFormat);
		
		SQLResult transactionSite1 = executeQuery(dbFCSWQA, queryTransFormat);
		
		boolean validateTrans = transactionSite1.isEmpty();
		
		if(!validateTrans) {
			folio = transactionSite1.getData(0, "FOLIO");
			String mti = transactionSite1.getData(0, "MTI");
			String respCode = transactionSite1.getData(0, "RESP_CODE");
			authWmCode = transactionSite1.getData(0, "AUTH_ID_RES");
			System.out.println("Folio: " + folio);
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(transactionSite1);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(transactionSite1);
		}
		assertFalse(validateTrans, "No se encontro registro de transaccion");
		/******************************************Paso 4*************************************/
		
		addStep("Validar que no se encuentren registros de error de la PE2V2");
	
		String errorFormat = String.format(queryError, folio);
		
		SQLResult errorLogSite1 = executeQuery(dbFCWMLTAEQA_MTY, errorFormat);
		
		boolean validateErrorLog = errorLogSite1.isEmpty();
		if (!validateErrorLog) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(errorLogSite1);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(errorLogSite1);
		}
		assertTrue(validateErrorLog, "Se obtuvieron registros de error");
		softAssert.assertAll();
		
		/******************************************Paso 5*************************************/
		
		addStep("Validar en la base de datos **FCWMLTAQ** se este registrando el securityLog ");

		String channelFormat = String.format(channelQuery, folio);
		
		SQLResult safeChannel = dbFCWMLTAEQA_MTY.executeQuery(channelFormat);
		System.out.println(channelQuery);
		
		boolean validateSafeChannel = safeChannel.isEmpty();
		if(!validateSafeChannel) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(safeChannel);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(safeChannel);
		}
		assertFalse(validateSafeChannel, "No se obtuvieron registros");
		softAssert.assertAll();
	}	
			

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "JoseO@Hexaware.com";
	}

	@Override
	public String setTestFullName() {
		
		return "ATC_FT_027_PE2V2_ComprasMenoresDe250PagandoConTarjetaFisica_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

}
