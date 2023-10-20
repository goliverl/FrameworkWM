package interfaces.ro4;

import static org.junit.Assert.assertFalse;
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

public class RO4_MEX_Facturas_y_Notas_Manual extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_RO4_002_MEX_Facturas_y_Notas_Manual(HashMap<String, String> data) throws Exception {

		/*
		 * UtilerÃ­as
		 ***********************************************************************************************/

		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
				GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS,
				GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * Variables
		 * ***********************************************************************************************
		 * 
		 * 
		 */

		String rangoFechas = "SELECT DISTINCT week_nb, to_char(min_date,'DD/MM/RRRR'), to_char(max_date,'DD/MM/RRRR') FROM "
				+ "WMUSER.wm_cat_ro4_weeks WHERE max_date < SYSDATE AND "
				+ "week_nb IN nvl((SELECT DISTINCT MAX(decode(status,'E',week_nb,'S',week_nb+1,1)) "
				+ "FROM WMUSER.wm_log_ro4 WHERE cr_plaza ='" + data.get("plaza")
				+ "'AND week_nb IN (SELECT DISTINCT MAX(week_nb) " + "FROM WMUSER.wm_log_ro4 WHERE cr_plaza ='"
				+ data.get("plaza") + "') ),1)";// posUser

		String minFechasColumna = "TO_CHAR(MIN_DATE,'DD/MM/RRRR')";

		String maxFechasColumna = "to_char(max_date,'DD/MM/RRRR')";

		String pendiente_rechead_temp_temp_1 = "select * from (SELECT SHIPMENT,ORDER_NO,SUPPLIER_ID,RECEIVE_DATE,TERMS_DESC"
				+ " FROM fem_rechead_temp" + " WHERE TRUNC(receive_date ) BETWEEN TO_DATE('%s', 'dd,MM,yyyy')"
				+ " AND TO_DATE('%s', 'dd,MM,yyyy')" + " AND reference_3 IS NULL " + " AND reference_8 IS NULL "
				+ " AND loc " + " IN (SELECT store FROM store WHERE SUBSTR(store_name10, 1,5) = '" + data.get("plaza")
				+ "') order by receive_date desc) where rownum <=1";// RMS

		String pendiente_rechead_temp_temp_2 = "select * from (SELECT CURRENCY_CODE,EXCHANGE_RATE,LOC,COMMENTS,REFERENCE_1"
				+ " FROM fem_rechead_temp" + " WHERE TRUNC(receive_date ) BETWEEN TO_DATE('%s', 'dd,MM,yyyy')"
				+ " AND TO_DATE('%s', 'dd,MM,yyyy')" + " AND reference_3 IS NULL " + " AND reference_8 IS NULL "
				+ " AND loc " + " IN (SELECT store FROM store WHERE SUBSTR(store_name10, 1,5) = '" + data.get("plaza")
				+ "') order by receive_date desc) where rownum <=1";// RMS

		String pendiente_rechead_temp_temp_3 = "select * from (SELECT REFERENCE_3,REFERENCE_4,REFERENCE_5,REFERENCE_6,REFERENCE_7,REFERENCE_10,MERCH_TYPE,REMISION"
				+ " FROM fem_rechead_temp" + " WHERE TRUNC(receive_date ) BETWEEN TO_DATE('%s', 'dd,MM,yyyy')"
				+ " AND TO_DATE('%s', 'dd,MM,yyyy')" + " AND reference_3 IS NULL " + " AND reference_8 IS NULL "
				+ " AND loc " + " IN (SELECT store FROM store WHERE SUBSTR(store_name10, 1,5) = '" + data.get("plaza")
				+ "') order by receive_date desc) where rownum <=1";// RMS

		String pendiente_fem_rtvhead_temp_1 = "select * from (SELECT RTV_ORDER_NO,SUPPLIER_ID,TERMS_DESC,CURRENCY_CODE,EXCHANGE_RATE "
				+ "FROM RMS100.fem_rtvhead_temp WHERE TRUNC(completed_date)  BETWEEN TO_DATE('%s','dd-mm-yy') AND "
				+ " TO_DATE('%s','dd-mm-yy') AND reference_3 IS NULL AND reference_8 IS NULL AND store IN (SELECT store FROM store WHERE "
				+ "substr(store_name10, 1,5) ='" + data.get("plaza") + "'))where rownum <=1";// RMS

