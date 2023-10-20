package interfaces.tpe_ftc;

import static org.testng.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import modelo.BaseExecution;
import om.OxxoRequest;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/**
 * Servicios Electrónicos: MTC-FT-065 Transaccion exitosa de Consulta de terminos y condiciones Spin (QRY01)
 * Prueba de regresión para comprobar la no afectación en la funcionalidad principal de Consulta de Términos y Condiciones SPIN de la interface FEMSA_TPE_FTC al ser migradas de WM9.9 a WM10.5.
 * @author Roberto Flores
 * @date   2022/06/01
 * 
 * Mtto: Jose Onofre
 * @date 02/21/2023
 * 
 */
public class ATC_FT_003_TPE_FTC_TransacExitConsulTermCondSpin extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_TPE_FTC_TransaccionExitosaVinculacionTarjetaSpin_test(HashMap<String, String> data) throws Exception {

		testCase.setPrerequisites("1. Contar con acceso a las base de datos de FCACQA_MTY y FCWMLTAEQA_MTY.\r\n" + 
				"2. Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA.\r\n" + 
				"3. Se debe contar con postman instalado.");
		
		/*
		 * Utilerías
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		SimpleDateFormat pvDateFormatter = new SimpleDateFormat("yyyyMMddHHmmss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		testCase.setTest_Description(data.get("id")+ data.get("descripcion"));
		SQLUtil dbMtyFCACQA = new SQLUtil(GlobalVariables.DB_HOST_FCACQA, GlobalVariables.DB_USER_FCACQA,GlobalVariables.DB_PASSWORD_FCACQA);
		SQLUtil dbMtyFCWMLTAQ = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAQ_MTY,GlobalVariables.DB_USER_FCWMLTAQ_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAQ_MTY);
		//TPE_OLS rhUtil = new TPE_OLS(data, testCase, dbMtyFCTPE);

		/*
		 * Variables
		 *********************************************************************/
		String wmCodeToValidateQRY01 = "101";
		
		Date fechaEjecucionQRY01Inicio = null;
		Date fechaEjecucionQRY01Fin = null;

		String consultaQRY01= "SELECT OPERATION, ENTITY, FOLIO,WM_CODE, APPLICATION,CREATION_DATE,PROC_CODE\r\n"
				+ "FROM TPEUSER.CTR_SE_TRANSACTION \r\n"
				+ "WHERE WM_CODE = 101 \r\n"
				+ "AND ENTITY = 'FTECH' \r\n"
				+ "AND PROC_CODE = 'VIN' \r\n"
				+ "AND CREATION_DATE >= (TRUNC(SYSDATE))";
		
		String consultaError = "SELECT error_id, folio, error_date,description tpe_type \r\n" + 
				"FROM WMLOG.WM_LOG_ERROR_TPE \r\n" + 
				"WHERE   TPE_TYPE LIKE '%s' \r\n" + 
				"AND ERROR_DATE BETWEEN TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"AND TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"ORDER BY ERROR_DATE DESC";
		
		//----------------------------------------------------------------------------------------------------
		
		String wmCodeRequestQRY01;
		String folioQRY01;
		
		String respuestaQRY01;
		SoftAssert softAssert = new SoftAssert();

		testCase.setProject_Name("POC WMx86");

		/**************************************************************************************************
		 * Consulta QRY01
		 * 
		 *************************************************************************************************/
		
		 
		// Paso 1 ****************************************************
		addStep("Ejecutar en la herramienta de postman la consulta de la TPE_FTC del server QA8");
		
		System.out.println("Consulta QRY01");

		
		
		String QRY01RequestUrl = "https://"+GlobalVariables.HOST_FTC+"/invoke/TPE.FTC.pub.request/_post?jsonIn= \r\n" + 
				data.get("JsonQRY01");
	
		
		
		//Reemplazar adDate y pvDate
		fechaEjecucionQRY01Inicio = new Date();
		QRY01RequestUrl = QRY01RequestUrl.replace("<<pvDate>>", pvDateFormatter.format(fechaEjecucionQRY01Inicio));
		QRY01RequestUrl = QRY01RequestUrl.replace("<<adDate>>", pvDateFormatter.format(fechaEjecucionQRY01Inicio).substring(0, 8));
		
