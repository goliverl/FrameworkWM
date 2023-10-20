package interfaces.re4;

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
 


public class RE4_EnviaTransferenciasDeProductoAProveedores extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_RE4_Envia_Transferencias_De_ProductoA_Proveedores(HashMap<String, String> data) throws Exception {
	
/* Utilerías ********************************************************************************************************************************************/
		
		
		
		utils.sql.SQLUtil dbLog= new utils.sql.SQLUtil( GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,GlobalVariables.DB_USER_RMS_MEX, GlobalVariables.DB_PASSWORD_RMS_MEX);

 /* Variables ******************************************************************************************************************************************/
		
	//Paso 1		
		String valorCedis = "SELECT A.SUP_ID, C.RETEK_CR, C.ID, C.RETEK_PHYSICAL_CR \r\n" + 
				"FROM POSUSER.WM_EDI_SUPPLIER_DAYS A, POSUSER.WM_EDI_SUPPLIER_CEDIS B, POSUSER.CEDIS C, POSUSER.WM_EDI_SUPPLIER_FUNCTION E, POSUSER.WM_EDI_FUNCTIONS F \r\n" + 
				"WHERE A.TE_ID = 'T' \r\n" + 
				"AND A.SUP_ID = B.SUP_ID \r\n" + 
				"AND B.CEDIS_ID = C.ID \r\n" + 
				"AND E.SUP_ID = B.SUP_ID \r\n" + 
				"AND E.FUNCTION_ID = F.ID \r\n" + 
				"AND F.FUNCTION = 'TSF_OUT' " ;   
				//AND A.SUP_ID = 327;
		
		//Paso 2
		String registrosPos1  = "SELECT d.sup_id, d.cedis, d.last_sent_date, d.transaction, d.status, d.doc_name \r\n" + 
				"FROM POSUSER.WM_EDI_CONTROL_ENVIO D, POSUSER.WM_EDI_SUPPLIER_DAYS A \r\n" + 
				"WHERE A.SUP_ID = D.SUP_ID \r\n" + 
				"AND D.CEDIS = '%s' \r\n" + 
				"AND D.TRANSACTION = 'SLSRPT' ";
		
		String registrosPos2 = "SELECT a.sup_id, a.te_id, a.lunes , a.martes, a.miercoles, a.jueves, a.viernes, a.sabado, a.domingo\r\n" + 
				"FROM POSUSER.WM_EDI_CONTROL_ENVIO D, POSUSER.WM_EDI_SUPPLIER_DAYS A \r\n" + 
				"WHERE A.SUP_ID = D.SUP_ID \r\n" + 
				"AND D.CEDIS = '%s' \r\n" + 
				"AND D.TRANSACTION = 'SLSRPT'";
		
		//Paso 3
		String  registrosRetek1= "SELECT s.supplier, s.filetype, s.status, s.cost, s.id, s.surtirporpieza, s.format_type "
				+ "FROM WMUSER.WM_EDI_MAP_SUPPLIER S, WMUSER.WM_EDI_INTERCHANGE I \r\n" + 
				"WHERE S.SUPPLIER = I.SUPPLIER AND S.STATUS = I.STATUS_TYPE AND S.ID = '%s'";
		
		String registrosRetek2 = "SELECT s.max_attempt,i.supplier, i.status_type, i.command_id, i.qualifier  "
				+ "FROM WMUSER.WM_EDI_MAP_SUPPLIER S, WMUSER.WM_EDI_INTERCHANGE I \r\n" + 
				"WHERE S.SUPPLIER = I.SUPPLIER AND S.STATUS = I.STATUS_TYPE AND S.ID = '%s'";
		
		
		//Paso 4
		
		String wm_status_i = "SELECT cedis, plaza, tienda, fecha_creacion, depto, status_cedis, status_tienda, item, wm_status  \r\n" + 
				"FROM XXFC.XXFC_FALT_CEDIS  \r\n" + 
				"WHERE CEDIS = '%s' \r\n" + 
				"AND ITEM = '%s' \r\n" + 
				"AND INSERT_DATE >= SYSDATE-7 \r\n" + 
				"AND WM_STATUS = 'I'";
	
