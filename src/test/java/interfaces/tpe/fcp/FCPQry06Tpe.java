package interfaces.tpe.fcp;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import modelo.BaseExecution;
import om.TPE_FCP;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.webmethods.ReadRequest;

public class FCPQry06Tpe extends BaseExecution {
	
	/*
	 * 
	 * @cp TPE_Realizar la actualizacion de  los datos frecuentes de clientes
	 * 
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_TPE_FCP_004_Qry06Tpe(HashMap<String, String> data) throws Exception {
		
		//Nota:  Pruebas pendientes  por custiones de insumos en la primera consulta 
		
		

		/*
		 * Utilerias
		 ****************************************************************************/
		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE,
				GlobalVariables.DB_PASSWORD_FCTPE);
		TPE_FCP fcpUtil = new TPE_FCP(data, testCase, db);

		/*
		 * Variables
		 *********************************************************************/
		String queryValidation01 = "SELECT D.CODE, D.VALUE1, D.VALUE2, D.DESCRIPTION, D.ACCOUNT_ID "
				+ "FROM FCPUSER.TPE_FCP_ACCESS A, FCPUSER.TPE_FCP_ACCOUNT C, FCPUSER.TPE_FCP_FREQ_DATA D "
				+ "WHERE A.ACCOUNT_ID = C.ACCOUNT_ID " + "AND C.ACCOUNT_ID = D.ACCOUNT_ID " + "AND A.STATUS = 'A' "
				+ "AND C.STATUS = 'A' " + "AND D.APPLICATION = '" + data.get("application") + "' " + "AND D.ENTITY = '"
				+ data.get("entity") + "' " + "AND D.CATEGORY = 'TPE_LIST' " + "AND D.STATUS = 'A' "
				+ "AND A.ACCESS_ID = '" + data.get("cardId") + "'";

		String queryValidation02 = "SELECT OPERATION,FOLIO,CREATION_DATE,WM_CODE,WM_DESC"
				+ " FROM TPEUSER.TPE_FR_TRANSACTION WHERE APPLICATION = '" + data.get("application") + "' "
				+ "AND ENTITY = '" + data.get("entity") + "' AND OPERATION = '" + data.get("operation")
				+ "' AND FOLIO = %s";
		
		String validaFolio = "SELECT folio, wm_code FROM TPEUSER.TPE_FR_TRANSACTION WHERE folio = '%s'"
		           + "AND creation_date>= (sysdate-1) ";

		String folio;
		String wmCodeRequestQRY01, wmCodeDbQRY01;
		String wmCodeToValidateDbQRY01 = "100";

		/*
		 * Pasos
		 ************************************************************************************************/
		// Paso 1

		addStep("Validar que exista informacion de los datos frecuentes para el cardId. ");

		
		System.out.println(queryValidation01);
		SQLResult result_queryValidation01 = executeQuery(db, queryValidation01);

		boolean valida_queryValidation01 = result_queryValidation01.isEmpty();

		if (!valida_queryValidation01) {
			testCase.addQueryEvidenceCurrentStep(result_queryValidation01);
		}
		assertFalse("No se muestran registros en TPE_FR_TRANSACTION ", valida_queryValidation01);
		System.out.println(valida_queryValidation01);

		// Paso 2
		addStep("Ejecutar el servicio TPE.FCP.Pub:request para realizar la transaccion.");

		String respuestaQRY01 = fcpUtil.generacionFolioTransaccionCliente();
		System.out.println("Respuesta:\n" + respuestaQRY01);
		folio = RequestUtil.getFolioXml(respuestaQRY01);
		wmCodeRequestQRY01 = RequestUtil.getSimpleDataXml(respuestaQRY01, "wmcode");

		boolean validationResponseFolio = true;

		if (respuestaQRY01 != null) {

			validationResponseFolio = false;

		}
		assertFalse(validationResponseFolio);

		/*
		 * String respuestaTRN01 = fcpUtil.ejecutarTpeQRY01();
		 * System.out.println("Respuesta:\n" + respuestaTRN01); folio =
		 * RequestUtil.getFolioXml(respuestaTRN01); Document
		 * runGetFolioRequestDoc =
		 * ReadRequest.convertStringToXMLDocument(respuestaTRN01); Element
		 * eElement = (Element)
		 * runGetFolioRequestDoc.getElementsByTagName("wmcode").item(0);
		 * wmCodeRequestTRN01 = eElement.getAttribute("value");
		 * 
		 */

		// Paso 3
		addStep("Verificar que la interface retorna el XML de respuesta con el detalle de la transaccion.");

		// Se manda el folio a la consulta
		String format_validaFolio = String.format(validaFolio, folio);
		System.out.println(format_validaFolio);
		SQLResult result_validaFolio = executeQuery(db, format_validaFolio);

		// Se valida que el wm_code del xml de respuesta sea igual a 100
		boolean validationRequest = wmCodeRequestQRY01.equals( wmCodeToValidateDbQRY01);
		System.out.println(validationRequest + " - wmCode request: " + wmCodeRequestQRY01);
		testCase.addTextEvidenceCurrentStep("Response: \n" + respuestaQRY01);// Se agrega el response a la evidencia
																				
		// Se extrae el wm_code de la base de datos
		wmCodeDbQRY01 = result_validaFolio.getData(0, "WM_CODE");
		testCase.addQueryEvidenceCurrentStep(result_validaFolio); // Se imprime la consulta en la evidencia
																	
		// Se valida que el wm_code de la base de datos sea igual a 100
		boolean validationDb = wmCodeDbQRY01.equals(wmCodeToValidateDbQRY01);
		System.out.println(validationDb + " - wmCode db: " + wmCodeDbQRY01);

		boolean validaCodigos = false;

		if (validationRequest == validationDb) {

			validaCodigos = true;
		}

		assertTrue(validaCodigos);

		// Paso 4

		addStep("Se registra la transaccion de consulta en la tabla TPE_FR_TRANSACTION de TPEUSER.");

		
		String format_queryValidation02  = String.format(queryValidation02 , folio);
	    System.out.println("Consultar folio en TPE_FR_TRANSACTION: "+ queryValidation02 );
	    
		SQLResult result_queryValidation02  = executeQuery(db,format_queryValidation02  );	
		 
		 boolean valida_queryValidation02  = result_queryValidation02 .isEmpty();
			
		 if (!valida_queryValidation02 ) {
				testCase.addQueryEvidenceCurrentStep(result_queryValidation02 );
			}
			assertFalse("No se muestran registros en TPE_FR_TRANSACTION ",  valida_queryValidation02  );
			System.out.println( valida_queryValidation02  );

	}

	@Override
	public void beforeTest() {
	}

	@Override
	public String setTestDescription() {
		return "Realizar la actualizacion de los datos frecuentes de clientes";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_TPE_FCP_004_Qry06Tpe";
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
