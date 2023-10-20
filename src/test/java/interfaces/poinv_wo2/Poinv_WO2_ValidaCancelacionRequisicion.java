package interfaces.poinv_wo2;

import static org.junit.Assert.assertFalse;
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

public class Poinv_WO2_ValidaCancelacionRequisicion extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_Poinv_WO2_002_ValidCancelacionRequicision(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 ********************************************************************************************************************************************/
		SQLUtil dbPortal = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_IAS, GlobalVariables.DB_USER_IAS,
				GlobalVariables.DB_PASSWORD_IAS);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,
				GlobalVariables.DB_PASSWORD_FCWMLQA);
	

		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1

		String validInf = "SELECT ID_LE,FOLIO_LE,PROPERTYNUM,STATUS_EPAPER,EPAPER_UPLOAD_DATE,WM_STATUS "
				+ "from XXEX.XXEX_SYNC_EPAPER_PO  " + "where SUBSTR(PROPERTYNUM, 1, 5) = '" + data.get("plaza") + "'  "
				+ "and WM_STATUS in ('L', 'P') " + "AND STATUS_EPAPER = 'A' " + "ORDER BY EPAPER_UPLOAD_DATE DESC";

		// Paso 3

		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'POINV_WO2'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  ORDER BY START_DT DESC";

//		Paso 5
		String ValidaUpdPortal = " SELECT ID_LE,FOLIO_LE,PROPERTYNUM,STATUS_EPAPER,CANCELED_DATE,WM_STATUS "
				+ "FROM XXEX.XXEX_SYNC_EPAPER_PO  " + "WHERE PROPERTYNUM = '%s'  " + "AND WM_STATUS = 'P'  "
				+ "AND TRUNC(CANCELED_DATE) = TRUNC(SYSDATE); ";

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='POINV_WO2' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//		Paso 1	************************	

		addStep("Validar que existe información en la tabla XXEX_SYNC_EPAPER_PO "
				+ "de PORTAL de acuerdo al parámetro propertyNum proporcionado con wm_status L y P.");
		String ID_LE = "";
		String PROPERTYNUM = "";
		System.out.println(GlobalVariables.DB_HOST_IAS);
		System.out.println(validInf);
		SQLResult validInfExc = dbPortal.executeQuery(validInf);

		boolean validInfRes = validInfExc.isEmpty();

		if (!validInfRes) {

			testCase.addQueryEvidenceCurrentStep(validInfExc);
			ID_LE = validInfExc.getData(0, "ID_LE");
			System.out.println("LE_ID= " + ID_LE);
			PROPERTYNUM = validInfExc.getData(0, "PROPERTYNUM");
			System.out.println("PROPERTYNUM= " + PROPERTYNUM);
		}

		System.out.println(validInfRes);

		assertFalse("No existe información", validInfRes);

//		Paso 2	************************

		// Utileria
		addStep("Ejecutar el servicio: POINV_WO2.Pub:wsInsertRequisicion.");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecución

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(3);

		}

//				Paso 3	************************				
		addStep("Validar que la interfaz se ejecutó correctamente en WMLOG con estatus S.");

		String RUN_ID = "";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RUN_ID = ExecLog.getData(0, "RUN_ID");
			System.out.println("RUN_ID: " + RUN_ID);
			testCase.addQueryEvidenceCurrentStep(ExecLog);
		}

		System.out.println(LogRequest);
		assertFalse(LogRequest, "No se muestra la información.");

//				Paso 4 *********************************

		/*
		 * Validar que se recibe el correo correctamente notificando la cancelación de
		 * la requisición procesada anteriormente (WM_STATUS = P). NO SE PUEDE REALIZAR
		 * PASO DE COMPROBACION DE CORREO
		 */

//				Paso 5 ******************************************

		addStep("Validar que se actualizó el campo CANCELED_DATE en la tabla XXEX_SYNC_EPAPER_PO de PORTAL.");
		System.out.println(GlobalVariables.DB_HOST_IAS);
		String ValidaUpdPortalFormat = String.format(ValidaUpdPortal, PROPERTYNUM);
		System.out.println(ValidaUpdPortalFormat);
		SQLResult ExecValidaUpdPortal = dbPortal.executeQuery(ValidaUpdPortalFormat);
		boolean ValidaUpdPortalRes = ExecValidaUpdPortal.isEmpty();
		if (!ValidaUpdPortalRes) {

			testCase.addQueryEvidenceCurrentStep(ExecValidaUpdPortal);

		}

		System.out.println(ValidaUpdPortalRes);
		assertFalse("No existe información de la requisición para la plaza", ValidaUpdPortalRes);

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_Poinv_WO2_002_ValidCancelacionRequicision";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construida. Validar que se cancela correctamente una requisicion";
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
