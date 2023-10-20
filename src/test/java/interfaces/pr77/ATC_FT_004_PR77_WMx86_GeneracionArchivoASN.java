package interfaces.pr77;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.controlm.ControlM;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLResultExcel;
import utils.sql.SQLUtil;

/**
 * Interfaces BO MEX: MTC_FT_014 PR77 Generaci�n de archivo ASN de Advanced Shipping Notice a trav�s de la interface FEMSA_PR77
 * Desc:
 * Prueba de regresion para comprobar la no afectacion en la funcionalidad principal de la interface FEMSA_PR77 
 * para generar archivos ASN (Advanced Shipping Notice) de bajada (de RDM a WM OUTBOUND), 
 * al ser migrada la interface de WM9.9 a WM10.5.
 * @author Roberto Flores
 * @date   2022/07/05
 */
public class ATC_FT_004_PR77_WMx86_GeneracionArchivoASN extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_PR77_WMx86_GenArchivoASN_test (HashMap<String, String> data) throws Exception {
		
		testCase.setPrerequisites("*Contar con acceso a las bases de datos de FCWM6QA y RDM correspondientes al CEDIS que se utilizar�, FCWMLQA (oxfwm6q00). \r\n"
				+ "*Contar con el nombre y grupo del nuevo Job de Control M para PR77 de WM10.\r\n"
				+ "*Contar con acceso a repositorio de buz�n de la tienda.\r\n"
				+ "*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA de Back Office.\r\n"
				+ "");
		
		/*
		 * Utiler�as
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		SQLUtil FCRD01CU = new SQLUtil(GlobalVariables.DB_HOST_RDM, GlobalVariables.DB_USER_RDM,GlobalVariables.DB_PASSWORD_RDM);
		SQLUtil FCWM6QA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil FCWMLQA_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, GlobalVariables.DB_USER_FCWMLQA_WMLOG,GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		
		
		
		/*
		 * Querys
		 *********************************************************************/
		String qryRegistrosProcesar = "SELECT d.item_id, h.wm_status_asn, h.*\r\n"
				+ "FROM wmuser.xxfc_posterior_embarque h\r\n"
				+ "INNER JOIN wmuser.xxfc_posterior_emb_citems d\r\n"
				+ "ON (h.tsf_no        = d.tsf_no)\r\n"
				+ "WHERE h.crplaza = '"+data.get("CR_PLAZA")+"'\r\n"
				+ "AND h.cr_tienda = '"+data.get("CR_TIENDA")+"'\r\n"
				+ "AND h.status        = 'S'\r\n"
				+ "AND h.wm_status_asn = 'I'\r\n"
				+ "AND h.DEST_ID NOT LIKE '9999999%'\r\n"
				+ "ORDER BY h.create_date DESC";
		
		String qryWmLogRun = "select RUN_ID,INTERFACE,START_DT,END_DT,STATUS,SERVER \r\n"
				+ "from WMLOG.wm_log_run \r\n"
				+ "where interface LIKE '%s' AND status = 'S' \r\n"
				+ "AND start_dt >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n"
				+ "ORDER BY RUN_ID DESC";
		
		String qryError = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION FROM WMLOG.WM_LOG_ERROR WHERE RUN_ID= %s";
		
		String qryCambioStatus = "SELECT cedis, crplaza, tsf_no, wm_status_asn, wm_doc_name_asn, wm_run_id_asn, wm_sent_date_asn \r\n"
				+ "FROM wmuser.xxfc_posterior_embarque \r\n"
				+ "WHERE wm_status_asn = 'E'\r\n"
				+ "AND crplaza = '"+data.get("CR_PLAZA")+"'\r\n"
				+ "AND cr_tienda = '"+data.get("CR_TIENDA")+"'\r\n"
				+ "AND wm_sent_date_asn >= TRUNC(SYSDATE) \r\n"
				+ "AND wm_run_id_asn = %s";
		
		String qryOutboundDocs = "SELECT * FROM posuser.pos_outbound_docs\r\n"
				+ "WHERE doc_name = '%s' \r\n"
				+ "AND doc_type = 'ASN' \r\n"
				+ "AND pv_cr_plaza = '"+data.get("CR_PLAZA")+"' \r\n"
				+ "AND pv_cr_tienda = '"+data.get("CR_TIENDA")+"' \r\n"
				+ "AND status = 'L'";
		
