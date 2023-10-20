package interfaces.tpe.fac;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_FAC;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
public class FACConSadelPreRequisitos extends BaseExecution {
	
@Test(dataProvider = "data-provider")
	
	public void test(HashMap<String, String> data) throws Exception {

//	SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FAC, GlobalVariables.DB_USER_FAC, GlobalVariables.DB_PASSWORD_FAC);
	SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCACQA, GlobalVariables.DB_USER_FCACQA, GlobalVariables.DB_PASSWORD_FCACQA);

	TPE_FAC facUtil = new TPE_FAC(data, testCase, db);

	
	 String queryTransaction = "SELECT PLAZA,TIENDA,FOLIO,CREATION_DATE,IS_NAME,WM_CODE,WM_DESC FROM TPEUSER.TPE_FR_TRANSACTION"
				+ " WHERE APPLICATION = 'FAC'"	
				+ " AND CREATION_DATE >= (SYSDATE-1)"
				+ " AND folio = %s";
	
	String queryTienda = "SELECT * FROM TPEUSER.CONFIG_TIENDAS_SADEL WHERE CR_TIENDA = '"+data.get("tienda")+"'";
	
	String wmCodeToValidate = data.get("wmCode");   
	String wmDescToValidate = data.get("mensaje");
	double wmTimeToValidate = Double.parseDouble(data.get("tiempo"));
	
	String act = "A";
	String inact = "I";
	String nuevo = "N";
	String bloqueado = "B";
	String reset = "R";
	String folio;
	String wmCodeRequest;
	String wmDescRequest;
	double tiempoRespuesta=0;
	String wmCodeDb;
	
	testCase.setProject_Name("Configuracion de ambientes.");
	
	
	//Pasos------------------------------------------------------------------------------
	addStep("RESUMEN");
	
	//paso1------------------------------------------------------------------------------
	testCase.addBoldTextEvidenceCurrentStep("Ejecutar el servicio de consulta QRY01 con el servidor "+data.get("host"));
	   long inicio = facUtil.obtenerSegundosInicio();
	   
	   String respuesta = facUtil.QRYConSadel();
		
	   long fin = facUtil.obtenerSegundosFin();

	   System.out.println("Respuesta: " + respuesta);
					
	   folio = RequestUtil.getFolioXml(respuesta);
	   
	   wmCodeRequest = RequestUtil.getWmCodeXml(respuesta);
	   
		if(!respuesta.isEmpty() && wmCodeRequest.equals(wmCodeToValidate)) {
			testCase.addTextEvidenceCurrentStep("-Se ejecutó correctamente el request.");
		}else {		
			testCase.addTextEvidenceCurrentStep("-No se ejecutó correctamente el request. El código wmCode no es el esperado: " + wmCodeToValidate);
		}
	
	//paso 2------------------------------------------------------------------------------
	testCase.addBoldTextEvidenceCurrentStep("Validar que el tiempo de respuesta del request sea menor a 8 segundos.");
		tiempoRespuesta = facUtil.obtenerDiferencia(fin, inicio);
	    
		boolean validationRequestTime=false; 
		
		if (tiempoRespuesta<=wmTimeToValidate){
			
			validationRequestTime=true;
			
		}		
		
		System.out.println(validationRequestTime + " - time request: " + tiempoRespuesta);	
		
		if(validationRequestTime) {
			testCase.addTextEvidenceCurrentStep("-Tiempo obtenido de respuesta: "+tiempoRespuesta);		
			testCase.addTextEvidenceCurrentStep("-El tiempo de respuesta es menor de 8 segundos");
		}else {
			testCase.addTextEvidenceCurrentStep("-Tiempo obtenido de respuesta: "+tiempoRespuesta);		
			testCase.addTextEvidenceCurrentStep("-Sobrepasa el tiempo de respuesta esperado.");
		}
		
	//paso 3------------------------------------------------------------------------------
	testCase.addBoldTextEvidenceCurrentStep("Verificar que la tienda cuente con sadel.");

	    System.out.println(queryTienda);
	    
	    SQLResult resultTienda = executeQuery(db, queryTienda);
							
		boolean validationDbTienda = resultTienda.isEmpty();
							
		
		if(!validationDbTienda) {
			testCase.addTextEvidenceCurrentStep("-La tienda cuenta con Sadel.");
		}else {		
			testCase.addTextEvidenceCurrentStep("-La tienda no cuenta con Sadel.");
		}	
		
	//paso 4------------------------------------------------------------------------------
	testCase.addBoldTextEvidenceCurrentStep("Validar que se ha creado un registro en la tabla TPE_FR_TRANSACTION de TPEUSER.");
		    
