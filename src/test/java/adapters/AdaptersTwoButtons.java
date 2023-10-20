package adapters;

import static org.testng.Assert.assertTrue;
import java.util.ArrayList;
import java.util.HashMap;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class AdaptersTwoButtons extends BaseExecution {
	 
	SeleniumUtil u;

	@Test(dataProvider = "data-provider")
	public void test(HashMap<String, String> data) throws Exception {

		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA,
				GlobalVariables.DB_HOST_FCWMLQA);
 
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = '" + data.get("WMRun") + "'" + " and start_dt >= trunc(sysdate)"
				+ " order by start_dt desc)" + " where rownum = 1";

		String tdcQueryStatusLog = "SELECT run_id,interface,start_dt,status,server" + " FROM WMLOG.wm_log_run"
				+ " WHERE RUN_ID = %s";

		String tdcQueryError = "SELECT error_id,run_id,error_date,description,message FROM" + " WMLOG.wm_log_error"
				+ " WHERE run_id =  %s";

		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String status = "E";
		String run_id;

		testCase.setProject_Name("Autonomía Sistema OXXO Colombia");

		addStep("Apagar adapter");

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		boolean isOff = pok.switchAdapter(data.get("adapter"));

		assertTrue(isOff, "No se apago el adapter");

		ArrayList<String> tabs2 = new ArrayList<String>(u.getDriver().getWindowHandles());
		u.getDriver().close();
		u.getDriver().switchTo().window(tabs2.get(0));

		addStep("Ejecutar interfaz " + data.get("interface") + " " + data.get("servicio"));

		u.hardWait(4);

		pok.runIntefaceWmAdapter(data.get("interface"), data.get("servicio"), null);

		System.out.print(tdcQueryIntegrationServer);

		SQLResult resultQueryIS = dbLog.executeQuery(tdcQueryIntegrationServer);

		run_id = resultQueryIS.getData(0, "RUN_ID");// guarda el run id de la ejecución

		status = resultQueryIS.getData(0, "STATUS");

		boolean valuesStatus = status.equals(searchedStatus);// Valida si se encuentra en estatus R

		while (valuesStatus) {

			resultQueryIS = executeQuery(dbLog, tdcQueryIntegrationServer);

			run_id = resultQueryIS.getData(0, "RUN_ID");// guarda el run id de la ejecución

			status = resultQueryIS.getData(0, "STATUS");

			valuesStatus = status.equals(searchedStatus);

			u.hardWait(2);

		}

		addStep("Validar que el registro de ejecución de la interfaz termino en estatus 'E' en la tabla WM_LOG_RUN.");

        System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		
		String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
		
	    System.out.println(queryStatusLog);
	    
	    SQLResult resultStatusLog = executeQuery(dbLog, queryStatusLog);
		  
		String statusWmLog = resultStatusLog.getData(0, "STATUS");
	
		boolean validateStatus = status.equals(statusWmLog);
		
		System.out.println(validateStatus);
		
		if(validateStatus){	
	    	
		    String error = String.format(tdcQueryError, run_id);
		    
		    SQLResult resultError = executeQuery(dbLog, error);
		    
		    boolean emptyError = resultError.isEmpty();	
		    
		    String  message  = resultError.getData(0, "MESSAGE");
		    
		    if(!emptyError){  
		    	
		    	testCase.addQueryEvidenceCurrentStep(resultError);
		    	
		    	testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
		    	
		    	testCase.addTextEvidenceCurrentStep(message);
	    	
		    }
		}		
		
		assertTrue(validateStatus, "La ejecución de la interfaz terminó en un estatus diferente de E");

		pok.adapterOn(data.get("adapter"));

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Realizar el apagado de adapter y validar el registro de error en WM_LOG";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "EjecuciÃ³n de interface con adapter apagado";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@AfterMethod
	public void after() {           
	    u.close();
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}
	

}
