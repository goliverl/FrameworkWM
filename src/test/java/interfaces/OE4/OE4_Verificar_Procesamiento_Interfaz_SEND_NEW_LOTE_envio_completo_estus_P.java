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

public class OE4_Verificar_Procesamiento_Interfaz_SEND_NEW_LOTE_envio_completo_estus_P extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_OE4_004_Verificar_Interfaz_SEND_NEW_LOTE_RESPONSE_No_Completo(HashMap<String, String> data) throws Exception {

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
				
		
		String tdcQueryPaso1_3 = " SELECT * \r\n"
				+ " FROM wmuser.xxfc_dollar_sales a, xxfc_maestro_de_crs_v m \r\n"
				+ " WHERE a.FECHA_RECEPCION>=Trunc(SYSDATE-30) \r\n"
				+ " and a.wm_status = 'L' \r\n"
				+ " and a.plaza = m.oracle_cr_superior \r\n"
				+ " and a.tienda = m.oracle_cr \r\n"
				+ " and m.oracle_cia = %s \r\n"
				+ " ORDER BY TRANS_ID\r\n";
				
		String tdcQueryPaso3 = "SELECT * \r\n"
				+ " FROM wmlog.wm_log_run \r\n"
				+ " WHERE interface='OE4' \r\n"
				+ " and start_dt>=trunc(sysdate) \r\n"
				+ " and status = 'S' \r\n"
				+ " ORDER BY start_dt DESC\r\n";
				

		String tdcQueryPaso4 = " SELECT * \r\n"
				+ " FROM WMUSER.xxfc_lotes_dollars \r\n"
				+ " WHERE fecha_envio >= TRUNC(SYSDATE) \r\n"
				+ " AND STATUS_ENVIO = 'P' \r\n"
				+ " AND ORACLE_CIA = '%s' \r\n";
				


		String tdcQueryPaso5 = " SELECT * \r\n"
				+ " FROM WMUSER.xxfc_dollar_sales \r\n"
				+ " WHERE TRANS_ID = [xxfc_dollar_sales.trans_id] \r\n"
				+ " AND FECHA_ENVIO >= TRUNC(SYSDATE) \r\n"
				+ " AND WM_STATUS = 'P' \r\n";
				
				

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

// Paso 1_2 ***************************

		addStep(" Validar que los datos del servidor FTP del Proveedor muestren error.\r\n");

// Paso 1_3 ***************************

		addStep("Verificar que existan datos pendientes de procesar en la BD de ORAFIN.");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		String FormatoPaso2 = String.format(tdcQueryPaso1_3, OraCia);

		SQLResult Paso2 = executeQuery(dbEbs, FormatoPaso2);
		System.out.println(Paso2);

		boolean ValidacionPaso2 = Paso2.isEmpty();

		if (!ValidacionPaso2) {

			testCase.addQueryEvidenceCurrentStep(Paso2);

		}

		System.out.println(ValidacionPaso2);

		assertFalse(ValidacionPaso2, "No se obtiene informacion de la consulta");

// Paso 2 ****************************

		addStep("Ejecutar el servicio OE4.pub:run, solicitando el job: runOE4.\r\n");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

//	Paso 3	************************

		addStep("Validar que la interface finalice correctamente en el WMLOG.\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		System.out.println(tdcQueryPaso3);
		SQLResult Paso3 = executeQuery(dbLog, tdcQueryPaso3);

		boolean ValidacionPaso3 = Paso3.isEmpty();
		if (!ValidacionPaso3) {

			testCase.addQueryEvidenceCurrentStep(Paso3);

		}

		System.out.println(ValidacionPaso3);

		assertFalse(ValidacionPaso3, "No se obtiene informacion de la consulta");

//		Paso 4	************************

		addStep("Verificar la actualización de datos en la tabla XXFC_LOTES_DOLLARS de ORAFIN.\r\n");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		SQLResult Paso4 = executeQuery(dbEbs, tdcQueryPaso4);

		System.out.println(tdcQueryPaso4);

		boolean ValidacionPaso4 = Paso4.isEmpty();

		if (!ValidacionPaso4) {

			testCase.addQueryEvidenceCurrentStep(Paso4);

		}

		System.out.println(ValidacionPaso4);

		assertFalse(ValidacionPaso4, "No se obtiene informacion de la consulta");

// Paso 5 ******************************

		addStep("Verificar la actualización de los detalles en la tabla XXFC_DOLLAR_SALES de ORAFIN.\r\n");

		System.out.println(GlobalVariables.DB_HOST_Ebs);

		System.out.println(tdcQueryPaso5);

		SQLResult Paso5 = executeQuery(dbEbs, tdcQueryPaso5);

		boolean ValidacionPaso5 = Paso5.isEmpty();

		if (!ValidacionPaso5) {

			testCase.addQueryEvidenceCurrentStep(Paso5);

		}

		System.out.println(ValidacionPaso5);

		assertFalse(ValidacionPaso5, "No se obtiene informacion de la consulta");

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
		return "Bloqueado. Verificar procesamiento de la interfaz, modulo SEND_NEW_LOTE, errorType = 3";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_OE4_004_Verificar_Interfaz_SEND_NEW_LOTE_RESPONSE_No_Completo";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
