package interfaces.pp01;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_PP01_002_WMx86_GeneracionArchivosServISE extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PP01_002_WMx86_GeneracionArchivosServISE_test(HashMap<String, String> data) throws Exception {
		
		/*Back Office Mexico: MTC-FT-015 PP01 Generacion de archivo ISE de informacion de pagos de servicios electronicos a traves de la interface FEMSA_PP01
 * Desc:
 * Prueba de regresión  para comprobar la no afectacion en la funcionalidad principal de la interface FEMSA_PP01 para generar archivos 
 * ISE (Información de Servicios Electronicos) de bajada (de FCIASQA_XXPSO  a WM OUTBOUND), al ser migrada la interface de WM9.9 a WM10.5
 * @author Marisol Rodriguez
 * @date   2022/07/16*/
		
		/**
		 * Nota: Posible update para generar insumos

               update XXPSO.XXPSO_SERVICE_LOG set wm_status = 'L'
               where unique_id = '10260'
               and ms_servid = '101289499'
               and cr_plaza = '10MON'
               and cr_tienda = '50MRC';
		 */
/* Utilerias *********************************************************************/		
		
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		SQLUtil dbCNT = new SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
				
	
/**
* Variables ******************************************************************************************
* 
*/
		
//Paso 1
		String statusL = "select unique_id, ms_servid, wm_sent_date, wm_status, wm_doc_name, cr_plaza, cr_tienda \r\n"+
				"from XXPSO.XXPSO_SERVICE_LOG \r\n" + 
				"where cr_plaza = '" + data.get("plaza") +"' \r\n"+
			    "and wm_status = 'L'"; 
		
//Paso 2
		
		String xxpso_m_service = "SELECT UNIQUE_ID, MS_SERVID, MS_MCASERV, MS_DESC, MS_TIPO, MS_REF1, MS_REF2, MS_REF3, STATUS \r\n" + 
				"from  XXPSO.XXPSO_M_SERVICE \r\n" + 
				"where unique_id = '%s' \r\n" + 
				"and status >= 1";
		
		
//Paso 3	   
		// consultas de error
				String consultaError1 = " select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE \r\n"+
							 "from wmlog.WM_LOG_ERROR \r\n" + 
							 "where RUN_ID='%s' \r\n" +
							 "and rownum <=1"; // dbLog
						
				String consultaError2 = " select description,MESSAGE \r\n" +
							 "from wmlog.WM_LOG_ERROR \r\n" +
							 "where RUN_ID='%s' \r\n" +
							 "and rownum <= 1"; // dbLog
//Paso 4
				
		      String  tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server \r\n" + 
		      		"FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE = 'PP01' \r\n"+
		      	    "AND STATUS = 'S' \r\n" +
		      		"ORDER BY START_DT DESC) \r\n"+
		            "WHERE ROWNUM <=1";
		      	  
		      
//Paso 5
		      String qry_threads1 = "SELECT THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS \r\n" +
		      		"FROM WMLOG.WM_LOG_THREAD \r\n" + 
					"WHERE PARENT_ID = '%s'";
				
				String qry_threads2 = "SELECT  ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 \r\n" +
					"FROM WMLOG.WM_LOG_THREAD \r\n" + 
					"WHERE PARENT_ID = '%s'";
		      
//Paso 6
				String statusE = "SELECT unique_id, ms_servid, wm_sent_date, wm_status, wm_doc_name, cr_plaza, wm_run_id \r\n"
						+ "FROM XXPSO.XXPSO_SERVICE_LOG \r\n"
						+ "where wm_status = 'E'\r\n"
						+ "and wm_sent_date is not null\r\n"
						+ "and unique_id = '%s' \r\n"
						+ "and ms_servid = '%s'\r\n"
						+ "and wm_run_id = '%s'\r\n"
						+ "order by insert_date desc";
				
//Paso 7
				String xxpso_m_service2 = "SELECT UNIQUE_ID, MS_SERVID, MS_MCASERV, MS_DESC, MS_TIPO, MS_REF1, MS_REF2, MS_REF3, STATUS \r\n" + 
						"from xxpso_m_service \r\n" + 
						"where unique_id = '%s' \r\n" + 
						"and status = 2";
				
//Paso 8
				String documentoPos ="SELECT id, doc_name,doc_type,pv_cr_plaza,pv_cr_tienda,status, partition_date \r\n"
						+ "FROM POSUSER.POS_OUTBOUND_DOCS\r\n"
						+ "WHERE DOC_TYPE = 'ISE'\r\n"
						+ "AND PV_CR_PLAZA = '" + data.get("plaza") +"'\r\n"
						+ "AND PV_CR_TIENDA = '" + data.get("tienda") +"'\r\n"
						+ "AND DOC_NAME = '%s' \r\n"
						+ "AND STATUS = 'L' \r\n"
						+ "order by SENT_DATE desc ";
				
				testCase.setProject_Name("POC WMX86");
				testCase.setTest_Description(data.get("id") + " - " + data.get("descripcion"));
				testCase.setPrerequisites(data.get("prerequicitos"));
		
		      
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
				
//*****************************************Paso 1*******************************************************************
	 addStep("Establecer Conexión con la BD FCIASQA_XXPSO");
				 
			testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
			testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCIASQA);
					
