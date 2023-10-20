package interfaces.IO01;


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


public class IO01_RegistrarFacturasDeClientes extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_IO01_RegistrarFacturasDeClientes(HashMap<String, String> data) throws Exception {
		/*
		 * Utilerias
		 *********************************************************************/
		//utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS,GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,	GlobalVariables.DB_PASSWORD_AVEBQA);

		//Paso 1
		String PedidosProcesados = "SELECT * FROM AR_LINES\r\n" + 
				"WHERE ORIG_SYSTEM_BILL_CUSTOMER_ID = '" + data.get("ORIG_SYSTEM_BILL_CUSTOMER_ID") +"'\r\n" + 
				"AND WM_STATUS = 'L'";
		
		//Paso 2 y 3
			String tdcIntegrationServerFormat = "	select * fro m (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl "  
				+ "WHERE INTERFACE =  'IO01' "
				+ "AND TRUNC(START_DT) = TRUNC(SYSDATE)"
				+ "AND STATUS = 'S'"
				+ "ORDER BY START_DT DESC) "
				+ "where rownum <=1";// Consulta para estatus de la ejecucion
			
		//Paso 4
			String  actualizaStatusPedidos = "SELECT * FROM AR_LINES\r\n" + 
					"WHERE ORIG_SYSTEM_BILL_CUSTOMER_ID = '" + data.get("ORIG_SYSTEM_BILL_CUSTOMER_ID") +"'\r\n" + 
					"AND WM_STATUS = 'E'\r\n" + 
					"AND WM_RUN_ID = '%s' ";
			
		//Paso 5
			String insercionPedidosProcesados  = "SELECT * FROM RA_INTERFACE_LINES_ALL\r\n" + 
					"WHERE INTERFACE_LINE_CONTEXT = 'IMMEX'\r\n" + 
					"AND INTERFACE_LINE_ATTRIBUTE1 = '%s'\r\n" + 
					"AND INTERFACE_LINE_ATTRIBUTE2 = '%s'";
			
		String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// Consulta para los errores
		String consultaERROR2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores
		String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores

		
		
		
		

//Paso 1    ************************        
		addStep("Validar que exista información de facturas pendientes por procesar en la tabla AR_LINES de IMMEX con WM_STATUS = L para el proveedor ");
		
		System.out.println( PedidosProcesados);
		
		SQLResult  PedidosProcesadosR = dbEbs.executeQuery(PedidosProcesados); //cambiar a base de datos IMMEX	
		
        String  ORIG_SYS_DOCUMENT_REF =  PedidosProcesadosR.getData(0, "ORIG_SYS_DOCUMENT_REF");
		
		System.out.println("ORIG_SYS_DOCUMENT_REF "+ ORIG_SYS_DOCUMENT_REF);
		
		boolean validaPedidosProcesados = PedidosProcesadosR.isEmpty();
		
		if (!validaPedidosProcesados) {
			
			testCase.addQueryEvidenceCurrentStep(PedidosProcesadosR);
		}
		assertFalse("La consulta no retorna la información de las facturas pendientes por procesar de los sistemas IMMEX para el proceedor. ", validaPedidosProcesados);
		


//Paso 2    ************************        
		addStep("Ejecutar el servicio IO01.Pub:run desde el Job runIO01 de Ctrl-M para registrar en ORACLE AR las líneas de las facturas de clientes de los sistemas IMMEX pendientes a procesar ");
		
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
		addStep("Validar la correcta ejecución de la interface IO01 en la tabla WM WM_LOG_RUN de WMLOG.");
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
		
		
		addStep("Validar la actualización de estado de los registros de las líneas de las facturas enviadas a WM_STATUS = 'E' en la tabla AR_LINES de IMMEX.");
		
        String actualizaStatusPedidos_f = String.format(actualizaStatusPedidos, run_id);
		
		System.out.println(actualizaStatusPedidos_f );
		
		SQLResult actualizaStatusPedidos_R = dbEbs.executeQuery(actualizaStatusPedidos_f );
		
		String INTERFACE_LINE_ATTRIBUTE1 = actualizaStatusPedidos_R.getData(0, "INTERFACE_LINE_ATTRIBUTE1");
		
		System.out.println("INTERFACE_LINE_ATTRIBUTE1 "+INTERFACE_LINE_ATTRIBUTE1);
		
        String INTERFACE_LINE_ATTRIBUTE2 = actualizaStatusPedidos_R.getData(0, "INTERFACE_LINE_ATTRIBUTE2");
		
		System.out.println("INTERFACE_LINE_ATTRIBUTE2 "+INTERFACE_LINE_ATTRIBUTE2);
		
		
		boolean validaactualizaStatusPedidos = actualizaStatusPedidos_R.isEmpty();

		if (!validaactualizaStatusPedidos) {

			testCase.addQueryEvidenceCurrentStep(actualizaStatusPedidos_R);

		}
		assertFalse("Se actualiza el estatus de las facturas enviadas a WM_STATUS = 'E'", validaactualizaStatusPedidos);


		
		
		// Paso 5 ************************
		
		addStep("Validar la inserción de las líneas de la facturas del proveedor en la tabla RA_INTERFACE_LINES_ALL de ORAFIN.");
				
        String insercionPedidosProcesados_f = String.format(insercionPedidosProcesados, INTERFACE_LINE_ATTRIBUTE1,INTERFACE_LINE_ATTRIBUTE2);
		
		System.out.println(insercionPedidosProcesados_f );
		
		SQLResult insercionPedidosProcesados_R = dbEbs.executeQuery(insercionPedidosProcesados_f); //Cambiar base de datos a IMMEX	
		
		
		boolean validaInsercionPedidosProcesados = insercionPedidosProcesados_R.isEmpty();

		if (!validaInsercionPedidosProcesados) {

			testCase.addQueryEvidenceCurrentStep(insercionPedidosProcesados_R);
		}
			
			assertFalse("La información de las facturas de los sistemas IMMEX no fue insertada en la tabla de Oracle AR de manera correcta.", validaInsercionPedidosProcesados);


	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Registrar en Oracle AR las facturas de clientes de los sistemas IMMEX ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_IO01_RegistrarFacturasDeClientes";
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
