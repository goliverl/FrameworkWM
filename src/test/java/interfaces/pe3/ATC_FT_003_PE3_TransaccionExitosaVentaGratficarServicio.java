package interfaces.pe3;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.testng.annotations.Test;
import utils.ApiMethodsUtil;
import io.restassured.response.Response;
import modelo.BaseExecution;
import om.PE3;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

/**
 * Proyecto: Actualizacion tecnologica Webmethods
 * CP: MTC-FT-011 PE3 Transaccion exitosa de venta giftcar de servicio
 *Prueba de regresión  para comprobar la no afectación en la funcionalidad 
 *principal de transaccionalidad de la interface FEMSA_PE3 al ser migrada de WM9.9 a WM10.5*
 * Modificado para mantenimiento.
 *
 * */

public class ATC_FT_003_PE3_TransaccionExitosaVentaGratficarServicio extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PE3_TransaccionExitosaVentaGratficarServicio_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/
		SQLUtil dbFCTAEQA_QRO = new SQLUtil(GlobalVariables.DB_HOST_FCTAEQA_QRO, GlobalVariables.DB_USER_FCTAEQA_QRO,GlobalVariables.DB_PASSWORD_FCTAEQA_QRO);
		SQLUtil dbFCWMLTAEQA_MTY = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAQ_MTY,GlobalVariables.DB_USER_FCWMLTAQ_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAQ_MTY);
		
		PE3 pe3Util = new PE3(data, testCase,dbFCTAEQA_QRO);

		/*
		 * Variables
		 *********************************************************************/

		
		String tdcTransactionQuery = "SELECT FOLIO, CREATION_DATE, PLAZA, TIENDA, CAJA, UPC, CARD_NO\r\n"
				+ "FROM TPEUSER.GIF_TRANSACTION GIF\r\n"
				+ "WHERE  FOLIO= '%s'";
		
		String queryErrorRecords = "SELECT ERROR_ID, ERROR_DATE, ERROR_CODE, DESCRIPTION, MESSAGE\r\n"
				+ "FROM WMLOG.WM_LOG_ERROR_TPE\r\n"
				+ "WHERE ERROR_DATE >= TO_DATE('%s', 'DD/MM/YYYY HH24:MI:SS')\r\n"
				+ "AND TPE_TYPE LIKE '%s'\r\n";
		

		String folio = null;
		testCase.setTest_Description(data.get("id")+data.get("Descripcion"));
		String baseUri = String.format(data.get("baseUri"), data.get("host"), data.get("interface"));
		
		/**************************************************************************************************
		 * Pasos
		 *************************************************************************************************/
		
		//********************************* Paso 1 ******************************************************
