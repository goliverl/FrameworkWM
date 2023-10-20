package interfaces.tpe.fac;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import modelo.BaseExecution;
import om.TPE_FAC;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import utils.webmethods.ReadRequest;

public class FAC_WMx86_ConsultaCajerosLoginCambioPassLogout extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_010_TPE_FAC_WMx86_ConsultaCajerosLoginCambioPassLogout(HashMap<String, String> data)
			throws Exception {

	/**
	 * TPE_FAC: MTC-FT-053 Consulta de cajeros, login, cambio de pass y logout.
	 * 
	 * Prueba de regresion para comprobar la no afectacion en la funcionalidad 
	 * principal de transaccionalidad de la interface TPE_FAC y TPE_FR al ser migradas 
	 * de WM9.9 a WM10.5, para una transaccion de Consulta de cajeros, login, 
	 * cambio de pass y logout.
	 * 
	 * 	@author JoseO@Hexaware.com
	 * 	@date 02/13/2023
	 */
		
		
		/*
		 * Utilerias
		 *********************************************************************/

		SQLUtil dbFCAC_MTY = new SQLUtil(GlobalVariables.DB_HOST_FCACQA, GlobalVariables.DB_USER_FCACQA,
				GlobalVariables.DB_PASSWORD_FCACQA);
		SQLUtil dbFCAC_QRO = new SQLUtil(GlobalVariables.DB_HOST_FCACQA_QRO, GlobalVariables.DB_USER_FCACQA,
				GlobalVariables.DB_PASSWORD_FCACQA);
		SQLUtil dbFCWMLTAEQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_QA,
				GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);


		TPE_FAC FACUtil = new TPE_FAC(data, testCase, dbFCAC_MTY);

		/*
		 * Variables
		 *********************************************************************/

		String ValidUser = "SELECT A.CR_TIENDA, A.CR_PLAZA, A.ID_EMPLEADO, A.ID_ESTADO, A.FUNCION, A.RFC, C.ESTADO, C.ID_USUARIO, C.DIAS, C.CONTRASENA "
				+ "FROM XXADCJ.XXADCJ_EMPLEADOS A  " + "INNER JOIN XXADCJ.XXADCJ_ROL_FUNCION B "
				+ "ON(B.CVE_FUNCION = A.ID_FUNCION) " + "INNER JOIN XXADCJ.XXADCJ_USUARIOS C "
				+ "ON(C.ID_EMPLEADO = A.ID_EMPLEADO) " + "WHERE  C.ESTADO = 'AE'  " + "AND A.ID_ESTADO = 3 "
				+ "AND C.DIAS <=7";

		String ValidLogin = "SELECT ID_USER,FECHA_ACCESO,APLICACION,APP " + "FROM TPEUSER.TPE_FAC_LOGIN_APLICACION "
				+ "WHERE  ID_USER = '%s'";

		String ValidUpdPass = "SELECT A.CR_TIENDA, A.CR_PLAZA, A.ID_EMPLEADO, A.ID_ESTADO, A.FUNCION, A.RFC, C.ESTADO, C.ID_USUARIO, C.DIAS, C.CONTRASENA "
				+ "FROM XXADCJ.XXADCJ_EMPLEADOS A  " + "INNER JOIN XXADCJ.XXADCJ_ROL_FUNCION B "
				+ "ON(B.CVE_FUNCION = A.ID_FUNCION) " + "INNER JOIN XXADCJ.XXADCJ_USUARIOS C "
				+ "ON(C.ID_EMPLEADO = A.ID_EMPLEADO) " + "WHERE  C.ESTADO = 'A' " + "AND A.ID_ESTADO = '3' "
				+ "AND C.DIAS >='180' " + "AND C.ID_USUARIO='%s'";

		String RegPass = "SELECT * FROM XXADCJ.XXADCJ_BITACORA_PWD WHERE ID_USUARIO = '%s' ORDER BY FECHA_CREACION";

		String ValidTransac = "SELECT APPLICATION,ENTITY,OPERATION,TIENDA,PLAZA,CREATION_DATE,WM_CODE,WM_DESC  "
				+ "FROM TPEUSER.TPE_FR_TRANSACTION  " + "WHERE CREATION_DATE >= trunc(sysdate) "
				+ "AND  APPLICATION = 'FAC' " + "AND ENTITY='BANK' " + "AND TIENDA='%s' "
				+ "ORDER BY CREATION_DATE DESC";


		String errorTransaccion = "select * from(SELECT ERROR_ID,FOLIO,ERROR_DATE,SEVERITY,ERROR_TYPE,ERROR_CODE "
				+ "FROM WMLOG.WM_LOG_ERROR_TPE " + "WHERE TPE_TYPE like '%s' "
				+ "AND ERROR_DATE BETWEEN trunc(sysdate-1) and trunc(sysdate+1) "
				+ "ORDER BY ERROR_DATE DESC) where rownum <=4";// FCWMLTAEQA.

		String LogSecSess = "select * from(SELECT  APPLICATION,OPERATION,SOURCE,FOLIO,PLAZA,TIENDA,CREATION_DATE "
				+ "FROM  WMLOG.SECURITY_SESSION_LOG " + "WHERE CREATION_DATE >=TRUNC(SYSDATE) "
				+ "AND APPLICATION like '%s' " + "ORDER BY CREATION_DATE DESC) where rownum <=4";

		testCase.setProject_Name("POC WMx86");
		
		String wmCodeCons = data.get("wmCodeConsult");
		String wmCodeLog = data.get("wmCodeLogin");
		String wmCodeUpd = data.get("wmCodeUpd");
		String wmCodeLogOut = data.get("wmCodeLogOut");


		String wmCodeRequest;
		// ************************************************************************************************/
		// Paso 1 FCACQA_MTY y FCACQA_QRO.
		addStep("Establecer conexion con la base de datos: FCACQA_MTY ");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCACQA.FEMCOM.NET ");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexion a la base de datos FCACQA_MTY fue exitosa.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCACQA);

		addStep("Establecer conexion con la base de datos: FCACQA_QRO ");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCACQA.FEMCOM.NET ");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexion a la base de datos FCACQA_QRO fue exitosa.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCACQA_QRO);

