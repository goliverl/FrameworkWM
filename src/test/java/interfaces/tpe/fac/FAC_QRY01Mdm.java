package interfaces.tpe.fac;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_FAC;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;

public class FAC_QRY01Mdm extends BaseExecution {
	
	/*
	 * 
	 * @cp Verificar la operacion QRY01 entity MDM
	 * 
	 */

@Test(dataProvider = "data-provider")
	
	public void ATC_FT_003_TPE_FAC_QRY01Mdm(HashMap<String, String> data) throws Exception {
				
/* Utilerias *********************************************************************/
//		SqlUtil db = new SqlUtil(GlobalVariables.DB_USERPE1, GlobalVariables.DB_PASSWORDPE1, GlobalVariables.DB_HOSTPE1);
		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCACQA,GlobalVariables.DB_USER_FCACQA, GlobalVariables.DB_PASSWORD_FCACQA);
	
		TPE_FAC facUtil = new TPE_FAC(data, testCase, db);
	
		
/* Variables *********************************************************************/
			
	
		String wmCodeQR01 = "101";	
		String tdcQRY01Mdm = "SELECT WM_CODE,APPLICATION,ENTITY,OPERATION,FOLIO FROM TPEUSER.TPE_FR_TRANSACTION"
								+ " WHERE APPLICATION = 'FAC'"					
								//+" AND ENTITY ="+ data.get("entity")
								+ " AND ENTITY = 'BANK'"					
								+ " AND OPERATION = 'QRY01'"
								+ " AND folio = %s "
								+ " AND CREATION_DATE >= (SYSDATE-1)";
	String wmCodeRequestQRY01;
						
	String folio;
	String wmCodeRequest;
	String wmCodeDb;
		
	
	
/* Paso 1 *****************************************************************************************/
		
//Paso 1
	addStep("Llamar al servicio de consulta QRY01");
			
				String respuestaQRY01MDM = facUtil.QRY01MDM();
			
				System.out.println("Respuesta: " + respuestaQRY01MDM);
			
			
			
				folio = RequestUtil.getFolioXml(respuestaQRY01MDM);
				wmCodeRequestQRY01 = RequestUtil.getWmCodeXml(respuestaQRY01MDM);		
			
		testCase.passStep();	
/* Paso 2 *****************************************************************************************/
		
//Paso 2	

		addStep("Verificar la respuesta generada por el servicio");
		
		
		boolean validationRequest = wmCodeQR01.equals(wmCodeRequestQRY01);
	
		System.out.println(validationRequest + " - wmCode request: " );
		
		

	testCase.validateStep(validationRequest);
	
/*Paso 3 *********************************************************/

	addStep("Validar que se ha creado un registro en la tabla tdc_transaction de TPEUSER");
					
						String query = String.format(tdcQRY01Mdm, folio);
						
						System.out.println(query);
						
						Thread.sleep(6000);
						
						SQLResult Transaction = executeQuery(db, query);
							
						wmCodeDb = Transaction.getData(0, "WM_CODE");
												
						boolean validationDb = wmCodeQR01.equals(wmCodeDb);
						
						System.out.println(validationDb + " - wmCode db: ");
								
									
	testCase.validateStep(validationDb);		
	
}	
	
	
	
	
	
	
	
	
	
	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Verificar la operacion QRY01 entity MDM";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_003_TPE_FAC_QRY01Mdm";
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
