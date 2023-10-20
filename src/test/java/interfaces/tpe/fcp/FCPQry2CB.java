package interfaces.tpe.fcp;

import java.util.HashMap;


import org.testng.annotations.Test;

import om.TPE_FCP;

import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import modelo.BaseExecution;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;



public class FCPQry2CB extends BaseExecution {
	
	//Nota: Faltan datos reales para el invoke
	
	/*
	 * 
	 * @cp FRQ_Realizar la consulta de beneficios activos para el cardId 9100268371270
	 * 
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_TPE_FCP_005_Qry2CB(HashMap<String, String> data) throws Exception {
		
		
		//Nota: Prueba pendiente, insumos incorrectos
		
		/* Utilerias *********************************************************************/
		utils.sql.SQLUtil db= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE , GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		TPE_FCP rtpUtil = new TPE_FCP(data, testCase, db);
		
		/* Variables *********************************************************************/
		
		
		
		
		String info_CFD ="SELECT C.ACCOUNT_ID FROM TPEUSER.TPE_FCP_ACCESS A, TPEUSER.TPE_FCP_ACCOUNT C WHERE A.ACCOUNT_ID = C.ACCOUNT_ID "
				+"AND A.STATUS = 'A'"
				+"AND C.STATUS = 'A'"
				+"AND A.TYPE = UPPER('keyring')"
				+"AND A.ACCESS_ID ='" + data.get("id") + "'";
		
		String beneficios = "SELECT A.ACCOUNT_ID, B.REWARD_ID, B.REWARD_NAME, D.EMISSION_ID, D.PROMOTION_ID\r\n" + 
				"FROM   FCPUSER.TPE_FCP_REWARD A, FCPUSER.FQY_CONF_REWARD B, FCPUSER.FQY_CONF_MECHANISM C, FCPUSER.FQY_CONF_EMISSION_REWARD D\r\n" + 
				"WHERE  1=1\r\n" + 
				"  AND  A.REWARD_ID = B.REWARD_ID\r\n" + 
				"  AND  B.REWARD_ID = C.REWARD_ID\r\n" + 
				"  AND  C.REWARD_ID = D.REWARD_ID\r\n" + 
				"  AND  A.STATUS = 'ACTIVO'\r\n" + 
				"  AND  C.STATUS = 'ACTIVO'\r\n" + 
				"  AND  D.STATUS = 'ACTIVO'\r\n" + 
				"  AND  D.PLAZA_ID ='"+data.get("plaza")+"'" + 
				"  AND  A.ACCOUNT_ID ='%s'"+ 
				"  AND  SYSDATE BETWEEN C.START_DATE "
				+ "AND C.END_DATE "; 
				
		
       String TPE_FR_TRANSACTION= "SELECT APPLICATION,ENTITY,OPERATION,folio, SOURCE,PLAZA,TIENDA,CREATION_DATE,WM_CODE,WM_DESC  "
		+ "FROM TPEUSER.TPE_FR_TRANSACTION WHERE APPLICATION = '"+data.get("application")+"' AND ENTITY = '"+data.get("entity")+"' AND OPERATION = '"+data.get("operation01")+"' and folio = %s";
       
       String validaFolio = "SELECT folio, wm_code FROM TPEUSER.TPE_FR_TRANSACTION WHERE folio = '%s'"
	           + "AND creation_date>= (sysdate-1) ";
		
		
       String wmCodeToValidateQRY01  = "101";
	
	   String wmCodeRequestQRY01, wmCodeDbQRY01, folio;

		
		
		

			
		
	        //************************************************************************************************/
			//Paso 1
		    //Obtener Account_id
		    addStep("Validar que exista informacion con el cardid en la tabla:"); 
		    
		    System.out.println( info_CFD);
		    
			SQLResult result_info_CFD  = executeQuery(db,info_CFD );	
			 
			 boolean valida_info_CFD  = result_info_CFD .isEmpty();
				
			 if (!valida_info_CFD ) {
					testCase.addQueryEvidenceCurrentStep(result_info_CFD);
				}
				assertFalse("No se muestran registros en las tablas ", valida_info_CFD);
				System.out.println( valida_info_CFD);

		
	     String account_id = result_info_CFD.getData(0, "account_id");
	     System.out.println("ACCOUNT_ID= " + account_id);
	
		
        //************************************************************************************************/
		//Paso 2
	    //Consultar Beneficios
	    addStep("Validar que exista informacion: Consultar Beneficios"); 
	    
	    String format_beneficios = String.format(beneficios, account_id );
	    
	    System.out.println(format_beneficios );
	    
	    SQLResult result_beneficios  = executeQuery(db,format_beneficios);	
		 
		 boolean valida_beneficios  = result_beneficios .isEmpty();
			
		 if (!valida_beneficios ) {
				testCase.addQueryEvidenceCurrentStep(result_beneficios );
			}
			assertFalse("No se muestran registros en TPE_FR_TRANSACTION ",  valida_beneficios );
			System.out.println( valida_beneficios);
		
		
	

		/* ***********************************************************************************************
		 * Folio QRY01
		 * ***********************************************************************************************/
		//Paso 3
		addStep("Ejecutar el servicio TPE.FCP.Pub:request para realizar la transaccion de Solicitud de Llaves.");
		
		String respuestaQRY01 = rtpUtil.generacionFolioTransaccion();
		
		System.out.println("Respuesta: " + respuestaQRY01);
		
		folio = RequestUtil.getFolioXml(respuestaQRY01);
		//Se guarda el request en wmCodeRequestQRY01
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
		
		//Paso 5
		addStep("Verificar datos insertados en TPUSER.TPE_FCP_TRANSACTION");
		
		String format_TPE_FR_TRANSACTION = String.format(TPE_FR_TRANSACTION, folio);
	    System.out.println("Consultar folio en TPE_FR_TRANSACTION: "+ TPE_FR_TRANSACTION);
	    
		SQLResult result_TPE_FR_TRANSACTION = executeQuery(db,format_TPE_FR_TRANSACTION);	
		 
		 boolean valida_TPE_FR_TRANSACTION = result_TPE_FR_TRANSACTION.isEmpty();
			
		 if (!valida_TPE_FR_TRANSACTION) {
				testCase.addQueryEvidenceCurrentStep(result_TPE_FR_TRANSACTION);
			}
			assertFalse("No se muestran registros en TPE_FR_TRANSACTION ",  valida_TPE_FR_TRANSACTION );
			System.out.println( valida_TPE_FR_TRANSACTION );

	

      
	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_TPE_FCP_005_Qry2CB";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Realizar la consulta de informacion de beneficios activos para el cardId 9100268371270";
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