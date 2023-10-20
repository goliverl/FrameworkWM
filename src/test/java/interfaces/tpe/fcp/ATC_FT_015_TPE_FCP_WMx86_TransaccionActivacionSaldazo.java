package interfaces.tpe.fcp;


import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


import java.util.HashMap;

import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import modelo.BaseExecution;
import om.FCP_WMx86;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.webmethods.ReadRequest;

public class ATC_FT_015_TPE_FCP_WMx86_TransaccionActivacionSaldazo extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_015_TPE_FCP_WMx86_TransaccionActivacionSaldazo_test(HashMap<String, String> data) throws Exception {

		/**
		 * Proyecto: Servicios Electronicos (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-050-C1 Transaccion exitosa de Activacion de tarjeta Saldazo.
		 * @author 
		 * @date 
		 */
		
		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbTPEMTY = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE,
				GlobalVariables.DB_PASSWORD_FCTPE);
		SQLUtil dbTPEQRO = new SQLUtil(GlobalVariables.DB_HOST_TPE_LOT, GlobalVariables.DB_USER_TPE_LOT,
				GlobalVariables.DB_PASSWORD_TPE_LOT);
		SQLUtil dbFCS = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA,
				GlobalVariables.DB_PASSWORD_FCSWQA);
		SQLUtil dbFCSWQA_QRO = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO, GlobalVariables.DB_USER_FCSWQA_QRO,
				GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		SQLUtil dbFCWMLTAEQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_QA,
				GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);

		FCP_WMx86 FCPUtil = new FCP_WMx86(data, testCase, dbTPEMTY);

		/*
		 * Variables
		 *********************************************************************/

		String ValidTransGeneral = "SELECT APPLICATION,ENTITY,OPERATION,SOURCE,FOLIO,CREATION_DATE,WM_CODE,PAN,PROC_CODE,AUTH_ID_RES,RESP_CODE,ACK,ATT1"
				+ ",IS_NAME,WM_DESC " + "	FROM TPEUSER.TPE_FR_TRANSACTION  "
				+ "	WHERE CREATION_DATE >= trunc(sysdate) " + "	AND  APPLICATION = 'FCP' "
				+ "	ORDER BY CREATION_DATE DESC";

		String ValidTrans1 = "Select * from(SELECT APPLICATION,ENTITY,OPERATION,SOURCE,FOLIO,CREATION_DATE,WM_CODE "
				+ "	FROM TPEUSER.TPE_FR_TRANSACTION  " + "	WHERE CREATION_DATE >= trunc(sysdate) "
				+ "	AND  APPLICATION = 'FCP' " + "	ORDER BY CREATION_DATE DESC) where rownum <= 3";

		String ValidTrans2 = "Select * from(SELECT PAN,PROC_CODE,AUTH_ID_RES,RESP_CODE,ACK,ATT1 "
				+ "FROM TPEUSER.TPE_FR_TRANSACTION  " + "WHERE CREATION_DATE >= trunc(sysdate)  "
				+ "AND  APPLICATION = 'FCP'  " + "ORDER BY CREATION_DATE DESC) where rownum <= 3";

		String ValidTrans3 = "Select * from(SELECT IS_NAME,WM_DESC " + "FROM TPEUSER.TPE_FR_TRANSACTION  "
				+ "WHERE CREATION_DATE >= trunc(sysdate)  " + "AND  APPLICATION = 'FCP'  "
				+ "ORDER BY CREATION_DATE DESC) where rownum <= 3";

		String ValidOperation = "SELECT CARD_CODE, STATUS, OPERATION, CREATION_DATE,LOG_ID "
				+ "FROM TPEUSER.TPE_FCP_CARD_LOG  " + "WHERE CREATION_DATE >= trunc(sysdate)  "
				+ "AND OPERATION= 'ACT'  " + "AND card_code='%s' " + "ORDER BY CREATION_DATE DESC";

		String tdcTLOGQuery = " SELECT CREATION_DATE, FOLIO, MTI, AMOUNT, PAN, AUTH_ID_RES "
				+ " FROM SWUSER.TPE_SW_TLOG" + " where " + " MTI='0200'" + " and folio = %s" + " and RESP_CODE='00'"
				+ " AND CREATION_DATE >=TRUNC(SYSDATE)";

		String tdcTLOGQueryPart2 = " SELECT SW_CODE, COUNTER, IS_NAME, APPLICATION, ENTITY" + " FROM SWUSER.TPE_SW_TLOG"
				+ " where " + " MTI='0200'" + " and folio = %s" + " and RESP_CODE='00'"
				+ " AND CREATION_DATE >=TRUNC(SYSDATE)";

		String tdcTLOGQueryPart3 = " SELECT PLAZA, TIENDA, CAJA, ACQUIRER, RESP_CODE, POS_ENTRY_MODE, PROC_CODE"
				+ " FROM SWUSER.TPE_SW_TLOG" + " where " + " MTI='0200'" + " and folio = %s" + " and RESP_CODE='00'"
				+ " AND CREATION_DATE >=TRUNC(SYSDATE)";

		String tdcTLOGQueryGeneral = " SELECT CREATION_DATE, FOLIO, MTI, AMOUNT, PAN, AUTH_ID_RES, SW_CODE, COUNTER, IS_NAME, APPLICATION, ENTITY, PLAZA, TIENDA, CAJA, ACQUIRER, RESP_CODE, POS_ENTRY_MODE, PROC_CODE"
				+ " FROM SWUSER.TPE_SW_TLOG" + " where " + " MTI='0200'" + " and folio = %s" + " and RESP_CODE='00'"
				+ " AND CREATION_DATE >=TRUNC(SYSDATE)";

		String errorTransaccion = "SELECT ERROR_ID,FOLIO,ERROR_DATE,SEVERITY,ERROR_TYPE,ERROR_CODE "
				+ "FROM WMLOG.WM_LOG_ERROR_TPE " + "WHERE TPE_TYPE like '%s' " + "AND FOLIO='%s' "
				+ "AND ERROR_DATE>=trunc(sysdate) " + "ORDER BY ERROR_DATE DESC";// FCWMLTAEQA.
		
		
		String LogSecSess = "SELECT  APPLICATION,OPERATION,SOURCE,FOLIO,PLAZA,TIENDA,CREATION_DATE "
				+ "FROM  WMLOG.SECURITY_SESSION_LOG " + "WHERE CREATION_DATE >=TRUNC(SYSDATE) "
				+ "AND APPLICATION like '%s' " + "ORDER BY CREATION_DATE DESC";
		
		testCase.setProject_Name("POC WMx86");
		String folio;
		String CreationDate;

		String wmCodeFolio = data.get("wmCodeFolio");
		String wmCodeToValidateAuth = data.get("wmCodeAuth");
		String wmCodeToValidateAck = data.get("wmCodeAck");

		String wmCodeRequest;
		SoftAssert softAssert = new SoftAssert();
		// ************************************************************************************************/
		// Paso 1
		addStep("Ejecutar el servicio de solicitud de folio para una activación saldazo");
		System.out.println("Paso 1: ");
		String respuestaFolio = FCPUtil.generacionFolioTransaccion3();

		System.out.println("Esta es la respuesta:\n" + respuestaFolio + "\nFIN");

		folio = getFolioXml(respuestaFolio);
		System.out.println("Folio: " + folio);

		wmCodeRequest = getWmCodeXml(respuestaFolio);
		System.out.println("WMcode: " + wmCodeRequest);
		
		CreationDate =getCreationdate(respuestaFolio);
		System.out.println("CreationDate: " + CreationDate);
		
		if(wmCodeFolio.equals(wmCodeRequest)) {	
			testCase.addBoldTextEvidenceCurrentStep("WmCode iguales: \n"
					+"Actual: "+wmCodeRequest+" Esperado: "+wmCodeFolio);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("WmCode distintos: \n"
					+"Actual: "+wmCodeRequest+" Esperado: "+wmCodeFolio);
		}

		softAssert.assertEquals(wmCodeRequest,wmCodeFolio ,"Paso 1: Se obtuvo WmCode diferente al esperado ");

		// ************************************************************************************************/
		// Paso 2
		addStep("Ejecutar en el navegador el servicio para realizar la solicitud de autorización de FCP "
				+ "para la activación de tarjeta saldazo");
		System.out.println("Paso 2: ");
		
		String respuestaAut = FCPUtil.generarAutorizacion(folio,CreationDate);

		System.out.println("\n" + respuestaAut + "\n");

		wmCodeRequest = getWmCodeXml(respuestaAut);
		System.out.println("WMcode: " + wmCodeRequest);

		if(wmCodeRequest.equals(wmCodeToValidateAuth)) {	
			testCase.addBoldTextEvidenceCurrentStep("WmCode iguales: \n"
					+"Actual: "+wmCodeRequest+" Esperado: "+wmCodeToValidateAuth);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("WmCode distintos: \n"
					+"Actual: "+wmCodeRequest+" Esperado: "+wmCodeToValidateAuth);	
		}

		softAssert.assertEquals(wmCodeRequest,wmCodeToValidateAuth,"Paso 2: Se obtuvo WmCode diferente al esperado ");

		/*
		 * *****************************************************************************
		 * ****************** Paso 3
		 ***********************************************************************************************/

		addStep("Ejecutar el servicio  de la FCP  para confirmar el ACK de la transacción activación de tarjeta Saldazo");
		System.out.println("Paso 3: ");
		
		String respuestaAck = FCPUtil.generacionACK(folio);
		System.out.println("\n" + respuestaAck + "\n");

		folio = getFolioXml(respuestaAck);
		System.out.println("Folio: " + folio);

		wmCodeRequest = getWmCodeXml(respuestaAck);
		System.out.println("WMcode: " + wmCodeRequest);

		// VALIDAR QUE EL WMCODE DE LA RESPUESTA SEA IGUAL A 101
		if(wmCodeRequest.equals(wmCodeToValidateAck)) {	
			testCase.addBoldTextEvidenceCurrentStep("WmCode iguales: \n"
					+"Actual: "+wmCodeRequest+" Esperado: "+wmCodeToValidateAck);
		}else {
			testCase.addBoldTextEvidenceCurrentStep("WmCode distintos: \n"
					+"Actual: "+wmCodeRequest+" Esperado: "+wmCodeToValidateAck);	
		}

		softAssert.assertEquals(wmCodeRequest,wmCodeToValidateAck,"Paso 3: Se obtuvo WmCode diferente al esperado ");
		softAssert.assertAll();
		/*
		 * Paso 4
		 *****************************************************************************************/
		addStep("Establecer conexión con la base de datos: FCTPEQA_Site1 ");
		System.out.println("Paso 4: ");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTPEQA.FEMCOM.NET ");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexión a la base de datos FCTAEQA_MTY fue exitosa.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCTPE);

		//addStep("Establecer conexión con la base de datos: FCTPEQA_Site2 ");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTPEQA.FEMCOM.NET ");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexión a la base de datos FCTAEQA_QRO fue exitosa.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_TPE_LOT);

		/*
		 * Paso 5
		 *****************************************************************************************/
		addStep("Ejecutar consulta para validar que se registró correctamente la transacción activación de tarjeta en FCP en site 1 MTY: ");
		System.out.println("Paso 5: ");
		
		// Se forma el query a utilizar
		System.out.println(GlobalVariables.DB_HOST_FCTPE);
		System.out.println(ValidTrans1);
		SQLResult result1 = executeQuery(dbTPEMTY, ValidTrans1);
		SQLResult result2 = executeQuery(dbTPEMTY, ValidTrans2);
		SQLResult result3 = executeQuery(dbTPEMTY, ValidTrans3);

		String WM_CODE = "";
		String ATT1 = "";
		boolean transaction = result1.isEmpty();
		boolean validWMCode = false;
		if (!transaction) {
			WM_CODE = result1.getData(0, "WM_CODE");
			ATT1 = result2.getData(0, "ATT1");
			validWMCode = WM_CODE.equals("101");
			if (validWMCode) {
				testCase.addTextEvidenceCurrentStep("El dato WmCode es correcto: " + WM_CODE);
			}

		}
		testCase.addTextEvidenceCurrentStep("Se ejecuta la conssulta: \n" + ValidTransGeneral);
		testCase.addQueryEvidenceCurrentStep(result1);
		testCase.addQueryEvidenceCurrentStep(result2);
		testCase.addQueryEvidenceCurrentStep(result3);

