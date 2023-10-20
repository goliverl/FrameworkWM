package interfaces.cp01;

import static org.testng.Assert.assertFalse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLUtil;
import utils.sql.SQLResult;

public class CP01_validaCambios extends BaseExecution{ 
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_CP01_validaCambios(HashMap<String, String> data) throws Exception {
		
		
		
/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbCNT = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);		
	
		SeleniumUtil u;
		PakageManagment pok;		
		
		/**
		 * ALM
		 * Validar que se envien los cambios al Buzon
		 */
		
/** VARIABLES *********************************************************************/	
		
		String status = "S"; // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id, doc_name="";
		
		String ValidaPOS_CAMBIOSL = "SELECT PLAZA_CR, TIENDA_CR, WM_STATUS "
				+ "FROM WMUSER.POS_CAMBIOS  WHERE WM_STATUS = 'L' AND PLAZA_CR = '" + data.get("plaza") +"' AND TIENDA_CR = '" + data.get("tienda") +"'";
		
		String validaEmail = "SELECT EMAIL, A.GROUP_ID, GROUP_NAME, INTERFACE_NAME "
				+ "FROM WMLOG.WM_LOG_GROUP A, WMLOG.WM_LOG_USER_GROUP B, WMLOG.WM_LOG_USER C "
				+ "WHERE A.GROUP_ID = B.GROUP_ID "
				+ "AND B.USER_ID = C.USER_ID "
				+ "AND A.GROUP_NAME = 'CP01' "
				+ "AND A.INTERFACE_NAME = 'CP01' "
				+ "AND C.PLAZA IS NULL";
		
		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1"; // dbLog
		
		String consultaError2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1"; // dbLog
		
		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1"; // dbLog

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ " FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE = 'CP01' AND STATUS='S' "
				+ " ORDER BY START_DT DESC) where rownum <=1";

		String ValidaPOS_CAMBIOSE = "SELECT WM_RUN_ID, PLAZA_CR, TIENDA_CR, WM_STATUS, WM_SEND_DT "
				+ " FROM WMUSER.POS_CAMBIOS WHERE WM_STATUS = 'E' "
				+ " AND TRUNC(WM_SEND_DT) = TRUNC(SYSDATE) AND PLAZA_CR = '" + data.get("plaza") + "' " 
				+ " AND TIENDA_CR = '" + data.get("tienda") + "' AND WM_RUN_ID = '%s'";

		String verifyFile = "SELECT ID, doc_type, status, DOC_NAME,PV_CR_PLAZA,PV_CR_TIENDA,DATE_CREATED "
				+ " FROM POSUSER.POS_OUTBOUND_DOCS WHERE PV_CR_PLAZA = '" + data.get("plaza") + "'" 
				+ " AND PV_CR_TIENDA = '" + data.get("tienda") + "' AND TRUNC(SENT_DATE) = TRUNC(SYSDATE) and Doc_type ='DGT' and Status = 'L' order by sent_date desc";

		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
		
		/* PASO 1 *********************************************************************/
		
		addStep("Que exista registro de la Plaza " + data.get("plaza") + " Y TIENDA  " + data.get("tienda")	+ " con STATUS L en la tabla POS_CAMBIOS de la BD CNT. ");

		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		System.out.println(ValidaPOS_CAMBIOSL);

		SQLResult ValidaPOS_CAMBIOSL_Res = executeQuery(dbCNT, ValidaPOS_CAMBIOSL);

		boolean validaStatusL = ValidaPOS_CAMBIOSL_Res.isEmpty();

		if (!validaStatusL) {
			testCase.addQueryEvidenceCurrentStep(ValidaPOS_CAMBIOSL_Res);
		}

		System.out.println(validaStatusL);
		assertFalse(validaStatusL, "No se encontro registro con las tiendas correspondientes");

		/* PASO 2 *********************************************************************/

		addStep("Comprobar la direccion de email para recibir las natificaciones en las tablas WM_LOG_GROUP, WM_LOG_USER_GROUP y WM_LOG_USER para el GROUP_NAME igual a CP01 y INTERFACE_NAME igual a CP01 en la BD WMLOG.");

