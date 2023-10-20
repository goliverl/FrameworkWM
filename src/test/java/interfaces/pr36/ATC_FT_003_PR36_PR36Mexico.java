package interfaces.pr36;

import static org.testng.Assert.assertFalse;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.TimeZone;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ATC_FT_003_PR36_PR36Mexico extends BaseExecution {
	public WebDriver webDriver;
	private Connection conn;
	public String run_id;

	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PR36_PR36Mexico_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);		
		utils.sql.SQLUtil dbwmUser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);


		try {
			Class.forName("oracle.jdbc.OracleDriver");
		} catch (ClassNotFoundException e) {
			logger.error("JDBC class name not found", e);
		}

		logger.info("Oracle JDBC driver loaded ok.");

		try {
			TimeZone timeZone = TimeZone.getTimeZone("Mexico/Monterrey");
		     TimeZone.setDefault(timeZone);
		      conn = null;
			conn = DriverManager.getConnection("jdbc:oracle:thin:@" + GlobalVariables.DB_HOST_FCWMQA_UPD, GlobalVariables.DB_USER_FCWMQA_UPD, GlobalVariables.DB_PASSWORD_FCWMQA_UPD);
			conn.createStatement();
			logger.debug("Connect with @oracle:1521:orcl");
		} catch (SQLException e) {
			logger.error("Error connecting to database.", e);
			throw (e);
		}
		
		
		/**
		 * Informacion de ejecucion
		 * ******************************************************************************************
		 * Para poder ejecutar de forma correcta la interfaz se debe realizar lo siguiente o contactar a @author 53012(Iván Carvajal)
		 * 1.- Realizar la consulta select * from WMUSER.WM_DATES_EXEC where INTERFASE='PR36'
		 * 2.- Al obtener el end_date colocarlo en el query available_tiendas
		 * 3.- si no sale ningun resultado cambias la fecha end_date por un mes anerior ejemplo si end_date es 04/02/2022 10:50:51 AM restarle un mes 04/01/2022 10:50:51 AM 
		 * y volver a realizar la consulta hasta que aparezca una plaza(location)
		 * 4.- realizar el update updDates en donde en SET el valor es la fecha que si mostro un location y en el where la ultima fecha de end_date registrada(la mas reciente)
		 * 5.- ejecutar la interfaz de manera manual y observar los threads que haya almenos uno en ststus S
		 * 6.- Al encontrar la plaza que tenga estatus S modificarla en el dataProvider
		 * 7. Ejecutar
		 * 
		 */

		
		String updDates = "UPDATE WMUSER.WM_DATES_EXEC " + 
				" SET END_DATE = to_date ('04/01/2022 10:50:51 AM','DD/MM/YYYY HH:MI:SS AM')" + 
				" where END_DATE = to_date ('%s','DD/MM/YYYY HH:MI:SS AM')";
		
		String end_date = "";
	
		String pos_outbound_docs = "SELECT * FROM (SELECT ID, DOC_NAME, DOC_TYPE, SENT_DATE, PV_CR_PLAZA, PV_CR_TIENDA " + 
				" FROM POSUSER.POS_OUTBOUND_DOCS" + 
				" WHERE SOURCE_ID='%s'" + 
				" AND PV_CR_PLAZA ='" + data.get("plaza") +"'" 
				+ " AND PV_CR_TIENDA = '"+ data.get("tienda") +"' "+ ")"//La tienda la encuentras ejecutando el query sin esta linea
						+ " where rownum = 1";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" 
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PR36main'" 
				+ " and  start_dt >= TRUNC(SYSDATE)"
				+ " order by start_dt desc)" + " where rownum = 1";
		
		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " 
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA

		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = %s AND ATT1='" + data.get("plaza") +"' "  ; // FCWMLQA
		
		String consulta5 = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER "
				+ " FROM WMLOG.WM_LOG_RUN "
				+ " WHERE RUN_ID =%s";
		
		String last_exec = "select to_char(max(END_DATE),'DD/MM/YYYY HH:MI:SS AM') LAST_EXEC_DATE from"
				+ " WMUSER.WM_DATES_EXEC where INTERFASE='PR36'";

		String ftp_config = "select ftp_base_dir, ftp_serverhost, ftp_serverport, ftp_username, "
				+ "ftp_password as ftp_password from wmuser.wm_ftp_connections where ftp_conn_id = 'PR50POS'";

		String available_tiendas = "select LOCATION, SUBSTR(store_name10,1,5) PLAZA from (SELECT DISTINCT TO_NUMBER(LOCATION) LOCATION \r\n" + 
				" FROM  (SELECT DISTINCT LOCATION FROM wmuser.repl_item_loc ril, wmuser.item_loc_traits ilt, wmuser.sups s \r\n" + 
				" WHERE source_wh IS NULL \r\n" + 
				" AND ilt.item (+) = ril.item\r\n" + 
				" AND ilt.loc (+) = ril.LOCATION \r\n" + 
				" AND ril.primary_repl_supplier = s.supplier \r\n" + 
				" AND ((ril.last_update_datetime > TO_DATE ('%s', 'DD/MM/YYYY HH:MI:SS AM') --hora exac dates posuser\r\n" + 
				" AND ril.last_update_datetime <= TO_DATE (to_char(sysdate,'DD/MM/YYYY HH:MI:SS AM'), 'DD/MM/YYYY HH:MI:SS AM')) \r\n" + 
				" OR (ilt.last_update_datetime > TO_DATE ('%s', 'DD/MM/YYYY HH:MI:SS AM') \r\n" + 
				" AND ilt.last_update_datetime <= TO_DATE (to_char(sysdate,'DD/MM/YYYY HH:MI:SS AM'), 'DD/MM/YYYY HH:MI:SS AM'))) \r\n" + 
				" AND ril.loc_type='S') ) a, store b \r\n" + 
				" where a.LOCATION=b.STORE \r\n" + 
				" AND SUBSTR(store_name10,1,5) ='" + data.get("plaza") +"'" + 
				" order by PLAZA";

		/*
		 * Variables
		 *********************************************************************/
	    String status = "S";
	    String searchedStatus = "R";
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String statusSuccess = "S";
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);

			
		/**
		 * 
		 * **********************************Pasos del caso de Prueba * *****************************************
		 * 
		 * 
		 */

