package interfaces.pb6;

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

public class PB6_Ejec_Normal_Plaza extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PB6_001_Ejec_Normal_Plaza(HashMap<String, String> data) throws Exception {

		/**
		 * @category pruebas
		 * @param data
		 * @throws Exception
		 * @description :
		 * @dateup febr-2021
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
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Ebs, GlobalVariables.DB_USER_Ebs,
				GlobalVariables.DB_PASSWORD_Ebs);
		utils.sql.SQLUtil dbBI = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_BI, GlobalVariables.DB_USER_BI,
				GlobalVariables.DB_PASSWORD_BI);
		/**
		 * Variables
		 * ******************************************************************************************
		 * pb6
		 * 
		 */
		String tdcRVP = "SELECT A.ID, A.PE_ID, A.PV_DOC_ID, A.status,A.PV_DOC_NAME, A.TARGET_ID, A.TARGET_ALT_ID, A.PARTITION_DATE "
				+ "FROM posuser.POS_INBOUND_DOCS A, posuser.POS_RVP B, posuser.POS_RVP_DETL C  "
				+ "WHERE STATUS = 'I'  " + "AND DOC_TYPE = 'RVP'  " + "AND A.ID = B.PID_ID  " + "AND A.ID = C.PID_ID "
				+ "AND SUBSTR(PV_DOC_NAME, 4,5) ='" + data.get("plaza") + "'";

		// '" + data.get("plaza") + "'

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server,"
				+ " (END_DT - START_DT)*24*60 FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE LIKE '%PB6%' ORDER BY START_DT DESC) "
				+ "where rownum <=1";// WMLOG

		String tdcQueryStatusLog = "SELECT run_id,interface,start_dt,status,server " + " FROM WMLOG.wm_log_run"
				+ " WHERE interface like '%PB6%'and start_dt >= trunc(sysdate) " // FCWMLQA
				+ " ORDER BY start_dt DESC";

		String tdcQueryWmlogError = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// WMLOG
		String tdcQueryWmlogError1 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG
		String tdcQueryWmlogError2 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG
		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = '%s'"; // FCWMLQA

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID= '%s' "; // FCWMLQA

		String tdcRVPE = "SELECT SUBSTR(PV_DOC_NAME, 4,5) PV_CR_PLAZA, STATUS, TARGET_ID, DOC_TYPE "
				+ "FROM posuser.POS_INBOUND_DOCS \r\n" + "WHERE DOC_TYPE = 'RVP'  " + "AND STATUS = 'E'  "
				+ "AND SUBSTR(PV_DOC_NAME, 4,5) ='" + data.get("plaza") + "' " + " AND TARGET_ID ='%s'"; // [WM_LOG_THREAD.THREAD_ID]

		String NSP = "SELECT ID_REGISTRO, ID_TIENDA, LOADDATE,WM_DOC_NAME,WM_RUN_ID "
				+ "FROM BIODSMKT.NSP_REG_VISITAS_PROV  " + "WHERE SUBSTR(WM_DOC_NAME, 4,5) ='" + data.get("plaza")
				+ "' " + " AND TRUNC(LOADDATE) = TRUNC(SYSDATE) " + " AND WM_RUN_ID ='%s' ";// [WM_LOG_THREAD.THREAD_ID];

		String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		// String con = "http://" + user + ":" + ps + "@" + server;
		String searchedStatus = "R";

//			Paso 1	************************		

		addStep("Que exista al menos un registro con STATUS = I y DOC_TYPE = NSP  en las tablas POS_INBOUND_DOCS.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(tdcRVP); // get shipment
		SQLResult servQueryExe = dbPos.executeQuery(tdcRVP);
		// String id = servQueryExe.getData(0, "ID");
		boolean validacionServ = servQueryExe.isEmpty();
		if (!validacionServ) {
			testCase.addQueryEvidenceCurrentStep(servQueryExe);
		}
		System.out.println(validacionServ);

		assertFalse(validacionServ, "No hay insumos a procesar");

		addStep("Ejecutar  el servicio de la interfaz PB6");
		// utileria

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
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

			String error = String.format(tdcQueryWmlogError, run_id);
			String error1 = String.format(tdcQueryWmlogError1, run_id);
			String error2 = String.format(tdcQueryWmlogError2, run_id);

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

//							Paso 4	************************

		addStep("Comprobar que se registra la ejecucion en WMLOG");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		// String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
		// System.out.println(queryStatusLog);

		SQLResult statusQueryExe = dbLog.executeQuery(tdcQueryStatusLog);
		System.out.println("status "+statusQueryExe);
		String fcwS = statusQueryExe.getData(0, "STATUS");
		boolean validateStatus = status.equals(fcwS);
		System.out.println(validateStatus);

		boolean validacionQueryStatus = statusQueryExe.isEmpty();
		if (!validacionQueryStatus) {
			testCase.addQueryEvidenceCurrentStep(statusQueryExe);
		}
		System.out.println(validacionQueryStatus);

		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");

//				 		Paso 5	************************

		addStep("Se valida la generacion de thread");

		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);

		SQLResult regPlazaTiendaThread = dbLog.executeQuery(queryStatusThread);
		String thread_id = regPlazaTiendaThread.getData(0, "THREAD_ID");
		String Threadstatus = regPlazaTiendaThread.getData(0, "STATUS");

		boolean threadResult = regPlazaTiendaThread.isEmpty();
		if (!threadResult) {
			testCase.addQueryEvidenceCurrentStep(regPlazaTiendaThread);
		}
//							
//							testCase.addQueryEvidenceCurrentStep(regPlazaTiendaThread);
//						}
//						
		boolean statusThread = status.equals(Threadstatus);
		System.out.println(statusThread);
		if (!statusThread) {

			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult errorQueryExe = dbLog.executeQuery(error);

			boolean emptyError = errorQueryExe.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(regPlazaTiendaThread);

			} else {

				testCase.addQueryEvidenceCurrentStep(regPlazaTiendaThread);

			}
		}
		u.close();

		addStep("Se valida el STATUS igual a E en la tabla POS_INBOUND_DOCS de la BD POSUSER.");

		System.out.println(GlobalVariables.DB_HOST_Puser);

		System.out.println(tdcRVPE);
		String id1 = String.format(tdcRVPE, thread_id);
		SQLResult adjQuery = dbPos.executeQuery(id1);

		boolean adj = adjQuery.isEmpty();

		if (!adj) {

			testCase.addQueryEvidenceCurrentStep(adjQuery);

		}

		System.out.println(adj);

		addStep("Se valida que los datos se hayan insertado en la tabla NSP_REG_VISITAS_PROV de la BD BI");

		System.out.println(GlobalVariables.DB_HOST_BI);
		System.out.println(GlobalVariables.DB_USER_BI);
		System.out.println(GlobalVariables.DB_HOST_BI);

		String thr = String.format(NSP, thread_id);
		System.out.println(thr);
		SQLResult retekQuery = dbBI.executeQuery(thr);

		boolean retek = retekQuery.isEmpty();

		if (!retek) {

			testCase.addQueryEvidenceCurrentStep(retekQuery);

		}

		System.out.println(retek);

		assertFalse(retek, "No se encuentran registros en la tabla BI");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada.EJECUCION NORMAL DE LA INTERFAZ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PB6_001_Ejec_Normal_Plaza";
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
