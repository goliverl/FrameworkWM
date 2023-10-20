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

public class FL1_Validar_QueLaInformacion_SeReProcese_RDM extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_FL1_002_ValidarInformacSeReprocesoRD(HashMap<String, String> data) throws Exception {
		
		/* Utilerias *********************************************************************/

		utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser,GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbRDM = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RDM,GlobalVariables.DB_USER_RDM, GlobalVariables.DB_PASSWORD_RDM);

		/**
		 * ALM
		 * Validar que la informacion se actualice para ser re-procesada por la interfaz FL1
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
				+ " FROM XXFC_ESTIMADO_SURTIDO"
				+ " WHERE crplaza = '" + data.get("Crplaza")+"'" 
		     	+ " AND wm_status  = 'F'";
		
		String tdcQuerywm_log_run = "SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS, SERVER "
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE TRUNC(START_DT) = TRUNC(SYSDATE)" 
		     	+ " AND interface = 'FL1_REPROC' ORDER BY run_id DESC";
		
		String tdcQueryESTIMADO_SURTIDO= "SELECT CEDIS, CRPLAZA, STATUS, CREATE_DATE, WM_STATUS, WM_RUN_ID, WM_SENT_DATE "
				+ " FROM XXFC_ESTIMADO_SURTIDO"
				+ " WHERE crplaza = '" + data.get("Crplaza")+"'" 
		     	+ " AND wm_status  = 'I'"
				+ " AND WM_RUN_ID = '%s'";
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		
		String status = "S";
		
		
		/*Pasos *********************************************************************************/
		
		///										Paso 1 ************************************************
		addStep("Validar que exista la informacion de la conexion a RDM para el CEDIS Oxxo Leon en WMINT.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		
		 System.out.println("\n"+ tdcQueryWM_RDM_CONNECTIONS); 
	     
		  SQLResult QueryWM_RDM= executeQuery(dbPuser, tdcQueryWM_RDM_CONNECTIONS);
		     
		  boolean StatusQueryWM_RDM = QueryWM_RDM.isEmpty();
		     
		  testCase.addQueryEvidenceCurrentStep(QueryWM_RDM);
			
		  assertFalse(StatusQueryWM_RDM," No existe informacion de la conexion"); 
		
		///									Paso 2 ************************************************
		addStep("Validar que exista informacion pendiente de procesar para la plaza" + data.get("Crplaza") +" en la conexion de RDM ");
		System.out.println(GlobalVariables.DB_HOST_RDM);
					
		 System.out.println("\n"+ tdcQueryXXFC_ESTIMADO_SURTIDO); 

		 SQLResult QueryXXFC= executeQuery(dbRDM, tdcQueryXXFC_ESTIMADO_SURTIDO);
			     
		 boolean StatusQueryXXFC = QueryXXFC.isEmpty();
			     
		 testCase.addQueryEvidenceCurrentStep(QueryXXFC);
				
		 assertFalse(StatusQueryXXFC,"No existe informacion pendiente"); 
		 
		///									Paso 3 ************************************************
		
		 addStep("Ejecutar el servicio de la interface:FL1.Pub:runReprocesar");
	    System.out.println(GlobalVariables.DB_HOST_RDM);
				
		 u = new SeleniumUtil(new ChromeTest(), true);

		 pok = new PakageManagment(u, testCase);

		 String contra = "http://" + user + ":" + ps + "@" + server + ":5555";

		 u.get(contra);

		 u.hardWait(4);

		 // pok.runIntefaceWM(data.get("interfase"), data.get("servicio"),data.get("Retek"));
		  pok.runIntefaceWmWithInput10(data.get("interfase"), data.get("servicio"), data.get("Retek"),
					"origen");
		 
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

		assertTrue(validateStatus,"La ejecuci�n de la interfaz no fue exitosa"); 
		 
		///									Paso 5 ************************************************
		addStep("Validar que se actualizaron correctamente los campos WM_STATUS a \"I\" y el campo WM_RUN_ID de la tabla XXFC_ESTIMADO_SURTIDO en RDM");
		System.out.println(GlobalVariables.DB_HOST_RDM);
					 
		String QueryESTIMADO_SURTIDO = String.format(tdcQueryESTIMADO_SURTIDO, RunID);

		SQLResult EXQueryESTIMADO_SURTIDO = executeQuery(dbRDM, QueryESTIMADO_SURTIDO);
			    
		System.out.println(EXQueryESTIMADO_SURTIDO);

		
			    
		boolean validateStatusWM = EXQueryESTIMADO_SURTIDO.isEmpty();

		System.out.println(validateStatusWM);
		if(!validateStatusWM) {
			
			testCase.addQueryEvidenceCurrentStep(EXQueryESTIMADO_SURTIDO);
			
		}
				
		assertFalse(validateStatusWM,"No se Actualizo Correctamente"); 
		 
		 
		 
}
	
	

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Validar que la informaci�n de RDM se reprocese correctamente para el CEDIS";
	}
	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}
	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_FL1_002_ValidarInformacSeReprocesoRD";
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

