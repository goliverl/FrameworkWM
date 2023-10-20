package interfaces.pr23;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.SimpleDateFormat;
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
import utils.sql.SQLUtil;
/**
 * Proyecto: "Actualizacion tecnologica Webmethods"
 * Interfaces BO MEX: MTC_FT_016 PR23 Procesar archivo REC de recibo de transferencia a traves de la interface FEMSA_PR23
 * Desc:
 * Prueba de regresion para comprobar la no afectacion en la funcionalidad principal de la interface FEMSA_PR23 para procesar archivos REC 
 * (Recibos de transferencias de entrada de Cedis o de tienda) de subida (WM INBOUND a RMS) 
 * con la informacion de los recibos de transferencias de entrada (pedidos de CEDIS a tienda o de tienda a tienda) , 
 * al ser migrada la interfaz de WM9.9 a WM10.5.
 * @author Roberto Flores
 * @date   2022/07/05
 */
public class ATC_FT_003_PR23_WMx86_ProcesarArchivoRECReciboTransferencia extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PR23_WMx86_ProcesarArchivoRECReciboTransferencia_test(HashMap<String, String> data) throws Exception {
		
		testCase.setPrerequisites("Contar con un punto de venta de XPOS que se pueda comunicar con la PR50V2 de WM10.5 a trav�s de la DS50 y el TrendingNetwork para generar y enviar el archivo REC.\r\n"
				+ "* Contar con acceso a las bases de datos de FCWM6QA y de RMS.\r\n"
				+ "* Contar con el nombre y grupo del nuevo Job de Control M para PR23 de WM10.\r\n"
				+ "Tener acceso al repositorio destino donde se almacena un archivo .dat en el servidor FTP_RMS: RTKRMS, Path: /u04/retek/rmsqa/batch/wmworking/REC.\r\n"
				+ "* Contar con acceso al repositorio de buzon de la tienda.\r\n"
				+ "* Contar con las credenciales de WM10.5 de los nuevos servidores del Integration Server de QA de Back Office.\r\n"
				+ "");
		
		/*
		 * Utilerias
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		SQLUtil FCWM6QA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil FCRMSQA = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX,GlobalVariables.DB_PASSWORD_RMS_MEX);
		SQLUtil FCWMLQA_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, GlobalVariables.DB_USER_FCWMLQA_WMLOG,GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		
		
		
		/*
		 * Querys
		 *********************************************************************/
		String qryInformacionPendiente = "SELECT ID, pv_cve_mvt, created_date, pv_ced_id, pv_cr_from_loc, tsf_no, ext_ref_no, order_no\r\n"
				+ ", extended, status, ext_ref_no_in\r\n"
				+ "  FROM (SELECT /*+ INDEX(B,POS_INBOUND_DOCS_NU8) INDEX(C,REC_PK) */  b.ID, pv_cve_mvt,\r\n"
				+ "               TO_CHAR (created_date, 'dd/mm/yyyy') created_date,\r\n"
				+ "               pv_ced_id, pv_cr_from_loc, tsf_no, ext_ref_no, order_no\r\n"
				+ ", c.extended, b.status, c.ext_ref_no_in\r\n"
				+ "          FROM posuser.pos_inbound_docs b, posuser.pos_rec c\r\n"
				+ "         WHERE --SUBSTR(b.PV_DOC_NAME,4,10) = ?\r\n"
				+ "            b.doc_type = 'REC'\r\n"
				+ "           AND status IN ('I')\r\n"
				+ "           AND c.pid_id = b.ID\r\n"
				+ "           AND c.pv_cve_mvt <> 10\r\n"
				+ "and b.partition_date > sysdate - 45)";
		
		String qryEmpaquetadoREQ = "SELECT *\r\n" + 
				"FROM POSUSER.POS_ENVELOPE\r\n" + 
				"WHERE PV_CR_TIENDA= '"+data.get("cr_tienda")+"'\r\n" + 
				"AND RECEIVED_DATE >= TO_DATE('%s','dd/mm/yyyy')\r\n" + 
				"ORDER BY RECEIVED_DATE DESC";
		
		String qryArchivosRelacionados = "SELECT * FROM POSUSER.POS_INBOUND_DOCS WHERE ID = %s";
		
		String qryaHeaderArchivoRec = "SELECT * FROM POSUSER.POS_REC WHERE PID_ID=%s AND PV_CVE_MVT = '"+data.get("PV_CVE_MVT")+"' AND TSF_NO !=0";
		
		String qryDetalleArchivoRec = "SELECT * FROM POSUSER.POS_REC_DETL WHERE PID_ID=%s";
		
