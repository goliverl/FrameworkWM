package interfaces.tpe.fcp;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import modelo.BaseExecution;
import om.FCP_WMx86;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.webmethods.ReadRequest;

public class ATC_FT_017_TPE_FCP_WMx86_TransaccionRedencionPromSinergia extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_017_TPE_FCP_WMx86_TransaccionRedencionPromSinergia_test(HashMap<String, String> data) throws Exception {

		/**
		 * Proyecto: Servicios Electronicos (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-052-C1 Transaccion exitosa de Redención de promocion Sinergia.
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


		String errorTransaccion = "SELECT ERROR_ID,FOLIO,ERROR_DATE,SEVERITY,ERROR_TYPE,ERROR_CODE "
				+ "FROM WMLOG.WM_LOG_ERROR_TPE " + "WHERE TPE_TYPE like '%s' " + "AND FOLIO='%s' "
				+ "AND ERROR_DATE>=trunc(sysdate) " + "ORDER BY ERROR_DATE DESC";// FCWMLTAEQA.
		
		String LogSecSess = "SELECT  APPLICATION,OPERATION,SOURCE,FOLIO,PLAZA,TIENDA,CREATION_DATE "
				+ "FROM  WMLOG.SECURITY_SESSION_LOG " + "WHERE CREATION_DATE >=TRUNC(SYSDATE) "
				+ "AND APPLICATION like '%s' " + "ORDER BY CREATION_DATE DESC";
		
		String folio;

		testCase.setProject_Name("POC WMx86");
		String wmCodeFolio = data.get("wmCodeFolio");
		String wmCodeToValidateAuth = data.get("wmCodeAuth");

		String wmCodeRequest;
		String CreationDate;
		// ************************************************************************************************/
		// Paso 1
		addStep("Ejecutar el servicio para la solicitud de consulta de cupón de la interface TPE_FCP:");
		System.out.println("Paso 1: ");
		String respuestaFolio = FCPUtil.Consultacupon();

		System.out.println("Esta es la respuesta:\n" + respuestaFolio + "\nFIN");

		folio = getFolioXml(respuestaFolio);
		System.out.println("Folio: " + folio);

		wmCodeRequest = getWmCodeXml(respuestaFolio);
		System.out.println("WMcode: " + wmCodeRequest);
		
		CreationDate =getCreationdate(respuestaFolio);
		System.out.println("CreationDate: " + CreationDate);

		boolean validationResponseFolio = true;

		if (respuestaFolio != null) {
			validationResponseFolio = false;
		}

		boolean validationRequestFolio = wmCodeRequest.equals(wmCodeFolio);

		System.out.println("\n" + validationRequestFolio + " - wmCode request: " + wmCodeRequest + "\n");

//		assertTrue(validationRequestFolio, "El Código wmCode no es el esperado: " + wmCodeFolio);

		// ************************************************************************************************/
		// Paso 2

		addStep("Ejecutar el servicio para la solicitud de redención de cupón de la interface TPE_FCP:");
		System.out.println("Paso 2: ");

		String respuestaAut = FCPUtil.generarRedencionCup(folio);

		System.out.println("\n" + respuestaAut + "\n");

		wmCodeRequest = getWmCodeXml(respuestaAut);
		System.out.println("WMcode: " + wmCodeRequest);

		boolean validationResponseAut = true;

		if (respuestaAut != null) {
			validationResponseAut = false;
		}

		boolean validationRequestAut = wmCodeRequest.equals(wmCodeToValidateAuth);

		System.out.println("\n" + validationRequestAut + " - wmCode request: " + wmCodeRequest + "\n");

//		assertTrue(validationRequestAut, "El Código wmCode no es el esperado: " + wmCodeToValidateAuth);

		/*
		 * *****************************************************************************
		 * ****************** Paso 3
		 ***********************************************************************************************/
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
		 * Paso 4
		 *****************************************************************************************/
	
	 addStep("Ejecutar consulta para validar que se registró correctamente la transacción activación de tarjeta en FCP en site 1 MTY: ");

			// Se forma el query a utilizar
			System.out.println("Paso 4: "+GlobalVariables.DB_HOST_FCTPE);
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

//		***************************Consulta en SIte 2 QRO*************************************************	
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
		 * Paso 5
		 *****************************************************************************************/
			addStep("Establecer conexión con la base de datos: FCWMLTAEQA ");
			testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAEQA.FEMCOM.NET ");
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("La conexión a la base de datos FCWMLTAEQA fue exitosa.");
			testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMLTAEQA);

		/*
		 * Paso 6
		 *****************************************************************************************/
		addStep(" Validar en la base de datos **FCWMLTAQ** se este registrando en el securityLog las la invocación realizada de la interface TPE_FCP: ");
		
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
	
		
		/* Paso 10 *****************************************************************************************/
		 addStep("Ejecutar  la siguiente consulta  en la BD   *FCWMLTAEQA* para verificar que no  viajo por un canal seguro.");
		
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
		return "ATC_FT_017_TPE_FCP_WMx86_TransaccionRedencionPromSinergia_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. MTC-FT-052 Transacción exitosa de Redención de promoción Sinergia";
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
		return "*Contar con registro de promociones Sinergia para que pueda redimir de forma exitosa.\r\n"
				+ "*Contar con acceso a las base de datos de FCTPEQA_MTY, FCTPEQA_QRO, FCWMLTAEQA_MTY y FCWMLTAEQA_QRO.\r\n"
				+ "*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA.";
	}
}
