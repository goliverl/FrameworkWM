package interfaces.pe6;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;
import org.w3c.dom.Document;

import modelo.BaseExecution;
import om.PE6;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.webmethods.ReadRequest;

public class PE6_WMx86_TransaccionDepositoNumeroCelular extends BaseExecution {

	@Test(dataProvider = "data-provider")

	public void ATC_FT_031_PE6_WMx86_TransaccionDepositoNumeroCelular(HashMap<String, String> data) throws Exception {
		SQLUtil dbFCTDCQA = new SQLUtil(GlobalVariables.DB_HOST_FCTDCQA_QRO, GlobalVariables.DB_USER_FCTDCQA_QRO,
				GlobalVariables.DB_PASSWORD_FCTDCQA_QRO);
		SQLUtil dbFCWMLTAEQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_QA,
				GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		SQLUtil dbFCSWQA_QRO = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO, GlobalVariables.DB_USER_FCSWQA_QRO,
				GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		SQLUtil dbFCSWQA = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA,
				GlobalVariables.DB_PASSWORD_FCSWQA);

		PE6 pe6Util = new PE6(data, testCase, null);

		// consulta para verificar la transaccion registrada
		String transaccionRegistrada_part1 = "SELECT folio,Plaza,Tienda,caja,Creation_date,Prom_Type,Sw_auth "
				+ "FROM TPEUSER.TDC_TRANSACTION " + "WHERE FOLIO='%s' AND " + "WM_CODE='101' AND " + "SW_AUTH_CODE='00' "
				+ " and CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCTDCQA.
		String transaccionRegistrada_part2 = "SELECT Wm_code,Track1,Card_Type,BIN,Issuer,Bank "
				+ "FROM TPEUSER.TDC_TRANSACTION " + "WHERE FOLIO='%s' AND " + "WM_CODE='101' AND " + "SW_AUTH_CODE='00'"
				+ "AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCTDCQA.
		String transaccionRegistrada_part3 = "SELECT Sw_auth_code,Amount,Switch,IS_Name,Site "
				+ "FROM TPEUSER.TDC_TRANSACTION " + "WHERE FOLIO='%s' AND " + "WM_CODE='101' AND " + "SW_AUTH_CODE='00'"
				+ "AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCTDCQA.

		String tdcTLOGQuery = " SELECT CREATION_DATE, FOLIO, MTI, AMOUNT, PAN, AUTH_ID_RES "
				+ " FROM SWUSER.TPE_SW_TLOG" + " where " + "  MTI='0200'" + " and folio = %s" + " and AUTH_ID_RES='%s'"
				+ " and RESP_CODE='00'" + " AND CREATION_DATE >=TRUNC(SYSDATE)";

		String tdcTLOGQueryPart2 = " SELECT SW_CODE, COUNTER, IS_NAME, APPLICATION, ENTITY" + " FROM SWUSER.TPE_SW_TLOG"
				+ " where " + "  MTI='0200'" + " and folio = %s" + " and AUTH_ID_RES='%s'" + " and RESP_CODE='00'"
				+ " AND CREATION_DATE >=TRUNC(SYSDATE)";

		String tdcTLOGQueryPart3 = " SELECT PLAZA, TIENDA, CAJA, ACQUIRER, RESP_CODE, POS_ENTRY_MODE, PROC_CODE"
				+ " FROM SWUSER.TPE_SW_TLOG" + " where " + "  MTI='0200'" + " and folio = %s" + " and AUTH_ID_RES='%s'"
				+ " and RESP_CODE='00'" + " AND CREATION_DATE >=TRUNC(SYSDATE)";

