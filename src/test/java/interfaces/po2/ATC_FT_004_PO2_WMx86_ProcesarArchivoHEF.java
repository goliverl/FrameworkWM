package interfaces.po2;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.json.JSONArray;
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
 * 004-FT-BACK OFFICE AVANTE: MTC_FT_001-3 PO2 Procesar archivo HEF de hoja de entrega final a través de la interface FEMSA_PO2 (EBS 12.2.4 CHO_52074283)
 * Desc:
 * Prueba de regresión para comprobar la no afectación en la funcionalidad principal de la interface FEMSA_PO2 de avante 
 * para procesar archivos HEF (Hoja de Entrega Final) de subida (de WM INBOUND a EBS), al ser migrada la interface de WM9.9 a WM10.5 
 * Hoja de Entrega Final La interface se encarga de transferir la información relativa a las formas de pago de la tiendas de México (POS) 
 * hacia la instancia Oracle GL de México. Origen: POS, Documentos HEF (Hoja de entrega final) procesados por la interface PR50 Tabla: 
 * POS_HEF_DETL POS_INBOUND_DOCS Destino: Oracle GL, Pólizas insertadas en la tabla Open interface de Oracle GL instancia Colombia. 
 * tablas: GL_INTERFACE.
 * NOTA: EBS 12.2.4
 * @author Roberto Flores
 * @date   2022/06/24
 */
public class ATC_FT_004_PO2_WMx86_ProcesarArchivoHEF extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_PO2_WMx86_ProcArchivoHEF_test(HashMap<String, String> data) throws Exception {
		
		testCase.setPrerequisites("*Contar con un punto de venta de XPOS que se pueda comunicar con la PR50V2 de WM10.5 a través de la DS50 y el TrendingNetwork para generar y enviar el archivo HEF.\r\n"
				+ "*Contar con acceso a las bases de datos de FCWM6QA, FCWMLQA y  AVEBQA (EBS) de avante.\r\n"
				+ "*Contar con el nombre y grupo del nuevo Job de Control M para la ejecución de la interface PO2.\r\n"
				+ "*Contar con acceso a repositorio de buzon de la tienda.\r\n"
				+ "*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA de Back Office. \r\n"
				+ "*Haber ejecutado el caso de prueba PR50V2_IN_HEF de Recepción de documentos. \r\n"
				+ "");
		
		/*
		 * Utilerías
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		SQLUtil FCWM6QA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil AVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil FCWMLQA_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, GlobalVariables.DB_USER_FCWMLQA_WMLOG,GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		
		
		
		/*
		 * Querys
		 *********************************************************************/
		String qryBoEncendido = "SELECT T.*,T.ROWID \r\n"
				+ "FROM wmuser.WM_FW_BO_ENCENDIDO T \r\n"
				+ "WHERE T.APPLICATION='PO2' \r\n"
				+ "AND cr_plaza = '*'\r\n"
				+ "AND cr_tienda = '*'\r\n"
				+ "AND status = 'A'\r\n"
				+ "AND application = 'PO2'";
		
		String qryRegistrosPosInbound = "SELECT   * FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ "WHERE STATUS='I' \r\n"
				+ "AND DOC_TYPE='HEF' \r\n"
				+ "ORDER BY PARTITION_DATE DESC";
		
		String qryRegistrosPosHef = "SELECT PH.* \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS PI, POSUSER.POS_HEF PH \r\n"
				+ "WHERE PI.ID = PH.PID_ID AND PI.STATUS='I' \r\n"
				+ "AND PI.DOC_TYPE='HEF'";
		
		String qryaRegistrosHefDetl = "SELECT * \r\n"
				+ "FROM POSUSER.POS_HEF_DETL \r\n"
				+ "WHERE PID_ID in (%s)";
		
		String qryActualizacionInboundDocs = "SELECT * FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ "WHERE DOC_TYPE='HEF' \r\n"
				+ "AND status = 'E'\r\n"
				+ "AND ID in (%s)";
		
		String qryGlInterface = "SELECT * FROM GL.GL_INTERFACE \r\n"
				+ "WHERE TRUNC(DATE_CREATED) = TRUNC(SYSDATE) \r\n"
				+ "AND SEGMENT3 = '"+data.get("CR_PLAZA")+"' \r\n"
				+ "AND STATUS = 'NEW'\r\n"
				+ "AND REFERENCE6 = %s";
		
