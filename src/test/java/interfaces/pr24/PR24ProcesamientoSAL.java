package interfaces.pr24;

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

public class PR24ProcesamientoSAL extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_pr24_ProcesamientoSAL(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		
//		BD NUCLEO
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_fcwmesit,
				GlobalVariables.DB_USER_fcwmesit, GlobalVariables.DB_PASSWORD_fcwmesit);
//		BD ORIGINAL
//		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
//				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
	
//	    BD NUCLEO
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCRMSMGR,
				GlobalVariables.DB_USER_FCRMSMGR, GlobalVariables.DB_PASSWORD_FCRMSMGR);
//		BD ORIGINAL
//	    utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
//				GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
	    
		
//		utils.sql.SQLUtil dbServ = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,
//				GlobalVariables.DB_PASSWORD_Puser);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		String tdcQuerySAL = " SELECT PV_CR_PLAZA, PV_CR_TIENDA,B.STATUS, B.ID"
				+ " FROM POSUSER.POS_ENVELOPE A, POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_SAL_DETL C"
				+ " WHERE B.PE_ID=A.ID " + " AND B.ID = C.PID_ID " + " AND B.DOC_TYPE='SAL' "
				+ " AND B.PARTITION_DATE>=TRUNC(SYSDATE-7) " + " AND A.PARTITION_DATE>=TRUNC(SYSDATE-7) "
				+ " AND B.STATUS='I' ";
//			" GROUP BY PV_CR_PLAZA, PV_CR_TIENDA, B.ID";		

		String tdcQueryConnRetek = "SELECT ftp_base_dir, ftp_serverhost, ftp_serverport, ftp_username, ftp_password"
				+ " FROM wmuser.wm_ftp_connections" + " WHERE ftp_conn_id = 'RTKRMS'";

		String tdcQueryProceSal = " SELECT DOC_TYPE,RUN_ID,CREATED_DATE,STATUS,CR_PLAZA,CR_TIENDA"
				+ " FROM wmuser.RTK_INBOUND_DOCS " + " WHERE DOC_TYPE = 'SAL'" + " AND STATUS = 'L'"
				+ " AND run_id in ('%s')";

		String tdcQueryProceInboud = "SELECT ID,PE_ID,DOC_TYPE,STATUS,PV_DOC_NAME,TARGET_ID,PARTITION_DATE"
				+ " FROM POSUSER.POS_INBOUND_DOCS" + " WHERE DOC_TYPE = 'SAL'" + " AND STATUS = 'E'"
				+ " AND ID IN (%s)";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = '" + data.get("Run_interface") + "'" + " and  start_dt >= TRUNC(SYSDATE)"
				+ " order by start_dt desc)" + " where rownum = 1";

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = %s"; // FCWMLQA

		String qry_threads2 = "SELECT  ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID = '%s'";

		// consultas de error
		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1"; // dbLog
		String consultaError2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1"; // dbLog
		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1"; // dbLog

		String tdcQueryStatusLog = "select * from (SELECT run_id,interface,start_dt,status,server "
				+ " FROM WMLOG.wm_log_run" + " WHERE interface = '" + data.get("Run_interface") + "'"
				+ " and start_dt >= trunc(sysdate) " // FCWMLQA
				+ " ORDER BY start_dt DESC)" + " where rownum = 1";

		String status = "S";
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server + ":5555";
		String searchedStatus = "R";
		String run_id;
		String id = "";
		String thread = "";

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 */

		/* Paso 1 */
		addStep("Buscar registros para procesar en las tablas POS_ENVELOPE, POS_INBOUND_DOCS y POS_SAL_DETL de POSUSER ");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcQuerySAL);

		SQLResult paso1 = executeQuery(dbPos, tdcQuerySAL);
		// Saque la variable
//		id = SQLUtil.getColumn(testCase, dbPos, tdcQuerySAL, "ID");

		boolean reg = paso1.isEmpty();

		if (!reg) {
			id = paso1.getData(0, "ID");
			System.out.println("Paso 1 ID: " + id);
			testCase.addQueryEvidenceCurrentStep(paso1);

		} else if (reg) {
			testCase.addQueryEvidenceCurrentStep(paso1);
			testCase.addTextEvidenceCurrentStep("No encontro registros para procesar en las tablas POS_ENVELOPE, POS_INBOUND_DOCS y POS_SAL_DETL de POSUSER" );
		}

		System.out.println(reg);

		assertFalse(reg, " No se muestran registros a procesar ");

		/* Paso 2 */
		addStep("Validar los datos de conexión FTP del servidor RETEK donde se enviarán los documentos generados por la ejecucion de la interface.");

		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryConnRetek);
		SQLResult paso2 = executeQuery(dbPos, tdcQueryConnRetek);

		boolean serv = paso2.isEmpty();

		if (!serv) {

			testCase.addQueryEvidenceCurrentStep(paso2);

		} else if (serv) {
			testCase.addQueryEvidenceCurrentStep(paso2);
			testCase.addTextEvidenceCurrentStep("No encontro datos de conexión FTP del servidor RETEK donde se enviarán los documentos generados por la ejecucion" );
			
		}

		System.out.println(serv);

