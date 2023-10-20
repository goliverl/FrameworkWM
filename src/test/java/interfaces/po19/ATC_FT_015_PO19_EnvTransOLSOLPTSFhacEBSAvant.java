package interfaces.po19;



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

public class ATC_FT_015_PO19_EnvTransOLSOLPTSFhacEBSAvant extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_015_PO19_EnvTransOLSOLPTSFhacEBSAvant_test(HashMap<String, String> data) throws Exception {

		/**
		 * Proyecto: BackOffice Oracle
		 * Caso de prueba: MTS-FT-015-C1 PO19 Envio de transacciones OLS,OLP, TSF hacia 
		 * EBS Avante a traves de la interface PO19
		 * @author edwin.ramirez
		 * @date 2022/Dic/17
		 */
		
		/*
		 * Utilerias
		 *********************************************************************/

		
		SQLUtil dbFCTPEQA = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, 
				GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		
		SQLUtil dbFCMWL6QA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, 
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		SQLUtil dbFCWMLQA_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG,
				GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		
		SQLUtil dbAVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		
		
		/*
		 * Variables
		 *********************************************************************/
		//Paso 2
		String consulta_tpe_fr_ent = "SELECT APPLICATION,ENTITY,DESCRIPTION,STATUS\r\n"
				+ "FROM tpeuser.tpe_fr_ent_cat \r\n"
				+ "WHERE APPLICATION IN ('OLS','OLP','TSF')\r\n"
				+ "AND ENTITY IN('FL','SKY','OXXO','CFE','RTPQRO','ATT','GEN','TELCELPAGO') \r\n"
				+ "AND STATUS = 'A'";
		//Paso 3
		String consulta_tpe_fr_docs = "SELECT * FROM(\r\n"
				+ "SELECT ID,FOLIO,PLAZA,TIENDA,CAJA,CREATION_DATE,STATUS\r\n"
				+ "FROM TPEUSER.TPE_FR_DOCS_POS\r\n"
				+ "WHERE CREATION_DATE >= SYSDATE -5 AND STATUS = 'L'\r\n"
				+ "ORDER BY CREATION_DATE DESC\r\n"
				+ ")WHERE ROWNUM <= 10";
		//Paso 4
		String consulta_tpe_fr_trans = "SELECT * FROM(\r\n"
				+ "SELECT APPLICATION,ENTITY,OPERATION,FOLIO,CREATION_DATE,PLAZA,TIENDA,STATUS\r\n"
				+ "FROM TPEUSER.tpe_fr_transaction WHERE STATUS='L' \r\n"
				+ "AND FECHA_TRANSACCION IS NOT NULL \r\n"
				+ "AND CREATION_DATE  >= SYSDATE-5\r\n"
				+ "ORDER BY CREATION_DATE DESC\r\n"
				+ ")WHERE ROWNUM <=10";
		//Paso 8
		String consultaWM_LOG_RUN = "SELECT * FROM(\r\n"
				+ "SELECT RUN_ID,INTERFACE,START_DT,END_DT,STATUS,SERVER \r\n"
				+ "FROM WMLOG.WM_LOG_RUN \r\n"
				+ "WHERE INTERFACE LIKE '%PO19%' \r\n"
				+ "ORDER BY START_DT DESC\r\n"
				+ ")WHERE ROWNUM <= 10";
		//Paso 9
		String consultaWM_LOG_THREAD  = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,STATUS \r\n"
				+ "FROM  WMLOG.WM_LOG_THREAD WHERE PARENT_ID = '%s'";
		
		String consultaWM_LOG_ERROR = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,SEVERITY,ERROR_TYPE \r\n"
				+ "FROM  WMLOG.WM_LOG_ERROR \r\n"
				+ "WHERE RUN_ID = '%s'";
		
		String consulta_tpe_fr_trans_2 = "SELECT APPLICATION,ENTITY,OPERATION, FOLIO,CREATION_DATE,PLAZA,TIENDA,STATUS\r\n"
				+ "FROM TPEUSER.TPE_FR_TRANSACTION WHERE FOLIO IN ('%s')\r\n"
				+ "AND CREATION_DATE >= SYSDATE-5 \r\n"
				+ "AND STATUS='I'\r\n"
				+ "AND FECHA_TRANSACCION IS NOT NULL \r\n"
				+ "ORDER BY CREATION_DATE DESC";
		
