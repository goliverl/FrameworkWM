package interfaces.PR4_CO;

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

public class PR4AjusteDeInvetarioCol extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR4_002_AjusteDeInventarioCol(HashMap<String, String> data) throws Exception {
	
/* Utilerías *********************************************************************/
	


		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_COL,GlobalVariables.DB_USER_RMS_COL, GlobalVariables.DB_PASSWORD_RMS_COL);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
/**
* Variables ******************************************************************************************
* pr4
* 
*/		
		
		String tdcSerFTP ="SELECT ftp_base_dir, ftp_serverhost, ftp_serverport, ftp_username, ftp_password" + 
				" FROM wmuser.wm_ftp_connections" + 
				" WHERE ftp_conn_id = 'RTKRMS'";
		
		String tdcInfPlazaTienda ="select * from (SELECT DISTINCT SUBSTR(pv_doc_name,4,5) pv_cr_plaza, SUBSTR(pv_doc_name,9,5) pv_cr_tienda \r\n" + 
				" FROM POSUSER.pos_inbound_docs b, POSUSER.pos_adj c, POSUSER.pos_adj_detl d" + 
				" WHERE b.status = 'I'" + 
				" AND b.doc_type = 'ADJ'" + 
				" AND SUBSTR(pv_doc_name,4,5) = '" +data.get("plaza")+"'"+ 
				" AND SUBSTR(pv_doc_name,9,5) ='"  +data.get("tienda")+"'"+ 
				" AND c.pid_id = b.id " + 
				" AND d.pid_id = b.id " + 
				" AND d.adj_qty <> 0 " + 
				" AND b.partition_date > SYSDATE - 4 ) where rownum <=5";
		
		
		String tdcQueryStatusLog = "SELECT run_id,interface,start_dt,status,server "
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PR4_CO' "
				+ " and status= 'S' "
				+ " and start_dt >= trunc(sysdate) " // FCWMLQA 
				+ " ORDER BY start_dt DESC";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				 + " WHERE interface = 'PR4_CO'" 	
				 +" and  start_dt >= trunc(SYSDATE)"
			     +" ORDER BY START_DT DESC) where rownum <=1";
				

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = '%s'" ; //FCWMLQA 
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID= '%s' "; //FCWMLQA 

		
		String tdcQueryDocADJ =" SELECT DISTINCT SUBSTR(pv_doc_name,4,5) plaza, SUBSTR(pv_doc_name,9,5) tienda, status \r\n" + 
				" FROM POSUSER.pos_inbound_docs" + 
				" WHERE status = 'E'" + 
				" AND doc_type = 'ADJ'" + 
				" AND SUBSTR(pv_doc_name,4,5) = '" + data.get("plaza")  +"'"+ 
				" AND SUBSTR(pv_doc_name,9,5) = '" + data.get("tienda") +"'"+
				" AND partition_date > SYSDATE - 4";
		
		String tdcQueryDocRetek = "Select * from (SELECT doc_type,cr_plaza,cr_tienda,run_id,status "+
				" FROM wmuser.rtk_inbound_docs where doc_type = 'ADJ'" + 
				" order by created_date desc)where rownum <=3 "; 
//				" WHERE run_id = '%s'";			
//				" AND cr_plaza = '" + data.get("plaza")  +"'"+ 
//				" AND cr_tienda = '"+ data.get("tienda") +"'";
				
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server,"
				+ " (END_DT - START_DT)*24*60 FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE LIKE '%PR4%' ORDER BY START_DT DESC) "
				+ "where rownum <=1";// WMLOG
		String consulta6 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// WMLOG
		String consulta61 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG
		String consulta62 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG
		
		
		
 String status = "S";
 //utileria


 
 /**
  * 
  * **********************************Pasos del caso de Prueba *****************************************
  * 
  * 		
  */


// 		Paso 1	************************	
	 	addStep("Validar la configuración del servidor FTP.");	
	 	System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
	
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

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
	
		System.out.println(tdcInfPlazaTienda);
		
		SQLResult tiendaQueryExe = dbPos.executeQuery(tdcInfPlazaTienda);
		boolean validacionTienda = tiendaQueryExe.isEmpty();
		if (!validacionTienda) {
			testCase.addQueryEvidenceCurrentStep(tiendaQueryExe);
		}
		System.out.println(validacionTienda);
			 
			
		assertFalse(validacionTienda, "Los datos no se encuentran configurados correctamente");


//		Paso 3	************************		
	
		addStep("Ejecutar el servicio PR4.Pub:run.");
// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";
		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		
		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);
		SQLResult is = executeQuery(dbLog, tdcIntegrationServerFormat);
		String status1 = is.getData(0, "STATUS");
		String run_id = is.getData(0, "RUN_ID");
		
		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			is = executeQuery(dbLog, tdcIntegrationServerFormat);
			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);
			u.hardWait(2);
		
		}
		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {
		
			String error = String.format(consulta6, run_id);
			String error1 = String.format(consulta61, run_id);
			String error2 = String.format(consulta62, run_id);
		
			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();
		
			if (!emptyError) {
		
				testCase.addTextEvidenceCurrentStep(
						"Se encontro un error en la ejecucutar la interfaz en la tabla WM_LOG_ERROR");
		
				testCase.addQueryEvidenceCurrentStep(errorr);
		
			}
		
			SQLResult errorIS = dbLog.executeQuery(error1);
			boolean emptyError1 = errorIS.isEmpty();
			if (!emptyError1) {
				testCase.addQueryEvidenceCurrentStep(errorIS);
			}
		
			SQLResult errorIS2 = dbLog.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();
		
			if (!emptyError2) {
		
				testCase.addQueryEvidenceCurrentStep(errorIS2);
		
			}
		
		}
	 
		
			

//		Paso 6	************************

		addStep("Comprobar que se registra la ejecucion en WMLOG");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
		System.out.println(queryStatusLog);

		SQLResult statusQueryExe = dbLog.executeQuery(queryStatusLog);
		String fcwS = statusQueryExe.getData(0, "STATUS");
		boolean validateStatus = status.equals(fcwS);
		System.out.println(validateStatus);
		
		boolean validacionQueryStatus = tiendaQueryExe.isEmpty();
		if (!validacionQueryStatus) {
			testCase.addQueryEvidenceCurrentStep(statusQueryExe);
		}
		System.out.println(validacionQueryStatus);	
				
		assertTrue(validateStatus,"La ejecución de la interfaz no fue exitosa");




//		Paso 7	************************

		addStep("Se valida la generacion de thread");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		
		String threadFormat = String.format(tdcQueryStatusThread, run_id);
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
	
		System.out.println(tdcQueryDocADJ);
		SQLResult adjQuery = dbPos.executeQuery(tdcQueryDocADJ);
	
		boolean adj = adjQuery.isEmpty();
		
			if (!adj) {
				
				testCase.addQueryEvidenceCurrentStep(adjQuery);
				
			}
	
	
			System.out.println(adj);

		assertFalse(adj,"No se encuentra el  documentos ADJ ");		
 
//		Paso 8	************************

		addStep("Validar la inserción del registro del documento enviado en la tabla de RETEK");
 
 		System.out.println(GlobalVariables.DB_HOST_RmsP);
 	
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
		return " Validar el envío de Ajustes a RETEK para la plaza col. ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR4_002_AjusteDeInventarioCol";
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
