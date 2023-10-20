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

public class GR13EnviarInformacionDocumentos extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_gr13_EnviarInformacionDocumentos(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);

		
		
		
/**
* Variables ******************************************************************************************
* 
* 
*/
		String tdcQueryGas = "SELECT a.ID, a.STATUS, b.CR_PLAZA, b.CR_TIENDA, a.DOC_TYPE"
				+ " FROM posuser.GAS_INBOUND_DOCS a, posuser.GAS_BFC b"
				+ " WHERE a.ID = b.GID_ID"
				+ " AND a.DOC_TYPE = 'BFC'"
				+ " AND a.STATUS IN ('I','R')"
				+ " AND b.CR_PLAZA = '" + data.get("plaza") +"'";
		
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
		
		String tdcQueryxxfc = "SELECT CR_PLAZA, CR_TIENDA, WM_RUNID "
				+ " FROM XXFC.XXFC_LAST_COST_PO_GAS"
				+ " WHERE CR_PLAZA = '" + data.get("plaza") +"'"
				+ " AND WM_RUNID = %s";
		
		String tdcQueryStatusP = "SELECT ID, STATUS, DOC_TYPE FROM POSUSER.GAS_INBOUND_DOCS"
				+ " WHERE DOC_TYPE = 'BFC'"
				+ " AND STATUS = 'P'"
				+ " AND ID = %s";
		
		String status = "S";
		
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
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryGas);
		
		SQLResult gasresult = executeQuery(dbPos, tdcQueryGas);
		String id = gasresult.getData(0, "ID");

		boolean gas = gasresult.isEmpty();
		
			if (!gas) {
		
			testCase.addQueryEvidenceCurrentStep(gasresult);
			
						} 
		
		System.out.println(gas);

		assertFalse(gas, "No se obtiene informacion de la consulta");
		
//		Paso 2	************************ 
		addStep("Ejecutar el servicio GR13.Pub:run para procesar la información de OXXO Gas a RMS.");
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		
//		Paso 3	************************
		addStep("Validar que el registro de la tabla WM_LOG_RUN termine en estatus 'S'.");
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
		   
		    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
		   
		    testCase.addQueryEvidenceCurrentStep(paso2);
		   
		   }
		}
		    testCase.addQueryEvidenceCurrentStep(query);

//		Paso 4 ********************************
		addStep("Validar la inserción de las transacciones de la plaza en la tabla XXFC.XXFC_LAST_COST_PO_GAS de RETEK.");
		
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String formatxxfc = String.format(tdcQueryxxfc, run_id);
		System.out.println(formatxxfc);
		
		SQLResult xxfcresult = executeQuery(dbRms, formatxxfc);

		boolean xxfc = xxfcresult.isEmpty();
		
			if (!xxfc) {
		
			testCase.addQueryEvidenceCurrentStep(xxfcresult);
			
						} 
		
		System.out.println(xxfc);

		assertFalse(xxfc, "No se obtiene informacion de la consulta");

//		Paso 5 ********************************
		addStep("Validar la actualización del estatus (STATUS = 'P'), en la tabla GAS_INBOUND_DOCS de POSUSER para los documentos procesados.");
		
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String statusPFormat = String.format(tdcQueryStatusP, id);
		System.out.println(statusPFormat);
		
		SQLResult statusPresult = executeQuery(dbPos, statusPFormat);

		boolean statusP = statusPresult.isEmpty();
		
			if (!statusP) {
		
			testCase.addQueryEvidenceCurrentStep(statusPresult);
			
						} 
		
		System.out.println(statusP);

		assertFalse(statusP, "No se obtiene informacion de la consulta");
		
		


		
		
		

		
		
		
		
		
		
		
		
		
		
		
		
		
		
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
		return " Enviar la información de los documentos BFC de OXXO GAS a RMS. ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_gr13_EnviarInformacionDocumentos";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}