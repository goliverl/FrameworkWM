package interfaces.pb12;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;
import org.json.JSONObject;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ATC_FT_002_PB12_WMx86ProcesaArchivoRPA extends BaseExecution {	
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PB12_WMx86ProcesaArchivoRPA_test(HashMap<String, String> data) throws Exception {
		
		/*Back Office Mexico: MTC-FT-017 PB12 Procesar archivo RPA de resguardo de pagares a traves de la interface FEMSA_PB12
 * Desc:
 * Prueba de regresion  para comprobar la no afectacion en la funcionalidad principal de la interface FEMSA_PB12 para procesar archivos RPA (Resguardo de Pagares) 
 * de subida (de WM INBOUND a BI Portales), al ser migrada la interface de WM9.9 a WM10.5
 * @author 
 * @date   2022/07/16*/
		

/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbLOG = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG,GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG );
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbCNT = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);

		
				
/** VARIABLES *********************************************************************/	

		//Paso 1 
		String infoPendStatusI = "SELECT B.PE_ID,B.STATUS, B.PV_DOC_NAME\r\n"
				+ "FROM POSUSER.POS_ENVELOPE A, POSUSER.POS_INBOUND_DOCS B  \r\n"
				+ "WHERE B.PE_ID = A.ID \r\n"
				+ "AND B.DOC_TYPE = 'RPA' \r\n"
				+ "AND B.STATUS  = 'I'  \r\n"
				+ "AND A.PV_CR_PLAZA =  '" + data.get("plaza") + "' \r\n"
				+ "AND A.PV_CR_TIENDA = '" + data.get("tienda") + "' \r\n"
				+ "AND SUBSTR(B.PV_DOC_NAME,4,5) = A.PV_CR_PLAZA \r\n"
				+ "AND SUBSTR(B.PV_DOC_NAME,9,5) = A.PV_CR_TIENDA \r\n"
				+ "order by B.PARTITION_DATE DESC";
		
		//Paso 7 Consulta threads
		String consultaTHREAD = "SELECT THREAD_ID, PARENT_ID, NAME,START_DT, END_DT, STATUS  \r\n"
				+ "FROM wmlog.WM_LOG_THREAD \r\n" + 
				" where PARENT_ID= '%s'";
				
		String consultaTHREAD2 = "SELECT ATT1, ATT2,ATT3,ATT4,ATT5 FROM wmlog.WM_LOG_THREAD \r\n"
				+ "where PARENT_ID='%s'";
				

		String tdcIntegrationServerFormat = "SELECT run_id, interface, start_dt, end_dt, status, server \r\n"
				+ "FROM wmlog.wm_log_run \r\n"
				+ "WHERE interface = 'PB12' \r\n"
				+ "AND start_dt>=TRUNC(SYSDATE) \r\n"
				+ "AND  ROWNUM <= 1 \r\n"
				+ "ORDER BY start_dt DESC ";

		// consultas de error
		String consultaError1 = " select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE \r\n"+
					 "from wmlog.WM_LOG_ERROR \r\n" + 
					 "where RUN_ID='%s' \r\n" +
					 "and rownum <=1"; // dbLog
				
		String consultaError2 = " select description,MESSAGE \r\n" +
					 "from wmlog.WM_LOG_ERROR \r\n" +
					 "where RUN_ID='%s' \r\n" +
					 "and rownum <= 1"; // dbLog
				
		
		String infoPendStatusE = "SELECT id,pe_id,pv_doc_name,doc_type,status,partition_date \r\n" +
			    "FROM POSUSER.POS_INBOUND_DOCS \r\n" + 
				" WHERE ID = '%s' \r\n" +
			    "AND DOC_TYPE = 'RPA' \r\n" +
			    "AND STATUS = 'E' \r\n" + 
			    "AND SUBSTR(PV_DOC_NAME,4,5) = '" + data.get("plaza") + "' \r\n"
			  + "and SUBSTR(PV_DOC_NAME,9,5) = '" + data.get("tienda") + "'";
		
		String validarXXRPA = "SELECT last_updated_date, nombre_archivo \r\n"
				+ "FROM XXRPA.XXRPA_PAGARE \r\n"
				+ "WHERE CR_PLAZA = '" + data.get("plaza") + "' \r\n"
				+ "AND CR_TIENDA = '" + data.get("tienda") + "' \r\n"
				+ "AND NOMBRE_ARCHIVO = '%s'"
				+ "AND TRUNC(LAST_UPDATED_DATE) = TRUNC(SYSDATE) ";

		
