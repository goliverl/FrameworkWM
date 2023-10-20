package interfaces.rr02;



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

public class RR02ValidaEnvioDocASN extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_RR02_002_ValidaEnvioDocASN(HashMap<String, String> data) throws Exception {
		
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
		
		String adapterRDM = "SELECT * FROM POSUSER.wm_rdm_connections WHERE retek_cr = '"+ data.get("retek_cr")+"' AND status = 'A' "; //obtener rtv_id
		//Paso 2
		
		String infoPendienteCedis = "SELECT b.facility_id, b.bol_nbr, ts.facility_type, ts.dc_dest_id, m.trailer_id, m.dest_id "
				+ " FROM RDM100.ship_dest s, RDM100.trailer t, RDM100.manifest m, RDM100.transshipment_setup ts, WMUSER.wm_bol_to_upload b \r\n" + 
				"WHERE NVL(b.pub_status, 'U') = 'U' \r\n" + 
				"AND b.facility_id = ts.facility_id \r\n" + 
				"AND ts.facility_type ='PROD' \r\n" + 
				"AND ts.dc_dest_id = '%s' \r\n" + 
				"AND b.facility_id = m.facility_id \r\n" + 
				"AND b.bol_nbr = m.bol_nbr \r\n" + 
				"AND m.facility_id = t.facility_id \r\n" + 
				"AND m.trailer_id = t.trailer_id \r\n" + 
				"AND m.facility_id = s.facility_id \r\n" + 
				"AND m.dest_id = s.dest_id ";
		//Paso 3 y 4
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'RR02main'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum <=1 ";
		
		String tdcQueryErrorId = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"
				+ " and rownum = 1 ";
		
		//Paso 5
		String consultaThreads = " select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server \r\n" + 
				"FROM WMLOG.WM_LOG_RUN Tbl WHERE interface = 'RR02_%s' \r\n" + 
				"AND start_dt >= trunc(SYSDATE)\r\n" + 
				"AND status = 'S'\r\n" + 
				"ORDER BY START_DT DESC) where rownum <=1 ";// Consulta para los Threads
		
		//Paso 6 
		
		String infoProcesadaRDM = "SELECT * FROM wm_bol_to_upload where bol_nbr = '%s' AND wm_run_id = '%s' AND pub_status = 'S'' ";
		
		//Paso 7
		
		String infoProcesadaRms ="SELECT shipment, bol_no, ship_date, receive_date FROM RMS100.SHIPMENT WHERE bol_no like '%s %' \r\n" + 
				"and bol_no like '% %s'";
		
		String infoProcesadaRms2 = "SELECT shipment, seq_no, item, distro_no, status_code"
				+ " FROM RMS100.shipsku "
				+ "WHERE shipment = '%s'";
		
		
				
				
	
/**
 * **************************************      Pasos del caso de Prueba		 *******************************************/
					
//*************************************************Paso 1 **************************************************************			
		addStep("Validar que existan los adapter de conexión a la base de datos de RDM en la tabla wm_rdm_connections de WMINT.");	
		
		System.out.println(adapterRDM);
		
		SQLResult adapterRDMResult = executeQuery(dbPos, adapterRDM);
		
		String RETEK_PHYSICAL_CR = adapterRDMResult.getData(0, "RETEK_PHYSICAL_CR");
			
		System.out.println("RETEK_PHYSICAL_CR " + RETEK_PHYSICAL_CR);
		
	    boolean validaadapterRDM = adapterRDMResult.isEmpty();
		
		if (!validaadapterRDM) {
			
			testCase.addQueryEvidenceCurrentStep(adapterRDMResult);
		}
		
		System.out.println(validaadapterRDM);
		
		assertFalse(validaadapterRDM, "No se presentaron los adapter de conexión de tipo NT y XA para la base de datos de RDM.");
		
//***********************************************Paso 2 *************************************************************		
		
		addStep("Validar que exista información pendiente de procesar para el CEDIS  ");
	
		String infoPendienteCedisF = String.format(infoPendienteCedis, RETEK_PHYSICAL_CR);
		
		System.out.println(infoPendienteCedisF);
		
		SQLResult infoPendienteCedisResult = dbRdm.executeQuery(infoPendienteCedisF);
		
		String BOL_NBR = infoPendienteCedisResult.getData(0, "BOL_NBR");
		
