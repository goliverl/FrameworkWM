///No genera hilos, pero el ALM menciona hilos.
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

public class ATC_FT_EO08_002_ProcesamientoErrorObtenerJournal_Id extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_EO08_002_ProcesamientoErrorObtenerJournal_Id_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/

		utils.sql.SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * ALM
		 * Verificar procesamiento, error al no poder obtener el JOURNAL_ID. plaza 10MON
		 * NOTA:Falta paso 2 y 3
		 */
		
		/*
		 * Variables
		 *********************************************************************/

		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String run_id;

		String tdcQueryConfig = "SELECT operacion, valor1, valor2, valor3,"
				+ " DECODE (valor4, NULL, NULL,valor4) AS valor4, valor5 " 
				+ " FROM WMUSER.wm_interfase_config "
				+ " WHERE interfase = '" + data.get("interfase1") + "' "; // FCWM6QA

		String tdcQueryIntegrationServer = " SELECT * FROM wmlog.wm_log_run " 
		        + " WHERE interface='"+ data.get("interfase1") + "'"
				+ " And trunc(start_dt) >= trunc(sysdate)" 
		        + " And status = 'E'"
				+ " ORDER BY start_dt DESC"; // FCWMLQA

		String tdcQueryStatusThread = "SELECT * FROM wmlog.wm_log_thread " 
		        + " WHERE PARENT_ID = %s "
				+ " ORDER BY start_dt DESC "; // FCWMLQA

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " 
		        + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA

		/**
		 * **************************Pasos del caso de Prueba
		 *******************************************/

//	 	Paso 1	************************

		addStep("Validar que exitan datos en la configuracion WM_INTERFASE_CONFIG");

		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryConfig);

		SQLResult configResult = executeQuery(dbPos, tdcQueryConfig);
		boolean config = configResult.isEmpty();
		System.out.println(config);

		if (!config) {

			testCase.addQueryEvidenceCurrentStep(configResult);

		}

		System.out.println(config);

		assertFalse(config, "No se obtiene informacion de la consulta");

//	 	Paso 2	************************

		// addStep("Verificar que existan archivos en directorio FTP para ser
		// procesados, "
		// + "dirpath = AMEX, remoteFile = EPA_MEX_OXXO_yyyymmdd*");

//	 	Paso 3	************************

		// addStep("Generar un error al obtener el valor de la secuencia "
		// + "(deshabilitar DBS_WMINT_NT o generar error en
		// EO08.DB.WMINT:wrpGetJournalReference).");

//	 	Paso 4	************************

		addStep("Ejecutar el servicio EO08.Pub:run");
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));

//	 	Paso 5	***********************

		addStep("Validar que la interfaz haya finalizado con error en la tabla en el WM_LOG_RUN.");

		System.out.println(tdcQueryIntegrationServer);
		SQLResult queryIntegrationServer = executeQuery(dbLog, tdcQueryIntegrationServer);
		run_id = queryIntegrationServer.getData(0, "RUN_ID");
		boolean config1 = queryIntegrationServer.isEmpty();

		System.out.println(config1);
		System.out.println(run_id);

		if (!config1) {

			testCase.addQueryEvidenceCurrentStep(queryIntegrationServer);
		}

		assertFalse(config1, "No se obtiene informacion de la consultas ");

//	 	Paso 6	************************

		addStep("Validar que los threads hayan finalizado con error en la tabla en el WM_LOG_THREAD");

		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);
		SQLResult paso6 = executeQuery(dbLog, queryStatusThread);
		boolean thread = paso6.isEmpty();

		if (!thread) {

			testCase.addQueryEvidenceCurrentStep(paso6);
		}

		assertFalse(thread, "No se obtiene informacion de la consultas ");

//	 	Paso 7	************************

		addStep("Validar que la interfaz haya insertado el error en la tabla WM_LOG_ERROR.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String queryErrorID = String.format(tdcQueryErrorId, run_id);
		System.out.println(queryErrorID);
		SQLResult paso7 = executeQuery(dbLog, queryErrorID);

		boolean error = paso7.isEmpty();

		if (!error) {

			testCase.addQueryEvidenceCurrentStep(paso7);
		}

		System.out.println(error);

		assertFalse(error, "No se obtiene informacion de la consulta");

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
		return "Verificar procesamiento, error al no poder obtener el JOURNAL_ID con la plaza.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_EO08_002_ProcesamientoErrorObtenerJournal_Id_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
