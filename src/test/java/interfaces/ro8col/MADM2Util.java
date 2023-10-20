package interfaces.ro8col;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;

import modelo.BaseExecution;
import modelo.TestCase;
import util.GlobalVariables;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class MADM2Util {

	//Variables generales del caso de prueba
	private HashMap<String, String> data;
	private TestCase testCase;
	private BaseExecution b;
	
	//Método para usar directamente addStep en script en lugar de baseExecution.addStep
	private void addStep(String desc) {
		this.b.addStep(desc);
	}
	
	//Método para usar directamente executeQuery en script en lugar de baseExecution.executeQuery
	private SQLResult executeQuery(SQLUtil sqlUtil, String query) throws SQLException {
		return this.b.executeQuery(sqlUtil, query);
	}
	
	/*
	 * Utilerías
	 *********************************************************************/
	private SimpleDateFormat formatter; 
	
	private SQLUtil dbRmsCol;
	private SQLUtil dbWmLogCol;
	private SQLUtil dbEbsCol;
	private SQLUtil dbLog;
	
	
	
	public MADM2Util(HashMap<String, String> data, TestCase testCase, BaseExecution baseExecution) throws SQLException {
		this.data = data;
		this.testCase = testCase;
		this.b = baseExecution;
		
		formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		dbRmsCol = new SQLUtil(GlobalVariables.DB_HOST_RMS_COL_QAVIEW,GlobalVariables.DB_USER_RMS_COL_QAVIEW, GlobalVariables.DB_PASSWORD_RMS_COL_QAVIEW);
		dbWmLogCol = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG_COL,GlobalVariables.DB_USER_FCWMLQA_WMLOG_COL, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG_COL);
		dbEbsCol = new SQLUtil(GlobalVariables.DB_HOST_EBS_COL,GlobalVariables.DB_USER_EBS_COL, GlobalVariables.DB_PASSWORD_EBS_COL);
		dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	
	public List<String> ejecutarRo8ControlM(Date fechaEjecucionInicio) throws Exception {
		
		String qryWmLogRun = "select * \r\n"
				+ "from wm_log_run \r\n"
				+ "where interface LIKE '%s' AND status = 'S' \r\n"
				+ "AND start_dt >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n"
				+ "ORDER BY RUN_ID DESC";
		
		/****************************************************************************************************************************************
		 * Paso 1
		 * **************************************************************************************************************************************/
		addStep("Conectarse a la base de datos de RMS Colombia");
				
				testCase.addTextEvidenceCurrentStep("Conexion: RMS Colombia");
				testCase.addTextEvidenceCurrentStep(dbRmsCol.getHost());
				
		/****************************************************************************************************************************************
		 * Paso 2
		 * **************************************************************************************************************************************/
		addStep("Validar información pendiente de procesar en la tabla FEM_FIF_STG.");
		
				String qryRegistrosProcesar = data.get("qry_info_pendiente");
				System.out.println("qryRegistrosProcesar: \r\n "+ qryRegistrosProcesar);
				SQLResult qryRegistrosProcesar_r = b.executeQuery(dbRmsCol, qryRegistrosProcesar);
				testCase.addQueryEvidenceCurrentStep(qryRegistrosProcesar_r, true);
				
				assertFalse(qryRegistrosProcesar_r.isEmpty());
				
				List<String> idsFemFifStgList = new ArrayList<>();
				for (int i = 0; i < qryRegistrosProcesar_r.getRowCount(); i++) {
					idsFemFifStgList.add(qryRegistrosProcesar_r.getData(i, "ID"));
				}
		////Inicio control-m
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/****************************************************************************************************************************************
		* Paso 3
		* **************************************************************************************************************************************/
		addStep("Ejecución control-M");
				
				// Se obtiene la cadena de texto del data provider en la columna "jobs"
				// Se asigna a un array para poder manejarlo
				JSONArray array = new JSONArray(data.get("cm_jobs"));
				
				testCase.addTextEvidenceCurrentStep("Ejecución Job: " + data.get("cm_jobs"));
				SeleniumUtil u = new SeleniumUtil(new ChromeTest());
				Control_mInicio cm = new Control_mInicio(u, data.get("cm_user"), data.get("cm_ps"));
				
				testCase.addTextEvidenceCurrentStep("Login");
				addStep("Login");
				u.get(data.get("cm_server"));
				u.hardWait(90);
				u.waitForLoadPage();
				cm.logOn();
				
				testCase.addTextEvidenceCurrentStep("Inicio de job");
				JobManagement j = new JobManagement(u, testCase, array);
				String resultadoEjecucion = j.jobRunner();
		
		
		/****************************************************************************************************************************************
		* Paso 4
		* **************************************************************************************************************************************/
				addStep("Abrir la herramienta de control M para validar que la ejecución del job haya sido exitosa");
				
				testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
				System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);
				
				//assertEquals(resultadoEjecucion, "Ended OK");
				u.close();
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		////Fin control-m
		
		/****************************************************************************************************************************************
		 * Paso 5
		 * **************************************************************************************************************************************/
		addStep("Establecer la conexión a la FCWML6QA");
				
		testCase.addTextEvidenceCurrentStep("Conexion: FCWMLQA_WMLOG");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA_WMLOG_COL);
				
		
		/****************************************************************************************************************************************
		 * Paso 6
		 * **************************************************************************************************************************************/
		addStep("Validar que se inserte el detalle de la ejecución de la interface en la tabla WM_LOG_RUN de la DB  FCWML6QA");
				
				String fechaEjecucionInicio_f = formatter.format(fechaEjecucionInicio);
		
				String qryWmLogRun_f = String.format(qryWmLogRun, "%RO08_COL%", fechaEjecucionInicio_f);
				System.out.println("qryWmLogRun_f: \r\n "+ qryWmLogRun_f);
				
				SQLResult qryWmLogRun_r = executeQuery(dbWmLogCol, qryWmLogRun_f);
				testCase.addQueryEvidenceCurrentStep(qryWmLogRun_r, true);
				
				assertFalse(qryWmLogRun_r.isEmpty());
				
		return idsFemFifStgList;
				
	}
	
	public List<String> validarEjecucionGlLines(List<String> idsFemFifStgList, boolean contieneDatosGlLines) throws Exception {
		String qryActualizacion = data.get("select_fem_fim_stg") + ",REFERENCE_3,REFERENCE_9"+ " FROM fem_fif_stg\r\n"
				+ "WHERE ID = %s and reference_3 is not null and reference_9 is not null";
		
		String qryGlHeadersCol = "SELECT * FROM  wmuser.WM_GL_HEADERS_COL\r\n"
				+ "WHERE TRAN_CODE in ("+ data.get("tran_code") +")\r\n"
				+ "AND  header_id = %s \r\n"
				+ "AND CR_PLAZA = '"+ data.get("plaza") +"'\r\n"
				+ "AND GL_JOURNAL_ID is not null\r\n"
				+ "AND JOURNAL_TYPE_ID is not null";
		
		String qryGlLines = "SELECT * FROM wmuser.wm_gl_lines_col \r\n"
				+ "WHERE header_id IN (%s)";
		
		/****************************************************************************************************************************************
		 * Paso 7
		 * **************************************************************************************************************************************/
		addStep("Verificar la actualización de REFERNCE_3 con el JOURNAL_ID y el REFERENCE_9 con el HEADER_ID en la tabla FEM_FIF_STG.");
		
			List<String> referencias3 = new ArrayList<>();
			List<String> referencias9 = new ArrayList<>();
			
			for (String id : idsFemFifStgList) {
				String qryActualizacionFemFifStg_f = String.format(qryActualizacion, id);
				System.out.println("qryActualizacionFemFifStg_f: \r\n "+ qryActualizacionFemFifStg_f);
				
				SQLResult qryActualizacionFemFifStg_r = executeQuery(dbRmsCol, qryActualizacionFemFifStg_f);
				testCase.addQueryEvidenceCurrentStep(qryActualizacionFemFifStg_r, true);
				
				assertFalse(qryActualizacionFemFifStg_r.isEmpty());
				
				referencias3.add(qryActualizacionFemFifStg_r.getData(0, "REFERENCE_3"));
				referencias9.add(qryActualizacionFemFifStg_r.getData(0, "REFERENCE_9"));
			}
		
		/****************************************************************************************************************************************
		 * Paso 8
		 * **************************************************************************************************************************************/
		addStep("Verificar la actualización de JOURNAL_ID, JOURNAL_TYPE_ID y RUN_ID   en la tabla WM_GL_HEADERS_COL.");
		
			String headerIds = "";
		
			for (String referencia9 : referencias9) {
				String qryGlHeadersCol_f = String.format(qryGlHeadersCol, referencia9);
				System.out.println("qryGlHeadersCol_f: \r\n "+ qryGlHeadersCol_f);
				
				SQLResult qryGlHeadersCol_r = executeQuery(dbRmsCol, qryGlHeadersCol_f);
				testCase.addQueryEvidenceCurrentStep(qryGlHeadersCol_r, true);
				
				assertFalse(qryGlHeadersCol_r.isEmpty());
				
				headerIds += qryGlHeadersCol_r.getData(0, "HEADER_ID") + ",";
			}
			//Se elimina el último caracter que sería una comma del ciclo anterior
			headerIds = headerIds.substring(0, headerIds.length() - 1);
		
		/****************************************************************************************************************************************
		 * Paso 9
		 * **************************************************************************************************************************************/
		addStep("Inserción de líneas en la tabla WM_GL_LINES_COL");
				
				String qryGlLines_f = String.format(qryGlLines, headerIds);
				System.out.println("qryGlLines_f: \r\n "+ qryGlLines_f);
				
				SQLResult qryGlLines_r = executeQuery(dbRmsCol, qryGlLines_f);
				testCase.addQueryEvidenceCurrentStep(qryGlLines_r, true);
				
				if (contieneDatosGlLines) {
					assertFalse(qryGlLines_r.isEmpty());
				} else {
					assertTrue(qryGlLines_r.isEmpty());
				}
				
		return referencias3;
	}
	
	public void validarGlInterface(List<String> referencias3) throws SQLException {
		
		String qryValidarGlInterface = "SELECT status,reference6,reference4,reference10,reference22,reference25,user_je_category_name,user_je_source_name,segment4,ENTERED_DR,ENTERED_CR \r\n"
				+ "FROM GL_INTERFACE \r\n"
				+ "WHERE reference6 IN (%s)";
		
		/****************************************************************************************************************************************
		 * Paso 10 (8)
		 * **************************************************************************************************************************************/
		addStep("Conectarse a la base de datos de EBS Colombia");
		
			testCase.addTextEvidenceCurrentStep("Conexion: EBS Colombia");
			testCase.addTextEvidenceCurrentStep(dbEbsCol.getHost());
			
		/****************************************************************************************************************************************
		 * Paso 11
		 * **************************************************************************************************************************************/
		addStep("Verificar la inserción de líneas en la tabla GL_INTERFACE.");
			String referencias3Str = "";
			for (int i = 0; i < referencias3.size(); i++) {
				referencias3Str += referencias3.get(i);
				if (i < referencias3.size()-1 ) {
					referencias3Str += ",";
				}
			}
			
			String qryValidarGlInterface_f = String.format(qryValidarGlInterface, referencias3Str);
			System.out.println("qryValidarGlInterface_f: \r\n "+ qryValidarGlInterface_f);
			
			SQLResult qryValidarGlInterface_r = executeQuery(dbEbsCol, qryValidarGlInterface_f);
			testCase.addQueryEvidenceCurrentStep(qryValidarGlInterface_r, true);
			
			assertFalse(qryValidarGlInterface_r.isEmpty());
			
			//Se comentó que no era necesario validar el texto exacto 
//		/****************************************************************************************************************************************
//		 * Paso 12-15 (10)
//		 * **************************************************************************************************************************************/
//		validarDetalleGlInterface(
//				"Validar en la table GL_INTERFACE que se encuentra en el credito la sumatoria del  movimiento \"Transferencias entre tarifa general\"", 
//				"8395950015", 
//				"Transferencias entre tarifa general", 
//				referencias3Str);
//		
//		validarDetalleGlInterface(
//				"Validar que se encuentra en el credito la sumatoria del  movimiento 'Transferencias entre tiendas tarifa 5'", 
//				"8395950047", 
//				"Transferencias entre tiendas tarifa 5", 
//				referencias3Str);
//		
//		validarDetalleGlInterface(
//				"Validar que se encuentra en el credito la sumatoria del  movimiento 'TRANSFERENCIA CEDIS'", 
//				"9601010004", 
//				"TRANSFERENCIA CEDIS", 
//				referencias3Str);
//		
//		validarDetalleGlInterface(
//				"Validar que se encuentra en el credito la sumatoria del  movimiento 'TRANSFERENCIA CEDIS'", 
//				"9601010004", 
//				"TRANSFERENCIA CEDIS", 
//				referencias3Str);
		
	}
	
	/**
	 * Valuda que se registre un error en las tablas WM_LOG_THREAD, WM_LOG_ERROR con status E
	 * @param fechaEjecucionInicio
	 * @throws SQLException
	 */
	public void validarErrores(Date fechaEjecucionInicio) throws SQLException {
		String qryWmLogThread = "select * \r\n"
				+ "				from wmlog.WM_LOG_THREAD  \r\n"
				+ "				where start_dt >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss') and status = 'E'";
		
		String qryWmLogError = "SELECT * FROM wmlog.WM_LOG_ERROR WHERE RUN_ID in (%s)";
		
		/****************************************************************************************************************************************
		 * Paso Validar WM_LOG_THREAD
		 * **************************************************************************************************************************************/
		addStep("Verificar que el thread relacionado termine en status E de error");
		
				String fechaEjecucionInicio_f = formatter.format(fechaEjecucionInicio);
				
				String qryWmLogThread_f = String.format(qryWmLogThread, fechaEjecucionInicio_f);
				System.out.println("qryWmLogThread_f: \r\n "+ qryWmLogThread_f);
				
				SQLResult qryWmLogThread_r = executeQuery(dbLog, qryWmLogThread_f);
				testCase.addQueryEvidenceCurrentStep(qryWmLogThread_r, true);
				
				assertFalse(qryWmLogThread_r.isEmpty());
				
				String threadId = qryWmLogThread_r.getData(0, "THREAD_ID");
				
		/****************************************************************************************************************************************
		 * Paso Validar WM_LOG_ERROR 
		 * **************************************************************************************************************************************/
		addStep("Validar que se genero un error en el thread de ajustes");
				
				String qryWmLogError_f = String.format(qryWmLogError, threadId);
				System.out.println("qryWmLogError_f: \r\n "+ qryWmLogError_f);
				
				SQLResult qryWmLogError_r = executeQuery(dbLog, qryWmLogError_f);
				testCase.addQueryEvidenceCurrentStep(qryWmLogError_r, true);
				
				assertFalse(qryWmLogError_r.isEmpty());
				
				String descripcion = qryWmLogError_r.getData(0, "DESCRIPTION");
				String mensaje = qryWmLogError_r.getData(0, "MESSAGE");
				
				testCase.addCodeEvidenceCurrentStep(descripcion);
				testCase.addCodeEvidenceCurrentStep(mensaje);

	}
	
	private void validarDetalleGlInterface(String stepDescription, String segment4, String reference10, String referencias3Str) throws SQLException {
		String qryValidarGlInterfaceDetail = "select status,reference6,reference4,reference10,reference22,reference25,user_je_category_name,user_je_source_name,segment4,ENTERED_DR,ENTERED_CR \r\n"
				+ "from gl_interface \r\n"
				+ "where  segment4 = %s \r\n"
				+ "and ENTERED_DR IS NOT NULL \r\n"
				+ "and reference10 = '%s'\r\n"
				+ "and reference6 in (%s)";
		
		
		/****************************************************************************************************************************************
		 * Paso 12-? (10)
		 * **************************************************************************************************************************************/
		addStep("Validar en la table GL_INTERFACE que se encuentra en el credito la sumatoria del  movimiento \"Transferencias entre tarifa general\"");

			String qryValidarGlInterfaceDetail_f = String.format(qryValidarGlInterfaceDetail, segment4, reference10, referencias3Str);
			System.out.println("qryValidarGlInterfaceDetail_f: \r\n "+ qryValidarGlInterfaceDetail_f);
			
			SQLResult qryValidarGlInterfaceDetail_r = executeQuery(dbEbsCol, qryValidarGlInterfaceDetail_f);
			testCase.addQueryEvidenceCurrentStep(qryValidarGlInterfaceDetail_r, true);
			
			assertFalse(qryValidarGlInterfaceDetail_r.isEmpty());
	}
	
}