		//Paso 5
		
		String items ="SELECT item, supplier, origin_country_id, unit_cost, ti, hi, create_datetime \r\n" + 
				"FROM RMS100.ITEM_SUPP_COUNTRY WHERE SUPPLIER = '%s'"
				+ "order by create_datetime desc ";
		
		//Paso 6 y 7
		
		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server \r\n" + 
				"FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE = 'RE4' " +
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
		
		
		String EDI_OUTBOUND_DOCS1 = "SELECT doc_name, doc_type, edi_control, supplier, status FROM WMUSER.EDI_OUTBOUND_DOCS"
				+ " WHERE TRUNC(DATE_SENT) >= SYSDATE - 7 \r\n" + 
				"AND DATE_RECEIVED >= SYSDATE - 7  \r\n" + 
				"AND RUN_ID = '%s' \r\n" + 
				"AND SUPPLIER = '%s'\r\n" + 
				"AND STATUS = 'E' ";
		
		String EDI_OUTBOUND_DOCS2 = "SELECT date_sent, run_id, task_id FROM WMUSER.EDI_OUTBOUND_DOCS  "
				+ "WHERE TRUNC(DATE_SENT) >= SYSDATE - 7 \r\n" + 
				"AND DATE_RECEIVED >= SYSDATE - 7  \r\n" + 
				"AND RUN_ID = '%s' \r\n" + 
				"AND SUPPLIER = '%s'\r\n" + 
				"AND STATUS = 'E'";
		
		//Paso 10
		String wm_status_e = "SELECT cedis, plaza, tienda, fecha_creacion, depto, status_cedis, status_tienda, item, wm_status  \r\n" + 
				"FROM XXFC.XXFC_FALT_CEDIS \r\n" + 
				"WHERE WM_STATUS = 'E' \r\n" + 
				"AND CEDIS = '%s' \r\n" + 
				"AND INSERT_DATE >= SYSDATE - 7 ";
		
		//Paso 11
		String WM_EDI_CONTROL_ENVIO = "SELECT * FROM POSUSER.WM_EDI_CONTROL_ENVIO WHERE LAST_SENT_DATE >= SYSDATE-1 \r\n" + 
				"AND STATUS = 'E' \r\n" + 
				"AND TRANSACTION = 'SLSRPT' \r\n" + 
				"AND SUP_ID = '%s' \r\n" + 
				"AND CEDIS = '%s'";
		
//Pasos ***********************************************************************************************************************************************************************		
		
		
		//Paso 1
		
		addStep("Consiultar el valor de CEDIS en la tabla CEDIS de la BD POSUSER ");   
		
		
		 System.out.println(valorCedis);
	        
	       SQLResult valorCedis_Result = dbPos.executeQuery(valorCedis);
	       
	       //Obtentenemos el SUP_ID
	       String SUP_ID = valorCedis_Result.getData(0, "SUP_ID");
	       
	       System.out.println("SUP_ID= "+ SUP_ID ); 
	       
	       //Obtenemos el RETEK_PHYSICAL_CR
           String RETEK_PHYSICAL_CR = valorCedis_Result.getData(0, "RETEK_PHYSICAL_CR");
	       
	       System.out.println("RETEK_PHYSICAL_CR= "+ RETEK_PHYSICAL_CR );
	       
	       //Obtenemos el RETEK_CR
          String RETEK_CR = valorCedis_Result.getData(0, "RETEK_CR");
	       
	       System.out.println("RETEK_CR= "+ RETEK_CR );
	       
	     
	       boolean  valorCedis_valida = valorCedis_Result.isEmpty(); //checa que el string contenga datos
	    		  
	       
	       if(!valorCedis_valida) {
	    	   
	    	   testCase.addQueryEvidenceCurrentStep(valorCedis_Result);  //Si no esta vacio, lo agrega a la evidencia
	       }
	      
