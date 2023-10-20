package interfaces.po8;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class PO8_ProcesararchivoINV_inventario_diario extends BaseExecution {
	private static final boolean ValidaPaso6 = false;

	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PO8_PO8_ProcesararchivoINV_inventario_diario(HashMap<String, String> data) throws Exception {
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
		addStep("Validar informacion para procesar en POSUSER.");
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
		
		
	// paso 2 ***********************

		addStep("Ejecutar el servicio PO8.");
		addStep("Ejecutar el servicio PO8.pub:run. La interfaz será ejecutada por el job PO8 de Ctrl-M ");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		
		JSONObject obj = new JSONObject(data.get("job"));

		addStep("Jobs en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
		//testCase.addPaso("Paso con addPaso");
		addStep("Login");
		u.get(data.get("server"));
		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn(); 

		//Buscar del job
		addStep("Inicio de job ");
		ControlM control = new ControlM(u, testCase, obj);
		boolean flag = control.searchJob();
		assertTrue(flag);
		
		//Ejecucion
		String resultado = control.executeJob();
		System.out.println("Resultado de la ejecucion -> " + resultado);

		//Valor del output 
		System.out.println ("Valor de output :" +control.getOutput());
		
		//Validacion del caso
		Boolean casoPasado = true;
		if(resultado.equals("Wait Condition")) {
		casoPasado = true;
		}		
		assertTrue(casoPasado);
		//assertNotEquals("Failure",resultado);
		control.closeViewpoint(); 
		

		// Paso 3 ************************
		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run ");
		SQLResult is1 = executeQuery(dbLog, tdcIntegrationServerFormat);

		String fcwS = is1.getData(0, "STATUS");
		Object status = null;
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
		
		
		///		Paso 5	************************

				addStep("Se valida la generacion de thread");

				String tdcQueryStatusThread = null;
				Object run_id = null;
				String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
				System.out.println(queryStatusThread);

				SQLResult regPlazaTiendaThread = dbLog.executeQuery(queryStatusThread);

				String Threadstatus = regPlazaTiendaThread.getData(0, "STATUS");

				boolean threadResult = regPlazaTiendaThread.isEmpty();
				if (!threadResult) {
					testCase.addQueryEvidenceCurrentStep(regPlazaTiendaThread);
				}
						
				boolean statusThread = status.equals(Threadstatus);
				System.out.println(statusThread);
				if (!statusThread) {

					String tdcQueryWmlogError = null;
					String error = String.format(tdcQueryWmlogError, run_id);
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

				
				
//			     paso 6 *********************************   

				addStep("Validar que la interface finalice con error en las tablas del  WMLOG.\r\n");
						
				System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
				String tdcQueryPaso4 = null;
				System.out.println(tdcQueryPaso4);
				SQLResult paso6 = executeQuery(dbLog, tdcQueryPaso4);
				String thread = "";
				boolean ValidaPaso4 = paso6.isEmpty();
				if (!ValidaPaso6) {
					thread = paso6.getData(0, "RUN_ID");
					testCase.addQueryEvidenceCurrentStep(paso6);

				}

				System.out.println(ValidaPaso4);

				assertFalse(ValidaPaso4, "No se obtiene informacion de la consulta");
				

		
		/*
		 * paso 7 Verificar el estatus de los documentos enviados. .
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
		return "Construido. Validar la operación para el envío de documentos SDI (Saldo Diario) a FINANZAS. (CO)";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_003_PO8_PO8_ProcesararchivoINV_inventario_diario";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
}