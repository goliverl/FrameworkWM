package interfaces.tpe.btc;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_BTC;
import om.TPE_TSF;
import util.GlobalVariables;
import util.RequestUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

import utils.sql.SQLUtil;
import utils.sql.SQLResult;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class BTC_insertarTransaccionQRY01 extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_TPE_BTC_Insertar_Transaccion_QRY01(HashMap<String, String> data) throws Exception {
		
/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		TPE_BTC btcUtil = new TPE_BTC(data, testCase, db);		
		
/** VARIABLES *********************************************************************/	
		
		String wmCodeToValidate =  data.get("wmCode");
		
		String tdcTransactionQry01 = "SELECT folio, wm_code, creation_date, wm_desc FROM TPEUSER.TPE_FR_TRANSACTION" + 
				" WHERE APPLICATION = 'TSF'" + 
				" AND ENTITY = '" + data.get("entity") + "'" + 
				" AND OPERATION = 'QRY01'" + 
				" AND FOLIO = '%s'";
				
		String tdcSelectTicket = "SELECT * FROM TPEUSER.POS_TRANSACTION WHERE TICKET= '%s'";

		String tdcSelectRefNo = "SELECT application, entity, operation, folio, ret_ref_no, wm_code FROM TPEUSER.TPE_FR_TRANSACTION WHERE APPLICATION = 'TSF' AND ENTITY = 'WU' "
				+ " and amount " + data.get("signo") + " %s and ATT1 = 'Western Union' and rownum <= 1 order by creation_date desc";
		
		String tdcVerifyError = "SELECT Error_ID, Folio, Error_Code, Error_Date, Description, Message FROM WMLOG.WM_LOG_ERROR_TPE WHERE Folio = '%s'";
		
		String folio, wmCodeRequest, wmCodeDb, umbral="", ref_no="";
				
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
	
		/* PASO 1 *********************************************************************/
		System.out.println("****************** QRY01 **********************");
		
		addStep("Ejecutar consulta XML QRY01");
		
		System.out.println("TSFConsultaQRY01");		
		String respuesta = btcUtil.ejecutarQRY01();				
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

		addStep("Verificar datos insertados en TPUSER.POS_TRANSACTION");		

		String query = String.format(tdcSelectTicket, folio);
		System.out.println(query);		
	    Thread.sleep(8000);
		SQLResult paso3 = executeQuery(db, query);		
				
		boolean validationDb3 = paso3.isEmpty();
		
		System.out.println(validationDb3);
		
		if (!validationDb3) {				
			testCase.addQueryEvidenceCurrentStep(paso3);
			
			wmCodeDb = paso3.getData(0, "WM_CODE");
			validationDb3 = wmCodeDb.equals(wmCodeToValidate);
			System.out.println("Codigo valido?: "+validationDb3 + " - wmCode db: " + wmCodeDb);
			validationDb3 = !validationDb3;
		}	

		assertFalse(validationDb3, "No se encontró un registro con el ticket solicitado o WM_CODE incorrecto.");

	}	
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_001_TPE_BTC_Insertar_Transaccion_QRY01";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Realizar la ejecucion con la operación QRY01 para insertar una transaccion exitosa";
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

