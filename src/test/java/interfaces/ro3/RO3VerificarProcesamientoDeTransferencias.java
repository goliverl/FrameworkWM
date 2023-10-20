package interfaces.ro3;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class RO3VerificarProcesamientoDeTransferencias extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_RO3_002_VerificarProcesamientoDeTransferencias(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/
		
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_Ebs, GlobalVariables.DB_USER_Ebs,GlobalVariables.DB_PASSWORD_Ebs);		
		
		String status = "S";
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id ;
		
		testCase.setProject_Name("AutomationQA");
		
/**
* Variables ******************************************************************************************
* 
* 
*/
		
		String tdcQueryFem = "SELECT CR_PLAZA, REFERENCE_2, REFERENCE_7, REFERENCE_8, FROM_LOC_TYPE, TO_LOC_TYPE, TRAN_CODE, TRAN_DATE" 
				+ " FROM fem_fif_stg"
//				+ " WHERE cr_plaza = '10AGC'"
				+ " WHERE reference_2 IS NULL" 
				+ " AND reference_7 = -1"
				+ " AND reference_8 IS NULL"
				+ " AND ((from_loc_type = 'W' AND to_loc_type = 'W' AND tran_code = 30)"
				+ " OR (from_loc_type = 'W' AND to_loc_type = 'S' AND tran_code = 30)"
				+ " OR (from_loc_type = 'S' AND to_loc_type = 'W' AND tran_code = 32))"
				+ " AND tran_date >= TRUNC(SYSDATE)";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = '" + data.get("wm_log") +"'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum = 1";
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"
				+ " and rownum = 1"; 
		
		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = %s" ; 
		
		String tdcQueryfemNotNull = "SELECT CR_PLAZA, REFERENCE_2, REFERENCE_7, REFERENCE_8, FROM_LOC_TYPE, TO_LOC_TYPE, TRAN_CODE, TRAN_DATE" 
				+ " FROM fem_fif_stg"
//				+ " WHERE cr_plaza = '10AGC'"
				+ " WHERE reference_2 IS NOT NULL"
				+ " AND reference_8 IS NOT NULL"
				+ " AND reference_7 = 0" 
				+ " AND ((from_loc_type = 'W' AND to_loc_type = 'W' AND tran_code = 30)"
				+ " OR (from_loc_type = 'W' AND to_loc_type = 'S' AND tran_code = 30)" 
				+ " OR (from_loc_type = 'S' AND to_loc_type = 'W' AND tran_code = 32))" 
				+ " AND tran_date >= TRUNC(SYSDATE)";
		
		String tdcQueryHeaders = "SELECT HEADER_ID, RUN_ID, AP_INVOICE_ID, CREATED_DATE "
				+ " FROM wmuser.wm_ap_headers "
				+ " WHERE ap_invoice_id IS NOT NULL "
				+ " AND created_date = TRUNC(SYSDATE) "
				+ " AND run_id = %s "
				+ " AND header_id = %s";
		
		String tdcQueryapInterface = "SELECT INVOICE_ID, INVOICE_NUM, VENDOR_ID "
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
				
		addStep("Validar que exista información pendiente de procesar para las Transferencias de la plaza en la tabla fem_fif_stg de RETEK..");
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(tdcQueryFem);
					
		SQLResult femResult = executeQuery(dbRms, tdcQueryFem);		

		boolean fem = femResult.isEmpty();
		
		if (!fem) {
			
			testCase.addQueryEvidenceCurrentStep(femResult);
			} 
				
		System.out.println(fem);

		assertFalse(fem, "No se obtiene informacion de la consulta");
		
//		Paso 2	************************ 
		
		addStep("Ejecutar el servicio RO3.Mappings:run.");
		
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		
		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));

		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		
