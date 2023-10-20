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

public class OE06_Validar_Recepcion_Notificacion_Error_Via_Email_flujo_Normal extends BaseExecution {

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

		String tdcQueryPaso1 = " SELECT hp.header_id, to_char(hp.creation_date,'YYYYMMDDHH24MISS') creation_date, sv.legacy_id \r\n"
				+ " FROM xxfc_header_pago_servicios hp ,xxfc.xxfc_services_vendor_comm_data sv \r\n"
				+ " WHERE hp.servicio IN (SELECT service_id FROM wm_services_config \r\n"
				+ " WHERE attribute1 ='OE06' AND attribute2 ='A') \r\n"
				+ " AND hp.header_id NOT IN (SELECT header_id FROM wm_pagoservicios_finanzas \r\n"
				+ " WHERE status='L') \r\n"
				+ " AND hp.estatus = 'P' \r\n"
				+ " AND hp.servicio = sv.service_id \r\n";
				
		String tdcQueryPaso2 = " SELECT u.email FROM wm_log_group \r\n"
				+ " g INNER JOIN wm_log_user_group ug ON g.group_id = g.group_id INNER JOIN wm_log_user u ON ug.user_id = u.user_id \r\n"
				+ " WHERE g.group_name = 'ERRORES_CORRESPONSALIAS' \r\n"
				+ " AND g.interface_name = 'OE06'\r\n";
				
		String tdcQueryPaso3 = " UPDATE wm_interfase_config SET valor1 = valor1 || 'XX' \r\n"
				+ " WHERE interfase = 'OE06' AND operacion = 'FTPADMIN' \r\n";

		String tdcQueryPaso5 = " SELECT * FROM wm_log_run \r\n"
				+ " WHERE interface = 'OE06' \r\n"
				+ " and status= 'S' \r\n"
				+ " and start_dt >= trunc(sysdate) \r\n"
				+ " ORDER BY start_dt DESC \r\n";
						
		String tdcQueryPaso6 = " SELECT * FROM wm_log_thread \r\n"
				+ " WHERE parent_id = [wm_log_run.run_id] \r\n";
				
		String tdcQueryPaso7 = " SELECT * FROM wm_pagoservicios_finanzas \r\n"
				+ " WHERE status = 'L' \r\n"
				+ " AND run_id_sent IS NULL \r\n"
				+ " AND header_id = [xxfc_header_pago_servicios.header_id] \r\n"
				+ " AND sent_date IS NULL \r\n"
				+ " AND package = 'OE06' \r\n";
				 
		String tdcQueryPaso10 = " UPDATE wm_interfase_config SET valor1 = REPLACE(valor1, 'XX', '') \r\n"
				+ " WHERE interfase = 'OE06' AND operacion = 'FTPADMIN'\r\n";
				
//utileria

		
	
		/**
		 * Script de Pasos
		 * ******************************************************************************************
		 * 
		 * 
		 */

//paso 1   **************************
		
		addStep("Validar que exista información pendiente de procesar:\r\n");
				
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(tdcQueryPaso1);
		SQLResult Paso1 = executeQuery(dbAvebqa, tdcQueryPaso1);
		boolean ValidaPaso1 = Paso1.isEmpty();
		
		String headerid = ""; 
		
		if (!ValidaPaso1) {

			headerid = Paso1.getData(0,"header_id");
			testCase.addQueryEvidenceCurrentStep(Paso1);

		}

		assertFalse(ValidaPaso1, "No se encuentran resultados");

//paso 2  **************************++
		
		addStep("Validar que existan cuentas de correo electronico registradas para el grupo de errores de la interfaz OE06:\r\n");
		
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(tdcQueryPaso2);
		SQLResult Paso2 = executeQuery(dbAvebqa, tdcQueryPaso2);
		boolean ValidaPaso2 = Paso2.isEmpty();
		if (!ValidaPaso2) {

			testCase.addQueryEvidenceCurrentStep(Paso2);

		}

		assertFalse(ValidaPaso2, "No se encuentran resultados");
				
//paso 3    ************************
		
		addStep("Actualizar la dirección del servidor FTP para generar el error.\r\n");
		
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(tdcQueryPaso3);
		SQLResult Paso3 = executeQuery(dbAvebqa, tdcQueryPaso3);
		boolean ValidaPaso3 = Paso3.isEmpty();
		if (!ValidaPaso3) {

			testCase.addQueryEvidenceCurrentStep(Paso3);

		}

		assertFalse(ValidaPaso3, "No se encuentran resultados");
		
//Paso 4	************************

		addStep("Ejecutar el servicio: OE06.Pub:runPayServices. El servicio será ejecutado por el job runOE06 de Ctrl-M.\r\n");

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server + ":5555";
	
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		u.get(con);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

		
//Paso 5	************************

		addStep("Validar el registro de ejecución de la interfaz en estatus 'S'.");
		
		System.out.println(GlobalVariables.DB_HOST_LOG);
		System.out.println(tdcQueryPaso5);
		SQLResult Paso5 = executeQuery(dbLog, tdcQueryPaso5);
		boolean ValidaPaso5 = Paso5.isEmpty();
		
		String Runid = "";
		
		if (!ValidaPaso5) {
			
            Runid = Paso5.getData(0, "PARENT_ID");
			testCase.addQueryEvidenceCurrentStep(Paso5);

		}

		assertFalse(ValidaPaso5, "No se encuentran resultados");
				

//Paso 6	************************
		
		addStep("Validar el lanzamiento de threads para el procesamiento de los headers.\r\n");
		
		System.out.println(GlobalVariables.DB_HOST_LOG);
		
		String FormatoPaso6 = String.format(Runid, tdcQueryPaso6);
		
		System.out.println(FormatoPaso6);
		SQLResult Paso6 = executeQuery(dbLog, FormatoPaso6);
		boolean ValidaPaso6 = Paso6.isEmpty();
		if (!ValidaPaso6) {

			testCase.addQueryEvidenceCurrentStep(Paso6);

		}

		assertFalse(ValidaPaso6, "No se encuentran resultados");
				

		
//Paso 7	************************
		
		addStep("Validar la inserción del del registro de pago de servicio en la tabla wm_pagoservicios_finanzas de ORAFIN.\r\n");
				
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		
		String FormatoPaso7 = String.format(headerid, tdcQueryPaso7);
		
		System.out.println(FormatoPaso7);
		SQLResult Paso7 = executeQuery(dbAvebqa, FormatoPaso7);
		boolean ValidaPaso7 = Paso7.isEmpty();
		if (!ValidaPaso7) {

			testCase.addQueryEvidenceCurrentStep(Paso7);

		}

		assertFalse(ValidaPaso7, "No se encuentran resultados");
				
		
//Paso 8	************************
		
		addStep("Validar que el archivo creado [wm_pagoservicios_finanzas.doc_name] sea encuentra en el fileSystem [config/workingPath].\r\n");
			
		
//Paso 9	************************
		
		addStep("Validar la recepción del email notificando el error de envio del documento.\r\n");
			
		
//Paso 10	************************
		
		addStep("Dejar la configuración del servidor FTP por default.\r\n");
		
		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso10);
		SQLResult Paso10 = executeQuery(dbAvebqa, tdcQueryPaso10);
		boolean ValidaPaso10 = Paso10.isEmpty();
		if (!ValidaPaso10) {

			testCase.addQueryEvidenceCurrentStep(Paso10);

		}

		assertFalse(ValidaPaso10, "No se encuentran resultados");
				
		
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return " Validar la recepcion de una notificacion de error via email en el flujo normal";
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