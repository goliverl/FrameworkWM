package interfaces.pr23Reple_CL;

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
import utils.sql.SQLUtil;

public class PR23Reple_CL_EnviarOrdenesDeCompras extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PR23_REPLE_CL_Enviar_Ordenes_Compras(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 ********************************************************************************************************************************************/
		SQLUtil dbEbsCL = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,
				GlobalVariables.DB_PASSWORD_OIEBSBDQ);
		SQLUtil dbLogCL = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile,
				GlobalVariables.DB_PASSWORD_LogChile);
		SQLUtil dbPosCL = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile,
				GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
		SQLUtil dbRmsCL = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERChile,
				GlobalVariables.DB_USER_RMSWMUSERChile, GlobalVariables.DB_PASSWORD_RMSWMUSERChile);

		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1
		String TiendasDisp = "SELECT PV_CR_PLAZA, PV_CR_TIENDA "
				+ "FROM POSUSER.POS_ENVELOPE A, POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C "
				+ "WHERE B.PE_ID = A.ID " + "AND B.DOC_TYPE = 'REC' " + "AND B.STATUS = 'I' " + "AND C.PID_ID = B.ID "
				+ "AND C.PV_CVE_MVT='10' " + "AND C.ORDER_NO <> 0 " + "AND B.PARTITION_DATE > SYSDATE -45"
				+ "GROUP BY PV_CR_PLAZA, PV_CR_TIENDA ";
		// Paso 2
		String VerifInfOrafin = "SELECT ORACLE_CR, ORACLE_CR_SUPERIOR, ORACLE_EF, "
				+ "ORACLE_EF_DESC, ORACLE_CIA, LEGACY_EF, LEGACY_CR, RETEK_CR, RETEK_DISTRITO "
				+ "FROM XXFC.XXFC_MAESTRO_DE_CRS_V " + "WHERE ESTADO = 'A' " + "AND ORACLE_CR_SUPERIOR = '"
				+ data.get("plaza") + "' " + "AND ORACLE_CR = '" + data.get("tienda") + "' "
				+ "AND ORACLE_CR_TYPE = 'T' ";

		// Paso 3
		String VerifPos = "SELECT B.ID, C.PID_ID, PV_CVE_MVT, TO_CHAR(CREATED_DATE,'YYYYMMDDHH24MISS') CREATED_DATE, "
				+ "PV_CED_ID, PV_CR_FROM_LOC, TSF_NO, EXT_REF_NO, ORDER_NO "
				+ "FROM POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C " + "WHERE SUBSTR(PV_DOC_NAME,4,10)= '"
				+ data.get("plaza") + "' || '" + data.get("tienda") + "' " + "AND B.DOC_TYPE='REC' "
				+ "AND STATUS ='I' " + "AND C.PID_ID=B.ID " + "AND C.PV_CVE_MVT=10 " + "AND C.ORDER_NO<>0 "
				+ "AND B.PARTITION_DATE > SYSDATE -45 ";
//		Paso 4
		String VerifPosRec = "SELECT ITEM, RECEIVED_QTY, NVL(CARTON,0) CARTON, BOL_NO,  PV_RETAIL_PRICE, NVL(REMISION,0) REMISION "
				+ "FROM POSUSER.POS_REC_DETL " + "WHERE PID_ID = '%s'";
		// Paso 5
		String ValidFTP = "SELECT FTP_BASE_DIR, FTP_SERVERHOST, FTP_SERVERPORT, FTP_USERNAME, FTP_PASSWORD AS FTP_PASSWORD"
				+ " FROM WMUSER.WM_FTP_CONNECTIONS " + " WHERE FTP_CONN_ID = 'RTKRMS' ";

//		Paso 7
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'PR23Reple'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  ORDER BY START_DT DESC";

		// Paso 8
		String LogThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = '%s'";

		// Paso 9

		String ValidStatus = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, DOC_TYPE, PV_DOC_NAME "
				+ " FROM POSUSER.POS_INBOUND_DOCS " + " WHERE ID = %s " + " AND STATUS = 'E'";

		// Paso 10
		String VerifInsert = "SELECT DOC_TYPE, DOC_ID, RUN_ID, STATUS, CR_PLAZA, CR_TIENDA "
				+ " FROM WMUSER.rtk_inbound_docs " + "WHERE run_id IN ('%s')";

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='PR23Reple' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//		Paso 1	************************	

		addStep("Tener tiendas disponibles en las tablas de POSUSER Chile para procesar");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(TiendasDisp);
		SQLResult TiendasDispExc = dbPosCL.executeQuery(TiendasDisp);

		boolean TiendasDispRes = TiendasDispExc.isEmpty();

		if (!TiendasDispRes) {

			testCase.addQueryEvidenceCurrentStep(TiendasDispExc);
		}

		System.out.println(TiendasDispRes);

		assertFalse("No se encontraron registros a procesar ", TiendasDispRes);

