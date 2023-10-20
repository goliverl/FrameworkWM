package interfaces.po9;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.json.JSONArray;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLResultExcel;
import utils.sql.SQLUtil;

/**
 * 004-FT-BACK OFFICE AVANTE: MTC_FT_005-3 PO9 Procesar archivo TPE de
 * transacciones de pago electrónico de la interface FEMSA_PO9 (EBS 12.2.4
 * CHO_52074285) Desc: Prueba de regresión para comprobar la no afectación en la
 * funcionalidad principal de la interface FEMSA_PO9 de avante para procesar
 * archivos TPE (Transacciones de Pago Electrónico) y ser enviados de WM INBOUND
 * a EBS, al ser migrada la interface de WM9.9 a WM10.5. Transacciones de Pago
 * Electrónicas La interface se encarga de transferir la información relativa a
 * las formas de pago Electrónicas de la tienda (POS) hacia Oracle GL. Origen:
 * POS Documentos TPE (Transacciones de Pago Electrónico) procesados por la
 * interface PR50 Destino: Oracle GL Pólizas insertadas en la tabla Open
 * interface de Oracle GL. NOTA: EBS 12.2.4
 * 
 * @author Roberto Flores
 * @date 2022/06/30
 */
public class ATC_FT_003_PO9_WMx86_ProcesarArchivoTPEPagoElectronico extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PO9_WMx86_ProcesarArchivoTPE_test(HashMap<String, String> data) throws Exception {

		testCase.setPrerequisites(
				"*Contar con acceso a las bases de datos de FCWM6QA, FCWMLQA y AVEBQA (EBS) de Avante.\r\n"
						+ "*Contar con el nombre y grupo del nuevo Job de Control M para la ejecución de la interface PO9.\r\n"
						+ "*Contar con acceso al repositorio de buzón de la tienda.\r\n"
						+ "*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA de Back Office. \r\n"
						+ "*Haber procesado un archivo TPE, por medio de la DS50 desde un punto de venta XPOS, para ser enviado a WM.\r\n"
						+ "");

		/*
		 * Utilerías
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));

		SQLUtil FCWM6QA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,
				GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil AVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil FCWMLQA_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG,
				GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);

		/*
		 * Querys
		 *********************************************************************/
		// Paso 2
		String qryRegistrosProcesar = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,BACKUP_STATUS "
				+ "FROM posuser.POS_INBOUND_DOCS " + "WHERE PV_DOC_NAME LIKE 'TPE%' " + "AND DOC_TYPE='TPE' "
				+ "AND STATUS='I'";

		String qryHeaderTPE = "SELECT * FROM POSUSER.POS_TPE WHERE PID_ID IN ('%s')";

		String qryaRegistrosTpeDetl = "SELECT * FROM POSUSER.POS_TPE_DETL WHERE PID_ID IN ('%s')";

		//Paso 7
		String qryActualizacion = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,BACKUP_STATUS  \r\n"
				+ "FROM posuser.POS_INBOUND_DOCS WHERE PV_DOC_NAME = 'TPE' \r\n"
				+ "AND DOC_TYPE='TPE' AND STATUS='E' AND ID IN '%s'";

		String qryWmLogRun = "select * \r\n" + "from wm_log_run \r\n"
				+ "where interface LIKE '%s' AND status = 'S' \r\n"
				+ "AND start_dt >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + "ORDER BY RUN_ID DESC";

		String qryThreads = "select * from WM_LOG_THREAD where PARENT_ID = %s and status = 'S'";

		String qryError = "SELECT * FROM WM_LOG_ERROR WHERE RUN_ID= %s";
		
		String qryPoliza = "SELECT --*\r\n"
				+ " DATE_CREATED, USER_JE_CATEGORY_NAME, SEGMENT1, SEGMENT2, SEGMENT3, GROUP_ID\r\n"
				+ " FROM GL.GL_INTERFACE\r\n"
				+ " WHERE DATE_CREATED >= SYSDATE\r\n"
				+ "ORDER BY DATE_CREATED DESC";

