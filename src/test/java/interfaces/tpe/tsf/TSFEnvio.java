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

public class TSFEnvio extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_006_TSF_Envio(HashMap<String, String> data) throws Exception {
		
/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		TPE_TSF tsfUtil = new TPE_TSF(data, testCase, db);
		
/** VARIABLES *********************************************************************/
		
		String wmCodeToValidateTRN01 = "100";
		String wmCodeToValidateTRN02 = "000";
		String wmCodeToValidateTRN03 = "101";
		String wmCodeToValidateTRN04 = "101";
		
		String tpeTransactionQuery = "SELECT APPLICATION,ENTITY ,FOLIO,plaza, WM_CODE, TIENDA FROM TPEUSER.TPE_FR_TRANSACTION" + 
				" WHERE APPLICATION = 'TSF'" + 
				" AND ENTITY = 'OXXO'" + 
				" AND OPERATION = '%s'" + 
				" AND FOLIO = '%s'";
		
		String tpeTransactionQueryTRN04 = "SELECT APPLICATION,ENTITY ,FOLIO,plaza, WM_CODE, TIENDA FROM TPEUSER.TPE_FR_TRANSACTION" + 
				" WHERE APPLICATION = 'TSF'" + 
				" AND ENTITY = 'OXXO'" + 
				" AND REVERSED = 2" + 
				" AND FOLIO = '%s'";
		
		String folio, wmCodeRequestTRN01, wmCodeRequestTRN02, wmCodeRequestTRN03, wmCodeRequestTRN04, wmCodeDbTRN01, wmCodeDbTRN02, wmCodeDbTRN03, wmCodeDbTRN04;
		
/** FOLIO TRN01 *********************************************************************/	
				
		/* PASO 1 *********************************************************************/
		System.out.println("****************** TRNO1 **********************");

		addStep("Ejecutar el servicio TPE.TSF.Pub:request para realizar la transacción de Solicitud de Folio para el envío.");

		String respuestaTRN01 = tsfUtil.ejecutarTRN01();
		String creationDate = RequestUtil.getCreationDate(respuestaTRN01);

		System.out.println("Respuesta: " + respuestaTRN01);

		folio = RequestUtil.getFolioXml(respuestaTRN01);
		wmCodeRequestTRN01 = RequestUtil.getWmCodeXml(respuestaTRN01);
		

		/* PASO 2 *********************************************************************/

		addStep("Se ejecuta la interface y se retorna el XML de  respuesta con el detalle de la ejecución y el folio generado.");

		boolean validationRequestTRN01 = wmCodeRequestTRN01.equals(wmCodeToValidateTRN01);
		System.out.println(validationRequestTRN01 + " - wmCode request: " + wmCodeRequestTRN01);

		testCase.addTextEvidenceCurrentStep(respuestaTRN01);

		assertTrue(validationRequestTRN01, "La respuesta no tiene el wmCode esperado.");
		

		/* PASO 3 *********************************************************************/

		addStep("Validar la inserción de la transacción en la tabla TPE_FR_TRANSACTION de TPEUSER.");

		String query = String.format(tpeTransactionQuery, "TRN01", folio);
		System.out.println(query);

		SQLResult paso3 = executeQuery(db, query);		
		boolean validationDb = paso3.isEmpty();
		
		if (!validationDb) {				
			wmCodeDbTRN01 = paso3.getData(0, "WM_CODE");
			testCase.addQueryEvidenceCurrentStep(paso3);

			validationDb = wmCodeDbTRN01.equals(wmCodeToValidateTRN01);
			System.out.println("Codigo valido?: "+ validationDb + " - wmCode db: " + wmCodeDbTRN01);			
			validationDb = !validationDb;
			System.out.println("folio");
		}	

		assertFalse(validationDb, "No se encontró un registro con el folio solicitado o WM_CODE incorrecto.");		
			
		
/** AUTH TRN02 *********************************************************************/	
		
		/* PASO 1 *********************************************************************/
		System.out.println("****************** TRNO2 **********************");

		addStep("Ejecutar el servicio TPE.TSF.Pub:request para realizar la transacción de Solicitud de Autorización de envío.");

		Thread.sleep(5000);
		String respuestaTRN02;
		
		if (data.get("destinatario_id2").isEmpty())			
			respuestaTRN02 = tsfUtil.ejecutarTRN02(folio, creationDate);		
		else			
			respuestaTRN02 = tsfUtil.ejecutarTRN02_1(folio, creationDate);

		System.out.println("Respuesta: " + respuestaTRN02);
		wmCodeRequestTRN02 = RequestUtil.getWmCodeXml(respuestaTRN02);		
		

		/* PASO 2 *********************************************************************/

		addStep("Se ejecuta la interface y se retorna el XML de  respuesta con el detalle de la ejecución de la transacción.");

		boolean validationRequestTRN02 = wmCodeRequestTRN02.equals(wmCodeToValidateTRN02);
		System.out.println(validationRequestTRN02 + " - wmCode request: " + wmCodeRequestTRN02);
		
		testCase.addTextEvidenceCurrentStep(respuestaTRN02);

