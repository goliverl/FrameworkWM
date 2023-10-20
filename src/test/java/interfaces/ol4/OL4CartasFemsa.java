package interfaces.ol4;

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

public class OL4CartasFemsa extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_OL4_OL4CartasFemsa(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Ebs,
				GlobalVariables.DB_USER_Ebs,GlobalVariables.DB_PASSWORD_Ebs);
		utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, 
				GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);

		/*
		 * Variables
		 *********************************************************************/
		String tdcQueryCartasFemsa = "SELECT ORIGEN, DESCRIPCION_CORTA, WM_STATUS, CREATION_DATE, WM_RUN_ID"
				+ " FROM XXFC.XXFC_CARTAS_FEMSA" 
		        + " WHERE ORIGEN =  '" + data.get("origen") + "'" 
				+ " AND ORACLE_CR =  '" + data.get("oracle_cr") + "'"
				+ " AND CARTA_FEMSA_ID = '"+ data.get("carta_id") + "'";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" 
		        + " FROM WMLOG.wm_log_run"
		        + " WHERE interface = 'OL4'" 
		        + " AND START_DT >= TRUNC (SYSDATE)"
				+ " order by start_dt desc)" + " where rownum = 1";

		String tdcQueryStatusLog = "SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER" 
		        + " FROM WMLOG.WM_LOG_RUN"
				+ " WHERE INTERFACE = 'OL4'" 
		        + " AND RUN_ID = %s";

		String tdcQueryStatusThread = "SELECT PARENT_ID,THREAD_ID,NAME,STATUS,ATT1,ATT2 " 
		        + " FROM WMLOG.WM_LOG_THREAD "
				+ " WHERE PARENT_ID = %s"
				+ " ORDER BY THREAD_ID ASC";

		String tdcQueryError = "SELECT error_id,run_id,error_date,description,message FROM" 
		        + " WMLOG.wm_log_error"
				+ " WHERE run_id =  %s";
		
		String tdcQueryLegacyOutbound = "SELECT * FROM LEGUSER.LEGACY_OUTBOUND_DOCS"
				+ " WHERE DOC_TYPE = 'CAR'"
				+ " AND LEGACY_USER = 'LEGUSER'"
				+ " AND RUN_ID = %s";
		
		String tdcQueryCartasFemsaActual = "SELECT DESCRIPCION_CORTA, WM_STATUS, CREATION_DATE, WM_RUN_ID "
				+ " FROM XXFC.XXFC_CARTAS_FEMSA"
				+ " WHERE ORIGEN= '" + data.get("origen") + "'"
				+ " AND ORACLE_CR = '"+ data.get("oracle_cr") + "'"
				+ " AND WM_STATUS = 'E'"
				+ " AND WM_RUN_ID = %s";


		SeleniumUtil u1,u2; 
		PakageManagment pok1,pok2; 
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String statusToValidate = "S";
		String run_id, status, thread_status, thread_id;
	
		
		//testCase.setProject_Name("Interfaces Web Methods");
	//	testCase.setFullTestName("OL4_"+data.get("origen"));
		//testCase.setTest_Description("Transferir la información de las Cartas FEMSA de origen "+data.get("origen")+" del módulo de Oracle hacia el servidor Legacy vía FTP.");
		

		/*
		 * *****************************************************************************
		 * Pasos
		 ***********************************************************************************************/
		/*
		 * Paso 1
		 *****************************************************************************************/
		addStep("Verificar que las cartas FEMSA de origen "+data.get("origen")+" de la tabla XXFC_CARTAS_FEMSA tengan el campo WM_STATUS = 'L'.");

		System.out.println(GlobalVariables.DB_HOST_Ebs);

		System.out.println(tdcQueryCartasFemsa);

		SQLResult resultCartasFemsa = executeQuery(dbEbs, tdcQueryCartasFemsa);
		
        String cartas_status = resultCartasFemsa.getData(0, "WM_STATUS");
        
		testCase.addQueryEvidenceCurrentStep(resultCartasFemsa);

		boolean launcherEmpty = cartas_status.equals("L");

		assertTrue(launcherEmpty, "La Carta Femsa de origen no tiene el campo WM_STATUS = 'L'");
		
		/*
		 * Paso 2
		 *****************************************************************************************/

		  u1 = new SeleniumUtil(new ChromeTest(), true);
		  pok1 = new PakageManagment(u1, testCase);
		
		  addStep("Ejecutar la interface OL4.Pub:run para transferir los documentos con la información las Cartas FEMSA hacia el Servidor Legacy.");
		  
		  String contra = "http://"+user+":"+ps+"@"+server+":5555";
		  
		  u1.get(contra);
		  
		  u1.hardWait(4);
		  
		  pok1.runIntefaceWmWithInput10(data.get("interface"),data.get("servicio"),data.get("origen"),"origen");
		  
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
		  
		  addStep("Validar la correcta ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD de WMLOG.");
		  
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
		  
		  testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR"
		  );
		  
		  testCase.addQueryEvidenceCurrentStep(resultQueryError);
		  
		  } }
		  
		  assertTrue(
		  statusThread,"El registro de ejecución de la threads no fue exitoso.");
	      
		  /*
			 * Paso 6
			 *****************************************************************************************/
		  
		  addStep("Validar que se inserte la información de los documentos CAR enviados por FTP al servidor Legacy en la tabla LEGACY_OUTBOUND_DOCS de LEGUSER." );
		  
		  System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		  String queryLegacyOutbound = String.format(tdcQueryLegacyOutbound, run_id);
		    
	      System.out.println(queryLegacyOutbound);			

	      SQLResult resultLegacy = executeQuery(dbPuser, queryLegacyOutbound);
			
		  boolean legacyEmpty = resultLegacy.isEmpty();
			
		  if (!legacyEmpty) {
				testCase.addQueryEvidenceCurrentStep(resultLegacy);
		  }

		  assertFalse(legacyEmpty,"No se inserta la información de los documentos CAR enviados.");
			

		  /*
			 * Paso 7
			 *****************************************************************************************/
		  
		  addStep("Validar la actualización del estado (WM_STATUS = 'E') de las Cartas FEMSA enviadas al Servidor Legacy en la tabla XXFC_CARTAS_FEMSA de Oracle.");
		  
		  System.out.println(GlobalVariables.DB_HOST_Ebs);
		  
		  String queryCartasFemsaActual = String.format(tdcQueryCartasFemsaActual, run_id);

		  System.out.println(queryCartasFemsaActual);
		  
		  SQLResult resultCartasFemsa2 = executeQuery(dbPuser, queryLegacyOutbound);
		  
		  cartas_status = resultCartasFemsa2.getData(0, "WM_STATUS");
	        
		  testCase.addQueryEvidenceCurrentStep(resultCartasFemsa);

		  boolean statusUpdate = cartas_status.equals("E");

		  assertTrue(statusUpdate,"El estado (WM_STATUS) de las Cartas Femsa enviadas no cambio a E.");
			
		  
	

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construida. Transferir la información de las Cartas FEMSA hacia el servidor Legacy vía FTP";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_OL4_OL4CartasFemsa";
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
