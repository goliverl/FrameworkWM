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
* PE2V2:MTC-FT-057 Transaccion no exitosa  Provider - propiedad JSSE en el puerto desactivado en la reversa
* Desc:
* Validar que el WS del provider al tener una configuración diferente a la esperada no permita realizar la transaccion
* @author Gilberto Martinez
* @date   2022/11/10
* 
* Mtto:
* @author Jose Onofre
* @date 02/27/2023
*/
public class ATC_FT_057_PE2V2_TransaccionNoExitosaJSSEpuertoOFFReversa extends BaseExecution {

@Test (dataProvider = "data-provider")
	
	public void ATC_FT_057_PE2V2_TransaccionNoExitosaJSSEpuertoOFFReversa_test (HashMap<String, String> data) throws Exception {
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
		 *- Validar que en el servidor del consumer se encuentren las siguientes propiedades 
			1.-Seleccionar el en side menu Settings >Extended
			2.- Verificar los Extended Settings los siguientes Setting se encuentre como lo siguiente:
			watt.server.tae.fd.strict=true
			watt.server.WSDL.debug=true
			watt.security.ssl.ignoreExpiredChains=true
			watt.security.ssl.client.ignoreEmptyAuthoritiesList=true
			watt.security.cert.wmChainVerifier.trustByDefault=true
			watt.security.ssl.cacheClientSessions=false
			watt.security.ssl.keyAlias=
			watt.security.ssl.keypurposeverification=false
			watt.security.ssl.keyStoreAlias=
			watt.net.jsse.client.enabledProtocols=TLSv1.2
			watt.net.jsse.server.enabledProtocols=TLSv1.2
			watt.net.ssl.client.cipherSuiteList=default
			watt.net.ssl.client.handshake.maxVersion=TLSv1.2
			watt.net.ssl.client.handshake.minVersion=TLSv1.2
			watt.net.ssl.client.strongcipheronly=true
			watt.net.ssl.server.handshake.maxVersion=TLSv1.2
			watt.net.ssl.server.handshake.minVersion=TLSv1.2
			watt.net.ssl.server.strongcipheronly=true
			watt.server.ssl.keyStoreAlias=
			watt.server.ssl.trustStoreAlias=
			
			Nota: es importante que solo quede TLSv1.2
			3.- Reiniciar el servidor, si se realizo alguna Modificación en Extended
			
			- **Modifique en el Consumer del servidor donde se encuentra la interface **
			1. Ingresando al IS: <IS>
			2. Revisar cual es el keystore mas actual:
				Settings > Web Services >FEMCO_SWITCH_OXXO_WSS > View 
			    WS Security Properties > Keystore Alias
			3. Apagar la funcionalidad del acelerador criptografico de acuerdo al keystor actual (ver paso anterior) en: Security >Keystore > View Keystore Alias  (ejemplo wss_sw_2021)
				Editar el keystore apagando la propiedad *HSM Based Keystore* en false, verificando que: 
				Type = PKCS12
			    Provider = SunJSSE
			4. Editar desde *Settings > Web Services > Web Service Consumer Endpoints List > FEMCO_SWITCH_OXXO_WSS *
			En el *WS Security Properties* 
			4.1 Timestamp Precision = milisegundos
			4.2 Timestamp Time to Live = 1
			4.3 Timestamp Maximum Skew = 1
			5. Volver a encender la funcionalidad del acelerador
				Security >Keystore > View Keystore Alias  (ejemplo wss_sw_2021)
			    Type = PKCS12
			    Provider = SunJSSE
			    HSM Based Keystore = True
			    
			    Validar que se encuentre en HOLD el job PE2V2ReverseManager
				{Se encuentra en HOLD el job}blue 
				
				- ? Realizar <tipo_transaccion>
				1. Solicitud de Folio WM_CODE=100
				2. Solicitud de Autorizacion WM_CODE=000
				{se realiza hasta la autorizacion exitosa}green
				
				
				- Solicitar a Soporte apoyo para configurar el Webservice
				Ingresar al IS del Switch (QA9)
				1. Ingresar al puerto HTTPS configurado para el WSSecurity (WMSS-SecurityPort ) desde: 
					Security > Ports > 1208
				2. Modificar en *Security Configuration* la propiedad Use JSSE y cambiarlo a No
				
				Nota: valor configurado *Yes*

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
		return "Validar que en el servidor del consumer se encuentren las siguientes propiedades \r\n"
				+ "			1.-Seleccionar el en side menu Settings >Extended\r\n"
				+ "			2.- Verificar los Extended Settings los siguientes Setting se encuentre como lo siguiente:\r\n"
				+ "			watt.server.tae.fd.strict=true\r\n"
				+ "			watt.server.WSDL.debug=true\r\n"
				+ "			watt.security.ssl.ignoreExpiredChains=true\r\n"
				+ "			watt.security.ssl.client.ignoreEmptyAuthoritiesList=true\r\n"
				+ "			watt.security.cert.wmChainVerifier.trustByDefault=true\r\n"
				+ "			watt.security.ssl.cacheClientSessions=false\r\n"
				+ "			watt.security.ssl.keyAlias=\r\n"
				+ "			watt.security.ssl.keypurposeverification=false\r\n"
				+ "			watt.security.ssl.keyStoreAlias=\r\n"
				+ "			watt.net.jsse.client.enabledProtocols=TLSv1.2\r\n"
				+ "			watt.net.jsse.server.enabledProtocols=TLSv1.2\r\n"
				+ "			watt.net.ssl.client.cipherSuiteList=default\r\n"
				+ "			watt.net.ssl.client.handshake.maxVersion=TLSv1.2\r\n"
				+ "			watt.net.ssl.client.handshake.minVersion=TLSv1.2\r\n"
				+ "			watt.net.ssl.client.strongcipheronly=true\r\n"
				+ "			watt.net.ssl.server.handshake.maxVersion=TLSv1.2\r\n"
				+ "			watt.net.ssl.server.handshake.minVersion=TLSv1.2\r\n"
				+ "			watt.net.ssl.server.strongcipheronly=true\r\n"
				+ "			watt.server.ssl.keyStoreAlias=\r\n"
				+ "			watt.server.ssl.trustStoreAlias=\r\n"
				+ "			\r\n"
				+ "			Nota: es importante que solo quede TLSv1.2\r\n"
				+ "			3.- Reiniciar el servidor, si se realizo alguna Modificación en Extended\r\n"
				+ "			\r\n"
				+ "			- **Modifique en el Consumer del servidor donde se encuentra la interface **\r\n"
				+ "			1. Ingresando al IS: <IS>\r\n"
				+ "			2. Revisar cual es el keystore mas actual:\r\n"
				+ "				Settings > Web Services >FEMCO_SWITCH_OXXO_WSS > View \r\n"
				+ "			    WS Security Properties > Keystore Alias\r\n"
				+ "			3. Apagar la funcionalidad del acelerador criptografico de acuerdo al keystor actual (ver paso anterior) en: Security >Keystore > View Keystore Alias  (ejemplo wss_sw_2021)\r\n"
				+ "				Editar el keystore apagando la propiedad *HSM Based Keystore* en false, verificando que: \r\n"
				+ "				Type = PKCS12\r\n"
				+ "			    Provider = SunJSSE\r\n"
				+ "			4. Editar desde *Settings > Web Services > Web Service Consumer Endpoints List > FEMCO_SWITCH_OXXO_WSS *\r\n"
				+ "			En el *WS Security Properties* \r\n"
				+ "			4.1 Timestamp Precision = milisegundos\r\n"
				+ "			4.2 Timestamp Time to Live = 1\r\n"
				+ "			4.3 Timestamp Maximum Skew = 1\r\n"
				+ "			5. Volver a encender la funcionalidad del acelerador\r\n"
				+ "				Security >Keystore > View Keystore Alias  (ejemplo wss_sw_2021)\r\n"
				+ "			    Type = PKCS12\r\n"
				+ "			    Provider = SunJSSE\r\n"
				+ "			    HSM Based Keystore = True\r\n"
				+ "			    \r\n"
				+ "			    Validar que se encuentre en HOLD el job PE2V2ReverseManager\r\n"
				+ "				{Se encuentra en HOLD el job}blue \r\n"
				+ "				\r\n"
				+ "				- ? Realizar <tipo_transaccion>\r\n"
				+ "				1. Solicitud de Folio WM_CODE=100\r\n"
				+ "				2. Solicitud de Autorizacion WM_CODE=000\r\n"
				+ "				{se realiza hasta la autorizacion exitosa}green\r\n"
				+ "				\r\n"
				+ "				\r\n"
				+ "				- Solicitar a Soporte apoyo para configurar el Webservice\r\n"
				+ "				Ingresar al IS del Switch (QA9)\r\n"
				+ "				1. Ingresar al puerto HTTPS configurado para el WSSecurity (WMSS-SecurityPort ) desde: \r\n"
				+ "					Security > Ports > 1208\r\n"
				+ "				2. Modificar en *Security Configuration* la propiedad Use JSSE y cambiarlo a No\r\n"
				+ "				\r\n"
				+ "				Nota: valor configurado *Yes*";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "MTC-FT-057 Transaccion no exitosa  Provider - propiedad JSSE en el puerto desactivado en la reversa";
	}


	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_057_PE2V2_TransaccionNoExitosaJSSEpuertoOFFReversa_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
