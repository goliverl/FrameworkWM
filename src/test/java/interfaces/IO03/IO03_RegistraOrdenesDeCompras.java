package interfaces.IO03;

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


public class IO03_RegistraOrdenesDeCompras extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_IO03_RegistraOrdenesDeCompras(HashMap<String, String> data) throws Exception {
		/*
		 * Utilerias
		 *********************************************************************/
		//utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS,GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,	GlobalVariables.DB_PASSWORD_AVEBQA);

		//Paso 1
		String infoOrdenCompraL = "SELECT*FROM(SELECT DISTINCT h.VENDOR_NUM, l.*\r\n" + 
				"FROM PO_HEADERS h, PO_LINES l\r\n" + 
				"WHERE h.REMESA = l.REMESA\r\n" + 
				"AND h.REMESA = '" + data.get("REMESA") +"'\r\n" + 
				"AND h.WM_STATUS = 'L')WHERE rownum <= 1";
		
		//Paso 2 y 3
			String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl "  
				+ "WHERE INTERFACE =  'IO03' "
				+ "AND TRUNC(START_DT) = TRUNC(SYSDATE)"
				+ "AND STATUS = 'S'"
				+ "ORDER BY START_DT DESC) "
				+ "where rownum <=1";// Consulta para estatus de la ejecucion
			
		//Paso 4
			String infoOrdenCompraE = "SELECT * FROM PO_HEADERS\r\n" + 
					"WHERE WM_RUN_ID = '%s'\r\n" + 
					"AND REMESA = " + data.get("REMESA") +"\r\n" + 
					"AND WM_STATUS = 'E'";
			
		//Paso 5
			String validaHeader = "SELECT INTERFACE_HEADER_ID, BATCH_ID, DOCUMENT_NUM, VENDOR_NUM, COMMENTS FROM PO.PO_HEADERS_INTERFACE\r\n" + 
					"WHERE VENDOR_NUM = '%s'\r\n" + 
					"AND COMMENTS = " + data.get("REMESA") +"";
		//Paso 6
			String validaLineas = "SELECT INTERFACE_LINE_ID, INTERFACE_HEADER_ID, PO_LINE_ID FROM PO.PO_LINES_INTERFACE\r\n" + 
					"WHERE INTERFACE_HEADER_ID = '%s'";
			
		String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// Consulta para los errores
		String consultaERROR2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores
		String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores

		
		
		
		

//Paso 1    ************************        
		addStep("Validar que exista información de las ordenes de compras para el vendor a procesar en las tablas PO_HEADERS y PO_LINES de la base de datos de IMMEX con WM_STATUS = 'L'.");
		System.out.println( infoOrdenCompraL);
		
		SQLResult  infoOrdenCompraLR = dbEbs.executeQuery(infoOrdenCompraL); //cambiar a base de datos IMMEX
		
        String  VENDOR_NUM = infoOrdenCompraLR.getData(0, "VENDOR_NUM");
		
		System.out.println("VENDOR_NUM "+VENDOR_NUM);
		
      
		
		boolean validaInfoPedidosL = infoOrdenCompraLR.isEmpty();
		
		if (!validaInfoPedidosL) {
			
			testCase.addQueryEvidenceCurrentStep(infoOrdenCompraLR);
		}
		assertFalse("No se muestra la información de las lineas de las ordenes de compra pendientes por procesar para el vendor seleccionado ", validaInfoPedidosL);
		


//Paso 2    ************************        
		addStep("Ejecutar la el servicio IO03.Pub:run desde el Job runIO03 de Ctrl-M para registrar en Oracle PO las órdenes de compra de los sistemas IMMEX pendientes a procesar ");
		
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
						"Se encontro un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");

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
		addStep("Validar la correcta ejecución de la interface IO03 en la tabla WM WM_LOG_RUN de WMLOG.");
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
		
		addStep("Validar la actualización de estado de los registros las órdenes de compras enviadas a WM_STATUS = 'E' en la tabla PO_HEADERS de IMMEX.");

		String infoOrdenCompraE_f = String.format(infoOrdenCompraE, run_id);
		
		System.out.println(infoOrdenCompraE_f );
		
		SQLResult infoOrdenCompraE_R = dbEbs.executeQuery(infoOrdenCompraE_f); //Cambiar base de datos a IMMEX	
		
		
		boolean validainfoOrdenCompraE = infoOrdenCompraE_R.isEmpty();

		if (!validainfoOrdenCompraE) {

			testCase.addQueryEvidenceCurrentStep(infoOrdenCompraE_R);
			assertFalse("Se actualiza el estatus de las ordenes de compras enviadas a WM_STATUS = 'E'", validainfoOrdenCompraE);

		}
		// Paso 5 ************************
		addStep("Validar la inserción del Header de la orden de compra en la tabla PO_HEADERS_INTERFACE de ORAFIN.");
		
		String validaHeader_f = String.format(validaHeader, VENDOR_NUM);
		
		System.out.println(validaHeader_f );
		
		SQLResult validaHeader_R = dbEbs.executeQuery(validaHeader_f );
		
		 String  INTERFACE_HEADER_ID = validaHeader_R.getData(0, "INTERFACE_HEADER_ID");
			
			System.out.println("INTERFACE_HEADER_ID  "+INTERFACE_HEADER_ID );
		
	
		boolean validaHeader_B = validaHeader_R.isEmpty();

		if (!validaHeader_B) {

			testCase.addQueryEvidenceCurrentStep(validaHeader_R);

		}
		assertFalse("La información del Header de las órdenes de compras de los sistemas IMMEX no fue insertada en la tabla de Oracle PO de manera correcta.", validaHeader_B);

		// Paso 6 ************************
				addStep("Validar la inserción de las líneas de detalle  de la orden de compra en la tabla PO_LINES_INTERFACE de ORAFIN.");
				
				String validaLineas_f = String.format(validaLineas, INTERFACE_HEADER_ID);
				
				System.out.println(validaLineas);
				
				SQLResult validaLineas_R = dbEbs.executeQuery(validaLineas_f );
					         
				
				boolean validaLineas_B = validaLineas_R.isEmpty();

				if (!validaLineas_B) {

					testCase.addQueryEvidenceCurrentStep(validaLineas_R);

				}
				assertFalse("Las líneas de detalle de las órdenes de compras de los sistemas IMMEX son insertadas en la tabla de Oracle PO de manera correcta.", validaLineas_B);

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Registrar en Oracle OM los pedidos de artículos de los sistemas IMMEX ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_IO03_RegistraOrdenesDeCompras";
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
