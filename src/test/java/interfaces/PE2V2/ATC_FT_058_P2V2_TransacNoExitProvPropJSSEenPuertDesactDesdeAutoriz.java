package interfaces.PE2V2;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.util.HashMap;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import modelo.BaseExecution;
import om.PE2;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_058_P2V2_TransacNoExitProvPropJSSEenPuertDesactDesdeAutoriz extends BaseExecution {

	/**
	 * PE2V2:MTC-FT-058 Transaccion no exitosa  Provider - propiedad JSSE en el puerto desactivado desde la autorizacion
	 * Desc:
	 * Validar que el WS del provider al tener una configuración diferente a la esperada no permita realizar la transaccion
	 * 
	 * @author Jose Onofre
	 * 
	 * Mtto:
	 * @author Jose Onofre
	 * @date 02/27/2023
	 */
	@Test(dataProvider = "data-provider")
	public void ATC_FT_058_P2V2_TransacNoExitProvPropJSSEenPuertDesactDesdeAutoriz_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/

		SQLUtil dbFCTDCQA = new SQLUtil(GlobalVariables.DB_HOST_FCTDCQA, GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		
		SQLUtil dbFCSWQA = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		
		SQLUtil dbFCWMLTAQ = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA);
		
		
		/*
		 * Variables
		 *********************************************************************/
		
		String consultaTransaccionTDC = "SELECT tdc.wm_code, tdc.app, tdc.prom_type, tdc.* FROM TPEUSER.TDC_TRANSACTION tdc "
				+ "WHERE FOLIO='%s' AND CREATION_DATE>='%s'";
	
		
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
		
		String consultaTDCReverse = "SELECT re.wm_code, re.status, re.counter, re.* "
				+ "FROM TPEUSER.TDC_REVERSE "
				+ "WHERE FOLIO= '%s' AND CREATION_DATE>= '%s' ";
	
		
		
		String expected_wm_code = "109";
		String creation_date = "";
		String folio = "";
		
		String bd_wm_code = "";
		String prom_type = "";
		String expected_status = "p";
		
		
		testCase.setTest_Description(data.get("id") + data.get("description"));
		
		testCase.setPrerequisites("prerequisites");
		
		
		// Paso 1 ****************************************************
		
		addStep("Validar que en el servidor del consumer se encuentren las siguientes propiedades");
		
		
		
		
		// Paso 2 ****************************************************
		
		addStep("Solicitar a Soporte apoyo para configurar el Webservice");
		
		
		
		
		// Paso 3 ****************************************************
		
		addStep("**Modifique en el Consumer del servidor donde se encuentra la interface **");
		
		
		
		// Paso 4 ****************************************************
		
		addStep("Realizar <tipo_transaccion>");
			
		
		// Paso 5 ****************************************************
		
		
		addStep("Conectarse a la Base de Datos: **FCTDCQA** en esquema: **TPEUSER**  del site ");
		
		boolean conexiondbFCTDCQA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCTDCQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCSWQA);

		assertTrue(conexiondbFCTDCQA, "La conexion no fue exitosa");
		
		
		// Paso 6 ****************************************************
		
		addStep("Validar que la transacción se registró correctamente"
				+ " en la tabla TDC_TRANSACTION de la BD  **FCTDCQA**");
				
		String ConsultaTransTDC_f = String.format(consultaTransaccionTDC,folio, creation_date);
		
		System.out.println("Consulta de referencia : \r\n "+ ConsultaTransTDC_f);
	
		SQLResult ConsultaTransTDC_r = executeQuery(dbFCTDCQA, ConsultaTransTDC_f);
		
		boolean validaTransTDC = ConsultaTransTDC_r.isEmpty();
		
		if(!validaTransTDC) {
			
			testCase.addQueryEvidenceCurrentStep(ConsultaTransTDC_r);
			
			bd_wm_code = ConsultaTransTDC_r.getData(0, "Wm_Code");
			prom_type = ConsultaTransTDC_r.getData(0, "Wm_Code");
			
		}
		
		assertEquals(expected_wm_code,bd_wm_code);
		
		// Paso 7 ****************************************************
		
		addStep("Ejecutar que se ejecute el job PE2V2ReverseManager y una vez finalizado dejar en HOLD");
		
		
		// Paso 8 ****************************************************
		
		addStep("Ejecutar la consulta del estado de la reversa en la BD **FCTDCQA**");
				
		String ConsultaTransTDCReverse_f = String.format(consultaTDCReverse,folio, creation_date);
		
		String status = "";
		
		System.out.println("Consulta de referencia : \r\n "+ ConsultaTransTDCReverse_f);
	
		SQLResult ConsultaTransTDCReverse_r = executeQuery(dbFCTDCQA, ConsultaTransTDCReverse_f);
		
		boolean validaTransTDCReverse = ConsultaTransTDCReverse_r.isEmpty();
		
		if(!validaTransTDCReverse) {
			
			testCase.addQueryEvidenceCurrentStep(ConsultaTransTDCReverse_r);
			
			bd_wm_code = ConsultaTransTDCReverse_r.getData(0, "Wm_Code");
			status = ConsultaTransTDCReverse_r.getData(0, "status");
			
			
		}
		
		assertEquals(expected_wm_code, bd_wm_code);
		assertEquals(expected_status, status);
		
		
		// Paso 9 ****************************************************
		
		addStep("Validar que la transacción se registró correctamente"
				+ " en la tabla TDC_TRANSACTION de la BD  **FCTDCQA**");
				
		String ConsultaTransTDC2_f = String.format(consultaTransaccionTDC,folio, creation_date);
		
		System.out.println("Consulta de referencia : \r\n "+ ConsultaTransTDC2_f);
	
		SQLResult ConsultaTransTDC2_r = executeQuery(dbFCTDCQA, ConsultaTransTDC2_f);
		
		boolean validaTransTDC2 = ConsultaTransTDC_r.isEmpty();
		
		if(!validaTransTDC2) {
			
			testCase.addQueryEvidenceCurrentStep(ConsultaTransTDC2_r);
			
			bd_wm_code = ConsultaTransTDC_r.getData(0, "Wm_Code");
			prom_type = ConsultaTransTDC_r.getData(0, "Wm_Code");
			
		}
		
		
		assertEquals(expected_wm_code, bd_wm_code);
		assertEquals(expected_status, status);
		
		
		// Paso 10 ****************************************************
		
		addStep("Conectarse a la Base de Datos: **FCSWQA** en esquema: **TPEUSER**  del site ");
		
		boolean conexiondbFCSWQA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCSWQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCSWQA);

		assertTrue(conexiondbFCSWQA, "La conexion no fue exitosa");
		
		
		// Paso 11 ****************************************************
		
		addStep("Validar en la base de datos del **FCSWQA**, que NO se encuentre el registro de la transaccion ");
		
		String response_code = "";
	
		String ConsultaTDCRev_f = String.format(consultaRegTransaccion, creation_date,folio);
		
		System.out.println("Consulta de referencia : \r\n "+ ConsultaTDCRev_f);
	
		SQLResult ConsultaTDCRev_r = executeQuery(dbFCSWQA, ConsultaTDCRev_f);
		
		boolean validaTrans = ConsultaTDCRev_r.isEmpty();
		
		if(validaTrans) {
			
			testCase.addQueryEvidenceCurrentStep(ConsultaTDCRev_r);
			testCase.addTextEvidenceCurrentStep("Efectivamente no se encontro registro");
		}
		else {
			
			
			testCase.addQueryEvidenceCurrentStep(ConsultaTDCRev_r);
			
			testCase.addTextEvidenceCurrentStep(
					"Codigo Wm Esperado: " + expected_wm_code  + "\r\n" + "Codigo Wm Actual: " + response_code);
				
		}
		
		assertTrue(validaTrans);
		
		// Paso 12 ****************************************************
		
		addStep("Conectarse a la Base de Datos **FCWMLTAQ** con esquema: **WMLOG** ");
				
		boolean conexiondbFCWMLTAQ = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCWMLTAQ ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLTAEQA);

		assertTrue(conexiondbFCWMLTAQ, "La conexion no fue exitosa");
				
		
		
		// Paso 13 ****************************************************
		
		addStep("Validar en la base de datos **FCWMLTAQ** que se "
				+ "encuentren registros de error de la PE2V2 ");
		
		String current_error_date = "";
		
		String ConsultaNoError_f = String.format(consultaNoError, current_error_date);
		
		System.out.println("Consulta de referencia : \r\n "+ ConsultaNoError_f);
	
		SQLResult ConsultaNoError_r = executeQuery(dbFCWMLTAQ, ConsultaNoError_f);
		
		boolean validaNoError = ConsultaNoError_r.isEmpty();
		
		if(validaNoError) {
			
			testCase.addQueryEvidenceCurrentStep(ConsultaNoError_r);
			testCase.addTextEvidenceCurrentStep("Se encontraron errores en la consulta");
		}
		else {
			testCase.addQueryEvidenceCurrentStep(ConsultaNoError_r);
			testCase.addTextEvidenceCurrentStep("Se encontraron errores en la consulta");
		}
		
		assertTrue(!validaNoError);
	}
	

	@Override
	public String setTestFullName() {
		return "ATC_FT_058_P2V2_TransacNoExitProvPropJSSEenPuertDesactDesdeAutoriz_test";
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
