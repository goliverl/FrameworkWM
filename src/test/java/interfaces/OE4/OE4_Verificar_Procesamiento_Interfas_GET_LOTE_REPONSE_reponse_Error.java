package interfaces.OE4;

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

public class OE4_Verificar_Procesamiento_Interfas_GET_LOTE_REPONSE_reponse_Error extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_OE4_001_Verificar_Interfaz_GET_LOTE_RESPONSE_Error(HashMap<String, String> data) throws Exception {

		/*
		 * 
		 * 
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Ebs, GlobalVariables.DB_USER_Ebs,GlobalVariables.DB_PASSWORD_Ebs);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		/*
		 * Variables
		 * *****************************************************************************
		 * *************
		 * 
		 * 
		 */ 
		
		String tdcQueryPaso1 = "SELECT * \r\n"
				+ " FROM WMUSER.XXFC_DOLLAR_CONFIG \r\n"
				+ " WHERE PROVEEDOR_ID = 001 \r\n"
				+ " AND ORACLE_CIA = '099' \r\n";

		String tdcQeryPaso1_1 = "SELECT * \r\n"
				+ " FROM WMUSER.XXFC_LOTES_DOLLARS \r\n"
				+ " WHERE STATUS_ENVIO = 'I' \r\n"
				+ " AND ORACLE_CIA = %s \r\n";
		
		String tdcQueryPaso4 = "SELECT * \r\n"
				+ " FROM wmlog.wm_log_run \r\n"
				+ " WHERE interface='OE4' \r\n"
				+ " and start_dt>=trunc(sysdate) \r\n"
				+ " and status = 'E' \r\n"
				+ " ORDER BY start_dt DESC \r\n";
				
		String tdcQueryPaso4_1 = " SELECT * \r\n"
				+ " FROM wm_log_error \r\n"
				+ " WHERE run_id=%s \r\n";
				
		String tdcQueryPaso5 = " SELECT * \r\n"
				+ " FROM xxfc_lotes_dollars \r\n"
				+ " WHERE status_envio = 'E' \r\n"
				+ " AND status_recepcion='R' \r\n"
				+ " AND fecha_recepcion>= TRUNC(SYSDATE) \r\n"
				+ " AND oracle_cia = %s \r\n";

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		testCase.setProject_Name("AutomationQA");

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 */

//		Paso 1	************************ 

		addStep("Verificar que existan datos pendientes de procesar en la BD de ORAFIN.");
		System.out.println(GlobalVariables.DB_HOST_Ebs);
		System.out.println(tdcQueryPaso1);

		SQLResult Paso1 = executeQuery(dbEbs, tdcQueryPaso1);

		String OraCia = Paso1.getData(0, "ORACLE_CIA");
		boolean validacionPaso1 = Paso1.isEmpty();
		System.out.println(validacionPaso1);
		if (!validacionPaso1) {

			testCase.addQueryEvidenceCurrentStep(Paso1);

		}
		assertFalse(validacionPaso1, "No se obtiene informacion de la consulta");

// Paso 1_1 ***************************

		addStep("Verificar que existan datos pendientes de procesar en la BD de ORAFIN.");
		System.out.println(GlobalVariables.DB_HOST_Ebs);
		String FormatoPaso2 = String.format(tdcQeryPaso1_1, OraCia);

		SQLResult Paso2 = executeQuery(dbEbs, FormatoPaso2);
		System.out.println(Paso2);

		boolean ValidacionPaso2 = Paso2.isEmpty();

		if (!ValidacionPaso2) {

			testCase.addQueryEvidenceCurrentStep(Paso2);

		}

		System.out.println(ValidacionPaso2);

		assertFalse(ValidacionPaso2, "No se obtiene informacion de la consulta");

// Paso 2 ****************************

		addStep("Validar que el archivo de respuesta contenga un codigo diferente de 000, para indicar que fue rechazado la transacción");

// Paso 3	************************

		
		addStep("Ejecutar el servicio OE4.pub:run, solicitando el job: runOE4.\r\n");
				
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));

// Paso 4	************************

		addStep("Validar que la interface finalice con error en las tablas del  WMLOG.\r\n");
				
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(tdcQueryPaso4);
		SQLResult paso4 = executeQuery(dbLog, tdcQueryPaso4);
		String thread = "";
		boolean ValidaPaso4 = paso4.isEmpty();
		if (!ValidaPaso4) {
			thread = paso4.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(paso4);

		}

		System.out.println(ValidaPaso4);

		assertFalse(ValidaPaso4, "No se obtiene informacion de la consulta");
		
		
//	Paso 4_1	***********************
		
		addStep("Validar que la interface finalice con error en las tablas del  WMLOG.\r\n");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		
		String FormatoPaso4 = String.format(tdcQueryPaso4_1, thread);
		SQLResult paso4_1= executeQuery(dbLog, FormatoPaso4);
		boolean ValidaPaso4_1 = paso4_1.isEmpty();
		if (!ValidaPaso4_1) {

			testCase.addQueryEvidenceCurrentStep(paso4_1);

		}

		System.out.println(ValidaPaso4_1);

		assertFalse(ValidaPaso4_1, "No se obtiene informacion de la consulta");

// Paso 5 ******************************

		addStep("Verificar la actualización de datos en la tabla XXFC_LOTES_DOLLARS en ORAFIN, STATUS_ENVIO = 'E',STATUS_RECEPCION=R.");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		
		String FormatoPAso5 = String.format(tdcQueryPaso5,tdcQueryPaso5 );
		
		System.out.println(FormatoPAso5);

		SQLResult Paso5 = executeQuery(dbEbs, FormatoPAso5);

		boolean ValidaPaso5 = Paso5.isEmpty();

		if (!ValidaPaso5) {

			testCase.addQueryEvidenceCurrentStep(Paso5);

		}

		System.out.println(ValidaPaso5);

		assertFalse(ValidaPaso5, "No se obtiene informacion de la consulta");
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
		return "Bloqueado. Verificar procesamiento de la interfaz, modulo GET_LOTE_RESPONSE, responseCode != 000.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_OE4_001_Verificar_Interfaz_GET_LOTE_RESPONSE_Error";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
