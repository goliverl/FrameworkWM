package interfaces.IO04;

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


public class IO04_RegistraPedidosDeArticulos extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_IO04_Registrar_Pedidos_Articulos(HashMap<String, String> data) throws Exception {
		/*
		 * Utilerias
		 *********************************************************************/
		//utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS,GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,	GlobalVariables.DB_PASSWORD_AVEBQA);

		//Paso 1
		String infoPedidosL = "SELECT * FROM(SELECT DISTINCT l.* \r\n" + 
				"FROM OM_HEADERS h, OM_LINES l\r\n" + 
				"WHERE h.ORDER_SOURCE = l.ORDER_SOURCE_ID\r\n" + 
				"AND h.ORIG_SYS_DOCUMENT_REF = l.ORIG_SYS_DOCUMENT_REF\r\n" + 
				"AND h.CUSTOMER_NUMBER = '" + data.get("CUSTOMER_NUMBER") +"'\r\n" + 
				"AND h.WM_STATUS = 'L')WHERE rownum <= 1";
		
		//Paso 2
			String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl "  
				+ "WHERE INTERFACE =  'IO04' "
				+ "AND TRUNC(START_DT) = TRUNC(SYSDATE)"
				+ "AND STATUS = 'S'"
				+ "ORDER BY START_DT DESC) "
				+ "where rownum <=1";// Consulta para estatus de la ejecucion
			
		//Paso 4
			String infoPedidosE = "SELECT * FROM OM_HEADERS\r\n" + 
					"WHERE CUSTOMER_NUMBER = '" + data.get("CUSTOMER_NUMBER") +"'\r\n" + 
					"AND WM_RUN_ID = '%s'\r\n" + 
					"AND WM_STATUS = 'E'";
			
		//Paso 5
			String validaPedidosOFRAIN = "SELECT * FROM OE_HEADERS_IFACE_ALL\r\n" + 
					"WHERE CUSTOMER_NUMBER = '" + data.get("CUSTOMER_NUMBER") +"'\r\n" + 
					"AND ORIG_SYS_DOCUMENT_REF = '%s'\r\n" + 
					"AND TRUNC(CREATION_DATE) = TRUNC(SYSDATE)";
		//Paso 6
			String insersionLineas = "SELECT * FROM ONT.OE_LINES_IFACE_ALL\r\n" + 
					"WHERE ORDER_SOURCE_ID = '%s'\r\n" + 
					"AND ORIG_SYS_DOCUMENT_REF = '%s'\r\n" + 
					"AND TRUNC(CREATION_DATE) = TRUNC(SYSDATE)";
			
		String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// Consulta para los errores
		String consultaERROR2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores
		String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores


//Paso 1    ************************        
		addStep("Validar que exista información de los pedidos de artículos para el Cliente pendientes por procesar en las tablas OM_HEADERS y OM_LINES de IMMEX con WM_STATUS = 'L'");
		System.out.println(infoPedidosL);
		
		SQLResult infoPedidosLR = dbEbs.executeQuery(infoPedidosL); //cambiar a base de datos IMMEX
		
		boolean validaInfoPedidosL = infoPedidosLR.isEmpty();
		
		if (!validaInfoPedidosL) {
			
			testCase.addQueryEvidenceCurrentStep(infoPedidosLR);
		}
		assertFalse("La consulta retorna la información de los pedidos de artículos de los sistemas IMMEX pendientes por procesar con WM_STATUS = 'L'.", validaInfoPedidosL);
		


//Paso 2    ************************        
		addStep("Ejecutar el servicio IO04.Pub:run desde el Job runIO04 Ctrl-Mpara registrar en Oracle OM los pedidos de artículos de los sistemas IMMEX pendientes de procesar");
		
		String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		
		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
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

