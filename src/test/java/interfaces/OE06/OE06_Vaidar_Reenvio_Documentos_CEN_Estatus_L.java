package interfaces.OE06;

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

public class OE06_Vaidar_Reenvio_Documentos_CEN_Estatus_L extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LOG,
				GlobalVariables.DB_USER_LOG, GlobalVariables.DB_PASSWORD_LOG);
		utils.sql.SQLUtil dbAvebqa = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,
				GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		
		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		String tdcQueryPaso1 = "SELECT doc_name,header_id \r\n"
				+ " FROM wm_pagoservicios_finanzas \r\n"
				+ " WHERE status = 'L' \r\n"
				+ " AND PACKAGE = 'OE06' \r\n";
				
		String tdcQueryPaso4 = "SELECT * FROM wm_log_run \r\n"
				+ " WHERE interface = 'OE06' \r\n"
				+ " and status= 'S' \r\n"
				+ " and start_dt >= trunc(sysdate) \r\n"
				+ " ORDER BY start_dt DESC \r\n";
				
		String tdcQueryPaso5 = "SELECT * FROM wm_pagoservicios_finanzas \r\n"
				+ " WHERE status = 'E' \r\n"
				+ " AND run_id_sent = %s \r\n"
				+ " AND TRUNC(sent_date) = TRUNC(SYSDATE) \r\n"
				+ " AND package = 'OE06' \r\n";
				
//utileria

		/**
		 * Script de Pasos
		 * ******************************************************************************************
		 * 
		 * 
		 */

//paso 1   **************************

		addStep(" Validar que existan documentos pendientes de envío: ");

		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(tdcQueryPaso1);
		SQLResult Paso1 = executeQuery(dbAvebqa, tdcQueryPaso1);
		boolean ValidaPaso1 = Paso1.isEmpty();
		if (!ValidaPaso1) {

			testCase.addQueryEvidenceCurrentStep(Paso1);

		}

		assertFalse(ValidaPaso1, "No se encuentran resultados");

//paso 2  **************************++

		addStep(" Validar que el archivo [wm_pagoservicios_finanzas.doc_name] se encuentre en el fileSystem [config/workingpath].\r\n");

//paso 3    ************************

		addStep("Ejecutar el servicio: OE06.Pub:runPayServices. El servicio será ejecutado por el job runOE06 de Ctrl-M.\r\n");

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server + ":5555";

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		u.get(con);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

//Paso 4	************************

		addStep("Validar el registro de ejecución de la interfaz en estatus 'S'.\r\n");

		System.out.println(GlobalVariables.DB_HOST_LOG);
		System.out.println(tdcQueryPaso4);
		SQLResult Paso4 = executeQuery(dbLog, tdcQueryPaso4);
		boolean ValidaPaso4 = Paso4.isEmpty();
		
		String runid = "";
		
		if (!ValidaPaso4) {
			
			runid = Paso4.getData(0, "PARENT_ID");
			testCase.addQueryEvidenceCurrentStep(Paso4);

		}

		assertFalse(ValidaPaso4, "No se encuentran resultados");

//Paso 4_1	************************

		addStep("Validar el lanzamiento de threads para el procesamiento de los headers.\\r\\n");

		System.out.println(GlobalVariables.DB_HOST_LOG);
		
		String FormatPaso4_1  = String.format(runid, tdcQueryPaso4);
		
		System.out.println(FormatPaso4_1);
		SQLResult Paso4_1 = executeQuery(dbLog, FormatPaso4_1);
		boolean ValidaPaso4_1 = Paso4_1.isEmpty();
		
		String Parentid = "";
		
		if (!ValidaPaso4_1) {
			
            Parentid = Paso4_1.getData(0, "PARENT_ID");
			testCase.addQueryEvidenceCurrentStep(Paso4_1);

		}

		assertFalse(ValidaPaso4_1, "No se encuentran resultados");

//Paso 5	************************

		addStep("Validar la actualización del registro del documento en la tabla wm_pagoservicios_finanzas de ORAFIN.");

		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		
		String FormatoPaso5 = String.format(Parentid, tdcQueryPaso5);
		
		System.out.println(FormatoPaso5);
		SQLResult Paso5 = executeQuery(dbAvebqa, FormatoPaso5);
		boolean ValidaPaso5 = Paso5.isEmpty();
		if (!ValidaPaso5) {

			testCase.addQueryEvidenceCurrentStep(Paso5);

		}

		assertFalse(ValidaPaso5, "No se encuentran resultados");

//Paso 6	************************

		addStep("Validar que el archivo [wm_pagoservicios_finanzas.doc_name] sea eliminado del fileSystem [config/workingPath].\r\n");

//Paso 7	************************

		addStep("Validar que el archivo [wm_pagoservicios_finanzas.doc_name] sea enviado al fileSystem del servidor FTP [config/pathAdmin].\r\n");

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return " Validar el reenio de los documentos CEN con estatus L ";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Conciliacion de todas las transacciones (Exitosas y Canceladas) y garantizar que no existan transacciones a las "
				+ "cuales se le deba dar algún tipo de gestión o aclaración";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAutomation";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

}
