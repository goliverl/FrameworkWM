package interfaces.pe2;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PE2;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

public class PE2Auth extends BaseExecution {
	
	/*
	 * 
		@cp1 Auth - Solicitud de ACK de autorizacion para transaccion
		@cp2 Auth - Solicitud de autorizacion para transaccion
		@cp3 Auth - Solicitud de Folio
	 * 
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_PE2_Flujo_Auth_Interfaz(HashMap<String, String> data) throws Exception {
		/* Utilerias ************************************************************************/
		
		
		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA,GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		
		PE2 pe2Util = new PE2(data, testCase, db);
		
		/* Variables ********************************************************************************/
		
		String wmCodeToValidateFolio = "100";
		String tdcTransactionQuery = "SELECT FOLIO, WM_CODE \r\n"
				+ "FROM tpeuser.tdc_transaction \r\n"
				+ "WHERE FOLIO = '%s'";
		
		String wmCodeToValidateAuth = "000";
		String tdcTransactionQueryAuth = "SELECT FOLIO, WM_CODE \r\n"
				+ "FROM tpeuser.tdc_transaction \r\n"
				+ "WHERE folio = %s";
		
		String wmCodeToValidateAck = "101";
		String tdcTransactionQueryAck = "SELECT folio, wm_code \r\n"
				+ "FROM tpeuser.tdc_transaction \r\n"
				+ "WHERE folio = %s";
		
		String folio;
		String creationDate;
		String wmCodeRequest;
		String wmCodeDb = "";

		
       /* Pasos *******************************************************************************************************/
	//Paso 1
	testCase.nextStep("Llamar al servico PE2.Pub:runGetFolio http://10.80.20.103:5555/invoke/PE2.Pub/runGetFolio?plaza=10MON&tienda=50EDI&caja=2&type=V");
 
	    //Ejecuta el servicio PE2.Pub:runGetFolio
    	String responseRunGetFolio = pe2Util.ejecutarRunGetFolio();//obtener el folio del 
    	System.out.println(responseRunGetFolio); 
    	
    	//Se obtienen los datos folio, wm_code y creation_date del xml de respuesta y se agregan a la evidencia
    	folio =  getSimpleDataXml(responseRunGetFolio, "folio");
    	wmCodeRequest = getSimpleDataXml(responseRunGetFolio, "wmCode");
        creationDate =  getSimpleDataXml(responseRunGetFolio, "creationDate");
    	
//    testCase.passStep();
   
    //Paso 2
	testCase.nextStep("Verificar la respuesta generada por el servicio"); 
	   
	    //Se valida que el wm_code del xml de respuesta sea igual a 100
	    boolean validationRequest = wmCodeRequest.equals(wmCodeToValidateFolio);
	    System.out.println(validationRequest + " - wmCode request: " + wmCodeRequest);
	    
	    testCase.addTextEvidenceCurrentStep("Response: \n"+responseRunGetFolio);//Se agrega la respuesta a la evidencia
        testCase.addTextEvidenceCurrentStep("Folio: " + folio);//Se agrega a la evidencia el folio
        testCase.addTextEvidenceCurrentStep("Wm_Code: " + wmCodeRequest);//Se agrega a la evidencia el wm_code
        testCase.addTextEvidenceCurrentStep("CreationDate: " + creationDate);//Se agrega a la evidencia el creation_date
     
        assertTrue(validationRequest, "wmCode es diferente al esperado.");

    //Paso 3
    testCase.nextStep("Validar que se ha creado un registro del folio en la tabla tdc_transaction de TPEUSER, pasando como folio el que recibimos en la respuesta de la ejecucion del servicio. SELECT * FROM tpeuser.tdc_transaction WHERE folio =  [folio] AND status = 100;") ;
    
    	//Consulta a BD, se forma el query a utilizar
        String query = String.format(tdcTransactionQuery, folio);
	    
        //Se realiza la consulta a la BD y se obtiene el wm_code
        SQLResult result1 = executeQuery(db, query);
        if (!result1.isEmpty()) {
        	wmCodeDb = result1.getData(0, "WM_CODE");
    		testCase.addQueryEvidenceCurrentStep(result1);
        }
	    System.out.println("Code DB: "+wmCodeDb);
	    
	    //Se valida que sea igual a 100
	    boolean validationDb = wmCodeDb.equals(wmCodeToValidateFolio);
	    System.out.println(validationDb + " - wmCode db: " + wmCodeDb);
	
	    assertTrue(validationDb, "WM_CODE no es igual al esperado");
	
	/**************************************************************************************************
	 * Solicitud de autoriazacion
	 *************************************************************************************************/
	
    //Paso 4
    testCase.nextStep("Llamar al servico PE2.Pub:runGetAuth y pasar como parametros algunos de los valores retornados al crear el folio de solicitud.") ;

        //Ejecuta el servicio PE2.Pub:runGetAuth