//		Paso 2	************************

		addStep(" Verificar que exista informacion dispoble para procesar en ORAFIN chile.");
		System.out.println(GlobalVariables.DB_HOST_OIEBSBDQ);
		System.out.println(VerifInfOrafin);
		SQLResult ExecVerifInfOrafin = dbEbsCL.executeQuery(VerifInfOrafin);
		boolean VerifInfOrafinRes = ExecVerifInfOrafin.isEmpty();
		if (!VerifInfOrafinRes) {

			testCase.addQueryEvidenceCurrentStep(ExecVerifInfOrafin);
		}

		System.out.println(VerifInfOrafinRes);
		assertFalse("No hay informacion disponible para procesar", VerifInfOrafinRes);

//		Paso 3	************************

		addStep("Verificar que exista informacion en POSUSER para procesar.");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(VerifPos);
		SQLResult ExecVerifPos = dbPosCL.executeQuery(VerifPos);
		String ID = "";
		String PID_ID = "";

		boolean VerifPosRes = ExecVerifPos.isEmpty();

		if (!VerifPosRes) {

			testCase.addQueryEvidenceCurrentStep(ExecVerifPos);
			ID = ExecVerifPos.getData(0, "ID");
			System.out.println("POS_INBOUND_DOCS.ID= " + ID);
			PID_ID = ExecVerifPos.getData(0, "PID_ID");
			System.out.println("POS_REC.ID= " + PID_ID);
		}

		System.out.println(VerifPosRes);

		assertFalse("No se encontraron registros a procesar ", VerifPosRes);

//		Paso 4	************************

		addStep("Validar que existan datos para procesar en POSUSER");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		String VerifPosRecFormat = String.format(VerifPosRec, ID);
		System.out.println(VerifPosRecFormat);
		SQLResult ExecVerifPosRecFormat = dbPosCL.executeQuery(VerifPosRecFormat);

		boolean VerifPosRecFormatRes = ExecVerifPosRecFormat.isEmpty();

		if (!VerifPosRecFormatRes) {

			testCase.addQueryEvidenceCurrentStep(ExecVerifPosRecFormat);
		}

		System.out.println(VerifPosRecFormatRes);

		assertFalse("No existen detalles de la informacion a procesar", VerifPosRecFormatRes);

		// Paso 5 ************************************

		addStep("Validar la configuracion del servidor FTP en la tabla WM_FTP_CONNECTIONS.");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(ValidFTP);

		SQLResult ftpConn = executeQuery(dbPosCL, ValidFTP);

		boolean validaFTP = ftpConn.isEmpty();

		if (!validaFTP) {

			testCase.addQueryEvidenceCurrentStep(ftpConn);

		}

		System.out.println(validaFTP);

		assertFalse("No se encontro  informacion del servidor FTP", validaFTP);
//		Paso 6 ******************************************************

		// Utileria
		addStep("Ejecutar el servicio PR23Reple.Pub:run invocando el Job execPR23reple.");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLogCL.executeQuery(tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecución

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(3);

		}

//		Paso 7	************************

		addStep("Verificar que la interfaz termino con exito en WMLOG. ");

		String RUN_ID = "";
		System.out.println(GlobalVariables.DB_HOST_LogChile);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLogCL.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RUN_ID = ExecLog.getData(0, "RUN_ID");
			System.out.println("RUN_ID: " + RUN_ID);
			testCase.addQueryEvidenceCurrentStep(ExecLog);
		}

		System.out.println(LogRequest);
		assertFalse(LogRequest, "No se muestra la información.");

//		Paso 8	************************

		addStep("Verificar los threads generados durante la ejecucion");
		String THREAD_ID = "";
		System.out.println(GlobalVariables.DB_HOST_LogChile);
		String threadFormat = String.format(LogThread, RUN_ID);
		System.out.println(threadFormat);
		SQLResult ExecLogThread = dbLogCL.executeQuery(threadFormat);

		boolean LogRequestTh = ExecLogThread.isEmpty();

		if (!LogRequestTh) {

			THREAD_ID = ExecLogThread.getData(0, "thread_id");
			System.out.println("THREAD_ID: " + THREAD_ID);
			testCase.addQueryEvidenceCurrentStep(ExecLogThread);
		}

		System.out.println(LogRequestTh);
		assertFalse(LogRequestTh, "No se muestra  la información.");

//		Paso 9	************************

		addStep("Verificar los status en la tabla POS_INBOUND_DOCS.");
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		String posFormat = String.format(ValidStatus, PID_ID);
		System.out.println(posFormat);
		SQLResult posResult = executeQuery(dbPosCL, posFormat);
		boolean posInbound = posResult.isEmpty();

		if (!posInbound) {
			testCase.addQueryEvidenceCurrentStep(posResult);
		}

		System.out.println(posInbound);

		assertFalse(posInbound, "La tabla no contiene registros");

//		Paso 10	************************

		addStep("Verificar el registro insertado en la tabla RTK_INBOUND_DOCS");
		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);
		String VerifInsertFormat = String.format(VerifInsert, THREAD_ID);
		System.out.println(VerifInsertFormat);

		SQLResult rtkResult = executeQuery(dbRmsCL, VerifInsertFormat);
		boolean rtkInbound = rtkResult.isEmpty();

		if (!rtkInbound) {

			testCase.addQueryEvidenceCurrentStep(rtkResult);

		}

		System.out.println(rtkInbound);

		assertFalse(rtkInbound, "La tabla no contiene registros");

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PR23_REPLE_CL_Enviar_Ordenes_Compras";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Enviar ordenes de compras.";
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
