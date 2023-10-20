package interfaces.tpe.fcp;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.sql.ResultSet;
import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_FCP_MON;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class MON_Validar_Solicitud_de_Folio_para_Tarjeta_ME extends BaseExecution{
	
	/*
	 * 
	 * @cp1 MON_Validar el Cierre de Activacion
	 * @cp2 MON_Validar la Solicitud de Activacion
	 * @cp3 MON_Validar la Solicitud de Folio para Tarjeta Monedero Electronico
	 * 
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_TPE_FCP_012_MON_Validar_Solicitud_Folio_Tarjeta_M(HashMap<String, String> data) throws Exception {
		
		/* Utilerias *********************************************************************/
//		SqlUtil db = new SqlUtil(GlobalVariables.DB_USERPE3, GlobalVariables.DB_PASSWORDPE3, GlobalVariables.DB_HOSTPE3);
		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);

		TPE_FCP_MON rtpUtil = new TPE_FCP_MON(data, testCase, db);
		
		/* Variables *********************************************************************/
		
	
		
		String Query = "SELECT folio,tienda,plaza,creation_date,wm_code  FROM TPEUSER.TPE_FR_TRANSACTION WHERE APPLICATION ='FCP' AND ENTITY = 'MON' AND OPERATION = 'TRN01' and folio = %s";
		String QueryAut= "SELECT folio,tienda,plaza,creation_date,wm_code FROM TPEUSER.TPE_FR_TRANSACTION WHERE APPLICATION ='FCP' AND ENTITY = 'MON' AND OPERATION = 'TRN02' and folio = %s";
		String queryTarjeta = "SELECT CARD_NUMBER FROM TPEUSER.TPE_FCP_CARD  WHERE STATUS ='P' and rownum <= 1";
		String queryCierre ="SELECT folio,tienda,plaza,creation_date,wm_code FROM TPEUSER.TPE_FR_TRANSACTION WHERE APPLICATION ='FCP' AND ENTITY = 'MON' AND OPERATION = 'TRN03' and folio = %s";
		
		String folio;
		String plaza;
		String tienda;
		String creationDate;
		String wmCodeRequestTRN01;
		String wmCodeRequestTRN02;
		String wmCodeRequestTRN03;
		String wmCodeDbCard;
		
		String wmCodeToValidateTRN01 = "100";
		String wmCodeToValidateTRN02= "000";
			
		
//		String wmCodeDbTRN01;
//		String wmCodeDbTRN02;
//		String wmCodeDbTRN03;
		String wmCodeDbWM_CODE = "100";
		String wmCodeDbWM_CODE3 = "101";
	

		
		/* ***********************************************************************************************
		 * Folio TRN01
		 * ***********************************************************************************************/
		//Paso 1
		addStep("Ejecutar el servicio TPE.RPT.Pub:request para realizar la transaccion de Solicitud de Llaves.");
		
			String respuestaTRN01 = rtpUtil.solicitudFolioSaldazo();
			
			System.out.println("Respuesta: " + respuestaTRN01);
			
			folio = RequestUtil.getFolioXml(respuestaTRN01);
			creationDate =RequestUtil.getCreationDate(respuestaTRN01);
			wmCodeRequestTRN01 = RequestUtil.getWmCodeXml(respuestaTRN01);
		
		testCase.passStep();
		
		//Paso 2
		addStep("Verificar el XML de la respuesta");
		
			boolean validationRequestTRN01 = wmCodeRequestTRN01.equals(wmCodeToValidateTRN01);
			System.out.println(validationRequestTRN01 + " - wmCode request: " + wmCodeRequestTRN01);
		
		testCase.passStep();
		
		//Paso 3
		addStep("Verificar datos insertados en TPUSER.TPE_FR_TRANSACTION");
		
			String query = String.format(Query, folio);
			System.out.println(query);
			
//			wmCodeDbTRN01 = SQLUtil.getWmCodeQuery(testCase, db, query);
//			boolean validationDbTRN01 = wmCodeDbTRN01.equals(wmCodeDbWM_CODE);
//			System.out.println(validationDbTRN01 + " - wmCode db: " + wmCodeDbTRN01);
			
			SQLResult wmCodeDbTRN01 = executeQuery(db, query);
			boolean validationDbTRN01 = wmCodeDbTRN01.isEmpty();
			if (!validationDbTRN01) {
				testCase.addQueryEvidenceCurrentStep(wmCodeDbTRN01);
			}
			
		assertTrue(!validationDbTRN01);
//		testCase.passStep();
		
		//Paso 2.1
		addStep("Ejecutar el servicio TPE.RPT.Pub:request para realizar solicitud de activacion.");
		
		//Consulta de la tarjeta 
		
		
//			testCase.addQueryEvidenceCurrentStep(db, queryTarjeta);
				 
//		    ResultSet rs = db.executeQuery(queryTarjeta);
			SQLResult rs = executeQuery(db, queryTarjeta);
			
			testCase.addQueryEvidenceCurrentStep(rs);