		String consulta_tpe_fr_docs_2 = "SELECT * FROM TPEUSER.TPE_FR_DOCS_POS WHERE CREATION_DATE >= sysdate -5 AND STATUS='I' AND FOLIO IN ([FOLIO])\r\n" + 
				"ORDER BY CREATION_DATE DESC";
		
		String consulta_Pago_Servicios_PRE = "select * from xxfc.XXFC_PAGO_SERVICIOS_PRE\r\n" + 
				"where PLAZA = '[PLAZA]'\r\n" + 
				"and TIENDA in ('[TIENDA]')\r\n" + 
				"and SERVICIO in ('[ID_SERVICIO]')\r\n" + 
				"and FECHA_RECEPCION  >= TRUNC(SYSDATE)";
		
		String consulta_Pago_Servicios_FORMAS = "";
		
		String expec_tpe_fr_status = "A";
		String expec_tpe_fr_trans_status = "L";
		String expec_WM_LOG_RUN_stat = "S";
		String expec_tpe_fr_trans_2_status = "I";
		
		Date fechaEjecucionInicio;
		
		testCase.setTest_Description(data.get("id") + data.get("description"));
		
		testCase.setPrerequisites("prerequisites");
		
	
		// Paso 1 ****************************************************
		addStep("Realizar conexión a la BD FCTPEQA");
		
		
		boolean conexiondbFCTPEQA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCTPEQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCTPE);

		assertTrue(conexiondbFCTPEQA, "La conexion no fue exitosa");
	
		
		// Paso 2 ****************************************************
		
		addStep("Validar que los registros en la tabla tpe_fr_ent_cat "
				+ "se encuentren con STATUS en valor A, en la BD FCTPEQA");
		System.out.println("Paso 2: \n"+consulta_tpe_fr_ent);
		
		SQLResult consulta_tpe_fr_ent_r = executeQuery(dbFCTPEQA, consulta_tpe_fr_ent);

		String tpe_fr_status = "";
		

		boolean validatpe_fr_ent = consulta_tpe_fr_ent_r.isEmpty();

		if (!validatpe_fr_ent) {

			tpe_fr_status = consulta_tpe_fr_ent_r.getData(0, "STATUS");

			System.out.println("STATUS: " + tpe_fr_status);
			
			testCase.addQueryEvidenceCurrentStep(consulta_tpe_fr_ent_r);
		}

		System.out.println(validatpe_fr_ent);

		assertFalse(validatpe_fr_ent, "El status obtenido es diferente");
		assertEquals(expec_tpe_fr_status, tpe_fr_status);
		
		
		// Paso 3 ****************************************************
		
		addStep("Validar que existan transacciones en la tabla TPEUSER.TPE_FR_DOCS_POS "
				+ "con estatus L y con fecha no mayor a 5 días en la BD FCTPEQA");
		System.out.println("Paso 3: \n"+consulta_tpe_fr_docs);
	
		SQLResult consulta_tpe_fr_docs_r = executeQuery(dbFCTPEQA, consulta_tpe_fr_docs);
		String folio = "";
		
		boolean validatpe_fr_docs = consulta_tpe_fr_docs_r.isEmpty();

		if (!validatpe_fr_docs) {
			folio = consulta_tpe_fr_docs_r.getData(0, "FOLIO");
			testCase.addQueryEvidenceCurrentStep(consulta_tpe_fr_docs_r);
		}

		System.out.println(validatpe_fr_docs);

		assertFalse(validatpe_fr_docs, "No existen transacciones para procesar de la entidad GDF");
		
		
		// Paso 4 ****************************************************
		
		addStep("Validar que existan transacciones de servicios "
				+ "en línea en la TPEUSER.tpe_fr_transaction para diferentes "
				+ "entidades con estatus"
				+ " en L y con fecha no mayor a 7 días en la BD FCTPEQA");
		System.out.println("Paso 4: \n"+consulta_tpe_fr_trans);
		
		SQLResult consulta_tpe_fr_trans_r = executeQuery(dbFCTPEQA, consulta_tpe_fr_trans);

		String tpe_fr_trans_status = "";
		

		boolean validatpe_fr_trans = consulta_tpe_fr_trans_r.isEmpty();

		if (!validatpe_fr_trans) {

			tpe_fr_trans_status = consulta_tpe_fr_trans_r.getData(0, "STATUS");

			System.out.println("STATUS: " + tpe_fr_trans_status);
			
			testCase.addQueryEvidenceCurrentStep(consulta_tpe_fr_trans_r);
		}

		System.out.println(validatpe_fr_trans);

		assertFalse(validatpe_fr_trans, "El status obtenido es diferente");
		assertEquals(expec_tpe_fr_trans_status, tpe_fr_trans_status);
		
		
		// Paso 5 ****************************************************
		
		addStep("Solicitar a los Usu FC Operadores SITE vía correo electrónico "
				+ "<UsuFEMCOMOperadoresSITE@oxxo.com>,la ejecución de la interfaz PO19");
	
		
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
		
		
		// Paso 6 ****************************************************
		
		addStep("Consultar la ejecución del Job en Control M");
		
		testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
		System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);
		
		assertEquals(resultadoEjecucion, "Ended OK");
		u.close();
		
		// Paso 7 ****************************************************
		
		addStep("Realizar la conexión a la BD FCWMLQA_WMLOG");
		
		
		boolean conexiondbFCWMLQA_WMLOG = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCWMLQA_WMLOG ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA_WMLOG);

		assertTrue(conexiondbFCWMLQA_WMLOG, "La conexion no fue exitosa");
		
		// Paso 8 ****************************************************
		
		addStep(" Validar la correcta ejecución de la interface PO19"
				+ " en la tabla WM WM_LOG_RUN de la BD FCWMLQA_WMLOG");
		System.out.println("Paso 8: \n"+consultaWM_LOG_RUN);
		
		String WM_LOG_RUN_stat = null;
		String WM_LOG_RUN_runId = null;
		
		SQLResult consultaWM_LOG_RUN_Result = executeQuery(dbFCWMLQA_WMLOG, consultaWM_LOG_RUN);	

		boolean validaWM_LOG_RUN = consultaWM_LOG_RUN_Result.isEmpty();

		if (!validaWM_LOG_RUN) {

			WM_LOG_RUN_stat = consultaWM_LOG_RUN_Result.getData(0, "STATUS");
			WM_LOG_RUN_runId = consultaWM_LOG_RUN_Result.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(consultaWM_LOG_RUN_Result);
		}

		System.out.println(validaWM_LOG_RUN);

		assertEquals(WM_LOG_RUN_stat, expec_WM_LOG_RUN_stat);
		
		// Paso 9 ****************************************************
		
		addStep("Validar la correcta ejecución de los Threads lanzados por "
				+ "la interface PO19 en la tabla WM_LOG_THREAD de la BD FCWMLQA");
		String consultaWM_LOG_THREAD_format = String.format(consultaWM_LOG_THREAD,WM_LOG_RUN_runId);
		System.out.println("Paso 9: \n"+consultaWM_LOG_THREAD_format);
		
		SQLResult consultaWM_LOG_THREAD_Result = executeQuery(dbFCWMLQA_WMLOG, consultaWM_LOG_THREAD_format);	

		boolean validaWM_LOG_THREAD = consultaWM_LOG_THREAD_Result.isEmpty();

		if (!validaWM_LOG_THREAD) {

			testCase.addQueryEvidenceCurrentStep(consultaWM_LOG_THREAD_Result);
		}

		System.out.println(validaWM_LOG_THREAD);

		assertTrue(validaWM_LOG_THREAD, "Se encontraron Threads de la ejecucion");
		
		// Paso 10 ****************************************************
		
		addStep("verificar que no se encuentre ningún error presente "
				+ "en la ejecución de la interfaz PO11 en la tabla WM_LOG_ERROR de BD FCWMLQA");
		String consultaWM_LOG_ERROR_format = String.format(consultaWM_LOG_ERROR,WM_LOG_RUN_runId);
		System.out.println("Paso 10: \n"+consultaWM_LOG_ERROR_format);
		
		SQLResult consultaWM_LOG_ERROR_Result = executeQuery(dbFCWMLQA_WMLOG, consultaWM_LOG_ERROR_format);	

		boolean validaWM_LOG_ERROR = consultaWM_LOG_ERROR_Result.isEmpty();

		if (!validaWM_LOG_ERROR) {

			
			testCase.addQueryEvidenceCurrentStep(consultaWM_LOG_ERROR_Result);
		}

		System.out.println(validaWM_LOG_ERROR);

		assertTrue(validaWM_LOG_ERROR, "Se muestran errores en la ejecucion");
		
		
		
		// Paso 11 ****************************************************
		
		addStep("Validar que en la Tpeuser.TPE_FR_TRANSACTION se le actualizo el "
				+ "status de las transacciones al valor I, en la BD FCTPEQA");
		String consulta_tpe_fr_trans_2_r_format = String.format(consulta_tpe_fr_trans_2,folio);
		System.out.println("Paso 11: \n"+consulta_tpe_fr_trans_2_r_format);
		
		SQLResult consulta_tpe_fr_trans_2_r = executeQuery(dbFCTPEQA, consulta_tpe_fr_trans_2_r_format);

		String tpe_fr_trans_2_status = "";
		
		boolean validatpe_fr_trans_2 = consulta_tpe_fr_trans_2_r.isEmpty();

		if (!validatpe_fr_trans_2) {

			tpe_fr_trans_2_status = consulta_tpe_fr_trans_r.getData(0, "STATUS");

			System.out.println("STATUS: " + tpe_fr_trans_2_status);
			
			testCase.addQueryEvidenceCurrentStep(consulta_tpe_fr_trans_r);
		}

		System.out.println(validatpe_fr_trans);

		assertFalse(validatpe_fr_trans, "El status obtenido es diferente");
		assertEquals(tpe_fr_trans_2_status, expec_tpe_fr_trans_2_status);
		
		
		
		// Paso 12 ****************************************************
		
		addStep(" Validar que en la TPE _FR_DOCS_POS se le actualizo "
				+ "el status de las transacciones al valor I, en la BD FCTPEQA");
		
		
		
		SQLResult consulta_tpe_fr_docs_2_r = executeQuery(dbFCTPEQA, consulta_tpe_fr_docs_2);
		
		boolean validatpe_fr_docs_2 = consulta_tpe_fr_docs_2_r.isEmpty();

		if (!validatpe_fr_docs_2) {
			
			testCase.addQueryEvidenceCurrentStep(consulta_tpe_fr_docs_2_r);
		}

		System.out.println(validatpe_fr_docs_2);

		assertFalse(validatpe_fr_docs_2, "No se actualizo correctamente el status");
		
		
		// Paso 13 ****************************************************
		
		addStep("Realizar conexión en la BD AVEBQA de EBS");
		
		boolean conexiondbAVEBQA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbAVEBQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_AVEBQA);

		assertTrue(conexiondbAVEBQA, "La conexion no fue exitosa");
		
		// Paso 14 ****************************************************
		
		addStep("Validar que se insertaron los registros en la XXFC_PAGO_SERVICIOS_PRE"
				+ " en la BD AVEBQA de EBS. provenientes de la tabla tpe_fr_transaction");
		
		
		
		SQLResult consulta_Pago_Servicios_PRE_r = executeQuery(dbAVEBQA, consulta_Pago_Servicios_PRE);
		
		boolean validaPago_Servicios_PRE = consulta_Pago_Servicios_PRE_r.isEmpty();

		if (!validaPago_Servicios_PRE) {
			
			testCase.addQueryEvidenceCurrentStep(consulta_Pago_Servicios_PRE_r);
		}

		System.out.println(validaPago_Servicios_PRE);

		assertFalse(validaPago_Servicios_PRE, "No se actualizo correctamente el status");
		
		
		
		
		// Paso 15 ****************************************************
		
		addStep("Validar que se insertaron los registros en la "
				+ "APPS.XXFC_PAGO_SERVICIO_FORMAS_STG en la BD AVEBQA de "
				+ "EBS.provenientes de la tabla TPE_FR_DOCS_POS");
		
		
		SQLResult consulta_Pago_Servicios_FORMAS_r = executeQuery(dbAVEBQA, consulta_Pago_Servicios_FORMAS);
		
		boolean validaPago_Servicios_FORMAS = consulta_Pago_Servicios_FORMAS_r.isEmpty();

		if (!validaPago_Servicios_FORMAS) {
			
			testCase.addQueryEvidenceCurrentStep(consulta_Pago_Servicios_FORMAS_r);
		}

		System.out.println(validaPago_Servicios_FORMAS);

		assertFalse(validaPago_Servicios_FORMAS, "No se insertaron los registros en la tabla APPS.XXFC_PAGO_SERVICIO_FORMAS_STG");
		
		
	}
	
	

	@Override
	public String setTestFullName() {
		return "ATC_FT_015_PO19_EnvTransOLSOLPTSFhacEBSAvant_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
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