	       System.out.println(valorCedis_valida); 
       
	       
       assertFalse(valorCedis_valida, "No se encontraron el CEDIS " ); //Si esta vacio, imprime mensaje
       
       
//****************************************************************************************************************************************************************
     //Paso 2
		
     		addStep("Consultar que existan registros en la tabla WM_EDI_CONTROL_ENVIO de la BD POSUSER. ");   
     		 //Primera parte
     		  String registrosPos1_f = String.format(registrosPos1,RETEK_CR);
     		  
     		  System.out.println(registrosPos1_f);
     	        
     	       SQLResult registrosPos1_R = dbPos.executeQuery(registrosPos1_f);
     	       
               boolean  registrosPos1_valida = registrosPos1_R.isEmpty(); //checa que el string contenga datos
     	    		  
     	       
     	       if(!registrosPos1_valida) {
     	    	   
     	    	   testCase.addQueryEvidenceCurrentStep(registrosPos1_R);  //Si no esta vacio, lo agrega a la evidencia
     	       }
     	      
     	       System.out.println(registrosPos1_valida); 
            
     	       //Segunda parte
              String registrosPos2_f = String.format(registrosPos2,RETEK_CR);
     		  
     		  System.out.println(registrosPos2_f);
     	        
     	       SQLResult registrosPos2_R = dbPos.executeQuery(registrosPos2_f);
     	       
               boolean  registrosPos2_valida = registrosPos2_R.isEmpty(); //checa que el string contenga datos
     	    		  
     	       
     	       if(!registrosPos2_valida) {
     	    	   
     	    	   testCase.addQueryEvidenceCurrentStep(registrosPos2_R);  //Si no esta vacio, lo agrega a la evidencia
     	       }
     	      
     	       System.out.println(registrosPos2_valida); 
     	       
            assertFalse(registrosPos2_valida, "No se encontraron registros para procesar "); //Si esta vacio, imprime mensaje
            
//****************************************************************************************************************************************************************
            //Paso 3
       		
            		addStep("Comprobar que existan registros en las tablas WM_EDI_MAP_SUPPLIER y WM_EDI_INTERCHANGE de la BD RETEK para el ID igual a " + SUP_ID );   
            		 //Primera parte
            		  String registrosRetek1_f = String.format(registrosRetek1,SUP_ID);
            		  
            		  System.out.println(registrosRetek1_f);
            	        
            	      SQLResult registrosRetek1_R = dbRms.executeQuery(registrosRetek1_f);
            	       
                      String SUPPLIER = registrosRetek1_R.getData(0, "SUPPLIER");
             	       
             	       System.out.println("SUPPLIER= "+ SUPPLIER);
                   	       
            	       
                      boolean  registrosRetek1_valida = registrosRetek1_R.isEmpty(); //checa que el string contenga datos
            	    		  
            	       
            	       if(!registrosRetek1_valida) {
            	    	   
            	    	   testCase.addQueryEvidenceCurrentStep(registrosRetek1_R);  //Si no esta vacio, lo agrega a la evidencia
            	       }
            	      
            	       System.out.println(registrosRetek1_valida); 
                   
            	       //Segunda parte
                     String registrosRetek2_f = String.format(registrosRetek2,SUP_ID);
            		  
            		  System.out.println(registrosRetek2_f);
            	        
            	       SQLResult registrosRetek2_R = dbRms.executeQuery(registrosRetek2_f);
            	       
                      boolean registrosRetek2_valida = registrosRetek2_R.isEmpty(); //checa que el string contenga datos
            	    		  
            	       
            	       if(!registrosRetek2_valida) {
            	    	   
            	    	   testCase.addQueryEvidenceCurrentStep(registrosRetek2_R);  //Si no esta vacio, lo agrega a la evidencia
            	       }
            	      
            	       System.out.println(registrosRetek2_valida); 
            	       
                   assertFalse(registrosRetek2_valida, "No se encontraron registros para procesar "); //Si esta vacio, imprime mensaje
                   
//****************************************************************************************************************************************************************
                   //Paso 4
              		
