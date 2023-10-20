package interfaces.pe1;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.PE1;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PE1_WMx86_TransaccionExitosaRecargaTiempoAire extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_008_PE1_WMx86_TransaccionExitosaRecarga(HashMap<String, String> data) throws Exception {
		
		
		/*
		 * 
		 * Modificado por mantenimiento
		 * @author Brandon Ruiz
		 * @date 15/02/2023
		 * @cp MTC-FT-001 PE1 Transaccion exitosa de recarga de tiempo aire electronico
		 * @projectname Actualizacion Tecnologica Webmethods
		 * 
		 */

		/*
		 * Utilerias
		 *********************************************************************/
		
		SQLUtil dbFCT = new SQLUtil(GlobalVariables.DB_HOST_FCTAEQA, GlobalVariables.DB_USER_FCTAEQA,GlobalVariables.DB_PASSWORD_FCTAEQA);
		SQLUtil dbFCW = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_QA,GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		SQLUtil dbFCS = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA,GlobalVariables.DB_PASSWORD_FCSWQA);
		SQLUtil dbFCSWQA_QRO = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO, GlobalVariables.DB_USER_FCSWQA_QRO,GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		PE1 pe1Util = new PE1(data, testCase, dbFCT);

		/*
		 * Variables
		 *************************************************************************/
		String wmCodeToValidate = data.get("wmCodeFolio");
		String wmCodeToValidateAuth = data.get("wmCodeAuth");
		String wmCodeToValidateAck = data.get("wmCodeAck");

		String folio;
		String wmCodeRequest;
