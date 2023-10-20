package interfaces.ro5col;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
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

public class RO5_COL_facturas_ModoAuto extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_RO5_COL_Ejecucion_Interfaz_Procesamiento_Modo_AUTO(HashMap<String, String> data) throws Exception {

		/*
		 * UtilerÃƒÂ­as
		 ***********************************************************************************************/
	
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS_COL, GlobalVariables.DB_USER_EBS_COL,GlobalVariables.DB_PASSWORD_EBS_COL);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbRMSCOL = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_COL,GlobalVariables.DB_USER_RMS_COL, GlobalVariables.DB_PASSWORD_RMS_COL);
		
		/**
		 * Variables
		 * ***********************************************************************************************
		 * 
		 * 
		 */

		String rangoFechas="SELECT DISTINCT week_nb, to_char(min_date,'DD/MM/RRRR') as min_date, to_char(max_date,'DD/MM/RRRR') as max_date" + 
				" FROM WMUSER.wm_cat_ro4_weeks " + 
				" WHERE max_date < SYSDATE " + 
				" AND week_nb IN nvl((SELECT DISTINCT MAX(decode(status,'E',week_nb,'S',week_nb+1,1)) " + 
				" FROM WMUSER.wm_log_ro5 " + 
				" WHERE cr_plaza = " + "'"+ data.get("plaza") + "'" + 
				" AND week_nb IN (SELECT DISTINCT MAX(week_nb) " + 
				" FROM WMUSER.wm_log_ro5 " + 
				" WHERE cr_plaza = "+ "'"+ data.get("plaza") + "'" + ") ),1)";//posUser
		

		String validarInsumo ="SELECT SUPPLIER_ID, TERMS_DESC, LOC, REFERENCE_3, REFERENCE_8, receive_date "
				+ "FROM fem_rechead_temp " + 
				" WHERE TRUNC(receive_date) BETWEEN "
				+ " TO_DATE('%s','DD/MM/YYYY')" + 
				" AND TO_DATE('%s','DD/MM/YYYY')" + 
				" AND reference_3 IS NULL" + 
				" AND reference_8 IS NULL" + 
				" AND loc IN (SELECT store FROM store WHERE SUBSTR(store_name10, 1,5) = "+ "'"+ data.get("plaza") + "'" + ")";
		
		String procesoRechead ="SELECT SUPPLIER_ID, TERMS_DESC, LOC, REFERENCE_3, REFERENCE_8, receive_date "
				+ "FROM fem_rechead_temp " + 
				" WHERE TRUNC(receive_date) BETWEEN "
				+ " TO_DATE('%s','DD/MM/YYYY')" + 
				" AND TO_DATE('%s','DD/MM/YYYY')" + 
				" AND reference_3 IS NOT NULL" + 
				" AND reference_8 IS NOT NULL" + 
				" AND loc IN (SELECT store FROM store WHERE SUBSTR(store_name10, 1,5) = "+ "'"+ data.get("plaza") + "'" + ")";
		
				
		String wm_cfg_launcher ="SELECT * FROM wmuser.wm_cfg_launcher " + 
				" WHERE interface_name = 'RO5_COL'" + 
				" AND auto_status = 'A' AND attribute1 = "+ "'"+ data.get("plaza") + "'" ;
	

		String tdcQueryIntegrationServer = "SELECT * FROM wmlog.wm_log_run" + 
				" WHERE interface = 'RO5_COL'" + 
				" AND start_dt>=TRUNC(SYSDATE)" + 
				" ORDER BY start_dt DESC";
		
		
		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " 
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA


		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = %s"; // FCWMLQA
		
		String consulta5 = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER "
				+ " FROM WMLOG.WM_LOG_RUN "
				+ " WHERE RUN_ID =%s";
		
		String invoicesInterface="SELECT INVOICE_ID, INVOICE_DATE, VENDOR_ID, INVOICE_CURRENCY_CODE, DESCRIPTION"
				+ " FROM ap.ap_invoices_interface" + 
				" WHERE invoice_id = '%s'";//REF_3

		String invoiceLines = "SELECT INVOICE_ID, INVOICE_LINE_ID, LINE_NUMBER, LINE_TYPE_LOOKUP_CODE, TAX_CODE"
				+ " FROM ap.ap_invoice_lines_interface " + 
				" WHERE invoice_id = '%s'";//REF_3


		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 */
		
		
		

//	Paso 1	************************		

		addStep("Obtener el rango de fechas a procesar por la interfaz RO5_COL en la BD WMINT.");

		System.out.println(rangoFechas);

		SQLResult fechas = dbPos.executeQuery(rangoFechas);
		String fechaMin = fechas.getData(0, "MIN_DATE");
		String fechaMax = fechas.getData(0, "max_date");
		
		boolean paso1 = fechas.isEmpty();
		if (!paso1) {
			testCase.addQueryEvidenceCurrentStep(fechas);
		}
		assertFalse("No se muestran registros que cumplan con el rango de fechas de ejecucion", paso1);

