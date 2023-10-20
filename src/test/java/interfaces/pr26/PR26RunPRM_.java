package interfaces.pr26;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PR26RunPRM_ extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_pr26_PR26RunPRM_(HashMap<String, String> data) throws Exception {
		/*
		 * Utilerías
		 *********************************************************************/

//		BD Original
		utils.sql.SQLUtil dbPOSUSER = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

//		BD NUCLEO
//		utils.sql.SQLUtil dbPOSUSER = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_fcwmesit,GlobalVariables.DB_USER_fcwmesit, GlobalVariables.DB_PASSWORD_fcwmesit);

//		BD original
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);

		// BD NUCLEO
//		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCEBSSIT,GlobalVariables.DB_USER_FCEBSSIT, GlobalVariables.DB_PASSWORD_FCEBSSIT);

//		BD ORIGINAL
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);

//		BD NUCLEO
	//	utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCRMSMGR,GlobalVariables.DB_USER_FCRMSMGR, GlobalVariables.DB_PASSWORD_FCRMSMGR);

		utils.sql.SQLUtil dbLOG = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

//		******************************QUERY**************************************************************

		// Paso 1
		String plaza = "select*from (SELECT ORACLE_CR,ORACLE_CR_SUPERIOR, ORACLE_CR_TYPE, ESTADO, RETEK_CR, RETEK_ASESOR_NOMBRE FROM XXFC.XXFC_MAESTRO_DE_CRS_V\r\n"
				+ "WHERE ORACLE_CR_SUPERIOR = '" + data.get("plaza") + "'" + " AND ORACLE_CR = '" + data.get("tienda")
				+ "'" + " AND  ORACLE_CR_TYPE = 'T') WHERE rownum <= 1";

		// Paso 2
		String conITM = "SELECT  * FROM (SELECT  WM_TARGET_PRM, load_batch_id, location, wm_prm_status "
				+ "FROM wmuser.pos_prm_head "
				+ " WHERE  wm_prm_status='L' and location = '%s' ORDER BY LOAD_DATE DESC  )  "
				+ "  WHERE rownum <= 1  ";
		// Paso 3, 4 y 5
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE = 'PR26_PRM' "
				+ "ORDER BY START_DT DESC) where rownum <=1";// Consulta para estatus de la ejecucion

		String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// Consulta para los errores
		String consultaERROR2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores
		String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores

		// Paso 6
		String consultaTHREAD = "select * from (SELECT THREAD_ID, PARENT_ID, NAME,START_DT, END_DT, STATUS  "
				+ "FROM wmlog.WM_LOG_THREAD" + " where PARENT_ID= '%s' ) where rownum <=1";

		String consultaTHREAD2 = "select * from (SELECT ATT1, ATT2,ATT3,ATT4,ATT5 FROM wmlog.WM_LOG_THREAD "
				+ "where PARENT_ID='%s'  ) where rownum <=1";

		// Paso 7
		String consultaITM = "	select WM_TARGET_PRM, load_batch_id, location,  wm_prm_status "
				+ "from wmuser.pos_prm_head where load_batch_id='%s' and location='%s' and WM_PRM_STATUS = 'E' order by load_date desc";

		// Paso 8
		String consultaL = " select * from (select id,target_type, doc_name,doc_type, sent_date "
				+ "from POSUSER.POS_OUTBOUND_DOCS" + " where DOC_TYPE='PRM' and STATUS='L'  order by SENT_DATE desc) "
				+ "WHERE rownum <= 1";

		String consultaL2 = "select * from (select pv_cr_plaza, pv_cr_tienda, status, source_id, partition_date"
				+ " from POSUSER.POS_OUTBOUND_DOCS " + "where DOC_TYPE='PRM' and STATUS='L'  order by SENT_DATE desc) "
				+ "WHERE rownum <= 1 ";

		testCase.setProject_Name("Remediaciones SYGNIA (AP1)");

//*******************************Paso 1 ********************************************************************************************	

		addStep("Obtener el valor RETEK_CR de la tabla XXFC_MAESTRO_DE_CRS_V de ORAFIN para la plaza  y tienda .");
		System.out.println("DB Ebs: " + plaza);
		SQLResult plazas = dbEbs.executeQuery(plaza);
		String retek = "";

		boolean paso11 = plazas.isEmpty();
		if (!paso11) {
			retek = plazas.getData(0, "RETEK_CR");
			System.out.println("RETEK_CR " + retek);
			testCase.addQueryEvidenceCurrentStep(plazas);
		} else if (paso11) {
			testCase.addQueryEvidenceCurrentStep(plazas);
			testCase.addTextEvidenceCurrentStep(
					"No se obtuvo el valor RETEK_CR de la tabla XXFC_MAESTRO_DE_CRS_V de ORAFIN para la plaza  y tienda");
		}
		System.out.println(paso11);
		assertFalse("No se muestra la información de la tienda", paso11);