//		assertFalse(serv, "No se tiene conexión al servidor FTP");

		/* Paso 3 */
		addStep("Ejecutar el servicio PR24.Pub:run");
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

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
		System.out.println(successRun);
		if (!successRun) {

			String error = String.format(consultaError1, run_id);
			String error1 = String.format(consultaError2, run_id);
			String error2 = String.format(consultaError3, run_id);

			SQLResult paso3 = executeQuery(dbLog, error);

			boolean emptyError = paso3.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(paso3);

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

//	Paso 4	************************

		addStep("Comprobar que se registra la ejecucion en WMLOG");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
		System.out.println(queryStatusLog);
		SQLResult paso4 = executeQuery(dbLog, queryStatusLog);
		boolean ejecucion = paso4.isEmpty();
		String fcwS = "";

		if (!ejecucion) {

			testCase.addQueryEvidenceCurrentStep(paso4);
			fcwS = paso4.getData(0, "STATUS");

		} else if (ejecucion) {
			testCase.addQueryEvidenceCurrentStep(paso4);
			testCase.addTextEvidenceCurrentStep("No encontro registro la ejecucion en WMLOG" );
			
		}

		boolean validateStatus = status.equals(fcwS);
		System.out.println(validateStatus);

//		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");

		SQLResult log = dbLog.executeQuery(tdcQueryIntegrationServer);

		testCase.addQueryEvidenceCurrentStep(log);

//	Paso 5	************************

		addStep("Se valida la generacion de thread");
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);
		SQLResult queryStatusThreadResult = dbLog.executeQuery(queryStatusThread);

		boolean threadResult = queryStatusThreadResult.isEmpty();
		if (!threadResult) {

			testCase.addQueryEvidenceCurrentStep(queryStatusThreadResult);
			thread = queryStatusThreadResult.getData(0, "THREAD_ID");
		} else if (threadResult) {
			testCase.addQueryEvidenceCurrentStep(queryStatusThreadResult);
			
		}

		// .-----------Segunda consulta
		String consulta2 = String.format(qry_threads2, run_id);
		SQLResult consultaThreads2 = dbLog.executeQuery(consulta2);
		boolean threads1 = consultaThreads2.isEmpty();
		if (!threads1) {
			testCase.addQueryEvidenceCurrentStep(consultaThreads2);
		} else if (threads1) {
			testCase.addQueryEvidenceCurrentStep(consultaThreads2);
			testCase.addTextEvidenceCurrentStep("No encontro generacion de thread" );
		}
		System.out.println(threads1);
//		assertFalse("No se generaron threads en la tabla", threads1);

		/* Paso 6 */
		addStep("Validar que se inserten los registros de los documentos SAL procesados en la tabla "
				+ "RTK_INBOUND_DOCS de RETEK con STATUS = 'L'. El run id de cada registro es el thread que se lanzo por cada documento.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);

		String queryPrs = String.format(tdcQueryProceSal, thread);
		System.out.println(queryPrs);
		SQLResult paso6 = executeQuery(dbRms, queryPrs);

		boolean regSAL = paso6.isEmpty();

		if (!regSAL) {

			testCase.addQueryEvidenceCurrentStep(paso6);

		} else if (regSAL) {
			testCase.addQueryEvidenceCurrentStep(paso6);
			testCase.addTextEvidenceCurrentStep(" No inserto los registros de los documentos SAL procesados en la tabla RTK_INBOUND_DOCS de RETEK con STATUS = 'L'" );
		}

		System.out.println(regSAL);

//		assertFalse(regSAL, " No se muestran registros a procesar ");

		/* Paso 7 */
		addStep("Validar que se actualice el estatus de los documentos procesados en la tabla POS_INBOUND_DOCS de POSUSER a STATUS='E'");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcQueryProceInboud);

		String stSAL = String.format(tdcQueryProceInboud, id);
		System.out.println(stSAL);
		SQLResult paso7 = executeQuery(dbPos, stSAL);

		boolean valSta = paso7.isEmpty();

		if (!valSta) {

			testCase.addQueryEvidenceCurrentStep(paso7);

		} else if (valSta) {
			testCase.addQueryEvidenceCurrentStep(paso7);
			testCase.addTextEvidenceCurrentStep("No encontro registros que coincidan con lo requerido(No actualizo status)" );
		}
		

		System.out.println(stSAL);

		assertFalse(valSta, " No se muestran registros a procesado");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Enviar la información de ventas diarias al sistema RMS";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAautomation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_pr24_ProcesamientoSAL";
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
