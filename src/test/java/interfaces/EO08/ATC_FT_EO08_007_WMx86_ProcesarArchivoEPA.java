package interfaces.EO08;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.commons.net.ftp.FTPFile;
import org.json.JSONArray;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/**
 * 004-FT-BACK OFFICE AVANTE: MTC-FT-012 EO08 Procesar archivo EPA de estados de cuenta a trav�s de la interface FEMSA_EO08
 * Desc:
 * Prueba de regresion  para comprobar la no afectaci�n en la funcionalidad principal de la interface FEMSA_EO08 de avante para procesar archivos EPA (Estados de Cuenta) desde el FTP de AMEX a EBS_AVANTE y genere los archivos .DAT en el FTP de OL11_OL14, al ser migrada la interface de WM9.9 a WM10.5.
 * P�lizas AMEX.La EO08 obtiene los estados de cuenta AMEX (EPA) para poder depositarlos en la entrada de la interfaz OL11-14 y despu�s de que termina de ejecutarse la interfaz OL11-14 con el nuevo documento EPA se generara una p�liza contable por cada plaza que se pueda insertar por medio de comandos en la tabla GL_INTERFACE de Oracle. 
 * AMEX es quien deposita los estados de cuenta en el servidor ConnectDirect para que la interfaz EO08 pueda obtenerlos y de esta forma validar cada una de las funciones que corresponden al estado cuenta, asegurar que los montos, IVA  y comisiones son correctas para despu�s poder transformarlo al formato que maneja actualmente la interfaz OL11-14.
 * As� como tambi�n generar las p�lizas contables correspondientes a cada plaza, para que nos pueda ayudar para la conciliaci�n de dep�sitos
 * Origen:
 * Connect Direct el cual es el servidor donde se depositan los estados de cuenta por las personas encargadas de AMEX. 
 * Destino: 
 * Es la OL11-14 una interfaz que recibe los estados de cuenta de los usuarios por ftp, de esta manera aplica algunas transformaciones por tipo de banco 
 * (Serfin, Bancomer, HSBC y Femsa) e introducir los datos en tablas de Oracle utilizando WebMethods integration server.
 * @author Roberto Flores
 * @date   2022/07/07
 */
public class ATC_FT_EO08_007_WMx86_ProcesarArchivoEPA extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_EO08_007_WMx86_ProcesarArchivoEPA_test(HashMap<String, String> data) throws Exception {
		
		testCase.setPrerequisites("*Contar con un archivo EPA valido y actualizado para ser procesado.\r\n"
				+ "*Contar con acceso al FTP de AMEX donde se depositar�n los archivos EPA para ser procesados.\r\n"
				+ "*Contar con el acceso al FTP de OL11_OL14 donde se depositar�n los archivos generados .DAT.\r\n"
				+ "*Contar con acceso a las bases de datos de FCWM6QA, FCWMLQA y   AVEBQA (EBS) de Avante.\r\n"
				+ "*Comprobar que las configuraciones de las interfaces EO08 y OL11_OL14 de las tablas wmuser.wm_interfase_config y wm_fw_bo_encendido sean correctas para las plazas relacionadas con la informaci�n del archivo EPA.\r\n"
				+ "*Contar con el nombre y grupo del nuevo Job de Control M para la ejecuci�n de la interface EO08.\r\n"
				+ "*Contar con acceso a repositorio de buz�n de la tienda.\r\n"
				+ "*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA de Back Office."
				+ "");
		
		/*
		 * Utiler�as
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
//		SQLUtil FCRMSQA = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX,GlobalVariables.DB_PASSWORD_RMS_MEX);
		SQLUtil FCWM6QA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil FCWMLQA_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, GlobalVariables.DB_USER_FCWMLQA_WMLOG,GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		SQLUtil AVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		
		
		
		/*
		 * Querys
		 *********************************************************************/
//		String qryConfiguracion = "SELECT operacion,\r\n"
//				+ "  DECODE (operacion, 'FTP', wm_encryption.decrypt_data (valor1, ?), valor1) AS valor1,\r\n"
//				+ "  valor2,\r\n"
//				+ "  DECODE (operacion, 'FTP', wm_encryption.decrypt_data (valor3, ?), valor3) AS valor3,\r\n"
//				+ "  DECODE (valor4, NULL, NULL, wm_encryption.decrypt_data (valor4, ?))       AS valor4,\r\n"
//				+ "  valor5\r\n"
//				+ "FROM wm_interfase_config\r\n"
//				+ "WHERE interface = 'EO08'";
		String qryConfiguracion = "select * from wmuser.wm_interfase_config\r\n"
				+ "WHERE interfase = 'EO08'";
		
