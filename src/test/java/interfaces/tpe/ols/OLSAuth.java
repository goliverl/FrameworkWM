package interfaces.tpe.ols;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_OLS;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class OLSAuth extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_TPE_OLS_Auth(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/
		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCTPE,GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		TPE_OLS olsUtil = new TPE_OLS(data, testCase, db);
/**
 * Status: Terminado.
 * CP: Validar la operacion TRN01 Obtener Folio para la entidad SKY 
 * CP:Validar la operación TRN02 - entity SKY              
 * CP: Validar la operación TRN03- entity SKY  
 * Este script aplica para varias entity, solo se agrega en el dataprovider una iteracion por entity
 */
		
		/*
		 * Variables
		 *********************************************************************/

		String wmCodeToValidateTRN01 = data.get("wmCodeTRN01");
		String wmCodeToValidateTRN02 = data.get("wmCodeTRN02");
		String wmCodeToValidateTRN03 = data.get("wmCodeTRN03");

		String tpeTransactionQuery = "SELECT ENTITY,PLAZA,TIENDA,FOLIO,WM_CODE,CREATION_DATE "
				+ "FROM TPEUSER.TPE_FR_TRANSACTION WHERE APPLICATION = 'OLS' " 
				+" AND CREATION_DATE >= (SYSDATE-1) AND FOLIO = '%s'";

		String folio;

		String wmCodeRequestTRN01;
		String wmCodeRequestTRN02;
		String wmCodeRequestTRN03;

		String wmCodeDbTRN01;
		String wmCodeDbTRN02;
		String wmCodeDbTRN03;

		/**************************************************************************************************
		 * Solicitud de folio TRN01
		 *************************************************************************************************/
		/*
		 * Paso 1
		 *****************************************************************************************/
		addStep("Ejecutar el servicio TPE.TSF.Pub:request para realizar la operacion TRN01 y generar el folio.");

		String respuestaTRN01 = olsUtil.ejecutarTRN01();

		System.out.println("Respuesta: " + respuestaTRN01);

		folio = RequestUtil.getFolioXml(respuestaTRN01);

		wmCodeRequestTRN01 = RequestUtil.getWmCodeXml(respuestaTRN01);

		boolean respuesta = true;

		if (respuestaTRN01 != null) {
			respuesta = false;
		}
		assertFalse(respuesta);

		/*
		 * Paso 2
		 *****************************************************************************************/
		addStep("Verificar el estatus 100 del XML de respuesta.");

		boolean validationRequestTRN01 = wmCodeRequestTRN01.equals(wmCodeToValidateTRN01);

		System.out.println(validationRequestTRN01 + " - wmCode request: " + wmCodeRequestTRN01);

	    testCase.addTextEvidenceCurrentStep("Codigo esperado: " + wmCodeToValidateTRN01 + "\n" + "Codigo XML: " + wmCodeRequestTRN01);

		assertTrue(validationRequestTRN01);

		/*
		 * Paso 3
		 *****************************************************************************************/
		addStep("Verificar que se hayan insertado los datos en la tabla TPUSER.TPE_FR_TRANSACTION.");

		String query = String.format(tpeTransactionQuery, folio);

		System.out.println(query);
		SQLResult result = executeQuery(db, query);

		wmCodeDbTRN01 = result.getData(0, "WM_CODE");

		testCase.addQueryEvidenceCurrentStep(result);

		boolean validationDbTRN01 = wmCodeDbTRN01.equals(wmCodeToValidateTRN01);

		System.out.println(validationDbTRN01 + " - wmCode db: " + wmCodeDbTRN01);

		assertTrue(validationDbTRN01);

		/**************************************************************************************************
		 * Solicitud de autoriazación TRN02
		 *************************************************************************************************/
		/*
		 * Paso 4
		 *****************************************************************************************/
		addStep("Ejecutar el servicio TPE.OLS.Pub:request para realizar la operacion TRN02 y realizar el pago.");

		String respuestaTRN02 = olsUtil.ejecutarTRN02(folio);

		System.out.println("Respuesta: " + respuestaTRN02);

		wmCodeRequestTRN02 = RequestUtil.getWmCodeXml(respuestaTRN02);

		boolean respuesta2 = true;

		if (respuestaTRN02 != null) {
			respuesta2 = false;
		}
		assertFalse(respuesta2);

		/*
		 * Paso 5
		 *****************************************************************************************/
		addStep("Verificar el estatus 000 del XML de respuesta.");

		System.out.println(wmCodeRequestTRN02);

		System.out.println(wmCodeToValidateTRN02);

		boolean validationRequestTRN02 = wmCodeRequestTRN02.equals(wmCodeToValidateTRN02);

		System.out.println(validationRequestTRN02 + " - wmCode request: " + wmCodeRequestTRN02);

	
		testCase.addTextEvidenceCurrentStep(
				"Codigo esperado: " + wmCodeToValidateTRN02 + "\n" + "Código XML: " + wmCodeRequestTRN02);

		assertTrue(validationRequestTRN02);

		/*
		 * Paso 6
		 *****************************************************************************************/
		addStep("Verificar que se haya cambiado el estatus de WmCode en la tabla TPUSER.TPE_FR_TRANSACTION.");

		System.out.println(query);

		SQLResult result2 = executeQuery(db, query);

		wmCodeDbTRN02 = result2.getData(0, "WM_CODE");

		testCase.addQueryEvidenceCurrentStep(result2);

		boolean validationDbTRN02 = wmCodeDbTRN02.equals(wmCodeToValidateTRN02);

		System.out.println(validationDbTRN02 + " - wmCode db: " + wmCodeDbTRN02);

		assertTrue(validationDbTRN02);

		/**************************************************************************************************
		 * Solicitud de ack TRN03
		 *************************************************************************************************/
		/*
		 * Paso 7
		 *****************************************************************************************/
		addStep("Ejecutar el servicio TPE.OLS.Pub:request para realizar la operacion TRN03 y generar el ACK.");

		String respuestaTRN03 = olsUtil.ejecutarTRN03(folio);

		System.out.println("Respuesta: " + respuestaTRN03);

		wmCodeRequestTRN03 = RequestUtil.getWmCodeXml(respuestaTRN03);

		boolean respuesta3 = true;

		if (respuestaTRN03 != null) {
			respuesta3 = false;
		}
		assertFalse(respuesta3);

		/*
		 * Paso 8
		 *****************************************************************************************/
		addStep("Verificar el estatus 101 del XML de respuesta.");

		boolean validationRequestTRN03 = wmCodeRequestTRN03.equals(wmCodeToValidateTRN03);

		System.out.println(validationRequestTRN03 + " - wmCode request: " + wmCodeRequestTRN03);

		
		testCase.addTextEvidenceCurrentStep(
				"Codigo esperado: " + wmCodeToValidateTRN03 + "\n" + "Codigo XML: " + wmCodeRequestTRN03);

		assertTrue(validationRequestTRN03);

		/*
		 * Paso 9
		 *****************************************************************************************/
		addStep("Verificar que se haya cambiado el estatus de WmCode en la tabla TPUSER.TPE_FR_TRANSACTION.");

		System.out.println(query);

		SQLResult result3 = executeQuery(db, query);

		wmCodeDbTRN03 = result3.getData(0, "WM_CODE");

		testCase.addQueryEvidenceCurrentStep(result3);

		boolean validationDbTRN03 = wmCodeDbTRN03.equals(wmCodeToValidateTRN03);

		System.out.println(validationDbTRN03 + " - wmCode db: " + wmCodeDbTRN03);

		testCase.addTextEvidenceCurrentStep(validationDbTRN03 + " - wmCode db: " + wmCodeDbTRN03);

		assertTrue(validationDbTRN03);

	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_001_TPE_OLS_Auth";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar la operacion TRN01 (Obtener Folio para la entidad SKY), validar la operacion TRN02 - entity SKY, validar la operacion TRN03 - entity SKY";
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
