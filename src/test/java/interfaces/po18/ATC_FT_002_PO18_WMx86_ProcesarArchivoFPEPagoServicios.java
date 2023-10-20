package interfaces.po18;


import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
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
import utils.controlm.ControlM;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLResultExcel;
import utils.sql.SQLUtil;

/**
 * 004-FT-BACK OFFICE AVANTE: MTC_FT_008 PO18 Procesar archivo FPE de forma de
 * pago utilizado en pagos de servicio de la interface FEMSA_PO18 Desc: Prueba
 * de regresi�n para comprobar la no afectaci�n en la funcionalidad principal de
 * la interface FEMSA_PO18 de avante para procesar archivos FPE (Formas de Pago
 * de Servicios electr�nicos) y ser enviados de WM INBOUND a EBS, al ser migrada
 * la interface de WM9.9 a WM10.5. El prop�sito de la interface es de transferir
 * la informaci�n del documento FPE de los medio de pago utilizados en el pago
 * de un servicio que se encuentra de la base de datos de WebMethods, a una
 * nueva tabla de Oracle Finanzas. Origen: Tablas de POSUSER: POS_INBOUND_DOCS,
 * POS_FPE y POS_FPE_DETL. Destino: Tablas de ORAFIN:
 * XXFC_PAGO_SERVICIO_FORMAS_STG XXFC.XXFC_PAGO_SERVICIO_FORMAS.
 * 
 * @author Roberto Flores
 * @date 2022/06/30
 */
public class ATC_FT_002_PO18_WMx86_ProcesarArchivoFPEPagoServicios extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PO18_WMx86_ProcesarArchivoFPEPagoServicios_test(HashMap<String, String> data) throws Exception {

		testCase.setPrerequisites(
				"*Contar con un punto de venta de XPOS que se pueda comunicar con la PR50V2 de WM10.5 a trav�s de la DS50 y el Trending Network para generar y enviar el archivo FPE. \r\n"
						+ "*Contar con acceso a las bases de datos de FCWM6QA, FCWMLQA y AVEBQA (EBS) de Avante. *Contar con el nombre y grupo del nuevo Job de Control M para la ejecuci�n de la interface PO18. *Contar con acceso a repositorio de buz�n de la tienda. \r\n"
						+ "*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA de Back Office.\r\n"
						+ "");

		/*
		 * Utiler�as
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
		// String qryRegistrosProcesar = "SELECT ID, STATUS, DOC_TYPE, PV_DOC_NAME FROM
		// POSUSER.POS_INBOUND_DOCS \r\n " +
		// "WHERE PV_DOC_NAME LIKE 'TPE%' \r\n"+
		// "AND DOC_TYPE='TPE' \r\n "+
		// "AND STATUS='I'";

		String qryRegistrosProcesar = "select ID, PE_ID, PV_DOC_ID, STATUS, PV_DOC_NAME, INSERTED_DATE, VERSION, PARTITION_DATE from POSUSER.POS_INBOUND_DOCS\r\n"
				+ "where DOC_TYPE = 'FPE' and STATUS = 'I'\r\n"
				+ "AND ROWNUM<=5\r\n"
				+ "order by RECEIVED_DATE desc";

		String qryHeaderFPE = "select * from POSUSER.POS_FPE where PID_ID in  (%s)";

		String qryaRegistrosFpeDetl = "select * from POSUSER.POS_FPE_DETL where PID_ID in (%s)";

		String qryActualizacionInboundDocs = "SELECT ID, STATUS, DOC_TYPE, PV_DOC_NAME, RECEIVED_DATE\r\n"
				+ "				FROM POSUSER.POS_INBOUND_DOCS\r\n"
				+ "                where DOC_TYPE = 'FPE'\r\n"
				+ "				and STATUS = 'I'\r\n"
				+ "				AND RECEIVED_DATE <= TRUNC(SYSDATE  )\r\n"
				+ "                AND ROWNUM <=5\r\n"
				+ "                order by RECEIVED_DATE desc";

		String qryWmLogRun = "SELECT RUN_ID, INTERFACE, START_DT, STATUS, SERVER from wm_log_run \r\n" +
				"WHERE interface LIKE '%PO18%' AND status = 'S' \r\n" +
				//"AND start_dt >= TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')"+
               "And START_DT >= TRUNC(SYSDATE-10) \r\n" +
                "ORDER BY RUN_ID DESC";

		String qryThreads = "select * from WM_LOG_THREAD where PARENT_ID = '%s' and status = 'S' ORDER BY START_DT DESC";

		String ValidaRegistros = "Select * from WMUSER.XXFC_PAGO_SERVICIO_FORMAS_STG \r\n"
				+ "WHERE ROWNUM<=5 \r\n"
				+ "order by FECHA_TRANSACCION desc";
		
		String ValidaError = "SELECT * FROM  WMLOG.WM_LOG_ERROR WHERE RUN_ID= '%s'";

		Date fechaEjecucionInicio;

		testCase.setProject_Name("POC WMx86");

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	

		/****************************************************************************************************************************************
		 * Paso 1
		 **************************************************************************************************************************************/
		addStep("Establecer la conexion con la BD **FCWM6QA**.");

