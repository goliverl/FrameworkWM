package interfaces.tpe.fcp;

import modelo.BaseExecution;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import org.testng.annotations.Test;

import om.TPE_FCP;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;

public class FCPQry01CFD  extends BaseExecution{
	
	/*
	 * 
	 * @cp CFD_Realizar la consultar la lista de CFD de un CardId
	 * 
	 */
	
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_TPE_FCP_001_Qry01CFD(HashMap<String, String> data) throws Exception {

		/* Utilerías **********************************************************************/
		utils.sql.SQLUtil db= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE , GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		TPE_FCP rtpUtil = new TPE_FCP(data, testCase, db);
		
		/* Variables *********************************************************************/
		String validaFolio = "SELECT folio, wm_code FROM TPEUSER.TPE_FR_TRANSACTION WHERE folio = '%s'"
				           + "AND creation_date>= (sysdate-1) ";
		
		String wmCodeToValidateQRY01 = "101";
		String genFolioQuery = "SELECT APPLICATION,ENTITY,OPERATION,folio, SOURCE,PLAZA,TIENDA,CREATION_DATE,WM_CODE,WM_DESC "
				+ " FROM TPEUSER.TPE_FR_TRANSACTION WHERE APPLICATION = '" + data.get("application") + "' "
						+ "AND ENTITY = '" + data.get("entity") + "' AND OPERATION = '" + data.get("operation01") + "' and folio = %s";
		
		String folio;
		String wmCodeRequestQRY01;

		
		String wmCodeDbQRY01;

		
		/* ***********************************************************************************************
		 * Folio QRY01
		 * ***********************************************************************************************/
		//Paso 1
		 addStep("Ejecutar el servicio TPE.RPT.Pub:request para realizar la transaccion de Solicitud de Llaves.");
		
			String respuestaQRY01 = rtpUtil.generacionFolioTransaccion();
			
			System.out.println("Respuesta: " + respuestaQRY01);
			
			folio = RequestUtil.getFolioXml(respuestaQRY01);
			wmCodeRequestQRY01 = RequestUtil.getWmCodeXml(respuestaQRY01);
			
			boolean validationResponseFolio = true;

			 
	        if(respuestaQRY01!= null) {
	        	
	        	validationResponseFolio= false;
	        	
	        	}
	          assertFalse(validationResponseFolio);
		
		
		//Paso 2
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
 			
		
		
		//Paso 3
		addStep("Verificar datos insertados en TPUSER.TPE_FR_TRANSACTION");
		
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
		return "ATC_FT_TPE_FCP_001_Qry01CFD";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Realizar la consultar la lista de CFD de un CardId";
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
