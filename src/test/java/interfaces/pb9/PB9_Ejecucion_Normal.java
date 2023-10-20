package interfaces.pb9;

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

public class PB9_Ejecucion_Normal extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PB9_001_Verifica_Proceso_Normal(HashMap<String, String> data) throws Exception {

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
		 * pb9
		 * 
		 */
		
		String tdcCOZ = "SELECT A.ID,A.PE_ID, A.STATUS, A.PV_DOC_NAME, A.TARGET_ID, A.PARTITION_DATE " + 
				"FROM posuser.pos_inbound_docs A, posuser.pos_coz  B, posuser.pos_coz_detl C " + 
				"WHERE A.DOC_TYPE = 'COZ' AND A.status = 'I'  " + 
				"AND substr(A.pv_doc_name,4,5) ='" + data.get("plaza") + "' " + 
				"AND B.pid_id = C.pid_id  AND B.pid_id = A.ID " + 
				"AND A.PARTITION_DATE >= SYSDATE -7  " + 
				"order by A.partition_date desc  ";
				
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server,"
				+ " (END_DT - START_DT)*24*60 FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE LIKE '%PB9%' ORDER BY START_DT DESC) "
				+ "where rownum <=1";// WMLOG
		
		String tdcQueryWmlogError = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// WMLOG
		String tdcQueryWmlogError1 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG
		String tdcQueryWmlogError2 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG
		
		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = '%s'"; // FCWMLQA
		
		
		String tdcCOZE = "SELECT A.ID,A.PE_ID, A.STATUS, A.PV_DOC_NAME, A.TARGET_ID, A.PARTITION_DATE\r\n" + 
				"FROM posuser.pos_inbound_docs A WHERE status = 'E' " + 
				"AND id ='%s'  " + 
				"AND substr(pv_doc_name,4,5)= '" + data.get("plaza") + "' " + 
				"AND DOC_TYPE = 'COZ' ";
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

		addStep("Validar que exista información en la tabla POS_INBOUND_DOCS de POSUSER  con tipos de documento COZ y STATUS I.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(tdcCOZ); // get shipment
		SQLResult servQueryExe = dbPos.executeQuery(tdcCOZ);
		 String id = servQueryExe.getData(0, "ID");
		boolean validacionServ = servQueryExe.isEmpty();
		if (!validacionServ) {
			testCase.addQueryEvidenceCurrentStep(servQueryExe);
		}
		System.out.println(validacionServ);
		assertFalse(validacionServ, "No hay insumos a procesar");
		
		
		addStep("Ejecutar  el servicio de la interfaz PB9");
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

		// String queryStatusLog = String.format(tdcIntegrationServerFormat, run_id);
		// System.out.println(queryStatusLog);

		SQLResult statusQueryExe = dbLog.executeQuery(tdcIntegrationServerFormat);
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
		u.close();
		addStep("Validar que el estatus en la tabla POS_INBOUND_DOCS sea igual a 'E' con tipos de documeto COZ Base de Datos del POSUSER. ");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(tdcCOZE); // get shipment
		String COZE = String.format(tdcCOZE, id);
		SQLResult servQueryExeE = dbPos.executeQuery(COZE);
		// String id = servQueryExe.getData(0, "ID");
		boolean validacionServE = servQueryExeE.isEmpty();
		if (!validacionServE) {
			testCase.addQueryEvidenceCurrentStep(servQueryExeE);
		}
		System.out.println(validacionServE);
		assertFalse(validacionServE, "No hay insumos a procesar");

}

@Override
public void beforeTest() {
	// TODO Auto-generated method stub

}

@Override
public String setTestDescription() {
	// TODO Auto-generated method stub
	return "Construido. Ejecución de la interfaz para verificar el proceso normal de la plaza y tienda.";
}

@Override
public String setTestDesigner() {
	// TODO Auto-generated method stub
	return "AutomationQA";
}

@Override
public String setTestFullName() {
	// TODO Auto-generated method stub
	return "ATC_FT_PB9_001_Verifica_Proceso_Normal";
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