//		    rs.next();
//		    wmCodeDbCard= rs.getString("CARD_NUMBER");
			wmCodeDbCard = rs.getData(0, "CARD_NUMBER");
		    
		    System.out.println("Tarjeta"+wmCodeDbCard);
			
			String respuestaTRN21 = rtpUtil.validarSolicitudActivacion(folio,creationDate,wmCodeDbCard);
			
			System.out.println("Respuesta: " + respuestaTRN21);
			
			wmCodeRequestTRN02 = RequestUtil.getWmCodeXml(respuestaTRN21);

	    testCase.passStep();
	    
	  //Paso 2.2
	    addStep("Verificar el XML de la respuesta solicitud autorizacion");
		
			boolean validationRequestTRN02 = wmCodeRequestTRN02.equals(wmCodeToValidateTRN02);
			System.out.println(validationRequestTRN02 + " - wmCode request: " + wmCodeRequestTRN02);
		
	    testCase.passStep();
	
	   //2.3
	    addStep("Verificar datos insertados en TPUSER.TPE_FR_TRANSACTION");
		
			String query02 = String.format(QueryAut, folio);
			System.out.println(query02);
			
//			wmCodeDbTRN02 = SQLUtil.getWmCodeQuery(testCase, db, query02);
			SQLResult result = executeQuery(db, query02);
					
//			boolean validationDbTRN02 = wmCodeDbTRN02.equals(wmCodeToValidateTRN02);
//			System.out.println(validationDbTRN02 + " - wmCode db: " + wmCodeDbTRN02);
			
			boolean validationDbTRN02 = result.isEmpty();
			if(!validationDbTRN02) {
				testCase.addQueryEvidenceCurrentStep(result);
			}
			
//			rs = db.executeQuery(query02);	    
//		    rs.next();
//		    plaza= rs.getString("plaza");
			plaza = result.getData(0, "plaza");
//		    tienda= rs.getString("tienda");
			tienda = result.getData(0, "tienda");
	
//	    testCase.passStep();
			assertFalse(validationDbTRN02);
	    
	    //2.4
		addStep("Verificar datos insertados en la tabla TPEUSER.TPE_FCP_CARD");
	    
	    String QueryCard= "SELECT CARD_NUMBER, tienda, plaza, status FROM TPEUSER.TPE_FCP_CARD WHERE PLAZA = '"+plaza+"'   AND TIENDA =  '"+tienda+"' AND CARD_NUMBER= '"+wmCodeDbCard+"' AND STATUS = 'A'";
	    
	    
	    	System.out.println(QueryCard);
//	    	rs = db.executeQuery( QueryCard);	    
//	    	rs.next();
	    	SQLResult queryCardResult = executeQuery(db, QueryCard);
//	    	String numCard= rs.getString("CARD_NUMBER");
	    	String numCard = queryCardResult.getData(0, "CARD_NUMBER");			

	
	    	validationDbTRN02 = wmCodeDbCard.equals(numCard);
	
//	 		System.out.println(validationDbTRN02 + " - wmCode db: " + wmCodeDbTRN02);
	    	System.out.println(validationDbTRN02 + " - wmCode db: " + result);
			
	    testCase.passStep();
	    
	  //Paso 3.1
	    addStep("Ejecutar el servicio TPE.RPT.Pub:request para el cierre de autorizacion.");
	  		
	  			String respuestaTRN03 = rtpUtil.cierreAutorizacion(folio,creationDate);
	  			
	  			System.out.println("Respuesta: " + respuestaTRN03);

	  			wmCodeRequestTRN03 = RequestUtil.getWmCodeXml(respuestaTRN03);
	  		
	  	testCase.passStep();
	  	
	  	//Paso 3.2
		
	  	addStep("Verificar el XML de la respuesta cierre de  autorizacion");
			
			boolean validationRequestTRN03 = wmCodeRequestTRN03.equals(wmCodeDbWM_CODE3);
			System.out.println(validationRequestTRN03 + " - wmCode request: " + wmCodeRequestTRN03);
		
	    testCase.passStep();
	    
	    //Paso 3.3
	    
		addStep("Verificar datos insertados en TPUSER.TPE_FR_TRANSACTION");
		
			String query04 = String.format(queryCierre, folio);
			System.out.println(query04);
			
//			wmCodeDbTRN03 = SQLUtil.getWmCodeQuery(testCase, db, query04);
//			boolean validationDbTRN03 = wmCodeDbWM_CODE3.equals(wmCodeDbTRN03);
//			System.out.println(validationDbTRN03 + " - wmCode db: " + wmCodeDbTRN03);
			
			SQLResult result3 = executeQuery(db, query04);
			boolean validationDbTRN03 = result3.isEmpty();
			if (!validationDbTRN03) {
				testCase.addQueryEvidenceCurrentStep(result3);
			}
		
			assertFalse(validationDbTRN03);

	    
	}
	

	
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_TPE_FCP_012_MON_Validar_Solicitud_Folio_Tarjeta_M";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar la generacion del folio unico para la transaccion de la Tarjeta Monedero Electronico/r/n"
				+ "Validar la transaccion de la Solicitud de Activacion/r/n"
				+ "Validar el Cierre de Activacion de la tarjeta del cliente";
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