		Date fechaEjecucionInicio;
		
		fechaEjecucionInicio = new Date();

		testCase.setProject_Name("POC WMx86");

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	

		/****************************************************************************************************************************************
		 * Paso 1
		 **************************************************************************************************************************************/
		addStep("Establecer la conexión con la BD **FCWM6QA**.");

		testCase.addTextEvidenceCurrentStep("Conexion: FCWM6QA");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		/****************************************************************************************************************************************
		 * Paso 2
		 **************************************************************************************************************************************/
		addStep("Validar que haya registros a procesar con status='I' y doc_type='TPE' de las plazas [PLAZA], en la BD FCWM6QA");

		System.out.println("Paso 2: " + GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(qryRegistrosProcesar);

		SQLResult qryRegistrosProcesar_r = executeQuery(FCWM6QA, qryRegistrosProcesar);
		String idRegistro = "";
		boolean validaConsultaPlaza = qryRegistrosProcesar_r.isEmpty();

		if (!validaConsultaPlaza) {
			idRegistro = qryRegistrosProcesar_r.getData(0, "ID");
			testCase.addQueryEvidenceCurrentStep(qryRegistrosProcesar_r);
		}

		/****************************************************************************************************************************************
		 * Paso 3
		 **************************************************************************************************************************************/
		addStep("Validar el Header del archivo TPE  con la tabla POSUSER.POS_TPE ");

		System.out.println("Paso 3: " + GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String qryHeaderTPE_f = String.format(qryHeaderTPE, idRegistro);
		System.out.println(qryHeaderTPE_f);

		SQLResult qryHeaderTPE_r = executeQuery(FCWM6QA, qryHeaderTPE_f);
		testCase.addQueryEvidenceCurrentStep(qryHeaderTPE_r, true);

		assertFalse(qryHeaderTPE_r.isEmpty());

		/****************************************************************************************************************************************
		 * Paso 4
		 **************************************************************************************************************************************/
		addStep("Validar el detalle de los archivos TPE con la tabla POSUSER.POS_TPE_DETL");

		System.out.println("Paso 4: " + GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String qryaRegistrosTpeDetl_f = String.format(qryaRegistrosTpeDetl, idRegistro);
		System.out.println(qryaRegistrosTpeDetl_f);

		SQLResultExcel qryaRegistrosTpeDetl_r = executeQueryExcel(FCWM6QA, qryaRegistrosTpeDetl_f);
		testCase.addDocumentEvidence(qryaRegistrosTpeDetl_r.getRelativePath(), "consulta registros pos_tpe_detl");

		assertFalse(qryaRegistrosTpeDetl_r.isEmpty());

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/****************************************************************************************************************************************
		 * Paso 5
		 **************************************************************************************************************************************/
	/*	addStep("Ejecución control-M");

		

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

		/****************************************************************************************************************************************
		 * Paso 6
		 **************************************************************************************************************************************/
	/*	addStep("Abrir la herramienta de control M para validar que la ejecución del job haya sido exitosa");

		testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
		System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);

		assertEquals(resultadoEjecucion, "Ended OK");
		u.close();
	
		/****************************************************************************************************************************************
		 * Paso 7
		 **************************************************************************************************************************************/
		addStep("Verificar que los registros se hayan procesado correctamente y tengan status=E, en la BD FCWM6QA");
		System.out.println("ID REGISTRO: " + idRegistro);
		System.out.println("Paso 7: " + GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String qryActualizacionFormat = String.format(qryActualizacion, idRegistro);
		System.out.println(qryActualizacionFormat);

		SQLResult qryActualizacionInboundDocs_r = executeQuery(FCWM6QA, qryActualizacionFormat);
		testCase.addQueryEvidenceCurrentStep(qryActualizacionInboundDocs_r, true);

		assertFalse(qryActualizacionInboundDocs_r.isEmpty());

		

		/****************************************************************************************************************************************
		 * Paso 8
		 **************************************************************************************************************************************/
		addStep("Realizar conexión a la BD FCWMLTAQ.FEMCOM.NET del host oxfwm6q00.femcom.net.");
		System.out.println("Paso 8: " + GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCWMLQA_WMLOG");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA_WMLOG);

		/****************************************************************************************************************************************
		 * Paso 9
		 **************************************************************************************************************************************/
		addStep("Validar la correcta ejecución de la interface PO9 en la tabla WM WM_LOG_RUN de la BD FCWML6QA.");
		System.out.println("Paso 9: " + GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		
		String fechaEjecucionInicio_f = formatter.format(fechaEjecucionInicio);

		String qryWmLogRun_f = String.format(qryWmLogRun, "%PO9%", fechaEjecucionInicio_f);
		System.out.println("qryWmLogRun_f: \r\n " + qryWmLogRun_f);

		SQLResult qryWmLogRun_r = executeQuery(FCWMLQA_WMLOG, qryWmLogRun_f);
		testCase.addQueryEvidenceCurrentStep(qryWmLogRun_r, true);

		assertFalse(qryWmLogRun_r.isEmpty());

		String runId = qryWmLogRun_r.getData(0, "RUN_ID");

		/****************************************************************************************************************************************
		 * Paso 10
		 **************************************************************************************************************************************/
		addStep("Validar la correcta ejecución de los Threads lanzados por la interface PO9 en la tabla WM_LOG_THREAD de la BD FCWML6QA");
		System.out.println("Paso 10: " + GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		
		String qryThreads_f = String.format(qryThreads, runId);
		System.out.println("qryThreads_f: \r\n " + qryThreads_f);

		SQLResult qryThreads_r = executeQuery(FCWMLQA_WMLOG, qryThreads_f);
		testCase.addQueryEvidenceCurrentStep(qryThreads_r, true);

		assertFalse(qryThreads_r.isEmpty());

		/****************************************************************************************************************************************
		 * Paso 11
		 **************************************************************************************************************************************/
		addStep("Realizar la siguiente consulta para verificar que no se encuentre ningún error presente en la ejecución de la interfaz PO9 en la tabla WM_LOG_ERROR de BD FCWML6QA.");
		System.out.println("Paso 11: " + GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
		
		String qryError_f = String.format(qryError, runId);
		System.out.println("qryError_f: \r\n " + qryError);

		SQLResult qryError_r = executeQuery(FCWMLQA_WMLOG, qryError_f);
		testCase.addQueryEvidenceCurrentStep(qryError_r, true);

		assertTrue(qryError_r.isEmpty());

		/****************************************************************************************************************************************
		 * Paso 12
		 **************************************************************************************************************************************/
		addStep("Realizar conexión a la BD AVEBQA de EBS.");
		System.out.println("Paso 12: " + GlobalVariables.DB_HOST_AVEBQA);

		testCase.addTextEvidenceCurrentStep("Conexion: AVEBQA");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_AVEBQA);
		
		/****************************************************************************************************************************************
		 * Paso 13
		 **************************************************************************************************************************************/
		addStep("Validar que la póliza de los registros se hayan generado en EBS.");
		System.out.println("Paso 13: " + GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(qryPoliza);
		
		SQLResult qryPoliza_r = executeQuery(AVEBQA, qryPoliza);
		
		boolean validaQryPoliza = qryPoliza_r.isEmpty();

		if (!validaQryPoliza) {
			testCase.addQueryEvidenceCurrentStep(qryPoliza_r);
		}
		System.out.println(validaQryPoliza);
		
		assertFalse(validaQryPoliza);
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_003_PO9_WMx86_ProcesarArchivoTPEPagoElectronico";
	}

	@Override
	public String setTestDescription() {
		return "MTC_FT_005-3 PO9 Procesar archivo TPE de transacciones de pago electrónico de la interface FEMSA_PO9 (EBS 12.2.4 CHO_52074285)";
	}

	@Override
	public String setTestDesigner() {
		return "AutomationQA";
	}

	@Override
	public String setTestInstanceID() {
		return null;
	}

	@Override
	public void beforeTest() {
	}

	@Override
	public String setPrerequisites() {
		return null;
	}

}
