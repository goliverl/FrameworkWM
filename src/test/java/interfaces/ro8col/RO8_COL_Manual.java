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

public class RO8_COL_Manual extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_RO8_COL_003_Manual(HashMap<String, String> data) throws Exception {

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

		String SelectCFG = "SELECT INTERFACE_NAME, MANUAL_STATUS, ATTRIBUTE1, ATTRIBUTE2 "
				+ " FROM WMUSER.wm_cfg_launcher " 
				+ " WHERE interface_name = 'RO8_COL'" 
				+ " AND attribute1=" + "'"+ data.get("plaza") + "'" 
				+ " AND attribute2 =" + "'" + data.get("tranCode") + "'"
				+ " AND manual_status = 'A'";// posuser

		String SelectInsumos1 = " SELECT ITEM, ITEM_DESC,STORE,TRAN_DATE,TRAN_CODE "
				+ " FROM fem_fif_stg " 
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60" 
				+ " AND cr_plaza =" + "'"+ data.get("plaza") + "'" 
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'"
				+ " AND reference_3 IS NULL" 
				+ " AND reference_9 IS NULL";// RMSCOL
		
		String SelectInsumos2 = " SELECT REFERENCE_3,REFERENCE_9,CR_PLAZA,ID "
				+ " FROM fem_fif_stg " 
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60" 
				+ " AND cr_plaza =" + "'"+ data.get("plaza") + "'" 
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'"
				+ " AND reference_3 IS NULL" 
				+ " AND reference_9 IS NULL";// RMSCOL

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" 
		        + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'RO08_COL'" 
		        + " AND START_DT >= TRUNC (SYSDATE)"
				+ " order by start_dt desc)" 
		        + " where rownum = 1";

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " 
		        + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; 

		String tdcQueryStatusLog = "SELECT run_id,interface,start_dt,status,server " 
		        + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'RO08_COL' " 
				+ " and start_dt >= trunc(sysdate) " 
				+ " and run_id = %s " 
				+ " ORDER BY start_dt DESC";

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " 
				+ " WHERE parent_id = %s";
				//+ " AND att1 = '"+ data.get("plaza") + "'"
				//+ " AND att2 = '"+ data.get("tranCode") + "'"; ; 

		String VerificacionHeader = "SELECT HEADER_ID,TRAN_CODE,TRAN_DATE,CR_PLAZA,RUN_ID,JOURNAL_TYPE_ID,GL_JOURNAL_ID"
				+ " FROM WMUSER.WM_GL_HEADERS_COL" 
				+ " WHERE cr_plaza =" + "'" + data.get("plaza") + "'"
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'" 
				+ " AND run_id =%s";// RMSCOL

		String ConsultaGl1 = "SELECT reference6,reference4,reference10,reference22,reference25"
				+ " FROM GL_INTERFACE" 
				+ " WHERE reference6 =%s";
		
		String ConsultaGl2 = "SELECT user_je_category_name,user_je_source_name,segment4"
				+ " FROM GL_INTERFACE" 
				+ " WHERE reference6 =%s";

		String ValidacionR3R9p1 = "SELECT ITEM,ITEM_DESC,STORE,TRAN_DATE,TRAN_CODE"
				+ " FROM fem_fif_stg" 
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60" 
				+ " AND cr_plaza =" + "'"+ data.get("plaza") + "'" 
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'"
				+ " AND reference_3 = %s"
				+ " AND reference_9 = %s";
		
		String ValidacionR3R9p2 = "SELECT REFERENCE_3,REFERENCE_9,CR_PLAZA,ID"
				+ " FROM fem_fif_stg" 
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60" 
				+ " AND cr_plaza =" + "'"+ data.get("plaza") + "'" 
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'"
				+ " AND reference_3 = %s"
				+ " AND reference_9 = %s";
		
		String consultaReference3 = "SELECT reference_3" 
		        + " FROM fem_fif_stg"
		        + " WHERE tran_date >= TRUNC (SYSDATE) - 60" 
				+ " AND cr_plaza =" + "'"+ data.get("plaza") + "'" 
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'"
				+ " AND reference_9 IS NOT NULL" 
				+ " AND reference_3 IS NOT NULL";

