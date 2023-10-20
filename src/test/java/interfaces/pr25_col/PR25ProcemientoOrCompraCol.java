package interfaces.pr25_col;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class PR25ProcemientoOrCompraCol extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR25_COL_001_Procemiento_Or_Compra_Col(HashMap<String, String> data) throws Exception {
/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_COL, GlobalVariables.DB_USER_RMS_COL, GlobalVariables.DB_PASSWORD_RMS_COL);
		utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,	GlobalVariables.DB_PASSWORD_Puser);
/** VARIABLES *********************************************************************/	

		testCase.setProject_Name("Remediaciones SYGNIA (AP1)");    

		String tdcQueryFTPserv = "SELECT ftp_base_dir, ftp_serverhost, ftp_serverport, ftp_username "
				+ " FROM wmuser.wm_ftp_connections" + " WHERE ftp_conn_id = 'PR50POS'"; // FCWMQA
		
		String tdcQueryOrder = "select* from( SELECT a.load_batch_id,a.order_number,a.wm_status,location  \r\n"
				+ " FROM WMUSER.ordenes_control a, WMUSER.ordenes_head b\r\n"
				+ " WHERE a.load_batch_id = b.load_batch_id AND a.order_number = b.order_number AND b.external_order_num \r\n"
				+ " IS NULL AND a.wm_status = 'L'order by a.load_date desc)  where rownum <= 3";

		String tdcQueryStatusLog = "select * from (SELECT run_id,interface,start_dt,status,server "
				+ " FROM WMLOG.wm_log_run" + " WHERE interface = 'PR25_COmain'"
				+ " and run_id= '%s' " + " and start_dt >= trunc(sysdate) " // FCWMLQA
				+ " ORDER BY start_dt DESC ) where rownum <=3";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PR25_COmain'"
				+ " and start_dt >= trunc(sysdate)" + " order by start_dt desc)" + " where rownum = 1";

		String tdcQueryStatusThread = "select * from (SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = %s ) where rownum <=3 "; // FCWMLQA

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA

		String tdcQueryDocPosOutbound = "SELECT ID,doc_name,doc_type,PV_CR_PLAZA,PV_CR_TIenda,status,sent_date "
				+ " FROM  POSUSER.pos_outbound_docs" + " WHERE doc_type = 'POD' " 
				+ " AND TRUNC(sent_date) = TRUNC(SYSDATE) " 
				+ " AND status = 'L' AND pv_cr_plaza = '" + data.get("plaza") + "'" + " AND pv_cr_tienda = '" + data.get("tienda") + "' "
				+ " order by sent_date desc ";
		
		String tdcQueryUpdateStatus = "select* from( SELECT a.load_batch_id,a.order_number,a.wm_status,location  \r\n"
				+ " FROM WMUSER.ordenes_control a, WMUSER.ordenes_head b\r\n"
				+ " WHERE a.load_batch_id = b.load_batch_id AND a.order_number = b.order_number AND b.external_order_num \r\n"
				+ " IS NULL AND a.wm_status = 'E'order by a.load_date desc)  where rownum <= 3";
		
		String verifyFile = " SELECT ID, doc_type, status, DOC_NAME,PV_CR_PLAZA,PV_CR_TIENDA,DATE_CREATED "
				+ " FROM POSUSER.POS_OUTBOUND_DOCS " 
				+ " WHERE PV_CR_PLAZA = '" + data.get("plaza") + "' "
				+ " AND PV_CR_TIENDA = '" + data.get("tienda") + "' "
				+ " AND TRUNC(SENT_DATE) = TRUNC(SYSDATE) order by sent_date desc ";

