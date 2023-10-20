package interfaces.re2;

import static org.junit.Assert.assertFalse;


import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ATC_FT_001_RE2_ValidaEnvioOrdenesCompraRMS extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_RE2_ValidaEnvioOrdenesCompraRMS_test(HashMap<String, String> data) throws Exception {

		
		/*
		 * Utilerias
		 ********************************************************************************************************************************************/

		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
				GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		/**
		 * ALM
		 * Validar el envio de Ordenes de compra a RMS (runSend)
		 */

		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1
		String supplier = "";
		String ValidDat = "SELECT EID_ID,ORDER_NUMBER,LOCATION, DOC_TYPE,STATUS,SUPPLIER" + " FROM WMUSER.EDI_OCP P "
				+ "INNER JOIN WMUSER.EDI_INBOUND_DOCS D ON D.ID = P.EID_ID  " + "WHERE  D.STATUS = 'L' AND D.DOC_TYPE = 'OCP'";

		// Paso 2
		String VerifiConf = "SELECT  S.SUPPLIER, S.FILETYPE, S.STATUS,S.COST,S.ID,I.STATUS_TYPE,I.COMMAND_ID   "
				+ "FROM WMUSER.WM_EDI_MAP_SUPPLIER S "
				+ "INNER JOIN WMUSER.WM_EDI_INTERCHANGE I ON S.SUPPLIER = I.SUPPLIER " + 
				"WHERE  I.SUPPLIER = '%s'";

//		Paso 4
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'RE2_Send'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  ORDER BY RUN_ID DESC";

		String validThread = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,STATUS,ATT1,ATT2  " + "FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID = '%s' ";

//	Paso 5 
		String ValidInsert = "SELECT D.VENDOR_ORDER_NO,D.SUPPLIER,D.LOCATION,D.ITEM,WM_RUN_ID,WM_STATUS,STATUS_ORDER "
				+ "FROM  RMS100.FEM_VMI_ORDERS_HEAD H " + "INNER JOIN RMS100.FEM_VMI_ORDERS_DETAIL D  "
				+ "ON D.SUPPLIER = H.SUPPLIER " + "AND H.VENDOR_ORDER_NO = D.VENDOR_ORDER_NO  "
				+ "AND H.LOCATION = D.LOCATION WHERE WM_SENT_DATE >= TRUNC(SYSDATE)  " + "AND WM_RUN_ID = '%s' "
				+ "AND H.WM_STATUS = 'I'";

//	Paso 6 
		String ValidUpdate = "SELECT * FROM WMUSER.EDI_INBOUND_DOCS " + " WHERE STATUS = 'E'  "
				+ "AND RUN_ID_SENT = '%s' " + "AND SENT_DATE >= TRUNC(SYSDATE)";
//****	

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='RE2_Send' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************

		addStep("Verificar que existan datos en las tablas EDI_OCP y EDI_INBOUND_DOCS de la BD del WMINT. ");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(ValidDat);

		SQLResult validaDatRes = dbPos.executeQuery(ValidDat);

		boolean ValidaDatBool = validaDatRes.isEmpty(); // checa que el string contenga datos

		if (!ValidaDatBool) {
			supplier = validaDatRes.getData(0, "SUPPLIER");

			System.out.println("supplier= " + supplier);

			testCase.addQueryEvidenceCurrentStep(validaDatRes); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(ValidaDatBool); // Si no, imprime la fechas
		assertFalse("No se muestran datos en las tablas EDI_OCP y EDI_INBOUND_DOCS", ValidaDatBool); // Si esta vacio,
																										// imprime
		// mensaje

//*************************************************Paso 2***********************************************************************************************************************

		addStep("Verificar la configuraci�n del Proveedor en las tablas WM_EDI_MAP_SUPPLIER y WM_EDI_INTERCHANGE de la BD RETEK;");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);// RMS

		String VerifiConfFormat = String.format(VerifiConf, supplier);

		System.out.println(VerifiConfFormat);

		SQLResult verif = dbRms.executeQuery(VerifiConfFormat);

		boolean validRes = verif.isEmpty();

		if (!validRes) {

			testCase.addQueryEvidenceCurrentStep(verif);

		}

		System.out.println(validRes);
		assertFalse("No se muestra la configuraci�n del Proveedor", validRes);

//**********************************************************Paso 3*************************************************************************************************************		

		addStep("Se ejecuta el proceso RE2.Pub:runSend, solicitando el job: runRE2_Send");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String status = "S";

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecuci�n

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(3);

		}

////*********************************Paso 4*********************************************************

		addStep("Verificar que la ejecucion sea registrada en las tablas de la BD WMLOG.");
		String RunID = "";
		String ThreadID = "";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RunID = ExecLog.getData(0, "RUN_ID");

			String ValidThreadFormat = String.format(validThread, RunID);
			System.out.println(ValidThreadFormat);
			SQLResult ExecThread = dbLog.executeQuery(ValidThreadFormat);

			ThreadID = ExecThread.getData(0, "THREAD_ID");
			testCase.addQueryEvidenceCurrentStep(ExecLog);
			testCase.addQueryEvidenceCurrentStep(ExecThread);

		}

		System.out.println(LogRequest);
		assertFalse("No se muestra  la informaci�n.", LogRequest);

//********************************************Paso 5**************************************************************************************************************************

		addStep("Verificar que se inserte los datos en las tablas FEM_VMI_ORDERS_HEAD y FEM_VMI_ORDERS_DETAIL de  la BD RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);// RMS
		String ValidInsertFormat = String.format(ValidInsert, ThreadID);
		System.out.println(ValidInsertFormat);
		SQLResult ExecuteValidIns = dbRms.executeQuery(ValidInsertFormat);

		boolean ExecuteValidInsReq = ExecuteValidIns.isEmpty();

		if (!ExecuteValidInsReq) {

			testCase.addQueryEvidenceCurrentStep(ExecuteValidIns);

		}

		System.out.println(ExecuteValidInsReq);
		assertFalse("No se muestran datos insertados", ExecuteValidInsReq);

//*********************************************************Paso 6 **********************************************************************************************
		addStep("Verificar que se actualizaron correctamente los datos en la tabla EDI_INBOUND_DOCS de la BD WMUSER");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String ValidUpdtFormat = String.format(ValidUpdate, RunID);
		System.out.println(ValidUpdtFormat);
		SQLResult ExecuteUpdtFormat = dbPos.executeQuery(ValidUpdtFormat);

		boolean ExecuteValidUpdtReq = ExecuteUpdtFormat.isEmpty();

		if (!ExecuteValidUpdtReq) {

			testCase.addQueryEvidenceCurrentStep(ExecuteUpdtFormat);

		}

		System.out.println(ExecuteValidUpdtReq);
		assertFalse("No se muestran datos insertados", ExecuteValidUpdtReq);

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar el envio de Ordenes de compra a RMS (runSend)";
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
