package interfaces.tpe.fcp;

import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_FCP;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class OPR01 extends BaseExecution{
	
	/*
	 * 
	 * @cp SIN_Validar la operacion OPR01 de la entity SIN
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_TPE_FCP_013_OPR01(HashMap<String, String> data) throws Exception {
		
		/* Utilerias *********************************************************************/
//		SqlUtil db = new SqlUtil(GlobalVariables.DB_USERPE3, GlobalVariables.DB_PASSWORDPE3, GlobalVariables.DB_HOSTPE3);
		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);

		TPE_FCP fcpUtil = new TPE_FCP(data, testCase, db);	
		
        /* Variables *********************************************************************/		
		String queryValidation = "SELECT OPERATION,FOLIO,CREATION_DATE,PLAZA,TIENDA,WM_CODE,WM_DESC "
				+ "FROM TPEUSER.TPE_FR_TRANSACTION "
				+ "WHERE APPLICATION = 'FCP' AND ENTITY = 'SIN' AND OPERATION = 'OPR01' AND FOLIO = %s";
		
 		String folio;
		String wmCodeRequestTRN01;
		String wmCodeToValidateTRN01 = "100";
		
		
		/* Pasos ************************************************************************************************/	
		//Paso 1
		
		addStep("Ejecutar el servicio TPE.FCP.Pub:request para realizar la transaccion.");
		
	     	   String respuestaTRN01 = fcpUtil.ejecutarOPR01();	
		       System.out.println("Respuesta:\n" + respuestaTRN01);	
		       folio = RequestUtil.getFolioXml(respuestaTRN01);
		       wmCodeRequestTRN01 = RequestUtil.getWmCodeXml(respuestaTRN01);
	
	    testCase.passStep();
		
	    //Paso 2
	  	addStep("Verificar que la interface retorna el XML de respuesta con el detalle de la transaccion.");
	  		
	  			boolean validationResponseTRN01 = wmCodeRequestTRN01.equals(wmCodeToValidateTRN01);
	  			System.out.println(validationResponseTRN01 + " - wmCode response: " + wmCodeRequestTRN01);  			
	  			testCase.addTextEvidenceCurrentStep("Folio: "+folio);
	  			testCase.addTextEvidenceCurrentStep("Wm_Code: "+wmCodeRequestTRN01);
	  		
	  			assertTrue(validationResponseTRN01);
	  	
	  		
	  		
	  	//Paso 3
		addStep("Validar la insercion de la transaccion en la tabla TPE_FR_TRANSACTION de TPEUSER.");
		
		       String query = String.format(queryValidation, folio);		
		       
		       SQLResult wmCodeDbTRN01 = executeQuery(db, query);
		       
		       boolean validationDbTRN01 = wmCodeDbTRN01.isEmpty();
		       if(!validationDbTRN01) {
		    	   
		    	   testCase.addQueryEvidenceCurrentStep(wmCodeDbTRN01);
		    	   
		       }
		
	   
		       System.out.println(validationDbTRN01);
				
		       assertTrue(!validationDbTRN01);
	
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		return "Validar la operacion OPR01 de la entity SIN";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_TPE_FCP_013_OPR01";
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
