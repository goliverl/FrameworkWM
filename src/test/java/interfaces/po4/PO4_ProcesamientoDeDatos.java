package interfaces.po4;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import modelo.TestCase;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.FTPUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class PO4_ProcesamientoDeDatos extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PO4_002_Procesamiento_De_Datos(HashMap<String, String> data) throws Exception {
	
/** UTILERIA *********************************************************************/	
		
								//ORIGINAL
								//new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX,GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbFcr = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCRMSMGR, GlobalVariables.DB_USER_FCRMSMGR,GlobalVariables.DB_PASSWORD_FCRMSMGR); //NUCLEO
		
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA); // SIN CAMBIOS
		
								//ORIGINAL
								//new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbFcw = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_fcwmesit, GlobalVariables.DB_USER_fcwmesit, GlobalVariables.DB_PASSWORD_fcwmesit); //NUCLEO
	    //utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS, GlobalVariables.DB_PASSWORD_EBS); 
		
		SeleniumUtil u;
		PakageManagment pok;	
		testCase.setProject_Name("Remediaciones SYGNIA (AP1)");
		
/** VARIABLES *********************************************************************/	

		String status = "S"; // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id;
		
		String consulta1 = "SELECT RETEK_CR, ORACLE_CR_SUPERIOR, ORACLE_CR, ORACLE_CR_DESC" 
				+ " FROM XXFC_MAESTRO_DE_CRS_V" 
				+ " WHERE ESTADO = 'A'"
				+ " AND ORACLE_CR_TYPE = 'T'" 
				+ " AND ORACLE_CR_SUPERIOR = '" + data.get("plaza") + "'" 
				+ " AND ORACLE_CR = '" + data.get("tienda") + "'";
		
		String consulta2 = "SELECT C.SOURCE_ID, C.POS_CONFIG_ID, C.RECORD_TYPE, C.TENDER_TYPE_GROUP, C.TENDER_TYPE_DESC, S.SEQ_NO, S.STATUS" 
				+ " FROM WMUSER.FEM_POS_CFG_STG C, WMUSER.FEM_POS_CFG_STG_STORE S, WMUSER.FEM_POS_CFG_STG_ITEM I"
				+ " WHERE C.SOURCE_ID = S.SOURCE_ID" 
				+ " AND C.POS_CONFIG_ID = S.POS_CONFIG_ID" 
				+ " AND C.RECORD_TYPE = S.RECORD_TYPE"
				+ " AND S.SOURCE_ID = I.SOURCE_ID(+)" 
				+ " AND S.POS_CONFIG_ID = I.POS_CONFIG_ID(+)" 
				+ " AND S.RECORD_TYPE = I.RECORD_TYPE(+)"
				+ " AND S.STORE = I.STORE(+)"
				+ " AND C.COMPLETE_IND = 'Y'" 
				+ " AND S.WM_ID IS NULL"
				+ " AND S.STORE = %s";	//retek_cr
		
		String consulta3 = "SELECT ftp_conn_id, ftp_base_dir, ftp_serverhost, ftp_serverport, ftp_username"
				+ " FROM WMUSER.WM_FTP_CONNECTIONS" 
				+ " WHERE FTP_CONN_ID = 'PR50POS'";
		
		String  tdcQueryIntegrationServer = "SELECT * FROM (SELECT run_id, start_dt, status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PO4'" 	
				+ " AND start_dt >= TRUNC(SYSDATE)"
			    + " ORDER BY start_dt DESC)"
				+ " WHERE ROWNUM <= 1";	
		
		String tdcQueryErrorId = "SELECT ERROR_ID, RUN_ID, ERROR_DATE, DESCRIPTION"
				+ " FROM WMLOG.WM_LOG_ERROR"
				+ " WHERE RUN_ID = %s"; //FCWMLQA 
		
		String consulta5 = "SELECT RUN_ID, INTERFACE, START_DT, STATUS, SERVER"
				+ "FROM WMLOG.WM_LOG_RUN"
				+ "WHERE RUN_ID = %s";		

		String consulta6 = "SELECT parent_id, thread_id, name, status, att1, att2"
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = %s" ; //FCWMLQA 
		
		String consulta7= "SELECT ID, TARGET_TYPE, DOC_NAME, PV_CR_PLAZA, PV_CR_TIENDA, SENT_DATE"
				+ " FROM POSUSER.POS_OUTBOUND_DOCS" 
				+ " WHERE DOC_TYPE = 'PMC'" 
				+ " AND PV_CR_PLAZA = '" + data.get("plaza") + "'" 
				+ " AND PV_CR_TIENDA = '"+ data.get("tienda") + "'";
			//	+ " AND TRUNC(SENT_DATE) = TRUNC(SYSDATE)";
		
		String consulta9= "SELECT SOURCE_ID, POS_CONFIG_ID, RECORD_TYPE, STATUS, WM_ID, EXTRACT_DATE"
				+ " FROM WMUSER.FEM_POS_CFG_STG_STORE" 
				+ " WHERE STORE = %s" 	 //retek_cr
				+ " AND WM_ID = '%s'"; 	//doc_name 
			//	+" AND TRUNC(EXTRACT_DATE) = TRUNC(SYSDATE)";		
		
		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
		
		
		//													PASO 1
		
		addStep("Consultar el RETEK_CR de la tienda a procesar en la tabla XXFC_MAESTRO_DE_CRS_V de ORAFIN, para buscar la información pendiente por procesar.");		
	
		System.out.println(GlobalVariables.DB_HOST_FCRMSMGR);
		SQLResult paso1 = executeQuery(dbFcr, consulta1);		
		String Retek_CR = paso1.getData(0, "retek_cr");
		System.out.println(consulta1);

		boolean av = paso1.isEmpty();		
		if (!av) {
			testCase.addQueryEvidenceCurrentStep(paso1);
		} 

		System.out.println(av);		
		assertFalse(av, "No se obtiene informacion de la consulta");
		
		
		//													 PASO 2 
	
		addStep("Validar que exista información pendiente por procesar para la tienda en la base de RETEK.");		
	
		System.out.println(GlobalVariables.DB_HOST_FCRMSMGR);
		String consultaTemp2 = String.format(consulta2, Retek_CR);
		System.out.println(consultaTemp2);
		SQLResult paso2 = executeQuery(dbFcr, consultaTemp2);

		boolean av2 = paso2.isEmpty();		
		if (av2) {
			testCase.addTextEvidenceCurrentStep("No existe registro con S.STORE = " + Retek_CR);	
		} 
		
		testCase.addQueryEvidenceCurrentStep(paso2);

		System.out.println(av2);		
		// assertFalse(av2, "No se obtiene informacion de la consulta");
		 
		 
		//													 PASO 3
		 
		addStep("Validar los datos de conexión FTP para el envío del archivo en la tabla WM_FTP_CONNECTIONS de WMINT.");
		
		System.out.println(GlobalVariables.DB_HOST_fcwmesit);
		SQLResult paso3 = executeQuery(dbFcw, consulta3);
		System.out.println(consulta3);		
		boolean av3 = paso3.isEmpty();
		
		if (!av3) {
			testCase.addQueryEvidenceCurrentStep(paso3);
		} 

		System.out.println(av3);		
		assertFalse(av3, "No se obtiene informacion de la consulta");
		
		
		// 													   PASO 4 
		
		addStep("Ejecutar el servicio PO4.Pub:run con el job PO4 de Ctrl-M.");
		
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		u.getDriver().manage().timeouts().pageLoadTimeout(30, TimeUnit.MINUTES);
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution " + dateExecution);
		
		System.out.println(tdcQueryIntegrationServer);	
		SQLResult is = dbLog.executeQuery(tdcQueryIntegrationServer);
		run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");
		System.out.println("RUN_ID = " + run_id + "\t Status: " + status1 );

		boolean valuesStatus = status1.equals(searchedStatus); // Valida si se encuentra en estatus R

		while (valuesStatus) {			
			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);
			u.hardWait(2);			
		}

		boolean successRun = status1.equals(status); // Valida si se encuentra en estatus S		

		if (!successRun) {
			String error = String.format(tdcQueryErrorId, run_id);
			
	
			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();
			
			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(errorr);				
			}
		} 
		
		    
		//													PASO 5     
		    
	    addStep("Verificar el estatus con el cual fue terminada la ejecución de la interface en la tabla WM_LOG_RUN del usuario WMLOG.");
		
	    boolean validateStatus = status.equals(status1);
		System.out.println("VALIDACION DE STATUS = S - " + validateStatus);
		
		if (!validateStatus) {
			testCase.addTextEvidenceCurrentStep("La ejecucion de la interfaz no fue exitosa, STATUS = " + status1);
		}  		
			
		testCase.addQueryEvidenceCurrentStep(is);	
								
		//assertFalse(!validateStatus, "La ejecución de la interfaz no fue exitosa");
			

		//													 PASO 6 
		
		addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD con STATUS = 'S'");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);		
		String consultaTemp6 = String.format(consulta6, run_id);
		SQLResult paso6 = executeQuery(dbLog, consultaTemp6);		
		System.out.println(consultaTemp6);
		String estatusThread = "";
				
		if (!paso6.isEmpty()) {
			estatusThread = paso6.getData(0, "Status");
		}

		boolean SR = estatusThread.equals(status);		
		if (!SR) {
			testCase.addTextEvidenceCurrentStep("No existe registro con parent_id = " + run_id);
		} 
		
		testCase.addQueryEvidenceCurrentStep(paso6);
		
		System.out.println(SR);		
		//assertFalse(SR, "No se obtiene informacion de la consulta");
		
		
		// 													PASO 7 
		
		addStep("Validar que se inserte el registro del documento procesado en la tabla POS_OUTBOUND_DOCS de POSUSER.");		
		
		System.out.println(GlobalVariables.DB_HOST_fcwmesit);
		SQLResult paso7 = executeQuery(dbFcw, consulta7);
		System.out.println(consulta7);
		String doc_name = null;

		boolean av7 = paso7.isEmpty();		
		if (!av7) {
			doc_name = paso7.getData(0, "DOC_NAME");						
		} else {
			testCase.addTextEvidenceCurrentStep("No existe registro en la tabla 'POS_OUTBOUND_DOCS' de POSUSER");
		}
		
		testCase.addQueryEvidenceCurrentStep(paso7);
		
		System.out.println(av7);		
		// assertFalse(av7, "No se obtiene informacion en la tabla POS_OUTBOUND_DOCS de POSUSER.");
		
		FTPUtil ftp = new FTPUtil("10.182.92.13",21,"posuser","posuser");
		String path = "/FEMSA_OXXO/POS/"+ data.get("plaza") +"/"+ data.get("tienda") +"/working/" + doc_name;
				
		if (ftp.fileExists(path)) {
			System.out.println("Existe");
			testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: " + path);
		} else {
			System.out.println("No Existe");
			testCase.addTextEvidenceCurrentStep("No se obtiene el archivo por FTP con la ruta: " + path);
		}
		
		//assertFalse(!ftp.fileExists(path), "No se obtiene el archivo por FTP con la ruta " + path);
		
		/* PASO 8 *********************************************************************/
		
		addStep("Verificar que se actualize el campo WM_ID y EXTRACT_DATE de la tabla FEM_POS_CFG_STG_STORE en RMS.");		
		
		System.out.println(GlobalVariables.DB_HOST_FCRMSMGR);
		String consultaTemp9 = String.format(consulta9, Retek_CR, doc_name);
		SQLResult paso9 = executeQuery(dbFcr, consultaTemp9);  		
		System.out.println(consultaTemp9);

		boolean av9 = paso9.isEmpty();		
		if (av9) {
			testCase.addTextEvidenceCurrentStep("No existe registro en 'WMUSER.FEM_POS_CFG_STG_STORE' con STORE = " + Retek_CR
					+ " y WM_ID = " + doc_name);			
		} 
		
		testCase.addQueryEvidenceCurrentStep(paso9);
		
		System.out.println(av9);		
		// assertFalse(av9, "No se obtiene informacion de la consulta");	
		    
	}
	
	@Override
	public void beforeTest() {
		
	}

	@Override
	public String setTestDescription() {		
		return "Validar la operación para envío de datos de RMS y envío del archivo XML vía FTP.";
	}

	@Override
	public String setTestDesigner() {		
		return "Equipo automatización";
	}

	@Override
	public String setTestFullName() {		
		return "ATC_FT_PO4_002_Procesamiento_De_Datos";
	}

	@Override
	public String setTestInstanceID() {		
		return null;
	}

	@Override
	public String setPrerequisites() {		
		return null;
	}
}
