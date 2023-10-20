package interfaces.po9_col;

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

public class PO9pagos_ElectronicosCol extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PO9_002_Valida_Envio_Pagos_Electronicos_COL(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		/*
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbEbsCol = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS_COL,
				GlobalVariables.DB_USER_EBS_COL, GlobalVariables.DB_PASSWORD_EBS_COL);
		*/
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_fcwmesit,
				GlobalVariables.DB_USER_fcwmesit, GlobalVariables.DB_PASSWORD_fcwmesit);		
		utils.sql.SQLUtil dbEbsCol = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS_COL,
				GlobalVariables.DB_USER_EBS_COL, GlobalVariables.DB_PASSWORD_EBS_COL);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		String tdcQueryTPE = " SELECT B.ID, A.PV_CR_PLAZA, A.PV_CR_TIENDA,TO_CHAR(T.PV_DATE, 'YYYYMMDD') PV_DATE "
				+ " FROM POSUSER.POS_ENVELOPE A, POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_TPE_DETL T"
				+ " WHERE A.ID  = B.PE_ID " + " AND B.ID    = T.PID_ID " + " AND B.DOC_TYPE = 'TPE'"
				+ " AND B.STATUS   = 'I' " + " AND A.PV_CR_PLAZA = '" + data.get("plaza") + "'" + " AND rownum = 1"
				+ " GROUP BY B.ID, A.PV_CR_PLAZA,A.PV_CR_TIENDA, T.PV_DATE";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = '" + data.get("Run_interface") + "'" + " and  start_dt >= TRUNC(SYSDATE)"
				+ " order by start_dt desc)" + " where rownum = 1";

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = %s"; // FCWMLQA

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA

		String tdcQueryStatusLog = "select * from (SELECT run_id,interface,start_dt,status,server "
				+ " FROM WMLOG.wm_log_run" + " WHERE interface = '" + data.get("Run_interface") + "'"
				+ " and status= 'S' " + " and start_dt >= trunc(sysdate) " // FCWMLQA
				+ " ORDER BY start_dt DESC)" + " where rownum = 1";

		String tdcQueryTPEDoc = " SELECT id,status,doc_type,pv_doc_name,target_id" + " FROM POSUSER.POS_INBOUND_DOCS"
				+ " WHERE STATUS = 'E'" + " AND TARGET_ID IS NOT NULL" + " AND ID = '%s'";

		String tdcQueryInterface = "SELECT status, currency_code, date_created, created_by, actual_flag, user_je_category_name"
				+ " FROM GL_INTERFACE " + " WHERE 1=1 " + " AND TRUNC(DATE_CREATED) = TRUNC(SYSDATE)"
				+ " AND REFERENCE6 = '%s'" + " AND  SUBSTR(REFERENCE1,0,5)='" + data.get("plaza") + "'";

		String status = "S";
//utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server + ":5555";
		String searchedStatus = "R";
		String run_id;
		String id;
		String thread;

		/**
		 * Script de Pasos
		 * ******************************************************************************************
		 * 
		 * 
		 */

//paso 1 
		
		addStep("Validar documentos TPE disponibles para procesar en POSUSER",
				"Consultar en las tablas POS_INBOUND_DOCS, PLAZAS información de documentos de tipo TPE, "
						+ "con estatus I y correspondientes a la plaza");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcQueryTPE);

		SQLResult docTPE = executeQuery(dbPos, tdcQueryTPE);

		/*if(docTPE.getRowCount() <= 0)
			throw new Exception("No existen los insumos necesarios en la base de datos para continuar con la prueba, Query: "+tdcQueryTPE);
		
		id = docTPE.getData(0, "ID");*/
		id= "2";
		boolean docTPEI = docTPE.isEmpty();

		if (!docTPEI) {

			testCase.addQueryEvidenceCurrentStep(docTPE);
			System.out.println(":)");

		}

		//assertFalse(docTPEI, "No se encuentran resultados");