		String pendiente_fem_rtvhead_temp_2 = "select * from (SELECT STORE,WH,COMPLETED_DATE,RET_AUTH_NUM,HANDLING_COST,REFERENCE_3,REFERENCE_8 "
				+ "FROM RMS100.fem_rtvhead_temp WHERE TRUNC(completed_date)  BETWEEN TO_DATE('%s','dd-mm-yy') AND "
				+ " TO_DATE('%s','dd-mm-yy') AND reference_3 IS NULL AND reference_8 IS NULL AND store IN "
				+ "(SELECT store FROM store WHERE substr(store_name10, 1,5) ='" + data.get("plaza")
				+ "'))where rownum <=1";// RMS

		String verifConfig = "SELECT * FROM WMUSER.wm_cfg_launcher WHERE interface_name = "
				+ "'RO4' AND manual_status = 'A' AND attribute1 = '" + data.get("plaza") + "'";// posUser

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server,"
				+ " (END_DT - START_DT)*24*60 FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE LIKE '%RO4_MX%' ORDER BY START_DT DESC) "
				+ "where rownum <=1";// WMLOG
		String consulta6 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// WMLOG
		String consulta61 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG
		String consulta62 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// WMLOG

		String validar_threads_1_1 = "SELECT * from(SELECT SHIPMENT,ORDER_NO,SUPPLIER_ID,RECEIVE_DATE,CURRENCY_CODE"
				+ " FROM RMS100.fem_rechead_temp WHERE SHIPMENT='%s'AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND loc IN (SELECT store FROM"
				+ " store WHERE substr(store_name10, 1,5) = '" + data.get("plaza") + "')) where ROWNUM<=1";// RMS

		String validar_threads_1_2 = "SELECT * from(SELECT EXCHANGE_RATE,LOC,LOC_TYPE,COMMENTS,REFERENCE_1 FROM"
				+ " RMS100.fem_rechead_temp WHERE SHIPMENT='%s'"
				+ " AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND loc IN (SELECT store"
				+ " FROM store WHERE substr(store_name10, 1,5) = '" + data.get("plaza") + "')) where ROWNUM<=1";// RMS

		String validar_threads_1_3 = "SELECT * from(SELECT REFERENCE_2,REFERENCE_3,REFERENCE_4,REFERENCE_5,REFERENCE_6 "
				+ "FROM RMS100.fem_rechead_temp WHERE SHIPMENT='%s'"
				+ " AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND loc IN (SELECT store FROM"
				+ " store WHERE substr(store_name10, 1,5) = '" + data.get("plaza") + "')) where ROWNUM<=1";// RMS

		String validar_threads_1_4 = "SELECT * from(SELECT REFERENCE_7,REFERENCE_8,REFERENCE_9,REFERENCE_10,MERCH_TYPE,REMISION"
				+ " FROM RMS100.fem_rechead_temp WHERE SHIPMENT='%s'"
				+ " AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND loc IN (SELECT store "
				+ "FROM store WHERE substr(store_name10, 1,5) = '" + data.get("plaza") + "')) where ROWNUM<=1";// RMS

		String validar_threads_2_1 = "SELECT * from(SELECT RTV_ORDER_NO,SUPPLIER_ID,TERMS_DESC,CURRENCY_CODE,EXCHANGE_RATE FROM "
				+ "RMS100.fem_rtvhead_temp WHERE RTV_ORDER_NO='%s'"
				+ "AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND store IN (SELECT store FROM store WHERE"
				+ " substr(store_name10, 1,5) = '" + data.get("plaza") + "')) where ROWNUM<=1";// RMS

		String validar_threads_2_2 = "SELECT * from(SELECT STORE,WH,COMPLETED_DATE,RET_AUTH_NUM,HANDLING_PCT FROM "
				+ "RMS100.fem_rtvhead_temp WHERE RTV_ORDER_NO='%s'"
				+ "AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND store IN (SELECT store "
				+ "FROM store WHERE substr(store_name10, 1,5) = '" + data.get("plaza") + "')) where ROWNUM<=1";// RMS

