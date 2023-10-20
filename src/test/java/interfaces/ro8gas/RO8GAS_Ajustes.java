package interfaces.ro8gas;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.TPE_TSF;
import util.GlobalVariables;
import util.RequestUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class RO8GAS_Ajustes extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_RO8_GAS_Ajustes(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Ebs, GlobalVariables.DB_USER_Ebs,
				GlobalVariables.DB_PASSWORD_Ebs);
		utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,
				GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
				GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		/*
		 * Variables
		 *********************************************************************/
		String tdcQueryWmCfgLauncher = "SELECT ATTRIBUTE1 AS PLAZA, ATTRIBUTE2 AS TRANCODE FROM WMUSER.WM_CFG_LAUNCHER "
				+ "WHERE INTERFACE_NAME = 'RO08_GAS' " + "AND AUTO_STATUS= 'A' " + "AND ATTRIBUTE3 = 'AJUST' "
				+ "AND ATTRIBUTE1= '" + data.get("plaza") + "'";

		String tdcQueryFemFifStg1 = "SELECT ITEM, ITEM_DESC, TRAN_DATE, TRAN_CODE, REFERENCE_3, REFERENCE_9, ID"
				+ " FROM FEM_FIF_STG" + " WHERE tran_date >= TRUNC (SYSDATE) - 60" + " AND cr_plaza = '"
				+ data.get("plaza") + "'" + " AND tran_code = '22'" + " AND reference_3 IS NULL"
				+ " AND reference_9 IS NULL"; // posuser

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'RO08_GAS_AJUST'" + " AND START_DT >= TRUNC (SYSDATE)"
				+ " order by start_dt desc)" + " where rownum = 1";

		String tdcQueryStatusLog = "SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER"
				+ " FROM WMLOG.WM_LOG_RUN" + " WHERE INTERFACE = 'RO08_GAS_AJUST'" + " AND RUN_ID = %s";

		String tdcQueryStatusThread = "SELECT PARENT_ID,THREAD_ID,NAME,STATUS,ATT1,ATT2 " + " FROM WMLOG.WM_LOG_THREAD "
				+ " WHERE PARENT_ID = %s" + " ORDER BY THREAD_ID ASC";

		String tdcQueryError = "SELECT error_id,run_id,error_date,description,message FROM" + " WMLOG.wm_log_error"
				+ " WHERE run_id =  %s";

		String VerificacionHeader = "SELECT HEADER_ID,TRAN_CODE,TRAN_DATE,CR_PLAZA,wm_run_id"
				+ " FROM wmuser.WM_GL_HEADERS_GAS" + " WHERE cr_plaza =" + "'" + data.get("plaza") + "'"
				+ " AND tran_code =" + "'22'"// RMS_MEX
				+ " AND wm_run_id =%s";// thread_id

		String VerificacionR3R9 = "SELECT STORE,TRAN_DATE,TRAN_CODE,REFERENCE_3,REFERENCE_9,CR_PLAZA,ID"
				+ " FROM fem_fif_stg" + " WHERE tran_date >= TRUNC (SYSDATE) - 60" + " AND cr_plaza = " + "'"
				+ data.get("plaza") + "'" + " AND tran_code = '22'"
				//+ " AND reference_9 = %s" + // header_id
			+	" AND reference_3 ='%s'";// RMS_MEX

		String ConsultaPRE = "select reference_3, reference_9 from fem_fif_stg where   cr_plaza = '" + data.get("plaza") + "'"
				+ "and reference_3 is not NULL and reference_9 is not null AND  tran_code = 22   "
				+ "and tran_date >= SYSDATE -60  order by tran_date desc";

		String ConsultaGl = "SELECT reference6,reference4,reference10,reference22,reference25,user_je_category_name,user_je_source_name,segment4 "
				+ "FROM GL_INTERFACE WHERE reference6 = '%s' " + " ";// reference6 =[fem_fif_stg.reference_3]
		String Verificar = "SELECT reference_3, reference_9, item_desc FROM fem_fif_stg"
				+ " WHERE reference_9 ='%s' \r\n" + // reference_9 = [wm_gl_header_col.header_id]
				"AND reference_3='%s' \r\n" + // reference_3=[gl_interface.reference6]
				//"AND tran_date = to_date(transactionDate,'dd/mm/yyyy') \r\n" +
				"AND CR_PLAZA = '" + data.get("plaza") + "' " + "AND tran_code = '22' "; // rms

		String lines = "SELECT LINE_ID, HEADER_ID, NET_RETAIL, NET_COST,TOTAL_RETAIL FROM WMUSER.wm_gl_lines_gas WHERE header_id =' %s'"; // [wm_gl_header_col.header_id]

		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String status = "S";
		SQLResult resultfor;
		
		
		
		String run_id,  thread_status, thread_id;

		testCase.setProject_Name("Interfaces Web Methods");
		testCase.setFullTestName("RO8_MEX_" + data.get("tran"));

		/*
		 * *****************************************************************************
		 * Pasos
		 ***********************************************************************************************/
		/*
		 * Paso 1
		 *****************************************************************************************/
		addStep("Verificar parametros configurados en la WMUSER.WM_CFG_LAUNCHER para ejecución AUTOMATICA");

		System.out.println(GlobalVariables.DB_HOST_Puser);

		System.out.println(tdcQueryWmCfgLauncher);

		SQLResult resultQueryWmCfgLauncher = executeQuery(dbPuser, tdcQueryWmCfgLauncher);

		testCase.addQueryEvidenceCurrentStep(resultQueryWmCfgLauncher);

		boolean launcherEmpty = resultQueryWmCfgLauncher.isEmpty();

