package interfaces.PE2V2;

import static org.testng.Assert.assertEquals;
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
* PE2V2:MTC-FT-055 Transaccion no exitosa Provider - Politica anterior
* Desc:
* Validar que el WS del Provider al tener  la politica anterior no permita 
* realizar la transaccion y lo marque por error de WsSecurity
* 
* @author Gilberto Martinez
* @date   2022/11/09
* 
* Mtto:
* @author Jose Onofre
* @date 02/27/2023
*/
public class ATC_FT_055_PE2V2_TransaccionNoExitosaProviderPoliticaAnterior extends BaseExecution{
	@Test (dataProvider = "data-provider")
	
	public void ATC_FT_055_PE2V2_TransaccionNoExitosaProviderPoliticaAnterior_test (HashMap<String, String> data) throws Exception {
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
	
		String folio = "";
		String wmCode = "";
		String authWmCode = "";
		String ackWmCode = "";
		String ackAction = "";
		String creationDate = "";
		String codeToValidate = data.get("wmCodeToValidateFolio");
		String codeToValidateAuth = data.get("wmCodeToValidateAuth");
		String codeToValidate1 = data.get("codigoACK");
		String responseRunGetFolio = "";
		String responseRunGetAuth = "";
		String responseRunGetAck = "";
		String WmCodeEsperado="160";
		SoftAssert softAssert = new SoftAssert();
		
		
		/******PREREQUISITOS******
		 * - Solicitar a Soporte apoyo para configurar el Webservice
			Desde Designer:
			1. Ingresar al paquete del Switch sobre la ruta: FC_OXXO_SWITCH > FC > TPE >SWITCH > WSSecurity
			2. Eliminar la politica desde la pestaña: Policies 
			3. Agregar la politica anterior: X509Autentication
			
			Nota: politica nueva: X509Autentication_Signature_WSS
			
			- Solicitar a Soporte apoyo para configurar el Webservice
			**Modifique en el Consumer del servidor donde se encuentra la interface **
			1. Ingresando al IS: <IS>
			2. Apagar la funcionalidad del acelerador criptografico en: Security > Keystore > wss_sw 
				Editar el keystore apagando la propiedad *HSM Based Keystore* en false, verificando que: 
				Type = PKCS12
			    Provider = SunJSSE
			3. Editar desde *Settings > Web Services > Web Service Consumer Endpoints List > FEMCO_SWITCH_OXXO_WSS *
			4. En el *WS Security Properties* 
			4.1 Timestamp Precision = milisegundos
			4.2 Timestamp Time to Live = 1
			4.3 Timestamp Maximum Skew = 1
			5. Volver a encender la funcionalidad del acelerador
				Security > Keystore > wss_sw 
			    Type = PKCS12
			    Provider = SunJSSE
			    HSM Based Keystore = True
		 */
		
		
		/*  En los pasos de octane no especifica el tipo de transaccion ni que requests enviar, se comenta codigo de transaccion normal hasta verificar que sea el mismo proceso
		- Realizar <tipo_transaccion>
			{Se rechaza la transacción y obtenemos el folio}green
		 - 
				
		******************************************Paso 1*************************************
		addStep("Solicitar un folio desde el navegador o desde la herramienta Soap UI, invocando el servicio runGetFolio de la Interface PE2V2");
		
		responseRunGetFolio = pe2Util.ejecutarRunGetFolio();
		System.out.println(responseRunGetFolio);
		
		folio = getSimpleDataXml(responseRunGetFolio, "folio");
		wmCode = getSimpleDataXml(responseRunGetFolio, "wmCode");
		creationDate = getSimpleDataXml(responseRunGetFolio, "creationDate");
		
		System.out.println("Folio: " + folio +" wmCode: " + wmCode + " Creation Date: " + creationDate);
		softAssert.assertEquals(wmCode, codeToValidate, "Paso 1: Se obtiene WmCode diferente al esperado");
		
		******************************************Paso 2*************************************
		addStep("Solicitar autorización de la compra con tarjeta invocando el servicio runGetAuth de la interface PE2v2");
	
		responseRunGetAuth = pe2Util.ejecutarRunGetAuth(folio, creationDate);
		System.out.println("ResposeAuth" + responseRunGetAuth);
		
		authWmCode = getSimpleDataXml(responseRunGetAuth, "wmCode");
	
		System.out.println("wmCode: " + authWmCode );
		softAssert.assertEquals(authWmCode, codeToValidateAuth, "Paso 2: Se obtiene WmCode diferente al esperado");
		
		/******************************************Paso 3*************************************
		addStep("Solicitar confirmación ACK del pago con tarjeta desde un navegador invocando servicio **runGetAuthAck**");
	
		responseRunGetAck = pe2Util.ejecutarRunGetAck(folio, creationDate);
		System.out.println(responseRunGetAck);
		
		ackWmCode = getSimpleDataXml(responseRunGetAck, "wmCode");
		ackAction = getSimpleDataXml(responseRunGetAck, "action");
		
		System.out.println("ack Code: " + ackWmCode + " ack Action: " + ackAction);
		softAssert.assertEquals(ackWmCode, codeToValidate1, "Paso 3: Se obtiene WmCode diferente al esperado");
		
		 */
		
		/******************************************Paso 4*************************************/
		addStep("validar que la transacción se registró correctamente en la tabla TDC_TRANSACTION de la BD  **FCTDCQA**");

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
		assertEquals(WmCodeEsperado, wmCode);
		/******************************************Paso 5*************************************/
		addStep("Validar en la base de datos del **FCSWQA**, que NO se encuentre el registro de la transaccion");
	
		String queryTransFormat = String.format(queryTransaction, folio);
		System.out.println(queryTransFormat);
		
		SQLResult transactionSite1 = executeQuery(dbFCSWQA, queryTransFormat);
		
		boolean validateTrans = transactionSite1.isEmpty();
		
		if(validateTrans) {
			testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
			testCase.addQueryEvidenceCurrentStep(transactionSite1);
		}

		assertTrue(validateTrans, "SI se encontro registro de transaccion");
		/******************************************Paso 6*************************************/
		
		addStep("Validar que se encuentren registros de error de la PE2V2");
	
		String errorFormat = String.format(queryError, folio);
		
		SQLResult errorLogSite1 = executeQuery(dbFCWMLTAEQA_MTY, errorFormat);
		
		boolean validateErrorLog = errorLogSite1.isEmpty();
		if (!validateErrorLog) {
			testCase.addBoldTextEvidenceCurrentStep(" Se encontraron registros de error Site 1: ");
			
		}
		testCase.addQueryEvidenceCurrentStep(errorLogSite1);
		assertFalse(validateErrorLog, "No se obtuvieron registros de error");
		softAssert.assertAll();
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return "- Solicitar a Soporte apoyo para configurar el Webservice\r\n"
				+ "Desde Designer:\r\n"
				+ "1. Ingresar al paquete del Switch sobre la ruta: FC_OXXO_SWITCH > FC > TPE >SWITCH > WSSecurity\r\n"
				+ "2. Eliminar la politica desde la pestaña: Policies \r\n"
				+ "3. Agregar la politica anterior: X509Autentication\r\n"
				+ "\r\n"
				+ "Nota: politica nueva: X509Autentication_Signature_WSS\r\n"
				+ "\r\n"
				+ "- Solicitar a Soporte apoyo para configurar el Webservice\r\n"
				+ "**Modifique en el Consumer del servidor donde se encuentra la interface **\r\n"
				+ "1. Ingresando al IS: <IS>\r\n"
				+ "2. Apagar la funcionalidad del acelerador criptografico en: Security > Keystore > wss_sw \r\n"
				+ "	Editar el keystore apagando la propiedad *HSM Based Keystore* en false, verificando que: \r\n"
				+ "	Type = PKCS12\r\n"
				+ "    Provider = SunJSSE\r\n"
				+ "3. Editar desde *Settings > Web Services > Web Service Consumer Endpoints List > FEMCO_SWITCH_OXXO_WSS *\r\n"
				+ "4. En el *WS Security Properties* \r\n"
				+ "4.1 Timestamp Precision = milisegundos\r\n"
				+ "4.2 Timestamp Time to Live = 1\r\n"
				+ "4.3 Timestamp Maximum Skew = 1\r\n"
				+ "5. Volver a encender la funcionalidad del acelerador\r\n"
				+ "	Security > Keystore > wss_sw \r\n"
				+ "    Type = PKCS12\r\n"
				+ "    Provider = SunJSSE\r\n"
				+ "    HSM Based Keystore = True";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "MTC-FT-055 Transaccion no exitosa Provider - Politica anterior";
	}


	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_055_PE2V2_TransaccionNoExitosaProviderPoliticaAnterior_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
