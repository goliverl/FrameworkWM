package interfaces.tpe.tsf;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_TSF;
import util.GlobalVariables;
import util.RequestUtil;

import utils.sql.SQLUtil;
import utils.sql.SQLResult;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class prueba extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_prueba(HashMap<String, String> data) throws Exception {
		
/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		TPE_TSF tsfUtil = new TPE_TSF(data, testCase, db);
		
/** VARIABLES *********************************************************************/
		
		String wmCodeToValidate = "101";
		
		String tdcTransactionQuery = "SELECT folio, wm_code, creation_date FROM TPEUSER.TPE_FR_TRANSACTION" + 
				" WHERE APPLICATION = 'TSF'" + 
				" AND ENTITY = 'OXXO'" + 
				" AND OPERATION = 'QRY02'" + 
				" AND FOLIO = '%s'";
		
		String folio, wmCodeRequest, wmCodeDb;
		
/** FOLIO TRN01 *********************************************************************/	
				
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
		
//		/* PASO 1 *********************************************************************/
//		
//		addStep("Ejecutar la interfaz");
//
//		System.out.println("TSFConsultaQRY02");
//		String respuesta = tsfUtil.prueba();
//		System.out.println("Respuesta: " + respuesta);
//
//	//	folio = RequestUtil.getFolioXml(respuesta);
//		wmCodeRequest = RequestUtil.getpvDocId(respuesta);
//	
//		

		
	}	
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_001_prueba";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Validar la operación TRN03 Cierre de Transacción de envío de la entidad OXXO ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
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

