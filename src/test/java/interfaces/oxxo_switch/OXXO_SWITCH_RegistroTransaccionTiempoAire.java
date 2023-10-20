package interfaces.oxxo_switch;

import java.util.HashMap;

import org.apache.commons.net.ftp.FTPClient;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.Util;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class OXXO_SWITCH_RegistroTransaccionTiempoAire extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	
	public void ATC_FT_001_OXXO_SWITCH_RegistroTransaccionTiempoAire (HashMap<String,String> data) throws Exception {
		
		/*
		 * Construido.
		 * @author Brandon Ruiz.
		 * @date   09/02/2023.
		 * @cp MTC-FT-017 SWITCH_PE1 Registro de transaccion de tiempo aire electronico en el switch.
		 * @projectname Actualizacion Tecnologica Webmethods
		 * 
		 */
		
		/*
		 * Paso 4 comentado debido a que falta conocer el password para acceder al servidor ftp
		 */
		
		/*****Utilerias**************************************************************************************/
		Util o = new Util(testCase);
		
		String FCSWQAS1_HOST = GlobalVariables.DB_HOST_FCSWQA;
		SQLUtil FCSWQAS1 = new SQLUtil(FCSWQAS1_HOST, GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		
		String FCSWQAS2_HOST = GlobalVariables.DB_HOST_FCSWQA_QRO;
		SQLUtil FCSWQAS2 = new SQLUtil(FCSWQAS2_HOST,GlobalVariables.DB_USER_FCSWQA_QRO,GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		
		
		/*****Variables**************************************************************************************/
		//generales
		String defaultError = "La consulta realizada no devolvio ningun registro";
		String successfulConnection = "Consulta realizada con exito";
		
		//ftp
		String ftpHost = data.get("ftphost");
		String ftpUser = data.get("ftpuser");
		//falta saber cual es el password para el servidor ftp
		String ftpPass = data.get("ftppass");
		String ftpPort = data.get("ftpport");
		String ftpPath = data.get("ftppath");
		
		//xml
		String respuesta = "";
		
		//Paso 2
		String queryPaso2 = "SELECT * FROM SWUSER.TPE_SW_TLOG WHERE ROWNUM <=5";
		SQLResult resultPaso2;
		
		//Paso 3
		String queryPaso3;
		SQLResult resultPaso3;
		
		/*****Paso 1**************************************************************************************/
		step("Ejecutar caso ATC_FT_008_PE1_WMx86_TransaccionExitosaRecarga Transaccion exitosa de recarga de tiempo aire electronico");
		o.logBold("Ejecutar en un navegador directamente el servicio rungGetFolio de solicitud de folio de la PE1 del server premq01");
		String runGetFolio = "PE1/RunGetFolio.txt";
		HashMap<String,String> datos = new HashMap<>();
		datos.put("host", data.get("host"));
		datos.put("plaza", data.get("plaza"));
		datos.put("tienda", data.get("tienda"));
		datos.put("caja", data.get("caja"));
		respuesta = o.getXmlResponse(runGetFolio, datos);
		String folio = o.getXmlTagValue(respuesta, "folio");
		String wmCode = o.getXmlTagValue(respuesta, "wmCode");
		if (!wmCode.equalsIgnoreCase("100")) {
			o.mensajeError("el valor de wmCode: "+wmCode+" fue distinto al esperado: 100");
		}else {
			o.log("El servicio  de solicitud de folio de la PE1 proporciona un numero de folio: "+folio+" y wmCode: "+wmCode);
		}
		datos.clear();
		
		o.logBold("Ejecutar en el navegador el servicio runGetAuth para realizar la solicitud de autorizacion de PE1 para TAE");
		String runGetAuth = "PE1/RunGetAuth.txt";
		String fecha = o.getFormattedDate("yyyyMMddHHmmss");
		datos.put("host", data.get("host"));
		datos.put("folio", folio);
		datos.put("pv_date", fecha);
		datos.put("ad_date", fecha.substring(0, 8));
		datos.put("carrier", data.get("carrier"));
		datos.put("phone", data.get("phone"));
		datos.put("amount", data.get("amount"));
		respuesta = o.getXmlResponse(runGetAuth, datos);
		datos.clear();
		wmCode = o.getXmlTagValue(respuesta, "wmCode");
		if (!wmCode.equalsIgnoreCase("000")) {
			o.log("el valor de wmCode: "+wmCode+" fue distinto al esperado: 000");
		}else {
			o.log("El servicio RunGetAuth muestra la respuesta de la solicitud de autorizacion de PE1 con wmCode: "+wmCode);
		}
		
		o.logBold("Ejecutar el servicio RunGetAck de la PE1 para confirmar el ACK de la transaccion TAE");
		String runGetAck = "PE1/RunGetAck.txt";
		datos.put("host", data.get("host"));
		datos.put("folio", folio);
		datos.put("ack", data.get("ack"));
		respuesta = o.getXmlResponse(runGetAck, datos);
		wmCode = o.getXmlTagValue(respuesta, "wmCode");
		if (!wmCode.equalsIgnoreCase("101")) {
			o.mensajeError("el valor de wmCode: "+wmCode+" fue distinto al esperado: 101");
		}else {
			o.log("El servicio RunGetAck de la PE1 muestra una respuesta exitosa a la solicitud de ACK "
					+ "para la transaccion de TAE con wmCode: "+wmCode);
		}
		
		/*****Paso 2**************************************************************************************/
		step("Conectarse a la Base de Datos *FCSWQA* (ambos sites)");
		o.logHost(FCSWQAS1_HOST);
		resultPaso2 = ejecutaQuery(queryPaso2, FCSWQAS1);
		if (resultPaso2.isEmpty()) {
			o.mensajeError(defaultError);
		}else {
			o.log(successfulConnection);
		}
		
		o.logHost(FCSWQAS2_HOST);
		resultPaso2 = ejecutaQuery(queryPaso2, FCSWQAS2);
		if (resultPaso2.isEmpty()) {
			o.mensajeError(defaultError);
		}else {
			o.log(successfulConnection);
		}
		
		/*****Paso 3**************************************************************************************/
		step("Ejecutar la siguiente consulta para validar la transaccion en la base de datos del *FCSWQA* (ambos sites)");
		boolean bandera = false;
		queryPaso3 = "SELECT PLAZA, TIENDA, FOLIO, CREATION_DATE FROM SWUSER.TPE_SW_TLOG\r\n"
				+ "WHERE FOLIO = '"+folio+"'";
		resultPaso3 = ejecutaQuery(queryPaso3, FCSWQAS1);
		o.validaRespuestaQuery(resultPaso3);
		if (resultPaso3.isEmpty()) {
			o.log("No se encontro ningun registro de transaccion con folio: "+folio+" para el site 1");
		}else {
			o.log("Se muestra el registro con el numero correspondiente al folio: "+folio);
			bandera = true;
		}
		
		resultPaso3 = ejecutaQuery(queryPaso3, FCSWQAS2);
		o.validaRespuestaQuery(resultPaso3);
		if (resultPaso3.isEmpty()) {
			o.log("No se encontro ningun registro de transaccion con folio: "+folio+" para el site 2");
		}else {
			o.log("Se muestra el registro con el numero correspondiente al folio: "+folio);
			bandera = true;
		}
		
		if (!bandera) {
			o.mensajeError("No se encontro ningun registro de transaccion con folio: "+folio+" para ningun site consultado");
		}
		
		
		/*****Paso 4**************************************************************************************/
		step("Establecer la conexion con el servidor FTP para acceder al repositorio del LOG DEL SWITCH "
				+ "y validar que el archivo LOG DEL SWITCH exista");
		
		//falta conocer el password para acceder al servidor ftp
		o.mensajeError("No fue posible conectarse al FTP");
		
//		String date = o.getFormattedDate("ddMMyyyy");
//		String fileName = "switch_oxxo";
//		fileName = fileName.concat(date+".log");
//		FTPClient ftpClient = o.getFTPClient(ftpHost, ftpPort, ftpUser, ftpPass);
//		o.searchFTPFileByName(ftpClient, ftpPath, fileName);
		
	}
	
	int contador = 0;
	public void step(String step){
		contador++;
		System.out.println("\r\nStep "+contador+"-"+step);
		addStep(step);
	}
	
	public void printQuery(String query) {
		System.out.println("\r\n#----- Query Ejecutado -----#\r\n");
		System.out.println(query+"\r\n");
		System.out.println("#---------------------------#\r\n");
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
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return "*Contar con un simulador de proveedor de TAE que responda las transacciones de forma exitosa.\r\n"
				+ "*Contar con acceso a las base de datos de FCSWPQA (ambos sites)\r\n"
				+ "*Contar con las credenciales de WM10.11 de los nuevos servers del Integration Server de QA. \r\n";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Prueba de regresion para comprobar la no afectacion en la funcionalidad principal "
				+ "de la interface FC_OXXO_SWITCH y el registro de transacciones en el log "
				+ "con los mensajes ISO entre OXXO y Proveedor al ser migrada de webmethods v10.5 "
				+ "a webmethods 10.11 y del sistema operativo Solaris(Unix) a Redhat 8.5 (Linux X86).";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_OXXO_SWITCH_RegistroTransaccionTiempoAire";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	

}
