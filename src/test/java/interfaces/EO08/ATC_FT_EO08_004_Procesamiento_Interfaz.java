//No se tiene FTP
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

public class ATC_FT_EO08_004_Procesamiento_Interfaz extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_EO08_004_Procesamiento_Interfaz_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/

		utils.sql.SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbEbs =  new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * ALM
		 * Verificar procesamiento de la interfaz
		 * NOTA: Faltan validaciones de FTP
		 * 
		 */
		
		/*
		 * Variables
		 *********************************************************************/

		String status = "S";
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id = null;
		String gl_journal_id;
		String status1 = null;

		String tdcQueryConfig = "SELECT operacion, valor1, valor2, valor3,"
				+ " DECODE (valor4, NULL, NULL,valor4) AS valor4, valor5 " 
				+ " FROM WMUSER.wm_interfase_config "
				+ " WHERE interfase = '" + data.get("interfase1") + "' "; // FCWM6QA

		String tdcQueryIntegrationServer = " SELECT * FROM wmlog.wm_log_run " 
		        + " WHERE interface='"+ data.get("interfase1") + "'" 
				+ " And trunc(start_dt) >= trunc(sysdate)" 
		        + " And status = 'S'"
				+ " ORDER BY start_dt DESC"; // FCWMLQA

		String tdcQueryStatusThread = "SELECT * FROM wmlog.wm_log_thread " 
		        + " WHERE PARENT_ID = %s "
				+ " ORDER BY start_dt DESC "; // FCWMLQA

		String tdcQueryEPA_HEADER = "SELECT * FROM EPA_HEADER" 
		        + " WHERE RUNID = %s "
				+ " AND TRUNC(CREATION_DATE) >= TRUNC(SYSDATE)" 
		        + " AND WM_STATUS_INSERTED = 'P'"
				+ " AND WM_STATUS_SENT = 'P'" 
		        + " AND DOC_NAME_SENT IS NOT NULL"; // FCFINQA

		String tdcQueryGL_INTERFACE = " SELECT * from GL_INTERFACE  " 
		        + " WHERE reference6 = %s " 
				+ " AND status='NEW' "
				+ " AND TRUNC(date_created) = TRUNC(sysdate)" 
				+ " AND segment3 = '" + data.get("plaza") + "'";

		String tdcQueryxxfcglinterface = "SELECT * FROM xxfc_gl_interface "
				+ "WHERE xxfc_estatus = 'S'"
				+ " AND segment3 = '" + data.get("plaza") + "'" 
				+ " AND TRUNC(date_created) >= TRUNC(SYSDATE)"
				+ " AND reference6 = s%"; // FCFINQA

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
		// procesados, dirpath = AMEX, remoteFile = EPA_MEX_OXXO_yyyymmdd");

//	 	Paso 3	************************

		addStep("Ejecutar el servicio EO08.Pub:run  desde el Job EO08");

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));

		System.out.println(tdcQueryIntegrationServer);
		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);

		boolean queryIntegration = query.isEmpty();

		if (!queryIntegration) {

			testCase.addQueryEvidenceCurrentStep(query);

			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");

			System.out.println(status1);
			System.out.println(run_id);
		}

		assertFalse(queryIntegration, "No se obtiene informacion de la consultas ");

//	 	Paso 4	***********************

		addStep("Validar que la interfaz haya finalizado correctamente en el WMLOG.");

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
						"Se encontr� un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(paso3);

			}
		}

//	 	Paso 4	************************

		addStep("Validar que se inserte el detalle de la ejecuci�n de los Threads lanzados por la ejecuci�n de la interface en la tabla WM_LOG_THREAD de WMLOG.");

		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);
		SQLResult paso4 = executeQuery(dbLog, queryStatusThread);
		boolean thread = paso4.isEmpty();

		if (!thread) {

			testCase.addQueryEvidenceCurrentStep(paso4);
		}

		assertFalse(thread, "No se obtiene informacion de la consultas ");

//	 	Paso 5	************************

		addStep("Verificar la actualizaci�n de los estatus en la tabla EPA_HEADER, wm_status_inserted=P y wm_status_sent=P.");

		
		String queryEPA_HEADER = String.format(tdcQueryEPA_HEADER, run_id);
		System.out.println(queryEPA_HEADER);
		SQLResult paso5 = executeQuery(dbEbs, queryEPA_HEADER);
		gl_journal_id = paso5.getData(0, "gl_journal_id");

		boolean envio = paso5.isEmpty();

		if (!envio) {

			testCase.addQueryEvidenceCurrentStep(paso5);

		}

		System.out.println(envio);

		assertFalse(envio, "No se obtiene informacion de la consulta");

//	 	Paso 6	************************

		addStep("Verificar la inserci�n de p�lizas contables en la tabla GL_INTERFACE");

		
		String queryGL_INTERFACE = String.format(tdcQueryGL_INTERFACE, gl_journal_id);
		System.out.println(queryGL_INTERFACE);
		SQLResult paso6 = executeQuery(dbEbs, queryGL_INTERFACE);

		boolean envio1 = paso6.isEmpty();

		if (!envio1) {

			testCase.addQueryEvidenceCurrentStep(paso6);

		}

		System.out.println(envio1);

		assertFalse(envio1, "No se obtiene informacion de la consulta");

//	 	Paso 7	************************	

		addStep("Verificar la actualizaci�n de los datos en la tabla XXFC_GL_INTERFACE, xxfc_estatus = S");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		String queryxxfcglinterface = String.format(tdcQueryxxfcglinterface, gl_journal_id);
		System.out.println(queryxxfcglinterface);
		SQLResult paso7 = executeQuery(dbEbs, queryxxfcglinterface);

		boolean envio2 = paso7.isEmpty();

		if (!envio2) {

			testCase.addQueryEvidenceCurrentStep(paso7);

		}

		System.out.println(envio2);

		assertFalse(envio2, "No se obtiene informacion de la consulta");

//      Paso 8 **************************

		// addStep("Verificar el documento EPA en la entrada de la interfaz OL11-14");
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
		return "Verificar procesamiento de la interfaz";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_EO08_004_Procesamiento_Interfaz_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
