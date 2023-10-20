package interfaces.vdate;

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
import utils.sql.SQLUtil;

public class ProcesoEjecucionVDate extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
				GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		String tdcQueryVDate = "SELECT vdate FROM PERIOD";

		String tdcQueryInfo = "SELECT COUNT(B.ID) FROM " + 
				  " posuser.POS_INBOUND_DOCS B, posuser.POS_TSF C"
				+ " WHERE B.DOC_TYPE='TSF'" 
				+ " AND STATUS = 'H'" 
				+ " AND SUBSTR(PV_DOC_NAME,4,5) = '"+data.get("plaza")+"'"
				+ " AND SUBSTR(PV_DOC_NAME,9,5) = '"+data.get("tienda")+"'" 
				+ " AND B.ID = C.PID_ID"
				+ " AND TRUNC(C.SHIP_DATE) <=TO_DATE('%s',"+"'YYYY,MM,DD')";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = '" + data.get("Run_interface") + "'" 
				+ " and  start_dt >= TRUNC(SYSDATE)"
				+ " order by start_dt desc)" + " where rownum = 1";

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " 
				+ " WHERE parent_id = %s"; // FCWMLQA

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " 
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA

		String tdcQueryStatusLog = "select * from (SELECT run_id,interface,start_dt,status,server "
				+ " FROM WMLOG.wm_log_run" + " WHERE interface = '" + data.get("Run_interface") + "'"
				+ " and status= 'S' " + " and start_dt >= trunc(sysdate) " // FCWMLQA
				+ " ORDER BY start_dt DESC)" + " where rownum = 1";
		
		String tdcQueryVdates ="SELECT RETEK_VDATE "
				+ " FROM POSUSER.WM_RETEK_VDATE "
				+ " WHERE TRUNC(retek_vdate) =TO_DATE('%s',"+ "'YYYY-MM-DD')";
		
		String tdcQueryInboundDocs = "SELECT ID, STATUS, DOC_TYPE, SUBSTR(PV_DOC_NAME,4,5) PLAZA, SUBSTR(PV_DOC_NAME,9,5) TIENDA " + 
				" FROM POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_TSF C " + 
				" WHERE B.DOC_TYPE='TSF'" + 
				" AND STATUS = 'I'" + 
				" AND SUBSTR(PV_DOC_NAME,4,5) = '"+data.get("plaza")+"'" +
				" AND SUBSTR(PV_DOC_NAME,9,5) = '"+data.get("tienda")+"'"+
				" AND B.ID = C.PID_ID" + 
				" AND TRUNC(C.SHIP_DATE) <= TO_DATE('%s',"+"'YYYY-MM-DD')";

		String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server;
		String searchedStatus = "R";
		String run_id;
		String id;
		String thread;
		String vDate;

		/**
		 * Script de Pasos
		 * ******************************************************************************************
		 * 
		 * 
		 */

		// PAso 1
		addStep("Verificar la fecha de VDATE en la tabla PERIOD en RETEK",
				"Expected Result:Devuelve una fecha establecida en la tabla PERIOD");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(tdcQueryVDate);

		SQLResult vdate = executeQuery(dbRms, tdcQueryVDate);
		vDate = vdate.getData(0, "VDATE");
		
		boolean vdateValidate = vdate.isEmpty();

		if (!vdateValidate) {

			testCase.addQueryEvidenceCurrentStep(vdate);
			System.out.println(":)");

		}

		assertFalse(vdateValidate, "Error fatal ");

//Paso 2 

		addStep("Validar que exista información en POSUSER con fecha menor al registro en PERIOD.VDATE "
				+ " con STATUS H y doc_type RTV en las tablas POS_INBOUND_DOCS y POS_REC",
				"Expected Result : Devuelve la cantidad de documentos que prodran ser procesados.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
	

		String vdateInfFormat = String.format(tdcQueryInfo, vDate.split(" ")[0]);
		System.out.println(vdateInfFormat);
		
		
		
		SQLResult vdateInf = executeQuery(dbPos, vdateInfFormat);

		boolean vdateBoolean = vdateInf.isEmpty();

		if (!vdateBoolean) {

			testCase.addQueryEvidenceCurrentStep(vdateInf);
			System.out.println(":)");

		}

		assertFalse(vdateBoolean, "no se muestran registros a procesar");

//Paso 3 
		addStep("Ejecutar el servicio VDATE.Pub:run mediante la invocacion del JOB enableHeldDocs",
				"Que la interfaz se ejecute correctamente");

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));

		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {

			query = executeQuery(dbLog, tdcQueryIntegrationServer);
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult paso3 = executeQuery(dbLog, error);

			boolean emptyError = paso3.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(paso3);

			}
		}

//Paso 4	************************

		addStep("Comprobar que se registra la ejecucion en WMLOG");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
		System.out.println(queryStatusLog);
		SQLResult paso4 = executeQuery(dbLog, tdcQueryStatusLog);
		boolean ejecucion = paso4.isEmpty();
		if (!ejecucion) {

			testCase.addQueryEvidenceCurrentStep(paso4);
		}

		String fcwS = paso4.getData(0, "STATUS");
		boolean validateStatus = status.equals(fcwS);
		System.out.println(validateStatus);

		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");

//Paso 5	************************

		addStep("Comprobar que la fecha en WM_RETEK_VDATE de POSUSER sea la misma que se encuentra en PERIOD de RMS100.",
				"Que la fecha conincida con el valor que se encuentra en PERIOD.VDATE");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcQueryVdates);

		//spliter 
		String retekDateFormat = String.format(tdcQueryVdates, vDate.split(" ")[0]);
		System.out.println(vDate.split(" ")[0]);
		System.out.println(tdcQueryVdates);
		System.out.println(retekDateFormat);	
		
		
		
		
		SQLResult retekDate = executeQuery(dbPos, retekDateFormat);
		System.out.println(retekDate);
		boolean retekDateBolen = retekDate.isEmpty();
		
			if (!retekDateBolen) {
				
				testCase.addQueryEvidenceCurrentStep(retekDate);
				System.out.println(":)");
				
			}
		
		
assertFalse(retekDateBolen, "no se generaron registros");	

//PAso 6 *******************************

	addStep("Validar que se actualice el Status en POS_INBOUND_DOCS", 
			"Que se actualizaran los registros con las carateristicas solicitadas.");


	System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
	
	
	//spliter 
	String inboundFormat = String.format(tdcQueryInboundDocs, vDate.split(" ")[0]);
	System.out.println(inboundFormat);
	
	
	
	SQLResult inbound = executeQuery(dbPos, inboundFormat);
	boolean inboundBo = inbound.isEmpty();
	
	if (!inboundBo) {
		
		
		testCase.addQueryEvidenceCurrentStep(inbound);
		System.out.println(":)");
	}


	assertFalse(inboundBo, "no tiene registros ");

		
		
		
		

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
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
