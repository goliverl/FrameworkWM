package interfaces.PE2V2;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import modelo.BaseExecution;
import om.TPE_FAC;
import om.TPE_OLS;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.webmethods.ReadRequest;

public class ATC_FT_026_P2V2_ComprMen250pagaTarjCaptNnip extends BaseExecution {

	/**
	 * PE2V2: MTC-FT-026 CP043 Compras Menores de 250 pagando con Tarjeta captura nip
	 * Desc:
	 * Regresion con XPOS
	 * Ticket menor a 250.00, captura de PIN , Leyenda: Autorizado mediante firma electrónica
	 * 
	 * @author Jose Onofre
	 * 
	 * Mtto:
	 * @author Jose Onofre
	 * @date 02/27/2023
	 */
	@Test(dataProvider = "data-provider")
	public void ATC_FT_026_P2V2_ComprMen250pagaTarjCaptNnip_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/

		SQLUtil dbFCTDCQA = new SQLUtil(GlobalVariables.DB_HOST_FCTDCQA, GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		
		SQLUtil dbFCSWQA = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		
		utils.sql.SQLUtil dbFCWMLTAEQA_MTY = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QAVIEW);
		
	
		/*
		 * Variables
		 *********************************************************************/
		
		String consultaTransaccionTDC = "SELECT tdc.wm_code, tdc.* FROM " 
				+ " TPEUSER.TDC_TRANSACTION tdc WHERE "
				+ "FOLIO=:FOLIO AND CREATION_DATE>='%s'";
		
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
		
		String consultaSecurityLOG = "SELECT * FROM SECURITY_SESSION_LOG "
				+ "WHERE APPLICATION like '%PE2V2%' "
				+ "and creation_date >= '%s' " 
				+ "and folio= '%s' " 
				+ "ORDER BY CREATION_DATE DESC ";
		
		
		
		String expected_wm_code = "101";
		String expected_auth_code = "00";
		String expected_respCode = "00";
		
		testCase.setTest_Description(data.get("id") + data.get("description"));
		
		testCase.setPrerequisites("prerequisites");
		
		
		// Paso 1 ****************************************************
		addStep("Solicitar a XPOS realizar una compra con una tarjeta ");
		
		
		
		
		
		
		// Paso 2 ****************************************************
		
		addStep("Conectarse a la Base de Datos: **FCTDCQA** en esquema: **TPEUSER** ");
		