//		***************************Paso 2*****************************************************

		addStep("Ejecutar consulta desde la DB FCACQA el estatus del usuario se a igual a AE ");

		System.out.println(GlobalVariables.DB_HOST_FCACQA);
		System.out.println(ValidUser);
		SQLResult resultValidUs = executeQuery(dbFCAC_MTY, ValidUser);
		String ID_USUARIO="";
		String CONTRASENA="";
		boolean transaction1 = resultValidUs.isEmpty();

		if (!transaction1) {
			testCase.addTextEvidenceCurrentStep(
					"Se realiza la consulta exitosamente y se muestra el estatus = AE y DIAS sea igual o menor a 7 y mayor que 1.");
			ID_USUARIO = resultValidUs.getData(0, "ID_USUARIO");
			System.out.println("Id_User: " + ID_USUARIO);
			CONTRASENA = resultValidUs.getData(0, "CONTRASENA");
			System.out.println("Pass: " + CONTRASENA);
		}

		testCase.addQueryEvidenceCurrentStep(resultValidUs);

//	***************************Consulta en SIte 2 QRO*************************************************	
		testCase.addBoldTextEvidenceCurrentStep(
				"consulta desde la DB FCACQA el estatus del usuario se a igual a AE en site 2 "
						+ GlobalVariables.DB_HOST_FCACQA_QRO);

		System.out.println(GlobalVariables.DB_HOST_FCACQA_QRO);
		System.out.println(ValidUser);
		SQLResult resulVal2 = executeQuery(dbFCAC_QRO, ValidUser);

		testCase.addTextEvidenceCurrentStep(
				"Se ejecuta la conssulta: \n" + ValidUser + " en host: " + GlobalVariables.DB_HOST_FCACQA_QRO);
		testCase.addQueryEvidenceCurrentStep(resulVal2);

		assertFalse(transaction1, "No se encontraron datos en consulta");

