package interfaces.os1_mx;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.controlm.ControlM;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.FirefoxTest;
import utils.selenium.MEdgeTest;
import utils.selenium.SeleniumUtil;

public class ATC_FT_004_OS1_MX_PagoNominaGeneracionDePoliza extends BaseExecution {	

	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_OS1_MX_PagoNominaGeneracionDePoliza_test(HashMap<String, String> data) throws Exception {
		
/*/**
 * Back Office AVANTE: MTC-FT-017 OS1_MX Envio de pago de nomina y generacion de polizas a traves de la interface OS1_MX
 * Desc:
 * Prueba de regresion  para comprobar la no afectacion en la funcionalidad principal de la interface FEMSA_OS1_MX de avante para 
 * enviar informacion de pólizas de SAP_INBOUND_DOCS hacia SAP_RPY_DETL y contabilizacion de pólizas en GL_INTERFACE, al ser migrada la interface de WM9.9 a WM10.5.
 * @author Marisol Rodriguez
 * @date   2022/07/16
 * 
 * Mtto:
 * @author Jose Onofre
 * @date 02/08/2023
 */		

/** UTILERIA *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA );
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser, GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		
		
		 testCase.setProject_Name("POC WMX86");
			
		 testCase.setPrerequisites(data.get("prerequicitos"));



/** VARIABLES *********************************************************************/

		// Paso 2
		String SelectS = "SELECT t1.id, t1.create_date, t1.status,t2.sid_id, t2.target_sys,target_id  \r\n"
				+ "FROM sapuser.SAP_INBOUND_DOCS t1, sapuser.SAP_PYR_HEADER t2, sapuser.SAP_PYR_DETL t3\r\n"
				+ "WHERE (t1.ID = t2.sid_id "
				+ "AND t2.sid_id = t3.sid_id) "
				+ "AND (t1.status = 'S' "
				+ "AND t2.target_sys = 'O')"
				+ "AND rownum <= 5 " 
				+ "order by CREATE_DATE desc";
				//+ "and t1.CREATE_DATE >= SYSDATE ";
		
		//Paso 4
		String consultaGL1 = "SELECT STATUS, LEDGER_ID, USER_JE_CATEGORY_NAME, SEGMENT1,SEGMENT2, SEGMENT3\r\n"
				+ "FROM APPS.GL_INTERFACE\r\n"
				+ "WHERE GROUP_ID = '%s' \r\n"
				+ "AND UPPER( STATUS ) <> 'NEW'\r\n"
				+ "AND STATUS IS NOT NULL \r\n"
				+ "AND ROWNUM = 1 ";
		
		String consultaGL2 = "SELECT SEGMENT4,SEGMENT7,REFERENCE6,DATE_CREATED,GROUP_ID\r\n"
				+ "FROM APPS.GL_INTERFACE\r\n"
				+ "WHERE GROUP_ID = '%s' \r\n"
				+ "AND UPPER( STATUS ) <> 'NEW'\r\n"
				+ "AND STATUS IS NOT NULL "
				+ "AND ROWNUM = 1\r\n"; 

		String tdcIntegrationServerFormat = "select run_id,interface, start_dt, end_dt, status, server \r\n"
				+ "from WMLOG.WM_LOG_RUN  \r\n"
				+ "WHERE INTERFACE LIKE '%OS1%'\r\n"
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
				
				  
		//Consultas threads
		String consultaTHREAD = "SELECT THREAD_ID, PARENT_ID, NAME,START_DT, END_DT, STATUS  \r\n"
				+ "FROM wmlog.WM_LOG_THREAD \r\n" + 
				" where PARENT_ID= '%s'";
				

		String consultaTHREAD2 = "SELECT ATT1, ATT2,ATT3,ATT4,ATT5 FROM wmlog.WM_LOG_THREAD \r\n"
				+ "where PARENT_ID='%s' ";
				


		// Paso 12
		String validarRPY = "SELECT sod_id, docnum, gsber, journalid, runid \r\n"
				+ "FROM SAPUSER.SAP_RPY_DETL\r\n"
				+ "WHERE journalid = '%s'";

		// Paso 6
		String validarF = "SELECT t1.id, t1.create_date, t1.status,t2.sid_id, t2.target_sys,target_id  \r\n"
				+ "FROM sapuser.SAP_INBOUND_DOCS t1, sapuser.SAP_PYR_HEADER t2, sapuser.SAP_PYR_DETL t3\r\n"
				+ "WHERE (t1.ID = t2.sid_id \r\n"
				+ "AND t1.ID = '%s'\r\n"
				+ "AND t2.sid_id = t3.sid_id)\r\n"
				+ "AND (t1.status = 'F' \r\n"
				+ "AND t2.target_sys = 'O')\r\n"
				+ "and t1.CREATE_DATE >= SYSDATE";
		

//***************************************** PASOS DEL CASO DE PRUEBA *********************************************
				
//**************************************************Paso 1  ******************************************************
		
				addStep("Establecer la conexion a la BD **FCWM6QA**.");
				 
				 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
				 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
				 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCWMQA_NUEVA);
				 
				
							
//***************************************************Paso 2 ******************************************************

