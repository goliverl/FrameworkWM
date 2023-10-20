package interfaces.ps2;

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

public class Ps2_ValidarInsercionDeLaInformacionEnXXSE_Transaction extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PS2_ValidarInsercionDeLaInformacionEnXXSE_Transaction(HashMap<String, String> data) throws Exception {

		/*
		 * UtilerÃƒÆ’Ã‚Â­as
		 ***********************************************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil dbCNT = new SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA,
				GlobalVariables.DB_PASSWORD_FCIASQA);

		/**
		 * Variables
		 * ***********************************************************************************************
		 * 
		 * 
		 */

		String Paso1 = " SELECT * FROM POSUSER.pos_inbound_docs" 
		        + " WHERE doc_type = 'SIN'"
			//	+ " AND (status ='B' OR status = 'R')"
			    + " AND (status ='I')"
		        + " AND SUBSTR(pv_doc_name, 4, 5) = '" + data.get("plaza") + "'";
	

		String Paso2 = " SELECT * FROM POSUSER.pos_sin_detl" 
		        + " WHERE pid_id = 4470895207 " 
				+ " AND version = 3"
				+ " AND affected = 'N'";

		String Paso3 = " select * from  POSUSER.pos_inbound_docs" 
		        + " where status = 'I' " 
			    + " and id = %s";

		String Paso4 = " SELECT * FROM WMLOG.wm_log_run " 
		        + " WHERE TRUNC(start_dt) = TRUNC(SYSDATE) " 
				+ " And status = 'S' "
				+ " AND interface = 'PS2'" 
		        + " order by run_id desc";

		String Paso5 = " SELECT * FROM xxse_transaction_error" 
		        + " WHERE cr_plaza = %s"
				+ " and cr_tienda = %s" 
		        + " and bar_code = %s"; // dbLog

		String Paso6 = "SELECT id FROM pos_inbound_docs " 
		        + " WHERE doc_type = 'SIN' " + " AND status = 'E'"
				+ " AND SUBSTR(pv_doc_name, 4, 5) = '" + data.get("plaza") + "'" 
		        + " AND id = %s";

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 */

//					Paso 1	************************

		System.out.println("Paso 1");

		addStep("Tener documentos de tipo SIN con estatus B o R en la tabla POS_INBOUND_DOCS de POSUSER"
				+ " que ya hayan sido procesados anteriormente. ");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(Paso1);

		SQLResult Paso_1 = executeQuery(dbPos, Paso1);

		String id = null;


		boolean validar_Paso1 = Paso_1.isEmpty();

		if (!validar_Paso1) {
			
			id = Paso_1.getData(0, "ID");

			testCase.addQueryEvidenceCurrentStep(Paso_1);

		}

		System.out.println(validar_Paso1);
		//assertFalse(validar_Paso1, "No se tiene el documento de tipo SIN");

//                Paso 2   ************************

		System.out.println("Paso 2");

		addStep("Validar que en la tabla POS_SIN_DETL de POSUSER, "
				+ "los documentos que serán procesados tengan VERSION 3 y el campo AFFECTED con el valor N");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso2 = String.format(Paso2, id);
		System.out.println(FormatoPaso2);
		SQLResult Paso_2 = executeQuery(dbPos, FormatoPaso2);
		String Bar_code = Paso_2.getData(0, "BAR_CODE");
		boolean validar_Paso2 = Paso_2.isEmpty();

		if (!validar_Paso2) {

			testCase.addQueryEvidenceCurrentStep(Paso_2);

		}

		System.out.println(validar_Paso2);

		assertFalse(validar_Paso2, "No hay datos con la Version 3");

//      Paso 3 *********************************************

		System.out.println("Paso 3");

		addStep("Actualizar el estatus de los documentos a estatus I en la tabla POS_INBOUND_DOCS de POSUSER.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		String FormatoPaso3 = String.format(Paso3, id);
		System.out.println(FormatoPaso3);
		SQLResult Paso_3 = executeQuery(dbPos, FormatoPaso3);

		boolean validar_Passo3 = Paso_3.isEmpty();

		if (!validar_Passo3) {

			testCase.addQueryEvidenceCurrentStep(Paso_3);

		}

		System.out.println(validar_Passo3);

	//	assertFalse(validar_Passo3, "No hay datos con la Version 3");

//             Paso 4 *********************************************

		System.out.println("Paso 4");
		addStep("Validar que la interfaz se ejecutó correctamente en WMLOG.");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(Paso4);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
		String ATT1 = is.getData(0, "ATT1"); // ejecución
		String ATT2 = is.getData(0, "ATT2");
		String ATT4 = is.getData(0, "ATT4");

		String date2 = is.getData(0, "START_DT");
		String searchedStatus = "R";
		String status = "S";

		System.out.println(run_id);
		System.out.println(date2);
		System.out.println(status1);

		boolean valuesStatus = status1.equals(searchedStatus); // Valida si se
		// encuentra en
		// estatus R
		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en S

		System.out.println(status1);
		System.out.println(successRun);

		if (!successRun) {

			String error = String.format(Paso4, run_id);
			String error1 = String.format(Paso4, run_id);
			String error2 = String.format(Paso4, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorr);

			}

			SQLResult errorIS = dbLog.executeQuery(error1);

			boolean emptyError1 = errorIS.isEmpty();

			if (!emptyError1) {

				testCase.addQueryEvidenceCurrentStep(errorIS);

			}

			SQLResult errorIS2 = dbLog.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();

			if (!emptyError2) {

				testCase.addQueryEvidenceCurrentStep(errorIS2);

			}
		}

//                 Paso 5 ************************************************

		addStep("Validar que se depositó correctamente la información en la tabla XXSE_TRANSACTION_ERROR de SINERGIA.");

		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		successRun = status1.equals(status);
		assertFalse(!successRun, "La ejecución de la interfaz no fue exitosa");

		System.out.println(Paso5);

		String FormatoPAso5 = String.format(Paso5, ATT1, ATT2, Bar_code);
		SQLResult Paso_5 = executeQuery(dbCNT, FormatoPAso5);

		boolean validar_Paso5 = Paso_5.isEmpty();
		System.out.println(validar_Paso5 + "cea0kéjce");

		if (!validar_Paso5) {

			testCase.addQueryEvidenceCurrentStep(Paso_5);

		}

		System.out.println(validar_Paso5);

		assertFalse(validar_Paso5, "No se realizo el deposito");

//      Paso 6 ************************************************

		addStep("Validar que se actualizaron correctamente los registros de la tabla POS_INBOUND_DOCS a status E");

		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		System.out.println(Paso6);

		String Formato_6 = String.format(Paso6, ATT4);
		SQLResult Paso_6 = executeQuery(dbCNT, Formato_6);

		boolean validar_Paso6 = Paso_6.isEmpty();
		System.out.println(validar_Paso6 + "cea0kéjce");

		if (!validar_Paso6) {

			testCase.addQueryEvidenceCurrentStep(Paso_6);

		}

		System.out.println(validar_Paso6);

		assertFalse(validar_Paso5, "No se actualizo Correctamente");
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PS2_ValidarInsercionDeLaInformacionEnXXSE_Transaction";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. Validar que se deposite correctamente la informacion en la tabla XXSE_Transaccion de Sinergia.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION OXXO";
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