//		**************************************Paso 3***************************************************
		addStep("Ejecutar desde el navegador el servicio para validar la nueva consulta de cajeros (QRY01)");

		String respuestaConsulta = FACUtil.ConsultaCajeros();

		wmCodeRequest = getWmCodeXml(respuestaConsulta);
		System.out.println("WMcode: " + wmCodeRequest);

		boolean validationRequestWmCode = wmCodeRequest.equals(wmCodeCons);

		System.out.println("\n" + validationRequestWmCode + " - wmCode request: " + wmCodeRequest + "\n");

		assertTrue(validationRequestWmCode, "El Codigo wmCode no es el esperado: " + wmCodeCons);

		// ************************************************************************************************/
		// Paso 4

		addStep(" Ejecutar desde el navegador el servicio para validar el registro  de  el login (QRY09) ");

		String respuestaConsComp = FACUtil.ValidLogin(ID_USUARIO, CONTRASENA);

		System.out.println("\n" + respuestaConsComp + "\n");

		wmCodeRequest = getWmCodeXml(respuestaConsComp);
		System.out.println("WMcode: " + wmCodeRequest);

		boolean validationRequestConsComp = wmCodeRequest.equals(wmCodeLog);

		System.out.println("\n" + validationRequestConsComp + " - wmCode request: " + wmCodeRequest + "\n");

		assertTrue(validationRequestConsComp, "El Codigo wmCode no es el esperado: " + wmCodeLog);

		// ************************************************************************************************/
		// Paso 5

		addStep("Validar en la siguiente consulta desde la DB **FCACQA** el registro del login del usuario:");

		System.out.println(GlobalVariables.DB_HOST_FCACQA);
		String FormatValidLogin = String.format(ValidLogin, ID_USUARIO);
		System.out.println(FormatValidLogin);
		SQLResult resultValidLogin = executeQuery(dbFCAC_MTY, FormatValidLogin);

		boolean transactionLog = resultValidLogin.isEmpty();

		if (!transactionLog) {
			testCase.addTextEvidenceCurrentStep("Se comprueba la creacion exitosa del registro del login.");
		}

		testCase.addQueryEvidenceCurrentStep(resultValidLogin);
		assertFalse(transactionLog, "No se encontraron datos en consulta");

//	******************** Paso 6 *****************************************************************************		  
		addStep("Ejecutar desde el navegador la siguiente url para validar el servicio de Actualizacion de contrasena (QRY13) ");

		String respuestaUpd = FACUtil.ActualizacionPass(ID_USUARIO);
		System.out.println("\n" + respuestaUpd + "\n");
		String StatusUpd = "A";

		String Stat = getStatus(respuestaUpd);
		System.out.println("Status: " + Stat);

		wmCodeRequest = getWmCodeXml(respuestaUpd);
		System.out.println("WMcode: " + wmCodeRequest);

		boolean validationRequestUpd = wmCodeRequest.equals(wmCodeUpd);
		boolean validStatUpd = Stat.equals(StatusUpd);

		System.out.println("\n" + validationRequestUpd + " - wmCode request: " + wmCodeRequest + "\n");
		System.out.println("\n" + validStatUpd + " - status request: " + StatusUpd + "\n");

		assertTrue(validationRequestUpd, "El Codigo wmCode no es el esperado: " + wmCodeUpd);
		assertTrue(validStatUpd, "El Status no es el esperado: " + StatusUpd);

		/*
		 * *****************************************************************************
		 * ****************** Paso 7
		 ***********************************************************************************************/
		addStep("Validar en la siguiente consulta desde la DB **FCACQA**  el cambio de status de usuario y actualizacion de contrasena ");

		System.out.println(GlobalVariables.DB_HOST_FCACQA);
		String FormatValidUpdPass = String.format(ValidUpdPass, ID_USUARIO);
		System.out.println(FormatValidUpdPass);
		SQLResult resultValidUpdPass = executeQuery(dbFCAC_MTY, FormatValidUpdPass);

		boolean transactionUpd = resultValidUpdPass.isEmpty();

		if (!transactionUpd) {
			testCase.addTextEvidenceCurrentStep(
					"Se valida que  la actualizacion de los campos: status = A,  dias>=180 ");
		}

		testCase.addQueryEvidenceCurrentStep(resultValidUpdPass);
		assertFalse(transactionUpd, "No se encontraron datos en consulta");

