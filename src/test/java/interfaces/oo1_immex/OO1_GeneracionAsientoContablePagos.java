
package interfaces.oo1_immex;


import static org.junit.Assert.assertFalse;
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


public class OO1_GeneracionAsientoContablePagos extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_oo1_immex_GeneracionAsientoContablePagos(HashMap<String, String> data) throws Exception {
	
/* Utilerías ********************************************************************************************************************************************/
		
		
		utils.sql.SQLUtil dbEbs= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS,GlobalVariables.DB_USER_EBS , GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbLog= new utils.sql.SQLUtil( GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		
 /* Variables ******************************************************************************************************************************************/
		
	//Paso 1
		
		String infoProcesar = "SELECT ACA.BANK_ACCOUNT_NAME, SUBSTR(SITE.VENDOR_SITE_CODE,0,5) AS VENDOR_SITE, CHECK_DATE\r\n" + 
				"FROM CE.CE_BANK_ACCOUNTS ABA, AP.AP_CHECKS_ALL ACA, CE.CE_BANK_ACCT_USES_ALL CBAU,\r\n" + 
				"  GL.GL_CODE_COMBINATIONS GCC, AP.ap_suppliers SUP, AP.ap_supplier_sites_all SITE    \r\n" + 
				"WHERE ACA.CE_BANK_ACCT_USE_ID=CBAU.BANK_ACCT_USE_ID\r\n" + 
				"AND CBAU.BANK_ACCOUNT_ID=ABA.BANK_ACCOUNT_ID\r\n" + 
				"AND ABA.ASSET_CODE_COMBINATION_ID=GCC.CODE_COMBINATION_ID\r\n" + 
				"AND SITE.VENDOR_SITE_CODE NOT LIKE GCC.SEGMENT3||'%'\r\n" + 
				"AND ACA.STATUS_LOOKUP_CODE IN ('RECONCILED','RECONCILED UNACCOUNTED','NEGOTIABLE')\r\n" + 
				"AND ACA.VOID_DATE IS NULL\r\n" + 
				"AND ACA.ATTRIBUTE2 IS NULL\r\n" + 
				"AND ACA.ATTRIBUTE3 IS NULL\r\n" + 
				"AND ACA.ATTRIBUTE4 IS NULL\r\n" + 
				"AND GCC.SEGMENT3 = '"+data.get("CR_PLAZA") +"' \r\n" + 
				"AND SUP.VENDOR_ID = ACA.VENDOR_ID        \r\n" + 
				"AND SITE.VENDOR_SITE_ID = ACA.VENDOR_SITE_ID  \r\n" + 
				"GROUP BY ACA.BANK_ACCOUNT_NAME, SUBSTR(SITE.VENDOR_SITE_CODE,0,5), CHECK_DATE";
		
	
		//Paso 3
		
		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server \r\n" + 
				"FROM WMLOG.WM_LOG_RUN Tbl WHERE INTERFACE = 'OO1_IMX' \r\n"
				+ "AND status = 'S' \r\n"
				+ "AND trunc(start_dt) >= trunc(SYSDATE)  \r\n" + 
				"ORDER BY START_DT DESC) where rownum <=1";
		
		//Paso 4
		String actAtributosOrafamiex = "SELECT aca.bank_account_name,aca.check_id, aca.attribute2, aca.attribute3\r\n" + 
				"FROM AP_CHECKS_ALL ACA WHERE ATTRIBUTE2 IS NOT NULL \r\n" + 
				"AND status_lookup_code IN ('RECONCILED','RECONCILED UNACCOUNTED','NEGOTIABLE')\r\n" + 
				"AND substr(attribute3,0,8) = TO_CHAR(SYSDATE, 'yyyymmdd')\r\n" + 
				"AND attribute4 IS NULL\r\n" + 
				"AND void_date IS NULL\r\n" + 
				"AND VENDOR_SITE_ID IN ( \r\n" + 
				"  SELECT SITE.VENDOR_SITE_ID \r\n" + 
				"  FROM AP.AP_SUPPLIER_SITES_ALL SITE \r\n" + 
				"  WHERE SITE.VENDOR_SITE_ID = ACA.VENDOR_SITE_ID \r\n" + 
				"  AND SITE.VENDOR_SITE_CODE NOT LIKE '"+data.get("CR_PLAZA") +"' || '%')";
		
		
		//consultas de error
		String consultaError1 = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";
		
		String consultaError2 = " select * from (select description,MESSAGE "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		
		String consultaError3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 "
				+ "from wmlog.WM_LOG_ERROR where RUN_ID='%s')WHERE rownum <= 1";
		
		
		//Paso 5
		
		String insercionLineasPolizas = "SELECT status, ledger_id, accounting_date,actual_flag, reference6 FROM GL.GL_INTERFACE WHERE reference6 = '%s'";
		
		
		
//Pasos *****************************************************************************************************************************************************************		
		
		
		//Paso 1
		
		addStep("Validar que existe información de Cancelaciones pendientes de procesar para la Plaza.");   
		
		//Primera consulta
		 System.out.println(infoProcesar);
	        
	       SQLResult infoProcesar_Result = dbEbs.executeQuery(infoProcesar);
	       
	       boolean  infoProcesar_valida = infoProcesar_Result.isEmpty(); //checa que el string contenga datos
	    		  
	       
	       if(!infoProcesar_valida) {
	    	   
	    	   testCase.addQueryEvidenceCurrentStep(infoProcesar_Result);  //Si no esta vacio, lo agrega a la evidencia
	       }
	      
	       System.out.println(infoProcesar_valida); 

	       
       assertFalse("No se presentará la información pendiente de procesar. ",infoProcesar_valida ); //Si esta vacio, imprime mensaje
       
       
 //***************************************************************************************************************************************************************
       //Paso 2
       
           
       addStep("Ejecutar el servicio OO1_IMX.Pub:run. El servicio será ejecutado a través del job runOO1 desde Ctrl-M.");
       
       
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

	   
	
	      String dateExecution = pok.runIntefaceWmWithInput10(data.get("interfase"), data.get("servicio"), data.get("CR_PLAZA"), "CR_PLAZA");
	      System.out.println("Respuesta dateExecution" + dateExecution);
	      
	     
	      
	       SQLResult is= dbLog.executeQuery(tdcIntegrationServerFormat);
	        String run_id  = null;
	        System.out.println("RUN_ID = "+ run_id );
	        String status1 = null;
	      
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
	      
	      //Paso 3
	        
	  	  addStep("Validar el registro de ejecución de la interfaz en la tabla wm_log_run de WMLOG. ");
	
	  	
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
			
			//Paso 4
			
			addStep("Validar la actualización del atributo 2 y 3 en la tabla ap_checks_all de ORAFINIMMEX.");
			
			System.out.println(actAtributosOrafamiex);
		
			 SQLResult actualizacionAtributosOrafamiex_R =  dbEbs.executeQuery(actAtributosOrafamiex); //Cambiar a Orafimex
			 
			 String  attribute2 = actualizacionAtributosOrafamiex_R.getData(0, "ATTRIBUTE2");
			 
			 System.out.println("ATTRIBUTE2 " + attribute2);
	        
			
			boolean validaActualizacion = actualizacionAtributosOrafamiex_R.isEmpty();
			
			if (!validaActualizacion) {

				testCase.addQueryEvidenceCurrentStep(actualizacionAtributosOrafamiex_R);

			}
			
			System.out.println(validaActualizacion);
			
			assertFalse("Los campos atributo 2 y 3 no fueron actualizados con el id del journal. ", validaActualizacion);
			
			
//************************************************************************************************************************************************************************************			
		
			//Paso 5
			
			addStep("Validar la inserción de las líneas de pólizas en la tabla gl_interface de ORAFINIMMEX.");
		  
			String insercionLineasPolizas_f = String.format(insercionLineasPolizas,attribute2);
			
			System.out.println(insercionLineasPolizas_f);
		                
		    SQLResult insercionLineasPolizas_Result = dbEbs.executeQuery(insercionLineasPolizas_f); //Cambiar a Orafimex
		      
		    boolean insercionLineasPolizas_valida = insercionLineasPolizas_Result.isEmpty(); //checa que el string contenga datos
		    		  
		       
		       if(!insercionLineasPolizas_valida) {
		    	   
		    	   testCase.addQueryEvidenceCurrentStep( insercionLineasPolizas_Result);  //Si no esta vacio, lo agrega a la evidencia
		       }

		       System.out.println(insercionLineasPolizas_valida); 
	       
	       
     assertFalse("Las líneas de pólizas no fueron insertadas en la base de datos de ORAFINIMMEX  ",insercionLineasPolizas_valida ); //Si esta vacio, imprime mensaje
     
//**************************************************************************************************************************************************************************************
     
     
	}    
        
		
	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_oo1_immex_GeneracionAsientoContablePagos";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construida. Validar la generación del asiento contable para los pagos";
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