//		String wmCodeDb;

		String tdcTransactionQuery = "SELECT FOLIO, CREATION_DATE, PLAZA, TIENDA, WM_CODE"
				+ " FROM TPEUSER.TAE_TRANSACTION" + " WHERE CREATION_DATE >=TRUNC(SYSDATE)" + " AND folio = %s"
				+ " ORDER BY CREATION_DATE DESC";

		String tdcTransactionQueryPart2 = "SELECT PHONE, SW_AUTH, SWITCH, IS_NAME " + " FROM TPEUSER.TAE_TRANSACTION"
				+ " WHERE CREATION_DATE >=TRUNC(SYSDATE)" + " AND folio = %s" + " ORDER BY CREATION_DATE DESC";

		String tdcTransactionQueryGeneral = "SELECT FOLIO, CREATION_DATE, PLAZA, TIENDA, WM_CODE, PHONE, SW_AUTH, SWITCH, IS_NAME "
				+ " FROM TPEUSER.TAE_TRANSACTION" + " WHERE CREATION_DATE >=TRUNC(SYSDATE)" + " AND folio = %s"
				+ " ORDER BY CREATION_DATE DESC";

		String tdcErrorQuery = "SELECT ERROR_ID, FOLIO, ERROR_DATE, ERROR_CODE, TPE_TYPE "
				+ " FROM WMLOG.WM_LOG_ERROR_TPE " + " WHERE ERROR_DATE >=TRUNC(SYSDATE) " + " AND (TPE_TYPE = 'PE1')"
				+ " ORDER BY ERROR_DATE DESC";

		String tdcTLOGQuery = " SELECT CREATION_DATE, FOLIO, MTI, AMOUNT, PAN, AUTH_ID_RES "
				+ " FROM SWUSER.TPE_SW_TLOG" + " where " + "  APPLICATION='TAE'" + " and MTI='0200'" + " and folio = %s"
				+ " and AUTH_ID_RES='%s'" + " and RESP_CODE='00'" + " AND CREATION_DATE >=TRUNC(SYSDATE)";

		String tdcTLOGQueryPart2 = " SELECT SW_CODE, COUNTER, IS_NAME, APPLICATION, ENTITY" + " FROM SWUSER.TPE_SW_TLOG"
				+ " where " + "  APPLICATION='TAE'" + " and MTI='0200'" + " and folio = %s" + " and AUTH_ID_RES='%s'"
				+ " and RESP_CODE='00'" + " AND CREATION_DATE >=TRUNC(SYSDATE)";

		String tdcTLOGQueryPart3 = " SELECT PLAZA, TIENDA, CAJA, ACQUIRER, RESP_CODE, POS_ENTRY_MODE, PROC_CODE"
				+ " FROM SWUSER.TPE_SW_TLOG" + " where " + "  APPLICATION='TAE'" + " and MTI='0200'" + " and folio = %s"
				+ " and AUTH_ID_RES='%s'" + " and RESP_CODE='00'" + " AND CREATION_DATE >=TRUNC(SYSDATE)";

		String tdcTLOGQueryGeneral = " SELECT CREATION_DATE, FOLIO, MTI, AMOUNT, PAN, AUTH_ID_RES, SW_CODE, COUNTER, IS_NAME, APPLICATION, ENTITY, PLAZA, TIENDA, CAJA, ACQUIRER, RESP_CODE, POS_ENTRY_MODE, PROC_CODE"
				+ " FROM SWUSER.TPE_SW_TLOG" + " where " + "  APPLICATION='TAE'" + " and MTI='0200'" + " and folio = %s"
				+ " and AUTH_ID_RES='%s'" + " and RESP_CODE='00'" + " AND CREATION_DATE >=TRUNC(SYSDATE)";

		String LogSecSess = "SELECT  APPLICATION,OPERATION,SOURCE,FOLIO,PLAZA,TIENDA,CREATION_DATE "
				+ "FROM  WMLOG.SECURITY_SESSION_LOG " + "WHERE CREATION_DATE >=TRUNC(SYSDATE) "
				+ "AND APPLICATION = 'PE1' " + "AND FOLIO = '%s' ORDER BY CREATION_DATE DESC";

		testCase.setProject_Name("Actualizacion tecnologica");
		/*
		 * Pasos
		 ************************************************************************
		 * 
		 * 
		 * 
		 * /* Paso 1
		 *****************************************************************************************/

		step("Ejecutar en un navegador directamente el servicio rungGetFolio de solicitud de folio de la PE1 del server QA8.");

		// Ejecuta el servicio PE1.Pub:runGetFolio

		String respuestaFolio = pe1Util.ejecutarRunGetFolio();

		System.out.println("\n" + respuestaFolio + "\n");

		folio = getSimpleDataXml(respuestaFolio, "folio");

		wmCodeRequest = getSimpleDataXml(respuestaFolio, "wmCode");

		boolean validationResponseFolio = true;

		if (respuestaFolio != null) {
			validationResponseFolio = false;
		}

		assertFalse(validationResponseFolio);

		boolean validationRequestFolio = wmCodeRequest.equals(wmCodeToValidate);

		System.out.println("\n" + validationRequestFolio + " - wmCode request: " + wmCodeRequest + "\n");

		assertTrue(validationRequestFolio, "El Codigo wmCode no es el esperado: " + wmCodeToValidate);

		/*
		 * Paso 2
		 *****************************************************************************************/
		step("Ejecutar en un navegador directamente el servicio de solicitud de autorizacion:");

		// Ejecuta el servicio PE1.Pub/runGetAuth
		String respuestaAuth = pe1Util.ejecutarRunGetAuth(folio);

		System.out.print("\n" + respuestaAuth + "\n");

		wmCodeRequest = getSimpleDataXml(respuestaAuth, "wmCode");

		boolean validationResponseAuth = true;

		if (respuestaAuth != null) {
			validationResponseAuth = false;
		}
		assertFalse(validationResponseAuth);

		boolean validationRequestAuth = wmCodeRequest.equals(wmCodeToValidateAuth);

		System.out.println("\n" + validationRequestAuth + " - wmCode request: " + wmCodeRequest + "\n");
		testCase.addTextEvidenceCurrentStep(
				"Codigo esperado: " + wmCodeToValidateAuth + "\n" + "Codigo XML: " + wmCodeRequest);

		assertTrue(validationRequestAuth, "El Codigo wmCode no es el esperado: " + wmCodeToValidateAuth);

		/*
		 * Paso 3
		 *****************************************************************************************/
		String ackNegativo = "01";
		String ackStep = " ";
		if (data.get("ack").equals(ackNegativo)) {
			ackStep = "Negativo";
		}

		step("Ejecutar en un navegador directamente el servicio de solicitud de Confirmacion ACK " + ackStep + ":");

		// Ejecuta el servicio PE1.Pub:runGetAck
		String respuestaAck = pe1Util.ejecutarRunGetAck(folio);

		System.out.print("\n" + respuestaAck + "\n");

		wmCodeRequest = getSimpleDataXml(respuestaAck, "wmCode");

		boolean validationResponseAck = true;

		if (respuestaAck != null) {
			validationResponseAck = false;
		}
		assertFalse(validationResponseAck);

		// VALIDAR QUE EL WMCODE DE LA RESPUESTA SEA IGUAL A 101
		boolean validationRequestAck = wmCodeRequest.equals(wmCodeToValidateAck);

		System.out.println("\n" + validationRequestAck + " - wmCode request: " + wmCodeRequest + "\n");

		assertTrue(validationRequestAck, "El codigo wmCode no es el esperado: " + wmCodeToValidateAck);

		/*
		 * Paso 4
		 *****************************************************************************************/
		step("Establecer conexion con la base de datos: FCTAEQA_MTY ");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTAEQA.FEMCOM.NET ");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexion a la base de datos FCTAEQA_MTY fue exitosa.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCTAEQA);

		/*
		 * Paso 5
		 *****************************************************************************************/
		step("Ejecutar la siguiente consulta de la tabla TAE_TRANSACTION:");

		// Se forma el query a utilizar
		System.out.println(GlobalVariables.DB_HOST_FCTAEQA);
		String query = String.format(tdcTransactionQuery, folio);
		String queryPart2 = String.format(tdcTransactionQueryPart2, folio);
		String queryGeneral = String.format(tdcTransactionQueryGeneral, folio);
		System.out.println(query);

		SQLResult result1 = executeQuery(dbFCT, query);
		SQLResult result1Part2 = executeQuery(dbFCT, queryPart2);
		String SW_AUTH = "";
		boolean transaction = result1.isEmpty();

		if (!transaction) {
			SW_AUTH = result1Part2.getData(0, "SW_AUTH");
			testCase.addTextEvidenceCurrentStep(queryGeneral);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"Se muestra el registro de la transaccion de TAE de forma exitosa.");
			
		}
		testCase.addQueryEvidenceCurrentStep(result1);
		testCase.addQueryEvidenceCurrentStep(result1Part2);

		assertTrue(!transaction);