		String qryInfoDisponibleProcesar = "SELECT DISTINCT STATUS, TRANSFER_NUMBER \r\n" + 
				"FROM TRANSFER_HEAD \r\n" + 
				"WHERE TRANSFER_NUMBER = %s\r\n" + 
				"AND STATUS = 'S'";
		
		String qryConfFtp = "SELECT FTP_BASE_DIR, FTP_SERVERHOST, FTP_SERVERPORT, FTP_USERNAME, FTP_PASSWORD AS FTP_PASSWORD \r\n" + 
				"FROM WMUSER.WM_FTP_CONNECTIONS \r\n" + 
				"WHERE FTP_CONN_ID = 'RTKRMS'";
		
		String qryWmLog = "SELECT *\r\n" + 
				"FROM WM_LOG_RUN\r\n" + 
				"WHERE INTERFACE   = 'PR23main'\r\n" + 
				"AND STATUS        = 'S'\r\n" + 
				"AND TRUNC(END_DT) = TRUNC(SYSDATE)\r\n" + 
				"ORDER BY RUN_ID DESC";
		
		String qryVerificarThreads = "SELECT * FROM WM_LOG_THREAD\r\n" + 
				"WHERE PARENT_ID = %s AND STATUS = 'S'";
		
		String qryErrorLog = "SELECT * FROM WM_LOG_ERROR WHERE RUN_ID = %s";
		
		String qryStatusReq = "SELECT * FROM POSUSER.POS_INBOUND_DOCS WHERE ID = %s AND STATUS = 'E'";
		
		String qryRtkInbound = "SELECT * FROM wmuser.rtk_inbound_docs WHERE run_id IN (%s) and doc_type = 'REC' AND status = 'L'";
		

		testCase.setProject_Name("POC WMx86");
		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		
		/****************************************************************************************************************************************
		 * Paso 1
		 * **************************************************************************************************************************************/
		addStep("Establecer la conexion con la BD **FCWM6QA**.");
				
				testCase.addTextEvidenceCurrentStep("Conexion: FCWM6QA");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		
		
		/****************************************************************************************************************************************
		 * Paso 2
		 * **************************************************************************************************************************************/
		addStep("Validar que exista informacion de la Plaza y Tienda pendiente de procesar con antiguedad mayor a 45 dias en POSUSER");
				
				System.out.println("qryInformacionPendiente: \r\n "+ qryInformacionPendiente);
				SQLResult qryInformacionPendiente_r = executeQuery(FCWM6QA, qryInformacionPendiente);
				testCase.addQueryEvidenceCurrentStep(qryInformacionPendiente_r, true);
				
				assertFalse(qryInformacionPendiente_r.isEmpty());
				
				String creationDate = qryInformacionPendiente_r.getData(0, "CREATED_DATE").substring(0,10);
				String id = qryInformacionPendiente_r.getData(0, "ID");
				
				System.out.println("creationDate: " + creationDate);
		
				
		/****************************************************************************************************************************************
		 * Paso 3
		 * **************************************************************************************************************************************/
		addStep("Validar con la siguiente consulta en la tabla POSUSER.POS_ENVELOPE de la BD FCWM6QA el empaquetamiento REC");
				
				String qryEmpaquetadoREQ_f = String.format(qryEmpaquetadoREQ, creationDate);
				System.out.println("qryEmpaquetadoREQ_f: \r\n "+ qryEmpaquetadoREQ_f);
				
				SQLResult qryEmpaquetadoREQ_r = executeQuery(FCWM6QA, qryEmpaquetadoREQ_f);
				testCase.addQueryEvidenceCurrentStep(qryEmpaquetadoREQ_r, true);
				
				assertFalse(qryEmpaquetadoREQ_r.isEmpty());
				
				
		/****************************************************************************************************************************************
		 * Paso 4
		 * **************************************************************************************************************************************/
		addStep("Validar con la siguiente consulta en la tabla POSUSER.POS_INBOUND_DOCS de la BD FCWM6QA que se muestren los archivos relacionados con el empaquetamiento (incluyendo el REC):");
				
				String qryArchivosRelacionados_f = String.format(qryArchivosRelacionados, id);
				System.out.println("qryArchivosRelacionados_f: \r\n "+ qryArchivosRelacionados_f);
				
				SQLResult qryArchivosRelacionados_r = executeQuery(FCWM6QA, qryArchivosRelacionados_f);
				testCase.addQueryEvidenceCurrentStep(qryArchivosRelacionados_r, true);
				
				assertFalse(qryArchivosRelacionados_r.isEmpty());
		