		    if(data.get("name").equals("Servidor FCWMQA9")){
		    	Thread.sleep(8000);
		    }
		       					
			String query = String.format(queryTransaction, folio);
			
			System.out.println(query);
		 
			SQLResult result = executeQuery(db, query);
								
			boolean validationDb = result.isEmpty();						
			
			if(!validationDb) {
				testCase.addTextEvidenceCurrentStep("-Se creó un registro del request.");
			}else {		
				testCase.addTextEvidenceCurrentStep("-No se creó un registro del request.");
			}		
		
		
		
	
	
	//*********************************EVIDENCIA**********************************************************/
	testCase.addBoldTextEvidenceCurrentStep("EVIDENCIAS:");
	//*******************Paso 1 *************************************************************************************************************	
	
	addStep("Ejecutar el servicio de consulta QRY01 con el servidor "+data.get("host"));
	
//	   inicio = facUtil.obtenerSegundosInicio();
//	   
	   respuesta = facUtil.QRYConSadel();
//		
//	   fin = facUtil.obtenerSegundosFin();
//
//	   System.out.println("Respuesta: " + respuesta);
//					
//	   folio = RequestUtil.getFolioXml(respuesta);
//	   
//	   wmCodeRequest = RequestUtil.getWmCodeXml(respuesta);
	   
		if(!respuesta.isEmpty() && wmCodeRequest.equals(wmCodeToValidate)) {
			testCase.addTextEvidenceCurrentStep("Se ejecutó correctamente el request.");
		}else {		
			testCase.addTextEvidenceCurrentStep("No se ejecutó correctamente el request. El código wmCode no es el esperado: " + wmCodeToValidate);
		}
			
		
  /*Paso 2 *********************************************************/
		
	    addStep("Validar que el tiempo de respuesta del request sea menor a 8 segundos.");
			
//	    tiempoRespuesta = facUtil.obtenerDiferencia(fin, inicio);
	    
		validationRequestTime=false; 
		
		if (tiempoRespuesta<=wmTimeToValidate){
			
			validationRequestTime=true;
			
		}		
		
		System.out.println(validationRequestTime + " - time request: " + tiempoRespuesta);	
		
		if(validationRequestTime) {
			testCase.addTextEvidenceCurrentStep("Tiempo obtenido de respuesta: "+tiempoRespuesta);		
			testCase.addTextEvidenceCurrentStep("El tiempo de respuesta es menor de 8 segundos.");
		}else {
			testCase.addTextEvidenceCurrentStep("Tiempo obtenido de respuesta: "+tiempoRespuesta);		
			testCase.addTextEvidenceCurrentStep("Sobrepasa el tiempo de respuesta esperado.");
		}
		

	    
        /*Paso 3 *********************************************************/
		
	    addStep("Verificar que la tienda cuente con sadel.");
	    
	    System.out.println(queryTienda);
	    
	    resultTienda = executeQuery(db, queryTienda);
		
		testCase.addQueryEvidenceCurrentStep(resultTienda);
							
		validationDbTienda = resultTienda.isEmpty();
							
		
		if(!validationDbTienda) {
			testCase.addTextEvidenceCurrentStep("La tienda cuenta con Sadel.");
		}else {		
			testCase.addTextEvidenceCurrentStep("La tienda no cuenta con Sadel.");
		}					
		

	     /*Paso 4 *********************************************************/

	    addStep("Validar que se ha creado un registro en la tabla TPE_FR_TRANSACTION de TPEUSER.");
		    
		    if(data.get("name").equals("Servidor FCWMQA9")){
		    	Thread.sleep(8000);
		    }
		       					
			query = String.format(queryTransaction, folio);
			
			System.out.println(query);
		 
			result = executeQuery(db, query);
			
			testCase.addQueryEvidenceCurrentStep(result);
								
			validationDb = result.isEmpty();						
			
			if(!validationDb) {
				testCase.addTextEvidenceCurrentStep("Se creó un registro del request.");
			}else {		
				testCase.addTextEvidenceCurrentStep("No se creó un registro del request.");
			}								
			
			
	
	
	
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

@Override
public String setTestDescription() {
	// TODO Auto-generated method stub
	return "Se verifica que la configuración de los cajeros con Sadel sea correcta.";
}

@Override
public String setTestDesigner() {
	// TODO Auto-generated method stub
	return "tbd";
}

@Override
public String setTestFullName() {
	// TODO Auto-generated method stub
	return "Validación de configuración de Cajeros con Sadel";
}

@Override
public String setTestInstanceID() {
	// TODO Auto-generated method stub
	return null;
}
				
	

}