		assertTrue(validationRequestTRN02, "La respuesta no tiene el wmCode esperado.");
		
		
		/* PASO 3 *********************************************************************/
		
		addStep("Validar la inserción de la transacción en la tabla TPE_FR_TRANSACTION de TPEUSER.");

		String query2 = String.format(tpeTransactionQuery, "TRN02", folio);
		System.out.println(query2);

		SQLResult paso3TRN02 = executeQuery(db, query2);
		validationDb = paso3TRN02.isEmpty();
		
		if (!validationDb) {				
			wmCodeDbTRN02 = paso3TRN02.getData(0, "WM_CODE");
			testCase.addQueryEvidenceCurrentStep(paso3TRN02);

			validationDb = wmCodeDbTRN02.equals(wmCodeToValidateTRN02);
			testCase.validateStep(validationDb, wmCodeDbTRN02);
			System.out.println("Codigo valido?: "+ validationDb + " - wmCode db: " + wmCodeDbTRN02);			
			validationDb = !validationDb;
		}	

		assertFalse(validationDb, "No se encontró un registro con el folio solicitado o WM_CODE incorrecto.");				
		

/** ACK TRN03 *********************************************************************/	
		
		/* PASO 1 *********************************************************************/
		System.out.println("****************** TRNO3 **********************");

		addStep("Ejecutar el servicio TPE.TSF.Pub:request para realizar el cierre de transacción de envío.");

		Thread.sleep(5000);
		String respuestaTRN03 = tsfUtil.ejecutarTRN03(folio, creationDate);
		
		System.out.println("Respuesta: " + respuestaTRN03);
		wmCodeRequestTRN03 = RequestUtil.getWmCodeXml(respuestaTRN03);
		

		/* PASO 2 *********************************************************************/
		
		addStep("Se ejecuta la interface y se retorna el XML de  respuesta con el detalle de la ejecución de la transacción.");

		boolean validationRequestTRN03 = wmCodeRequestTRN03.equals(wmCodeToValidateTRN03);
		System.out.println(validationRequestTRN03 + " - wmCode request: " + wmCodeRequestTRN03);
		
		testCase.addTextEvidenceCurrentStep(respuestaTRN03);

		assertTrue(validationRequestTRN03, "La respuesta no tiene el wmCode esperado.");
		

		/* PASO 3 *********************************************************************/
				
		addStep("Validar la inserción de la transacción en la tabla TPE_FR_TRANSACTION de TPEUSER.");

		String query3 = String.format(tpeTransactionQuery, "TRN03", folio);
		System.out.println(query3);

		SQLResult paso3TRN03 = executeQuery(db, query3);
		validationDb = paso3TRN03.isEmpty();
		
		if (!validationDb) {				
			wmCodeDbTRN03 = paso3TRN03.getData(0, "WM_CODE");
			testCase.addQueryEvidenceCurrentStep(paso3TRN03);

			validationDb = wmCodeDbTRN03.equals(wmCodeToValidateTRN03);
			testCase.validateStep(validationDb);
			System.out.println("Codigo valido?: "+ validationDb + " - wmCode db: " + wmCodeDbTRN03);			
			validationDb = !validationDb;
		}	

		assertFalse(validationDb, "No se encontró un registro con el folio solicitado o WM_CODE incorrecto.");	
		
		
/** AUTH TRN04 *********************************************************************/	
		
		/* PASO 1 *********************************************************************/
		System.out.println("****************** TRNO4 **********************");

		addStep("Ejecutar el servicio TPE.TSF.Pub:request para realizar el cierre de transacción de envío.");

		Thread.sleep(5000);
		String respuestaTRN04 = tsfUtil.ejecutarTRN04(folio, creationDate);
		
		System.out.println("Respuesta: " + respuestaTRN04);
		wmCodeRequestTRN04 = RequestUtil.getWmCodeXml(respuestaTRN04);
		

		/* PASO 2 *********************************************************************/
		
		addStep("Se ejecuta la interface y se retorna el XML de  respuesta con el detalle de la ejecución de la transacción.");

		boolean validationRequestTRN04 = wmCodeRequestTRN04.equals(wmCodeToValidateTRN04);
		System.out.println(validationRequestTRN04 + " - wmCode request: " + wmCodeRequestTRN04);
		
		testCase.addTextEvidenceCurrentStep(respuestaTRN04);

		assertTrue(validationRequestTRN04, "La respuesta no tiene el wmCode esperado.");
		

		/* PASO 3 *********************************************************************/
				
		addStep("Validar la inserción de la transacción en la tabla TPE_FR_TRANSACTION de TPEUSER.");

		String query4 = String.format(tpeTransactionQueryTRN04, folio);
		System.out.println(query4);

		SQLResult paso3TRN04 = executeQuery(db, query4);
		validationDb = paso3TRN04.isEmpty();
		
		if (!validationDb) {				
			testCase.addQueryEvidenceCurrentStep(paso3TRN04);
		}	

		assertFalse(validationDb, "No se encontró un registro con el folio solicitado.");	
		
	}	
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_006_TSF_Envio";
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

