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

public class ATC_FT_PO10_001_ValidaError extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PO10_001_ValidaError_test(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		
		SQLUtil dbFci = new SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
						//new SQLUtil(GlobalVariables.DB_HOST_FCIASSIT, GlobalVariables.DB_USER_FCIASSIT, GlobalVariables.DB_PASSWORD_FCIASSIT);
		
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		
/**
* Variables ******************************************************************************************
* 
* 
*/			
		String tdcQueryInfo = "SELECT ID, PLAZA, TIENDA, SERVICIO, COMISION, VALOR, WM_STATUS"  
				+ " FROM XXPPRO.XXFC_SERV_CANCELADOS_TEMP"
				+ " WHERE plaza= '" + data.get("plaza") +"'"
				+ " AND tienda = '" + data.get("tienda") +"'"; 
		
		String tdcQueryDetail = "SELECT POS_CONFIG_TYPE, POS_CONFIG_ID, BARCODE, PROMOTION, STORE"
				+ " FROM RMS100.FEM_POS_CFG_DETAIL"
				+ " WHERE POS_CONFIG_TYPE = 'TTYP' "
				+ " AND POS_CONFIG_ID = 2"
				+ " AND STORE = " + data.get("store") +"";
		
		String tdcQueryIntegrationServer = "SELECT * FROM (SELECT run_id, start_dt, status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PO10'"
				+ " AND start_dt >= TRUNC(SYSDATE)"
			    + " ORDER BY start_dt desc)"
				+ " WHERE ROWNUM = 1";
		
		String tdcQueryErrorId = "SELECT ERROR_ID, RUN_ID, ERROR_DATE, DESCRIPTION"
				+ " FROM WMLOG.WM_LOG_ERROR"
				+ " WHERE RUN_ID = %s"
				+ " AND ROWNUM = 1"; 
				
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//															PASO 1	
		
		addStep("Tener información pendiente de procesar de la plaza " + data.get("plaza") +" y tienda " + data.get("tienda2") +" en la tabla:  XXFC_SERV_CANCELADOS_TEMP de SINERGIA.");
		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		System.out.println(tdcQueryInfo);
		
		SQLResult infoResult = executeQuery(dbFci, tdcQueryInfo);
					
		boolean info = infoResult.isEmpty();		
		if (!info) {			
			testCase.addQueryEvidenceCurrentStep(infoResult);
		}
		
		System.out.println(info);
		
		assertFalse(info, "No se obtiene información de la consulta");
		
		
//															PASO 2 
		
		addStep("Validar que no existe informacion de la cuenta para la Plaza: " + data.get("plaza") +" y tienda: " + data.get("tienda2") +" en la tabla FEM_POS_CFG_DETAIL de RETEK");
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(tdcQueryDetail);
		
		SQLResult detailResult = executeQuery(dbRms, tdcQueryDetail);
					
		boolean detail = detailResult.isEmpty();		
		if (detail) {			
			testCase.addQueryEvidenceCurrentStep(detailResult);
		}
		
		System.out.println(detail);
		
		assertTrue(detail, "Se obtiene informacion de la consulta");
		
		
//															PASO 3
		
		addStep("Ejecutar la interfaz: PO10.Pub:run con el parámetro plaza =" + data.get("plaza") +".");
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
		
		pok.runIntefaceWmWithInput10(data.get("interfase"), data.get("servicio"), data.get("plaza"), "plaza");
		
		u.close();

//															PASO 4 
		
		addStep("Validar que la interfaz terminó con errores.");
		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		System.out.println(tdcQueryIntegrationServer);
		
		boolean valuesStatus = status1.equals(searchedStatus);	//Valida si se encuentra en estatus R
		while (valuesStatus) {			
			query = executeQuery(dbLog, tdcQueryIntegrationServer);	
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");
		 
			u.hardWait(2);		 
		}
		
		boolean successRun = status1.equals(status);//Valida si se encuentra en estatus S
		if(!successRun) {		   
			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult paso2 = executeQuery(dbLog, error);
		   
		   boolean emptyError = paso2.isEmpty();		   
		   if(!emptyError) {
			   testCase.addTextEvidenceCurrentStep("Se encontro un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
			   testCase.addQueryEvidenceCurrentStep(paso2);		   
		   }
		}
		    
		testCase.addQueryEvidenceCurrentStep(query);
		
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
		return "Validar que al no existir informacion de la cuenta, se muestre el error correspondiente.";
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
