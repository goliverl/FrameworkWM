package interfaces.Ro4_Peru;

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
import utils.sql.SQLUtil;

public class RO4_Peru_Facturas_ModoAuto extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_RO4_Peru_001_Facturas_ModoAuto(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 ***********************************************************************************************/

		SQLUtil dbPuserPeru = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,
				GlobalVariables.DB_PASSWORD_Oiwmqa);
		SQLUtil dbRmsPeru = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERPeru, GlobalVariables.DB_USER_RMSWMUSERPeru,
				GlobalVariables.DB_PASSWORD_RMSWMUSERPeru);
		SQLUtil dbLogPeru = new SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,
				GlobalVariables.DB_PASSWORD_Oiwmqa);
		SQLUtil dbEbsPeru = new SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,
				GlobalVariables.DB_PASSWORD_OIEBSBDQ); 

		/**
		 * Variables
		 * ***********************************************************************************************
		 * 
		 * 
		 */

		String rangoFechas = " SELECT DISTINCT week_nb, to_char(min_date,'DD/MM/RRRR'), to_char(max_date,'DD/MM/RRRR') FROM \r\n"
				+ " WMUSER.wm_cat_ro4_weeks WHERE max_date < SYSDATE AND \r\n"
				+ " week_nb IN nvl((SELECT DISTINCT MAX(decode(status,'E',week_nb,'S',week_nb+1,1)) \r\n"
				+ " FROM WMUSER.wm_log_ro4 WHERE cr_plaza ='" + data.get("plaza") 
				+ " 'AND week_nb IN (SELECT DISTINCT MAX(week_nb) \r\n" 
				+ " FROM WMUSER.wm_log_ro4 WHERE cr_plaza ='"+ data.get("plaza") + "') ),1) \r\n";// posUser

		String pendiente_rechead_temp_temp_1 = " Select * from (SELECT SHIPMENT,ORDER_NO,SUPPLIER_ID,RECEIVE_DATE,TERMS_DESC \r\n"
				+ " FROM fem_rechead_temp \r\n" 
				+ " WHERE TRUNC(receive_date ) BETWEEN TO_DATE('%s', 'dd,MM,yyyy') \r\n"
				+ " AND TO_DATE('%s', 'dd,MM,yyyy') \r\n" 
				+ " AND reference_3 IS NULL \r\n" 
				+ " AND reference_8 IS NULL \r\n"
				+ " AND loc " + " IN (SELECT store FROM store WHERE SUBSTR(store_name10, 1,5) = '" + data.get("plaza")
				+ "') order by receive_date desc) where rownum <=3 \r\n";// RMS

		String pendiente_rechead_temp_temp_2 = " Select * from (SELECT CURRENCY_CODE,EXCHANGE_RATE,LOC,COMMENTS,REFERENCE_1 \r'n"
				+ " FROM fem_rechead_temp \r\n" 
				+ " WHERE TRUNC(receive_date ) BETWEEN TO_DATE('%s', 'dd,MM,yyyy') \r\n"
				+ " AND TO_DATE('%s', 'dd,MM,yyyy') \r\n" 
				+ " AND reference_3 IS NULL \r\n" 
				+ " AND reference_8 IS NULL \r\n"
				+ " AND loc \r\n" 
				+ " IN (SELECT store FROM store WHERE SUBSTR(store_name10, 1,5) = '" + data.get("plaza")
				+ "') order by receive_date desc) where rownum <=3 \r\n";// RMS

		String pendiente_rechead_temp_temp_3 = " Select * from (SELECT REFERENCE_3,REFERENCE_4,REFERENCE_5,REFERENCE_6,REFERENCE_7,\r\n"
				+ " REFERENCE_10,MERCH_TYPE,REMISION \r'n"
				+ " FROM fem_rechead_temp \r\n" 
				+ " WHERE TRUNC(receive_date ) BETWEEN TO_DATE('%s', 'dd,MM,yyyy') \r\n"
				+ " AND TO_DATE('%s', 'dd,MM,yyyy') \r\n" 
				+ " AND reference_3 IS NULL \r\n" 
				+ " AND reference_8 IS NULL \r\n"
				+ " AND loc " + " IN (SELECT store FROM store WHERE SUBSTR(store_name10, 1,5) = '" + data.get("plaza")
				+ "') order by receive_date desc) where rownum <=3 \r\n";// RMS

		String verifConfig = " SELECT * FROM WMUSER.wm_cfg_launcher WHERE interface_name = \r\n"
				+ " 'RO4' AND auto_status = 'A' AND attribute1 = '" + data.get("plaza") + "' \r\n";// posUser

		String tdcIntegrationServerFormat = " Select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, \r\n"
				+ " (END_DT - START_DT)*24*60  \r\n"
				+ " FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE LIKE '%RO4_PER%' ORDER BY START_DT DESC) \r\n"
				+ " where rownum <=1 \r\n";// WMLOG
		
		String consulta6 = " Select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE \r\n"
				+ " from  wmlog.WM_LOG_ERROR \r\n" 
				+ " where RUN_ID='%s') where rownum <=1 \r\n";// WMLOG
		
		String consulta61 = " select * from (select description,MESSAGE \r\n" 
		        + " from wmlog.WM_LOG_ERROR \r\n"
				+ " where RUN_ID='%s')WHERE rownum <= 1 \r\n";// WMLOG
		
		String consulta62 = " Select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 \r\n"
				+ " from wmlog.WM_LOG_ERROR \r\n" 
				+ " where RUN_ID='%s')WHERE rownum <= 1 \r\n";// WMLOG

		String validar_threads_1_1 = " SELECT * from(SELECT SHIPMENT,ORDER_NO,SUPPLIER_ID,RECEIVE_DATE,CURRENCY_CODE \r\n"
				+ " FROM RMS100.fem_rechead_temp WHERE SHIPMENT='%s'AND reference_3 IS NOT NULL \r\n"
				+ " AND reference_8 IS NOT NULL AND loc IN (SELECT store FROM \r\n"
				+ " store WHERE substr(store_name10, 1,5) = '" + data.get("plaza") + "')) where ROWNUM<=1 \r\n";// RMS

		String validar_threads_1_2 = " SELECT * from(SELECT EXCHANGE_RATE,LOC,LOC_TYPE,COMMENTS,REFERENCE_1 FROM \r\n"
				+ " RMS100.fem_rechead_temp WHERE SHIPMENT='%s' \r\n"
				+ " AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND loc IN (SELECT store \r\n"
				+ " FROM store WHERE substr(store_name10, 1,5) = '" + data.get("plaza") + "')) where ROWNUM<=1 \r\n";// RMS

		String validar_threads_1_3 = " SELECT * from(SELECT REFERENCE_2,REFERENCE_3,REFERENCE_4,REFERENCE_5,REFERENCE_6 \r\n"
				+ " FROM RMS100.fem_rechead_temp WHERE SHIPMENT='%s' \r\n"
				+ " AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND loc IN (SELECT store FROM \r\n"
				+ " store WHERE substr(store_name10, 1,5) = '" + data.get("plaza") + "')) where ROWNUM<=1 \r\n";// RMS

		String validar_threads_1_4 = " SELECT * from(SELECT REFERENCE_7,REFERENCE_8,REFERENCE_9, \r\n"
				+ " REFERENCE_10,MERCH_TYPE,REMISION \r\n"
				+ " FROM RMS100.fem_rechead_temp WHERE SHIPMENT='%s' \r\n"
				+ " AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND loc IN (SELECT store \r\n"
				+ " FROM store WHERE substr(store_name10, 1,5) = '" + data.get("plaza") + "')) where ROWNUM<=1 \r\n";// RMS

		String consulta5 = " SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS \r\n" 
		        + " FROM WMLOG.WM_LOG_THREAD \r\n"
				+ " WHERE PARENT_ID='%s' \r\n";
		
		String consulta51 = " SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 \r\n" 
		        + "FROM WMLOG.WM_LOG_THREAD \r\n"
				+ " WHERE PARENT_ID='%s' \r\n";//

		String FAC_EBS = " SELECT  INVOICE_ID, INVOICE_NUM, INVOICE_DATE, VENDOR_ID \r\n"
				+ " FROM ap.ap_invoices_interface WHERE invoice_id =%s \r\n";
		
		String FAC_EBS2 = " SELECT  vendor_name, vendor_site_id , invoice_amount, invoice_currency_code \r\n"
				+ "FROM ap.ap_invoices_interface WHERE invoice_id =%s \r\n";
		
		String FAC_EBS3 = " SELECT  description , ORG_ID \r\n"
				+ " FROM ap.ap_invoices_interface WHERE invoice_id =%s \r\n";

		String FAC_EBS_LIN = " SELECT invoice_id, invoice_line_id, line_number,line_type_lookup_code,amount \r\n"
				+ " FROM ap.ap_invoice_lines_interface \r\n"
				+ " WHERE invoice_id =%s \r\n";
		
		String FAC_EBS_LIN2 = "SELECT accounting_date, description,amount_includes_tax_flag, \r\n"
				+ " prorate_across_flag, tax_code \r\n"
				+ " FROM ap.ap_invoice_lines_interface \r\n"
				+ " WHERE invoice_id =%s \r\n";
		
		String FAC_EBS_LIN3 = "SELECT dist_code_concatenated, awt_group_name,created_by, creation_date \r\n"
				+ " FROM ap.ap_invoice_lines_interface WHERE invoice_id =%s \r\n";

		String status = "S";

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 */

