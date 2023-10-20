package interfaces.pr1;


import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class PR1TransferenciaCedis extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_PR1_TransferenciaCedis_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
				GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		String tdcQueryConnections = "SELECT ftp_conn_id,ftp_base_dir,ftp_serverhost,description "
				+ " FROM WMUSER.WM_FTP_CONNECTIONS" + " WHERE ftp_conn_id = 'RTKRMS'";
		

		String tdcQueryConnectionsTSF = "SELECT * from(Select id,PE_ID,STATUS,DOC_TYPE,PV_DOC_NAME,RECEIVED_DATE,target_id "
				+ " FROM posuser.pos_inbound_docs " + " where doc_type IN ('TSF')" + " AND STATUS ='I'"
				+ " and SUBSTR(PV_DOC_NAME,4,10)= '" + data.get("plaza") + data.get("tienda") + "'"
				+ " AND partition_date > SYSDATE - 13" + " ORDER BY RECEIVED_DATE  DESC)where rownum=1";
		

		String tdcQueryPosTSF2 = "SELECT PID_ID,NO_RECORDS,PV_CVE_MVT,SHIP_DATE,PV_CR_FROM_LOC,CEDIS_ID  FROM POSUSER.POS_TSF"
				+ " WHERE PID_ID = '%s'" + " ORDER BY PARTITION_DATE DESC";

		String tdcQueryPosDETLTSF2 = "SELECT * FROM POSUSER.POS_TSF_DETL" + " WHERE PID_ID = '%s'";

		
	
		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,status,att1,att2 "
		        + " FROM wmlog.wm_log_thread "
				+ " WHERE parent_id = '%s'" 
		        + " AND att1 = '" + data.get("plaza") +"'"
		        + " AND att2 = '"+ data.get("tienda") + "'";


		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID='%s'"; // FCWMLQA

		String tdcQueryConnectionsTSF4 = "SELECT id,PE_ID,STATUS,DOC_TYPE,PV_DOC_NAME,RECEIVED_DATE,target_id "
				+ " FROM POSUSER.POS_INBOUND_DOCS" + " WHERE ID =  '%s' AND STATUS = 'E' " + " ORDER BY RECEIVED_DATE DESC";
		
		
		String tdcQueryRetekSendDoc = "SELECT DOC_TYPE,CR_PLAZA,CR_TIENDA,STATUS,RUN_ID,rtk_filename "
				+ " FROM wmuser.rtk_inbound_docs" + " WHERE doc_type = 'TSF' " + " AND cr_plaza = '" + data.get("plaza")
				+ "'" + " AND cr_tienda = '" + data.get("tienda") + "'" + " AND status = 'L' "
				+ " AND rtk_filename = '%s'";
		    
		//Consultas de error
		String tdcIntegrationServerFormat = "select * from "
				+ "(SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl "
				+ "WHERE INTERFACE = 'PR1main' ORDER BY START_DT DESC) "
				+ "where rownum <=1";// WMLOG
		
		String consulta6 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// WMLOG
		String consulta61 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG
		String consulta62 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG
		
		String tdcQueryStatusLog = "SELECT run_id, interface, start_dt, status, server " + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PR1main'and start_dt >= trunc(sysdate) "
				+ "and rownum = 1" // FCWMLQA
				+ " ORDER BY start_dt DESC ";

		
		//Utileria
		String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		
		
		/*
		 * Pasos
		 *********************************************************************************/

///										Paso 1 ************************************************
		addStep("Validar la configuración del servidor FTP en la tabla WM_FTP_CONNECTIONS.");

		System.out.println(tdcQueryConnections);

		SQLResult conFTP = executeQuery(dbPuser, tdcQueryConnections);
		
		boolean connId = conFTP.isEmpty();

		System.out.println(connId);
		
		if (!connId) {

			testCase.addQueryEvidenceCurrentStep(conFTP);

		}else {
			
			testCase.addQueryEvidenceCurrentStep(conFTP);
			testCase.addBoldTextEvidenceCurrentStep("No se valido la configuración del servidor FTP en la tabla WM_FTP_CONNECTIONS.");
			
		}

	//	assertFalse(connId, "No se encontró la configuración del servidor");
		

		/// Paso 2****************************************************

		addStep("Deben existir documentos TSF pendientes de procesar en la tabla POS_INBOUND_DOCS de POSUSER.");

		System.out.println(tdcQueryConnectionsTSF);

		SQLResult penTSF = executeQuery(dbPuser, tdcQueryConnectionsTSF);
		
		String id = "0";
		
		boolean docTsf = penTSF.isEmpty();
		
		System.out.println(docTsf);

		if (!docTsf) {

			testCase.addQueryEvidenceCurrentStep(penTSF);
			id = penTSF.getData(0, "ID"); //obtenemos el ID de POS_INBOUND_DOCS

		}else {
			
			testCase.addQueryEvidenceCurrentStep(penTSF);
			testCase.addBoldTextEvidenceCurrentStep("No se encontraron documentos TSF pendientes de procesar en la tabla POS_INBOUND_DOCS de POSUSER.");
			
		}

	//	assertFalse(docTsf, "No existen documentos pendientes de procesar");
		

