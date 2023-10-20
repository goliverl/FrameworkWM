package interfaces.ro8mex;

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
import utils.sql.SQLUtil;

public class ATC_FT_001_RO8_MEX_AjustesInterfaz extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_RO8_MEX_AjustesInterfaz_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		utils.sql.SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA,
				GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, 
				GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
				GlobalVariables.DB_USER_RMS_MEX,GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);

		/*
		 * Variables
		 *********************************************************************/
		String tdcQueryWmCfgLauncher = "SELECT INTERFACE_NAME,AUTO_STATUS,MANUAL_STATUS,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4 FROM WMUSER.WM_CFG_LAUNCHER" 
		        + " WHERE INTERFACE_NAME = 'RO8_MEX'"
		        + " AND ATTRIBUTE1= '" + data.get("plaza") + "'" 
				+ " AND ATTRIBUTE2 = '" + data.get("tran_code") + "'"
				+ " AND AUTO_STATUS = 'A'";

		String tdcQueryFemFifStg1 = "SELECT ITEM, ITEM_DESC, TRAN_DATE, TRAN_CODE, REFERENCE_3, REFERENCE_9, ID" 
		        + " FROM FEM_FIF_STG" + " WHERE tran_date >= TRUNC (SYSDATE) - 60"			
		        + " AND cr_plaza = '" + data.get("plaza") + "'" 			
		        + " AND tran_code = '" + data.get("tran_code")+ "'" 
		        + " AND reference_3 IS NULL" + 
		        " AND reference_9 IS NULL";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" 
		        + " FROM WMLOG.wm_log_run"
		        + " WHERE interface = '" + data.get("bd") + "'" 
		        + " AND START_DT >= TRUNC (SYSDATE)"
				+ " order by start_dt desc)" + " where rownum = 1";

		String tdcQueryStatusLog = "SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER" 
		        + " FROM WMLOG.WM_LOG_RUN"
				+ " WHERE INTERFACE = '" + data.get("bd") + "'" 
		        + " AND RUN_ID = %s";

		String tdcQueryStatusThread = "SELECT PARENT_ID,THREAD_ID,NAME,STATUS,ATT1,ATT2 " 
		        + " FROM WMLOG.WM_LOG_THREAD "
				+ " WHERE PARENT_ID = %s"
				+ " ORDER BY THREAD_ID ASC";

		String tdcQueryError = "SELECT error_id,run_id,error_date,description,message FROM" 
		        + " WMLOG.wm_log_error"
				+ " WHERE run_id =  %s";

		String tdcQueryWmGlHeaders = "SELECT HEADER_ID,TRAN_CODE,TRAN_DATE,NET_COST,GL_JOURNAL_ID,RUN_ID"
				+ " FROM WMUSER.WM_GL_HEADERS_MEX" 
				+ " WHERE TRAN_CODE =" + data.get("tran_code")
				+ " AND  RUN_ID=%s "
				+ " AND CR_PLAZA = '" + data.get("plaza") + "'";

		String tdcQueryFemFifStg2 = "SELECT ITEM, ITEM_DESC, TRAN_DATE, TRAN_CODE, REFERENCE_3, REFERENCE_9, ID"
				+ " FROM FEM_FIF_STG " 
				+ " WHERE CR_PLAZA = '" + data.get("plaza") + "'"
				+ " AND TRAN_CODE = " + data.get("tran_code") 
				+ " AND TRAN_DATE >= TRUNC (SYSDATE) - 60"
				+ " AND REFERENCE_3 IS NOT NULL " 
				+ "AND REFERENCE_9 = '%s'";

		String tdcQueryGlInterface = "SELECT STATUS, DATE_CREATED, ACTUAL_FLAG,REFERENCE1, GROUP_ID"
				+ " FROM GL.GL_INTERFACE WHERE GROUP_ID = %s";

		String tdcQueryGlHeadersMexHist = "SELECT HEADER_ID, TRAN_CODE, CR_PLAZA, GL_JOURNAL_ID, JOURNAL_TYPE_ID"
				+ " FROM WMUSER.WM_GL_HEADERS_MEX_HIST" 
				+ " WHERE CR_PLAZA = '" + data.get("plaza") + "'"
				+ " AND TRAN_CODE = " + data.get("tran_code") 
				+ " AND HEADER_ID = %s";

		SeleniumUtil u1,u2; 
		PakageManagment pok1,pok2; 
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String statusToValidate = "S";
		String run_id, status, thread_status, thread_id;

		testCase.setProject_Name("Interfaces Web Methods");
		testCase.setFullTestName("RO8_MEX_"+data.get("tran"));
	

		/*
		 * *****************************************************************************
		 * Pasos
		 ***********************************************************************************************/
		/*
		 * Paso 1
		 *****************************************************************************************/
		addStep("Verificar que la plaza se encuentre configurada en la tabla WM_CFG_LAUNCHER de WMINT");

		System.out.println(GlobalVariables.DB_HOST_Puser);

		System.out.println(tdcQueryWmCfgLauncher);

		SQLResult resultQueryWmCfgLauncher = executeQuery(dbPuser, tdcQueryWmCfgLauncher);

		testCase.addQueryEvidenceCurrentStep(resultQueryWmCfgLauncher);

		boolean launcherEmpty = resultQueryWmCfgLauncher.isEmpty();

		assertFalse(launcherEmpty, "La plaza se encuentra configurada");
		
		/*
		 * Paso 2
		 *****************************************************************************************/

		addStep("Validar información pendiente de procesar en la tabla FEM_FIF_STG");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);

		System.out.println(tdcQueryFemFifStg1);

		SQLResult resultQueryFemFifStg1 = executeQuery(dbRms, tdcQueryFemFifStg1);

		testCase.addQueryEvidenceCurrentStep(resultQueryFemFifStg1);

		boolean femFifEmpty = resultQueryFemFifStg1.isEmpty();

		assertFalse(femFifEmpty, "No existe información pendiente de procesar");

		/*
		 * Paso 3
		 *****************************************************************************************/
		  u1 = new SeleniumUtil(new ChromeTest(), true);
		  pok1 = new PakageManagment(u1, testCase);
		
		  addStep("Ejecutar la interfaz RO8 con el servicio "+data.get("servicio"));
		  
		  String contra = "http://"+user+":"+ps+"@"+server+":5555";
		  
		  u1.get(contra);
		  
		  u1.hardWait(4);
		  
		  pok1.runIntefaceWM(data.get("interface"),data.get("servicio"),null);
		  
		  System.out.print(tdcQueryIntegrationServer);
		  
		  SQLResult resultQueryIS = dbLog.executeQuery(tdcQueryIntegrationServer);
		  
		  run_id = resultQueryIS.getData(0, "RUN_ID");//guarda el run id de la ejecución
		  
		  status = resultQueryIS.getData(0, "STATUS");
		  
		  boolean valuesStatus = status.equals(searchedStatus);//Valida si se encuentra en estatus R
		  
		  while (valuesStatus) {
		  
		  resultQueryIS = executeQuery(dbLog, tdcQueryIntegrationServer);
		  
		  run_id = resultQueryIS.getData(0, "RUN_ID");//guarda el run id de la ejecución
		  
		  status = resultQueryIS.getData(0, "STATUS");
		  
		  valuesStatus = status.equals(searchedStatus);
		  
		  u1.hardWait(2);
		  
		  }
		  
		  boolean successRun = status.equals(statusToValidate);//Valida si se encuentra en estatus S
		  
		  if(!successRun){
		  
		  String error = String.format(tdcQueryError, run_id);
		  
		  SQLResult resultQueryError = executeQuery(dbLog, error);
		  
		  boolean emptyError = resultQueryError.isEmpty();
		  
		  if(!emptyError){
		  
		  testCase.
		  addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR"
		  );
		  
		  testCase.addQueryEvidenceCurrentStep(resultQueryError);
		  
		  } }
		  

		 
		  /*
			 * Paso 4
			 *****************************************************************************************/
		
		  addStep("Validar que el registro de ejecución de la interfaz termino en estatus 'S' en la tabla WM_LOG_RUN.");
		  
		  System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		  
		  String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
		  
		  System.out.println(queryStatusLog);
		  
		  SQLResult resultQueryStatusLog = executeQuery(dbLog, queryStatusLog);
		  
		  String statusWmLog = resultQueryIS.getData(0, "STATUS");
		  
		  boolean validateStatus = statusWmLog.equals(statusToValidate);
		  
		  System.out.println(validateStatus);
		  
		  testCase.addQueryEvidenceCurrentStep(resultQueryStatusLog);
		  
		  assertTrue(validateStatus,"La ejecución de la interfaz no fue exitosa");
		  
		  /*
			 * Paso 5
			 *****************************************************************************************/
		  
		  addStep("Validar que los threads de la ejecución terminaron en estatus 'S' en la tabla WM_LOG_THREAD.");
		  
		  System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		  
		  String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		  
		  System.out.println(queryStatusThread);
		  
		  SQLResult resultThreads = executeQuery(dbLog, queryStatusThread);
		  
		   
		  thread_status = resultThreads.getData(0, "STATUS");
		  
		  thread_id = resultThreads.getData(0, "THREAD_ID");
		  
		  boolean statusThread = status.equals(thread_status);
		  
		  System.out.println(statusThread);
		  
		  testCase.addQueryEvidenceCurrentStep(resultThreads);
		  
		  if(!statusThread){
		  
		  String error = String.format(tdcQueryError, run_id);
		  
		  SQLResult resultQueryError = executeQuery(dbLog, error);
		  
		  boolean emptyError = resultQueryError.isEmpty();
		  
		  if(!emptyError){
		  
		  testCase.
		  addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR"
		  );
		  
		  testCase.addQueryEvidenceCurrentStep(resultQueryError);
		  
		  } }
		  
		  assertFalse(
		  statusThread,"El registro de ejecución de la threads no fue exitoso.");
	      
		  /*
			 * Paso 6
			 *****************************************************************************************/
		  
		  addStep("Verificar que los datos sean insertados en la tabla WM_GL_HEADERS_MEX de RETEK." );
		  
		  System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		  		  
          String consultafor="";
		  
		  SQLResult resultfor;
		  
		  for(int i = 0; i < resultThreads.getRowCount(); i++) {
			  
			  
			  String id = resultThreads.getData(i, "THREAD_ID");
			  System.out.println(id);
			  consultafor = String.format(tdcQueryWmGlHeaders, id);
			  resultfor = executeQuery(dbRms, consultafor);
			  boolean foav = resultfor.isEmpty();
			  if (!foav) {

			   thread_id = resultThreads.getData(i, "THREAD_ID");

			   }
			  }
		  
		  String queryWmGlHeaders = String.format(tdcQueryWmGlHeaders, thread_id);
		  
		  System.out.println(queryWmGlHeaders);	  
		  
		  SQLResult resultQueryWmGlHeaders = executeQuery(dbRms, queryWmGlHeaders);
		  
		  String header_id = resultQueryWmGlHeaders.getData(0, "HEADER_ID");
		  
		  boolean wmGlHeaders = resultQueryWmGlHeaders.isEmpty();
		  
		  if (!wmGlHeaders) {

			  testCase.addQueryEvidenceCurrentStep(resultQueryWmGlHeaders);

		  }	  
		  
		  assertFalse(wmGlHeaders,"No se insertaron los datos procesados.");
		  
		  /*
			 * Paso 7
			 *****************************************************************************************/
		  
		  addStep("Verificar la actualización de los campos:  REFERENCE_3 y REFERENCE_9 en la tabla FEM_FIF_STG de RETEK."
		  );
		  
		  System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		  
		  String queryFemFifStg2 = String.format(tdcQueryFemFifStg2,header_id);
		  
		  System.out.println(queryFemFifStg2);
		  
		  SQLResult resultQueryqueryFemFifStg2 = executeQuery(dbRms, queryFemFifStg2);
		  
		  String reference3 = resultQueryqueryFemFifStg2.getData(0, "reference_3");
		  
		  testCase.addQueryEvidenceCurrentStep(resultQueryqueryFemFifStg2 );
		  
		  boolean femFifStg = resultQueryqueryFemFifStg2 .isEmpty();
		  
		  assertFalse( femFifStg,"No se actualizaron los campos REFERENCE_3 y REFERENCE_9.");
		  
		  /*
			 * Paso 8
			 *****************************************************************************************/
		  
		  addStep("Verificar la inserción de lineas en la tabla GL_INTERFACE de ORAFIN.");
		  
		  System.out.println(GlobalVariables.DB_USER_EBS);
		  
		  String queryGlInterface = String.format(tdcQueryGlInterface, reference3);
		  
		  System.out.println(queryGlInterface);
		  
		  SQLResult resultQueryGlInterface = executeQuery(dbEbs, queryGlInterface);
		  
		  testCase.addQueryEvidenceCurrentStep(resultQueryGlInterface );
		  
		  boolean glEmpty = resultQueryGlInterface .isEmpty();
		  
		  assertFalse(glEmpty,"No se insertaron lineas en la tabla.");
		  
		  /*
			 * Paso 9
			 *****************************************************************************************/
		  u2 = new SeleniumUtil(new ChromeTest(), true);
		  pok2 = new PakageManagment(u2, testCase);
		
		  addStep("Ejecutar el servicio de historico "+data.get("servicio2"));
		  
		  contra = "http://"+user+":"+ps+"@"+server+":5555";	  
		  
		  u2.get(contra);
		  
		  u2.hardWait(60);
		  
		  pok2.runIntefaceWmOneButton(data.get("interface"),data.get("servicio2"));
		  
		  resultQueryIS = executeQuery(dbLog, tdcQueryIntegrationServer);
		  
		  run_id = resultQueryIS.getData(0, "RUN_ID");//guarda el run id de la ejecución
		  
		  status = resultQueryIS.getData(0, "STATUS");
		  
		  valuesStatus = status.equals(searchedStatus);//Valida si se encuentra en estatus R
		  
		  while (valuesStatus) {
		  
		  resultQueryIS = executeQuery(dbLog, tdcQueryIntegrationServer);
		  
		  run_id = resultQueryIS.getData(0, "RUN_ID");//guarda el run id de la ejecución
		  
		  status = resultQueryIS.getData(0, "STATUS");
		  
		  valuesStatus = status.equals(searchedStatus);
		  
		  u2.hardWait(2);
		  
		  }
		  
		  successRun = status.equals(statusToValidate);//Valida si se encuentra en estatus S
		  
		  if(!successRun){
		  
		  String error = String.format(tdcQueryError, run_id);
		  
		  SQLResult resultQueryError = executeQuery(dbLog, error);
		  
		  boolean emptyError = resultQueryError.isEmpty();
		  
		  if(!emptyError){
		  
		  testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la "
		  		+ "interfaz en la tabla WM_LOG_ERROR");
		  
		  testCase.addQueryEvidenceCurrentStep(resultQueryError);
		  
		  } }
		  
		  /*
			 * Paso 10
			 *****************************************************************************************/
		  
		  addStep("Verificar que los datos son insertados en la tabla de historico: WM_GL_HEADERS_MEX_HIST de RETEK.");
		  
		  System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		  
		  String queryGlHeadersMexHist = String.format(tdcQueryGlHeadersMexHist, header_id);
		  
		  System.out.println(queryGlHeadersMexHist);
		  
		  SQLResult resultGlHeadersMexHist = executeQuery(dbRms, queryGlHeadersMexHist);
		  
		  testCase.addQueryEvidenceCurrentStep(resultGlHeadersMexHist);
		  
		  boolean wmGlHeadersHist = resultGlHeadersMexHist.isEmpty();
		  
		  assertFalse(wmGlHeadersHist,"No se insertaron los datos en la tabla de historial.");
		  
		 

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestFullName() {
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Verificar proceso de interface AJUSTES "
				+ " la tabla fem_fif_stg de Retek.";
	}

	@Override
	public String setTestDesigner() {
		return "Equipo de automatizacion";
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
