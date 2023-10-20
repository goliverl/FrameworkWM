package interfaces.cp01_cl;


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

public class CP01_CL_ValidaCambiosBuzon extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_CP01_CL_ValidaCambiosBuzon(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbLogCL = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile,GlobalVariables.DB_PASSWORD_LogChile);
		utils.sql.SQLUtil dbCNTCL = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_CNTCHILE, GlobalVariables.DB_USER_CNTCHILE,GlobalVariables.DB_PASSWORD_CNTCHILE);
		utils.sql.SQLUtil dbPosCL = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile,GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);

		/**
		 * oxxo123 oxxo123
		 * 
		 * Variables
		 * ******************************************************************************************
		 * 
		 */
		// Paso 1
		String ValidaPOS_CAMBIOSL = "SELECT PLAZA_CR, TIENDA_CR, WM_STATUS "
				+ "FROM WMUSER.POS_CAMBIOS  WHERE WM_STATUS = 'L' AND PLAZA_CR = '" + data.get("plaza")
				+ "' AND TIENDA_CR = '" + data.get("tienda") + "'";

		// Paso 2
		String validaEmail = "SELECT EMAIL, A.GROUP_ID, GROUP_NAME, INTERFACE_NAME "
				+ "FROM WMLOG.WM_LOG_GROUP A, WMLOG.WM_LOG_USER_GROUP B, WMLOG.WM_LOG_USER C "
				+ "WHERE A.GROUP_ID = B.GROUP_ID " + "AND B.USER_ID = C.USER_ID " + "AND A.GROUP_NAME = 'CP01' "
				+ "AND A.INTERFACE_NAME = 'CP01' " + "AND C.PLAZA IS NULL";

		// Paso 3

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ " FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE = 'CP01' AND STATUS='S' "
				+ " ORDER BY START_DT DESC) where rownum <=1";

		// Paso 4

		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'CP01'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  ORDER BY START_DT DESC";

		// Paso 5
		String ValidaCambiosCNT = "SELECT WM_RUN_ID, PLAZA_CR, TIENDA_CR, WM_STATUS, WM_SEND_DT "
				+ " FROM WMUSER.POS_CAMBIOS WHERE WM_STATUS = 'E' "
				+ " AND TRUNC(WM_SEND_DT) = TRUNC(SYSDATE) AND PLAZA_CR = '" + data.get("plaza") + "' "
				+ " AND TIENDA_CR = '" + data.get("tienda") + "' AND WM_RUN_ID = '%s'";

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 */

//**********************************************Paso 1	****************************************************************************
		addStep("Que exista registro de la Plaza Y TIENDA con STATUS L en la tabla POS_CAMBIOS de la BD CNT. ");

		System.out.println(GlobalVariables.DB_HOST_CNTCHILE);
		System.out.println(ValidaPOS_CAMBIOSL);
		SQLResult ValidaPOS_CAMBIOSL_Res = executeQuery(dbCNTCL, ValidaPOS_CAMBIOSL);

		boolean validaStatusL = ValidaPOS_CAMBIOSL_Res.isEmpty();

		if (!validaStatusL) {
			testCase.addQueryEvidenceCurrentStep(ValidaPOS_CAMBIOSL_Res);
		}

		System.out.println(validaStatusL);
		assertFalse(validaStatusL, "No se encontro registro con las tiendas correspondientes");

//**********************************************Paso 2	****************************************************************************** 
		System.out.println(GlobalVariables.DB_HOST_LogChile);
		addStep("Comprobar la dirección de email para recibir las natificaciones en las tablas WM_LOG_GROUP, WM_LOG_USER_GROUP"
				+ " y WM_LOG_USER para el GROUP_NAME igual a CP01 y INTERFACE_NAME igual a CP01 en la BD WMLOG.");

		System.out.println(validaEmail);
		SQLResult validaEmail_Res = executeQuery(dbLogCL, validaEmail);

		boolean validaEmailRes = validaEmail_Res.isEmpty();

		if (!validaEmailRes) {
			testCase.addQueryEvidenceCurrentStep(validaEmail_Res);
		}

		System.out.println(validaEmailRes);
		assertFalse(validaEmailRes, "No se conoce la dirección de e-mail al cual llegará el correo de notificación");

//		***********************************************Paso 3****************************************
		addStep("Se ejecuta el proceso CP01.Pub:run.");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String status = "S";

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		System.out.println(contra);

		String dateExecution = pok.runIntefaceWM(data.get("interface"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution" + dateExecution);
//
		SQLResult is = dbLogCL.executeQuery(tdcIntegrationServerFormat);
		System.out.println(tdcIntegrationServerFormat);
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

			u.hardWait(4);

		}
//*******************************************************Paso 4************************************************************************

		addStep("Se valida que el STATUS sea igual a S en la tabla WM_LOG_RUN  de la BD WM_LOG  donde INTERFACE = CP01.");

		String RUN_ID = "";
		System.out.println(GlobalVariables.DB_HOST_LogChile);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLogCL.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RUN_ID = ExecLog.getData(0, "RUN_ID");
			System.out.println("RUN_ID: " + RUN_ID);
			testCase.addQueryEvidenceCurrentStep(ExecLog);
		}

		System.out.println(LogRequest);
		assertFalse(LogRequest, "No se muestra  la información.");

//**************************************************Paso 5 **************************************************************************

		addStep("Se valida que WM_STATUS sea igual a E en la tabla POS_CAMBIOS de la BD CNT ");
		System.out.println(GlobalVariables.DB_HOST_CNTCHILE);
		String ValidaCambiosCNTFormat = String.format(ValidaCambiosCNT, run_id);
		System.out.println(ValidaCambiosCNTFormat);
		SQLResult ValidRes = executeQuery(dbCNTCL, ValidaCambiosCNTFormat);

		boolean validaCambiosE = ValidRes.isEmpty();

		if (!validaCambiosE) {
			testCase.addQueryEvidenceCurrentStep(ValidRes);
		}

		System.out.println(validaCambiosE);
		assertFalse(validaCambiosE, "Los campos de wm_status, wm_send_dt y wm_run_id no  fueron actualizados.");

//**************************************************Paso 6 ***************************************************************************		
		String verifyFile = "SELECT ID, doc_type, status, DOC_NAME,PV_CR_PLAZA,PV_CR_TIENDA,DATE_CREATED "
				+ " FROM POSUSER.POS_OUTBOUND_DOCS WHERE PV_CR_PLAZA = '" + data.get("plaza") + "' "
				+ " AND PV_CR_TIENDA = '" + data.get("tienda")
				+ "' AND TRUNC(SENT_DATE) = TRUNC(SYSDATE) and Doc_type ='DGT' and Status = 'L' order by sent_date desc";

		addStep("Validar que se inserte el registro del documento procesado en la tabla POS_OUTBOUND_DOCS de POSUSER.");
		String doc_name = "";
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(verifyFile);
		SQLResult paso7 = executeQuery(dbPosCL, verifyFile);

		boolean step7 = paso7.isEmpty();

		if (!step7) {
			doc_name = paso7.getData(0, "DOC_NAME");
			System.out.println(doc_name);
			testCase.addQueryEvidenceCurrentStep(paso7);
		}

		System.out.println(step7);

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
		return "Construida. Construida. Validar que se envíen los cambios al Buzon de la Tienda 50APL";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_CP01_CL_ValidaCambiosBuzon";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
