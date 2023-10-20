package interfaces.pr35;

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

public class PR35ProcesamientoPos extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PR35_ProcesamientoPos(HashMap<String, String> data) throws Exception {
	
		/* Utilerías *********************************************************************/		
	    						//ORIGINAL
								//new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCRMSMGR, GlobalVariables.DB_USER_FCRMSMGR, GlobalVariables.DB_PASSWORD_FCRMSMGR);
								  
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA); // SIN CAMBIOS
		
								//ORIGINAL
								//new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_fcwmesit, GlobalVariables.DB_USER_fcwmesit, GlobalVariables.DB_PASSWORD_fcwmesit); // NUCLEO
								 
		utils.sql.SQLUtil dbEbs= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS, GlobalVariables.DB_PASSWORD_EBS);
			
		/**
		 * Variables
		 ******************************************************************************************
		 * 
		 */

		//														PASO 1		
        String tdcAvailebleDateIn = "SELECT to_char(max(END_DATE), 'DD/MM/YYYY HH:MI:SS AM') LAST_EXEC_DATE "
        		+ "FROM wmuser.WM_DATES_EXEC WHERE INTERFASE = 'PR35'"; // pos user

       //														PASO 2
		String tdcAvailableDateIn2 = "SELECT to_char(sysdate,'DD/MM/YYYY HH:MI:SS AM') CURRENT_DATE FROM DUAL"; //end_date

       //														PASO 2
		String tdcDatosProc = "SELECT DISTINCT il.loc location, SUBSTR(store_name10, 1, 5) plaza"  
				+ " FROM wmuser.item_master im, wmuser.item_loc il, store s" 
				+ " WHERE im.item = il.item" 
				+ " AND ((il.create_datetime >= TO_DATE ('%s', 'DD/MM/YYYY HH:MI:SS AM')" 
				+ " AND il.create_datetime <=  TO_DATE ('%s', 'DD/MM/YYYY HH:MI:SS AM'))" 
				+ " OR (im.last_update_datetime >= TO_DATE ('%s', 'DD/MM/YYYY HH:MI:SS AM')" 
				+ " AND im.last_update_datetime <=  TO_DATE ('%s', 'DD/MM/YYYY HH:MI:SS AM')))" 
				+ " AND im.pack_ind = 'Y'" 
				+ " AND s.store = il.loc" 
				//"AND  il.loc = \r\n" + 
				+ " ORDER BY plaza";
		

		String tdcTableMaestros = "SELECT ORACLE_CR_DESC, RETEK_CR, ESTADO, ORACLE_CR_TYPE, ORACLE_CR_SUPERIOR"
				+ " FROM XXFC_MAESTRO_DE_CRS_V" 
				+ " WHERE ESTADO = 'A'" 
				+ " AND ORACLE_CR_TYPE = 'T'"
				+ " AND RETEK_CR = '%s'"; // Appsview_EBS
                   
		//														PASO 3
		String FTPContention = "SELECT FTP_BASE_DIR, FTP_SERVERHOST, FTP_SERVERPORT, FTP_USERNAME, FTP_PASSWORD"
				+ " FROM WMUSER.WM_FTP_CONNECTIONS"
				+ " WHERE FTP_CONN_ID = 'PR50POS'";
                
        //														PASO 4-5
		String tdcQueryStatusLog = "SELECT run_id, interface, start_dt, status, server"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PR35main' AND status = 'S' AND start_dt >= trunc(sysdate)" // FCWMLQA
				+ " ORDER BY start_dt DESC";

		String tdcQueryIntegrationServer = "SELECT * FROM (SELECT run_id, start_dt, status FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PR35main'"
				+ " AND status = 'E'"
				+ " AND start_dt >= TRUNC(SYSDATE)"
				+ " ORDER BY start_dt desc) WHERE ROWNUM = 1";

		String tdcQueryStatusThread = "SELECT parent_id, thread_id, name, wm_log_thread.status, att1, att2"
				+ " FROM WMLOG.wm_log_thread WHERE parent_id = '%s'"; // FCWMLQA

		String tdcQueryErrorId = "SELECT ERROR_ID, RUN_ID, ERROR_DATE, DESCRIPTION FROM WMLOG.WM_LOG_ERROR"
				+ " WHERE RUN_ID = '%s'"; // FCWMLQA
                    
		//														PASO 6
		String tdcDocPosOutbound = "SELECT SOURCE_ID, DOC_NAME, DOC_TYPE, SENT_DATE, PV_CR_PLAZA, PV_CR_TIENDA"
				+ " FROM POSUSER.POS_OUTBOUND_DOCS" 
				+ " WHERE SOURCE_ID = '%s'"
				+ " AND SENT_DATE >= TRUNC(SYSDATE)"
				+ " AND DOC_TYPE='PCC' ORDER BY SENT_DATE DESC ";

        //														PASO 7
          String RegistroPos = "SELECT * FROM wmuser.WM_DATES_EXEC WHERE INTERFASE = 'PR35'"
          		+ "AND  RUN_ID = '%s' "
          		+ "ORDER BY END_DATE DESC";
          
		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * ***************************************** 
		 * 
		 */
          
          
        //															PASO 1	
          
          addStep("Consulta la ultima fecha de ejecucion (start_date)");

          System.out.println(GlobalVariables.DB_HOST_fcwmesit);
          System.out.println(tdcAvailebleDateIn);

          SQLResult AvailableDate = executeQuery(dbPos, tdcAvailebleDateIn);		
          String LAST_EXEC_DATE = "";        
	
          boolean day = AvailableDate.isEmpty();
          if (!day) {
        	  LAST_EXEC_DATE = AvailableDate.getData(0, "LAST_EXEC_DATE");
        	  //testCase.addQueryEvidenceCurrentStep(AvailableDate);
          }
          
          System.out.println("LAST_EXEC_DATE: " + LAST_EXEC_DATE);
          
          if (LAST_EXEC_DATE == null) {
        	  testCase.addTextEvidenceCurrentStep("La consulta no genera registros");
          }

          System.out.println(day);
          testCase.addQueryEvidenceCurrentStep(AvailableDate);
          // assertFalse(day, "La consulta no muestra informacion de la ultima ejecucion");
	
          
          //														PASO 2	

          addStep("Validar que exista información para el rango de fechas de ejecucion (end_date)");

          System.out.println(tdcAvailableDateIn2);

          SQLResult AvailableDate2 = executeQuery(dbPos, tdcAvailableDateIn2);		
          String CURRENT_DATE = null;

          boolean days = AvailableDate2.isEmpty();
          if (!days) {
			CURRENT_DATE = AvailableDate2.getData(0, "CURRENT_DATE");
          }
          
          System.out.println("CURRENT_DATE: " + CURRENT_DATE);
          
          if (CURRENT_DATE == null) {
        	  testCase.addTextEvidenceCurrentStep("La consulta no genera registros");
          }
          
          System.out.println(days);
          testCase.addQueryEvidenceCurrentStep(AvailableDate2);
         // assertFalse(days, "No se muestra la fecha actual");
          
		
          //														PASO 3		

          addStep("Validar que exista información para el rango de tiempo que designamos en las tablas ITEM_MASTER, ITEM_LOC y STORE");
          
          String location = "00000"; // Valor inicial, si posteriormente es lcoation = "00000" este paso falló
          							 // y no actualiza su valor, y habra error en los pasos posteriores
          
          if (LAST_EXEC_DATE == null || CURRENT_DATE == null) {
        	  testCase.addTextEvidenceCurrentStep("No se puede ejecutar la query: LAST_EXEC_DATE = " 
        			  + LAST_EXEC_DATE + " y CURRENT_DATE = " + CURRENT_DATE);
          } else {
          
        	  String datos = String.format(tdcDatosProc, LAST_EXEC_DATE, CURRENT_DATE, LAST_EXEC_DATE, CURRENT_DATE);		
        	  System.out.println(datos);
		
        	  SQLResult Date3 = executeQuery(dbRms, datos);		
	
	          boolean date = Date3.isEmpty();
	          if (!date) {
	        	  location = Date3.getData(0, "LOCATION");
	          } else {
	        	  testCase.addTextEvidenceCurrentStep("No se muestran registros que cumplan con el rango de fechas de ejecucion" 
	        			  + LAST_EXEC_DATE + " " + CURRENT_DATE);
	          }
	
	          System.out.println(date);
	          testCase.addQueryEvidenceCurrentStep(Date3);
	          //assertFalse(date, "No se muestran registros que cumplan con el rango de fechas de ejecucion");
          }
	
          //													PASO 4
          
          addStep("Comprobar que exista informacion necesaria para procesar en XXFC_MAESTRO_DE_CRS_V");
		
          System.out.println(GlobalVariables.DB_HOST_EBS);
          
          String tdcTableMaestrosForm = String.format(tdcTableMaestros, location);		
          System.out.println(tdcTableMaestrosForm);
	
          SQLResult TableMaestros = executeQuery(dbEbs, tdcTableMaestrosForm);

          boolean ma = TableMaestros.isEmpty();
          if (ma) {
        	  testCase.addTextEvidenceCurrentStep("No existe registro en 'XXFC_MAESTRO_DE_CRS_V' con RETEK_CR = '" + location + "'");
          }

          System.out.println(ma);
          testCase.addQueryEvidenceCurrentStep(TableMaestros);
          //assertFalse(ma, "La tabla no contiene ningun registro");		
		
		
          //														PASO 5 
          
          addStep(" Comprobar los datos para conectarse al Buzón de POS");
          
          System.out.println(FTPContention);

          SQLResult FTPContentionRes = executeQuery(dbPos, FTPContention);
	
          boolean ValidaFTPContention = FTPContentionRes.isEmpty();
          if (!ValidaFTPContention) {
        	  testCase.addQueryEvidenceCurrentStep(FTPContentionRes);
          }

          System.out.println(ValidaFTPContention);

          assertFalse(ValidaFTPContention, "No se encontraron los datos para conectarse al Buzón de POS");
          

          //														PASO 6			

          addStep("Ejecutar el servicio PR35.Pub:");
          
          SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
          PakageManagment pok = new PakageManagment(u, testCase);
          
          String status = "S";
          String user = data.get("user");
          String ps = PasswordUtil.decryptPassword(data.get("ps"));
          String server = data.get("server");
          String searchedStatus = "R";
          String run_id = "";
          
          String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
          u.get(contra);

          String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
          System.out.println("DATE EXECUTION: " + dateExecution);
        
          SQLResult IntegrationServer = executeQuery(dbLog, tdcQueryIntegrationServer);
          System.out.println(tdcQueryIntegrationServer);
         
          String Status = "";
          
          if (!IntegrationServer.isEmpty()) {
        	  run_id = IntegrationServer.getData(0, "RUN_ID");
              Status = IntegrationServer.getData(0, "STATUS");
          }

          boolean valuesStatus = Status.equals(searchedStatus);	//Valida si se encuentra en estatus R
          while (valuesStatus) {
        	  IntegrationServer = executeQuery(dbLog, tdcQueryIntegrationServer);
        	  run_id = IntegrationServer.getData(0, "RUN_ID");
        	  Status = IntegrationServer.getData(0, "STATUS");
        	  valuesStatus = Status.equals(searchedStatus);

        	  u.hardWait(2);
          }
          
          boolean successRun = Status.equals(status); //Valida si se encuentra en estatus S
          if (!successRun) {
        	  String error = String.format(tdcQueryErrorId, run_id);

        	  SQLResult errorQuery = executeQuery(dbLog, error);
			
        	  boolean emptyError = errorQuery.isEmpty();
        	  if (emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(errorQuery);
        	  }
        	  
          }

          // 														PASO 7	

          addStep("Comprobar que se registra la ejecucion en WMLOG");

          System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
          
          String queryStatusLog = String.format(tdcQueryStatusLog, run_id);          
          System.out.println(queryStatusLog);

          SQLResult StatusLog = executeQuery(dbLog, queryStatusLog);
          String fcwS = StatusLog.getData(0, "STATUS");

          boolean validateStatus = status.equals(fcwS);		
          if(validateStatus) {			
			testCase.addQueryEvidenceCurrentStep(StatusLog);
          }
          
          System.out.println(validateStatus);

          assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");
          

          //	 													PASO 8

          addStep("Se valida la generacion de threads");

          String queryStatusThread = String.format(tdcQueryStatusThread, run_id);		
          System.out.println(queryStatusThread);

          SQLResult StatusThread = executeQuery(dbLog, queryStatusThread);
		
          boolean statusThread = StatusThread.isEmpty();          
          if (statusThread) {
        	  testCase.addTextEvidenceCurrentStep("No existen registros en 'WMLOG.wm_log_thread' con parent_id = " + run_id);			
          }
          
          System.out.println(statusThread);
          
          testCase.addQueryEvidenceCurrentStep(StatusThread);
          //assertFalse(statusThread, "No se generaron threads");
          

          //														PASO 9

          addStep("Validar que se haya insertado los registro en la tabla POS_OUTBOUND_DOCS de los documentos enviados al servidor FTP");
         
          String PosPutBoundFormat = String.format(tdcDocPosOutbound, run_id);		
          System.out.println(PosPutBoundFormat);

          SQLResult PosPutBound = executeQuery(dbPos, PosPutBoundFormat);

          boolean doc = PosPutBound.isEmpty();
          if (doc) { //!
        	  testCase.addTextEvidenceCurrentStep("No existen registros en 'POS_OUTBOUND_DOCS' con SOURCE_ID = " + run_id);
          }
          
          testCase.addQueryEvidenceCurrentStep(PosPutBound);
          System.out.println(doc);

          // assertFalse(doc, "No se encuentran registros en la tabla POS_OUTBOUND_DOCS");
	
          
          // 														PASO 10
          
          addStep("Validar que se inserto un nuevo registro de la ejecucion en la DB WMINT.");

          String RegistroPosFormat = String.format(RegistroPos, run_id);		
          System.out.println(RegistroPosFormat);

          SQLResult RegistroPosRes = executeQuery(dbPos, RegistroPosFormat);

          boolean validaRegistroPosRes = RegistroPosRes.isEmpty();
          if (validaRegistroPosRes) {
        	  testCase.addTextEvidenceCurrentStep("No existen registros en 'WM_DATES_EXEC' con RUN_ID = " + run_id);
          }
          
          testCase.addQueryEvidenceCurrentStep(RegistroPosRes);
          System.out.println(validaRegistroPosRes);

          //assertFalse(validaRegistroPosRes, "No se encuentran registros en la tabla WM_DATES_EXEC ");
	}	

	@Override
	public String setTestFullName() {
		return "ATC_FT_001_PR35_ProcesamientoPos";
	}

	@Override
	public String setTestDescription() {
		return "Construido. ";
	}

	@Override
	public String setTestDesigner() {
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestInstanceID() {
		return null;
	}

	@Override
	public void beforeTest() {
		
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

}
