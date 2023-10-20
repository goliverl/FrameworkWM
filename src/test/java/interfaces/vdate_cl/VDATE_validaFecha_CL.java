package interfaces.vdate_cl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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


public class VDATE_validaFecha_CL extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_VDATE_CL_001_VDATE_validarfecha_cl(HashMap<String, String> data) throws Exception {
	
/* Utilerías ********************************************************************************************************************************************/
		
		
		utils.sql.SQLUtil dbRmsChile = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMSWMUSERChile,GlobalVariables.DB_USER_RMSWMUSERChile , GlobalVariables.DB_PASSWORD_RMSWMUSERChile);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LogChile,GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile,GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
 /* Variables ******************************************************************************************************************************************/
		
	//Paso 1
		
		String verificaFechaPeriod = "SELECT VDATE FROM PERIOD ";
		
		//(to_char( VDATE,'DD/MM/YYYY HH24:MI:SS')) 
		
	//Paso 2
		
		String fechaPosuser = "SELECT * FROM POSUSER.WM_RETEK_VDATE ";
		
		
	//Paso 3
		
		String infoPosuser = "SELECT b.ID, b.pe_id, b.pv_doc_id, b.status, b.doc_type, b.pv_doc_name, b.received_date, c.pid_id , C.ADJ_DATE "
				+ "FROM POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_ADJ C \r\n" + 
				"WHERE B.DOC_TYPE = 'ADJ' \r\n" + 
				"AND B.STATUS = 'H' \r\n" + 
			//	"--AND SUBSTR(PV_DOC_NAME,4,5) = '10CLP'  \r\n" + 
			//	"--AND SUBSTR(PV_DOC_NAME,9,5) = '50KJI'\r\n" + 
				"AND B.ID = C.PID_ID \r\n" + 
				"AND TRUNC(C.ADJ_DATE) <= (select CAST( TO_TIMESTAMP ('%s','YYYY-MM-DD HH24:MI:SS.FF') AS DATE) from DUAL)\r\n" + 
				"AND ROWNUM  <= 1 ";
		
		//Paso 4, 5
		
		String tdcIntegrationServerFormat = "	SELECT Tbl.run_id,interface, start_dt, end_dt, status, server \r\n" + 
				"FROM WMLOG.WM_LOG_RUN Tbl WHERE interface = 'VDATE_CL'\r\n" + 
				"AND status = 'S' \r\n" + 
				"AND start_dt >= TRUNC(SYSDATE)\r\n" + 
				"ORDER BY run_id DESC ";
		
	//Paso 6
		
		String cambioFechaPosuser = "SELECT * FROM POSUSER.WM_RETEK_VDATE WHERE TRUNC(retek_vdate) = '%s'";
		
		
	//Paso 7
		
		String validaStatus = "SELECT b.ID, b.pe_id, b.pv_doc_id, b.status, b.doc_type, b.pv_doc_name, b.received_date, c.pid_id, C.ADJ_DATE "
				+ "FROM POSUSER.POS_INBOUND_DOCS B, POSUSER.POS_ADJ C \r\n" + 
				"WHERE B.DOC_TYPE = 'ADJ' \r\n" + 
				"AND B.STATUS = 'I' \r\n" + 
				//"--AND SUBSTR(PV_DOC_NAME,4,5) = '"+data.get("plaza") +"'  \r\n" + 
				//"--AND SUBSTR(PV_DOC_NAME,9,5) = '"+data.get("tienda") +"' \r\n" + 
				"AND B.ID = C.PID_ID \r\n"
				+ "AND B.ID = '%s' \r\n"
				+ "AND ROWNUM  <= 1";
		
		//consultas de error
		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";
		
		String consultaError2 = " select * from (select description,MESSAGE "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		
		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		
		
		
		
