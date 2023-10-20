package interfaces.pb5;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
//import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class PB5RegistroTickets extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PB5_001_Registro_Tickets(HashMap<String, String> data) throws Exception {

		/*
		 * 
		 * 
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbTPE = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE,GlobalVariables.DB_PASSWORD_FCTPE);

		/*
		 * Variables
		 * *****************************************************************************
		 * *************
		 * 
		 * 
		 */ String tdcQueryPos = "SELECT A.ID, B.ESTATUS, B.PID_ID, A.PV_CR_PLAZA, A.PV_CR_TIENDA, B.VT_LLAVERO "
				+ " FROM posuser.POS_ENVELOPE A, posuser.POS_TIC_DETL_F B, posuser.POS_INBOUND_DOCS C "
				+ " WHERE A.ID = C.PE_ID " 
				+ " AND C.ID = B.PID_ID " 
				+ " AND B.ESTATUS = 'INSERTADO' "
				+ " AND A.PV_CR_PLAZA = '" + data.get("plaza") + "'" 
				+ " AND A.PV_CR_TIENDA = '" + data.get("tienda")+ "'" 
				+ " AND B.VT_LLAVERO IS not NULL";

		String tdcQueryTpeAccess = "SELECT ACCESS_ID, TYPE, ACCOUNT_ID, STATUS, PLAZA, TIENDA "
				+ " FROM TPEUSER.TPE_FCP_ACCESS" 
				+ " WHERE ACCESS_ID = %s " 
				+ " AND PLAZA = '" + data.get("plaza")+ "' " 
				+ " AND TIENDA = '" + data.get("tienda") + "'";

		String tdcQueryIntegrationServer = "SELECT run_id,start_dt,status" 
		        + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = '" + data.get("wm_log") + "'" 
		        + " And  TRUNC(start_dt) >= TRUNC(SYSDATE)"
				+ " And rownum = 1"
		        + " order by start_dt desc";

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " 
		        + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s" 
		        + " and rownum = 1";

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " 
				+ " WHERE parent_id = %s";

		String tdcQueryPorProcesar = "SELECT A.PLAZA, I.BRMS_STATUS, A.TIENDA, A.TYPE, A.ACTUALIZATION_DATE "
				+ " FROM tpeuser.TPE_FCP_ACTIVITY A INNER JOIN tpeuser.TPE_FCP_ITEM I ON I.ACTIVITY_ID = A.ACTIVITY_ID"
				+ " where A.PLAZA = '" + data.get("plaza") + "' " 
				+ " AND I.BRMS_STATUS = 'POR PROCESAR'"
				+ " AND A.TIENDA = '" + data.get("tienda") + "' " 
				+ " AND A.TYPE = 'NO FACTURADO'"
				+ " AND TRUNC(A.ACTUALIZATION_DATE) = TRUNC(sysdate)" 
				+ " order by ACTUALIZATION_DATE desc";

		String tdcQueryProcesado = "SELECT A.ID, B.ESTATUS, B.PID_ID, A.PV_CR_PLAZA, A.PV_CR_TIENDA, B.VT_LLAVERO "
				+ " FROM posuser.POS_ENVELOPE A, posuser.POS_TIC_DETL_F B, posuser.POS_INBOUND_DOCS C "
				+ " WHERE A.ID = C.PE_ID " 
				+ " AND C.ID = B.PID_ID " 
				+ " AND B.ESTATUS = 'PROCESADO' "
				+ " AND A.PV_CR_PLAZA = '" + data.get("plaza") + "'" 
				+ " AND A.PV_CR_TIENDA = '" + data.get("tienda") + "'";

		

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id;
		String status = "S";

		testCase.setProject_Name("AutomationQA");

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 */

//		Paso 1	************************ 

		addStep("Consultar que existan registros en las tablas POS_ENVELOPE, POS_TIC_DETL_F, POS_INBOUND_DOCS de la BD POSUSER.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryPos);

		SQLResult posResult = executeQuery(dbPos, tdcQueryPos);
		String llavero = posResult.getData(0, "VT_LLAVERO");

		boolean pos = posResult.isEmpty();
		System.out.println(pos);
		if (!pos) {

			testCase.addQueryEvidenceCurrentStep(posResult);

		}
		assertFalse(pos, "No se obtiene informacion de la consulta");

// Paso 2 ***************************

		addStep("Verificar que exista información en la tabla TPE_FCP_ACCESS de la BD TPEUSER.");
		System.out.println(GlobalVariables.DB_HOST_FCTPE);
		String accessFormat = String.format(tdcQueryTpeAccess, llavero);

		SQLResult accessResult = executeQuery(dbTPE, accessFormat);
		System.out.println(accessFormat);

		boolean access = accessResult.isEmpty();

		if (!access) {

			testCase.addQueryEvidenceCurrentStep(accessResult);

		}

		System.out.println(access);

		assertFalse(access, "No se obtiene informacion de la consulta");

// Paso 3 ****************************

		addStep("Ejecutar el servicio PB5.Pub:run invocando el Job runMainPB5.");
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");

//		Paso 4	************************

		addStep("Validar que el registro de la tabla WM_LOG_RUN termine en estatus 'S'.");
		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		if (valuesStatus) {

			query = executeQuery(dbLog, tdcQueryIntegrationServer);
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult paso2 = executeQuery(dbLog, error);

			boolean emptyError = paso2.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(paso2);

			}
		}
		testCase.addQueryEvidenceCurrentStep(query);

//		Paso 5	************************

		addStep("Se valida la generacion de thread.");
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String statusThreadFormat = String.format(tdcQueryStatusThread, run_id);
		SQLResult threadResult = executeQuery(dbLog, statusThreadFormat);

		System.out.println(statusThreadFormat);

		String statusThread = threadResult.getData(0, "Status");

		boolean ST = statusThread.equals(status);
		ST = !ST;
		if (!ST) {

			testCase.addQueryEvidenceCurrentStep(threadResult);

		}

		System.out.println(ST);

		assertFalse(ST, "No se obtiene informacion de la consulta");

// Paso 6 ******************************

		addStep("Confirmar que se insertaron correctamente los registros de los tickets en la "
				+ "tablas TPE_FCP_ACTIVITY y TPE_FCP_ITEM de la BD TPEUSER.");

		System.out.println(GlobalVariables.DB_HOST_FCTPE);
		System.out.println(tdcQueryPorProcesar);

		SQLResult procesarResult = executeQuery(dbTPE, tdcQueryPorProcesar);

		boolean procesar = procesarResult.isEmpty();

		if (!procesar) {

			testCase.addQueryEvidenceCurrentStep(procesarResult);

		}

		System.out.println(procesar);

		assertFalse(procesar, "No se obtiene informacion de la consulta");

// Paso 7 *********************************

		addStep("Comprobar que los tickets fueron actualizados correctamente en la tabla POS_TIC_DETL_F en la BD POSUSER.");

		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryProcesado);

		SQLResult procesadoResult = executeQuery(dbPos, tdcQueryProcesado);

		boolean procesado = procesadoResult.isEmpty();

		if (!procesado) {

			testCase.addQueryEvidenceCurrentStep(procesadoResult);

		}

		System.out.println(procesado);

		assertFalse(procesado, "No se obtiene informacion de la consulta");
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
		return "Terminada.Que la información de los tickets sea procesada y sea registrado el Tikect..";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PB5_001_Registro_Tickets";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
