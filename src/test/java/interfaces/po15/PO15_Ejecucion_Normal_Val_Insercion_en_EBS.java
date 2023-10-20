package interfaces.po15;

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

public class PO15_Ejecucion_Normal_Val_Insercion_en_EBS extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PO15_Ejecucion_Normal_Insercion_EBS(HashMap<String, String> data) throws Exception {

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

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,
				GlobalVariables.DB_PASSWORD_AVEBQA);

		/**
		 * Variables
		 * ******************************************************************************************
		 * PO15
		 * 
		 */

		String tdcVAL = "SELECT A.ID, A.PE_ID, A.PV_DOC_ID, A.status,A.PV_DOC_NAME, A.TARGET_ID, A.TARGET_ALT_ID, A.PARTITION_DATE FROM POSUSER.POS_INBOUND_DOCS A, POSUSER.POS_VAL B, POSUSER.POS_VAL_DETL C "
				+ "WHERE SUBSTR(A.PV_DOC_NAME, 4,5) = '" + data.get("plaza") + "' "
				+ "AND SUBSTR(A.PV_DOC_NAME,9,5) = '" + data.get("tienda") + "' " + "AND A.STATUS = 'I' "
				+ "AND A.DOC_TYPE = 'VAL' " + "AND B.PID_ID= A.ID "
				+ "AND C.PID_ID= A.ID AND A.PARTITION_DATE >= SYSDATE-7 order by A.PARTITION_DATE DESC ";

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server,"
				+ " (END_DT - START_DT)*24*60 FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE LIKE '%PO15%' ORDER BY START_DT DESC) "
				+ "where rownum <=1";// WMLOG

		String tdcQueryWmlogError = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// WMLOG
		String tdcQueryWmlogError1 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG
		String tdcQueryWmlogError2 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,status,att1,att2 " + 
				" FROM WMLOG.wm_log_thread " + 
				" WHERE parent_id = '%s'" ; //FCWMLQA 

		String tdcQueryEbs = "SELECT ID_VAL, CR_PLAZA, CR_TIENDA, ENVASE, CREATION_DATE, PID_ID  "
				+ "FROM XXFC.XXFC_VALIDADOR_ELECTRONICO  " + "WHERE CR_PLAZA = '" + data.get("plaza") + "'  "
				+ "AND CR_TIENDA = '" + data.get("tienda") + "' " + "AND PID_ID = '%s'" + // [POS_INBOUND_DOCS.ID]
				"AND TRUNC(CREATION_DATE) = TRUNC(sysdate)  " + "order by ID_VAL desc ";

		String tdcVAL_E = "SELECT A.ID, A.PE_ID, A.PV_DOC_ID, A.status,A.PV_DOC_NAME, A.TARGET_ID, A.TARGET_ALT_ID, A.PARTITION_DATE FROM POSUSER.POS_INBOUND_DOCS A, POSUSER.POS_VAL B, POSUSER.POS_VAL_DETL C "
				+ "WHERE SUBSTR(A.PV_DOC_NAME, 4,5) = '" + data.get("plaza") + "' "
				+ "AND SUBSTR(A.PV_DOC_NAME,9,5) = '" + data.get("tienda") + "' " + "AND A.STATUS = 'E' "
				+ "AND A.DOC_TYPE = 'VAL' " + "AND B.PID_ID= A.ID "
				+ "AND C.PID_ID= A.ID AND A.PARTITION_DATE >= SYSDATE-7 order by A.PARTITION_DATE DESC ";
		String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";

//				Paso 1	************************		

		addStep("Tener información para procesar en las tablas: POS_INBOUND_DOCS, POS_VAL y POS_VAL_DETL en POSUSER");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcVAL); // get shipment
		SQLResult servQueryExe = dbPos.executeQuery(tdcVAL);
		String id = "";
		boolean validacionServ = servQueryExe.isEmpty();
		if (!validacionServ) {
			servQueryExe.getData(0, "ID");
			testCase.addQueryEvidenceCurrentStep(servQueryExe);
		}
		System.out.println(validacionServ);
		assertFalse(validacionServ, "No hay insumos a procesar");
		
// paso 2 ************************
		addStep("Ejecutar  el servicio de la interfaz PO15");
		// utileria

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
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

//		Paso 4	************************

		addStep("Comprobar que se registra la ejecucion en WMLOG");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		SQLResult statusQueryExe = dbLog.executeQuery(tdcIntegrationServerFormat);
		System.out.println("status " + statusQueryExe);
		String fcwS = statusQueryExe.getData(0, "STATUS");
		boolean validateStatus = status.equals(fcwS);
		System.out.println(validateStatus);

		boolean validacionQueryStatus = statusQueryExe.isEmpty();
		if (!validacionQueryStatus) {
			testCase.addQueryEvidenceCurrentStep(statusQueryExe);
		}
		System.out.println(validacionQueryStatus);

		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");

//		Paso 5	************************

		addStep("Se valida la generacion de thread");

		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);

		SQLResult regPlazaTiendaThread = dbLog.executeQuery(queryStatusThread);

		String Threadstatus = regPlazaTiendaThread.getData(0, "STATUS");

		boolean threadResult = regPlazaTiendaThread.isEmpty();
		if (!threadResult) {
			testCase.addQueryEvidenceCurrentStep(regPlazaTiendaThread);
		}
				
		boolean statusThread = status.equals(Threadstatus);
		System.out.println(statusThread);
		if (!statusThread) {

			String error = String.format(tdcQueryWmlogError, run_id);
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

//      paso 6 *********************************
		addStep("Validar que el estatus de los registros fue cambiado a E en la tabla POS_INBOUND_DOCS de POSUSER");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcVAL_E); // get shipment
		SQLResult servQueryExe2 = dbPos.executeQuery(tdcVAL_E);
		// String id = servQueryExe.getData(0, "ID");
		boolean validacionServ2 = servQueryExe2.isEmpty();
		if (!validacionServ2) {
			testCase.addQueryEvidenceCurrentStep(servQueryExe2);
		}
		System.out.println(validacionServ2);
		assertFalse(validacionServ2, "No hay registros.");
		
//  paso 7 *******************************************
		addStep("Validar que se insertó correctamente la información en la tabla XXFC_VALIDADOR_ELECTRONICO de ORAFIN");

		String consultaPRC1 = String.format(tdcQueryEbs, id);
		System.out.print("" + consultaPRC1);
		SQLResult PRC = dbEbs.executeQuery(consultaPRC1);

		boolean paso8 = PRC.isEmpty();
		if (!paso8) {
			testCase.addQueryEvidenceCurrentStep(PRC);
		}
		assertFalse("No hay información.", paso8);
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. Validar que se inserte la información en la tabla XXFC_VALIDADOR_ELECTRONICO de ORAFIN para la Plaza y tienda.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PO15_Ejecucion_Normal_Insercion_EBS";
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