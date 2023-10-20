package interfaces.tpe.fcp;

/**
 * @author 53015
 */

import java.util.HashMap;
import org.testng.annotations.Test;
import om.TPE_FCP;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import modelo.BaseExecution;
import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class FCPQry4ValidarCardid extends  BaseExecution {
	
	/*
	 * 
	 * @cp FCP_Verificar si el CardId es valido y obtener el accountId de TPESERVICES
	 * 
	 */


	@Test(dataProvider = "data-provider")
	public void ATC_FT_TPE_FCP_007_Qry4ValidarCardid(HashMap<String, String> data) throws Exception {
		
		
		//NOTA: Pruebas pendientes ya que se necesitan buscar los insumos correctos, faltan datos reales para el invoke.
		//wm_code 114.
		
		/* Utilerias *********************************************************************/
		utils.sql.SQLUtil db= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE , GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		TPE_FCP rtpUtil = new TPE_FCP(data, testCase, db) ;
		
		/* Variables *********************************************************************/
		
		
		String valida_info ="SELECT C.ACCOUNT_ID FROM TPEUSER.TPE_FCP_ACCESS A, TPEUSER.TPE_FCP_ACCOUNT C "
				+ "WHERE A.ACCOUNT_ID = C.ACCOUNT_ID "
				+"AND A.STATUS = 'A'"
				+"AND C.STATUS = 'A'"
				+"AND A.TYPE = UPPER('keyring')"
				+"AND A.ACCESS_ID ='" + data.get("id") + "'";
		
		String validaFolio = "SELECT folio, wm_code FROM TPEUSER.TPE_FR_TRANSACTION WHERE folio = '%s'"
		           + "AND creation_date>= (sysdate-1) ";
		
        String TPE_FR_TRANSACTION ="SELECT * FROM TPEUSER.TPE_FR_TRANSACTION " + 
        		"WHERE APPLICATION = '" + data.get("application") + "'" + 
        		"AND ENTITY = '" + data.get("entity") + "'" + 
        		"AND OPERATION = '" + data.get("operation01") + "'" + 
        		"AND FOLIO = '%s'";
				
		
		
		String wmCodeToValidateQRY01  = "100"; //verificar codigo de validacion
		String wmCodeRequestQRY01, folio, wmCodeDbQRY01;
			
		
	        //************************************************************************************************/
			//Paso 1
		    //Obtener Account_id
		    addStep("Validar que exista informacion con el cardid en la tabla:"); 
		
	
		System.out.println("Consulta informacion con el card_id : "+ valida_info);
		
		SQLResult result_valida_info = executeQuery(db,valida_info);	
		 
		 boolean valida_valida_info = result_valida_info.isEmpty();
			
		 if (!valida_valida_info) {
				testCase.addQueryEvidenceCurrentStep(result_valida_info);
			}
			assertFalse("No se muestran registros en TPE_FR_TRANSACTION ",  valida_valida_info);
			System.out.println( valida_valida_info);
		
		
		 
		// * ***********************************************************************************************/
		//Paso 2
		addStep("Ejecutar el servicio TPE.FCP.Pub:request para realizar la transaccion de Solicitud de Llaves.");
		
		String respuestaQRY01 = rtpUtil.generacionFolioTransaccion();
		
		System.out.println("Respuesta: " + respuestaQRY01);
		
		folio = RequestUtil.getFolioXml(respuestaQRY01);
		wmCodeRequestQRY01 = RequestUtil.getWmCodeXml(respuestaQRY01);
		
		boolean validationResponseFolio = true;

		 
        if(respuestaQRY01!= null) {
        	
        	validationResponseFolio= false;
        	
        	}
          assertFalse(validationResponseFolio);
          
       // * ***********************************************************************************************/
          
          //Paso 3
          
          addStep("Verificar el XML de la respuesta");
        
        //Se manda el folio a la consulta
 	     String format_validaFolio = String.format(validaFolio, folio);
          System.out.println(format_validaFolio);		
 		 SQLResult result_validaFolio = executeQuery(db,format_validaFolio );	
 		
 		//Se valida que el wm_code del xml de respuesta sea igual a 101
  		 boolean validationRequest = wmCodeRequestQRY01.equals(wmCodeToValidateQRY01);
  		 System.out.println(validationRequest + " - wmCode request: " + wmCodeRequestQRY01);
  		 testCase.addTextEvidenceCurrentStep("Response: \n"+ respuestaQRY01);//Se agrega el response a la evidencia
  		  			
  		 //Se extrae el wm_code de la base de datos
  		wmCodeDbQRY01 = result_validaFolio.getData(0, "WM_CODE");
  		 testCase.addQueryEvidenceCurrentStep(result_validaFolio); //Se imprime la consulta en la evidencia
  		 
  		//Se valida que el wm_code de la base de datos  sea igual a 101
  		 boolean validationDb = wmCodeDbQRY01.equals(wmCodeToValidateQRY01);	
  		 System.out.println(validationDb + " - wmCode db: " + wmCodeDbQRY01);
  			
  		 boolean validaCodigos = false;
  			
  			
  			if(validationRequest == validationDb){
  				
  				validaCodigos = true;
  			}
  						
  			assertTrue(validaCodigos);
  			
  			
  			//Paso 4
  			
  			addStep("Verificar datos insertados en TPUSER.TPE_FR_TRANSACTION");
  			
			String format_TPE_FR_TRANSACTION = String.format(TPE_FR_TRANSACTION, folio);
		   
			
		    
			SQLResult result_TPE_FR_TRANSACTION = executeQuery(db,format_TPE_FR_TRANSACTION );	
			
			System.out.println("Consultar folio en TPE_FR_TRANSACTION: "+ format_TPE_FR_TRANSACTION);
			 
			 boolean valida_TPE_FR_TRANSACTION = result_TPE_FR_TRANSACTION.isEmpty();
				
			 if (!valida_TPE_FR_TRANSACTION) {
					testCase.addQueryEvidenceCurrentStep(result_TPE_FR_TRANSACTION);
				}
				assertFalse("No se muestran registros en TPE_FR_TRANSACTION ",  valida_TPE_FR_TRANSACTION );
				System.out.println( valida_TPE_FR_TRANSACTION );

	
	}
	
	
    	
		@Override
		public String setTestFullName() {
			return "ATC_FT_TPE_FCP_007_Qry4ValidarCardid";
		}

		@Override
		public String setTestDescription() {
			// TODO Auto-generated method stub
			return "Verificar si el CardId es valido y obtener el accountId de TPESERVICES";
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