//		assertFalse(launcherEmpty, "La plaza se encuentra configurada");

		/*
		 * Paso 2
		 *****************************************************************************************/

		addStep("Validar información pendiente de procesar en la tabla FEM_FIF_STG");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);

		System.out.println(tdcQueryFemFifStg1);

		SQLResult resultQueryFemFifStg1 = executeQuery(dbRms, tdcQueryFemFifStg1);

		testCase.addQueryEvidenceCurrentStep(resultQueryFemFifStg1);

		boolean femFifEmpty = resultQueryFemFifStg1.isEmpty();

//		assertFalse(femFifEmpty, "No existe información pendiente de procesar");

		/*
		 * Paso 3
		 *****************************************************************************************/
		

		addStep("Ejecutar la interfaz RO8 con el servicio " + data.get("servicio"));
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";

		u.get(contra);

		u.hardWait(4);

		pok.runIntefaceWmOneButton10(data.get("interface"), data.get("servicio"));

		System.out.print(tdcQueryIntegrationServer);

		SQLResult resultQueryIS = dbLog.executeQuery(tdcQueryIntegrationServer);

		run_id = resultQueryIS.getData(0, "RUN_ID");// guarda el run id de la ejecución

		status = resultQueryIS.getData(0, "STATUS");

		boolean valuesStatus = status.equals(searchedStatus);// Valida si se encuentra en estatus R

		while (valuesStatus) {

			resultQueryIS = executeQuery(dbLog, tdcQueryIntegrationServer);

			run_id = resultQueryIS.getData(0, "RUN_ID");// guarda el run id de la ejecución

			status = resultQueryIS.getData(0, "STATUS");

			valuesStatus = status.equals(searchedStatus);

			u.hardWait(2);

		}

		boolean successRun = status.equals(status);// Valida si se encuentra en estatus S

		if (!successRun) {

			String error = String.format(tdcQueryError, run_id);

			SQLResult resultQueryError = executeQuery(dbLog, error);

			boolean emptyError = resultQueryError.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(resultQueryError);

			}
		}

		/*
		 * Paso 4
		 *****************************************************************************************/

		addStep("Validar que el registro de ejecución de la interfaz termino en estatus 'S' en la tabla WM_LOG_RUN.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		String queryStatusLog = String.format(tdcQueryStatusLog, run_id);

		System.out.println(queryStatusLog);

		SQLResult resultQueryStatusLog = executeQuery(dbLog, queryStatusLog);

		String statusWmLog = resultQueryIS.getData(0, "STATUS");

		boolean validateStatus = statusWmLog.equals(status);

		System.out.println(validateStatus);

		testCase.addQueryEvidenceCurrentStep(resultQueryStatusLog);

		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");

		/*
		 * Paso 5
		 *****************************************************************************************/

		addStep("Validar que los threads de la ejecución terminaron en estatus 'S' en la tabla WM_LOG_THREAD.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		SQLResult paso5 = executeQuery(dbLog,queryStatusThread );

		System.out.println(paso5);
		String estatusThread = paso5.getData(0, "Status");
		System.out.println(queryStatusThread);

		SQLResult resultThreads = executeQuery(dbLog, queryStatusThread);

		thread_status = resultThreads.getData(0, "STATUS");

		thread_id = resultThreads.getData(0, "THREAD_ID");

		boolean statusThread = status.equals(thread_status);

		System.out.println(statusThread);

		testCase.addQueryEvidenceCurrentStep(resultThreads);

		if (!statusThread) {

			String error = String.format(tdcQueryError, run_id);

			SQLResult resultQueryError = executeQuery(dbLog, error);

			boolean emptyError = resultQueryError.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(resultQueryError);

			}
		}

		assertTrue(statusThread, "El registro de ejecución de la threads no fue exitoso.");
		// Paso 6 *********************************

		addStep("Verificar que los datos sean insertados en la tabla WM_GL_HEADERS_GAS de RETEK.");

		String consultafor=""; //thread_id, thread_status;	  
	
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String queryStatusThread1 = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread1);
		
		/*SQLResult Result5 = executeQuery(dbLog, queryStatusThread);
		String regPlazaTienda = Result5.getData(0,  "STATUS");*/
		
		SQLResult resultThreads1 = executeQuery(dbLog, queryStatusThread1);
		  
		thread_id = resultThreads1.getData(0, "THREAD_ID");
		thread_status = resultThreads1.getData(0, "STATUS");
		 String header_id = null;
		  for(int i = 0; i < resultThreads1.getRowCount(); i++) {
			  
			  
			  String id = resultThreads1.getData(i, "THREAD_ID");
			  System.out.println(id);
			  consultafor = String.format(VerificacionHeader, id);
			  resultfor = executeQuery(dbRms, consultafor);
			
			  boolean foav = resultfor.isEmpty();
			  if (!foav) {

			   thread_id = resultThreads1.getData(i, "THREAD_ID");
			   thread_status = resultThreads1.getData(i, "STATUS");
			   }
			  }
		  
		testCase.addQueryEvidenceCurrentStep(resultThreads1);
		  
		boolean statusThread1 = status.equals(thread_status);
		
		System.out.println(statusThread1);
		
		if (!statusThread1) {

			String error = String.format(tdcQueryError, run_id);
			
			SQLResult resultError = executeQuery(dbLog, error);
			boolean emptyError = resultError.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(resultError);

			}
		}

		//assertTrue(statusThread, "El registro de ejecución de la plaza y tienda no fue exitoso");


		// Paso 7 *********************************

	

		// Paso 8 *********************************

		addStep("Verificar la insercion de lineas en la tabla GL_INTERFACE de ORAFIN.");
		System.out.println(GlobalVariables.DB_HOST_EBS);
		System.out.println("CONSULTA PRE"+ConsultaPRE);
		SQLResult resultPRE = executeQuery(dbRms, ConsultaPRE);
		
		String REF3 = resultPRE.getData(0, "reference_3");
		String REF9 = resultPRE.getData(0, "reference_9");
		String consulta3 = String.format(ConsultaGl, REF3);

		System.out.println("CONSUTLTA3"+consulta3);

		SQLResult result10 = executeQuery(dbEbs, consulta3);
      	String REF6 = result10.getData(0, "reference6");

		boolean av8 = result10.isEmpty();

		if (!av8) {

			testCase.addQueryEvidenceCurrentStep(result10);

		}

		System.out.println(av8);

		assertFalse(av8, "No se obtiene informacion de la consulta");