//	***************************Consulta en SIte 2 QRO*************************************************	
		testCase.addBoldTextEvidenceCurrentStep("Ejecutar consulta para validar que se registró correctamente "
				+ "la transacción activación de tarjeta en FCP en site 2 QRO: " + GlobalVariables.DB_HOST_TPE_LOT);

		// Se forma el query a utilizar
		System.out.println(GlobalVariables.DB_HOST_TPE_LOT);
		System.out.println(ValidTrans1);
		SQLResult resul1 = executeQuery(dbTPEQRO, ValidTrans1);
		SQLResult resul2 = executeQuery(dbTPEQRO, ValidTrans2);
		SQLResult resul3 = executeQuery(dbTPEQRO, ValidTrans3);

		testCase.addTextEvidenceCurrentStep(
				"Se ejecuta la conssulta: \n" + ValidTransGeneral + " en host: " + GlobalVariables.DB_HOST_FCTPE);
		testCase.addQueryEvidenceCurrentStep(resul1);
		testCase.addQueryEvidenceCurrentStep(resul2);
		testCase.addQueryEvidenceCurrentStep(resul3);

		assertFalse(transaction, "No se encontraron datos en consulta");
		assertTrue(validWMCode, "EL WMcode de la cosulta No fue 101");

		/*
		 * Paso 6
		 *****************************************************************************************/
		addStep(" Ejecutar la siguiente consulta para validar que  el registro de la tarjeta activa haya cambiado a OPERATION= 'ACT' en site 1 MTY.  ");
		System.out.println("Paso 6: ");
		
		System.out.println(GlobalVariables.DB_HOST_FCTPE);
		String ValidFormat = String.format(ValidOperation, ATT1);
		System.out.println(ValidFormat);

		SQLResult ValidExec = executeQuery(dbTPEMTY, ValidFormat);

		boolean ValidTAEVal = ValidExec.isEmpty();

		if (!ValidTAEVal) {
			testCase.addTextEvidenceCurrentStep("Se encontraron resultados recientes con operacion ACT");
		}
		testCase.addQueryEvidenceCurrentStep(ValidExec);

