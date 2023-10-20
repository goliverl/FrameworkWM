package interfaces.pr23_reple;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class pr23_reple extends BaseExecution {

	/**
	 * @author 1000075178 Ultima modificacion Mariana Vives
	 * @date 27/02/2023
	 */
	
	@Test(dataProvider = "data-provider")
	public void pr23_reple_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 ********************************************************************************************************************************************/
//	BD original
//		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS,
//				GlobalVariables.DB_USER_EBS, GlobalVariables.DB_PASSWORD_EBS);
		//BD NUCLEO
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCEBSSIT,
				GlobalVariables.DB_USER_FCEBSSIT, GlobalVariables.DB_PASSWORD_FCEBSSIT);
				
//		BD Original
//		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
//				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
//		BD NUCLEO
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_fcwmesit,
				GlobalVariables.DB_USER_fcwmesit, GlobalVariables.DB_PASSWORD_fcwmesit);
//		BD ORIGINAL
//		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
//				GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
//		BD NUCLEO
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCRMSMGR,
				GlobalVariables.DB_USER_FCRMSMGR, GlobalVariables.DB_PASSWORD_FCRMSMGR);
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		/*
		 * Variables
		 ******************************************************************************************************************************************/
		// Paso 1
		String paso1 = "SELECT PV_CR_PLAZA, PV_CR_TIENDA "
				+ "FROM POSUSER.POS_ENVELOPE A, POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C "
				+ "WHERE B.PE_ID = A.ID " + "AND B.DOC_TYPE = 'REC' " + "AND B.STATUS = 'I' " + "AND C.PID_ID = B.ID "
				+ "AND C.PV_CVE_MVT=10 " + "AND C.ORDER_NO <> 0 " + "AND B.PARTITION_DATE > SYSDATE -45 "
				+ "GROUP BY PV_CR_PLAZA, PV_CR_TIENDA ";

		// Paso 2

		String paso2_qry1 = "SELECT ORACLE_CR, ORACLE_CR_DESC, ORACLE_CR_SUPERIOR, ORACLE_EF, ORACLE_EF_DESC "
				+ "FROM XXFC.XXFC_MAESTRO_DE_CRS_V " + "WHERE ESTADO = 'A' " + "AND ORACLE_CR_SUPERIOR = '"
				+ data.get("plaza") + "' " + "AND ORACLE_CR = '" + data.get("tienda") + "' "
				+ "AND ORACLE_CR_TYPE = 'T' ";

		String paso2_qry2 = "SELECT ORACLE_CIA, ORACLE_CIA_DESC, LEGACY_EF, LEGACY_CR, RETEK_CR, RETEK_DISTRITO "
				+ "FROM XXFC.XXFC_MAESTRO_DE_CRS_V " + "WHERE ESTADO = 'A' " + "AND ORACLE_CR_SUPERIOR = '"
				+ data.get("plaza") + "' " + "AND ORACLE_CR = '" + data.get("tienda") + "' "
				+ "AND ORACLE_CR_TYPE = 'T' ";
		// Paso 3

		String paso3 = "SELECT B.ID, C.PID_ID, PV_CVE_MVT, TO_CHAR(CREATED_DATE,'YYYYMMDDHH24MISS') CREATED_DATE, "
				+ "PV_CED_ID, PV_CR_FROM_LOC, TSF_NO, EXT_REF_NO, ORDER_NO "
				+ "FROM POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C " + "WHERE 1=1 "// SUBSTR(PV_DOC_NAME,4,10)= '"+
																						// data.get("plaza") + "'|| '" +
																						// data.get("tienda") + "' "
				+ "AND B.DOC_TYPE='REC' " + "AND STATUS ='I' " + "AND C.PID_ID=B.ID " + "AND C.PV_CVE_MVT=10 "
				+ "AND C.ORDER_NO<>0 " + "AND B.PARTITION_DATE > SYSDATE -45 ";

		// Paso 4
		String paso4 = "SELECT ITEM, RECEIVED_QTY, NVL(CARTON,0) CARTON, BOL_NO,  PV_RETAIL_PRICE, NVL(REMISION,0) REMISION FROM POSUSER.POS_REC_DETL WHERE PID_ID = '%s'";

		// Paso 5
		String tdcQueryConnections = "SELECT FTP_BASE_DIR, FTP_SERVERHOST, FTP_SERVERPORT, FTP_USERNAME, FTP_PASSWORD AS FTP_PASSWORD"
				+ " FROM WMUSER.WM_FTP_CONNECTIONS " + " WHERE FTP_CONN_ID = '" + data.get("FTP_CONN_ID") + "'";

		// Paso 6

		String tdcQueryIntegrationServer = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60  "
				+ "FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE = 'PR23Reple'  "
				+ "ORDER BY START_DT DESC) where rownum <=1 ";

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA

		String tdcQueryLogRun = "SELECT * from (SELECT run_id,interface,start_dt,status,server"
				+ " FROM WMLOG.wm_log_run" + " WHERE interface = 'PR23Reple'" + " and start_dt >= trunc(sysdate) "
				+ " ORDER BY start_dt DESC) where rownum=1";

		// consultas de error
		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1"; // dbLog
		String consultaError2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1"; // dbLog
		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1"; // dbLog

		// Paso 8
		String tdcQueryStatusThread = "SELECT PARENT_ID,THREAD_ID,NAME,STATUS,ATT1,ATT2 " + " FROM WMLOG.WM_LOG_THREAD "
				+ " WHERE PARENT_ID = %s";

		// Paso 9
		String tdcQueryInboundPos = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, DOC_TYPE, PV_DOC_NAME "
				+ " FROM POSUSER.POS_INBOUND_DOCS " + " WHERE ID = %s " + " AND STATUS = 'E'";

		// Paso 10
		String tdcQueryRtk = "SELECT DOC_TYPE, DOC_ID, RUN_ID, STATUS, CR_PLAZA, CR_TIENDA "
				+ " FROM WMUSER.rtk_inbound_docs " + "WHERE run_id IN (%s)";

