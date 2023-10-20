package interfaces.FL1;

import modelo.BaseExecution;
import util.GlobalVariables;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class FL1_Validar_QueLaInformacion_SeProcese_RDM extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_FL1_001_ValidarInformacSeProcesoRDM(HashMap<String, String> data) throws Exception {
		
		/* Utilerias *********************************************************************/

		utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser,GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbRDM = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RDM,GlobalVariables.DB_USER_RDM, GlobalVariables.DB_PASSWORD_RDM);

		/**
		 * ALM
		 * Validar que la informacion de RDM se procese correctamente para el CEDIS 15CMF y la plaza 10ZAL
		 */
		
		/**
		 * Variables ******************************************************************************************
		 * 
		 * 
		*/
		
		String tdcQueryWM_RDM_CONNECTIONS= "SELECT RETEK_CR, RETEK_PHYSICAL_CR, ORACLE_CR, CONNECTION_NAME, CEDIS_DESC, STATUS "
				+ " FROM WMUSER.WM_RDM_CONNECTIONS"
				+ " WHERE retek_physical_cr = '" + data.get("Retek")+"'" 
		     	+ " AND transaction_type = 'NT'"
				+ " AND status = 'A'";

		
		String tdcQueryXXFC_ESTIMADO_SURTIDO= "SELECT CEDIS, CRPLAZA, STATUS, CREATE_DATE, WM_STATUS "
				+ " FROM XXFC_ESTIMADO_SURTIDO "
				+ " WHERE crplaza = '" + data.get("Crplaza")+"'" 
		     	+ " AND wm_status  = 'I'";
		
		String tdcQuerywm_log_run = "select * from(SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER "
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE TRUNC(START_DT) = TRUNC(SYSDATE)" 
		     	+ " AND interface like '%FL1_9%' ORDER BY run_id DESC)where rownum <=1";
		
	
		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,status,att1,att2"
				+ " FROM WMLOG.wm_log_thread"
				+ " WHERE parent_id = '%s'";
		
		String tdcQueryESTIMADO_SURTIDO= "SELECT CEDIS, CRPLAZA, STATUS, CREATE_DATE, WM_STATUS, WM_RUN_ID, WM_SENT_DATE "
				+ " FROM XXFC_ESTIMADO_SURTIDO"
				+ " WHERE crplaza = '" + data.get("Crplaza")+"'" 
		     	+ " AND wm_status  = 'E'"
				+ " AND WM_RUN_ID = '%s'";
		   //     + "  AND TSF_NO = '%s'";

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		//String con = "http://" + user + ":" + ps + "@" + server;
		//String searchedStatus = "R";
		String status = "S";
		

		
		/*Pasos *********************************************************************************/
		
		///										Paso 1 ************************************************
		addStep("Validar que exista la informacion de la conexion a RDM para el CEDIS Oxxo Leon (15CMF) en WMINT.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		
		  System.out.println("\n"+ tdcQueryWM_RDM_CONNECTIONS); 
		     
		  SQLResult QueryWM_RDM= executeQuery(dbPuser, tdcQueryWM_RDM_CONNECTIONS);
		     
		  boolean StatusQueryWM_RDM = QueryWM_RDM.isEmpty();
		     
		     testCase.addQueryEvidenceCurrentStep(QueryWM_RDM);
			
		 	 assertFalse(StatusQueryWM_RDM,"No existe informacion de la conexion"); 
		 	
		///									Paso 2 ************************************************
		addStep("Validar que exista informacion pendiente de procesar para la plaza" + data.get("Crplaza") +" en la conexion de RDM del CEDIS" + data.get("Retek"));
		System.out.println(GlobalVariables.DB_HOST_RDM);
				
		  System.out.println("\n"+ tdcQueryXXFC_ESTIMADO_SURTIDO); 

		  SQLResult QueryXXFC= executeQuery(dbRDM, tdcQueryXXFC_ESTIMADO_SURTIDO);
		
		     
		  boolean StatusQueryXXFC = QueryXXFC.isEmpty();
		     
		  testCase.addQueryEvidenceCurrentStep(QueryXXFC);
			
		  assertFalse(StatusQueryXXFC,"No existe informacion pendiente"); 
		 	 
		///									Paso 3 ************************************************
	    addStep("Ejecutar el servicio de la interface: FL1.Pub:runNotification");
		System.out.println(GlobalVariables.DB_HOST_RDM);

			String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
			u.get(contra);

			u.hardWait(4);

			pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
			
			 
			
		///									Paso 4 ************************************************
		addStep("Validar que el archivo se envio correctamente al terminar la interfaz FL1 sin errores");
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
	 
		System.out.println("\n"+ tdcQuerywm_log_run); 

		SQLResult Querywm_log_run= executeQuery(dbLog, tdcQuerywm_log_run);
		
		String Statuslog = Querywm_log_run.getData(0, "STATUS");

		String RunID = Querywm_log_run.getData(0, "RUN_ID");

		boolean validateStatus = Statuslog.equals(status);
		
       if(validateStatus){
			
			testCase.addQueryEvidenceCurrentStep(Querywm_log_run);
		}

		System.out.println(validateStatus + Statuslog);
		System.out.println(RunID);
		
		
		
	    assertTrue(validateStatus,"La ejecucion de la interfaz no fue exitosa");
	    
	   
	    
		//Thread 
	    addStep("Validar Thread");
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
	 
	    String QueryThread = String.format(tdcQueryStatusThread, RunID);

	    SQLResult EXQueryThread = executeQuery(dbLog, QueryThread);

		System.out.println(EXQueryThread);
		
		///									Paso 5 ************************************************
		addStep("Validar que se actualizaron correctamente los campos WM_STATUS a \"E\", WM_SENT_DATE a la fecha actual y los campos WM_RUN_ID y WM_DOC_NAME  de la tabla XXFC_ESTIMADO_SURTIDO en RDM");
		System.out.println(GlobalVariables.DB_HOST_RDM);
		System.out.println(RunID);	
		
		
	    String QueryESTIMADO_SURTIDO = String.format(tdcQueryESTIMADO_SURTIDO, RunID);
	    
	    System.out.println(QueryESTIMADO_SURTIDO);	

	    SQLResult EXQueryESTIMADO_SURTIDO = executeQuery(dbRDM, QueryESTIMADO_SURTIDO);
	    
		System.out.println(EXQueryESTIMADO_SURTIDO);

	
		boolean validateStatusWM = EXQueryESTIMADO_SURTIDO.isEmpty();
		
		if(!validateStatusWM) {
			
			testCase.addQueryEvidenceCurrentStep(EXQueryESTIMADO_SURTIDO);
			
		}
		
		
		System.out.println(validateStatusWM);
		
	    assertFalse(validateStatusWM,"No se Actualizo Correctamente");
	    
	 //   u.close();
	    
}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar que la informacion de RDM se procese correctamente para el CEDIS";
	}
	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}
	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_FL1_001_ValidarInformacSeProcesoRDM";
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