//******************************************Paso 2****************************************************************** 
		addStep("Validar que exista información pendiente de procesar en la tabla XXPSO_SERVICE_LOG en  FCIASQA_XXPSO");
		
		System.out.println(statusL);
		
		SQLResult statusL_Res = executeQuery(dbCNT, statusL);
		
		String unique_id = "";
		
		String ms_servid = "";
		
		String WM_DOC_NAME = "";
			
		boolean validaStatusL = statusL_Res.isEmpty();
		
			if (!validaStatusL) {
				
				//Usamos el wm_doc_name en pos POSUSER.POS_OUTBOUND_DOCS para validar el mismo registro
                 WM_DOC_NAME = statusL_Res.getData(0, "WM_DOC_NAME ");
				
				 System.out.println("WM_DOC_NAME: " + WM_DOC_NAME );
				
				 //Usamos el unique_id para validar el registro en la siguiente consulta
				 unique_id = statusL_Res.getData(0, "UNIQUE_ID");
				 
				 System.out.println("UNIQUE_ID = "+ unique_id);
				 
				//Usamos el ms_servid para validar cuando el registro cambie a status E
				 ms_servid = statusL_Res.getData(0, "MS_SERVID");
				 
				 System.out.println("MS_SERVID = "+ ms_servid);
				
			     testCase.addQueryEvidenceCurrentStep(statusL_Res);
			
						} 
		
		System.out.println(validaStatusL);

		assertFalse(validaStatusL, "No existe informacion pendiente de procesar en PORTAL.");
		
//**************************************Paso 3	*********************************************************************** 
		
		addStep("Validar que exista información en la tabla XXPSO_M_SERVICE en FCIASQA_XXPSO.");
	
		String xxpso_m_serviceFormat = String.format(xxpso_m_service, unique_id);
		 
		System.out.println(xxpso_m_serviceFormat);
		
		SQLResult xxpso_m_service_Res = executeQuery(dbCNT, xxpso_m_serviceFormat);
		
		boolean validaxxpso_m_service = xxpso_m_service_Res.isEmpty();
		
			if (!validaxxpso_m_service) {
		
			testCase.addQueryEvidenceCurrentStep(xxpso_m_service_Res);
			
						} 
		
		System.out.println(validaxxpso_m_service);

		assertFalse(validaxxpso_m_service, "No existe información en PORTAL.");
		
//**********************************************Paso 4 Y 5 *****************************************************************
		
		addStep("Ejecutar el  Job runPP1 y Grupo: TQA_BO_105 y validar que la ejecución del job haya sido exitosa");
		

		
		// utileria
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
		assertFalse(!flag, "No se encontro el job");
		
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
//*********************************************Paso 6 *******************************************************************
			addStep("Establecer la conexión a la BD ** FCWMLQA**.");
				 
			testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
			testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA);
			testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCWMLQA);
				 
			
		
//*********************************************Paso 7*******************************************************
		
		addStep("Validar que la interfaz se ejecutó correctamente.");
		
	
		SQLResult ISF_Result = executeQuery(dbLog, tdcIntegrationServerFormat);

		String STATUS = ISF_Result.getData(0, "STATUS"); //Se obtiene el status de la ejecucion
		
		String RUN_ID = ISF_Result.getData(0, "RUN_ID");
		
		boolean validateStatus = STATUS .equals(status);
		
		System.out.println("Status es S = "+ validateStatus );
		
		
		if (validateStatus) {

			testCase.addQueryEvidenceCurrentStep(ISF_Result);
		}
		

		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");
		

		
//*****************************************Paso 7 **************************************************************

			addStep("Validar que se inserte el detalle de la ejecución de los Threads lanzados por la interface en la tabla");

				//Parte 1
				String consultaTreads1 = String.format(qry_threads1, RUN_ID );
				
				System.out.println(consultaTreads1);
				
				SQLResult consultaTreads1_R = dbLog.executeQuery(consultaTreads1);
				
				String THREAD_ID = "";

				boolean validaThreads1 = consultaTreads1_R.isEmpty();
				
				if (!validaThreads1) {
					
				    THREAD_ID = ISF_Result.getData(0, "THREAD_ID");
					testCase.addQueryEvidenceCurrentStep(consultaTreads1_R);
				}
			
				System.out.println(validaThreads1);
				
				// Parte 2

				String consultaTreads2 = String.format(qry_threads2, RUN_ID);
				
				System.out.println(consultaTreads2);
				
				SQLResult consultaTreads2_R = dbLog.executeQuery(consultaTreads2);

				boolean validaThreads2 = consultaTreads2_R.isEmpty();
				
				if (!validaThreads2) {
					
					testCase.addQueryEvidenceCurrentStep(consultaTreads2_R);
				}

				System.out.println(validaThreads2);
				
			    assertFalse("No se generaron threads ",validaThreads2);
			    
			    
