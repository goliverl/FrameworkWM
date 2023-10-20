package interfaces.OE06;

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

public class OE06_Validar_Procesamiento_Envio_Documento_Interfaz_OL9 extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LOG,
				GlobalVariables.DB_USER_LOG, GlobalVariables.DB_PASSWORD_LOG);
		utils.sql.SQLUtil dbAvebqa = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,
				GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		
		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		String tdcQueryPaso1 = " SELECT doc_name , id, created_date FROM wm_ol9_outbound_docs \r\n"
				+ " WHERE TRUNC(created_date) >= TRUNC(SYSDATE - 7) \r\n"
				+ " AND SUBSTR(doc_name,1,3) = 'CEN' \r\n"
				+ " AND id NOT IN (SELECT header_id FROM wm_pagoservicios_finanzas \r\n"
				+ " WHERE SUBSTR(doc_name,1,3) = 'CEN' \r\n"
				+ " AND doc_name NOT LIKE 'CEN%_OS.DAT') \r\n"
				+ " AND service_id IN (SELECT service_id FROM wm_services_config \r\n"
				+ " WHERE attribute1 = 'OE06' \r\n"
				+ " AND attribute2 ='A') \r\n" ;
				
		String tdcQueryPaso4 = " SELECT * FROM WMLOG.wm_log_run \r\n"
				+ " WHERE interface = 'OE06' \r\n"
				+ " and status= 'S' \r\n"
				+ " and start_dt >= trunc(sysdate) \r\n"
				+ " ORDER BY start_dt DESC \r\n";
		
		String tdcQueryPaso4_1 = "  SELECT * FROM WMLOG.WM_LOG_THREAD \r\n"
				+ " WHERE PARENT_ID = %s \r\n";
				
		String tdcQueryPaso6 = " SELECT * FROM wm_pagoservicios_finanzas \r\n"
				+ " WHERE status = 'E' \r\n"
				+ " AND run_id_sent = %s \r\n"
				+ " AND header_id = %s \r\n"
				+ " AND TRUNC(sent_date) = TRUNC(SYSDATE) \r\n"
				+ " AND package = 'OL9' \r\n";
			



//utileria

		/**
		 * Script de Pasos
		 * ******************************************************************************************
		 * 
		 * 
		 */

//paso 1   ************************

		addStep("Validar que exista información pendiente de enviar:\r\n");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso1);
		SQLResult Paso1 = executeQuery(dbAvebqa, tdcQueryPaso1);
		boolean ValidaPaso1 = Paso1.isEmpty();

		String id = "";
		String doc_name = "";
		
		if (!ValidaPaso1) {

			id = Paso1.getData(0, "id");
			doc_name = Paso1.getData(0, "doc_name");
			testCase.addQueryEvidenceCurrentStep(Paso1);

		}

		assertFalse(ValidaPaso1, "No se encuentran resultados");

//paso 2    ************************

		addStep("Validar que el archivo exista en el fileSystem [config/getpathAcpt].\r\n");

//paso 3    ************************

		addStep("Ejecutar el servicio: OE06.Pub:runPayServices. El servicio será ejecutado por el job runOE06 de Ctrl-M.\r\n");

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server + ":5555";

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		u.get(con);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

//Paso 4	************************

		addStep("Validar el registro de ejecución de la interfaz en estatus 'S'.\r\n");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso4);
		SQLResult Paso4 = executeQuery(dbLog, tdcQueryPaso4);
		boolean ValidaPaso4 = Paso4.isEmpty();
		
		String Runid = "";
		
		if (!ValidaPaso4) {
			
			Runid = Paso4.getData(0,"RUN_ID");
			testCase.addQueryEvidenceCurrentStep(Paso4);

		}

		assertFalse(ValidaPaso4, "No se encuentran resultados");

//Paso 4_1	************************

		addStep("Validar el registro de ejecución de la interfaz en estatus 'S'.\r\n");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		
		String FormatoPaso4_1 = String.format(Runid, tdcQueryPaso4_1);
		System.out.println(FormatoPaso4_1);
		SQLResult Paso4_1 = executeQuery(dbLog, FormatoPaso4_1);
		boolean ValidaPaso4_1 = Paso4_1.isEmpty();
		
		String Parentid = "";
		
		if (!ValidaPaso4_1) {

			Parentid = Paso4_1.getData(0, "PARENT_ID");
			testCase.addQueryEvidenceCurrentStep(Paso4_1);

		}

		assertFalse(ValidaPaso4_1, "No se encuentran resultados");

//Paso 5	************************

		addStep("Validar que el archivo [wm_ol9_outbound_docs.doc_name] fue enviado al fileSystem del servidor FTP [config/pathAdmin].");

//Paso 6	************************

		addStep("Validar el registro del envio del archivo en la tabla wm_pagoservicios_finanzas de ORAFIN.");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		
		String FormatoPaso6 = String.format(Runid,Parentid,tdcQueryPaso6);
		
		System.out.println(FormatoPaso6);
		SQLResult Paso6 = executeQuery(dbAvebqa, FormatoPaso6);
		boolean ValidaPaso6 = Paso6.isEmpty();
		if (!ValidaPaso6) {

			testCase.addQueryEvidenceCurrentStep(Paso6);

		}

		assertFalse(ValidaPaso6, "No se encuentran resultados");

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "Validar el procesamiento del envio de documentos a la interfaz OL9";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Conciliacion de todas las transacciones (Exitosas y Canceladas) y garantizar que no existan transacciones a las "
				+ "cuales se le deba dar algún tipo de gestión o aclaración";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAutomation";
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

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

}