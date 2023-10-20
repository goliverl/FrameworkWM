package interfaces.Ro8Chl;


import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class Ro8_Chl_Transferencias extends BaseExecution {
	public String thread_id;
	@Test(dataProvider = "data-provider")
	public void ATC_FT_RO8_Chl_005_Transferencias(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/


		SQLUtil dbPuserChile = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile, GlobalVariables.DB_USER_PosUserChile,
				GlobalVariables.DB_PASSWORD_PosUserChile);
		SQLUtil dbRmsChile = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERChile, GlobalVariables.DB_USER_RMSWMUSERChile,
				GlobalVariables.DB_PASSWORD_RMSWMUSERChile);
		SQLUtil dbLogChile = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile,
				GlobalVariables.DB_PASSWORD_LogChile);
		SQLUtil dbEbsChile = new SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,
				GlobalVariables.DB_PASSWORD_OIEBSBDQ); 

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		String tdcQueryPaso1 = " SELECT INTERFACE_NAME, AUTO_STATUS, ATTRIBUTE1, ATTRIBUTE2 \r\n"
				+ " FROM WMUSER.wm_cfg_launcher \r\n" 
				+ " WHERE interface_name = 'RO8_CL' \r\n" 
				+ " AND attribute1=" + "'" + data.get("plaza") + "' \r\n" 
				+ " AND attribute2 =" + "'" + data.get("tranCode") + "' \r\n"
				+ " AND auto_status = 'A' \r\n";// posuser

		String tdcQueryPaso2 = " SELECT STORE,TRAN_DATE,TRAN_CODE,REFERENCE_3,REFERENCE_9,CR_PLAZA,ID \r\n"
				+ " FROM fem_fif_stg \r\n" 
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60 \r\n" 
				+ " AND cr_plaza =" + "'"+ data.get("plaza") + "' \r\n" 
				+ " AND tran_code =" + "'" + data.get("tranCode") + "' \r\n"
				+ " AND reference_3 IS NULL \r\n"
				+ " AND reference_9 IS NULL \r\n";// RMS_MEX

		String tdcQueryPaso4 = " select * from ( SELECT run_id,start_dt,status \r\n" 
				+ " FROM WMLOG.wm_log_run  \r\n"
				+ " WHERE interface = 'RO8_CL_TRANS' \r\n" 
				+ " and  start_dt >= TRUNC(SYSDATE) \r\n"
				+ " order by start_dt desc) \r\n" 
				+ " where rownum = 1 \r\n";

		String tdcQueryPaso4_1 = " SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 \r\n"
				+ " FROM WMLOG.wm_log_thread \r\n" 
				+ " WHERE parent_id = %s \r\n"; // FCWMLQA
		

		String tdcQueryPaso5 = " SELECT HEADER_ID,TRAN_CODE,TRAN_DATE,CR_PLAZA,RUN_ID \r\n"
				+  " FROM WMUSER.WM_GL_HEADERS_CL \r\n"
				+  " WHERE cr_plaza =" +"'"+ data.get("plaza")+"' \r\n"
			    +  " AND tran_code =" +"'"+ data.get("tranCode")+"' \r\n"//RMS_MEX
				+  " AND run_id =%s \r\n";//thread_id
		
		String tdcQueryPaso7 = " SELECT  STATUS,DATE_CREATED,USER_JE_CATEGORY_NAME,USER_CURRENCY_CONVERSION_TYPE "
				+ " FROM GL_INTERFACE" //ebs
			    + " WHERE group_id =%s";//Reference_3
		
		 String tdcQueryPaso6 = " SELECT STORE,TRAN_DATE,TRAN_CODE,REFERENCE_3,REFERENCE_9,CR_PLAZA,ID"  
		 		+ " FROM fem_fif_stg"  
		 		+ " WHERE tran_date >= TRUNC (SYSDATE) - 60"  
		 		+ " AND cr_plaza = " +"'"+ data.get("plaza")+"'"  
		 		+ " AND tran_code = "  +"'"+ data.get("tranCode")+"'"  
		 		+ " AND reference_9 = %s"  //header_id
		 		+ " AND reference_3 IS NOT NULL";//RMS_MEX
		 
		String tdcQueryPaso9= " SELECT HEADER_ID,TRAN_DATE,CR_PLAZA,NET_RETAIL \r\n"  
				+ " FROM WMUSER.wm_gl_headers_Cl_hist \r\n" 
				+ " WHERE cr_plaza = "+"'"+ data.get("plaza")+"' \r\n"  
				+ " AND tran_code ="+"'"+ data.get("tranCode")+"' \r\n"
				+ " AND header_id = '%s' \r\n ";//header_id RMS_MEX
		
		


		/**
		 * 
		 * **********************************Pasos del caso de Prueba *
		 * *****************************************
		 * 
		 * 
		 */
		testCase.setProject_Name("Interfaces WebMethods");

//		Paso 1	**********************************************	
		
		addStep("Verificar parametros configurados en la tabla WM_CFG_LAUNCHER de la BD WMINT para la ejecución AUTOMATICA de la plaza 10MON.");

		System.out.println(GlobalVariables.DB_HOST_PosUserChile);
		System.out.println(tdcQueryPaso1);
		SQLResult Paso1 = executeQuery(dbPuserChile, tdcQueryPaso1);
		boolean ValPaso1 = Paso1.isEmpty();

		if (!ValPaso1) {

			testCase.addQueryEvidenceCurrentStep(Paso1);

		}

		System.out.println(ValPaso1);

		assertFalse(ValPaso1, "No se obtiene informacion de la consulta");

