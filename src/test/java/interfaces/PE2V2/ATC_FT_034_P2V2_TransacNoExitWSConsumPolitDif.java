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

public class ATC_FT_034_P2V2_TransacNoExitWSConsumPolitDif extends BaseExecution {

	/**
	 * PE2V2:MTC-FT-034 Transaccion no exitosa  por WS Consumer- politica diferente
	 * Desc:
	 * Validar que el WS del consumer al tener una politica diferente no permita realizar la transaccion
	 * 
	 * @author Jose Onofre
	 * 
	 * Mtto:
	 * @author Jose Onofre
	 * @date 02/27/2023
	 */
	@Test(dataProvider = "data-provider")
	public void ATC_FT_034_P2V2_TransacNoExitWSConsumPolitDif_test(HashMap<String, String> data) throws Exception {

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
		
	
		
		String expected_wm_code = "160";	
		String responseRunGetFolio = "";
		String responseRunGetAuth = "";
		String authWmCode = "";
		String authAction = "";
		String Wm_code = "";
		
		String creation_date = "";
		String folio = "";
		
		SoftAssert softAssert = new SoftAssert();
		
		testCase.setTest_Description(data.get("id") + data.get("description"));
		
		testCase.setPrerequisites("prerequisites");
		
		
		// Paso 1 ****************************************************
		
		addStep("Solicitar a Soporte apoyo para configurar el Webservice");
		
		System.out.println(""
				+ "1. Ingresar al paquete del la PE2V2 sobre la ruta: FEMSA_PE2V2 > PE2V2 > WS > wsSwFemco \r\n" 
				+ "2. Eliminar la politica desde la pestaña: Policies \r\n" 
				+ "3. Agregar una politica diferente (ej. SAMLAutentication)");
		
		
		// Paso 2 ****************************************************
		
		addStep("Contar con el acelerador criptografico encendido (QA8)");
		
		
		System.out.println("Security > Keystore > wss_sw \r\n" + 
				"    Type = PKCS12\r\n" + 
				"    Provider = SunJSSE\r\n" + 
				"    HSM Based Keystore = True");
		
		
		// Paso 3 ****************************************************
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
		
		
		
		// Paso 4 ****************************************************
		
		addStep("Solicitar autorización de pago con tarjeta desde un navegador o "
				+ "desde la herramienta Soap UI, invocando el servicio **runGetAuth** "
				+ "de la interface PE2V2  del site");
		
		
		responseRunGetAuth = pe2Util.ejecutarRunGetAuth(folio, creation_date, 0);
		System.out.println("ResposeAuth" + responseRunGetAuth);
		
		authWmCode = getSimpleDataXml(responseRunGetAuth, "wmCode");
		authAction = getSimpleDataXml(responseRunGetAuth, "action");
		
		System.out.println("wmCode: " + authWmCode + " Action: " + authAction);
		softAssert.assertEquals(authWmCode, expected_wm_code, "Paso 2: Se obtiene WmCode diferente al esperado");
		
		
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
			
			
		}
		
		
		// Paso 7 ****************************************************
		
		addStep("Conectarse a la Base de Datos: **FCSWQA** en esquema: **TPEUSER**  del site ");
		
		boolean conexiondbFCSWQA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCSWQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCSWQA);

		assertTrue(conexiondbFCSWQA, "La conexion no fue exitosa");
		
		
		
		// Paso 8 ****************************************************
		
		
		addStep("Validar en la base de datos del **FCSWQA**, que se encuentre el registro de la transaccion ");
		
		
		String response_code = "";
	
		String ConsultaTDCRev_f = String.format(consultaRegTransaccion, creation_date,folio);
		
		System.out.println("Consulta de referencia : \r\n "+ ConsultaTDCRev_f);
	
		SQLResult ConsultaTDCRev_r = executeQuery(dbFCSWQA, ConsultaTDCRev_f);
		
		boolean validaTrans = ConsultaTDCRev_r.isEmpty();
		
		if(!validaTrans) {
			
			response_code = ConsultaTDCRev_r.getData(0, "RESP_CODE");

			
			testCase.addQueryEvidenceCurrentStep(ConsultaTDCRev_r);
			
			testCase.addTextEvidenceCurrentStep(
					"Codigo Wm Esperado: " + expected_wm_code  + "\r\n" + "Codigo Wm Actual: " + response_code);
				
		}
		
		
		assertEquals(expected_wm_code, response_code);
		
		// Paso 9 ****************************************************
		
		addStep("Conectarse a la Base de Datos **FCWMLTAQ** con esquema: **WMLOG** ");
				
		boolean conexiondbFCWMLTAQ = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCWMLTAQ ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLTAEQA);

		assertTrue(conexiondbFCWMLTAQ, "La conexion no fue exitosa");
				
		
		
		// Paso 10 ****************************************************
		
		addStep("Validar en la base de datos **FCWMLTAQ** que no se "
				+ "encuentren registros de error de la PE2V2 ");
		
		String current_error_date = "";
		
		String ConsultaNoError_f = String.format(consultaNoError, current_error_date);
		
		System.out.println("Consulta de referencia : \r\n "+ ConsultaNoError_f);
	
		SQLResult ConsultaNoError_r = executeQuery(dbFCWMLTAQ, ConsultaNoError_f);
		
		boolean validaNoError = ConsultaNoError_r.isEmpty();
		
		if(!validaNoError) {
			
			testCase.addQueryEvidenceCurrentStep(ConsultaNoError_r);
			
		}
		
	}
	

	@Override
	public String setTestFullName() {
		return "ATC_FT_034_P2V2_TransacNoExitWSConsumPolitDif_test";
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

