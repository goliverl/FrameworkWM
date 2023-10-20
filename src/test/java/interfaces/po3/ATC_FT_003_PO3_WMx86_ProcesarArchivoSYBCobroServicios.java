package interfaces.po3;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
 * 004-FT-BACK OFFICE AVANTE: MTC_FT_002 PO3 Procesar archivo SYB de cobro de servicios a través de la interface FEMSA_PO3
 * Desc:
 * Prueba de regresión para comprobar la no afectación en la funcionalidad principal de la interface FEMSA_PO3 de avante para procesar archivos SYB (Cobro de Servicios)
 * de subida (de WM INBOUND a EBS), al ser migrada la interface de WM9.9 a WM10.5.
 * Interface que transfiere información relacionada a los Pagos de Servicios en POS (Telmex, Radio BIP, Gas, Alestra, Encante, etc.) 
 * y el uso de vales de cerveza de POS Systems a Oracle Applications Database. 
 * Origen: POSUSER 
 * Tablas: POS_INBOUND_DOCS POS_SYB POS_SYB_DETL
 * Destino: ORAFIN 
 * Tablas: XXFC_PAGO_SERVICIOS XXFC_PAGO_SERVICIOS_WORK
 * @author Roberto Flores
 * @date   2022/06/28
 */
public class ATC_FT_003_PO3_WMx86_ProcesarArchivoSYBCobroServicios extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PO3_WMx86_ProcesarArchivoSYB_test(HashMap<String, String> data) throws Exception {
		