        String responseRunGetAuth = pe2Util.ejecutarRunGetAuth(folio,creationDate,numExecution);
        System.out.println(responseRunGetAuth); 
        //Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la evidencia 
	    wmCodeRequest = getSimpleDataXml(responseRunGetAuth, "wmCode");	     
	 
//	testCase.passStep();
	
	//Paso 5
	testCase.nextStep("Verificar la respuesta generada por el servicio");
	   
	    //Se valida que el wm_code del xml de respuesta sea igual a 000
	    boolean validationRequestAuth = wmCodeRequest.equals(wmCodeToValidateAuth);
	    System.out.println(validationRequestAuth + " - wmCode request: " + wmCodeRequest);

	    testCase.addTextEvidenceCurrentStep("Response: \n"+responseRunGetAuth);//Se agrega response a evidencia
	    testCase.addTextEvidenceCurrentStep("Folio: "+folio);//Se agrega a la evidencia el folio
		testCase.addTextEvidenceCurrentStep("WM Code: "+wmCodeRequest);//Se agrega a la evidencia el wm_code
		
		assertTrue(validationRequestAuth, "wmCode es diferente al esperado");

	
	//Paso 6
	testCase.nextStep("Validar que se insertaron los datos proporcionados como parametros para el folio indicado.");
		
	    //Consulta a BD, se forma el query a utilizar
	    String queryAuth = String.format(tdcTransactionQueryAuth, folio);
			
	    //Se realiza la consulta a la BD y se obtiene el wm_code
	    SQLResult result2 = executeQuery(db, queryAuth);
	    if(!result2.isEmpty()) {
	    	wmCodeDb = result2.getData(0, "WM_CODE");
			testCase.addQueryEvidenceCurrentStep(result2);
	    }
	    System.out.println("Code DB: "+wmCodeDb);
	    
	    //Se valida que sea igual a 000
	    boolean validationDbAuth = wmCodeDb.equals(wmCodeToValidateAuth);
			
	    System.out.println(validationDbAuth + " - wmCode db: " + wmCodeDb);
											
	    assertTrue(validationDbAuth, "WM_CODE es diferente al esperado.");
	
	
	/**************************************************************************************************
	 * Solicitud de autoriazacion ACK
	 *************************************************************************************************/
	
	//Paso 7
	testCase.nextStep("llamar al servico PE2.Pub:runGetAuthAck");
	
	    //Ejecuta el servicio PE2.Pub:runGetAuthAck
        String responseRunGetAck = pe2Util.ejecutarRunGetAck(folio,creationDate);
        System.out.println(responseRunGetAck); 
        
        //Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la evidencia
        wmCodeRequest = getSimpleDataXml(responseRunGetAck, "wmCode");	     
	
//	testCase.passStep();
	
	//Paso 8
	testCase.nextStep("Verificar la respuesta generada por el servicio");
      
	    //Se valida que el wm_code del xml de respuesta sea igual a 101
	    boolean validationRequestAck = wmCodeRequest.equals(wmCodeToValidateAck);
        System.out.println(validationRequestAck + " - wmCode request: " + wmCodeRequest);
        testCase.addTextEvidenceCurrentStep("Response: \n"+responseRunGetAck);//Se agrega response a evidencia 
        testCase.addTextEvidenceCurrentStep("Folio: "+folio);//Se agrega a la evidencia el folio
	    testCase.addTextEvidenceCurrentStep("WM Code: "+wmCodeRequest);//Se agrega a la evidencia el wm_code
	    
	    assertTrue(validationRequestAck, "wmCode es diferente al esperado.");
	
    //Paso 9
	testCase.nextStep("Validar que el estatus del folio ha sido actualizado en la tabla tdc_transaction de TPEUSER.");
	
		//Consulta a BD, se forma el query a utilizar
	    String queryAck = String.format(tdcTransactionQueryAck, folio);
			
	    //Se realiza la consulta a la BD y se obtiene el wm_code
	    SQLResult result3 = executeQuery(db, queryAck);
	    if(!result3.isEmpty()) {
	    	wmCodeDb = result3.getData(0, "WM_CODE");
			testCase.addQueryEvidenceCurrentStep(result3);
	    }
	    
	    //Se valida que sea igual a 101
	    boolean validationDbAck = wmCodeDb.equals(wmCodeToValidateAck);			
	    System.out.println(validationDbAck + " - wmCode db: " + wmCodeDb);
		
	    assertTrue(validationDbAck, "WM_CODE es diferente al esperado");

  }
	

  @Override
  public String setTestFullName() {
    return "ATC_FT_004_PE2_Flujo_Auth_Interfaz";
  }

  @Override
  public String setTestDescription() {
    return "Solicitud de ACK de autorizacion para transaccion\r\n"
    		+ "Solicitud de autorizacion para transaccion\r\n"
    		+ "Solicitud de Folio";
  }

  @Override
  public String setTestDesigner() {
    return "Equipo de Automatizacion";
  }

  @Override
  public String setTestInstanceID() {
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