		String tdcTLOGQueryGeneral = " SELECT CREATION_DATE, FOLIO, MTI, AMOUNT, PAN, AUTH_ID_RES, SW_CODE, COUNTER, IS_NAME, APPLICATION, ENTITY, PLAZA, TIENDA, CAJA, ACQUIRER, RESP_CODE, POS_ENTRY_MODE, PROC_CODE"
				+ " FROM SWUSER.TPE_SW_TLOG" + " where " + "  MTI='0200'" + " and folio = %s" + " and AUTH_ID_RES='%s'"
				+ " and RESP_CODE='00'" + " AND CREATION_DATE >=TRUNC(SYSDATE)";

		// consulta para verificar que no se registraron errores en la transaccion
		String errorTransaccion = "SELECT ERROR_ID,FOLIO,ERROR_DATE,SEVERITY,ERROR_TYPE,ERROR_CODE "
				+ "FROM WMLOG.WM_LOG_ERROR_TPE WHERE " + "TPE_TYPE='PE6' " + "AND FOLIO='%s' "
				+ "AND ERROR_DATE>=(sysdate) " + "ORDER BY ERROR_DATE DESC";// FCWMLTAEQA.

//		String LogSecSess = "SELECT  APPLICATION,OPERATION,SOURCE,FOLIO,PLAZA,TIENDA,CREATION_DATE "
//		+ "FROM  WMLOG.SECURITY_SESSION_LOG " + "WHERE CREATION_DATE  BETWEEN (sysdate) AND (sysdate-300/86400) "
//		+ "AND APPLICATION = 'PE6' " + "ORDER BY CREATION_DATE DESC";

		
		String LogSecSess = "Select * from(SELECT  APPLICATION,OPERATION,SOURCE,FOLIO,PLAZA,TIENDA,CREATION_DATE  "
				+ "FROM  WMLOG.SECURITY_SESSION_LOG  "
				+ "WHERE CREATION_DATE  >=trunc(sysdate) "
				+ "AND APPLICATION = 'PE6'  "
				+ "ORDER BY CREATION_DATE DESC ) where rownum <=3";

		// Configuracion de ambiente
		boolean tipo = pe6Util.tipoDePrueba(dbFCTDCQA);
		testCase.setProject_Name("POC WMx86");
		// Valores WMcode esperados
		final String expectedWMCodeGetFolio = data.get("WMCodeGetFolio");
		final String expectedWMCodeGetAuth = data.get("WMCodeGetAuth");
		final String expectedWMCodeGetAuthAck = data.get("WMCodeGetAuthAck");

		// Paso 1 *********************************************************

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

		// Paso 2 *********************************************************

		addStep("Solicitar autorización  de depósito a número de celular desde un navegador, invocando el servicio **runGetAuth**");

		// Ejecutar el request
		String responseRunGetAuth = pe6Util.runGetAuth_requestWM_NumCel(folio,creationDate);

		// Obtener variables de la respuesta
		Document runGetAuthRequestDoc = ReadRequest.convertStringToXMLDocument(responseRunGetAuth);

		String wmCodeAuth = runGetAuthRequestDoc.getElementsByTagName("wmCode").item(0).getTextContent();

		boolean validationAuth = wmCodeAuth.equals(expectedWMCodeGetAuth);

		System.out.println("wmCodeAuth: " + wmCodeAuth);

		assertTrue(validationAuth, "el wmCodeAuth no es" + expectedWMCodeGetAuth);

		// Paso 3 *********************************************************

		addStep("Ejecutar el servicio  RunGetauthack desde un navegador de la interface PE6");
		// Ejecutar el request
		String responseRunGetAuthAck = pe6Util.runGetAuthAck_requestWM_NumCel();

		// Obtener variables de la respuesta
		Document runGetAuthAckRequestDoc = ReadRequest.convertStringToXMLDocument(responseRunGetAuthAck);

		String wmCodeAuthAck = runGetAuthAckRequestDoc.getElementsByTagName("wmCode").item(0).getTextContent();

		boolean responseAck = wmCodeAuthAck.equals(expectedWMCodeGetAuthAck);

		System.out.println("wmCodeAuthAck: " + wmCodeAuthAck);

		assertTrue(responseAck, "el wmCodeAuthAck no es" + expectedWMCodeGetAuthAck);