//			
		addStep("Verificar la actualizacion de los campos: REFERENCE_3 y REFERENCE_9 en la tabla FEM_FIF_STG de RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		// header_id = resultfor.getData(0, "HEADER_ID");
		// String header_id = "1228216495";
		
		String consulta2 = String.format(VerificacionR3R9,REF6);
		System.out.println(consulta2);
		SQLResult result9 = executeQuery(dbRms, consulta2);
	//	String R3 = result9.getData(0, "REFERENCE_3");
		System.out.println(consulta2);

		boolean av6 = result9.isEmpty();

		if (!av6) {

			testCase.addQueryEvidenceCurrentStep(result9);

		}

		System.out.println(av6);

		assertFalse(av6, "No se obtiene informacion de la consulta");

////		Paso 9  *************************************************

		addStep("Verificar la actualizacion de REFERENCE_3 con el JOURNAL_ID y el REFERENCE_9 con el HEADER_ID en la tabla FEM_FIF_STG. ");
		String consultaV = String.format(Verificar, REF9, REF6);
		System.out.println("CONSULTA V"+consultaV);
		SQLResult resultVER = executeQuery(dbRms, consultaV);
		boolean av9 = resultVER.isEmpty();

		if (!av9) {

			testCase.addQueryEvidenceCurrentStep(resultVER);

		}

		System.out.println(av9);

		assertFalse(av9, "No se obtiene informacion de la consulta");

		addStep("Inserción de lineas en la tabla WM_GL_LINES_GAS ");
		String consultalines = String.format(lines, REF9);
		System.out.println("CONSULTA  LINES"+consultalines);
		SQLResult resultlines = executeQuery(dbRms, consultalines);
		boolean av0 = resultlines.isEmpty();

		if (!av0) {

			testCase.addQueryEvidenceCurrentStep(resultlines);

		}

		System.out.println(av0);

		assertFalse(av0, "No se obtiene informacion de la consulta");

		//
		
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestFullName() {
		return " ATC_FT_004_RO8_GAS_Ajustes ";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Verificar proceso de interface AJUSTES " + " la tabla fem_fif_stg de Retek.";
	}

	@Override
	public String setTestDesigner() {
		return "AutomationQA";
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
