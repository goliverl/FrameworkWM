package interfaces.tpeMun;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;


import om.TPE_MUN;
import om.TPE_OLS;

public class MUNConsultaQRY01 extends BaseExecution {
	 

	@Test(dataProvider = "data-provider")	
	public void ATC_FT_001_TPE_MUN_MUNConsultaQRY01(HashMap<String, String> data) throws Exception {
		
		/* Utilerías *********************************************************************/
//		SqlUtil db = new SqlUtil(GlobalVariables.DB_USERPE3, GlobalVariables.DB_PASSWORDPE3, GlobalVariables.DB_HOSTPE3);
		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE,GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
	
		TPE_MUN munUtil = new TPE_MUN(data, testCase, db);
	
		/* Variables *********************************************************************/
		
		String wmCodeToValidate = "101";
		String tdcTransactionQuery = "select APPLICATION,ENTITY,OPERATION,FOLIO,WM_CODE,WM_DESC from TPEUSER.TPE_FR_TRANSACTION "
				+ " where application = 'MUN' "
				+ " AND ENTITY = 'PRD'"
				+ " AND OPERATION = 'QRY01'"
				+ " AND FOLIO = '%s'";
		
		String expedienteQuery ="select expediente,IMPUESTO,RECARGOS,STATUS from TPEUSER.tpe_mun_predial_detalle " + 
				"WHERE EXPEDIENTE ="+ data.get("expediente");
		
		String folio;
		String wmCodeRequest;
		String wmCodeDb;
		String expediente;

		/* Pasos **********************Escenario de consulta ***********************************************/
		
		//Paso 1
		addStep("Ejecutar el servicio TPE.MUN.Pub:request para hacer la solicitud de Consulta de Predial.");
				
					String respuestaQ = munUtil.ejecutarConsulta();
//					
					wmCodeRequest = RequestUtil.getWmCodeXml(respuestaQ);
					
					folio = RequestUtil.getFolioXml(respuestaQ);
					
					expediente = RequestUtil.getFolioXml(respuestaQ);
							
		       
				
		//Paso 2-------------------------------------------------------------------------------------
		addStep("Se ejecuta la interface retornando el xml de salida con la información del Predial.");
				
					testCase.addTextEvidenceCurrentStep(respuestaQ);
				
					boolean validationRequestQ = wmCodeRequest.equals(wmCodeToValidate);
					
					System.out.println(validationRequestQ + "- wmCode to validate: "+ wmCodeToValidate + "\nwmCode request: " + wmCodeRequest);

					System.out.println(folio);
					
					assertTrue(validationRequestQ , "wmCodeRequest no es Igual a wmCodeToValidate");
		
		//Paso 3-------------------------------------------------------------------------------------	
		addStep("Se registra la transacción de la Consulta con el folio generado en la tabla TPE_FR_TRANSACTION de TPEUSER:");
		
				String queryQ = String.format(tdcTransactionQuery, folio);
		
				System.out.println(queryQ);
				
			    SQLResult Transaction = executeQuery(db, queryQ);

				wmCodeDb = Transaction.getData(0, "WM_CODE");
		
				boolean validationDb = wmCodeToValidate.equals(wmCodeDb);

						System.out.println(wmCodeToValidate + " es igual " + wmCodeRequest);
		
				System.out.println(validationDb + " - wmCode db: " + wmCodeRequest);
		
				assertTrue(validationDb , "wmCodeToValidate no es Igual a wmCodeDb");
		
		
		/* Pasos *********************************************************************/
		//Paso 1
		addStep("Ejecutar el servicio MUN.Pub:runGetQuery");
		
			String respuesta = munUtil.ejecutarQRY01();
//			
			wmCodeRequest = RequestUtil.getWmCodeXml(respuesta);
			
			folio = RequestUtil.getFolioXml(respuesta);
			
			expediente = RequestUtil.getFolioXml(respuesta);
					
			
		//Paso 2
			addStep("Verificar la respuesta generada por el servicio.");
		
			testCase.addTextEvidenceCurrentStep(respuesta);
		
			boolean validationRequest = wmCodeRequest.equals(wmCodeToValidate);
			
			System.out.println(validationRequest + "- wmCode to validate: "+ wmCodeToValidate + "\nwmCode request: " + wmCodeRequest);

			System.out.println(folio);
			
			assertTrue(validationDb , "wmCodeRequest no es Igual a wmCodeToValidate");
		
		//Paso 3
		
		
		addStep("Verificar datos insertados en TPUSER.TPE_FR_TRANSACTION");
		
			
			
			String queryE = String.format(expedienteQuery, expediente);
			
			System.out.println(queryE);
			
		    SQLResult Expediente = executeQuery(db, queryE);
		   
			
			//SQLUtil.getWmCodeQuery(testCase, db, query);
			
			boolean validation = Expediente.isEmpty();
			
			assertFalse(validation , "No se encontro Expediente");

		
		
		
		
	}
	

	

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. Validar el servicio de consulta de información de predial para el expediente";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_TPE_MUN_MUNConsultaQRY01 ";
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
