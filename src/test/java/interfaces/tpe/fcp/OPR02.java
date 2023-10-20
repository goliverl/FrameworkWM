package interfaces.tpe.fcp;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_FCP;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class OPR02 extends BaseExecution {
	
	/*
	 * 
	 * @cp SIN_Validar la operacion OPR02 de la entity SIN
	 * 
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_TPE_FCP_014_OPR02(HashMap<String, String> data) throws Exception {

		/* Utilerias *********************************************************************/
//		SqlUtil db01 = new SqlUtil(GlobalVariables.DB_USERPE3, GlobalVariables.DB_PASSWORDPE3,GlobalVariables.DB_HOSTPE3);
		SQLUtil db01 = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		SQLUtil db02 = new SQLUtil(GlobalVariables.DB_HOST_IAS, GlobalVariables.DB_USER_IAS, GlobalVariables.DB_PASSWORD_IAS);
		
		TPE_FCP fcpUtil = new TPE_FCP(data, testCase, db01);

		/* Variables *********************************************************************/
		String queryValidation01 = "SELECT OPERATION,FOLIO,CREATION_DATE,PLAZA,TIENDA,WM_CODE,WM_DESC "
				+ "FROM TPEUSER.TPE_FR_TRANSACTION "
				+ "WHERE APPLICATION = 'FCP' AND ENTITY = 'SIN' AND OPERATION = 'OPR02' AND FOLIO = %s";
		
		String queryValidation02 = "SELECT TRANSACTION_DATE,TICKET,CARD_ID,PROMOTION_ID,CREATED_DATE,FOLIO FROM XXPEMP.XXSE_TRANSACTION "
				+ "WHERE CR_PLAZA = '"+data.get("plaza")+"' AND CR_TIENDA = '"+data.get("tienda")
				+ "' AND EMISSION_ID = "+data.get("emissionId")+" AND BAR_CODE ="+data.get("barCode")
						+ " AND FOLIO = %s";


		String folio;
		String wmCodeRequestTRN01;
		String wmCodeDbTRN01, folioDbTRN01=null;
		String wmCodeToValidateTRN01 = "101";


		/* Pasos ************************************************************************************************/
		// Paso 1

		addStep("Ejecutar el servicio TPE.FCP.Pub:request para realizar la transaccion.");

		String respuestaTRN01 = fcpUtil.ejecutarOPR02();
		System.out.println("Respuesta:\n" + respuestaTRN01);
		folio = RequestUtil.getFolioXml(respuestaTRN01);
		wmCodeRequestTRN01 = RequestUtil.getWmCodeXml(respuestaTRN01);
		//addStep("Verificar que la interface retorna el XML de respuesta con el detalle de la transaccion.");

		boolean validationResponseTRN01 = wmCodeRequestTRN01.equals(wmCodeToValidateTRN01);
		System.out.println(validationResponseTRN01 + " - wmCode response: " + wmCodeRequestTRN01);
		testCase.addTextEvidenceCurrentStep("Folio: " + folio);
		testCase.addTextEvidenceCurrentStep("Wm_Code: " + wmCodeRequestTRN01);

		assertTrue(validationResponseTRN01);
		

		/// Paso 2
		addStep("Validar insercion de la transaccion en la tabla TPE_FR_TRANSACTION de TPEUSER.");

		String query01 = String.format(queryValidation01, folio);
		
		SQLResult result2 = executeQuery(db01, query01);
		System.out.println(query01);
		
		boolean validationDb01 = result2.isEmpty();
		 if (!validationDb01) { 
			 
				testCase.addQueryEvidenceCurrentStep(result2);

		 }
		 		 
		System.out.println(validationDb01);
		
		assertTrue(!validationDb01);

	

		// Paso 3
		addStep("Validar insercion de la transaccion en la tabla XXPEMP.XXSE_TRANSACTION de SINERGIA.");

		String query02 = String.format(queryValidation02,folio);	
		SQLResult result3 = executeQuery(db02, query02);
	
		boolean validationDb02 = result3.isEmpty();
		 if (!validationDb02) { 
			 
				testCase.addQueryEvidenceCurrentStep(result3);

		 }
		 		 
		System.out.println(validationDb02);
		
		assertTrue(!validationDb02);
	}


	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		return "Validar la operacion OPR02 de la entity SIN.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_TPE_FCP_014_OPR02";
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
