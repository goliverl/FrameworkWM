package interfaces.ro8gas;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class RO8GAS_Hist extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_RO8_GAS_005_Hist(HashMap<String, String> data) throws Exception {

		/*
		 * UtilerÃƒÂ­as
		 ***********************************************************************************************/

		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * Variables
		 * ***********************************************************************************************
		 * 
		 * 
		 */
		
		//Paso 1
		
		String statusE = "select header_id, tran_code, tran_date, cr_plaza, wm_creation_date, wm_run_id, wm_status FROM WMUSER.WM_GL_HEADERS_GAS WHERE WM_STATUS = 'E' ";
		
		
		//Paso 2
		
		//consultas de error
		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";
		
		String consultaError2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";
		
		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";
		
		//Paso 3

		String  tdcIntegrationServerFormat = "select*from (SELECT run_id,start_dt,status "
				+ "FROM WMLOG.wm_log_run "
				+ "where interface = 'RO08_GAS_HIST' "
				+ " order by start_dt desc) where rownum = 1 ";

		String status = "S";
		
		
		//Paso 4
		
		String headers = "select header_id, tran_code, tran_date, cr_plaza, wm_creation_date, wm_run_id, wm_status "
				+ "from WMUSER.WM_GL_HEADERS_GAS_HIST WHERE trunc(wm_creation_date) = '%s' ";
		
		//Paso 5
		
		
		String linea = "SELECT line_id, header_id, net_retail, net_cost, total_retail, litros_gas "
				+ "FROM  WMUSER.WM_GL_LINES_GAS_HIST  WHERE header_id= '%s'";
		
		//Paso 6
		
		String  eliminaLineas = "SELECT line_id, header_id, net_retail, net_cost, total_retail, total_cost "
				+ "FROM WMUSER.WM_GL_LINES_GAS WHERE HEADER_ID IN (SELECT HEADER_ID FROM WMUSER.WM_GL_HEADERS_GAS WHERE WM_STATUS = 'E')";
		
		//Paso 7
		
		String eliminaHeaders = "select header_id, tran_code, tran_date, cr_plaza,wm_creation_date, wm_run_id, wm_status "
				+ "from WMUSER.WM_GL_HEADERS_GAS WHERE WM_STATUS = 'E' ";
		
		
		
		

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 */

//					Paso 1	************************
		
		
		 addStep("Tener datos para procesar con WM_STATUS = E en la tabla WM_GL_HEADERS_GAS");        

	        
   //Parte 1
		 
	        System.out.println(GlobalVariables.DB_HOST_RMS_MEX);//RMS
	        
	         System.out.println(statusE);	     
	         
	        SQLResult statusER = dbRms.executeQuery(statusE);
	        
	        String wm_creation_date = statusER.getData(0, "wm_creation_date");
	        
	        String substring = wm_creation_date.substring(0, 10);
	        
	        System.out.println("Create_date" + substring);
	      
	       boolean validaSatusE =  statusER.isEmpty(); //checa que el string contenga datos
	    		  
	       System.out.println(validaSatusE); 
	       
	       if(!validaSatusE) {
	    	   
	    	   testCase.addQueryEvidenceCurrentStep(statusER);  //Si no esta vacio, lo agrega a la evidencia
	    	   
	       }
	       
	
	       
	       
	       assertFalse("No se muestran datos  para procesar", validaSatusE ); //Si esta vacio, imprime mensaje
	       
	  //******************************************Paso 2*************************************************************************************
	       
	       addStep("Ejecutar la interface con el servicio RO8_GAS.Pub:runHIST. Solicitar la ejecucion del job: runRO8_GAS_HIST ");
		     
		     // Utileria
		      SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		      PakageManagment pok = new PakageManagment(u, testCase);
		      


		      String user = data.get("user");
		      String ps = PasswordUtil.decryptPassword(data.get("ps"));
		      String server = data.get("server");
		      String searchedStatus = "R";

		  
		      System.out.println(GlobalVariables.DB_HOST_LOG);
		      String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		      u.get(contra);

		   
		      String dateExecution = pok.runIntefaceWM2(data.get("interfase"), data.get("servicio"), null);
		      System.out.println("Date excecution" +  dateExecution);		      
		     
		      
		       SQLResult is= dbLog.executeQuery(tdcIntegrationServerFormat);
		        String run_id  = is.getData(0, "RUN_ID");
		        String status1 = is.getData(0,  "STATUS");
		      
		        boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		      
		        while (valuesStatus) {
		       
		    	  

		          status1= is.getData(0, "STATUS");
		          run_id= is.getData(0, "RUN_ID");
		          valuesStatus = status1.equals(searchedStatus);

		      u.hardWait(2);
		       
		     }    
		      
		      boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		      
		      if (!successRun) {

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
//*********************************************Paso 3************************************************************************************
		      addStep("Verificar que la ejecucion halla terminado con exito.");
		  	
			  	
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
				
//************************************Paso 4 ******************************************************************************************
				addStep("Verificar la insercion de los headers en WM_GL_HEADERS_GAS_HIST ");   
				
				 System.out.println(headers);
				 
				 String conCreationDate = String.format(headers,substring);
				 
				 System.out.println(conCreationDate);
			        
			       SQLResult headers_r = dbRms.executeQuery(conCreationDate);
			       
			       String header_id = headers_r.getData(0,"header_id");
			       
			       System.out.println("HEADER_ID "+ header_id);  //la imprime
		     
			       boolean  valida_headers = headers_r.isEmpty(); //checa que el string contenga datos
			    		  
			       
			       if(!valida_headers) {
			    	   
			    	   testCase.addQueryEvidenceCurrentStep(headers_r);  //Si no esta vacio, lo agrega a la evidencia
			       }
			      
			       System.out.println(valida_headers); 
			       
			       assertFalse("No se inserataron lo headers en WM_GL_HEADERS_GAS_HIST",valida_headers); //Si esta vacio, imprime mensaje
			       
//****************************** Paso 5 ***********************************************************************************************
			       
			       addStep("Verificar la insercion de lineas en la tabla WM_GL_LINES_GAS_HIST ");   
					
					 String linea_f= String.format(linea,header_id );
					 
					 System.out.println(linea_f);
				        
				     SQLResult linea_r = dbRms.executeQuery(linea_f);
			     
				       boolean  valida_linea = linea_r.isEmpty(); //checa que el string contenga datos
				    		  
				       
				       if(!valida_linea) {
				    	   
				    	   testCase.addQueryEvidenceCurrentStep(linea_r);  //Si no esta vacio, lo agrega a la evidencia
				       }
				      
				       System.out.println(valida_linea); 
				       
				       assertFalse("No se insertaron las lineas en WM_GL_LINES_GAS_HIST  ",valida_linea); //Si esta vacio, imprime mensaje
				       
//***************************Paso 6 ************************************************************************************************************
		    addStep("Verificar la eliminacion de las lineas en la tabla WM_GL_LINES_GAS .");   
						
			
		    SQLResult eliminaLineas_r= dbRms.executeQuery(eliminaLineas);
				     
		    boolean  valida_eliminaLineas = eliminaLineas_r.isEmpty(); //checa que el string contenga datos
					    		  
					       

			 if(valida_eliminaLineas) {

		
					    	   
					    testCase.addQueryEvidenceCurrentStep(eliminaLineas_r);  //Si no esta vacio, lo agrega a la evidencia
			}
					      
				System.out.println(valida_eliminaLineas); 
					       

			//   assertTrue(valida_eliminaLineas, "No se eliminaron las lineas "); //Si esta vacio, imprime mensaje

			    
//***************************Paso 7*******************************************************************************************************************
			    
			    
			    addStep("Verificar la eliminacion de los headers en la tabla WM_GL_HEADERS_GAS.");   
				
				
			    SQLResult eliminaHeaders_r= dbRms.executeQuery(eliminaHeaders);
					     
			    boolean  valida_eliminaHeaders =eliminaHeaders_r.isEmpty(); //checa que el string contenga datos
						    		  
						       

				 if(valida_eliminaHeaders) {

						    	   
						    testCase.addQueryEvidenceCurrentStep(eliminaHeaders_r);  //Si no esta vacio, lo agrega a la evidencia
				}
						      
					System.out.println(valida_eliminaHeaders); 
						       

				    assertTrue(valida_eliminaHeaders, "No se eliminaron los headers "); //Si esta vacio, imprime mensaje

				       
				       
		   	 
	       
	     
	       
	      
	      

		
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_RO8_GAS_005_Hist";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Verificar proceso de interface HIST ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION OXXO";
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
