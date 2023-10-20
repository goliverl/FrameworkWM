package interfaces.gr13;

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

public class GR13ProcesoDeReintentos extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_gr13_ProcesoDeReintentos(HashMap<String, String> data) throws Exception {
		
		
/* Utilerías *********************************************************************/		
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);

			
/**
* Variables ******************************************************************************************
* 
* 
*/
		String tdcQueryxxfc = "SELECT CR_PLAZA, CR_TIENDA, PROVEEDOR, RETEK_TIENDA, VENDOR_ORDER_NO "
				+ " FROM XXFC.XXFC_LAST_COST_PO_GAS"
				+ " WHERE CR_PLAZA = '" + data.get("plaza") +"'"
				+ " AND CR_TIENDA = '" + data.get("tienda") +"'"
				+ " AND PROVEEDOR = " + data.get("proveedor") +""
				+ " AND RETEK_TIENDA = " + data.get("retek_tienda") +""
				+ " AND VENDOR_ORDER_NO = " + data.get("vendor_order") +"";
		
		String tdcQueryGas = "SELECT b.GID_ID, a.DOC_TYPE, a.STATUS, b.CR_PLAZA, b.CR_TIENDA, b.PROVEEDOR, b.RETEK_TIENDA, b.EXT_REF_NO, a.NB_SENT"
				+ " FROM posuser.GAS_INBOUND_DOCS a, posuser.GAS_BFC b "
				+ " WHERE a.ID = b.GID_ID"
				+ " AND a.DOC_TYPE = 'BFC'"
				+ " AND a.STATUS = 'R'"
				+ " AND b.CR_PLAZA = '" + data.get("plaza") +"'"
				+ " AND b.CR_TIENDA = '" + data.get("tienda") +"'"
				+ " AND b.PROVEEDOR = " + data.get("proveedor") +""
				+ " AND b.RETEK_TIENDA = " + data.get("retek_tienda") +""
				+ " AND b.EXT_REF_NO = " + data.get("vendor_order") +"";
		
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
		
		String tdcQuerySatusX = "SELECT ID, STATUS, DOC_TYPE FROM POSUSER.GAS_INBOUND_DOCS"
				+ " WHERE DOC_TYPE = 'BFC'"
				+ " AND STATUS = 'X'"
				+ " AND ID = %s";
		
		String status = "E";
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id ;
		
		testCase.setProject_Name("AutomationQA");
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
				
//		Paso 1	************************ 
		addStep("Validar que exista información pendiente por procesar en la tabla GAS_BFC y GAS_INBOUND_DOCS de POSUSER de tipo de documento DOC_TYPE = 'BFC' para la plaza.");
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(tdcQueryxxfc);
				
		SQLResult xxfcresult = executeQuery(dbRms, tdcQueryxxfc);
	
		boolean xxfc = xxfcresult.isEmpty();
				
			if (!xxfc) {
				
			testCase.addQueryEvidenceCurrentStep(xxfcresult);
					
								} 
				
		System.out.println(xxfc);

		assertFalse(xxfc, "No se obtiene informacion de la consulta");
		
//		Paso 2	************************ 
		addStep("Marcar el documento de las transacciones a STATUS = 'R', NB_SENT = 4, y validar que existan información pendiente por procesar en la tabla GAS_BFC y GAS_INBOUND_DOCS de POSUSER de tipo de documento DOC_TYPE = 'BFC' para la plaza.");
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryGas);
				
		SQLResult gasResult = executeQuery(dbPos, tdcQueryGas);
		String id = gasResult.getData(0, "GID_ID");
	
		boolean gas = gasResult.isEmpty();
				
			if (!gas) {
				
			testCase.addQueryEvidenceCurrentStep(gasResult);
					
								} 
				
		System.out.println(gas);

		assertFalse(gas, "No se obtiene informacion de la consulta");
		
//		Paso 3 ************************
		addStep("Ejecutar el servicio GR13.Pub:run para procesar la información de OXXO Gas a RMS.");
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		
//		Paso 4	************************
		addStep("Validar que se inserte el detalle de la ejecución de la interface GR13 en la tabla WM_LOG_RUN de WMLOG.");
		boolean valuesStatus = status1.equals(searchedStatus);//Valida si se encuentra en estatus R
		while (valuesStatus) {
			
			query = executeQuery(dbLog, tdcQueryIntegrationServer);	
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");
		 
		 u.hardWait(2);
		 
		}
		
		boolean successRun = status1.equals(status);//Valida si se encuentra en estatus S
		    if(!successRun){
		    	
		    	testCase.addQueryEvidenceCurrentStep(query);
		   
		    }
		    
		testCase.addQueryEvidenceCurrentStep(query);
		   
//		Paso 5 **************************
		addStep("Validar que se inserte el detalle del error generado durante la ejecución de la interface en la tabla WM_LOG_ERROR de WMLOG.");
		    	
		String error = String.format(tdcQueryErrorId, run_id);
		SQLResult paso2 = executeQuery(dbLog, error);
		boolean emptyError = paso2.isEmpty();
		   
		if(!emptyError){  
		   
		    testCase.addQueryEvidenceCurrentStep(paso2);
		   
		   }
		
		assertFalse(emptyError, "No se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
		
//		Paso 6 ***************************   
		addStep("Validar la actualización del estatus (STATUS = 'X') en la tabla GAS_INBOUND_DOCS de POSUSER para los documentos procesados.");
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String statusXFormat = String.format(tdcQuerySatusX, id);
		System.out.println(statusXFormat);
		
		SQLResult statusXresult = executeQuery(dbPos, statusXFormat);

		boolean statusX = statusXresult.isEmpty();
		
			if (!statusX) {
		
			testCase.addQueryEvidenceCurrentStep(statusXresult);
			
						} 
		
		System.out.println(statusX);

		assertFalse(statusX, "No se obtiene informacion de la consulta");
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
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
		return " Validar el proceso de reint, de procesam. de doctos (Status = R),(Status = X)";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_gr13_ProcesoDeReintentos";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
