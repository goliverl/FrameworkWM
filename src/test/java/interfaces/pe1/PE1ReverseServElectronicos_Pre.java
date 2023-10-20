package interfaces.pe1;

import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.PE1;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

public class PE1ReverseServElectronicos_Pre extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_PE1_PE1ReverseServElectronicos_Pre(HashMap<String, String> data) throws Exception {
		
		// Utilerías *********************************************************************
		
		utils.sql.SQLUtil dbFCTAEQA= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTAEQA, GlobalVariables.DB_USER_FCTAEQA,GlobalVariables.DB_PASSWORD_FCTAEQA );	
		utils.sql.SQLUtil dbFCSWQA= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA,GlobalVariables.DB_PASSWORD_FCSWQA);
		
		PE1 pe1Util = new PE1(data, testCase, dbFCTAEQA); 
		
		// Variables *********************************************************************
		
		//PTAE_TRANSACTION
		
			String query_TAE_TRANSACTION1 = "SELECT folio, creation_date, plaza, tienda, phone, carrier, amount, wm_code  "
						+ "FROM TPEUSER.TAE_TRANSACTION WHERE CREATION_DATE >= TO_CHAR(SYSDATE,'DD-MON-YY') "
						+ "AND TIENDA= '"+ data.get("tienda")+"'AND folio = '%s' ORDER BY CREATION_DATE DESC ";
							
		//TAE_REVERSE
				
			String query_TAE_REVERSE1= "SELECT folio, creation_date, plaza, tienda, phone, carrier, sw_auth, wm_code, retek_cr "
						+ "FROM TPEUSER.TAE_REVERSE WHERE CREATION_DATE >=  TO_CHAR(SYSDATE,'WW-MON-YY') "
						+ "AND FOLIO='%s' ORDER BY CREATION_DATE DESC ";
				
				
		//TPE_SW_TLOG
				
			String query_TPE_SW_TLOG1= "select plaza, tienda, caja, retek_cr, channel, application, entity, folio, creation_date "
						+ "from SWUSER.TPE_SW_TLOG where TIENDA='"+ data.get("tienda")+"' and APPLICATION='TAE' and CREATION_DATE >= SYSDATE ";
				
	
		//Consultas de codigos
		
		String wmCodeToValidate = "100";
		String tdcTransactionQuery = "SELECT folio, wm_code FROM tpeuser.tae_transaction WHERE folio = '%s' ";
			
		String wmCodeToValidateAuth = "000";
		String tdcTransactionQueryAuth = "SELECT folio, wm_code FROM tpeuser.tae_transaction WHERE folio = '%s' ";
		
		String folio, wmCodeRequest, wmCodeDb;
		
//*****************************************************Prerequisitos************************************************************************************************************		
		
		//Consultas pre-requicitos
		
	
		
		String query_wm_code = "select * from tpeuser.tae_wm_code where wm_code= '%s'";
		
		
		System.out.print("Pre-requicitos \n");
		
		
		// Validar que el carrier sea correcto
		
		pe1Util.validaCarrier(dbFCTAEQA);
		
		//Validar que la tienda cuente con ese carrier
		
		pe1Util.validaConfigTienda(dbFCTAEQA);
		
			
		//Validar el telefono
		pe1Util.validarTelefono();
		
		
		
		
		

//***************************************************Llamar al servico PE1.Pub:runGetFolio****************************************************************************************************************************************
	
		//Paso 1
				addStep("Llamar al servico PE1.Pub:runGetFolio");
				
				    //Ejecuta el servicio PE1.Pub:runGetFolio
				    System.out.println("Paso 1: Llamar al servico PE1.Pub:runGetFolio \n");
					String respuesta = pe1Util.ejecutarRunGetFolio();
								
					//Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la evidencia 
					folio = getSimpleDataXml(respuesta, "folio");
					wmCodeRequest = getSimpleDataXml(respuesta, "wmCode");
				//	testCase.addTextEvidenceCurrentStep("Folio: "+ folio);//Se agrega a la evidencia el folio
					
				
					
					boolean validationResponseFolio=true;

					 
			        if(respuesta!= null) {
			        	
			        	validationResponseFolio= false;
			        	
			        	}
			          assertFalse(validationResponseFolio);
			 
			         
	  //Paso 2		          
			        addStep("Verificar la respuesta generada por el servicio");
			  		
			  		
			  		//Se valida que el wm_code del xml de respuesta sea igual a 100
			           
			  			boolean validationRequest = wmCodeRequest.equals(wmCodeToValidate);
			  			System.out.println(validationRequest + " - wmCode request: " + wmCodeRequest);
			  
			  			
			  		//Si el request es diferente a 100, muestra la descripcion
		  				
		  				String wm_code = String.format(query_wm_code,wmCodeRequest );
		  				System.out.println(wm_code);
			  						  			
			  			SQLResult wmCodeRes = executeQuery(dbFCTAEQA,wm_code );
	
			  			
