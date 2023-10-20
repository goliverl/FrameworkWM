package interfaces.Ro8Chl;


import modelo.BaseExecution;
import util.GlobalVariables;
import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class Ro8_Chl_Ventas extends BaseExecution {
public String Thread_id;

	@Test(dataProvider = "data-provider")
	public void ATC_FT_RO8_Chl_006_Ventas(HashMap<String, String> data) throws Exception {
	
	/* Utilerías *********************************************************************/

	
	SQLUtil dbPuserChile = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile, GlobalVariables.DB_USER_PosUserChile,
			GlobalVariables.DB_PASSWORD_PosUserChile);
	SQLUtil dbRmsChile = new SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERChile, GlobalVariables.DB_USER_RMSWMUSERChile,
			GlobalVariables.DB_PASSWORD_RMSWMUSERChile);
	SQLUtil dbLogChile = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile,
			GlobalVariables.DB_PASSWORD_LogChile);
	SQLUtil dbEbsChile = new SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,
			GlobalVariables.DB_PASSWORD_OIEBSBDQ); 
	/**
	 * Variables ******************************************************************************************
	 * 
	 * 
	*/

	String tdcQueryPaso1 = "SELECT INTERFACE_NAME,AUTO_STATUS,MANUAL_STATUS,ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4 FROM WMUSER.WM_CFG_LAUNCHER \r\n" 
	        + " WHERE INTERFACE_NAME = 'RO8_CL' \r\n"
	        + " AND ATTRIBUTE1= '" + data.get("plaza") + "' \r\n" 
			+ " AND ATTRIBUTE2 = '" + data.get("Tran_cod") + "' \r\n"
			+ " AND AUTO_STATUS = 'A'";
	
	String tdcQueryPaso2 = "SELECT ITEM,TRAN_CODE,CR_PLAZA,REFERENCE_3,REFERENCE_9,TRAN_DATE \r\n"
			+ " FROM fem_fif_stg \r\n"
			+ " WHERE  tran_date >= TRUNC (SYSDATE) - 60 \r\n"
			+ " AND CR_PLAZA = '" + data.get("plaza")+"' \r\n"
			+ " AND TRAN_CODE = '"+ data.get("Tran_cod")+"' \r\n"
			+ " AND REFERENCE_3 IS NULL \r\n"
			+ " AND REFERENCE_9 IS NULL \r\n";
			
	String tdcQueryPaso4 = "select * from ( SELECT run_id,start_dt,status \r\n"
			+ " FROM WMLOG.wm_log_run \r\n"
			+ " WHERE interface = '" + data.get("INTERFACE")+"' \r\n" 	
			+ " and  start_dt >= TRUNC (SYSDATE) \r\n"
		    + " order by start_dt desc) \r\n"
			+ " where rownum = 1 \r\n";	
	
	String tdcQueryPaso4_1 = "SELECT parent_id,thread_id,name,status,att1,att2 \r\n"
			+ " FROM WMLOG.wm_log_thread \r\n"
			+ " WHERE parent_id = %s \r\n";
	     	
	String tdcQueryPaso5= "SELECT HEADER_ID,TRAN_CODE,TRAN_DATE,CR_PLAZA,RUN_ID \r\n"
			+ " FROM WMUSER.wm_gl_headers_cl \r\n"
			+ " WHERE TRAN_CODE = '"+ data.get("Tran_cod")+"' \r\n"
			+ " AND RUN_ID = %s \r\n"
			+ " AND CR_PLAZA = '" + data.get("plaza")+"' \r\n";
	
	String tdcQueryPaso6= "SELECT ITEM,TRAN_CODE,CR_PLAZA,REFERENCE_3,REFERENCE_9,TRAN_DATE \r\n"
			+ " FROM fem_fif_stg \r\n"
			+ " WHERE  tran_date >= TRUNC (SYSDATE) - 60 \r\n"
			+ " AND TRAN_CODE = '"+ data.get("Tran_cod")+"' \r\n"
			+ " AND REFERENCE_9 = %s \r\n"
			+ " AND REFERENCE_3 IS NOT NULL \r\n"
			+ " AND CR_PLAZA = '" + data.get("plaza")+"' \r\n";
	
	String tdcQueryPaso7= "SELECT GROUP_ID \r\n"
			+ " FROM GL.GL_INTERFACE  \r\n"
			+ " WHERE GROUP_ID = %s \r\n";
	
	String tdcQueryPaso9= "SELECT TRAN_CODE,CR_PLAZA,HEADER_ID \r\n"
			+ " FROM WMUSER.wm_gl_headers_cl_hist \r\n"
			+ " WHERE TRAN_CODE = '"+ data.get("Tran_cod")+"' \r\n"
			+ " AND CR_PLAZA = '" + data.get("plaza")+"' \r\n"
			+ " AND HEADER_ID = %s \r\n";


	/*
	 * Pasos
	 *********************************************************************************/

/// Paso 1 ************************************************

	addStep("Verificar parametros configurados en la tabla WM_CFG_LAUNCHER de la BD WMINT");
	System.out.println(GlobalVariables.DB_HOST_PosUserChile);

	System.out.println(tdcQueryPaso1);

	SQLResult Paso1 = executeQuery(dbPuserChile, tdcQueryPaso1);

	boolean ValPaso1 = Paso1.isEmpty();

	if (!ValPaso1) {

		testCase.addQueryEvidenceCurrentStep(Paso1);

	}
	assertFalse(ValPaso1, "No existen parametros configurados en la tabla");

