package interfaces.ro8col;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;


public class ATC_FT_013_RO8_COL_QuitarLineasCostoPromedioPolizaBucaramanga_Bogota extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_013_RO8_COL_QuitarLineasCostoPromedioPolizaBucaramanga_Bogota_Test (HashMap<String, String> data) throws Exception{
		/*
		 * Utileria****************************************************************/
		utils.sql.SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_COL_QAVIEW,GlobalVariables.DB_USER_RMS_COL_QAVIEW, GlobalVariables.DB_PASSWORD_RMS_COL_QAVIEW);
		utils.sql.SQLUtil FCWMLQA_WMLOG_COL = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG_COL,GlobalVariables.DB_USER_FCWMLQA_WMLOG_COL, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG_COL);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS_COL,GlobalVariables.DB_USER_EBS_COL, GlobalVariables.DB_PASSWORD_EBS_COL);
		
		SeleniumUtil u = null;
		/*
		 * Variables****************************************************************/
		String queryPendingProc = "SELECT TRAN_CODE ,TRAN_DATE, REFERENCE_3, REFERENCE_9\r\n"
				+ " FROM fem_fif_stg\r\n"
				+ " WHERE     CR_PLAZA IN ( '%s')\r\n"
				+ "       AND tran_code in (1,4,22,30,32)\r\n"
				+ "       AND tran_date >= TRUNC(SYSDATE-60)\r\n"
				+ "       AND reference_3 IS NULL\r\n"
				+ "       AND reference_9 IS NULL \r\n"
				+ "and vat_code in ('IVA5','IVA19','EXCLUD','NIVA19','NIVA5')";
		
		String queryExecution = "SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER\r\n"
				+ "FROM WMLOG.wm_log_run\r\n"
				+ "WHERE interface = 'RO08_COL' \r\n"
				+ "AND status = 'S' \r\n"
				+ "AND start_dt >= TRUNC (SYSDATE)\r\n"
				+ "ORDER BY start_dt DESC";
		
		String queryVerifyUpd = "SELECT HEADER_ID, TRAN_CODE, RUN_ID\r\n"
				+ "FROM  WMUSER.WM_GL_HEADERS_COL\r\n"
				+ "WHERE TRAN_CODE = 1 \r\n"
				+ "AND  RUN_ID= '%s' AND CR_PLAZA = '10BCA'";
		
		String queryInsertLines = "SELECT HEADER_ID \r\n"
				+ "FROM WMUSER.wm_gl_lines_col WHERE header_id = '%s'";
		
		String queryInsertLinesGL = "SELECT reference6,reference4,reference10,reference22,reference25,user_je_category_name,user_je_source_name,segment4 \r\n"
				+ "	FROM GL.GL_INTERFACE \r\n"
				+ "WHERE reference6 = '%s'";
		
		String queryVerifyUpdRef = "SELECT * FROM fem_fif_stg WHERE reference_9 = '%s' \r\n"
				+ "AND reference_3 = '%s' \r\n"
				+ "AND tran_date = to_date(transactionDate,'dd/mm/yyyy') \r\n"
				+ "AND CR_PLAZA = '%s'  \r\n"
				+ "AND tran_code in (1,4,22,30,32)";
		
		String queryNoUpdate = "SELECT STATUS, DATE_CREATED, STATUS_DESCRIPTION\r\n"
				+ "FROM GL.GL_INTERFACE\r\n"
				+ "WHERE DATE_CREATED >= TRUNC(SYSDATE) and REFERENCE6 in ('%s')\r\n"
				+ "order by DATE_CREATED DESC";
		
		testCase.setTest_Description(data.get("name"));
		
		/********************************Paso 1*************************************/
		addStep("Validar información pendiente de procesar en la tabla FEM_FIF_STG");
		
		String pendingProcFormat = String.format(queryPendingProc, data.get("plaza"));
		System.out.println(pendingProcFormat);
		SQLResult pendingProc = dbRms.executeQuery(pendingProcFormat);
		boolean validatePendingProc = pendingProc.isEmpty();
		String reference3 = "";
		if(!validatePendingProc) {
			reference3 = pendingProc.getData(0, "REFERENCE_3");
			testCase.addQueryEvidenceCurrentStep(pendingProc);
		}else {
			testCase.addQueryEvidenceCurrentStep(pendingProc);
		}
		assertFalse(validatePendingProc, "No se obtuvo informacion");
		/********************************Paso 2*************************************/
		addStep("Ejecutar el servicio RO8_COL La interfaz será ejecutada por el job runRO8_COL de Ctrl-M ");

		u  = new SeleniumUtil(new ChromeTest(), true);
		
		JSONObject obj = new JSONObject(data.get("job"));
		//Paso 3
		addStep("Jobs en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
		//paso 4
		addStep("Login");
		u.get(data.get("server"));
		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn(); 

		//Buscar del job
		//Paso 5
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
		
		/********************************Paso 6*************************************/
		addStep("Verificar que la ejecución termino con éxito.");
		
		SQLResult execution = FCWMLQA_WMLOG_COL.executeQuery(queryExecution);
		System.out.println(queryExecution);
		boolean validateExecution = execution.isEmpty();
		String runId = "";
		if(!validateExecution) {
			runId = execution.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(execution);
		}else {
			testCase.addQueryEvidenceCurrentStep(execution);
		}
		assertFalse(validateExecution, "No se obtuvo informacion");
		/********************************Paso 7*************************************/
		addStep("Verificar la actualización de JOURNAL_ID, JOURNAL_TYPE_ID y RUN_ID   en la tabla WM_GL_HEADERS_COL.");
		
		String verifyUpd = String.format(queryVerifyUpd, runId ,data.get("plaza"));
		System.out.println(verifyUpd);
		SQLResult verifyUpdate = dbRms.executeQuery(verifyUpd);
		boolean validateVerifyUpd = verifyUpdate.isEmpty();
		String headerId = "";
		if(!validateVerifyUpd) {
			headerId = verifyUpdate.getData(0, "HEADER_ID");
			testCase.addQueryEvidenceCurrentStep(verifyUpdate);
		}else {
			testCase.addQueryEvidenceCurrentStep(verifyUpdate);
		}
		assertFalse(validateVerifyUpd, "No se obtuvo informacion");
		/********************************Paso 8*************************************/
		addStep("Inserción de líneas en la tabla WM_GL_LINES_COL");
		
		String insertLinesFormat = String.format(queryInsertLines, headerId);
		System.out.println(insertLinesFormat);
		SQLResult insertLines = dbRms.executeQuery(insertLinesFormat);
		boolean validateInsertLines = insertLines.isEmpty();
		if(!validateInsertLines) {
			testCase.addQueryEvidenceCurrentStep(insertLines);
		}else {
			testCase.addQueryEvidenceCurrentStep(insertLines);
		}
		assertFalse(validateInsertLines, "No se obtuvo informacion");
		/********************************Paso 9*************************************/
		addStep("Verificar la inserción de líneas en la tabla GL_INTERFACE");
		
		String insertLinesGLFormat = String.format(queryInsertLinesGL, reference3);
		System.out.println(insertLinesGLFormat);
		SQLResult insertLinesGL = dbEbs.executeQuery(insertLinesGLFormat);
		boolean validateInsertLinesGL = insertLinesGL.isEmpty();
		String reference6 = "";
		if(!validateInsertLinesGL) {
			reference6 = insertLinesGL.getData(0, "REFERENCE6");
			testCase.addQueryEvidenceCurrentStep(insertLinesGL);
		}else {
			testCase.addQueryEvidenceCurrentStep(insertLinesGL);
		}
		assertFalse(validateInsertLinesGL, "No se obtuvo informacion");
		/********************************Paso 10*************************************/
		addStep("Verificar la actualización de REFERNCE_3 con el JOURNAL_ID y el REFERENCE_9 con el HEADER_ID en la tabla FEM_FIF_STG.");
		
		String queryVerifyUpdRef1 = String.format(queryVerifyUpdRef, headerId, reference6 ,data.get("plaza"));
		System.out.println(queryVerifyUpdRef1);
		SQLResult verifyUpdRef = dbRms.executeQuery(queryVerifyUpdRef1);
		boolean validateVerifyUpdRef = verifyUpdRef.isEmpty();
		
		if(!validateVerifyUpdRef) {
			testCase.addQueryEvidenceCurrentStep(verifyUpdRef);
		}else {
			testCase.addQueryEvidenceCurrentStep(verifyUpdRef);
		}
		assertFalse(validateVerifyUpdRef, "No se obtuvo informacion");
		/********************************Paso 11*************************************/
		addStep("Validar que no se actualizan las lineas de costo promedio en la tabla gl_interface  para ningun movimiento");
		
		String NoUpdateFormat = String.format(queryNoUpdate, reference3);
		System.out.println(NoUpdateFormat);
		SQLResult NoUpdate = dbEbs.executeQuery(NoUpdateFormat);
		boolean validateNoUpdate = NoUpdate.isEmpty();
		
		if(!validateNoUpdate) {
			testCase.addQueryEvidenceCurrentStep(NoUpdate);
		}else {
			testCase.addQueryEvidenceCurrentStep(NoUpdate);
		}
		assertTrue(validateNoUpdate, "Se realizo actualizacion en las lineas Costo promedio");
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
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Gabriel Ivan Cardenas Duarte";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_013_RO8_COL_QuitarLineasCostoPromedioPolizaBucaramanga_Bogota_Test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
