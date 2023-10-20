package interfaces.pr11;

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
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class PR11SincronizacionDeInventariosFisicos extends BaseExecution {

@Test(dataProvider = "data-provider")
public void ATC_FT_003_PR11_SincronizacionDeInventariosFisicos(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/	
		
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
/**
*  Variables ******************************************************************************************
* 
* 
*/	
		
		
		String tdcQueryDocProcesar ="SELECT DISTINCT PE.PV_CR_PLAZA, PE.PV_CR_TIENDA, PID.ID, PID.PE_ID, PID.STATUS, TO_CHAR(PD.FECHA_ADM,'YYYYMMDD') FECHA_MVT" + 
				" FROM POSUSER.POS_ENVELOPE PE" + 
				" JOIN POSUSER.POS_INBOUND_DOCS PID" + 
				" ON PID.PE_ID = PE.ID" + 
				" JOIN POSUSER.POS_INV_DETL PD" + 
				" ON  PID.ID = PD.PID_ID" + 
				" WHERE PID.DOC_TYPE = 'INV'" + 
				" AND PID.STATUS = 'I'" + 
				" AND PID.PARTITION_DATE>=TRUNC(SYSDATE-2)" + 
				" AND PE.PARTITION_DATE>=TRUNC(SYSDATE-2)" +
				"AND PE.PV_CR_PLAZA = '" + data.get("plaza") +"'";
		
		String tdcQueryInvFisico = "SELECT PID_ID, PV_INV_FIS, TO_CHAR(FECHA_ADM,'YYYYMMDD') FECHA_ADM "
				+ "FROM POSUSER.POS_INV" + 
				" WHERE PID_ID = '%s'" + 
				" AND PV_INV_FIS = 'F'";
		
		
		String tdcQueryStatusLog = "SELECT run_id,interface,start_dt,status,server "
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PR11' "
				+ " and status= 'S' "
				+ " and start_dt >= trunc(sysdate) " // FCWMLQA 
				+ " ORDER BY start_dt DESC";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				 + " WHERE interface = 'PR11'" 	
				 +" and  start_dt >= TRUNC(SYSDATE)"
			     +" order by start_dt desc)"
				+ " where rownum = 1";	

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = %s" ; //FCWMLQA 

		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; //FCWMLQA 
		
		
		String tdcQueryDocProcesados=" SELECT CR_PLAZA,CR_TIENDA,FECHA_MVT,PROCESSED_DATE" +
				" FROM XXFC.XXFC_ITEM_LOC_POS" + 
				" WHERE 1=1 " +
				" AND CR_PLAZA = '%s'" + 
				" AND CR_TIENDA = '%s'" + 
//				" AND FECHA_MVT = [FECHA_MVT]" + 
				" Order by processed_date desc";
		
		String tdcQueryValidateRe = "select 'Existen Registros' from dual where EXISTS (" + 
				" SELECT  CR_PLAZA,CR_TIENDA,FECHA_MVT,PROCESSED_DATE" + 
				" FROM XXFC.XXFC_ITEM_LOC_POS" + 
				" WHERE 1=1" + 
				" and CR_PLAZA = '%s'" + 
				" AND CR_TIENDA = '%s')";
		
		String tdcQueryUpdateStatus = "SELECT DOC_TYPE,STATUS,ID " +
				" FROM POSUSER.POS_INBOUND_DOCS" + 
				" WHERE DOC_TYPE = 'INV'" + 
				" AND STATUS = 'E'" + 
				" AND ID = '%s'";//[POS_INBOUND_DOCS.ID]
		
		String tdcQueryMovInvFisico = "SELECT CR_PLAZA,CR_TIENDA "+
				" FROM XXFC.XXFC_REPLEN_CONTROL_LOCS_F" + 
				" WHERE CR_PLAZA ='%s' " + //[CR_PLAZA]
				" AND CR_TIENDA = '%s'"; //[CR_TIENDA]

		String status = "S";
		//utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String con ="http://"+user+":"+ps+"@"+server;
		String searchedStatus = "R";
		String run_id ;
		String id ;
		String cr_plaza,cr_tienda;
		
		
		
		
/**
* 
* **********************************Pasos del caso de Prueba *****************************************
* 
*  
* 		
*/


//						Paso 1	************************
		
addStep("Validar que exista documentos de tipo DOC_TYPE='INV' pendiente de procesar en la tabla POS_INBOUND_DOCS de POSUSER con STATUS = 'I'");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(tdcQueryDocProcesar);
		
		SQLResult paso1 = executeQuery(dbPos, tdcQueryDocProcesar);

		id = paso1.getData(0, "ID");
		
		boolean docI = paso1.isEmpty();

			if (!docI) {

				testCase.addQueryEvidenceCurrentStep(paso1);
		
					} 

			System.out.println(docI);


assertFalse(docI, "No se obtiene informacion de la consulta");


//						Paso 2	***********************
addStep("Ejecutar el servicio PR11.Pub:");				

		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		
		
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		
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
		   
		   String error = String.format(tdcQueryErrorId, run_id);
		   SQLResult paso2 = executeQuery(dbLog, error);
		   
		   boolean emptyError = paso2.isEmpty();
		   
		   if(!emptyError){  
		   
		    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
		   
		    testCase.addQueryEvidenceCurrentStep(paso2);
		   
		   }
		}




//				Paso 3	************************

addStep("Comprobar que se registra la ejecucion en WMLOG");



			System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
			String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
			SQLResult paso3 = executeQuery(dbLog, queryStatusLog);
			System.out.println(queryStatusLog);
			
			boolean ejecucion = paso3.isEmpty();
			if(!ejecucion) {
				
				testCase.addQueryEvidenceCurrentStep(paso3);
			}
				

			String fcwS = paso3.getData(0, "STATUS");
			boolean validateStatus = status.equals(fcwS);
			System.out.println(validateStatus);



assertTrue(validateStatus,"La ejecución de la interfaz no fue exitosa");




//				Paso 4	************************

addStep("Se valida la generacion de thread");

			String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
			System.out.println(queryStatusThread);
			SQLResult queryStatusThreadResult = dbLog.executeQuery(queryStatusThread);
			
			boolean thread = queryStatusThreadResult.isEmpty();
			if(!thread) {
				
				testCase.addQueryEvidenceCurrentStep(queryStatusThreadResult);
			}
				
			
			
			String regPlazaTienda = queryStatusThreadResult.getData(0, "STATUS");
			boolean statusThread = status.equals(regPlazaTienda);
			System.out.println(statusThread);
				if(!statusThread){

				String error = String.format(tdcQueryErrorId, run_id);
				SQLResult errorResult = dbLog.executeQuery(error);

				boolean emptyError = errorResult.isEmpty();
				if(!emptyError){  

					testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

					testCase.addQueryEvidenceCurrentStep(errorResult);
							}
				}
assertTrue(statusThread,"El registro de ejecución de la plaza y tienda no fue exitoso");


//				Paso 5	************************

addStep("Validar que se inserte la información de los documentos procesados en la tabla XXFC.XXFC_ITEM_LOC_POS de RETEK");


			SQLResult result = executeQuery(dbLog, queryStatusThread);
			cr_plaza = result.getData(0, "ATT1");
			cr_tienda = result.getData(0, "ATT2");
			
			String xxfFormat = String.format(tdcQueryValidateRe,cr_plaza,cr_tienda);
			System.out.println(xxfFormat);
			SQLResult paso5 = executeQuery(dbRms, xxfFormat);
			boolean xxfFormatBo = paso5.isEmpty();
			

					if (!xxfFormatBo) {
		
		
						testCase.addQueryEvidenceCurrentStep(paso5);
		
							}

					System.out.println(xxfFormatBo);

assertFalse(xxfFormatBo, " No se muestran registros a procesar ");		
		



//				Paso 6	************************

addStep("Validar que se actualice el estatus de los documentos procesados en la tabla POS_INBOUND_DOCS de POSUSER a STATUS = 'E'");
			
	   		String proc = String.format(tdcQueryUpdateStatus, id);
	   		SQLResult paso7 = executeQuery(dbPos, proc);
	   		
	   		boolean procValue = paso7.isEmpty();
	   		System.out.println(procValue);

			if (!procValue) {


				testCase.addQueryEvidenceCurrentStep(paso7);

					}

			System.out.println(procValue);

assertFalse(procValue, " No se muestran registros a procesar ");	





//				Paso 7	************************

addStep("Validar que se actualice la fecha de movimiento para la plaza-tienda con inventario físico procesada en la taba XXFC_REPLEN_CONTROL_LOCS_F de RETEK.");


				//Sacar la variable 
		
			//format de la consulta 
					String queryFiscInventory = String.format(tdcQueryInvFisico, id);
					SQLResult result8 = executeQuery(dbPos, queryFiscInventory);
					System.out.println(queryFiscInventory);
					
					String invFics = String.format(tdcQueryMovInvFisico,cr_plaza,cr_tienda);
					System.out.println(invFics);
					SQLResult paso8 = executeQuery(dbRms, invFics);

					boolean ficsIn = paso8.isEmpty();   

					if (!ficsIn) {

						
						testCase.addQueryEvidenceCurrentStep(result8);
						System.out.println(queryFiscInventory);
						
						testCase.addQueryEvidenceCurrentStep(paso8);
						
						System.out.println(invFics);
					
						
						
							}

					System.out.println(ficsIn);

					
assertFalse(ficsIn, " No se muestran registros ");		
				
		


		
	}	
		
		
		
		
		
		
		
		
		
		
		
		
		
		

		@Override
		public void beforeTest() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String setTestDescription() {
			// TODO Auto-generated method stub
			return " Validar la sincron. de Inventarios para la plaza 10RYN - Inv. fís.";
		}

		@Override
		public String setTestDesigner() {
			// TODO Auto-generated method stub
			return "QAautomation";
		}

		@Override
		public String setTestFullName() {
			// TODO Auto-generated method stub
			return "ATC_FT_003_PR11_SincronizacionDeInventariosFisicos";
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
