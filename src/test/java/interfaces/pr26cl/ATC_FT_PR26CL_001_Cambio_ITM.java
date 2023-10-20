package interfaces.pr26cl;

import static org.testng.Assert.assertFalse;

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

public class ATC_FT_PR26CL_001_Cambio_ITM extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR26CL_001_Cambio_ITM_test(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		SQLUtil dbrms = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERChile, GlobalVariables.DB_USER_RMSWMUSERChile, GlobalVariables.DB_PASSWORD_RMSWMUSERChile);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_PosUserChile, GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
		
/**
* Variables ******************************************************************************************
* 
* 
*/
		
		String tdcQueryInfo = "SELECT BATCH_ID, ARCHIVO, wm_itm_status, WM_TARGET_ITM "
				+ " FROM wmuser.POS_ITM_PRM_HEAD "
				+ " WHERE wm_itm_status = 'L' "
				+ " ORDER BY batch_id,location ASC";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = '" + data.get("wm_log") +"'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum = 1";
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"
				+ " and rownum = 1";
		
		String tdcQueryEnvio = "SELECT ID, DOC_NAME, DOC_TYPE, SENT_DATE, PV_CR_PLAZA, PV_CR_TIENDA"
				+ " FROM POS_OUTBOUND_DOCS"
				+ " WHERE doc_type = 'ITM' "
				+ " AND Trunc( sent_date ) = Trunc( SYSDATE )"
				+ " ORDER BY sent_date";
		
		
		testCase.setProject_Name("AutomationQA");	
		
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************
		addStep("Acceder a la BD de RMS.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: BDCHRMSQ.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_RMSWMUSERChile);
								
//Paso 2	************************ 	
		addStep("Validar que exista información a exportar.");
		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);
		System.out.println(tdcQueryInfo);
		
		SQLResult infoResult = executeQuery(dbrms, tdcQueryInfo);
		
		boolean info = infoResult.isEmpty();
		
		if (!info) {
			
			testCase.addQueryEvidenceCurrentStep(infoResult);
		}
		
		System.out.println(info);
		
		assertFalse(info, "No se obtiene información de la consulta");
		
//Paso 3	************************ 	
		addStep("Ejecutar la interface PR26_CL.");
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

//Paso 4 *************************
		addStep("Acceder a la BD de WM en esquema WMLOG.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: OCHWMQA.femcom.net");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_LogChile);	
				
//Paso 5	************************ 	
		addStep("Verificar que la interfaz se haya ejecutado sin problemas.");
		
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
	    
//Paso 6 *******************************
	    addStep("Verificar que no contenga errores de ejecución.");

	   String error = String.format(tdcQueryErrorId, run_id);
	   SQLResult paso2 = executeQuery(dbLog, error);
	   
	   boolean emptyError = paso2.isEmpty();
	   
	   if(!emptyError){  
	   	   
	    testCase.addQueryEvidenceCurrentStep(paso2);
	   
	   }
	   
	   testCase.addQueryEvidenceCurrentStep(paso2);
	   testCase.addTextEvidenceCurrentStep(" No se encontró un error en la ejecución de la interfaz, en la tabla WM_LOG_ERROR");

//paso 7 ****************************
	   addStep("Acceder al esquema POSUSER y verificar que exista el registro de envío del documento.");
	   System.out.println(GlobalVariables.DB_HOST_PosUserChile);
	   System.out.println(tdcQueryEnvio);
		
		SQLResult envioResult = executeQuery(dbPos, tdcQueryEnvio);
		
		boolean envio = envioResult.isEmpty();
		
		if (!envio) {
			
			testCase.addQueryEvidenceCurrentStep(envioResult);
		}
		
		System.out.println(envio);
		
		assertFalse(envio, "No se obtiene información de la consulta");
		

		
		
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
		return "Terminada. Ejecutar la interface PR26_CL y comprobar que exporte el campo VAT_CODE_COST.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
