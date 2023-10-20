package interfaces.ol6;


import static org.junit.Assert.assertFalse;
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


public class OL6_ASI extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_OL6_OL6_ASI(HashMap<String, String> data) throws Exception {
	
/* Utilerías ********************************************************************************************************************************************/
		
		
		utils.sql.SQLUtil dbEbs= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS,GlobalVariables.DB_USER_EBS , GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbLog= new utils.sql.SQLUtil( GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		//Crear variable para INVSOL y agregarla al paso 2
	

 /* Variables ******************************************************************************************************************************************/
		
		
//PASO 1
		 //general
		/*String info_XXFC_SOLINV_CARGOCEN = "SELECT * FROM  XXFC_SOLINV_CARGOCEN\r\n" + 
				"WHERE INVOICE_ID = '"+data.get("invoice_id") +"'\r\n" + 
				"  AND WM_STATUS_VAL = 'L'\r\n" + 
				"ORDER BY GRUPO ";*/
		
		//Parte 1
		String info_XXFC_SOLINV_CARGOCEN_1="select invoice_id, si_number, total_amount, creation_date, wm_sent_date, wm_run_id " + 
				" from XXFC_SOLINV_CARGOCEN " + 
				" WHERE INVOICE_ID = '"+data.get("invoiceID") +"' " + 
				" AND WM_STATUS_VAL = 'S' " + 
				" ORDER BY GRUPO ";
		
		String info_XXFC_SOLINV_CARGOCEN_2= "select wm_status_val, wm_status_exec, status_actualizacion, grupo, qty_affected, invoice_number " + 
				" from XXFC_SOLINV_CARGOCEN " + 
				" WHERE INVOICE_ID = '"+data.get("invoiceID") +"' " + 
				" AND WM_STATUS_VAL = 'S' " + 
				" ORDER BY GRUPO ";
		
		
//PASO 2
		
		String info_REG1_INV_SOL = "SELECT * FROM    REG1_INV_SOL \r\n" + 
				"WHERE INV_SOL1_NUMERO = '%s'\r\n" + 
				"AND INV_SOL1_CONSECUTIVO = 0 ";
		
		String info_REG2_INV_SOL = "SELECT * FROM REG2_INV_SOL \r\n" + 
				"WHERE  INV_SOL2_NUMERO = '%s'\r\n" + 
				"AND INV_SOL2_CONSECUTIVO <> 0 " ;
		
		
//PASO 3
		
		
		
		
		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%OL6%' "
				+ "ORDER BY START_DT DESC) where rownum <=1";
		
		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA
		
		
		
//PASO 4
		
		
		String tdcQueryStatusLog = "SELECT * FROM WM_LOG_RUN " + 
				" WHERE TRUNC(START_DT) = TRUNC(SYSDATE) " + 
				" AND INTERFACE = 'OL6' ORDER BY RUN_ID DESC ";
		
		
//PASO 5
		
	/*	String act_XXFC_SOLINV_CARGOCEN = "SELECT * FROM  XXFC_SOLINV_CARGOCEN " + 
				" WHERE INVOICE_ID = '"+ data.get("invoice_id") +"' " + 
				" AND WM_STATUS_VAL = 'S' " + 
				" AND WM_RUN_ID = '%s' " + 
				" AND TRUNC(WM_SENT_DATE) = TRUNC(SYSDATE)";*/
		
		
		
		String  act_XXFC_SOLINV_CARGOCEN_1 = "SELECT invoice_id, si_number, total_amount, creation_date, wm_sent_date, wm_run_id "
				+ "FROM  XXFC_SOLINV_CARGOCEN " + 
				" WHERE INVOICE_ID = '"+ data.get("invoiceID") +"' " + 
				" AND WM_STATUS_VAL = 'S' " + 
				" AND WM_RUN_ID = '%s' " + 
				" AND TRUNC(WM_SENT_DATE) = TRUNC(SYSDATE)";
		
		
		String act_XXFC_SOLINV_CARGOCEN_2 = "SELECT wm_status_val, wm_status_exec, status_actualizacion, grupo, qty_affected, invoice_number "
				+ "FROM  XXFC_SOLINV_CARGOCEN " + 
				" WHERE INVOICE_ID = '"+ data.get("invoiceID") +"' " + 
				" AND WM_STATUS_VAL = 'S' " + 
				" AND WM_RUN_ID = '%s' " + 
				" AND TRUNC(WM_SENT_DATE) = TRUNC(SYSDATE)";
		
		
//***********************************************************************************************************************************************************************		
		
		//PASO 1
		 addStep("Tener información pendiente de procesar con el campo WM_STATUS_VAL igual a L y el INVOICE_ID 95537 en la BD ORAFIN.");        

	        

	        System.out.println(GlobalVariables.DB_HOST_EBS);//EBS
	       
	        //Primera parte de la consulta
	        SQLResult info_XXFC_SOLINV_CARGOCEN_Result_1= dbEbs.executeQuery(info_XXFC_SOLINV_CARGOCEN_1);
	        
	        System.out.println(info_XXFC_SOLINV_CARGOCEN_1);  //la imprime
	        
	        String SI_NUMBER = info_XXFC_SOLINV_CARGOCEN_Result_1.getData(0, "SI_NUMBER"); //Obtenemos el SI_NUMBER
	       
	        System.out.println("SI_NUMBER: "+ SI_NUMBER);  //la imprime
	        
	        boolean  info_XXFC_SOLINV_CARGOCEN_Valida_1=  SI_NUMBER.isEmpty(); //checa que el string contenga datos
  		  
		       
		       if(!info_XXFC_SOLINV_CARGOCEN_Valida_1) {
		    	   
		    	   testCase.addQueryEvidenceCurrentStep(info_XXFC_SOLINV_CARGOCEN_Result_1);  //Si no esta vacio, lo agrega a la evidencia
		       }
		       
		       System.out.println(info_XXFC_SOLINV_CARGOCEN_Valida_1); 
			      
	        
          ///Segunda parte de la consulta
		       
	        SQLResult info_XXFC_SOLINV_CARGOCEN_Result_2 = dbEbs.executeQuery(info_XXFC_SOLINV_CARGOCEN_2);
	        
	        System.out.println(info_XXFC_SOLINV_CARGOCEN_2);  //la imprime
	       
	       boolean  info_XXFC_SOLINV_CARGOCEN_Valida_2=  info_XXFC_SOLINV_CARGOCEN_Result_2.isEmpty(); //checa que el string contenga datos
	    		  
	       
	       if(!info_XXFC_SOLINV_CARGOCEN_Valida_2) {
	    	   
	    	   testCase.addQueryEvidenceCurrentStep(info_XXFC_SOLINV_CARGOCEN_Result_2);  //Si no esta vacio, lo agrega a la evidencia
	       }
	       
	       assertFalse("No se muestran registros que cumplan el rango de ejecucion.", info_XXFC_SOLINV_CARGOCEN_Valida_2 ); //Si esta vacio, imprime mensaje
	       
	      System.out.println(info_XXFC_SOLINV_CARGOCEN_Valida_2); 
	      
	      
//***********************************************************************************************************************************************************************	 
	     
	      //Paso 2
	      
	    //Nota revisar base de datos y cambiarla por   INVSOL
	      
	      addStep("Validar que existe información en la tabla REG1_INV_SOL  de INVSOL ");        

	        String info_REG1_INV_SOL_format = String.format(info_REG1_INV_SOL, SI_NUMBER);
	        
	        System.out.println(info_REG1_INV_SOL_format);
	        
	        SQLResult info_REG1_INV_SOL_Result = dbEbs.executeQuery(info_REG1_INV_SOL_format);
	        
	     
	       boolean  info_REG1_INV_SOL_Valida=  info_REG1_INV_SOL_Result.isEmpty(); //checa que el string contenga datos
	    		  
	       
	       if(!info_REG1_INV_SOL_Valida) {
	    	   
	    	   testCase.addQueryEvidenceCurrentStep(info_REG1_INV_SOL_Result);  //Si no esta vacio, lo agrega a la evidencia
	       }
	       
	       assertFalse("No se muestran registros que cumplan el rango de ejecucion.",info_REG1_INV_SOL_Valida ); //Si esta vacio, imprime mensaje
	       
	      System.out.println(info_REG1_INV_SOL_Valida); 
	      
//***********************************************************************************************************************************************************************
	      
	      addStep("Validar que existe información en la tabla REG2_INV_SOL de INVSOL.");  
	      
	      //info_REG2_INV_SOL

	        String info_REG2_INV_SOL_format = String.format(info_REG2_INV_SOL, SI_NUMBER);
	        
	        System.out.println(info_REG2_INV_SOL_format);
	        
	        SQLResult info_REG2_INV_SOL_Result = dbEbs.executeQuery(info_REG2_INV_SOL_format);
	        
	     
	       boolean  info_REG2_INV_SOL_valida =  info_REG2_INV_SOL_Result.isEmpty(); //checa que el string contenga datos
	    		  
	       
	       if(!info_REG2_INV_SOL_valida ) {
	    	   
	    	   testCase.addQueryEvidenceCurrentStep(info_REG2_INV_SOL_Result);  //Si no esta vacio, lo agrega a la evidencia
	       }
	       
	       assertFalse("No se muestran registros que cumplan el rango de ejecucion.",info_REG2_INV_SOL_valida ); //Si esta vacio, imprime mensaje
	       
	      System.out.println(info_REG2_INV_SOL_valida ); //Si no, imprime la fechas
	      
//********************************************************************************************************************************************************************
	      //Dos botones y recibe el invoice_id
	      addStep("Ejecutar el servicio: OL6.Pub:run.");
	      
	      String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");	
		String searchedStatus = "R";		
		String run_id;
		

			String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
			u.get(contra);

			String dateExecution = pok.runIntefaceWmWithInput(data.get("interfase"), data.get("servicio"),data.get("invoiceID"),"invoiceID");

			System.out.println(dateExecution);

			String tdcIntegrationServer = String.format(tdcIntegrationServerFormat, dateExecution);
			System.out.println("Respuesta dateExecution"+  tdcIntegrationServer);
			
			SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);
			run_id = is.getData(0, "RUN_ID");
			String status1 = is.getData(0, "STATUS");// guarda el run id de la
														// ejecución
		
			boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
			                                                      // encuentra en
			                                                      // estatus R

			while (valuesStatus) {
				
				status1 = is.getData(0, "STATUS");
				run_id = is.getData(0, "RUN_ID");
				valuesStatus = status1.equals(searchedStatus);


				u.hardWait(2);

			}
			
			boolean successRun = status1.equals(status);// Valida si se encuentra en
			// estatus S
			if (!successRun) {

				String error = String.format(tdcQueryErrorId, run_id);
				
				SQLResult errorResult = dbLog.executeQuery(error);
				boolean emptyError = errorResult.isEmpty();

				

				if (!emptyError) {

					testCase.addTextEvidenceCurrentStep(
							"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");

					testCase.addQueryEvidenceCurrentStep(errorResult);

				}
			}
			
//***************************************************************************************************************************************************
			
//Paso 4			
			addStep("Validar que la interface se ejecuta correctamente con estatus S en WMLOG ");

			System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
			
			String queryStatusLog = String.format(tdcQueryStatusLog, run_id);
			System.out.println(queryStatusLog);

			String fcwS= String.format(queryStatusLog, "STATUS");
			
	     	boolean validateStatus = status.equals(fcwS);
			System.out.println(validateStatus);

			assertTrue(validateStatus, "La ejecución de la interfaz no fue exitosa");
			
//***************************************************************************************************************************************************

//Paso 5
			
			
			addStep("Validar que se actualizaron correctamente los campos en la tabla XXFC_SOLINV_CARGOCEN de ORAFIN ");
			
