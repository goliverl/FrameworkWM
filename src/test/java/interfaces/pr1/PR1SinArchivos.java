package interfaces.pr1;

/**
 * @author 41335
 */
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.openqa.selenium.By;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.PR1;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class PR1SinArchivos extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_PR1_PR1SinArchivos_test(HashMap<String, String> data) throws Exception {
	
	/* Utilerías *********************************************************************/

	
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
	

	/*
	 * Comentario : No esta en ALM utiliza insumos de colombia y mexico
	 */

	/**
	 * Variables ******************************************************************************************
	 * 
	 * 
	*/
	
	String tdcQueryConnectionsTSF ="SELECT ID,PE_ID,STATUS,DOC_TYPE,PV_DOC_NAME,RECEIVED_DATE,TARGET_ID "
			+" FROM POSUSER.POS_INBOUND_DOCS " + 
			" WHERE DOC_TYPE IN ('TSF')" + 
			" AND STATUS ='I'" + 
			" AND PARTITION_DATE > SYSDATE - 13"+
			" ORDER BY RECEIVED_DATE  DESC";
	
	
	String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
			+ " FROM WMLOG.wm_log_run"
			 + " WHERE interface = 'PR1main'" 	
			 +" and  start_dt >= TRUNC(SYSDATE)"
		     +" order by start_dt desc)"
			+ " where rownum = 1";	
	
	String tdcQueryStatusLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER"
			+ " FROM WMLOG.WM_LOG_RUN"
			+ " WHERE RUN_ID = %s";
	
	String tdcQueryStatusThread = "SELECT PARENT_ID,THREAD_ID,NAME,STATUS,ATT1,ATT2 "
			+ " FROM WMLOG.WM_LOG_THREAD "
			+ " WHERE PARENT_ID = %s";
	
	String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
			+ " FROM WMLOG.WM_LOG_ERROR "
			+ " where RUN_ID=%s"; //FCWMLQA 
	
	
	/*Pasos *********************************************************************************/
	
	// 								Paso 1****************************************************
	
	
addStep("Validar que no existan documentos TSF a procesar en la tabla POS_INBOUND_DOCS. "+tdcQueryConnectionsTSF);
	
    System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);	
	System.out.println(tdcQueryConnectionsTSF);
	
	SQLResult proc = dbPuser.executeQuery(tdcQueryConnectionsTSF);
	
	boolean docTsf = proc.isEmpty();
	
	if (docTsf) {
			
			
			testCase.addBoldTextEvidenceCurrentStep("Resultado esperado: No existen docuemntos TSF a procesar");
			
			
		}
	

	
assertTrue(docTsf,"Existen documentos TSF");
	
///								 Paso 2***************************************************
	
addStep("Ejecutar el servicio "+data.get("servicio"));

SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
PakageManagment pok = new PakageManagment(u, testCase);
String user = data.get("user");
String ps = PasswordUtil.decryptPassword( data.get("ps"));
String server = data.get("server");
String con ="http://"+user+":"+ps+"@"+server+":5555";
String searchedStatus = "R";
String status = "S";
String  run_id;
testCase.setProject_Name("Autonomía Sistema OXXO Colombia");

			
		
	u.get(con);	
	pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));




	SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);
	System.out.println(query);
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
	   System.out.println(error);
	   SQLResult paso3 = executeQuery(dbLog, error);
	   
	   boolean emptyError = paso3.isEmpty();
	   
	   if(!emptyError){  
	   
	    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
	   
	    testCase.addQueryEvidenceCurrentStep(paso3);
	   
	   }
	}
		
		
/// 								Paso 3***************************************************** 
		
addStep("Validar que el registro de ejecución de la interfaz termino en estatus 'S' en la tabla WM_LOG_RUN. " );
			
    System.out.println(GlobalVariables.DB_HOST_FCWMLQA);			
	System.out.println(tdcQueryStatusLog);
	
	 String logsFormat = String.format(tdcQueryStatusLog, run_id);
	
	SQLResult logS = executeQuery(dbLog,logsFormat);
	
	String statusLog = logS.getData(0, "STATUS");	
		
	boolean validateStatus = status.equals(statusLog);
		if (validateStatus) {
			
			testCase.addQueryEvidenceCurrentStep(logS);
			
		}
	
	
	

		
assertTrue(validateStatus,"La ejecución de la interfaz no fue exitosa");
		
//										Paso 4******************************************************
		
	
	
addStep("Validar que la ejecución no genere hilos en la tabla WM_LOG_THREAD. ");	
	
	String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
	System.out.println(queryStatusThread);
	
	SQLResult thred = dbLog.executeQuery(queryStatusThread);
	
	
	boolean  threadEmpty = thred.isEmpty();
	
	if (threadEmpty) {
		
		
		testCase.addBoldTextEvidenceCurrentStep("Resultado esperado: No se generaron threads");		
		
	}
	
	
		
assertTrue(threadEmpty,"La ejecución genero threads");
			
	
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "No contar con archivos a procesar, validar que la WM_LOG finalice exitosa en su ejecución y no muestre hilos ejecutados";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_004_PR1_PR1SinArchivos_test";
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