		// Paso 4 *********************************************************

		addStep("Conectarse a la BD FCTDCQA_MTY.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTDCQA.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCTDCQA);

		// Paso 5 *********************************************************

		addStep("Ejecutar la siguiente consulta para validar que la transacción se registró correctamente en la tabla TDC_TRANSACTION:");

		// Parte 1 *********************************************************

		System.out.println(GlobalVariables.DB_HOST_FCTDCQA);
		String SW_AUTH = "";
		String formatTransaccionRegistrada_part1 = String.format(transaccionRegistrada_part1, folio);
		System.out.println(formatTransaccionRegistrada_part1);

		SQLResult ExecutetransaccionRegistrada_part1 = dbFCTDCQA.executeQuery(formatTransaccionRegistrada_part1);

		boolean ValidaRegistroBoolean_1 = ExecutetransaccionRegistrada_part1.isEmpty();

		System.out.println(ValidaRegistroBoolean_1);

		if (!ValidaRegistroBoolean_1) {
			SW_AUTH = ExecutetransaccionRegistrada_part1.getData(0, "SW_AUTH");
			testCase.addTextEvidenceCurrentStep(formatTransaccionRegistrada_part1);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"Se muestra el registro de la transacción con los siguientes valores:");
		}
		testCase.addQueryEvidenceCurrentStep(ExecutetransaccionRegistrada_part1);

		// Parte 2 *********************************************************

		String formatTransaccionRegistrada_part2 = String.format(transaccionRegistrada_part2, folio);
		System.out.println(formatTransaccionRegistrada_part2);

		SQLResult ExecutetransaccionRegistrada_part2 = dbFCTDCQA.executeQuery(formatTransaccionRegistrada_part2);

		boolean ValidaRegistroBoolean_2 = ExecutetransaccionRegistrada_part1.isEmpty();

		String WmCodeFCTDCQA = ExecutetransaccionRegistrada_part2.getData(0, "Wm_code");
		System.out.println("WmCodeFCTDCQA= " + WmCodeFCTDCQA);

		System.out.println(ValidaRegistroBoolean_2);

		testCase.addQueryEvidenceCurrentStep(ExecutetransaccionRegistrada_part2);

		// Parte 3 *********************************************************

		String formatTransaccionRegistrada_part3 = String.format(transaccionRegistrada_part3, folio);
		System.out.println(formatTransaccionRegistrada_part3);

		SQLResult ExecutetransaccionRegistrada_part3 = dbFCTDCQA.executeQuery(formatTransaccionRegistrada_part3);

		boolean ValidaRegistroBoolean_3 = ExecutetransaccionRegistrada_part3.isEmpty();

		System.out.println(ValidaRegistroBoolean_3);

		testCase.addQueryEvidenceCurrentStep(ExecutetransaccionRegistrada_part3);

		assertFalse(ValidaRegistroBoolean_1, "No se encontraron resultados");

