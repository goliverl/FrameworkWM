package interfaces.pb4;

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

public class PB4_Proc_Pedidos_No_Surtidos extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PB4_001_Proc_Pedidos_No_Surtidos(HashMap<String, String> data) throws Exception {

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

	
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Ebs, GlobalVariables.DB_USER_Ebs,
				GlobalVariables.DB_PASSWORD_Ebs);
		utils.sql.SQLUtil dbBI = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_BI,GlobalVariables.DB_USER_BI, GlobalVariables.DB_PASSWORD_BI);

		/**
		 * Variables
		 * ******************************************************************************************
		 * pr4
		 * 
		 */
		String tdcNSP = "SELECT DISTINCT(A.PV_CR_PLAZA), A.PV_CR_TIENDA, B.ID,B.PE_ID, B.STATUS, B.PV_DOC_NAME  \r\n"
				+ "FROM POSUSER.POS_ENVELOPE A, POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_NSP D, POSUSER.POS_NSP_DETL C\r\n"
				+ "WHERE A.ID = B.PE_ID AND B.DOC_TYPE = 'NSP' \r\n"
				+ "AND B.STATUS = 'I' AND SUBSTR(PV_DOC_NAME, 4,5) = '" + data.get("plaza") + "' "
				+ "AND B.ID = C.PID_ID AND B.ID=D.PID_ID AND D.ORDER_NO = C.BOL_NO";

		String tdcInfPlazaTienda = "SELECT ORACLE_CR,ORACLE_CR_TYPE, ESTADO, RETEK_CR, RETEK_ASESOR_NOMBRE FROM XXFC.XXFC_MAESTRO_DE_CRS_V "
				+ "WHERE ESTADO='A' AND ORACLE_CR_SUPERIOR = '" + data.get("plaza") + "' "
				+ "AND ORACLE_CR_TYPE = 'T' AND ORACLE_CR = '" + data.get("tienda") + "'";// FCWM6QA.

		String tdcQueryStatusLog = "SELECT run_id,interface,start_dt,status,server " + " FROM WMLOG.wm_log_run"
				+ " WHERE interface like '%PB4%'and start_dt >= trunc(sysdate) " // FCWMLQA
				+ " ORDER BY start_dt DESC";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PB4'"
//				 +" and  start_dt > To_Date ('%s', 'DD-MM-YYYY hh24:mi' )"
				+ " ORDER BY START_DT DESC) where rownum <=1";

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = '%s'"; // FCWMLQA

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID= '%s' "; // FCWMLQA

		String tdcNSP_E = "SELECT ID,PE_ID, STATUS, doc_type,PV_DOC_NAME  FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ "WHERE DOC_TYPE = 'NSP' AND STATUS = 'E' " + "AND ID = '%s'";

		String tdcBI = "SELECT id_pedido,id_tienda,id_proveedor,num_pedido,loaddate FROM BIODSMKT.PEDIDO WHERE ID_TIENDA ='%s'\r\n" + 
				"AND TRUNC(LOADDATE) =  TRUNC(SYSDATE)";
//				" WHERE run_id = '%s'";			
//				" AND cr_plaza = '" + data.get("plaza")  +"'"+ 
//				" AND cr_tienda = '"+ data.get("tienda") +"'";

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server,"
				+ " (END_DT - START_DT)*24*60 FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE LIKE '%PB4%' ORDER BY START_DT DESC) "
				+ "where rownum <=1";// WMLOG
		String consulta6 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// WMLOG
		String consulta61 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG
		String consulta62 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
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
		System.out.println(GlobalVariables.DB_HOST_LOG);

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 */

// 				Paso 1	************************		

		addStep("Que exista al menos un registro con STATUS = I y DOC_TYPE = NSP  en las tablas POS_INBOUND_DOCS.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(tdcNSP); // get shipment
		SQLResult servQueryExe = dbPos.executeQuery(tdcNSP);
		String id = ""; 
		boolean validacionServ = servQueryExe.isEmpty();
		if (!validacionServ) {
			testCase.addQueryEvidenceCurrentStep(servQueryExe);
			id = servQueryExe.getData(0, "ID");
		}
		System.out.println(validacionServ);

	//	assertFalse(validacionServ, "No hay insumos a procesar");

//			Paso 1.2	************************		

		addStep("Que este registrada la plaza y la tienda en tabla XXFC_MAESTRO_DE_CRS_V en la BD ORAFIN " + "");

		System.out.println(tdcInfPlazaTienda); // get shipment
		SQLResult servQueryExe1 = dbEbs.executeQuery(tdcInfPlazaTienda);
		String idtienda= servQueryExe1.getData(0, "RETEK_CR");
		boolean validacionServ1 = servQueryExe.isEmpty();
		if (!validacionServ1) {
			testCase.addQueryEvidenceCurrentStep(servQueryExe1);
		}
		System.out.println(validacionServ1);

		assertFalse(validacionServ1, "No hay insumos a procesar");

//				Paso 2	************************		

		addStep("Ejecutar  el servicio de la interfaz PB4");
// utileria

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

//					Paso 4	************************

		addStep("Comprobar que se registra la ejecucion en WMLOG");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		//String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
		//System.out.println(queryStatusLog);

		SQLResult statusQueryExe = dbLog.executeQuery(tdcQueryStatusLog);
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

//				Paso 7	************************

		addStep("Se valida el cambio de estatus a E");

		System.out.println(GlobalVariables.DB_HOST_Puser);

		System.out.println(tdcNSP_E);
		String id1 = String.format(tdcNSP_E, id);
		SQLResult adjQuery = dbPos.executeQuery(id1);

		boolean adj = adjQuery.isEmpty();

		if (!adj) {

			testCase.addQueryEvidenceCurrentStep(adjQuery);

		}

		System.out.println(adj);

		assertFalse(adj, "No se encuentra el  documentos ");

//				Paso 8	************************

		addStep("Se valida que los datos se hayan insertado en la tabla PEDIDO de la BD BI ");

		System.out.println(GlobalVariables.DB_HOST_BI);
		System.out.println(GlobalVariables.DB_USER_BI);
		System.out.println(GlobalVariables.DB_HOST_BI);

		String thr = String.format(tdcBI, idtienda);
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
		return "Validar que  se procesan los datos para la plaza correspondiente (PEDIDOS NO SURTIDOS)";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PB4_001_Proc_Pedidos_No_Surtidos";
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
