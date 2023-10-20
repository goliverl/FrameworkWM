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

public class OE06_Validar_Procesamiento_Flujo_Normal_Envio_Documentos_CEN_heade_id2159 extends BaseExecution {

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
				+ " WHERE hp.servicio IN (SELECT service_id \r\n"
				+ " FROM wm_services_config \r\n"
				+ " WHERE attribute1 ='OE06' \r\n"
				+ " AND attribute2 ='A') AND hp.header_id NOT IN \r\n"
				+ " (SELECT header_id FROM wm_pagoservicios_finanzas WHERE status='L') \r\n"
				+ " AND hp.estatus = 'P' \r\n"
				+ " AND hp.servicio = sv.service_id \r\n";
				
		String tdcQueryPaso3 = " SELECT * FROM WMLOG.wm_log_run \r\n"
				+ " WHERE interface = 'OE06' \r\n"
				+ " and status= 'S' \r\n"
				+ " and start_dt >= trunc(sysdate) \r\n"
				+ " ORDER BY start_dt DESC \r\n";
				
		String tdcQueryPaso4 = " SELECT * FROM WMLOG.wm_log_thread \r\n"
				+ " WHERE parent_id = %s \r\n";
		

		String tdcQueryPaso5 =" SELECT * FROM wm_pagoservicios_finanzas \r\n"
				+ " WHERE status = 'E' \r\n"
				+ " AND run_id_sent = %s \r\n"
				+ " AND header_id = %s \r\n"
				+ " AND TRUNC(sent_date) = TRUNC(SYSDATE) \r\n"
				+ " AND package = 'OE06' \r\n";

//utileria

		
	
		/**
		 * Script de Pasos
		 * ******************************************************************************************
		 * 
		 * 
		 */

//paso 1   **************************
		
		addStep(" Validar que exista información pendiente de procesar:\r\n ");
				
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(tdcQueryPaso1);
		SQLResult Paso1 = executeQuery(dbAvebqa, tdcQueryPaso1);
		boolean ValidaPaso1 = Paso1.isEmpty();
		
		String id = "";
		
		if (!ValidaPaso1) {

			id = Paso1.getData(0, "id");
			testCase.addQueryEvidenceCurrentStep(Paso1);

		}

		assertFalse(ValidaPaso1, "No se encuentran resultados");

//paso 2  **************************++
		
		addStep(" Ejecutar el servicio: OE06.Pub:runPayServices. El servicio será ejecutado con el job runOE06 desde Ctrl-M.\r\n ");
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server + ":5555";
	
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		u.get(con);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));

				
		
//paso 3    ************************
		
		addStep("Validar el registro de ejecución de la interfaz en estatus 'S'.");
		
		System.out.println(GlobalVariables.DB_HOST_LOG);
		System.out.println(tdcQueryPaso3);
		SQLResult Paso3 = executeQuery(dbLog, tdcQueryPaso3);
		boolean ValidaPaso3 = Paso3.isEmpty();
		
		String Runid = "";
		
		if (!ValidaPaso3) {
			
			Runid = Paso3.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(Paso3);

		}

		assertFalse(ValidaPaso3, "No se encuentran resultados");
		
//Paso 4	************************

		addStep("Validar el lanzamiento de threads para el procesamiento de los headers.\r\n");
		
		System.out.println(GlobalVariables.DB_HOST_LOG);
		
		String FormatoPaso4 = String.format(Runid, tdcQueryPaso4);
		System.out.println(FormatoPaso4);
		SQLResult Paso4 = executeQuery(dbLog, FormatoPaso4);
		boolean ValidaPaso4 = Paso4.isEmpty();
		
		String Parentid = "";
		
		if (!ValidaPaso4) {

			Parentid = Paso4.getData(0, "PARENT_ID");
			testCase.addQueryEvidenceCurrentStep(Paso4);

		}

		assertFalse(ValidaPaso4, "No se encuentran resultados");

		
//Paso 5	************************

		addStep("Validar la inserción del del registro de pago de servicio en la tabla wm_pagoservicios_finanzas de ORAFIN.\r\n");
		
		
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		
		String FormatoPaso5 = String.format(Parentid,id, tdcQueryPaso5);
		System.out.println(FormatoPaso5);
		SQLResult Paso5 = executeQuery(dbAvebqa, FormatoPaso5);
		boolean ValidaPaso5 = Paso5.isEmpty();
		if (!ValidaPaso5) {

			testCase.addQueryEvidenceCurrentStep(Paso5);

		}

		assertFalse(ValidaPaso5, "No se encuentran resultados");
				

//Paso 6	************************
		
		addStep("Validar que el archivo creado [wm_pagoservicios_finanzas.doc_name] sea eliminado del fileSystem [config/workingPath].");

		
//Paso 7	************************
		
		addStep("Validar que el archivo creado [wm_pagoservicios_finanzas.doc_name] sea enviado al fileSystem del servidor FTP [config/pathAdmin].\r\n");
				
		
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "Validar el Procesamiento del Flujo normal de envio de documentos CEN con header_id 2159 ";
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