		String validar_threads_2_3 = "SELECT * from(SELECT HANDLING_COST,COMMENT_DESC,REFERENCE_1,REFERENCE_2,REFERENCE_3 FROM "
				+ "RMS100.fem_rtvhead_temp WHERE RTV_ORDER_NO='%s'"
				+ " AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND store IN (SELECT store FROM "
				+ "store WHERE substr(store_name10, 1,5) = '" + data.get("plaza") + "')) where ROWNUM<=1";// RMS

		String validar_threads_2_4 = "SELECT * from(SELECT REFERENCE_4,REFERENCE_5,REFERENCE_6,REFERENCE_7,REFERENCE_8 FROM "
				+ "RMS100.fem_rtvhead_temp WHERE RTV_ORDER_NO='%s'"
				+ " AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND store IN (SELECT store FROM "
				+ "store WHERE substr(store_name10, 1,5) = '" + data.get("plaza") + "')) where ROWNUM<=1";// RMS

		String validar_threads_2_5 = "SELECT * from(SELECT REFERENCE_9,REFERENCE_10,MERCH_TYPE,FEM_REMISION FROM "
				+ "RMS100.fem_rtvhead_temp WHERE RTV_ORDER_NO='%s'"
				+ " AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND store IN (SELECT store"
				+ " FROM store WHERE substr(store_name10, 1,5) = '" + data.get("plaza") + "')) where ROWNUM<=1";// RMS

		String consulta5 = " SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS " + "FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID='%s'";
		String consulta51 = " SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 " + "FROM WMLOG.WM_LOG_THREAD"
				+ " WHERE PARENT_ID='%s'";//

		String FAC_EBS = "SELECT  INVOICE_ID, INVOICE_NUM, INVOICE_DATE, VENDOR_ID FROM ap.ap_invoices_interface WHERE invoice_id =%s";
		String FAC_EBS2 = "SELECT  vendor_name, vendor_site_id , invoice_amount, invoice_currency_code FROM ap.ap_invoices_interface WHERE invoice_id =%s";
		String FAC_EBS3 = "SELECT  description , ORG_ID FROM ap.ap_invoices_interface WHERE invoice_id =%s";

		String FAC_EBS_LIN = "SELECT invoice_id, invoice_line_id, line_number,line_type_lookup_code,amount FROM ap.ap_invoice_lines_interface WHERE invoice_id =%s";
		String FAC_EBS_LIN2 = "SELECT accounting_date, description,amount_includes_tax_flag, prorate_across_flag, tax_code FROM ap.ap_invoice_lines_interface WHERE invoice_id =%s";
		String FAC_EBS_LIN3 = "SELECT dist_code_concatenated, awt_group_name,created_by, creation_date FROM ap.ap_invoice_lines_interface WHERE invoice_id =%s";

		String status = "S";

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 */

//				Paso 1	************************		

		addStep("Obtener el rango de fechas a procesar por la interfaz RO4_MEX.\r    								"
				+ "SELECT DISTINCT week_nb, to_char(min_date,'DD/MM/RRRR'), to_char(max_date,'DD/MM/RRRR')"
				+ "FROM wm_cat_ro4_weeks WHERE max_date < SYSDATE AND week_nb IN nvl((SELECT DISTINCT MAX(decode(status,'E',week_nb,'S',week_nb+1,1)) "
				+ "FROM wm_log_ro4 WHERE cr_plaza = '10MEX' AND week_nb IN (SELECT DISTINCT MAX(week_nb) FROM wm_log_ro4 WHERE cr_plaza = '10MEX') ),1);");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(rangoFechas);

		SQLResult fechas = dbPos.executeQuery(rangoFechas);
		String fechaMin = fechas.getData(0, "TO_CHAR(MIN_DATE,'DD/MM/RRRR')");
		System.out.println("Fecha minima= " + fechaMin);
		String fechaMax = fechas.getData(0, "to_char(max_date,'DD/MM/RRRR')");
		System.out.println("Fecha minima= " + fechaMax);
		boolean paso1 = fechas.isEmpty();
		if (!paso1) {
			testCase.addQueryEvidenceCurrentStep(fechas);
		}
		assertFalse("No se muestran registros que cumplan con el rango de fechas de ejecucion", paso1);
		System.out.println(paso1);

//				Paso 2	************************	

