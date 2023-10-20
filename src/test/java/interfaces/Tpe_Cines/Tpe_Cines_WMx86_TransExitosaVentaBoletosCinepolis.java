package interfaces.Tpe_Cines;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import modelo.BaseExecution;
import om.FCP_WMx86;
import om.Tpe_Cines_WMx86;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.webmethods.ReadRequest;

public class Tpe_Cines_WMx86_TransExitosaVentaBoletosCinepolis extends BaseExecution {

	/**
	 * TPE_CINES: MTC-FT-055 Transaccion exitosa de venta de boletos de cines Cinepolis.
	 * Desc: Prueba de regresión para comprobar la no afectación en la funcionalidad 
	 * principal de transaccionalidad de la interface TPE_CINES y TPE_FR al ser migradas 
	 * de WM9.9 a WM10.5
	 * 
	 * Mtto:
	 * @author JoseO@Hexaware.com
	 * @Date 02/14/2023
	 */
	
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_08_TPE_CINES_WMx86_TransExitosaVentaBoletosCinepolis_test(HashMap<String, String> data) throws Exception {

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
		SQLUtil dbFCWMLTAEQAQRO = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO, GlobalVariables.DB_USER_FCWMLTAEQA_QA,
				GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		
		

		Tpe_Cines_WMx86 CinesUtil = new Tpe_Cines_WMx86(data, testCase, dbTPEMTY);

		/*
		 * Variables
		 *********************************************************************/

		String ValidTransGeneral = "SELECT APPLICATION,ENTITY,OPERATION,SOURCE,FOLIO,CREATION_DATE,WM_CODE,PAN,PROC_CODE,AUTH_ID_RES,RESP_CODE,ACK,ATT1"
				+ ",IS_NAME,WM_DESC " + "	FROM TPEUSER.TPE_FR_TRANSACTION  "
				+ "	WHERE CREATION_DATE >= trunc(sysdate) " + "	AND  APPLICATION = 'CINES' "
				+ "	ORDER BY CREATION_DATE DESC";

		String ValidTrans1 = "select * from(SELECT APPLICATION,ENTITY,OPERATION,SOURCE,FOLIO,CREATION_DATE,WM_CODE "
				+ "	FROM TPEUSER.TPE_FR_TRANSACTION  " + "	WHERE CREATION_DATE >= trunc(sysdate) "
				+ "	AND  APPLICATION = 'CINES' " + "	ORDER BY CREATION_DATE DESC) where rownum <=4";

		String ValidTrans2 = "select * from(SELECT PAN,PROC_CODE,AUTH_ID_RES,RESP_CODE,ACK,ATT1 "
				+ "FROM TPEUSER.TPE_FR_TRANSACTION  " + "WHERE CREATION_DATE >= trunc(sysdate)  "
				+ "AND  APPLICATION = 'CINES'  " + "ORDER BY CREATION_DATE DESC) where rownum <=4";

		String ValidTrans3 = "select * from(SELECT IS_NAME,WM_DESC " + "FROM TPEUSER.TPE_FR_TRANSACTION  "
				+ "WHERE CREATION_DATE >= trunc(sysdate)  " + "AND  APPLICATION = 'CINES'  "
				+ "ORDER BY CREATION_DATE DESC) where rownum <=4";

	
		String errorTransaccion = "select * from(SELECT ERROR_ID,FOLIO,ERROR_DATE,SEVERITY,ERROR_TYPE,ERROR_CODE "
				+ "FROM WMLOG.WM_LOG_ERROR_TPE " + "WHERE TPE_TYPE like '%s' "
//						+ "OR TPE_TYPE like  '%s' " 
				+ "AND ERROR_DATE BETWEEN trunc(sysdate-1) and trunc(sysdate+1) " + "ORDER BY ERROR_DATE DESC) where rownum <=4";// FCWMLTAEQA.
		
	
		String LogSecSess = "select * from(SELECT  APPLICATION,OPERATION,SOURCE,FOLIO,PLAZA,TIENDA,CREATION_DATE "
				+ "FROM  WMLOG.SECURITY_SESSION_LOG " + "WHERE CREATION_DATE >=TRUNC(SYSDATE) "
				+ "AND APPLICATION like '%s' " + "ORDER BY CREATION_DATE DESC) where rownum <=4";
		
		
		testCase.setProject_Name("POC WMx86");
		String folio;

		String wmCodeCons = data.get("wmCodeConsult");
		String wmCodeConsCom = data.get("wmCodeConsCom");
		String wmCodeResFol = data.get("wmCodeResFol");
		String wmCodeResSolAut = data.get("wmCodeResSolAut");
		String wmCodeResConfACK= data.get("wmCodeResConfACK");

		String wmCodeRequest;
		// ************************************************************************************************/
		// Paso 1
		addStep("Ejecutar el servicio de la Consulta de Ciudades para la TPE_CINES del server QA8");

		String respuestaConsulta = CinesUtil.ConsultaCiudad();
		
		wmCodeRequest = getWmCodeXml(respuestaConsulta);
		System.out.println("WMcode: " + wmCodeRequest);

		// VALIDAR QUE EL WMCODE DE LA RESPUESTA SEA IGUAL A 101

		boolean validationRequestWmCode = wmCodeRequest.equals(wmCodeCons);

		System.out.println("\n" + validationRequestWmCode + " - wmCode request: " + wmCodeRequest + "\n");

		assertTrue(validationRequestWmCode, "El Código wmCode no es el esperado: " + wmCodeCons);
//
//		// ************************************************************************************************/
//		// Paso 2

		addStep("Ejecutar el servicio de la Consulta de Complejos para la TPE_CINES del server QA8 ");

		String respuestaConsComp = CinesUtil.ConsultaComplejos();

		System.out.println("\n" + respuestaConsComp + "\n");

		wmCodeRequest = getWmCodeXml(respuestaConsComp);
		System.out.println("WMcode: " + wmCodeRequest);

		// VALIDAR QUE EL WMCODE DE LA RESPUESTA SEA IGUAL A 101

		boolean validationRequestConsComp = wmCodeRequest.equals(wmCodeConsCom);

		System.out.println("\n" + validationRequestConsComp + " - wmCode request: " + wmCodeRequest + "\n");

		assertTrue(validationRequestConsComp, "El Código wmCode no es el esperado: " + wmCodeConsCom);

		// ************************************************************************************************/
		// Paso 3

		addStep("Ejecutar el servicio de solicitud de folio de la TPE_CINES del server QA8 ");

		String respuestaSoliFol = CinesUtil.SolicitaFolio();

		System.out.println("\n" + respuestaSoliFol + "\n");
		
		folio = getFolioXml(respuestaSoliFol);
		System.out.println("Folio: " + folio);
		wmCodeRequest = getWmCodeXml(respuestaSoliFol);
		System.out.println("WMcode: " + wmCodeRequest);

		boolean validationRequestSoliFol = wmCodeRequest.equals(wmCodeResFol);

		System.out.println("\n" + validationRequestSoliFol + " - wmCode request: " + wmCodeRequest + "\n");

		assertTrue(validationRequestSoliFol, "El Código wmCode no es el esperado: " + wmCodeResFol);

		/*
		 * *****************************************************************************
		 * ****************** Paso 4
		 ***********************************************************************************************/


		addStep("Ejecutar el servicio de solicitud de Autorización de la interface TPE_CINES del servidor QA8 ");

		String respuestaSoliAut = CinesUtil.SolicitaAutorizacion(folio);

		System.out.println("\n" + respuestaSoliAut + "\n");
		
		wmCodeRequest = getWmCodeXml(respuestaSoliAut);
		System.out.println("WMcode: " + wmCodeRequest);

		boolean validationRequestSoliAut = wmCodeRequest.equals(wmCodeResSolAut);

		System.out.println("\n" + validationRequestSoliAut + " - wmCode request: " + wmCodeRequest + "\n");

		assertTrue(validationRequestSoliAut, "El Código wmCode no es el esperado: " + wmCodeResSolAut);
//		
		
		
		/*
		 * *****************************************************************************
		 * ****************** Paso 5
		 ***********************************************************************************************/
		addStep(" Ejecutar el servicio de Confirmación ACK de la TPE_CINES del servidor QA8 ");

		String respuestaConfACK= CinesUtil.ConfirmacionACK(folio);

		System.out.println("\n" + respuestaConfACK + "\n");
		
		wmCodeRequest = getWmCodeXml(respuestaConfACK);
		System.out.println("WMcode: " + wmCodeRequest);

		boolean validationRequestConfACK = wmCodeRequest.equals(wmCodeResConfACK);

		System.out.println("\n" + validationRequestConfACK + " - wmCode request: " + wmCodeRequest + "\n");

		assertTrue(validationRequestConfACK, "El Código wmCode no es el esperado: " + wmCodeResConfACK);

		/*
		 * Paso 6
		 *****************************************************************************************/
		addStep("Establecer conexión con la base de datos: FCTPEQA_Site1 ");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTPEQA.FEMCOM.NET ");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexión a la base de datos FCTAEQA_MTY fue exitosa.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCTPE);

		addStep("Establecer conexión con la base de datos: FCTPEQA_Site2 ");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTPEQA.FEMCOM.NET ");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexión a la base de datos FCTAEQA_QRO fue exitosa.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_TPE_LOT);

		/*
		 * Paso 7
		 *****************************************************************************************/
		addStep("Ejecutar consulta para validar que se registró correctamente la transacción  en site 1 MTY: ");

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
			System.out.println(transaction+" WMCODECONS: "+WM_CODE);
			validWMCode = WM_CODE.equals(data.get("wmCodeConsult"));
			if (validWMCode) {
				testCase.addTextEvidenceCurrentStep("El dato WmCode es correcto: " + WM_CODE);
			}

		}
		testCase.addTextEvidenceCurrentStep("Se ejecuta la conssulta: \n" + ValidTransGeneral);
		testCase.addQueryEvidenceCurrentStep(result1);
		testCase.addQueryEvidenceCurrentStep(result2);
		testCase.addQueryEvidenceCurrentStep(result3);

////	***************************Consulta en SIte 2 QRO*************************************************	
		testCase.addBoldTextEvidenceCurrentStep("Ejecutar la siguiente consulta en la BD * FCTPEQA* en site 2 para validar los resgistros de transaccion. "
				 + GlobalVariables.DB_HOST_TPE_LOT);

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
		 * Paso 8
		 *****************************************************************************************/
		// ****************************************************Validar conexion FCWMLTAEQA
		// **************************************************************************************

