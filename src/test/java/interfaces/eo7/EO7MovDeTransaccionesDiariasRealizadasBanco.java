package interfaces.eo7;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;
import org.openqa.selenium.By;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import net.sf.cglib.transform.impl.AddStaticInitTransformer;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/**
 * @author 41335
 */

public class EO7MovDeTransaccionesDiariasRealizadasBanco extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_EO07_002_Mov_Transacciones_Banco(HashMap<String, String> data) throws Exception {

		testCase.setPrerequisites("Prerequiistos");

		/*
		 * Utiler�as
		 *********************************************************************/
		utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbEBS = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);

		/**
		 * ALM
		 * Validar que la interface obtenga y procese correctamente el archivo para la cuenta 0155999227
		 * Validar que la interface obtenga y procese correctamente el archivo para la cuenta 0198875065
		 * Validar que la interface obtenga y procese correctamente el archivo, para la cuenta 0155999227.
		 * Validar que la interface obtenga y procese correctamente el archivo, para la cuenta 0198875065.
		 */
		
		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

//	Paso 1*********************************************************************
		String tdcQueryFTPConexions = "SELECT operacion,valor1,valor2,valor3,valor4,valor5,valor6,descripcion"
				+ " FROM WMUSER.WM_INTERFASE_CONFIG" + " WHERE INTERFASE = 'EO07'" + " AND OPERACION IN ('FS', 'FTP')";
//	Paso 2 *******************************************************************
		String tdcQueyBANKAccounts = "SELECT BANK_ACCOUNT_ID" + " FROM CE_BANK_ACCOUNTS"
				+ " WHERE SUBSTR(BANK_ACCOUNT_NUM, -10) = SUBSTR('" + data.get("Bank") + "', 15, 10)"
				+ " AND ATTRIBUTE4 = 'Y'";

//	Paso 4 ********************************************************************
		String tdcQueryLogRun = "SELECT * from (SELECT run_id,interface,start_dt,status,server"
				+ " FROM WMLOG.wm_log_run" + " WHERE interface = 'EO07'" + " and start_dt >= trunc(sysdate) "
				+ " ORDER BY start_dt DESC) where rownum=1";
//	Paso 5 *******************************************************************
		String tdcQueryORAFINBank = " SELECT * " + " FROM XXOG.XXOG_AR_MOV_BANCO" + " WHERE NUMERO_CUENTA = SUBSTR('"
				+ data.get("Bank") + "', 15, 10)" + " AND TRUNC(CREATION_DATE) = TRUNC(SYSDATE)";

//Paso 3 **************************************	********************************
		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'EO07' " + " and  start_dt >= TRUNC(SYSDATE)" + " order by start_dt desc)"
				+ " where rownum = 1";
//******************************************************************************************
		SeleniumUtil u;

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";
		String status = "S";
		String statusPos = "E";
		String statusPosInv = "I";
		String id, tarCol, tarMex, run_id;
		String statusWmLog = "S";

		/**
		 * Pasos
		 * ******************************************************************************************
		 * 
		 * 
		 */
		// Paso 1
		addStep("Validar que se tenga configuradas las conexiones de FTP en la tabla WM_INTERFASE_CONFIG para la interfaces EO07 en la BD de WMINT.");

		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryFTPConexions);

		SQLResult conn = executeQuery(dbPuser, tdcQueryFTPConexions);
		boolean connFTP = conn.isEmpty();
		System.out.print("connftp: " + connFTP);
		if (!connFTP) {

			testCase.addQueryEvidenceCurrentStep(conn);

		}

		assertFalse(connFTP, "No se obtiene la conexion");

//Paso 2
		addStep("Validar que el archivo contenga una cuenta existente en la tabla CE_BANK_ACCOUNTS.");

		System.out.println(GlobalVariables.DB_HOST_EBS);
		System.out.println(tdcQueyBANKAccounts);

		SQLResult bank = executeQuery(dbEBS, tdcQueyBANKAccounts);

		boolean trBank = bank.isEmpty();
		System.out.println(trBank);
		if (!trBank) {
			testCase.addQueryEvidenceCurrentStep(bank);

		}

		assertFalse(trBank, "La consulta no regresa datos para el numero de cuenta.");

//Paso 3
		addStep("Ejecutar el servicio EO07.Pub:run  para procesar la informaci�n de los movimientos de transacciones diarias en el banco e insertar la informaci�n en la tabla XXOG.XXOG_AR_MOV_BAN");

		u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String con = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(con);
		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		String tdcIntegrationServerFormat = String.format(tdcQueryIntegrationServer, dateExecution);
		SQLResult tdcIntegrationServerResult = dbLog.executeQuery(tdcIntegrationServerFormat);

		run_id = tdcIntegrationServerResult.getData(0, "RUN_ID");// guarda el run id de la ejecuci�n

		boolean valuesStatus = tdcIntegrationServerResult.getData(0, "STATUS").equals(searchedStatus);// Valida si se
																										// encuentra en
																										// estatus R

		while (valuesStatus) {

			tdcIntegrationServerResult = dbLog.executeQuery(tdcIntegrationServerFormat);

			valuesStatus = tdcIntegrationServerResult.getData(0, "STATUS").equals(searchedStatus);// Valida si se
																									// encuentra en
																									// estatus R

			u.hardWait(2);

		}

		boolean successRun = tdcIntegrationServerResult.getData(0, "STATUS").equals(status);// Valida si se encuentra en
																							// estatus S

		if (!successRun) {

			String error = String.format(tdcQueryErrorId, run_id);

			SQLResult er = dbLog.executeQuery(error);
			boolean emptyError = er.isEmpty();
			System.out.println(emptyError);
			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se inserta los detalles del error generado durante la ejecuci�n de la interface en la tabla WM_LOG_ERROR de WMLOG.");

				testCase.addQueryEvidenceCurrentStep(er);

			}
		}

//Paso 4	
		addStep("Validar la correcta ejecuci�n de la interface EO07 en la tabla WM WM_LOG_RUN de WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(tdcQueryLogRun);
		SQLResult exeQueryLogRun = executeQuery(dbLog, tdcQueryLogRun);
		boolean validateStatus = exeQueryLogRun.isEmpty();

		if (!validateStatus) {
			testCase.addQueryEvidenceCurrentStep(exeQueryLogRun);
		}
		System.out.println(validateStatus);

		assertFalse(validateStatus,
				"Se inserta el detalle de la ejecuci�n de la interface EO07 en la tabla WM_LOG_RUN de WMLOG con STATUS='E'");

//Paso 5

		addStep("Validar la inserci�n de la informaci�n contenida en el archivo de banco en la tabla XXOG.XXOG_AR_MOV_BANCO de ORAFIN.");

		System.out.println(GlobalVariables.DB_HOST_EBS);
		System.out.println(tdcQueryORAFINBank);

		SQLResult ar = executeQuery(dbEBS, tdcQueryORAFINBank);
		boolean trarchiv = ar.isEmpty();
		System.out.println(trarchiv);
		if (!trarchiv) {

			testCase.addQueryEvidenceCurrentStep(ar);
		}

		assertFalse(trarchiv, "EL archivo no fue insertado correctamente.");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_EO07_002_Mov_Transacciones_Banco";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Valida que la interface obtenga y procese correctamente el archivo de la cuenta."; 
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
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

}