			String error = String.format(consultaERROR, run_id);
			String error1 = String.format(consultaERROR2, run_id);
			String error2 = String.format(consultaERROR3, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontr? un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");

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
//Paso 3    ************************		
		addStep("Validar la correcta ejecución de la interface IO04 en la tabla WM WM_LOG_RUN de WMLOG. ");
		SQLResult is1 = executeQuery(dbLog, tdcIntegrationServerFormat);

		String fcwS = is1.getData(0, "STATUS");
		
		boolean validateStatus = fcwS.equals(status);
		System.out.println(validateStatus);
		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");
		SQLResult log = dbLog.executeQuery(tdcIntegrationServerFormat);
		System.out.println("Respuesta " + log);
		
		boolean log1 = log.isEmpty();
		
		if (!log1) {

			testCase.addQueryEvidenceCurrentStep(log);
		}

		System.out.println(log1);
		assertFalse("r", log1);

		// Paso 4 ************************
		
		addStep("Validar la actualización de estado de los registros de los pedidos de artículos enviados a WM_STATUS = 'E' en la tabla OM_HEADERS de IMMEX.");

		String infoPedidosE_f = String.format(infoPedidosE, run_id);
		
		System.out.println(infoPedidosE_f );
		
		SQLResult infoPedidosE_R = dbEbs.executeQuery(infoPedidosE_f); //Cambiar base de datos a IMMEX	
		
		String  ORIG_SYS_DOCUMENT_REF= infoPedidosE_R.getData(0, "ORIG_SYS_DOCUMENT_REF");
		
		System.out.println("ORIG_SYS_DOCUMENT_REF "+ORIG_SYS_DOCUMENT_REF);
		
     
		boolean validaInfoPedidosE = infoPedidosE_R.isEmpty();

		if (!validaInfoPedidosE) {

			testCase.addQueryEvidenceCurrentStep(infoPedidosE_R);
			assertFalse("No se actualizo el estatus de los pedidos de artículos enviados a WM_STATUS = 'E'", validaInfoPedidosE);

		}
		// Paso 5 ************************
		addStep("Validar la inserción del Header de los pedidos de artículos en la tabla OE_HEADERS_IFACE_ALL de ORAFIN.");
	
		
		String validaPedidosOFRAIN_f = String.format(validaPedidosOFRAIN, ORIG_SYS_DOCUMENT_REF);
		
		System.out.println(validaPedidosOFRAIN_f);
		
		SQLResult validaPedidosOFRAIN_R = dbEbs.executeQuery(validaPedidosOFRAIN_f );
		
         String  ORDER_SOURCE_ID= validaPedidosOFRAIN_R.getData(0, "ORDER_SOURCE_ID");
		
		System.out.println("ORDER_SOURCE_ID "+ORDER_SOURCE_ID);
		
        String  ORIG_SYS_DOCUMENT_REF_OFRAIN= infoPedidosE_R.getData(0, "ORIG_SYS_DOCUMENT_REF");
		
		System.out.println("ORIG_SYS_DOCUMENT_REF "+ ORIG_SYS_DOCUMENT_REF_OFRAIN);
		
		boolean validaPedidosOFRAIN_B = validaPedidosOFRAIN_R.isEmpty();

		if (!validaPedidosOFRAIN_B) {

			testCase.addQueryEvidenceCurrentStep(validaPedidosOFRAIN_R);

		}
		assertFalse("La información del Header de los pedidos de artículos de los sistemas IMMEX del cliente no fue insertada en la tabla de Oracle OM ", validaPedidosOFRAIN_B);

		// Paso 6 ************************
				addStep("Validar la inserción de las líneas de detalle  de los pedidos de artículos en la tabla ONT.OE_LINES_IFACE_ALL de ORAFIN.");
			
				
				String insersionLineas_f = String.format(insersionLineas, ORDER_SOURCE_ID, ORIG_SYS_DOCUMENT_REF_OFRAIN);
				
				System.out.println(insersionLineas);
				
				SQLResult insersionLineas_R = dbEbs.executeQuery(insersionLineas_f );
					         
				
				boolean insersionLineas_B = insersionLineas_R.isEmpty();

				if (!insersionLineas_B) {

					testCase.addQueryEvidenceCurrentStep(insersionLineas_R);

				}
				assertFalse("Las líneas de detalle de los pedidos de artículos de los sistemas IMMEX no fueron insertadas en la tabla de Oracle OM de manera correcta.", insersionLineas_B);

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Registrar en Oracle OM los pedidos de artículos de los sistemas IMMEX.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_IO04_Registrar_Pedidos_Articulos";
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

}