//****************************Paso 2 ***********************************************************************************************
		addStep("Validar que exista información pendiente por procesar en la tabla WMUSER.POS_ITM_PRM_HEAD de RETEK con WM_PRM_STATUS = 'L'.");
		String qryRetek_cr = String.format(conITM, retek);

		System.out.println("DB Rms: " + qryRetek_cr);

		SQLResult ITM1 = dbRms.executeQuery(qryRetek_cr);

		String id = "";

		String loc = "";

		boolean paso1 = ITM1.isEmpty();

		if (!paso1) {
			id = ITM1.getData(0, "LOAD_BATCH_ID");
			System.out.println("LOAD_BATCH_ID " + id);

			loc = ITM1.getData(0, "LOCATION");
			System.out.println("LOCATION " + loc);

			testCase.addQueryEvidenceCurrentStep(ITM1);
		} else if (paso1) {
			testCase.addQueryEvidenceCurrentStep(ITM1);
			testCase.addTextEvidenceCurrentStep(
					"No se obtuvo información pendiente por procesar en la tabla WMUSER.POS_ITM_PRM_HEAD de RETEK con WM_PRM_STATUS = 'L'");
		}

		System.out.println(paso1);
		assertFalse("No se muestra la información pendiente por procesar para la tienda.", paso1);

//***************************Paso 3 ***********************************************************************************************
		addStep("Ejecutar el servicio PR26.Pub:runPRM con el job runPRM de Ctrl-M para enviar la información relacionada a los artículos, cambios de precio y promociones de RMS a POS");

		String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = executeQuery(dbLOG, tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");
		String run_id = is.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {

			is = executeQuery(dbLOG, tdcIntegrationServerFormat);

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(consultaERROR, run_id);
			String error1 = String.format(consultaERROR2, run_id);
			String error2 = String.format(consultaERROR3, run_id);

			SQLResult errorr = dbLOG.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontro un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorr);

			}

			SQLResult errorIS = dbLOG.executeQuery(error1);
			boolean emptyError1 = errorIS.isEmpty();
			if (!emptyError1) {
				testCase.addQueryEvidenceCurrentStep(errorIS);
			}

			SQLResult errorIS2 = dbLOG.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();

			if (!emptyError2) {

				testCase.addQueryEvidenceCurrentStep(errorIS2);

			}

		}

//*************************Paso 4 ************************************************************************

		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run ");
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(tdcIntegrationServerFormat);
		SQLResult is1 = executeQuery(dbLOG, tdcIntegrationServerFormat);
		String statusID = "";
		String statusWmLog = "S";

		boolean validateLog = is1.isEmpty();
		boolean validateStatus = false;

		if (!validateLog) {
			run_id = is1.getData(0, "RUN_ID");
			System.out.println("RunID_Log: " + run_id);
			statusID = is1.getData(0, "STATUS");
			System.out.println("Status_Log: " + statusID);

			validateStatus = statusWmLog.equals(statusID);
		}

		if (validateStatus) {
			testCase.addQueryEvidenceCurrentStep(is1);
		} else if (!validateStatus) {
			testCase.addQueryEvidenceCurrentStep(is1);
			testCase.addTextEvidenceCurrentStep(
					"El estatus del log es diferente a S ó No se encontraron datos en WM_Log_Run");
		}

		System.out.println(validateStatus);
		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");

