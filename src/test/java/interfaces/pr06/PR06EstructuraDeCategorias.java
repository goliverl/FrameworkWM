package interfaces.pr06;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLResultExcel;
import utils.sql.SQLUtil;

public class PR06EstructuraDeCategorias extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PR06_PR06EstructuraDeCategorias(HashMap<String, String> data) throws Exception {
		
/* Utilerías*********************************************************************/
		/*
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		 * */

		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_FCRMSMGR, GlobalVariables.DB_USER_FCRMSMGR, GlobalVariables.DB_PASSWORD_FCRMSMGR);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);


		testCase.setProject_Name("Remediaciones SYGNIA (AP1)");    
		
		
		
		
/** Variables *********************************************************************/
		
		String tdcQueryMerchY = "SELECT MERCH_HIER_UPDAT_IND "
				+ "FROM WMUSER.WM_SYSTEM_VARIABLES "
				+ "WHERE MERCH_HIER_UPDAT_IND='Y'";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = '" + data.get("wm_log") +"'"
				+" and  start_dt >= TRUNC(SYSDATE)"
			    +" order by start_dt desc)"
				+ " where rownum = 1";
		
		String tdcQueryErrorId =" SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION "
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; //FCWMLQA
		
		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread "
				+ " WHERE parent_id = %s" ; //FCWMLQA 

		String tdcQueryPosuser = "SELECT ID, DOC_NAME, DOC_TYPE, SENT_DATE, PV_CR_PLAZA, PV_CR_TIENDA, STATUS "
				+ "FROM POSUSER.POS_OUTBOUND_DOCS "
				+ "WHERE PV_CR_PLAZA='" + data.get("plaza") +"' "
				+ "AND DOC_TYPE='ECA' "
				+ "AND STATUS='L' "
				+ "AND TRUNC(SENT_DATE)=TRUNC(SYSDATE)";
		
		String tdcQueryMerchN = "SELECT MERCH_HIER_UPDAT_IND "
				+ "FROM WMUSER.WM_SYSTEM_VARIABLES "
				+ "WHERE MERCH_HIER_UPDAT_IND='N'";
		
		String tdcQueryMerchLog = "SELECT ID_CHANGE, MERCH_HIER_UPDAT_IND, NIVEL, OLD_VALUE, NEW_VALUE "
				+ "FROM WM_MERCH_HIER_LOG "
				+ "WHERE MERCH_HIER_UPDAT_IND='N'";
		
		String tdcQueryftp = "SELECT ID, POE_ID,TARGET_TYPE,DOC_NAME,PV_CR_PLAZA,PV_CR_TIENDA,SENT_DATE " 
				+ " FROM POSUSER.POS_OUTBOUND_DOCS "
				+ "WHERE PV_CR_PLAZA = '" + data.get("plaza") + "'"
				+ " AND TRUNC(SENT_DATE) = TRUNC(SYSDATE)";
		


		String status = "S";
		//utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id ;
				
		
		
		
/** **************************Pasos del caso de Prueba*******************************************/

	
//		Paso 1 ************

		addStep("Validar el valor de MERCH_HIER_UPDAT_IND en la tabla WM_SYSTEM_VARIABLES de RMS.");

		
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(tdcQueryMerchY);
		
		SQLResult result1 = executeQuery(dbRms, tdcQueryMerchY);
		boolean valor = result1.isEmpty();
		
		if (!valor) {	
				
			testCase.addQueryEvidenceCurrentStep(result1);
			
					} 

		System.out.println(valor);

		assertFalse(valor, "La tabla no contiene registros");
		
		
//		Paso 2	************************	
		
		addStep("Ejecutar el servicio PR6.Pub:run.");
		
		String contra =   "http://"+user+":"+ps+"@"+server+":5555";
		u.get(contra);
	
		
		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));

		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		String status1 = query.getData(0, "STATUS");
		run_id = query.getData(0, "RUN_ID");
		
// paso 3 ****************************
		addStep("Verificar el estatus con el cual fue terminada la ejecución de la interfaz en la tabla WM_LOG_RUN del usuario WMLOG.");		
				 
		boolean valuesStatus = status1.equals(searchedStatus);//Valida si se encuentra en estatus R
		while (valuesStatus) {
			
			query = executeQuery(dbLog, tdcQueryIntegrationServer);	
			status1 = query.getData(0, "STATUS");
			run_id = query.getData(0, "RUN_ID");
		 
		 u.hardWait(2);
		 
		}
		
		boolean successRun = status1.equals(status);//Valida si se encuentra en estatus S

        if(successRun) {
       		SQLResult log = dbLog.executeQuery(tdcQueryIntegrationServer);
       		System.out.println("Respuesta " + log);

       		boolean log1 = log.isEmpty();
      
       		if (!log1) {

       			testCase.addQueryEvidenceCurrentStep(log);
       		}
        }
        else
		    if(!successRun){
		   
		   String error = String.format(tdcQueryErrorId, run_id);
		   SQLResult paso2 = executeQuery(dbLog, error);
		   
		   boolean emptyError = paso2.isEmpty();
		   
		   if(!emptyError){  
		   
		    testCase.addTextEvidenceCurrentStep("Se encontro un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");
		   
		    testCase.addQueryEvidenceCurrentStep(paso2);
		   
		   }
		}
		    

		    
		    
