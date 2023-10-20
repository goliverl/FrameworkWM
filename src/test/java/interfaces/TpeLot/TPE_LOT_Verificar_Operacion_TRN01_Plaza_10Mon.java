package interfaces.TpeLot;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.TPE_LOT;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class TPE_LOT_Verificar_Operacion_TRN01_Plaza_10Mon extends BaseExecution  {
	
	/*
	 * 
	 * @cp Verificar la operacion TRN01 - Plaza 10MON
	 * 
	 */
	
@Test(dataProvider = "data-provider")
	
	public void ATC_FT_001_TPE_LOT_TPE_LOT_Verificar_Operacion_TRN01_Plaza_10Mon(HashMap<String, String> data) throws Exception {
				
/* UtilerIas *********************************************************************/

		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_TPE_LOT, GlobalVariables.DB_USER_TPE_LOT, GlobalVariables.DB_PASSWORD_TPE_LOT);


		TPE_LOT TPELOTUTIL = new TPE_LOT(data, testCase, db);
	
		
		
/* Variables *********************************************************************/
					
    String wmCodeTRN01 = "100";	
	String tdcQRY01Bank = " SELECT APPLICATION,ENTITY,OPERATION,FOLIO,CREATION_DATE,PLAZA,"
			            + " TIENDA,MTI,PROC_CODE,WM_CODE,WM_DESC,LOCAL_DT FROM TPEUSER.TPE_FR_TRANSACTION \n"
			            + " WHERE APPLICATION='LOT' AND "
			            + " ENTITY='MLJ' AND "
			            + " OPERATION='TRN01' AND "
			            + " TRUNC(CREATION_DATE)=TRUNC(SYSDATE) AND "
			            + " PLAZA='10MON' AND "
			            + " FOLIO=%s";
		
	String wmCodeRequesTRN01;
						
	String folio;
		
		
/* Paso 1 *****************************************************************************************/
		
		addStep("Ejecutar via HTTP la interface con el XML indicado");
			
				String respuestaTRN01 = TPELOTUTIL.TRN01();
			
				System.out.println("Respuesta: " + respuestaTRN01);
				folio = RequestUtil.getFolioXml(respuestaTRN01);
				System.out.println(folio);
				wmCodeRequesTRN01 = RequestUtil.getWmCodeXml(respuestaTRN01);		
			
		testCase.passStep();

/*Paso 2 *********************************************************/

		addStep("Verificar xmlResponse");
			
				boolean validationRequest = wmCodeTRN01.equals(wmCodeRequesTRN01);
			
				System.out.println(validationRequest + " - wmCode request: " + wmCodeRequesTRN01);
		
		testCase.validateStep(validationRequest);

/*Paso 3 *********************************************************/

		addStep("Verificar la tabla TPE_FR_TRASACTION");
		
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
		return "Verificar la operacion TRN01(Solicitud de folio)";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_TPE_LOT_TPE_LOT_Verificar_Operacion_TRN01_Plaza_10Mon";
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
