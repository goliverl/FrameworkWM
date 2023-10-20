package interfaces.PE2V2;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import modelo.BaseExecution;
import om.PE2;
import om.TPE_FAC;
import om.TPE_OLS;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.webmethods.ReadRequest;

public class ATC_FT_009_P2V2_RealizReversPorACKnegatUsanConectaActual extends BaseExecution {

	/**
	 * PE2V2: MTC-FT-009 Realizar reversa por ACK negativo usando conector actual
	 * Desc:
	 * Comprobar que las reversas ejecutadas de la interface PE2v2 de devolucion por ACK negativo, 
	 * son procesadas desde el mismo site al que se solicito el folio, en un esquema activo-activo 
	 * para un sistema DRP de un site
	 * 
	 * @author Jose Onofre
	 * 
	 * Mtto:
	 * @author Jose Onofre
	 * @date 02/27/2023
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_009_P2V2_RealizReversPorACKnegatUsanConectaActual_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/

		SQLUtil dbFCTDCQA = new SQLUtil(GlobalVariables.DB_HOST_FCTDCQA, GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		
		SQLUtil dbFCSWQA = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		
		SQLUtil dbFCWMLTAQ = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA);
		
	
		PE2 pe2Util = new PE2(data, testCase, null);
		
		
		
		/*
		 * Variables
		 *********************************************************************/
		
		String consultaTransaccionTDC = "SELECT tdc.wm_code, tdc.* FROM TPEUSER.TDC_TRANSACTION tdc "
				+ "WHERE FOLIO='%s' AND CREATION_DATE>='%s'";
	
		
		String consultaTDCReverse = "SELECT * FROM TPEUSER.TDC_REVERSE "
				+ "WHERE FOLIO= '%s' AND CREATION_DATE>= '%s' ";
		
		String consultaRegTransaccion = "SELECT tl.auth_id_res, tl.resp_code, tl.sw_code, tl.counter, tl.* "
				+ "FROM SWUSER.TPE_SW_TLOG  tl " 
				+ "WHERE creation_date >= '%s' " 
				+ "AND folio = '%s'";
		
		String consultaNoError = "SELECT * FROM " 
				+ "FROM WMLOG.WM_LOG_ERROR_TPE TPE "
				+ "WHERE TRUNC(ERROR_DATE) >= '%s' "
				+ "AND TPE_TYPE like '%PE2V2%' " 
				+ "AND ROWNUM <= 100 " 
				+ "ORDER BY error_date  DESC";
		
	
		
		String expected_wm_code = "102";
		String expected_SWDevCode = "00";
		String expected_status = "S";
		
		String responseRunGetFolio = "";
		String responseRunGetAuth = "";
		String authWmCode = "";
		String authAction = "";
		String Wm_code = "";
		String ackNegativo="01";
		String creation_date = "";
		String folio = "";
		
		SoftAssert softAssert = new SoftAssert();
		
		testCase.setTest_Description(data.get("id") + data.get("description"));
		
		testCase.setPrerequisites("prerequisites");
		
		
		// Paso 1 ****************************************************
		addStep(" Solicitar un folio desde el navegador"
				+ " o desde la herramienta Soap UI, "
				+ "invocando el servicio runGetFolio de la Interface PE2V2");
		
		
		responseRunGetFolio = pe2Util.ejecutarRunGetFolio();
		System.out.println(responseRunGetFolio);
		
		folio = getSimpleDataXml(responseRunGetFolio, "folio");
		Wm_code = getSimpleDataXml(responseRunGetFolio, "wmCode");
		creation_date = getSimpleDataXml(responseRunGetFolio, "creationDate");
		
		System.out.println("Folio: " + folio +" wmCode: " + Wm_code + " Creation Date: " + creation_date);
		softAssert.assertEquals(Wm_code, expected_wm_code, "Paso 1: Se obtiene WmCode diferente al esperado");
		
		
		
		// Paso 2 ****************************************************
		
		addStep("Solicitar autorización de cashback desde un navegador o "
				+ "desde la herramienta Soap UI, invocando el servicio **runGetAuth** "
				+ "de la interface PE2V2  del site");
		
		
		responseRunGetAuth = pe2Util.ejecutarRunGetAuth(folio, creation_date, 0);
		System.out.println("ResposeAuth" + responseRunGetAuth);
		
		authWmCode = getSimpleDataXml(responseRunGetAuth, "wmCode");
		authAction = getSimpleDataXml(responseRunGetAuth, "action");
		
		System.out.println("wmCode: " + authWmCode + " Action: " + authAction);
		softAssert.assertEquals(authWmCode, expected_wm_code, "Paso 2: Se obtiene WmCode diferente al esperado");
		
		
		
		// Paso 3 ****************************************************
		
		addStep("Solicitar confirmacion ACK NEGATIVO de compra regular desde *un navegador "
				+ "o desde alguna herramienta como SOAP UI ó Postman*, "
				+ "invocando el servicio **runGetAuth** de la interface PE2V2");
		
		
		if(data.get("ack").equals(ackNegativo)) {
			
				addStep("Confirmar de forma negativa la reversa de retiro de dinero "
						+ "desde un navegador (con ack=01), invocando el servicio runGetAuthAck:");
		}
		else {
		addStep("Solicitar autorización, invocando el servicio runGetAck");}
		