		String qryWmLogRun = "select * \r\n"
				+ "from wm_log_run \r\n"
				+ "where interface LIKE '%s' AND status = 'S' \r\n"
				+ "AND start_dt >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n"
				+ "ORDER BY RUN_ID DESC";
		
		String qryThreads = "select * from WM_LOG_THREAD where PARENT_ID = %s and status = 'S'";
		
		Date fechaEjecucionInicio;

		testCase.setProject_Name("POC WMx86");
		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		
		/****************************************************************************************************************************************
		 * Paso 1
		 * **************************************************************************************************************************************/
		addStep("Establecer la conexión con la BD **FCWM6QA**.");
				
				testCase.addTextEvidenceCurrentStep("Conexion: FCWM6QA");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		
		
		/****************************************************************************************************************************************
		 * Paso 2
		 * **************************************************************************************************************************************/
		addStep("Validar la configuración de la bandera de CUTOVER_FLAG este en estatus S encendida y la fecha del campo CUTOVER_DATE sea mayor o igual al de la fecha en que se realizó la prueba, en BD FCWM6QA");
				
				System.out.println("qryBoEncendido: \r\n "+ qryBoEncendido);
				SQLResult qryBoEncendido_r = executeQuery(FCWM6QA, qryBoEncendido);
				testCase.addQueryEvidenceCurrentStep(qryBoEncendido_r, true);
				
				assertFalse(qryBoEncendido_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 3
		 * **************************************************************************************************************************************/
		addStep("Consultar que existan registros en las tablas POS_INBOUND_DOCS de la BD FCWM6QA con DOC_TYPE igual a 'HEF' con STATUS igual a 'I'");
				
				System.out.println("qryRegistrosPosInbound: \r\n "+ qryRegistrosPosInbound);
				
				SQLResult qryRegistrosPosInbound_r = executeQuery(FCWM6QA, qryRegistrosPosInbound);
				testCase.addQueryEvidenceCurrentStep(qryRegistrosPosInbound_r, true);
				
				assertFalse(qryRegistrosPosInbound_r.isEmpty());
				
				
		/****************************************************************************************************************************************
		 * Paso 4
		 * **************************************************************************************************************************************/
		addStep("Consultar que existan registros en las tablas POSUSER.POS_HEF de la BD FCWM6QA con de los archivos ‘HEF’ que están en la tabla POS_INBOUND_DOCS listos para procesar");
				
				System.out.println("qryRegistrosPosHef: \r\n "+ qryRegistrosPosHef);
				
				SQLResult qryRegistrosPosHef_r = executeQuery(FCWM6QA, qryRegistrosPosHef);
				testCase.addQueryEvidenceCurrentStep(qryRegistrosPosHef_r, true);
				
				assertFalse(qryRegistrosPosHef_r.isEmpty());
				
				String pidIds = "";
				for (int i = 0; i < qryRegistrosPosHef_r.getRowCount(); i++) {
					pidIds += qryRegistrosPosHef_r.getData(i, 0);
					if (i < qryRegistrosPosHef_r.getRowCount() - 1) {
						pidIds += ",";
					}
				}
		
		/****************************************************************************************************************************************
		 * Paso 5
		 * **************************************************************************************************************************************/
		addStep("Consultar que existan registros en las tablas POSUSER.POS_HEF_DET_ de la BD FCWM6QA con el detalle de la información contenida en los archivos");
				
				String qryaRegistrosHefDetl_f = String.format(qryaRegistrosHefDetl, pidIds);
				System.out.println("qryaRegistrosHefDetl_f: \r\n "+ qryaRegistrosHefDetl_f);
				
//				SQLResult qryaRegistrosHefDetl_r = executeQuery(FCWM6QA, qryaRegistrosHefDetl_f);
//				testCase.addQueryEvidenceCurrentStep(qryaRegistrosHefDetl_r, true);
				
				SQLResultExcel qryaRegistrosHefDetl_r = executeQueryExcel(FCWM6QA, qryaRegistrosHefDetl_f);
				testCase.addDocumentEvidence(qryaRegistrosHefDetl_r.getRelativePath(), "consulta registros pos_hef_det");
				
				assertFalse(qryaRegistrosHefDetl_r.isEmpty());
				
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/****************************************************************************************************************************************
		 * Paso 6
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
		 * Paso 7
		 * **************************************************************************************************************************************/
		addStep("Abrir la herramienta de control M para validar que la ejecución del job haya sido exitosa");
				
				testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
				System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);
				