//* **************************** CASO DE PRUEBA ***************************************************************
		
		 testCase.setProject_Name("POC WMX86");
			
		 testCase.setPrerequisites(data.get("prerequicitos"));
						
		
//********************************** Paso 1  *******************************************************************
		
		addStep("Establecer la conexión a la BD **FCWM6QA**.");
		 
		 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCWMQA_NUEVA);
		 
			
//********************************** Paso 2 ********************************************************************

		addStep("Validar que exista la información pendiente de procesar con con STATUS = I y DOC_TYPE = RPA en las tablas POS_ENVELOPE y "
				+ "POS_INBOUND_DOCS para la plaza y tienda correspondientes en la BD de FCWM6QA:");

		System.out.println(infoPendStatusI);
		
		SQLResult infoPendStatusI_Result = dbPos.executeQuery(infoPendStatusI);		
		
		String pe_id = "";

		boolean valida_infoPendStatusI = infoPendStatusI_Result.isEmpty(); // checa que el string contenga datos

		if (!valida_infoPendStatusI ) {
			
			pe_id = infoPendStatusI_Result.getData(0, "PE_ID");
			testCase.addQueryEvidenceCurrentStep(infoPendStatusI_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(valida_infoPendStatusI);
		assertFalse("No se encontraron registros a procesar con STATUS = I y DOC_TYPE = RPA.", valida_infoPendStatusI); // Si esta vacio, imprime mensaje
		
	
		
//************************************Paso 3 y 4 *********************************************************************/	

		addStep(" Ingresar a Control-M con sus respectivas credenciales. Validar que el Job se haya ejecutado exitosamente siguiendo la ruta "
				+ "correspondiente para ubicar al Job.");
		
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
		testCase.addBoldTextEvidenceCurrentStep(status);
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
		
		System.out.println("El caso paso correctamente");
		
		}		
		assertTrue(casoPasado, "El caso fallo");

		control.closeViewpoint(); 
		
		u.close();

	
//************************************* Paso 5 *******************************************************************
				addStep("Establecer la conexión a la BD **FCWMLQA**.");
				 
				 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
				 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA);
				 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCWMLQA);
				 	
		
//************************************** Paso 6 *********************************************************************	

		addStep(" Validar el STATUS = S en la tabla WM_LOG_RUN de la BD FCWML6QA (WMLOG), donde INTERFACE = PB12.");

		SQLResult ISF_Result = executeQuery(dbLOG, tdcIntegrationServerFormat);

		String STATUS = ISF_Result.getData(0, "STATUS"); //Se obtiene el status de la ejecucion
		
		String RUN_ID = ISF_Result.getData(0, "RUN_ID");
		
		boolean validateStatus = STATUS .equals(status);
		
		System.out.println("Status es S = "+ validateStatus );
		
		
		if (validateStatus) {

			testCase.addQueryEvidenceCurrentStep(ISF_Result);
		}
		

		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");
		
//*********************************** Paso 7 **************************************************************

		addStep(" Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interfaz en la tabla WM_LOG_THREAD de WMLOG.");

			//Parte 1
			String consultaTreads1 = String.format(consultaTHREAD, RUN_ID);
			
			System.out.println(consultaTreads1);
			
			SQLResult consultaTreads1_R = dbLOG.executeQuery(consultaTreads1);

			boolean validaThreads1 = consultaTreads1_R.isEmpty();
			
			if (!validaThreads1) {
				
				testCase.addQueryEvidenceCurrentStep(consultaTreads1_R);
			}
		
			System.out.println(validaThreads1);
			
			// Parte 2

			String consultaTreads2 = String.format(consultaTHREAD2, RUN_ID);
			
			System.out.println(consultaTreads2);
			
			SQLResult consultaTreads2_R = dbLOG.executeQuery(consultaTreads2);

			boolean validaThreads2 = consultaTreads2_R.isEmpty();
			
			if (!validaThreads2) {
				
				testCase.addQueryEvidenceCurrentStep(consultaTreads2_R);
			}

			System.out.println(validaThreads2);
			
		    assertFalse("No se generaron threads ",validaThreads2);		
		