                   		addStep("Comprobar que existan registros en la tabla ITEM_SUPP_COUNTRY de la BD RETEK en donde SUPPLIER es igual a [WM_EDI_MAP_SUPPLIER.SUPPLIER] ");   
                   		
                   		  String items_f = String.format(items,SUPPLIER);
                   		  
                   		  System.out.println(items_f);
                   	        
                   	       SQLResult items_R = dbRms.executeQuery(items_f);
                   	       
                   	       String ITEM = items_R.getData(0, "ITEM");
              	       
              	           System.out.println("ITEM= "+ ITEM);
                   	       
                   	    
                             boolean  items_valida = items_R.isEmpty(); //checa que el string contenga datos
                   	    		  
                 	       
                   	       if(! items_valida) {
                   	    	   
                   	    	   testCase.addQueryEvidenceCurrentStep(items_R);  //Si no esta vacio, lo agrega a la evidencia
                   	       }
                   	      
                   	       System.out.println(items_valida); 
                   	       
                          assertFalse(items_valida, "No se encontraron registros para procesar "); //Si esta vacio, imprime mensaje
                                             
   
       
 //***************************************************************************************************************************************************************
                          
                        //Paso 5
                    		
                     		addStep("Comprobar que existan registros en la tabla  XXFC_FALT_CEDIS de la BD RETEK con WM_STATUS es igual a I ");   
                     		
                     		  String wm_status_i_f = String.format(wm_status_i,RETEK_PHYSICAL_CR, ITEM);
                     		  
                     		  System.out.println(wm_status_i_f);
                     	        
                     	       SQLResult wm_status_i_R = dbRms.executeQuery(wm_status_i_f);
                     	       
                               boolean  wm_status_i_valida = wm_status_i_R.isEmpty(); //checa que el string contenga datos
                     	    		  
                   	       
                     	       if(! wm_status_i_valida) {
                     	    	   
                     	    	   testCase.addQueryEvidenceCurrentStep(items_R);  //Si no esta vacio, lo agrega a la evidencia
                     	       }
                     	      
                     	       System.out.println(wm_status_i_valida); 
                     	       
                            assertFalse(wm_status_i_valida, "No se encontraron registros para procesar "); //Si esta vacio, imprime mensaje
                                               
     
         
   //***************************************************************************************************************************************************************
       //Paso 6
       
           
       addStep("Ejecutar el servicio RE4.Pub:run");
       
       
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

	   
	
	      String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
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
	      
	      //Paso 7
	        
	  	  addStep("Comprobar que existe registro de la ejecucion correcta en la tabla WM_LOG_RUN de la BD WMLOG en status S");
	
	  	
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
			
			//Paso 8
			
			addStep("Comprobar que exista registro en tabla WM_LOG_THREAD de la BD WMLOG, donde PARENT_ID es igual a WM_LOG_RUN.RUN_ID, STATUS igual a 'S'.");
			
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
			
			
//************************************************************************************************************************************************************************************			
		//Paso 9
			
			addStep("Comprobar que se INSERTEN los registros en la tabla EDI_OUTBOUND_DOCS de la BD RETEK en donde DATE_SENT es igual a la fecha actual, y SUPPLIER es igual a [WM_EDI_MAP_SUPPLIER.SUPPLIER]");   
			
			//Primera parte
			String EDI_OUTBOUND_DOCS1_f = String.format(EDI_OUTBOUND_DOCS1, run_id, SUPPLIER);
			
			 System.out.println(EDI_OUTBOUND_DOCS1);
		        
		       SQLResult EDI_OUTBOUND_DOCS1_r = dbRms.executeQuery(EDI_OUTBOUND_DOCS1_f);
		       
		       boolean  EDI_OUTBOUND_DOCS1_valida = EDI_OUTBOUND_DOCS1_r.isEmpty(); //checa que el string contenga datos
		    		  
		       
		       if(!EDI_OUTBOUND_DOCS1_valida) {
		    	   
		    	   testCase.addQueryEvidenceCurrentStep(EDI_OUTBOUND_DOCS1_r);  //Si no esta vacio, lo agrega a la evidencia
		       }
		      
