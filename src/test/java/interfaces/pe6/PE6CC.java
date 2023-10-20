package interfaces.pe6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.lang.Override;
import java.lang.String;
import java.util.HashMap;
import modelo.BaseExecution;
import om.PE6;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import org.testng.annotations.Test;

public class PE6CC extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PE6_Solicitud_Caso_Critico(HashMap<String, String> data) throws Exception {
		
		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST, GlobalVariables.DB_USER, GlobalVariables.DB_PASSWORD);
		PE6 pe6Obj = new PE6(data, testCase, db);
				
		// Valores WMcode esperados:
		final String expectedWMCodeGetCC = "101";
		final String expectedWMCodeGetCCDB = "198";
		final String expectedCriticCase = "Y";

		// Variables

		String query = "SELECT folio, wm_code, critic_case FROM tpeuser.tdc_transaction where folio = %s";
		String folio;

		// Paso 1 ************************************

		System.out.println("Paso 1");
		addStep("Llamar al servico PE6.Pub:runGetCC");

		String responseRunGetCC = pe6Obj.runGetCC_request();

		// Añadir evidencia de la respuesta al testCase

		testCase.addTextEvidenceCurrentStep(responseRunGetCC);

		testCase.passStep(); 

		// Paso 2 **************************************

		System.out.println("Paso 2");
		addStep("Verificar la respuesta generada por el servicio");

		// imprime el response
		System.out.println(responseRunGetCC);

		testCase.addTextEvidenceCurrentStep(responseRunGetCC);
		System.out.println("wmCode: " + pe6Obj.getWmCode());

		assertEquals(expectedWMCodeGetCC, pe6Obj.getWmCode());

		testCase.passStep(); 

		// Paso 3 **************************************

		addStep("cambio de consulta ");

		System.out.println("Paso 3");
		String queryLog = String.format(query, data.get("folio"));
		System.out.println(queryLog);
		SQLResult executeQueryLog = db.executeQuery(queryLog);
		boolean validationDb = executeQueryLog.isEmpty();
		System.out.println(validationDb);

		assertFalse(validationDb); 

		////// revisar porque la consulta no muestra el registro con el folio

	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_003_PE6_Solicitud_Caso_Critico";
	}

	@Override
	public String setTestDescription() {
		return "Terminado. Determinar fallas que lleven a ser casos considerados como críticos se informen a la capa de integración de OXXO";
	}

	@Override
	public String setTestDesigner() {
		return "Equipo de Automatización.";
	}

	@Override
	public String setTestInstanceID() {
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