				addStep("Establecer conexión con la base de datos: FCWMLTAEQA_MTY");
				testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAQ.FEMCOM.NET");
				testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
				testCase.addBoldTextEvidenceCurrentStep("La conexión a la base de datos FCWMLTAEQA_MTY fue exitosa.");
				testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMLTAEQA);
				
				addStep("Establecer conexión con la base de datos: FCWMLTAEQA_QRO");
				testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAQ.FEMCOM.NET");
				testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
				testCase.addBoldTextEvidenceCurrentStep("La conexión a la base de datos FCWMLTAEQA_QRO fue exitosa.");
				testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMLTAEQA_QRO);
				
//		**********************Paso 9*******************************************************************************		
		
				addStep(" Ejecutar la siguiente consulta en la base de datos *FCWMLTAEQA_MTY* buscando que no se encuentren "
						+ "registros de error de compra de boletos de cine ");
				
				System.out.println(GlobalVariables.DB_HOST_FCWMLTAEQA);

				String formatErrorTransaccion = String.format(errorTransaccion, "%CINES%");
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
				
				
//				***************************Consulta en SIte 2 QRO*************************************************	
				testCase.addBoldTextEvidenceCurrentStep(" Ejecutar la  consulta en la base de datos *FCWMLTAEQA_QRO* buscando que no se encuentren "
						+ "registros de error de compra de boletos de cine en Host: "
						 + GlobalVariables.DB_HOST_FCWMLTAEQA_QRO);
		
				// Se forma el query a utilizar
				System.out.println(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO);
				System.out.println(formatErrorTransaccion);
				SQLResult resqro = executeQuery(dbFCWMLTAEQAQRO, formatErrorTransaccion);
				
		
				testCase.addTextEvidenceCurrentStep(
						"Se ejecuta la conssulta: \n" + formatErrorTransaccion + " en host: " + GlobalVariables.DB_HOST_FCWMLTAEQA_QRO);
				testCase.addQueryEvidenceCurrentStep(resqro);
				
				assertTrue(ValidaRegistroErrorBoolean, "hay registros de error en la tabla WMLOG.WM_LOG_ERROR_TPE ");


		// *********Paso 10*******************************************
		addStep("Ejecutar la siguiente consulta en la BD *FCWMLTAEQA* en site 1 para verificar  que la transacción no  viajo por un canal seguro.");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLTAEQA);
		String formatSec = String.format(LogSecSess, "%CINES%");
		System.out.println(formatSec);
		SQLResult resultSec = executeQuery(dbFCWMLTAEQA, formatSec);

		boolean resultSecVal = resultSec.isEmpty();

		if (!resultSecVal) {
			testCase.addTextEvidenceCurrentStep(LogSecSess);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"Se visualiza los registros de las invocaciones realizadas de la  transacción  en la BD *FCWMLTAEQA* , por lo tanto no viajo por un canal seguro");

		}
		testCase.addQueryEvidenceCurrentStep(resultSec);

		assertFalse(resultSecVal, "No se encontraron registros en la consulta");
		
		
		

	}

	public static String getFolioXml(String xmlResponse) {
		Document runGetFolioRequestDoc = ReadRequest.convertStringToXMLDocument(xmlResponse);
		Element eElement = (Element) runGetFolioRequestDoc.getElementsByTagName("header").item(0);

		return eElement.getAttribute("folio");
	}

	public static String getWmCodeXml(String xmlResponse) {
		Document runGetFolioRequestDoc = ReadRequest.convertStringToXMLDocument(xmlResponse);
		Element eElement = (Element) runGetFolioRequestDoc.getElementsByTagName("wmCode").item(0);

		return eElement.getAttribute("value");
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_08_TPE_CINES_WMx86_TransExitosaVentaBoletosCinepolis_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. MTC-FT-055 Transacción exitosa de venta de boletos de cines Cinepolis.";
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
		return "*Contar con un simulador de proveedor de CINEPOLIS que responda las transacciones de forma exitosa.\r\n"
				+ "*Contar con acceso a las base de datos de FCTPEQA_MTY, FCTPEQA_QRO, FCWMLTAEQA_MTY y FCWMLTAEQA_QRO.\r\n"
				+ "*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA.";
	}
}
