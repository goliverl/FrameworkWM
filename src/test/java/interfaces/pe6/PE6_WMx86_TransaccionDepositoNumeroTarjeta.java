package interfaces.pe6;


import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.PE6;
import om.Util;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PE6_WMx86_TransaccionDepositoNumeroTarjeta extends BaseExecution {
	
	/*
	 * 
	 * Modificado por mantenimiento
	 * @author Brandon Ruiz
	 * @Date 17/02/2023
	 * @cp MTC-FT-013 PE6_PAY Transaccion exitosa deposito a numero de tarjeta
	 * @projectname Actualizacion Tecnologica WebMethods 10
	 * 
	 */

	@Test(dataProvider = "data-provider")

	public void ATC_FT_030_PE6_WMx86_TransaccionDepositoNumeroTarjeta(HashMap<String, String> data) throws Exception {
		
		String FCTDCQAS1_HOST = GlobalVariables.DB_HOST_FCTDCQA;
		SQLUtil FCTDCQAS1 = new SQLUtil(FCTDCQAS1_HOST, GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		String FCTDCQAS2_HOST = GlobalVariables.DB_HOST_FCTDCQA_QRO;
		SQLUtil FCTDCQAS2 = new SQLUtil(GlobalVariables.DB_HOST_FCTDCQA_QRO, GlobalVariables.DB_USER_FCTDCQA_QRO,
				GlobalVariables.DB_PASSWORD_FCTDCQA_QRO);
		String dbFCWMLTAEQA_HOST = GlobalVariables.DB_HOST_FCWMLTAEQA;
		SQLUtil dbFCWMLTAEQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA, GlobalVariables.DB_USER_FCWMLTAEQA_QA,
				GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		String dbFCSWQA_HOST = GlobalVariables.DB_HOST_FCSWQA;
		SQLUtil dbFCSWQA = new SQLUtil(GlobalVariables.DB_HOST_FCSWQA, GlobalVariables.DB_USER_FCSWQA,
				GlobalVariables.DB_PASSWORD_FCSWQA);
		String FCSWQAS2_HOST = GlobalVariables.DB_HOST_FCSWQA_QRO;
		SQLUtil FCSWQAS2 = new SQLUtil(FCSWQAS2_HOST, GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);

		Util o = new Util(testCase);
		
		String host = data.get("host");
		String plaza = data.get("plaza");
		String tienda = data.get("tienda");
		String caja = data.get("caja");
		String type = data.get("type");
		String cardNo = data.get("cardNo");
		String entryMode = data.get("entryMode");
		String promType = data.get("promType");
		String amount = data.get("amount");
		String operator = data.get("operator");
		String serviceId = data.get("serviceId");
		String accountType = data.get("accountType");
		String accountNo = data.get("accountNo");
		String bankId = data.get("bankId");
		String ack = data.get("ack");
		String track2 = data.get("track2");
		
		
		HashMap <String, String> fechas = new HashMap<>();

		// Paso 1 *********************************************************

		step("Ingresar al navegador para solicitar un folio con el servicio **runGetFolio** de la Interface PE6 "
				+ "para una transaccion de deposito a numero de tarjeta");
		HashMap<String, String> datos = new HashMap<>();
		String runGetFolio = "PE6/RunGetFolio2.txt";
		datos.put("host", host);
		datos.put("plaza", plaza);
		datos.put("tienda", tienda);
		datos.put("caja", caja);
		datos.put("type", type);
		String response = o.getXmlResponse(runGetFolio, datos);
		datos.clear();
		String wmCode = o.getXmlTagValue(response, "wmCode");
		String folio = o.getXmlTagValue(response, "folio");
		o.validaWmCodeXML(response, wmCode, "100", "Se obtiene el folio solicitado: "+folio+" con wmCode: "+wmCode);

		// Paso 2 *********************************************************

		step("Ingresar al navegador para solicitar autorizacion con el servicio **runGetAuth** de la interface PE6 para una transaccion de deposito a numero de tarjeta");
		fechas = o.getXMLDates(response);
		String creationDate = fechas.get("creationDate");
		String pvDate = fechas.get("pvDate");
		String adDate = fechas.get("adDate");
		String runGetAuth = "PE6/RunGetAuth.txt";
		datos.put("host", host);
		datos.put("folio", folio);
		datos.put("creationDate", creationDate);
		datos.put("pvDate", pvDate);
		datos.put("adDate", adDate);
		datos.put("cardNo", cardNo);
		datos.put("entryMode", entryMode);
		datos.put("promType", promType);
		datos.put("amount", amount);
		datos.put("operator", operator);
		datos.put("serviceId", serviceId);
		datos.put("accountType", accountType);
		datos.put("accountNo", accountNo);
		datos.put("bankId", bankId);
		datos.put("track2", track2);
		response = o.getXmlResponse(runGetAuth, datos);
		datos.clear();
		wmCode = o.getXmlTagValue(response, "wmCode");
		o.validaWmCodeXML(response, wmCode, "000", "Se obtiene respuesta exitosa de la solicitud de autorizacion con wmCode: "+wmCode);
		
		// Paso 3 *********************************************************

		step("Ingresar al navegador para solicitar confirmacion con el servicio **runGetAuthAck** de la interface PE6 para una transaccion de deposito a numero de tarjeta");
		String runGetAuthAck = "PE6/RunGetAuthAck2.txt";
		datos.put("host",host);
		datos.put("folio", folio);
		datos.put("creationDate", creationDate);
		datos.put("ack", ack);
		datos.put("track2", track2);
		response = o.getXmlResponse(runGetAuthAck, datos);
		datos.clear();
		wmCode = o.getXmlTagValue(response, "wmCode");
		o.validaWmCodeXML(response, wmCode, "101", "Se obtiene respuesta exitosa de la solicitud de confirmacion con wmCode: "+wmCode);

		// Paso 4 *********************************************************

		step("Realizar conexion a la BD 'FCTDCQA'");
		o.logHost(FCTDCQAS2_HOST);
		String queryPaso4 = "SELECT * FROM TPEUSER.TDC_TRANSACTION WHERE ROWNUM <=5";
		SQLResult resultPaso4 = ejecutaQuery(queryPaso4, FCTDCQAS2);
		if (resultPaso4.isEmpty()) {
			o.mensajeError("La consulta realizada no devolvio ningun registro");
		}else {
			o.log("consulta realizada con exito");
		}

		// Paso 5 *********************************************************
		step("Ejecutar la siguiente consulta para validar que la transaccion se registro correctamente en la tabla TDC_TRANSACTION:");
		String queryPaso5 = "SELECT FOLIO, CREATION_DATE, PLAZA, TIENDA, AMOUNT, WM_CODE, SW_AUTH_CODE FROM TPEUSER.TDC_TRANSACTION\r\n"
				+ "WHERE FOLIO = '"+folio+"' AND WM_CODE = 101 AND SW_AUTH_CODE = 00 ";
		SQLResult resultPaso5 = ejecutaQuery(queryPaso5, FCTDCQAS2);
		o.validaRespuestaQuery(resultPaso5);
		if (resultPaso5.isEmpty()) {
			o.log("No se encontro registro de la transaccion con wm_code: 101 y sw_auth_code: 00 para el folio: "+folio+" en la BD: "+FCTDCQAS2_HOST);
			resultPaso5 = ejecutaQuery(queryPaso5, FCTDCQAS1);
			o.validaRespuestaQuery(resultPaso5);
			if (resultPaso5.isEmpty()) {
				o.muestraError(queryPaso5, "No se encontro registro de la transaccion con wm_code: 101 y sw_auth_code: 00 para el folio: "+folio+"en la BD: "+FCTDCQAS1_HOST);
			}
		}else {
			o.log("Se muestra el registro de la transaccion con wm_code: 101 y sw_auth_code: 00");
		}
		
		// Paso 6 *********************************************************
		step("Realizar conexion a la BD FCSWQA.");
		o.logHost(dbFCSWQA_HOST);
		String queryPaso6 = "SELECT * FROM SWUSER.TPE_SW_TLOG WHERE ROWNUM <= 5";
		SQLResult resultPaso6 = ejecutaQuery(queryPaso6, dbFCSWQA);
		if (resultPaso6.isEmpty()) {
			o.mensajeError("La consulta realizada no devolvio ningun registro");
		}else {
			o.log("Consulta realizada con exito");
		}

		// Paso 7 *********************************************************
		step("Ejecutar la siguiente consulta en la BD FCSWQA para validar que se registro la transaccion de deposito a numero de tarjeta");
		String queryPaso7 = "SELECT FOLIO, PLAZA, TIENDA, RESP_CODE, SW_CODE FROM SWUSER.TPE_SW_TLOG\r\n"
				+ "WHERE FOLIO = '"+folio+"' AND RESP_CODE = 00 AND SW_CODE = 00";
		SQLResult resultPaso7 = ejecutaQuery(queryPaso7, dbFCSWQA);
		o.validaRespuestaQuery(resultPaso7);
		if (resultPaso7.isEmpty()) {
			o.log("No se encontro registro de la transaccion con folio: "+folio+" resp_code: 00 y sw_auth: 00 en la BD: "+dbFCSWQA_HOST);
			resultPaso7 = ejecutaQuery(queryPaso7,FCSWQAS2);
			o.validaRespuestaQuery(resultPaso7);
			if (resultPaso7.isEmpty()) {
				o.muestraError(queryPaso7, "No se encontro registro de la transaccion con folio: "+folio+" resp_code: 00 y sw_auth: 00 en la BD: "+FCSWQAS2_HOST);
			}
		}

		// Paso 8 *********************************************************
		step("Realizar conexion a la BD FCWMLTAEQA");
		o.logHost(dbFCWMLTAEQA_HOST);
		String queryPaso8 = "SELECT * FROM WMLOG.WM_LOG_ERROR_TPE WHERE ROWNUM <=5";
		SQLResult resultPaso8 = ejecutaQuery(queryPaso8,dbFCWMLTAEQA);
		if (resultPaso8.isEmpty()) {
			o.mensajeError("La consulta realizada no devolvio ningun registro");
		}else {
			o.log("Consulta realizada con exito");
		}
		
		// Paso 9 *********************************************************
		step("Ejecutar la siguiente consulta en la BD FCWMLTAEQA para verificar que no se hayan registrado errores en la ejecucion de la PE6");
		String queryPaso9 = "SELECT ERROR_ID, FOLIO, ERROR_DATE, ERROR_CODE, DESCRIPTION FROM WMLOG.WM_LOG_ERROR_TPE\r\n"
				+ "WHERE FOLIO = '"+folio+"'";
		SQLResult resultPaso9 = ejecutaQuery(queryPaso9,dbFCWMLTAEQA);
		o.validaRespuestaQuery(resultPaso9);
		if (resultPaso9.isEmpty()) {
			o.log("No se muestran registros de errores");
		}else {
			o.mensajeError("Se encontro registro de error para la transaccion con folio: "+folio);
		}

		/*
		 * Paso 10
		 *****************************************************************************************/
		step("Ejecutar la siguiente consulta en la BD *FCWMLTAEQA* para verificar que no viajo por un canal seguro.");
		String queryPaso10 = "SELECT APPLICATION, OPERATION, FOLIO, CREATION_DATE FROM WMLOG.SECURITY_SESSION_LOG \r\n"
				+ "WHERE FOLIO = '"+folio+"'";
		SQLResult resultPaso10 = ejecutaQuery(queryPaso10, dbFCWMLTAEQA);
		o.validaRespuestaQuery(resultPaso10);
		if (resultPaso10.isEmpty()) {
			o.muestraError(queryPaso10, "No se encontraron registros de las invocaciones ralizadas por el canal no seguro del F5");
		}else {
			o.log("Se muestran los registros de las invocaciones realizadas por el canal no seguro del F5 para la transaccion de deposito a numero de tarjeta: \r\n"
					+ "runGetFolio, runGetAuth, runGetAuthAck");
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
		return "Prueba de regresion para comprobar la no afectacion en la funcionalidad principal de "
				+ "deposito de efectivo a numero de tarjeta de la interface FEMSA_PE6 al ser migrada de "
				+ "webmethods v10.5 a webmethods 10.11, al migrar el sistema operativo "
				+ "Solaris(Unix) a Redhat 8.5 (Linux X86) y al migrar base de datos de Oracle 11 a Oracle 19 Grid 19 "
				+ "con Redhat 8.6.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_030_PE6_WMx86_TransaccionDepositoNumeroTarjeta";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return "*Contar con un simulador de proveedor de corresponsalias que responda las transacciones de forma exitosa.\r\n"
				+ "*Contar con acceso a las base de datos de FCTDCPRD, FCSWPRD y OXWMLOGQA.\r\n"
				+ "*Contar con las credenciales de WM10.11 de los nuevos servers del Integration Server de QA.";
	}
}