		addStep("Verificar que existen datos pendientes por procesar en la tabla fem_rechead_temp de RETEK para la plaza "
				+ data.get("plaza")
				+ " con el rango de fechas obtenido.\r												"
				+ "Consulta fem_rechead_temp: SELECT * FROM fem_rechead_temp WHERE TRUNC(receive_date) BETWEEN [min_date] AND [max_date] AND reference_3 IS NULL AND reference_8 IS NULL AND loc IN (SELECT STORE FROM STORE WHERE SUBSTR(store_name10, 1,5) = '10MEX'); \r\n");
		// Consulta en fem_rechead_temp
		// ************************************************************************************************************************************
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String consulta_pendiente_rechead_temp_temp_1 = String.format(pendiente_rechead_temp_temp_1, fechaMin,
				fechaMax);
		System.out.println(consulta_pendiente_rechead_temp_temp_1); // get shipment
		SQLResult rechead_1 = dbRms.executeQuery(consulta_pendiente_rechead_temp_temp_1);
		String Shipment = rechead_1.getData(0, "SHIPMENT");
		System.out.println("Shipment= " + Shipment);
		boolean paso2_rechead_1 = rechead_1.isEmpty();
		if (!paso2_rechead_1) {
			testCase.addQueryEvidenceCurrentStep(rechead_1);
		}
		System.out.println(paso2_rechead_1);
		// ************************************************************************************************************************************
		String consulta_pendiente_rechead_temp_temp_2 = String.format(pendiente_rechead_temp_temp_2, fechaMin,
				fechaMax);
		System.out.println(consulta_pendiente_rechead_temp_temp_2);
		SQLResult rechead_2 = dbRms.executeQuery(consulta_pendiente_rechead_temp_temp_2);
		boolean paso2_rechead_2 = rechead_2.isEmpty();
		if (!paso2_rechead_2) {
			testCase.addQueryEvidenceCurrentStep(rechead_2);
		}
		System.out.println(paso2_rechead_2);
		// ************************************************************************************************************************************
		String consulta_pendiente_rechead_temp_temp_3 = String.format(pendiente_rechead_temp_temp_3, fechaMin,
				fechaMax);
		System.out.println(consulta_pendiente_rechead_temp_temp_3);
		SQLResult rechead_3 = dbRms.executeQuery(consulta_pendiente_rechead_temp_temp_3);
		boolean paso2_rechead_3 = rechead_3.isEmpty();
		if (!paso2_rechead_3) {
			testCase.addQueryEvidenceCurrentStep(rechead_3);
		}
		System.out.println(paso2_rechead_3);
		assertFalse("No se muestran registros que cumplan con el rango de fechas de ejecucion en fem_rechead_temp",
				paso2_rechead_3);

		addStep("Verificar que existen datos pendientes por procesar en la tabla fem_rtvhead_temp de RETEK para la plaza "
				+ data.get("plaza") + " con el rango de fechas obtenido.\r"
				+ "SELECT * FROM fem_rtvhead_temp WHERE trunc(completed_date) BETWEEN [min_date] AND [max_date] AND reference_3 IS NULL AND reference_8 IS NULL AND store IN (SELECT store FROM store WHERE substr(store_name10, 1,5) = '10MEX');\r");
		// Consulta en rtvhead_temp
		// ************************************************************************************************************************************
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String consulta_pendiente_fem_rtvhead_temp_1 = String.format(pendiente_fem_rtvhead_temp_1, fechaMin, fechaMax);
		System.out.println(consulta_pendiente_fem_rtvhead_temp_1);
		SQLResult rtvhead_1 = dbRms.executeQuery(consulta_pendiente_fem_rtvhead_temp_1);
		String rtvOrder = rtvhead_1.getData(0, "RTV_ORDER_NO");
		System.out.println("rtv Order number= " + rtvOrder);
		boolean paso2_rtvhead_1 = rtvhead_1.isEmpty();
		if (!paso2_rtvhead_1) {
			testCase.addQueryEvidenceCurrentStep(rtvhead_1);
		}
		System.out.println(paso2_rtvhead_1);
		// ************************************************************************************************************************************

