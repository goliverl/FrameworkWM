package interfaces.Ro4_Peru;


	
	import static org.junit.Assert.assertFalse;
	import static org.testng.Assert.assertFalse;
	import static org.testng.Assert.assertTrue;

	import java.util.HashMap;

	import org.testng.annotations.Test;

	import integrationServer.om.PakageManagment;
	import modelo.BaseExecution;
	import util.GlobalVariables;
	import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.password.PasswordUtil;
	import utils.selenium.ChromeTest;
	import utils.selenium.SeleniumUtil;


	public class RO4_Peru_NOTAS_CREDITO_AUTO extends BaseExecution {
		
		@Test(dataProvider = "data-provider")
		public void ATC_FT_RO4_Peru_003_NOTAS_CREDITO_AUTO(HashMap<String, String> data) throws Exception {
		
	/* Utilerías ********************************************************************************************************************************************/
			
			SQLUtil dbPuserPeru = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,
					GlobalVariables.DB_PASSWORD_Oiwmqa);
			SQLUtil dbRmsPeru = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERPeru, GlobalVariables.DB_USER_RMSWMUSERPeru,
					GlobalVariables.DB_PASSWORD_RMSWMUSERPeru);
			SQLUtil dbLogPeru = new SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,
					GlobalVariables.DB_PASSWORD_Oiwmqa);
			SQLUtil dbEbsPeru = new SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,
					GlobalVariables.DB_PASSWORD_OIEBSBDQ); 

	 /* Variables ******************************************************************************************************************************************/

			
			//Paso 1
			
			String rangoFechas = "SELECT DISTINCT week_nb, to_char(min_date,'DD/MM/RRRR'), to_char(max_date,'DD/MM/RRRR') \r\n"
					+ "FROM  WMUSER.wm_cat_ro4_weeks WHERE max_date < SYSDATE AND week_nb  \r\n"
					+ "IN nvl((SELECT DISTINCT MAX(decode(status,'E',week_nb,'S',week_nb+1,1)) \r\n"
					+ "FROM  WMUSER.wm_log_ro4 WHERE cr_plaza = '"+ data.get("plaza") +"' AND week_nb \r\n"
					+"IN (SELECT DISTINCT MAX(week_nb) FROM  WMUSER.wm_log_ro4 WHERE cr_plaza = '"+ data.get("plaza")+"') ),1) \r\n"; 
			
			
			
			
			String minFechasColumna= "TO_CHAR(MIN_DATE,'DD/MM/RRRR')"; //Columna para el rango de fecha minima
			String maxFechasColumna= "to_char(max_date,'DD/MM/RRRR')"; //Columna para el rango de fecha minima
			
			
	//***********************************************************************************************************************************************************************		
			
			//Paso 2
			
			
				
			String pendiente_fem_rtvhead_temp_1 = "select * from (SELECT RTV_ORDER_NO,SUPPLIER_ID,TERMS_DESC,CURRENCY_CODE,EXCHANGE_RATE \r\n"
	                + "FROM fem_rtvhead_temp WHERE TRUNC(completed_date)  BETWEEN TO_DATE('%s','dd-mm-yy') AND \r\n"
	                + " TO_DATE('%s','dd-mm-yy') AND reference_3 IS NULL AND reference_8 IS NULL AND store IN (SELECT store FROM store WHERE \r\n"
	                + "substr(store_name10, 1,5) ='"+data.get("plaza")+"'))where rownum <=1 \r\n";//RMS
			
			
			String datosPtesProcesar1= "SELECT* FROM (SELECT completed_date, ret_auth_num, handling_pct, handling_cost, comment_desc \r\n"
					+ " FROM RMS100.FEM_RTVHEAD_TEMP WHERE trunc(completed_date) \r\n"
					+ " BETWEEN to_date ('%s','dd,mm,yyyy') AND to_date('%s','dd,mm,yyyy') AND reference_3 IS NULL \r\n"
					+ " AND reference_8 IS NULL AND \r\n"
					+ " store IN (SELECT store FROM store WHERE substr(store_name10, 1,5) = '"+ data.get("plaza")+"')) WHERE rownum <= 1 \r\n \\";
		       
			
			String datosPtesProcesar2 = "SELECT* FROM (SELECT reference_1, reference_2, reference_3, reference_4, reference_5, reference_6 \r\n"
					+ " FROM RMS100.FEM_RTVHEAD_TEMP WHERE trunc(completed_date) \r\n"
					+ " BETWEEN to_date ('%s','dd,mm,yyyy') AND to_date('%s','dd,mm,yyyy') AND reference_3 IS NULL AND reference_8 IS NULL AND \r\n "
					+ " store IN (SELECT store FROM store WHERE substr(store_name10, 1,5) = '"+data.get("plaza")+"'))WHERE rownum <= 1 \r\n";
			
			
			String datosPtesProcesar3 = "SELECT* FROM (SELECT reference_7, reference_8, reference_9, reference_10, merch_type, fem_remision \r\n"
					+ " FROM RMS100.FEM_RTVHEAD_TEMP WHERE trunc(completed_date) \r\n"
					+ " BETWEEN to_date ('%s','dd,mm,yyyy') AND to_date('%s','dd,mm,yyyy') AND reference_3 IS NULL AND reference_8 IS NULL AND \r\n"
					+ " store IN (SELECT store FROM store WHERE substr(store_name10, 1,5) = '"+data.get("plaza")+"'))WHERE rownum <= 1 \r\n";
			
	//********************************************************************************************************************************************************************************		
			
			//Paso 3
			
			String verificaConfig= "SELECT * FROM WMUSER.wm_cfg_launcher \r\n"
					+ " WHERE interface_name = 'RO4' AND auto_status = 'A' AND attribute1 = '"+ data.get("plaza")+"'\r\n";
			
			
			
	//*************************************************************************************************************************************************************************
			
			//Paso 4
			
			String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 \r\n"
					+ " FROM WMLOG.WM_LOG_RUN Tbl \r\n" 
					+ " WHERE INTERFACE LIKE '%RO4%' \r\n"
					+ " ORDER BY START_DT DESC) where rownum <=1 \r\n";

			//consultas de error
			String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE \r\n"
					+ " from  wmlog.WM_LOG_ERROR \r\n" 
					+ " where RUN_ID='%s') where rownum <=1 \r\n";
			
			String consultaError2 = " select * from (select description,MESSAGE \r\n" 
			        + " from wmlog.WM_LOG_ERROR \r\n"
					+ " where RUN_ID='%s') WHERE rownum <= 1 \r\n";
			
			String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 \r\n"
					+ " from wmlog.WM_LOG_ERROR \r\n" 
					+ " where RUN_ID='%s')WHERE rownum <= 1 \r\n";
			
	//*******************************************************************************************************************************************************************		
			//paso 5
			
			// String validaWM_LOG_RUN = "SELECT * FROM wmlog.wm_log_run WHERE interface = 'RO4' ORDER BY start_dt DESC "; ----
			
			// String validaWM_LOG_THREAD  = "SELECT * FROM wmlog.wm_log_thread WHERE parent_id = '%s' ";-----> Dividida
			
			//*************
			String consultaThreads = " SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS \r\n" 
			        + " FROM WMLOG.WM_LOG_THREAD \r\n"
					+ " WHERE PARENT_ID='%s' \r\n";// Consulta para los Threads
			
			String consultaThreads2 = " SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 \r\n"
			        + " FROM WMLOG.WM_LOG_THREAD \r\n"
					+ " WHERE PARENT_ID='%s' \r\n";// Consulta para los Threads

	//*******************************************************************************************************************************************************************
					
			//Paso 6
					
			String  datos_fem_rtvhead_temp = " select * from(SELECT rtv_order_no, supplier_id, terms_desc, currency_code, exchange_rate, store, wh, completed_date \r\n"  
					+ " FROM RMS100.fem_rtvhead_temp where RTV_ORDER_NO='%s'  AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND \r\n"
					+ " store IN (SELECT store FROM store WHERE substr(store_name10, 1,5) = '" + data.get("plaza") + "')) WHERE rownum <= 1 \r\n";
			
			String  datos_fem_rtvhead_temp2 = "select*from(SELECT ret_auth_num,handling_pct,  handling_cost, comment_desc, reference_1, reference_2, reference_3, reference_4 \r\n"  
					+ " FROM RMS100.fem_rtvhead_temp where RTV_ORDER_NO='%s'  AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND \r\n"
					+ " store IN (SELECT store FROM store WHERE substr(store_name10, 1,5) = '" + data.get("plaza") + "')) WHERE rownum <= 1 \r\n";
			
			String  datos_fem_rtvhead_temp3 = "select*from(SELECT reference_5, reference_6, reference_7, reference_8, reference_9, reference_10, merch_type, fem_remision \r\n"  
					+ "FROM RMS100.fem_rtvhead_temp where RTV_ORDER_NO='%s'  AND reference_3 IS NOT NULL AND reference_8 IS NOT NULL AND \r\n"
					+ " store IN (SELECT store FROM store WHERE substr(store_name10, 1,5) = '" + data.get("plaza") + "')) WHERE rownum <= 1\r\n";
			
					
			
	//*******************************************************************************************************************************************************************
			
			//Paso 7
			
			String datos_invoice_interface = " SELECT invoice_id, invoice_line_id, line_number, line_type_lookup_code, line_group_number, amount \r\n"
					+ " FROM ap.ap_invoice_lines_interface WHERE invoice_id = '%s' \r\n" ;
			
			String datos_invoice_line_interface = " SELECT invoice_id, invoice_line_id, line_number, line_type_lookup_code, \r\n"
					+ " line_group_number, amount FROM ap.ap_invoice_lines_interface WHERE invoice_id = '%s' \r\n";

			
			
			// ********************************************************************************************************************************************************************************

			/* Pasos */

			// ********************************************************************************************************************************************************************************

			// Paso 1

			addStep("Obtener el rango de fechas a procesar por la interfaz RO4_Peru");
			

			System.out.println(GlobalVariables.DB_HOST_Oiwmqa); 

			System.out.println(rangoFechas);// POSTUSER

			SQLResult fechas = dbPuserPeru.executeQuery(rangoFechas);
			String fechaMin = fechas.getData(0, minFechasColumna);
			System.out.println("Fecha minima" + fechaMin); // la imprime
			String fechaMax = fechas.getData(0, maxFechasColumna);
			System.out.println("Fecha maxima" + fechaMax); // la imprime

			boolean checarFechas = fechas.isEmpty(); // checa que el string contenga datos

			if (!checarFechas) {

				testCase.addQueryEvidenceCurrentStep(fechas); // Si no esta vacio, lo agrega a la evidencia
			}

			assertFalse("No se muestran registros que cumplan el rango de ejecucuion.", checarFechas); // Si esta vacio,
																										// imprime
																										// mensaje
			System.out.println(checarFechas); // Si no, imprime la fechas

			// *****************************************************************************************************************************************************************************
			// Paso 2

			addStep("Verificar que existen datos pendientes por procesar en la tabla fem_rtvhead_temp de RETEK para la plaza");

			System.out.println(GlobalVariables.DB_HOST_RMSWMUSERPeru); // RMS

			// Parte 1

			String consulta1 = String.format(pendiente_fem_rtvhead_temp_1, fechaMin, fechaMax);

			System.out.println(consulta1);

			// Obtenermos el rtv_order que se usara en el paso 6

			SQLResult consultaDatos1 = dbRmsPeru.executeQuery(consulta1); // recibe la consulta con las fechas maxima y
																			// minima.

			String rtvOrder = consultaDatos1.getData(0, "RTV_ORDER_NO"); // Obtiene el rtv_order_no de la consulta

			System.out.println("RTV_ORDER_NO: =" + rtvOrder);

			boolean validaConsulta = rtvOrder.isEmpty();

			if (!validaConsulta) {
				testCase.addQueryEvidenceCurrentStep(consultaDatos1);

			}

			System.out.println(validaConsulta);

			// Parte 2

			String consulta2 = String.format(datosPtesProcesar1, fechaMin, fechaMax);

			System.out.println(consulta2);

			SQLResult consultaDatos2 = dbRmsPeru.executeQuery(consulta2);

			boolean validaConsulta2 = consultaDatos2.isEmpty();

			if (!validaConsulta2) {

				testCase.addQueryEvidenceCurrentStep(consultaDatos2);

			}

			System.out.println(validaConsulta2);

			// Parte 3

			String consulta3 = String.format(datosPtesProcesar2, fechaMin, fechaMax);

			System.out.println(consulta3);

			SQLResult consultaDatos3 = dbRmsPeru.executeQuery(consulta3);

			boolean validaConsulta3 = consultaDatos3.isEmpty();

			if (!validaConsulta3) {

				testCase.addQueryEvidenceCurrentStep(consultaDatos3);

			}
			System.out.println(validaConsulta3);

			// Parte 4

			String consulta4 = String.format(datosPtesProcesar3, fechaMin, fechaMax);

			System.out.println(consulta4);

			SQLResult consultaDatos4 = dbRmsPeru.executeQuery(consulta4);

			boolean validaConsulta4 = consultaDatos4.isEmpty();

			if (!validaConsulta4) {
				testCase.addQueryEvidenceCurrentStep(consultaDatos4);

			}
			assertFalse("No se muestran registros que cumplan con el rango de fechas en fem_rechead_temp",
					validaConsulta4);

			System.out.println(validaConsulta4);

			// **************************************************************************************************************************************************************************************
			// Paso 3

			addStep("Verificar que la plaza " + data.get("plaza")
					+ " está configurada para su procesamiento en la tabla wm_cfg_launcher de WMINT en modo AUTO ");

			System.out.println(GlobalVariables.DB_HOST_Oiwmqa); // posuser

			// Parte 1
			System.out.println(verificaConfig);

			SQLResult verificaConfigDatos = dbPuserPeru.executeQuery(verificaConfig);

			boolean consultaConfig = verificaConfigDatos.isEmpty();

			if (!consultaConfig) {

				testCase.addQueryEvidenceCurrentStep(verificaConfigDatos);

			}

			System.out.println(consultaConfig);
			assertFalse("No se muestran registros que cumplan con el rango de fechas en la tabla FEM_RTVHEAD_TEM",
					consultaConfig);

			// ************************************************************************************************************************************************************************************

			// Paso 4

			addStep(" Ejecutar el servicio RO4_MEX.Pub:run");

			// Utileria
			SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
			PakageManagment pok = new PakageManagment(u, testCase);
			String status = "S";

			String user = data.get("user");
			String ps = PasswordUtil.decryptPassword(data.get("ps"));
			String server = data.get("server");
			String searchedStatus = "R";

			System.out.println(GlobalVariables.DB_HOST_Oiwmqa);
			String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
			u.get(contra);

			String dateExecution = pok.runIntefaceWmWithInput(data.get("interfase"), data.get("servicio"),
					data.get("TYPE"), "TYPE");
			System.out.println("Respuesta dateExecution" + dateExecution);

			SQLResult is = dbLogPeru.executeQuery(tdcIntegrationServerFormat);
			String run_id = is.getData(0, "RUN_ID");
			String status1 = is.getData(0, "STATUS");

			boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

			while (valuesStatus) {

				status1 = is.getData(0, "STATUS");
				run_id = is.getData(0, "RUN_ID");
				valuesStatus = status1.equals(searchedStatus);

				u.hardWait(2);

			}

			boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S

			if (!successRun) {

				String error = String.format(consultaError1, run_id);
				String error1 = String.format(consultaError2, run_id);
				String error2 = String.format(consultaError3, run_id);

				SQLResult errorr = dbLogPeru.executeQuery(error);
				boolean emptyError = errorr.isEmpty();
				if (!emptyError) {

					testCase.addTextEvidenceCurrentStep(
							"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

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

			// ***********************************************************************************************************************************************************************************

			// Paso 5

			addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run ");

			boolean validateStatus = status.equals(status1);
			System.out.println(validateStatus);
			assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");

			boolean av2 = is.isEmpty();
			if (av2 == false) {

				testCase.addQueryEvidenceCurrentStep(is);

			} else {
				testCase.addQueryEvidenceCurrentStep(is);
			}
			System.out.println(av2);

			addStep("Verificar que la interfaz genero Threads ");

			String threads = String.format(consultaThreads, run_id);

			System.out.println("CONSULTA THREAD " + threads);

			SQLResult threadsResult = dbLogPeru.executeQuery(threads);

			boolean av31 = threadsResult.isEmpty();
			if (av31 == false) {

				testCase.addQueryEvidenceCurrentStep(threadsResult);

			} else {
				testCase.addQueryEvidenceCurrentStep(threadsResult);
			}
			System.out.println(av31);

			// .-----------Segunda consulta

			String threads2 = String.format(consultaThreads2, run_id);

			SQLResult threadsResult2 = dbLogPeru.executeQuery(threads2);

			boolean av3111 = threadsResult2.isEmpty();
			if (av3111 == false) {

				testCase.addQueryEvidenceCurrentStep(threadsResult2);

			} else {
				testCase.addQueryEvidenceCurrentStep(threadsResult2);
			}
			System.out.println(av31);
			assertFalse("No se generaron threads en la tabla", av3111);

			// ************************************************************************************************************************************************************************************

			// Paso 6

			addStep("Verificar que los datos de la tabla fem_rtvhead_temp de RETEK fueron procesados correctamente ");

			System.out.println(GlobalVariables.DB_HOST_RMSWMUSERPeru); // RMS

			// primera parte

			String consulta_rtvorder = String.format(datos_fem_rtvhead_temp, rtvOrder);

			System.out.println("Consulta 1 : " + consulta_rtvorder);

			SQLResult consultaRTV = dbRmsPeru.executeQuery(consulta_rtvorder);

			boolean validaConsultaRTV = consultaRTV.isEmpty();

			if (!validaConsultaRTV) {

				testCase.addQueryEvidenceCurrentStep(consultaRTV);

			}

			System.out.println(validaConsultaRTV);

			// Segunda parte

			String consulta_rtvorder2 = String.format(datos_fem_rtvhead_temp2, rtvOrder);

			SQLResult consultaRTV2 = dbRmsPeru.executeQuery(consulta_rtvorder2);

			System.out.println("Consulta 2 : " + consulta_rtvorder2);

			String reference_3 = consultaRTV2.getData(0, "reference_3");

			boolean validaConsultaRTV2 = reference_3.isEmpty();

			if (!validaConsultaRTV2) {

				testCase.addQueryEvidenceCurrentStep(consultaRTV2);

			}

			System.out.println(validaConsultaRTV2);

			// Tercera parte

			String consulta_rtvorder3 = String.format(datos_fem_rtvhead_temp3, rtvOrder);

			System.out.println("Consulta 3: " + consulta_rtvorder3);

			SQLResult consultaRTV3 = dbRmsPeru.executeQuery(consulta_rtvorder3);

			boolean validaConsultaRTV3 = consultaRTV3.isEmpty();

			if (!validaConsultaRTV3) {

				testCase.addQueryEvidenceCurrentStep(consultaRTV3);

			}
			assertFalse("No se muestran registros ", validaConsultaRTV3);

			System.out.println(validaConsultaRTV3);

			// *****************************************************************************************************************************************************************************

			// Paso 7

			addStep("Verificar que las Facturas fueron insertadas en la tablas ap_invoices_interface de ORAFIN");

			System.out.println(GlobalVariables.DB_HOST_EBS);

			System.out.println(reference_3);

			// Primera consulta

			String consulta_ap_invoices_interface = String.format(datos_invoice_interface, reference_3);

			System.out.println(consulta_ap_invoices_interface);

			SQLResult invoice_interface = dbEbsPeru.executeQuery(consulta_ap_invoices_interface);

			boolean valida_Invoice_Interface = invoice_interface.isEmpty();

			if (!valida_Invoice_Interface) {

				testCase.addQueryEvidenceCurrentStep(invoice_interface);

			}
			assertFalse(valida_Invoice_Interface,
					"No se encontraron facturas y notas en la tabla ap_invoices_interface");

			System.out.println(valida_Invoice_Interface);

			// Segunda consulta

			addStep("Verificar que las Facturas fueron insertadas en la tabla ap_invoice_lines_interface de ORAFIN");
					

			String consulta_invoices_line_interface = String.format(datos_invoice_line_interface, reference_3);

			System.out.println(consulta_invoices_line_interface);

			SQLResult invoice_interface_lines = dbEbsPeru.executeQuery(consulta_invoices_line_interface);

			boolean valida_Invoice_Interface_lines = invoice_interface_lines.isEmpty();

			if (!valida_Invoice_Interface_lines) {

				testCase.addQueryEvidenceCurrentStep(invoice_interface_lines);

			}
			assertFalse(valida_Invoice_Interface_lines,
					"No se encontraron facturas y notas en la tabla ap_invoice_lines_interface");

			System.out.println(valida_Invoice_Interface_lines);

		}

		@Override
		public String setTestFullName() {
			// TODO Auto-generated method stub
			return "ATC_FT_RO4_Peru_003_NOTAS_CREDITO_AUTO";
		}

		@Override
		public String setTestDescription() {
			// TODO Auto-generated method stub
			return "Construido. Ejecución de la interfaz para el procesamiento de las Notas de crédito en modo AUTO Peru";
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