		System.out.println("Nuevo XML: \n");
		System.out.println(QRY01RequestUrl);
		testCase.addTextEvidenceCurrentStep(QRY01RequestUrl);
		
		//Se solicita el request del QRY01
		
		respuestaQRY01 = OxxoRequest.executeRequestDisableSSL(QRY01RequestUrl);
		fechaEjecucionQRY01Fin = new Date();
		
		System.out.println(respuestaQRY01);
		testCase.addTextEvidenceCurrentStep(respuestaQRY01);
		
		wmCodeRequestQRY01 = RequestUtil.getWmCodeJson(respuestaQRY01);
		folioQRY01 = RequestUtil.getFolioJson(respuestaQRY01);
		
		//assertNotNull(wmCodeRequestQRY01);
		
		/*
		 *
		 *****************************************************************************************/
		//Valida el status esperado del QRY01
		addStep("Verificar que el JSON de respuesta tenga como wmCode: " + wmCodeToValidateQRY01);
		
		System.out.println("Código esperado: " + wmCodeToValidateQRY01  + "\r\n" + "Código JSON: " + wmCodeRequestQRY01);
		testCase.addTextEvidenceCurrentStep(
				"Código esperado: " + wmCodeToValidateQRY01  + "\r\n" + "Código JSON: " + wmCodeRequestQRY01);
		
		softAssert.assertEquals(wmCodeRequestQRY01, wmCodeToValidateQRY01);


	//  Paso 2 *********************************************************************************************************
		
		addStep("Conectarse a la Base de Datos: **FCACQA** en esquema: **TPEUSER**  de MTY");
		
		boolean conexionFCTPEQA = true;
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCACQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCACQA);
		
		assertTrue(conexionFCTPEQA, "La conexion no fue exitosa");
		 
	 //Paso 3 *******************************************************************************************************
	
		addStep("Ejecutar la siguiente consulta para validar la Transacción exitosa en la tabla CTR_SE_TRANSACTION  de la BD  **FCACQA**");
       
		System.out.println("Paso 3: \n"+consultaQRY01);
		
		SQLResult exe_consultaQRY01 = executeQuery(dbMtyFCACQA,consultaQRY01);
		
		boolean validaConsultaQRY01 = exe_consultaQRY01.isEmpty();
		
		if(!validaConsultaQRY01) {
			testCase.addQueryEvidenceCurrentStep(exe_consultaQRY01);
		}
		
		System.out.println(validaConsultaQRY01);
		softAssert.assertFalse(validaConsultaQRY01, "No se insertaron datos en la tabla TPUSER.CTR_SE_TRANSACTION");
		
		//Paso 4 ***************************************************************************************************

		addStep("Establecer conexión con la Base de Datos **FCWMLTAEQA** en el site 1 MTY");
        
		boolean conexionFCWMLTAQ = true;
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCWMLTAQ ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA);
		
		softAssert.assertTrue(conexionFCWMLTAQ, "La conexion no fue exitosa");
	
		
		//Paso 5 *****************************************************************************************************************
		addStep("Ejecutar la siguiente consulta en la base de datos **FCWMLTAEQA** para validar que no se encuentren registros de error de la FTC");
		
		String fechaEjecucionQRY01_f = formatter.format(fechaEjecucionQRY01Inicio);
		String fechaEjecucionTRN03_f = formatter.format(fechaEjecucionQRY01Fin);
		
		String consultaError_f = String.format(consultaError, "%FTC%", fechaEjecucionQRY01_f, fechaEjecucionTRN03_f);
		
		System.out.println(consultaError_f);
	
		SQLResult consultaError_r = dbMtyFCWMLTAQ.executeQuery(consultaError_f);
	
		boolean validaConsultaError = consultaError_r.isEmpty();
	
		System.out.println(validaConsultaError);
		
		testCase.addQueryEvidenceCurrentStep(consultaError_r, true);
		if (!validaConsultaError) {
			testCase.addTextEvidenceCurrentStep("Error encontrado en WMLOG.WM_LOG_ERROR_TPE");
		} else {
			testCase.addTextEvidenceCurrentStep("No se encontraron errores en WMLOG.WM_LOG_ERROR_TPE");
		}
	
		assertTrue(validaConsultaError, "Se visualizan errores en la base de datos");
		softAssert.assertAll();
	}

	@Override
	public String setTestFullName() {
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDesigner() {
		return "Equipo automatizacion";
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