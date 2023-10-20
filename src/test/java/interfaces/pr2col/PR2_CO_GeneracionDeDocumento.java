package interfaces.pr2col;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;



public class PR2_CO_GeneracionDeDocumento extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PR2_COL_Confirmacion_De_Transferencias(HashMap<String, String> data) throws Exception {
		
/*
 * UtilerÃ­as *********************************************************************/

		SQLUtil dbRms = new SQLUtil(GlobalVariables.DB_HOST_RMS_COL, GlobalVariables.DB_USER_RMS_COL, GlobalVariables.DB_PASSWORD_RMS_COL);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		

/**
* Variables ******************************************************************************************
* 
* 
*/	
		String tdcQuerySend = "SELECT TSF_NO, FROM_LOC_TYPE, FROM_LOC, TO_LOC_TYPE, STATUS, SEND_POS"
				+ " FROM XXFC_ORDCECOL_SEND_POS_TSF"
				+ " WHERE SEND_POS =  'N'"
				+ " AND FROM_LOC_TYPE = 'S' "
				+ " AND TO_LOC_TYPE = 'S' "
				+ " AND STATUS = 'A'";
		
		String tdcQueryStore = "SELECT SUBSTR(STORE_NAME10,1,5) AS CR_PLAZA, substr(store_name10,6,10) AS cr_tienda"
				+ " FROM STORE WHERE STORE = %s";
		
		String tdcQueryTdetail = "SELECT ITEM, TSF_QTY "
				+ "FROM XXFC_ORDCECOL_SEND_POS_TDETAIL"
				+ " WHERE TSF_NO = %s";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = '" + data.get("wm_log") +"'"
				+ " and  start_dt >= TRUNC(SYSDATE)"
			    + " order by start_dt desc)"
				+ " where rownum = 1";
		
		String verifyFile= "select * from (SELECT ID, POE_ID,TARGET_TYPE,DOC_NAME,PV_CR_PLAZA,PV_CR_TIENDA,DATE_CREATED " 
				+ " FROM POSUSER.POS_OUTBOUND_DOCS" 
				+ " WHERE DOC_TYPE = 'HSC'"
				+ " AND PV_CR_PLAZA = '%s'"
				+ " AND PV_CR_TIENDA = '%s'"
				+ " AND TRUNC(SENT_DATE) = TRUNC(SYSDATE)  order by sent_date desc) where rownum = 1";
		
		testCase.setProject_Name("Remediaciones SYGNIA (AP1)");		
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************			
		addStep("Comprobar que existan registros en la tabla XXFC_ORDCECOL_SEND_POS_TSF en la BD RETEK  con el flag send_pos = 'N' y status = 'A' .");
		System.out.println(GlobalVariables.DB_HOST_RMS_COL);
		
		SQLResult sendResult = executeQuery(dbRms, tdcQuerySend);
		System.out.println(tdcQuerySend);

		String from_loc = sendResult.getData(0, "FROM_LOC");
		String tsf_no = sendResult.getData(0, "TSF_NO");
		
		System.out.println(tdcQuerySend);
		
		boolean send = sendResult.isEmpty();
		
		if (!send) {
			
			testCase.addQueryEvidenceCurrentStep(sendResult);
		}
		
		System.out.println(send); 
		
//paso 2 *************************
		addStep("Comprobar que existan registros en la tabla STORE en la BD RETEK para la tienda y plaza origen y destino.");
		System.out.println(GlobalVariables.DB_HOST_RMS_COL);
		
		String storeFormat = String.format(tdcQueryStore, from_loc);
		SQLResult storeResult = executeQuery(dbRms, storeFormat);
		System.out.println(storeFormat);
		String Tienda = storeResult.getData(0, "CR_TIENDA");
		String Plaza = storeResult.getData(0, "CR_PLAZA");
//		/u01/posuser/FEMSA_OXXO/POS/[CR_PLAZA]/[CR_TIENDA]/working/
		System.out.println("tienda "+Tienda);
		System.out.println("plaza "+Plaza);
		System.out.println("/u01/posuser/FEMSA_OXXO/POS/"+Plaza+"/"+Tienda+"/working/");
		
		boolean store = storeResult.isEmpty();
		
