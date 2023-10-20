package interfaces.rsp01;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;
//import org.testng.asserts.SoftAssert;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ATF_FT_002_RSP01_EnvioReportesMasivosPromDesdeSFTPRMSaSharePoint extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATF_FT_002_RSP01_EnvioReportesMasivosPromDesdeSFTPRMSaSharePoint_test(HashMap<String, String> data) throws Exception {
	
/* Utilerías ********************************************************************************************************************************************/
		
		
		
		utils.sql.SQLUtil dbLog= new utils.sql.SQLUtil( GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);

 /* Variables ******************************************************************************************************************************************/
//		SoftAssert softAssert = new SoftAssert();
		String StatusSearch="S";
	//Paso 1		
		String docsPorProcesar = "SELECT NOMBRE_ARCHIVO, PLAZA, ORIGEN, DESTINO, ESTATUS, FECHA_ACTUALIZACION \r\n" + 
				"FROM XXFC.XXFC_REP_MAS_PROM_FILES_CTRL\r\n" + 
				"WHERE ESTATUS = 'L' \r\n" + 
				"AND NOMBRE_ARCHIVO LIKE 'P%'" ;   
						

		String datosConexionSharePoint = "SELECT VALOR3, VALOR4, VALOR1, VALOR5 \r\n" + 
				"FROM WMUSER.WM_INTERFASE_CONFIG\r\n" + 
				"WHERE INTERFASE = 'RSP01' \r\n" + 
				"AND OPERACION = 'LOGIN_SFTP' \r\n" + 
				"AND ENTIDAD = 'WM'";
		
		
	  	String ValidLog = "SELECT run_id,start_dt,status " + "FROM WMLOG.WM_LOG_RUN  "
	  			+ "WHERE INTERFACE LIKE '%RSP01%' "  + "AND START_DT >= TRUNC(SYSDATE) "
	  			+ "AND rownum = 1  " + "ORDER BY START_DT DESC";
		

	
		//consultas de error
		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s";

		String consultaThreads = " SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS " 
		+ "FROM WMLOG.WM_LOG_THREAD   WHERE PARENT_ID='%s'  "
				+ "ORDER BY START_DT DESC";// Consulta para los Threads
		
	
		
		String docsProcesados = "SELECT NOMBRE_ARCHIVO, PLAZA, ESTATUS, FECHA_ACTUALIZACION \r\n" + 
				"FROM XXFC.XXFC_REP_MAS_PROM_FILES_CTRL \r\n" + 
				"WHERE NOMBRE_ARCHIVO  = '%s'\r\n" + 
				"AND FECHA_ACTUALIZACION >= TRUNC(SYSDATE) \r\n" + 
				"AND ESTATUS = 'P' ";
		
		
//Pasos ***********************************************************************************************************************************************************************		
			
		//Paso 1
		
		addStep("Validar  que existan registros de documentos Listos para ser procesados en la BD RETEK:");   
		
		   System.out.println(GlobalVariables.DB_HOST_RMS_MEX);
		   System.out.println(docsPorProcesar);
	        
	       SQLResult docsPorProcesar_Result = dbRms.executeQuery(docsPorProcesar);
	       String NOMBRE_ARCHIVO = "";

	       boolean  docsPorProcesar_valida = docsPorProcesar_Result.isEmpty(); 
	    		    
	       if(!docsPorProcesar_valida) {
	    	   NOMBRE_ARCHIVO = docsPorProcesar_Result.getData(0, "NOMBRE_ARCHIVO");
	    	   System.out.println("NOMBRE_ARCHIVO= "+ NOMBRE_ARCHIVO ); 
	    	   testCase.addTextEvidenceCurrentStep("Se encontraron registros de documentos Listos para ser procesados");
	       }
	       testCase.addQueryEvidenceCurrentStep(docsPorProcesar_Result);
	      
	       System.out.println(docsPorProcesar_valida); 
       
	       
	       assertFalse(docsPorProcesar_valida, "No se encontraron  datos disponibles para ser procesados." ); 
       
       
