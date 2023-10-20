package interfaces.re1;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.controlm.ControlM;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class ATC_FT_004_RE1_ordenes_compra_ANSIX12_IMMEX extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_RE1_ordenes_compra_ANSIX12_IMMEX_test(HashMap<String, String> data) throws Exception {
		
		/*No se tienen datos en las tablas de la primera consulta */
		/*
		 * Utilerías
		 ********************************************************************************************************************************************/

		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1

		String validaDoc = "SELECT s.id, s.supplier, s.filetype, i.command_id, i.qualifier, s.status, s.cost, s.surtirporpieza, s.format_type, s.max_attempt \r\n" + 
				"FROM WMUSER.wm_edi_map_supplier s, WMUSER.wm_edi_interchange i \r\n" + 
				"WHERE s.supplier = i.supplier \r\n" + 
				"AND s.status = i.status_type \r\n" + 
				"AND s.filetype = 'ANSIX12 ' \r\n" + 
				"AND s.supplier = '" + data.get("supplier") + "'";

		// Paso 2
		String ordenesPlaza = "SELECT sup_id, plaza_id, cr_plaza \r\n" + 
				"FROM POSUSER.WM_EDI_SUPPLIER_PLAZA sp \r\n" + 
				"INNER JOIN POSUSER.plazas p \r\n" + 
				"ON (sp.plaza_id = p.id) \r\n" + 
				"WHERE  sp.sup_id = '%s' \r\n" + 
				"AND p.cr_plaza = '" + data.get("plaza") + "'";
		
	  //Paso 3
		
		String ordenesPendientes = "SELECT load_batch_id, load_date, load_status, wm_status, order_type, order_number, supplier \r\n"
				+ "FROM WMUSER.edi_order_control \r\n"
				+ " WHERE  wm_status = 'L' \r\n"
				+ "AND order_type = 'NW' \r\n"
				+ "AND supplier = '" + data.get("supplier") + "'";
		
	 //Paso 4
		
		String detalleOrdenCompra = "SELECT a.order_number, a.supplier, b.order_date, b.not_bef_date, b.not_aft_date, b.load_batch_id, b.load_week \r\n" + 
				"FROM WMUSER.edi_order_control a, WMUSER.edi_order_head b \r\n" + 
				"WHERE a.load_batch_id = b.load_batch_id \r\n" + 
				"AND a.load_week = b.load_week \r\n" + 
				"AND a.wm_status = 'L' \r\n" + 
				"AND a.order_type = 'NW' \r\n" + 
				"AND a.supplier = '" + data.get("supplier") + "'";
		
		//Paso 5
		
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server \r\n" +
				  " FROM WMLOG.WM_LOG_RUN Tbl \r\n" + 
                "WHERE INTERFACE='RE1' \r\n" +
                "AND start_dt >= trunc(SYSDATE) \r\n" + 
				"ORDER BY START_DT DESC) \r\n"
			  + "where rownum <=1 ";

		
		// consultas de error
		String consultaError1 = " select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE \r\n"+
			 "from wmlog.WM_LOG_ERROR \r\n" + 
			 "where RUN_ID='%s' \r\n" +
			 "and rownum <=1"; // dbLog
		
		String consultaError2 = " select description,MESSAGE \r\n" +
			 "from wmlog.WM_LOG_ERROR \r\n" +
			 "where RUN_ID='%s' \r\n" +
			 "and rownum <= 1"; // dbLog
		
		String consultaError3 = " select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 \r\n" +
				"from wmlog.WM_LOG_ERROR \r\n" + 
				"where RUN_ID='%s' \r\n" +
			    "and rownum <= 1"; // dbLog
				
		//Paso 7
				
		String thread = "SELECT thread_id, parent_id,name, status, att5 \r\n"
				+ "FROM WMLOG.wm_log_thread \r\n"
				+ "WHERE parent_id = '%s'";
		
		//Paso 8
		
		String ordenActualizada = "SELECT load_batch_id, wm_status, doc_name, run_id_sent, date_sent, wm_code, actualization_date \r\n" + 
				"FROM WMUSER.edi_order_control \r\n" + 
				"WHERE wm_status = 'E' \r\n" + 
				"AND supplier = '" + data.get("supplier") + "' \r\n" + 
				"AND run_id_sent = '%s'";
		
		//Paso 9
		String registroDoc = "SELECT doc_name, doc_type, edi_control, supplier, status, run_id \r\n"
				+ " FROM WMUSER.edi_outbound_docs \r\n"
				+ " WHERE status = 'E' \r\n"
				+ " AND doc_type = 'EDIFACT' "
				+ " AND date_sent >= TRUNC(SYSDATE) \r\n"
				+ " AND run_id = '%s'";
		
		 SeleniumUtil u = null;
		
		 testCase.setProject_Name("Impactos de Núcleo a IMMEX");
		
		 testCase.setPrerequisites(data.get("prerequicitos"));
		

//********************************************************************************************************************************************************************************		

		/* Pasos */

//************************************************Paso 1********************************************************************************************************************************

		addStep("Validar el tipo de documento configurado para el Proveedor para intercambio de documentos EDI en RETEK ");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);// RMS
		

		System.out.println(validaDoc);

		SQLResult validaDocRes = dbRms.executeQuery(validaDoc);

		String id = validaDocRes.getData(0, "ID");

		System.out.println("wm_edi_map_supplier.id = " + id); // la imprime

		boolean ValidaDocBool = validaDocRes.isEmpty(); // checa que el string contenga datos

		if (!ValidaDocBool) {

			testCase.addQueryEvidenceCurrentStep(validaDocRes); // Si no esta vacio, lo agrega a la evidencia
		}

		assertFalse("No se muestran la configuración del documento EDI.", ValidaDocBool); // Si esta vacio, imprime
																							// mensaje

		System.out.println(ValidaDocBool); // Si no, imprime la fechas

