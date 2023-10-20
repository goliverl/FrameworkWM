package interfaces.pe2;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.PE2;

import static util.RequestUtil.getSimpleDataXml;

public class PE2Query extends BaseExecution{
	
	/*
	 * 
	 * @cp Query - Consulta de bandera
	 * 
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_010_PE2_Solicitud_Folio_Transaccion_Pago(HashMap<String, String> data) throws Exception {
		/* Utilerias *********************************************************************/
		PE2 pe2Util = new PE2(data, testCase, null);
		
		/* Variables *********************************************************************/
		
		String wmCodeToValidate = "101";
		String wmCodeRequest;
		
		/* Pasos *********************************************************************/
		//Paso 1
		testCase.nextStep("Ejecutar el servicio PE2.Pub:runGetQuery");
		
		    //Ejecuta el servicio PE2.Pub:runGetQuery	
			String respuesta = pe2Util.ejecutarRunGetQuery();
			//Se obtiene el wm_code del xml de respuesta y se agregan a la evidencia
			wmCodeRequest = getSimpleDataXml(respuesta, "wmCode");
		
		testCase.passStep();
		
		//Paso 2
		testCase.nextStep("Verificar la respuesta generada por el servicio.");
		    
			testCase.addTextEvidenceCurrentStep("Response: "+respuesta);//Se agrega la respuesta a la evidencia
			//Se valida que el wm_code del xml de respuesta sea igual a 101
			boolean validationRequest = wmCodeRequest.equals(wmCodeToValidate);
			System.out.println(validationRequest + "- wmCode to validate: "+ wmCodeRequest);
			testCase.addTextEvidenceCurrentStep("Wm_Code: " + wmCodeRequest);//Se agrega a la evidencia el wm_code

		testCase.validateStep(validationRequest);		
	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_010_PE2_Solicitud_Folio_Transaccion_Pago";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Consulta de bandera";
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
