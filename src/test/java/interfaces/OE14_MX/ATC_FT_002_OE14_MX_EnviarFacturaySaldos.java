package interfaces.OE14_MX;

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

public class ATC_FT_002_OE14_MX_EnviarFacturaySaldos extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_OE14_MX_EnviarFacturaySaldos_test(HashMap<String, String> data) throws Exception {
	
/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbAvebqa = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		
		SeleniumUtil u;
		PakageManagment pok;	
		
		testCase.setProject_Name("I20134-TI01 Homologación CxP FEMSA");    
				
		/** VARIABLES *********************************************************************/	
		
		String status = data.get("status");  // status exitoso
		String wm_status = data.get("wmStatus"); // status exitoso
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String run_id = "";
		
		String select = data.get("select");
		
		String select2 = data.get("select2");
		
		String select3 = data.get("select3");
	
		String select4 = data.get("select4");
		
		String selectCampos = "SELECT WM_STATUS, WM_MENSAJE, WM_FECHA_ENVIO, WM_RUN_ID FROM "+ data.get("tabla") + " where rownum <=1";//retek_cr
		
		String selectCamposID = "SELECT WM_STATUS, WM_MENSAJE, TRUNC(WM_FECHA_ENVIO) AS WM_FECHA_ENVIO, WM_RUN_ID FROM "+ data.get("tabla") + " where " + data.get("identificador") +" = '%s'";
		
		String selectDate = "SELECT TRUNC(SYSDATE) FROM DUAL";

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%OE14MX_FS%' "
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
			
	
		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
		
		/* PASO 1, 2 y 3 *********************************************************************/
		
		addStep("Validar que la tabla cuente con los siguientes campos para WM en la tabla " + data.get("tabla"));		
		
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		SQLResult paso1 = executeQuery(dbAvebqa, selectCampos);		
		System.out.println(selectCampos);
		
		boolean av = paso1.isEmpty();
		
		//if (!av) {
			testCase.addQueryEvidenceCurrentStep(paso1);
		//} 

		System.out.println(av);		
		assertFalse(av, "No se obtiene información de la consulta en la tabla " + data.get("tabla"));
		 
		
		/* PASO 4 *********************************************************************/
		
		addStep("Validar que se tenga información para procesar en la tabla " + data.get("tabla"));		
	
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		String selectFormat = String.format(select, data.get("variable1"));	
		String selectFormat2 = String.format(select2, data.get("variable1"));
		String selectFormat3 = String.format(select3, data.get("variable1"));
		String selectFormat4 = String.format(select4, data.get("variable1"));
		System.out.println(selectFormat);		
		SQLResult paso2 = executeQuery(dbAvebqa, selectFormat);		
		
		boolean av2 = paso2.isEmpty();
		
		//if (!av2) {
			testCase.addQueryEvidenceCurrentStep(paso2);
			
			SQLResult f2 = dbAvebqa.executeQuery(selectFormat2);
			testCase.addQueryEvidenceCurrentStep(f2);
			
			SQLResult f3 = dbAvebqa.executeQuery(selectFormat3);
			testCase.addQueryEvidenceCurrentStep(f3);
			
			SQLResult f4 = dbAvebqa.executeQuery(selectFormat4);
			testCase.addQueryEvidenceCurrentStep(f4);
		//} 

		System.out.println(av2);		
		assertFalse(av2, "No se obtiene información de la consulta en la tabla " + data.get("tabla"));
		
		 
		/* PASO 5 y 6 *********************************************************************/
			
		addStep("Ejecutar el servicio " + data.get("servicio"));
		
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
		    
		/* PASO 7 *********************************************************************/		    
		 
		//String status1 = "S";
		//run_id = "2174844561";
		
	    addStep("Verificar el estatus con el cual fue terminada la ejecución de la interface en la tabla WM_LOG_RUN del usuario WMLOG.");		
		
		boolean validateStatus = status.equals(status1);
		System.out.println("VALIDACION DE STATUS = S - " + validateStatus);
	
		testCase.addQueryEvidenceCurrentStep(is);					
		assertFalse(!validateStatus, "La ejecución de la interfaz no fue exitosa");
		
		
		/* PASO 8 *********************************************************************/		    
	    addStep("Validar en la tabla  WMLOG.WM_LOG_ERROR  no se genere errores");		

		String error = String.format(consultaError1, run_id);
		String error1 = String.format(consultaError2, run_id);
		String error2 = String.format(consultaError3, run_id);

		SQLResult errorr = dbLog.executeQuery(error);
		boolean emptyError = errorr.isEmpty();
		
		testCase.addQueryEvidenceCurrentStep(errorr);
		
		SQLResult errorIS = dbLog.executeQuery(error1);
		testCase.addQueryEvidenceCurrentStep(errorIS);
		
		SQLResult errorIS2 = dbLog.executeQuery(error2);
		testCase.addQueryEvidenceCurrentStep(errorIS2);
		
