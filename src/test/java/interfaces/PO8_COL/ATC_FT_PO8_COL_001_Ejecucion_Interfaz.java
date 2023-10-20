package interfaces.PO8_COL;

import static org.junit.Assert.assertFalse;
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

public class ATC_FT_PO8_COL_001_Ejecucion_Interfaz extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PO8_COL_001_Ejecucion_Interfaz_test(HashMap<String, String> data) throws Exception {
		/*
		 * Utilería   s
		 *********************************************************************/
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS_COL, GlobalVariables.DB_USER_EBS_COL,
				GlobalVariables.DB_PASSWORD_EBS_COL);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		String DSI = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, doc_type,PV_DOC_NAME, RECEIVED_DATE  " + "  FROM POSUSER.POS_INBOUND_DOCS  "
				+ "  WHERE STATUS='I' AND DOC_TYPE='SDI' AND SUBSTR(PV_DOC_NAME,4,5)= '" + data.get("plaza") + "' "
				+ "  AND RECEIVED_DATE > SYSDATE-4 " + "  order by  received_date desc";

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%PO8%' "
				+ "ORDER BY START_DT DESC) where rownum <=3";// Consulta para estatus de la ejecucion
		String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// Consulta para los errores
		String consultaERROR2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores
		String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores

		String fin = "SELECT  CR_PLAZA, TRAN_CODE_RETEK, TRAN_CODE_POS, FOLIO, TOTAL_PRECIO_COSTO, TOTAL_PRECIO_VENTA, INTERFACE_DATE, VAT_CODE\r\n"
				+ "FROM XXFC_SALDO_DIARIO_POS WHERE CR_PLAZA='" + data.get("plaza")
				+ "' AND INTERFACE_DATE>=TRUNC(SYSDATE) ";
		

		String DSIE = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, doc_type,PV_DOC_NAME, RECEIVED_DATE " + "  FROM POSUSER.POS_INBOUND_DOCS  "
				+ "  WHERE STATUS='E' AND DOC_TYPE='SDI' AND SUBSTR(PV_DOC_NAME,4,5)= '" + data.get("plaza") + "' "
				+ "  AND RECEIVED_DATE > SYSDATE-4 " + "  order by  received_date desc";

		/*
		 * paso 1 Validar documentos SDI disponibles para procesar en POSUSER. .
		 **********************/
		addStep("Validar documentos SDI disponibles para procesar en POSUSER.");
		System.out.println("Respuesta " + DSI);
		SQLResult DSI1 = dbPos.executeQuery(DSI);
		System.out.println("Respuesta " + DSI1);
		// String retek = plazas.getData(0, "RETEK_CR");
		// System.out.println("Respuesta " + retek);
		boolean paso1 = DSI1.isEmpty();
		if (!paso1) {
			testCase.addQueryEvidenceCurrentStep(DSI1);
		}
		assertFalse("No hay insumos a procesar", paso1);

		addStep("Ejecutar el servicio PO8 COL.");
		String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server;
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		u.hardWait(4);
		// String dateExecution = null;
		// String dateExecution ="";

		String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);

		SQLResult is = executeQuery(dbLog, tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");
		String run_id = is.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			// is = SQLUtil.getVaribleDataIntegrationServer(testCase, dbLOG,
			// tdcIntegrationServerFormat, "STATUS", "RUN_ID");
			is = executeQuery(dbLog, tdcIntegrationServerFormat);

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(consultaERROR, run_id);
			String error1 = String.format(consultaERROR2, run_id);
			String error2 = String.format(consultaERROR3, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontr? un error en la ejecuci?n de la interfaz en la tabla WM_LOG_ERROR");

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

		// Paso 3 ************************
		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run ");
		SQLResult is1 = executeQuery(dbLog, tdcIntegrationServerFormat);

		String fcwS = is1.getData(0, "STATUS");
		// String fcwS = SQLUtil.getColumn(testCase, dbLOG, tdcIntegrationServerFormat,
		// "STATUS");
		boolean validateStatus = fcwS.equals(status);
		System.out.println(validateStatus);
		assertTrue(validateStatus, "La ejecuci?n de la interfaz no fue exitosa");
		SQLResult log = dbLog.executeQuery(tdcIntegrationServerFormat);
		System.out.println("Respuesta " + log);
		// SQLResult errorIS= dbLOG.executeQuery(error1);

		boolean log1 = log.isEmpty();
		// boolean av2 = SQLUtil.isEmptyQuery(testCase, dbLOG,
		// tdcIntegrationServerFormat);
		if (!log1) {

			testCase.addQueryEvidenceCurrentStep(log);
		}

		System.out.println(log1);
		assertFalse("r", log1);

		// Paso 4 ************************
		addStep("Verificar la información enviada a FINANZAS. ");
		System.out.println("Respuesta " + fin);
		SQLResult finn = dbEbs.executeQuery(fin);
		System.out.println("Respuesta " + finn);
		// String retek = plazas.getData(0, "RETEK_CR");
		// System.out.println("Respuesta " + retek);
		boolean paso4 = finn.isEmpty();
		if (!paso4) {
			testCase.addQueryEvidenceCurrentStep(finn);
		}

		
		/*
		 * paso 5 Verificar el estatus de los documentos enviados. .
		 **********************/
		addStep("Verificar el estatus de los documentos enviados.");
		System.out.println("Respuesta " + DSIE);
		SQLResult DSI1E = dbPos.executeQuery(DSIE);
		System.out.println("Respuesta " + DSI1E);
		// String retek = plazas.getData(0, "RETEK_CR");
		// System.out.println("Respuesta " + retek);
		boolean paso1E = DSI1E.isEmpty();
		if (!paso1E) {
			testCase.addQueryEvidenceCurrentStep(DSI1E);
		}
		assertFalse("No hay insumos a procesar", paso1E);

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
		return "Terminado. Verificar proceso normal de ejecución de la interfaz PO8_COL.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA AUTOMATION";
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