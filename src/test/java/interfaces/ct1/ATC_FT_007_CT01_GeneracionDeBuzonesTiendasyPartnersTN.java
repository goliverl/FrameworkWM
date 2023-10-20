package interfaces.ct1;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import static org.testng.Assert.assertTrue;
import org.json.JSONObject;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;


public class ATC_FT_007_CT01_GeneracionDeBuzonesTiendasyPartnersTN extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_007_CT01_GeneracionDeBuzonesTiendasyPartnersTN_test(HashMap<String, String> data) throws Exception {
		
		
		
		/*  Este script cubre los siguientes casos de prueba: (A excepcion de los pasos que estan comentados).
		  -Verificar el procesamiento de la interfaz - Crear el Buzon de la Plaza y Tienda (runBuzon).
          
		
/* Utilerias *********************************************************************/		
		
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbCNT = new SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		
		
	
/**
* Variables ******************************************************************************************
* 
*/
	    //Paso 1
		String ValidaSatusL = "SELECT ID, CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON\r\n" + 
				"   FROM WMUSER.WM_BUZONES_T_TIENDAS \r\n" + 
				" WHERE WM_STATUS_BUZON = 'L' \r\n" + 
				"    AND CR_PLAZA = '" + data.get("plaza") +"' \r\n" + 
				"    AND CR_TIENDA = '" + data.get("tienda") +"'\r\n" + 
				"  ORDER BY CREATION_DATE";

			      
	   //Paso 3
						      
	String  tdcIntegrationServerFormat ="select * from (SELECT run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "+
		      "FROM WMLOG.WM_LOG_RUN " +
		      "WHERE INTERFACE LIKE '%CT01%' " +
		      "AND START_DT >= TRUNC(SYSDATE) "  +
		      "AND STATUS = 'S' "+
		     " ORDER BY 1 DESC) where rownum <=1"; 
		      
	 //Paso 4
		      
		   
		      String qry_threads1 = "SELECT THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS FROM WMLOG.WM_LOG_THREAD " + 
						"WHERE PARENT_ID = '%s'";
				
				String qry_threads2 = "SELECT  ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 FROM WMLOG.WM_LOG_THREAD " + 
						"WHERE PARENT_ID = '%s'";
				
	//Paso 5
				String ValidaError= "SELECT ERROR_ID,RUN_ID,ERROR_DATE,ERROR_CODE "
						+ "FROM WMLOG.WM_LOG_ERROR "
						+ "WHERE RUN_ID='%s'";
				
				
				
				String ValidaSatusE= "SELECT ID, CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON, WM_FECHA_PROC\r\n" + 
						"   FROM WMUSER.WM_BUZONES_T_TIENDAS \r\n" + 
						"WHERE WM_STATUS_BUZON = 'E' \r\n" + 
						"     AND CR_PLAZA = '" + data.get("plaza") +"'\r\n" + 
						"     AND CR_TIENDA = '" + data.get("tienda") +"'\r\n" + 
						"     AND TRUNC(WM_FECHA_PROC) = TRUNC(SYSDATE)\r\n" + 
						"     AND WM_RUN_ID = '%s'\r\n" + 
						" ORDER BY CREATION_DATE ";
	 
		
		      
		      
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
//**********************************************Paso 1	****************************************************************************
	
		addStep("Verificar que existan  la plaza y la tienda en la tabla WM_BUZONES_T_TIENDAS con WM_STATUS_BUZON = 'L'");
		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		System.out.println(ValidaSatusL);
		
		SQLResult ValidaValidaSatusL_Res = executeQuery(dbCNT, ValidaSatusL);
		
		boolean validaStatusL = ValidaValidaSatusL_Res.isEmpty();
		
			if (!validaStatusL) {
		
			testCase.addBoldTextEvidenceCurrentStep("SE Verifico que existe la plaza y la tienda en la tabla WM_BUZONES_T_TIENDAS con WM_STATUS_BUZON = 'L'");
			
						} 
		testCase.addQueryEvidenceCurrentStep(ValidaValidaSatusL_Res);
		System.out.println(validaStatusL);

//		assertFalse(validaStatusL, "No se encontro registro de la plaza y tienda");
		

		
		
//**********************************************Paso 2	****************************************************************************** 
		
		addStep("Ejecutar el JOB runCT1Buzon desde Control M para la ejecucion del job runCT1Buzon ");
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		
		JSONObject obj = new JSONObject(data.get("job"));

