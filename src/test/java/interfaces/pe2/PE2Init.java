package interfaces.pe2;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PE2;
import util.GlobalVariables;
import utils.sql.SQLResult;
import static util.RequestUtil.getSimpleDataXml;

public class PE2Init extends BaseExecution{
	
	/*
	 * 
		Init - Solicitud de Folio
		Init - Solicitud de Inicializacion
	 * 
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_007_PE2_Enviar_Solicitud_Pago_Transaccion(HashMap<String, String> data) throws Exception {
		
		/* Utilerias *********************************************************************/
		
//		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST,GlobalVariables.DB_USER, GlobalVariables.DB_PASSWORD);
		
		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA,GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		
		PE2 pe2Util = new PE2(data, testCase, db);
		
		/* Variables *********************************************************************/
		
		String wmCodeToValidate = "100";
		String tdcTransactionQuery = "SELECT folio, wm_code FROM tpeuser.tdc_transaction WHERE folio = %s";
		
		String folio;
		String wmCodeRequest;
		String wmCodeDb;
		
		/* Pasos *********************************************************************/
		//Paso 1
		testCase.nextStep("Llamar al servico PE2.Pub:runGetFolio");
		    //Ejecuta el servicio PE2.Pub:runGetFolio
			String respuesta = pe2Util.ejecutarRunGetFolio();
			//Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la evidencia
			folio = getSimpleDataXml(respuesta, "folio");
			wmCodeRequest = getSimpleDataXml(respuesta, "wmCode");
			
		testCase.passStep();
		
		//Paso 2
		testCase.nextStep("Verificar la respuesta generada por el servicio");
		
		    //Se valida que el wm_code del xml de respuesta sea igual a 100
			boolean validationRequest = wmCodeRequest.equals(wmCodeToValidate);
			System.out.println(validationRequest + " - wmCode request: " + wmCodeRequest);
			testCase.addTextEvidenceCurrentStep("Response: \n"+respuesta);//Se agrega response a evidencia
			testCase.addTextEvidenceCurrentStep("Folio: " + folio);//Se agrega a la evidencia el folio
	        testCase.addTextEvidenceCurrentStep("Wm_Code: " + wmCodeRequest);//Se agrega a la evidencia el wm_code
		
		testCase.validateStep(validationRequest);
		
		//Paso 3
		testCase.nextStep("Validar que se ha creado un registro del folio en la tabla tdc_transaction de TPEUSER");
		    
		    //Consulta a BD, se forma el query a utilizar
			String query = String.format(tdcTransactionQuery, folio);
			System.out.println(query);
			
			//Se realiza la consulta a la BD y se obtiene el wm_code
	//		wmCodeDb = pe2Util.getWmCodeQuery(query);
			
			SQLResult result1 = executeQuery(db, query);
			wmCodeDb = result1.getData(0, "WM_CODE");
			testCase.addQueryEvidenceCurrentStep(result1);
			
			//Se valida que sea igual a 100
			boolean validationDb = wmCodeDb.equals(wmCodeToValidate);		
			System.out.println(validationDb + " - wmCode db: " + wmCodeDb);
				
		testCase.validateStep(validationDb);
		
	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_007_PE2_Enviar_Solicitud_Pago_Transaccion";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Solicitud de Folio\r\n"
				+ "Solicitud de Inicializacion";
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