//					Paso 1	************************		

		addStep("Obtener el rango de fechas a procesar por la interfaz RO4_PER");

		System.out.println(GlobalVariables.DB_HOST_Oiwmqa);
		System.out.println(rangoFechas);

		SQLResult fechas = dbPuserPeru.executeQuery(rangoFechas);
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

//					Paso 2	************************	
		
		addStep("Verificar que existen datos pendientes por procesar en la tabla fem_rechead_temp de RETEK para la plaza "
				+ data.get("plaza") + " con el rango de fechas obtenido.\r");
				
		
		// Consulta en fem_rechead_temp
		// ************************************************************************************************************************************
		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERPeru);
		String consulta_pendiente_rechead_temp_temp_1 = String.format(pendiente_rechead_temp_temp_1, fechaMin,
				fechaMax);
		System.out.println(consulta_pendiente_rechead_temp_temp_1); // get shipment
		SQLResult rechead_1 = dbRmsPeru.executeQuery(consulta_pendiente_rechead_temp_temp_1);
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
		SQLResult rechead_2 = dbRmsPeru.executeQuery(consulta_pendiente_rechead_temp_temp_2);
		boolean paso2_rechead_2 = rechead_2.isEmpty();
		if (!paso2_rechead_2) {
			testCase.addQueryEvidenceCurrentStep(rechead_2);
		}
		System.out.println(paso2_rechead_2);
		// ************************************************************************************************************************************
		String consulta_pendiente_rechead_temp_temp_3 = String.format(pendiente_rechead_temp_temp_3, fechaMin,
				fechaMax);
		System.out.println(consulta_pendiente_rechead_temp_temp_3);
		SQLResult rechead_3 = dbRmsPeru.executeQuery(consulta_pendiente_rechead_temp_temp_3);
		boolean paso2_rechead_3 = rechead_3.isEmpty();
		if (!paso2_rechead_3) {
			testCase.addQueryEvidenceCurrentStep(rechead_3);
		}
		System.out.println(paso2_rechead_3);
		assertFalse("No se muestran registros que cumplan con el rango de fechas de ejecucion en fem_rechead_temp",
				paso2_rechead_3);