//Pasos ***********************************************************************************************************************************************************************		

		// PASO 1
		addStep("Tener tiendas disponibles en las tablas de POSUSER para procesar ");

		// Primera consulta
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(paso1);

		SQLResult paso1_Result = dbPos.executeQuery(paso1);

		boolean paso1_valida = paso1_Result.isEmpty(); // checa que el string contenga datos

		if (!paso1_valida) {

			testCase.addQueryEvidenceCurrentStep(paso1_Result); // Si no esta vacio, lo agrega a la evidencia
		} else if (paso1_valida) {
			testCase.addQueryEvidenceCurrentStep(paso1_Result);
			testCase.addTextEvidenceCurrentStep(
					"No encontro tiendas disponibles en las tablas de POSUSER para procesar");
		}

		System.out.println(paso1_valida);

//		assertFalse("No se encontraron registros a procesar ", paso1_valida); // Si esta vacio, imprime mensaje

		// *************************************************************************************************************************************************************************

		// PASO 2

		addStep(" Verificar que exista informacion dispoble para procesar en ORAFIN.");

		// Primera consulta
		System.out.println(GlobalVariables.DB_HOST_FCEBSSIT);
		System.out.println(paso2_qry1);

		SQLResult paso2_qry1_Result = dbEbs.executeQuery(paso2_qry1);

		boolean paso2_qry1_valida = paso2_qry1_Result.isEmpty(); // checa que el string contenga datos

		if (!paso2_qry1_valida) {

			testCase.addQueryEvidenceCurrentStep(paso2_qry1_Result); // Si no esta vacio, lo agrega a la evidencia
		} else if (paso2_qry1_valida) {

			testCase.addQueryEvidenceCurrentStep(paso2_qry1_Result);

		}

		System.out.println(paso2_qry1_valida);

		// Segunda consulta

		System.out.println(paso2_qry2);

		SQLResult paso2_qry2_Result = dbEbs.executeQuery(paso2_qry2);

		boolean paso2_qry2_valida = paso2_qry2_Result.isEmpty(); // checa que el string contenga datos

		if (!paso2_qry2_valida) {

			testCase.addQueryEvidenceCurrentStep(paso2_qry2_Result); // Si no esta vacio, lo agrega a la evidencia
		} else if (paso2_qry2_valida) {
			testCase.addQueryEvidenceCurrentStep(paso2_qry2_Result);
			testCase.addTextEvidenceCurrentStep("No encontro informacion dispoble para procesar en ORAFIN");
		}

		System.out.println(paso2_qry2_valida);