			//Primera parte 
			String XXFC_SOLINV_CARGOCEN_format_1 = String.format(act_XXFC_SOLINV_CARGOCEN_1, run_id);
			System.out.println(XXFC_SOLINV_CARGOCEN_format_1);
			
			SQLResult XXFC_SOLINV_CARGOCEN_result_1 = dbEbs.executeQuery(XXFC_SOLINV_CARGOCEN_format_1);
			boolean XXFC_SOLINV_CARGOCEN_valida_1 = XXFC_SOLINV_CARGOCEN_result_1.isEmpty();
			System.out.println(XXFC_SOLINV_CARGOCEN_valida_1);

			if (!XXFC_SOLINV_CARGOCEN_valida_1) {

				testCase.addQueryEvidenceCurrentStep(XXFC_SOLINV_CARGOCEN_result_1);

			}
			
			//Segunda parte
			String XXFC_SOLINV_CARGOCEN_format_2 = String.format(act_XXFC_SOLINV_CARGOCEN_2, run_id);
			System.out.println(XXFC_SOLINV_CARGOCEN_format_2);
			
			SQLResult XXFC_SOLINV_CARGOCEN_result_2 = dbEbs.executeQuery(XXFC_SOLINV_CARGOCEN_format_2);
			boolean XXFC_SOLINV_CARGOCEN_valida_2 = XXFC_SOLINV_CARGOCEN_result_2.isEmpty();
			System.out.println(XXFC_SOLINV_CARGOCEN_valida_2);

			if (!XXFC_SOLINV_CARGOCEN_valida_2) {

				testCase.addQueryEvidenceCurrentStep(XXFC_SOLINV_CARGOCEN_result_2);

			}

			
			

			assertFalse(XXFC_SOLINV_CARGOCEN_valida_2, " No se muestran Registros");

		   	 
		      
	       
      
	
	
	
	
	}    
        
		
	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_OL6_OL6_ASI";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar que se actualiza la información en la tabla XXFC_SOLINV_CARGOCEN correctamente para el INVOICE_ID correspondiente ";
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