//	 	Paso 3	************************

        addStep("Se valida la generacion de thread.");
	
		String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
		System.out.println(queryStatusThread);
		SQLResult paso3 = executeQuery(dbLog, queryStatusThread);
		boolean thread = paso3.isEmpty();
		if(!thread) {
			
			testCase.addQueryEvidenceCurrentStep(paso3);
		}
				
		String regPlazaTienda =  paso3.getData(0, "STATUS");
			boolean statusThread = status.equals(regPlazaTienda);
			System.out.println(statusThread);
//					if(!statusThread){
//   
//		String error = String.format(tdcQueryErrorId, run_id);
//		SQLResult result = executeQuery(dbLog, error);
//		testCase.addQueryEvidenceCurrentStep(result);
//							
//		boolean emptyError = result.isEmpty();
//   
//		if(!emptyError){  
//   
//		testCase.addTextEvidenceCurrentStep("Se encontro un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");
//				
//		testCase.addQueryEvidenceCurrentStep(result);
//   
//						}
//			}
					
		//assertTrue(statusThread,"El registro de ejecución no fue exitoso");



//		Paso 4	************************			
		
		addStep("Verificar la informacion procesada en la tabla POS_OUTBOUND_DOCS de POSUSER.");
		
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryPosuser);
		
		SQLResultExcel result = executeQueryExcel(dbPos, tdcQueryPosuser);
		String DOC_NAME = result.getData(0, "DOC_NAME");
		boolean outbound = result.isEmpty();
		
		if (!outbound) {	
			
			testCase.addDocumentEvidence(result.getRelativePath(), "Ejecucion paso 4" );		
		
					} 
		testCase.addTextEvidenceCurrentStep(tdcQueryPosuser);
		System.out.println(outbound);

		assertFalse(outbound, "La tabla no contiene registros");
		
		Thread.sleep(20000);

		
// paso 4 ********************************
		
		StringBuilder DOC = new StringBuilder(DOC_NAME);
		int i = 1;
		while (i <= 5) {
			DOC = DOC.deleteCharAt(8); // 8,9,10,11,12
			i++;
		}
		
		addStep("Verificar envío por FTP del archivo XML ECA, al buzón de la Plaza del POS.");
		FTPUtil ftp = new FTPUtil("10.182.92.13",21,"posuser","posuser");
		String path = "/FEMSA_OXXO/POS/"+ data.get("plaza") + "/working/" + DOC;
				
		if (ftp.fileExists(path)) {
			System.out.println("Existe");
			testCase.addTextEvidenceCurrentStep(path);
		} else {
			System.out.println("No Existe");
		}
				
		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQueryftp);

		SQLResultExcel resultftp = executeQueryExcel(dbPos, tdcQueryftp);
		boolean queryftp = resultftp.isEmpty();

		if (!queryftp) {

			testCase.addDocumentEvidence(resultftp.getRelativePath(), "Envio por FTP del archivo XML ECA");
		
		}

		System.out.println(queryftp);
		testCase.addTextEvidenceCurrentStep(tdcQueryftp);

//		assertFalse(queryftp, "La tabla no contiene registros");
		
		
		
//	Paso 5	************************
		
		addStep("Validar actualizacion del valor MERCH_HIER_UPDAT_IND en la tabla WM_SYSTEM_VARIABLES.");
		
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(tdcQueryMerchN);
		
		SQLResult result5 = executeQuery(dbRms, tdcQueryMerchN);
		boolean variables = result5.isEmpty();
				
		if (variables) {	

			testCase.addQueryEvidenceCurrentStep(result5);
					} 

		System.out.println(variables);

//		assertFalse(variables, "La tabla no fue actualizada correctamente");


//		Paso 6	************************

		addStep("Validar actualizacion WM_MERCH_HIER_LOG.");
	
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		System.out.println(tdcQueryMerchLog);
		
		SQLResult result6 = executeQuery(dbRms, tdcQueryMerchLog);
		boolean merchlog = result6.isEmpty();
		System.out.println(merchlog);		
		
		if (merchlog) {	
			
			testCase.addQueryEvidenceCurrentStep(result6);					} 

		System.out.println(merchlog);

		assertFalse(!merchlog, "La tabla no fue actualizada correctamente");
		
		
		
		
	}


	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar la operación para envío de datos en un archivo XML de RMS al buzón de la plaza.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatización";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PR06_PR06EstructuraDeCategorias";
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
