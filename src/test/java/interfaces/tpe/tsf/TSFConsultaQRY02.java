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

public class TSFConsultaQRY02 extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_TSF_ConsultaQRY02(HashMap<String, String> data) throws Exception {

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
		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
		
		/* PASO 1 *********************************************************************/
		
		addStep("Ejecutar la interfaz");

		System.out.println("TSFConsultaQRY02");
		String respuesta = tsfUtil.ejecutarQRY02();
		System.out.println("Respuesta: " + respuesta);

		folio = RequestUtil.getFolioXml(respuesta);
		wmCodeRequest = RequestUtil.getWmCodeXml(respuesta);

		
		
		/* PASO 2 *********************************************************************/
		
		addStep("Verificar el XML de la respuesta");

		boolean validationRequest = wmCodeRequest.equals(wmCodeToValidate);
		System.out.println(validationRequest + " - wmCode request: " + wmCodeRequest);

		testCase.addTextEvidenceCurrentStep(respuesta);
		
		assertTrue(validationRequest, "La respuesta no tiene el wmCode esperado.");
		
		

		/* PASO 3 *********************************************************************/
		
		addStep("Verificar datos insertados en TPUSER.TPE_FR_TRANSACTION");

		String query = String.format(tdcTransactionQuery, folio);
		System.out.println(query);

		SQLResult paso3 = executeQuery(db, query);
		
		boolean validationDb = paso3.isEmpty();
		
		if (!validationDb) {				
			testCase.addQueryEvidenceCurrentStep(paso3);
			
			wmCodeDb = paso3.getData(0, "WM_CODE");
			validationDb = wmCodeDb.equals(wmCodeToValidate);
			System.out.println("Codigo valido?: "+validationDb + " - wmCode db: " + wmCodeDb);
			validationDb = !validationDb;
		}	

		assertFalse(validationDb, "No se encontró un registro con el folio solicitado o WM_CODE incorrecto.");

	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_004_TSF_ConsultaQRY02";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Validar la operación QRY02 Consulta de beneficiarios de la entidad OXXO ";
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
