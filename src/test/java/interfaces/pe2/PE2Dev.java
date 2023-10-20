package interfaces.pe2;


import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.lang.Override;
import java.lang.String;
import java.util.HashMap;
import modelo.BaseExecution;
import om.PE2;
import util.GlobalVariables;
import utils.sql.SQLResult;

import org.testng.annotations.Test;

public class PE2Dev extends BaseExecution {
	
	
	/*
	 * 
		@cp1 Dev - Solicitud de ACK de devolucion
		@cp2 Dev - Solicitud de devolucion
		@cp3 Dev - Solicitud de Folio
	 * 
	 */

	
 @Test(dataProvider = "data-provider")
 public void ATC_FT_006_PE2_Solicitud_Folio_Devolucion(HashMap<String, String> data) throws Exception {
	/* Utilerias ************************************************************************/
	// utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST, GlobalVariables.DB_USER, GlobalVariables.DB_PASSWORD);
	 utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA,GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		
	    PE2 pe2Util = new PE2(data, testCase, db);
		
	/* Variables ********************************************************************************/
		
		String wmCodeToValidateFolio = "100";
		String tdcTransactionQuery = "SELECT folio, wm_code FROM tpeuser.tdc_devolution  WHERE folio = %s";
		
		String wmCodeToValidateDev = "000";
		String tdcTransactionQueryDev = "SELECT folio, wm_code from tpeuser.tdc_devolution WHERE folio = %s";
		
		String wmCodeToValidateAck = "101";
		String tdcTransactionQueryAck = "SELECT folio, wm_code FROM tpeuser.tdc_devolution WHERE folio = %s";
		
		String folio;
	    String wmCodeRequest;
	    String wmCodeDb;
		
    /* Pasos *******************************************************************************************************/
	//Paso 1
	testCase.nextStep("Llamar al servico PE2.Pub:runGetFolio http://10.184.40.110:8890/invoke/PE2.Pub/runGetFolio?plaza=10MON&tienda=50EDI&caja=2&type=V") ;

	  	//Ejecutar servicio
    	String responseRunGetFolio = pe2Util.ejecutarRunGetFolio();
    	folio =  getSimpleDataXml(responseRunGetFolio, "folio");
    	wmCodeRequest = getSimpleDataXml(responseRunGetFolio, "wmCode");
    	System.out.println("Folio: " + folio); 	
    	
    testCase.passStep();
   
    //Paso 2
	testCase.nextStep("Verificar la respuesta generada por el servicio " );
			
	    //Se valida que el wm_code del xml de respuesta sea igual a 100
        boolean validationRequest = wmCodeRequest.equals(wmCodeToValidateFolio);
        System.out.println(validationRequest + " - wmCode request: " + wmCodeRequest);
        testCase.addTextEvidenceCurrentStep("Response: \n"+responseRunGetFolio);//Se agrega la respuesta a la evidencia
        testCase.addTextEvidenceCurrentStep("Folio: "+folio);//Se agrega a la evidencia el folio
 	    testCase.addTextEvidenceCurrentStep("WM Code: "+wmCodeRequest);//Se agrega a la evidencia el wm_code
      
    testCase.validateStep(validationRequest);

    //Paso 3
    testCase.nextStep("Validar que se ha creado un registro del folio en la tabla tdc_transaction de TPEUSER, pasando como folio el que recibimos en la respuesta de la ejecucion del servicio. SELECT * FROM tpeuser.tdc_transaction WHERE folio =  [folio] AND status = 100;") ;
    
        //Consulta a BD, se forma el query a utilizar
        String query = String.format(tdcTransactionQuery, folio);
    
        //Se realiza la consulta a la BD y se obtiene el wm_code
        SQLResult result1 = executeQuery(db, query);
		wmCodeDb = result1.getData(0, "WM_CODE");
		testCase.addQueryEvidenceCurrentStep(result1);
        //wmCodeDb = pe2Util.getWmCodeQuery(query);
        System.out.println("Code DB: "+wmCodeDb);
    
        //Se valida que sea igual a 100
        boolean validationDb = wmCodeDb.equals(wmCodeToValidateFolio);
        System.out.println(validationDb + " - wmCode db: " + wmCodeDb);

    testCase.validateStep(validationDb);
	    
	/**************************************************************************************************
	 * Solicitud de autoriazacion Devolucion
	 *************************************************************************************************/
	
    //Paso 4
    testCase.nextStep("Llamar al servico PE2.Pub:runGetDev y pasar como parametros algunos de los valores retornados al crear el folio de solicitud."); 
		
        //Ejecuta el servicio PE2.Pub:runGetDev
        String responseRunGetDev = pe2Util.ejecutarRunGetDev(folio);
        System.out.println(responseRunGetDev); 
   
        //Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la evidencia 
        wmCodeRequest = getSimpleDataXml(responseRunGetDev, "wmCode");	     
	
	testCase.passStep();
	
	//Paso 5
	testCase.nextStep("Verificar la respuesta generada por el servicio");
		
		//Se valida que el wm_code del xml de respuesta sea igual a 000
	    boolean validationRequestDev = wmCodeRequest.equals(wmCodeToValidateDev);
	    System.out.println(validationRequestDev + " - wmCode request: " + wmCodeRequest);
	    testCase.addTextEvidenceCurrentStep("Response: \n"+responseRunGetDev);//Se agrega response a evidencia
	    testCase.addTextEvidenceCurrentStep("Folio: "+folio);//Se agrega a la evidencia el folio
	    testCase.addTextEvidenceCurrentStep("WM Code: "+wmCodeRequest);//Se agrega a la evidencia el wm_code
	    
	   // assertTrue(validationRequestDev,"No se genero el wmCode esperado");
	testCase.validateStep(validationRequestDev,"No se genero el wmCode esperado");
	
	//Paso 6
	testCase.nextStep("Validar que se insertaron los datos proporcionados como parametros para el folio indicado.");
		
	    //Consulta a BD, se forma el query a utilizar
        String queryAuth = String.format(tdcTransactionQueryDev, folio);
		
        //Se realiza la consulta a la BD y se obtiene el wm_code
        SQLResult result2 = executeQuery(db, queryAuth);
		wmCodeDb = result2.getData(0, "WM_CODE");
		testCase.addQueryEvidenceCurrentStep(result2);
        //wmCodeDb = pe2Util.getWmCodeQuery(queryAuth);
        System.out.println("Code DB: "+wmCodeDb);
    
        //Se valida que sea igual a 000
        boolean validationDbDev = wmCodeDb.equals(wmCodeToValidateDev);
        System.out.println(validationDbDev + " - wmCode db: " + wmCodeDb);
										
    testCase.validateStep(validationDbDev,"No se genero el wmCode esperado");
	
	/**************************************************************************************************
	 * Solicitud de autoriazacion ACK Devolucion
	 *************************************************************************************************/
	
	//Paso 7
	testCase.nextStep("llamar al servico PE2.Pub:runGetDevAck");
	
	    //Ejecuta el servicio PE2.Pub:runGetDevAck
        String responseRunGetAck = pe2Util.ejecutarRunGetDevAck(folio);
        System.out.println(responseRunGetAck); 
    
        //Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la evidencia
        wmCodeRequest = getSimpleDataXml(responseRunGetAck, "wmCode");	     
       
	testCase.passStep();
	
	testCase.nextStep("Verificar la respuesta generada por el servicio");
	
	    //Se valida que el wm_code del xml de respuesta sea igual a 101
        boolean validationRequestAck = wmCodeRequest.equals(wmCodeToValidateAck);
        System.out.println(validationRequestAck + " - wmCode request: " + wmCodeRequest);
        testCase.addTextEvidenceCurrentStep("Response: \n"+responseRunGetAck);//Se agrega response a evidencia
        testCase.addTextEvidenceCurrentStep("Folio: "+folio);//Se agrega a la evidencia el folio
        testCase.addTextEvidenceCurrentStep("WM Code: "+wmCodeRequest);//Se agrega a la evidencia el wm_code
       
    testCase.validateStep(validationRequestAck);
	
	
	testCase.nextStep("Validar que el estatus del folio ha sido actualizado en la tabla tdc_transaction de TPEUSER.");
	
	    //Consulta a BD, se forma el query a utilizar
        String queryAck = String.format(tdcTransactionQueryAck, folio);
		
        //Se realiza la consulta a la BD y se obtiene el wm_code
        SQLResult result3 = executeQuery(db, query);
		wmCodeDb = result3.getData(0, "WM_CODE");
		testCase.addQueryEvidenceCurrentStep(result3);
		
        //wmCodeDb = pe2Util.getWmCodeQuery(queryAck);
    
        //Se valida que sea igual a 101
        boolean validationDbAck = wmCodeDb.equals(wmCodeToValidateAck);			
        System.out.println(validationDbAck + " - wmCode db: " + wmCodeDb);
										
    testCase.validateStep(validationDbAck);	   

  }
	

  @Override
  public String setTestFullName() {
    return "ATC_FT_006_PE2_Solicitud_Folio_Devolucion" ;
  }

  @Override
  public String setTestDescription() {
    return "Solicitud de ACK de devolucion\r\n"
    		+ "Solicitud de devolucion\r\n"
    		+ "Solicitud de Folio" ;
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