				assertEquals(resultadoEjecucion, "Ended OK");
				u.close();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		/****************************************************************************************************************************************
		 * Paso 8
		 * **************************************************************************************************************************************/
		addStep("Comprobar que los documentos fueron actualizados correctamente en la tabla POS_INBOUND_DOCS en la BD FCWM6QA  donde WMSTATUS igual a 'E'.");
				
				System.out.println("qryActualizacionInboundDocs: \r\n "+ qryActualizacionInboundDocs);
				
				SQLResult qryActualizacionInboundDocs_r = executeQuery(FCWM6QA, qryActualizacionInboundDocs);
				testCase.addQueryEvidenceCurrentStep(qryActualizacionInboundDocs_r, true);
				
				assertFalse(qryActualizacionInboundDocs_r.isEmpty());
				
				String targetIds = "";
				for (int i = 0; i < qryActualizacionInboundDocs_r.getRowCount(); i++) {
					targetIds += qryActualizacionInboundDocs_r.getData(i, "TARGET_ID");
					if (i < qryActualizacionInboundDocs_r.getRowCount() - 1) {
						targetIds += ",";
					}
				}
				
				
		/****************************************************************************************************************************************
		 * Paso 9
		 * **************************************************************************************************************************************/
		addStep("Realizar conexión a la BD AVEBQA de EBS.");
				
				testCase.addTextEvidenceCurrentStep("Conexion: AVEBQA");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_AVEBQA);
				
				
		/****************************************************************************************************************************************
		 * Paso 10
		 * **************************************************************************************************************************************/
		addStep("Comprobar que los registros se insertaron correctamente en la Tabla GL_INTERFACE en la BD AVEBQA EBS AVANTE donde SEGMENT3  es igual a la plaza");
				
				String qryGlInterface_f = String.format(qryGlInterface, targetIds);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				SQLResult qryGlInterface_r = executeQuery(AVEBQA, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());
		
		
		/****************************************************************************************************************************************
		 * Paso 11
		 * **************************************************************************************************************************************/
		addStep("Realizar conexión a la BD FCWMLTAQ.FEMCOM.NET del host oxfwm6q00.femcom.net.");
				
		testCase.addTextEvidenceCurrentStep("Conexion: FCWMLQA_WMLOG");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
				
		
		/****************************************************************************************************************************************
		 * Paso 12
		 * **************************************************************************************************************************************/
		addStep("Validar la correcta ejecución de la interface PO2 en la tabla WM_LOG_RUN de BD FCWMLQA.");
				
				String fechaEjecucionInicio_f = formatter.format(fechaEjecucionInicio);
		
				String qryWmLogRun_f = String.format(qryWmLogRun, "%PO2", fechaEjecucionInicio_f);
				System.out.println("qryWmLogRun_f: \r\n "+ qryWmLogRun_f);
				
				SQLResult qryWmLogRun_r = executeQuery(FCWMLQA_WMLOG, qryWmLogRun_f);
				testCase.addQueryEvidenceCurrentStep(qryWmLogRun_r, true);
				
				assertFalse(qryWmLogRun_r.isEmpty());
				
				String runId = qryWmLogRun_r.getData(0, "RUN_ID");
				
		/****************************************************************************************************************************************
		 * Paso 13
		 * **************************************************************************************************************************************/
		addStep("Validar la correcta ejecución de los Threads lanzados por la interface PO2 en la tabla WM_LOG_THREAD  de BD FCWMLQA");
				
				String qryThreads_f = String.format(qryThreads, runId);
				System.out.println("qryThreads_f: \r\n "+ qryThreads_f);
				
				SQLResult qryThreads_r = executeQuery(FCWMLQA_WMLOG, qryThreads_f);
				testCase.addQueryEvidenceCurrentStep(qryThreads_r, true);
				
				assertFalse(qryThreads_r.isEmpty());
	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_PO2_004_WMx86_PO2_ProcesarArchivoHEF";
	}

	@Override
	public String setTestDescription() {
		return "MTC_FT_001-3 PO2 Procesar archivo HEF de hoja de entrega final a través de la interface FEMSA_PO2 (EBS 12.2.4 CHO_52074283)";
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
