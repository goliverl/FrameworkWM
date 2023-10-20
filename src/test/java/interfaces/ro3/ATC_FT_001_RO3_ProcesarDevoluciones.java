package interfaces.ro3;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.MEdgeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class ATC_FT_001_RO3_ProcesarDevoluciones extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_RO3_ProcesarDevoluciones_test(HashMap<String, String> data) throws Exception {

/* Utilerías *********************************************************************/
		
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);		
		
		String status = "S";
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id = "" ;
		
		testCase.setProject_Name("I20134-TI01 Homologación CxP FEMSA");  
		testCase.setTest_Description("Procesar las Devoluciones del CEDIS " + data.get("cedis") +" en RETEK");
		
/**
* Variables ******************************************************************************************
* 
* 
*/
	
		String tdcQueryReference = "SELECT RTV_ORDER_NO, WH, REFERENCE_2, REFERENCE_9, COMPLETED_DATE "
				+ " FROM fem_rtvhead_temp "
				+ " WHERE wh <> '-1'"
				+ " AND reference_2 IS NULL" 
				+ " AND reference_9 IS NULL" 
				+ " AND wh = " + data.get("wh") +"";
				//		+ " and RTV_ORDER_NO = '830040028'";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id, interface, start_dt, end_dt, status, server "
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface like '%" + data.get("wm_log") +"%'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum = 1";
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; 
		
	/*	String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = %s" ; */
		
		String consultaThreads = "SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID='%s' "
				+ "AND ATT2 = '"+data.get("att2")+"'"
				+ "ORDER BY THREAD_ID DESC"; // FCWMLQA 
 
		String consultaThreads2 = "SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 "
				+ "FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID='%s' "
				+ "AND ATT2 = '"+data.get("att2")+"'"
				+ "ORDER BY THREAD_ID DESC"; // FCWMLQA 
		
		String tdcQueryRefNotNull = "SELECT RTV_ORDER_NO, WH, REFERENCE_2, REFERENCE_9, COMPLETED_DATE "
				+ " FROM fem_rtvhead_temp WHERE wh <> '-1'"  
				+ " AND reference_2 IS NOT NULL" 
				+ " AND reference_9 IS NOT NULL"
				+ " AND wh = " + data.get("wh") +""
				+ " AND rtv_order_no = %s";
		
		String tdcQueryHeaders = "SELECT HEADER_ID, RUN_ID, AP_INVOICE_ID, CREATED_DATE"
				+ " FROM wmuser.wm_ap_headers"
				+ " WHERE ap_invoice_id IS NOT NULL" 
			//	+ " AND created_date >= TRUNC(SYSDATE)"
				+ " AND run_id = %s"
				+ " AND header_id = %s";
		
		String tdcQueryapInterface = "SELECT INVOICE_ID, INVOICE_NUM, INVOICE_DATE, VENDOR_ID, STATUS "
				+ " FROM ap.ap_invoices_interface "
				+ " WHERE invoice_id = %s";
		
		String tdcQueryapLines = "SELECT INVOICE_ID, INVOICE_LINE_ID, LINE_NUMBER, LINE_TYPE_LOOKUP_CODE "
				+ " FROM ap.ap_invoice_lines_interface "
				+ " WHERE invoice_id = %s";
			
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
				
//		Paso 1	************************ 
		
		addStep("Validar que exista información pendiente de procesar para el CEDIS "+data.get("att2")+" con clave retek " + data.get("wh") +".");
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(tdcQueryReference);
		String rtv = "";
		
		SQLResult referenceResult = executeQuery(dbRms, tdcQueryReference);		
		boolean reference = referenceResult.isEmpty();
		testCase.addQueryEvidenceCurrentStep(referenceResult);
		
			if (!reference) {	
			    rtv = referenceResult.getData(0, "RTV_ORDER_NO");	
			} 
		
		System.out.println(reference);

		assertFalse(reference, "No se obtiene informacion de la consulta");

//		Paso 2	************************ 
		
		addStep("Ejecutar el servicio "+data.get("servicio"));		 
		
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		
		pok.runIntefaceWmTwoButtonsWihtoutInputs10(data.get("interfase"), data.get("servicio"));
		
		//pok.runIntefaceWmTextBox(data.get("interfase"), data.get("servicio"));
		//pok.runIntefaceWmWithInput(data.get("interfase"), data.get("servicio"), "22", "tranType");
       
		String status1 = "";
		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		if(!query.isEmpty()) {
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");
		}
		
		