//		Paso 3	************************
		addStep("Validar que el registro de la tabla WM_LOG_RUN termine en estatus 'S'.");
		boolean valuesStatus = status1.equals(searchedStatus);//Valida si se encuentra en estatus R
		while (valuesStatus) {
			
			query = executeQuery(dbLog, tdcQueryIntegrationServer);	
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");
		 
		 u.hardWait(2);
		 
		}
		
		boolean successRun = status1.equals(status);//Valida si se encuentra en estatus S
		    if(!successRun){
		   
		   String error = String.format(tdcQueryErrorId, run_id);
		   SQLResult paso2 = executeQuery(dbLog, error);
		   
		   boolean emptyError = paso2.isEmpty();
		   
		   if(!emptyError){  
		   
		    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
		   
		    testCase.addQueryEvidenceCurrentStep(paso2);
		   
		   }
		}
		    testCase.addQueryEvidenceCurrentStep(query);

//		Paso 4	************************
		addStep("Se valida la generacion de thread.");
			
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String statusThreadFormat = String.format(tdcQueryStatusThread, run_id);
		SQLResult threadResult = executeQuery(dbLog, statusThreadFormat);

		System.out.println(statusThreadFormat);
		String statusThread = threadResult.getData(0, "Status");
		String thread_id = threadResult.getData(0, "tread_id");

		boolean ST = statusThread.equals(status);
		ST = !ST;

		if (!ST) {

			testCase.addQueryEvidenceCurrentStep(threadResult);
				    
				} 

		System.out.println(ST);

		assertFalse(ST, "No se obtiene informacion de la consulta");
		
//		Paso 5 ***********************
		
		addStep("Verificar que los campos reference_2, reference_7 y reference8 no esten nulos, en la tabla fem_fif_stg de RETEK.");
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(tdcQueryfemNotNull);
					
		SQLResult femNotNullResult = executeQuery(dbRms, tdcQueryfemNotNull);
		String ref8 = femNotNullResult.getData(0, "REFERENCE_8");
		String ref2 = femNotNullResult.getData(0, "REFERENCE_2");

		boolean femNotNull = femNotNullResult.isEmpty();
		
		if (!femNotNull) {
			
			testCase.addQueryEvidenceCurrentStep(femNotNullResult);
			} 
				
		System.out.println(femNotNull);

		assertFalse(femNotNull, "No se obtiene informacion de la consulta");
		
//		Paso 6 ***********************
		
		addStep("Validar la actualización del campo ap_invoice_id en la tabla wm_ap_headers en RETEK.");
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String formatHeaders = String.format(tdcQueryHeaders, run_id, ref8);
		System.out.println(formatHeaders);
		
		SQLResult headersResult= executeQuery(dbRms, formatHeaders);
		

		boolean headers = headersResult.isEmpty();
		
			if (!headers) {
		
			testCase.addQueryEvidenceCurrentStep(headersResult);
			
						} 
		
		System.out.println(headers);

		assertFalse(headers, "No se obtiene informacion de la consulta");
		
//		Paso 7	************************
		addStep("Verificar que la información se haya insertado en la tabla ap_invoices_interface en ORAFIN.");
		System.out.println(GlobalVariables.DB_HOST_Ebs);
		String apFormat = String.format(tdcQueryapInterface, ref2);
		System.out.println(apFormat);
		
		SQLResult apResult= executeQuery(dbEbs, apFormat);
		

		boolean apInterface = apResult.isEmpty();
		
			if (!apInterface) {
		
			testCase.addQueryEvidenceCurrentStep(apResult);
			
						} 
		
		System.out.println(apInterface);

		assertFalse(apInterface, "No se obtiene informacion de la consulta");
		
		
//		Paso 8	************************
		
		addStep("Verificar que la información se haya insertado en la tabla ap_invoice_lines_interface en ORAFIN.");
		System.out.println(GlobalVariables.DB_HOST_Ebs);
		String apLineFormat = String.format(tdcQueryapLines, ref2);
		System.out.println(apLineFormat);
		
		SQLResult apLineResult= executeQuery(dbEbs, apLineFormat);
		

		boolean apLine = apLineResult.isEmpty();
		
			if (!apLine) {
		
			testCase.addQueryEvidenceCurrentStep(apLineResult);
			
						} 
		
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
		// TODO Auto-generated method stub
		return "Verificar el procesamiento de las Tranferencias";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_RO3_002_VerificarProcesamientoDeTransferencias";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