		testCase.addBoldTextEvidenceCurrentStep("Jobs en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
		//testCase.addPaso("Paso con addPaso");
		testCase.addBoldTextEvidenceCurrentStep("Login");
		u.get(data.get("server"));
		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn(); 

		//Buscar del job
		testCase.addBoldTextEvidenceCurrentStep("Inicio de job ");
		ControlM control = new ControlM(u, testCase, obj);
		boolean flag = control.searchJob();
		assertTrue(flag);
		
		//Ejecucion
		String resultado = control.executeJob();
		System.out.println("Resultado de la ejecucion -> " + resultado);

		u.hardWait(30);
		
		//Valor del output 
		

		String Res2 = control.getNewStatus();
		

		System.out.println ("Valor de output getNewStatus:" +Res2);
		
		
		String output = control.getOutput();
		System.out.println ("Valor de output control:" +output);
		
		testCase.addTextEvidenceCurrentStep("Status de ejecucion: "+Res2);
		testCase.addTextEvidenceCurrentStep("Output de ejecucion: "+output);
		//Validacion del caso
		Boolean casoPasado = false;
		if(Res2.equals("Ended OK")) {
		casoPasado = true;
		}		
		
		control.closeViewpoint();
		u.close();
//		assertTrue(casoPasado);
		//assertNotEquals("Failure",resultado);

//*******************************************************Paso 3************************************************************************
		
		addStep("Validar que la interface haya finalizado correctamente en el WMLOG");
		
		System.out.println(tdcIntegrationServerFormat);
		
		SQLResult ValidaStatusS = executeQuery(dbLog, tdcIntegrationServerFormat);
		String run_id="";
		boolean validaStatus = ValidaStatusS.isEmpty();
		
			if (!validaStatus) {
		
			run_id=ValidaStatusS.getData(0, run_id);
			System.out.println("run_id: "+run_id);
						} 
		testCase.addQueryEvidenceCurrentStep(ValidaStatusS);
		System.out.println(validaStatus);

//		assertFalse(validaStatus, "Error en la ejecucion de la interfaz");
		
//**************************************************Paso 4 **************************************************************************
		
		addStep("Verificar que los threads de la interfaz finalizaron correctamente.");

		String consulta1 = String.format(qry_threads1, run_id);
		
		System.out.println("CONSULTA THREAD " + consulta1);
		
		SQLResult consultaThreads = dbLog.executeQuery(consulta1);
	
		boolean threads = consultaThreads.isEmpty();
		if (!threads) {

			testCase.addQueryEvidenceCurrentStep(consultaThreads);
		}
		System.out.println(threads);
		// .-----------Segunda consulta
		String consulta2 = String.format(qry_threads2, run_id);
		SQLResult consultaThreads2 = dbLog.executeQuery(consulta2);
		boolean threads1 = consultaThreads2.isEmpty();
		if (!threads1) {
			testCase.addQueryEvidenceCurrentStep(consultaThreads);
		}
		testCase.addQueryEvidenceCurrentStep(consultaThreads);
		System.out.println(threads1);
//		assertFalse("No se generaron threads en la tabla", threads1);
		
		
		
		
//		********************************************Paso 5****************************************************************
		addStep(" Validar que no se hayan generado errores de la ejecuci�n de la interface CT01 en la tabla WM_LOG_ERROR  de la BD *FCWMLQA*");
				
		
		String formatquery = String.format(ValidaError, run_id);
				System.out.println(formatquery);
				
				SQLResult ValidaErrorExec = executeQuery(dbLog,formatquery );
				boolean validaStatusr = ValidaErrorExec.isEmpty();
				
					if (validaStatusr) {
				
					testCase.addBoldTextEvidenceCurrentStep("OK. No se encontraron errores de ejecucion");
				
					} 
				testCase.addQueryEvidenceCurrentStep(ValidaErrorExec);
				System.out.println(validaStatusr);
		
//				assertTrue(validaStatusr, "se encontro Error en la ejecucion de la interfaz");
//*******************************************Script 1************************************************************************************
		
	/*	Verificar que el directorio fue creado en el filesystem,
		 
		PATH=/u01/posuser/FEMSA_OXXO/POS/[PLAZA]/[TIENDA]/ DIRECTORIO=backup,working,recovery,outbox,duplicate.*/
		
	
		
		

//**************************************************Paso 5 ***************************************************************************		
		
addStep("Verificar que se actualice el registro de la tabla WM_BUZONES_T_TIENDAS, WM_STATUS_BUZON = E,WM_RUN_ID = [WM_LOG_RUN.RUN_ID], WM_FECHA_PROC = SYSDATE.");

        String ValidaSatusE_format = String.format(ValidaSatusE, run_id);
		
		System.out.println(ValidaSatusE_format);
		
		SQLResult ValidaStatusE = executeQuery(dbCNT,ValidaSatusE_format);
		
		boolean validaStatusE = ValidaStatusE.isEmpty();
		
			if (!validaStatusE) {
		
			testCase.addQueryEvidenceCurrentStep(ValidaStatusS);
			
						} 
		
		System.out.println(validaStatusE);

		assertFalse(validaStatusE, "No se actualizo correctamente el registro");
		
		
		 
		
		/*
		 * Solicitar al equipo de soporte L3 (soporteh3cwm@oxxo.com) por medio de un ticket que nos confirme si fue creado el perfil del partner (PLAZA, TIENDA) de TN en My webmethods server.
		 */
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
		return "MTC-FT-001 CT01 Generaci�n de buzones de tiendas y Parthners de Tranding Network a traves de la interface FEMSA_CT01";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_007_CT01_GeneracionDeBuzonesTiendasyPartnersTN_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
	
	
