package interfaces.tpe.fcp;



import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import java.sql.ResultSet;
import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_FCP_FRQ;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class FRQ_Redencion_desde_Xml extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_TPE_FCP_011_Redencion_desde_Xml(HashMap<String, String> data) throws Exception {
		
		/* Utilerías *********************************************************************/
//		SqlUtil db = new SqlUtil(GlobalVariables.DB_USERPE3, GlobalVariables.DB_PASSWORDPE3, GlobalVariables.DB_HOSTPE3);
		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);

		TPE_FCP_FRQ rtpUtil = new TPE_FCP_FRQ(data, testCase, db);
		
		/* Variables *********************************************************************/
		
		String wmCodeToValidateTRN01 = "100";
			
		
		String genFolioQuery = "SELECT APPLICATION,ENTITY ,FOLIO,plaza,TIENDA  FROM TPEUSER.TPE_FR_TRANSACTION WHERE APPLICATION = 'FCP' AND ENTITY = 'FRQ' AND OPERATION = 'TRN01' and folio = %s";
		String tpeKeyQuery = "select folio,llave,tienda,plaza from RTPUSER.POS_RTP_KEY_LOG where  folio = %s";
		
		String folio;
		
		String wmCodeRequestTRN01;

		
//		String wmCodeDbTRN01;
		String wmCodeDbTRN02;

		
		/* ***********************************************************************************************
		 * Folio TRN01
		 * ***********************************************************************************************/
		//Paso 1
		addStep("Ejecutar el servicio TPE.RPT.Pub:request para realizar la transacción de Solicitud de Llaves.");
		
			String respuestaTRN01 = rtpUtil.generacionFolioTransaccion();
			
			System.out.println("Respuesta: " + respuestaTRN01);
			
			folio = RequestUtil.getFolioXml(respuestaTRN01);
			wmCodeRequestTRN01 = RequestUtil.getWmCodeXml(respuestaTRN01);
		
		testCase.passStep();
		
		//Paso 2
		addStep("Verificar el XML de la respuesta");
		
			boolean validationRequestTRN01 = wmCodeRequestTRN01.equals(wmCodeToValidateTRN01);
			System.out.println(validationRequestTRN01 + " - wmCode request: " + wmCodeRequestTRN01);
		
		testCase.passStep();
		assertTrue(validationRequestTRN01);
		
		//Paso 3
		addStep("Verificar datos insertados en TPUSER.TPE_FR_TRANSACTION");
		
			String query = String.format(genFolioQuery, folio);
			System.out.println(query);
			
//			wmCodeDbTRN01 = SQLUtil.getWmCodeQuery(testCase, db, query);
			
			SQLResult wmCodeDbTRN01 = executeQuery(db, query);
			
//			boolean validationDbTRN01 = wmCodeDbTRN01.equals(wmCodeToValidateTRN01);
			
			boolean validationDbTRN01 = wmCodeDbTRN01.isEmpty();
			
			if(!validationDbTRN01) {
				testCase.addQueryEvidenceCurrentStep(wmCodeDbTRN01);
			}
			
//			System.out.println(validationDbTRN01 + " - wmCode db: " + wmCodeDbTRN01);
			

			assertFalse(validationDbTRN01);
		
		// Paso 4
	/*	testCase.nextStep("Verificar datos insertados en TPUSER.POS_RTP_KEY_LOG");
		
		String queryKey = String.format(tpeKeyQuery, folio);
		System.out.println(queryKey);
		
		testCase.addQueryEvidenceCurrentStep(db, query);
		 
	    ResultSet rs = db.getResultSet(query);	    
	    rs.next();
	    wmCodeDbTRN02 = rs.getString("folio");
	    
		
//	wmCodeDbTRN02 = SQLUtil.getWmCodeQuery(testCase, db, queryKey);
		
		boolean validationDbTRN02= wmCodeDbTRN02.equals(folio);
		
		System.out.println(validationDbTRN02 + " - wmCode db: " + wmCodeDbTRN02);
		
	   testCase.passStep();*/
		
		
	}
	
	

	
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_TPE_FCP_011_Redencion_desde_Xml";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. RTP transaccion";
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