		String tdcCfgLauncher = "SELECT ATTRIBUTE1 AS PLAZA,ATTRIBUTE2 AS TRANCODE, 'MANUAL' AS TYPE, MANUAL_STATUS"
				+ " FROM WMUSER.wm_cfg_launcher " 
				+ " WHERE interface_name = 'RO8_COL'" 
				+ " AND attribute1=" + "'"+ data.get("plaza") + "'" 
				+ " AND attribute2 =" + "'" + data.get("tranCode") + "'";
		
		

		String status = "S";

		// utileria
		SeleniumUtil u ;
		PakageManagment pok ;

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id;
		
		testCase.setProject_Name("Automatización Interfaces Web Methods");
		testCase.setTest_Description("Verificar proceso de interface VENTAS proceso manual");

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 */
		/*
		 * Paso 1
		 *****************************************************************************************/
		addStep("Verificar que la plaza se encuentre configurada en la tabla WM_CFG_LAUNCHER de WMINT.");

		System.out.println(GlobalVariables.DB_HOST_Puser);

		System.out.println(SelectCFG);

		SQLResult result1 = executeQuery(dbPos, SelectCFG);

		boolean av = result1.isEmpty();

		if (!av) {

			testCase.addQueryEvidenceCurrentStep(result1);

		}

		System.out.println(av);

		assertFalse(av, "No se obtiene información de la consulta");

		/*
		 * Paso 2
		 *****************************************************************************************/	
		addStep("Validar información pendiente de procesar en la tabla FEM_FIF_STG.");

		System.out.println(GlobalVariables.DB_HOST_RMS_COL);

		System.out.println(SelectInsumos1);

		SQLResult resultInsumos1 = executeQuery(dbRms, SelectInsumos1);
		SQLResult resultInsumos2 = executeQuery(dbRms, SelectInsumos2);

		boolean SC = resultInsumos1.isEmpty();

		if (!SC) {

			testCase.addQueryEvidenceCurrentStep(resultInsumos1);
			testCase.addQueryEvidenceCurrentStep(resultInsumos2,false);

		}

		System.out.println(SC);

		assertFalse(SC, "No se obtiene información de la consulta");

		/*
		 * Paso 3
		 *****************************************************************************************/
		addStep("Ejecutar el servicio de la interface: RO8_COL.Pub:run. Solicitando la ejecución del job: runRO8_COL.");

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		u.hardWait(4);

		pok.runIntefaceWmWithInput(data.get("interfase"), data.get("servicio"),"MANUAL","executionType");

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

		/*
		 * Paso 4
		 *****************************************************************************************/

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
		
		/*
		 * Paso 5
		 *****************************************************************************************/
		
		addStep("Verificar los threads de la ejecución.");
		
