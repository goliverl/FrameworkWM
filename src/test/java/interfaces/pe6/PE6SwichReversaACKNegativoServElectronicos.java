package interfaces.pe6;

import java.lang.Override;
import java.lang.String;
import java.util.HashMap;
import modelo.BaseExecution;
import om.PE6;
import util.GlobalVariables;
import utils.webmethods.ReadRequest;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.GlobalVariables.DB_PASSWORD_FCTDCQA;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

import org.w3c.dom.Document;

public class PE6SwichReversaACKNegativoServElectronicos extends BaseExecution {
	//

	@BeforeMethod
	public void beforeMethod() {

	}

	@Test(dataProvider = "data-provider")

	public void ATC_FT_006_PE6_Reversa_ACK_Negativo(HashMap<String, String> data) throws Exception {
		utils.sql.SQLUtil dbFCTDCQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA,
				GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		utils.sql.SQLUtil dbFCWMLTAEQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA,
				GlobalVariables.DB_USER_FCWMLTAEQA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA); 
		utils.sql.SQLUtil dbFCSWQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO,
				GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		// BASE DE DATOS ANTIGUA
        utils.sql.SQLUtil dbFCSWQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA,GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);

		// BASE DE DATOS NUEVA PARA PRUEBAS
		
		//utils.sql.SQLUtil dbFCSWQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWPRD_CRECI,
			//	GlobalVariables.DB_USER_FCSWPRD_CRECI, GlobalVariables.DB_PASSWORD_FCSWPRD_CRECI);

		PE6 pe6Util = new PE6(data, testCase, null);

		// consulta para verificar la transaccion registrada
		String transaccionRegistrada_part1 = "SELECT folio,Plaza,Tienda,caja,Creation_date,Prom_Type,Sw_auth FROM TPEUSER.TDC_TRANSACTION WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCTDCQA.
		String transaccionRegistrada_part2 = "SELECT Wm_code,Track1,Card_Type,BIN,Issuer,Bank FROM TPEUSER.TDC_TRANSACTION WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCTDCQA.
		String transaccionRegistrada_part3 = "SELECT Sw_auth_code,Amount,Switch,IS_Name,Site FROM TPEUSER.TDC_TRANSACTION WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCTDCQA.

		String transRegistradaFCSWQA_part1 = "SELECT Creation_Date,Folio,MTI,Amount,PAN FROM SWUSER.TPE_SW_TLOG WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCSWQA
		String transRegistradaFCSWQA_part2 = "SELECT AUTH_ID_RES,RESP_CODE,SW_CODE,COUNTER,IS_NAME FROM SWUSER.TPE_SW_TLOG WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCSWQA
		String transRegistradaFCSWQA_part3 = "SELECT APPLICATION,ACQUIRER,ENTITY,Plaza,Tienda FROM SWUSER.TPE_SW_TLOG WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCSWQA
		String transRegistradaFCSWQA_part4 = "SELECT Caja,POS_ENTRY_MODE,PROC_CODE FROM SWUSER.TPE_SW_TLOG WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCSWQA

		// consulta para verificar que no se registraron errores en la transaccion
		String errorTransaccion = "SELECT ERROR_ID,FOLIO,ERROR_DATE,SEVERITY,ERROR_TYPE,ERROR_CODE FROM WMLOG.WM_LOG_ERROR_TPE WHERE TPE_TYPE='PE6' AND FOLIO='%s' AND ERROR_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY') ORDER BY ERROR_DATE DESC";// FCWMLTAEQA.
		// String tipo=data.get("tipoDePrueba");
		// Configuracion de ambiente
		boolean tipo = pe6Util.tipoDePrueba(dbFCTDCQA);

		// Valores WMcode esperados
		final String expectedWMCodeGetFolio = data.get("WMCodeGetFolio");
		final String expectedWMCodeGetAuth = data.get("WMCodeGetAuth");
		final String expectedWMCodeGetAuthAck = data.get("WMCodeGetAuthAck");

		//testCase.setFullTestName(data.get("casoDePrueba"));
		//testCase.setProject_Name("Crecimiento Servicios Electrónicos (SF)");
		//testCase.setTest_Description(data.get("Description"));

		String pre = data.get("pre");
		testCase.setPrerequisites(
				"Contar con acceso a la BD FCTDCQA_MTY.                                                                                        "
						+ "Contar con acceso a la BD FCWMLTAEQA_MTY.                                                                                       "
						+ "Contar con acceso a la BD FCSWPRD.                                                                                            "
						+ "Contar con un simulador bancario para que responda las solicitudes de autorización.                "
						+ "El bin relacionado a la tarjeta de " + pre
						+ " que se va a utilizar deberá tener el valor del campo APPLY_DEP='Y' en la tabla TDC_BIN de la BD FCTDCQA_MTY.                                                                 "
						+ "El bin relacionado a la tarjeta de " + pre
						+ " que se va a utilizar deberá tener el valor PROC_CODE='PAY' en la tabla TDC_ROUTING de la BD FCTDCQA_MTY.                                             "
						+ "Que el servicio/job PE6.Pub:runReverseManager.sh se encuentre en ejecución automática.");

		// Paso 1 *****************************************************************************************************
		
		addStep("Solicitar un folio desde el navegador, invocando el servicio runGetFolio:");
		// ejecutar request
		String responseRunGetFolio = pe6Util.runGetFolio_request();
		String folio;
		String creationDate;
		// Obtener variables de la respuesta
		Document runGetFolioRequestDoc = ReadRequest.convertStringToXMLDocument(responseRunGetFolio);
		folio = runGetFolioRequestDoc.getElementsByTagName("folio").item(0).getTextContent();
		creationDate = runGetFolioRequestDoc.getElementsByTagName("creationDate").item(0).getTextContent();

		System.out.println("Folio: " + folio);
		System.out.println("creationDate: " + creationDate);

		String wmCodeFolio = runGetFolioRequestDoc.getElementsByTagName("wmCode").item(0).getTextContent();
		boolean validateRequest = wmCodeFolio.equals(expectedWMCodeGetFolio);
		System.out.println("wmCodeFolio: " + wmCodeFolio);

		assertTrue(validateRequest, "el wmCodeFolio no es" + responseRunGetFolio);

		/**************************************************************************************************
		 * Solicitud de autoriazación
		 *************************************************************************************************/

       // Paso 2 *****************************************************************************************************
		
		String tarjeta = data.get("Name");
		addStep("Solicitar autorización de " + tarjeta + " desde un navegador, invocando el servicio runGetAuth:");

		// Ejecutar el request
		String responseRunGetAuth = pe6Util.runGetAuth_request();

		// Obtener variables de la respuesta
		Document runGetAuthRequestDoc = ReadRequest.convertStringToXMLDocument(responseRunGetAuth);

		String wmCodeAuth = runGetAuthRequestDoc.getElementsByTagName("wmCode").item(0).getTextContent();

		boolean validationAuth = wmCodeAuth.equals(expectedWMCodeGetAuth);

		System.out.println("wmCodeAuth: " + wmCodeAuth);

		assertTrue(validationAuth, "el wmCodeAuth no es" + expectedWMCodeGetAuth); 

       // Paso 3 *********************************************************************************************************
		
		addStep("Solicitar confirmación de forma negativa a la autorización del " + tarjeta
				+ " desde un navegador (con ACK=01), invocando el servicio runGetAuth: ");

		// Ejecutar el request
		String responseRunGetAuthAck = pe6Util.runGetAuthAck_request();

		// Obtener variables de la respuesta
		Document runGetAuthAckRequestDoc = ReadRequest.convertStringToXMLDocument(responseRunGetAuthAck);

		String wmCodeAuthAck = runGetAuthAckRequestDoc.getElementsByTagName("wmCode").item(0).getTextContent();

		boolean responseAck = wmCodeAuthAck.equals(expectedWMCodeGetAuthAck);

		System.out.println("wmCodeAuthAck: " + wmCodeAuthAck);

		assertTrue(responseAck, "el wmCodeAuthAck no es" + expectedWMCodeGetAuthAck); 

		// Paso 4 *********************************************************************************************************
		
		addStep("Conectarse a la BD FCTDCQA_MTY.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTDCQA.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCTDCQA);

		// Paso 5 *********************************************************************************************************
		
		addStep("Ejecutar la siguiente consulta para validar que la transacción se registró correctamente con la reversa en la tabla TDC_TRANSACTION:");

		// Parte 1 *********************************************************************************************************
		
		System.out.println(GlobalVariables.DB_HOST_FCTDCQA);

		String formatTransaccionRegistrada_part1 = String.format(transaccionRegistrada_part1, folio);
		System.out.println(formatTransaccionRegistrada_part1);

		SQLResult ExecutetransaccionRegistrada_part1 = dbFCTDCQA.executeQuery(formatTransaccionRegistrada_part1);

		boolean ValidaRegistroBoolean_1 = ExecutetransaccionRegistrada_part1.isEmpty();
		System.out.println(ValidaRegistroBoolean_1);

		if (!ValidaRegistroBoolean_1) {
			testCase.addTextEvidenceCurrentStep(formatTransaccionRegistrada_part1);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"Se muestra el registro de las transacciones con los siguientes valores:");
			testCase.addQueryEvidenceCurrentStep(ExecutetransaccionRegistrada_part1, false);
		}
		
		// Parte2 *********************************************************************************************************
		
		String formatTransaccionRegistrada_part2 = String.format(transaccionRegistrada_part2, folio);
		System.out.println(formatTransaccionRegistrada_part2);

		SQLResult ExecutetransaccionRegistrada_part2 = dbFCTDCQA.executeQuery(formatTransaccionRegistrada_part2);

		boolean ValidaRegistroBoolean_2 = ExecutetransaccionRegistrada_part1.isEmpty();

		String WmCodeFCTDCQA = ExecutetransaccionRegistrada_part2.getData(0, "Wm_code");
		System.out.println("WmCodeFCTDCQA= " + WmCodeFCTDCQA);

		System.out.println(ValidaRegistroBoolean_2);

		if (!ValidaRegistroBoolean_2) {

			testCase.addQueryEvidenceCurrentStep(ExecutetransaccionRegistrada_part2, false);

		}
		
		// Parte 3 *********************************************************************************************************
		
		String formatTransaccionRegistrada_part3 = String.format(transaccionRegistrada_part3, folio);
		System.out.println(formatTransaccionRegistrada_part3);

		SQLResult ExecutetransaccionRegistrada_part3 = dbFCTDCQA.executeQuery(formatTransaccionRegistrada_part3);

		boolean ValidaRegistroBoolean_3 = ExecutetransaccionRegistrada_part3.isEmpty();

		System.out.println(ValidaRegistroBoolean_3);

		if (!ValidaRegistroBoolean_3) {

			testCase.addQueryEvidenceCurrentStep(ExecutetransaccionRegistrada_part3, false);

		}

		// ****************************************************Validar conexion
		// FCSWQA*************************************************************************
		
		addStep("Conectarse a la BD FCSWPRD.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCSWPRD ");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCSWPRD_CRECI);

        // Paso 7 *********************************************************************************************************
		
		addStep("Ejecutar la siguiente consulta para validar que la transacción se registró correctamente en la tabla TPE_SW_TLOG:");
		System.out.println(GlobalVariables.DB_HOST_FCSWPRD_CRECI);
		Thread.sleep(6000);
		
		// Parte 1 *********************************************************************************************************
		
		String formatTransRegistradaFCSWQA_part1 = String.format(transRegistradaFCSWQA_part1, folio);
		System.out.println(formatTransRegistradaFCSWQA_part1);

		SQLResult ExecuteTransRegistradaFCSWQA_part1 = executeQuery(dbFCSWQA, formatTransRegistradaFCSWQA_part1);

		boolean ValidaTransRegistroBoolean_1 = ExecuteTransRegistradaFCSWQA_part1.isEmpty();

		System.out.println(ValidaTransRegistroBoolean_1);

		if (!ValidaTransRegistroBoolean_1) {
			testCase.addTextEvidenceCurrentStep(formatTransRegistradaFCSWQA_part1);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"Para las transacciones de reversa se generan 2 registros, en los cuáles el campo MTI tendrá los siguientes valores:");
			testCase.addBoldTextEvidenceCurrentStep("MTI=0200 ->PARA LA SOLICITUD");
			testCase.addBoldTextEvidenceCurrentStep("MTI=0420 -> PARA LA REVERSA");
			testCase.addBoldTextEvidenceCurrentStep(
					"Se muestra el registro de la transacción con los siguientes valores:");
			testCase.addQueryEvidenceCurrentStep(ExecuteTransRegistradaFCSWQA_part1, false);
		} else {

			SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, formatTransRegistradaFCSWQA_part1);

			ValidaTransRegistroBoolean_1 = resultQRO.isEmpty();

			if (!ValidaTransRegistroBoolean_1) {
				testCase.addTextEvidenceCurrentStep(formatTransRegistradaFCSWQA_part1);
				testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
				testCase.addBoldTextEvidenceCurrentStep(
						"Para las transacciones de reversa se generan 2 registros, en los cuáles el campo MTI tendrá los siguientes valores:");
				testCase.addBoldTextEvidenceCurrentStep("MTI=0200 ->PARA LA SOLICITUD");
				testCase.addBoldTextEvidenceCurrentStep("MTI=0420 -> PARA LA REVERSA");
				testCase.addBoldTextEvidenceCurrentStep(
						"Se muestra el registro de la transacción con los siguientes valores:");
				testCase.addQueryEvidenceCurrentStep(resultQRO, false);
			}
		}

		// Parte 2 *********************************************************************************************************
		
		String formatTransRegistradaFCSWQA_part2 = String.format(transRegistradaFCSWQA_part2, folio);
		System.out.println(formatTransRegistradaFCSWQA_part2);

		SQLResult ExecuteTransRegistradaFCSWQA_part2 = executeQuery(dbFCSWQA, formatTransRegistradaFCSWQA_part2);

		boolean ValidaTransRegistroBoolean_2 = ExecuteTransRegistradaFCSWQA_part2.isEmpty();

		System.out.println(ValidaTransRegistroBoolean_2);

		if (!ValidaTransRegistroBoolean_2) {
			testCase.addQueryEvidenceCurrentStep(ExecuteTransRegistradaFCSWQA_part2, false);
		} else {

			SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, formatTransRegistradaFCSWQA_part2);

			ValidaTransRegistroBoolean_2 = resultQRO.isEmpty();

			if (!ValidaTransRegistroBoolean_2) {
				testCase.addQueryEvidenceCurrentStep(resultQRO, false);
			}
		}

		// Parte 3 *********************************************************************************************************
		
		String formatTransRegistradaFCSWQA_part3 = String.format(transRegistradaFCSWQA_part3, folio);
		System.out.println(formatTransRegistradaFCSWQA_part3);

		SQLResult ExecuteTransRegistradaFCSWQA_part3 = executeQuery(dbFCSWQA, formatTransRegistradaFCSWQA_part3);

		boolean ValidaTransRegistroBoolean_3 = ExecuteTransRegistradaFCSWQA_part3.isEmpty();

		System.out.println(ValidaTransRegistroBoolean_3);

		if (!ValidaTransRegistroBoolean_3) {
			testCase.addQueryEvidenceCurrentStep(ExecuteTransRegistradaFCSWQA_part3, false);
		} else {

			SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, formatTransRegistradaFCSWQA_part3);

			ValidaTransRegistroBoolean_3 = resultQRO.isEmpty();

			if (!ValidaTransRegistroBoolean_3) {
				testCase.addQueryEvidenceCurrentStep(resultQRO, false);
			}
		}
		
		// Parte 4 *********************************************************************************************************
		
		String formatTransRegistradaFCSWQA_part4 = String.format(transRegistradaFCSWQA_part4, folio);
		System.out.println(formatTransRegistradaFCSWQA_part4);

		SQLResult ExecuteTransRegistradaFCSWQA_part4 = executeQuery(dbFCSWQA, formatTransRegistradaFCSWQA_part4);

		boolean ValidaTransRegistroBoolean_4 = ExecuteTransRegistradaFCSWQA_part4.isEmpty();

		System.out.println(ValidaTransRegistroBoolean_4);

		if (!ValidaTransRegistroBoolean_4) {
			testCase.addQueryEvidenceCurrentStep(ExecuteTransRegistradaFCSWQA_part4, false);
			testCase.addTextEvidenceCurrentStep("Se encontró el registro en la BD de FCSWPRD.");

		} else {

			SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, formatTransRegistradaFCSWQA_part4);

			ValidaTransRegistroBoolean_4 = resultQRO.isEmpty();

			if (!ValidaTransRegistroBoolean_4) {
				testCase.addQueryEvidenceCurrentStep(resultQRO, false);
				testCase.addTextEvidenceCurrentStep("Se encontró el registro en la BD de FCSWQA_QUERETARO_S2.");

			}
		}
		String wmcode119 = "119";
		boolean boton = data.get("WMCodeGetAuthAck").equals(wmcode119);
		if (boton) {
			assertTrue(ValidaTransRegistroBoolean_4, "se muestra registro");
		} else {
			assertTrue(!ValidaTransRegistroBoolean_4, "No se muestra registro"); 

		}

		// ****************************************************Validar conexion
		// FCWMLTAEQA
		// *************************************************************************
		
		addStep("Conectarse a la BD FCWMLTAEQA_MTY.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAQ.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito con la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMLTAEQA);

		// Paso 9 *********************************************************************************************************
		
		addStep(" Ejecutar la siguiente consulta para validar que no se registró error en la transacción:");
		System.out.println(GlobalVariables.DB_HOST_FCWMLTAEQA);

		String formatErrorTransaccion = String.format(errorTransaccion, folio);
		System.out.println(formatErrorTransaccion);

		SQLResult ExecuteErrorTransaccion = dbFCWMLTAEQA.executeQuery(formatErrorTransaccion);

		boolean ValidaRegistroErrorBoolean = ExecuteErrorTransaccion.isEmpty();

		System.out.println(ValidaRegistroErrorBoolean);

		if (ValidaRegistroErrorBoolean) {
			testCase.addTextEvidenceCurrentStep(formatErrorTransaccion);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"No se muestra ningún registro de error relacionado con la transacción.");
			testCase.addQueryEvidenceCurrentStep(ExecuteErrorTransaccion, false);

		}
		assertTrue(ValidaRegistroErrorBoolean, "No hay registros de error en la tabla WMLOG.WM_LOG_ERROR_TPE ");
	
	} 

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. Solicitud de confirmacion Negativa de venta ACK";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_006_PE6_Reversa_ACK_Negativo";
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