		String qryWmLogRun = "select * \r\n"
				+ "from wm_log_run \r\n"
				+ "where interface LIKE '%s' AND status = 'S' \r\n"
				+ "AND start_dt >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n"
				+ "ORDER BY RUN_ID DESC";
		
		String qryThreads = "select * from WM_LOG_THREAD where PARENT_ID = %s and status = 'S'";
		
		String qryError = "SELECT * FROM WM_LOG_ERROR WHERE RUN_ID= %s";
		
		String qryCambioStatus = "SELECT * FROM wmuser.EPA_HEADER WHERE RUNID = %s \r\n"
				+ "AND CREATION_DATE >= TRUNC(SYSDATE) \r\n"
				+ "AND WM_STATUS_INSERTED = 'P' \r\n"
				+ "AND WM_STATUS_SENT = 'P' \r\n"
				+ "AND DOC_NAME_SENT IS NOT NULL";
		
		String qryEpaRecord = "select * from wmuser.epa_record where epa_id in (%s)";
		
		String qryEpaRoc = "select * from wmuser.epa_roc where epa_id in  (%s)";
		
		String qryGlInterface = "SELECT * from gl.GL_INTERFACE \r\n"
				+ "WHERE reference6 = %s \r\n"
				+ "AND status='NEW' \r\n"
				+ "AND date_created = trunc(sysdate) \r\n"
				+ "AND segment3 = '"+data.get("CR_PLAZA")+"'";
		
		String qryXxfcGlInterface = "SELECT * FROM xxfc.xxfc_gl_interface \r\n"
				+ "WHERE xxfc_estatus = 'S' \r\n"
				+ "AND segment3 = '"+data.get("CR_PLAZA")+"' \r\n"
				+ "AND date_created>= TRUNC(SYSDATE) \r\n"
				+ "AND reference6 = %s";
		
		Date fechaEjecucionInicio;

		testCase.setProject_Name("POC WMx86");
		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		
		/****************************************************************************************************************************************
		 * Paso 1
		 * **************************************************************************************************************************************/
		addStep("Realizar conexi�n en la BD FCWM6QA.FEMCOM.NET. del host: oxfwm6q00.femcom.net.");
				
				testCase.addTextEvidenceCurrentStep("Conexion: FCWM6QA");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		
		/****************************************************************************************************************************************
		 * Paso 2
		 * **************************************************************************************************************************************/
		addStep("Verificar la configuraci�n de la interfaz tenga los datos para procesar en la BD FCWM6QA");
				
				System.out.println("qryConfiguracion: \r\n "+ qryConfiguracion);
				SQLResult qryConfiguracion_r = executeQuery(FCWM6QA, qryConfiguracion);
				testCase.addQueryEvidenceCurrentStep(qryConfiguracion_r, true);
				
				assertFalse(qryConfiguracion_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 3
		 * **************************************************************************************************************************************/
		addStep("Validar que existan archivos en directorio FTP para ser procesados \"EPA_MEX_OXXO_yyyymmdd_3004.dat\", en el FTP");
				
				testCase.addTextEvidenceCurrentStep("FTP_HOST: " + data.get("FTP_HOST") +
						"\nFTP_PORT: " + data.get("FTP_PORT") +
						"\nFTP_USER: " + data.get("FTP_USER"));
				
				FTPUtil ftp = new FTPUtil(
						data.get("FTP_HOST"),
						Integer.parseInt(data.get("FTP_PORT")),
						data.get("FTP_USER"),
						data.get("FTP_PASSWORD"));
				
				FTPFile[] files = ftp.getClient().listFiles("/AMEX");
				
				ArrayList<String> archivosPendientes = new ArrayList<>();
				
				for (FTPFile file : files) {
				    String fileName = file.getName();
				    
				    if (fileName.startsWith("EPA_MEX_OXXO") && fileName.endsWith("3004.dat")) {
				    	archivosPendientes.add(fileName);
				    	System.out.println(fileName);
					}
				}
				
				assertTrue(archivosPendientes.size()>0, "No se encontraron archivos para");
		

		
//Inicio control-m
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/****************************************************************************************************************************************
		 * Paso 4
		 * **************************************************************************************************************************************/
		addStep("Ejecuci�n control-M");
		
				fechaEjecucionInicio = new Date();
		
				// Se obtiene la cadena de texto del data provider en la columna "jobs"
				// Se asigna a un array para poder manejarlo
				JSONArray array = new JSONArray(data.get("cm_jobs"));

				testCase.addTextEvidenceCurrentStep("Ejecuci�n Job: " + data.get("cm_jobs"));
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
		 * Paso 5
		 * **************************************************************************************************************************************/
		addStep("Abrir la herramienta de control M para validar que la ejecuci�n del job haya sido exitosa");
				
				testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
				System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);
				