		/****************************************************************************************************************************************
		 * Paso 5
		 * **************************************************************************************************************************************/
		addStep("Validar con la siguiente consulta en la tabla POSUSER.POS_REC de la BD FCWM6QA que se muestre el header del archivo REC");
				
				String qryaHeaderArchivoRec_f = String.format(qryaHeaderArchivoRec, id);
				System.out.println("qryaHeaderArchivoRec_f: \r\n "+ qryaHeaderArchivoRec_f);
				
				SQLResult qryaHeaderArchivoRec_r = executeQuery(FCWM6QA, qryaHeaderArchivoRec_f);
				testCase.addQueryEvidenceCurrentStep(qryaHeaderArchivoRec_r, true);
				
				String tsfNo = qryaHeaderArchivoRec_r.getData(0, "TSF_NO");
				
				assertFalse(qryaHeaderArchivoRec_r.isEmpty());
				
		
		/****************************************************************************************************************************************
		 * Paso 6
		 * **************************************************************************************************************************************/
		addStep("Validar con la siguiente consulta en la tabla POSUSER.POS_REC_DETL en la BD FCWM6QA el detalle del contenido archivo REC");
				
				String qryDetalleArchivoRec_f = String.format(qryDetalleArchivoRec, id);
				System.out.println("qryDetalleArchivoRec_f: \r\n "+ qryDetalleArchivoRec_f);
				
				SQLResult qryDetalleArchivoRec_r = executeQuery(FCWM6QA, qryDetalleArchivoRec_f);
				testCase.addQueryEvidenceCurrentStep(qryDetalleArchivoRec_r, true);
				
				assertFalse(qryDetalleArchivoRec_r.isEmpty());
		
				
		/****************************************************************************************************************************************
		 * Paso 7
		 * **************************************************************************************************************************************/
		addStep("Establecer la conexion a la BD **FCRMSQA**.");
				