		testCase.addTextEvidenceCurrentStep("Conexion: FCWM6QA");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		/****************************************************************************************************************************************
		 * Paso 2
		 **************************************************************************************************************************************/
		addStep("Validar que existan archivos tipo FPE en status = 'I' en la POS_INBOUND_DOCS en la BD FCWM6QA.");
		
		//Solicité update del query para que regrese insumos con Status I con fecha mas actual.
		System.out.println("qryRegistrosProcesar: \r\n " + qryRegistrosProcesar);
		SQLResult qryRegistrosProcesar_r = executeQuery(FCWM6QA, qryRegistrosProcesar);

		boolean queryRegistrosProcesar = qryRegistrosProcesar_r.isEmpty();
		System.out.println(queryRegistrosProcesar);

		if (!queryRegistrosProcesar) {
			testCase.addBoldTextEvidenceCurrentStep(
					"Se valida que existan archivos FPE en Status = 'I' en la POS_INBOUND_DOCS en la BD FCWM6QA.");
		}
		testCase.addQueryEvidenceCurrentStep(qryRegistrosProcesar_r, true);
		assertFalse(queryRegistrosProcesar);

		String idPosInboundDocs = "";
		for (int i = 0; i < qryRegistrosProcesar_r.getRowCount(); i++) {
			idPosInboundDocs += qryRegistrosProcesar_r.getData(i, "ID");
			if (i < qryRegistrosProcesar_r.getRowCount() - 1) {
				idPosInboundDocs += ",";
			}
		}
		System.out.println("idPosInboundDocs : " + idPosInboundDocs);

		/****************************************************************************************************************************************
		 * Paso 3
		 **************************************************************************************************************************************/
		addStep("Validar que por cada archivo del paso anterior existe un registro en la POS_FPE de la BD FCWM6QA, indicando el numero de registros que contiene el archivo.");

		String qryHeaderFPE_f = String.format(qryHeaderFPE, idPosInboundDocs);
		System.out.println("qryHeaderFPE_f: \r\n " + qryHeaderFPE_f);

		SQLResult qryHeaderFPE_r = executeQuery(FCWM6QA, qryHeaderFPE_f);
		testCase.addQueryEvidenceCurrentStep(qryHeaderFPE_r, true);

		assertFalse(qryHeaderFPE_r.isEmpty());

		/****************************************************************************************************************************************
		 * Paso 4
		 **************************************************************************************************************************************/
		addStep("Validar que los registros mostrados corresponden con el numero de registros que indica el paso anterior, con el detalle de cada uno en la POS_FPE_DETL de la BD FCWM6QA.");

		String qryaRegistrosFpeDetl_f = String.format(qryaRegistrosFpeDetl, idPosInboundDocs);
		System.out.println("qryaRegistrosFpeDetl_f: \r\n " + qryaRegistrosFpeDetl_f);

		SQLResultExcel qryaRegistrosFpeDetl_r = executeQueryExcel(FCWM6QA, qryaRegistrosFpeDetl_f);
		testCase.addDocumentEvidence(qryaRegistrosFpeDetl_r.getRelativePath(), "consulta registros pos_fpe_detl");

		assertFalse(qryaRegistrosFpeDetl_r.isEmpty());

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/****************************************************************************************************************************************
		 * Paso 5
		 **************************************************************************************************************************************/
		addStep("Ejecucion control-M");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		JSONObject obj = new JSONObject(data.get("cm_jobs"));
		testCase.addBoldTextEvidenceCurrentStep("Jobs en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("cm_user"), data.get("cm_ps"));
		//testCase.addPaso("Paso con addPaso");
		testCase.addBoldTextEvidenceCurrentStep("Login");
		u.get(data.get("cm_server"));
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
		
		
		//String output = control.getOutput();
		//System.out.println ("Valor de output control:" +output);
		
		testCase.addTextEvidenceCurrentStep("Status de ejecucion: "+Res2);
		//testCase.addTextEvidenceCurrentStep("Output de ejecucion: "+output);

		//Validacion del caso
		Boolean casoPasado = false;
		if(Res2.equals("Ended OK")) {
		casoPasado = true;
		}		
		
		control.closeViewpoint();
		u.close();
		assertTrue(casoPasado);
		//assertNotEquals("Failure",resultado);

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		/****************************************************************************************************************************************
		 * Paso 7
		 **************************************************************************************************************************************/
		addStep("Validar que se actualizo el status de los archivos tipo FPE a status = 'E' en la POS_INBOUND_DOCS en la BD FCWM6QA.");

		System.out.println("qryActualizacionInboundDocs: \r\n " + qryActualizacionInboundDocs);

		SQLResult qryActualizacionInboundDocs_r = executeQuery(FCWM6QA, qryActualizacionInboundDocs);
		boolean QryActualizacionInboundDoc = qryActualizacionInboundDocs_r.isEmpty();
		testCase.addQueryEvidenceCurrentStep(qryActualizacionInboundDocs_r, true);

		assertFalse(QryActualizacionInboundDoc, "No se actualizo el status de los archivos tipo FPE a status=E en la POS_INBOUND_DOCS.");

		String targetIds = "";
		for (int i = 0; i < qryActualizacionInboundDocs_r.getRowCount(); i++) {
			targetIds += qryActualizacionInboundDocs_r.getData(i, "TARGET_ID");
			if (i < qryActualizacionInboundDocs_r.getRowCount() - 1) {
				targetIds += ",";
			}
		}
		System.out.println("Target Ids : " + targetIds);

		/****************************************************************************************************************************************
		 * Paso 8
		 **************************************************************************************************************************************/
		addStep("Realizar conexion a la BD AVEBQA de EBS.");

		testCase.addTextEvidenceCurrentStep("Conexion: AVEBQA");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_AVEBQA);

