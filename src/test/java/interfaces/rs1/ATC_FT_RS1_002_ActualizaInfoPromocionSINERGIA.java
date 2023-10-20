package interfaces.rs1;

import static org.junit.Assert.assertFalse;
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

public class ATC_FT_RS1_002_ActualizaInfoPromocionSINERGIA extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_RS1_002_ActualizaInfoPromocionSINERGIA_test(HashMap<String, String> data) throws Exception {



		/*
		 * Utilerias
		 ********************************************************************************************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
//		BD NUCLEO
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCRMSMGR,
				GlobalVariables.DB_USER_FCRMSMGR, GlobalVariables.DB_PASSWORD_FCRMSMGR);
//		BD ORIGINAL
//		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
//				GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
//	BD NUCLEO
		utils.sql.SQLUtil dbSinergia = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCIASSIT,
				GlobalVariables.DB_USER_FCIASSIT, GlobalVariables.DB_PASSWORD_FCIASSIT);
//		BD ORIGINAL
//		utils.sql.SQLUtil dbSinergia = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCIASQAS,
//				GlobalVariables.DB_USER_FCIASQAS, GlobalVariables.DB_PASSWORD_FCIASQAS);


		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1
		String Promotion = "";
		String ValidInf = "Select * from (select s.promotion, s.PROM_NAME, s.prom_desc,  "
				+ "to_char(s.start_date,'DD/MM/YYYY'), to_char(s.end_date,'DD/MM/YYYY'), "
				+ "s.status, s.comment_desc, s.wm_status, a.store  "
				+ "from WMUSER.wm_prom_sinergia s, WMUSER.promstore a, WMUSER.store_hierarchy b  "
				+ "where wm_status = 'R'  "
				+ "and a.promotion = s.promotion  "
				+ "and a.store = b.store "
				+ "and b.area = '"+data.get("Area")+"' ) where rownum <=10 ";
		// Paso 2
		String ValidPromo =	"select PROMOTION,PROMOTION_NAME,PROMOTION_DESC,STATUS,WM_RUN_ID,WM_SEND_DATE,TEMA_ID "
				+ "from XXPEMP.xxse_rms_promotion  "
				+ "where promotion = '%s'";
		
//		******************
//		Paso 4
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'RS1'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ " order by start_dt desc";
		
		String validThread = "SELECT THREAD_ID,PARENT_ID,NAME,START_DT,STATUS,ATT1,ATT2  " + "FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID = '%s' ";

//	Paso 5 
		String ValidUpd =	"select PROMOTION,PROMOTION_NAME,PROMOTION_DESC,STATUS,COMMENT_DESC,WM_RUN_ID,WM_SEND_DATE,DISCOUNT_PARTNER,DISCOUNT_OXXO,TEMA_ID "
				+ " from XXPEMP.xxse_rms_promotion  "
				+ "where promotion = '%s' "
				+ "and wm_run_id = '%s' "
				+ "and trunc(wm_send_date) = trunc(sysdate)";
//	Paso 6 
		
		String tdcQueryStatusThread = "SELECT PARENT_ID,THREAD_ID,NAME,STATUS,ATT1,ATT2 " + " FROM WMLOG.WM_LOG_THREAD "
				+ " WHERE PARENT_ID = %s";
//		***********
		String ValidStatus =	"select promotion, prom_name, prom_desc, to_char(start_date,'DD/MM/YYYY'), to_char(end_date,'DD/MM/YYYY'), comment_desc, wm_status  "
				+ "from WMUSER.wm_prom_sinergia  "
				+ "where promotion = '%s' "
				+ "and wm_status = 'E'";
		
		// consultas de error
				String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
						+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1"; // dbLog
				String consultaError2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
						+ "where RUN_ID='%s')WHERE rownum <= 1"; // dbLog
				String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
						+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1"; // dbLog
//****	

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='RS1' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************* ***********************************************************************************************************

		addStep("Validar que existe informacion de promociones en la tabla wm_prom_sinergia de "
				+ "RETEK con el campo WM_STATUS igual a R, para la plaza(Area) "+data.get("Area"));

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);

		System.out.println(ValidInf);

		SQLResult validInfRes = dbRms.executeQuery(ValidInf);

		boolean ValidaBool = validInfRes.isEmpty(); // checa que el string contenga datos

		if (!ValidaBool) {

			Promotion = validInfRes.getData(0, "PROMOTION");
			System.out.println("Promotion: "+Promotion);
			testCase.addQueryEvidenceCurrentStep(validInfRes); 
		}else if(ValidaBool) {
			testCase.addQueryEvidenceCurrentStep(validInfRes);
			testCase.addTextEvidenceCurrentStep(
					"No se muestran información de promociones en la tabla wm_prom_sinergia");
		}

		System.out.println(ValidaBool); // Si no, imprime la fechas
		assertFalse(ValidaBool,"No se muestran informacion de promociones en la tabla wm_prom_sinergia"); // Si esta vacio,
																										// imprime
		// mensaje

//*************************************************Paso 2***********************************************************************************************************************
		
		addStep("Validar que  existe informacion de las promociones en la tabla: xxse_rms_promotion de SINERGIA.");
		String ValidPromFormat = String.format(ValidPromo, Promotion);

		System.out.println(ValidPromFormat);

		SQLResult ExecValidProm = dbSinergia.executeQuery(ValidPromFormat);

		boolean validPromRes = ExecValidProm.isEmpty();

		if (!validPromRes) {

			testCase.addQueryEvidenceCurrentStep(ExecValidProm);

		}else if(validPromRes) {
			testCase.addQueryEvidenceCurrentStep(ExecValidProm);
			testCase.addTextEvidenceCurrentStep(
					"No hay información de las promociones en la tabla: xxse_rms_promotion de SINERGIA");
		}

		System.out.println(validPromRes);
		assertFalse(validPromRes,"No se muestra información de las promociones en la tabla: xxse_rms_promotion de SINERGIA");

//**********************************************************Paso 3*************************************************************************************************************		

		addStep("Ejecutar el servicio:  RS1.Pub:run, solicitando el job: runRS1.");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String status = "S";

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecución

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}
		
		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		System.out.println(successRun);
		if (!successRun) {

			String error = String.format(consultaError1, run_id);
			String error1 = String.format(consultaError2, run_id);
			String error2 = String.format(consultaError3, run_id);

			SQLResult paso33 = executeQuery(dbLog, error);

			boolean emptyError = paso33.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(paso33);

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

//*********************************Paso 4*********************************************************
		
		addStep("Validar que la interfaz se ejecuto correctamente.");
		String RunID = "";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);
		String StatusID = "";
		String statusWmLog = "S";
		
		boolean LogRequest = ExecLog.isEmpty();
		boolean validateStatus = false;
		
		if (!LogRequest) {
			RunID = ExecLog.getData(0, "RUN_ID");
			System.out.println("RunID_Log: " + RunID);
			
			StatusID = ExecLog.getData(0, "STATUS");
			System.out.println("Status_Log: " + StatusID);
			
			validateStatus = statusWmLog.equals(StatusID);

		}if (validateStatus) {
			testCase.addQueryEvidenceCurrentStep(ExecLog);
		} else if (!validateStatus) {
			testCase.addQueryEvidenceCurrentStep(ExecLog);
			testCase.addTextEvidenceCurrentStep(
					"El estatus del log es diferente a S o No se encontraron datos en WM_Log_Run");
		}

		
		System.out.println(validateStatus);
		assertTrue(validateStatus,"No se muestra  la informacion.");
		

//********************************************Paso 5**************************************************************************************************************************
		
		addStep("Validar que el registro de ejecucion de la plaza y tienda termina en estatus 'S' en la tabla WM_LOG_THREAD. ");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String queryStatusThread = String.format(tdcQueryStatusThread, RunID);
		System.out.println(queryStatusThread);
		SQLResult queryStatusThreadResult = dbLog.executeQuery(queryStatusThread);
		String THREAD_ID = "";
		String StatusThread = "";

		boolean validathread = queryStatusThreadResult.isEmpty();
		boolean validateStatusThr = false;

		if (!validathread) {
			THREAD_ID = queryStatusThreadResult.getData(0, "THREAD_ID");
			System.out.println("THREAD_ID: " + THREAD_ID);
			StatusThread = queryStatusThreadResult.getData(0, "STATUS");
			System.out.println("Status_thread: " + StatusThread);

			validateStatusThr = statusWmLog.equals(StatusThread);
		}

		if (validateStatusThr) {
			testCase.addQueryEvidenceCurrentStep(queryStatusThreadResult);
		} else if (!validateStatusThr) {
			testCase.addQueryEvidenceCurrentStep(queryStatusThreadResult);
			testCase.addTextEvidenceCurrentStep(
					"El estatus del thread es diferente a S o no se encontro informacion en WM_LOG_THREAD ");
		}

		assertTrue(validateStatusThr,"El estatus del thread es diferente a S o no se encontro informacion en WM_LOG_THREAD");
		
		
		
//*****************************************Paso 6************************************************************		
		addStep("Validar que se actualizo la informacion en la tabla: xxse_rms_promotion de SINERGIA.");
		String ValidInsertFormat = String.format(ValidUpd, Promotion, RunID);
		System.out.println(ValidInsertFormat);
		SQLResult ExecuteValidIns = dbSinergia.executeQuery(ValidInsertFormat);

		boolean ExecuteValidInsReq = ExecuteValidIns.isEmpty();

		if (!ExecuteValidInsReq) {

			testCase.addQueryEvidenceCurrentStep(ExecuteValidIns);

		}else if (ExecuteValidInsReq) {
			testCase.addQueryEvidenceCurrentStep(ExecuteValidIns);
			testCase.addTextEvidenceCurrentStep("No se se actualizo la informacion en la tabla: xxse_rms_promotion de SINERGIA ");
		}

		System.out.println(ExecuteValidInsReq);
		assertFalse(ExecuteValidInsReq,"No se actualizo la tabla");

//*********************************************************Paso 6 **********************************************************************************************
	
		addStep("Validar que el campo WM_STATUS se actualizo a E correctamente en la tabla wm_prom_sinergia de RETEK.");
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String ValidStatusFormat = String.format(ValidStatus, Promotion);
		System.out.println(ValidStatusFormat);
		SQLResult ExecuteStatusFormat = dbRms.executeQuery(ValidStatusFormat);

		boolean ExecuteValidUpdtReq = ExecuteStatusFormat.isEmpty();

		if (!ExecuteValidUpdtReq) {

			testCase.addQueryEvidenceCurrentStep(ExecuteStatusFormat);

		}else if (ExecuteValidUpdtReq) {
			testCase.addQueryEvidenceCurrentStep(ExecuteStatusFormat);
			testCase.addTextEvidenceCurrentStep("el campo WM_STATUS NO se actualizo a E correctamente en la tabla wm_prom_sinergia de RETEK ");
		}

		System.out.println(ExecuteValidUpdtReq);
		assertFalse(ExecuteValidUpdtReq,"No se muestran datos insertados");

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Validar que se actualice la informacion de promociones en SINERGIA";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
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