				testCase.addTextEvidenceCurrentStep("Conexion: FCRMSQA");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_RMS_MEX);
				
				
		/****************************************************************************************************************************************
		 * Paso 8
		 * **************************************************************************************************************************************/
		addStep("Verificar la informacion disponible para procesar en FCRMSQA.");
				
				String qryInfoDisponibleProcesar_f = String.format(qryInfoDisponibleProcesar, tsfNo);
				System.out.println("qryInfoDisponibleProcesar_f: \r\n "+ qryInfoDisponibleProcesar_f);
				
				SQLResult qryInfoDisponibleProcesar_r = executeQuery(FCRMSQA, qryInfoDisponibleProcesar_f);
				testCase.addQueryEvidenceCurrentStep(qryInfoDisponibleProcesar_r, true);
				
				assertFalse(qryInfoDisponibleProcesar_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 9
		 * **************************************************************************************************************************************/
		addStep("Verificar que la configuracion FTP existe en la BD FCWM6QA");
				
				System.out.println("qryConfFtp: \r\n "+ qryConfFtp);
				
				SQLResult qryConfFtp_r = executeQuery(FCWM6QA, qryConfFtp);
				testCase.addQueryEvidenceCurrentStep(qryConfFtp_r, true);
				
				assertFalse(qryConfFtp_r.isEmpty());
		
		
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/****************************************************************************************************************************************
		 * Paso 10
		 * **************************************************************************************************************************************/
		addStep("Ejecucion PR23 control-M");
		
				// Se obtiene la cadena de texto del data provider en la columna "jobs"
				// Se asigna a un array para poder manejarlo
				JSONArray array = new JSONArray(data.get("cm_jobs"));

				testCase.addTextEvidenceCurrentStep("Ejecucion Job: " + data.get("cm_jobs"));
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
		 * Paso 11
		 * **************************************************************************************************************************************/
		addStep("Abrir la herramienta de control M para validar que la ejecucion del job haya sido exitosa");
				
				testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
				System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);
				
				assertEquals(resultadoEjecucion, "Ended OK");
				u.close();
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		/****************************************************************************************************************************************
		 * Paso 12
		 * **************************************************************************************************************************************/
		addStep("Establecer la conexion con la BD *FCWMLQA*");
				
				testCase.addTextEvidenceCurrentStep("Conexion: DB_HOST_FCWMLQA_WMLOG");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
				
				
		/****************************************************************************************************************************************
		 * Paso 13
		 * **************************************************************************************************************************************/
		addStep("Verificar en la BD *FCWMLQA*  que la interfaz termino con exito en WMLOG.");
				
				System.out.println("qryWmLog: \r\n "+ qryWmLog);
				
				SQLResult qryWmLog_r = executeQuery(FCWMLQA_WMLOG, qryWmLog);
				testCase.addQueryEvidenceCurrentStep(qryWmLog_r, true);
				
				String runId = qryWmLog_r.getData(0, "RUN_ID");
				//String runId = "2162934222"; //TODO ELIMINAR, DATO DE PRUEBA
				assertFalse(qryWmLog_r.isEmpty());
				
				
		/****************************************************************************************************************************************
		 * Paso 14
		 * **************************************************************************************************************************************/
		addStep("Verificar los threads  en la BD *FCWMLQA* generados durante la ejecucion");
				
				String qryVerificarThreads_f = String.format(qryVerificarThreads, runId);
				System.out.println("qryVerificarThreads_f: \r\n "+ qryVerificarThreads_f);
				
				SQLResult qryVerificarThreads_r = executeQuery(FCWMLQA_WMLOG, qryVerificarThreads_f);
				testCase.addQueryEvidenceCurrentStep(qryVerificarThreads_r, true);
				
				String threadId = qryVerificarThreads_r.getData(0, "THREAD_ID");
				
				assertFalse(qryVerificarThreads_r.isEmpty());
		
		
		/****************************************************************************************************************************************
		 * Paso 15
		 * **************************************************************************************************************************************/
		addStep("Validar que no se hayan generado errores de la ejecucion de la interface PR23  en la tabla WM_LOG_ERROR  de la BD *FCWMLQA*");
				
				String qryErrorLog_f = String.format(qryErrorLog, runId);
				System.out.println("qryErrorLog_f: \r\n "+ qryErrorLog_f);
				
				SQLResult qryErrorLog_r = executeQuery(FCWMLQA_WMLOG, qryErrorLog_f);
				testCase.addQueryEvidenceCurrentStep(qryErrorLog_r, true);
				
				assertFalse(qryErrorLog_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 16
		 * **************************************************************************************************************************************/
		addStep("Verificar los status de el archivo REC de la interfaz PR23  en la tabla POS_INBOUND_DOCS");
				
				String qryStatusReq_f = String.format(qryStatusReq, id);
				System.out.println("qryStatusReq_f: \r\n "+ qryStatusReq_f);
				
				SQLResult qryStatusReq_r = executeQuery(FCWM6QA, qryStatusReq_f);
				testCase.addQueryEvidenceCurrentStep(qryStatusReq_r, true);
				
				assertFalse(qryStatusReq_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 17
		 * **************************************************************************************************************************************/
		addStep("Verificar el registro insertado en la tabla RTK_INBOUND_DOCS en la BD *FCRMSQA*");
				
				String qryRtkInbound_f = String.format(qryRtkInbound, threadId);
				System.out.println("qryRtkInbound_f: \r\n "+ qryRtkInbound_f);
				
				SQLResult qryRtkInbound_r = executeQuery(FCRMSQA, qryRtkInbound_f);
				testCase.addQueryEvidenceCurrentStep(qryRtkInbound_r, true);
				
				String rtkFilename = qryRtkInbound_r.getData(0, "RTK_FILENAME");
				
				assertFalse(qryRtkInbound_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 18
		 * **************************************************************************************************************************************/
		addStep("Establecer la conexion con filezilla con  el servidor FTP de RMS");
		
				testCase.addTextEvidenceCurrentStep("FTP_HOST: " + data.get("FTP_HOST") +
						"\nFTP_PORT: " + data.get("FTP_PORT") +
						"\nFTP_USER: " + data.get("FTP_USER"));
				
				FTPUtil ftp = new FTPUtil(
						data.get("FTP_HOST"),
						Integer.parseInt(data.get("FTP_PORT")),
						data.get("FTP_USER"),
						data.get("FTP_PASSWORD")
				);
				
		/****************************************************************************************************************************************
		 * Paso 19
		 * **************************************************************************************************************************************/
		addStep("Validar que se realizo el envio del archivo REC  por la interfaz PR23  en el directorio de archivos REC en RMS");
				String path = data.get("FTP_REC_PATH") + "/" +rtkFilename;
				//String path = data.get("FTP_REC_PATH") + "/" + "123.dat";
		
				if (ftp.fileExists(path)) {
					System.out.println(path + " - Existe");
					testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: " + path);
				} else {
					System.out.println(path + " - No xiste");
					testCase.addTextEvidenceCurrentStep("Error - no se encontro archivo: " + path);
				}
				
				assertTrue(ftp.fileExists(path), "No se obtiene el archivo por FTP.");
				
	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_003_PR23_WMx86_ProcesarArchivoRECReciboTransferencia_test";
	}

	@Override
	public String setTestDescription() {
		return "Construido. MTC_FT_016 PR23 Procesar archivo REC de recibo de transferencia a traves de la interface FEMSA_PR23";
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