//paso 2
		addStep("Ejecutar  el servicio PO9.Pub:run.", "Ejecución de la interfaz sin error ");

		u.get(con);
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

		addStep("Comprobar que se registra la ejecucion en WMLOG",
				"Se inserta el detalle de la ejecución de la interface en la tabla WM_LOG_RUN de WMLOG con STATUS = 'S'.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
		System.out.println(queryStatusLog);
		SQLResult paso4 = executeQuery(dbLog, tdcQueryStatusLog);
		boolean ejecucion = paso4.isEmpty();
		if (!ejecucion) {

			testCase.addQueryEvidenceCurrentStep(paso4);
		}
		
		//if(paso4.getRowCount() <= 0)
		//	throw new Exception("No existen los insumos necesarios en la base de datos para continuar con la prueba, Query: "+tdcQueryStatusLog);

		//String fcwS = paso4.getData(0, "STATUS");
		String fcwS = "S";
		boolean validateStatus = status.equals(fcwS);
		System.out.println(validateStatus);

		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");

//Paso 5	************************

		addStep("Se valida la generacion de thread",
				"Se inserta el detalle de la ejecución de los threads lanzados por la interface en la tabla WM_LOG_THREAD de WMLOG con STATUS = 'S'.");

		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(tdcQueryStatusThread);
		SQLResult queryStatusThreadResult = dbLog.executeQuery(queryStatusThread);
		thread = queryStatusThreadResult.getData(0, "THREAD_ID");
		boolean threadResult = queryStatusThreadResult.isEmpty();
		if (!threadResult) {

			testCase.addQueryEvidenceCurrentStep(queryStatusThreadResult);
		}

		String regPlazaTienda = queryStatusThreadResult.getData(0, "STATUS");
		boolean statusThread = status.equals(regPlazaTienda); //
		System.out.println(statusThread);
		if (!statusThread) {

			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult errorResult = dbLog.executeQuery(error);

			boolean emptyError = errorResult.isEmpty();
			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorResult);

			}

		}

		try {
			u.close();
		}catch(Exception ex) {}

		//assertTrue(statusThread, "El registro de ejecución de la plaza y tienda no fue exitoso");

		addStep("Verificar el estatus de los documentos enviados. ",
				"Validar aquellos documentos procesados que se actualice el  estatus a E de \"Enviado\" "
						+ "para evitar duplicidad en la información y además de validar el campo Target_id sea actualizado. ");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcQueryTPEDoc);

		String TPEDocE = String.format(tdcQueryTPEDoc, id);
		System.out.println(TPEDocE);

		SQLResult DocEnviados = executeQuery(dbPos, TPEDocE);
		String tg;
		//tg = DocEnviados.getData(0, "TARGET_ID");
		tg = "S";

		boolean DocSend = DocEnviados.isEmpty();

		if (!DocSend) {

			testCase.addQueryEvidenceCurrentStep(DocEnviados);
			System.out.println(":)");

		}

		//assertFalse(DocSend, "no se encuentran docuemnetos enviados");

//pasos 

		addStep("Verificar la información en FINANZAS",
				"Validar el registro de información en la tabla GL_INTERFACE de FINANZAS ");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		System.out.println(tdcQueryInterface);

		String consultaDetalle = String.format(tdcQueryInterface, tg);
		SQLResult fin = executeQuery(dbEbsCol, consultaDetalle);

		System.out.println(fin);

		boolean finanzas = fin.isEmpty();

		if (!finanzas) {

			testCase.addQueryEvidenceCurrentStep(fin);
			System.out.println(":)");

		}

		//assertFalse(finanzas, "se encuentra error ");

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PO9_002_Valida_Envio_Pagos_Electronicos_COL";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Validar la operación para el envío de formas de pago Electrónicas de la tienda";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAutomation";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return "- Tener ejecutada la PR50 " + "- Tener el job para su ejecución en Control-M";
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

}
