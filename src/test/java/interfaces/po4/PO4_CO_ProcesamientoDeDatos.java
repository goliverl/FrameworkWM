package interfaces.po4;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

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

public class PO4_CO_ProcesamientoDeDatos extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PO4_002_Procesamiento_De_Datos(HashMap<String, String> data) throws Exception {
	
/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_COL, GlobalVariables.DB_USER_RMS_COL,GlobalVariables.DB_PASSWORD_RMS_COL);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS_COL, GlobalVariables.DB_USER_EBS_COL, GlobalVariables.DB_PASSWORD_EBS_COL);
		
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
		
		String selectRetek = "SELECT RETEK_CR, ORACLE_CR_SUPERIOR, ORACLE_CR, ORACLE_CR_DESC" 
				+ " FROM XXFC_MAESTRO_DE_CRS_V" 
				+ " WHERE ESTADO = 'A'"
				+ " AND ORACLE_CR_TYPE = 'T'" 
				+ " AND ORACLE_CR_SUPERIOR = "+"'"+ data.get("plaza")+"'" 
				+ " AND ORACLE_CR = "+"'"+ data.get("tienda")+"'";
		
		String selectStore = "SELECT C.SOURCE_ID, C.POS_CONFIG_ID, C.RECORD_TYPE, C.TENDER_TYPE_GROUP, C.TENDER_TYPE_DESC, S.SEQ_NO, S.STATUS " + 
				" FROM WMUSER.FEM_POS_CFG_STG C, WMUSER.FEM_POS_CFG_STG_STORE S" + 
				" WHERE C.SOURCE_ID = S.SOURCE_ID" + 
				" AND C.POS_CONFIG_ID = S.POS_CONFIG_ID" + 
				" AND C.RECORD_TYPE = S.RECORD_TYPE" + 
				" AND C.COMPLETE_IND='Y'" + 
				" AND S.WM_ID IS NULL" 
				+ " AND S.STORE = '%s'";//retek_cr
		 
		String selectFile = "SELECT ftp_conn_id,ftp_base_dir,ftp_serverhost,ftp_serverport,ftp_username  " +
				" FROM WMUSER.WM_FTP_CONNECTIONS" + 
				" WHERE FTP_CONN_ID = 'PR50POS'";
		
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%PO4_CO%' "
				+ "ORDER BY START_DT DESC) where rownum <=1";
		
		String consultaError1 = "select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";

		String consultaError2 = "select * from (select description,MESSAGE "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";

		String consultaError3 = "select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1"; //FCWMLQA 
		
		String consultaThreads = "SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS "
				+ "FROM WMLOG.WM_LOG_THREAD   WHERE PARENT_ID='%s'  ORDER BY THREAD_ID DESC"; // FCWMLQA 
 
		String consultaThreads2 = "SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 "
				+ "FROM WMLOG.WM_LOG_THREAD   WHERE PARENT_ID='%s'  ORDER BY THREAD_ID DESC"; // FCWMLQA 
		
		String verifyFile= "SELECT ID, POE_ID, TARGET_TYPE, DOC_NAME, PV_CR_PLAZA, PV_CR_TIENDA, DATE_CREATED " +
				" FROM POSUSER.POS_OUTBOUND_DOCS" + 
				" WHERE DOC_TYPE = 'PMC'" +
				" AND PV_CR_PLAZA = "+"'"+ data.get("plaza")+"'" + 
				" AND PV_CR_TIENDA = "+"'"+ data.get("tienda")+"'" +
				" AND TRUNC(SENT_DATE) = TRUNC(SYSDATE) order by id desc";
		
		String verifyWM_ID= "SELECT SOURCE_ID,POS_CONFIG_ID,RECORD_TYPE,STATUS,WM_ID,EXTRACT_DATE "
				+ " FROM WMUSER.FEM_POS_CFG_STG_STORE" 
				+ " WHERE STORE = '%s'"  //retek_cr
				+ " AND WM_ID = '%s'" //doc_name 
				+ " AND TRUNC(EXTRACT_DATE) = TRUNC(SYSDATE)";				
	
		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
		
		/* PASO 1 *********************************************************************/
		
		addStep("Consultar el RETEK_CR de la tienda a procesar en la tabla XXFC_MAESTRO_DE_CRS_V de EBS_CO, para buscar la información pendiente por procesar.", "false");		
	
		System.out.println(GlobalVariables.DB_HOST_EBS_COL);
		SQLResult paso1 = executeQuery(dbEbs, selectRetek);		
		String Retek_CR = paso1.getData(0, "retek_cr");
		System.out.println(selectRetek);
		
		boolean av = paso1.isEmpty();
		
		if (!av) {
			testCase.addQueryEvidenceCurrentStep(paso1);
		} 

		System.out.println(av);		
		assertFalse(av, "No se obtiene informacion de la consulta en la tabla XXFC_MAESTRO_DE_CRS_V de EBS_CO");
		
		
		/* PASO 2 *********************************************************************/
	
		addStep("Validar que exista información pendiente por procesar para la tienda en la base de RETEK.");		
	
		System.out.println(GlobalVariables.DB_HOST_RMS_COL);
		String consultaTemp2 = String.format(selectStore, Retek_CR);
		SQLResult paso2 = executeQuery(dbRms, consultaTemp2);
		System.out.println(consultaTemp2);

		boolean av2 = paso2.isEmpty();
		
		if (!av2) {
			testCase.addQueryEvidenceCurrentStep(paso2);
		} 

		System.out.println(av2);		
		assertFalse(av2, "No se obtiene informacion de la consulta");
		 
		 
		/* PASO 3 *********************************************************************/
		 
		addStep("Validar los datos de conexión FTP para el envío del archivo en la tabla WM_FTP_CONNECTIONS de WMINT.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		SQLResult paso3 = executeQuery(dbPos, selectFile);
		System.out.println(selectFile);		

		boolean av3 = paso3.isEmpty();
		
		if (!av3) {
			testCase.addQueryEvidenceCurrentStep(paso3);
		} 

		System.out.println(av3);		
		assertFalse(av3, "No se obtiene informacion de la consulta");
		
		
		/* PASO 4 *********************************************************************/
		
		addStep("Ejecutar el servicio PO4_CO.Pub:run.");
		
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution " + dateExecution);
		System.out.println(tdcIntegrationServerFormat);
		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
		run_id = is.getData(0, "RUN_ID");
		String status1 = is.getData(0, "STATUS");
		System.out.println("RUN_ID = " + run_id + "\t Status: " + status1 );

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
	
			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();
			
			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(errorr);
				
				SQLResult errorIS = dbLog.executeQuery(error1);
				testCase.addQueryEvidenceCurrentStep(errorIS);
				
				SQLResult errorIS2 = dbLog.executeQuery(error2);
				testCase.addQueryEvidenceCurrentStep(errorIS2);
			}
		}	

		    
		/* PASO 5 *********************************************************************/		    
		    
	    addStep("Verificar el estatus con el cual fue terminada la ejecución de la interface en la tabla WM_LOG_RUN del usuario WMLOG.");		
		
		boolean validateStatus = status.equals(status1);
		System.out.println("VALIDACION DE STATUS = S - " + validateStatus);
	
		testCase.addQueryEvidenceCurrentStep(is);					
		assertFalse(!validateStatus, "La ejecución de la interfaz no fue exitosa");
		

		/* PASO 6 *********************************************************************/
		
		addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD con STATUS = 'S'");		
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);		
		String consultaTemp6 = String.format(consultaThreads, run_id);	
		System.out.println(consultaTemp6);		
		SQLResult paso6 = executeQuery(dbLog, consultaTemp6);	
		
		boolean validacion6 = paso6.isEmpty();

		if (!validacion6) {
			String estatusThread = paso6.getData(0, "Status");			
			validacion6 = estatusThread.equals(status);
			System.out.println(validacion6);
			testCase.addQueryEvidenceCurrentStep(paso6);
			
			String consultaTemp6pt2 = String.format(consultaThreads2, run_id);	
			paso6 = executeQuery(dbLog, consultaTemp6pt2);	
			testCase.addQueryEvidenceCurrentStep(paso6);

			validacion6 = !validacion6;		//Como se reutiliza el mismo booleano que el isEmpty lo regreso al mismo estado que estaba antes del if
		}					
		
		assertFalse(validacion6, "No se obtiene informacion o Thread con STATUS diferente a 'S' de la consulta en la tabla WM_LOG_THREAD");
		
		
		/* PASO 7 *********************************************************************/
		
		addStep("Validar que se inserte el registro del documento procesado en la tabla POS_OUTBOUND_DOCS de POSUSER.");
				
		System.out.println(GlobalVariables.DB_HOST_Puser);
		SQLResult paso7 = executeQuery(dbPos, verifyFile);
		String doc_name = paso7.getData(0, "DOC_NAME");
		System.out.println(verifyFile);

		boolean av7 = paso7.isEmpty();
		
		if (!av7) {
			testCase.addQueryEvidenceCurrentStep(paso7);			
		} 

		System.out.println(av7);		
		assertFalse(av7, "No se obtiene informacion de la consulta en la tabla POS_OUTBOUND_DOCS de POSUSER.");
				
		FTPUtil ftp = new FTPUtil("10.182.92.13",21,"posuser","posuser");
		String path = "/u01/posuser/FEMSA_OXXO/POS/"+ data.get("plaza") +"/"+ data.get("tienda") +"/working/" + doc_name;
				
		if (ftp.fileExists(path)) {
			System.out.println("Existe");
			testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: "+path);
		} else {
			System.out.println("No Existe");
		}
		
		assertFalse(!ftp.fileExists(path), "No se obtiene el archivo por FTP con la ruta " + path);

		
		/* PASO 8 *********************************************************************/
		
		addStep("Verificar que se actualize el campo WM_ID y EXTRACT_DATE de la tabla FEM_POS_CFG_STG_STORE en RMS COL.");		
		
		System.out.println(GlobalVariables.DB_HOST_RMS_COL);
		String consultaTemp9 = String.format(verifyWM_ID,Retek_CR, doc_name);
		SQLResult paso9 = executeQuery(dbRms, consultaTemp9);  		
		System.out.println(consultaTemp9);

		boolean av9 = paso9.isEmpty();
		
		if (!av9) {
			testCase.addQueryEvidenceCurrentStep(paso9);			
		} 

		System.out.println(av9);		
		assertFalse(av9, "No se obtiene informacion de la consulta en la tabla FEM_POS_CFG_STG_STORE en RMS COL.");	
		    
	}
	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Validar la operación para envío de datos de RMS y envío del archivo XML vía FTP.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatización";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PO4_002_Procesamiento_De_Datos";
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