//		*********************Paso 8 *********************************************************
		addStep("Ejecutar desde el navegador el servicio para validar el  servicio de logout de usuario.");

		String respuestaLogOut = FACUtil.ValidLogout(ID_USUARIO);

		System.out.println("\n" + respuestaLogOut + "\n");

		wmCodeRequest = getWmCodeXml(respuestaLogOut);
		System.out.println("WMcode: " + wmCodeRequest);

		boolean validationRequestSoliAut = wmCodeRequest.equals(wmCodeLogOut);

		System.out.println("\n" + validationRequestSoliAut + " - wmCode request: " + wmCodeRequest + "\n");

		assertTrue(validationRequestSoliAut, "El Codigo wmCode no es el esperado: " + wmCodeLogOut);

		/*
		 * *****************************************************************************
		 * ****************** Paso 9
		 ***********************************************************************************************/
		addStep("Validar en la siguiente consulta desde la DB FCACQA el logout del usuario:");

		System.out.println(GlobalVariables.DB_HOST_FCACQA);
		String FormatValidLogOut = String.format(ValidLogin, ID_USUARIO);
		System.out.println(FormatValidLogOut);
		SQLResult resultValidLogOut = executeQuery(dbFCAC_MTY, FormatValidLogOut);

		boolean transactionLogOut = resultValidLogOut.isEmpty();

		if (transactionLogOut) {
			testCase.addTextEvidenceCurrentStep(
					"Se valida exitosamente que no hay registros de login despues de realizar el logout. ");
		}

		testCase.addQueryEvidenceCurrentStep(resultValidLogOut);
		assertTrue(transactionLogOut, "Se encontraron datos en consulta no se realizo LogOut");

//		******************************Paso 10 ******************************************************************

		addStep("Validar en la siguiente consulta desde la DB FCACQA el registro de la contrasena en la bitacora:");

		System.out.println(GlobalVariables.DB_HOST_FCACQA);
		String FormatRegPass = String.format(RegPass, ID_USUARIO);
		System.out.println(FormatRegPass);

		SQLResult resultRegPass = executeQuery(dbFCAC_MTY, FormatRegPass);

		boolean transactionRegPass = resultRegPass.isEmpty();

		if (!transactionRegPass) {
			testCase.addTextEvidenceCurrentStep(
					"Se muestra el registro creado con la nueva contrasena en el historico de contrasenas. ");
		}

		testCase.addQueryEvidenceCurrentStep(resultRegPass);
		assertFalse(transactionRegPass, "No se encontraron datos en consulta realizada");

		/*
		 * Paso 11
		 *****************************************************************************************/

		addStep("Validar en la siguiente consulta desde la DB **FCACQ** las transacciones consulta de cajeros, login, "
				+ "cambio de pass y logout en la tabla TPE_FR_TRANSACTION  de la BD  **FCACQA** ");

		System.out.println(GlobalVariables.DB_HOST_FCACQA);
		String FormatTrans = String.format(ValidTransac, data.get("tienda"));
		System.out.println(FormatTrans);

		SQLResult resultTrans = executeQuery(dbFCAC_MTY, FormatTrans);

		boolean transactionTrans = resultTrans.isEmpty();

		if (!transactionTrans) {
			testCase.addTextEvidenceCurrentStep("Se muestra el registro con los siguientes valores WM_CODE=101 "
					+ "para una transaccion de Consulta de cajeros (QRY01), login (QRY09), cambio de pass (QRY13) y logout (QRY29).");
		}

		testCase.addQueryEvidenceCurrentStep(resultTrans);
