package interfaces.re1;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class ATC_FT_002_RE1_ordenes_compra_ANSIX12 extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_RE1_ordenes_compra_ANSIX12_test(HashMap<String, String> data) throws Exception {

		/**
		 * Proyecto:  (Regresion Enero 2023)
		 * Validar el env�o de Ordenes de Compra del Proveedor 5074 a la Plaza 10MON (ANSIX12 - Formato 1)
		 * @author 
		 * @date 
		*/
		
		
		/*
		 * Utiler�as
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
				"AND s.filetype = 'ANSIX12 ' ;\r\n" + 
				"AND s.supplier = '" + data.get("supplier") + "'";

		// Paso 2
		String ordenesPlaza = "SELECT sup_id, plaza_id, cr_plaza \r\n" + 
				"FROM POSUSER.WM_EDI_SUPPLIER_PLAZA sp \r\n" + 
				"INNER JOIN POSUSER.plazas p \r\n" + 
				"ON (sp.plaza_id = p.id) \r\n" + 
				"WHERE  sp.sup_id = '%s' \r\n" + 
				"AND p.cr_plaza = '" + data.get("plaza") + "'";
		
	  //Paso 3
		
		String ordenesPendientes = "SELECT load_batch_id, load_date, load_status, wm_status, order_type, order_number, supplier FROM WMUSER.edi_order_control "
				+ " WHERE  wm_status = 'L' AND order_type = 'NW' AND supplier = '" + data.get("supplier") + "'";
		
	 //Paso 4
		
		String detalleOrdenCompra = "SELECT a.order_number, a.supplier, b.order_date, b.not_bef_date, b.not_aft_date, b.load_batch_id, b.load_week \r\n" + 
				"FROM WMUSER.edi_order_control a, WMUSER.edi_order_head b \r\n" + 
				"WHERE a.load_batch_id = b.load_batch_id \r\n" + 
				"AND a.load_week = b.load_week \r\n" + 
				"AND a.wm_status = 'L' \r\n" + 
				"AND a.order_type = 'NW' \r\n" + 
				"AND a.supplier = '" + data.get("supplier") + "'";
		
		//Paso 5
		
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 " 
								+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='RE1' " 
								+ " ORDER BY START_DT DESC) where rownum <=1 ";
		
		// consultas de error
				String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
						+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1"; // dbLog
				String consultaError2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
						+ "where RUN_ID='%s')WHERE rownum <= 1"; // dbLog
				String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
						+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1"; // dbLog
				
		//Paso 7
				
		String thread = "SELECT thread_id, parent_id,name, status, att5 FROM WMLOG.wm_log_thread WHERE parent_id = '%s'";
		
		//Paso 8
		
		String ordenActualizada = "SELECT load_batch_id, wm_status, doc_name, run_id_sent, date_sent, wm_code, actualization_date \r\n" + 
				"FROM WMUSER.edi_order_control \r\n" + 
				"WHERE wm_status = 'E' \r\n" + 
				"AND supplier = '" + data.get("supplier") + "'" + 
				"AND run_id_sent = '%s'";
		
		//Paso 9
		String registroDoc = "SELECT doc_name, doc_type, edi_control, supplier, status, run_id "
				+ " FROM WMUSER.edi_outbound_docs "
				+ " WHERE status = 'E' "
				+ " AND doc_type = 'EDIFACT' "
				+ " AND date_sent >= TRUNC(SYSDATE) "
				+ " AND run_id = '%s'";
		

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

		assertFalse("No se muestran la configuraci�n del documento EDI.", ValidaDocBool); // Si esta vacio, imprime
																							// mensaje

		System.out.println(ValidaDocBool); // Si no, imprime la fechas

//*************************************************Paso 2***********************************************************************************************************************

		addStep("Validar que el Proveedor pueda enviar ordenes compra hacia la Plaza en POSUSER");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);// Pos

		String ordenesPlaza_f = String.format(ordenesPlaza, id);

		System.out.println(ordenesPlaza_f);

		SQLResult ordenesPlaza_r = dbPos.executeQuery(ordenesPlaza_f);

		boolean validaOrdenesPlaza = ordenesPlaza_r.isEmpty();

		if (!validaOrdenesPlaza) {

			testCase.addQueryEvidenceCurrentStep(ordenesPlaza_r);

		}

		System.out.println(validaOrdenesPlaza);
		assertFalse("No se muestra la configuraci�n del Supplier con la Plaza a la que puede enviar informaci�n.", validaOrdenesPlaza);

		
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
		assertFalse("No se muestra  la informaci�n pendiente de procesar", validaordenesPendiente);
		
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
		assertFalse("No se muestra  el detalle de las �rdenes de compra a procesar.",validadetalleOrdenCompra);
		
//*****************************************Paso 5*****************************************************************************************************************************
		
		addStep("Ejecutar el servicio RE1.pub:run. La interfaz ser� ejecutada por el job execRE1 de Ctrl-M ");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String status = "S";

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
	
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		String run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecuci�n

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en
		
		System.out.println(successRun);
													// estatus S

		if (successRun== false) {

			String error = String.format(consultaError1, run_id);
			String error1 = String.format(consultaError2, run_id);
			String error2 = String.format(consultaError3, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontr� un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorr);

			}

			SQLResult errorIS = dbLog.executeQuery(error1);

			boolean emptyError1 = errorIS.isEmpty();

			if (!emptyError1) {

				testCase.addQueryEvidenceCurrentStep(errorIS);

			}

			SQLResult errorIS2 = dbLog.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();

			if (!emptyError2) {

				testCase.addQueryEvidenceCurrentStep(errorIS2);

			}
		}
//*********************************************************Paso 6 **********************************************************************************************
		
		addStep("Validar el registro de ejecuci�n de la interfaz en la base de datos del WMLOG.");

		boolean validateStatus = status.equals(status1);
		System.out.println(validateStatus);
		assertTrue(validateStatus, "La ejecuci�n de la interfaz no fue exitosa");

		if (validateStatus==true) {

			testCase.addQueryEvidenceCurrentStep(is);

		}

		System.out.println("La interface termino en S - "+ validateStatus);
		
//********************************************************Paso 7**********************************************************************************************
		
		addStep("Validar el registro de ejecuci�n del Proveedor en la base de datos del WMLOG.");

		
		String threads_f = String.format(thread, run_id);

		System.out.println(threads_f);

		SQLResult threads_r = dbLog.executeQuery(threads_f);
		
		String thread_id = threads_r.getData(0, "THREAD_ID");

		boolean validaThreads = threads_r .isEmpty();

		if (!validaThreads) {

			testCase.addQueryEvidenceCurrentStep(threads_r);

		}

		System.out.println(validaThreads);
		assertFalse("No se mustra el registro de ejecuci�n del Supplier ", validaThreads);
		
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

		SQLResult registroDoc_r = dbRms.executeQuery(ordenActualizada_f );

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
		return "ATC_FT_002_RE1_ordenes_compra_ANSIX12_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Env�o de �rdenes de compra a proveedores ANSIX12";
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

