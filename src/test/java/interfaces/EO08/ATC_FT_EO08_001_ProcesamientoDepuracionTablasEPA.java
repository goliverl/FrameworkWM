//PENDIENTE

package interfaces.EO08;

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
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_EO08_001_ProcesamientoDepuracionTablasEPA extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_EO08_001_ProcesamientoDepuracionTablasEPA_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/

		utils.sql.SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbEbs1 = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * ALM
		 * Verificar procesamiento de la interfaz, depuración de tablas EPA.
		 */
		
		/*
		 * Variables
		 *********************************************************************/

		String status = "S";
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id = null;
		String valor;
		String status1 = null;

		String tdcQueryConfig = "SELECT operacion, valor1, valor2, valor3,"
				+ " DECODE (valor4, NULL, NULL,valor4) AS valor4, valor5 " 
				+ " FROM WMUSER.wm_interfase_config "
				+ " WHERE interfase = '" + data.get("interfase1") +"'"; // FCWM6QA

		String tdcQueryInterfazConfig = "SELECT valor1 FROM WMUSER.wm_interfase_config "
				+ " WHERE operacion ='GET_DAYS' " 
				+ " AND interfase = '" + data.get("interfase1") + "'"; // FCWM6QA

		String tdcQueryEpaHeader_epa_record = "SELECT distinct id FROM epa_header a "
				+ " INNER JOIN epa_record b ON a.id = b.epa_id " 
				+ " Where wm_receive_date < (SYSDATE - %s)"; // FCFINQA

		String tdcQueryEpaHeader_EpaRoc = "SELECT distinct id FROM epa_header a "
				+ " INNER JOIN epa_roc b ON a.id = b.epa_id " 
				+ " Where wm_receive_date < (SYSDATE - %s) "; // FCFINQA

		String tdcQueryEpaHeader_adjrecord = "SELECT distinct id FROM epa_header a "
				+ " INNER JOIN epa_adjrecord b ON a.id = b.epa_id " 
				+ " Where wm_receive_date < (SYSDATE - %s)"; // FCFINQA

		String tdcQueryIntegrationServer = " SELECT * FROM wmlog.wm_log_run " 
		        + " WHERE interface='" +data.get("interfase1") + "'" 
		        + " And trunc(start_dt) >= trunc(sysdate)" 
				+ " And status = 'S'"
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

		addStep("Verificar que existan datos a procesar por la interfaz.");

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

		addStep("Obtener valor1 de la configuracion en wm_interfase_config.");

		
		System.out.println(tdcQueryInterfazConfig);
		SQLResult queryInterfazConfig = executeQuery(dbPos, tdcQueryInterfazConfig);
		valor = queryInterfazConfig.getData(0, "valor1");

		boolean config1 = queryInterfazConfig.isEmpty();
		System.out.println(config1);

		if (!config1) {

			// Se busca en el primer Script del segundo paso
			testCase.addQueryEvidenceCurrentStep(queryInterfazConfig);

			String queryEpaHeader = String.format(tdcQueryEpaHeader_epa_record, valor);
			System.out.println(queryEpaHeader);
			SQLResult executeEpaHeader = executeQuery(dbEbs1, queryEpaHeader);

			boolean interConfig = executeEpaHeader.isEmpty();

			System.out.println(interConfig);
			if (!interConfig) {

				testCase.addQueryEvidenceCurrentStep(executeEpaHeader);
				// Se busca en el primer Script del tercer paso

				System.out.println(GlobalVariables.DB_HOST_EBS);
				String queryEpaHeader_Eparoc = String.format(tdcQueryEpaHeader_EpaRoc, valor);
				System.out.println(queryEpaHeader_Eparoc);
				SQLResult executeEpaHeader_EpaRoc = executeQuery(dbEbs1, queryEpaHeader_Eparoc);

				boolean interConfig1 = executeEpaHeader_EpaRoc.isEmpty();

				System.out.println(interConfig1);
				if (!interConfig1) {

					testCase.addQueryEvidenceCurrentStep(executeEpaHeader_EpaRoc);

					String queryEpaHeader_adjrecord = String.format(tdcQueryEpaHeader_adjrecord, valor);
					System.out.println(queryEpaHeader_adjrecord);
					SQLResult executeEpaHeader_adjrecord = executeQuery(dbEbs1, queryEpaHeader_adjrecord);

					boolean interConfig2 = executeEpaHeader_adjrecord.isEmpty();
					System.out.println(interConfig2);

					if (!interConfig2) {

						testCase.addQueryEvidenceCurrentStep(executeEpaHeader_adjrecord);

					}
					assertFalse(interConfig2, "No se obtiene informacion de la consultas ");
				}

				assertFalse(interConfig1, "No se obtiene informacion de la consultas ");
			}

			assertFalse(interConfig, "No se obtiene informacion de la consultas ");

		}

		assertFalse(config1, "No se obtiene informacion de la consultas ");