		String consultafor="", thread_id, thread_status;	  
		SQLResult resultfor;
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);
		
		/*SQLResult Result5 = executeQuery(dbLog, queryStatusThread);
		String regPlazaTienda = Result5.getData(0,  "STATUS");*/
		
		SQLResult resultThreads = executeQuery(dbLog, queryStatusThread);
		  
		thread_id = resultThreads.getData(0, "THREAD_ID");
		thread_status = resultThreads.getData(0, "STATUS");
		
		  for(int i = 0; i < resultThreads.getRowCount(); i++) {
			  
			  
			  String id = resultThreads.getData(i, "THREAD_ID");
			  System.out.println(id);
			  consultafor = String.format(VerificacionHeader, id);
			  resultfor = executeQuery(dbRms, consultafor);
			  boolean foav = resultfor.isEmpty();
			  if (!foav) {

			   thread_id = resultThreads.getData(i, "THREAD_ID");
			   thread_status = resultThreads.getData(i, "STATUS");
			   }
			  }
		  
		testCase.addQueryEvidenceCurrentStep(resultThreads);
		  
		boolean statusThread = status.equals(thread_status);
		
		System.out.println(statusThread);
		
		if (!statusThread) {

			String error = String.format(tdcQueryErrorId, run_id);
			
			SQLResult resultError = executeQuery(dbLog, error);
			boolean emptyError = resultError.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(resultError);

			}
		}

		//assertTrue(statusThread, "El registro de ejecución de la plaza y tienda no fue exitoso");

		/*
		 * Paso 6
		 *****************************************************************************************/

		addStep("Verificar la actualización de JOURNAL_ID, JOURNAL_TYPE_ID y RUN_ID en la tabla WM_GL_HEADERS_COL.");		
		
		System.out.println(GlobalVariables.DB_HOST_RMS_COL);
		String consulta = String.format(VerificacionHeader, thread_id);
		System.out.println(consulta);
		
		SQLResult resultConsulta = executeQuery(dbRms, consulta);
		
		String header_id = resultConsulta.getData(0,"HEADER_ID");
		boolean av7 = resultConsulta.isEmpty();
		
		if (av7 == false) {
			testCase.addQueryEvidenceCurrentStep(resultConsulta);
		} else {
			testCase.addQueryEvidenceCurrentStep(resultConsulta);
		}
		System.out.println(av7);

		assertFalse(av7, "No se obtiene informacion de la consulta");

		/*
		 * Paso 7
		 *****************************************************************************************/

		addStep("Verificar la inserción de líneas en la tabla GL_INTERFACE.");
		System.out.println(GlobalVariables.DB_HOST_RMS_COL);
		SQLResult resultR3 = executeQuery(dbRms, consultaReference3);
		
		String reference3 = resultR3.getData(0, "reference_3");
		System.out.println(reference3);

		System.out.println(GlobalVariables.DB_HOST_EBS_COL);
		String consulta1 = String.format(ConsultaGl1, reference3);
		String consulta2 = String.format(ConsultaGl2, reference3);
		System.out.println(consulta1);
		
		SQLResult resultGl1 = executeQuery(dbEbs, consulta1);
		SQLResult resultGl2 = executeQuery(dbEbs, consulta2);

		boolean va = resultGl1.isEmpty();

		if (!va) {

			testCase.addQueryEvidenceCurrentStep(resultGl1);
			testCase.addQueryEvidenceCurrentStep(resultGl2,false);

		}

		System.out.println(va);

		assertFalse(va, "No se obtiene informacion de la consulta");

		/*
		 * Paso 8
		 *****************************************************************************************/

		addStep("Verificar la actualización de REFERNCE_3 con el JOURNAL_ID y el REFERENCE_9 con el HEADER_ID en la tabla FEM_FIF_STG.");

		System.out.println(GlobalVariables.DB_HOST_RMS_COL);
		String vact1 = String.format(ValidacionR3R9p1, reference3, header_id);
		String vact2 = String.format(ValidacionR3R9p2, reference3, header_id);
		System.out.println(vact1);

		SQLResult resultP1 = executeQuery(dbRms, vact1);
		SQLResult resultP2 = executeQuery(dbRms, vact2);
		boolean va1 = resultP1.isEmpty();

		if (!va1) {

			testCase.addQueryEvidenceCurrentStep(resultP1);
			testCase.addQueryEvidenceCurrentStep(resultP2,false);

		}

		System.out.println(va1);

		assertFalse(va1, "No se obtiene informacion de la consulta");

		/*
		 * Paso 9
		 *****************************************************************************************/
		addStep("Actualización del campo MANUAL_STATUS en la tabla WM_CFG_LAUNCHER a I.");

		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcCfgLauncher);
		
		SQLResult result8 = executeQuery(dbPos, tdcCfgLauncher);
		
		String cfgStatus = result8.getData(0, "MANUAL_STATUS");

		boolean va2 = cfgStatus.equals("I");

		if (va2) {
		
		   testCase.addQueryEvidenceCurrentStep(result8);
		
	    }

		System.out.println(va2);

		assertTrue(va2, "No se actualizo el campo MANUAL_STATUS");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Verificar proceso de interface VENTAS proceso manual ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_RO8_COL_003_Manual";
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
