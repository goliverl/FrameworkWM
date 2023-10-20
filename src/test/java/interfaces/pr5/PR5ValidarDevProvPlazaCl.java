package interfaces.pr5;

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

public class PR5ValidarDevProvPlazaCl extends BaseExecution {

	@Test(dataProvider = "data-provider")

	public void ATC_FT_PR5_003_Validar_Dev_Prov_Plaza_Cl(HashMap<String, String> data) throws Exception {

	
/* Utilerías *********************************************************************/
		
	
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser,GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		
	
/**
 * Variables ******************************************************************************************
* pr5
* 
*/
		
		
		String tdcDocumentsRTV = "select * from (Select ID,pe_id,STATUS,DOC_TYPE,PV_DOC_NAME,PARTITION_DATE "
					+ " from POSUSER.POS_INBOUND_DOCS"
			 		+ " where DOC_TYPE='RTV'"
			 	//	+ " and  SUBSTR(PV_DOC_NAME,4,5) = '" + data.get("plaza")+"'"
			 		+ " and STATUS = 'I'"
			 		+ " ORDER BY PARTITION_DATE DESC ) where rownum <=3";				//FCWM6QA
		
		String tdcCabeceraRTV = "SELECT pid_id,created_date,PV_CR_FROM_LOC,SUPPLIER,PARTITION_DATE"
						+ " FROM POSUSER.POS_RTV "
						+ " WHERE PID_ID =%s";						//FCWM6QA
		
		String tdcDetalleRTV = "SELECT pid_id,item,PARTITION_DATE "
				+ " FROM POSUSER.POS_RTV_DETL "
				+ " WHERE PID_ID=%s";								//FCWM6QA

		String tdcQueryStatusLog = "SELECT run_id,interface,start_dt,status,server "
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PR5_CO'"
				+ " and status= 'S' "
				+ " and start_dt >= trunc(sysdate) " 				// FCWMLQA 
				+ " ORDER BY start_dt DESC";

		/*String tdcIntegrationServerFormat  = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				 + " WHERE interface = 'PR5_CO'" 	
				// +" and  start_dt > To_Date ('%s', 'DD-MM-YYYY hh24:mi' )"
			     +" order by start_dt desc)"
				+ " where rownum = 1";	*/
		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%PR5_CO%' "
				+ "ORDER BY START_DT DESC) where rownum <=1";

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = %s" ; 							//FCWMLQA 

		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; 									//FCWMLQA 
		
		String tdcOutbounStatusE="select * from (select ID, PE_ID, STATUS,DOC_TYPE, PV_DOC_NAME "
		 		+ " from POSUSER.POS_INBOUND_DOCS   "
		 		+ " where DOC_TYPE='RTV' "
		 	//	+ " and SUBSTR(PV_DOC_NAME,4,5) = '"+data.get("plaza")+"'"
		 	//	+ " and ID= %s"
		 		+ " and STATUS='E'"
		 		+ " ORDER BY PARTITION_DATE DESC ) where rownum <=4 ";
		String consultaerror = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE from  wmlog.WM_LOG_ERROR where RUN_ID='%s') where rownum <=1";
		String consultaerror1 = " select * from (select description,MESSAGE from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		String consultaerror2 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		String status = "S";
		//utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String con ="http://"+user+":"+ps+"@"+server;
		String searchedStatus = "R";
		String id ;
		
/**
* 
* **********************************Pasos del caso de Prueba *****************************************
* 
* 		
*/		
	
		
/*paso 1  Buscar en la BD FCWMQA archivos RTV en estatus I (listos para ser procesados por la interfase)**********************/
		
addStep("1.-  Buscar en la BD FCWMQA archivos RTV en estatus I.  select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
		+ "FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE LIKE '%PR5_CO%' ORDER BY START_DT DESC) where rownum <=1 ");
					
				
			System.out.println(GlobalVariables.DB_HOST_Puser);
			System.out.println(tdcDocumentsRTV);
			
			SQLResult consultaRTVQuery = dbPos.executeQuery(tdcDocumentsRTV);
			String iD = "";
			System.out.println(iD);
			boolean validateRTV=consultaRTVQuery.isEmpty();
			if (validateRTV == false) {
				iD = consultaRTVQuery.getData(0, "ID");
				testCase.addQueryEvidenceCurrentStep(consultaRTVQuery);
			
			} 
			System.out.println(validateRTV);
			assertFalse(validateRTV, "Los datos no se encuentran configurados correctamente");
			
			
/*paso  2.- Validar que exista información en la cabecera del documento RTV SELECT * FROM POS_RTV WHERE PID_ID = [POS_INBOUND_DOCS.ID];*****************************************************/
addStep("2.- Validar que exista información en la cabecera del documento RTV. SELECT * FROM POS_RTV WHERE PID_ID = [POS_INBOUND_DOCS.ID]; ");

		System.out.println(GlobalVariables.DB_HOST_Puser);
			
				
		
		String consultaRTVcabecera = String.format(tdcCabeceraRTV, iD);
		SQLResult executeConsultaRTVCabecera = dbPos.executeQuery(consultaRTVcabecera);
		String pid_id = executeConsultaRTVCabecera.getData(0, "PID_ID");
		