//		assertFalse("No hay informacion disponible para procesar ", paso2_qry2_valida); // Si esta vacio, imprime
		// mensaje

		// ***************************************************************************************************************************************************************

		// Paso 3

		addStep("Verificar que exista informacion en POSUSER para procesar.");

		// Primera consulta
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(paso3);

		SQLResult paso3_Result = dbPos.executeQuery(paso3);
		String ID = "";
		String PID_ID = "";

		boolean paso3_valida = paso3_Result.isEmpty(); // checa que el string contenga datos

		if (!paso3_valida) {

			testCase.addQueryEvidenceCurrentStep(paso3_Result); // Si no esta vacio, lo agrega a la evidencia
			ID = paso3_Result.getData(0, "ID");
			System.out.println("POS_INBOUND_DOCS.ID= " + ID);

			PID_ID = paso3_Result.getData(0, "PID_ID");
			System.out.println("POS_REC.ID= " + PID_ID);
		} else if (paso3_valida) {
			testCase.addQueryEvidenceCurrentStep(paso3_Result);
			testCase.addTextEvidenceCurrentStep("No encontro informacion en POSUSER para procesar");
		}

		System.out.println(paso3_valida);

//		assertFalse("No se encontraron registros a procesar ", paso3_valida); // Si esta vacio, imprime mensaje

		// ****************************************************************************************************************************************************************

		// Paso 4

		addStep("Validar que existan datos para procesar en POSUSER");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String paso4_format = String.format(paso4, ID);
		System.out.println(paso4_format);
		SQLResult paso4_Result = dbPos.executeQuery(paso4_format);

		boolean paso4_valida = paso4_Result.isEmpty(); // checa que el string contenga datos

		if (!paso4_valida) {

			testCase.addQueryEvidenceCurrentStep(paso4_Result); // Si no esta vacio, lo agrega a la evidencia
		} else if (paso4_valida) {
			testCase.addQueryEvidenceCurrentStep(paso4_Result);
			testCase.addTextEvidenceCurrentStep("No encontro datos para procesar en POSUSER");
		}

		System.out.println(paso4_valida);

//		assertFalse("No existen detalles de la informacion a procesar", paso4_valida); // Si esta vacio, imprime mensaje

//********************************************************************************************************************************************************

