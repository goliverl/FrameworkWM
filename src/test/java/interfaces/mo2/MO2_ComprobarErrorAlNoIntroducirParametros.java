package interfaces.mo2;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class MO2_ComprobarErrorAlNoIntroducirParametros extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_MO2_002_Comprobar_Error_No_Params(HashMap<String, String> data) throws Exception {

		
		/*
		 * Utilerías
		 ********************************************************************************************************************************************/
				   
	    
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);


		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 2
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'MO2'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'E'  order by start_dt desc";

			
//		Paso 3

	
		String ValidError = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION FROM WMLOG.WM_LOG_ERROR WHERE RUN_ID = '%s' ";


	
//****	

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='MO2' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************		
		
		addStep("Se ejecuta el proceso MO2.Pub:run.");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);


		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmWithDosInput(data.get("interfase"), data.get("servicio"),
				data.get("Periodo"),"periodo_id",
				data.get("Despacho"),"despacho_id");
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecución

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}
		

//*************************************************Paso 2***********************************************************************************************************************

		addStep("Comprobar que existe registro de la ejecucion  en la tabla WM_LOG_RUN de la BD WMLOG, donde INTERFACE es igual a 'MO2' y STATUS es igual a 'E'.");

		String RunID = "";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RunID = ExecLog.getData(0, "RUN_ID");

			testCase.addQueryEvidenceCurrentStep(ExecLog);

		}

		System.out.println(LogRequest);
		assertFalse(LogRequest, "No se muestra la información del log.");
		
		
////		********************Paso 3***************************************************************
			
		addStep("Verificar que se guardo el detalle del error en la tabla WM_LOG_ERROR de la "
				+ "BD WMLOG, donde RUN_ID es igual a 'wm_log_run.run_id' y ERROR_DATE igual a la fecha actual.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String ValidErrorFormat = String.format(ValidError, RunID);
		System.out.println(ValidErrorFormat);
		SQLResult ExecValidErrorFormat = dbLog.executeQuery(ValidErrorFormat);

		boolean ExecValidErrorFormatRequest = ExecValidErrorFormat.isEmpty();

		if (!ExecValidErrorFormatRequest) {


			testCase.addQueryEvidenceCurrentStep(ExecValidErrorFormat);

		}

		System.out.println(ExecValidErrorFormatRequest);
		assertFalse(ExecValidErrorFormatRequest, "No se muestra la información del log.");	

		
	}


	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_MO2_002_Comprobar_Error_No_Params";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Comprobar que se genere error al no introducir parametros de entrada.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
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
}