/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
		
		/* PASO 1 *********************************************************************/
		
		addStep("Validar la configuración del servidor FTP del POS");

		System.out.println(GlobalVariables.DB_HOST_Puser);

		System.out.println(tdcQueryFTPserv);

		SQLResult tdcQueryFTPservRes = dbPos.executeQuery(tdcQueryFTPserv);
		boolean serv = tdcQueryFTPservRes.isEmpty();

		if (!serv) {
			testCase.addQueryEvidenceCurrentStep(tdcQueryFTPservRes);
		}

		System.out.println(serv);
		assertFalse(serv, "No se tiene conexion al servidor FTP");


		/* Paso 2 *********************************************************************/

		
		addStep("Validar que existan ordenes de compra pendientes de procesar para la Tienda.");

		System.out.println(GlobalVariables.DB_HOST_RMS_COL);
		System.out.println(tdcQueryOrder);

		SQLResult tdcQueryOrderRes = dbRms.executeQuery(tdcQueryOrder);
		boolean or = tdcQueryOrderRes.isEmpty();
		if (!or) {
			testCase.addQueryEvidenceCurrentStep(tdcQueryOrderRes);
		}

		System.out.println(or);
		assertFalse(or, "no se muestran registros para la ejecucion");

		/* Paso 3 *********************************************************************/
		
		addStep("Ejecutar la interfaz PR25 COL");

		String status = "S";
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server;
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		u.hardWait(4);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));

		System.out.println(dateExecution);

		String tdcIntegrationServerFormat = String.format(tdcQueryIntegrationServer, dateExecution);
		System.out.println("Respuesta dateExecution" + tdcIntegrationServerFormat);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la ejecución

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		
		while (valuesStatus) {			
			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);
			u.hardWait(2);
		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S

		if (!successRun) {

			String error = String.format(tdcQueryErrorId, run_id);

			SQLResult errorResult = dbLog.executeQuery(error);
			boolean emptyError = errorResult.isEmpty();

			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(errorResult);
			}
		}


		// Paso 4 *********************************************************************/

		addStep("Comprobar que se registra la ejecucion en WMLOG");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
		SQLResult regPlazaTiendaRes1 = executeQuery(dbLog, queryStatusLog);
		System.out.println(queryStatusLog);
		String fcwS = regPlazaTiendaRes1.getData(0, "STATUS");		

		boolean validateStatus = status.equals(fcwS);
		System.out.println(validateStatus);
		if (validateStatus== true) {
			testCase.addQueryEvidenceCurrentStep(regPlazaTiendaRes1);
		}
		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");

		// Paso 5 *********************************************************************/

		addStep("Se valida la generacion de thread");

		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);

		String regPlazaTienda = String.format(queryStatusThread, "STATUS", "THREAD_ID");

		SQLResult regPlazaTiendaRes = executeQuery(dbLog, regPlazaTienda);
		String statusThread = "";
		String thread_id = "";
		
		if (!regPlazaTiendaRes.isEmpty()) {
			statusThread = regPlazaTiendaRes.getData(0, "STATUS");
			thread_id = regPlazaTiendaRes.getData(0, "THREAD_ID");
		}

		boolean validaStatusThread = status.equals(statusThread);
		
		if (validaStatusThread== true) {
			statusThread = regPlazaTiendaRes.getData(0, "STATUS");
			testCase.addQueryEvidenceCurrentStep(regPlazaTiendaRes);
		}

		System.out.println(thread_id);
		System.out.println(statusThread);
		
		if (!validaStatusThread) {
			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult errorRes = executeQuery(dbLog, error);
			boolean emptyError = errorRes.isEmpty();

			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(errorRes);
			}
		}

		assertTrue(validaStatusThread, "El registro de ejecución de la plaza y tienda no fue exitoso");
		
		// Paso 6 *********************************************************************/

		addStep("Validar el registro del documento enviado al servidor FTP del POS en la tabla pos_outbound_docs de POSUSER.");
		System.out.println(tdcQueryDocPosOutbound);
		SQLResult sendDocRes = dbPos.executeQuery(tdcQueryDocPosOutbound);
		System.out.println(sendDocRes);
		boolean sendDoc = sendDocRes.isEmpty();
		System.out.println(sendDoc);
		System.out.println(tdcQueryDocPosOutbound);
		if (!sendDoc) {
			testCase.addQueryEvidenceCurrentStep(sendDocRes);
		}

		assertFalse(sendDoc, "No se muestran Registros");

		/* Paso 7 *********************************************************************/
		
		addStep("Validar la actualización del estatus de la ordern a 'E' en la tabla ordenes_control de RETEK.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		SQLResult querySatusLogRes = dbRms.executeQuery(tdcQueryUpdateStatus);
		boolean upOrder = querySatusLogRes.isEmpty();

		if (!upOrder) {
			testCase.addQueryEvidenceCurrentStep(querySatusLogRes);
		}
		System.out.println(upOrder);
		assertFalse(upOrder, "no se encuentran oredenes actualizadas");
		
		/* Paso 8 *********************************************************************/		

		addStep("Validar que se inserte el registro del documento procesado en la tabla POS_OUTBOUND_DOCS de POSUSER.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		SQLResult paso6 = executeQuery(dbPuser, verifyFile);
		System.out.println(verifyFile);
        String doc_name = null;
        
		boolean step6 = paso6.isEmpty();
		
		if (!step6) {
			doc_name = paso6.getData(0, "DOC_NAME");
			testCase.addQueryEvidenceCurrentStep(paso6);
		}

		System.out.println(step6);
		assertFalse(step6, "No se obtiene informacion de la consulta en la tabla POS_OUTBOUND_DOCS de POSUSER.");
		
		Thread.sleep(15000);

		FTPUtil ftp = new FTPUtil("10.182.92.13",21,"posuser","posuser");
		String path = "/FEMSA_OXXO/POS/"+ data.get("plaza") +"/"+ data.get("tienda") +"/working/" + doc_name;
				
		if (ftp.fileExists(path)) {
			System.out.println("Existe");
			testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: "+path);
		} else {
			System.out.println("No Existe");
		}
		
		assertFalse(!ftp.fileExists(path), "No se obtiene el archivo por FTP con la ruta " + path);
		
		
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. La interface PR25 Col envía los datos empresariales relacionados con Órdenes de Compra generadas por Retek (RMS COL) a los sistemas POS.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatización";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR25_COL_001_Procemiento_Or_Compra_Col";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
}
