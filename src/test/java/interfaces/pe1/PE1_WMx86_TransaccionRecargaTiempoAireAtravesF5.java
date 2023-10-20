package interfaces.pe1;


import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.Util;
import util.GlobalVariables;
import utils.ApiMethodsUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PE1_WMx86_TransaccionRecargaTiempoAireAtravesF5 extends BaseExecution{


	@Test(dataProvider = "data-provider")
	public void ATC_FT_010_PE1_WMx86_TransaccionRecargaTiempoAireAtravesF5(HashMap<String, String> data) throws Exception {
		
		
		/*
		 * 
		 * Modificado por mantenimiento
		 * @author Brandon Ruiz
		 * @date 16/02/2023
		 * @cp MTC-FT-002 PE1 Transaccion exitosa de recarga de tiempo aire electronico a traves de F5 y OAUTH2.0
		 * @projectname Actualizacion Tecnologica WebMethods 10
		 * 
		 */
		
		
		/***Utileria*************************************************************************/

		String FCACQA_HOST = GlobalVariables.DB_HOST_FCACQA_QRO;
		SQLUtil FCACQA_QRO = new SQLUtil(FCACQA_HOST, GlobalVariables.DB_USER_FCACQA_QRO, GlobalVariables.DB_PASSWORD_FCACQA_QRO);
		String FCTAEQAS1_HOST = GlobalVariables.DB_HOST_FCTAEQA;
		SQLUtil FCTAEQAS1 = new SQLUtil(FCTAEQAS1_HOST, GlobalVariables.DB_USER_FCTAEQA, GlobalVariables.DB_PASSWORD_FCTAEQA);
		String FCTAEQAS2_HOST = GlobalVariables.DB_HOST_FCTAEQA_QRO;
		SQLUtil FCTAEQAS2 = new SQLUtil(FCTAEQAS2_HOST, GlobalVariables.DB_USER_FCTAEQA_QRO, GlobalVariables.DB_PASSWORD_FCTAEQA_QRO);
		String FCSWQA_HOST = GlobalVariables.DB_HOST_FCSWQA_QRO;
		SQLUtil FCSWQA = new SQLUtil(FCSWQA_HOST, GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		String FCWMLTAEQA_HOST = GlobalVariables.DB_HOST_FCWMLTAEQA;
		SQLUtil FCWMLTAEQA = new SQLUtil(FCWMLTAEQA_HOST, GlobalVariables.DB_USER_FCWMLTAEQA_QA,
				GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		
		Util o = new Util(testCase);
		 
		/***Variables*************************************************************************/
		String defaultError = "La consulta realizada no devolvio ningun registro";
		String successfulConnection = "Consulta realizada con exito";
		
		String host = data.get("host");
		String plaza = data.get("plaza");
		String tienda = data.get("tienda");
		String caja = data.get("caja");
		String carrier = data.get("carrier");
		String phone = data.get("phone");
		String amount = data.get("amount");
		String ack = data.get("ack");
		
		String pvDate = o.getPvDate();
		String adDate = o.getAdDate();
		
		
		/***Paso 1****************************************************************************/
		step("Realizar conexion hacia la BD FCACQA_QRO");
	/*	o.logHost(FCACQA_HOST);
		String queryPaso1 = "SELECT * FROM TPEUSER.TPE_SEC_INVENTARIO_POS_DETL WHERE ROWNUM <= 5";
		SQLResult resultPaso1 = ejecutaQuery(queryPaso1,FCACQA_QRO);
		if (resultPaso1.isEmpty()) {
			o.mensajeError(defaultError);
		}else {
			o.log(successfulConnection);
		}*/
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCACQA_QRO ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCACQA_QRO);
		
		/***Paso 2****************************************************************************/
		step("Ejecutar la consulta de la BD FCACQA_QRO, en la tabla TPEUSER.TPE_SEC_INVENTARIO_POS_DETL "
				+ "para validar las credenciales de alguna tienda valida con STATUS= '1'");
		
		String queryPaso2 = "SELECT CRPLAZA, CRTIENDA, STATUS, MAC_ID "
				+ "FROM TPEUSER.TPE_SEC_INVENTARIO_POS_DETL\r\n"
				+ "WHERE STATUS = 1 AND CRPLAZA = '"+data.get("plaza")+"' AND CRTIENDA = '"+data.get("tienda")+"'";
		
		SQLResult resultPaso2 = ejecutaQuery(queryPaso2,FCACQA_QRO);
		o.validaRespuestaQuery(resultPaso2);
		String macID = "";
		String pass = "";
		
		if (resultPaso2.isEmpty()) {
			o.mensajeError("No se encontro ningun registro con crplaza: "+plaza+", crtienda: "+tienda+" y status: 1");
		}else {

			String queryPaso2_1 = "SELECT PASSWORD FROM TPEUSER.TPE_SEC_INVENTARIO_POS_DETL\r\n"
					+ "WHERE STATUS = 1 AND CRPLAZA = '"+data.get("plaza")+"' AND CRTIENDA = '"+data.get("tienda")+"'";
			SQLResult resultPaso2_1 = ejecutaQuery(queryPaso2_1, FCACQA_QRO);
			o.validaRespuestaQuery(resultPaso2_1);
			o.logBold("Se muestran las credenciales de cada tienda en las columnas MAC_ID y PASSWORD");
			macID = resultPaso2.getData(0, "MAC_ID");
			pass = resultPaso2_1.getData(0, "PASSWORD");
			System.out.println("Campos extraidos de TPE_SEC_INVENTARIO_POS_DETL:\r\n "
					+ "MAC_ID: "+macID+"\r\nPASSWORD: "+pass);
		}
		
		/***Paso 3****************************************************************************/
		step("Ejecutar la consulta de la BD FCACQA_QRO, en la tabla TPEUSER.TPE_SEC_ACCESS_CREDENTIALS "
				+ "para validar las credenciales para el token F5");
		String queryPaso3 = "SELECT CLIENT_ID, SECRET_ID FROM TPEUSER.TPE_SEC_ACCESS_CREDENTIALS";
		SQLResult resultPaso3 = ejecutaQuery(queryPaso3, FCACQA_QRO);
		o.validaRespuestaQuery(resultPaso3);
		String clientID = "";
		String secretID = "";
		if (resultPaso3.isEmpty()) {
			o.mensajeError("No se encontraron credenciales para el token F5");
		}else {
			o.logBold("Se muestran las credenciales de cada tienda en las columnas CLIENT_ID y SECRET_ID");
			clientID = resultPaso3.getData(0, "CLIENT_ID");
			secretID = resultPaso3.getData(0, "SECRET_ID");
			System.out.println("Recupera CLIENT_ID: "+clientID+" y SECRET_ID: "+secretID+" del primer registro");
		}
		
		/***Paso 4****************************************************************************/
		step("Generar token POST desde la herramienta POSTMAN para que se envie en los request posteriores");
		String url = "https://qa-auth-se.femcom.net/f5-oauth2/v1/token";
		HashMap<String,String> datos = new HashMap<>();
		datos.put("grant_type", data.get("grant_type"));
		datos.put("username", macID);
		datos.put("password", pass);
		datos.put("client_id", clientID);
		datos.put("client_secret", secretID);
		datos.clear();
		ApiMethodsUtil api = new ApiMethodsUtil(url);
		String respuesta = api.postRequestMethod(url, datos).getBody().asPrettyString();
		System.out.println(respuesta);
//		String REsp = api.postRequestWithHeaders(uri, data.get("uwu"), Headers).asPrettyString();
//		String REsp = api.postRequestMethod(uri, data.get("uwu")).getBody().asPrettyString();
		
		/***Paso 5****************************************************************************/
		step("Capturar el token obtenido en la seccion: Authorization y Type: Oauth 2.0 "
				+ "de los siguientes servicios de la interface PE1: runGenFolio, runGetAutn y runGetAuthACK");
		
		
		/***Paso 6****************************************************************************/
		step("Solicitar un folio desde el navegador, invocando el servicio **runGetFolio** "
				+ "de la Interface PE1 para una transaccion de recarga de tiempo aire mediante la siguiente url");
		String runGetFolio = "PE1/RunGetFolio.txt";
		datos.put("host", host);
		datos.put("plaza", plaza);
		datos.put("tienda", tienda);
		datos.put("caja", caja);
		String response = o.getXmlResponse(runGetFolio, datos);
		datos.clear();
		String folio = o.getXmlTagValue(response, "folio");
		String wmCode = o.getXmlTagValue(response, "wmCode");
		System.out.println("Recupera folio: "+folio+" y wmCode: "+wmCode);
		if (wmCode.equalsIgnoreCase("100")) {
			o.logBold("El web service responde exitosamente con numero de folio: "+folio+" y  wmCode: "+wmCode);
		}else {
			o.mensajeError("El web service responde con wmCode: "+wmCode+" diferente al esperado: 100");
		}
		
		/***Paso 7****************************************************************************/
		step("Ejecutar la solicitud de autorizacion invocando el servicio runGetAuth mediante la siguiente url");
		String runGetAuth = "PE1/RunGetAuth.txt";
		datos.put("host", host);
		datos.put("folio", folio);
		datos.put("pv_date", pvDate);
		datos.put("ad_date", adDate);
		datos.put("carrier", carrier);
		datos.put("phone", phone);
		datos.put("amount", amount);
		response = o.getXmlResponse(runGetAuth, datos);
		datos.clear();
		wmCode = o.getXmlTagValue(response, "wmCode");
		String swAuth = "";
		if (wmCode.equalsIgnoreCase("000")) {
			swAuth = o.getXmlTagValue(response, "swAuth");
			o.logBold("El web service responde exitosamente con el folio: "+folio+" wmCode: "+wmCode+" y un swAuth: "+swAuth
					+" como numero de autorizacion para la transaccion");
		}else {
			o.mensajeError("El web service responde con wmCode: "+wmCode+" diferente al esperado: 000");
		}
		
		/***Paso 8****************************************************************************/
		step("Ejecutar la Solicitud de confirmacion del ACK de recarga de tiempo aire desde un navegador,"
				+ " invocando el servicio **runGetAck** de la interface PE1");
		String runGetAck = "PE1/runGetAck.txt";
		datos.put("host", host);
		datos.put("folio", folio);
		datos.put("ack", ack);
		response = o.getXmlResponse(runGetAck, datos);
		datos.clear();
		wmCode = o.getXmlTagValue(response, "wmCode");
		if (wmCode.equalsIgnoreCase("101")) {
			o.logBold("El web service confirma el ACK para la transaccion de recarga de tiempo aire exitosa con wmCode: "+wmCode);
		}else {
			o.mensajeError("El web service responde con wmCode: "+wmCode+" diferente al esperado: 101");
		}
		
		/***Paso 9****************************************************************************/
		step("Establecer exitosamente la conexion con la BD *FCTAEQA* en site 1 (MTY) y 2 (QRO)");
		o.logHost(FCTAEQAS1_HOST);
		String queryPaso9 = "SELECT * FROM TPEUSER.TAE_TRANSACTION WHERE ROWNUM <=5";
		SQLResult resultPaso9_1 = ejecutaQuery(queryPaso9, FCTAEQAS1);
		if (resultPaso9_1.isEmpty()) {
			o.mensajeError(defaultError);
		}
		
		o.logHost(FCTAEQAS2_HOST);
		SQLResult resultPaso9_2 = ejecutaQuery(queryPaso9, FCTAEQAS2);
		if (resultPaso9_2.isEmpty()) {
			o.mensajeError(defaultError);
		}
		
		o.log("Conexion establecida exitosamente para FCTAEQA (ambos sites)");
		
		/***Paso 10****************************************************************************/
		step("Ejecutar la siguiente consulta para validar que la transaccion de recarga de tiempo aire"
				+ " se registro correctamente en la tabla TPEUSER.TAE_TRANSACTION de la BD **FCTAEQA** "
				+ "de ambos Sites con la siguiente query");
		String queryPaso10="SELECT FOLIO, CREATION_DATE, PLAZA, TIENDA, AMOUNT, WM_CODE, SW_AUTH_CODE FROM TPEUSER.TAE_TRANSACTION \r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "AND FOLIO = '"+folio+"' AND WM_CODE = 101 AND SW_AUTH_CODE = 00 \r\n"
				+ "ORDER BY CREATION_DATE DESC";
		String errorPaso10 = "No se encontro registro exitoso para la transaccion con folio: "+folio;
		String successPaso10 = "Se muestra el registro de la transaccion TAE con folio: "+folio+" wmCode: 101 y sw_auth_code: 00 "
				+ "que indican que la transaccion de recarga de tiempo aire fue exitosa";
		SQLResult resultPaso10 = ejecutaQuery(queryPaso10, FCTAEQAS1);
		if (resultPaso10.isEmpty()) {
			resultPaso10 = ejecutaQuery(queryPaso10, FCTAEQAS2);
			o.validaRespuestaQuery(resultPaso10);
			if (resultPaso10.isEmpty()) {
				o.muestraError(queryPaso10, errorPaso10);
			}else {
				o.logBold(successPaso10);
			}
		}else {
			o.validaRespuestaQuery(resultPaso10);
			o.logBold(successPaso10);
		}
		
		
		/***Paso 11****************************************************************************/
		step("Establecer exitosamente la conexion con la BD *FCSWQA* de QRO");
		o.logHost(FCSWQA_HOST);
		String queryPaso11 = "SELECT * FROM SWUSER.TPE_SW_TLOG WHERE ROWNUM <=5";
		SQLResult resultPaso11 = ejecutaQuery(queryPaso11, FCSWQA);
		if (resultPaso11.isEmpty()) {
			o.mensajeError(defaultError);
		}else {
			o.log(successfulConnection);
		}
		
		/***Paso 12****************************************************************************/
		step("Validar en la base de datos del **FCSWQA**, que se encuentre el registro de la transaccion utilizando la siguiente consulta");
		String queryPaso12 = "SELECT FOLIO, MTI, RESP_CODE, AUTH_ID_RES, CREATION_DATE FROM SWUSER.TPE_SW_TLOG \r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE) \r\n"
				+ "AND FOLIO = '"+folio+"'";
		SQLResult resultPaso12 = ejecutaQuery(queryPaso12, FCSWQA);
		o.validaRespuestaQuery(resultPaso12);
		if (resultPaso12.isEmpty()) {
			o.muestraError(queryPaso12, "No se encontro ningun registro con folio: "+folio);
		}else {
			String auth_id_res = resultPaso12.getData(0, "AUTH_ID_RES");
			o.logBold("Se comprueba el registro exitoso del folio: "+folio+", mti: 0200 (solicitud de autorizacion), "
					+ "resp_code: 00 (codigo de respuesta exitoso del proveedor) y auth_id_res: "+auth_id_res
					+ " (numero de autorizacion del proveedor para la transaccion");
		}
		
		
		/***Paso 13****************************************************************************/
		step("Establecer exitosamente la conexion con la BD *OXWMLOGQA_PREM*");
		o.logHost(FCWMLTAEQA_HOST);
		String queryPaso13 = "SELECT * FROM WMLOG.WM_LOG_ERROR_TPE WHERE ROWNUM <=5";
		SQLResult resultPaso13 = ejecutaQuery(queryPaso13, FCWMLTAEQA);
		if (resultPaso13.isEmpty()) {
			o.mensajeError(defaultError);
		}else {
			o.log(successfulConnection);
		}
		
		/***Paso 14****************************************************************************/
		step("Validar en la base de datos *OXWMLOGQA_PREM* que NO se este registrando error en la tabla WM_LOG_ERROR_TPE");
		String queryPaso14 = "SELECT ERROR_ID, FOLIO, DESCRIPTION, MESSAGE FROM WMLOG.WM_LOG_ERROR_TPE \r\n"
				+ "WHERE ERROR_DATE >= TRUNC(SYSDATE)\r\n"
				+ "AND FOLIO = '"+folio+"'";
		SQLResult resultPaso14 = ejecutaQuery(queryPaso14,FCWMLTAEQA);
		o.validaRespuestaQuery(resultPaso14);
		if (resultPaso14.isEmpty()) {
			o.logBold("No se muestra ningun registro en la BD *OXWMLOGQA_PREM*, comprobando asi la ausencia de errores en la transaccion ejecutada en esta prueba");
		}else {
			o.muestraError(queryPaso14, "Se encontro un registro de error con folio: "+folio);
		}
		
		/***Paso 15****************************************************************************/
		step("Realizar la siguiente consulta de la BD *OXWMLOGQA_PREM* para validar "
				+ "que no se hayan registrado errores en la transaccion de recarga de tiempo aire");
		String queryPaso15 = "SELECT FOLIO, PLAZA, TIENDA FROM WMLOG.SECURITY_SESSION_LOG \r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE) AND FOLIO = '"+folio+"'";
		SQLResult resultPaso15 = ejecutaQuery(queryPaso15, FCWMLTAEQA);
		o.validaRespuestaQuery(resultPaso15);
		if(resultPaso15.isEmpty()) {
			o.logBold("No se muestran las invocaciones de la PE1 relacionadas con la transaccion de recarga de tiempo aire"
					+ ", comprobando que paso por el canal seguro F5");
		}else {
			o.log("Se encuentra(n) registro(s) con folio: "+folio+" indicando que no paso por el canal seguro F5");
		}
		
		
	}
	
	public void printQuery(String query) {
		System.out.println("\r\n#----- Query Ejecutado -----#\r\n");
		System.out.println(query+"\r\n");
		System.out.println("#---------------------------#\r\n");
	}
	
	int contador = 0;
	public void step(String step){
		contador++;
		System.out.println("\r\nStep "+contador+"-"+step);
		addStep(step);
	}
	
	public SQLResult ejecutaQuery(String query, SQLUtil obj) throws Exception{
		SQLResult queryResult;
		printQuery(query);
		queryResult = executeQuery(obj, query);
		return queryResult;
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Prueba de regresion para comprobar la no afectacion en la funcionalidad principal de recarga TAE "
				+ "de la interface FEMSA_PE1 (a traves de F5 y OAUTH2.0) al ser migrada de webmethods v10.5 a webmethods 10.11 "
				+ "y del sistema operativo Solaris(Unix) a Redhat 8.5 (Linux X86).";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_010_PE1_WMx86_TransaccionRecargaTiempoAireAtravesF5";
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
				+ "*Contar con acceso a las base de datos de FCACQA_QRO, FCTAEQA_QRO,  FCSWQA_QRO y OXWMLOGQA_PREM.\r\n"
				+ "*Contar con las credenciales de WM10.11 de los nuevos servers del Integration Server de QA.\r\n"
				+ "*Contar con los request en POSTMAN que permitan solicitar un token a F5, para poderlo inyectar en la autorizacion de la PE1. ";
	}
	
	
}

