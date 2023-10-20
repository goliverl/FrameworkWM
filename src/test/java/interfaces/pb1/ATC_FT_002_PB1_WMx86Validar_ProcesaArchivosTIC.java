package interfaces.pb1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;
import org.json.JSONObject;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;


public class ATC_FT_002_PB1_WMx86Validar_ProcesaArchivosTIC extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PB1_WMx86Validar_ProcesaArchivosTIC_test(HashMap<String, String> data) throws Exception { 
		
		
		/*Back Office Mexico: MTC-FT-024 PB1 Procesar al menos 10 archivos TIC de tickets de tienda a través de la interface FEMSA_PB1
 * Desc:
 * Prueba de regresión  para comprobar la no afectación en la funcionalidad principal de la interface FEMSA_PB1 para procesar al menos 
 * 10 archivos TIC (Tickets de Tienda) de subida (de WM INBOUND a BI/IMA), al ser migrada la interface de WM9.9 a WM10.5.
 * 
 * NOTA: No aparece el job

   Esta interface permite consultar y procesar  la información de los tickets de las tiendas almacenados en el sistema del POS, y
    enviarla al sistema de BI.
 * @author Marisol Rodriguez
 * @date   2022/07/16*/
		
		
/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG,GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG );
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbIas = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_IAS, GlobalVariables.DB_USER_IAS, GlobalVariables.DB_PASSWORD_IAS);
		utils.sql.SQLUtil dbBi = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_BiTicket, GlobalVariables.DB_USER_BiTicket,GlobalVariables.DB_PASSWORD_BiTicket);
				
/** VARIABLES *********************************************************************/	
		//Paso 2
		String selectPlazas = "SELECT id, cr_plaza, cr_plaza_desc, fch_arranque, ventana_dias, dias_tolerancia\r\n"
				+ "FROM POSUSER.PLAZAS \r\n"
				+ "WHERE FCH_ARRANQUE <= SYSDATE \r\n"
				+ "AND FCH_ARRANQUE IS NOT NULL \r\n"
				+ "AND CR_PLAZA =  '" + data.get("plaza") + "'"; //POSUSER
		//Paso 3
		String selectI = "SELECT ID, PE_ID, status, doc_type, pv_doc_name, received_date \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ "WHERE  RECEIVED_DATE BETWEEN TRUNC(SYSDATE-30) AND SYSDATE\r\n"
				+ "AND STATUS = 'I' \r\n"
				+ "AND SUBSTR(PV_DOC_NAME,4,5) = '" + data.get("plaza") + "'\r\n"
				+ "AND DOC_TYPE = 'TIC'\r\n"
				+ "order by received_date desc";
		
		//Paso 4
		String consultaPOS_TIC = "SELECT * FROM  POSUSER.POS_TIC where PID_ID = '%s'";
		
		//Paso 5
		String  consultaPOS_TIC_DETL = "SELECT pid_id, item, pv_cve_mvt,pv_fch_mvt\r\n"
				+ "FROM  POSUSER.POS_TIC_DETL\r\n"
				+ "where pid_id ='%s'";

		//Consulta de ejecucion
		String tdcIntegrationServerFormat = "select run_id,interface, start_dt, end_dt, status, server \r\n"
				+ "from WMLOG.WM_LOG_RUN  \r\n"
				+ "WHERE INTERFACE = 'PB1main'\r\n"
				+ "AND  start_dt >= TRUNC(SYSDATE) \r\n"
				+ "AND rownum =1 \r\n"
				+ "ORDER BY START_DT DESC";// Consulta para estatus de la ejecucion
		

		// consultas de error
		String consultaError1 = " select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE \r\n"+
					 "from wmlog.WM_LOG_ERROR \r\n" + 
					 "where RUN_ID='%s' \r\n" +
					 "and rownum <=1"; // dbLog
				
		String consultaError2 = " select description,MESSAGE \r\n" +
					 "from wmlog.WM_LOG_ERROR \r\n" +
					 "where RUN_ID='%s' \r\n" +
					 "and rownum <= 1"; // dbLog
				
				  
				//Consultas de threrads
		String consultaTHREAD = "SELECT THREAD_ID, PARENT_ID, NAME,START_DT, END_DT, STATUS  \r\n"
						+ "FROM wmlog.WM_LOG_THREAD \r\n" + 
						" where PARENT_ID= '%s'";


		String consultaTHREAD2 = "SELECT ATT1, ATT2,ATT3,ATT4,ATT5 FROM wmlog.WM_LOG_THREAD \r\n"
						+ "where PARENT_ID='%s'";
						
		//Paso 8
		String selectE = "SELECT id,pe_id,pv_doc_name,doc_type,status,partition_date \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS \r\n" 
				+ "WHERE ID = '%s' \r\n"
				+ "AND DOC_TYPE = 'TIC' \r\n"
				+ "AND STATUS = 'E' \r\n" 
				+ "AND SUBSTR(PV_DOC_NAME,4,5) = '" + data.get("plaza") + "' \r\n" 
				+ "and SUBSTR(PV_DOC_NAME,9,5) = '" + data.get("tienda") + "'";
		
	
		
		String selectTempTicket= "SELECT DISTINCT VT_CR,VT_CR_PLAZ,FECHA_CARGA \r\n"
				+ " FROM TEMP_TICKET \r\n"
				+ "WHERE TRUNC(FECHA_CARGA) = TRUNC(SYSDATE) \r\n"
				+ "AND VT_CR_PLAZ = '" + data.get("plaza") + "'";
		
		SeleniumUtil u;
		PakageManagment pok;
		
		String status = "S"; // status exitoso
		String user = data.get("user");
		String server = data.get("server");
		String searchedStatus = "R";
		 String ps = PasswordUtil.decryptPassword(data.get("ps"));
		
		
		 testCase.setProject_Name("POC WMX86");
			
		 testCase.setPrerequisites(data.get("prerequicitos"));