		testCase.setPrerequisites("*Contar con un punto de venta de XPOS que se pueda comunicar con la PR50V2 de WM10.5 a travez de la DS50 y el TrendingNetwork para generar y enviar el archivo SYB.\r\n"
				+ "*Contar con acceso a las bases de datos de FCWM6QA, FCWMLQA y AVEBQA (EBS) de Avante.\r\n"
				+ "*Contar con el nombre y grupo del nuevo Job de Control M para la ejecución de la interface PO3.\r\n"
				+ "*Contar con acceso a repositorio de buzon de la tienda.\r\n"
				+ "*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA de Back Office. *Haber ejecutado el caso de prueba de PR50V2_IN_HEF de Recepción de documentos.\r\n"
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
		String qryDocumentosPendientesSYB = "SELECT * \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ "WHERE STATUS='I' \r\n"
				+ "AND DOC_TYPE='SYB'\r\n"
				+ "ORDER BY PARTITION_DATE DESC";
		
		String qryRegistrosPosSyb = "SELECT * FROM POSUSER.POS_SYB WHERE PID_ID in (%s)";
		
		String qryRegistrosPosSybDetl = "SELECT * FROM POSUSER.POS_SYB_DETL WHERE PID_ID in (%s)";
		
		String qryActualizacionSyb = "SELECT * \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS\r\n"
				+ "WHERE DOC_TYPE = 'SYB' \r\n"
				+ "AND STATUS = 'E' \r\n"
				+ "AND ID = %s";
		
		String qryServicios = "SELECT distinct(servicio) FROM POSUSER.POS_SYB_DETL WHERE PID_ID in (%s)";
		
		String qryPagoServicios = "SELECT * \r\n"
				+ "FROM XXFC.XXFC_PAGO_SERVICIO\r\n"
				+ "WHERE PLAZA = "+data.get("CR_PLAZA")+" \r\n"
				+ "AND SERVICIO in %s \r\n"
				+ "AND TRUNC(FECHA_RECEPCION) = TRUNC(SYSDATE)";
		
		String qryPagoServiciosWork = "SELECT * \r\n"
				+ "FROM XXFC.XXFC_PAGO_SERVICIOS_WORK \r\n"
				+ "WHERE PLAZA = "+data.get("CR_PLAZA")+" \r\n"
				+ "AND FECHA_RECEPCION = TO_CHAR(SYSDATE,'DD-MON-YY')";
		
		String qryWmLogRun = "select * \r\n"
				+ "from wm_log_run \r\n"
				+ "where interface LIKE '%s' AND status = 'S' \r\n"
				+ "AND start_dt >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n"
				+ "ORDER BY RUN_ID DESC";
		
		String qryThreads = "select * from WM_LOG_THREAD where PARENT_ID = %s and status = 'S'";
		
		String qryError = "SELECT * FROM WM_LOG_ERROR WHERE RUN_ID= %s";
		
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
		addStep("Validar que exista información de documentos SYB pendientes por procesar en la tabla POS_INBOUND_DOCS de BD FCWM6QA, con STATUS = 'I' para la plaza especifica.");
				
				System.out.println("qryDocumentosPendientesSYB: \r\n "+ qryDocumentosPendientesSYB);
				SQLResult qryDocumentosPendientesSYB_r = executeQuery(FCWM6QA, qryDocumentosPendientesSYB);
				testCase.addQueryEvidenceCurrentStep(qryDocumentosPendientesSYB_r, true);
				
				assertFalse(qryDocumentosPendientesSYB_r.isEmpty());
				
				String idPosInboundDocs = "";
				for (int i = 0; i < qryDocumentosPendientesSYB_r.getRowCount(); i++) {
					idPosInboundDocs += qryDocumentosPendientesSYB_r.getData(i, "ID");
					if (i < qryDocumentosPendientesSYB_r.getRowCount() - 1) {
						idPosInboundDocs += ",";
					}
				}
						
				
		/****************************************************************************************************************************************
		 * Paso 3
		 * **************************************************************************************************************************************/
		addStep("Validar que contenga registros los archivos SYB en la POSUSER.POS_SYB en la BD FCWM6QA");
				
				String qryRegistrosPosSYB_f = String.format(qryRegistrosPosSyb, idPosInboundDocs);
				System.out.println("qryRegistrosPosSYB_f: \r\n "+ qryRegistrosPosSYB_f);
				
				SQLResult qryRegistrosPosSYB_r = executeQuery(FCWM6QA, qryRegistrosPosSYB_f);
				testCase.addQueryEvidenceCurrentStep(qryRegistrosPosSYB_r, true);
				
				assertFalse(qryRegistrosPosSYB_r.isEmpty());
				
		
		/****************************************************************************************************************************************
		 * Paso 4
		 * **************************************************************************************************************************************/
		addStep("Validar el detalle de registros los archivos SYB en la POSUSER.POS_SYB_DETL en la BD FCWM6QA.");
				
				String qryRegistrosPosSybDetl_f = String.format(qryRegistrosPosSybDetl, idPosInboundDocs);
				System.out.println("qryRegistrosPosSybDetl_f: \r\n "+ qryRegistrosPosSybDetl_f);
				
				
				SQLResultExcel qryRegistrosPosSybDetl_r = executeQueryExcel(FCWM6QA, qryRegistrosPosSybDetl_f);
				testCase.addDocumentEvidence(qryRegistrosPosSybDetl_r.getRelativePath(), "consulta registros POS_SYB_DETL");
				
				assertFalse(qryRegistrosPosSybDetl_r.isEmpty());
				
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/****************************************************************************************************************************************
		 * Paso 5
		 * **************************************************************************************************************************************/
		addStep("Ejecución P03 control-M");
		
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
		 * Paso 6
		 * **************************************************************************************************************************************/
		addStep("Abrir la herramienta de control M para validar que la ejecución del job haya sido exitosa");
				
				testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
				System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);
				
				assertEquals(resultadoEjecucion, "Ended OK");
				u.close();
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		/****************************************************************************************************************************************
		 * Paso 7
		 * **************************************************************************************************************************************/
		addStep("Validar la actualización del estatus de los documentos SYB procesados");
				
				String qryActualizacionSyb_f = String.format(qryActualizacionSyb, idPosInboundDocs);
				System.out.println("qryActualizacionSyb_f: \r\n "+ qryActualizacionSyb_f);
				
				SQLResult qryActualizacionSyb_r = executeQuery(FCWM6QA, qryActualizacionSyb_f);
				testCase.addQueryEvidenceCurrentStep(qryActualizacionSyb_r, true);
				
				//assertFalse(qryActualizacionSyb_r.isEmpty());
				
//				String targetIds = "";
//				for (int i = 0; i < qryActualizacionSyb_r.getRowCount(); i++) {
//					targetIds += qryActualizacionSyb_r.getData(i, "TARGET_ID");
//					if (i < qryActualizacionSyb_r.getRowCount() - 1) {
//						targetIds += ",";
//					}
//				}
				
				
		/****************************************************************************************************************************************
		 * Paso 8
		 * **************************************************************************************************************************************/
		addStep("Realizar conexión a la BD AVEBQA de EBS.");
				