//Paso 5

		addStep("Validar la configuracion del servidor FTP en la tabla WM_FTP_CONNECTIONS.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcQueryConnections);

		SQLResult ftpConn = executeQuery(dbPos, tdcQueryConnections);

		boolean validaFTP = ftpConn.isEmpty();

		if (!validaFTP) {

			testCase.addQueryEvidenceCurrentStep(ftpConn);

		} else if (validaFTP) {
			testCase.addQueryEvidenceCurrentStep(ftpConn);
			testCase.addTextEvidenceCurrentStep("No se encontro  informacion del servidor FTP\"");
		}

		System.out.println(validaFTP);

//		assertFalse("No se encontro  informacion del servidor FTP", validaFTP); // Si esta vacio, imprime mensaje

//**********************************************************************************************************************************************************************
		// Paso 6

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server + ":5555";
		String searchedStatus = "R";
		String status = "S";
		String run_id;
		String statusWmLog = "S";

		addStep("Ejecutar el servicio " + data.get("servicio"));

		u.get(con);
		u.hardWait(4);

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

			SQLResult paso33 = executeQuery(dbLog, error);

			boolean emptyError = paso33.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(paso33);

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

		// **************************************************************************************************************************************
//Paso 7

		addStep("Validar que el registro de ejecucion de la interfaz termino en estatus 'S' en la tabla WM_LOG_RUN.");
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(tdcQueryLogRun);
		SQLResult exeQueryLogRun = executeQuery(dbLog, tdcQueryLogRun);
		String statusID = "";
		boolean validateLog = exeQueryLogRun.isEmpty();
		boolean validateStatus = false;

		if (!validateLog) {
			run_id = exeQueryLogRun.getData(0, "RUN_ID");
			System.out.println("RunID_Log: " + run_id);
			statusID = exeQueryLogRun.getData(0, "STATUS");
			System.out.println("Status_Log: " + statusID);

			validateStatus = statusWmLog.equals(statusID);
		}

		if (validateStatus) {
			testCase.addQueryEvidenceCurrentStep(exeQueryLogRun);
		} else if (!validateStatus) {
			testCase.addQueryEvidenceCurrentStep(exeQueryLogRun);
			testCase.addTextEvidenceCurrentStep(
					"El estatus del log es diferente a S ó No se encontraron datos en WM_Log_Run");
		}

		System.out.println(validateStatus);

//		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");// cambiar a true

//*****************************************************************************************************************************************

//Paso 8

		addStep("Validar que el registro de ejecucion de la plaza y tienda termina en estatus 'S' en la tabla WM_LOG_THREAD. ");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);
		SQLResult queryStatusThreadResult = dbLog.executeQuery(queryStatusThread);
		String THREAD_ID = "";
		String StatusThread = "";

		boolean validathread = queryStatusThreadResult.isEmpty();
		boolean validateStatusThr = false;

		if (!validathread) {
			THREAD_ID = queryStatusThreadResult.getData(0, "THREAD_ID");
			System.out.println("THREAD_ID: " + THREAD_ID);
			StatusThread = queryStatusThreadResult.getData(0, "STATUS");
			System.out.println("Status_thread: " + StatusThread);

			validateStatusThr = statusWmLog.equals(StatusThread);
		}

		if (validateStatusThr) {
			testCase.addQueryEvidenceCurrentStep(queryStatusThreadResult);
		} else if (!validateStatusThr) {
			testCase.addQueryEvidenceCurrentStep(queryStatusThreadResult);
			testCase.addTextEvidenceCurrentStep(
					"El estatus del thread es diferente a S o no se encontro informacion en WM_LOG_THREAD ");
		}

//		assertTrue(validateStatusThr,"El estatus del thread es diferente a S o no se encontro informacion en WM_LOG_THREAD");

//*******************************************************************************************************************************************
		// Paso 9
		addStep("Verificar los status en la tabla POS_INBOUND_DOCS.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		String posFormat = String.format(tdcQueryInboundPos, PID_ID);
		System.out.println(posFormat);

		SQLResult posResult = executeQuery(dbPos, posFormat);
		boolean posInbound = posResult.isEmpty();

		if (!posInbound) {

			testCase.addQueryEvidenceCurrentStep(posResult);
		} else if (posInbound) {
			testCase.addQueryEvidenceCurrentStep(posResult);
			testCase.addTextEvidenceCurrentStep("No se encontraron registros con estatus E en POS_INBOUND_DOCS ");
		}

		System.out.println(posInbound);

//      assertFalse(posInbound, "La tabla no contiene registros");

		// ***************************************************************************************************************************************

		// Paso 10

		addStep("Verificar el registro insertado en la tabla RTK_INBOUND_DOCS.");
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String rtkFormat = String.format(tdcQueryRtk, THREAD_ID);
		System.out.println(rtkFormat);

		SQLResult rtkResult = executeQuery(dbRms, rtkFormat);

		boolean rtkInbound = rtkResult.isEmpty();

		if (!rtkInbound) {

			testCase.addQueryEvidenceCurrentStep(rtkResult);

		} else if (rtkInbound) {
			testCase.addQueryEvidenceCurrentStep(rtkResult);
			testCase.addTextEvidenceCurrentStep("No se encontraron registros insertados en la tabla RTK_INBOUND_DOCS ");
		}

		System.out.println(rtkInbound);

      assertFalse(rtkInbound, "La tabla no contiene registros");

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "PR23_REPLE";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Confirmar a RMS los recibos de ordenes de compra que se generan por el proceso de reple en automatico ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
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
}
