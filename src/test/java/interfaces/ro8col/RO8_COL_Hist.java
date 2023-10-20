package interfaces.ro8col;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import modelo.TestCase;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class RO8_COL_Hist extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_RO8_COL_002_Hist(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_COL,
				GlobalVariables.DB_USER_RMS_COL, GlobalVariables.DB_PASSWORD_RMS_COL);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,
				GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS_COL,
				GlobalVariables.DB_USER_EBS_COL, GlobalVariables.DB_PASSWORD_EBS_COL);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		String VerificacionHC1 = "SELECT HEADER_ID,TRAN_CODE,CR_PLAZA,GL_JOURNAL_ID,RUN_ID"
				+ " FROM WMUSER.WM_GL_HEADERS_COL " 
				+ " WHERE GL_JOURNAL_ID IS NOT NULL";
		
		String VerificacionHC2 = "SELECT HEADER_ID,TRAN_CODE,CR_PLAZA,GL_JOURNAL_ID,RUN_ID"
				+ " FROM WMUSER.WM_GL_HEADERS_COL " 
				+ " WHERE HEADER_ID = %s";

		String VerificacionHist = "SELECT HEADER_ID,TRAN_CODE,CR_PLAZA,GL_JOURNAL_ID,RUN_ID"
				+ " FROM WMUSER.WM_GL_HEADERS_HIST_COL " 
				+ " WHERE HEADER_ID = %s";

		String VerificacionLinesGasH = "SELECT LINE_ID,HEADER_ID,NET_RETAIL,NET_COST,VAT_RATE_COST,VAT_RATE_RETAIL"
				+ " FROM WMUSER.WM_GL_LINES_HIST_COL" 
				+ " WHERE HEADER_ID = %s";

		String VerificacionLinesCol = "SELECT LINE_ID,HEADER_ID,TYPE,NET_RETAIL,VAT_RETAIL"
				+ " FROM WMUSER.WM_GL_LINES_COL " 
				+ " Where HEADER_ID = %s";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" 
		        + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'RO08_COL_HIST'" 
				+ " AND START_DT >= TRUNC (SYSDATE)"
				+ " order by start_dt desc)" + " where rownum = 1";

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " 
		        + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA

		String tdcQueryStatusLog = "SELECT run_id,interface,start_dt,status,server " 
		        + " FROM wmlog.wm_log_run"
				+ " WHERE interface = 'RO08_COL_HIST'" 
				+ " AND run_id=%s";

		
		String status = "S";

		// utileria
		SeleniumUtil u ;
		PakageManagment pok;

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server;
		String searchedStatus = "R";
		String run_id;
		
		testCase.setProject_Name("Automatización Interfaces Web Methods");
		testCase.setTest_Description("Verificar proceso de "+data.get(""));

//							Paso 1	**********************************************	
		addStep("Tener datos para procesar con GL_JOURNAL_ID <>  NULL en la tabla WM_GL_HEADERS_COL");

		System.out.println(GlobalVariables.DB_HOST_RMS_COL);
		SQLResult result1 = executeQuery(dbRms, VerificacionHC1);

		String header_id = result1.getData(0, "header_id");
		System.out.println(VerificacionHC1);

		boolean av = result1.isEmpty();

		if (!av) {

			testCase.addQueryEvidenceCurrentStep(result1);

		}

		System.out.println(av);

		assertFalse(av, "No se obtiene informacion de la consulta");

//			Paso 2	**********************************************	
		addStep("Ejecutar la interface con el servicio RO8_COL.Pub:runRO8_COLHist. Solicitar la ejecución del job: runRO8_COLHist");

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
        u.hardWait(4);
		pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);

		SQLResult result5 = executeQuery(dbLog, tdcQueryIntegrationServer);

		String status1 = result5.getData(0, "STATUS");
		run_id = result5.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			result5 = executeQuery(dbLog, tdcQueryIntegrationServer);
			status1 = result5.getData(0, "STATUS");
			run_id = result5.getData(0, "RUN_ID");

			u.hardWait(2);

		}
		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult result3 = executeQuery(dbLog, error);

			boolean emptyError = result3.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(result3);

			}
		}

//			Paso 3  *************************************************

		addStep("Verificar que la ejecución termina con éxito.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
		System.out.println(queryStatusLog);
		
		SQLResult Result4 = executeQuery(dbLog, queryStatusLog);
		String fcwS = Result4.getData(0,  "STATUS");
		
		testCase.addQueryEvidenceCurrentStep(Result4);
		
		boolean validateStatus = status.equals(fcwS);
		System.out.println(validateStatus);

		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");


//				Paso 4 *********************************
		addStep("Verificar la inserción de los headers en WM_GL_HEADERS_HIST_COL.");

		System.out.println(GlobalVariables.DB_HOST_RMS_COL);
		
		String queryHist = String.format(VerificacionHist, header_id);
		
		SQLResult result4 = executeQuery(dbRms, queryHist);
		System.out.println(queryHist);

		boolean av2 = result4.isEmpty();

		if (!av2) {

			testCase.addQueryEvidenceCurrentStep(result4);
		}

		System.out.println(av2);

		assertFalse(av2, "No se obtiene informacion de la consulta");

//				Paso 5 *********************************

		addStep("Verificar la insercion de lineas en la tabla WM_GL_LINES_HIST_COL.");

		System.out.println(GlobalVariables.DB_HOST_RMS_COL);
		String consulta1 = String.format(VerificacionLinesGasH, header_id);
		System.out.println(consulta1);
		
		SQLResult resultHea = executeQuery(dbRms, consulta1);
		boolean av3 = resultHea.isEmpty();

		if (!av3) {

			testCase.addQueryEvidenceCurrentStep(resultHea);
		}

		System.out.println(av3);
		assertFalse(av3, "No se obtiene informacion de la consulta");

//				Paso 6 *********************************
		addStep("Verificar la eliminacion de las lineas en la tabla WM_GL_LINES_COL");

		System.out.println(GlobalVariables.DB_HOST_RMS_COL);
		String consulta2 = String.format(VerificacionLinesCol, header_id);
		System.out.println(consulta2);

		SQLResult result6 = executeQuery(dbRms, consulta2);
		boolean av4 = result6.isEmpty();

		testCase.addQueryEvidenceCurrentStep(result6);
		

		System.out.println(av4);
		assertTrue(av4, "Se obtiene información de la consulta");

		//Paso 7  *********************************
		addStep("Verificar la eliminacion de los headers en la tabla WM_GL_HEADERS_COL");
				
	    System.out.println(GlobalVariables.DB_HOST_RMS_COL);
		String consulta3 = String.format(VerificacionHC2, header_id);
		System.out.println(consulta3);
				
		SQLResult result7 = executeQuery(dbRms, consulta3);
		boolean av5 = result7.isEmpty();
		
		testCase.addQueryEvidenceCurrentStep(result7);
		
				
		System.out.println(av5);
		assertTrue(av5, "Se obtiene información de la consulta");
				
				

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Verificar proceso de interface HIST ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_RO8_COL_002_Hist";
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
