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

public class RR02EnvioDeInfoProcesadaATablaDeHistorial extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_RR02_001_EnvioDeInfoProcesadaATablaDeHistorial(HashMap<String, String> data) throws Exception {
		
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
		
		String  infoProcesada = "SELECT * FROM wm_bol_to_upload WHERE pub_status = 'S'";
		
		
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
		
		String infoEliminadaRDM = "SELECT * FROM wm_bol_to_upload where  wm_run_id = '%s'  ";
		
		//Paso 7
		
		String validaInfoAlmacenada = "SELECT * FROM wm_bol_to_upload_hist where wm_run_id = '%s' AND pub_status = 'S'";
		
		
		
				
				
	
/**
 * **************************************      Pasos del caso de Prueba		 *******************************************/
					
//*************************************************Paso 1 **************************************************************			
		addStep("Validar que existan los adapter de conexión a la base de datos de RDM en la tabla wm_rdm_connections de WMINT");	
		
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
		
		addStep("Validar que existe información procesada con estatus 'S' en la tabla wm_bol_to_upload, visualizar ID de ejecución en el campo wm_run_id.");
	
	/*	String infoPendienteCedisF = String.format(infoPendienteCedis, RETEK_PHYSICAL_CR);
	 * 
	*/	System.out.println(infoProcesada);
		
		SQLResult infoProcesadaResult = dbRdm.executeQuery(infoProcesada);
		
		String WM_RUN_ID = infoProcesadaResult.getData(0, "WM_RUN_ID");
		
		System.out.println("WM_RUN_ID " + WM_RUN_ID);
		
		
		boolean validaiinfoProcesadaResult = infoProcesadaResult.isEmpty();
		
		if (!validaiinfoProcesadaResult) {
			
			testCase.addQueryEvidenceCurrentStep(infoProcesadaResult);
					
		}
		
		System.out.println(validaiinfoProcesadaResult);
		
		assertFalse(validaiinfoProcesadaResult , "No se presenta la información procesada con estatus 'S'");
		
//**********************************************Paso 3 ************************************************************
		
		addStep("Ejecutar el servicio RR02.Pub:runNotification. La interfaz será invocada desde el job runRR02 desde Ctrl-M.");		
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
			
		addStep("Validar que la información fue eliminada de la tabla wm_bol_to_upload.");
		
		
		String infoEliminadaRDMFormat = String.format(infoEliminadaRDM, WM_RUN_ID);
		
		System.out.println(infoEliminadaRDMFormat);
		
		SQLResult infoEliminadaRDMResult = executeQuery(dbRdm, infoEliminadaRDMFormat );
		
		
		boolean valdainfoEliminadaRDM = infoEliminadaRDMResult.isEmpty();
		
		if (valdainfoEliminadaRDM) {
			
			testCase.addQueryEvidenceCurrentStep(infoEliminadaRDMResult);
			testCase.addBoldTextEvidenceCurrentStep("La informacion se elimino de la tabla wm_bol_to_upload");
			
		}else {
			
			testCase.addQueryEvidenceCurrentStep(infoEliminadaRDMResult);
			testCase.addBoldTextEvidenceCurrentStep("La informacion no se elimino de la tabla wm_bol_to_upload");
		}
		
		
		System.out.println(valdainfoEliminadaRDM);
		
		assertFalse(valdainfoEliminadaRDM, "La información no fue eliminada en la tabla wm_bol_to_upload.");
		
//********************************************* Paso 7 *********************************************************************************
		
   addStep("Validar que la información fue almacenada en la tabla wm_bol_to_upload_hist.");
		
		//Primera consulta
		String validaInfoAlmacenadaF = String.format(validaInfoAlmacenada, WM_RUN_ID );
		
		System.out.println(validaInfoAlmacenadaF);
		
		SQLResult validaInfoAlmacenadaResult = executeQuery(dbRms, validaInfoAlmacenadaF);
		
		boolean validaInfoAlmacenadaB = validaInfoAlmacenadaResult.isEmpty();
		
		if (!validaInfoAlmacenadaB) {
			
			
			testCase.addQueryEvidenceCurrentStep(validaInfoAlmacenadaResult);
			
		}
		
		System.out.println(validaInfoAlmacenadaB);
		
			
		assertFalse(validaInfoAlmacenadaB, "La información anteriormente almacenada en la tabla wm_bol_to_upload_hist  no fue almacenada en la tabla de historial");
		
		
		
				
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
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
		return "ATC_FT_RR02_001_EnvioDeInfoProcesadaATablaDeHistorial";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}