				testCase.addTextEvidenceCurrentStep("Conexion: AVEBQA");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_AVEBQA);
				
				
		/****************************************************************************************************************************************
		 * Paso 9
		 * **************************************************************************************************************************************/
		addStep("Validar la inserción de los pagos válidos de la plaza en la tabla XXFC.XXFC_PAGO_SERVICIOS de BD AVEBQA.");
				
				String qryServicios_f = String.format(qryServicios, idPosInboundDocs);
				System.out.println("qryServicios_f: \r\n "+ qryServicios_f);
				
				SQLResult qryServicios_r = executeQuery(AVEBQA, qryServicios_f);
				
				String servicios = "";
				for (int i = 0; i < qryServicios_r.getRowCount(); i++) {
					servicios += qryServicios_r.getData(i, "servicio");
					if (i < qryServicios_r.getRowCount() - 1) {
						servicios += ",";
					}
				}
		
				String qryPagoServicios_f = String.format(qryPagoServicios, servicios);
				System.out.println("qryPagoServicios_f: \r\n "+ qryPagoServicios_f);
				
				SQLResult qryPagoServicios_r = executeQuery(AVEBQA, qryPagoServicios_f);
				testCase.addQueryEvidenceCurrentStep(qryPagoServicios_r, true);
				
				assertFalse(qryPagoServicios_r.isEmpty());
				
				
		/****************************************************************************************************************************************
		 * Paso 10
		 * **************************************************************************************************************************************/
		addStep("Validar la inserción de los pagos inválidos de la plaza");
				
				System.out.println("qryPagoServiciosWork_f: \r\n "+ qryPagoServiciosWork);
				
				SQLResult qryPagoServiciosWork_r = executeQuery(AVEBQA, qryPagoServiciosWork);
				testCase.addQueryEvidenceCurrentStep(qryPagoServiciosWork_r, true);
				
				assertFalse(qryPagoServiciosWork_r.isEmpty());
				

		
		/****************************************************************************************************************************************
		 * Paso 11
		 * **************************************************************************************************************************************/
		addStep("Realizar conexión a la BD FCWMLTAQ.FEMCOM.NET del host oxfwm6q00.femcom.net.");
				
		testCase.addTextEvidenceCurrentStep("Conexion: FCWMLQA_WMLOG");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
				
		
		/****************************************************************************************************************************************
		 * Paso 12
		 * **************************************************************************************************************************************/
		addStep("Validar la correcta ejecución de la interface PO3 en la tabla WM WM_LOG_RUN de BD FCWMLQA");
				
				String fechaEjecucionInicio_f = formatter.format(fechaEjecucionInicio);
		
				String qryWmLogRun_f = String.format(qryWmLogRun, "%PO3%", fechaEjecucionInicio_f);
				System.out.println("qryWmLogRun_f: \r\n "+ qryWmLogRun_f);
				
				SQLResult qryWmLogRun_r = executeQuery(FCWMLQA_WMLOG, qryWmLogRun_f);
				testCase.addQueryEvidenceCurrentStep(qryWmLogRun_r, true);
				
				assertFalse(qryWmLogRun_r.isEmpty());
				
				String runId = qryWmLogRun_r.getData(0, "RUN_ID");
				
				
		/****************************************************************************************************************************************
		 * Paso 13
		 * **************************************************************************************************************************************/
		addStep("Validar la correcta ejecución de los Threads lanzados por la interface PO3 en la tabla WM_LOG_THREAD de  BD FCWMLQA");
				
				String qryThreads_f = String.format(qryThreads, runId);
				System.out.println("qryThreads_f: \r\n "+ qryThreads_f);
				
				SQLResult qryThreads_r = executeQuery(FCWMLQA_WMLOG, qryThreads_f);
				testCase.addQueryEvidenceCurrentStep(qryThreads_r, true);
				
				assertFalse(qryThreads_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 14
		 * **************************************************************************************************************************************/
		addStep("Realizar la siguiente consulta para verificar que no se encuentre ningún error presente en la ejecución de la interfaz PO3");
				
				String qryError_f = String.format(qryError, runId);
				System.out.println("qryError_f: \r\n "+ qryError);
				
				SQLResult qryError_r = executeQuery(FCWMLQA_WMLOG, qryError_f);
				testCase.addQueryEvidenceCurrentStep(qryError_r, true);
				
				assertTrue(qryError_r.isEmpty());
	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_003_PO3_WMx86_ProcesarArchivoSYBCobroServicios";
	}

	@Override
	public String setTestDescription() {
		return "MTC_FT_002 PO3 Procesar archivo SYB de cobro de servicios a través de la interface FEMSA_PO3";
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
