package interfaces.Ro8Peru;


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

public class Ro8_Peru_Redondeo extends BaseExecution {
	public String thread_id;
	@Test(dataProvider = "data-provider")

	public void ATC_FT_RO8_Peru_004_Peru_Redondeo(HashMap<String, String> data) throws Exception {


		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbPuserPeru = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,
				GlobalVariables.DB_PASSWORD_Oiwmqa);
		SQLUtil dbRmsPeru = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERPeru, GlobalVariables.DB_USER_RMSWMUSERPeru,
				GlobalVariables.DB_PASSWORD_RMSWMUSERPeru);
		SQLUtil dbLogPeru = new SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,
				GlobalVariables.DB_PASSWORD_Oiwmqa);
		SQLUtil dbEbsPeru = new SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,
				GlobalVariables.DB_PASSWORD_OIEBSBDQ); 
		/*

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */

		String tdcQueryPaso1 = "SELECT INTERFACE_NAME, AUTO_STATUS, ATTRIBUTE1, ATTRIBUTE2 \r\n"
				+ " FROM WMUSER.wm_cfg_launcher \r\n" 
				+ " WHERE interface_name = 'RO8_PER' \r\n" 
				+ " AND attribute1=" + "'" + data.get("plaza") + "' \r\n" 
				+ " AND attribute2 =" + "'" + data.get("tranCode") + "'\r\n"
				+ " AND auto_status = 'A' \r\n";// posuser

		String tdcQueryPaso2 = " SELECT STORE,TRAN_DATE,TRAN_CODE,REFERENCE_3,REFERENCE_9,CR_PLAZA,ID \r\n"
				+ " FROM fem_fif_stg \r\n" 
				+ " WHERE tran_date >= TRUNC (SYSDATE) - 60 \r\n" 
				+ " AND cr_plaza =" + "'"+ data.get("plaza") + "' \r\n" 
				+ " AND tran_code =" + "'" + data.get("tranCode") + "' \r\n"
				+ " AND reference_3 IS NULL \r\n"
				+ " AND reference_9 IS NULL \r\n";// RMS_MEX

		String tdcQueryPaso4 = "select * from ( SELECT run_id,start_dt,status \r\n" 
				+ " FROM WMLOG.wm_log_run \r\n"
				+ " WHERE interface = 'RO8_PER_REDO' \r\n" 
				+ " and  start_dt >= TRUNC(SYSDATE) \r\n"
				+ " order by start_dt desc) \r\n" 
				+ " where rownum = 1 \r\n";

		String tdcQueryPaso4_1 = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 \r\n"
				+ " FROM WMLOG.wm_log_thread \r\n" 
				+ " WHERE parent_id = %s \r\n"; // FCWMLQA
		

		String tdcQueryPaso5 = "SELECT HEADER_ID,TRAN_CODE,TRAN_DATE,CR_PLAZA,RUN_ID \r\n"
				+  " FROM WMUSER.WM_GL_HEADERS_PER \r\n"
				+  " WHERE cr_plaza =" +"'"+ data.get("plaza")+"' \r\n"
			    +  " AND tran_code =" +"'"+ data.get("tranCode")+"' \r\n"//RMS_MEX
				+  " AND run_id =%s \r\n";//thread_id
		

		String tdcQueryPaso7 = " SELECT  STATUS,DATE_CREATED,USER_JE_CATEGORY_NAME,USER_CURRENCY_CONVERSION_TYPE \r\n"
				+ " FROM GL_INTERFACE \r\n"  //ebs
				+ " WHERE group_id =%s \r\n";//Reference_3
		
		 String tdcQueryPaso6 = "SELECT STORE,TRAN_DATE,TRAN_CODE,REFERENCE_3,REFERENCE_9,CR_PLAZA,ID \r\n"  
		 		+ " FROM fem_fif_stg \r\n" 
		 		+ " WHERE tran_date >= TRUNC (SYSDATE) - 60 \r\n"  
		 		+ " AND cr_plaza = " +"'"+ data.get("plaza")+"' \r\n"  
		 		+ " AND tran_code = "  +"'"+ data.get("tranCode")+"' \r\n"  
		 		+ " AND reference_9 = %s \r\n"  //header_id
		 		+ " AND reference_3 IS NOT NULL \r\n";//RMS_MEX
		 
		String tdcQueryPaso9= "SELECT HEADER_ID,TRAN_DATE,CR_PLAZA,NET_RETAIL \r\n"  
				+ " FROM WMUSER.wm_gl_headers_mex_hist \r\n"  
				+ " WHERE cr_plaza = "+"'"+ data.get("plaza")+"' \r\n"  
				+ " AND tran_code ="+"'"+ data.get("tranCode")+"' \r\n" 
				+ " AND header_id = '%s' \r\n";//header_id RMS_MEX
		

		/**
		 * 
		 * **********************************Pasos del caso de Prueba *
		 * *****************************************
		 * 
		 * 
		 */