		if (!emptyError) {
			testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");			
		}
	

		assertFalse(!emptyError, "Se obtiene información de la consulta en la tabla WM_LOG_ERROR");
		

		/* PASO 9 *********************************************************************/
		
		addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla WM_LOG_THREAD con STATUS = 'S'");		
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);		
		String consultaTemp6 = String.format(consultaThreads, run_id);	
		System.out.println(consultaTemp6);		
		SQLResult paso6 = executeQuery(dbLog, consultaTemp6);	
		
		boolean validacion6 = paso6.isEmpty();
		testCase.addQueryEvidenceCurrentStep(paso6);

		if (!validacion6) {
			String estatusThread = paso6.getData(0, "Status");			
			validacion6 = estatusThread.equals(status);
			System.out.println(validacion6);
			
			String consultaTemp6pt2 = String.format(consultaThreads2, run_id);	
			paso6 = executeQuery(dbLog, consultaTemp6pt2);	
			testCase.addQueryEvidenceCurrentStep(paso6);

			validacion6 = !validacion6;		//Como se reutiliza el mismo booleano que el isEmpty lo regreso al mismo estado que estaba antes del if
		}					
		
		assertFalse(validacion6, "No se obtiene información o Thread con STATUS diferente a 'S' de la consulta en la tabla WM_LOG_THREAD");
		
		
		/* PASO 10, 11, 12 y 13 *********************************************************************/
		
		addStep("Validar que la tabla cuente con los siguientes campos para WM en la tabla " + data.get("tabla"));		
		
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		String selectFormat10 = String.format(selectCamposID, data.get("variable1"));
		System.out.println(selectFormat10);
		SQLResult paso10 = executeQuery(dbAvebqa, selectFormat10);		
		
		boolean av10 = paso10.isEmpty();
		testCase.addQueryEvidenceCurrentStep(paso10);	
		
		if (!av10) {
			
		    addStep("Validar  campos  WM_STATUS");
		    testCase.addQueryEvidenceCurrentStep(paso10);
			String statusWM = paso10.getData(0, "wm_status");			
			boolean wm = statusWM.equals(wm_status);
			if (wm) {
				testCase.addTextEvidenceCurrentStep("Se muestra  WM_STATUS = '"+wm_status+"'");
			}
			System.out.println("WM Status: " + statusWM);
			assertFalse(!wm, "La respuesta no tiene el WM STATUS esperado.");			
			
			addStep("Validar  campos  WM_MENSAJE");	
			testCase.addQueryEvidenceCurrentStep(paso10);
			String wmMensaje = paso10.getData(0, "WM_MENSAJE");				
			boolean wm2;
			//if (wmMensaje == null) {
			if (wmMensaje.equals("Proceso Exitoso")) {
				wm2 = true;
				testCase.addTextEvidenceCurrentStep("Se muestra el mensaje de Proceso Exitoso");
			}else {
				wm2 = false;			
			}
			System.out.println("WM Mensaje: " + wmMensaje);
			//assertFalse(!wm2, "Se obtiene información en el campo WM_MENSAJE.");
			assertTrue(wm2, " No se obtiene el mensaje esperado en el campo WM_MENSAJE.");
			
			addStep("Validar  campos  WM_FECHA_ENVIO");	
			testCase.addQueryEvidenceCurrentStep(paso10);
			SQLResult queryDate = executeQuery(dbAvebqa, selectDate);
			String date = queryDate.getData(0, "TRUNC(SYSDATE)");				
			String wmfecha = paso10.getData(0, "WM_FECHA_ENVIO");			
			boolean wmDate = wmfecha.equals(date);
			if (wmDate) {
				testCase.addTextEvidenceCurrentStep("Se muestra el campo WM_FECHA_ENVIO con la fecha del envio");
			}
			System.out.println("WM fecha envio: " + wmfecha);
			assertFalse(!wmDate, "La respuesta no tiene el WM_FECHA_ENVIO esperado.");	
			
			addStep("Validar  campos  WM_RUN_ID");	
			testCase.addQueryEvidenceCurrentStep(paso10);
			String wmRun = paso10.getData(0, "WM_RUN_ID");			
			boolean wmID = wmRun.equals(run_id);
			if (wmID) {
				testCase.addTextEvidenceCurrentStep("Se muestra el run_id que se muestra en la tabla  WMLOG.WM_LOG_RUN");
			}
			System.out.println("WM RUN ID: " + wmRun);
			assertFalse(!wmID, "La respuesta no tiene el WM_RUN_ID esperado.");	
		} 


		assertFalse(av10, "No se obtiene información de la consulta en la tabla " + data.get("tabla"));
		
	}
	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Poder enviar la información de facturación  mediante la ejecución de una interfaz hacía Xpertal,la información será tomada de las tablas de paso de EBS.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
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
