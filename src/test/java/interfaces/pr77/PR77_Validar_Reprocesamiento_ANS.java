package interfaces.pr77;

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

public class PR77_Validar_Reprocesamiento_ANS extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR77_003_Validar_Reprocesamiento_ANS(HashMap<String, String> data) throws Exception {

		/**
		 * @category pruebas
		 * @param data
		 * @throws Exception
		 * @description :
		 * @dateup
		 */

		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
				GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,
				GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbRDM = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RDM, GlobalVariables.DB_USER_RDM,
				GlobalVariables.DB_PASSWORD_RDM);

		/**
		 * Variables
		 * ******************************************************************************************
		 * PR77
		 * 
		 */

		String tdcAdapter = " SELECT * FROM wmuser.wm_rdm_connections WHERE retek_cr='%s'  AND status = 'A'";
		String tdcFTP = " SELECT ftp_base_dir, ftp_serverhost, ftp_serverport, ftp_username, ftp_password FROM  wmuser.wm_ftp_connections WHERE ftp_conn_id = 'PR50POS'";
		String tdcInfo_Pendiente = "SELECT h.CEDIS,h.CRPLAZA, h.TSF_NO, h.BOL_NBR, h.STATUS,h.CREATE_DATE, h.RUTA, h.SHIP_TS, h.GENERADO, h.CR_TIENDA FROM RDM100.xxfc_posterior_embarque h "
				+ "INNER JOIN RDM100.xxfc_posterior_emb_citems d ON (h.tsf_no = d.tsf_no) " + "WHERE h.crplaza ='"
				+ data.get("plaza") + "' " + "AND h.cr_tienda ='" + data.get("tienda") + "' " + "AND "
				+ "h.status = 'S' " + "AND h.wm_status_asn = 'F' " + "and h.FECHA_INSERT >= SYSDATE-24 "
				+ "ORDER BY h.FECHA_INSERT DESC " + "";

		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%PR77%' "
				+ "ORDER BY START_DT DESC) where rownum <=1";

		String consultaTHREAD = " SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD " + "WHERE PARENT_ID='%s'";
		String consultaTHREAD2 = " SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 " + "FROM WMLOG.WM_LOG_THREAD"
				+ " WHERE PARENT_ID='%s'";
		String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";
		String consultaERROR2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";
		String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";

		String consultaE = "SELECT cedis, crplaza, tsf_no, wm_status_asn, wm_doc_name_asn, wm_run_id_asn, wm_sent_date_asn \r\n"
				+ "FROM rdm100.xxfc_posterior_embarque WHERE wm_status_asn = 'E' " + "AND crplaza ='" + data.get("plaza") + "' AND cr_tienda = '" + data.get("tienda") + "' AND wm_sent_date_asn >= TRUNC(SYSDATE)  " + "AND wm_run_id_asn ='%s'";
		
		String consultaPOS="SELECT  ID, DOC_NAME,STATUS FROM posuser.pos_outbound_docs " + 
				"WHERE doc_name = '%s'" + 
				"AND doc_type = 'ASN' " + 
				"AND pv_cr_plaza ='" + data.get("plaza") + "'" + 
				"AND pv_cr_tienda = '" + data.get("tienda") + "'" + 
				"AND status = 'L'";

		String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		// String con = "http://" + user + ":" + ps + "@" + server;
		String searchedStatus = "R";

