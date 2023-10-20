package interfaces.RO1_MX;

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

public class ATC_FT_001_Ro1_MX_ActualizarRegistroProveedoresError extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_Ro1_MX_ActualizarRegistroProveedoresError_test(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/
		
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables. DB_PASSWORD_AVEBQA);
		
		
		String status = "S";
		
		
		String run_id ;
		
		testCase.setProject_Name("Regresion");
/**
 * Variables ******************************************************************************************
* 
* 
*/
		String tdcQueryContactName = "SELECT ID, VENDORID, CONTACTNAME, TERMSNAME, WM_STATUS, CREATE_DATE, ADDR_TYPE "
				+ " FROM WMUSER.WM_VENDORS_SYNC "
				+ " WHERE WM_STATUS='L' "
				+ " AND CONTACTNAME = '"+ data.get("CONTACTNAME")+"'";
		
		String tdcQueryTerms = "SELECT TERMS "
				+ "FROM TERMS "
				+ "WHERE TERMS_CODE = '30 dias'"; 

		
		String tdcQueryError = "SELECT RUN_ID, INTERFACE, END_DT, S"
				+ "TATUS FROM WM_LOG_RUN "
				+ "WHERE INTERFACE = 'RO1' "
				+ "AND TRUNC(END_DT) = TRUNC(SYSDATE) "
				+ "AND STATUS = 'E' ORDER BY RUN_ID DESC;\r\n";
		
		String tdcQueryErrorGenerado = "SELECT ERROR_ID, RUN_ID, ERROR_DATE, DESCRIPTION"
				+ " FROM WM_LOG_ERROR "
				+ "WHERE RUN_ID = [WM_LOG_RUN.RUN_ID] "
				+ "AND TRUNC(ERROR_DATE) = TRUNC(SYSDATE);";
		
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = '" + data.get("wm_log") +"'"
				+" and  start_dt >= TRUNC(SYSDATE)"
			    +" order by start_dt desc)"
				+ " where rownum = 1";	
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; //FCWMLQA
		
		String tdcQueryContact = "SELECT ERROR_ID, RUN_ID, ERROR_DATE, DESCRIPTION"
				+ " FROM WM_LOG_ERROR "
				+ "WHERE RUN_ID = [WM_LOG_RUN.RUN_ID] "
				+ "AND TRUNC(ERROR_DATE) = TRUNC(SYSDATE);";
		
		
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//		Paso 1	************************ 
		
addStep("Consultar que existan registros en las tablas WM_VENDORS_SYNC de la BD ORAFIN");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		System.out.println(tdcQueryContactName);
		
		SQLResult result1 = executeQuery(dbEbs, tdcQueryContactName);

		boolean contact = result1.isEmpty();
		
			if (!contact) {
		
			testCase.addQueryEvidenceCurrentStep(result1);
			
						} 
		
		System.out.println(contact);

assertFalse(contact, "No se obtiene informacion de la consulta");


//		Paso 2	************************

addStep("Comprobar que existan registros en la tabla TERMS de la BD RETEK");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(tdcQueryTerms);
		
		SQLResult result2 = executeQuery(dbRms, tdcQueryTerms);
		
		boolean terms = result2.isEmpty();
		
			if (!terms) {	
				
				testCase.addQueryEvidenceCurrentStep(result2);
				
						} 
	
		System.out.println(terms);

assertFalse(terms, "La tabla no contiene registros");


//Paso 3	************************

addStep("Ejecutar el servicio RO1.Pub:run.");

SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
PakageManagment pok = new PakageManagment(u, testCase);

String user = data.get("user");
String ps = PasswordUtil.decryptPassword( data.get("ps"));
String server = data.get("server");
//String con ="http://"+user+":"+ps+"@"+server;
String searchedStatus = "R";

String contra =   "http://"+user+":"+ps+"@"+server+":5555";
u.get(contra);


pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));

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
   SQLResult paso5 = executeQuery(dbLog, error);
   
   boolean emptyError = paso5.isEmpty();
   
   if(!emptyError){  
   
    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
   
    testCase.addQueryEvidenceCurrentStep(paso5);
   
   }
}




//	Paso 4	************************

addStep("Se devuelve el registro de la interfaz ejecuta mostrando errores.");
 	
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(tdcQueryError);
		
		SQLResult result3 = executeQuery(dbRms, tdcQueryError);
		
		
		boolean addr = result3.isEmpty();
		
			if (!addr) {	
				
				testCase.addQueryEvidenceCurrentStep(result3);	
				
						} 
		
		System.out.println(addr);

assertFalse(addr, "La tabla no contiene registros");
	

	    
//		Paso 5	************************

addStep("Comprobar que existan registros de los proveedores que se procesaran, en la tabla SUPS de la BD RETEK.");

	    System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
	    System.out.println(tdcQueryErrorGenerado);
	    
	    SQLResult result4 = executeQuery(dbRms, tdcQueryErrorGenerado);


	    boolean sups = result4.isEmpty();

	    	if (!sups) {	
	    		
	    		testCase.addQueryEvidenceCurrentStep(result4);	
	    		
	    				} 

	    System.out.println(sups);

assertFalse(sups, "La tabla no contiene registros");
	    



//		Paso 6	************************

addStep("Comprobar que los registros fueron actualizados correctamente.");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		System.out.println(tdcQueryContact);
		
		SQLResult result8 = executeQuery(dbEbs, tdcQueryContact);
		
		
		boolean vendors = result8.isEmpty();
		
		if (!vendors) {	
		
		testCase.addQueryEvidenceCurrentStep(result8);	
		
				} 
		
		System.out.println(vendors);

assertFalse(vendors, "Los registros no fueron actualizados correctamente");


	}


	
	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Actualizar los registros de proveedores en RETEK";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
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




	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

}
	