		boolean conexiondbFCTDCQA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCTDCQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCTDCQA);

		assertTrue(conexiondbFCTDCQA, "La conexion no fue exitosa");
		
		
		
		// Paso 3 ****************************************************
		
		
		addStep("consulta para validar que la transacción se registró correctamente"
				+ " en la tabla TDC_TRANSACTION de la BD  **FCTDCQA**");
	
		String creation_date = "";
		String  wm_code = "";
		String Sw_auth_code = "";
		String ConsultaTransTDC_f = String.format(consultaTransaccionTDC, creation_date);
		
		System.out.println("Consulta de referencia : \r\n "+ ConsultaTransTDC_f);
	
		SQLResult ConsultaTransTDC_r = executeQuery(dbFCTDCQA, ConsultaTransTDC_f);
		
		boolean validaTransTDC = ConsultaTransTDC_r.isEmpty();
		
		if(!validaTransTDC) {
			
			wm_code = ConsultaTransTDC_r.getData(0, "Wm_code");
			Sw_auth_code = ConsultaTransTDC_r.getData(0, "Sw_auth_code");
			
			testCase.addQueryEvidenceCurrentStep(ConsultaTransTDC_r);
			
			testCase.addTextEvidenceCurrentStep(
					"Codigo Wm Esperado: " + expected_wm_code  + "\r\n" + "Codigo Wm Actual: " + wm_code
					+ "\r\n" + "Codigo auth esperado : " + expected_auth_code + "\r\n" + "Codigo auth Actual: " + Sw_auth_code);
			
		}
		
		
		assertEquals(expected_wm_code, wm_code);
		assertEquals(expected_auth_code,Sw_auth_code);
		
		
		
		
		// Paso 4 ****************************************************
		
		addStep("Conectarse a la Base de Datos **FCSWQA** con esquema: **SWUSER** ");
		
		
		boolean conexiondbFCSWQA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCSWQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCSWQA);

		assertTrue(conexiondbFCSWQA, "La conexion no fue exitosa");
		
		
		// Paso 5 ****************************************************
		
		addStep("Validar en la base de datos del **FCSWQA**, que se encuentre el registro de la transaccion ");
		
		String creation_date2 = "";
		String folio = "";
		String response_code = "";
		
		String ConsultaTrans_f = String.format(consultaRegTransaccion, creation_date2,folio);
		
		System.out.println("Consulta de referencia : \r\n "+ ConsultaTrans_f);
	
		SQLResult ConsultaTrans_r = executeQuery(dbFCSWQA, ConsultaTrans_f);
		
		boolean validaTrans = ConsultaTrans_r.isEmpty();
		
		if(!validaTrans) {
			
			response_code = ConsultaTrans_r.getData(0, "RESP_CODE");
			Sw_auth_code = ConsultaTrans_r.getData(0, "Sw_auth_code");
			
			testCase.addQueryEvidenceCurrentStep(ConsultaTrans_r);
			
			testCase.addTextEvidenceCurrentStep(
					"Codigo Wm Esperado: " + expected_respCode  + "\r\n" + "Codigo Wm Actual: " + response_code
					+ "\r\n" + "Codigo auth esperado : " + expected_auth_code + "\r\n" + "Codigo auth Actual: " + Sw_auth_code);
			
		}
		
		
		assertEquals(expected_respCode, response_code);
		assertEquals(expected_auth_code,Sw_auth_code);
		
		
	
		// Paso 6 ****************************************************
		
		addStep("Conectarse a la Base de Datos **FCWMLTAQ** con esquema: **WMLOG** ");
		
		boolean conexiondbFCWMLTAQ = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCWMLTAQ ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLTAEQA);

		assertTrue(conexiondbFCWMLTAQ, "La conexion no fue exitosa");
		
		
		// Paso 7 ****************************************************
		
		addStep("Validar en la base de datos **FCWMLTAQ** que no se "
				+ "encuentren registros de error de la PE2V2 ");
		
		String current_date = "";
		
		String ConsultaNoError_f = String.format(consultaNoError, current_date);
		
		System.out.println("Consulta de referencia : \r\n "+ ConsultaNoError_f);
	
		SQLResult ConsultaNoError_r = executeQuery(dbFCSWQA, ConsultaNoError_f);
		
		boolean validaNoError = ConsultaNoError_r.isEmpty();
		
		if(!validaNoError) {
			
			testCase.addQueryEvidenceCurrentStep(ConsultaNoError_r);
			
		}
		
		
		
		// Paso 8 ****************************************************
		
		addStep("Validar en la base de datos **FCWMLTAQ** se este registrando el securityLog ");
		
		
		String ConsultaSecLOG_f = String.format(consultaSecurityLOG, creation_date, folio);
		
		System.out.println("Consulta de referencia : \r\n "+ ConsultaSecLOG_f);
	
		SQLResult ConsultaSecLOG_r = executeQuery(dbFCWMLTAEQA_MTY, ConsultaSecLOG_f);
		
		boolean validaSecLOG = ConsultaSecLOG_r.isEmpty();
		
		if(!validaSecLOG) {
			
			testCase.addQueryEvidenceCurrentStep(ConsultaSecLOG_r);
			
		}
		
		
	}
	

	@Override
	public String setTestFullName() {
		return "ATC_FT_026_P2V2_ComprMen250pagaTarjCaptNnip_test";
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