//	Paso 2	************************		
		addStep("Verificar que existen datos pendientes por procesar en la tabla fem_rechead_temp de RETEK para la plaza "	+ data.get("plaza") + " con el rango de fechas." );
		
		String validarInsumoFormat = String.format(validarInsumo, fechaMin,fechaMax);
		
		SQLResult queryvalidarInsumo = executeQuery(dbRMSCOL, validarInsumoFormat);
		
		boolean insumoEmpty = queryvalidarInsumo.isEmpty();
		
		if (!insumoEmpty) {
			testCase.addQueryEvidenceCurrentStep(queryvalidarInsumo);
		}
		assertTrue(!insumoEmpty, "No hay registros a procesar"); 
		
	
//					Paso 3	************************	
		addStep("Verificar que la plaza 10BGA está configurada para su procesamiento en la tabla wm_cfg_launcher de WMINT.");

		SQLResult cfg = dbPos.executeQuery(wm_cfg_launcher);
		System.out.println(wm_cfg_launcher);

		boolean existeCfg = cfg.isEmpty();
		
		if (!existeCfg) {
			testCase.addQueryEvidenceCurrentStep(cfg);
		}
		assertTrue(!existeCfg, "La plaza no esta configurada");
		
//		Paso 4	************************	
		
		addStep("Ejecutar el servicio RO5_COL.Pub:run, solicitando el job: runRO5_COL.");
		
		// utileria
				SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
				PakageManagment pok = new PakageManagment(u, testCase);
				String status = "S";

				
				String user = data.get("user");
				String ps = PasswordUtil.decryptPassword(data.get("ps"));
				String server = data.get("server");
				String searchedStatus = "R";
				String run_id;

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWmWithInput10(data.get("interfase"), data.get("servicio"), data.get("TYPE"), "type");
		
		SQLResult result5 = executeQuery(dbLog, tdcQueryIntegrationServer);
		
		
		String status1 = result5.getData(0, "STATUS");
		run_id = result5.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			result5 = executeQuery(dbLog, tdcQueryIntegrationServer);
			status1 = result5.getData(0, "STATUS");
			run_id = result5.getData(0, "RUN_ID");
			

			u.hardWait(2);

		}
		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult result3 = executeQuery(dbLog, error);

			boolean emptyError = result3.isEmpty();
			

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(result3);

			}
		}
		u.close();
		// Paso 5 ************************
		addStep("Verificar que la interfaz se ejecuto correctamente, en la tabla wm_log_run deberá existir un registro con el campo status en \"S\".");
		
		
			System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
			String verificacionInterface = String.format(consulta5, run_id);
			SQLResult paso4 = executeQuery(dbLog, verificacionInterface);
			System.out.println(verificacionInterface);



			boolean av5 = paso4.isEmpty();
			
			if (!av5) {

				testCase.addQueryEvidenceCurrentStep(paso4);
				
			} 

			System.out.println(av5);

			
			assertFalse(av5, "No se obtiene informacion de la consulta");
//			

//		Paso 6  *************************************************
		addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD con STATUS = 'S'");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String consultaTemp6 = String.format(tdcQueryStatusThread, run_id);
		SQLResult paso5 = executeQuery(dbLog, consultaTemp6);
		
		System.out.println(consultaTemp6);
		String estatusThread = paso5.getData(0, "Status");

		boolean SR = estatusThread.equals(status);
		SR = !SR;
		
		if (!SR) {

			testCase.addQueryEvidenceCurrentStep(paso5);
			
		} 

		System.out.println(SR);

		
		assertFalse(SR, "No se obtiene informacion de la consulta");
		
//		Paso 7  *************************************************
		addStep("Verificar que los datos de la tabla fem_rechead_temp de RETEK fueron procesados correctamente.");
		
		String RHDFormat = String.format(procesoRechead,fechaMin,fechaMax);
		 SQLResult RHD = dbRMSCOL.executeQuery(RHDFormat);
			System.out.println("Aqui: "+RHDFormat);

		 String ref3_RTV = RHD.getData(0, "reference_3");

		 boolean existeRegistro = RHD.isEmpty();
		
		if (!existeRegistro) {
			testCase.addQueryEvidenceCurrentStep(RHD);

		}
		assertTrue(!existeRegistro, "No existen datos a procesar en fem_rechead_temp"); 
//	
		
		// ************************ Paso 7 ************************
		addStep("Verificar que las Facturas fueron insertadas en las tablas ap_invoices_interface y ap_invoice_lines_interface de ORAFIN.");
		
		String consulta1 = String.format(invoicesInterface, ref3_RTV);
		SQLResult queryInvoices1 = dbEbs.executeQuery(consulta1);
		boolean av = queryInvoices1.isEmpty();
		if (!av) {

			testCase.addQueryEvidenceCurrentStep(queryInvoices1);
		}
		System.out.println(av);
		// .-----------Segunda consulta
		String consulta2 = String.format(invoiceLines, ref3_RTV);
		SQLResult queryInvoices2 = dbEbs.executeQuery(consulta2);
		 av = queryInvoices2.isEmpty();
		if (!av) {

			testCase.addQueryEvidenceCurrentStep(queryInvoices2);
		}
		System.out.println(av);
		assertFalse("No se generaron threads en la tabla", av);
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_RO5_COL_Ejecucion_Interfaz_Procesamiento_Modo_AUTO";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Ejecución de la interfaz para el procesamiento de las Facturas en modo AUTO.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO DE AUTOMATIZACION";
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