		// ****************************************************Validar conexion
		// FCSWQA*************************************************************************
		addStep("Conectarse a la BD FCSWQA.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCSWQA ");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCSWQA);

		// Paso 7 *********************************************************

		addStep("Ejecutar la siguiente consulta para validar que la transacción se registró en la tabla TPE_SW_TLOG:");
		System.out.println(GlobalVariables.DB_HOST_FCSWQA);

		// Parte 1 *********************************************************

		addStep("Ejecutar la siguiente consulta de la tabla TPE_SW_TLOG:");

		String tempTPEQuery = String.format(tdcTLOGQuery, folio, SW_AUTH);
		String tempTPEQueryPart2 = String.format(tdcTLOGQueryPart2, folio, SW_AUTH);
		String tempTPEQueryPart3 = String.format(tdcTLOGQueryPart3, folio, SW_AUTH);
		String tempTPEQueryGeneral = String.format(tdcTLOGQueryGeneral, folio, SW_AUTH);
		System.out.println(tempTPEQuery);
		SQLResult result3 = executeQuery(dbFCSWQA, tempTPEQuery);// parte 1
		SQLResult result3Part2 = executeQuery(dbFCSWQA, tempTPEQueryPart2);// parte 2
		SQLResult result3Part3 = executeQuery(dbFCSWQA, tempTPEQueryPart3);// parte 3

		System.out.print(result3);
		boolean validationTPE = result3.isEmpty();

		if (!validationTPE) {
			testCase.addTextEvidenceCurrentStep(tempTPEQueryGeneral);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"Se muestra el registro de la transacción de TAE de forma exitosa.");
			testCase.addQueryEvidenceCurrentStep(result3);
			testCase.addQueryEvidenceCurrentStep(result3Part2);
			testCase.addQueryEvidenceCurrentStep(result3Part3);
			testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWQA.");
		} else {

			SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, tempTPEQuery);
			SQLResult result3Part2QRO = executeQuery(dbFCSWQA_QRO, tempTPEQueryPart2);// parte 2
			SQLResult result3Part3QRO = executeQuery(dbFCSWQA_QRO, tempTPEQueryPart3);// parte 3

			validationTPE = resultQRO.isEmpty();

			if (!validationTPE) {
				testCase.addTextEvidenceCurrentStep(tempTPEQueryGeneral);
				testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
				testCase.addBoldTextEvidenceCurrentStep(
						"Se muestra el registro de la transacción de TAE de forma exitosa.");
				testCase.addQueryEvidenceCurrentStep(resultQRO, false);
				testCase.addQueryEvidenceCurrentStep(result3Part2QRO, false);
				testCase.addQueryEvidenceCurrentStep(result3Part3QRO, false);
				testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWQA_QUERETARO_S2.");
			}
		}

		assertTrue(!validationTPE, "No se obtuvieron los resultados esperados");

		// ****************************************************Validar conexion
		// FCWMLTAEQA
		// *************************************************************************
		addStep("Conectarse a la BD FCWMLTAEQA_MTY.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAQ.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMLTAEQA);

		// Paso 9 *********************************************************

		addStep("Ejecutar la siguiente consulta para validar que no se registró error en la transacción:");
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
			testCase.addQueryEvidenceCurrentStep(ExecuteErrorTransaccion);

		}
		assertTrue(ValidaRegistroErrorBoolean, "hay registros de error en la tabla WMLOG.WM_LOG_ERROR_TPE ");

		/*
		 * Paso 10
		 *****************************************************************************************/
		addStep("Ejecutar  la siguiente consulta  en la BD   *FCWMLTAEQA* para verificar que no  viajo por un canal seguro.");
		System.out.println(GlobalVariables.DB_HOST_FCWMLTAEQA);
		System.out.println(LogSecSess);

		SQLResult resultSec = executeQuery(dbFCWMLTAEQA, LogSecSess);

		boolean resultSecVal = resultSec.isEmpty();

		if (!resultSecVal) {
			testCase.addTextEvidenceCurrentStep(LogSecSess);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"Se visualiza los registros de las invocaciones realizadas a la PE6  de la  transacción  de TAE en la BD *FCWMLTAEQA* , por lo tanto no viajo por un canal seguro");

		}
		testCase.addQueryEvidenceCurrentStep(resultSec);

		assertTrue(!resultSecVal, "No se encontraron registros en la consulta");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. MTC-FT-014 PE6_PAY Transaccion exitosa deposito a numero de celular.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Sergio Robles Ramos";
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_031_PE6_WMx86_TransaccionDepositoNumeroCelular";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return "*Contar con un simulador de proveedor de corresponsalías que responda las transacciones de forma exitosa.\r\n"
				+ "*Contar con acceso a las base de datos de FCTDCQA_MTY, FCTDCQA_QRO, FCWMLTAEQA_MTY y FCWMLTAEQA_QRO.\r\n"
				+ "*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA.";
	}
}