// 	Paso 1 ***********************************************************************************************
		
		addStep("Verificar la configuración FTP:");
		SQLResult ftp_config_result = executeQuery(dbwmUser, ftp_config);

		boolean av = ftp_config_result.isEmpty();

		if (!av) {
			testCase.addQueryEvidenceCurrentStep(ftp_config_result);
		}

		assertFalse(av, "ERROR ,No se tiene configuración de FTP Outbound");

//Paso 2 ***********************************************************************************************

		addStep("Verificar la ultima fecha de ejecución en la tabla WM_DATES_EXEC:");

		SQLResult last_exec_date_results = executeQuery(dbwmUser, last_exec);
		String ultimaEjecucion = last_exec_date_results.getData(0, "LAST_EXEC_DATE");
		String updDatesFormat= String.format(updDates, ultimaEjecucion);
		System.out.println(updDatesFormat);
		System.out.println(ultimaEjecucion);
		
		PreparedStatement stmt;
		stmt = conn.prepareStatement(updDatesFormat);
		int rows = stmt.executeUpdate();
		System.out.println("FILAS AFECTADAS"+rows);

		SQLResult last_exec_date_results2 = executeQuery(dbwmUser, last_exec);
		System.out.println(last_exec_date_results2.getData(0, "LAST_EXEC_DATE"));

		boolean in = last_exec_date_results2.isEmpty();

		if (!in) {
			testCase.addQueryEvidenceCurrentStep(last_exec_date_results);
			end_date = last_exec_date_results2.getData(0, "LAST_EXEC_DATE");
		}
		assertFalse(in, "ERROR ,No se tiene la última fecha de ejecución de la interface");

//		// Paso 3
//		// ***********************************************************************************************

		addStep("Verificar que existan tiendas disponibles a procesar");
		String query = String.format(available_tiendas, end_date, end_date);
		SQLResult available_tiendas_results = executeQuery(dbRms, query);
		System.out.println(query);

		testCase.addQueryEvidenceCurrentStep(available_tiendas_results);
		
		assertFalse(available_tiendas_results.isEmpty(), "ERROR , no se encontraron tiendas a procesar");
		
		// Paso 4
				// ***********************************************************************************************
		addStep("Ejecutar la interfaz por medio del servicio -PR36.Pub:run. El servicio será invocado con el Job PR36.");
		  
	
		//for(int i=0;i<2;i++) {
		//	Thread.sleep(5);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		
		
		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		
		SQLResult result5 = executeQuery(dbLog, tdcQueryIntegrationServer);
		String status1 = result5.getData(0, "STATUS");
		 run_id = result5.getData(0, "RUN_ID");
		
		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			result5 = executeQuery(dbLog, tdcQueryIntegrationServer);
			status1 = result5.getData(0, "STATUS");
			run_id = result5.getData(0, "RUN_ID");
			
	
			Thread.sleep(2);
	
		}
		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {
	
			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult result3 = executeQuery(dbLog, error);
	
			boolean emptyError = result3.isEmpty();
			
	
			if (!emptyError) {
	
				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
	
				testCase.addQueryEvidenceCurrentStep(result3);
	
			}
		}else {
			testCase.addTextEvidenceCurrentStep("El servicio PR36.Pub:run fue ejecutado exitosamente");
		}
		//}
		// Paso 5
		// ***********************************************************************************************
		
	 addStep("Verificar el estatus con el cual fue terminada la ejecución de la interface en la tabla WM_LOG_RUN del usuario WMLOG.");
		
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

//	Paso 6  *************************************************
	addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD con STATUS = 'S'");
	
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

		// Paso 7
		// ***********************************************************************************************
		
		addStep("Verificar que el registro fue insertado en la tabla POS_OUTBOUND_DOCS.");
		
		String outboundFormat = String.format(pos_outbound_docs, run_id);
		System.out.println(outboundFormat);
		SQLResult pos_outbound_docs_result = executeQuery(dbwmUser, outboundFormat);
		
		boolean validationOut = pos_outbound_docs_result.isEmpty();
		
		if(!validationOut) {
			testCase.addQueryEvidenceCurrentStep(pos_outbound_docs_result);
			
		}
		
		assertFalse(SR, "No se inserto el docuemnto");


	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Verificar procesamiento de la interfaz Proveedor";
	}

	@Override
	public String setTestDesigner() {
		return "Equipo de Automatizacion";
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
