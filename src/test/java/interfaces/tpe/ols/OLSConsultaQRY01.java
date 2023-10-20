package interfaces.tpe.ols;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.TPE_OLS;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class OLSConsultaQRY01 extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_TPE_OLS_Consulta(HashMap<String, String> data) throws Exception {

		System.out.println("OLSConsultaQRY01");
		/*
		 * Utilerías
		 *********************************************************************/
		SQLUtil db = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE,
				GlobalVariables.DB_PASSWORD_FCTPE);
		TPE_OLS olsUtil = new TPE_OLS(data, testCase, db);

		/*
		 * Variables
		 *********************************************************************/
		String wmCodeToValidate = data.get("wmCode");

		String tdcTransactionQuery = "SELECT OPERATION,ENTITY,WM_CODE,FOLIO,PLAZA,TIENDA "
				+ "FROM TPEUSER.TPE_FR_TRANSACTION " + "WHERE CREATION_DATE >= (SYSDATE-1) AND APPLICATION = 'OLS'"
				+ " AND FOLIO = '%s'";

		String folio;
		String wmCodeRequest;
		String wmCodeDb;

		testCase.setProject_Name("Monitoreo de Servicios XPOS");

		/**************************************************************************************************
		 * Consulta QRY01
		 *************************************************************************************************/
		/*
		 * Paso 1
		 *****************************************************************************************/
		addStep("Ejecución de interfaz OLS servicio " + data.get("entity") + " con Referencia "
				+ data.get("service:idClient"));

		String respuesta = olsUtil.ejecutarQRY01();

		System.out.println("Respuesta: " + respuesta);		

		folio = RequestUtil.getFolioXml(respuesta);
		wmCodeRequest = RequestUtil.getWmCodeXml(respuesta);

		boolean respuestaQry01 = true;
		if (respuesta != null) {
			respuestaQry01 = false;
		}
		assertFalse(respuestaQry01, "no se genero el request");

		/*
		 * Paso 2
		 *****************************************************************************************/
		addStep("Verificar que el XML de respuesta tenga como wmCode: " + wmCodeToValidate);

		boolean validationRequest = wmCodeRequest.equals(wmCodeToValidate);
		
		System.out.println(validationRequest + " - wmCode request: " + wmCodeRequest);
		
		testCase.addTextEvidenceCurrentStep("\nRespuesta: \n" + respuesta);
		
		testCase.addTextEvidenceCurrentStep(
				"Código esperado: " + wmCodeToValidate + "\n" + "Código XML: " + wmCodeRequest);

		assertTrue(validationRequest, "El codigo wmCode no es el esperado: " + data.get("wmCode"));

		/*
		 * Paso 3
		 *****************************************************************************************/
		addStep("Verificación de  datos insertados en la tabla TPUSER.TPE_FR_TRANSACTION");

		String query = String.format(tdcTransactionQuery, folio);
		
		System.out.println(query);

		SQLResult result = executeQuery(db, query);
		
		wmCodeDb = result.getData(0, "WM_CODE");
		
		testCase.addQueryEvidenceCurrentStep(result);

		boolean validationDb = wmCodeDb.equals(wmCodeToValidate);

		System.out.println(validationDb + " - wmCode db: " + wmCodeDb);

		assertTrue(validationDb, "No se insertaron datos en la tabla TPUSER.TPE_FR_TRANSACTION");
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_002_TPE_OLS_Consulta";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. tpe.ols.ATC_FT_002_TPE_OLS_Consulta";
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