		if (!store) {
			
			testCase.addQueryEvidenceCurrentStep(storeResult);
		}
		
		System.out.println(store);
		
		assertFalse(store, "No se obtiene información de la consulta"); 
		
//paso 3 ************************
		addStep("Comprobar que existan registros en la tabla XXFC_ORDCECOL_SEND_POS_TDETAIL en la BD RETEK.");
		System.out.println(GlobalVariables.DB_HOST_RMS_COL);
		
		String tdetailFormat = String.format(tdcQueryTdetail, tsf_no);
		SQLResult tdetailResult = executeQuery(dbRms, tdetailFormat);
		System.out.println(tdetailFormat);
		
		boolean tdetail = tdetailResult.isEmpty();
		
		if (!tdetail) {
			
			testCase.addQueryEvidenceCurrentStep(tdetailResult);
		}
		
		System.out.println(tdetail);
		
		assertFalse(tdetail, "No se obtiene información de la consulta"); 
	
//paso 4 ***********************
		addStep("Ejecutar el servicio: PR2_CO.Pub:runHSC, mediante el job: runPR2_CO_HSC");
		String status = "S";
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		u.getDriver().manage().timeouts().pageLoadTimeout(10, TimeUnit.MINUTES);
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword( data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
//		String run_id ;
		String contra =   "http://"+user+":"+ps+"@"+server+":7797";
		u.get(contra);
		
		pok.runIntefaceWmOneButton10(data.get("interface"), data.get("servicio"));
//Paso 5	************************ 	
		addStep("Comprobar que se registre la transaccion en la tabla WM_LOG_RUN de la BD del WMLOG para la interface PR2_CO_HSC.");
		
		SQLResult query = executeQuery(dbLog, tdcQueryIntegrationServer);	
		String status1 = query.getData(0, "STATUS");
//		run_id = query.getData(0, "RUN_ID");
		System.out.println(tdcQueryIntegrationServer);
		
		boolean valuesStatus = status1.equals(searchedStatus);//Valida si se encuentra en estatus R
		while (valuesStatus) {
			
			query = executeQuery(dbLog, tdcQueryIntegrationServer);	
			status1 = query.getData(0, "STATUS");
//			run_id = query.getData(0, "RUN_ID");
		 
		 u.hardWait(2);
		 
		}
		
		boolean successRun = status1.equals(status);//Valida si se encuentra en estatus S
	    if(!successRun){
	   
	 
	    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
	  
	    }
	    testCase.addQueryEvidenceCurrentStep(query);		
		
//paso 6 ***************************
		    addStep("Validar que se inserte el registro del documento procesado en la tabla POS_OUTBOUND_DOCS de POSUSER."); 
	    System.out.println(GlobalVariables.DB_HOST_Puser); 
	    
	    String Pas7Format = String.format(verifyFile, Plaza,Tienda);
		SQLResult paso7 = executeQuery(dbPos, Pas7Format);  
		System.out.println(Pas7Format); 
	    boolean av7 = paso7.isEmpty(); 
	    String doc_name="";
	    if (!av7) { 

		     doc_name = paso7.getData(0, "DOC_NAME"); 
	    	testCase.addQueryEvidenceCurrentStep(paso7); 
	    	
	    } 
	    System.out.println(av7); 
	    assertFalse(av7, "No se obtiene informacion de la consulta en la tabla POS_OUTBOUND_DOCS de POSUSER.");
		
	    addStep("Comprobar que se genere el archivo y se almacene en la ruta del FileSystem");
    
		
		  FTPUtil ftp = new FTPUtil("10.182.92.13",21,"posuser","posuser");
			String path = "/FEMSA_OXXO/POS/"+ Plaza +"/"+ Tienda +"/working/" + doc_name;
					
			if (ftp.fileExists(path)) {
				System.out.println("Existe");
				testCase.addTextEvidenceCurrentStep("Se encontro archivo en la ruta: "+path);
			} else {
				System.out.println("No Existe");
			}
			
			assertFalse(!ftp.fileExists(path), "No se obtiene el archivo por FTP con la ruta " + path);
		
		
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
		return "Construido. Confirmacion de transferencias.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo Automatización";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PR2_COL_Confirmacion_De_Transferencias";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
