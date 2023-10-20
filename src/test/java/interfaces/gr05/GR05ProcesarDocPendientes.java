package interfaces.gr05;

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

public class GR05ProcesarDocPendientes extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_GR05_ProcesarDocPendientes(HashMap<String, String> data) throws Exception {
		

		
/* Utilerías *********************************************************************/		
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		
		
/**
* Variables ******************************************************************************************
* 
* 
*/ 
		String tdcQueryParams = "SELECT NAME, VALUE, MODULE "
				+ " FROM wmuser.WM_ORACLE_PARAMS "
				+ " WHERE IF_ID = 'GR05'";
		
		String tdcQueryFTP = "SELECT FTP_CONN_ID, FTP_BASE_DIR, FTP_SERVERHOST, DESCRIPTION "
				+ " FROM wmuser.wm_ftp_connections "
				+ " WHERE ftp_conn_id = 'RTK_RTV_GAS'";
		
		String tdcQueryGas = "SELECT  DISTINCT gi.id, SUBSTR(gi.pv_doc_name,4,5) cr_plaza, SUBSTR(gi.pv_doc_name,9,5) AS cr_tienda, gi.status, gi.target_id"
				+ " FROM posuser.gas_inbound_docs gi INNER JOIN posuser.gas_rtv rtv "
				+ " ON (gi.id = rtv.gid_id)"
				+ " INNER JOIN posuser.gas_rtv_detl dtl"
				+ " ON (rtv.gid_id = dtl.gid_id)"
				+ " WHERE doc_type = 'RTV'"
				+ " AND status = 'I'"
				+ " AND SUBSTR(gi.pv_doc_name, 4, 5) = '" + data.get("plaza") +"'"
				+ " AND SUBSTR(gi.pv_doc_name, 9, 5) = '" + data.get("tienda") +"'";
		
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
		
		String tdcQueryInbound = "SELECT ID, PV_DOC_ID, STATUS, DOC_TYPE, TARGET_ID "
				+ " FROM posuser.gas_inbound_docs gi"
				+ " WHERE gi.status = 'E'"
				+ " AND SUBSTR(gi.pv_doc_name ,4,5) = '" + data.get("plaza") +"'"
				+ " AND substr(gi.pv_doc_name,9,5) = '" + data.get("tienda") +"'"
				+ " AND gi.id = '%s'";
		
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
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//		Paso 1	************************ 
		addStep("Validar el retorno de los parámetros comunes para la interfaz GR05.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryParams);
		
		SQLResult paramResult = executeQuery(dbPos, tdcQueryParams);
		
		boolean params = paramResult.isEmpty();
		
			if (!params) {
		
			testCase.addQueryEvidenceCurrentStep(paramResult);
			
						} 
		
		System.out.println(params);

		assertFalse(params, "No se obtiene informacion de la consulta");
	
//		Paso 2	************************ 
		addStep("Validar la configuración del servidor FTP.");		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryFTP);
		
		SQLResult FTPresult = executeQuery(dbPos, tdcQueryFTP);

		boolean ftp = FTPresult.isEmpty();
		
			if (!ftp) {
		
			testCase.addQueryEvidenceCurrentStep(FTPresult);
			
						} 
		
		System.out.println(ftp);

		assertFalse(ftp, "No se obtiene informacion de la consulta");
		
//		Paso 3	************************ 
		addStep("Validar que existan registros RTV pendientes de procesar.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryGas);
		
		SQLResult gasResult = executeQuery(dbPos, tdcQueryGas);
		String id = gasResult.getData(0, "id");

		boolean gas = gasResult.isEmpty();
		
			if (!gas) {
		
			testCase.addQueryEvidenceCurrentStep(gasResult);
			
						} 
		
		System.out.println(gas);

		assertFalse(gas, "No se obtiene informacion de la consulta");
		
//		Paso 4	************************ 
		addStep("Ejecutar el servicio GR05.Pub:run.");
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		
//		Paso 5	************************
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
		    
//		Paso 6	************************
		addStep("Se valida la generacion de thread.");
			
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String statusThreadFormat = String.format(tdcQueryStatusThread, run_id);
		SQLResult threadResult = executeQuery(dbLog, statusThreadFormat);

		System.out.println(statusThreadFormat);

		String statusThread = threadResult.getData(0, "Status");
		String thread_id = threadResult.getData(0, "thread_id");

		boolean ST = statusThread.equals(status);
		ST = !ST;

		if (!ST) {

			testCase.addQueryEvidenceCurrentStep(threadResult);
				    
			} 

		System.out.println(ST);

			assertFalse(ST, "No se obtiene informacion de la consulta");

//		Paso 7	************************ 
		addStep("Validar que los registros fueron procesados.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		
		String inboundFormat = String.format(tdcQueryInbound, id);
		System.out.println(inboundFormat);
		SQLResult inboundResult = executeQuery(dbPos, inboundFormat);

		boolean inbound = inboundResult.isEmpty();
			
			if (!inbound) {
			
			testCase.addQueryEvidenceCurrentStep(inboundResult);
				
							} 
			
		System.out.println(inbound);

		assertFalse(inbound, "No se obtiene informacion de la consulta");
		    
		
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
		return " Procesar los documentos pendientes de las plazas y tdas.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_GR05_ProcesarDocPendientes";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	



}