//		***************************Consulta en SIte 2 QRO*************************************************	
		testCase.addBoldTextEvidenceCurrentStep(
				"consulta desde la DB FCACQA el estatus del usuario se a igual a AE en site 2 "
						+ GlobalVariables.DB_HOST_FCACQA_QRO);

		System.out.println(GlobalVariables.DB_HOST_FCACQA_QRO);
		System.out.println(FormatTrans);
		SQLResult resulValTra2 = executeQuery(dbFCAC_QRO, FormatTrans);

		testCase.addTextEvidenceCurrentStep(
				"Se ejecuta la conssulta: \n" + FormatTrans + " en host: " + GlobalVariables.DB_HOST_FCACQA_QRO);
		testCase.addQueryEvidenceCurrentStep(resulValTra2);

		assertFalse(transactionTrans, "No se encontraron datos en consulta realizada");

//		****************Paso 12 **********************************************************************

		// *****************************************************************************************/
		// ****************************************************Validar conexion
		// FCWMLTAEQA
		// **************************************************************************************

		addStep("Establecer conexion con la Base de Datos **FCWMLTAQ**  en site 1 MTY.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAQ.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexion a la base de datos FCWMLTAEQA_MTY fue exitosa.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMLTAEQA);

//		**********************Paso 13 *******************************************************************************		

		addStep(" Ejecutar la siguiente consulta en la base de datos **FCWMLTAQ** para validar que no se esten "
				+ "registrando las invocaciones el de la interface TPE_FAC en el securitylog.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLTAEQA);
		String formatSec = String.format(LogSecSess, "%FAC%");
		System.out.println(formatSec);
		SQLResult resultSec = executeQuery(dbFCWMLTAEQA, formatSec);

		boolean resultSecVal = resultSec.isEmpty();

		if (resultSecVal) {
			testCase.addTextEvidenceCurrentStep(formatSec);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"No se visualizan los registros de las invocaciones realizadas a la FAC  para las transacciones de la FAC (QRY01), "
							+ "login(QRY09), cambio de pass(QRY13) y logout(QRY29).");
		}
		testCase.addQueryEvidenceCurrentStep(resultSec);

		assertTrue(resultSecVal,
				"Se encontraron registros en la consulta y Solo se registran transacciones de pago electronicas.");

//		****************Paso 14 *******************************************************************************************

		addStep(" Validar en la base de datos **FCWMLTAQ** que no se encuentren registros de error de la interfaz FAC, utilizando la siguiente consulta:");

		System.out.println(GlobalVariables.DB_HOST_FCWMLTAEQA);
		String formatErrorTransaccion = String.format(errorTransaccion, "%FAC%");
		System.out.println(formatErrorTransaccion);

		SQLResult ExecuteErrorTransaccion = dbFCWMLTAEQA.executeQuery(formatErrorTransaccion);

		boolean ValidaRegistroErrorBoolean = ExecuteErrorTransaccion.isEmpty();

		System.out.println(ValidaRegistroErrorBoolean);

		if (ValidaRegistroErrorBoolean) {
			testCase.addTextEvidenceCurrentStep(formatErrorTransaccion);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"alida que no se muestran errores relacionado para una transaccion de Consulta de cajeros, login, cambio de pass y logout.");
		}
		testCase.addQueryEvidenceCurrentStep(ExecuteErrorTransaccion);
		assertTrue(ValidaRegistroErrorBoolean, "hay registros de error en la tabla WMLOG.WM_LOG_ERROR_TPE ");

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

	public static String getStatus(String xmlResponse) {
		Document runGetFolioRequestDoc = ReadRequest.convertStringToXMLDocument(xmlResponse);
		Element eElement = (Element) runGetFolioRequestDoc.getElementsByTagName("chpass").item(0);

		return eElement.getAttribute("status");
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_010_TPE_FAC_WMx86_ConsultaCajerosLoginCambioPassLogout";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Prueba de regresion para comprobar la no afectacion en la funcionalidad principal de consulta de "
				+ "cajeros, login, cambio de password y logout de la interface TPE_FAC y TPE_FR al ser migrada de "
				+ "webmethods v10.5 a webmethods 10.11 y del sistema operativo Solaris(Unix) a Redhat 8.5 (Linux X86).";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
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
		return "1 -Contar con acceso a las base de datos de FCACQA_MTY, FCACQA_QRO, FCWMLTAEQA_MTY y FCWMLTAEQA_QRO.\r\n"
				+ "2 -Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA.\r\n"
				+ "3. Usuario con estatus A. Y el campo XXADCJ_USUARIOS.DIAS igual a 7.\r\n"
				+ "Se puede buscar el usuario con la siguiente consulta:\r\n"
				+ "SELECT E.ID_EMPLEADO,U.ID_USUARIO, E.NOMBRE, E.AP_PATERNO, E.AP_MATERNO,U.ESTADO ESTADO_USR,E.ESTADO ESTADO_EMP,R.DESC_ROL,U.CONTRASENA,U.DIAS,U.PREGUNTA_SEC,U.RESPUESTA_SEC,E.CR_PLAZA, E.CR_TIENDA, E.ID_FUNCION, E.FUNCION FROM XXADCJ.XXADCJ_EMPLEADOS E INNER JOIN XXADCJ.XXADCJ_USUARIOS U \r\n"
				+ "ON U.ID_EMPLEADO = E.ID_EMPLEADO INNER JOIN XXADCJ.XXADCJ_ROL_FUNCION F\r\n"
				+ "ON F.CVE_FUNCION=E.ID_FUNCION INNER JOIN XXADCJ.XXADCJ_ROLES R ON R.CVE_ROL=F.CVE_ROL\r\n"
				+ "WHERE  E.CR_PLAZA = :PLAZA AND E.CR_TIENDA = :TIENDA;\r\n"
				+ "4. Tener configurado el campo ExpiredPassword = 7 en la tabla XXADCJ_PARAMETROS, se puede revisar con el siguiente query (SELECT * FROM XXADCJ.XXADCJ_PARAMETROS WHERE NOMBRE= 'expiredPassword';)\r\n"
				+ "5. Tener configurado el campo ExpiredDays=180 en la tabla XXADCJ_PARAMETROS, se puede revisar con el siguiente query (SELECT * FROM XXADCJ.XXADCJ_PARAMETROS WHERE NOMBRE= 'expiredDays';)\r\n"
				+ "6. Contar con la herramienta Control M, configurada.\r\n"
				+ "7. La tienda debe estar dada de alta a tiendas SADEL, en la BD FCACQA: \r\n"
				+ "SELECT * FROM tpeuser.CONFIG_TIENDAS_SADEL WHERE CR_TIENDA = :TIENDA;\r\n"
				+ "8. Usuario, debe tener 1 registro en el historial de contrasenas en la tabla XXADCJ_BITACORA_PWD.\r\n"
				+ "9. Tener configurado el parametro para tiempo de sesion (SESSION_TIMEOUT=5), se puede revisar con el siguiente query (SELECT * FROM TPE_FR_CONFIG WHERE APPLICATION='FAC' AND OPERATION='QRY09' AND CODE='SESSION_TIMEOUT';)\r\n"
				+ "10. Tener configurado el parametro para de dias de aviso de caducidad (PASSWORD-NOTIFICATION= 7), se puede revisar con el siguiente query (SELECT * FROM TPE_FR_CONFIG WHERE APPLICATION='FAC' AND OPERATION='QRY09' AND CODE='PASSWORD-NOTIFICATION';)\r\n"
				+ "11. Tener configurado el parametro para el valor de dias de expiracion de password (DIAS_CONFIG=180), se puede revisar con el siguiente query (SELECT * FROM TPE_FR_CONFIG WHERE APPLICATION='FAC' AND OPERATION='QRY13' AND CODE='DIAS_CONFIG';)\r\n"
				+ "";
	}
}