//*************************************************Paso 2***********************************************************************************************************************

		addStep("Validar que el Proveedor pueda enviar ordenes compra hacia la Plaza en POSUSER");


		String ordenesPlaza_f = String.format(ordenesPlaza, id);

		System.out.println(ordenesPlaza_f);

		SQLResult ordenesPlaza_r = dbPos.executeQuery(ordenesPlaza_f);

		boolean validaOrdenesPlaza = ordenesPlaza_r.isEmpty();

		if (!validaOrdenesPlaza) {

			testCase.addQueryEvidenceCurrentStep(ordenesPlaza_r);

		}

		System.out.println(validaOrdenesPlaza);
		assertFalse("No se muestra la configuración del Supplier con la Plaza a la que puede enviar información.", validaOrdenesPlaza);

		
//**********************************************************Paso 3*************************************************************************************************************		
		
		addStep("Validar que existan ordenes de compra pendientes de enviar para el Proveedor en RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);// RMS

		

		System.out.println(ordenesPendientes);

		SQLResult ordenesPendientes_r = dbRms.executeQuery(ordenesPendientes);

		boolean validaordenesPendiente = ordenesPendientes_r.isEmpty();

		if (!validaordenesPendiente) {

			testCase.addQueryEvidenceCurrentStep(ordenesPendientes_r);

		}

		System.out.println(validaordenesPendiente);
		assertFalse("No se muestra  la información pendiente de procesar", validaordenesPendiente);
		
//********************************************Paso 4**************************************************************************************************************************
		
		
		addStep("Validar que existe detalle de las ordenes de compra pendientes de enviar en RETEK. ");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);// RMS

		System.out.println(detalleOrdenCompra);

		SQLResult detalleOrdenCompra_r = dbRms.executeQuery(detalleOrdenCompra);

		boolean validadetalleOrdenCompra = detalleOrdenCompra_r.isEmpty();

		if (!validadetalleOrdenCompra) {

			testCase.addQueryEvidenceCurrentStep(detalleOrdenCompra_r);

		}

		System.out.println(validadetalleOrdenCompra);
		assertFalse("No se muestra  el detalle de las órdenes de compra a procesar.",validadetalleOrdenCompra);
		