//					Paso 3	************************	
		addStep("Verificar que la plaza " + data.get("plaza")
				+ " esta configurada para su procesamiento en la tabla wm_cfg_launcher de WMINT en modo MANUAL.\r\n");

		System.out.println(GlobalVariables.DB_HOST_Oiwmqa);
		System.out.println(verifConfig);
		SQLResult verificarConfiguracion = dbPuserPeru.executeQuery(verifConfig);
		boolean paso3 = verificarConfiguracion.isEmpty();
		if (!paso3) {
			testCase.addQueryEvidenceCurrentStep(verificarConfiguracion);
		}
		System.out.println(paso3);
		assertFalse("No se muestran registros que cumplan con el rango de fechas de ejecucion en fem_rtvhead_temp",
				paso3);
		
		// Paso 4 ************************
		
		addStep("Ejecutar  el servicio de la interfaz RO4_Peru");
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		System.out.println(GlobalVariables.DB_HOST_Oiwmqa);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		String dateExecution = pok.runIntefaceWmWithInput(data.get("interfase"), data.get("servicio"), data.get("TYPE"),
				"TYPE");
		System.out.println("Respuesta dateExecution" + dateExecution);
		SQLResult is = executeQuery(dbLogPeru, tdcIntegrationServerFormat);
		String status1 = is.getData(0, "STATUS");
		String run_id = is.getData(0, "RUN_ID");
		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			is = executeQuery(dbLogPeru, tdcIntegrationServerFormat);
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

			SQLResult errorr = dbLogPeru.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontro un error en la ejecucutar la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorr);

			}

			SQLResult errorIS = dbLogPeru.executeQuery(error1);
			boolean emptyError1 = errorIS.isEmpty();
			if (!emptyError1) {
				testCase.addQueryEvidenceCurrentStep(errorIS);
			}

			SQLResult errorIS2 = dbLogPeru.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();

			if (!emptyError2) {

				testCase.addQueryEvidenceCurrentStep(errorIS2);

			}

		}
		// ******************* Paso 5 ************************
		
		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run ");
				

		boolean validateStatus = status.equals(status1);
		System.out.println(validateStatus);
		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");
		SQLResult log = dbLogPeru.executeQuery(tdcIntegrationServerFormat);
		boolean log1 = log.isEmpty();
		if (!log1) {

			testCase.addQueryEvidenceCurrentStep(log);
		}
		System.out.println(log1);

		// ************************ Paso 6 ************************
		
		addStep("Verificar que la interfaz genero Threads ");
				
		String consulta1 = String.format(consulta5, run_id);
		System.out.println("CONSULTA THREAD " + consulta1);
		SQLResult consultaThreads = dbLogPeru.executeQuery(consulta1);
		boolean threads = consultaThreads.isEmpty();
		if (!threads) {

			testCase.addQueryEvidenceCurrentStep(consultaThreads);
		}
		System.out.println(threads);
		// .-----------Segunda consulta
		String consulta2 = String.format(consulta51, run_id);
		SQLResult consultaThreads2 = dbLogPeru.executeQuery(consulta2);
		boolean threads1 = consultaThreads2.isEmpty();
		if (!threads1) {
			testCase.addQueryEvidenceCurrentStep(consultaThreads);
		}
		System.out.println(threads1);
		assertFalse("No se generaron threads en la tabla", threads1);

