package interfaces.OEIMX;

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

public class ATC_FT_001_OEIMX_ActualizaEstatusLimiteEnvioFactura extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_OEIMX_ActualizaEstatusLimiteEnvioFactura_test(HashMap<String, String> data) throws Exception {
		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA,
				GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbLOG = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		// Paso 1
		String ValidInfo = "SELECT id_factura_digital, anio, mes, cr_plaza, cr_tienda, wm_status, wm_retries, version_cfdi, wm_run_id, docto_xml "
				+ "FROM XXFC.xxfc_cfd_factura_digital " + "WHERE version_cfdi = '3.3' " + "AND wm_status = 'L' "
				+ "AND NVL(wm_retries, 0) = 2 	" + "AND cr_plaza = '" + data.get("plaza") + "' " + "AND anio = '"
				+ data.get("año") + "' " + "AND mes = '" + data.get("mes") + "'";

		// Paso 2
		String ValidNoInfo = "SELECT tipo, descripcion, cantidad, unidad, precio_unitario, tipo_impuesto, tasa_impuesto, importe  "
				+ "FROM XXFC.xxfc_cfd_factura_digital_lines  " + "WHERE id_factura_digital = '%s'";
		// Paso 3, 4 y 5

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE = 'OEIMX' "
				+ "ORDER BY START_DT DESC) where rownum <=1";// Consulta para estatus de la ejecucion

		String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// Consulta para los errores
		String consultaERROR2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores
		String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores

		String ValidLog = "SELECT run_id,interface, start_dt, end_dt, status, server  " + "FROM WMLOG.wm_log_run  "
				+ "WHERE interface = 'OEIMX' " + "AND status = 'E' "
				+ "AND trunc(start_dt) >= trunc(SYSDATE) ORDER BY 1 DESC";

		String consultaTHREAD = "SELECT THREAD_ID, PARENT_ID, NAME,START_DT, END_DT, STATUS  "
				+ "FROM WMLOG.wm_log_thread " + "WHERE parent_id = '%s'";

		String ValidIncrement1 = "SELECT id_factura_digital, anio, mes, cr_plaza, cr_tienda, wm_status "
				+ "FROM XXFC.xxfc_cfd_factura_digital " + "WHERE version_cfdi = '3.3' " + "AND wm_status = 'N' "
				+ "AND wm_retries = '3' " + "AND cr_plaza = '" + data.get("plaza") + "' " + "AND anio = '"
				+ data.get("año") + "' " + "AND mes = '" + data.get("mes") + "' " + "AND wm_run_id = '%s'";

		String ValidIncrement2 = "select wm_retries, version_cfdi, wm_run_id, docto_xml "
				+ "FROM XXFC.xxfc_cfd_factura_digital " + "WHERE version_cfdi = '3.3' " + "AND wm_status = 'N' "
				+ "AND wm_retries = '3' " + "AND cr_plaza = '" + data.get("plaza") + "' " + "AND anio = '"
				+ data.get("año") + "' " + "AND mes = '" + data.get("mes") + "' " + "AND wm_run_id = '%s'";

//*******************************Paso 1 ********************************************************************************************	

		addStep("Validar que existe información pendiente de procesar en ORAFINIMMEX con estatus L en la tabla XXFC_CFD_FACTURA_DIGITAL.");

		System.out.println(GlobalVariables.DB_HOST_EBS);
		System.out.println(ValidInfo);
		SQLResult ValidInfoExec = dbEbs.executeQuery(ValidInfo);
		String ID_FACTURA_DIGITAL = "";

		boolean ValidInfoRes = ValidInfoExec.isEmpty();
		if (!ValidInfoRes) {
			ID_FACTURA_DIGITAL = ValidInfoExec.getData(0, "ID_FACTURA_DIGITAL");
			System.out.println("ID_FACTURA_DIGITAL: " + ID_FACTURA_DIGITAL);
			testCase.addQueryEvidenceCurrentStep(ValidInfoExec);
		} else if (ValidInfoRes) {
			testCase.addTextEvidenceCurrentStep("No se encontraron datos en la consulta /n" + ValidInfo);
			testCase.addQueryEvidenceCurrentStep(ValidInfoExec);
		}

		System.out.println(ValidInfoRes);
		assertFalse("No se muestra la información en la consulta", ValidInfoRes);

//****************************Paso 2 ***********************************************************************************************

		addStep("Validar que NO existe información en la tabla XXFC_CFD_FACTURA_DIGITAL_LINES de ORAFINIMMEX.");

		System.out.println(GlobalVariables.DB_HOST_EBS);
		String FormatValidNoInfo = String.format(ValidNoInfo, ID_FACTURA_DIGITAL);
		System.out.println(FormatValidNoInfo);
		SQLResult ValidNoInfoExec = dbEbs.executeQuery(FormatValidNoInfo);

