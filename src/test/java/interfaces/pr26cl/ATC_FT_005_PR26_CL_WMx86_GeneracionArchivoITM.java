package interfaces.pr26cl;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.json.JSONArray;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLResultExcel;
import utils.sql.SQLUtil;


/**
 * RDyBO INTERNACIONAL: MTC-FT-010-1-C1 PR26 Generacion de archivo ITM de Item Master a traves de la interface FEMSA_PR26_CL
 * Desc:
 * Prueba de regresión para comprobar la no afectacion en la funcionalidad principal de la interface FEMSA_PR26_CL 
 * para generar archivos ITM (Item Master) de bajada (de RMS a WM OUTBOUND) con la información actualizada o nueva de articulos, 
 * al ser migrada la interface de WM9.9 a WM10.5
 * @author Roberto Flores
 * @date   2022/06/30
 */
public class ATC_FT_005_PR26_CL_WMx86_GeneracionArchivoITM extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_PR26_CL_WMx86_GeneracionArchivoITM_test(HashMap<String, String> data) throws Exception {
		
		testCase.setPrerequisites("*Contar con acceso a las bases de datos de FCMWQA_Chile, FCWMLQA_Chile y de RMS_CHILE.\r\n"
				+ "*Tener acceso al FTP para el buzón de las tiendas-plazas.\r\n"
				+ "*Contar con Control-M o disponibilidad de operadores para la ejecución del Job PR26.\r\n"
				+ "*Contar con el nombre y grupo del nuevo Job de Control M para PR26_CL de WM10.\r\n"
				+ "*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA de Back Office.\r\n"
				+ "*Contar con acceso a la aplicación FileZilla: usuario y contraseña para ingresar al buzón de la tienda.\r\n"
				+ "*Contar con la configuración de DS50 de la siguiente manera con la IP del servidor FCWMQA8B (10.80.8.181)  de OXXO INTERNACIONAL  con WM 10.5.\r\n"
				+ "");
		
		/*
		 * Utilerías
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		SQLUtil RmsChile = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERChile, GlobalVariables.DB_USER_RMSWMUSERChile,GlobalVariables.DB_PASSWORD_RMSWMUSERChile);
		SQLUtil WmLogChile = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile,GlobalVariables.DB_PASSWORD_LogChile);
		SQLUtil OCHWMQA = new SQLUtil(GlobalVariables.DB_HOST_PosUserChile, GlobalVariables.DB_USER_PosUserChile,GlobalVariables.DB_PASSWORD_PosUserChile);
		
		
		
		/*
		 * Querys
		 *********************************************************************/
		String qryRegistrosProcesar = "SELECT DISTINCT b.LOCATION, b.BATCH_ID\r\n"
				+ "FROM WMUSER.FEM_POS_MODS_STG a, WMUSER.POS_ITM_PRM_HEAD b\r\n"
				+ "WHERE a.LOAD_BATCH_ID = b.LOAD_BATCH_ID\r\n"
				+ "AND a.LOAD_WEEK = b.LOAD_WEEK\r\n"
				+ "AND a.STORE = b.LOCATION\r\n"
				+ "AND b.WM_ITM_STATUS = 'L'\r\n"
				+ "AND a.TRAN_TYPE NOT IN (31, 32)"
				+ "";
		String qryWmLogRun = "select * \r\n"
				+ "from wm_log_run \r\n"
				+ "where interface LIKE '%s' AND status = 'S' \r\n"
				+ "AND start_dt >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n"
				+ "ORDER BY RUN_ID DESC";
		
		String qryThreads = "select * from WM_LOG_THREAD where PARENT_ID = %s and status = 'S'";
		
		String qryError = "SELECT * FROM WM_LOG_ERROR WHERE RUN_ID= %s";
		
		String qryOutboundDocs = "SELECT * FROM POSUSER.POS_OUTBOUND_DOCS\r\n"
				+ "WHERE DOC_TYPE = 'ITM'\r\n"
				+ "AND PV_CR_PLAZA = '"+data.get("CR_PLAZA")+"'\r\n"
				+ "AND PV_CR_TIENDA = '"+data.get("CR_TIENDA")+"'\r\n"
				+ "AND source_id in (%s)\r\n"
				+ "AND TRUNC(SENT_DATE) = TRUNC(SYSDATE)";
		
