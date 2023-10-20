package interfaces.po10;

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

public class ATC_FT_PO10_002_ValidaProcesamientoDeInfo extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PO10_002_ValidaProcesamientoDeInfo_test(HashMap<String, String> data) throws Exception {
		
/* Utilerias *********************************************************************/		
						//ORIGINAL
						//new SQLUtil(GlobalVariables.DB_HOST_FCIASQAS, GlobalVariables.DB_USER_FCIASQAS, GlobalVariables.DB_PASSWORD_FCIASQAS);	
		SQLUtil dbFci = new SQLUtil(GlobalVariables.DB_HOST_FCIASSIT, GlobalVariables.DB_USER_FCIASSIT, GlobalVariables.DB_PASSWORD_FCIASSIT); // NUCLEO
						
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA); // SIN CAMBIOS
		
						//ORIGINAL
						//new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables. DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil dbFce = new SQLUtil(GlobalVariables.DB_HOST_FCEBSSIT, GlobalVariables. DB_USER_FCEBSSIT, GlobalVariables.DB_PASSWORD_FCEBSSIT); // FCFINQA
						
/**		
* Variables ******************************************************************************************
* 
* 
*/			
		String tdcQueryInfo = "SELECT ID, PLAZA, TIENDA, SERVICIO, COMISION, VALOR, WM_STATUS, USUARIO"
				+ " FROM XXPPRO.XXFC_SERV_CANCELADOS_TEMP"
				+ " WHERE plaza = '" + data.get("plaza") +"'";
		
		String tdcQueryIntegrationServer = "SELECT * FROM (SELECT run_id, start_dt, status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PO10'"
				+ " AND  start_dt >= TRUNC(SYSDATE)"
			    + " ORDER BY start_dt desc)"
				+ " WHERE ROWNUM = 1";
		
		String tdcQueryErrorId =" SELECT ERROR_ID, RUN_ID, ERROR_DATE, DESCRIPTION"
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " WHERE RUN_ID = %s"
				+ " AND ROWNUM = 1"; 
		
		String tdcQueryStatusThread = "SELECT parent_id, thread_id, name, wm_log_thread.status, att1, att2"
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = %s"; 
		
		String tdcQueryGL = "SELECT STATUS, CURRENCY_CODE, SEGMENT1, SEGMENT2, SEGMENT3, ATTRIBUTE15, DATE_CREATED"
				+ " FROM GL.GL_INTERFACE"
				+ " WHERE segment3 = '" + data.get("plaza") +"'"
				+ " AND attribute15 = %s"
				+ " AND date_created >= TRUNC(sysdate)";				
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/		
		//									Paso 1 	
		
		addStep("Validar que se tiene informacion pendiente de procesar de la plaza " + 
				data.get("plaza") + " en la tabla:  XXFC_SERV_CANCELADOS_TEMP en SINERGIA.");
		
		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		System.out.println(tdcQueryInfo);
		
		SQLResult infoResult = executeQuery(dbFci, tdcQueryInfo);
		String servicio = null;	
		
		boolean info = infoResult.isEmpty();		
		if (!info) {
			servicio = infoResult.getData(0, "SERVICIO");			
		} else {
			testCase.addTextEvidenceCurrentStep("No existe registro en la tabla 'XXFC_SERV_CANCELADOS_TEMP' con plaza = '" + data.get("plaza") + "'");
		}
		
		System.out.println(info);
		testCase.addQueryEvidenceCurrentStep(infoResult);
		assertFalse(info, "No se obtiene informacion de la consulta");
		
		
		//									Paso 2 
		
		addStep("Ejecutar la interfaz: PO10.Pub:run con el parámetro plaza = " + data.get("plaza") +".");
		
		String status = "S";		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id = "";
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		
		u.get(contra);		
		pok.runIntefaceWmWithInput10(data.get("interfase"), data.get("servicio"), data.get("plaza"), "plaza");
		
		u.close();
		
		
		//									Paso 3
		
		addStep("Validar que la interfaz terminó sin errores.");
		
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
		
		boolean successRun = status1.equals(status); //Valida si se encuentra en estatus S		
		if(!successRun) {		
			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult paso2 = executeQuery(dbLog, error);
		   
			boolean emptyError = paso2.isEmpty();		   
			if(!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontro un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");		   
				testCase.addQueryEvidenceCurrentStep(paso2);		   
		   }
		}
		
		testCase.addQueryEvidenceCurrentStep(query);
		
		
		//									Paso 4 
		
	    addStep("Validar que se deposito la informacion correctamente en la tabla: GL_INTERFACE de ORAFIN.");
	    
	    System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		
		String depositoFormat = String.format(tdcQueryGL, servicio);
		System.out.println(depositoFormat);
		
		SQLResult depositoResult = dbFce.executeQuery(depositoFormat);		
		
		boolean deposito = depositoResult.isEmpty();		
		if (deposito) { // !			
			testCase.addTextEvidenceCurrentStep("No existe registro en 'GL_INTERFACE'");
		}
		
		System.out.println(deposito);
		
		testCase.addQueryEvidenceCurrentStep(depositoResult);
		 assertFalse(deposito, "No se obtiene información de la consulta");
		
		
		//									Paso 5 
		
		addStep("Validar que se eliminó correctamente la informacion de la tabla XXFC_SERV_CANCELADOS_TEMP");
		
		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		System.out.println(tdcQueryInfo);
		
		SQLResult eliminarResult = executeQuery(dbFci, tdcQueryInfo);
				
		boolean eliminar = eliminarResult.isEmpty();		
		if (!eliminar) {			
			testCase.addTextEvidenceCurrentStep("No se elimino correctamente la informacion.");			
		}
		
		System.out.println(eliminar);
		
		testCase.addQueryEvidenceCurrentStep(eliminarResult);
		assertTrue(eliminar, "No se eliminó correctamente la información");
	}

	@Override
	public void beforeTest() {
		
	}

	@Override
	public String setPrerequisites() {
		return null;
	}

	@Override
	public String setTestDescription() {
		return "Verificar que se procese la informacion en la tabla GL_INTERACE de ORAFIN";
	}

	@Override
	public String setTestDesigner() {
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		return null;
	}

	@Override
	public String setTestInstanceID() {
		return null;
	}	

}