//	 	Paso 2	************************

		addStep("Ejecutar el servicio EO08.Pub:run desde el Job EO08");
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

		System.out.println(tdcQueryIntegrationServer);
		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);
		boolean queryIntegration = query.isEmpty();

		if (!queryIntegration) {

			testCase.addTextEvidenceCurrentStep(tdcQueryIntegrationServer);
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");

			System.out.println(status1);
			System.out.println(run_id);
		}

		assertFalse(queryIntegration, "No se obtiene informacion de la consultas");

//	 	Paso 3	***********************

		addStep("Validar que el registro de la tabla WM_LOG_RUN termine en estatus 'S");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {

			query = executeQuery(dbLog, tdcQueryIntegrationServer);
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult paso3 = executeQuery(dbLog, error);

			boolean emptyError = paso3.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontro un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(paso3);

			}
		}

//	 	Paso 4	************************

		addStep("Validar que se inserte el detalle de la ejecucion de los "
				+ "Threads lanzados por la ejecucion de la interface en la tabla WM_LOG_THREAD de WMLOG.");

		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);
		SQLResult paso4 = executeQuery(dbLog, queryStatusThread);
		boolean thread = paso4.isEmpty();

		if (!thread) {

			testCase.addQueryEvidenceCurrentStep(paso4);
		}

		assertTrue(thread, "No se obtiene informacion de la consultas ");

//	 	Paso 5	************************

		addStep("Verificar que los datos de las tablas EPA_RECORD,EPA_ROC,EPA_ADJRECORD,EPA_HEADER fueron eliminados");

		System.out.println(GlobalVariables.DB_HOST_EBS);
		String queryEpa_Record = String.format(tdcQueryEpaHeader_epa_record, valor);
		System.out.println(queryEpa_Record);
		SQLResult paso5 = executeQuery(dbEbs1, queryEpa_Record);

		boolean envio = paso5.isEmpty();

		if (envio) {

			testCase.addQueryEvidenceCurrentStep(paso5);

			System.out.println(GlobalVariables.DB_HOST_EBS);
			String queryEpaRoc = String.format(tdcQueryEpaHeader_EpaRoc, valor);
			System.out.println(queryEpaRoc);
			SQLResult paso6 = executeQuery(dbEbs1, queryEpaRoc);
			boolean envio2 = paso6.isEmpty();

			if (envio2) {
				testCase.addQueryEvidenceCurrentStep(paso6);

				System.out.println(GlobalVariables.DB_HOST_EBS);
				String queryadjrecord = String.format(tdcQueryEpaHeader_adjrecord, valor);
				System.out.println(queryadjrecord);
				SQLResult paso7 = executeQuery(dbEbs1, queryadjrecord);
				boolean envio3 = paso7.isEmpty();

				if (envio3) {

					testCase.addQueryEvidenceCurrentStep(paso7);
				}

				assertFalse(envio3, "Se encontra datos en la tabla");

			}

			assertFalse(envio2, "Se encontra datos en la tabla");
		}

		System.out.println(envio);

		assertFalse(envio, "Se encontra datos en la tabla");
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
		return "Verificar procesamiento de la interfaz, depuraci�n de tablas EPA.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_EO08_001_ProcesamientoDepuracionTablasEPA_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