//*********************************** Paso 8 *******************************************************************
		    
		    addStep("Realizar la siguiente consulta para verificar que no se encuentre ningún error presente en la ejecución de la interfaz  dentro de la tabla WM_LOG_ERROR:");

			//Parte 1
			String consultaError1_f = String.format(consultaError1, RUN_ID);
			
			System.out.println(consultaTreads1);
			
			SQLResult consultaError1_R = dbLOG.executeQuery(consultaError1_f);

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
					
			SQLResult consultaError2_R = dbLOG.executeQuery(consultaError2_f);

			boolean validaConsultaError2_f = consultaError2_R.isEmpty();
					
			if (validaConsultaError2_f) {
				
				testCase.addBoldTextEvidenceCurrentStep("No se registraron errores en la ejecucion");		
				testCase.addQueryEvidenceCurrentStep(consultaError2_R);
			} else {
				
				testCase.addQueryEvidenceCurrentStep(consultaError2_R);
			}
				
			System.out.println(validaConsultaError2_f);
			assertTrue("Se genero un error en la ejecucion. ",validaConsultaError2_f);
		
//****************************************Paso 9 **********************************************************************	

		addStep("Validar el STATUS = E,  en la tabla POS_INBOUND_DOCS para la plaza y tienda correspondientes con Tipo de documento RPA en la BD POSUSER. ");

		String infoPendStatusE_format = String.format(infoPendStatusE, pe_id);
		
		System.out.println(infoPendStatusE_format);
		
		SQLResult infoPendStatusE_format_Result = dbPos.executeQuery(infoPendStatusE_format);	
		
		String pv_doc_name = "";

		boolean valida_infoPendStatusE_format = infoPendStatusE_format_Result.isEmpty(); // checa que el string contenga datos

		if (!valida_infoPendStatusE_format) {
			
			pv_doc_name = infoPendStatusE_format_Result.getData(0, "PV_DOC_NAME");
			
			System.out.println("PV_DOC_NAME: " + pv_doc_name );
			
			testCase.addQueryEvidenceCurrentStep(infoPendStatusE_format_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(valida_infoPendStatusE_format);
		
		assertFalse("No se encontraron registros STATUS = E en la tabla POS_INBOUND_DOCS", valida_infoPendStatusE_format); // Si esta vacio, imprime mensaje
	
		
//****************************************** Paso 10 ******************************************************************
		 addStep("Establecer la conexión a la FCIASQA");
		 
		 testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCIASQA);
		 testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCIASQA);

	
//****************************************** Paso 9 *********************************************************************
		

		addStep("Validar que los datos para la plaza  y tienda correspondientes se hayan insertado en la tabla XXRPA_PAGARE de la BD XXRPA.");
		
		String validarXXRPA_format = String.format(validarXXRPA , pv_doc_name);

		System.out.println(validarXXRPA_format);
		
		SQLResult validarXXRPA_Result = dbCNT.executeQuery(validarXXRPA_format);	

		boolean validarXXRPA_boolean = validarXXRPA_Result.isEmpty(); // checa que el string contenga datos

		if (!validarXXRPA_boolean) {
			
			testCase.addQueryEvidenceCurrentStep( validarXXRPA_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(validarXXRPA_boolean);
		
		assertFalse("No se encontraron registros ", validarXXRPA_boolean); // Si esta vacio, imprime mensaje
		
	}
	

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PB12_WMx86ProcesaArchivoRPA_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "ATC-FT-017 PB12 Procesar archivo RPA de resguardo de pagarés a través de la interface FEMSA_PB12";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatizacion";
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
