package interfaces.po14;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class PO14_ValidarProcesoDeTransaccionesPagoConDolares extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PO14_002_Valida_Transaccion_Dolares(HashMap<String, String> data) throws Exception {
		
		
		/*
		 * Utilerías
		 ********************************************************************************************************************************************/
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Ebs, GlobalVariables.DB_USER_Ebs,
				GlobalVariables.DB_PASSWORD_Ebs);

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		utils.sql.SQLUtil dbUpd = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_UPD,
				GlobalVariables.DB_USER_FCWMQA_UPD, GlobalVariables.DB_PASSWORD_FCWMQA_UPD);
		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1

		String ValidInfo = "SELECT  a.ID, a.PE_ID,a.PV_DOC_ID,a.STATUS,a.DOC_TYPE,a.PV_DOC_NAME,a.RECEIVED_DATE,b.PID_ID "
				+ "FROM posuser.POS_INBOUND_DOCS a, posuser.POS_DLS b " + "WHERE STATUS = 'I' "
				+ "AND DOC_TYPE = 'DLS' " + "AND SUBSTR(PV_DOC_NAME, 4,5) = '" + data.get("Plaza") + "' "
				+ " AND A.ID = B.PID_ID";

//		Paso 3

		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'PO14'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  order by start_dt desc";

//		Paso 4
		String ValidTrhead = "SELECT * FROM WMLOG.WM_LOG_THREAD " + "WHERE PARENT_ID = '%s' " + "AND status = 'S'";
//	Paso 5
String VlidInsert = "Select * from(SELECT TRANS_ID,PLAZA,TIENDA,FOLIO,FECHA_RECEPCION  " + "FROM WMUSER.XXFC_DOLLAR_SALES "
			+ "WHERE PLAZA = '" + data.get("Plaza") + "' " + "order by FECHA_RECEPCION desc) where rownum <=10";
		
		// Paso 6
		String ValidStatus = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,PV_DOC_NAME,RECEIVED_DATE,INSERTED_DATE "
				+ "FROM POSUSER.POS_INBOUND_DOCS " + "WHERE  SUBSTR(PV_DOC_NAME, 4,5) = '" + data.get("Plaza") + "' "
				+ "AND DOC_TYPE = 'DLS' AND STATUS = 'E' ";

//****	

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='PO14' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		
/*
 * GENERAR INSUMO
 */
		
		String Upd1="update posuser.POS_INBOUND_DOCS "
				+ "	set ID = '6432028597' "
				+ "	where ID = '26437841372'";
				
				
		String Upd2="update POSUSER.POS_INBOUND_DOCS  "
				+ "set STATUS='I' "
				+ "where ID='6432028597'";
				


		
		dbUpd.executeUpdate(Upd1);
		dbUpd.executeUpdate(Upd2);
		dbUpd.executeQuery("commit");
		
		
		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************		

		addStep("Tener información para procesar de la plaza " + data.get("Plaza")
				+ " en las tablas: POS_INBOUND_DOCS, POS_DLS y POS_DLS_DETL en POSUSER.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(ValidInfo);

		SQLResult ExecValidInfo = dbPos.executeQuery(ValidInfo);

		boolean ValidaBool = ExecValidInfo.isEmpty();

		if (!ValidaBool) {

			testCase.addQueryEvidenceCurrentStep(ExecValidInfo);
		}

		System.out.println(ValidaBool);
		assertFalse(ValidaBool, "No existen registros para procesar de la plaza " + data.get("Plaza"));

//*************************************************Paso 2***********************************************************************************************************************

		addStep("Ejecutar el servicio: PO14.Pub:run solicitando la ejecución del Job: runPO14.");

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

		String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecución

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}

//**********************************************************Paso 3*************************************************************************************************************		

		addStep("Validar que la interfaz terminó sin errores.");
		String RunID = "";
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RunID = ExecLog.getData(0, "RUN_ID");
			System.out.println("RUN_ID: "+RunID);
			testCase.addQueryEvidenceCurrentStep(ExecLog);

		}

		System.out.println(LogRequest);
		assertFalse(LogRequest, "No se muestra la información del log.");

//		*************************************Paso 4 **************************************************

		addStep("Comprobar que exista registro en tabla WM_LOG_THREAD de la BD WMLOG,"
				+ " donde PARENT_ID es igual a WM_LOG_RUN.RUN_ID, STATUS igual a 'S'.");
		String ThreadID = "";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String ValidTrheadFormat = String.format(ValidTrhead, RunID);
		System.out.println(ValidTrheadFormat);
		SQLResult ExecValidTrhead = dbLog.executeQuery(ValidTrheadFormat);

		boolean ExecValidTrheadRes = ExecValidTrhead.isEmpty();

		if (!ExecValidTrheadRes) {
			ThreadID = ExecValidTrhead.getData(0, "THREAD_ID");
			System.out.println("ThreadID: "+ThreadID);
			testCase.addQueryEvidenceCurrentStep(ExecValidTrhead);

		}

		System.out.println(ExecValidTrheadRes);
		assertFalse(ExecValidTrheadRes, "No se muestra la información del log.");

//	***************************Paso 5*************************************************	

		addStep("Validar que se insertó correctamente la información en la tabla XXFC_DOLLAR_SALES de ORAFIN.");
		System.out.println(GlobalVariables.DB_HOST_Ebs);

		System.out.println(VlidInsert);

		SQLResult ExecVlidInsert1 = dbEbs.executeQuery(VlidInsert);
		
		boolean ExecVlidInsertRes = ExecVlidInsert1.isEmpty();
		
		if (!ExecVlidInsertRes) {

			testCase.addQueryEvidenceCurrentStep(ExecVlidInsert1);

		}

		System.out.println(ExecVlidInsertRes);
		assertFalse(ExecVlidInsertRes,
				"No se insertó correctamente la información en la tabla XXFC_DOLLAR_SALES de ORAFIN.");

//*********************************Paso 6*********************************************************

		addStep("Validar que el estatus de los registros fue cambiado a \"E\" en la tabla POS_INBOUND_DOCS");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String ValidStatusFormat = String.format(ValidStatus);
		System.out.println(ValidStatusFormat);
		SQLResult ExecValidStatusFormat = dbPos.executeQuery(ValidStatusFormat);
		boolean ExecValidStatusFormatReq = ExecValidStatusFormat.isEmpty();
		System.out.println(ExecValidStatusFormatReq);

		if (!ExecValidStatusFormatReq) {
			testCase.addQueryEvidenceCurrentStep(ExecValidStatusFormat);
		}

		assertFalse(ExecValidStatusFormatReq, "No se actualizo  el estatus de los registros");

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PO14_002_Valida_Transaccion_Dolares";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Validar que se inserte la información en la tabla XXFC_DOLLAR_SALES de ORAFIN";
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