//		Paso 3	************************
		//comentar sig 2 lineas
		//status1 = "S";
		//run_id = "2175775949";
				
		addStep("Validar que el registro de la tabla WM_LOG_RUN termine en estatus 'S'.");
		boolean valuesStatus = status1.equals(searchedStatus);//Valida si se encuentra en estatus R
		while (valuesStatus) {
			
			query = executeQuery(dbLog, tdcQueryIntegrationServer);	
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");	 
	    	u.hardWait(2);
		 
		}
			
		boolean successRun = status1.equals(status);//Valida si se encuentra en estatus S
		testCase.addQueryEvidenceCurrentStep(query);
		
		    if(!successRun){
		   
		       String error = String.format(tdcQueryErrorId, run_id);
		       SQLResult paso2 = executeQuery(dbLog, error);	   
		       boolean emptyError = paso2.isEmpty();
		   
		       if(!emptyError){  		   
		          testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");		   
		          testCase.addQueryEvidenceCurrentStep(paso2);		   
		   }
		}
		    
		assertTrue(successRun, "La ejecución de la interfaz no fue exitosa");
		    
		    
//		Paso 4	************************
		
		addStep("Se valida la generacion de thread.");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);	
		String statusThreadFormat = String.format(consultaThreads, run_id);
		System.out.println(statusThreadFormat);
		SQLResult threadResult = executeQuery(dbLog, statusThreadFormat);		
		testCase.addQueryEvidenceCurrentStep(threadResult);
		String statusThreadFormat2 = String.format(consultaThreads2, run_id);
		System.out.println(statusThreadFormat2);
		SQLResult threadResult2 = executeQuery(dbLog, statusThreadFormat2);		
		testCase.addQueryEvidenceCurrentStep(threadResult2);
		
		boolean ST = false;
		String statusThread="", thread_id="";

		if (!threadResult.isEmpty()) {
		     statusThread = threadResult.getData(0, "Status");
		     thread_id = threadResult.getData(0, "thread_id");
		     ST = statusThread.equals(status);    
		} 

		System.out.println(ST);

		assertTrue(ST, "No se obtiene informacion o el campo STATUS del Thread es diferente a 'S' en la tabla WM_LOG_THREAD");

//		Paso 5	************************
		addStep("Verificar que los datos se hayan procesado en la tabla fem_rtvhead_temp de RETEK.");
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
	
		//comentar sig linea
		//rtv = "830036918";
		
		String formatRefNotNull = String.format(tdcQueryRefNotNull, rtv);
		System.out.println(formatRefNotNull);
		SQLResult refNotNullResult= executeQuery(dbRms, formatRefNotNull);
		boolean refNotNull = refNotNullResult.isEmpty();
		
		String ref9="", ref2="";
		if(!refNotNull) {
			ref9 = refNotNullResult.getData(0, "REFERENCE_9");
			ref2 = refNotNullResult.getData(0, "REFERENCE_2");
		}
		
	    testCase.addQueryEvidenceCurrentStep(refNotNullResult);
		System.out.println(refNotNull);

		assertFalse(refNotNull, "No se obtiene informacion de la consulta");
	
		
//		Paso 6	************************
		addStep("Validar la actualización del campo ap_invoice_id en la tabla wm_ap_headers en RETEK.");
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String consultafor="";
		SQLResult resultfor;
		
		for(int i = 0; i < threadResult.getRowCount(); i++) {
			String p = threadResult.getData(i, "THREAD_ID");
			System.out.println(p);
			consultafor = String.format(tdcQueryHeaders, p, ref9);
			resultfor = executeQuery(dbRms, consultafor);
			
			boolean foav = resultfor.isEmpty();			
			if (!foav) {
				thread_id = threadResult.getData(i, "THREAD_ID");
			}
		
		}
							
		String formatHeaders = String.format(tdcQueryHeaders, thread_id, ref9);
		System.out.println(formatHeaders);
		
		SQLResult headersResult= executeQuery(dbRms, formatHeaders);
		boolean headers = headersResult.isEmpty();	
		
		testCase.addQueryEvidenceCurrentStep(headersResult);
		System.out.println(headers);

		assertFalse(headers, "No se obtiene informacion de la consulta");
		
//		Paso 7	************************
		addStep("Verificar que la información se haya insertado en la tabla ap_invoices_interface en ORAFIN.");
		System.out.println(GlobalVariables.DB_HOST_Ebs);
		String apFormat = String.format(tdcQueryapInterface, ref2);
		System.out.println(apFormat);
		
		SQLResult apResult= executeQuery(dbEbs, apFormat);
		boolean apInterface = apResult.isEmpty();
		
		testCase.addQueryEvidenceCurrentStep(apResult);
        System.out.println(apInterface);

		assertFalse(apInterface, "No se obtiene informacion de la consulta");
		
		
//		Paso 8	************************
		
		addStep("Verificar que la información se haya insertado en la tabla ap_invoice_lines_interface en ORAFIN.");
		System.out.println(GlobalVariables.DB_HOST_Ebs);
		String apLineFormat = String.format(tdcQueryapLines, ref2);
		System.out.println(apLineFormat);
		
		SQLResult apLineResult= executeQuery(dbEbs, apLineFormat);
		boolean apLine = apLineResult.isEmpty();
	
		testCase.addQueryEvidenceCurrentStep(apLineResult);
		System.out.println(apLine);

		assertFalse(apLine, "No se obtiene informacion de la consulta");
			
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
		// TODO Auto-generated method stuO
		return "Validar la correcta ejecución de la interface RO3_MX";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
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