/// Paso 2****************************************************

	addStep("Validar que exista información pendiente de procesar en la tabla FEM_FIF_STG de RETEK.");

	System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);
	System.out.println(tdcQueryPaso2);
	SQLResult Paso2 = executeQuery(dbRmsChile, tdcQueryPaso2);

	boolean ValPaso2 = Paso2.isEmpty();

	if (!ValPaso2) {

		testCase.addQueryEvidenceCurrentStep(Paso2);

	}
	assertFalse(ValPaso2, "No existen informacion Pendiente");

/// Paso 3****************************************************
	
	SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
	PakageManagment pok = new PakageManagment(u, testCase);

	String user = data.get("user");
	String ps = PasswordUtil.decryptPassword(data.get("ps"));
	String server = data.get("server");

	addStep("Ejecutar el servicio de la interface: RO8_MEX.Pub:runManual Solicitando la ejecución del job: runRO8_MEX_M.");

	String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
	u.get(contra);

	pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
	
/// Paso 4****************************************************

	addStep("Verificar que la ejecución termina con éxito.");

	System.out.println(GlobalVariables.DB_HOST_LogChile);

	System.out.println(tdcQueryPaso4);

	SQLResult Paso4 = executeQuery(dbLogChile, tdcQueryPaso4);

	boolean ValPaso4 = Paso4.isEmpty();

	String runid = "";

	if (!ValPaso4) {

		runid = Paso4.getData(0, "RUN_ID");
		testCase.addQueryEvidenceCurrentStep(Paso4);

	}
	System.out.println(ValPaso4);

	assertFalse(ValPaso4, "La ejecución de la interfaz no fue exitosa");

/// Paso 4_1****************************************************

	addStep("Verificar hilos");

	System.out.println(GlobalVariables.DB_HOST_LogChile);

	String FormatoPaso4_1 = String.format(tdcQueryPaso4_1, runid);

	System.out.println(FormatoPaso4_1);

	SQLResult Paso4_1 = executeQuery(dbLogChile, FormatoPaso4_1);

	boolean ValPaso4_1 = Paso4.isEmpty();

	String threasid = "";

	if (!ValPaso4_1) {

		threasid = Paso4_1.getData(0, "thread_id");
		testCase.addQueryEvidenceCurrentStep(Paso4);

	}
	System.out.println(ValPaso4_1);

	assertFalse(ValPaso4_1, "La ejecución de la interfaz no fue exitosa");

/// Paso 5****************************************************

	System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);

	String FormatoPaso5 = String.format(tdcQueryPaso5, threasid);

	System.out.println(FormatoPaso5);

	SQLResult Paso5 = executeQuery(dbRmsChile, FormatoPaso5);

	boolean ValPaso5 = Paso5.isEmpty();

	String headerid = "";

	if (!ValPaso5) {

		headerid = Paso5.getData(0, "header_id");
		testCase.addQueryEvidenceCurrentStep(Paso4);

	}
	System.out.println(ValPaso5);

	assertFalse(ValPaso5, "El registro de ejecución de la plaza y tienda no fue exitoso");

// Paso 6 *********************************

	addStep("Verificar que los datos sean insertados en la tabla WM_GL_HEADERS_MEX de RETEK.");

	System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);

	String FormatoPaso6 = String.format(tdcQueryPaso6, headerid);

	System.out.println(FormatoPaso6);

	SQLResult Paso6 = executeQuery(dbRmsChile, FormatoPaso6);

	boolean ValPaso6 = Paso6.isEmpty();

	String reference3 = "";

	if (!ValPaso6) {

		reference3 = Paso6.getData(0, "reference_3");
		testCase.addQueryEvidenceCurrentStep(Paso4);

	}
	System.out.println(ValPaso6);

	assertFalse(ValPaso6, "El registro de ejecución de la plaza y tienda no fue exitoso");

/// Paso 7 ****************************************************

	addStep("Verificar la insercion de lineas en la tabla GL_INTERFACE de ORAFIN.");

	System.out.println(GlobalVariables.DB_HOST_OIEBSBDQ);

	String FormatoPaso7 = String.format(tdcQueryPaso7, reference3);

	SQLResult Paso7 = executeQuery(dbEbsChile, FormatoPaso7);

	boolean ValPaso7 = Paso7.isEmpty();

	if (!ValPaso7) {

		testCase.addQueryEvidenceCurrentStep(Paso7);
	}

	assertFalse(ValPaso7, "No existen datos en la tabla");

/// Paso 8****************************************************

	addStep("Ejecutar el servicio: RO8_MEX.Pub:runHist");
	System.out.println(GlobalVariables.DB_HOST_RMS_MEX);

	u = new SeleniumUtil(new ChromeTest(), true);

	pok = new PakageManagment(u, testCase);

	contra = "http://" + user + ":" + ps + "@" + server + ":5555";

	u.get(contra);

	u.hardWait(60);

	pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio2"));

/// Paso 9****************************************************

	addStep("Verificar que los datos son insertados en la tabla de Historial en RETEK");

	System.out.println(GlobalVariables.DB_HOST_RMSWMUSERChile);

	String FormatoPaso9 = String.format(tdcQueryPaso9, headerid);

	SQLResult Paso9 = executeQuery(dbRmsChile, FormatoPaso9);

	boolean ValPaso9 = Paso9.isEmpty();

	if (!ValPaso9) {

		testCase.addQueryEvidenceCurrentStep(Paso9);

	}

	assertFalse(ValPaso9, "No existen datos en la tabla");

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
	return "ATC_FT_RO8_Chl_006_Ventas";
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