		System.out.println(validaEmail);
		SQLResult validaEmail_Res = executeQuery(dbLog, validaEmail);

		boolean validaEmailRes = validaEmail_Res.isEmpty();

		if (!validaEmailRes) {
			testCase.addQueryEvidenceCurrentStep(validaEmail_Res);
		}

		System.out.println(validaEmailRes);
		assertFalse(validaEmailRes, "No se conoce la direcci�n de e-mail al cual llegar� el correo de notificaci�n");

		/* PASO 3 *********************************************************************/

		addStep("Se ejecuta el proceso CP01.Pub:run ");
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interface"), data.get("servicio"));
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
				testCase.addTextEvidenceCurrentStep("Se encontr� un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");
				testCase.addQueryEvidenceCurrentStep(errorr);
				
				SQLResult errorIS = dbLog.executeQuery(error1);
				testCase.addQueryEvidenceCurrentStep(errorIS);
				
				SQLResult errorIS2 = dbLog.executeQuery(error2);
				testCase.addQueryEvidenceCurrentStep(errorIS2);
			}
		}	
		

		
		/* PASO 4 *********************************************************************/		    
	    
	    addStep("Verificar el estatus con el cual fue terminada la ejecuci�n de la interface en la tabla WM_LOG_RUN del usuario WMLOG.");		
		
		boolean validateStatus = status.equals(status1);
		System.out.println("VALIDACION DE STATUS = S - " + validateStatus);
	
		testCase.addQueryEvidenceCurrentStep(is);					
		assertFalse(!validateStatus, "La ejecuci�n de la interfaz no fue exitosa");


		/* PASO 5 *********************************************************************/

		addStep("Se valida que WM_STATUS sea igual a E en la tabla POS_CAMBIOS de la BD CNT donde PLAZA_CR 10MON Y TIENDA_CR 50EDI");

		String ValidaPOS_CAMBIOSEFormat = String.format(ValidaPOS_CAMBIOSE, run_id);
		System.out.println(ValidaPOS_CAMBIOSEFormat);
		SQLResult ValidaPOS_CAMBIOSERes = executeQuery(dbCNT, ValidaPOS_CAMBIOSEFormat);

		boolean validaCambiosE = ValidaPOS_CAMBIOSERes.isEmpty();

		if (!validaCambiosE) {
			testCase.addQueryEvidenceCurrentStep(ValidaPOS_CAMBIOSERes);
		}

		System.out.println(validaCambiosE);
		assertFalse(validaCambiosE, "Los campos de wm_status, wm_send_dt y wm_run_id no  fueron actualizados.");

		
		/* PASO 6 *********************************************************************/
		
		addStep("Validar que se inserte el registro del documento procesado en la tabla POS_OUTBOUND_DOCS de POSUSER.");
				
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(verifyFile);
		SQLResult paso7 = executeQuery(dbPos, verifyFile);

		boolean step7 = paso7.isEmpty();
		
		if (!step7) {
			doc_name = paso7.getData(0, "DOC_NAME");
			System.out.println(doc_name);
			testCase.addQueryEvidenceCurrentStep(paso7);			
		} 

		System.out.println(step7);		
		assertFalse(step7, "No se obtiene informacion de la consulta en la tabla POS_OUTBOUND_DOCS de POSUSER.");
		
		FTPUtil ftp = new FTPUtil("10.182.92.13",21,"posuser","posuser");
		String path = "/FEMSA_OXXO/POS/"+ data.get("plaza") +"/"+ data.get("tienda") +"/working/" + doc_name;
				
		if (ftp.fileExists(path)) {
			System.out.println("Existe");
			testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: "+path);
		} else {
			System.out.println("No Existe");
		}
		
		assertFalse(!ftp.fileExists(path), "No se obtiene el archivo por FTP.");

		
		
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

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Validar que los cambios se envien al buzon de la tienda";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatizaci�n";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_CP01_validaCambios";
		
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}