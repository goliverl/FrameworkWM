package interfaces.tpe.fac;

import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_FAC;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;

public class FAC_QRYBank extends BaseExecution {
	
	/*
	 * 
	 * @cp1 Verificar la operacion QRY02 entity BANK 
	 * @cp2 Verificar la operación QRY03 entity BANK 
	 * @cp3 Verificar la operación QRY04 entity BANK
	 * 
	 */
	
@Test(dataProvider = "data-provider")
	
	public void ATC_FT_006_TPE_FAC_QRYBank(HashMap<String, String> data) throws Exception {
				
/* Utilerias *********************************************************************/
	//	SqlUtil db = new SqlUtil(GlobalVariables.DB_USERPE3, GlobalVariables.DB_PASSWORDPE3, GlobalVariables.DB_HOSTPE3);
		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCACQA,GlobalVariables.DB_USER_FCACQA, GlobalVariables.DB_PASSWORD_FCACQA);
		TPE_FAC facUtil = new TPE_FAC(data, testCase, db);
	
		
/* Variables *********************************************************************/
			
	
		
    String wmCodeQR01 = "101";	
	String tdcQRYBank = "SELECT wm_code, APPLICATION,ENTITY,FOLIO FROM TPEUSER.TPE_FR_TRANSACTION"
							+ " WHERE APPLICATION = 'FAC'"					
							+ " AND ENTITY = 'BANK' "
							+ " AND folio = %s "
							+ " AND CREATION_DATE >= (SYSDATE-1)";

	String wmCodeRequestQRY01;
						
	String folio;
	String wmCodeRequest;
	String wmCodeDb;
	
	
/**
 * 	las consultas QRY02 Y QRY04  podran ser parametrizados	
 */
	
//	testCase.setTest_Description(data.get("descripcion"));
	
/* Paso 1 *****************************************************************************************/
		
//Paso 1
	addStep("Llamar al servicio de consulta QRY01");
			
				String respuestaQRYBank = facUtil.QRYBank();
			
				System.out.println("Respuesta: " + respuestaQRYBank);
			
			
			
				folio = RequestUtil.getFolioXml(respuestaQRYBank);
				wmCodeRequestQRY01 = RequestUtil.getWmCodeXml(respuestaQRYBank);		
			
		testCase.passStep();
/*Paso 2 *********************************************************/
//Paso 2
		addStep("Verificar la respuesta generada por el servicio");
					
						
			boolean validationRequest = wmCodeQR01.equals(wmCodeRequestQRY01);
					
			System.out.println(validationRequest + " - wmCode request: " );
				
		assertTrue(validationRequest , "wmCodeQR01 no es Igual a wmCodeRequestQRY01");

/*Paso 3 *********************************************************/

		addStep("Validar que se ha creado un registro en la tabla tdc_transaction de TPEUSER");
						
							String query = String.format(tdcQRYBank, folio);
							
							System.out.println(query);
							
							SQLResult Transaction = executeQuery(db, query);

							wmCodeDb = Transaction.getData(0, "WM_CODE");

				//			wmCodeDb = facUtil.getWmCodeQuery(query);
							
		//	**				boolean validationDb = folio.equals(folio);
			
							boolean validationDb = wmCodeQR01.equals(wmCodeDb);
						
							System.out.println(validationDb + " - wmCode db: ");
									
										
				assertTrue(validationDb , "wmCodeDb no es Igual a wmCodeQR01");
	
}
	

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_006_TPE_FAC_QRYBank";
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