		Date fechaEjecucionInicio;

		testCase.setProject_Name("POC WMx86");
		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		
		/****************************************************************************************************************************************
		 * Paso 1
		 * **************************************************************************************************************************************/
		addStep("Establecer conexion a la BD FCRD01CU correspondiente al CEDIS que se estara utilizando.");
				
				testCase.addTextEvidenceCurrentStep("Conexion: FCRD01CU");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_RDM);
		
		
		/****************************************************************************************************************************************
		 * Paso 2
		 * **************************************************************************************************************************************/
		addStep("Validar que exista informacion pendiente de procesar en la tabla XXFC_POSTERIOR_EMBARQUE de la BD FCRD63CU.");
				
				System.out.println("qryRegistrosProcesar: \r\n "+ qryRegistrosProcesar);
				SQLResult qryRegistrosProcesar_r = executeQuery(FCRD01CU, qryRegistrosProcesar);
				testCase.addQueryEvidenceCurrentStep(qryRegistrosProcesar_r, true);
				
				assertFalse(qryRegistrosProcesar_r.isEmpty());
				
		
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/****************************************************************************************************************************************
		 * Paso 3
		 * **************************************************************************************************************************************/
		addStep("Ejecucion control-M");
		
				fechaEjecucionInicio = new Date();
		
				addStep("ir a Jobs de la carpeta TQA_BAJO_DEMANDA - Ejecutar JOB ProcAclaraciones de Ctrl-M ");
				SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
				
				JSONObject obj = new JSONObject(data.get("job"));

				testCase.addBoldTextEvidenceCurrentStep("Jobs en  Control M ");
				Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
				//testCase.addPaso("Paso con addPaso");
				testCase.addBoldTextEvidenceCurrentStep("Login");
				u.get(data.get("server"));
				u.hardWait(40);
				u.waitForLoadPage();
				CM.logOn(); 

				//Buscar del job
				testCase.addBoldTextEvidenceCurrentStep("Inicio de job ");
				ControlM control = new ControlM(u, testCase, obj);
				boolean flag = control.searchJob();
				assertTrue(flag);
				
				//Ejecucion
				String resultado = control.executeJob();
				System.out.println("Resultado de la ejecucion -> " + resultado);

				u.hardWait(30);
				
				//Valor del output 
				

				String Res2 = control.getNewStatus();
				

				System.out.println ("Valor de output getNewStatus:" +Res2);
				
				
				String output = control.getOutput();
				System.out.println ("Valor de output control:" +output);
				
				testCase.addTextEvidenceCurrentStep("Status de ejecucion: "+Res2);
				testCase.addTextEvidenceCurrentStep("Output de ejecucion: "+output);
				//Validacion del caso
				Boolean casoPasado = false;
				if(Res2.equals("Ended OK")) {
				casoPasado = true;
				}		
				
				control.closeViewpoint();
				u.close();
//				assertTrue(casoPasado);
				//assertNotEquals("Failure",resultado);
			
		
		/****************************************************************************************************************************************
		 * Paso 4
		 * **************************************************************************************************************************************/
		addStep("Abrir la herramienta de control M para validar que la ejecucion del job haya sido exitosa");
				
				testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultado);
				System.out.println("Resultado de la ejecucion -> " + resultado);
				