		String qryPosItmPrmHead = "SELECT * FROM WMUSER.POS_ITM_PRM_HEAD\r\n"
				+ "WHERE LOCATION IN (%s)\r\n"
				+ "AND WM_ITM_STATUS = 'E'\r\n"
				+ "AND WM_TARGET_ITM = '%s'\r\n"
				+ "";
		
		Date fechaEjecucionInicio;

		testCase.setProject_Name("POC WMx86");
		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		
		/****************************************************************************************************************************************
		 * Paso 1
		 * **************************************************************************************************************************************/
		addStep("Ingresar a la BD **RMS para Chile**.");
				
				testCase.addTextEvidenceCurrentStep("Conexion: RMS CHILE");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_RMSWMUSERChile);
		
		
		/****************************************************************************************************************************************
		 * Paso 2
		 * **************************************************************************************************************************************/
		addStep("Validar que existan archivos tipo FPE en status = 'I' en la POS_INBOUND_DOCS en la BD FCWM6QA.");
				
				System.out.println("qryRegistrosProcesar: \r\n "+ qryRegistrosProcesar);
				SQLResult qryRegistrosProcesar_r = executeQuery(RmsChile, qryRegistrosProcesar);
				testCase.addQueryEvidenceCurrentStep(qryRegistrosProcesar_r, true);
				
				assertFalse(qryRegistrosProcesar_r.isEmpty());
				
				String batchIds = "";
				for (int i = 0; i < qryRegistrosProcesar_r.getRowCount(); i++) {
					batchIds += qryRegistrosProcesar_r.getData(i, "BATCH_ID");
					if (i < qryRegistrosProcesar_r.getRowCount() - 1) {
						batchIds += ",";
					}
				}
				
				String locations = "";
				for (int i = 0; i < qryRegistrosProcesar_r.getRowCount(); i++) {
					locations += qryRegistrosProcesar_r.getData(i, "LOCATION");
					if (i < qryRegistrosProcesar_r.getRowCount() - 1) {
						locations += ",";
					}
				}
				

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/****************************************************************************************************************************************
		 * Paso 3
		 * **************************************************************************************************************************************/
		addStep("Ejecución control-M");
		
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
			
		
		/****************************************************************************************************************************************
		 * Paso 4
		 * **************************************************************************************************************************************/
		addStep("Abrir la herramienta de control M para validar que la ejecución del job haya sido exitosa");
				
				testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
				System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);
				
				assertEquals(resultadoEjecucion, "Ended OK");
				u.close();
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////				
		/****************************************************************************************************************************************
		 * Paso 5
		 * **************************************************************************************************************************************/
		addStep("Establecer conexión a la BD **FCWMLQA** (OCHWMQA.FEMCOM.NET).");
				
				testCase.addTextEvidenceCurrentStep("Conexion: AVEBQA");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_LogChile);
				
		/****************************************************************************************************************************************
		 * Paso 6
		 * **************************************************************************************************************************************/
		addStep("Validar que se inserte el detalle de la ejecución de la interface en la tabla WM_LOG_RUN de WMLOG");
				
				String fechaEjecucionInicio_f = formatter.format(fechaEjecucionInicio);
		
				String qryWmLogRun_f = String.format(qryWmLogRun, "%ITM%", fechaEjecucionInicio_f);
				System.out.println("qryWmLogRun_f: \r\n "+ qryWmLogRun_f);
				
				SQLResult qryWmLogRun_r = executeQuery(WmLogChile, qryWmLogRun_f);
				testCase.addQueryEvidenceCurrentStep(qryWmLogRun_r, true);
				
				assertFalse(qryWmLogRun_r.isEmpty());
				
				String runId = qryWmLogRun_r.getData(0, "RUN_ID");
				
		/****************************************************************************************************************************************
		 * Paso 7
		 * **************************************************************************************************************************************/
		addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD de WMLOG.");
				
				String qryThreads_f = String.format(qryThreads, runId);
				System.out.println("qryThreads_f: \r\n "+ qryThreads_f);
				
				SQLResult qryThreads_r = executeQuery(WmLogChile, qryThreads_f);
				testCase.addQueryEvidenceCurrentStep(qryThreads_r, true);
				
				assertFalse(qryThreads_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 8
		 * **************************************************************************************************************************************/
		addStep("Validar que no se hayan generado errores de la ejecución de la interface PR26_CL en la tabla WM_LOG_ERROR  de la BD **FCMWQA_Chile**");
				
				String qryError_f = String.format(qryError, runId);
				System.out.println("qryError_f: \r\n "+ qryError);
				
				SQLResult qryError_r = executeQuery(WmLogChile, qryError_f);
				testCase.addQueryEvidenceCurrentStep(qryError_r, true);
				
				assertTrue(qryError_r.isEmpty());
				

		/****************************************************************************************************************************************
		 * Paso 9
		 * **************************************************************************************************************************************/
		addStep("Establecer conexión a la BD **FCWMQA**");
				
		testCase.addTextEvidenceCurrentStep("Conexion: OCHWMQA.FEMCOM.NET");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_PosUserChile);
				
		
		/****************************************************************************************************************************************
		 * Paso 10
		 * **************************************************************************************************************************************/
		addStep("Validar que se inserte el registro del documento ITM generado por la interface en la tabla POS_OUTBOUND_DOCS de POSUSER");
		
				String qryOutboundDocs_f = String.format(qryOutboundDocs, batchIds);
				System.out.println("qryOutboundDocs_f: \r\n "+ qryOutboundDocs_f);
				
				SQLResult qryOutboundDocs_r = executeQuery(OCHWMQA, qryOutboundDocs_f);
				testCase.addQueryEvidenceCurrentStep(qryOutboundDocs_r, true);
				
				assertFalse(qryOutboundDocs_r.isEmpty());
				
				String docName = qryOutboundDocs_r.getData(0, "DOC_NAME");
				
		/****************************************************************************************************************************************
		 * Paso 11
		 * **************************************************************************************************************************************/
		addStep("Ingresar a la herramienta FileZilla mediante los siguientes datos ");
		
				testCase.addTextEvidenceCurrentStep("FTP_HOST: " + data.get("FTP_HOST") +
						"\nFTP_PORT: " + data.get("FTP_PORT") +
						"\nFTP_USER: " + data.get("FTP_USER"));
				
				FTPUtil ftp = new FTPUtil(
						data.get("FTP_HOST"),
						Integer.parseInt(data.get("FTP_PORT")),
						data.get("FTP_USER"),
						data.get("FTP_PASSWORD"));
		
				String path = "/u01/posuser/FEMSA_OXXO/POS/"+data.get("CR_PLAZA")+"/"+data.get("CR_TIENDA")+"/working" + "/" +docName;
		
				if (ftp.fileExists(path)) {
					System.out.println(path + " - Existe");
					testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: " + path);
				} else {
					System.out.println(path + " - No xiste");
					testCase.addTextEvidenceCurrentStep("Error - no se encontró archivo: " + path);
				}
				
				assertTrue(ftp.fileExists(path), "No se obtiene el archivo por FTP.");
				
				
		/****************************************************************************************************************************************
		 * Paso 12
		 * **************************************************************************************************************************************/
		addStep("Validar que se actualice el estatus y el nombre del archivo generado por la interface de los registros procesados");
		
				String qryPosItmPrmHead_f = String.format(qryPosItmPrmHead, locations, docName);
				System.out.println("qryPosItmPrmHead_f: \r\n "+ qryPosItmPrmHead_f);
				
				SQLResult qryPosItmPrmHead_r = executeQuery(RmsChile, qryPosItmPrmHead_f);
				testCase.addQueryEvidenceCurrentStep(qryPosItmPrmHead_r, true);
				
				assertFalse(qryPosItmPrmHead_r.isEmpty());
				

	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_005_PR26_CL_WMx86_GeneracionArchivoITM_test";
	}

	@Override
	public String setTestDescription() {
		return "MTC-FT-010-1-C1 PR26 Generación de archivo ITM de Item Master a traves de la interface FEMSA_PR26_CL";
	}

	@Override
	public String setTestDesigner() {
		return "Sergio Robles Ramos";
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