		String consulta_pendiente_fem_rtvhead_temp_2 = String.format(pendiente_fem_rtvhead_temp_2, fechaMin, fechaMax);
		System.out.println(consulta_pendiente_fem_rtvhead_temp_2);
		SQLResult rtvhead_3 = dbRms.executeQuery(consulta_pendiente_fem_rtvhead_temp_2);
		boolean paso2_rtvhead_2 = rtvhead_3.isEmpty();
		if (!paso2_rtvhead_2) {
			testCase.addQueryEvidenceCurrentStep(rtvhead_3);
		}
		System.out.println(paso2_rtvhead_2);
		assertFalse("No se muestran registros que cumplan con el rango de fechas de ejecucion en fem_rtvhead_temp",
				paso2_rtvhead_2);

//				Paso 3	************************	
		addStep("Verificar que la plaza " + data.get("plaza")
				+ " esta configurada para su procesamiento en la tabla wm_cfg_launcher de WMINT en modo MANUAL.									"
				+ "SELECT * FROM wm_cfg_launcher WHERE interface_name = 'RO4' AND manual_status = 'A' AND attribute1 = '10MEX';");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(verifConfig);
		SQLResult verificarConfiguracion = dbPos.executeQuery(verifConfig);
		boolean paso3 = verificarConfiguracion.isEmpty();
		if (!paso3) {
			
			testCase.addQueryEvidenceCurrentStep(verificarConfiguracion);
		}
		System.out.println(paso3);
		assertFalse("No se muestran registros que cumplan con el rango de fechas de ejecucion en fem_rtvhead_temp",
				paso3);
		// Paso 4 ************************
		addStep("Ejecutar  el servicio de la interfaz OL10");
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server;
		String searchedStatus = "R";
		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		String dateExecution = pok.runIntefaceWmWithInput(data.get("interfase"), data.get("servicio"), data.get("TYPE"),
				"TYPE");
		System.out.println("Respuesta dateExecution" + dateExecution);
		SQLResult is = executeQuery(dbLog, tdcIntegrationServerFormat);
		String status1 = is.getData(0, "STATUS");
		String run_id = is.getData(0, "RUN_ID");
		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			is = executeQuery(dbLog, tdcIntegrationServerFormat);
			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);
			u.hardWait(2);

		}
		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(consulta6, run_id);
			String error1 = String.format(consulta61, run_id);
			String error2 = String.format(consulta62, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontran un error en la ejecucucion de la interfaz en la tabla WM_LOG_ERROR");

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
		// ******************* Paso 5 ************************
		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run "
				+ "SELECT * FROM wmlog.wm_log_run WHERE interface = 'RO4' ORDER BY start_dt DESC;");

		boolean validateStatus = status.equals(status1);
		System.out.println(validateStatus);
		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");
		SQLResult log = dbLog.executeQuery(tdcIntegrationServerFormat);
		boolean log1 = log.isEmpty();
		if (!log1) {

			testCase.addQueryEvidenceCurrentStep(log);
		}
		System.out.println(log1);

		// ************************ Paso 6 ************************
		addStep("Verificar que la interfaz genero Threads "
				+ "SELECT * FROM wmlog.wm_log_thread WHERE parent_id = [wm_log_run.tun_id]");
		String consulta1 = String.format(consulta5, run_id);
		System.out.println("CONSULTA THREAD " + consulta1);
		SQLResult consultaThreads = dbLog.executeQuery(consulta1);
		boolean threads = consultaThreads.isEmpty();
		if (!threads) {

			testCase.addQueryEvidenceCurrentStep(consultaThreads);
		}
		System.out.println(threads);
		// .-----------Segunda consulta
		String consulta2 = String.format(consulta51, run_id);
		SQLResult consultaThreads2 = dbLog.executeQuery(consulta2);
		boolean threads1 = consultaThreads2.isEmpty();
		if (!threads1) {
			testCase.addQueryEvidenceCurrentStep(consultaThreads);
		}
		System.out.println(threads1);
		assertFalse("No se generaron threads en la tabla", threads1);