		String responseRunGetAck = pe2Util.ejecutarRunGetAck(folio,creation_date);// retorna el response
		System.out.println(responseRunGetAck);

		
		String wmCodeRequest = getSimpleDataXml(responseRunGetAck, "wmCode");
		// Se valida que el wm_code del xml de respuesta sea igual al esperado
		
		assertEquals(wmCodeRequest,expected_wm_code);
		
		
		
		// Paso 4 ****************************************************
		
		
		addStep("Conectarse a la Base de Datos: **FCTDCQA** en esquema: **TPEUSER**  del site ");
		
		boolean conexiondbFCTDCQA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCTDCQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCSWQA);

		assertTrue(conexiondbFCTDCQA, "La conexion no fue exitosa");
		
		
		// Paso 5 ****************************************************
		
		addStep("Validar que la transacción se registró correctamente"
				+ " en la tabla TDC_TRANSACTION de la BD  **FCTDCQA**");
				
		
		
		String ConsultaTransTDC_f = String.format(consultaTransaccionTDC,folio, creation_date);
		
		System.out.println("Consulta de referencia : \r\n "+ ConsultaTransTDC_f);
	
		SQLResult ConsultaTransTDC_r = executeQuery(dbFCTDCQA, ConsultaTransTDC_f);
		
		boolean validaTransTDC = ConsultaTransTDC_r.isEmpty();
		
		if(!validaTransTDC) {
			
			testCase.addQueryEvidenceCurrentStep(ConsultaTransTDC_r);
			
			
		}
		
		
		// Paso 6 ****************************************************
		
		
		addStep("Validar en la base de datos del **FCSWQA**, que se encuentre el registro de la transaccion ");
		
		
		String response_code = "";
		String Sw_Dev_code = "";
		String status_code = "";
		
		String ConsultaTDCRev_f = String.format(consultaTDCReverse,folio, creation_date);
		
		System.out.println("Consulta de referencia : \r\n "+ ConsultaTDCRev_f);
	
		SQLResult ConsultaTDCRev_r = executeQuery(dbFCTDCQA, ConsultaTDCRev_f);
		
		boolean validaTrans = ConsultaTDCRev_r.isEmpty();
		
		if(!validaTrans) {
			
			response_code = ConsultaTDCRev_r.getData(0, "RESP_CODE");
			Sw_Dev_code = ConsultaTDCRev_r.getData(0, "Sw_auth_code");
			status_code = ConsultaTDCRev_r.getData(0, "status");
			
			testCase.addQueryEvidenceCurrentStep(ConsultaTDCRev_r);
			
			testCase.addTextEvidenceCurrentStep(
					"Codigo Wm Esperado: " + expected_wm_code  + "\r\n" + "Codigo Wm Actual: " + response_code
					+ "\r\n" + "Codigo auth esperado : " + expected_SWDevCode + "\r\n" + "Codigo auth Actual: " + Sw_Dev_code
					+ "\r\n" + "Codigo Status esperado: " + expected_status + "\r\n" + "Status Actual: " + status_code);
			
		}
		
		
		assertEquals(expected_wm_code, response_code);
		assertEquals(expected_SWDevCode,Sw_Dev_code);
		assertEquals(expected_status, status_code);
		
		
		// Paso 7 ****************************************************
		
		addStep("Conectarse a la Base de Datos **FCSWQA** con esquema: **SWUSER** ");
		
		
		boolean conexiondbFCSWQA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCSWQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCSWQA);

		assertTrue(conexiondbFCSWQA, "La conexion no fue exitosa");
		
		
		// Paso 8 ****************************************************
		
		addStep("Validar en la base de datos del **FCSWQA**, que se encuentre el registro de la transaccion ");
		
		
		String ConsultaTrans_f = String.format(consultaRegTransaccion, creation_date,folio);
		
		System.out.println("Consulta de referencia : \r\n "+ ConsultaTrans_f);
	
		SQLResult ConsultaTrans_r = executeQuery(dbFCSWQA, ConsultaTrans_f);
		
		boolean validaTransSW = ConsultaTrans_r.isEmpty();
		
		if(!validaTransSW) {
			
			testCase.addQueryEvidenceCurrentStep(ConsultaTrans_r);
			
		
		}
		
		
	
		// Paso 9 ****************************************************
		
		addStep("Conectarse a la Base de Datos **FCWMLTAQ** con esquema: **WMLOG** ");
		
		boolean conexiondbFCWMLTAQ = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCWMLTAQ ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLTAEQA);

		assertTrue(conexiondbFCWMLTAQ, "La conexion no fue exitosa");
		
		
		// Paso 10 ****************************************************
		
		addStep("Validar en la base de datos **FCWMLTAQ** que no se "
				+ "encuentren registros de error de la PE2V2 ");
		
		String current_date = "";
		
		String ConsultaNoError_f = String.format(consultaNoError, current_date);
		
		System.out.println("Consulta de referencia : \r\n "+ ConsultaNoError_f);
	
		SQLResult ConsultaNoError_r = executeQuery(dbFCWMLTAQ, ConsultaNoError_f);
		
		boolean validaNoError = ConsultaNoError_r.isEmpty();
		
		if(!validaNoError) {
			
			testCase.addQueryEvidenceCurrentStep(ConsultaNoError_r);
			
		}
		
		
		
		
		
		
	}
	

	@Override
	public String setTestFullName() {
		return "ATC_FT_009_P2V2_RealizReversPorACKnegatUsanConectaActual_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "JoseOnofre@Hexaware.com";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
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

}

