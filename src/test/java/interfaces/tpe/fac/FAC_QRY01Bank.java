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

public class FAC_QRY01Bank extends BaseExecution  {
	
	/*
	 * 
	 * @cp Verificar la operacion QRY01 con el entity BANK
	 * 
	 */
	
@Test(dataProvider = "data-provider")
	
	public void ATC_FT_001_TPE_FAC_QRY01Bank(HashMap<String, String> data) throws Exception {
				
/* Utilerias *********************************************************************/
//		SqlUtil db = new SqlUtil(GlobalVariables.DB_USERPE1, GlobalVariables.DB_PASSWORDPE1, GlobalVariables.DB_HOSTPE1);
//		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCTAEQA, GlobalVariables.DB_USER_FCTAEQA, GlobalVariables.DB_PASSWORD_FCTAEQA);
		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCACQA, GlobalVariables.DB_USER_FCACQA, GlobalVariables.DB_PASSWORD_FCACQA);


		TPE_FAC facUtil = new TPE_FAC(data, testCase, db);
	
		
/* Variables *********************************************************************/
			
	
		
    String wmCodeQR01 = "101";	
	String tdcQRY01Bank = "SELECT APPLICATION,ENTITY,OPERATION,FOLIO FROM TPEUSER.TPE_FR_TRANSACTION"
						+ " WHERE APPLICATION = 'FAC'"					
						+ " AND ENTITY = 'BANK' "
						+ " AND OPERATION = 'QRY01'"
						+ "AND CREATION_DATE >= (SYSDATE-1)"
						+ " AND folio = %s ";
		
	String wmCodeRequestQRY01;
						
	String folio;
	String wmCodeRequest;
//	String wmCodeDb;
		
		
/* Paso 1 *****************************************************************************************/
		
//Paso 1
		addStep("Llamar al servicio de consulta QRY01");
			
				String respuestaQRY01Bank = facUtil.QRY01BANK();
			
				System.out.println("Respuesta: " + respuestaQRY01Bank);
			
			
			
				folio = RequestUtil.getFolioXml(respuestaQRY01Bank);
				wmCodeRequestQRY01 = RequestUtil.getWmCodeXml(respuestaQRY01Bank);		
			
		testCase.passStep();

/*Paso 2 *********************************************************/
//Paso 2
		addStep("Verificar la respuesta generada por el servicio");
			
				
				boolean validationRequest = wmCodeQR01.equals(wmCodeRequestQRY01);
			
				System.out.println(validationRequest + " - wmCode request: " + wmCodeRequestQRY01);
		
		testCase.validateStep(validationRequest);

/*Paso 3 *********************************************************/

		addStep("Validar que se ha creado un registro en la tabla tdc_transaction de TPEUSER");
		
		String query = String.format(tdcQRY01Bank, folio);
		System.out.println(query);

	       
	       SQLResult wmCodeDb = executeQuery(db, query);
	       
	       boolean validationDb = wmCodeDb.isEmpty();
	       if(!validationDb) {
	    	   
	    	   testCase.addQueryEvidenceCurrentStep(wmCodeDb);
	    	   
	       }
	

	       System.out.println(validationDb);
			
	      assertFalse(validationDb);
	
	
}
	
	

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Verificar la operacion QRY01 con el entity BANK";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_TPE_FAC_QRY01Bank";
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