//		***************************			  Paso 7        *****************************	

		// **************************** Consulta rechead **************************
		addStep("Verificar que la interfaz genero threads"
				+ "SELECT * FROM fem_rechead_temp WHERE trunc(receive_date) BETWEEN [min_date] AND [max_date] AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND loc IN (SELECT store FROM store WHERE substr(store_name10, 1,5) = '10MEX');");
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);

		String consulta_validar_threads_1_1 = String.format(validar_threads_1_1, Shipment);
		System.out.println(consulta_validar_threads_1_1);
		SQLResult rechead_validar_1 = dbRms.executeQuery(consulta_validar_threads_1_1);
		boolean paso7_rechead_1 = rechead_validar_1.isEmpty();
		if (!paso7_rechead_1) {
			testCase.addQueryEvidenceCurrentStep(rechead_validar_1);
		}
		System.out.println(paso7_rechead_1);
		// ********************************************************************************************************************************************
		String consulta_validar_threads_1_2 = String.format(validar_threads_1_2, Shipment);
		System.out.println(consulta_validar_threads_1_2);
		SQLResult rechead_validar_2 = dbRms.executeQuery(consulta_validar_threads_1_2);
		boolean paso7_rechead_2 = rechead_validar_2.isEmpty();
		if (!paso7_rechead_2) {
			testCase.addQueryEvidenceCurrentStep(rechead_validar_2);
		}
		System.out.println(paso7_rechead_2);
		// ********************************************************************************************************************************************
		String consulta_validar_threads_1_3 = String.format(validar_threads_1_3, Shipment);
		System.out.println(consulta_validar_threads_1_3);
		SQLResult rechead_validar_3 = dbRms.executeQuery(consulta_validar_threads_1_3);
		String rechead_reference_3 = rechead_validar_3.getData(0, "reference_3"); // reference_3 rechead
		System.out.println("rechead reference_3 = " + rechead_reference_3);
		boolean paso7_rechead_3 = rechead_validar_3.isEmpty();
		if (!paso7_rechead_3) {
			testCase.addQueryEvidenceCurrentStep(rechead_validar_3);
		}
		System.out.println(paso7_rechead_3);

		// ********************************************************************************************************************************************

		String consulta_validar_threads_1_4 = String.format(validar_threads_1_4, Shipment);
		System.out.println(consulta_validar_threads_1_4);
		SQLResult rechead_validar_4 = dbRms.executeQuery(consulta_validar_threads_1_4);
		boolean paso7_rechead_4 = rechead_validar_4.isEmpty();
		if (!paso7_rechead_4) {
			testCase.addQueryEvidenceCurrentStep(rechead_validar_4);
		}
		System.out.println(paso7_rechead_4);
		assertFalse("No se muestran threads", paso7_rechead_4);
		// ********************************************************************************************************************************************
		addStep("Verificar que la interfaz genero threads"
				+ "SELECT * FROM fem_rtvhead_temp WHERE trunc(completed_date) BETWEEN [min_date] AND [max_date] AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND store IN (SELECT store FROM store WHERE substr(store_name10, 1,5) = '10MEX');\r\n");
		// ********************************** Consulta rtvhead
		// ********************************
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String consulta_validar_threads_2_1 = String.format(validar_threads_2_1, rtvOrder);
		System.out.println(consulta_validar_threads_2_1);
		SQLResult rtvhead_validar_1 = dbRms.executeQuery(consulta_validar_threads_2_1);
		boolean paso7_rtvhead_1 = rtvhead_validar_1.isEmpty();
		if (!paso7_rtvhead_1) {
			testCase.addQueryEvidenceCurrentStep(rtvhead_validar_1);
		}
		System.out.println(paso7_rtvhead_1);
		// ********************************************************************************************************************************************

		String consulta_validar_threads_2_2 = String.format(validar_threads_2_2, rtvOrder);
		System.out.println(consulta_validar_threads_2_2);
		SQLResult rtvhead_validar_2 = dbRms.executeQuery(consulta_validar_threads_2_2);
		boolean paso7_rtvhead_2 = rtvhead_validar_2.isEmpty();
		if (!paso7_rtvhead_2) {
			testCase.addQueryEvidenceCurrentStep(rtvhead_validar_2);
		}
		System.out.println(paso7_rtvhead_2);
		// ********************************************************************************************************************************************

		String consulta_validar_threads_2_3 = String.format(validar_threads_2_3, rtvOrder);
		System.out.println(consulta_validar_threads_2_3);
		SQLResult rtvhead_validar_3 = dbRms.executeQuery(consulta_validar_threads_2_3);
		String rtvhead_reference_3 = rtvhead_validar_3.getData(0, "reference_3"); // reference_3 rtvhead
		System.out.println("rtvhead reference_3 = " + rtvhead_reference_3);
		boolean paso7_rtvhead_3 = rtvhead_validar_3.isEmpty();
		if (!paso7_rtvhead_3) {
			testCase.addQueryEvidenceCurrentStep(rtvhead_validar_3);
		}
		System.out.println(paso7_rtvhead_3);

		// ********************************************************************************************************************************************

		String consulta_validar_threads_2_4 = String.format(validar_threads_2_4, rtvOrder);
		System.out.println(consulta_validar_threads_2_4);
		SQLResult rtvhead_validar_4 = dbRms.executeQuery(consulta_validar_threads_2_4);
		boolean paso7_rtvhead_4 = rtvhead_validar_4.isEmpty();
		if (!paso7_rtvhead_4) {
			testCase.addQueryEvidenceCurrentStep(rtvhead_validar_4);
		}
		System.out.println(paso7_rtvhead_4);

		// ********************************************************************************************************************************************

		String consulta_validar_threads_2_5 = String.format(validar_threads_2_5, rtvOrder);
		System.out.println(consulta_validar_threads_2_5);
		SQLResult rtvhead_validar_5 = dbRms.executeQuery(consulta_validar_threads_2_5);
		boolean paso7_rtvhead_5 = rtvhead_validar_5.isEmpty();
		if (!paso7_rtvhead_5) {
			testCase.addQueryEvidenceCurrentStep(rtvhead_validar_5);
		}
		System.out.println(paso7_rtvhead_5);
		assertFalse("No se generaron threads en la tabla", threads1);

