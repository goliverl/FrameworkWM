package interfaces.pr77_col;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.TimeZone;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PR77_COL_Validar_envio_ANS extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR77_001_COL_Validar_envio_ANS(HashMap<String, String> data) throws Exception {

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

		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbRDM = new SQLUtil(GlobalVariables.DB_HOST_RDM_63, GlobalVariables.DB_USER_RDM_63, GlobalVariables.DB_PASSWORD_RDM_63);

		/**
		 * Variables
		 * ******************************************************************************************
		 * PR77_COL
		 * 
		 */

		String tdcAdapter = " SELECT * FROM wmuser.wm_rdm_connections WHERE retek_cr='%s'  AND status = 'A'";
		
		String tdcFTP = " SELECT ftp_base_dir, ftp_serverhost, ftp_serverport, ftp_username "
				+ "FROM  wmuser.wm_ftp_connections "
				+ "WHERE ftp_conn_id = 'PR50POS'";
		
		String tdcInfo_Pendiente = "SELECT h.CEDIS,h.CRPLAZA, h.TSF_NO, h.BOL_NBR, h.STATUS,h.CREATE_DATE, h.RUTA, h.SHIP_TS, h.GENERADO, h.CR_TIENDA FROM RDM100.xxfc_posterior_embarque h "
				+ "INNER JOIN RDM100.xxfc_posterior_emb_citems d ON (h.tsf_no = d.tsf_no) " + "WHERE h.crplaza ='"
				+ data.get("plaza") + "' " + "AND h.cr_tienda ='" + data.get("tienda") + "' " + "AND "
				+ "h.status = 'S' " + "AND h.wm_status_asn = 'I' " + "and h.FECHA_INSERT >= SYSDATE-24 "
				+ "ORDER BY h.FECHA_INSERT DESC " + "";

		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%PR77_CO%' "
				+ "ORDER BY START_DT DESC) where rownum <=1";

		String consultaTHREAD = " SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD " 
				+ "WHERE PARENT_ID='%s'";
		
		String consultaTHREAD2 = " SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 " 
				+ "FROM WMLOG.WM_LOG_THREAD"
				+ " WHERE PARENT_ID='%s'";
		
		String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";
		
		String consultaERROR2 = " select * from (select description,MESSAGE " 
				+ "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";
		
		String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";

		String consultaE = "SELECT cedis, crplaza, tsf_no, wm_status_asn, wm_doc_name_asn, wm_run_id_asn, wm_sent_date_asn \r\n"
				+ "FROM rdm100.xxfc_posterior_embarque WHERE wm_status_asn = 'E' " 
				+ "AND crplaza ='" + data.get("plaza") + "' AND cr_tienda = '" + data.get("tienda") + "' AND wm_sent_date_asn >= TRUNC(SYSDATE)  " + "AND wm_run_id_asn ='%s'";
		
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
		String searchedStatus = "R";
		String host = GlobalVariables.DB_HOST_RDM_63, user1 = GlobalVariables.DB_USER_RDM_63, ps1 = "RDM63cu.18";
		
		testCase.setProject_Name("Remediaciones SYGNIA (AP1)");		

		boolean conexionExitosa;

//		Paso 1	************************	
		addStep("Validar la configuración del servidor FTP del POS.");
		SQLResult servQueryExe = dbPos.executeQuery(tdcFTP);
		boolean validacionServ = servQueryExe.isEmpty();
		if (!validacionServ) {
			testCase.addQueryEvidenceCurrentStep(servQueryExe);
		}
		System.out.println(validacionServ);
		assertFalse(validacionServ, "No hay insumos a procesar");

/*		Paso 2	************************
		addStep("Validar que exista información pendiente de procesar.");

		System.out.println(tdcInfo_Pendiente);
		SQLResult DETL1 = dbRDM.executeQuery(tdcInfo_Pendiente);
		String CEDIS = DETL1.getData(0, "CEDIS");
		System.out.println(CEDIS);
		boolean paso2 = DETL1.isEmpty();
		if (!paso2) {
			testCase.addQueryEvidenceCurrentStep(DETL1);
		}
		assertFalse("No hay insumos a procesar", paso2); 

//		Paso 3	************************
		addStep("Validar que existan los adapter de conexión en la tabla wm_rdm_connections.");
		String consultaDETL1 = String.format(tdcAdapter, CEDIS);
		System.out.println(consultaDETL1);
		SQLResult servQueryExe1 = dbPos.executeQuery(consultaDETL1);
		
		boolean validacionServ1 = servQueryExe1.isEmpty();
		if (!validacionServ1) {
			testCase.addQueryEvidenceCurrentStep(servQueryExe1);
		}
		System.out.println(validacionServ1);
		assertFalse(validacionServ1, "No hay insumos a procesar");*/

//		Paso 4	************************
		addStep("Ejecutar  el servicio de la interfaz PR77");

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));

		SQLResult is = executeQuery(dbLog, tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");
		String run_id = is.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {

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
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

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

//Paso 5    ************************		
		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run ");
		SQLResult is1 = executeQuery(dbLog, tdcIntegrationServerFormat);

		String fcwS = is1.getData(0, "STATUS");
		boolean validateStatus = fcwS.equals(status);
		System.out.println(validateStatus);
		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");
		SQLResult log = dbLog.executeQuery(tdcIntegrationServerFormat);
		System.out.println("Respuesta " + log);

		boolean log1 = log.isEmpty();

		if (!log1) {

			testCase.addQueryEvidenceCurrentStep(log);
		}

		System.out.println(log1);
		assertFalse("r", log1);

//Paso 6    ************************
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
		
//Paso 7    ************************		
		addStep("Validar que el registro de la tabla xxfc_posterior_embarque en RDM sea actualizado a estatus E.");

		String consultaE2 = String.format(consultaE, run_id);
		SQLResult DETL22 = dbRDM.executeQuery(consultaE2);
		String DOC_NAME = DETL22.getData(0, "WM_DOC_NAME_ASN");
	
		boolean pasoE= DETL22.isEmpty();
		if (!pasoE) {
			testCase.addQueryEvidenceCurrentStep(DETL22);
		}
		assertFalse("No hay insumos a procesar", pasoE); 
		
//Paso 8  **************************
		addStep("Validar el registro del documento enviado en la tabla pos_outbound_docs de POSUSER.");
		String consultaASN= String.format(consultaPOS, DOC_NAME);
		SQLResult  servQueryExeASN= dbPos.executeQuery(consultaASN);
		
		boolean validacionServASN = servQueryExeASN.isEmpty();
		if (!validacionServASN) {
			testCase.addQueryEvidenceCurrentStep(servQueryExeASN);
		}
		System.out.println(validacionServASN);
		assertFalse(validacionServASN, "No hay insumos a procesar");
		 
//Paso 9 **************************
		addStep("Validar la creación del archivo en el servidor FTP del POS.");
		
		FTPUtil ftp = new FTPUtil("10.182.92.13",21,"posuser","posuser");
		String path = "/u01/posuser/FEMSA_OXXO/POS/"+ data.get("plaza") +"/"+ data.get("tienda") +"/working/" + DOC_NAME;
				
		if (ftp.fileExists(path)) {
			System.out.println("Existe");
			testCase.addTextEvidenceCurrentStep(path);
		} else {
			System.out.println("No Existe");
		} 
		
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. Validar que la interfaz PR77_CO genera el documento ASN y lo envía a POS_OUTBOUND_DOCS.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo Automatización";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR77_001_COL_Validar_envio_ANS";
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
