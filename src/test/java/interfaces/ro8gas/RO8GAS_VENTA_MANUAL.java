package interfaces.ro8gas;

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

public class RO8GAS_VENTA_MANUAL extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_007_RO8_GAS_VENTA_MANUAL(HashMap<String, String> data) throws Exception {

/** UTILERIA *********************************************************************/

		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS, GlobalVariables.DB_PASSWORD_EBS);

		SeleniumUtil u;
		PakageManagment pok;

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id;
		String status = "S";


/** VARIABLES *********************************************************************/

		String SelectCFG = "SELECT ATTRIBUTE1 AS PLAZA, ATTRIBUTE2 AS TRANCODE  " + " FROM WMUSER.wm_cfg_launcher "
				+ " WHERE interface_name = 'RO08_GAS'" + " AND attribute1=" + "'" + data.get("plaza") + "'"
				+ " AND attribute3 ='VTAS'" + " AND MANUAL_STATUS= 'A'";//Posuser

		String SelectInsumos1 = "SELECT ITEM, ITEM_DESC,STORE,TRAN_DATE,TRAN_CODE " + " FROM fem_fif_stg "
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60" + " AND cr_plaza =" + "'" + data.get("plaza") + "'"
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'" + " AND reference_3 IS NULL"
				+ " AND reference_9 IS NULL";// RMS

		String SelectInsumos2 = "SELECT REFERENCE_3,REFERENCE_9,CR_PLAZA,ID " + " FROM fem_fif_stg "
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60" + " AND cr_plaza =" + "'" + data.get("plaza") + "'"
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'" + " AND reference_3 IS NULL"
				+ " AND reference_9 IS NULL";// RMS

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" + " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'RO08_GAS_VTAS'" + " AND START_DT >= TRUNC (SYSDATE)" + " order by start_dt desc)"
				+ " where rownum = 1";

		String tdcQueryErrorId = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID='%s'";

		String tdcQueryStatusLog = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60"
				+ " FROM WMLOG.WM_LOG_RUN Tbl  WHERE INTERFACE = 'RO08_GAS_VTAS'"
				+ " ORDER BY START_DT DESC) where rownum <=1";

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = '%s'";

		String VerificacionHeader = "SELECT header_id, tran_code, tran_date, cr_plaza, wm_creation_date, wm_run_id, wm_status"
				+ " FROM WMUSER.WM_GL_HEADERS_GAS" + " WHERE cr_plaza =" + "'" + data.get("plaza") + "'"
				+ " AND tran_code = 1" + " AND wm_run_id ='%s'";// RMS

		String ConsultaGl1 = "SELECT reference6,reference4,reference10,reference22,reference25"
				+ " FROM GL.GL_INTERFACE" + " WHERE reference6 ='%s'";

		String ConsultaGl2 = "SELECT user_je_category_name,user_je_source_name,segment4" + " FROM GL.GL_INTERFACE"
				+ " WHERE reference6 ='%s'";//EBS

		String ValidacionR3R9p1 = "SELECT ITEM,ITEM_DESC,STORE,TRAN_DATE,TRAN_CODE" + " FROM RMS100.fem_fif_stg"
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60" + " AND cr_plaza =" + "'" + data.get("plaza") + "'"
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'" + " AND reference_3 = '%s'"
				+ " AND reference_9 = '%s'";

		String ValidacionR3R9p2 = "SELECT REFERENCE_3,REFERENCE_9,CR_PLAZA,ID" + " FROM RMS100.fem_fif_stg"
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60" + " AND cr_plaza =" + "'" + data.get("plaza") + "'"
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'" + " AND reference_3 = '%s'"
				+ " AND reference_9 = '%s'";

		String consultaReference3 = "SELECT reference_3, reference_6" + " FROM RMS100.fem_fif_stg"
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60" + " AND cr_plaza =" + "'" + data.get("plaza") + "'"
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'" + " AND reference_9 = '%s'"
				+ " AND reference_3 IS NOT NULL"; //RMS

		String VerificacionLines = "SELECT LINE_ID,HEADER_ID,NET_RETAIL,VAT_RETAIL,NET_COST,VAT_COST "
				+ " FROM WMUSER.WM_GL_LINES_GAS " + "WHERE HEADER_ID = '%s'";//RMS

		String VerificacionLines2 = "SELECT TOTAL_RETAIL,VAT_RATE_RETAIL,VAT_RATE_COST,VAT_CODE_RETAIL,VAT_CODE_COST "
				+ " FROM WMUSER.WM_GL_LINES_GAS " + "WHERE HEADER_ID = '%s'";
		
		String ManualStatus="SELECT interface_name, auto_status, manual_status,attribute1,attribute2,attribute3 FROM wmuser.WM_CFG_LAUNCHER " + 
				"WHERE INTERFACE_NAME = 'RO08_GAS' " + 
				"AND DECODE ('MANUAL', 'AUTO', AUTO_STATUS, MANUAL_STATUS) = 'I' " + 
				"AND ATTRIBUTE3 = 'VTAS' AND ATTRIBUTE2 = 1 AND ATTRIBUTE1 = '" + data.get("plaza") + "'";
		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep("Verificar parametros configurados en la WMUSER.WM_CFG_LAUNCHER para ejecución MANUAL plaza " + data.get("plaza"));

		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(SelectCFG);
		SQLResult result1 = executeQuery(dbPos, SelectCFG);

		boolean av = result1.isEmpty();

		if (!av) {
			testCase.addQueryEvidenceCurrentStep(result1);
		}

		System.out.println(av);
		assertFalse(av, "No se obtiene información de la consulta");
		
		

		/* PASO 2 *********************************************************************/

		addStep("Desmarcar información para procesar REFERENCE_3 = NULL y REFERENCE_9 = NULL en la tabla FEM_FIF_STG.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(SelectInsumos1);
		SQLResult resultInsumos1 = executeQuery(dbRms, SelectInsumos1); 
		SQLResult resultInsumos2 = executeQuery(dbRms, SelectInsumos2);

		boolean SC = resultInsumos1.isEmpty();

		if (!SC) {
			testCase.addQueryEvidenceCurrentStep(resultInsumos1);
			testCase.addQueryEvidenceCurrentStep(resultInsumos2, false);
		}

		System.out.println(SC);
		assertFalse(SC, "No se obtiene información de la consulta");
		
		

		/* PASO 3 *********************************************************************/

		addStep("Ejecutar la interface en AUTO RO8_GAS.Pub:runVTAS. Solicitar la ejecución del job: runRO8_GAS_VNTAS");

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		u.hardWait(4);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution: " + dateExecution);
		System.out.println(tdcQueryIntegrationServer);
		SQLResult result5 = executeQuery(dbLog, tdcQueryIntegrationServer);
		String status1 = result5.getData(0, "STATUS");
		run_id = result5.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		
		while (valuesStatus) {
			result5 = executeQuery(dbLog, tdcQueryIntegrationServer);
			status1 = result5.getData(0, "STATUS");
			run_id = result5.getData(0, "RUN_ID");
			u.hardWait(2);
		}
		
		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		
		if (!successRun) {
			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult result3 = executeQuery(dbLog, error);

			boolean emptyError = result3.isEmpty();

			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(result3);
			}
		}

		
		
		/* PASO 4 *********************************************************************/

		addStep("Verificar que la ejecución termina con éxito.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
		System.out.println(queryStatusLog);		
		SQLResult Result4 = executeQuery(dbLog, queryStatusLog);
		String fcwS = Result4.getData(0, "STATUS");

		testCase.addQueryEvidenceCurrentStep(Result4);

		boolean validateStatus = status.equals(fcwS);
		
		System.out.println(validateStatus);
		assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");

		addStep("Verificar los threads de la ejecución.");

		String consultafor = "", thread_id, thread_status;
		SQLResult resultfor;
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);
		SQLResult resultThreads = executeQuery(dbLog, queryStatusThread);
		thread_id = resultThreads.getData(0, "THREAD_ID");
		thread_status = resultThreads.getData(0, "STATUS");
		String thread_id2 = "";
		String thread_status2 = "";
		boolean registro = false;

		for (int i = 0; i < resultThreads.getRowCount(); i++) {
			String id = resultThreads.getData(i, "THREAD_ID");
			consultafor = String.format(VerificacionHeader, id); //Consulta si el thread registro el insumo en RMS
			System.out.println(consultafor);
			resultfor = executeQuery(dbRms, consultafor);			
			boolean foav = resultfor.isEmpty(); 	//Si no esta vacio, se pasan los datos del thread a una nueva variable para no ser sobreescritas en el ciclo y se activa la bandera.
			System.out.println("ESTA VACIO EL THREAD " + id + " ? "+ foav);

			if (!foav) {
				thread_id2 = resultThreads.getData(i, "THREAD_ID");
				thread_status2 = resultThreads.getData(i, "STATUS");
				registro = true;
			}			
		}

		testCase.addQueryEvidenceCurrentStep(resultThreads);

		if (!registro) {
			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult resultError = executeQuery(dbLog, error);
			boolean emptyError = resultError.isEmpty();

			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(resultError);
			}
		}

		assertTrue(registro, "El registro de ejecución de la plaza y tienda no fue exitoso");
		
		

		/* PASO 5 *********************************************************************/

		addStep("Verificar la actualizacion de JOURNAL_ID, JOURNAL_TYPE_ID,WM_LAST_UPDATE_DATE,WM_STATUS = E y RUN_ID en la tabla WM_GL_HEADERS_GAS.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String consulta = String.format(VerificacionHeader, thread_id2);
		System.out.println(consulta);		
		SQLResult resultConsulta = executeQuery(dbRms, consulta);
		String header_id = resultConsulta.getData(0, "HEADER_ID");
		
		boolean av7 = resultConsulta.isEmpty();

		if (av7 == false) {
			testCase.addQueryEvidenceCurrentStep(resultConsulta);
		} else {
			testCase.addQueryEvidenceCurrentStep(resultConsulta);
		}
		
		System.out.println(av7);
		assertFalse(av7, "No se obtiene informacion de la consulta");
		
		

		/* PASO 6 *********************************************************************/

		addStep("Verificar la inserción de líneas en la tabla GL_INTERFACE.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String consultaR3 = String.format(consultaReference3, header_id);
		SQLResult resultR3 = executeQuery(dbRms, consultaR3); //Se consulta el reference_3 que se utilizara en el siguiente paso
		System.out.println(consultaR3);
		String reference3 = resultR3.getData(0, "reference_3");

		System.out.println(GlobalVariables.DB_HOST_EBS);
		String consulta1 = String.format(ConsultaGl1, reference3);
		String consulta2 = String.format(ConsultaGl2, reference3);
		System.out.println(consulta1);

		SQLResult resultGl1 = executeQuery(dbEbs, consulta1);		//Se consulta el reference6 que seria el mismo numero que el reference_3
		SQLResult resultGl2 = executeQuery(dbEbs, consulta2);
		String reference6 = resultGl1.getData(0, "reference6");

		boolean va = resultGl1.isEmpty();

		if (!va) {
			testCase.addQueryEvidenceCurrentStep(resultGl1);
			testCase.addQueryEvidenceCurrentStep(resultGl2, false);
		}

		System.out.println(va);
		assertFalse(va, "No se obtiene informacion de la consulta");
		
		

		/* PASO 7 *********************************************************************/

		addStep("Verificar la actualización de REFERNCE_3 con el JOURNAL_ID y el REFERENCE_9 con el HEADER_ID en la tabla FEM_FIF_STG.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String vact1 = String.format(ValidacionR3R9p1, reference6, header_id);
		String vact2 = String.format(ValidacionR3R9p2, reference6, header_id);
		System.out.println(vact1);

		SQLResult resultP1 = executeQuery(dbRms, vact1);
		SQLResult resultP2 = executeQuery(dbRms, vact2);
		boolean va1 = resultP1.isEmpty();

		if (!va1) {
			testCase.addQueryEvidenceCurrentStep(resultP1);
			testCase.addQueryEvidenceCurrentStep(resultP2, false);
		}

		System.out.println(va1);
		assertFalse(va1, "No se obtiene informacion de la consulta");
		
		

		/* PASO 8 *********************************************************************/

		addStep("Inserción de líneas en la tabla WM_GL_LINES_GAS");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String gLines1 = String.format(VerificacionLines, header_id);
		String gLines2 = String.format(VerificacionLines2, header_id);
		System.out.println(gLines1);

		SQLResult result8 = executeQuery(dbRms, gLines1);
		SQLResult result9 = executeQuery(dbRms, gLines2);

		boolean va2 = result8.isEmpty();

		if (!va2) {
			testCase.addQueryEvidenceCurrentStep(result8);
			testCase.addQueryEvidenceCurrentStep(result9, false);
		}

		System.out.println(va2);
		assertFalse(va2, "No se obtiene informacion de la consulta");
		

		
		/* PASO 9 *********************************************************************/
		
		addStep("Actualización del campo MANUAL_STATUS en la tabla WM_CFG_LAUNCHER a I.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(ManualStatus);
		SQLResult StatusWmC1 = executeQuery(dbPos,ManualStatus);

		boolean Puser1 = StatusWmC1.isEmpty();
		testCase.addQueryEvidenceCurrentStep(StatusWmC1);

		assertFalse(Puser1, "No existen parametros configurados en la tabla");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Verificar proceso de interface VENTAS proceso manual ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_007_RO8_GAS_VENTA_MANUAL";
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
}
