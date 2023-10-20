//PASADO
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

public class ATC_FT_EO08_006_ProcesamientoSinArchivosFTP extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_EO08_006_ProcesamientoSinArchivosFTP_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/

		utils.sql.SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * ALM
		 * Verificar procesamiento de la interfaz sin archivos que obtener por FTP.
		 * NOTA: Falta el paso de validacion por FTP
		 * 
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
				+ " and trunc(start_dt) >= trunc(sysdate)" 
		        + " and status = 'E'"
				+ " ORDER BY start_dt DESC"; // FCWMLQA

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

		// addStep ("Verificar que existan archivos en directorio FTP para ser
		// procesados, dirpath = AMEX, remoteFile = EPA_MEX_OXXO_yyyymmdd");

//	 	Paso 3	************************

		addStep("Ejecutar el servicio EO08.Pub:run desde el Job EO08");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));

//	 	Paso 4	***********************

		addStep("Validar que la interfaz haya finalizado con error en la tabla en el WM_LOG_RUN.");

		System.out.println(tdcQueryIntegrationServer);
		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");

		System.out.println(status1);
		System.out.println(run_id);

//	 	Paso 5	************************

		addStep("Validar que la interfaz haya insertado el error en la tabla WM_LOG_ERROR.");

		String queryErrorId = String.format(tdcQueryErrorId, run_id);
		System.out.println(queryErrorId);
		SQLResult paso5 = executeQuery(dbLog, queryErrorId);
		boolean error = paso5.isEmpty();

		if (!error) {

			testCase.addQueryEvidenceCurrentStep(paso5);
		}

		assertFalse(error, "No se obtiene informacion de la consultas ");

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
		return "Verificar procesamiento de la interfaz sin archivos que obtener por FTP.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_EO08_006_ProcesamientoSinArchivosFTP_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