		       System.out.println(EDI_OUTBOUND_DOCS1_valida); 
		       
		       //Segunda parte
		       
		       String EDI_OUTBOUND_DOCS2_f = String.format(EDI_OUTBOUND_DOCS2, run_id, SUPPLIER);
				
				 System.out.println(EDI_OUTBOUND_DOCS2);
			        
			       SQLResult EDI_OUTBOUND_DOCS2_r = dbRms.executeQuery(EDI_OUTBOUND_DOCS2_f);
			       
			       boolean  EDI_OUTBOUND_DOCS2_valida = EDI_OUTBOUND_DOCS2_r.isEmpty(); //checa que el string contenga datos
			    		  
			       
			       if(!EDI_OUTBOUND_DOCS2_valida) {
			    	   
			    	   testCase.addQueryEvidenceCurrentStep(EDI_OUTBOUND_DOCS2_r);  //Si no esta vacio, lo agrega a la evidencia
			       }
			      
			       System.out.println(EDI_OUTBOUND_DOCS2_valida); 
		       
	       
		       
	       assertFalse(EDI_OUTBOUND_DOCS2_valida, "No se actualizaron ls datos en EDI_OUTBOUND_DOCS correctamente " ); //Si esta vacio, imprime mensaje
	       
	       
//****************************************************************************************************************************************************************
	       
	       //Paso 10

   		
    		addStep("Comprobar que se Actualicen los registros en la tabla XXFC_FALT_CEDIS de la BD RETEK con WM_STATUS es igual a E. ");   
    		
    		  String wm_status_e_f = String.format(wm_status_e, RETEK_CR);
    		  
    		  System.out.println(wm_status_e_f);
    	        
    	       SQLResult wm_status_e_R = dbRms.executeQuery(wm_status_i_f);
    	       
              boolean  wm_status_e_f_valida = wm_status_e_R.isEmpty(); //checa que el string contenga datos
    	    		  
  	       
    	       if(!wm_status_e_f_valida) {
    	    	   
    	    	   testCase.addQueryEvidenceCurrentStep(wm_status_e_R);  //Si no esta vacio, lo agrega a la evidencia
    	       }
    	      
    	       System.out.println(wm_status_e_f_valida); 
    	       
           assertFalse(wm_status_e_f_valida, "No se actualizaron ls datos en XXFC_FALT_CEDIS "); //Si esta vacio, imprime mensaje
	       
	       


//***************************************************************************************************************************************************************
           
           //Paso 11
           
        	
   		addStep("Comprobar que se ACTUALICEN los registros en la tabla WM_EDI_CONTROL_ENVIO de la BD POSUSER con STATUS igual a E  ");   
   		
   		  String WM_EDI_CONTROL_ENVIO_f = String.format(WM_EDI_CONTROL_ENVIO, SUPPLIER, RETEK_CR);
   		  
   		  System.out.println(WM_EDI_CONTROL_ENVIO_f);
   	        
   	       SQLResult WM_EDI_CONTROL_ENVIO_R = dbRms.executeQuery(WM_EDI_CONTROL_ENVIO_f);
   	       
             boolean  WM_EDI_CONTROL_ENVIO_valida = WM_EDI_CONTROL_ENVIO_R.isEmpty(); //checa que el string contenga datos
   	    		  
 	       
   	       if(!WM_EDI_CONTROL_ENVIO_valida) {
   	    	   
   	    	   testCase.addQueryEvidenceCurrentStep(wm_status_e_R);  //Si no esta vacio, lo agrega a la evidencia
   	       }
   	      
   	       System.out.println(WM_EDI_CONTROL_ENVIO_valida); 
   	       
          assertFalse(WM_EDI_CONTROL_ENVIO_valida, "No se actualizaron ls datos en WM_EDI_CONTROL_ENVIO "); //Si esta vacio, imprime mensaje
	       
   
	
	}    
        
		
	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_RE4_Envia_Transferencias_De_ProductoA_Proveedores";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Enviar las transferencias  de cada producto a sus respectivos proveedores ";
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



