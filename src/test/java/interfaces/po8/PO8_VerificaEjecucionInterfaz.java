package interfaces.po8;

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
import utils.sql.SQLResultExcel;

public class PO8_VerificaEjecucionInterfaz extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_P08_004_Ejecucion_Interfaz_Plaza_test (HashMap<String, String> data) throws Exception {
		/*
		 * Utilerías
		 *********************************************************************/
		//utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_POSUSER_DEV,GlobalVariables.DB_USER_POSUSER_DEV, GlobalVariables.DB_PASSWORD_POSUSER_DEV);
		//utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS_DEV, GlobalVariables.DB_USER_EBS_DEV,GlobalVariables.DB_PASSWORD_EBS_DEV);
		//utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);		
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		String DSI = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, doc_type,PV_DOC_NAME, RECEIVED_DATE \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ "WHERE STATUS='I' \r\n"
				+ "AND DOC_TYPE='SDI' \r\n"
				+ "AND SUBSTR(PV_DOC_NAME,4,5)= '" + data.get("plaza") + "' \r\n"
				+ "AND RECEIVED_DATE < SYSDATE-2 \r\n" 
				+ "order by  received_date desc ";

		String tdcIntegrationServerFormat = "select run_id,interface, start_dt, end_dt, status, server \r\n"
				+ "from WMLOG.WM_LOG_RUN \r\n"
				+ "WHERE INTERFACE = 'PO8'\r\n"
				+ "AND  start_dt >= TRUNC(SYSDATE) \r\n"
				+ "AND rownum =1 \r\n"
				+ "ORDER BY START_DT DESC";// Consulta para estatus de la ejecucion	
		
		String consultaERROR = " select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE \r\n"
				+ "from  wmlog.WM_LOG_ERROR \r\n" 
				+ "where RUN_ID='%s' ";// Consulta para los errores
		String consultaERROR2 = "select description,MESSAGE \r\n" 
				+ "from wmlog.WM_LOG_ERROR \r\n"
				+ "where RUN_ID='%s'";// Consulta para los errores
		String consultaERROR3 = "select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 \r\n"
				+ "from wmlog.WM_LOG_ERROR \r\n" 
				+ "where RUN_ID='%s'";// Consulta para los errores

		String fin = "SELECT  CR_PLAZA, TRAN_CODE_RETEK, TRAN_CODE_POS, FOLIO, TOTAL_PRECIO_COSTO, TOTAL_PRECIO_VENTA, INTERFACE_DATE, VAT_CODE\r\n"
				+ "FROM XXFC_SALDO_DIARIO_POS WHERE CR_PLAZA='" + data.get("plaza")
				+ "' AND INTERFACE_DATE>=TRUNC(SYSDATE) ";
		

		String DSIE = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, doc_type,PV_DOC_NAME, RECEIVED_DATE \r\n" 
		       + "FROM POSUSER.POS_INBOUND_DOCS  \r\n"
			   + "WHERE STATUS='E' \r\n"
			   + "AND DOC_TYPE='SDI' \r\n"
			   + "AND SUBSTR(PV_DOC_NAME,4,5)= '" + data.get("plaza") + "' \r\n"
			   + "AND RECEIVED_DATE > SYSDATE-2 \r\n" 
			   + "order by  received_date desc ";
		
//*********************************************** Paso 1 ***************************************************************
		
		addStep("Validar documentos SDI disponibles para procesar en POSUSER.");
		System.out.println("Paso 1 ");
		System.out.println("Respuesta " + DSI);
		SQLResult DSI1 = dbPos.executeQuery(DSI);		
		boolean paso1 = DSI1.isEmpty();
		
		System.out.println(paso1);
		
		if (!paso1) {
			testCase.addQueryEvidenceCurrentStep(DSI1);
		}
		assertFalse("No hay insumos a procesar", paso1);

//************************************************* Paso 2********************************************************************
		addStep("Ejecutar la interfaz PO8.");
		System.out.println("Paso 2 ");
		
		String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		u.hardWait(4);
	
		pok.runIntefaceWM2(data.get("interface"), data.get("servicio"), null);
		
	//	testCase.addScreenShotCurrentStep(u, "IS");
		
	
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

			String error = String.format(consultaERROR, run_id);
			String error1 = String.format(consultaERROR2, run_id);
			String error2 = String.format(consultaERROR3, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontro un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");

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

//************************************************ Paso 3 ************************************************
		
		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run ");
		System.out.println("Paso 3 ");
		System.out.println(tdcIntegrationServerFormat);
		
		SQLResult is1 = executeQuery(dbLog, tdcIntegrationServerFormat);

		String fcwS = is1.getData(0, "STATUS");

		boolean validateStatus = fcwS.equals(status);
		
		System.out.println(validateStatus);
		
		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");
		
		SQLResult log = dbLog.executeQuery(tdcIntegrationServerFormat);
	
		boolean log1 = log.isEmpty();
		
		System.out.println(log1);
		
		if (!log1) {

			testCase.addQueryEvidenceCurrentStep(log);
		}

		
		assertFalse("r", log1);

//*************************************************** Paso 4 ***********************************************************
		
		addStep("Verificar la informacion enviada a FINANZAS. ");
		System.out.println("Paso 4 ");
		
		System.out.println("Respuesta " + fin);
		
		SQLResultExcel finn = executeQueryExcel(dbEbs, fin);
				
		boolean paso4 = finn.isEmpty();
		
		System.out.println(paso4);
		
		if (!paso4) {
			testCase.addDocumentEvidence(finn.getRelativePath(), " 4. Verificar la información enviada a FINANZAS.");
		}

		assertFalse("No se envio la informacion correctamente",  paso4);
//************************************************* Paso 5 ************************************************************
		
		addStep("Verificar el estatus de los documentos enviados.");
		System.out.println("Paso 5 ");
		
		System.out.println("Respuesta " + DSIE);
		SQLResult DSI1E = dbPos.executeQuery(DSIE);
		
		
		boolean paso1E = DSI1E.isEmpty();
		
		System.out.println(paso1E);
		
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
		return "Validar la operación para el envío de documentos SDI (Saldo Diario) a FINANZAS.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_P08_004_Ejecucion_Interfaz_Plaza_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
}