		addStep("Validar tener informacion en la tabla SAP_INBOUND_DOCS de SAPUSER de la BD **FCWM6QA** con estatus S.");

		// Primera consulta
		
		System.out.println(SelectS);
		SQLResult SelectS_Result = dbPos.executeQuery(SelectS);		
		String targetID ="";
		String ID ="";
		

		boolean valida_SelectS_Result = SelectS_Result.isEmpty(); // checa que el string contenga datos

		if (!valida_SelectS_Result) {
			
			targetID = SelectS_Result.getData(0, "TARGET_ID");
			System.out.println("SAP_INBOUND_DOCS.TARGET_ID= " + targetID); // imprime el target_id que se usara en la siguiente consulta
			
			ID = SelectS_Result.getData(0, "ID");
			System.out.println("SAP_RPY_DETL.ID= " + ID); // imprime el target_id que se usara en la siguiente consulta
			testCase.addQueryEvidenceCurrentStep(SelectS_Result); // Si no esta vacio, lo agrega a la evidencia
		}
		
		System.out.println(valida_SelectS_Result);
		assertFalse("No se encontro informacion en la tabla SAP_INBOUND_DOCS de SAPUSER ", valida_SelectS_Result); // Si esta vacio, imprime mensaje

//************************************************** Paso 3 *******************************************************
		
		 addStep(" Realizar la conexion de BD **AVEBQA** de EBS.");
		 
		 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_AVEBQA);
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_AVEBQA);

		

//*************************************************** Paso 4 ******************************************************
		
		addStep("Validar la informacion de las pólizas en BD AVEBQA en la tabla GL_INTERFACE con estatus diferente de NEW.");

		//Parte 1
				String consultaGL1_F = String.format(consultaGL1, targetID);
				
				System.out.println(consultaGL1_F);
				
				SQLResult consultaGL1_R = dbEbs.executeQuery(consultaGL1_F);

				boolean validaConsultaGL1_R  = consultaGL1_R .isEmpty();
				
				if (!validaConsultaGL1_R) {
					
					testCase.addQueryEvidenceCurrentStep(consultaGL1_R);
				}
			
				System.out.println(validaConsultaGL1_R);
		
		//Parte 2
				
				String consultaGL2_F = String.format(consultaGL2, targetID);
				
				System.out.println(consultaGL2_F);
				
				SQLResult consultaGL2_R = dbEbs.executeQuery(consultaGL2_F);

				boolean validaConsultaGL2_R  = consultaGL2_R .isEmpty();
				
				if (!validaConsultaGL2_R) {
					
					testCase.addQueryEvidenceCurrentStep(consultaGL2_R );
				}
			
				System.out.println(validaConsultaGL2_R);
		
				

		assertTrue(validaConsultaGL2_R, "No se encontraron datos de las polizas en BD AVEBQA  en la tabla GL_INTERFACE");
			
		
		