//		Paso 1	**********************************************	

		addStep("Verificar parametros configurados en la tabla WM_CFG_LAUNCHER de la BD WMINT para la ejecución AUTOMATICA de la plaza 10MON.");

		System.out.println(GlobalVariables.DB_HOST_PosuserPeru);
		System.out.println(tdcQueryPaso1);
		SQLResult result = executeQuery(dbPuserPeru, tdcQueryPaso1);
		boolean ValPaso1 = result.isEmpty();

		if (!ValPaso1) {

			testCase.addQueryEvidenceCurrentStep(result);

		}

		System.out.println(ValPaso1);

		assertFalse(ValPaso1, "No se obtiene informacion de la consulta");

//Paso 2	**********************************************	

		addStep("Validar que exista información pendiente de procesar en la tabla FEM_FIF_STG de RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERPeru);

		System.out.println(tdcQueryPaso2);

		SQLResult result2 = executeQuery(dbRmsPeru, tdcQueryPaso2);

		boolean ValPaso2 = result2.isEmpty();

		if (!ValPaso2) {

			testCase.addQueryEvidenceCurrentStep(result2);

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

		addStep("Ejecutar el servicio de la interface: RO8_MEX.Pub:runREDO. Solicitando la ejecución del job: runRO8_MEX_REDO.");

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);

//Paso 4	************************

		addStep("Verificar el estatus con el cual fue terminada la ejecución de la interface en la tabla WM_LOG_RUN del usuario WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_LogChile);
		System.out.println(tdcQueryPaso4);
		SQLResult paso4 = executeQuery(dbLogPeru, tdcQueryPaso4);

		boolean Val4 = paso4.isEmpty();

		String runid = "";

		if (!Val4) {

			runid = paso4.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(paso4);

		}

		System.out.println(Val4);

		assertFalse(Val4, "No se obtiene informacion de la consulta");
//		

//	Paso 4_1  *************************************************

		addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD con STATUS = 'S'");

		System.out.println(GlobalVariables.DB_HOST_LogChile);

		String FormatoPaso4_1 = String.format(tdcQueryPaso4_1, runid);

		System.out.println(FormatoPaso4_1);

		SQLResult paso4_1 = executeQuery(dbLogPeru, FormatoPaso4_1);
		boolean ValPaso4_1 = paso4_1.isEmpty();

		String threadid = "";

		if (!ValPaso4_1) {

			threadid = paso4_1.getData(0, "thread_id");
			testCase.addQueryEvidenceCurrentStep(paso4_1);

		}

		System.out.println(ValPaso4_1);

		assertFalse(ValPaso4_1, "No se obtiene informacion de la consulta");

//Paso 5  *********************************

		addStep("Verificar que los datos sean insertados en la tabla WM_GL_HEADERS_MEX de RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERPeru);

		String FormatoPaso5 = String.format(tdcQueryPaso5, threadid);

		System.out.println(FormatoPaso5);

		SQLResult paso5 = executeQuery(dbRmsPeru, FormatoPaso5);
		boolean ValPaso5 = paso5.isEmpty();

		String headerid = "";

		if (!ValPaso5) {

			headerid = paso5.getData(0, "header_id");
			testCase.addQueryEvidenceCurrentStep(paso5);

		}

		System.out.println(ValPaso5);

		assertFalse(ValPaso5, "No se obtiene informacion de la consulta");

// Paso 6 *********************************

		addStep("Verificar la actualizacion de los campos: REFERENCE_3 y REFERENCE_9 en la tabla FEM_FIF_STG de RETEK.");

		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERPeru);
		String FormatoPaso6 = String.format(tdcQueryPaso6, headerid);
		System.out.println(FormatoPaso6);
		SQLResult Paso6 = executeQuery(dbRmsPeru, FormatoPaso6);

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

		System.out.println(GlobalVariables.DB_HOST_OIEBSBDQ);
		String FormatoPaso7 = String.format(tdcQueryPaso7, reference3);
		System.out.println(FormatoPaso7);
		SQLResult Paso7 = executeQuery(dbEbsPeru, FormatoPaso7);

		boolean ValPaso7 = Paso7.isEmpty();

		if (!ValPaso7) {

			testCase.addQueryEvidenceCurrentStep(Paso7);

		}

		System.out.println(ValPaso7);

		assertFalse(ValPaso7, "No se obtiene informacion de la consulta");
//		

////	Paso 8 *************************************************
		addStep("Ejecutar el servicio: RO8_MEX.Pub:runHist, solicitando la ejecución del job: runRO8_MEX_HIST");

		SeleniumUtil prueba = new SeleniumUtil(new ChromeTest(), true);

		prueba.hardWait(60);

		contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		prueba.get(contra);

////	Paso 9  *************************************************
		addStep("Verificar que los datos son insertados en la tabla de Historial en RETEK");

		System.out.println(GlobalVariables.DB_HOST_RMSWMUSERPeru);
		String FormatoPaso9 = String.format(tdcQueryPaso9, headerid);
		System.out.println(FormatoPaso9);
		SQLResult paso9 = executeQuery(dbRmsPeru, FormatoPaso9);

		boolean ValPaso9 = paso9.isEmpty();

		if (!ValPaso9) {

			testCase.addQueryEvidenceCurrentStep(paso9);

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

		return "Construido. Se procesa todos los movimientos de inventario registrados en Retek y actualiza Oracle con las pólizas correspondientes.";

	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub

		return "ATC_FT_RO8_Peru_004_Peru_Redondeo";

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
