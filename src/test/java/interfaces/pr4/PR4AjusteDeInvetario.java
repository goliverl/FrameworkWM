package interfaces.pr4;

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

public class PR4AjusteDeInvetario  extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR4_001_AjusteDeInventario(HashMap<String, String> data) throws Exception {

	
/* Utilerías *********************************************************************/
	

				
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
	

	/*	
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCRMSMGR,GlobalVariables.DB_USER_FCRMSMGR, GlobalVariables.DB_PASSWORD_FCRMSMGR);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_fcwmesit,GlobalVariables.DB_USER_fcwmesit, GlobalVariables.DB_PASSWORD_fcwmesit);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		
*/
/**
 * 
 * 
* Variables ******************************************************************************************
* pr4
* 
*/		
		String tdcSerFTP ="SELECT ftp_base_dir, ftp_serverhost, ftp_serverport, ftp_username, ftp_password" + 
				" FROM wmuser.wm_ftp_connections" + 
				" WHERE ftp_conn_id = 'RTKRMS'";
		
		String tdcInfPlazaTienda ="select * from (SELECT DISTINCT SUBSTR(pv_doc_name,4,5) pv_cr_plaza, SUBSTR(pv_doc_name,9,5) pv_cr_tienda, b.id,b.pe_id " + 
				" FROM POSUSER.pos_inbound_docs b, POSUSER.pos_adj c, POSUSER.pos_adj_detl d" + 
				" WHERE b.status = 'I'" + 
				" AND b.doc_type = 'ADJ'" + 
				" AND SUBSTR(pv_doc_name,4,5) = '" +data.get("plaza")+"'"+ 
				" AND SUBSTR(pv_doc_name,9,5) ='"  +data.get("tienda")+"'"+ 
				" AND c.pid_id = b.id " + 
				" AND d.pid_id = b.id " + 
				" AND d.adj_qty <> 0 " + 
				" AND b.partition_date > SYSDATE - 4 ) where rownum <=5";//FCWM6QA.


		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				 + " WHERE interface = 'PR4'" 	
				 +" and  start_dt >= trunc(SYSDATE)"
			     +" ORDER BY START_DT DESC) where rownum <=1";
				

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = '%s'" ; //FCWMLQA 


		
		String tdcQueryDocADJ =" SELECT DISTINCT SUBSTR(pv_doc_name,4,5) plaza, SUBSTR(pv_doc_name,9,5) tienda, status, id, pe_id \r\n" + 
				" FROM POSUSER.pos_inbound_docs" + 
				" WHERE status = 'E'" + 
				" AND doc_type = 'ADJ'" + 
				" AND SUBSTR(pv_doc_name,4,5) = '" + data.get("plaza")  +"'"+ 
				" AND SUBSTR(pv_doc_name,9,5) = '" + data.get("tienda") +"'"+
				" AND partition_date > SYSDATE - 4";
		
		String tdcQueryDocRetek = "Select * from (SELECT doc_type,cr_plaza,cr_tienda,run_id,status "+
				" FROM wmuser.rtk_inbound_docs where doc_type = 'ADJ'" + 
				" order by created_date desc)where rownum <=3 "; 
		

 
 /**
  * 
  * **********************************Pasos del caso de Prueba *****************************************
  * 
  * 		
  */


// 		Paso 1	************************		
		addStep("Validar la configuración del servidor FTP.");	
		
		System.out.println(tdcSerFTP); // get shipment
		SQLResult servQueryExe = dbPos.executeQuery(tdcSerFTP);
		boolean validacionServ = servQueryExe.isEmpty();
		if (!validacionServ) {
			testCase.addQueryEvidenceCurrentStep(servQueryExe);
		}
		System.out.println(validacionServ);
		 
		assertFalse(validacionServ, "No se tiene conexion al servidor FTP");
 
//		Paso 2	************************		
		addStep("Validar que exista información de la Plaza y Tienda pendiente de procesar con antigüedad mayor a 4 días.");	 
	
		System.out.println(tdcInfPlazaTienda);
			
		SQLResult tiendaQueryExe = dbPos.executeQuery(tdcInfPlazaTienda);
		boolean validacionTienda = tiendaQueryExe.isEmpty();
		if (!validacionTienda) {
			testCase.addQueryEvidenceCurrentStep(tiendaQueryExe);
		}
		System.out.println(validacionTienda);
		
		testCase.addBoldTextEvidenceCurrentStep("Error. Los datos no se encuentran configurados correctamente");
		testCase.addQueryEvidenceCurrentStep(tiendaQueryExe);
		
		assertFalse(validacionTienda, "Los datos no se encuentran configurados correctamente");


//		Paso 3	************************		
	
		addStep("Ejecutar  el servicio de la interfaz PR4");
		String status = "S";
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id ;
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		
		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));

	
//		Paso 4	************************
		addStep("Comprobar que se registra la ejecucion en WMLOG");
		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		
		boolean valuesStatus = status1.equals(searchedStatus);//Valida si se encuentra en estatus R
		while (valuesStatus) {
			
			query = executeQuery(dbLog, tdcQueryIntegrationServer);	
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");
		 
		 u.hardWait(2);
		 
		}
		
		boolean successRun = status1.equals(status);//Valida si se encuentra en estatus S
	    if(!successRun){
	   
	 
	    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
	  
	    }
	    testCase.addQueryEvidenceCurrentStep(query);

//		Paso 5	************************
		addStep("Se valida la generacion de thread");
		
		String threadFormat = String.format(tdcQueryStatusThread, run_id);
		SQLResult threadResult = executeQuery(dbLog, threadFormat);

		System.out.println(threadFormat);

			
			testCase.addBoldTextEvidenceCurrentStep("Error. No se genera thread");
		    testCase.addQueryEvidenceCurrentStep(threadResult);
		    
		
		


 
//		Paso 6	************************
		addStep("Validar la actualización de los documentos ADJ en la tabla pos_inbound_docs de POSUSER");
 	
		System.out.println(tdcQueryDocADJ);
		SQLResult adjQuery = dbPos.executeQuery(tdcQueryDocADJ);
		
		boolean adj = adjQuery.isEmpty();
		
			if (!adj) {
				
				testCase.addQueryEvidenceCurrentStep(adjQuery);
				
			}
		
		
		System.out.println(adj);

		testCase.addBoldTextEvidenceCurrentStep("Error. No se encuentra el  documentos ADJ");
		testCase.addQueryEvidenceCurrentStep(adjQuery);

		assertFalse(adj,"No se encuentra el  documentos ADJ ");		
 
//		Paso 7	************************

		addStep("Validar la inserción del registro del documento enviado en la tabla de RETEK");
 
 
 		String thr = String.format(tdcQueryDocRetek, run_id);
 		System.out.println(thr);
 		SQLResult retekQuery = dbRms.executeQuery(thr);

 		boolean retek = retekQuery.isEmpty();
 		
 			if(!retek) {
 				
 				testCase.addQueryEvidenceCurrentStep(retekQuery);
 				
 			}
 
 		System.out.println(retek);
 		

 			
 		assertFalse(retek, "No se encuentran registros en la tabla RETEK"); 			
 			
	}
	
	

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Validar el envío de Ajustes a RETEK para la plaza ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR4_001_AjusteDeInventario";
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