//****************************************************Validar conexion FCSWQA*************************************************************************

		step("Establecer conexion con la base de datos: FCSWQA");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCSWQA");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexion a la base de datos FCSWQA fue exitosa.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCSWQA);

		/*
		 * Paso 7
		 *****************************************************************************************/

		step("Ejecutar la siguiente consulta de la tabla TPE_SW_TLOG:");

		String tempTPEQuery = String.format(tdcTLOGQuery, folio, SW_AUTH);
		String tempTPEQueryPart2 = String.format(tdcTLOGQueryPart2, folio, SW_AUTH);
		String tempTPEQueryPart3 = String.format(tdcTLOGQueryPart3, folio, SW_AUTH);
		String tempTPEQueryGeneral = String.format(tdcTLOGQueryGeneral, folio, SW_AUTH);
		System.out.print(tempTPEQuery);
		SQLResult result3 = executeQuery(dbFCS, tempTPEQuery);// parte 1
		SQLResult result3Part2 = executeQuery(dbFCS, tempTPEQueryPart2);// parte 2
		SQLResult result3Part3 = executeQuery(dbFCS, tempTPEQueryPart3);// parte 3

		System.out.print(result3);
		boolean validationTPE = result3.isEmpty();

		if (!validationTPE) {
			testCase.addTextEvidenceCurrentStep(tempTPEQueryGeneral);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"Se muestra el registro de la transaccion de TAE de forma exitosa.");
			testCase.addQueryEvidenceCurrentStep(result3, false);
			testCase.addQueryEvidenceCurrentStep(result3Part2, false);
			testCase.addQueryEvidenceCurrentStep(result3Part3, false);
			testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWPRD.");
		} else {

			SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, tempTPEQuery);
			SQLResult result3Part2QRO = executeQuery(dbFCSWQA_QRO, tempTPEQueryPart2);// parte 2
			SQLResult result3Part3QRO = executeQuery(dbFCSWQA_QRO, tempTPEQueryPart3);// parte 3

			validationTPE = resultQRO.isEmpty();

			if (!validationTPE) {
				testCase.addTextEvidenceCurrentStep(tempTPEQueryGeneral);
				testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
				testCase.addBoldTextEvidenceCurrentStep(
						"Se muestra el registro de la transaccion de TAE de forma exitosa.");
				testCase.addQueryEvidenceCurrentStep(resultQRO, false);
				testCase.addQueryEvidenceCurrentStep(result3Part2QRO, false);
				testCase.addQueryEvidenceCurrentStep(result3Part3QRO, false);
				testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWQA_QUERETARO_S2.");
			}
		}

		assertTrue(!validationTPE);

		// ****************************************************Validar conexion
		// FCWMLTAEQA
		// *************************************************************************

		step("Establecer conexion con la base de datos: FCWMLTAEQA_MTY");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAQ.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexion a la base de datos FCWMLTAEQA_MTY fue exitosa.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMLTAEQA);

		/*
		 * Paso 9
		 *****************************************************************************************/

		step("Ejecutar la siguiente consulta en la base de datos *FCWMLTAEQ*  "
				+ "para validar que no se encuentren registros de error de la PE1:");

		String errorQuery = String.format(tdcErrorQuery);
		System.out.println(GlobalVariables.DB_HOST_FCWMLTAEQA);
		System.out.println(errorQuery);

		SQLResult result2 = executeQuery(dbFCW, errorQuery);

		boolean error = result2.isEmpty();

		if (error) {
			testCase.addTextEvidenceCurrentStep(errorQuery);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("No muestra ningun registro de error.");

		}
		testCase.addQueryEvidenceCurrentStep(result2);

		assertTrue(error, "Se encontraron errores en la consulta");

		/*
		 * Paso 10
		 *****************************************************************************************/
		step("Ejecutar la siguiente consulta  en la BD *FCWMLTAEQA* para verificar que no  viajo por un canal seguro.");
		System.out.println(GlobalVariables.DB_HOST_FCWMLTAEQA);
		System.out.println(LogSecSess);
		
		String queryStep10 = String.format(LogSecSess, folio);

		SQLResult resultSec = executeQuery(dbFCW, queryStep10);

		boolean resultSecVal = resultSec.isEmpty();

		if (!resultSecVal) {
			testCase.addTextEvidenceCurrentStep(LogSecSess);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"Se visualiza los registros de las invocaciones realizadas a la PE1  de la  transaccion  de TAE en la BD *FCWMLTAEQA* , por lo tanto no viajo por un canal seguro");

		}
		testCase.addQueryEvidenceCurrentStep(resultSec);

		assertTrue(!resultSecVal, "No se encontraron registros en la consulta");

	}
	
	int contador = 0;
	public void step(String step){
		contador++;
		System.out.println("\r\nStep "+contador+"-"+step);
		addStep(step);
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Prueba de regresion para comprobar la no afectacion en la funcionalidad principal "
				+ "de recarga TAE de la interface FEMSA_PE1 al ser migrada de webmethods v10.5 "
				+ "a webmethods 10.11 y del sistema operativo Solaris(Unix) a Redhat 8.5 (Linux X86).";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo Automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_008_PE1_WMx86_TransaccionExitosaRecarga";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return "*Contar con un simulador de proveedor de TAE que responda las transacciones de forma exitosa.\r\n"
				+ "*Contar con acceso a las base de datos de FCTAEQA_MTY, FCTAEQA_QRO, FCWMLTAEQA_MTY y FCWMLTAEQA_QRO.\r\n"
				+ "*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA.";
	}
	
	
}
