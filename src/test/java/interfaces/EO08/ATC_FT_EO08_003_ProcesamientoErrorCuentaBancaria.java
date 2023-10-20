//PENDIENTE

package interfaces.EO08;

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
import utils.sql.SQLUtil;

public class ATC_FT_EO08_003_ProcesamientoErrorCuentaBancaria extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_EO08_003_ProcesamientoErrorCuentaBancaria_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utiler�as
		 *********************************************************************/

		utils.sql.SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbEbs1 = new  SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * ALM
		 * Verificar procesamiento, error al no poder validar la cuenta bancaria. plaza 10MON
		 * NOTA: Falta alggregar validacion FTP
		 * 
		 */
		
		/*
		 * Variables
		 *********************************************************************/

	
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String run_id;
		testCase.setTest_Description(data.get("descripcion"));

		String tdcQueryConfig = "SELECT operacion, valor1, valor2, valor3,"
				+ " DECODE (valor4, NULL, NULL,valor4) AS valor4, valor5 " 
				+ " FROM WMUSER.wm_interfase_config "
				+ " WHERE interfase = '" + data.get("interfase1") + "' "; // FCWM6QA

		String tdcQueryAppsBank = "SELECT * FROM APPS.ce_bank_accounts" 
		        + " WHERE BANK_ACCOUNT_ID = 10000 " // duda
				+ " AND end_date IS NULL";

		String tdcQueryIntegrationServer = " SELECT * FROM wmlog.wm_log_run " 
		       + " WHERE interface='"+ data.get("interfase1") + "'" 
		       + " And trunc(start_dt) >= trunc(sysdate)" 
		       + " And status = 'E'"
			   + " ORDER BY start_dt DESC"; // FCWMLQA

		String tdcQueryEPA_HEADER = "SELECT * FROM epa_header" 
		        + " WHERE wm_status_inserted = 'E'" 
				+ " AND crplaza = '"+ data.get("plaza") + "'"; // FCFINQA

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " 
		        + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID= %s"; // FCWMLQA

		/**
		 * **************************Pasos del caso de Prueba
		 *******************************************/

//	 	Paso 1	************************

		addStep("Validar que exitan datos en la configuracion WM_INTERFASE_CONFIG y en la tabla de CE_Bank");

		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryConfig);

		SQLResult configResult = executeQuery(dbPos, tdcQueryConfig);
		boolean config = configResult.isEmpty();
		System.out.println(config);

		if (!config) {

			testCase.addQueryEvidenceCurrentStep(configResult);

			System.out.println(GlobalVariables.DB_HOST_EBS);
			String queryAppBank = String.format(tdcQueryAppsBank);
			System.out.println(queryAppBank);

			SQLResult queryApps = executeQuery(dbEbs1, queryAppBank);
			boolean Apps1 = queryApps.isEmpty();
			System.out.println(Apps1);
			if (!Apps1) {
				testCase.addQueryEvidenceCurrentStep(queryApps);
			}
			assertFalse(Apps1, "No se obtiene informacion de la consulta del APPs");
		}

		System.out.println(config);

		assertFalse(config, "No se obtiene informacion de la consulta");

//      Paso 2  ************************

		// addStep("Verificar que existan archivos en directorio FTP para ser "
		// + "procesados, dirpath = AMEX, remoteFile = EPA_MEX_OXXO_yyyymmdd*");

//	 	Paso 3	************************

		addStep("Ejecutar el servicio EO08.Pub:run desde el Job EO08");
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

//	 	Paso 4	***********************

		addStep("Validar que la interfaz haya finalizado con error en la tabla en el WM_LOG_RUN.");

		System.out.println(tdcQueryIntegrationServer);
		SQLResult queryIntegrationServe = executeQuery(dbLog, tdcQueryIntegrationServer);
		run_id = queryIntegrationServe.getData(0, "RUN_ID");
		boolean query = queryIntegrationServe.isEmpty();

		if (!query) {

			testCase.addQueryEvidenceCurrentStep(queryIntegrationServe);
		}

		assertFalse(query, "No se obtiene informacion de la consulta");

//	 	Paso 5	************************

		addStep("Validar que la interfaz haya insertado el error en la tabla WM_LOG_ERROR");

		String queryErrorid = String.format(tdcQueryErrorId, run_id);
		System.out.println(queryErrorid);
		SQLResult paso4 = executeQuery(dbLog, queryErrorid);
		boolean thread = paso4.isEmpty();

		if (!thread) {

			testCase.addQueryEvidenceCurrentStep(paso4);
		}

		assertFalse(thread, "No se obtiene informacion de la consultas ");

//	 	Paso 6	************************

		addStep("Validar que el estatus del archivo procesado se actualice a E en la tabla EPA_HEADER.");

		System.out.println(GlobalVariables.DB_HOST_EBS);
		System.out.println(tdcQueryEPA_HEADER);
		SQLResult paso5 = executeQuery(dbEbs1, tdcQueryEPA_HEADER);

		boolean envio = paso5.isEmpty();

		if (!envio) {
			testCase.addQueryEvidenceCurrentStep(paso5);

		}

		System.out.println(envio);

		assertFalse(envio, "No se obtiene informacion de la consulta");

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
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_EO08_003_ProcesamientoErrorCuentaBancaria_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