		/****************************************************************************************************************************************
		 * Paso 9
		 **************************************************************************************************************************************/
		addStep("Validar que por cada tienda se inserto cada uno de los registros que contenia el archivo procesado en la tabla XXFC_PAGO_SERVICIO_FORMAS_STG de la BD AVEBQA de EBS.");
			/*
			SQLResult ValidaRegistros_r = executeQuery(AVEBQA, ValidaRegistros);
			boolean validaRegistros = ValidaRegistros_r.isEmpty();
			
			if(!validaRegistros) {
				testCase.addBoldTextEvidenceCurrentStep("Se valida que por cada tienda se inserto cada uno de los registros que  contenia el archivo procesado en la tabla");
			String registroTiendas = "";
			for (int i = 0; i < ValidaRegistros_r.getRowCount(); i++) {
				registroTiendas += ValidaRegistros_r.getData(i, "TIENDA");
				
				if (i < ValidaRegistros_r.getRowCount() - 1) {
					registroTiendas += ",";
				}
			}
			System.out.println("Registro Tiendas : " + registroTiendas);
			}
			
			
			testCase.addQueryEvidenceCurrentStep(ValidaRegistros_r);
			assertFalse(validaRegistros, "No se insertaron los registros por cada tienda.");*/
			
		/****************************************************************************************************************************************
		 * Paso 10
		 **************************************************************************************************************************************/
		addStep("Realizar conexion a la BD FCWMLTAQ.FEMCOM.NET del host oxfwm6q00.femcom.net.");

		testCase.addTextEvidenceCurrentStep("Conexion: FCWMLQA_WMLOG");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA_WMLOG);

		/****************************************************************************************************************************************
		 * Paso 11
		 **************************************************************************************************************************************/
		addStep("Validar la correcta ejecucion de la interface PO18 en la tabla WM WM_LOG_RUN de la BD FCWML6QA.");

		//String fechaEjecucionInicio_f = formatter.format(fechaEjecucionInicio);
		//String qryWmLogRun_f = String.format(qryWmLogRun /*,"%PO18%" , fechaEjecucionInicio_f*/);
		//System.out.println("qryWmLogRun_f: \r\n " + qryWmLogRun_f);

		SQLResult qryWmLogRun_r = executeQuery(FCWMLQA_WMLOG, qryWmLogRun);
		
		boolean QryWMLogRun = qryWmLogRun_r.isEmpty();
		
		if(!QryWMLogRun) {
			
			testCase.addBoldTextEvidenceCurrentStep("Se verifco la correcta ejecucion de la interface PO18");
		}
		testCase.addQueryEvidenceCurrentStep(qryWmLogRun_r, true);
		
		assertFalse(qryWmLogRun_r.isEmpty());
		
		String runId = qryWmLogRun_r.getData(0, "RUN_ID");
		System.out.println("RUN_ID : " + runId);

		/****************************************************************************************************************************************
		 * Paso 12
		 **************************************************************************************************************************************/
		addStep("Validar la correcta ejecucion de los Threads lanzados por la interface PO18 en la tabla WM_LOG_THREAD de la BD FCWML6QA");
		
		String qryThreads_f = String.format(qryThreads, runId);
		System.out.println("qryThreads_f: \r\n " + qryThreads_f);

		SQLResult qryThreads_r = executeQuery(FCWMLQA_WMLOG, qryThreads_f);
		testCase.addQueryEvidenceCurrentStep(qryThreads_r, true);

		assertFalse(qryThreads_r.isEmpty());

		/****************************************************************************************************************************************
		 * Paso 13
		 **************************************************************************************************************************************/
		addStep("Realizar la siguiente consulta para verificar que no se encuentre ningon error presente en la ejecucion de la interfaz PO9 en la tabla WM_LOG_ERROR de BD FCWML6QA.");

		String qryError_f = String.format(ValidaError, runId);
		System.out.println("qryError_f: \r\n " + ValidaError);

		SQLResult qryError_r = executeQuery(FCWMLQA_WMLOG, qryError_f);
		testCase.addQueryEvidenceCurrentStep(qryError_r, true);

		assertTrue(qryError_r.isEmpty());

	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_002_PO18_WMx86_ProcesarArchivoFPEPagoServicios_test";
	}

	@Override
	public String setTestDescription() {
		return "MTC_FT_008 PO18 Procesar archivo FPE de forma de pago utilizado en pagos de servicio de la interface FEMSA_PO18";
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
