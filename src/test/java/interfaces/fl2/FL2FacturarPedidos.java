package interfaces.fl2;

import static org.testng.Assert.assertFalse;
import java.util.HashMap;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class FL2FacturarPedidos extends BaseExecution{
		
	@Test(dataProvider = "data-provider")
	public void ATC_FT_FL2_001_FacturarPedidos(HashMap<String, String> data) throws Exception {
		
/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbRdm = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RDM, GlobalVariables.DB_USER_RDM, GlobalVariables.DB_PASSWORD_RDM);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		SeleniumUtil u;		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id ;
		String status = "S";
		
		/**
		 * ALM
		 * Validar que la informacion de embarque se procese 
		 * Validar que la informacion de embarque se procese 
		 */
		
/** VARIABLES *********************************************************************/	
		
		String tdcQueryConnection = "SELECT RETEK_CR, RETEK_PHYSICAL_CR, ORACLE_CR, CONNECTION_NAME, CEDIS_DESC, TRANSACTION_TYPE, STATUS "
				+ " FROM POSUSER.WM_RDM_CONNECTIONS "
				+ " WHERE retek_physical_cr =  '" + data.get("cedis") +"'  "
				+ " AND status='A' "
				+ " AND transaction_type= 'NT'";
		
		String tdcQueryEmbarque = "SELECT CEDIS, CRPLAZA, TSF_NO, STATUS, GENERADO, RUTA, CR_TIENDA "
				+ " FROM RDM100.XXFC_POSTERIOR_EMBARQUE "
				+ " WHERE crplaza = '" + data.get("plaza") +"' "
				+ " AND wm_status = 'I'";
		
		String tdcQueryPreEmbarque = "SELECT CEDIS, CRPLAZA, TSF_NO, STATUS, GENERADO, RUTA, CR_TIENDA "
				+ " FROM RDM100.XXFC_PRE_POSTERIOR_EMBARQUE "
				+ " WHERE crplaza = '" + data.get("plaza") +"' "
				+ " AND wm_status = 'I'";
		
		String tdcQueryIntegrationServer2 = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface like 'FL2main'"
				+" and  start_dt > To_Date (SYSDATE, 'DD-MM-YYYY hh24:mi' )"
			    +" order by start_dt desc)"
				+ " where rownum = 1";
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID='%s'"; 
		
		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = '%s'" ; 
		
		String tdcQueryStatusEmbarque = "SELECT WM_STATUS, WM_RUN_ID, WM_SENT_DATE "
				+ " FROM XXFC_POSTERIOR_EMBARQUE "
				+ " WHERE crplaza = '" + data.get("plaza") +"' "
				+ " and WM_STATUS = 'E' "
				+ " AND WM_RUN_ID = '%s'";
		
		String tdcQueryStatusPre = "SELECT WM_STATUS, WM_RUN_ID, WM_SENT_DATE "
				+ " FROM XXFC_PRE_POSTERIOR_EMBARQUE "
				+ " WHERE crplaza = '" + data.get("plaza") +"' "
				+ " and WM_STATUS = 'E' "
				+ " AND WM_RUN_ID = '%s'";
		
		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
				
		
		/* PASO 1 *********************************************************************/	
				
		addStep("Validar que exista la informaci�n de la conexi�n a RDM.");
		
		System.out.println(tdcQueryConnection);		
		SQLResult connectionResult = executeQuery(dbPos, tdcQueryConnection);

		boolean connection = connectionResult.isEmpty();
		
		if (!connection) {				
			testCase.addQueryEvidenceCurrentStep(connectionResult);					
		} 

		System.out.println(connection);

		assertFalse(connection, "La tabla no contiene informaci�n.");
		
		
		
		/* PASO 2 *********************************************************************/	
				
		addStep("Validar que exista informaci�n pendiente de procesar para la plaza, en la conexi�n de RDM.");
		
		String servicio = "FL2.Pub:runNotification";		
		boolean embarqueServicio = data.get("servicio").equals(servicio);
		
		if (embarqueServicio) {			
			
			System.out.println(tdcQueryEmbarque);			
			SQLResult embarqueResult = executeQuery(dbRdm, tdcQueryEmbarque);			
			boolean embarque = embarqueResult.isEmpty();
			
			if (!embarque) {					
				testCase.addQueryEvidenceCurrentStep(embarqueResult);						
			} 

			System.out.println(embarque);
			assertFalse(embarque, "La tabla no contiene registros.");	
			
		} else {
			
			System.out.println(tdcQueryPreEmbarque);
			SQLResult preEmbarqueResult = executeQuery(dbRdm, tdcQueryPreEmbarque);
			
			boolean preEmbarque = preEmbarqueResult.isEmpty();
			
			if (!preEmbarque) {					
				testCase.addQueryEvidenceCurrentStep(preEmbarqueResult);						
			} 

			System.out.println(preEmbarque);
			
			assertFalse(preEmbarque, "La tabla no contiene registros.");
		}
		
		
				
		/* PASO 3 *********************************************************************/	
		
		addStep("Ejecutar el servicio " + data.get("servicio") +".");
		
		u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);		
		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));		
		System.out.println("Respuesta dateExecution " + dateExecution);
		
		SQLResult tdcIntegrationServerResult = dbLog.executeQuery(tdcQueryIntegrationServer2);
		
		
		
		/* PASO 4 *********************************************************************/	
				
		addStep("Validar que el archivo se envi� correctamente al terminar la interfaz FL2 sin errores.");

		run_id = tdcIntegrationServerResult.getData(0, "RUN_ID");//Guarda el run id de la ejecuci�n
		System.out.println("RUN_ID = " + run_id);
		boolean valuesStatus = tdcIntegrationServerResult.getData(0,"STATUS").equals(searchedStatus);//Valida si se encuentra en estatus R

		while (valuesStatus) {			
			tdcIntegrationServerResult = dbLog.executeQuery(tdcQueryIntegrationServer2);
			valuesStatus = tdcIntegrationServerResult.getData(0,"STATUS").equals(searchedStatus);//Valida si se encuentra en estatus R
			u.hardWait(2);
		}

		boolean successRun = tdcIntegrationServerResult.getData(0, "STATUS").equals(status);//Valida si se encuentra en estatus S

		if(!successRun){
			
			String error = String.format(tdcQueryErrorId, run_id);
			System.out.println(error);
			SQLResult er = dbLog.executeQuery(error);
			boolean emptyError = er.isEmpty();

			if(!emptyError){  				
				testCase.addTextEvidenceCurrentStep("Se encontr� un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(er);
			}
		}
		
		testCase.addQueryEvidenceCurrentStep(tdcIntegrationServerResult);
		
		
		
		/* PASO 5 *********************************************************************/	
				
		addStep("Validar que se actualizaron correctamente los campos WM_STATUS a 'E', WM_SENT_DATE a la fecha actual y  WM_RUN_ID de la tabla XXFC_POSTERIOR_EMBARQUE en RDM.");
		
		boolean servicioStatus = data.get("servicio").equals(servicio);
		
		if (servicioStatus) {
			
			String embarqueFormat = String.format(tdcQueryStatusEmbarque, run_id);			
			SQLResult embarqueStatusResult = executeQuery(dbRdm, embarqueFormat);
			System.out.println(embarqueFormat);
			
			boolean embarqueStatus = embarqueStatusResult.isEmpty();
			
			if (!embarqueStatus) {					
				testCase.addQueryEvidenceCurrentStep(embarqueStatusResult);					
			} 

			System.out.println(embarqueStatus);
			assertFalse(embarqueStatus, "La tabla no contiene registros.");	
			
		} else {
			
			String preFormat = String.format(tdcQueryStatusPre, run_id);
			
			SQLResult preResult = executeQuery(dbRdm, preFormat);
			System.out.println(preFormat);
			
			boolean preEmbarque = preResult.isEmpty();
			
			if (!preEmbarque) {					
				testCase.addQueryEvidenceCurrentStep(preResult);					
			} 

			System.out.println(preEmbarque);
			assertFalse(preEmbarque, "La tabla no contiene registros.");	
			
		}			
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
		return " Validar que la informaci�n de embarque se procese correc. para los CEDIS y las plazas. ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_FL2_001_FacturarPedidos";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
