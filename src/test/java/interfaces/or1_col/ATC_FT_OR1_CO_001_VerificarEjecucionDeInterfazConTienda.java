package interfaces.or1_col;

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

public class ATC_FT_OR1_CO_001_VerificarEjecucionDeInterfazConTienda extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_OR1_CO_001_VerificarEjecucionDeInterfazConTienda_test(HashMap<String, String> data) throws Exception {
		
/* Utiler�as *********************************************************************/		
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_Ebs, GlobalVariables.DB_USER_Ebs,GlobalVariables.DB_PASSWORD_Ebs);
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
/**
* Variables ******************************************************************************************
* 
* 
*/		
		String tdcQueryInventarios = "SELECT ASD_ITEM, ASD_ITEM_DESC, ASD_TRAN_DATE, ASD_CR_PLAZA, ASD_STORE, WM_STATUS, WM_RUN_ID"
				+ " FROM XXFC_AJUSTE_SALDOS_AUTOMATICOS"
				+ " WHERE WM_STATUS = 'L'"
				+ " AND ASD_CR_PLAZA = '"+ data.get("plaza") +"'"
				+ " and ASD_STORE IN (SELECT retek_cr "
				+ " FROM XXFC_CENTROS_RESPONSABILIDAD XCR "
				+ " WHERE XCR.ORACLE_CR_SUPERIOR = '"+ data.get("plaza") +"' "
				+ " AND ORACLE_CR = '"+ data.get("distrito") +"')";
		
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PO11'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum = 1";
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID= %s"
				+ " and rownum = 1"; 
		
		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " 
				+ " WHERE parent_id = %s "
				+ "AND ATT1='"+ data.get("plaza") +"'" ; // FCWMLQA
		
		String tdcQueryFem = "SELECT ITEM, ITEM_DESC, STORE, TRAN_DATE, CR_PLAZA"
				+ " FROM FEM_FIF_STG"
				+ " WHERE CR_PLAZA = '"+ data.get("plaza") +"'"
				+ " AND TRAN_DATE = '%s'"
				+ " AND ITEM = %s";
		
		String tdcQueryActualizacion = "SELECT ASD_ITEM, ASD_ITEM_DESC, ASD_TRAN_DATE, ASD_CR_PLAZA, ASD_STORE, WM_STATUS, WM_RUN_ID"
				+ " FROM XXFC_AJUSTE_SALDOS_AUTOMATICOS"
				+ " WHERE WM_STATUS = 'E'"
				+ " AND WM_RUN_ID = %s"
				+ " AND ASD_CR_PLAZA = '"+ data.get("plaza") +"'"
				+ " and ASD_STORE IN (select retek_cr from XXFC_CENTROS_RESPONSABILIDAD XCR "
				+ " where XCR.ORACLE_CR_SUPERIOR = '"+ data.get("plaza") +"' "
				+ " AND oracle_cr = '"+ data.get("distrito") +"')";
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************		
		addStep("Validar que existan informacion de ajustes de inventarios para la plaza "+ data.get("plaza") +" y distrito "+ data.get("distrito") +" pendiente por procesar en la tabla XXFC_AJUSTE_SALDOS_AUTOMATICOS de ORACLE AR.");
		
		System.out.println(GlobalVariables.DB_HOST_Ebs);
		System.out.println(tdcQueryInventarios);
		
		SQLResult inventariosResult = executeQuery(dbEbs, tdcQueryInventarios);
		String tran_date = inventariosResult.getData(0, "ASD_TRAN_DATE");
		String item = inventariosResult.getData(0, "ASD_ITEM");
		
		boolean inventarios = inventariosResult.isEmpty();
		
		if (!inventarios) {
			
			testCase.addQueryEvidenceCurrentStep(inventariosResult);
		}
		
		System.out.println(inventarios);
		
		assertFalse(inventarios, "No se obtiene informacion de la consulta");

		
//Paso 2 *************************		
		addStep("Ejecutar la interface OR1_col.Pub:run para enviar la informacion de los ajustes de inventarios de ORACLE AR a RETEK para la plaza "+ data.get("plaza") +" y distrito "+ data.get("distrito") +".");
		
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
		
//		pok.runIntefaceWmWithInput10(data.get("interfase"), data.get("servicio"), data.get("plaza"), "p_cr_plaza", data.get("distrito"), "p_cr_distrito");
		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));	
		
//Paso 3 **********************
		addStep("Validar la correcta ejecucion de la interface OR1_OG en la tabla WM_LOG_RUN de WMLOG.");
		
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
		   
		    testCase.addTextEvidenceCurrentStep("Se encontro un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");
		   
		    testCase.addQueryEvidenceCurrentStep(paso2);
		   
		   }
		}
		    testCase.addQueryEvidenceCurrentStep(query);	
		    
//Paso 4 *********************
		addStep("Validar la correcta ejecucion de los threads lanzados por la interface OR1_OG en la tabla WM_LOG_THREAD de WMLOG.");
		String threadFormat = String.format(tdcQueryStatusThread, run_id);
		SQLResult threadResult = executeQuery(dbLog, threadFormat);
		
		System.out.println(threadFormat);
		String estatusThread = threadResult.getData(0, "Status");
		String thread_id = threadResult.getData(0, "thread_id");

		boolean SR = estatusThread.equals(status);
		SR = !SR;
		
		if (!SR) {

			testCase.addQueryEvidenceCurrentStep(threadResult);
			
		} 

		System.out.println(SR);

		
		assertFalse(SR, "No se obtiene informacion de la consulta");
	
//Paso 5 ********************
		addStep("Validar la insercion de los ajustes de inventarios de la plaza "+ data.get("plaza") +" en la tabla FEM_FIF_STG de RETEK.");
		
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		
		String femFormat = String.format(tdcQueryFem, tran_date, item);
		SQLResult femResult = executeQuery(dbRms, femFormat);
		System.out.println(femFormat);
		
		
		boolean fem = femResult.isEmpty();
		
		if (!fem) {
			
			testCase.addQueryEvidenceCurrentStep(femResult);
		}
		
		System.out.println(fem);
		
		assertFalse(fem, "No se obtiene informaci�n de la consulta");
	
//Paso 6 *****************
		addStep("Validar la actualizacion del estatus (WM_STATUS='E'), en la tabla XXFC_AJUSTE_SALDOS_AUTOMATICOS de ORACLE AR para los ajustes de inventarios para la plaza "+ data.get("plaza") +".");
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		
		String actualizacionFormat = String.format(tdcQueryActualizacion, thread_id);
		SQLResult actualizacionResult = executeQuery(dbRms, actualizacionFormat);
		System.out.println(actualizacionFormat);
		
		
		boolean actualizacion = actualizacionResult.isEmpty();
		
		if (!actualizacion) {
			
			testCase.addQueryEvidenceCurrentStep(actualizacionResult);
		}
		
		System.out.println(actualizacion);
		
		assertFalse(actualizacion, "No se obtiene informacion de la consulta");
		
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
		return "Registrar en Retek RMS la informacion de ajustes de inventarios desde Oracle AR.";
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

