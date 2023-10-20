package interfaces.IO07;

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

public class IO07_ValidarElEnvioDeLasFacturasDeIMMEXDeLaPlaza extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_IO07_ValidarElEnvioDeLasFacturasDeIMMEXDeLaPlaza(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,
				GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,
				GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX,
				GlobalVariables.DB_PASSWORD_RMS_MEX);
		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */
		String Paso1 = " SELECT * FROM cfd_headers "
				+ " WHERE wm_status = 'L' AND "
				+ " cr_plaza = '" + data.get("plaza") + "' AND "
				+ " cr_tienda = '" + data.get("tienda") + "'";
				

		String Paso3 = " SELECT * FROM wm_log_run "
				+ " WHERE interface = 'IO07' AND "
				+ " status= 'S' AND "
				+ " start_dt >= TRUNC(SYSDATE) "
				+ " ORDER BY start_dt DESC";
			

		String Paso4 = "SELECT * FROM cfd_headers "
				+ " WHERE wm_status = 'E' AND "
				+ " cr_plaza = '" + data.get("plaza") + "' AND "
				+ " cr_tienda = '" + data.get("tienda") + "' AND "
				+ " convert(VARCHAR(10), wm_last_update, 103) = convert(varchar(10), getdate(), 103) ";
		
		String Paso5 = "SELECT * FROM xxfc_cfd_factura_digital "
				+ " WHERE TRUNC(creation_date) = TRUNC(SYSDATE) AND "
				+ " cr_plaza = '" + data.get("plaza") + "' AND "
				+ " cr_tienda = '" + data.get("plaza") + "' AND "
				+ " origen = 'IMXMM' ";
		
		String Paso6 = " SELECT * FROM xxfc_cfd_factura_digital_lines "
				+ " WHERE id_factura_digital = [xxfc_cfd_factura_digital.id_factura_digital] ";
				
				
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id;
		String status = "E";
		char id = 'A';
		testCase.setProject_Name("AutomationQA");

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 */

//			Paso 1	************************ 

		addStep("Validar que existan facturas pendientes para enviar para la Plaza y Tienda en IMMEX.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(Paso1);

		SQLResult xxfcresult = executeQuery(dbRms, Paso1);

		boolean xxfc = xxfcresult.isEmpty();

		if (!xxfc) {

			testCase.addQueryEvidenceCurrentStep(xxfcresult);

		}

		System.out.println(xxfc);

		assertFalse(xxfc, "No se obtiene informacion de la consulta");

//			Paso 2	************************ 

		addStep("Ejecutar el servicio IO07.Pub:run. El servicio será ejecutado con el job runIO_07 desde Ctrl-M.");

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

//			Paso 3 ************************

		addStep("Validar la correcta ejecución de la interface IO07 en la tabla WM WM_LOG_RUN de WMLOG.");

		SQLResult query = executeQuery(dbLog, Paso3);
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");

//			Paso 4	************************

		addStep("Validar la actualización de estado de los registros de las facturas "
				+ "enviadas a Oracle PO a WM_STATUS = 'E' en la tabla CFD_HEADERS de IMMEX.");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {

			query = executeQuery(dbLog, Paso4);
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			testCase.addQueryEvidenceCurrentStep(query);

		}

		testCase.addQueryEvidenceCurrentStep(query);

//			Paso 5 **************************

		addStep("Validar la inserción del Header de las facturas procesadas por la interface "
				+ "en la tabla XXFC_CFD_FACTURA_DIGITAL de ORAFIN.");

		String error = String.format(Paso5, run_id);
		SQLResult paso2 = executeQuery(dbLog, error);
		boolean emptyError = paso2.isEmpty();

		if (!emptyError) {

			testCase.addQueryEvidenceCurrentStep(paso2);

		}

		assertFalse(emptyError, "No se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

//			Paso 6 ***************************   

		addStep("Validar la actualización del estatus (STATUS = 'X') en la tabla GAS_INBOUND_DOCS de POSUSER para los documentos procesados.");
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String statusXFormat = String.format(Paso6, id);
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
		return ("El objetivo de la interface es transmitir hacia ORACLE PO los registros de facturas de los Sistemas Satélites "
				+ "IMMEX para que sean enviados hacia Buzón Fiscal.");
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return ("QA Automation");
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return ("ATC_FT_001_IO07_ValidarElEnvioDeLasFacturasDeIMMEXDeLaPlaza");
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