		boolean ValidNoInfoRes = ValidNoInfoExec.isEmpty();

		if (ValidNoInfoRes) {

			testCase.addQueryEvidenceCurrentStep(ValidNoInfoExec);
		} else if (!ValidNoInfoRes) {
			testCase.addTextEvidenceCurrentStep(
					"Si se encontraron datos en la consulta, no deberia haber registros /n" + ValidInfo);
			testCase.addQueryEvidenceCurrentStep(ValidInfoExec);
		}
		System.out.println(ValidNoInfoRes);
		assertTrue("Si existe información en la tabla XXFC_CFD_FACTURA_DIGITAL_LINES de ORAFINIMMEX.", ValidNoInfoRes);

//***************************Paso 3 ***********************************************************************************************
		addStep("Ejecutar el servicio: OEIMX.Pub:run.");

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

		addStep("Validar que la interfaz se ejecutó correctamente en WMLOG.");
		System.out.println(GlobalVariables.DB_HOST_LOG);
		System.out.println(ValidLog);
		SQLResult ValidLogExec = executeQuery(dbLOG, ValidLog);
		String StatusLog = "";

		boolean validateStatus = ValidLogExec.isEmpty();

		if (!validateStatus) {
			StatusLog = ValidLogExec.getData(0, "STATUS");
			run_id = ValidLogExec.getData(0, "run_id");
			testCase.addQueryEvidenceCurrentStep(ValidLogExec);
		} else if (validateStatus) {
			testCase.addTextEvidenceCurrentStep("No se encontraron registros con el status esperados /n" + ValidLog);
			testCase.addQueryEvidenceCurrentStep(ValidInfoExec);
		}
		System.out.println(validateStatus);
		assertFalse("La ejecucion de la interfaz no fue exitosa", validateStatus);

//************************************Paso 5 ***********************************************************
		addStep("Validar que el registro de ejecución  en la tabla WM_LOG_THREAD.");
		System.out.println(GlobalVariables.DB_HOST_LOG);
		String consultaTH = String.format(consultaTHREAD, run_id);
		System.out.println(consultaTH);
		SQLResult THRE = dbLOG.executeQuery(consultaTH);
		String THREAD_ID = "";

		boolean paso1TH = THRE.isEmpty();

		if (!paso1TH) {
			THREAD_ID = THRE.getData(0, "THREAD_ID");
			System.out.println("THREAD_ID: " + THREAD_ID);
			testCase.addQueryEvidenceCurrentStep(THRE);
		} else if (paso1TH) {
			testCase.addTextEvidenceCurrentStep("No se encontraron registros en thread /n" + consultaTH);
			testCase.addQueryEvidenceCurrentStep(THRE);
		}

		System.out.println(paso1TH);
		assertFalse("No se encontro la ejecución en thread", paso1TH);

//**************************************Paso 6 *************************************************************

		addStep("Validar que se incrementó correctamente el valor en el campo WM_RETRIES en 1 y"
				+ " se actualizo el estatus a N de los registros procesados en la tabla XXFC_CFD_FACTURA_DIGITAL en ORAFINIMMEX. ");

		System.out.println(GlobalVariables.DB_HOST_EBS);
		String consultas4 = String.format(ValidIncrement1, THREAD_ID);
		System.out.println(consultas4);
		SQLResult ValidIncrementExec = dbEbs.executeQuery(consultas4);

		boolean pasoE = ValidIncrementExec.isEmpty();

		if (!pasoE) {
			String consultas5 = String.format(ValidIncrement2, THREAD_ID);
			System.out.println(consultas5);
			SQLResult ValidIncrement5Exec = dbEbs.executeQuery(consultas5);

			testCase.addQueryEvidenceCurrentStep(ValidIncrementExec);
			testCase.addQueryEvidenceCurrentStep(ValidIncrement5Exec);
		} else if (pasoE) {
			testCase.addTextEvidenceCurrentStep(
					"No se encontraron registros procesados en la tabla XXFC_CFD_FACTURA_DIGITAL /n" + ValidIncrement1);
			testCase.addQueryEvidenceCurrentStep(ValidIncrementExec);
		}

		System.out.println(pasoE);

		assertFalse(
				"No se actualizo el estatus de los registros procesados en la tabla POS_ITM_PRM_HEAD de RETEK a WM_PRM_STATUS = 'E'",
				pasoE);

	}

	@Override
	public String setTestFullName() {
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar que se actualiza el campo WM_STATUS en la tabla XXFC_CFD_FACTURA_DIGITAL al "
				+ "alcanzar el maximo número de intentos en el campo WM_RETRIES";
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