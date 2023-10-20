package interfaces.tpe.rtp;



import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.sql.ResultSet;
import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_RTP;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;

public class RTPRegistroDeTransacciones extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_TPE_RTP_001_Registro_transacciones(HashMap<String, String> data) throws Exception {
		
		/* Utilerías *********************************************************************/
		utils.sql.SQLUtil db= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		
		TPE_RTP rtpUtil = new TPE_RTP(data, testCase, db);
		
		/* Variables *********************************************************************/
		
		String wmCodeToValidateTRN01 = "101";
		
		
		String tpeTransactionQuery = "select folio,wm_code,tienda,plaza, entity, application from TPEUSER.TPE_FR_TRANSACTION \r\n"
				+ "where application = 'RTP' \r\n "
				+ "and ENTITY = 'DTL' \r\n"
				+ "and folio = '%s'";
		String tpeKeyQuery = "select folio,plaza,tienda,wm_status from \r\n"
				+ "RTPUSER.POS_RTP_TRANS \r\n "
				+ "where wm_status = 'INSERTADO' \r\n "
				+ "and folio = %s";
		
		
		String folio;
		
		String wmCodeRequestTRN01;

		/* ***********************************************************************************************
		 * Folio TRN01
		 * ***********************************************************************************************/
		//Paso 1
		addStep("Ejecutar el servicio TPE.RPT.Pub:request para realizar la transacción de registro de Transacciones.");
		
			String respuestaTRN01 = rtpUtil.ejecutarOPR0RegistroDTL();
			
			System.out.println("Respuesta: " + respuestaTRN01);
			
			folio = RequestUtil.getFolioXml(respuestaTRN01);
			wmCodeRequestTRN01 = RequestUtil.getWmCodeXml(respuestaTRN01);
		
		
		
		//Paso 2
		 addStep("Verificar el XML de la respuesta");
		
			boolean validationRequestTRN01 = wmCodeRequestTRN01.equals(wmCodeToValidateTRN01);
			System.out.println(validationRequestTRN01 + " - wmCode request: " + wmCodeRequestTRN01);
			
			testCase.addTextEvidenceCurrentStep(respuestaTRN01);
			
			assertTrue(validationRequestTRN01,"El request no tiene el wm_code esperado ");
		
		//Paso 3
		 addStep("Verificar datos insertados en TPUSER.TPE_FR_TRANSACTION");
		
		 String query = String.format(tpeTransactionQuery, folio);
			System.out.println(query);
			
			
			SQLResult queryResult = db.executeQuery(query); 
		                
	        boolean validaQuery = queryResult.isEmpty();
		       
		       if (!validaQuery) {
		       testCase.addQueryEvidenceCurrentStep(queryResult);     
		       
		       }
		       
		       System.out.println(validaQuery);
			
					
			assertFalse(validaQuery, "No se encontro un registro con el folio solicitado");	 
	
		
		// Paso 4
		addStep("Verificar datos insertados en  RTPUSER.POS_RTP_TRANS");
		
		System.out.println("Folio"+folio);
		
		
		String queryKey = String.format(tpeKeyQuery, folio);
		System.out.println(queryKey);
		
		SQLResult queryKeyResult = db.executeQuery(queryKey); 
	       
   
        boolean validaQueryKey = queryKeyResult.isEmpty();
	       
	       if (!validaQueryKey) {
	       testCase.addQueryEvidenceCurrentStep(queryKeyResult);     
	       
	       }
	       
	       System.out.println(validaQueryKey);
	       assertFalse(validaQueryKey, "No se encontro un registro con el folio solicitado");
		
		
		
	}
	
	

	
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_TPE_RTP_001_Registro_transacciones";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Validar la operación OPR01 Registro de Transacciones";
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