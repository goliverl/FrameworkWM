package interfaces.tpe.fcp;


import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_FCP;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;


public class FCPQry05Tae extends BaseExecution {

	/*
	 * 
	 * @cp TAE_Realizar la consulta los datos frecuentes de clientes(telefonos de TAE)
	 * 
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_TPE_FCP_003_Qry05Tae(HashMap<String, String> data) throws Exception {
		
		//Nota:  Pruebas pendientes  ya que hay errores en el XML.

		/*
		 * Utilerias
		 *********************************************************************/
		utils.sql.SQLUtil db= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE , GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		TPE_FCP fcpUtil = new TPE_FCP(data, testCase, db);

		/*
		 * Variables
		 *********************************************************************/
		String queryValidation01 = "SELECT D.CODE, D.VALUE1, D.VALUE2, D.ACCOUNT_ID " + 
				"FROM FCPUSER.TPE_FCP_ACCESS A, FCPUSER.TPE_FCP_ACCOUNT C, FCPUSER.TPE_FCP_FREQ_DATA D " + 
				"WHERE A.ACCOUNT_ID = C.ACCOUNT_ID " + 
				"AND C.ACCOUNT_ID = D.ACCOUNT_ID " + 
				"AND A.STATUS = 'A' " + 
				"AND C.STATUS = 'A' " + 
				"AND D.APPLICATION = '" +data.get("application")+"' " + 
				"AND D.ENTITY = '" +data.get("entity")+"' " + 
				"AND D.CATEGORY = 'TAE_LIST' " + 
				"AND D.STATUS = 'A' " + 
				"AND A.ACCESS_ID = '" +data.get("cardId")+"'";

		String queryValidation02 = "SELECT OPERATION,FOLIO,CREATION_DATE,PLAZA,TIENDA,WM_CODE,WM_DESC"
				+ " FROM TPEUSER.TPE_FR_TRANSACTION WHERE APPLICATION = '" +data.get("application")+"' "
				+ "AND ENTITY = '" +data.get("entity")+"' AND OPERATION = '" +data.get("operation")+"' AND FOLIO = %s";

		String folio ;
		
		String wmCodeRequestQRY01, wmCodeDbQRY01;
		String wmCodeToValidateQRY01 = "101";
		
		
		String validaFolio = "SELECT folio, wm_code FROM TPEUSER.TPE_FR_TRANSACTION WHERE folio = '%s'"
		           + "AND creation_date>= (sysdate-1) ";


		/*
		 * Pasos
		 ************************************************************************************************/
		// Paso 1
		//como validar la lista de telefonos?
		addStep("Validar que exista informacion de los datos frecuentes para el cardId.");
		
		
		System.out.println(queryValidation01);
		
		SQLResult result_query01 = executeQuery(db,queryValidation01 );	
		 
		 boolean valida_query01 = result_query01.isEmpty();
			
		 if (!valida_query01) {
				testCase.addQueryEvidenceCurrentStep(result_query01);
			}
			assertFalse("No se muestran registros en TPE_FR_TRANSACTION ", valida_query01);
			System.out.println(valida_query01);
	

	    
		// Paso 2
	    //NO SE TIENE XML PARA REALIZAR EL INVOKE, POR LO QUE NO SE SABE SI FUNCIONA ESTE PASO
		
		addStep("Ejecutar el servicio TPE.FCP.Pub:request para realizar la transaccion.");

		String respuestaQRY01 = fcpUtil.ejecutarQRY01();
		System.out.println("Respuesta:\n" + respuestaQRY01);
		folio = RequestUtil.getFolioXml(respuestaQRY01);
		wmCodeRequestQRY01 = RequestUtil.getSimpleDataXml(respuestaQRY01,"wmcode");
		
		boolean validationResponseFolio = true;

		 
        if(respuestaQRY01!= null) {
        	
        	validationResponseFolio= false;
        	
        	}
          assertFalse(validationResponseFolio);

		
		// Paso 3
		addStep("Verificar que la interface retorna el XML de respuesta con el detalle de la transaccion.");
		
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
			
		// Paso 4
		addStep("Se registra la transaccion de consulta en la tabla TPE_FR_TRANSACTION de TPEUSER.");

		String query02 = String.format(queryValidation02, folio);
		
		SQLResult result_TPE_FR_TRANSACTION = executeQuery(db,query02 );	
		 
		 boolean valida_TPE_FR_TRANSACTION = result_TPE_FR_TRANSACTION.isEmpty();
			
		 if (!valida_TPE_FR_TRANSACTION) {
				testCase.addQueryEvidenceCurrentStep(result_TPE_FR_TRANSACTION);
			}
			assertFalse("No se muestran registros en TPE_FR_TRANSACTION ",  valida_TPE_FR_TRANSACTION );
			System.out.println( valida_TPE_FR_TRANSACTION );

	
	}

	@Override
	public void beforeTest() {
	}

	@Override
	public String setTestDescription() {
		return "Realizar la consulta los datos frecuentes de clientes(telefonos de TAE)";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_TPE_FCP_003_Qry05Tae";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

}
