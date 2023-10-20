package interfaces.rr04;

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

public class RR04ValidaProcesoCopiaRDM extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_RR04_001_Valida_Proceso_Copia_RDM(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbRdm = new SQLUtil(GlobalVariables.DB_HOST_RDM, GlobalVariables.DB_USER_RDM,	GlobalVariables.DB_PASSWORD_RDM);
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);

/**
* Variables ******************************************************************************************
* 
* 
*/		
		//Paso 1
		
		String configCEDIS = "select * from (SELECT * FROM POSUSER.wm_rdm_connections\r\n" + 
				"WHERE   status = 'A')\r\n" + 
				"where rownum <=1 "; //obtener rtv_id
		//Paso 2
		
		String infoPendiente = "select * from (SELECT rtv_id, vendor_nbr, concepto, wm_sent_flag, creation_date \r\n" + 
				"FROM rdm100.xxfc_rtv_storage_detail_v\r\n" + 
				"where wm_sent_flag = 'N' "
				+ "order by creation_date desc)\r\n" + 
				"where rownum <=1 ";
		//Paso 3 y 4
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'RR04'"
				+ "and status = 'S'"				
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum <=1";
		
		String tdcQueryErrorId = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"
				+ " and rownum = 1";
		
		//Paso 5
		String consultaThreads = " SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS, ATT5 " 
				+ "FROM WMLOG.WM_LOG_THREAD   WHERE PARENT_ID='%s'  "
						+ "ORDER BY THREAD_ID DESC";// Consulta para los Threads
		
		//Paso 6 
		
		String insercionInfo = "SELECT WH, RTV_ID,CONCEPTO, VENDOR_NBR, CREATION_DATE, RTV_ORDER_NO, WM_SENT_FLAG "
				+ "FROM XXFC.XXFC_RTV_COSTO_CONCEPTO WHERE rtv_id = '%s' ";
		
		//Paso 7
		
		String updateRDM ="SELECT FACILITY_ID,RTV_ID, CONCEPTO, VENDOR_NBR, WM_SENT_FLAG, CREATION_DATE "
				+ "FROM rdm100.xxfc_rtv_costo_concepto "
				+ "WHERE wm_sent_flag = 'E' "
				+ " AND rtv_id = '%s'";
		
		
				
				
	
/**
 * **************************************      Pasos del caso de Prueba		 *******************************************/
					
//*************************************************Paso 1 **************************************************************			
		addStep("Validar que el CEDIS este configurado en la tabla wm_rdm_connections de WMINT.");	
		
		System.out.println(configCEDIS);
		
		SQLResult configCEDISResult = executeQuery(dbPos, configCEDIS);
				
		
	    boolean validaconfigCEDIS = configCEDISResult.isEmpty();
		
		if (!validaconfigCEDIS) {
			
			testCase.addQueryEvidenceCurrentStep(configCEDISResult);
		}
		
		System.out.println(validaconfigCEDIS);
		
		assertFalse(validaconfigCEDIS, "No se presentaran los adapter de conexión para el CEDIS.");
		
//***********************************************Paso 2 *************************************************************		
		
		addStep("Validar que exista información pendiente de enviar con el ID de la prueba. ");
	
	
		System.out.println(infoPendiente);
		
		SQLResult infoPendienteResult = dbRdm.executeQuery(infoPendiente);
		
		String RTV_ID = infoPendienteResult.getData(0, "RTV_ID");
		
		System.out.println("RTV_ID: "+ RTV_ID);
		
		boolean validaInfoPendiente = infoPendienteResult.isEmpty();
		
		if (!validaInfoPendiente) {
			
			testCase.addQueryEvidenceCurrentStep(infoPendienteResult);
					
		}
		
		System.out.println(validaInfoPendiente);
		
		assertFalse(validaInfoPendiente , "No se obtiene información de la consulta");
		
//**********************************************Paso 3 ************************************************************
		
		addStep("Ejecutar el servicio RR04.Pub:run. El servicio será ejecutado desde Ctrl-M a través del job runRR04.");		
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
		
		
		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		System.out.println(tdcQueryIntegrationServer);
		
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
		
//*******************************************************Paso 4 ********************************************************************
		    addStep("Validar el registro de ejecución de la interfaz en la base de datos del WMLOG.");
			
		  	
	        boolean validateStatus = status.equals(status1);
			System.out.println("VALIDACION DE STATUS = S - "+ validateStatus);
			assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa ");
			
			

			boolean av2 = query.isEmpty();
			if (av2 == false) {

				testCase.addQueryEvidenceCurrentStep(query);

			} else {
				
				testCase.addQueryEvidenceCurrentStep(query);
			}
			
			System.out.println("El registro en WM_LOG_RUN esta vacio "+ av2);
						
		
//*************************************************** Paso 5 ****************************************************************************
addStep("Comprobar que exista registro en tabla WM_LOG_THREAD de la BD WMLOG, donde PARENT_ID es igual a WM_LOG_RUN.RUN_ID, STATUS igual a 'S'.");
			
			String threads = String.format(consultaThreads, run_id);
			
			System.out.println("CONSULTA THREAD "+ threads);
			
			 SQLResult threadsResult=  dbLog.executeQuery(threads);
	        
			
			boolean av31 = threadsResult.isEmpty();
			if (av31 == false) {

				testCase.addQueryEvidenceCurrentStep(threadsResult);

			} else {
				testCase.addQueryEvidenceCurrentStep(threadsResult);
			}
			System.out.println("El registro en WM_LOG_THREAD esta vacio 1- "+ av31);
			
			
			assertFalse(av31, "No se generaron threads en la tabla");
			
			
//*************************************************** Paso 6 ****************************************************************************			
		addStep("Validar la inserción de la información en la tabla xxfc_rtv_costo_concepto de RMS.");
		
		
		String insercionInfoFormat = String.format(insercionInfo, RTV_ID);
		
		System.out.println(insercionInfoFormat);
		
		SQLResult insercionInfoResult = executeQuery(dbRms, insercionInfoFormat);
		
		
		boolean valdaInsercionInfo = insercionInfoResult.isEmpty();
		
		if (!valdaInsercionInfo) {
			
			testCase.addQueryEvidenceCurrentStep(insercionInfoResult);
			
		}
		
		System.out.println(valdaInsercionInfo);
		
		assertFalse(valdaInsercionInfo, "No se encontro el registro insertado en la base de datos de RMS.");
		
//********************************************* Paso 7 *********************************************************************************
		
   addStep("Validar la actualización del campo wm_sent_flag a 'E' en la tabla xxfc_rtv_costo_concepto de RDM.");
		
		
		String updateRDMFormat = String.format(updateRDM, RTV_ID);
		
		System.out.println(updateRDMFormat);
		
		SQLResult updateRDMFormatResult = executeQuery(dbRdm, updateRDMFormat);
		
		
		boolean valdaupdateRDM = updateRDMFormatResult.isEmpty();
		
		if (!valdaupdateRDM) {
			
			testCase.addQueryEvidenceCurrentStep(updateRDMFormatResult);
			
		}
		
		System.out.println(valdaupdateRDM);
		
		assertFalse(valdaupdateRDM, "No se encontro el registro  enviado a RMS con estatus 'E'.");
		
				
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
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
		return "Construido. Valida el proceso de copia del RTV_ID del Cedis en RDM hacia RMS";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_RR04_001_Valida_Proceso_Copia_RDM";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