//****************************************************************************************************************************************************************
     //Paso 2
       
     		 addStep("validar cuales son los datos para conectarse al servidor SFTP donde se encuentran los documentos.");   
     		  System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
     		  System.out.println(datosConexionSharePoint);
     	        
     	       SQLResult datosConexionSharePoint_R = dbPos.executeQuery(datosConexionSharePoint);
     	       
               boolean  datosConexionSharePoint_valida = datosConexionSharePoint_R.isEmpty(); 
     	    		  
     	       
     	       if(!datosConexionSharePoint_valida) {
     	    	   
     	    	  testCase.addTextEvidenceCurrentStep("Se encontraron los datos para conectarse al servidor SFTP donde se encuentran los documentos");
     	       }
     	      
     	      testCase.addQueryEvidenceCurrentStep(datosConexionSharePoint_R);
     	       System.out.println(datosConexionSharePoint_valida); 
     	      
     	      assertFalse(datosConexionSharePoint_valida, "No se encontraron los datos para conectarse al servidor SFTP donde se encuentran los documentos"); 
            
//****************************************************************************************************************************************************************
            //Paso 3
            
            /*
             *  - Conectarse via SFTP al servidor IP: 10.184.16.184 con su usuario y contraseña
             *   Ingresamos a la ruta: /upload/promo
             *   PASO FUERA DE ALCANCE POR CONEXION FTP, NO SE CONOCE USUARIO NI CONTRASEÑA
             */
                   
//****************************************************************************************************************************************************************
                 
     //Paso 4 
          
           
       addStep("Ejecutar la interface RSP01.Pub:runRSP01 mediante la ejecución del JOB runRSP01");
       
       
   	/*
		 * Solicitar el ordenamiento del Job runRSP01 enviando un correo electrónico a
		 * los operadores USU UsuFEMCOMOperadoresSITE@oxxo.com , en Control M para su
		 * ejecución. de la interface RSP01.Pub:runRSP01 mediante la ejecución del JOB runRSP01 por medio del Control-M
		 */

		addStep("Solicitar el ordenamiento del Job Pub:runRSP01, en Control M para su ejecución.");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);

		JSONObject obj = new JSONObject(data.get("job"));

		testCase.addBoldTextEvidenceCurrentStep("Jobs en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
		// testCase.addPaso("Paso con addPaso");
		testCase.addBoldTextEvidenceCurrentStep("Login");
		u.get(data.get("server"));
		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn();

		// Buscar del job
		testCase.addBoldTextEvidenceCurrentStep("Inicio de job ");
		ControlM control = new ControlM(u, testCase, obj);
		boolean flag = control.searchJob();
		assertTrue(flag);

		// Ejecucion
		String resultado = control.executeJob();
		System.out.println("Resultado de la ejecucion -> " + resultado);

		u.hardWait(30);

		// Valor del output

		String Res2 = control.getNewStatus();

		System.out.println("Valor de output getNewStatus:" + Res2);

		String output = control.getOutput();
		System.out.println("Valor de output control:" + output);

		testCase.addTextEvidenceCurrentStep("Status de ejecucion: " + Res2);
		testCase.addTextEvidenceCurrentStep("Output de ejecucion: " + output);
		// Validacion del caso
		Boolean casoPasado = false;
		if (Res2.equals("Ended OK")) {
			casoPasado = true;
		}

		control.closeViewpoint();
		u.close();
		assertTrue(casoPasado);
		// assertNotEquals("Failure",resultado);
	      
//***************************************************************************************************************************************************************************************
	      
	      //Paso 5
	        
	  	  addStep("Validar que la interface haya finalizado correctamente en el WMLOG.");
	  	System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);

		SQLResult ValidLogRes = executeQuery(dbLog, ValidLog);
		String run_id="";
		String status="";

		boolean ValidLogVal = ValidLogRes.isEmpty();

		if (!ValidLogVal) {
			run_id = ValidLogRes.getData(0, "RUN_ID");
			status = ValidLogRes.getData(0, "status");
			testCase.addTextEvidenceCurrentStep("Se encontro detalle de la ejecuion de servicio");
	
		}
		testCase.addQueryEvidenceCurrentStep(ValidLogRes);
		System.out.println(ValidLogVal);

		assertEquals(status,StatusSearch, "El estatus de log no es S " );
			
