package interfaces.tpe.fcp;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_FCP;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;

public class FCPQry02UpdateCFDdnCardId extends  BaseExecution{
	
	/*
	 * 
	 * @cp CFD_Realizar la actualizacióon de la informacion del CFD de un CardId
	 * 
	 */

	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_TPE_FCP_002_Qry02UpdateCFDdnCardId(HashMap<String, String> data) throws Exception {
		
		/* Utilerias *********************************************************************/
		
		utils.sql.SQLUtil db= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE , GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		
		TPE_FCP rtpUtil = new TPE_FCP(data, testCase, db);
		
/* Variables *********************************************************************/
		
		String wmCodeToValidateQRY01  = "101";
		
		String wmCodeRequestQRY01;
		String folio, wmCodeDb;
		
		
		String query_TPE_FCP_ADDRESS ="SELECT STATUS, RFC_ID FROM FCPUSER.TPE_FCP_ADDRESS " + 
				"WHERE STATUS = 'A' " + 
				"AND RFC_ID ='" + data.get("rfcid") + "'";
		
		
		String validaFolio = "SELECT folio, wm_code FROM TPEUSER.TPE_FR_TRANSACTION WHERE folio = '%s'"
				+ "AND creation_date>= (sysdate-1) ";
		
		String query_TPE_FR_TRANSACTION ="SELECT APPLICATION,ENTITY, OPERATION, FOLIO FROM TPEUSER.TPE_FR_TRANSACTION" + 
				" WHERE APPLICATION = 'FCP'" + 
				"AND ENTITY = '" + data.get("entity") + "'" + 
				"AND OPERATION = '" + data.get("operation01") + "'" +				
				"AND FOLIO ='%s'" ;
	        //************************************************************************************************/
			
		
		//Paso 1 
		   
		   addStep("Validar informacion del RFC en la tabla TPE_FCP_ADDRESS de TPEUSER.:"); 
		   System.out.println(GlobalVariables.DB_HOST_FCTPE);
		   
		   System.out.println("Consulta informacion en FCP_ADDRESS: "+ query_TPE_FCP_ADDRESS);
		
		   SQLResult TPE_FCP_ADDRESS_Res = db.executeQuery(query_TPE_FCP_ADDRESS);
		   
		   
		   boolean paso1 = TPE_FCP_ADDRESS_Res.isEmpty();
		   System.out.println(paso1);
			
		 if (!paso1) {
				testCase.addQueryEvidenceCurrentStep(TPE_FCP_ADDRESS_Res);
			}
			
		   assertFalse(paso1, "No existe informacion del RFC en la tabla FCP_ADDRESS");
			
		
		
      //*******paso 3 Ejecutar Invoke***************************************
			
			//Parte 1 "Llamar al servicio"
			 
      addStep("Ejecutar el servicio TPE.FCP.Pub:request para realizar la transaccion de Solicitud de Llaves.");
		  			
     //Parte 1 
      String respuestaQRY01 = rtpUtil.generacionFolioTransaccion();
		
		System.out.println("Respuesta: " + respuestaQRY01);
		
		folio = RequestUtil.getFolioXml(respuestaQRY01);
		wmCodeRequestQRY01 = RequestUtil.getWmCodeXml(respuestaQRY01);
		
		
		
		boolean validationResponseFolio = true;

		 
        if(respuestaQRY01!= null) {
        	
        	validationResponseFolio= false;
        	
        	}
          assertFalse(validationResponseFolio);
          
		
		
	//Parte 2
		
		  addStep("Verificar la respuesta generada por el servicio");
		
	  		
	           
		     //Se manda el folio a la consulta
		     String format_validaFolio = String.format(validaFolio, folio);
	         System.out.println(format_validaFolio);		
			 SQLResult result_validaFolio = executeQuery(db,format_validaFolio );	
			
			//Se valida que el wm_code del xml de respuesta sea igual a 101
	  		 boolean validationRequest = wmCodeRequestQRY01.equals(wmCodeToValidateQRY01);
	  		 System.out.println(validationRequest + " - wmCode request: " + wmCodeRequestQRY01);
	  		 testCase.addTextEvidenceCurrentStep("Response: \n"+ respuestaQRY01);//Se agrega el response a la evidencia
	  		  			
	  		 //Se extrae el wm_code de la base de datos
	  		 wmCodeDb = result_validaFolio.getData(0, "WM_CODE");
	  		 testCase.addQueryEvidenceCurrentStep(result_validaFolio); //Se imprime la consulta en la evidencia
	  		 
	  		//Se valida que el wm_code de la base de datos  sea igual a 101
	  		 boolean validationDb = wmCodeDb.equals(wmCodeToValidateQRY01);	
	  		 System.out.println(validationDb + " - wmCode db: " + wmCodeDb);
	  			
	  		 boolean validaCodigos = false;
	  			
	  			
	  			if(validationRequest == validationDb){
	  				
	  				validaCodigos = true;
	  			}
	  						
	  			assertTrue(validaCodigos);
	  			

	
	//*******paso 4 ver la transaccion de actualizacion en la tabla TPE_FR_TRANSACTION de TPEUSER.
//***************************************
	   addStep("Se registra la transaccion de actualizacion en la tabla TPE_FR_TRANSACTION de TPEUSER "); 

	         String format_TPE_FR_TRANSACTION = String.format(query_TPE_FR_TRANSACTION, folio);
	         System.out.println("Consultar folio en TPE_FR_TRANSACTION: "+format_TPE_FR_TRANSACTION);		
			 SQLResult result_TPE_FR_TRANSACTION = executeQuery(db,format_TPE_FR_TRANSACTION );	
			 
			 boolean valida_TPE_FR_TRANSACTION = result_TPE_FR_TRANSACTION.isEmpty();
			 
				if (!valida_TPE_FR_TRANSACTION) {
					testCase.addQueryEvidenceCurrentStep(result_TPE_FR_TRANSACTION);
				}
				
				assertFalse( valida_TPE_FR_TRANSACTION , "No se muestran registros en TPE_FR_TRANSACTION ");
				System.out.println( valida_TPE_FR_TRANSACTION );

			//Paso 5
				
		addStep("Validar la actualizacion de los datos frecuentes de facturacion del cliente en la tabla TPE_FCP_ADDRESS de TPEUSER. "); 
	   
				 
				 System.out.println("Consulta la actualizacion en FCP_ADDRESS: "+ query_TPE_FCP_ADDRESS);
					
				 SQLResult TPE_FCP_ADDRESS_Res2 = db.executeQuery(query_TPE_FCP_ADDRESS);
				   
				   
				   boolean paso5 = TPE_FCP_ADDRESS_Res2.isEmpty();
				   
				   System.out.println(paso5);
	
					
				   if (!paso5) {
						testCase.addQueryEvidenceCurrentStep(TPE_FCP_ADDRESS_Res2);
					}
					assertFalse(paso5, "No existe informacion actualizada del RFC en la tabla FCP_ADDRESS");
					
	}
	@Override
	public String setTestFullName() {
		return "ATC_FT_TPE_FCP_002_Qry02UpdateCFDdnCardId";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Realizar la actualizacion de la informacion del CFD de un CardId";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
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