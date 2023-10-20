package interfaces.rsp01;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
 


public class RSP01_VerificaEjecucionExitosaAlEnviarArchivos extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_RSP01_VerificaEjecucionExitosaAlEnviarArchivos(HashMap<String, String> data) throws Exception {
	
/* Utilerías ********************************************************************************************************************************************/
		
		
		
		utils.sql.SQLUtil dbLog= new utils.sql.SQLUtil( GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);

 /* Variables ******************************************************************************************************************************************/
		
	//Paso 1		
		String docsPorProcesar = "SELECT NOMBRE_ARCHIVO, PLAZA, ORIGEN, DESTINO, ESTATUS, FECHA_ACTUALIZACION \r\n" + 
				"FROM XXFC.XXFC_REP_MAS_PROM_FILES_CTRL\r\n" + 
				"WHERE ESTATUS = 'L' \r\n" + 
				"AND NOMBRE_ARCHIVO LIKE 'P%'" ;   
				
		
	//paso 2
		String datosConexionSharePoint = "SELECT VALOR3, VALOR4, VALOR1, VALOR5 \r\n" + 
				"FROM WMUSER.WM_INTERFASE_CONFIG\r\n" + 
				"WHERE INTERFASE = 'RSP01' \r\n" + 
				"AND OPERACION = 'LOGIN_SP' \r\n" + 
				"AND ENTIDAD = 'WM'";
		
	//Paso 3
		String datosConexionSFTP = "SELECT VALOR3, VALOR4, VALOR1, VALOR5 \r\n" + 
				"FROM WMUSER.WM_INTERFASE_CONFIG\r\n" + 
				"WHERE INTERFASE = 'RSP01' \r\n" + 
				"AND OPERACION = 'LOGIN_SFTP' \r\n" + 
				"AND ENTIDAD = 'WM'";
		
		//Paso 5 y 6	
		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server \r\n" + 
				"FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE = 'RSP01' "
				+ "AND STATUS = 'S' " +
				"  AND start_dt >= trunc(SYSDATE)  " +				
				"ORDER BY START_DT DESC) where rownum <=1";
		
	
		//consultas de error
		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";
		
		String consultaError2 = " select * from (select description,MESSAGE "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		
		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		
		//Paso 8
		String consultaThreads = " SELECT  THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS " 
		+ "FROM WMLOG.WM_LOG_THREAD   WHERE PARENT_ID='%s'  "
				+ "ORDER BY THREAD_ID DESC";// Consulta para los Threads
		
		String consultaThreads2 = " SELECT   ATT1, ATT2, ATT3,ATT4, ATT5,ATT6,ATT7,ATT8 "
				+ "FROM WMLOG.WM_LOG_THREAD WHERE PARENT_ID='%s' "
						+ "ORDER BY THREAD_ID DESC ";// Consulta para los Threads
		
		//Paso 9
		
		
		String docsProcesados = "SELECT NOMBRE_ARCHIVO, PLAZA, ESTATUS, FECHA_ACTUALIZACION \r\n" + 
				"FROM XXFC.XXFC_REP_MAS_PROM_FILES_CTRL \r\n" + 
				"WHERE NOMBRE_ARCHIVO  = '%s'\r\n" + 
				"AND FECHA_ACTUALIZACION >= TRUNC(SYSDATE) \r\n" + 
				"AND ESTATUS = 'P' ";
		
		
//Pasos ***********************************************************************************************************************************************************************		
		
		
		//Paso 1
		
		addStep("Verificar que existan registros de documentos Listos para ser procesados en la BD RETEK:");   
		
		
		   System.out.println(docsPorProcesar);
	        
	       SQLResult docsPorProcesar_Result = dbRms.executeQuery(docsPorProcesar);
	       
	       //Obtentenemos NOMBRE_ARCHIVO
	       String NOMBRE_ARCHIVO = docsPorProcesar_Result.getData(0, "NOMBRE_ARCHIVO");
	       
	       System.out.println("NOMBRE_ARCHIVO= "+ NOMBRE_ARCHIVO ); 
	       
	       
	       boolean  docsPorProcesar_valida = docsPorProcesar_Result.isEmpty(); //checa que el string contenga datos
	    		  
	       
	       if(!docsPorProcesar_valida) {
	    	   
	    	   testCase.addQueryEvidenceCurrentStep(docsPorProcesar_Result);  //Si no esta vacio, lo agrega a la evidencia
	       }
	      