//*************************************************Paso 5  y 6*******************************************************

		addStep("Consultar la ejecucion del Job en Control M, donde debe mostrarse en color verde, que indica que la ejecución se realizo de forma exitosa para el Job OS1_MEX");
		/*
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
		assertFalse(!flag, "No se encontro el Job");
		
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
		
		u.close();
		*/
		
		// +++++++++++++++++++++++
		
		String status = "S";
		// Se obtiene la cadena de texto del data provider en la columna "jobs"
		// Se asigna a un array para poder manejarlo
		JSONArray array = new JSONArray(data.get("cm_jobs"));

		testCase.addTextEvidenceCurrentStep("Ejecución Job: " + data.get("cm_jobs"));
		SeleniumUtil u = new SeleniumUtil(new ChromeTest());
		Control_mInicio cm = new Control_mInicio(u, data.get("cm_user"), data.get("cm_ps"));

		testCase.addTextEvidenceCurrentStep("Login");
		addStep("Login");
		u.get(data.get("cm_server"));
		u.hardWait(50);
		u.waitForLoadPage();
		cm.logOn();

		testCase.addTextEvidenceCurrentStep("Inicio de job");
		JobManagement j = new JobManagement(u, testCase, array);
		u.hardWait(5);
		System.out.println(data.get("Espera 5 sec, antes de Resultado Ejecucion"));
		String resultadoEjecucion = j.jobRunner();

		// Abrir la herramienta de control M para validar que la ejecución del job haya
		// sido exitosa
		testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
		System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);

		assertEquals(resultadoEjecucion, "Ended OK");
		u.close();
		
		
//***************************************** Paso 7 **************************************************************
				addStep("Establecer la conexion a la BD **FCWMLQA**.");
				
				 
				 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
				 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA_WMLOG);
				 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCWMLQA_WMLOG);
				 			
		
//***************************************************Paso 8 *******************************************************
		

		addStep("Validar la correcta ejecucion de la interface OS1_MX en la tabla WM WM_LOG_RUN de la BD **FCWML6QA**");

		SQLResult ISF_Result = executeQuery(dbLog, tdcIntegrationServerFormat);

		String STATUS = ISF_Result.getData(0, "STATUS"); //Se obtiene el status de la ejecucion
		
		String RUN_ID = ISF_Result.getData(0, "RUN_ID");
		
		boolean validateStatus = STATUS .equals(status);
		
		System.out.println("Status es S = "+ validateStatus );
		
		
		if (validateStatus) {

			testCase.addQueryEvidenceCurrentStep(ISF_Result);
		}
		

		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");
		
//************************************************** Paso 9 **************************************************************

		addStep(" Validar la correcta ejecucion de los Threads lanzados por la interface OS1_MX en la tabla WM_LOG_THREAD de la BD **FCWML6QA**.");

			//Parte 1
			String consultaTreads1 = String.format(consultaTHREAD, RUN_ID );
			
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
			
		    assertFalse("No se generaron threads ",validaThreads2);		

//*************************************************** Paso 10 **************************************************
		    
		    addStep("Realizar la siguiente consulta para verificar que no se encuentre ningun error presente en la ejecución de la interfaz OS1_MX en la tabla WM_LOG_ERROR de BD **FCWML6QA**.");

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
			
		
		
//******************************************************* Paso 11 *******************************************************	

		addStep(" Validar que la informacion de la poliza se inserta correctamente en SAP_RPY_DETL en SAPUSER de la BD de **FCWM6QA**.");

		String validarRPY_format = String.format(validarRPY, ID);
		System.out.println(validarRPY_format);
		SQLResult validarRPY_format_Result = dbPos.executeQuery(validarRPY_format);

		boolean validarRPY_boolean =validarRPY_format_Result.isEmpty(); // checa que el string contenga datos

		if (!validarRPY_boolean) {
			
			testCase.addQueryEvidenceCurrentStep(validarRPY_format_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(validarRPY_boolean);
		assertFalse("No se encontraron registros ", validarRPY_boolean); // Si esta vacio, imprime mensaje
		
		

//*************************************************Paso 12 *******************************************************************

		addStep(" Validar que se actualiza correctamente la informacion de la tabla SAP_INBOUND_DOCS en BD FCWM6QA con estatus F. ");

		String validarF_format = String.format(validarF, ID);
		System.out.println(validarF_format);
		SQLResult validarF_Result = dbPos.executeQuery(validarF_format);

		boolean validarF_boolean = validarF_Result.isEmpty(); // checa que el string contenga datos

		if (!validarF_boolean) {
			testCase.addQueryEvidenceCurrentStep(validarF_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(validarF_boolean);
		assertFalse("No se encontraron registros ", validarF_boolean); // Si esta vacio, imprime mensaje

	}


	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "OS1_MX Envio de pago de nomina y generacion de polizas a traves de la interface OS1_MX";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de automatizacion";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
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
}
