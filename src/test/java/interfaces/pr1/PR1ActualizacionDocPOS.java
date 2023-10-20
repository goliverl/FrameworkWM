package interfaces.pr1;
/**
 * @author 41335
 */

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.PR1;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class PR1ActualizacionDocPOS extends BaseExecution{

//ALM caso de enmedio
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PR1_ActualizacionDocPOS_test(HashMap<String, String> data) throws Exception {
	
	/* Utilerías *********************************************************************/
		
	utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
	utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
	
	
	//agregar log

	/**
	 * Variables ******************************************************************************************
	 * 
	 * 
	*/
	
	String tdcQueryTSFDoc = " SELECT a.id,b.pid_id,a.doc_type, a.status,b.no_records "
			+ " FROM POSUSER.POS_INBOUND_DOCS a, POSUSER.POS_TSF b"
			+ " WHERE a.id = b.pid_id "
			+ " AND a.doc_type='TSF' "
			+ " AND b.no_records = 0 "
			+ " AND a.status = 'I'";
	
	
	String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
			+ " FROM WMLOG.wm_log_run"
			 + " WHERE interface = 'PR1main'" 	
			 +" and  start_dt >= TRUNC(SYSDATE)"
		     +" order by start_dt desc)"
			+ " where rownum = 1";	
	
	
	String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
			+ " FROM WMLOG.WM_LOG_ERROR "
			+ " where RUN_ID=%s"; //FCWMLQA 
	
	String tdcQueryStatusX = "SELECT status,id,received_date"
			+ " FROM POSUSER.pos_inbound_docs "
			+ "	WHERE status = 'X' " 
			+ " and id = '%s'"
			+ " ORDER BY received_date DESC";

	String id;
	
	
	
/*Pasos *********************************************************************************/
	
// 								Paso 1****************************************************
	
	
addStep("Validar que existan documentos TSF sin registros");
	
	System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
	
	System.out.println(tdcQueryTSFDoc);
	
	SQLResult resul = db.executeQuery(tdcQueryTSFDoc);
	 id = resul.getData(0, "id");
	boolean empty = resul.isEmpty();

	if (!empty) {
		
		
		testCase.addQueryEvidenceCurrentStep(resul);
		
	}
	
	
	System.out.println(empty);
	
assertFalse(empty,"No existen documentos TSF sin registros");	
	
	
//								Paso 2****************************************************

addStep("Ejecutar el servicio PR1.Pub:run.");

SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
PakageManagment pok = new PakageManagment(u, testCase);
String user = data.get("user");
String ps = PasswordUtil.decryptPassword( data.get("ps"));
String server = data.get("server");
String con ="http://"+user+":"+ps+"@"+server+":5555";
String searchedStatus = "R";
String run_id;
String status = "E";

System.out.println(pok);
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
   
   if(emptyError){  
   
    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
   
    testCase.addQueryEvidenceCurrentStep(paso3);
   
   }
}
    
   
	
	
    
//                               Paso 4 *******************************************************  
addStep("Validar que en la tabla WM_LOG_ERROR se genere un error.");    

     
    System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
    
   
    String error = String.format(tdcQueryErrorId, run_id);
    SQLResult exeQueryErrLog = dbLog.executeQuery(error);
    System.out.println(error);
    boolean exe = exeQueryErrLog.isEmpty();//true
    System.out.println(exe);
    
    if (!exe) {
    	
    	testCase.addQueryEvidenceCurrentStep(exeQueryErrLog);
		
	}
    
    assertFalse(exe, "La ejecución de la interfaz  fue exitosa");

 
 //	                             Paso5****************************************************
		
addStep("Validar que el registro en la tabla de POSUSER cambie de estatus a 'X'");

	
	System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);	
	System.out.println(tdcQueryStatusX);
	String idSts = String.format(tdcQueryStatusX, id);
	SQLResult stsDb = db.executeQuery(idSts);	
	boolean staticX = stsDb.isEmpty();
	
	if (!staticX) {
		
		testCase.addQueryEvidenceCurrentStep(stsDb);
		
		
	}
	
	
	System.out.println(staticX);
	
assertFalse(staticX,"El registro no cambio a estatus X");

	
	
	
	
	}
	
	

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Qaautomation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PR1_ActualizacionDocPOS_test";
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