	       System.out.println(docsPorProcesar_valida); 
       
	       
       assertFalse(docsPorProcesar_valida, "No se encontraron  datos disponibles para ser procesados." ); //Si esta vacio, imprime mensaje
       
       
//****************************************************************************************************************************************************************
     //Paso 2
		
     		addStep("Verificar cuales son los datos para conectarce al SharePoint, donde se enviaran los documentos en la BD WMINT ");   
     		 
     		  System.out.println(datosConexionSharePoint);
     	        
     	       SQLResult datosConexionSharePoint_R = dbPos.executeQuery(datosConexionSharePoint);
     	       
               boolean  datosConexionSharePoint_valida = datosConexionSharePoint_R.isEmpty(); //checa que el string contenga datos
     	    		  
     	       
     	       if(!datosConexionSharePoint_valida) {
     	    	   
     	    	   testCase.addQueryEvidenceCurrentStep(datosConexionSharePoint_R);  //Si no esta vacio, lo agrega a la evidencia
     	       }
     	      
     	      
     	       System.out.println(datosConexionSharePoint_valida); 
     	       
            assertFalse(datosConexionSharePoint_valida, "No se encontraron los datos de conexion al SharePoint "); //Si esta vacio, imprime mensaje
            
//****************************************************************************************************************************************************************
            //Paso 3
       		
            		addStep("Verificar cuales son los datos para conectarse al servidor SFTP donde se encuentran los documentos."); 
            		
      		          System.out.println(datosConexionSFTP);
            	        
            	      SQLResult datosConexionSFTP_R = dbPos.executeQuery(datosConexionSFTP);
            	       
                      boolean  datosConexionSFTP_valida = datosConexionSFTP_R.isEmpty(); //checa que el string contenga datos
            	    		  
            	       
            	       if(!datosConexionSFTP_valida) {
            	    	   
            	    	   testCase.addQueryEvidenceCurrentStep(datosConexionSFTP_R);  //Si no esta vacio, lo agrega a la evidencia
            	       }
            	      
            	       System.out.println(datosConexionSFTP_valida); 
                   
            	       
                   assertFalse(datosConexionSFTP_valida, "Comprobar cuales son los datos de conexion al Servidor SFTP"); //Si esta vacio, imprime mensaje
                   
//****************************************************************************************************************************************************************
                 
     //Paso 5  
          
           
       addStep("Ejecutar la interface RSP01.Pub:runRSP01 mediante la ejecución del JOB runRSP01");
       
       
         // Utileria
	      SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
	      PakageManagment pok = new PakageManagment(u, testCase);
	      String status = "S"; //status exitoso


	      String user = data.get("user");
	      String ps = PasswordUtil.decryptPassword(data.get("ps"));
	      String server = data.get("server");
	      String searchedStatus = "R";
     
          System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
	      String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
	      u.get(contra);

	   
	
	      String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
	      System.out.println("Respuesta dateExecution" + dateExecution);
	      
	     
	      
	       SQLResult is= dbLog.executeQuery(tdcIntegrationServerFormat);
	        String run_id  = is.getData(0, "RUN_ID");
	        System.out.println("RUN_ID = "+ run_id );
	        String status1 = is.getData(0, "STATUS");
	      
	        boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
	      
	        while (valuesStatus) {
	        	
	          status1= is.getData(0, "STATUS");
	          run_id= is.getData(0, "RUN_ID");
	          valuesStatus = status1.equals(searchedStatus);

	      u.hardWait(2);
	       
	     }    
	      
	      boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
	      
