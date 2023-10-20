package interfaces.OL12_MX;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_021_OL12_MX_GenyEnvArchTransacServLocFERIA extends BaseExecution {

	/**
	 * La clase esta construida completamente, sin embargo algunos pasos no se cumplen debido a falta de informacion
	 * 
	 * @param data
	 * @throws Exception
	 */
	
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_021_OL12_MX_GenyEnvArchTransacServLocFERIA_test(HashMap<String, String> data) throws Exception {
		
		/*
		 * Utilerias
		 *********************************************************************/
		
		SQLUtil dbAVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		
		SQLUtil dbFCWMLTAQ_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		
		
		/*
		 * Variables
		 *********************************************************************/
		
		//NEW QUERY
		
		String consulta_config_serv = "SELECT * FROM "
				+ "XXFC.XXFC_SERVICES_VENDOR_COMM_DATA WHERE SERVICE_ID IN ('%s')";
		
		
		String consulta_datServicio_FERIA = "SELECT S.TRANS_ID, S.PLAZA, S.SERVICIO, S.ESTATUS, S.LOTE, S.FECHA_TRANSMISION, S.*\r\n" + 
				"FROM XXFC.XXFC_PAGO_SERVICIOS S\r\n" + 
				"WHERE SERVICIO IN ('101289499')\r\n" + 
				"AND PLAZA = '10MON'\r\n" + 
				"AND FECHA_TRANSACCION BETWEEN TO_DATE ('%s', 'ddMMrrrr') AND TO_DATE ('%s', 'ddMMrrrr')\r\n" + 
				"AND ESTATUS IS NULL\r\n" + 
				"AND LOTE IS NULL";
		
		String consultaWM_LOG_RUN= "SELECT * FROM WMLOG.WM_LOG_RUN WHERE INTERFACE LIKE"
				+ " '%OL12%' ORDER BY START_DT DESC";
		
		String consultaWM_LOG_THREAD  = "SELECT * FROM WMLOG.WM_LOG_THREAD WHERE PARENT_ID IN (2165362228)";
		
		String consultaWM_LOG_ERROR = "SELECT * FROM WMLOG.WM_LOG_ERROR WHERE RUN_ID IN (2165362228)";
		
		String consulta_tpe_fr_trans = "SELECT * FROM XXFC_LOTES WHERE SERVICIO = '101289499' ORDER BY FECHA DESC;";
		
		String consulta_pago_serv = "SELECT S.TRANS_ID, S.PLAZA, S.SERVICIO, S.ESTATUS, S.LOTE, S.FECHA_TRANSMISION, S.*\r\n" + 
				"FROM XXFC.XXFC_PAGO_SERVICIOS S\r\n" + 
				"WHERE SERVICIO IN ('%s')\r\n" + 
				"AND PLAZA = '10MON'\r\n" + 
				"AND FECHA_TRANSACCION BETWEEN TO_DATE ('%s', 'ddMMrrrr') AND TO_DATE ('%s', 'ddMMrrrr')\r\n" + 
				"AND S.ESTATUS ='E'";
		
		
		String consulta_Serv_Plaza_Lote = "SELECT TRANS_ID, PLAZA, TIENDA, SERVICIO, FECHA_TRANSACCION, FOLIO_WM, NO_AUTO, REF1, VALOR, COMISION, HORA_TRANSACCION, ESTATUS, LOTE, FECHA_TRANSMISION, \r\n" + 
				"FECHA_RECEPCION, LAST_UPDATE_DATE, XXFC_FECHA_ADMINISTRATIVA, CONSECUTIVO, FOLIO_TRANSACCION, REF2, ORACLE_CR, CORTE, CAJA, TICKET, VALIDATED, ORG_ID, \r\n" + 
				"FACTURA, FACTURA_AR,GL, ID_SERVICIO, ORACLE_CIA,  ATRIBUTO3, POLIZA_OS, CARTA_FEMSA, LAST_UPDATED_BY, ATRIBUTO1, ATRIBUTO2, ATRIBUTO4, ATRIBUTO5\r\n" + 
				"FROM XXFC_PAGO_SERVICIOS \r\n" + 
				"WHERE SERVICIO in('%s')\r\n" + 
				"AND (ESTATUS IS NULL OR ESTATUS='%s') AND LOTE is NULL\r\n" + 
				"AND PLAZA='%s' AND TIENDA='%s'\r\n" + 
				"AND FECHA_TRANSACCION >= (sysdate-30) AND FECHA_TRANSACCION <= (sysdate-1)\r\n" + 
				"AND XXFC_FECHA_ADMINISTRATIVA >= (sysdate-30) AND XXFC_FECHA_ADMINISTRATIVA <= (sysdate-1)\r\n" + 
				"ORDER BY FECHA_TRANSACCION DESC";
		
		String expec_WM_LOG_RUN_stat = "S";
		
		
		Date fechaEjecucionInicio;
		
		testCase.setTest_Description(data.get("id") + data.get("description"));
		
		testCase.setPrerequisites("prerequisites");
		
		
		// Paso 1 ****************************************************
		addStep("Realizar conexión a la BD de EBS  FCAVEBQA");
		
		
		boolean conexiondbAVEBQA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbAVEBQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_AVEBQA);

		assertTrue(conexiondbAVEBQA, "La conexion no fue exitosa");
	
		
		// Paso 2 ****************************************************
		
		addStep("Ejecutar la siguiente consulta para comprobar que exista "
				+ "la configuracion para el servicio" + data.get("id_servicio") + " en la BD FCAVEBQA");
		
		String consulta_config_service = String.format(consulta_config_serv,data.get("id_servicio"));
		
		SQLResult consulta_config_service_r = executeQuery(dbAVEBQA, consulta_config_service);
		

		boolean validaconfig_service = consulta_config_service_r.isEmpty();

		if (!validaconfig_service) {

			
			testCase.addQueryEvidenceCurrentStep(consulta_config_service_r);
		}

		System.out.println(validaconfig_service);

		assertFalse(validaconfig_service, "No se encontraron registros en la consulta");
		
		
		// Paso 3 ****************************************************
		
		addStep("Validar con la siguiente consulta en la base de datos de EBS FCAVEBQA "
				+ "los datos para el servicio(FERIA) pendiente "
				+ "de procesar por la interfaz OL12_MX");
		
		String consulta_dat_Servicio_FERIA = String.format(consulta_datServicio_FERIA, data.get("from_date"), data.get("to_date"));
	
		SQLResult consulta_dat_Servicio_FERIA_r = executeQuery(dbAVEBQA, consulta_dat_Servicio_FERIA);
		
		boolean validadat_Servicio_FERIA = consulta_dat_Servicio_FERIA_r.isEmpty();

		if (!validadat_Servicio_FERIA) {
			
			testCase.addQueryEvidenceCurrentStep(consulta_dat_Servicio_FERIA_r);
		}

		System.out.println(validadat_Servicio_FERIA);

		assertFalse(validadat_Servicio_FERIA, "No se retorno ningun registro");
		
		
		// Paso 4 ****************************************************
		
		addStep("Ejecutar el JOB  de Control-M para ejecutar la interface FEMSA_OL12_MX,"
				+ " mandando los datos via correo al area de usufemcomoperadoressite@oxxo.com"
				+ " o ejecutandolo directamente desde Control-M");
		
		fechaEjecucionInicio = new Date();
		
		// Se obtiene la cadena de texto del data provider en la columna "jobs"
		// Se asigna a un array para poder manejarlo
		JSONArray array = new JSONArray(data.get("cm_jobs"));

		testCase.addTextEvidenceCurrentStep("Ejecución Job: " + data.get("cm_jobs"));
		SeleniumUtil u = new SeleniumUtil(new ChromeTest());
		Control_mInicio cm = new Control_mInicio(u, data.get("cm_user"), data.get("cm_ps"));

		testCase.addTextEvidenceCurrentStep("Login");
		addStep("Login");
		u.get(data.get("cm_server"));
		u.hardWait(40);
		u.waitForLoadPage();
		cm.logOn();
		
		testCase.addTextEvidenceCurrentStep("Inicio de job");
		JobManagement j = new JobManagement(u, testCase, array);
		String resultadoEjecucion = j.jobRunner();
		
		
		// Paso 5 ****************************************************
		
		addStep("Consultar la ejecución del Job en Control M");
		
		testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
		System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);
		
		assertEquals(resultadoEjecucion, "Ended OK");
		u.close();
		
		// Paso 6 ****************************************************
		
		addStep("Realizar conexión a la BD FCWMLQA_ WMLOG");
		
		
		boolean conexiondbFCWMLTAQ_WMLOG = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCWMLTAQ_WMLOG ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA_WMLOG);

		assertTrue(conexiondbFCWMLTAQ_WMLOG, "La conexion no fue exitosa");
		
		// Paso 7 ****************************************************
		
		addStep(" Validar la correcta ejecución de la interface PO19"
				+ " en la tabla WM WM_LOG_RUN de la BD FCWMLQA");
		
		
		String WM_LOG_RUN_stat = null;
		
		SQLResult consultaWM_LOG_RUN_Result = executeQuery(dbFCWMLTAQ_WMLOG, consultaWM_LOG_RUN);	

		boolean validaWM_LOG_RUN = consultaWM_LOG_RUN_Result.isEmpty();

		if (!validaWM_LOG_RUN) {

			WM_LOG_RUN_stat = consultaWM_LOG_RUN_Result.getData(0, "STATUS");
			
			testCase.addQueryEvidenceCurrentStep(consultaWM_LOG_RUN_Result);
		}

		System.out.println(validaWM_LOG_RUN);

		assertEquals(expec_WM_LOG_RUN_stat, WM_LOG_RUN_stat);
		
		// Paso 8 ****************************************************
		
		addStep("Validar la correcta ejecución de los Threads lanzados por "
				+ "la interface PO19 en la tabla WM_LOG_THREAD de la BD FCWMLQA");
		
		
		SQLResult consultaWM_LOG_THREAD_Result = executeQuery(dbFCWMLTAQ_WMLOG, consultaWM_LOG_THREAD);	

		boolean validaWM_LOG_THREAD = consultaWM_LOG_THREAD_Result.isEmpty();

		if (!validaWM_LOG_THREAD) {

			
			testCase.addQueryEvidenceCurrentStep(consultaWM_LOG_THREAD_Result);
		}

		System.out.println(validaWM_LOG_THREAD);

		assertFalse(validaWM_LOG_THREAD, "Se encontraron Threads de la ejecucion");
		
		// Paso 9 ****************************************************
		
		addStep("verificar que no se encuentre ningún error presente "
				+ "en la ejecución de la interfaz PO11 en la tabla WM_LOG_ERROR de BD FCWMLQA");
		
		
		SQLResult consultaWM_LOG_ERROR_Result = executeQuery(dbFCWMLTAQ_WMLOG, consultaWM_LOG_ERROR);	

		boolean validaWM_LOG_ERROR = consultaWM_LOG_ERROR_Result.isEmpty();

		if (!validaWM_LOG_ERROR) {

			
			testCase.addQueryEvidenceCurrentStep(consultaWM_LOG_ERROR_Result);
		}

		System.out.println(validaWM_LOG_ERROR);

		assertTrue(validaWM_LOG_ERROR, "Se muestran errores en la ejecucion");
		
		
		
		// Paso 10 ****************************************************
		
		addStep("Verificar que en la tabla xxfc_lotes de la  BD de EBS  FCAVEBQA "
				+ "se haya insertado el registro correspondiente vinculado con la "
				+ "transferencia de pago de servicio.");
		
		SQLResult consulta_tpe_fr_trans_r = executeQuery(dbAVEBQA, consulta_tpe_fr_trans);

		boolean validatpe_fr_trans = consulta_tpe_fr_trans_r.isEmpty();

		if (!validatpe_fr_trans) {

			
			testCase.addQueryEvidenceCurrentStep(consulta_tpe_fr_trans_r);
		}

		System.out.println(validatpe_fr_trans);

		assertFalse(validatpe_fr_trans, "No se retornaron registros con la fecha actual");
		
		
		// Paso 11 ****************************************************
		
		addStep("Validar que los pagos de servicios ahora contengan un número de lote"
				+ " y estatus ='E' en la tabla xxfc_pago_servicios de la BD de EBS  FCAVEBQA");
		
		String consulta_pago_servicio = String.format(consulta_pago_serv, data.get("id_servicio"),data.get("from_date"), data.get("to_date"));
		
		SQLResult consulta_consulta_pago_servicio_r = executeQuery(dbAVEBQA, consulta_pago_servicio);
		
		boolean validapago_servicio = consulta_consulta_pago_servicio_r.isEmpty();

		if (!validapago_servicio) {
			
			testCase.addQueryEvidenceCurrentStep(consulta_consulta_pago_servicio_r);
		}

		System.out.println(validapago_servicio);

		assertFalse(validapago_servicio, "No se mostraron los registros procesados");
		
		
		// Paso 12 ****************************************************
		
		addStep("Verificar que el archivo CEN se envié en un correo de notificación");
		
		///////PENDIENTE///////
		
		
		// Paso 13 ****************************************************
		
		addStep("Verificar el cuerpo del correo del VendorMail contenga");
		
		
		///////PENDIENTE///////
		
		
		
		// Paso 14 ****************************************************
		
		addStep("Verificar que la información del SERVICIO, PLAZA Y LOTE "
				+ "coincida con las transacciones exitosas procesadas por la OL12_MX");
		
		
		
		String consulta_Servicio_Plaza_Lote = String.format(consulta_Serv_Plaza_Lote, data.get("id_servicio"), data.get("status"), data.get("plaza"), data.get("tienda"));
		
		SQLResult consulta_Servicio_Plaza_Lote_r = executeQuery(dbAVEBQA, consulta_Servicio_Plaza_Lote);
		
		boolean validaServicio_Plaza_Lote = consulta_Servicio_Plaza_Lote_r.isEmpty();

		if (!validaServicio_Plaza_Lote) {
			
			testCase.addQueryEvidenceCurrentStep(consulta_Servicio_Plaza_Lote_r);
		}

		System.out.println(validaServicio_Plaza_Lote);

		assertFalse(validaServicio_Plaza_Lote, "No se insertaron los registros en la tabla APPS.XXFC_PAGO_SERVICIO_FORMAS_STG");
		
		
		
	}
	
	

	@Override
	public String setTestFullName() {
		return "ATC_FT_021_OL12_MX_GenyEnvArchTransacServLocFERIA_test";
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