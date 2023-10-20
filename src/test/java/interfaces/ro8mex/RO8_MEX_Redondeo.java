package interfaces.ro8mex;

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
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class RO8_MEX_Redondeo extends BaseExecution {
	public String thread_id;
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_RO8_MEX_Redondeo(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX, GlobalVariables.DB_USER_RMS_MEX,GlobalVariables.DB_PASSWORD_RMS_MEX);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS,GlobalVariables.DB_PASSWORD_EBS);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		String SelectCFG = "SELECT INTERFACE_NAME, AUTO_STATUS, ATTRIBUTE1, ATTRIBUTE2 "
				+ " FROM WMUSER.wm_cfg_launcher " 
				+ " WHERE interface_name = 'RO8_MEX'" 
				+ " AND attribute1=" + "'" + data.get("plaza") + "'" 
				+ " AND attribute2 =" + "'" + data.get("tranCode") + "'"
				+ " AND auto_status = 'A'";// posuser

		String SelectInsumos = " SELECT STORE,TRAN_DATE,TRAN_CODE,REFERENCE_3,REFERENCE_9,CR_PLAZA,ID"
				+ " FROM fem_fif_stg" 
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60" 
				+ " AND cr_plaza =" + "'"+ data.get("plaza") + "'" 
				+ " AND tran_code =" + "'" + data.get("tranCode") + "'"
				+ " AND reference_3 IS NULL"
				+ " AND reference_9 IS NULL";// RMS_MEX

		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" 
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'RO8_MEX_REDO'" 
				+ " and  start_dt >= TRUNC(SYSDATE)"
				+ " order by start_dt desc)" + " where rownum = 1";
		
		String tdcQueryIntegrationServer2 = "select * from ( SELECT run_id,start_dt,status" 
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'RO8_MEX_HIST'" 
				+ " and  start_dt >= TRUNC(SYSDATE)"
				+ " order by start_dt desc)" + " where rownum = 1";

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " 
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA


		String tdcQueryStatusThread = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 "
				+ " FROM WMLOG.wm_log_thread " + " WHERE parent_id = %s"; // FCWMLQA
		
		String consulta5 = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER "
				+ " FROM WMLOG.WM_LOG_RUN "
				+ " WHERE RUN_ID =%s";

		String VerificacionHeader = "SELECT HEADER_ID,TRAN_CODE,TRAN_DATE,CR_PLAZA,RUN_ID"
				+  " FROM WMUSER.WM_GL_HEADERS_MEX"
				+  " WHERE cr_plaza =" +"'"+ data.get("plaza")+"'"
			    +  " AND tran_code =" +"'"+ data.get("tranCode")+"'"//RMS_MEX
				+  " AND run_id =%s";//thread_id
		

		String ConsultaGl = " SELECT  STATUS,DATE_CREATED,USER_JE_CATEGORY_NAME,USER_CURRENCY_CONVERSION_TYPE "
							+ " FROM GL_INTERFACE" //ebs
							+ " WHERE group_id =%s";//Reference_3
		
		 String VerificacionR3R9 = "SELECT STORE,TRAN_DATE,TRAN_CODE,REFERENCE_3,REFERENCE_9,CR_PLAZA,ID" + 
		 		" FROM fem_fif_stg" + 
		 		" WHERE tran_date >= TRUNC (SYSDATE) - 60" + 
		 		" AND cr_plaza = " +"'"+ data.get("plaza")+"'" + 
		 		" AND tran_code = "  +"'"+ data.get("tranCode")+"'" + 
		 		" AND reference_9 = %s" + //header_id
		 		" AND reference_3 IS NOT NULL";//RMS_MEX
		 
		String VerificarHist= "SELECT HEADER_ID,TRAN_DATE,CR_PLAZA,NET_RETAIL" + 
				"  FROM WMUSER.wm_gl_headers_mex_hist" + 
				" WHERE cr_plaza = "+"'"+ data.get("plaza")+"'" + 
				" AND tran_code ="+"'"+ data.get("tranCode")+"'" +
				" AND header_id = '%s' ";//header_id RMS_MEX
		
		
		String status = "S";

		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String con = "http://" + user + ":" + ps + "@" + server;
		String searchedStatus = "R";
		String run_id;
		

		/**
		 * 
		 * **********************************Pasos del caso de Prueba * *****************************************
		 * 
		 * 
		 */
		testCase.setProject_Name("Interfaces WebMethods");

		
//		Paso 1	**********************************************	
		addStep("Verificar parametros configurados en la tabla WM_CFG_LAUNCHER de la BD WMINT para la ejecución AUTOMATICA de la plaza 10MON.");

		System.out.println(GlobalVariables.DB_HOST_Puser);

		System.out.println(SelectCFG);
		
		SQLResult result = executeQuery(dbPos, SelectCFG);
		boolean av = result.isEmpty();

		
		if (!av) {

			testCase.addQueryEvidenceCurrentStep(result);  

		}

		System.out.println(av);

		assertFalse(av, "No se obtiene informacion de la consulta");

//Paso 2	**********************************************	
		addStep("Validar que exista información pendiente de procesar en la tabla FEM_FIF_STG de RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);

		System.out.println(SelectInsumos);
		
		SQLResult result2 = executeQuery(dbRms, SelectInsumos);

		boolean SC = result2.isEmpty();

		if (!SC) {

			testCase.addQueryEvidenceCurrentStep(result2);

		}

		System.out.println(SC);

		assertFalse(SC, "No se obtiene informacion de la consulta");

//Paso 3  ****************** *******************************
		addStep("Ejecutar el servicio de la interface: RO8_MEX.Pub:runREDO. Solicitando la ejecución del job: runRO8_MEX_REDO.");

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
			

			u.hardWait(2);

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
		}

