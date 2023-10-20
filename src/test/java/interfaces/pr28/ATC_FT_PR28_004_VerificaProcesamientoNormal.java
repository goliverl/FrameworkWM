package interfaces.pr28;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ATC_FT_PR28_004_VerificaProcesamientoNormal extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR28_004_VerificaProcesamientoNormal_test(HashMap<String, String> data) throws Exception {
		
		/*
		 * Utilerias
		 */
								  //ORIGINAL										
		 						  //new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_fcwmesit, GlobalVariables.DB_USER_fcwmesit, GlobalVariables.DB_PASSWORD_fcwmesit); //NUCLEO
		
		//utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS, GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA); //SIN CAMBIOS
		
								  //ORIGINAL
		 						  //new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCRMSMGR, GlobalVariables.DB_USER_FCRMSMGR, GlobalVariables.DB_PASSWORD_FCRMSMGR); //NUCLEO
		/**
		 * ALM
		 * Verificar procesamiento normal de la interfaz  -  REC
		 * Verificar procesamiento normal de la interfaz  -  APO
		 */
		
		/**
		 * Variables
		 * *********************************************************************************************** 
		 */

		String tiendas = "SELECT PV_CR_PLAZA, PV_CR_TIENDA, B.STATUS"
				+ " FROM POSUSER.POS_ENVELOPE A, POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C"
				+ " WHERE B.PE_ID = A.ID AND B.DOC_TYPE = 'REC' AND B.STATUS IN ('I')"
				+ " AND C.PID_ID = B.ID AND C.PV_CVE_MVT = '10'"
				+ " AND C.EXT_REF_NO <> '0' AND C.ORDER_NO = 0 AND B.PARTITION_DATE >= TRUNC(SYSDATE-7)"
				+ " GROUP BY PV_CR_PLAZA, PV_CR_TIENDA, B.STATUS";

		String datosPos = "SELECT B.ID, TO_CHAR (C.CREATED_DATE, 'YYYYMMDDHH24MISS') CREATED_DATE, EXT_REF_NO, SUPPLIER"
				+ " FROM POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C" + " WHERE SUBSTR(B.PV_DOC_NAME,4,10) = '"
				+ 	data.get("plaza") + "'" + " || '" + data.get("tienda") + "'"
				+ " AND B.DOC_TYPE = 'REC' AND STATUS = 'I' AND C.PID_ID = B.ID AND C.PV_CVE_MVT = 10"
				+ " AND C.EXT_REF_NO <> '0' AND C.PARTITION_DATE >= TRUNC(SYSDATE-7) AND C.ORDER_NO = 0";

		String datosPosDETL = "SELECT ITEM, RECEIVED_QTY, NVL(CARTON,0) CARTON, BOL_NO, PV_RETAIL_PRICE, NVL(REMISION,0) REMISION"
				+ " FROM POSUSER.POS_REC_DETL WHERE PID_ID = '%s'";

		String FTP = "SELECT ftp_base_dir, ftp_serverhost, ftp_serverport,"
				+ " ftp_username, ftp_password as ftp_password FROM WMUSER.wm_ftp_connections"
				+ " WHERE ftp_conn_id = 'RTKRMS'";

		String tdcQueryIntegrationServer = "SELECT * FROM wmlog.wm_log_run WHERE interface = 'PR28main'"
				+ " AND start_dt>=TRUNC(SYSDATE) ORDER BY start_dt DESC";

		String tdcQueryErrorId = "SELECT ERROR_ID, RUN_ID, ERROR_DATE, DESCRIPTION FROM WMLOG.WM_LOG_ERROR"
				+ " WHERE RUN_ID = %s"; // FCWMLQA

		String tdcQueryStatusThread = "SELECT parent_id, thread_id, name, wm_log_thread.status, att1, att2"
				+ " FROM WMLOG.wm_log_thread WHERE parent_id = %s"; // FCWMLQA

		String consulta5 = "SELECT RUN_ID, INTERFACE, START_DT, STATUS, SERVER FROM WMLOG.WM_LOG_RUN"
				+ " WHERE RUN_ID = %s";

		String E = "SELECT ID, pe_id, PV_DOC_ID, STATUS, DOC_TYPE, PV_DOC_NAME, TARGET_ID"
				+ " FROM POSUSER.POS_INBOUND_DOCS WHERE ID = '%s' AND STATUS = 'E'";

		String rtk = "SELECT * FROM WMUSER.RTK_INBOUND_DOCS WHERE CREATED_DATE = TRUNC(SYSDATE)" 
				+ " AND CR_PLAZA = '" + data.get("plaza") + "'"
				+ " AND CR_TIENDA = '" + data.get("plaza") + "'"
				+ " AND RUN_ID = '%s'"
				+ " AND DOC_TYPE = 'APO'";
		
		String datosPos2 = "SELECT * FROM (SELECT id, pe_id, pv_doc_id, status, doc_type"
				+ " FROM POSUSER.POS_INBOUND_DOCS WHERE SUBSTR(PV_DOC_NAME,4,10) = '" + data.get("plaza") + "'"
				+ " AND DOC_TYPE = 'APO' AND STATUS = 'E' ORDER BY received_date DESC) WHERE ROWNUM <= 3";
		
	

		
		// 														PASO 1 
		addStep("Tener tiendas disponibles para procesar ");

		SQLResult tienda = dbPos.executeQuery(tiendas);
		System.out.println(tiendas);

		boolean validacionpaso1 = tienda.isEmpty();
		if (validacionpaso1) { //!
			testCase.addTextEvidenceCurrentStep("LA consulta no gener� registros.");
		}
		
		testCase.addQueryEvidenceCurrentStep(tienda);
		assertFalse("no hay insumos", validacionpaso1);

		
		// 														PASO 2 
		
		addStep("Tener datos disponibles para procesar en las tablas de POSUSER");

		SQLResult POS = dbPos.executeQuery(datosPos);
		System.out.println(datosPos);
		String ID = "";

		boolean validacionpaso2 = POS.isEmpty();
		if (!validacionpaso2) {
			ID = POS.getData(0, "ID");
		} else {
			testCase.addTextEvidenceCurrentStep("La consulta no genera registros.");
		}
		
		testCase.addQueryEvidenceCurrentStep(POS);
		 assertFalse("no hay insumos", validacionpaso2);

		
		//														PASO 3
		
		addStep("Verificar que existan detalles de los documentos a procesar en POSUSER");

		String DETL = String.format(datosPosDETL, ID);
		SQLResult DET = dbPos.executeQuery(DETL);
		System.out.println("Respuesta " + DETL);

		boolean validacionpaso3 = DET.isEmpty();
		if (validacionpaso3) { //!
			testCase.addTextEvidenceCurrentStep("La consulta no gener� registros.");			
		}
		
		testCase.addQueryEvidenceCurrentStep(DET);
		
		assertFalse("no hay insumos", validacionpaso3);
		

		//														PASO 4
		
		addStep("Verificar la configuraci�n FTP");

		SQLResult FT = dbPos.executeQuery(FTP);
		System.out.println("Respuesta " + FT);

		boolean validacionpaso4 = FT.isEmpty();
		if (!validacionpaso4) {
			testCase.addQueryEvidenceCurrentStep(FT);
		}
		
		assertFalse("Esta mal configurado el FTP", validacionpaso4);

		
		// 														PASO 5

		addStep("Invocar la interfaz PR28.Pub:run ");
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String status = "S";		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server;
		String searchedStatus = "R";
		String run_id = "";
		
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		SQLResult result5 = executeQuery(dbLog, tdcQueryIntegrationServer);

		String status1 = result5.getData(0, "STATUS");
		run_id = result5.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus); // Valida si se encuentra en estatus R
		while (valuesStatus) {
			result5 = executeQuery(dbLog, tdcQueryIntegrationServer);
			status1 = result5.getData(0, "STATUS");
			run_id = result5.getData(0, "RUN_ID");

			u.hardWait(2);

		}
		
		boolean successRun = status1.equals(status); // Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult result3 = executeQuery(dbLog, error);

			boolean emptyError = result3.isEmpty();
			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontr� un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(result3);
			}
		}

		
		// 														PASO 6
		
		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run deber� existir un registro con el campo status en \"S\".");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String verificacionInterface = String.format(consulta5, run_id);
		SQLResult paso4 = executeQuery(dbLog, verificacionInterface);
		System.out.println(verificacionInterface);

		boolean av5 = paso4.isEmpty();
		if (!av5) {			
			if (!"S".equals(paso4.getData(0, "STATUS"))) {
				testCase.addTextEvidenceCurrentStep("No se encontro registro con STATUS 'S'");
			}			
			
		} else {
			testCase.addTextEvidenceCurrentStep("La consulta no gener� registro.");
		}

		System.out.println(av5);
		testCase.addQueryEvidenceCurrentStep(paso4);
		//assertFalse(av5, "No se obtiene informacion de la consulta");

		
		//														 PASO 7
		
		addStep("Validar que se inserte el detalle de la ejecuci�n de los Threads lanzados por la interface en la tabla WM_LOG_THREAD con STATUS = 'S'");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String consultaTemp6 = String.format(tdcQueryStatusThread, run_id);
		SQLResult paso5 = executeQuery(dbLog, consultaTemp6);
		String thread_id = "";		
		String estatusThread = "";
		
		System.out.println(consultaTemp6);
		
		boolean queryVacia = paso5.isEmpty();
		
		if (!queryVacia) {
			thread_id = paso5.getData(0, "THREAD_ID");
			estatusThread = paso5.getData(0, "Status");
		} else {
			testCase.addTextEvidenceCurrentStep("La consulta no gener� resultados.");
		}	
				
		testCase.addQueryEvidenceCurrentStep(paso5);

		//assertFalse(SR, "No se obtiene informacion de la consulta");

		
		//				 											PASO 8
		
		addStep("Verificar el status de los registos procesados en la tabla POS_INBOUND_DOCS.");
		
		String E1 = String.format(E, ID);
		SQLResult E2 = dbPos.executeQuery(E1);
		System.out.println("Respuesta " + E2);

		boolean pasoE = E2.isEmpty();
		if (pasoE) {
			testCase.addTextEvidenceCurrentStep("La consulta no genero registros.");
		}
		
		testCase.addQueryEvidenceCurrentStep(E2);
		//assertFalse("no hay insumos", pasoE);
		
		
		// 															PASO 9 
		
		addStep("Verificar que los registros fueron insertados en la tabla de retek RTK_INBOUND_DOCS..");
		
		String RTK = String.format(rtk, thread_id);
		SQLResult rtk2 = dbRms.executeQuery(RTK);
		System.out.println("Respuesta " + RTK);

		boolean pasor = rtk2.isEmpty();
		if (pasor) { //!
			testCase.addTextEvidenceCurrentStep("La consulta no genero registros.");
		}
		
		testCase.addQueryEvidenceCurrentStep(rtk2);
		//assertFalse("no hay insumos", pasor);
		
		
		//															PASO 10 
		
		addStep("Ver los archivos procesados en las tablas de POSUSER");
		SQLResult POS2 = dbPos.executeQuery(datosPos2);
		System.out.println("Respuesta " + datosPos2);
		// String ID = POS.getData(0, "ID");
		
		boolean paso22 = POS2.isEmpty();
		if (paso22) {
			testCase.addTextEvidenceCurrentStep("La consulta no genero resultados.");
		}
		
		testCase.addQueryEvidenceCurrentStep(POS2);
	}

	@Override
	public void beforeTest() {		

	}

	@Override
	public String setPrerequisites() {		
		return null;
	}

	@Override
	public String setTestDescription() {		
		return "Verificar procesamiento normal de la interfaz";
	}

	@Override
	public String setTestDesigner() {		
		return null;
	}

	@Override
	public String setTestFullName() {		
		return null;
	}

	@Override
	public String setTestInstanceID() {		
		return null;
	}
}