addStep("Ejecutar en un navegador directamente el servicio rungGetFolio  de solicitud de folio de la PE3");
		
            String getFolioParams = pe3Util.runGetFolioParams();

            String uriFolio = baseUri + data.get("getFolio") + getFolioParams;
            System.out.println(uriFolio);
            ApiMethodsUtil api = new ApiMethodsUtil(uriFolio);
            Response requestFolio = api.getRequestMethod(uriFolio);

            String wmCodeFolio = "";
            if (requestFolio.getStatusCode() == Integer.parseInt(data.get("statusCode"))) {
	        String bodyFolio = requestFolio.getBody().asPrettyString();
	        System.out.println(bodyFolio);
	        folio = getSimpleDataXml(bodyFolio, "folio");
	        wmCodeFolio = getSimpleDataXml(bodyFolio, "wmCode");
	        //Agregar Request a la evidencia
	        testCase.addBoldTextEvidenceCurrentStep("Request:");
	        testCase.addBoldTextEvidenceCurrentStep("Metodo: GET");
	        testCase.addTextEvidenceCurrentStep(uriFolio);
	        //Agregar response a la evidencia
	        testCase.addBoldTextEvidenceCurrentStep("Response:");
	        testCase.addBoldTextEvidenceCurrentStep("Status: " + requestFolio.getStatusLine());
	        testCase.addTextEvidenceCurrentStep(bodyFolio);
	        testCase.addTextEvidenceCurrentStep("wmCode obtenido: " + wmCodeFolio);
	        testCase.addTextEvidenceCurrentStep("wmCode esperado: " + data.get("folioWmCode"));
	
	         assertEquals(wmCodeFolio, data.get("folioWmCode"), "Se obtiene wmCode diferente al esperado");
            }else {
	         assertEquals(requestFolio.getStatusCode(), Integer.parseInt(data.get("statusCode")), 
			"Se obtiene status diferente a 200");
             }
            

		//******************************** Paso 2 *******************************************************/
		addStep("Ejecutar en el navegador el servicio runGetAuth  para realizar la solicitud de autorizacion de PE3 para la activacion de GiftCard de servicio mediante la siguiente url:");
        
		String getAuthParams = pe3Util.runGetActivacionParams(folio);
		String uriAuth = baseUri + data.get("getAuth") + getAuthParams;
		System.out.println(uriAuth);
		Response requestAuth = api.getRequestMethod(uriAuth);
		
		String wmCodeAuth = "";
		if (requestAuth.getStatusCode() == Integer.parseInt(data.get("statusCode"))) {
			String bodyAuth = requestAuth.getBody().asString();
			System.out.println(bodyAuth);
			wmCodeAuth = getSimpleDataXml(bodyAuth, "wmCode");
			//Agregar Request a la evidencia
			testCase.addBoldTextEvidenceCurrentStep("Request:");
			testCase.addBoldTextEvidenceCurrentStep("Metodo: GET");
			testCase.addTextEvidenceCurrentStep(uriAuth);
			//Agregar response a la evidencia
			testCase.addBoldTextEvidenceCurrentStep("Response:");
			testCase.addBoldTextEvidenceCurrentStep("Status: " + requestAuth.getStatusLine());
			testCase.addTextEvidenceCurrentStep(bodyAuth);
			
			//imprime codigo wm_code
			testCase.addTextEvidenceCurrentStep("wmCode obtenido: " + wmCodeAuth);
			testCase.addTextEvidenceCurrentStep("wmCode esperado: " + data.get("authWmCode"));
			
			assertEquals(wmCodeAuth, data.get("authWmCode"), "Paso 2: Se obtiene wmCode diferente al esperado");
		}else {
			assertEquals(requestAuth.getStatusCode(), Integer.parseInt(data.get("statusCode")), 
					"Se obtiene status diferente a 200");
		}
	
		//******************************** Paso 3 ******************************************************/
		addStep("Ejecutar el servicio  RunGetAck de la PE3  para confirmar el ACK de la transaccion activación de tarjeta GifCtard , mediante la siguiente url");

		String getAckParams = pe3Util.runGetAckParams(folio);
		String uriAck = baseUri + data.get("getAck") + getAckParams;
		System.out.println(uriAck);
		Response requestAck = api.getRequestMethod(uriAck);
		
		String wmCodeAck = "";
		if (requestAck.getStatusCode() == Integer.parseInt(data.get("statusCode"))) {
			String bodyAck = requestAck.getBody().asString();
			System.out.println(bodyAck);
			wmCodeAck = getSimpleDataXml(bodyAck, "wmCode");
			//Agregar Request a la evidencia
			testCase.addBoldTextEvidenceCurrentStep("Request:");
			testCase.addBoldTextEvidenceCurrentStep("Metodo: GET");
			testCase.addTextEvidenceCurrentStep(uriAck);
			//Agregar response a la evidencia
			testCase.addBoldTextEvidenceCurrentStep("Response:");
			testCase.addBoldTextEvidenceCurrentStep("Status: " + requestAck.getStatusLine());
			testCase.addTextEvidenceCurrentStep(bodyAck);
			
			testCase.addTextEvidenceCurrentStep("wmCode obtenido: " + wmCodeAck);
			testCase.addTextEvidenceCurrentStep("wmCode esperado: " + data.get("ackWmCode"));
			
			assertEquals(wmCodeAck, data.get("ackWmCode"), "Paso 3: Se obtiene wmCode diferente al esperado");
		}else {
			assertEquals(requestAck.getStatusCode(), Integer.parseInt(data.get("statusCode")), 
					"Se obtiene status diferente a 200");
		}
		
	//***************************** Paso 4 *********************************************************************	
		addStep("Conectarse a la Base de Datos: *FCTPEQA* en site 1 y site 2.");
		
		testCase.addTextEvidenceCurrentStep("Conexion a la base de datos: FCTAEQA");
		testCase.addTextEvidenceCurrentStep("Status: ");
		testCase.addBoldTextEvidenceCurrentStep("Conectado");
		
	//***************************** Paso 5 *********************************************************************	
		
		addStep("Ejecutar la siguiente consulta para validar que la transacciOn se registro correctamente en la tabla TPEUSER.GIF_TRANSACTION de la BD *FCTPEQA*.");
		
		String validateTransactionFormat = String.format(tdcTransactionQuery, folio);
		Thread.sleep(10000);
		SQLResult validateTransaction = executeQuery(dbFCTAEQA_QRO, validateTransactionFormat);
		System.out.println(validateTransactionFormat);
		
		String creationDate = "";
		if(!validateTransaction.isEmpty()) {
			creationDate = validateTransaction.getData(0, "CREATION_DATE");
			testCase.addQueryEvidenceCurrentStep(validateTransaction);
		}
		
		assertFalse(validateTransaction.isEmpty(), "No se obtuvieron registros de la transaccion");
		
	//****************************** Paso 6 ******************************************************************	
		
         addStep(" Conectarse a la Base de Datos *FCWMLTAEQA* en site 1 y site 2.");
		
		testCase.addTextEvidenceCurrentStep("Conexion a la base de datos: FCWMLTAEQA");
		testCase.addTextEvidenceCurrentStep("Status: ");
		testCase.addBoldTextEvidenceCurrentStep("Conectado");
		
	//***************************** Paso 7 ******************************************************************	
		
		addStep("Ejecutar la siguiente consulta en la base de datos FCWMLTAEQA para validar que no se encuentren registros de error de la PE01_D");
		
		
		System.out.println("FECHA: " + creationDate);
		DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat targetFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
        Date date = originalFormat.parse(creationDate);
        date.setTime(date.getTime() - 2000);
        String StringDate = targetFormat.format(date);
        System.out.println("Date" + date);
        System.out.println("String Date" + StringDate);
        
		String errorRecordsFormat = String.format(queryErrorRecords, StringDate, "%" + data.get("interface") + "%");
		SQLResult errorRecords = dbFCWMLTAEQA_MTY.executeQuery(errorRecordsFormat);
		System.out.println(errorRecordsFormat);
		
		testCase.addQueryEvidenceCurrentStep(errorRecords);
		
		assertTrue(errorRecords.isEmpty(), "Se obtuvieron registros de error en la transaccion.");
		
			
	}

	@Override
	public String setTestFullName() {
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "";
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
