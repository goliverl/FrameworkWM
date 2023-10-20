package interfaces.IO05;

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


public class IO05_RegistraPedidosProcesados extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_IO05_RegistraPedidosProcesados(HashMap<String, String> data) throws Exception {
		/*
		 * Utilerias
		 *********************************************************************/
		//utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS,GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,	GlobalVariables.DB_PASSWORD_AVEBQA);

		//Paso 1
		String PedidosProcesados = "SELECT * FROM XXFC_OE_ORDERS_V \r\n" + 
				"WHERE WM_STATUS IS NULL \r\n" + 
				"AND CUSTOMER_NUMBER = '" + data.get("CUSTOMER_NUMBER") +"'\r\n" + 
				"AND ORDER_NUMBER = '" + data.get("ORDER_NUMBER") +"' ";
		
		//Paso 2 y 3
			String tdcIntegrationServerFormat = "	select * fro m (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl "  
				+ "WHERE INTERFACE =  'IO05' "
				+ "AND TRUNC(START_DT) = TRUNC(SYSDATE)"
				+ "AND STATUS = 'S'"
				+ "ORDER BY START_DT DESC) "
				+ "where rownum <=1";// Consulta para estatus de la ejecucion
			
		//Paso 4
			String insercionPedidosProcesados = "SELECT * FROM OM2\r\n" + 
					"WHERE NUM_AGENTE = '" + data.get("CUSTOMER_NUMBER") +"'\r\n" + 
					"AND PEDIDO_LEGADO = '%s'\r\n" + 
					"AND PEDIDO_ORACLE = '" + data.get("ORDER_NUMBER") +"' ";
			
		//Paso 5
			String actualizaStatusPedidos = "SELECT * FROM XXFC_OE_ORDERS_V\r\n" + 
					"WHERE CUSTOMER_NUMBER = '" + data.get("CUSTOMER_NUMBER") +"'\r\n" + 
					"AND ORDER_NUMBER = '" + data.get("ORDER_NUMBER") +"'\r\n" + 
					"AND WM_RUN_ID = '%s'\r\n" + 
					"AND WM_STATUS = 'E'";
			
		String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// Consulta para los errores
		String consultaERROR2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores
		String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores

		
		
		
		

//Paso 1    ************************        
		addStep("Validar que exista información de los pedidos procesados pendientes de enviar a SQL IMMEX en la Vista XXFC_OE_ORDERS_V (Tabla OE_ORDER_HEADERS_ALL) de Oracle OM con WM_STATUS = NULL (ATTRIBUTE2) para el cliente.");
		
		System.out.println( PedidosProcesados);
		
		SQLResult  PedidosProcesadosR = dbEbs.executeQuery(PedidosProcesados); //cambiar a base de datos IMMEX	
		
        String  ORIG_SYS_DOCUMENT_REF =  PedidosProcesadosR.getData(0, "ORIG_SYS_DOCUMENT_REF");
		
		System.out.println("ORIG_SYS_DOCUMENT_REF "+ ORIG_SYS_DOCUMENT_REF);
		
		boolean validaPedidosProcesados = PedidosProcesadosR.isEmpty();
		
		if (!validaPedidosProcesados) {
			
			testCase.addQueryEvidenceCurrentStep(PedidosProcesadosR);
		}
		assertFalse("La consulta retorna la información de los pedidos procesados en Oracle OM pendientes a enviar con WM_STATUS = NULL. ",validaPedidosProcesados);
		


//Paso 2    ************************        
		addStep("Ejecutar el servicio IO05.Pub:run desde el Job runIO04 de Ctrl-M para registrar en SQL IMMEX los pedidos procesados en Oracle OM");
		
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
		addStep("Validar la correcta ejecución de la interface IO05 en la tabla WM WM_LOG_RUN de WMLOG.\r\n" + 
				"");
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
		
		addStep("Validar la inserción de los pedidos procesados en Oracle OM en la tabla OM2 de SQL IMMEX ");

		String insercionPedidosProcesados_f = String.format(insercionPedidosProcesados, ORIG_SYS_DOCUMENT_REF);
		
		System.out.println(insercionPedidosProcesados_f );
		
		SQLResult insercionPedidosProcesados_R = dbEbs.executeQuery(insercionPedidosProcesados_f); //Cambiar base de datos a IMMEX	
		
		
		boolean validaInsercionPedidosProcesados = insercionPedidosProcesados_R.isEmpty();

		if (!validaInsercionPedidosProcesados) {

			testCase.addQueryEvidenceCurrentStep(insercionPedidosProcesados_R);
		}
			
			assertFalse("La información de los pedidos procesados del cliente no fue insertada en la tabla de SQL IMMEX de manera correcta.", validaInsercionPedidosProcesados);

		
		// Paso 5 ************************
		addStep("Validar la inserción del Header de la orden de compra en la tabla PO_HEADERS_INTERFACE de ORAFIN.");
		
		String actualizaStatusPedidos_f = String.format(actualizaStatusPedidos, run_id);
		
		System.out.println(actualizaStatusPedidos_f );
		
		SQLResult actualizaStatusPedidos_R = dbEbs.executeQuery(actualizaStatusPedidos_f );
		
		boolean validaactualizaStatusPedidos = actualizaStatusPedidos_R.isEmpty();

		if (!validaactualizaStatusPedidos) {

			testCase.addQueryEvidenceCurrentStep(actualizaStatusPedidos_R);

		}
		assertFalse("Se actualiza el estatus de los pedidos procesados y enviados IMMEX a WM_STATUS = 'E'.", validaactualizaStatusPedidos);

		

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Registrar en SQL IMMEX los pedidos procesados en Oracle OM para cte ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_IO05_RegistraPedidosProcesados";
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