/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
		 
//**************************************** Paso 1  *******************************************************************
			
			addStep("Establecer la conexión a la BD **FCWM6QA**.");
			 
			 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
			 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
			 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCWMQA_NUEVA);
			 
			
				 			
//***************************************** Paso 2 *******************************************************************	

		addStep(" Verificar que exista un registro de las PLAZAS en FCWM6QA, con FCH_ARRANQUE menor a la fecha actual, para las plazas indicadas.");

		System.out.println(selectPlazas);
		
		SQLResult selectPlazas_Result = dbPos.executeQuery(selectPlazas);		
		

		boolean valida_selectPlazas = selectPlazas_Result.isEmpty(); // checa que el string contenga datos

		if (!valida_selectPlazas) {
			
			testCase.addQueryEvidenceCurrentStep(selectPlazas_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(valida_selectPlazas);
		
		assertFalse("No se encontraron registros de las PLAZAS en FCWM6QA", valida_selectPlazas); // Si esta vacio, imprime mensaje
		
		
//****************************************** Paso 3 *********************************************************************

		addStep("Validar que existan los archivos en la tabla POS_INBOUND_DOCS  con STATUS = I y DOC_TYPE = TIC en la BD FCWM6QA.");

		System.out.println(selectI);
		SQLResult selectI_result = dbPos.executeQuery(selectI);
		
		String id= "";

		boolean validaSelectId = selectI_result.isEmpty(); // checa que el string contenga datos

		if (!validaSelectId) {
			
			id = selectI_result.getData(0, "ID"); ///Se mandara en el pid_id de la siguiente consulta.
			testCase.addQueryEvidenceCurrentStep(selectI_result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(validaSelectId);
		assertFalse("No se encontraron los archivos.", validaSelectId); // Si esta vacio, imprime mensaje
		
//****************************************** Paso 4 *********************************************************************

				addStep(" Validar que muestra los registros que lleva cada archivo a procesar en la tabla POS_TIC de la BD FCWM6QA.");

				String consultaPOS_TIC_format = String.format(consultaPOS_TIC, id);
				
				System.out.println(consultaPOS_TIC_format);
				
				SQLResult consultaPOS_TIC_result = dbPos.executeQuery(consultaPOS_TIC_format);
			
				boolean validaConsultaPOS_TIC_format = consultaPOS_TIC_result.isEmpty(); // checa que el string contenga datos

				if (!validaConsultaPOS_TIC_format) {
					
								
					testCase.addQueryEvidenceCurrentStep(consultaPOS_TIC_result); // Si no esta vacio, lo agrega a la evidencia
				}

				System.out.println(validaConsultaPOS_TIC_format);
				
				assertFalse("No se encontraron los registros que lleva cada archivo a procesar en la tabla POS_TIC", validaConsultaPOS_TIC_format); // Si esta vacio, imprime mensaje
		
//****************************************** Paso 5 *********************************************************************

				addStep("Validar que muestra el detalle de los registros que lleva cada archivo a procesar en la tabla POS_TIC_DETL de la BD FCWM6QA.");

				String consultaPOS_TIC_DETL_format = String.format(consultaPOS_TIC_DETL, id);
				
				System.out.println(consultaPOS_TIC_DETL_format);
				
				SQLResult consultaPOS_TIC_DETL_result = dbPos.executeQuery(consultaPOS_TIC_DETL_format);
			
				boolean validaConsultaPOS_TIC_DETL = consultaPOS_TIC_DETL_result.isEmpty(); // checa que el string contenga datos

				if (!validaConsultaPOS_TIC_DETL) {
					
					testCase.addQueryEvidenceCurrentStep(consultaPOS_TIC_DETL_result); // Si no esta vacio, lo agrega a la evidencia
				}

				System.out.println(validaConsultaPOS_TIC_DETL);
				
				assertFalse("No se encontro el detalle de los registros que lleva cada archivo a procesar en la tabla POS_TIC_DETL", validaConsultaPOS_TIC_DETL); // Si esta vacio, imprime mensaje
					
		
//**********************************************Paso 6 y 7 ********************************************************************	

	/*	addStep("Validar la ejecución del Job en Control M, donde debe mostrarse en color verde, que indica que la ejecución se realizó de forma exitosa para el Job PB1.");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		u.get(data.get("server"));
	
		
		String status = "S";
		
		
		JSONObject obj = new JSONObject(data.get("job"));

		testCase.addTextEvidenceCurrentStep("Job en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
		
		testCase.addTextEvidenceCurrentStep("Login");
		
		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn(); 

		//Buscar del job
		testCase.addTextEvidenceCurrentStep("Inicio de job ");
		ControlM control = new ControlM(u, testCase, obj);
		boolean flag = control.searchJob();
		assertFalse(!flag);
		
		//Ejecucion
		String resultado = control.executeJob();
		testCase.addTextEvidenceCurrentStep( resultado);
		System.out.println("Resultado de la ejecucion -> " + resultado);

		//Valor del output 
		testCase.addTextEvidenceCurrentStep("Output: " + control.getOutput());
		System.out.println ("Valor de output :" +control.getOutput());
		
		//Validacion del caso
		Boolean casoPasado = true;
		
		if(resultado.equals("Failure")) {
			
		casoPasado = false;
		
		System.out.println("La ejecucion del Job fue exitosa");
		
		}		
		assertTrue(casoPasado, "La ejecucion del job fallo");

		control.closeViewpoint(); 
		
		u.close();*/
				
				addStep("Invocar el servicio FEMSA_PB1.Pub:run mediante la ejecución del JOB PB1.");

				u = new SeleniumUtil(new ChromeTest(), true);
				pok = new PakageManagment(u, testCase);
				
				System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
				String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
				u.get(contra);

				String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));		
				System.out.println("Respuesta dateExecution " + dateExecution);
				System.out.println(tdcIntegrationServerFormat);
				SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
				String run_id = is.getData(0, "RUN_ID");
				String status1 = is.getData(0, "STATUS");
				System.out.println("RUN_ID = " + run_id + "\t Status: " + status1 );

				boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R

				while (valuesStatus) {			
					status1 = is.getData(0, "STATUS");
					run_id = is.getData(0, "RUN_ID");
					valuesStatus = status1.equals(searchedStatus);
					u.hardWait(2);			
				}

				boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S		

				if (!successRun) {
					String error = String.format(consultaError1, run_id);
					String error1 = String.format(consultaError2, run_id);
					
			
					SQLResult errorr = dbLog.executeQuery(error);
					boolean emptyError = errorr.isEmpty();
					
					if (!emptyError) {
						testCase.addTextEvidenceCurrentStep("Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
						testCase.addQueryEvidenceCurrentStep(errorr);
						
						SQLResult errorIS = dbLog.executeQuery(error1);
						testCase.addQueryEvidenceCurrentStep(errorIS);
						
						
					}
				}	
		
//********************************************** Paso 8 ****************************************************************	
		
		addStep(" Validar que se haya actualizado los estatus de los archivos procesados,  con STATUS = 'E' y el TARGET_ID en la tabla POS_INBOUNDS_DOCS de la BD FCWM6QA.");

		String selectE_Format = String.format(selectE, id);
		
		System.out.println(selectE_Format);
		
		SQLResult selectE_Result = dbPos.executeQuery(selectE_Format);	

		boolean validaSelectE = selectE_Result.isEmpty(); // checa que el string contenga datos

		if (!validaSelectE) {
			testCase.addQueryEvidenceCurrentStep(selectE_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(validaSelectE);
		assertFalse("No se encontraron registros STATUS = E en la tabla POS_INBOUND_DOCS", validaSelectE); // Si esta vacio, imprime mensaje
		
//************************************************* Paso 9 *****************************************************************
		
		 addStep("Realizar conexión a la BD FCTICQA.FEMCOM.NET de BI.");
		 
		 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_BiTicket);
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_BiTicket);

//************************************************Paso 10 *******************************************************************	 
		 
			addStep("Validar que los datos se hayan insertado en la tabla TEMP_TICKET de la BD FCTICQA.");
	
			System.out.println(selectTempTicket);
			
			SQLResult selectTempTicket_Result = dbBi.executeQuery(selectTempTicket);	
			
			boolean selectTempTicket_valida = selectTempTicket_Result.isEmpty(); // checa que el string contenga datos
	
			if (!selectTempTicket_valida) {
				
				testCase.addQueryEvidenceCurrentStep(selectTempTicket_Result); // Si no esta vacio, lo agrega a la evidencia
		     }
	
			System.out.println(selectTempTicket_valida);
			
			assertFalse("No se encontraron registros en  la tabla TEMP_TICKET", selectTempTicket_valida); // Si esta vacio, imprime mensaje	
			
//**********************************************Paso 11 *****************************************************************
			addStep("Establecer la conexión a la BD **FCWMLQA**.");
			 
			 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
			 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA);
			 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCWMLQA);
			 	

//********************************************** Paso 12 ****************************************************************
			
	    addStep("Validar la correcta ejecución de la interface PB1 en la tabla WM_LOG_RUN de BD FCWMLQA");		
		
	    SQLResult ISF_Result = executeQuery(dbLog, tdcIntegrationServerFormat);

		String STATUS = ISF_Result.getData(0, "STATUS"); //Se obtiene el status de la ejecucion
		
		String RUN_ID = ISF_Result.getData(0, "RUN_ID");
		
		boolean validateStatus = STATUS .equals(status);
		
		System.out.println("Status es S = "+ validateStatus );
		
		
		if (validateStatus) {

			testCase.addQueryEvidenceCurrentStep(ISF_Result);
		}
		

		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");
		
//************************************************Paso 13  *****************************************************************
		
		addStep("Validar la correcta ejecución de los Threads lanzados por la interface PB1 en la tabla WM_LOG_THREAD  de BD FCWMLQA.");		
		
		//Parte 1
				String consultaTreads1 = String.format(consultaTHREAD, RUN_ID);
				
				System.out.println(consultaTreads1);
				
				SQLResult consultaTreads1_R = dbLog.executeQuery(consultaTreads1);

				boolean validaThreads1 = consultaTreads1_R.isEmpty();
				
				if (!validaThreads1) {
					
					testCase.addQueryEvidenceCurrentStep(consultaTreads1_R);
				}
			
				System.out.println(validaThreads1);
				
				// Parte 2

				String consultaTreads2 = String.format(consultaTHREAD2, RUN_ID);
				
				System.out.println(consultaTreads2);
				
				SQLResult consultaTreads2_R = dbLog.executeQuery(consultaTreads2);

				boolean validaThreads2 = consultaTreads2_R.isEmpty();
				
				if (!validaThreads2) {
					
					testCase.addQueryEvidenceCurrentStep(consultaTreads2_R);
				}

				System.out.println(validaThreads2);
				
//**************************************************Paso 14 *********************************************************
				
				 addStep("Realizar la siguiente consulta para verificar que no se encuentre ningún error presente en la ejecución de la interfaz  dentro de la tabla WM_LOG_ERROR ");

					//Parte 1
					String consultaError1_f = String.format(consultaError1, RUN_ID);
					
					System.out.println(consultaTreads1);
					
					SQLResult consultaError1_R = dbLog.executeQuery(consultaError1_f);

					boolean validaConsultaError1_f = consultaError1_R.isEmpty();
					
					if (validaConsultaError1_f) {
						
						testCase.addBoldTextEvidenceCurrentStep("No se registraron errores en la ejecucion");
						testCase.addQueryEvidenceCurrentStep(consultaError1_R);
						
					} else {
						
						testCase.addQueryEvidenceCurrentStep(consultaError1_R);
						
					}
				
					System.out.println(validaConsultaError1_f);
					
					
					//Parte 2
					String consultaError2_f = String.format(consultaError2, RUN_ID);
							
					System.out.println(consultaTreads2);
							
					SQLResult consultaError2_R = dbLog.executeQuery(consultaError2_f);

					boolean validaConsultaError2_f = consultaError2_R.isEmpty();
							
					if (validaConsultaError2_f) {
						
						testCase.addBoldTextEvidenceCurrentStep("No se registraron errores en la ejecucion");		
						testCase.addQueryEvidenceCurrentStep(consultaError2_R);
					} else {
						
						testCase.addQueryEvidenceCurrentStep(consultaError2_R);
					}
						
					System.out.println(validaConsultaError2_f);
					assertTrue("Se genero un error en la ejecucion. ",validaConsultaError2_f);
		
		

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "ATC-FT-024 PB1 Procesar al menos 10 archivos TIC de tickets de tienda a través de la interface FEMSA_PB1";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PB1_WMx86Validar_ProcesaArchivosTIC_test";
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