//		***************************COnsulta site 2 FCTPE_QRO******************************************************
		System.out.println(GlobalVariables.DB_HOST_TPE_LOT);
		SQLResult ValidExec2 = executeQuery(dbTPEQRO, ValidFormat);
		testCase.addQueryEvidenceCurrentStep(ValidExec2);

		assertFalse(ValidTAEVal, "No se encontraron resultados recientes con operacion ACT");
		// ****************************************************Validar conexion
		// FCSWQA*************************************************************************

		addStep("Establecer conexión con la base de datos: FCSWQA Site 1 MTY");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCSWQA_MTY");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexión a la base de datos FCSWQA_MTY fue exitosa.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCSWQA);

		addStep("Establecer conexión con la base de datos: FCSWQA Site 2 QRO");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCSWQA_QRO");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexión a la base de datos FCSWQA_QRO fue exitosa.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCSWQA_QRO);

		/*
		 * Paso 7
		 *****************************************************************************************/

		addStep("Validar en la base de datos del **FCSWQA**, que se encuentre el registro de la transacción");

		String tempTPEQuery = String.format(tdcTLOGQuery, folio);
		String tempTPEQueryPart2 = String.format(tdcTLOGQueryPart2, folio);
		String tempTPEQueryPart3 = String.format(tdcTLOGQueryPart3, folio);
		String tempTPEQueryGeneral = String.format(tdcTLOGQueryGeneral, folio);

		SQLResult resultt3 = executeQuery(dbFCS, tempTPEQuery);// parte 1
		SQLResult result3Part2 = executeQuery(dbFCS, tempTPEQueryPart2);// parte 2
		SQLResult result3Part3 = executeQuery(dbFCS, tempTPEQueryPart3);// parte 3

		System.out.print(resultt3);
		boolean validationTPE = resultt3.isEmpty();

		if (!validationTPE) {
			testCase.addTextEvidenceCurrentStep(tempTPEQueryGeneral);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"Se muestra el registro de la transacción  de forma exitosa.");
		}
		testCase.addQueryEvidenceCurrentStep(resultt3);
		testCase.addQueryEvidenceCurrentStep(result3Part2);
		testCase.addQueryEvidenceCurrentStep(result3Part3);

		assertTrue(!validationTPE, "No se encontraron los resultados esperados");

		// ****************************************************Validar conexion
		// FCWMLTAEQA
		// *************************************************************************

		addStep("Establecer conexión con la base de datos: FCWMLTAEQA_MTY");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAQ.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexión a la base de datos FCWMLTAEQA_MTY fue exitosa.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMLTAEQA);

		/*
		 * Paso 9
		 *****************************************************************************************/
		addStep("Ejecutar la siguiente consulta para validar que no se registró error en la transacción:");
		System.out.println(GlobalVariables.DB_HOST_FCWMLTAEQA);

		String formatErrorTransaccion = String.format(errorTransaccion,"%FCP%", folio);
		System.out.println(formatErrorTransaccion);

		SQLResult ExecuteErrorTransaccion = dbFCWMLTAEQA.executeQuery(formatErrorTransaccion);

		boolean ValidaRegistroErrorBoolean = ExecuteErrorTransaccion.isEmpty();

		System.out.println(ValidaRegistroErrorBoolean);

		if (ValidaRegistroErrorBoolean) {
			testCase.addTextEvidenceCurrentStep(formatErrorTransaccion);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"No se muestra ningún registro de error relacionado con la transacción.");
		}
		testCase.addQueryEvidenceCurrentStep(ExecuteErrorTransaccion);
		assertTrue(ValidaRegistroErrorBoolean, "hay registros de error en la tabla WMLOG.WM_LOG_ERROR_TPE ");
		
		/* Paso 10 *****************************************************************************************/
		addStep("Ejecutar  la siguiente consulta  en la BD   *FCWMLTAEQA* para verificar que no  viajo por un canal seguro.");
		System.out.println(GlobalVariables.DB_HOST_FCWMLTAEQA);
		String formatSec = String.format(LogSecSess,"%FCP%");
		System.out.println(formatSec);
		SQLResult resultSec = executeQuery(dbFCWMLTAEQA, formatSec);
		
		boolean resultSecVal = resultSec.isEmpty();
		
		if (!resultSecVal) {
			testCase.addTextEvidenceCurrentStep(LogSecSess);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("Se visualiza los registros de las invocaciones realizadas de la  transacción  en la BD *FCWMLTAEQA* , por lo tanto no viajo por un canal seguro");
			
			
		}
		testCase.addQueryEvidenceCurrentStep(resultSec);
		
		assertFalse(resultSecVal,"No se encontraron registros en la consulta");

	}

	public static String getFolioXml(String xmlResponse) {
		Document runGetFolioRequestDoc = ReadRequest.convertStringToXMLDocument(xmlResponse);
		Element eElement = (Element) runGetFolioRequestDoc.getElementsByTagName("header").item(0);

		return eElement.getAttribute("folio");
	}
	
	public static String getCreationdate(String xmlResponse) {
		Document runGetFolioRequestDoc = ReadRequest.convertStringToXMLDocument(xmlResponse);
		Element eElement = (Element) runGetFolioRequestDoc.getElementsByTagName("header").item(0);

		return eElement.getAttribute("creationDate");
	}

	public static String getWmCodeXml(String xmlResponse) {
		Document runGetFolioRequestDoc = ReadRequest.convertStringToXMLDocument(xmlResponse);
		Element eElement = (Element) runGetFolioRequestDoc.getElementsByTagName("wmCode").item(0);

		return eElement.getAttribute("value");
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_015_TPE_FCP_WMx86_TransaccionActivacionSaldazo_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. MTC-FT-050 Transaccion exitosa de Activacion de tarjeta Saldazo";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Sergio Robles Ramos";
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
		return "*Contar con un simulador de proveedor de Banco Banamex que responda las transacciones de forma exitosa.\r\n"
				+ "*Contar con acceso a las base de datos de FCTPEQA_MTY, FCTPEQA_QRO, FCWMLTAEQA_MTY y FCWMLTAEQA_QRO.\r\n"
				+ "*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA.";
	}
}