		System.out.println("BOL_NBR " + BOL_NBR );
		
		
		boolean validainfoPendienteCedis = infoPendienteCedisResult.isEmpty();
		
		if (!validainfoPendienteCedis) {
			
			testCase.addQueryEvidenceCurrentStep(infoPendienteCedisResult);
					
		}
		
		System.out.println(validainfoPendienteCedis);
		
		assertFalse(validainfoPendienteCedis , "Se encontraron documentos pendientes de procesar en el CEDIS.");
		
//**********************************************Paso 3 ************************************************************
		
		addStep("Ejecutar el servicio RR02.Pub:runNotification, para ejecutar la interfaz RR02 y procesar la información del CEDIS. "
				+ "La interfaz será invocada desde el job runRR02 desde Ctrl-M.");		
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
		    addStep("Validar que el registro de la tabla wm_log_run termine en estatus 'S'");
			
		  	
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
			
			String threads = String.format(consultaThreads,RETEK_PHYSICAL_CR);
			
			System.out.println("CONSULTA THREAD "+ threads);
			
			 SQLResult threadsResult=  dbLog.executeQuery(threads);
	        
			
			boolean av31 = threadsResult.isEmpty();
			if (av31 == false) {

				testCase.addQueryEvidenceCurrentStep(threadsResult);

			} else {
				testCase.addQueryEvidenceCurrentStep(threadsResult);
			}
			System.out.println("El registro en WM_LOG_THREAD esta vacio 1- "+ av31);
			
			
			assertFalse(av31, "No se muestra el registro de ejecución para el CEDIS ");
			
			
//*************************************************** Paso 6 ****************************************************************************			
		addStep("Validar que la información pendiente de procesar sea actualizada con estatus 'S' y el id de ejecución.");
		
		
		String infoProcesadaRDMFormat = String.format(infoProcesadaRDM, BOL_NBR,run_id);
		
		System.out.println(infoProcesadaRDMFormat);
		
		SQLResult infoProcesadaRDMResult = executeQuery(dbRdm, infoProcesadaRDMFormat);
		
		
		boolean valdainfoProcesadaRDM = infoProcesadaRDMResult.isEmpty();
		
		if (!valdainfoProcesadaRDM) {
			
			testCase.addQueryEvidenceCurrentStep(infoProcesadaRDMResult);
			
		}
		
		System.out.println(valdainfoProcesadaRDM);
		
		assertFalse(valdainfoProcesadaRDM, "Se presentaran los documentos procesados con estatus S y el id de ejecución de la interfaz");
		
//********************************************* Paso 7 *********************************************************************************
		
   addStep("Validar la inserción de los datos en la base de datos de RMS.");
		
		//Primera consulta
		String infoProcesadaRmsF = String.format(infoProcesadaRms,BOL_NBR, RETEK_PHYSICAL_CR );
		
		System.out.println(infoProcesadaRmsF);
		
		SQLResult infoProcesadaRmsResult = executeQuery(dbRms, infoProcesadaRmsF);
		
		String SHIPMENT = adapterRDMResult.getData(0, "SHIPMENT");
		
		System.out.println("SHIPMENT " + SHIPMENT);
		
		//Segunda consulta
		
		 String infoProcesadaRms2F = String.format(infoProcesadaRms2, SHIPMENT );
			
		 System.out.println(infoProcesadaRms2F);
			
		 SQLResult infoProcesadaRms2Result = executeQuery(dbRms, infoProcesadaRms2F);
			
		 boolean valdainfoProcesadaRms = infoProcesadaRmsResult.isEmpty();
		
		 boolean valdainfoProcesadaRms2 = infoProcesadaRms2Result.isEmpty();
		 
		
		
		if (!(valdainfoProcesadaRms && valdainfoProcesadaRms2)) {
			
			testCase.addQueryEvidenceCurrentStep(infoProcesadaRmsResult);
			testCase.addQueryEvidenceCurrentStep(infoProcesadaRms2Result);
			
		}
		
		System.out.println(valdainfoProcesadaRms);
		System.out.println(valdainfoProcesadaRms2);
			
		assertFalse(valdainfoProcesadaRms, "No se presentara la información de los documentos insertadas en las tablas de RMS.");
		
		
		
				
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
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
		return "La interfaz se encarga del proceso de copia de las Devoluciones almacenadas en RDM hacia RMS.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_RR02_002_ValidaEnvioDocASN";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}