//Paso 4	************************
	    
	    
	    addStep("Verificar el estatus con el cual fue terminada la ejecución de la interface en la tabla WM_LOG_RUN del usuario WMLOG.");
		
		//String run = "2159857851";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
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

//	Paso 5  *************************************************
	addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD con STATUS = 'S'");
	
	System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
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

//Paso 6 *********************************

		addStep("Verificar que los datos sean insertados en la tabla WM_GL_HEADERS_MEX de RETEK.");
		
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String consultafor="";
		SQLResult resultfor;
		
		for(int i = 0; i < paso5.getRowCount(); i++) {
		String p = paso5.getData(i, "THREAD_ID");
		System.out.println(p);
		consultafor = String.format(VerificacionHeader, p);
		resultfor = executeQuery(dbRms, consultafor);
		
		boolean foav = resultfor.isEmpty();
		
		if (!foav) {

			thread_id = paso5.getData(i, "THREAD_ID");

		}
	
		}
		
		String consulta = String.format(VerificacionHeader, thread_id);
		System.out.println(consulta);
		SQLResult result8 = executeQuery(dbRms, consulta);
		
		boolean av7 = result8.isEmpty();

		
		if (!av7) {

			testCase.addQueryEvidenceCurrentStep(result8);

		}

		System.out.println(av7);

		assertFalse(av7, "No se obtiene informacion de la consulta");

		
		//Paso 7 *********************************
		
		addStep("Verificar la actualizacion de los campos: REFERENCE_3 y REFERENCE_9 en la tabla FEM_FIF_STG de RETEK.");
		
		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String header_id = result8.getData(0, "HEADER_ID");
		//String header_id = "1228216495";
		String consulta2 = String.format(VerificacionR3R9, header_id);
		SQLResult result9 = executeQuery(dbRms, consulta2);
		String R3 = result9.getData(0, "reference_3");
		System.out.println(consulta2);
		
		boolean av6 = result9.isEmpty();

		if (!av6) {

			testCase.addQueryEvidenceCurrentStep(result9);

		}

		System.out.println(av6);

		assertFalse(av6, "No se obtiene informacion de la consulta");

		//Paso 8 *********************************
		
		addStep("Verificar la insercion de lineas en la tabla GL_INTERFACE de ORAFIN.");
		System.out.println(GlobalVariables.DB_HOST_EBS);
		String consulta3 = String.format(ConsultaGl, R3);
		System.out.println(consulta3);
		
		SQLResult result10 = executeQuery(dbEbs, consulta3);
		

		boolean av8 = result10.isEmpty();

		if (!av8) {

			testCase.addQueryEvidenceCurrentStep(result10);

		}

		System.out.println(av8);

		assertFalse(av8, "No se obtiene informacion de la consulta");
//		
		
////	Paso 9  *************************************************
	addStep("Ejecutar el servicio: RO8_MEX.Pub:runHist, solicitando la ejecución del job: runRO8_MEX_HIST");
	
	SeleniumUtil prueba = new SeleniumUtil(new ChromeTest(), true);
	PakageManagment pokPrueba = new PakageManagment(prueba, testCase);
	
	prueba.hardWait(60);
	
	 contra = "http://" + user + ":" + ps + "@" + server + ":5555";
	prueba.get(contra);
	
    pokPrueba.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio_hist"));
    SQLResult resultE = executeQuery(dbLog, tdcQueryIntegrationServer2);
	String status2 = resultE.getData(0, "STATUS");
	run_id = resultE.getData(0, "RUN_ID");
	 
	boolean valuesStatus1 = status2.equals(searchedStatus);//Valida si se encuentra en estatus R
	while (valuesStatus1) {  
		resultE = executeQuery(dbLog, tdcQueryIntegrationServer);
		
	 status2 = resultE.getData(0, "STATUS");
	 
		run_id = resultE.getData(0, "RUN_ID");
		
	 prueba.hardWait(2);
	 
	}
	boolean successRun1 = status2.equals(status);//Valida si se encuentra en estatus S
	    if(!successRun1){
	   
	   String error1 = String.format(tdcQueryErrorId, run_id);
	   
	   SQLResult e1 = executeQuery(dbLog, error1);
	   
	   boolean emptyError1 = e1.isEmpty();
	   
	   if(!emptyError1){  
	   
	    testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
	   
	    testCase.addQueryEvidenceCurrentStep(e1);
	   
	   }
	}
		
	    
////	Paso 10  *************************************************
	addStep("Verificar que los datos son insertados en la tabla de Historial en RETEK");
	
	System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
	String consulta9 = String.format(VerificarHist, header_id);
	
	System.out.println(consulta9);
	SQLResult paso9 = executeQuery(dbRms, consulta9);

	
	
	boolean av9 = paso9.isEmpty();

	
	if (!av9) {

		testCase.addQueryEvidenceCurrentStep(paso9);

	}

	System.out.println(av9);

  assertFalse(av9, "No se obtiene informacion de la consulta");
	
	

	}
	


			

	
	
	

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Verificar proceso de interface REDONDEO ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Jorge Iván Carvajal González";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_004_RO8_MEX_Redondeo";
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