//	********************		Paso 8	************************	
		addStep("Verificar que las Facturas fueron insertadas en las tablas ap_invoices_interface "
				+ "SELECT * FROM ap.ap_invoices_interface " + "WHERE invoice_id = [fem_rechead_temp.reference_3]; ");
		String fac = String.format(FAC_EBS, rechead_reference_3);
		System.out.println(fac);
		SQLResult verificar_facturas_1 = dbEbs.executeQuery(fac);
		boolean paso8_1 = verificar_facturas_1.isEmpty();
		if (!paso8_1) {
			testCase.addQueryEvidenceCurrentStep(verificar_facturas_1);
		}
		System.out.println(paso8_1);
		// ********************************************************************************************************************************************

		String fac2 = String.format(FAC_EBS2, rechead_reference_3);
		System.out.println(fac2);
		SQLResult verificar_facturas_2 = dbEbs.executeQuery(fac2);
		boolean paso8_2 = verificar_facturas_2.isEmpty();
		if (!paso8_2) {
			testCase.addQueryEvidenceCurrentStep(verificar_facturas_2);
		}
		System.out.println(paso8_2);
		// ********************************************************************************************************************************************

		String fac3 = String.format(FAC_EBS3, rechead_reference_3);
		System.out.println(fac3);
		SQLResult verificar_facturas_3 = dbEbs.executeQuery(fac3);
		boolean paso8_3 = verificar_facturas_3.isEmpty();
		if (!paso8_3) {
			testCase.addQueryEvidenceCurrentStep(verificar_facturas_3);
		}
		System.out.println(paso8_3);
		assertFalse("No se generaron datos en la tabla", paso8_3);
		// Paso 8 ************************
		addStep("Verificar que las Facturas fueron insertadas en las tablas ap_invoice_lines_interface "
				+ "SELECT * FROM ap.ap_invoice_lines_interface"
				+ " WHERE invoice_id = [fem_rechead_temp.reference_3];");
		String lin = String.format(FAC_EBS_LIN, rechead_reference_3);
		System.out.println(lin);
		SQLResult verificar_facturas_4 = dbEbs.executeQuery(lin);
		boolean paso8_4 = verificar_facturas_4.isEmpty();
		if (!paso8_4) {
			testCase.addQueryEvidenceCurrentStep(verificar_facturas_4);
		}
		System.out.println(paso8_4);
		// ********************************************************************************************************************************************

		String lin2 = String.format(FAC_EBS_LIN2, rechead_reference_3);
		System.out.println(lin2);
		SQLResult verificar_facturas_5 = dbEbs.executeQuery(lin2);
		boolean paso8_5 = verificar_facturas_5.isEmpty();
		if (!paso8_5) {
			testCase.addQueryEvidenceCurrentStep(verificar_facturas_5);
		}
		System.out.println(paso8_5);
		// ********************************************************************************************************************************************

		String lin3 = String.format(FAC_EBS_LIN3, rechead_reference_3);
		System.out.println(lin3);
		SQLResult verificar_facturas_6 = dbEbs.executeQuery(lin3);
		boolean paso8_6 = verificar_facturas_6.isEmpty();
		if (!paso8_6) {
			testCase.addQueryEvidenceCurrentStep(verificar_facturas_6);
		}
		System.out.println(paso8_6);
		assertFalse("No se generaron datos en la tabla", paso8_6);

		addStep("Verificar que las Facturas fueron insertadas en las tablas ap_invoices_interface "
				+ "SELECT * FROM ap.ap_invoices_interface " + "WHERE invoice_id = [fem_rtvhead_temp.reference_3]; ");
		String facrtv = String.format(FAC_EBS, rtvhead_reference_3);
		System.out.println(facrtv);
		SQLResult verificar_notas_1 = dbRms.executeQuery(facrtv);
		boolean paso8_notas_1 = verificar_notas_1.isEmpty();
		if (!paso8_notas_1) {
			testCase.addQueryEvidenceCurrentStep(verificar_notas_1);
		}
		System.out.println(paso8_notas_1);
		// ********************************************************************************************************************************************

		String facrtv2 = String.format(FAC_EBS2, rtvhead_reference_3);
		System.out.println(facrtv2);
		SQLResult verificar_notas_2 = dbRms.executeQuery(facrtv2);
		boolean paso8_notas_2 = verificar_notas_2.isEmpty();
		if (!paso8_notas_2) {
			testCase.addQueryEvidenceCurrentStep(verificar_notas_2);
		}
		System.out.println(paso8_notas_2);
		// ********************************************************************************************************************************************
		String facrtv3 = String.format(FAC_EBS3, rtvhead_reference_3);
		System.out.println(facrtv3);
		SQLResult verificar_notas_3 = dbRms.executeQuery(facrtv3);
		boolean paso8_notas_3 = verificar_notas_3.isEmpty();
		if (!paso8_notas_3) {
			testCase.addQueryEvidenceCurrentStep(verificar_notas_3);
		}
		System.out.println(paso8_notas_3);
		assertFalse("No se generaron datos en la tabla", paso8_notas_3);

		// ********************************************************************************************************************************************

		// Paso 8 ************************
		addStep("Verificar que las Facturas fueron insertadas en las tablas ap_invoice_lines_interface "
				+ "SELECT * FROM ap.ap_invoice_lines_interface"
				+ " WHERE invoice_id = [fem_rtvhead_temp.reference_3];");
		String linrtv = String.format(FAC_EBS_LIN, rtvhead_reference_3);
		System.out.println(linrtv);
		SQLResult verificar_notas_4 = dbEbs.executeQuery(linrtv);
		boolean paso8_notas_4 = verificar_notas_4.isEmpty();
		if (!paso8_notas_4) {
			testCase.addQueryEvidenceCurrentStep(verificar_notas_4);
		}
		System.out.println(paso8_notas_4);
		// ********************************************************************************************************************************************

		String linrtv2 = String.format(FAC_EBS_LIN2, rtvhead_reference_3);
		System.out.println(linrtv2);
		SQLResult verificar_notas_5 = dbEbs.executeQuery(linrtv2);
		boolean paso8_notas_5 = verificar_notas_5.isEmpty();
		if (!paso8_notas_5) {
			testCase.addQueryEvidenceCurrentStep(verificar_notas_5);
		}
		System.out.println(paso8_notas_5);
		// ********************************************************************************************************************************************

		String linrtv3 = String.format(FAC_EBS_LIN3, rtvhead_reference_3);
		System.out.println(linrtv3);
		SQLResult verificar_notas_6 = dbEbs.executeQuery(linrtv3);
		boolean paso8_notas_6 = verificar_notas_6.isEmpty();
		if (!paso8_notas_6) {
			testCase.addQueryEvidenceCurrentStep(verificar_notas_6);
		}
		System.out.println(paso8_notas_6);

		assertFalse("No se generaron datos en la tabla", paso8_notas_6);

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_RO4_002_MEX_Facturas_y_Notas_Manual";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Ejecucion de la interfaz para el procesamiento de las Facturas/Notas de credito en modo MANUAL.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Antonio de Lira";
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