		boolean validateCabecera=executeConsultaRTVCabecera.isEmpty();
		if (validateCabecera == false) {
		
			testCase.addQueryEvidenceCurrentStep(executeConsultaRTVCabecera);
		
		} 
		System.out.println(validateCabecera);
		assertFalse(validateCabecera, "No se ecnuentran datos en la cabecera del documento rtv");



/*paso 3. Validar su detalle SELECT * FROM POS_RTV_DETL WHERE PID_ID = [POS_INBOUND_DOCS.ID];*/

addStep("3.- Validar su detalle de los registros RTV. SELECT * FROM POS_RTV_DETL WHERE PID_ID = [POS_INBOUND_DOCS.ID]");

		System.out.println(GlobalVariables.DB_HOST_Puser);

		
		String consultaDetalle = String.format(tdcDetalleRTV, pid_id);
		SQLResult executeConsultaDetalle = dbPos.executeQuery(consultaDetalle);
		
		boolean validateConsultaDetalle = executeConsultaDetalle.isEmpty();
		if (validateConsultaDetalle == false) {
		
			testCase.addQueryEvidenceCurrentStep(executeConsultaDetalle);
		
		} 
		System.out.println(validateConsultaDetalle);
		assertFalse(validateConsultaDetalle, "Consulta no exitosa");

/*paso 4.  Ejecutar la interface PR5 */	

addStep(" Ejecutar la interface PR5 ");
	
		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"),null);
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
		
			String error = String.format(consultaerror, run_id);
			String error1 = String.format(consultaerror1, run_id);
			String error2 = String.format(consultaerror2, run_id);
		
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
//				Paso 6	************************

addStep("Comprobar que se registra la ejecucion en WMLOG. select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
		+ "FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE LIKE '%PR5%' ORDER BY START_DT DESC) where rownum <=1");




			
			System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
			String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
			System.out.println(queryStatusLog);
			SQLResult executeQueryStatusLog=dbLog.executeQuery(queryStatusLog);
			String fcwS=executeQueryStatusLog.getData(0, "STATUS");
			boolean validateStatus = status.equals(fcwS);
			System.out.println(validateStatus);
			
			
			
			
			
			
			assertTrue(validateStatus,"La ejecución de la interfaz no fue exitosa");



//	 		Paso 7	************************

addStep("Validar que el registro de ejecución de la plaza y tienda terminó en estatus 'S' en la tabla  WM_LOG_THREAD."
		+ " select * from (SELECT THREAD_ID, PARENT_ID, NAME,START_DT, END_DT, STATUS "
		+ " FROM wmlog.WM_LOG_THREAD where PARENT_ID= '%s' ) where rownum <=1");
	
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

			String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
			System.out.println(queryStatusThread);
			SQLResult executeQueryStatusThread=dbLog.executeQuery(queryStatusThread);
			String regPlazaTienda=executeQueryStatusThread.getData(0, "STATUS");
			boolean statusThread = status.equals(regPlazaTienda);
			System.out.println(statusThread);
					if(!statusThread){
			
							String error = String.format(tdcQueryErrorId, run_id);
							SQLResult queryEmptyError=dbLog.executeQuery(error);
			
							boolean emptyError = queryEmptyError.isEmpty();
							if(!emptyError){  
			
								testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR."
										+ " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
										+ "from  wmlog.WM_LOG_ERROR where RUN_ID='%s') where rownum <=1");
			
								testCase.addQueryEvidenceCurrentStep(queryEmptyError);
			
											}
										}
			assertTrue(statusThread,"El registro de ejecución de la plaza y tienda no fue exitoso");


/*8.- Validar que el archivo RTV haya cambiado a estatus E en la bd FCWMQA*/

addStep(" Validar que el archivo RTV haya cambiado a estatus E en la bd FCWMQA ."
		+ "select ID, PE_ID, STATUS,DOC_TYPE, PV_DOC_NAME"
		+ " from POSUSER.POS_INBOUND_DOCS where DOC_TYPE='RTV' and  SUBSTR(PV_DOC_NAME,4,5) = 'plaza' "
		+ "and ID='%s' and STATUS='E' ORDER BY received_date DESC ");
	
						System.out.println(GlobalVariables.DB_HOST_Puser);
				//
				System.out.println("Respuesta paso ultimo ");
				//String consulta3 = String.format(consulta33, w);
				
				SQLResult verifStatusQuery = dbPos.executeQuery(tdcOutbounStatusE);
				boolean verificarStatus = verifStatusQuery.isEmpty();
				
				if (verificarStatus == false) {
				
					testCase.addQueryEvidenceCurrentStep(verifStatusQuery);
				
				} 
				System.out.println(verificarStatus);
				assertFalse("No se generaron datos en la tabla", verificarStatus);




	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub

		return "Construido. Validar la devolución a proveedores directos para la plaza (Cl).";

	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub

		return "ATC_FT_PR5_003_Validar_Dev_Prov_Plaza_Cl";

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
