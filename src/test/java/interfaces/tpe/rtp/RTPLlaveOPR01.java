package interfaces.tpe.rtp;


import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.TPE_RTP;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class RTPLlaveOPR01 extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_TPE_RTP_002_Solicitud_llaves(HashMap<String, String> data) throws Exception {
		
		/* Utilerías *********************************************************************/
		
		utils.sql.SQLUtil db= new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
	
		TPE_RTP rtpUtil = new TPE_RTP(data, testCase, db);
		
		/* Variables *********************************************************************/
		//
		
		String wmCodeToValidateTRN01 = "101";
			
		
		String tpeTransactionQuery = "select folio,wm_code,tienda,plaza, entity, application \r\n"
				+ "from TPEUSER.TPE_FR_TRANSACTION \r\n"
				+ "where  application = 'RTP' \r\n"
				+ "and ENTITY = 'KEY' \r\n"
				+ "and folio = '%s'";
		
		String tpeKeyQuery = "select folio,llave,tienda,plaza from RTPUSER.POS_RTP_KEY_LOG \r\n"
				+ "where  folio = %s";
		
		String folio;
		
		String wmCodeRequestTRN01;

		
		/* ***********************************************************************************************
		 * Folio TRN01
		 * ***********************************************************************************************/
		//Paso 1
		addStep("Ejecutar el servicio TPE.RPT.Pub:request para realizar la transacción de Solicitud de Llaves.");
		
			String respuestaTRN01 = rtpUtil.ejecutarOPR01Llave();
			
			System.out.println("Respuesta: " + respuestaTRN01);
			
			folio = RequestUtil.getFolioXml(respuestaTRN01);
			wmCodeRequestTRN01 = RequestUtil.getWmCodeXml(respuestaTRN01);
		
			
		//Paso 2
		addStep("Verificar el XML de la respuesta");
		
			testCase.addTextEvidenceCurrentStep(respuestaTRN01);
			
			boolean validationRequestTRN01 = wmCodeRequestTRN01.equals(wmCodeToValidateTRN01);
			System.out.println(validationRequestTRN01 + " - wmCode request: " + wmCodeRequestTRN01);
		
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
		addStep("Verificar datos insertados en TPUSER.POS_RTP_KEY_LOG");
		
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
		return "ATC_FT_TPE_RTP_002_Solicitud_llaves";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Validar la operación OPR01 Solicitud de Llaves para el proveedor correspondiente";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatizacion OXXO";
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