//			  			if(validationDb==false){
			  			testCase.addQueryEvidenceCurrentStep(wmCodeRes);
		  				
		  		//	}
	  						
	  			//	assertTrue(validationDb);
  				
			  			
			  			 			
			  			assertTrue(validationRequest);
			  			
			  		
	 //Paso 3
			  		addStep("Validar que se ha creado un registro del folio en la tabla tdc_transaction de TPEUSER");
			  	            
			  			 //Se forma el query a utilizar	
			  				String query = String.format(tdcTransactionQuery, folio);
			  				System.out.println(query);
			  				
			  				//Se realiza la consulta a la BD y se obtiene el wm_code
//			  				wmCodeDb = pe1Util.getWmCodeQuery(query);
			  				
			  				SQLResult result1 = executeQuery(dbFCTAEQA, query);
			  				wmCodeDb = result1.getData(0, "WM_CODE");
			  				testCase.addQueryEvidenceCurrentStep(result1);
			  				
			  				//Se valida que sea igual a 100
			  				boolean validationDb = wmCodeDb.equals(wmCodeToValidate);	
			  				System.out.println(validationDb + " - wmCode db: " + wmCodeDb);
			  				
			  				
				  		
			  				
//**********************************Llamar al servico PE1.Pub:runGetAuth********************************************************************************************************************
			  				
		//Paso 1	  				
			  				addStep("Llamar al servico PE1.Pub:runGetAuth");
			  				
			  			    //Ejecuta el servicio PE1.Pub:runGetAuth
			  				System.out.println("Paso 2: Llamar al servico PE1.Pub:runGetAuth \n");
			  				
			  				String respuestaAuth = pe1Util.ejecutarRunGetAuth(folio);	
			  				
			  						  				
			  				//Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la evidencia 
			  				folio = getSimpleDataXml(respuestaAuth, "folio");
			  				wmCodeRequest = getSimpleDataXml(respuestaAuth, "wmCode");	
			  			
			  				boolean validateRequestAuth = true;

							 
					        if(respuestaAuth!= null) {
					        	
					        	validateRequestAuth = false;
					        	
					        	}
					   
			  				
			  			assertFalse(validateRequestAuth);	
			  			
			  				  			
			  			
		//Paso 2 		
			  			
			  
			  	   addStep("Verificar la respuesta generada por el servicio");
			  			
			      //Se valida que el wm_code del xml de respuesta sea igual a 000
				  boolean validationRequestServAuth = wmCodeRequest.equals(wmCodeToValidateAuth);
				  System.out.println(validationRequestServAuth + " - wmCode request: " + wmCodeRequest);
				  	
				  
				  //Si el request es diferente a 000 imprime la descripcion
				   	
				   	wm_code = String.format(query_wm_code,wmCodeRequest );
	  				System.out.println(wm_code);
		  						  			
		  			wmCodeRes = executeQuery(dbFCTAEQA,wm_code );
				   	
				 //  	if(validationDb==false){
		  				
		  				testCase.addQueryEvidenceCurrentStep(wmCodeRes);
		  		//	}
				  
				  assertTrue(validationRequestServAuth);
					
				  
				  
	 //Paso 3 
				  addStep("Validar que se ha creado un registro del folio en la tabla tdc_transaction de TPEUSER");
					
				    //Se forma el query a utilizar
					String queryAuth = String.format(tdcTransactionQueryAuth, folio);
					System.out.println(queryAuth);
					
					//Se realiza la consulta a la BD y se obtiene el wm_code

					SQLResult result2 = executeQuery(dbFCTAEQA, queryAuth);
					wmCodeDb = result2.getData(0, "WM_CODE");
					testCase.addQueryEvidenceCurrentStep(result2);
					
					//Se valida que sea igual a 000
					boolean validationDbAuth = wmCodeDb.equals(wmCodeToValidateAuth);	
				   	System.out.println(validationDbAuth + " - wmCode db: " + wmCodeDb);		
										
				 assertTrue(validationDbAuth);
				 
//********************************************Validar base de datos FCTAEQA_MTY********************************************************************************
				
				addStep("Establecer conexión con la base de datos: FCTAEQA_MTY ");
				System.out.println("Paso 3: Establecer conexión con la base de datos: FCTAEQA_MTY \n");
				System.out.println(GlobalVariables.DB_HOST_FCTAEQA);
				
				testCase.addTextEvidenceCurrentStep("Conexion exitosa a la base de datos");
				testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCTAEQA);
				testCase.addTextEvidenceCurrentStep("User: "+ GlobalVariables.DB_USER_FCTAEQA);
				testCase.addTextEvidenceCurrentStep("Password: "+ GlobalVariables.DB_PASSWORD_FCTAEQA);
						

