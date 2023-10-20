package interfaces.os2;

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

public class OS2_Ejec_Interfaz_Con_Objetivo extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_OS2_Ejec_Interfaz_Con_Objetivo(HashMap<String, String> data) throws Exception {

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
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Ebs, GlobalVariables.DB_USER_Ebs,
				GlobalVariables.DB_PASSWORD_Ebs);
		utils.sql.SQLUtil dbBI = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_BI, GlobalVariables.DB_USER_BI,
				GlobalVariables.DB_PASSWORD_BI);
		/**
		 * Variables
		 * ******************************************************************************************
		 * OS2 Nota el objetivo se cambia en el excel
		 */

		String dtsProc = "SELECT DISTINCT OBJ.OBJETIVO_ID, OBJ.ETIQUETA_OBJETIVO, EF.ORACLE_CR_SUPERIOR, EF.ORDEN , OBJ.ETIQUETA_QUERY\r\n"
				+ "FROM XXFH.XXFH_OBJETIVO OBJ, XXFH.XXFH_EF_OBJETIVO EF, XXFC_MAESTRO_DE_CRS_V MCR  "
				+ "WHERE OBJ.ETIQUETA_OBJETIVO = '" + data.get("objetivo") + "'  " + "AND EF.ORACLE_CR_SUPERIOR='"
				+ data.get("plaza") + "'  " + "AND MCR.ORACLE_CR = '" + data.get("tienda") + "'  "
				+ "AND OBJ.ESTATUS = '1'  " + "AND EF.OBJETIVO_ID = OBJ.OBJETIVO_ID "
				+ "AND EF.ORACLE_CR_SUPERIOR=MCR.ORACLE_CR_SUPERIOR   " + "AND EF.ESTATUS = '1' ORDER BY EF.ORDEN ";
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server,"
				+ " (END_DT - START_DT)*24*60 FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE LIKE '%OS2%' ORDER BY START_DT DESC) "
				+ "where rownum <=1";// WMLOG
		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = '%s'"; // FCWMLQA
		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID= '%s' "; // FCWMLQA
		String conError = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// WMLOG
		String conError11 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG
		String conError2 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG

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

		addStep("Validar datos a procesar en ORAFIN ");

		System.out.println(dtsProc); // get shipment
		SQLResult servQueryExe = dbEbs.executeQuery(dtsProc);
		// String id = servQueryExe.getData(0, "ID");
		boolean validacionServ = servQueryExe.isEmpty();
		if (!validacionServ) {
			testCase.addQueryEvidenceCurrentStep(servQueryExe);
		}
		System.out.println(validacionServ);
		assertFalse(validacionServ, "No hay insumos a procesar");

		addStep("Ejecutar  el servicio de la interfaz OS2");
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

			String error = String.format(conError, run_id);
			String error1 = String.format(conError11, run_id);
			String error2 = String.format(conError2, run_id);

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

//					Paso 4	************************

		addStep("Comprobar que se registra la ejecucion en WMLOG");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		// String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
		// System.out.println(queryStatusLog);

		SQLResult statusQueryExe = dbLog.executeQuery(tdcIntegrationServerFormat);
		String fcwS = statusQueryExe.getData(0, "STATUS");
		boolean validateStatus = status.equals(fcwS);
		System.out.println(validateStatus);

		boolean validacionQueryStatus = statusQueryExe.isEmpty();
		if (!validacionQueryStatus) {
			testCase.addQueryEvidenceCurrentStep(statusQueryExe);
		}
		System.out.println(validacionQueryStatus);

		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");

//		 		Paso 5	************************

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
//					testCase.addQueryEvidenceCurrentStep(regPlazaTiendaThread);
//				}
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
		assertTrue(statusThread, "El registro de ejecución de la plaza y tienda no fue exitoso");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Verificar ejecución de la interfaz OS2 con el Obj. VTAGAS y plaza";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_OS2_Ejec_Interfaz_Con_Objetivo";
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
