package interfaces.pr4_Cl;

import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class PR4_AjusteDeInvetario_CL  extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR4_001_Valida_Envio_de_Ajustes(HashMap<String, String> data) throws Exception {

	
/* Utilerías *********************************************************************/
	


		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERChile,GlobalVariables.DB_USER_RMSWMUSERChile, GlobalVariables.DB_PASSWORD_RMSWMUSERChile);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile,GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LogChile,GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
	
/**
* Variables ******************************************************************************************
* pr4
* 
*/		
		String tdcQueryPaso1 ="SELECT ftp_base_dir, ftp_serverhost, ftp_serverport, ftp_username, ftp_password" + 
				" FROM wmuser.wm_ftp_connections" + 
				" WHERE ftp_conn_id = 'RTKRMS'";
		
		String tdcQueryPaso2 =" Select * from (SELECT DISTINCT SUBSTR(pv_doc_name,4,5) pv_cr_plaza, SUBSTR(pv_doc_name,9,5) pv_cr_tienda, b.id,b.pe_id " + 
				" FROM POSUSER.pos_inbound_docs b, POSUSER.pos_adj c, POSUSER.pos_adj_detl d" + 
				" WHERE b.status = 'I'" + 
				" AND b.doc_type = 'ADJ'" + 
				" AND SUBSTR(pv_doc_name,4,5) = '" +data.get("plaza")+"'"+ 
				" AND SUBSTR(pv_doc_name,9,5) ='"  +data.get("tienda")+"'"+ 
				" AND c.pid_id = b.id " + 
				" AND d.pid_id = b.id " + 
				" AND d.adj_qty <> 0 " + 
				" AND b.partition_date > SYSDATE - 4 ) where rownum <=5";//FCWM6QA.

		String tdcQueryPaso4 = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PR4_CL'" 	
				+ " and  start_dt >= trunc(SYSDATE)"
			    + " ORDER BY START_DT DESC) where rownum <=1";
				
		String tdcQueryPaso5 = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = '%s'" ; //FCWMLQA 

		String tdcQueryPaso6 =" SELECT DISTINCT SUBSTR(pv_doc_name,4,5) plaza, SUBSTR(pv_doc_name,9,5) tienda, status, id, pe_id \r\n" + 
				" FROM POSUSER.pos_inbound_docs" + 
				" WHERE status = 'E'" + 
				" AND doc_type = 'ADJ'" + 
				" AND SUBSTR(pv_doc_name,4,5) = '" + data.get("plaza")  +"'"+ 
				" AND SUBSTR(pv_doc_name,9,5) = '" + data.get("tienda") +"'"+
				" AND partition_date > SYSDATE - 4";
		
		String tdcQueryPaso7 = "Select * from (SELECT doc_type,cr_plaza,cr_tienda,run_id,status "+
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
 
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		
		System.out.println(tdcQueryPaso1); 
		SQLResult Paso1 = dbPos.executeQuery(tdcQueryPaso1);
		boolean ValidacionPaso1 = Paso1.isEmpty();
		if (!ValidacionPaso1) {
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}
		System.out.println(ValidacionPaso1);
		 
		assertFalse(ValidacionPaso1, "No se tiene conexion al servidor FTP");
 
//		Paso 2	************************		
		addStep("Validar que exista información de la Plaza y Tienda pendiente de procesar con antigüedad mayor a 4 días.");	 

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
	
		System.out.println(tdcQueryPaso2);
			
		SQLResult Paso2 = dbPos.executeQuery(tdcQueryPaso2);
		boolean ValidacionPaso2 = Paso2.isEmpty();
		if (!ValidacionPaso2) {
			testCase.addQueryEvidenceCurrentStep(Paso2);
		}
		System.out.println(ValidacionPaso2);
			 
			
		assertFalse(ValidacionPaso2, "Los datos no se encuentran configurados correctamente");


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
		
		
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

	
//		Paso 4	************************
		addStep("Comprobar que se registra la ejecucion en WMLOG");
		
		System.out.println(tdcQueryPaso4);
		SQLResult query = executeQuery(dbLog, tdcQueryPaso4);	
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		
		boolean valuesStatus = status1.equals(searchedStatus);//Valida si se encuentra en estatus R
		while (valuesStatus) {
			
			query = executeQuery(dbLog, tdcQueryPaso4);	
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
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		
		String threadFormat = String.format(tdcQueryPaso5, run_id);
		SQLResult threadResult = executeQuery(dbLog, threadFormat);
		System.out.println(threadFormat);
		
		String estatusThread = threadResult.getData(0, "Status");
		
		boolean SR = estatusThread.equals(status);
		SR = !SR;
		
		if (!SR) {
		
		    testCase.addQueryEvidenceCurrentStep(threadResult);
		    
		} 
		
		System.out.println(SR);

		assertFalse(SR, "No se obtiene información de la consulta");

 
//		Paso 7	************************
		addStep("Validar la actualización de los documentos ADJ en la tabla pos_inbound_docs de POSUSER");
 
		System.out.println(GlobalVariables.DB_HOST_Puser);
		
		System.out.println(tdcQueryPaso6);
		SQLResult adjQuery = dbPos.executeQuery(tdcQueryPaso6);
		
		boolean adj = adjQuery.isEmpty();
		
			if (!adj) {
				
				testCase.addQueryEvidenceCurrentStep(adjQuery);
				
			}
		
		
		System.out.println(adj);

		assertFalse(adj,"No se encuentra el  documentos ADJ ");		
 
//		Paso 8	************************

		addStep("Validar la inserción del registro del documento enviado en la tabla de RETEK");
 
 
 		System.out.println(GlobalVariables.DB_HOST_RmsP);
 		
 
 		String thr = String.format(tdcQueryPaso7, run_id);
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
		return "Construido. Valida el envio de Ajustes a RETEK para la plaza y la Tienda.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR4_001_Valida_Envio_de_Ajustes";
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