				assertEquals(resultadoEjecucion, "Ended OK");
				u.close();
				
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
//Fin control-m
		/****************************************************************************************************************************************
		 * Paso 6
		 * **************************************************************************************************************************************/
		addStep(" Realizar conexi�n a la BD FCWMLQA.FEMCOM.NET del host oxfwm6q00.femcom.net.");
				
		testCase.addTextEvidenceCurrentStep("Conexion: FCWMLQA_WMLOG");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
				
		
		/****************************************************************************************************************************************
		 * Paso 7
		 * **************************************************************************************************************************************/
		addStep("Validar correcta ejecuci�n de la interface en la tabla WM_LOG_RUN de BD FCWMLQA.");
				
				String fechaEjecucionInicio_f = formatter.format(fechaEjecucionInicio);
		
				String qryWmLogRun_f = String.format(qryWmLogRun, "%EO08%", fechaEjecucionInicio_f);
				System.out.println("qryWmLogRun_f: \r\n "+ qryWmLogRun_f);
				
				SQLResult qryWmLogRun_r = executeQuery(FCWMLQA_WMLOG, qryWmLogRun_f);
				testCase.addQueryEvidenceCurrentStep(qryWmLogRun_r, true);
				
				assertFalse(qryWmLogRun_r.isEmpty());
				
				String runId = qryWmLogRun_r.getData(0, "RUN_ID");
				
		/****************************************************************************************************************************************
		 * Paso 8
		 * **************************************************************************************************************************************/
		addStep("Realizar la siguiente consulta para verificar que no se encuentre ning�n error presente en la ejecuci�n de la interfaz EO08");
				
				String qryError_f = String.format(qryError, runId);
				System.out.println("qryError_f: \r\n "+ qryError);
				
				SQLResult qryError_r = executeQuery(FCWMLQA_WMLOG, qryError_f);
				testCase.addQueryEvidenceCurrentStep(qryError_r, true);
				