/// 								Paso 3****************************************************

		addStep("Validar que se muestre el registro relacionado con el archivo TSF en la tabla POS_TSF de POSUSER.");

		

		String queryPosTSF = String.format(tdcQueryPosTSF2, id);
		System.out.println(queryPosTSF);

		SQLResult pid_id = executeQuery(dbPuser, queryPosTSF);
		
		boolean posTsf = pid_id.isEmpty();
		
		System.out.println(posTsf);

		if (!posTsf) {

			testCase.addQueryEvidenceCurrentStep(pid_id);

		}
            else {
			
			testCase.addQueryEvidenceCurrentStep(pid_id);
			testCase.addBoldTextEvidenceCurrentStep("No se muestra el registro relacionado con el archivo TSF en la tabla POS_TSF de POSUSER.");
			
		}

	//	assertFalse(posTsf, "No hay registro relacionado con el documento TSF");
		

/// 								Paso 4****************************************************

		addStep("Validar que se encuentre el detalle de la transferencia en la tabla POS_TSF_DETL de POSUSER.");

		
		String queryPosDetlTSF = String.format(tdcQueryPosDETLTSF2, id);
		
		System.out.println(queryPosDetlTSF);
		
		SQLResult detlTSF = executeQuery(dbPuser, queryPosDetlTSF);
		
		boolean posDetl = detlTSF.isEmpty();
		
		System.out.println(posDetl);
		
		if (!posDetl) {

			testCase.addQueryEvidenceCurrentStep(detlTSF);

		}
           else {
			
			testCase.addQueryEvidenceCurrentStep(detlTSF);
			testCase.addBoldTextEvidenceCurrentStep("No se muestra el registro de la transferencia en la tabla POS_TSF_DETL de POSUSER.");
			
		}

		
	//	assertFalse(posDetl, "No hay detalle de la transferencia");
				

///								 Paso 5***************************************************

		addStep("Ejecutar el servicio PR1.Pub:run.");
		
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		String dateExecution = pok.runIntefaceWmTwoButtonsWihtoutInputs10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);
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

			String error = String.format(consulta6, run_id);
			String error1 = String.format(consulta61, run_id);
			String error2 = String.format(consulta62, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontro un error en la ejecucutar la interfaz en la tabla WM_LOG_ERROR");

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

		
		
/// 								Paso 6***************************************************** 

		addStep("Validar que el registro de ejecución de la interfaz termino en estatus 'S' en la tabla WM_LOG_RUN.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
	
		SQLResult statusQueryExe = dbLog.executeQuery(tdcQueryStatusLog);
		
		String fcwS = statusQueryExe.getData(0, "STATUS");
		
		boolean validateStatus = status.equals(fcwS);
		
		System.out.println(validateStatus);

		boolean validacionQueryStatus = statusQueryExe.isEmpty();
		
		if (!validacionQueryStatus) {
			
			testCase.addQueryEvidenceCurrentStep(statusQueryExe);
			
		} else {
			
			testCase.addQueryEvidenceCurrentStep(statusQueryExe);
			testCase.addBoldTextEvidenceCurrentStep("La interfaz no termino en estatus 'S' en la tabla WM_LOG_RUN");
			
		}
		
		System.out.println(validacionQueryStatus);

	//	assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");

//										Paso 7******************************************************

		addStep("Validar que el registro de ejecución de la plaza y tienda terminó en estatus 'S' en la tabla WM_LOG_THREAD.");

		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(tdcQueryStatusThread);
		SQLResult queryStatusThreadResult = dbLog.executeQuery(queryStatusThread);
		
		boolean statusThread = queryStatusThreadResult.isEmpty();
		System.out.println(statusThread);
		if (!statusThread) {

			testCase.addQueryEvidenceCurrentStep(queryStatusThreadResult);
			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult errorResult = dbLog.executeQuery(error);

			boolean emptyError = errorResult.isEmpty();
			System.out.println(emptyError);

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(errorResult);

			}
		}

	//	assertFalse(statusThread, "El registro de ejecución de la plaza y tienda no fue exitoso");

/// 								Paso 8****************************************************

		addStep("Validar nuevamente la tabla POS_INBOUND_DOCS de POSUSER, donde los registros cambien a estatus E.");

		
		String queryConnectionsTSF = String.format(tdcQueryConnectionsTSF4, id);
		
		System.out.println(queryConnectionsTSF);

		SQLResult reg = dbPuser.executeQuery(queryConnectionsTSF);
		
		String target_id = "null";
			
		boolean validateStatus3 = reg.isEmpty();
		
		System.out.println(validateStatus3);

		if (!validateStatus3) {

			testCase.addQueryEvidenceCurrentStep(reg);
			target_id = reg.getData(0, "TARGET_ID");
			

		}
           else {
			
			testCase.addQueryEvidenceCurrentStep(reg);
			testCase.addBoldTextEvidenceCurrentStep("Los registros no cambiaron a E en la tabla POS_INBOUND_DOCS de POSUSER");
			
		}

		
	//assertFalse(validateStatus3, "Los registros no cambiaron a estatus E");

		/// Paso 9********************************************************
		addStep("Validar el registro del documento enviado al servidor FTP de RETEK en la tabla RTK_INBOUND_DOCS.");

		String queryRetekSendDoc = String.format(tdcQueryRetekSendDoc, target_id);

		System.out.println(queryRetekSendDoc);
		
		SQLResult retek = dbRms.executeQuery(queryRetekSendDoc);
		
		boolean senDoc = retek.isEmpty() ;
		
		System.out.println(senDoc);

		if (!senDoc) {

			testCase.addQueryEvidenceCurrentStep(retek);

		}
          else {
			
			testCase.addQueryEvidenceCurrentStep(retek);
			testCase.addBoldTextEvidenceCurrentStep("No se valido el registro del documento enviado al servidor FTP");
			
		}

		
   //	assertFalse( senDoc, "No se registro el envío del documento al servidor FTP");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "PR1 Transferencia Cedis";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_005_PR1_TransferenciaCedis_test";
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