//*****************************************Paso 5*****************************************************************************************************************************
		
		addStep("Ejecutar el servicio RE1.pub:run. La interfaz será ejecutada por el job execRE1 de Ctrl-M ");

		
		        u  = new SeleniumUtil(new ChromeTest(), true);
						
				JSONObject obj = new JSONObject(data.get("job"));

				addStep("Jobs en  Control M ");
				Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
				//testCase.addPaso("Paso con addPaso");
				addStep("Login");
				u.get(data.get("server"));
				u.hardWait(40);
				u.waitForLoadPage();
				CM.logOn();

				addStep("Inicio de job ");
				ControlM control = new ControlM(u, testCase, obj);
				boolean flag = control.searchJob();
				assertTrue(flag);
				String resultado = control.executeJob();
				System.out.println("Resultado de la ejecucion -> " + resultado);
				System.out.println ("Valor de output :" +control.getOutput());
				Boolean casoPasado = true;
				if(resultado.equals("Failure")) {
				casoPasado = false;
				}		
				assertTrue(casoPasado);
				//assertNotEquals("Failure",resultado);
				control.closeViewpoint();

//*********************************************************Paso 6 **********************************************************************************************
		
		addStep("Validar el registro de ejecución de la interfaz en la base de datos del WMLOG.");

		String status = "S";
		String searchedStatus = "R";
		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = "";// guarda el run id de la
													// ejecución

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en																// estatus R
		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en
		
		System.out.println("El status es S: "+ successRun);
													// estatus S

		if (status1 == "E") {

			String error = String.format(consultaError1, run_id); //Primer consulta de error
			String error1 = String.format(consultaError2, run_id); //Segunda consulta de error
			String error2 = String.format(consultaError3, run_id); //Tercera consulta de error

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			//Primer consulta de error
			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorr);

			}
			//Segunda consulta de error
			SQLResult errorIS = dbLog.executeQuery(error1);

			boolean emptyError1 = errorIS.isEmpty();

			if (!emptyError1) {

				testCase.addQueryEvidenceCurrentStep(errorIS);

			}

			//Tercera consulta de error
			SQLResult errorIS2 = dbLog.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();

			if (!emptyError2) {

				testCase.addQueryEvidenceCurrentStep(errorIS2);

			}
		}

		if (successRun) {

			testCase.addQueryEvidenceCurrentStep(is);

		}
		

		System.out.println(tdcIntegrationServerFormat);
	
	//	assertTrue(successRun, "La ejecución de la interfaz no fue exitosa");

		
		
		
//********************************************************Paso 7**********************************************************************************************
		
		addStep("Validar el registro de ejecución del Proveedor en la base de datos del WMLOG.");

		
		String threads_f = String.format(thread, run_id);

		System.out.println(threads_f);

		SQLResult threads_r = dbLog.executeQuery(threads_f);
		
		String thread_id = threads_r.getData(0, "THREAD_ID");

		boolean validaThreads = threads_r .isEmpty();

		if (!validaThreads) {

			testCase.addQueryEvidenceCurrentStep(threads_r);

		}

		System.out.println(validaThreads);
		assertFalse("No se mustra el registro de ejecución del Supplier ", validaThreads);
		
//***********************************************Paso 8**********************************************************************************************************

		
addStep("Validar que la order procesada sea actualizada a estatus E en RETEK.");

		
		String ordenActualizada_f = String.format(ordenActualizada, thread_id);

		System.out.println(ordenActualizada_f);

		SQLResult ordenActualizada_r = dbRms.executeQuery(ordenActualizada_f );

		boolean validaOrdenActualizada = ordenActualizada_r  .isEmpty();

		if (!validaOrdenActualizada) {

			testCase.addQueryEvidenceCurrentStep(ordenActualizada_r);

		}

		System.out.println(validaOrdenActualizada);
		assertFalse("El registro de la orden de compra no fue actualizado a estatus E  ", validaOrdenActualizada);
		
//*****************************************Paso 9*****************************************************************************************************************
addStep("Validar que el documento EDI enviado sea registrado en la tabla edi_outbound_docs de RETEK ");

		
		String registroDoc_f = String.format(registroDoc, thread_id);

		System.out.println(registroDoc_f);

		SQLResult registroDoc_r = dbRms.executeQuery(registroDoc_f );

		boolean validaregistroDoc = registroDoc_r  .isEmpty();

		if (!validaregistroDoc) {

			testCase.addQueryEvidenceCurrentStep(registroDoc_r);

		}

		System.out.println(validaregistroDoc);
		assertFalse("No se encontro el registro del documento enviado. ", validaregistroDoc);
		
		
		
		
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_004_RE1_ordenes_compra_ANSIX12_IMMEX_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. ATC-FT-009-Validar ejecución de interface FEMSA_RE1 ANSIX12";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Dora Elia Reyes Obeso";
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