//*********************************************Paso 8 *********************************************************************
			    
	addStep("Realizar la siguiente consulta para verificar que no se encuentre ningún error presente en la ejecución "
			+ "de la interfaz  dentro de la tabla WM_LOG_ERROR ");

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
				
//***************************************Paso 9 *******************************************************************
				
		addStep("Establecer la conexión a la BD **FCWM6QA**.");
				 
		testCase.addBoldTextEvidenceCurrentStep("La conexion fue exitosa");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_USER_FCWMQA_NUEVA);
							
				
//****************************************Paso 10 ***************************************************
addStep("Validar que se inserte el registro del archivo ISE generado por la interface en la tabla POS_OUTBOUND_DOCS del esquema POSUSER de la BD FCWM6QA");
			
        String documentoPosFormat = String.format(documentoPos,WM_DOC_NAME );
        
		System.out.println(documentoPosFormat); 
		
		SQLResult documentoPosFormat_Res = executeQuery(dbPos,documentoPosFormat);
		
		String ID = "";
		
		
		boolean validadocumentoPos = documentoPosFormat_Res.isEmpty();
		
			if (!validadocumentoPos) {
				
				ID = documentoPosFormat_Res.getData(0, "ID");
				
				System.out.println("ID: "+ ID);
				
			
			testCase.addQueryEvidenceCurrentStep(documentoPosFormat_Res);
			
						} 
		
		System.out.println(validadocumentoPos);

		assertFalse(validadocumentoPos, "No se encontro el archivo ISE en la tabla POS_OUTBOUND_DOCS.");
		
		
		
//******************************************Paso 11 Y 12 **********************************************************************
		 Thread.sleep(20000);
			
			addStep(" Validar que se realice el envío del archivo ISE generado por la interface en el directorio configurado para la tienda procesada.\r\n"
					+ "Servicio para obtener el directorio: Utilities.FTP:configFTP.\r\n"
					+ "Ruta: /u01/posuser/FEMSA_OXXO/POS (en la carpeta working)");
			
	       FTPUtil ftp = new FTPUtil("10.182.92.13",21,"posuser","posuser");
	       //Ruta: /u01/posuser/FEMSA_OXXO/POS (en la carpeta working)
	       Thread.sleep(20000);
	       String ruta = "/FEMSA_OXXO/POS/"+ data.get("plaza") +"/"+ data.get("tienda") +"/working/" + WM_DOC_NAME ;
	                                 //host, puerto, usuario, contraseña
	       ///u01/posuser
	       boolean validaFTP;
	       
	        if (ftp.fileExists(ruta) ) {
	        	
	        	validaFTP = true;
	            testCase.addFileEvidenceCurrentStep(ruta);
	            System.out.println("Existe");
	            testCase.addBoldTextEvidenceCurrentStep("El archivo si existe ");
	            testCase.addBoldTextEvidenceCurrentStep(ruta);
	            
	        }else {
	        	testCase.addFileEvidenceCurrentStep(ruta);
	        	testCase.addBoldTextEvidenceCurrentStep("El archivo no existe ");
	            System.out.println("No Existe");
	            validaFTP = false;
	        }
	        
	        assertTrue("No se encontro el archivo xml en POSUSER ", validaFTP);
	        

		
//*******************************************Paso 13**********************************************************************		
		addStep("Validar que se actualizaron los campos WM_STATUS a E, WM_SENT_DATE a la fecha actual y WM_RUN_ID de la tabla xxpso_service_log en PORTAL.");
		
		String  statusEFormat= String.format(statusE, unique_id, ms_servid ,THREAD_ID);
		
		System.out.println(statusEFormat);
		
		SQLResult statusEt_Res = executeQuery(dbCNT, statusEFormat);
			
		boolean validaStatusE = statusEt_Res.isEmpty();
		
			if (!validaStatusE) {
		
			testCase.addQueryEvidenceCurrentStep(statusEt_Res);
			
						} 
		
		System.out.println(validaStatusE);

		assertFalse(validaStatusE, "No se actualizaron los campos correctamente en PORTAL.");
		
//*********************************************Paso 14 *****************************************************************
		
		addStep(" Validar que se actualizó el campo: STATUS con el valor 2 en la tabla XXPSO_M_SERVIC en la BD FCIASQA_XXPSO ");
		
		String xxpso_m_service2Format = String.format(xxpso_m_service2, unique_id);
		 
		System.out.println(xxpso_m_service2Format);
		
		SQLResult xxpso_m_service2_Res = executeQuery(dbCNT, xxpso_m_service2Format);
		
		boolean validaxxpso_m_service2 = xxpso_m_service2_Res.isEmpty();
		
			if (!validaxxpso_m_service2) {
		
			testCase.addQueryEvidenceCurrentStep(xxpso_m_service2_Res);
			
						} 
		
		System.out.println(validaxxpso_m_service2);

		assertFalse(validaxxpso_m_service2, "El campo STATUS se actualiza correctamente en PORTAL.");

		
		
		

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
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatizacion";
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
	
	
	


}