//			***************************			  Paso 7        *****************************	

		// **************************** Consulta rechead **************************
		addStep("Verificar que la interfaz genero threads");
				
		System.out.println(GlobalVariables.DB_HOST_Oiwmqa);

		String consulta_validar_threads_1_1 = String.format(validar_threads_1_1, Shipment);
		System.out.println(consulta_validar_threads_1_1);
		SQLResult rechead_validar_1 = dbRmsPeru.executeQuery(consulta_validar_threads_1_1);
		boolean paso7_rechead_1 = rechead_validar_1.isEmpty();
		if (!paso7_rechead_1) {
			testCase.addQueryEvidenceCurrentStep(rechead_validar_1);
		}
		System.out.println(paso7_rechead_1);
		// ********************************************************************************************************************************************
		String consulta_validar_threads_1_2 = String.format(validar_threads_1_2, Shipment);
		System.out.println(consulta_validar_threads_1_2);
		SQLResult rechead_validar_2 = dbRmsPeru.executeQuery(consulta_validar_threads_1_2);
		boolean paso7_rechead_2 = rechead_validar_2.isEmpty();
		if (!paso7_rechead_2) {
			testCase.addQueryEvidenceCurrentStep(rechead_validar_2);
		}
		System.out.println(paso7_rechead_2);
		// ********************************************************************************************************************************************
		String consulta_validar_threads_1_3 = String.format(validar_threads_1_3, Shipment);
		System.out.println(consulta_validar_threads_1_3);
		SQLResult rechead_validar_3 = dbRmsPeru.executeQuery(consulta_validar_threads_1_3);
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
		SQLResult rechead_validar_4 = dbRmsPeru.executeQuery(consulta_validar_threads_1_4);
		boolean paso7_rechead_4 = rechead_validar_4.isEmpty();
		if (!paso7_rechead_4) {
			testCase.addQueryEvidenceCurrentStep(rechead_validar_4);
		}
		System.out.println(paso7_rechead_4);
		assertFalse("No se muestran threads", paso7_rechead_4);