//***************************************************************************************************************************************************************************************
			
			//Paso 6

			addStep("Comprobar que los threads ejecutados terminaron con status igual a S");
			
			String threads = String.format(consultaThreads, run_id);
			System.out.println("CONSULTA THREAD "+ threads);
			SQLResult threadsResult=  dbLog.executeQuery(threads);
			String thread="";
	        
			boolean ValidThread = threadsResult.isEmpty();

			if (!ValidThread) {
				thread = threadsResult.getData(0, "THREAD_ID");
				testCase.addTextEvidenceCurrentStep("Se encontro detalle de la ejecuion de servicio ");
			}
			testCase.addQueryEvidenceCurrentStep(threadsResult);
			System.out.println(ValidThread);
			
			assertEquals(ValidThread,StatusSearch, "No se encontro detalle de la ejecuion de servicio en estatus S");
				
			
			
//***************************************************************************************************************************************************************************			
	//Paso 7
			
			addStep(" Realizar la siguiente consulta para comprobar que la ejecución de la interface RSP01 no registro errores.");
	
			System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
			String FormatError = String.format(tdcQueryErrorId, thread);
			System.out.println(FormatError);

			SQLResult FormatErrorRes = executeQuery(dbLog, FormatError);

			boolean FormatErrorVal = FormatErrorRes.isEmpty();

			if (FormatErrorVal) {

				testCase.addTextEvidenceCurrentStep(
						"No se encontro ningún error presente en la ejecución de la interfaz  dentro de la tabla WM_LOG_ERROR");

			}
			testCase.addQueryEvidenceCurrentStep(FormatErrorRes);
			System.out.println(FormatErrorVal);

			assertTrue(FormatErrorVal,
					"se encontro error presente en la ejecución de la interfaz  dentro de la tabla WM_LOG_ERROR");


//***************************************************************************************************************************************************************************
			
			//Paso 8

			addStep("validar que los registros procesados tengan el ESTATUS igual a  'P' en la tabla XXFC_REP_MAS_PROM_FILES_CTRL en la BD RETEK.");   		
			
			 String docsProcesados_f = String.format(docsProcesados, NOMBRE_ARCHIVO );
			
			 System.out.println(docsProcesados_f);
		        
		       SQLResult docsProcesados_r = dbRms.executeQuery(docsProcesados_f);
		       
		       boolean  docsProcesados_valida = docsProcesados_r.isEmpty(); 
		    		  
		       
		       if(!docsProcesados_valida) {
		    		testCase.addTextEvidenceCurrentStep("Los registros procesados se actualizaron correctamente.");
		    	  
		       }
		       testCase.addQueryEvidenceCurrentStep(docsProcesados_r);  
		       System.out.println(docsProcesados_valida); 
		       
		   
		       assertFalse(docsProcesados_valida, "Los registros procesados no se actualizaron correctamente." ); 
	       
	       
//****************************************************************************************************************************************************************
//	    Paso 9
	       
	       /*
	        * Abrimos el explorador web y escribimos la siguiente liga en la barra de dirección para revisar que el archivo fue depositado en el sharepoint: 
				http:\\fcxnvatpap02:38122\sitios\AdmFin\ERPIMP\CATNACL\Promociones\Promos_Los_Mochis_Digital 
				PAso pendiente ya que no tenemos acceso a la liga mencionada
	        */
		      
	}    
        
		
	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATF_FT_002_RSP01_EnvioReportesMasivosPromDesdeSFTPRMSaSharePoint_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "MTC-FT-026-CE RSP01 Envió de archivos de reportes masivos de promociones desde un SFTP de RMS hacia SharePoint a través de la interface FEMSA_RSP01";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
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



