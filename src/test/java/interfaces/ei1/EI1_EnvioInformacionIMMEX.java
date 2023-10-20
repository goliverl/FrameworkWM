package interfaces.ei1;

import static org.testng.Assert.assertFalse;

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

public class EI1_EnvioInformacionIMMEX extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_EI1_001_Envio_Informacion_IMMEX(HashMap<String, String> data) throws Exception {
		
		
/* Utilerias *********************************************************************/		
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		/**
		 * ALM
		 * Envío de información a IMMEX para plaza ...
		 */
		
/**
* Variables ******************************************************************************************
* 
* 
*/
		String tdcQueryFTP = "SELECT FTP_CONN_ID, FTP_BASE_DIR, FTP_SERVERHOST, FTP_SERVERPORT, FTP_USERNAME, DESCRIPTION  "
				+ " FROM wmuser.WM_FTP_CONNECTIONS"
				+ " WHERE FTP_CONN_ID = 'IMMEX_EDI'";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'EI1-Send'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum = 1";
		
		String tdcQueryErrorId = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"
				+ " and rownum = 1"; 
		
		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = %s" ;
		
		String tdcQueryInbound = "SELECT EE_ID, ID, STATUS, DOC_TYPE, TARGET_ID, RUN_ID_SENT "
				+ " FROM wmuser.EDI_INBOUND_DOCS"
				+ " WHERE STATUS = 'E' "
				+ " AND DOC_TYPE = 'ORD'"
				+ " AND RUN_ID_SENT = %s";
		
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************			
		addStep("Tener configurada la conexi�n por FTP en la tabla WM_FTP_CONNECTIONS.");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcQueryFTP);
		
		SQLResult ftpResult = executeQuery(dbPos, tdcQueryFTP);
		
		boolean ftp = ftpResult.isEmpty();
		
		if (!ftp) {
			
			testCase.addQueryEvidenceCurrentStep(ftpResult);
		}
		
		System.out.println(ftp);
		
		assertFalse(ftp, "No se obtiene informaci�n de la consulta");
		
//Paso 2 *************************				
		
		//Insertar informaci�n de orden en las tablas: 
		//EDI_ENVELOPE, EDI_INBOUND_DOCS, EDI_ORD y EDI_ORD_DETL de WMUSER
		addStep("Se ejecuta el servicio: EI1.pub:runSend.");
		String status = "S";
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id ;
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
		
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		
		
//Paso 3 *************************
		addStep("Verificar que la interfaz se ejecut� correctamente en WMLOG.");
		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		System.out.println(tdcQueryIntegrationServer);
		
		boolean valuesStatus = status1.equals(searchedStatus);//Valida si se encuentra en estatus R
		while (valuesStatus) {
			
			query = executeQuery(dbLog, tdcQueryIntegrationServer);	
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");
		 
		 u.hardWait(2);
		 
		}
		
		boolean successRun = status1.equals(status);//Valida si se encuentra en estatus S
		    if(!successRun){
		   
		   String error = String.format(tdcQueryErrorId, run_id);
		   SQLResult paso2 = executeQuery(dbLog, error);
		   
		   boolean emptyError = paso2.isEmpty();
		   
		   if(!emptyError){  
		   
		    testCase.addTextEvidenceCurrentStep("Se encontr� un error en la ejecuci�n de la interfaz en la tabla WM_LOG_ERROR");
		   
		    testCase.addQueryEvidenceCurrentStep(paso2);
		   
		   }
		}
		    testCase.addQueryEvidenceCurrentStep(query);
		    
//Paso 4	************************
		addStep("Se valida la generacion de thread.");
			
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String statusThreadFormat = String.format(tdcQueryStatusThread, run_id);
		SQLResult threadResult = executeQuery(dbLog, statusThreadFormat);

		System.out.println(statusThreadFormat);
		String statusThread = threadResult.getData(0, "Status");
		String thread_id = threadResult.getData(0, "tread_id");

		boolean ST = statusThread.equals(status);
		ST = !ST;

		if (!ST) {

			testCase.addQueryEvidenceCurrentStep(threadResult);
				    
				} 

		System.out.println(ST);

		assertFalse(ST, "No se obtiene informacion de la consulta");	
		
//Paso 5 ***************************
		addStep("Validar que se actualiza el estado de los documentos a E en la tabla EDI_INBOUND_DOCS de WMUSER.");
		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		
		String inboundFormat = String.format(tdcQueryInbound, thread_id);
		SQLResult inboundResult = dbPos.executeQuery(inboundFormat);
		
		System.out.println(inboundFormat);
		String doc = "";
		
		boolean inbound = inboundResult.isEmpty();
		
		if (!inbound) {
			
			testCase.addQueryEvidenceCurrentStep(inboundResult);
			doc = inboundResult.getData(0, "TARGET_ID");
			
		}
		
		System.out.println(inbound);
		
		assertFalse(inbound, "No se obtiene informaci�n de la consulta");
		
//paso 6 ***************************** falta la ruta correcta
		addStep("Validar que se envi� correctamente el archivo por FTP a IMMEX.");
		StringBuilder DOC = new StringBuilder(doc);
		int i = 1;
		while (i <= 5) {
			DOC = DOC.deleteCharAt(8); // 8,9,10,11,12
			i++;
		}
//		addStep("Comprobar que se genere el archivo y se almacene en la ruta del FileSystem: /u01/posuser/FEMSA_OXXO/POS/"
//				+ data.get("cr_plaza") + "/working/" + DOC);
//
//		FTPUtil ftp = new FTPUtil("10.182.92.13", 21, "posuser", "posuser");
//
//		String ruta = "/export/home/wmuser/testRI1/" + data.get("cr_plaza") + "/working/" + DOC;
//		System.out.println("Ruta: " + ruta);
//
//		if (ftp.fileExists(ruta)) {
//
//			testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: /u01/posuser/FEMSA_OXXO/POS/"
//					+ data.get("cr_plaza") + "/working/" + DOC);
//
//		} else {
//
//			System.out.println("No Existe");
//
//		}
//
//		assertFalse(!ftp.fileExists(ruta), "No Existen archivos en la ruta FTP: " + ruta);
		
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
		return "Construido. Validar que se envie la informaci�n a IMMEX obtenida de WMUSER.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_EI1_001_Envio_Informacion_IMMEX";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
