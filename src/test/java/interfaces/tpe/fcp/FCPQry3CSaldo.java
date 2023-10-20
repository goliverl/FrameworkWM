package interfaces.tpe.fcp;


import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import om.TPE_FCP;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import modelo.BaseExecution;



public class FCPQry3CSaldo extends BaseExecution{
	
	/*
	 * 
	 * @cp FRQ_Realizar la consulta de Saldo de Contadores para el cardId 9100046653081
	 * 
	 */
	
	//   Nota: Pruebas pendientes, devuelve un wm_code incorrecto

		@Test(dataProvider = "data-provider")
		public void ATC_FT_TPE_FCP_006_Qry3CSaldo(HashMap<String, String> data) throws Exception {
			
			/* Utilerias *********************************************************************/
			
			utils.sql.SQLUtil db= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE , GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
			TPE_FCP rtpUtil = new TPE_FCP(data, testCase, db) ;
			
			/* Variables *********************************************************************/
			
			
			
			String obtener_card_id ="SELECT C.ACCOUNT_ID FROM TPEUSER.TPE_FCP_ACCESS A, TPEUSER.TPE_FCP_ACCOUNT C WHERE A.ACCOUNT_ID = C.ACCOUNT_ID "
					+"AND A.STATUS = 'A'"
					+"AND C.STATUS = 'A'"
					+"AND A.TYPE = UPPER('keyring')"
					+"AND A.ACCESS_ID ='" + data.get("id") + "'";
			
			
			String consulta_saldo = "SELECT REWARD_ID, REWARD_NAME FROM (\r\n" + 
					"  SELECT  B.REWARD_ID, B.REWARD_NAME\r\n" + 
					"  FROM    FCPUSER.FQY_CONF_REWARD B, FCPUSER.FQY_CONF_MECHANISM C, FCPUSER.FQY_CONF_MECH_COUNT D, FCPUSER.FQY_CONF_COUNTER E, FCPUSER.FQY_DAT_COUNTER F, FCPUSER.FQY_CONF_EMISSION_REWARD G\r\n" + 
					"  WHERE   1=1\r\n" + 
					"    AND   C.REWARD_ID = B.REWARD_ID\r\n" + 
					"    AND   C.MECHANISM_ID = D.MECHANISM_ID\r\n" + 
					"    AND   D.COUNTER_ID = E.COUNTER_ID\r\n" + 
					"    AND   D.COUNTER_ID = F.COUNTER_ID\r\n" + 
					"    AND   C.REWARD_ID = G.REWARD_ID\r\n" + 
					"    AND   C.STATUS = 'ACTIVO'\r\n" + 
					"    AND   D.STATUS = 'ACTIVO'\r\n" + 
					"    AND   E.STATUS = 'ACTIVO'\r\n" + 
					"    AND   F.STATUS = 'ACTIVO'\r\n" + 
					"    AND   G.STATUS = 'ACTIVO'\r\n" + 
					"    AND   F.ACCOUNT_ID ='%s'"+ 
					"    AND   G.PLAZA_ID = '"+data.get("plaza")+"'" + 
					"  )GROUP BY REWARD_ID, REWARD_NAME\r\n" + 
					"ORDER BY REWARD_ID " + 
					"";
			
			
			
			String genFolioQuery = "SELECT APPLICATION,ENTITY,OPERATION,folio, SOURCE,PLAZA,TIENDA,CREATION_DATE,WM_CODE,WM_DESC  "
					+ "FROM TPEUSER.TPE_FR_TRANSACTION WHERE APPLICATION = '"+data.get("application")+"' AND ENTITY = '"+data.get("entity")+"' AND OPERATION = '"+data.get("operation01")+"' and folio = '%s'";
			
			String validaFolio = "SELECT folio, wm_code FROM TPEUSER.TPE_FR_TRANSACTION WHERE folio = '%s'"
			           + "AND creation_date>= (sysdate-1) ";
					
			String folio, wmCodeRequestQRY01, wmCodeDbQRY01;	
			
			String wmCodeToValidateQRY01  = "101"; //verificar codigo de validacion

			
		        //************************************************************************************************/
				//Paso 1
			    //Obtener Account_id
			    addStep("Validar que exista informacion con el card-id en la tabla:"); 
			    System.out.println("Consulta card_id: "+ obtener_card_id);
			    
		        SQLResult result_card_id = executeQuery(db,obtener_card_id );	
				 
				 boolean valida_card_id = result_card_id.isEmpty();
					
				 if (!valida_card_id) {
					 
						testCase.addQueryEvidenceCurrentStep(result_card_id);
					}
					assertFalse("No se encontro ningun card_id con la informacion requerida ",  valida_card_id);
					System.out.println( valida_card_id);
					
					 String account_id = result_card_id.getData(0, "ACCOUNT_ID"); //obtenemos el account_id
					 System.out.println("Account_id= " + account_id);

			
	        //************************************************************************************************/
			//Paso 2
		    //Consultar Beneficios
		    addStep("Validar que exista informacion: Consultar Saldo"); 
			
			
			
			String format_consulta_saldo = String.format(consulta_saldo, account_id);
			
			System.out.println("Consultar saldo  "+ format_consulta_saldo);
			
			System.out.println("consulta con Account_id= " + format_consulta_saldo);
			
			SQLResult result_consulta_saldo = executeQuery(db, format_consulta_saldo);	
			 
			 boolean valida_consulta_saldo = result_consulta_saldo.isEmpty();
				
			 if (!valida_consulta_saldo) {
				 
					testCase.addQueryEvidenceCurrentStep(result_consulta_saldo);
				}
				assertFalse("La cuenta obtenida no tiene  saldo ",  valida_consulta_saldo);
				System.out.println( valida_consulta_saldo);
			
			

			/* ***********************************************************************************************
			 * Folio QRY01
			 * ***********************************************************************************************/
			//Paso 3
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
		
			
			//Paso 4
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
			
	//************************************************************************************************************************** 			
	 			//Paso 5
			addStep("Verificar datos insertados en TPUSER.TPE_FCP_TRANSACTION");
			
			String format_TPE_FR_TRANSACTION = String.format(genFolioQuery, folio);
		    System.out.println("Consultar folio en TPE_FR_TRANSACTION: "+ genFolioQuery);
		    
			SQLResult result_TPE_FR_TRANSACTION = executeQuery(db,format_TPE_FR_TRANSACTION );	
			 
			 boolean valida_TPE_FR_TRANSACTION = result_TPE_FR_TRANSACTION.isEmpty();
				
			 if (!valida_TPE_FR_TRANSACTION) {
					testCase.addQueryEvidenceCurrentStep(result_TPE_FR_TRANSACTION);
				}
				assertFalse("No se muestran registros en TPE_FR_TRANSACTION ",  valida_TPE_FR_TRANSACTION );
				System.out.println( valida_TPE_FR_TRANSACTION );

		
				
			
		}
		
		@Override
		public String setTestFullName() {
			return "ATC_FT_TPE_FCP_006_Qry3CSaldo";
		}

		@Override
		public String setTestDescription() {
			// TODO Auto-generated method stub
			return "Validar la transaccion de Consulta Saldo de Contadores para el cardId 9100046653081";
		}

		@Override
		public String setTestDesigner() {
			// TODO Auto-generated method stub
			return "Equipo de Automatizacion";
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

