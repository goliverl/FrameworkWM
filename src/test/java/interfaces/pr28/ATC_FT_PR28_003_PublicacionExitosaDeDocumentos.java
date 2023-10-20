package interfaces.pr28;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;


public class ATC_FT_PR28_003_PublicacionExitosaDeDocumentos extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR28_003_PublicacionExitosaDeDocumentos_test(HashMap<String, String> data) throws Exception {
		
		/*
		 * Utilerias
		 *********************************************************************/
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * ALM
		 * Publicacion exitosa de los documentos - APO
		 */
		/**
		 * Variables
		 * ***********************************************************************************************
		 * 
		 * 
		 */
		String tdcQueryTiendas = "SELECT PV_CR_PLAZA, PV_CR_TIENDA, B.STATUS "
				+ " FROM POSUSER.POS_ENVELOPE A, POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C "
				+ " WHERE B.PE_ID = A.ID "
				+ " AND B.DOC_TYPE = 'REC' "
				+ " AND B.STATUS IN ('I') "
				+ " AND C.PID_ID = B.ID "
				+ " AND C.PV_CVE_MVT = '10' "
				+ " AND C.EXT_REF_NO <> '0' "
				+ " AND C.ORDER_NO = 0 "
				+ " AND B.PARTITION_DATE >= TRUNC(SYSDATE-7) "
				+ " GROUP BY PV_CR_PLAZA, PV_CR_TIENDA, B.STATUS";
		
		String tdcQueryDatos = "SELECT B.ID, TO_CHAR (C.CREATED_DATE, 'YYYYMMDDHH24MISS') CREATED_DATE, EXT_REF_NO, SUPPLIER "
				+ " FROM POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_REC C "
				+ " WHERE SUBSTR(B.PV_DOC_NAME,4,10) = '%s' "
				+ " AND B.DOC_TYPE = 'REC' "
				+ " AND STATUS = 'I'  "
				+ " AND C.PID_ID = B.ID  "
				+ " AND C.PV_CVE_MVT = 10  "
				+ " AND C.EXT_REF_NO <> '0'  "
				+ " AND C.PARTITION_DATE >= TRUNC(SYSDATE-7) "
				+ " AND C.ORDER_NO = 0";
		
		String tdcQueryDetl = "SELECT  ITEM, RECEIVED_QTY, NVL(CARTON,0) CARTON, BOL_NO, PV_RETAIL_PRICE, NVL(REMISION,0) REMISION "
				+ " FROM POSUSER.POS_REC_DETL"
				+ " WHERE PID_ID = %s";
		
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
		
		
		
		testCase.setProject_Name("AutomationQA");
		
		
// Paso 1 ************************
		addStep("Tener tiendas disponibles para procesar.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		
		SQLResult tiendaResult = dbPos.executeQuery(tdcQueryTiendas);
		String tienda = tiendaResult.getData(0, "PV_CR_TIENDA");
		String plaza = tiendaResult.getData(0, "PV_CR_PLAZA");
		String plazatienda = plaza + tienda;
		String pid_id = tiendaResult.getData(0, "PID_ID");
		System.out.println(tdcQueryTiendas);
		
		boolean tiendasDisponibles = tiendaResult.isEmpty();
		if (!tiendasDisponibles) {
			
			testCase.addQueryEvidenceCurrentStep(tiendaResult);
		}
		
		assertFalse("No se obtiene informacion de la consulta", tiendasDisponibles);

// Paso 2 ***********************
		addStep("Tener datos disponibles para procesar en las tablas de POSUSER.");
		System.out.println(GlobalVariables.DB_HOST_Puser);

		String datosFormat = String.format(tdcQueryDatos, plazatienda);
		SQLResult datosResult = dbPos.executeQuery(datosFormat);
		
		System.out.println(datosFormat);
		
		boolean datos = datosResult.isEmpty();
		if (!datos) {
					
			testCase.addQueryEvidenceCurrentStep(datosResult);
					
		}
				
		assertFalse("No se obtiene informacion de la consulta", datos);	
		
//Paso 3 ***********************
		addStep("Comprobar que existan detalles para los registros que seran procesados.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		
		String detlFormat = String.format(tdcQueryDetl, pid_id);
		SQLResult detlResult = dbPos.executeQuery(detlFormat);
		
		System.out.println(detlFormat);

		boolean detl = detlResult.isEmpty();
		if (!detl) {
							
			testCase.addQueryEvidenceCurrentStep(detlResult);
		}
		
		assertFalse("No se obtiene informacion de la consulta", detl);
		
// Paso 4 ************************
		addStep("Invocar la interfaz PR28.Pub:run.");
        
		String status = "S";
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String contra ="http://"+user+":"+ps+"@"+server;
		String searchedStatus = "R";
		String run_id ;
		
		u.get(contra);
		
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		
//paso 5 **********************
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
				   
				  testCase.addTextEvidenceCurrentStep("Se encontro un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");
				   
				  testCase.addQueryEvidenceCurrentStep(paso2);
				   
				 }
		}
				    testCase.addQueryEvidenceCurrentStep(query);
				    
// paso 6 ***************************
		addStep("Se valida la generacion de thread.");
					
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String statusThreadFormat = String.format(tdcQueryStatusThread, run_id);
		SQLResult threadResult = executeQuery(dbLog, statusThreadFormat);

		System.out.println(statusThreadFormat);

		String statusThread = threadResult.getData(0, "Status");

		boolean ST = statusThread.equals(status);
				ST = !ST;

		if (!ST) {

				testCase.addQueryEvidenceCurrentStep(threadResult);
						    
				} 

		System.out.println(ST);

		assertFalse(ST, "No se obtiene informacion de la consulta");  
				
		
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
		return "Verificar la publicacion de los documentos lanzados al procesar la informacion - APO";
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