//				assertEquals(resultado, "Ended OK");
//				u.close();
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		/****************************************************************************************************************************************
		 * Paso 5
		 * **************************************************************************************************************************************/
		addStep(" Establecer conexion a la BD FCWMLQA (oxfwm6q00.femcom.net).");
				
		testCase.addTextEvidenceCurrentStep("Conexion: FCWMLQA_WMLOG");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
				
		
		/****************************************************************************************************************************************
		 * Paso 6
		 * **************************************************************************************************************************************/
		addStep("Validar el registro de ejecucion de la interfaz en FCWMLQA.");
				
				String fechaEjecucionInicio_f = formatter.format(fechaEjecucionInicio);
		
				String qryWmLogRun_f = String.format(qryWmLogRun, "%PR77%", fechaEjecucionInicio_f);
				System.out.println("qryWmLogRun_f: \r\n "+ qryWmLogRun_f);
				
				SQLResult qryWmLogRun_r = executeQuery(FCWMLQA_WMLOG, qryWmLogRun_f);
				testCase.addQueryEvidenceCurrentStep(qryWmLogRun_r, true);
				
				assertFalse(qryWmLogRun_r.isEmpty());
				
				String runId = qryWmLogRun_r.getData(0, "RUN_ID");
				
		/****************************************************************************************************************************************
		 * Paso 7
		 * **************************************************************************************************************************************/
		addStep("Validar que no se registren errores en FCWMLQA.");
				
				String qryError_f = String.format(qryError, runId);
				System.out.println("qryError_f: \r\n "+ qryError);
				
				SQLResult qryError_r = executeQuery(FCWMLQA_WMLOG, qryError_f);
				testCase.addQueryEvidenceCurrentStep(qryError_r, true);
				
				assertTrue(qryError_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 8
		 * **************************************************************************************************************************************/
		addStep("Validar que el registro de la tabla xxfc_posterior_embarque en FCRD01CU sea actualizado a estatus E.");
				
				String qryCambioStatus_f = String.format(qryCambioStatus, runId);
				System.out.println("qryCambioStatus_f: \r\n "+ qryCambioStatus);
				
				SQLResult qryCambioStatus_r = executeQuery(FCRD01CU, qryCambioStatus_f);
				testCase.addQueryEvidenceCurrentStep(qryCambioStatus_r, true);
				
				assertFalse(qryCambioStatus_r.isEmpty());
				
				String wmDocNameAsn = qryWmLogRun_r.getData(0, "WM_DOC_NAME_ASN");
				
		/****************************************************************************************************************************************
		 * Paso 9
		 * **************************************************************************************************************************************/
		addStep("Establecer la conexion con la BD **FCWM6QA**.");
				
				testCase.addTextEvidenceCurrentStep("Conexion: FCWM6QA");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
				
				
		/****************************************************************************************************************************************
		 * Paso 10
		 * **************************************************************************************************************************************/
		addStep("Validar el registro del documento enviado en la tabla pos_outbound_docs de FCWM6QA.");
		
				String qryOutboundDocs_f = String.format(qryOutboundDocs, wmDocNameAsn);
				System.out.println("qryOutboundDocs_f: \r\n "+ qryOutboundDocs_f);
				
				SQLResult qryOutboundDocs_r = executeQuery(FCWM6QA, qryOutboundDocs_f);
				testCase.addQueryEvidenceCurrentStep(qryOutboundDocs_r, true);
				
				assertFalse(qryOutboundDocs_r.isEmpty());
				
		/****************************************************************************************************************************************
		 * Paso 11
		 * **************************************************************************************************************************************/
		addStep("Ingresar al FTP de POS y validar la creacion del archivo ASN.");
		
				testCase.addTextEvidenceCurrentStep("FTP_HOST: " + data.get("FTP_HOST") +
						"\nFTP_PORT: " + data.get("FTP_PORT") +
						"\nFTP_USER: " + data.get("FTP_USER"));
				
				FTPUtil ftp = new FTPUtil(
						data.get("FTP_HOST"),
						Integer.parseInt(data.get("FTP_PORT")),
						data.get("FTP_USER"),
						data.get("FTP_PASSWORD"));
		
				String path = "/u01/posuser/FEMSA_OXXO/POS/"+data.get("CR_PLAZA")+"/"+data.get("CR_TIENDA")+"/working" + "/" +wmDocNameAsn;
		
				if (ftp.fileExists(path)) {
					System.out.println(path + " - Existe");
					testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: " + path);
				} else {
					System.out.println(path + " - No xiste");
					testCase.addTextEvidenceCurrentStep("Error - no se encontr� archivo: " + path);
				}
				
				assertTrue(ftp.fileExists(path), "No se obtiene el archivo por FTP.");
		

	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_004_PR77_WMx86_GenArchivoASN_test";
	}

	@Override
	public String setTestDescription() {
		return "MTC_FT_014 PR77 Generaci�n de archivo ASN de Advanced Shipping Notice a trav�s de la interface FEMSA_PR77";
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