//		Paso 1	************************		

		addStep("Validar la configuración del servidor FTP del POS.");
		SQLResult servQueryExe = dbPos.executeQuery(tdcFTP);
		// String id = servQueryExe.getData(0, "ID");
		boolean validacionServ = servQueryExe.isEmpty();
		if (!validacionServ) {
			testCase.addQueryEvidenceCurrentStep(servQueryExe);
		}
		System.out.println(validacionServ);
		assertFalse(validacionServ, "No hay insumos a procesar");

		addStep("Validar que exista información pendiente de procesar.");

		// String consultaDETL1 = String.format(consDTL, id);
		System.out.println(tdcInfo_Pendiente);
		SQLResult DETL1 = dbRDM.executeQuery(tdcInfo_Pendiente);
		String CEDIS = DETL1.getData(0, "CEDIS");
		System.out.println(CEDIS);
		boolean paso2 = DETL1.isEmpty();
		if (!paso2) {
			testCase.addQueryEvidenceCurrentStep(DETL1);
		}
		assertFalse("No hay insumos a procesar", paso2);

		addStep("Validar que existan los adapter de conexión en la tabla wm_rdm_connections.");
		String consultaDETL1 = String.format(tdcAdapter, CEDIS);
		System.out.println(consultaDETL1);
		SQLResult servQueryExe1 = dbPos.executeQuery(consultaDETL1);
		
		// String id = servQueryExe.getData(0, "ID");
		boolean validacionServ1 = servQueryExe1.isEmpty();
		if (!validacionServ1) {
			testCase.addQueryEvidenceCurrentStep(servQueryExe1);
		}
		System.out.println(validacionServ1);
		assertFalse(validacionServ1, "No hay insumos a procesar");
		
		addStep("Ejecutar  el servicio de la interfaz PR77");

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		// String dateExecution = null;
		// String dateExecution ="";

		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

		SQLResult is = executeQuery(dbLog, tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");
		String run_id = is.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			// is = SQLUtil.getVaribleDataIntegrationServer(testCase, dbLog,
			// tdcIntegrationServerFormat, "STATUS", "RUN_ID");
			is = executeQuery(dbLog, tdcIntegrationServerFormat);

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(consultaERROR, run_id);
			String error1 = String.format(consultaERROR2, run_id);
			String error2 = String.format(consultaERROR3, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontr? un error en la ejecuci?n de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorr);

			}

			SQLResult errorIS = dbLog.executeQuery(error1);
			boolean emptyError1 = errorIS.isEmpty();
			if (!emptyError1) {
				testCase.addQueryEvidenceCurrentStep(errorIS);
			}

			SQLResult errorIS2 = dbLog.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();

			if (!emptyError2) {

				testCase.addQueryEvidenceCurrentStep(errorIS2);

			}

		}

//Paso 3    ************************		
		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run ");
		SQLResult is1 = executeQuery(dbLog, tdcIntegrationServerFormat);

		String fcwS = is1.getData(0, "STATUS");
		// String fcwS = SQLUtil.getColumn(testCase, dbLog, tdcIntegrationServerFormat,
		// "STATUS");
		boolean validateStatus = fcwS.equals(status);
		System.out.println(validateStatus);
		assertTrue(validateStatus, "La ejecuci?n de la interfaz no fue exitosa");
		SQLResult log = dbLog.executeQuery(tdcIntegrationServerFormat);
		System.out.println("Respuesta " + log);
		// SQLResult errorIS= dbLog.executeQuery(error1);

		boolean log1 = log.isEmpty();
		// boolean av2 = SQLUtil.isEmptyQuery(testCase, dbLog,
		// tdcIntegrationServerFormat);
		if (!log1) {

			testCase.addQueryEvidenceCurrentStep(log);
		}

		System.out.println(log1);
		assertFalse("r", log1);

		addStep("Validar que el registro de ejecución  en la tabla WM_LOG_THREAD.");

		String consultaTH = String.format(consultaTHREAD, run_id);
		SQLResult THRE = dbLog.executeQuery(consultaTH);

		boolean paso1TH = THRE.isEmpty();
		if (!paso1TH) {
			testCase.addQueryEvidenceCurrentStep(THRE);
		}
		assertFalse("No hay insumos a procesar", paso1TH);

		// .-----------Segunda consulta

		String consultaTH2 = String.format(consultaTHREAD2, run_id);
		SQLResult THRE2 = dbLog.executeQuery(consultaTH2);

		boolean paso1TH2 = THRE.isEmpty();
		if (!paso1TH2) {
			testCase.addQueryEvidenceCurrentStep(THRE2);
		}
		assertFalse("No hay insumos a procesar", paso1TH2);
		
		
		addStep("Validar que el registro de la tabla xxfc_posterior_embarque en RDM sea actualizado a estatus E.");

		// String consultaDETL1 = String.format(consDTL, id);
		String consultaE2 = String.format(consultaE, run_id);
		SQLResult DETL22 = dbRDM.executeQuery(consultaE2);
		String DOC_NAME = DETL22.getData(0, "WM_DOC_NAME_ASN");
	
		boolean pasoE= DETL22.isEmpty();
		if (!pasoE) {
			testCase.addQueryEvidenceCurrentStep(DETL22);
		}
		assertFalse("No hay insumos a procesar", pasoE);
		
		addStep("Validar el registro del documento enviado en la tabla pos_outbound_docs de POSUSER.");
		String consultaASN= String.format(consultaPOS, DOC_NAME);
		SQLResult  servQueryExeASN= dbPos.executeQuery(consultaASN);
		// String id = servQueryExe.getData(0, "ID");
		boolean validacionServASN = servQueryExeASN.isEmpty();
		if (!validacionServASN) {
			testCase.addQueryEvidenceCurrentStep(servQueryExeASN);
		}
		System.out.println(validacionServASN);
		assertFalse(validacionServASN, "No hay insumos a procesar");
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. EJECUCION NORMAL DE LA INTERFAZ PR77";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR77_003_Validar_Reprocesamiento_ANS";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}
}