//Paso 2	**********************************************	
		addStep("Validar que exista información pendiente de procesar en la tabla FEM_FIF_STG de RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);

		System.out.println(tdcQueryPaso2);

		SQLResult Paso2 = executeQuery(dbRmsChile, tdcQueryPaso2);

		boolean ValPaso2 = Paso2.isEmpty();

		if (!ValPaso2) {

			testCase.addQueryEvidenceCurrentStep(Paso2);

		}

		System.out.println(ValPaso2);

		assertFalse(ValPaso2, "No se obtiene informacion de la consulta");

//Paso 3  ****************** *******************************

		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		addStep("Ejecutar el servicio de la interface: RO8_MEX.Pub:runTRANS. Solicitando la ejecución del job: runRO8_MEX_TRANS.");

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		System.out.println(contra);
		u.get(contra);

		pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);

//Paso 4	************************

		addStep("Verificar el estatus con el cual fue terminada la ejecución de la interface en la tabla WM_LOG_RUN del usuario WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		System.out.println(tdcQueryPaso4);

		SQLResult paso4 = executeQuery(dbLogChile, tdcQueryPaso4);

		boolean ValPas4 = paso4.isEmpty();

		String runid = "";
		if (!ValPas4) {

			runid = paso4.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(paso4);

		}

		System.out.println(ValPas4);

		assertFalse(ValPas4, "No se obtiene informacion de la consulta");

//	

//Paso 4_1	************************

		addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD con STATUS = 'S'");

		System.out.println(GlobalVariables.DB_HOST_LogChile);


		String FormatoPaso4_1 = String.format(tdcQueryPaso4_1, runid);
		System.out.println(tdcQueryPaso4_1);
		SQLResult paso4_1 = executeQuery(dbLogChile, FormatoPaso4_1);

		boolean ValPas4_1 = paso4_1.isEmpty();

		String threadid = "";

		if (!ValPas4_1) {

			threadid = paso4_1.getData(0, "thread_id");
			testCase.addQueryEvidenceCurrentStep(paso4_1);

		}

		System.out.println(ValPas4_1);

		assertFalse(ValPas4_1, "No se obtiene informacion de la consulta");
//		

//	Paso 5  *************************************************

		addStep("Verificar que los datos sean insertados en la tabla WM_GL_HEADERS_MEX de RETEK.\n");

		System.out.println(GlobalVariables.DB_HOST_OIEBSBDQ);
		String FormatoPaso5 = String.format(tdcQueryPaso5, threadid);
		System.out.println(FormatoPaso5);
		SQLResult paso5 = executeQuery(dbRmsChile, FormatoPaso5);

		boolean ValPaso5 = paso5.isEmpty();

		String headerid = "";

		if (!ValPaso5) {

			headerid = paso5.getData(0, "header_id");
			testCase.addQueryEvidenceCurrentStep(paso5);

		}

		System.out.println(ValPaso5);

		assertFalse(ValPaso5, "No se obtiene informacion de la consulta");

//Paso 6 *********************************

		addStep("Verificar que los datos sean insertados en la tabla WM_GL_HEADERS_MEX de RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);

		String FormatoPaso6 = String.format(tdcQueryPaso6, headerid);
		System.out.println(FormatoPaso6);
		SQLResult Paso6 = executeQuery(dbRmsChile, FormatoPaso6);

		boolean ValPaso6 = Paso6.isEmpty();

		String reference3 = "";

		if (!ValPaso6) {

			reference3 = Paso6.getData(0, "reference_3");
			testCase.addQueryEvidenceCurrentStep(Paso6);

		}

		System.out.println(ValPaso6);

		assertFalse(ValPaso6, "No se obtiene informacion de la consulta");

// Paso 7 *********************************

		addStep("Verificar la insercion de lineas en la tabla GL_INTERFACE de ORAFIN.");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String FormatoPaso7 = String.format(tdcQueryPaso7, reference3);
		System.out.println(FormatoPaso7);
		SQLResult Paso7 = executeQuery(dbEbsChile, FormatoPaso7);
		boolean ValPaso7 = FormatoPaso7.isEmpty();

		if (!ValPaso7) {

			testCase.addQueryEvidenceCurrentStep(Paso7);

		}

		System.out.println(ValPaso7);

		assertFalse(ValPaso7, "No se obtiene informacion de la consulta");

////	Paso 8  *************************************************

		addStep("Ejecutar el servicio: RO8_MEX.Pub:runHist, solicitando la ejecución del job: runRO8_MEX_HIST");

		SeleniumUtil prueba = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pokPrueba = new PakageManagment(prueba, testCase);

		prueba.hardWait(60);

		contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		prueba.get(contra);

		pokPrueba.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio_hist"));

////	Paso 9  *************************************************

		addStep("Verificar que los datos son insertados en la tabla de Historial en RETEK");

		System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		String FormatoPaso9 = String.format(tdcQueryPaso9, headerid);

		System.out.println(FormatoPaso9);
		SQLResult Paso9 = executeQuery(dbRmsChile, FormatoPaso9);

		boolean ValPaso9 = Paso9.isEmpty();

		if (!ValPaso9) {

			testCase.addQueryEvidenceCurrentStep(Paso9);

		}

		System.out.println(ValPaso9);

		assertFalse(ValPaso9, "No se obtiene informacion de la consulta");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Verificar proceso de interface TRANSFERENCIAS ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_RO8_Chl_005_Transferencias";
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