//		********************		Paso 8	************************
		
		addStep("Verificar que las Facturas fueron insertadas en las tablas ap_invoices_interface ");
				
		String fac = String.format(FAC_EBS, rechead_reference_3);
		System.out.println(fac);
		SQLResult verificar_facturas_1 = dbEbsPeru.executeQuery(fac);
		boolean paso8_1 = verificar_facturas_1.isEmpty();
		if (!paso8_1) {
			testCase.addQueryEvidenceCurrentStep(verificar_facturas_1);
		}
		System.out.println(paso8_1);
		// ********************************************************************************************************************************************

		String fac2 = String.format(FAC_EBS2, rechead_reference_3);
		System.out.println(fac2);
		SQLResult verificar_facturas_2 = dbEbsPeru.executeQuery(fac2);
		boolean paso8_2 = verificar_facturas_2.isEmpty();
		if (!paso8_2) {
			testCase.addQueryEvidenceCurrentStep(verificar_facturas_2);
		}
		System.out.println(paso8_2);
		// ********************************************************************************************************************************************

		String fac3 = String.format(FAC_EBS3, rechead_reference_3);
		System.out.println(fac3);
		SQLResult verificar_facturas_3 = dbEbsPeru.executeQuery(fac3);
		boolean paso8_3 = verificar_facturas_3.isEmpty();
		if (!paso8_3) {
			testCase.addQueryEvidenceCurrentStep(verificar_facturas_3);
		}
		System.out.println(paso8_3);
		assertFalse("No se generaron datos en la tabla", paso8_3);
		
		// Paso 8 ************************
		
		addStep("Verificar que las Facturas fueron insertadas en las tablas ap_invoice_lines_interface ");
				
		String lin = String.format(FAC_EBS_LIN, rechead_reference_3);
		System.out.println(lin);
		SQLResult verificar_facturas_4 = dbEbsPeru.executeQuery(lin);
		boolean paso8_4 = verificar_facturas_4.isEmpty();
		if (!paso8_4) {
			testCase.addQueryEvidenceCurrentStep(verificar_facturas_4);
		}
		System.out.println(paso8_4);
		
		// ********************************************************************************************************************************************

		String lin2 = String.format(FAC_EBS_LIN2, rechead_reference_3);
		System.out.println(lin2);
		SQLResult verificar_facturas_5 = dbEbsPeru.executeQuery(lin2);
		boolean paso8_5 = verificar_facturas_5.isEmpty();
		if (!paso8_5) {
			testCase.addQueryEvidenceCurrentStep(verificar_facturas_5);
		}
		System.out.println(paso8_5);
		// ********************************************************************************************************************************************

		String lin3 = String.format(FAC_EBS_LIN3, rechead_reference_3);
		System.out.println(lin3);
		SQLResult verificar_facturas_6 = dbEbsPeru.executeQuery(lin3);
		boolean paso8_6 = verificar_facturas_6.isEmpty();
		if (!paso8_6) {
			testCase.addQueryEvidenceCurrentStep(verificar_facturas_6);
		}
		System.out.println(paso8_6);
		assertFalse("No se generaron datos en la tabla", paso8_6);

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_RO4_Peru_001_Facturas_ModoAuto";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Ejecucion de la interfaz para el procesamiento de las Facturas";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Qa Automatizacion";
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