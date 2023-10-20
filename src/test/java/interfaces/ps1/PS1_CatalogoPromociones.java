package interfaces.ps1;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
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

public class PS1_CatalogoPromociones extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_PS1_001_Valida_deposito_de_info_en_buzon(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
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

		String plazaActiva = "SELECT id, cr_plaza, fch_arranque,sinergia, cedis, pais \r\n "
				+ "FROM POSUSER.PLAZAS \r\n"
				+ "WHERE SINERGIA = 'Y' \r\n "
				+ "AND CR_PLAZA = '" + data.get("plaza") + "'";

		String infoProcesar = "SELECT cr_plaza, emission_id, emission_start_date, emission_end_date, promotion_id \r\n "
				+ "FROM xxpemp.XXSE_POS_SUMMARY_V \r\n"
				+ "WHERE CR_PLAZA='" + data.get("plaza") + "'";

		// consultas de error
		String consultaError1 = "select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE \r\n"
				+ "from wmlog.WM_LOG_ERROR \r\n" + 
				"where RUN_ID='%s') \r\n"
				+ "where rownum <=1"; // dbLog
		String consultaError2 = " select * from (select description,MESSAGE \r\n" 
				+ "from wmlog.WM_LOG_ERROR \r\n"
				+ "where RUN_ID='%s') \r\n"
				+ "WHERE rownum <= 1"; // dbLog
		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 \r\n"
				+ "from wmlog.WM_LOG_ERROR \r\n" 
				+ "where RUN_ID='%s')\r\n"
				+ "WHERE rownum <= 1"; // dbLog

		String tdcIntegrationServerFormat = "SELECT run_id, interface, start_dt, end_dt, status, server \r\n"
				+ "FROM wmlog.wm_log_run \r\n" + 
				"WHERE interface = 'PS1' \r\n" + 
				"AND start_dt>=TRUNC(SYSDATE) \r\n"
				+ "AND  ROWNUM <= 1 \r\n"
				+ "AND STATUS = 'S' \r\n" + 
				"ORDER BY start_dt DESC";
		
		
		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 */

//					Paso 1	************************

		addStep("Tener activa la plaza " + data.get("plaza") + " en la tabla PLAZAS de POSUSER. ");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(plazaActiva);

		SQLResult plazaActivaRes = executeQuery(dbPos, plazaActiva);

		boolean validaplazaActiva = plazaActivaRes.isEmpty();

		if (!validaplazaActiva) {

			testCase.addQueryEvidenceCurrentStep(plazaActivaRes);

		}

		System.out.println(validaplazaActiva);

		assertFalse(validaplazaActiva, "No se encontro la plaza  activa en POSUSER.");

//                Paso 2   ************************

		addStep("Tener información para procesar en la vista: XXSE_POS_SUMMARY_V de SINERGIA.");
		System.out.println(GlobalVariables.DB_USER_FCIASQA);
		System.out.println(infoProcesar);

		SQLResult infoProcesarRes = executeQuery(dbCNT, infoProcesar);

		boolean validainfoProcesar = infoProcesarRes.isEmpty();

		if (!validainfoProcesar) {

			testCase.addQueryEvidenceCurrentStep(infoProcesarRes);

		}

		System.out.println(validainfoProcesar);

		assertFalse(validainfoProcesar, "No Hay información para la plaza " + data.get("plaza") + " en SINERGIA ");

//             Paso 3 *********************************************
		addStep("Ejecutar la interfaz: PS1.pub:run. ");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
	

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWM2(data.get("interface"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecución
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
		
		
		System.out.println(successRun);

		
		
			if (!successRun) {

				String error = String.format(consultaError1, run_id);
				String error1 = String.format(consultaError2, run_id);
				String error2 = String.format(consultaError3, run_id);

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
			
	
		

//                 Paso 4 ************************************************

		addStep("Validar que la interfaz terminó sin errores en WMLOG ");
	
		
		successRun = status1.equals(status);
		assertFalse(!successRun, "La ejecución de la interfaz no fue exitosa");

		System.out.println(tdcIntegrationServerFormat);

		SQLResult tdcIntegrationServerFormatRes = executeQuery(dbLog, tdcIntegrationServerFormat);

		boolean validatdcIntegrationServerFormat = tdcIntegrationServerFormatRes.isEmpty();
		
		if (!validatdcIntegrationServerFormat) {

			testCase.addQueryEvidenceCurrentStep(tdcIntegrationServerFormatRes);

		}

		System.out.println(validatdcIntegrationServerFormat);

		assertFalse(validatdcIntegrationServerFormat, " La interfaz termina con errores y no procesa la información correctamente.");

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PS1_001_Valida_deposito_de_info_en_buzon";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. Validar que se deposite correctamente la información en el buzón de la plaza correspondiente ";
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