//************************************Paso 5 ***********************************************************
		addStep("Validar que el registro de ejecución  en la tabla WM_LOG_THREAD.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String consultaTH = String.format(consultaTHREAD, run_id);
		System.out.println(consultaTH);

		SQLResult THRE = dbLOG.executeQuery(consultaTH);
		String THREAD_ID = "";
		String StatusThread = "";

		boolean validathread = THRE.isEmpty();
		boolean validateStatusThr = false;

		if (!validathread) {
			THREAD_ID = THRE.getData(0, "THREAD_ID");
			System.out.println("THREAD_ID: " + THREAD_ID);
			StatusThread = THRE.getData(0, "STATUS");
			System.out.println("Status_thread: " + StatusThread);

			validateStatusThr = statusWmLog.equals(StatusThread);
		}

		if (validateStatusThr) {
			testCase.addQueryEvidenceCurrentStep(THRE);
		} else if (!validateStatusThr) {
			testCase.addQueryEvidenceCurrentStep(THRE);
			testCase.addTextEvidenceCurrentStep(
					"El estatus del thread es diferente a S o no se encontro informacion en WM_LOG_THREAD ");
		}

		assertTrue(validateStatusThr,"El estatus del thread es diferente a S o no se encontro informacion en WM_LOG_THREAD");

//**************************************Paso 6 *************************************************************

		addStep("Validar que se actualice el estatus y el nombre del archivo generado por la interface de los registros procesados en la tabla POS_ITM_PRM_HEAD de RETEK.");

		String consultas4 = String.format(consultaITM, id, loc);

		System.out.println(consultas4);

		SQLResult ITME = dbRms.executeQuery(consultas4);

		String wm_target_prm = "";

		boolean pasoE = ITME.isEmpty();

		if (!pasoE) {
			wm_target_prm = ITME.getData(0, "WM_TARGET_PRM");
			System.out.println("WM_TARGET_PRM: " + wm_target_prm);

			testCase.addQueryEvidenceCurrentStep(ITME);
		} else if (pasoE) {
			testCase.addQueryEvidenceCurrentStep(ITME);
			testCase.addTextEvidenceCurrentStep(
					"No se actualizo el estatus de los registros procesados en la tabla POS_ITM_PRM_HEAD");
		}

		System.out.println(pasoE);

		assertFalse("No se actualizo el estatus de los registros procesados en la tabla POS_ITM_PRM_HEAD de RETEK a WM_PRM_STATUS = 'E'", pasoE);

	
//***************************************Paso 7************************************************************	
		addStep("Validar que se inserte el registro del documento PRM generado por la interface en la tabla POS_OUTBOUND_DOCS de POSUSER");

		// Primera consulta

		System.out.println(consultaL);

		SQLResult L = dbPOSUSER.executeQuery(consultaL);

		String doc_name = "";

		boolean pasoL = L.isEmpty();

		if (!pasoL) {
			doc_name = L.getData(0, "DOC_NAME");
			System.out.println("DOC_NAME: " + doc_name);
			testCase.addQueryEvidenceCurrentStep(L);
		} else if (pasoL) {
			testCase.addQueryEvidenceCurrentStep(L);
			testCase.addTextEvidenceCurrentStep("No se encontraron resultados en la consulta");
		}

		System.out.println(pasoL);

		// Segunda consulta

		System.out.println(consultaL2);

		SQLResult L2 = dbPOSUSER.executeQuery(consultaL2);

		boolean pasoL2 = L2.isEmpty();

		if (!pasoL2) {

			testCase.addQueryEvidenceCurrentStep(L2);
		} else if (pasoL2) {
			testCase.addQueryEvidenceCurrentStep(L2);
			testCase.addTextEvidenceCurrentStep("No se encontraron resultados en la consulta");
		}

		System.out.println(pasoL2);
		
		assertFalse("No se inserto el detalle del documento generado por la interface en la tabla POS_OUTBOUND_DOCS de POSUSER ", pasoL2);

		

//*******************************Paso 8 **************************************************************

		addStep("Validar que se realice el envío del archivo PRM generado por la interface en el directorio configurado para la tienda procesada.");

		FTPUtil ftp = new FTPUtil("10.182.92.13", 21, "posuser", "posuser");

		// host, puerto, usuario, contraseña
		String ruta = "/FEMSA_OXXO/POS/" + data.get("plaza") + "/" + data.get("tienda") + "/working/" + doc_name;
		boolean validaFTP;

		if (ftp.fileExists(ruta)) {

			validaFTP = true;
			System.out.println("Existe");

			testCase.addBoldTextEvidenceCurrentStep("El archivo si existe ");

			testCase.addBoldTextEvidenceCurrentStep(ruta);

		} else {
			System.out.println("No Existe");
			validaFTP = false;
		}

		assertTrue("No se encontro el archivo xml en POSUSER ", validaFTP);

	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_002_pr26_PR26RunPRM_";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Validar la ejecución del servicio PR26.Pub:runPRM para enviar la información relacionada a los artículos,"
				+ " cambios de precio y promociones de Retek(RMS) a POS (Archivo de Promociones PRM).";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMTIZACION";
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