	      if (successRun) {

	          String error = String.format(consultaError1, run_id);
	          String error1 = String.format(consultaError2, run_id);
	          String error2 = String.format(consultaError3, run_id);


	          SQLResult errorr= dbLog.executeQuery(error);
	          boolean emptyError=errorr.isEmpty();
	          if (!emptyError) {

	              testCase.addTextEvidenceCurrentStep(
	                      "Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
	              
	              testCase.addQueryEvidenceCurrentStep(errorr);

                    
	          }

	          SQLResult errorIS=  dbLog.executeQuery(error1);
	          boolean emptyError1 = errorIS.isEmpty();
	         

	          if (!emptyError1) {

	        	  testCase.addQueryEvidenceCurrentStep(errorIS);

	          }
	          
	          SQLResult errorIS2=  dbLog.executeQuery(error2);
	          boolean emptyError2 =errorIS2.isEmpty();
	          
	          if (!emptyError2) {
	        	  
	        	  testCase.addQueryEvidenceCurrentStep(errorIS2);


	          }
	          
	      }
	      
//***************************************************************************************************************************************************************************************
	      
	      //Paso 6
	        
	  	  addStep("Validar que la interface haya finalizado correctamente en el WMLOG.");
	
	  	
	        boolean validateStatus = status.equals(status1);
			System.out.println("VALIDACION DE STATUS = S - "+ validateStatus);
			assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa ");
			
			

			boolean av2 = is.isEmpty();
			if (av2 == false) {

				testCase.addQueryEvidenceCurrentStep(is);

			} else {
				
				testCase.addQueryEvidenceCurrentStep(is);
			}
			
			System.out.println("El registro en WM_LOG_RUN esta vacio "+ av2);
			
//***************************************************************************************************************************************************************************************
			
			//Paso 7
			
			addStep("Comprobar que los threads ejecutados terminaron con status igual a S");
			
			String threads = String.format(consultaThreads, run_id);
			
			System.out.println("CONSULTA THREAD "+ threads);
			
			 SQLResult threadsResult=  dbLog.executeQuery(threads);
	        
			
			boolean av31 = threadsResult.isEmpty();
			if (av31 == false) {

				testCase.addQueryEvidenceCurrentStep(threadsResult);

			} else {
				testCase.addQueryEvidenceCurrentStep(threadsResult);
			}
			System.out.println("El registro en WM_LOG_THREAD esta vacio 1- "+ av31);
			
			// .-----------Segunda consulta
			
			String threads2 = String.format(consultaThreads2, run_id);
			
			SQLResult threadsResult2=  dbLog.executeQuery(threads2);
			
			boolean av3111 = threadsResult2.isEmpty();
			if (av3111 == false) {

				testCase.addQueryEvidenceCurrentStep( threadsResult2);

			} else {
				testCase.addQueryEvidenceCurrentStep( threadsResult2);
			}
			System.out.println("El registro en WM_LOG_THREAD esta vacio 2- "+ av31);
			
			assertFalse(av3111, "No se generaron threads en la tabla");
			
			
//***************************************************************************************************************************************************************************			
	//Paso 8
			
			/*Comprobar que el archivo fue procesado correctamente al SharePoint correspondiente en la ruta [XXFC_REP_MAS_PROM_FILES_CTRL.DESTINO]*/

//***************************************************************************************************************************************************************************
			
			//Paso 9
			
			addStep("Verificar que los registros procesados tengan el ESTATUS igual a  'P' en la tabla XXFC_REP_MAS_PROM_FILES_CTRL en la BD RETEK.");   		
			
			 String docsProcesados_f = String.format(docsProcesados, NOMBRE_ARCHIVO );
			
			 System.out.println(docsProcesados_f);
		        
		       SQLResult docsProcesados_r = dbRms.executeQuery(docsProcesados_f);
		       
		       boolean  docsProcesados_valida = docsProcesados_r.isEmpty(); //checa que el string contenga datos
		    		  
		       
		       if(!docsProcesados_valida) {
		    	   
		    	   testCase.addQueryEvidenceCurrentStep(docsProcesados_r);  //Si no esta vacio, lo agrega a la evidencia
		       }
		      
		       System.out.println(docsProcesados_valida); 
		       
		   
	       assertFalse(docsProcesados_valida, "Los registros procesados no se actualizaron correctamente." ); //Si esta vacio, imprime mensaje
	       
	       
//****************************************************************************************************************************************************************
	       
	 
	}    
        
		
	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_RSP01_VerificaEjecucionExitosaAlEnviarArchivos";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Verificar la ejecución exitosa al enviar archivos de las Plazas ";
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