				assertTrue(qryError_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 9
		 * **************************************************************************************************************************************/
		addStep(" Realizar la siguiente consulta para verificar el registro de los thread correctamente en la ejecuci�n de la interfaz EO08");
				
				String qryThreads_f = String.format(qryThreads, runId);
				System.out.println("qryThreads_f: \r\n "+ qryThreads_f);
				
				SQLResult qryThreads_r = executeQuery(FCWMLQA_WMLOG, qryThreads_f);
				testCase.addQueryEvidenceCurrentStep(qryThreads_r, true);
				
				assertFalse(qryThreads_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 10
		 * **************************************************************************************************************************************/
		addStep("Establecer la conexi�n con la BD **FCWM6QA**.");
		
				testCase.addTextEvidenceCurrentStep("Conexion: AVEBQA");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_AVEBQA);
				
				
		/****************************************************************************************************************************************
		 * Paso 11
		 * **************************************************************************************************************************************/
		addStep("Validar la actualizaci�n de los estatus en la tabla EPA_HEADER de la BD AVEBQA de EBS, con wm_status_inserted=P y wm_status_sent=P");
				
				String qryCambioStatus_f = String.format(qryCambioStatus, runId);
				System.out.println("qryCambioStatus_f: \r\n "+ qryCambioStatus_f);
				
				SQLResult qryCambioStatus_r = executeQuery(AVEBQA, qryCambioStatus_f);
				testCase.addQueryEvidenceCurrentStep(qryCambioStatus_r, true);
				
				assertFalse(qryCambioStatus_r.isEmpty());
				
				String idEpaHeader = qryCambioStatus_r.getData(0, "ID");
				String glJournalIdEpaHeader = qryCambioStatus_r.getData(0, "GL_JOURNAL_ID");
				
		/****************************************************************************************************************************************
		 * Paso 12
		 * **************************************************************************************************************************************/
		addStep("Validar que se insertaron registros en la tabla EPA_RECORD de BD AVEBQA.");
				
				String qryEpaRecord_f = String.format(qryEpaRecord, idEpaHeader);
				System.out.println("qryEpaRecord_f: \r\n "+ qryEpaRecord_f);
				
				SQLResult qryEpaRecord_r = executeQuery(AVEBQA, qryEpaRecord_f);
				testCase.addQueryEvidenceCurrentStep(qryEpaRecord_r, true);
				
				assertFalse(qryEpaRecord_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 13
		 * **************************************************************************************************************************************/
		addStep("Validar que se insertaron registros en la tabla EPA_ROC de BD AVEBQA.");
				
				String qryEpaRoc_f = String.format(qryEpaRoc, idEpaHeader);
				System.out.println("qryEpaRoc_f: \r\n "+ qryEpaRoc_f);
				
				SQLResult qryEpaRoc_r = executeQuery(AVEBQA, qryEpaRoc_f);
				testCase.addQueryEvidenceCurrentStep(qryEpaRoc_r, true);
				
				assertFalse(qryEpaRoc_r.isEmpty());		
		
				
		/****************************************************************************************************************************************
		 * Paso 14
		 * **************************************************************************************************************************************/
		addStep("Validar la inserci�n de p�lizas contables en la tabla GL_INTERFACE de la BD AVEBQA.");
				
				String qryGlInterface_f = String.format(qryGlInterface, glJournalIdEpaHeader);
				System.out.println("qryGlInterface_f: \r\n "+ qryGlInterface_f);
				
				SQLResult qryGlInterface_r = executeQuery(AVEBQA, qryGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryGlInterface_r, true);
				
				assertFalse(qryGlInterface_r.isEmpty());		
		
				
		/****************************************************************************************************************************************
		 * Paso 15
		 * **************************************************************************************************************************************/
		addStep("Validar la actualizaci�n de los datos en la tabla XXFC_GL_INTERFACE de la BD AVEBQA, con el campo xxfc_estatus = S.");
				
				String qryXxfcGlInterface_f = String.format(qryXxfcGlInterface, glJournalIdEpaHeader);
				System.out.println("qryXxfcGlInterface_f: \r\n "+ qryXxfcGlInterface_f);
				
				SQLResult qryXxfcGlInterface_r = executeQuery(AVEBQA, qryXxfcGlInterface_f);
				testCase.addQueryEvidenceCurrentStep(qryXxfcGlInterface_r, true);
				
				assertFalse(qryXxfcGlInterface_r.isEmpty());
				
				
		/****************************************************************************************************************************************
		 * Paso 16
		 * **************************************************************************************************************************************/
		addStep("Validar que los archivos en directorio FTP que se procesaron, se le haya agregado el prefijo ' Procesado_' + \"EPA_MEX_OXXO_yyyymmdd_3004.dat\"");
				
				for (String archivo : archivosPendientes) {
					String path = "/AMEX/" + "Procesado_" + archivo;
					
					if (ftp.fileExists(path)) {
						System.out.println(path + " - Existe");
						testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: " + path);
					} else {
						System.out.println(path + " - No xiste");
						testCase.addTextEvidenceCurrentStep("Error - no se encontr� archivo: " + path);
					}
					
					assertTrue(ftp.fileExists(path), "No se obtiene el archivo por FTP.");
					
				}
				
		/****************************************************************************************************************************************
		 * Paso 17
		 * **************************************************************************************************************************************/
//		addStep(" Validar que se insertaron los archivos generados por la EO08 en la carpeta de entrada de la interface OL11_OL14.");
//		
//				testCase.addTextEvidenceCurrentStep("FTP_HOST_OL11_OL14: " + data.get("FTP_HOSTOL11_OL14") +
//						"\nFTP_PORT: " + data.get("FTP_PORT_OL11_OL14") +
//						"\nFTP_USER: " + data.get("FTP_USER_OL11_OL14"));
//				
//				FTPUtil ftpOL11_OL14 = new FTPUtil(
//						data.get("FTP_HOST_OL11_OL14"),
//						Integer.parseInt(data.get("FTP_PORT_OL11_OL14")),
//						data.get("FTP_USER_OL11_OL14"),
//						data.get("FTP_PASSWORD_OL11_OL14"));
//		
//				for (String archivo : archivosPendientes) {
//					String path = "/u01/BATCH/OL11_OL14/" + "BBVI" + archivo;
//					
//					if (ftp.fileExists(path)) {
//						System.out.println(path + " - Existe");
//						testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: " + path);
//					} else {
//						System.out.println(path + " - No xiste");
//						testCase.addTextEvidenceCurrentStep("Error - no se encontr� archivo: " + path);
//					}
//					
//					assertTrue(ftp.fileExists(path), "No se obtiene el archivo por FTP.");
//					
//				}
						
		

	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_EO08_007_WMx86_ProcesarArchivoEPA_test";
	}

	@Override
	public String setTestDescription() {
		return null;
	}

	@Override
	public String setTestDesigner() {
		return "Equipo automatizacion";
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