package interfaces.ro8mex;

import modelo.BaseExecution;
import util.GlobalVariables;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ro8_Mex_Ventas extends BaseExecution {
public String Thread_id;

	@Test(dataProvider = "data-provider")
	public void ATC_FT_RO8_MEX_006_Ventas(HashMap<String, String> data) throws Exception {
	
	/* Utilerías *********************************************************************/
//	SqlUtil db = new SqlUtil(GlobalVariables.DB_USER_EBS_COL, GlobalVariables.DB_PASSWORD_EBS_COL, GlobalVariables.DB_HOST_EBS_COL);
//	SqlUtil dbPuser = new SqlUtil(GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser, GlobalVariables.DB_HOST_Puser);
//	SqlUtil dbRms = new SqlUtil(GlobalVariables.DB_USER_RMS_COL, GlobalVariables.DB_PASSWORD_RMS_COL, GlobalVariables.DB_HOST_RMS_COL);
//	SqlUtil dbLog = new SqlUtil(GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA, GlobalVariables.DB_HOST_FCWMLQA);
	utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser,GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
	utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
	utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
	utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Ebs,GlobalVariables.DB_USER_Ebs, GlobalVariables.DB_PASSWORD_Ebs);


	
	
	/**
	 * Variables ******************************************************************************************
	 * 
	 * 
	*/
	
	
	

	String tdcQueryStatusVerifyWmCfgLauncher = "SELECT INTERFACE_NAME,AUTO_STATUS,MANUAL_STATUS,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4 FROM WMUSER.WM_CFG_LAUNCHER" 
	        + " WHERE INTERFACE_NAME = 'RO8_MEX'"
	        + " AND ATTRIBUTE1= '" + data.get("plaza") + "'" 
			+ " AND ATTRIBUTE2 = '" + data.get("Tran_cod") + "'"
			+ " AND AUTO_STATUS = 'A'";
	
	String tdcQueryFemFifStg = "SELECT ITEM,TRAN_CODE,CR_PLAZA,REFERENCE_3,REFERENCE_9,TRAN_DATE"
			+ " FROM fem_fif_stg"
			+ " WHERE  tran_date >= TRUNC (SYSDATE) - 60"
			+ " AND CR_PLAZA = '" + data.get("plaza")+"'"
			+ " AND TRAN_CODE = '"+ data.get("Tran_cod")+"'"
			+ " AND REFERENCE_3 IS NULL"
			+ " AND REFERENCE_9 IS NULL";
			
	String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status"
			+ " FROM WMLOG.wm_log_run"
			 + " WHERE interface = '" + data.get("INTERFACE")+"'" 	
			 +" and  start_dt >= TRUNC (SYSDATE)"
		     +" order by start_dt desc)"
			+ " where rownum = 1";	
	
	String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,status,att1,att2"
			+ " FROM WMLOG.wm_log_thread"
			+ " WHERE parent_id = %s";
	     	
	String tdcQueryError ="SELECT error_id,run_id,error_date,description,message "
			+ " FROM WMLOG.wm_log_error"
			+ " WHERE run_id =  %s";

	String tdcQueryStatusLog = "SELECT run_id,interface,start_dt,status,server"
			+ " FROM WMLOG.wm_log_run"
			+ " WHERE interface = '" + data.get("INTERFACE")+"'"
			+ " AND RUN_ID = %s";
	
	String tdcQueryWM_GL_HEADERS_MEX= "SELECT HEADER_ID,TRAN_CODE,TRAN_DATE,CR_PLAZA,RUN_ID"
			+ " FROM WMUSER.wm_gl_headers_mex"
			+ " WHERE TRAN_CODE = '"+ data.get("Tran_cod")+"'"
			+ " AND RUN_ID = %s"
			+ " AND CR_PLAZA = '" + data.get("plaza")+"'";
	
	String tdcQueryUPDATEfem_fif_stg= "SELECT ITEM,TRAN_CODE,CR_PLAZA,REFERENCE_3,REFERENCE_9,TRAN_DATE"
			+ " FROM fem_fif_stg"
			+ " WHERE  tran_date >= TRUNC (SYSDATE) - 60"
			+ " AND TRAN_CODE = '"+ data.get("Tran_cod")+"'"
			+ " AND REFERENCE_9 = %s"
			+ " AND REFERENCE_3 IS NOT NULL"
			+ " AND CR_PLAZA = '" + data.get("plaza")+"'";
	
	String tdcQueryORAFIN= "SELECT GROUP_ID"
			+ " FROM GL.GL_INTERFACE "
			+ " WHERE GROUP_ID = %s";
	
	String tdcQueryRETEK= "SELECT TRAN_CODE,CR_PLAZA,HEADER_ID"
			+ " FROM WMUSER.wm_gl_headers_mex_hist"
			+ " WHERE TRAN_CODE = '"+ data.get("Tran_cod")+"'"
			+ " AND CR_PLAZA = '" + data.get("plaza")+"'"
			+ " AND HEADER_ID = %s";
	
	
	
	
	SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
	PakageManagment pok = new PakageManagment(u, testCase);
	String user = data.get("user");
	String ps = PasswordUtil.decryptPassword( data.get("ps"));
	String server = data.get("server");
	String searchedStatus = "R";
	String status = "S";

	
	
	
	/*Pasos *********************************************************************************/
	
	///										Paso 1 ************************************************
	addStep("Verificar parametros configurados en la tabla WM_CFG_LAUNCHER de la BD WMINT");
	System.out.println(GlobalVariables.DB_HOST_Puser);
	
     System.out.println("\n"+ tdcQueryStatusVerifyWmCfgLauncher); 
     
     SQLResult StatusWmC = executeQuery(dbPuser, tdcQueryStatusVerifyWmCfgLauncher);
     
//   boolean Puser = SQLUtil.isEmptyQuery(testCase, dbPuser, tdcQueryStatusVerifyWmCfgLauncher);
     
     boolean Puser = StatusWmC.isEmpty();
     
     testCase.addQueryEvidenceCurrentStep(StatusWmC);
	
 	assertFalse(Puser,"No existen parametros configurados en la tabla"); 
 	
 	/// 								Paso 2****************************************************
	addStep("Validar que exista información pendiente de procesar en la tabla FEM_FIF_STG de RETEK.");
	System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
	
    System.out.println("\n"+ tdcQueryFemFifStg);      
    
    SQLResult QFemFifStg = executeQuery(dbRms, tdcQueryFemFifStg);
    
//  boolean FemFifStg = SQLUtil.isEmptyQuery(testCase, dbRms, tdcQueryFemFifStg);
    
    boolean FemFifStg = QFemFifStg.isEmpty();
    
    testCase.addQueryEvidenceCurrentStep(QFemFifStg);
	
	assertFalse(FemFifStg,"No existen informacion Pendiente"); 
	
	/// 								Paso 3****************************************************
	addStep("Ejecutar el servicio de la interface: RO8_MEX.Pub:runManual Solicitando la ejecución del job: runRO8_MEX_M.");
	System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
	
	u = new SeleniumUtil(new ChromeTest(), true);

	pok = new PakageManagment(u, testCase);

	String contra = "http://" + user + ":" + ps + "@" + server + ":5555";

	u.get(contra);

	u.hardWait(4);

	pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
		

// 	String[] is = SQLUtil.getVaribleDataIntegrationServer(testCase, dbLog, tdcQueryIntegrationServer, "STATUS","RUN_ID");
	
	
	SQLResult is = executeQuery(dbLog, tdcQueryIntegrationServer);
	
	String status1 = is.getData(0, "STATUS");
	String run_id = is.getData(0, "RUN_ID");

//   	run_id = is[1];// guarda el run id de la ejecución

	boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

	while (valuesStatus) {

//    	is = SQLUtil.getVaribleDataIntegrationServer(testCase, dbLog, tdcQueryIntegrationServer, "STATUS","RUN_ID");
		
	is = executeQuery(dbLog, tdcQueryIntegrationServer);
    	
    status1 = is.getData(0, "STATUS");
    run_id = is.getData(0, "RUN_ID");
    	 
	valuesStatus = status1.equals(searchedStatus);	

	valuesStatus = status1.equals(searchedStatus);

	u.hardWait(2);

	}

	boolean successRun = status1.equals(status);//Valida si se encuentra en estatus S
	
    if(!successRun){	
    	
	    String error = String.format(tdcQueryError, run_id);
	    
	    SQLResult is3 = executeQuery(dbLog, error);
	    
	    boolean emptyError = is3.isEmpty();
	    
	    if(!emptyError){  
	    	
	    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
	    
	 testCase.addQueryEvidenceCurrentStep(is3);
	    	
	    }
	}
	
	
	/// 								Paso 4****************************************************
	addStep("Verificar que la ejecución termina con éxito.");
	System.out.println(GlobalVariables.DB_HOST_RMS_MEX);

	String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
	
    System.out.println(queryStatusLog);

//	String  fcwS  = SQLUtil.getColumn(testCase, dbLog,queryStatusLog, "STATUS");
	
	SQLResult is1 = executeQuery(dbLog, queryStatusLog);
	
	String status2 = is1.getData(0, "STATUS");
	
	boolean validateStatus = status.equals(status2);
	
	System.out.println(validateStatus);
	
    assertTrue(validateStatus,"La ejecución de la interfaz no fue exitosa");
    
	/// 								Paso 5****************************************************

	addStep("Validar que el registro de ejecución de la plaza y tienda terminó en estatus 'S' en la tabla WM_LOG_THREAD.");
	System.out.println(GlobalVariables.DB_HOST_RMS_MEX);

    String queryStatusThread = String.format(tdcQueryStatusThread, run_id);
	
	System.out.println(tdcQueryStatusThread);
	
	SQLResult is2 = executeQuery(dbLog, queryStatusThread);
	
	String status3 = is2.getData(0, "STATUS");
	

//	String regPlazaTienda = SQLUtil.getColumn(testCase, dbLog, queryStatusThread, "STATUS");
		
	boolean statusThread = status.equals(status3);
	
	System.out.println(statusThread);	
	
	if(!statusThread){	
    	
	    String error = String.format(tdcQueryError, run_id);
	    
	    SQLResult is3 = executeQuery(dbLog, error);

    	boolean emptyError = is3.isEmpty();
	    
//    boolean emptyError = SQLUtil.isEmptyQuery(testCase, dbLog, error);	
	    
	    if(!emptyError){  
	    	
	    	testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
		    
	    	testCase.addQueryEvidenceCurrentStep(is3);
	    	
	    }
	}
	
	assertTrue(statusThread,"El registro de ejecución de la plaza y tienda no fue exitoso");
	
	/// 								Paso 6****************************************************
	addStep("Verificar que los datos sean insertados en la tabla WM_GL_HEADERS_MEX de RETEK.");
	System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
	
	
	//Paso 6 *********************************

	 addStep("Verificar que los datos sean insertados en la tabla WM_GL_HEADERS_MEX de RETEK.");
	
	 System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
	String consultafor="";
	SQLResult resultfor;
	
	
	
	for(int i = 0; i < is2.getRowCount(); i++) {
	String p = is2.getData(i, "THREAD_ID");
	System.out.println(p);
	consultafor = String.format(tdcQueryWM_GL_HEADERS_MEX, p);
	resultfor = executeQuery(dbRms, consultafor);
	boolean foav = resultfor.isEmpty();
	if (!foav) {

	 Thread_id = is2.getData(i, "THREAD_ID");

	 }
	}
	String consulta = String.format(tdcQueryWM_GL_HEADERS_MEX, Thread_id);
	System.out.println(consulta);
	SQLResult result8 = executeQuery(dbRms, consulta);
	boolean av7 = result8.isEmpty();

	if (!av7) {

	 testCase.addQueryEvidenceCurrentStep(result8);

	 }

	 System.out.println(av7);

	 assertFalse(av7, "No se obtiene informacion de la consulta");
	
/*	String ThreadID = is2.getData(0, "THREAD_ID");
	
	String QueryWM_GL_HEADERS_MEX = String.format(tdcQueryWM_GL_HEADERS_MEX, ThreadID);
	
	SQLResult exeGLHeaders = executeQuery(dbRms, QueryWM_GL_HEADERS_MEX);
	
	boolean GLH = exeGLHeaders.isEmpty();
	
	assertFalse(GLH,"No existen datos insertados en la tabla"); 
*/	
	
	/// 								Paso 6****************************************************
	addStep("Verificar la actualizacion de los campos:  REFERENCE_3 y REFERENCE_9 en la tabla FEM_FIF_STG de RETEK.");
	System.out.println(GlobalVariables.DB_HOST_RMS_MEX);

	String HeaderID = result8.getData(0, "HEADER_ID");
	
	String QueryUPDATEfem_fif_stg = String.format(tdcQueryUPDATEfem_fif_stg, HeaderID);
	
	SQLResult exeUPDATEFem_fif = executeQuery(dbRms, QueryUPDATEfem_fif_stg);
	
	boolean UPDATEFem_fif = exeUPDATEFem_fif.isEmpty();
	
	assertFalse(UPDATEFem_fif,"No existen datos Actualizados en la tabla"); 
	
	
	/// 								Paso 7****************************************************

	addStep("Verificar la insercion de lineas en la tabla GL_INTERFACE de ORAFIN.");
	System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
	
	String Ref3 = exeUPDATEFem_fif.getData(0, "REFERENCE_3");
	
	String QueryRef3 = String.format(tdcQueryORAFIN, Ref3);
	
	SQLResult exeOrafin = executeQuery(db, QueryRef3);
	
	boolean Orafin = exeOrafin.isEmpty();
	
	assertFalse(Orafin,"No existen datos en la tabla"); 
	
	/// 								Paso 8****************************************************

	addStep("Ejecutar el servicio: RO8_MEX.Pub:runHist");
	System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		
	u = new SeleniumUtil(new ChromeTest(), true);

	pok = new PakageManagment(u, testCase);

	contra = "http://" + user + ":" + ps + "@" + server + ":5555";

	u.get(contra);

	u.hardWait(60);
	
	pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio2"));
		

// 	String[] is = SQLUtil.getVaribleDataIntegrationServer(testCase, dbLog, tdcQueryIntegrationServer, "STATUS","RUN_ID");
	
	
	is = executeQuery(dbLog, tdcQueryIntegrationServer);
	
	status1 = is.getData(0, "STATUS");
	run_id = is.getData(0, "RUN_ID");

//   	run_id = is[1];// guarda el run id de la ejecución

	valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

	while (valuesStatus) {

//    	is = SQLUtil.getVaribleDataIntegrationServer(testCase, dbLog, tdcQueryIntegrationServer, "STATUS","RUN_ID");
		
	is = executeQuery(dbLog, tdcQueryIntegrationServer);
    	
    status1 = is.getData(0, "STATUS");
    run_id = is.getData(0, "RUN_ID");
    	 
	valuesStatus = status1.equals(searchedStatus);	

	valuesStatus = status1.equals(searchedStatus);

	u.hardWait(2);

	}

	successRun = status1.equals(status);//Valida si se encuentra en estatus S
	
    if(!successRun){	
    	
	    String error = String.format(tdcQueryError, run_id);
	    
	    SQLResult is3 = executeQuery(dbLog, error);
	    
	    boolean emptyError = is3.isEmpty();
	    
	    if(!emptyError){  
	    	
	    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
		    
	 testCase.addQueryEvidenceCurrentStep(is3);
	    	
	    }
	}
	
	/// 								Paso 9****************************************************
    
    addStep("Verificar que los datos son insertados en la tabla de Historial en RETEK");
	System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
	
	String QueryRETEK = String.format(tdcQueryRETEK, HeaderID);

	SQLResult ExeRETEK = executeQuery(dbRms, QueryRETEK);
	
	boolean RETEK = ExeRETEK.isEmpty();
	
	assertFalse(RETEK,"No existen datos en la tabla"); 


	}
	

	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Verificar proceso de interface VENTAS ";
	}
	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}
	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_RO8_MEX_006_Ventas";
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