//Pasos ***********************************************************************************************************************************************************************		
		
		
		//Paso 1
		
		addStep("Verificar que la fecha de VDATE en la tabla PERIOD en RETEK.");   
		
		
		  System.out.println(verificaFechaPeriod);
	        
	       SQLResult verificaFechaPeriod_Result = dbRmsChile.executeQuery(verificaFechaPeriod);
	       
           String VDATE = verificaFechaPeriod_Result.getData(0, "VDATE");
	       
	       System.out.println("VDATE = "+ VDATE );  //la imprime
	        
	           
	       boolean  verificaFechaPeriod_b = verificaFechaPeriod_Result.isEmpty(); //checa que el string contenga datos
	    		  
	       
	       if(!verificaFechaPeriod_b) {
	    	   
	    	   testCase.addQueryEvidenceCurrentStep(verificaFechaPeriod_Result);  //Si no esta vacio, lo agrega a la evidencia
	       }
	      
	       System.out.println(verificaFechaPeriod_b); 
      
	       
       assertFalse("No se devuelve  fecha establecida en la tabla PERIOD  ",verificaFechaPeriod_b ); //Si esta vacio, imprime mensaje
       
       
 //***************************************************************************************************************************************************************
     //Paso 2
		
     		addStep("Comprobar que la fecha en WM_RETEK_VDATE de POSUSER sea DIFERENTE a la que se encuentra en PERIOD de RMS100. ");   
     		
     		
     		 System.out.println(fechaPosuser);
     	        
     	       SQLResult fechaPosuser_Result = dbPos.executeQuery(fechaPosuser);
     	       
               String RETEK_VDATE = fechaPosuser_Result.getData(0, "RETEK_VDATE");
     	       
     	       System.out.println("RETEK_VDATE = "+ RETEK_VDATE );  //la imprime
     	       
     	       
    	           	           
     	       boolean  verificaFechaPosuser = fechaPosuser_Result.isEmpty(); //checa que el string contenga datos
     	    		  
     	       
     	       if(!verificaFechaPosuser) {
     	    	   
     	    	   testCase.addQueryEvidenceCurrentStep(fechaPosuser_Result);  //Si no esta vacio, lo agrega a la evidencia
     	       }
     	      
     	       System.out.println(verificaFechaPosuser); 
     	       
     	       
     	      boolean  validaFechas = false;
	    		  
    	       
    	       if(RETEK_VDATE!=VDATE) {
    	    	   
    	    	   validaFechas = true;
    	    	   
    	    	   testCase.addTextEvidenceCurrentStep("Las fechas en PERIOD y WM_RETEK_VDATE son distintas");  //Si no esta vacio, lo agrega a la evidencia
    	       }
    	      
    	       System.out.println(validaFechas); 
    	       
    	       
           
     	       
            assertTrue("Las fechas en PERIOD y WM_RETEK_VDATE son Iguales",validaFechas ); //Si esta vacio, imprime mensaje
            
            
      //***************************************************************************************************************************************************************
       
            //Paso 3
    		
     		addStep("Validar que exista información en POSUSER con fecha menor al registro en PERIOD.VDATE para la plaza 10CLP y tienda 50IYQ con STATUS H  y doc_type ADJ en las tablas POS_INBOUND_DOCS y POS_ADJ");   
     		
     		
     		
     		 String infoPosuserF = String.format(infoPosuser, VDATE);
     		 
     		 System.out.println(infoPosuserF);
     	        
     	      SQLResult infoPosuser_Result = dbPos.executeQuery(infoPosuserF);
     	       
     	      String ID = infoPosuser_Result.getData(0, "ID");
   	       
   	         System.out.println("ID = "+ ID);  //la imprime
     	       
              
     	      boolean  validainfoPosuser = infoPosuser_Result.isEmpty();
	    		  
    	       
    	       if(!validainfoPosuser) {
    	    	   
    	    	  
    	    	   
    	    	   testCase.addQueryEvidenceCurrentStep(infoPosuser_Result);  //Si no esta vacio, lo agrega a la evidencia
    	       }
    	      
    	       System.out.println(validainfoPosuser); 
    	                    	       
            assertFalse("No devuelve los registros de los documentos que prodran ser procesados", validainfoPosuser ); //Si esta vacio, imprime mensaje
            
            
      //***************************************************************************************************************************************************************
            
       //Paso 4
       
           
       addStep("Ejecutar la interface Femsa_VDATE_CL.Pub:run ");
       
       
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
	      
	      //Paso 5
	        
	  	  addStep("Verificar que el estatus sea igual a 'S' en la tabla WM_LOG_RUN de la BD del WMLOG");
	
	  	
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
			
			//Paso 6 
			
			addStep("Comprobar que la fecha en WM_RETEK_VDATE de POSUSER sea la misma que se encuentra en PERIOD de RMS100. ");
			
			   
		       SQLResult verificaFechaPeriod_Result2 = dbRmsChile.executeQuery(verificaFechaPeriod);
		       
	           String VDATE2 = verificaFechaPeriod_Result2.getData(0, "VDATE");
		       
		       System.out.println("VDATE = "+ VDATE );  //la imprime
		       
		      
		       		       
		        String cambioFechaPosuser_format = String.format(cambioFechaPosuser, VDATE2);
	
		        System.out.println("Fecha actualizada: "+cambioFechaPosuser_format);
			        
			    SQLResult cambioFechaPosuser_Result = dbPos.executeQuery(cambioFechaPosuser_format);
			        
			    
			     
			    boolean  validaCambioFechaPosuser = cambioFechaPosuser_Result.isEmpty(); //checa que el string contenga datos
			    		  
			       
			       if(!validaCambioFechaPosuser) {
			    	   
			    	   testCase.addQueryEvidenceCurrentStep(cambioFechaPosuser_Result);  //Si no esta vacio, lo agrega a la evidencia
			       }

			       System.out.println(validaCambioFechaPosuser); 
		       
			
			       assertFalse("La fecha no coincide con el valor que se encuentra en PERIOD.VDATE ", validaCambioFechaPosuser ); //Si esta vacio, imprime mensaje
			       
			
			
//************************************************************************************************************************************************************************************			
		
			//Paso 7
			
			addStep("Validar que se actualice el Status en POS_INBOUND_DOCS ");
			
			  String validaStatus_format = String.format(validaStatus, ID);
			  
			  System.out.println(validaStatus_format);
		        
		      SQLResult validaStatus_Result = dbPos.executeQuery(validaStatus_format);
		    
		       boolean  validaStatus_b = validaStatus_Result.isEmpty(); //checa que el string contenga datos
		    		  
		       
		       if(!validaStatus_b) {
		    	   
		    	   testCase.addQueryEvidenceCurrentStep(validaStatus_Result);  //Si no esta vacio, lo agrega a la evidencia
		       }

		       System.out.println(validaStatus_b); 
	       
	       
     assertFalse("No se actualizaron los registros con las carateristicas solicitadas ", validaStatus_b ); //Si esta vacio, imprime mensaje
  
	}    
        
		
	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_VDATE_CL_001_VDATE_validarfecha_cl";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Sincroniza la fecha de RMS con posuser y cambia documentos de status H a I";
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



