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

public class ATC_FT_003_Ro1_MX_InsertarRegistroDeProveedores extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_RO1_001_Sincronizacion_usuarios(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/
		
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables. DB_PASSWORD_AVEBQA);
		
		
		String status = "S";
		
		
		
		testCase.setProject_Name("AutomationQA");
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
		
		String tdcQueryAddr = "SELECT ADDR_KEY, ADDR_TYPE, ADD_1, ADD_2, CONTACT_NAME, ORACLE_VENDOR_SITE_ID "
				+ "FROM ADDR "
				+ "WHERE ADDR_TYPE = 05 "
				+ "and ORACLE_VENDOR_SITE_ID = '"+ data.get("VendorID")+"'";		
		
		String tdcQuerySups = "SELECT SUPPLIER, SUP_NAME, CONTACT_NAME "
				+ "FROM SUPS "
				+ "WHERE CONTACT_NAME ='"+ data.get("CONTACTNAME")+"'"
				+ "and SUPPLIER ='"+ data.get("VendorID")+"'";

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = '" + data.get("wm_log") +"'"
				+" and  start_dt >= TRUNC(SYSDATE)"
			    +" order by start_dt desc)"
				+ " where rownum = 1";	
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; //FCWMLQA
		
		String tdcQueryContact = "SELECT ID, VENDORID, CONTACTNAME, TERMSNAME, WM_STATUS, CREATE_DATE, ADDR_TYPE "
				+ "FROM WMUSER.WM_VENDORS_SYNC "
				+ "WHERE WM_STATUS = 'E' "
				+ "AND CONTACTNAME ='"+ data.get("CONTACTNAME")+"'"
				+ "AND ID='"+data.get("ID")+"'";
		
		
		
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

//	Paso 3	************************

addStep("Comprobar que existan registros de los proveedores que se procesaran, en la tabla ADDR de la BD RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(tdcQueryAddr);
		
		SQLResult result3 = executeQuery(dbRms, tdcQueryAddr);
		
		
		boolean addr = result3.isEmpty();
		
			if (!addr) {	
				
				testCase.addQueryEvidenceCurrentStep(result3);	
				
						} 
		
		System.out.println(addr);

assertFalse(addr, "La tabla no contiene registros");
	

	    
//		Paso 4	************************

addStep("Comprobar que existan registros de los proveedores que se procesaran, en la tabla SUPS de la BD RETEK.");

	    System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
	    System.out.println(tdcQuerySups);
	    
	    SQLResult result4 = executeQuery(dbRms, tdcQuerySups);


	    boolean sups = result4.isEmpty();

	    	if (!sups) {	
	    		
	    		testCase.addQueryEvidenceCurrentStep(result4);	
	    		
	    				} 

	    System.out.println(sups);

assertFalse(sups, "La tabla no contiene registros");
	    
	    
//		Paso 5	************************

addStep("Ejecutar el servicio RO1.Pub:run.");

SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
PakageManagment pok = new PakageManagment(u, testCase);

String user = data.get("user");
String ps = PasswordUtil.decryptPassword( data.get("ps"));
String server = data.get("server");
//String con ="http://"+user+":"+ps+"@"+server;
String searchedStatus = "R";
String run_id ;

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
  
	    




//	Paso 6	************************

addStep("Confirmar que se actualizaron correctamente los proveedores en la tabla SUPS de la BD RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(tdcQuerySups);
		SQLResult result6 = executeQuery(dbRms, tdcQuerySups);
		
		
		boolean actualizarsups = result6.isEmpty();
		
			if (!actualizarsups) {	
				
				testCase.addQueryEvidenceCurrentStep(result6);
				
						} 
		
		System.out.println(actualizarsups);

assertFalse(actualizarsups, "Los proveedores no se actualizaron correctamente");


//		Paso 7	************************

addStep("Confirmar que se actualizaron los registros en la tabla ADDR de la BD RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(tdcQueryAddr);
		
		SQLResult result7 = executeQuery(dbRms, tdcQueryAddr);
		
		
		boolean actualizaraddr = result7.isEmpty();
		
		if (!actualizaraddr) {	
			
			testCase.addQueryEvidenceCurrentStep(result7);	
			
					} 
		
		System.out.println(actualizaraddr);

assertFalse(actualizaraddr, "Los registros no fueron actualizados correctamente");


//		Paso 8	************************

addStep("Comprobar que los registros fueron actualizados correctamente en la tabla WM_VENDORS_SYNC en la BD ORAFIN.");

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
		return "Terminada. Actualizar los registros de proveedores en RETEK";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_003_Ro1_MX_InsertarRegistroDeProveedores";
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