//********************************************Consulta en la tabla TAE_TRANSACTION:***********************************************************************************
								
				addStep("Ejecutar la siguiente consulta de la tabla TAE_TRANSACTION: ");
				System.out.println("Paso 4: Ejecutar  consulta en  la tabla TAE_TRANSACTION: \n");
			 
							
				String TAE_TRANSACTION1  = String.format(query_TAE_TRANSACTION1, folio);
				
				SQLResult TAE_TRANSACTIONRes1 = dbFCTAEQA.executeQuery(TAE_TRANSACTION1);
		        
			 	System.out.println(TAE_TRANSACTION1);    
			 	 
			    boolean TAE_TRANSACTIONBool =  TAE_TRANSACTIONRes1.isEmpty(); //checa que el string contenga datos
			    		  
			    System.out.println( TAE_TRANSACTIONBool); 
			    
			       if(! TAE_TRANSACTIONBool) {
			    	   
			    	   testCase.addQueryEvidenceCurrentStep(TAE_TRANSACTIONRes1);  //Si no esta vacio, lo agrega a la evidencia
			       }
			       
				       		       
			       assertFalse("El registro de la transacción de TAE no fue exitoso.", TAE_TRANSACTIONBool );
			       
			       System.out.println( TAE_TRANSACTIONBool); 
		    		  
			       
//***************************************************Ejecutar el job runPE1ReverseManager.sh*********************************************************************
			       
			       addStep("Ejecutar servicio runPE1ReverseManager.sh");
			       
			       System.out.println("Paso 5: Ejecutar servicio runPE1ReverseManager.sh ");
				     
				     // Utileria
				      SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
				      PakageManagment pok = new PakageManagment(u, testCase);
				      

				      String user = data.get("user");
				      String ps = PasswordUtil.decryptPassword(data.get("ps"));
				      String server = data.get("server");
				      String con = "http://" + user + ":" + ps + "@" + server;
				     
				  
				      System.out.println(GlobalVariables.DB_HOST_LOG);
				      String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
				      u.get(contra);

				   
				      String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
				      System.out.println("Respuesta dateExecution " + dateExecution);
				      			     
		  	u.close();	      
//**********************************Consulta en TAE_REVERSE*********************************************************************************************************
				      
				      addStep("Ejecutar el siguiente query para la tabla TAE_REVERSE de la BD FCTAEQA_MTY para validar que se reversó exitosamente la transacción:"); 
				      
				      System.out.println("Paso 6: Ejecutar  query en la tabla TAE_REVERSE para validar que se reversó exitosamente la transacción:");
				      
				    
				      String TAE_REVERSE1  = String.format(query_TAE_REVERSE1, folio);
						
						SQLResult TAE_REVERSERes1 = dbFCTAEQA.executeQuery(TAE_REVERSE1);
				        
					 	System.out.println(TAE_REVERSE1);    
					 	 
					    boolean TAE_REVERSE1Bool1 = TAE_REVERSERes1.isEmpty(); //checa que el string contenga datos
					    
					    System.out.println( TAE_REVERSE1Bool1 ); 
					    		  
					       
					    if(!TAE_REVERSE1Bool1) {
					    	   
					    	 testCase.addQueryEvidenceCurrentStep(TAE_REVERSERes1);  //Si no esta vacio, lo agrega a la evidencia
					     }
				      
					    assertFalse("La reversion en la transacción no fue exitosa.", TAE_REVERSE1Bool1);  
					    
					    System.out.println(TAE_REVERSE1Bool1);
					    
//****************************************************Validar conexion FCSWQA*************************************************************************
					    
					    addStep("Establecer conexión con la base de datos: FCSWQA");
					    testCase.addTextEvidenceCurrentStep("Conexion exitosa a la base de datos");
					    testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCSWQA);
						testCase.addTextEvidenceCurrentStep("User: "+ GlobalVariables.DB_USER_FCSWQA);
						testCase.addTextEvidenceCurrentStep("Password: "+ GlobalVariables.DB_PASSWORD_FCSWQA);
						
						System.out.println("Paso 7: Establecer conexión con la siguiente base de datos: FCSWQA.");
						System.out.println(GlobalVariables.DB_HOST_FCSWQA);
						    
								
//********************************************************Consulta query_TPE_SW_TLOG1*******************************************************************					    
					    				    
					   
					    addStep("Ejecutar la siguiente consulta de la tabla TPE_SW_TLOG: ");
					    System.out.println("Paso 8: Ejecutar la siguiente consulta de la tabla TPE_SW_TLOG:");
					    
                 
					    
						SQLResult TPE_SW_TLOG_Res1 = dbFCSWQA.executeQuery(query_TPE_SW_TLOG1);
				        
					 	System.out.println(TPE_SW_TLOG_Res1);    
					 	 
					    boolean TPE_SW_TLOG_bool1 = TPE_SW_TLOG_Res1.isEmpty(); //checa que el string contenga datos
					    
					    System.out.println( TPE_SW_TLOG_bool1);
					    		  					       
					    if(!TPE_SW_TLOG_bool1) {
					    	   
					    	 testCase.addQueryEvidenceCurrentStep(TPE_SW_TLOG_Res1);  //Si no esta vacio, lo agrega a la evidencia
					     }  
					    				    				 				   
					    assertFalse("El registro de la transacción de TAE no fue exitosa.", TPE_SW_TLOG_bool1 ); 
					    System.out.println( TPE_SW_TLOG_bool1);
					
	}
	
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_005_PE1_PE1ReverseServElectronicos_Pre";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. FEMSA_PE1_PE1ReverseServElectronicos_Pre";
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

