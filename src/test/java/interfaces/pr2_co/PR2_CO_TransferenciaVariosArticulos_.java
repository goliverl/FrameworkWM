package interfaces.pr2_co;


import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.Util;
import util.GetRequestFile;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PR2_CO_TransferenciaVariosArticulos_ extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_pr2_co_TransferenciaVariosArticulos_(HashMap<String, String> data) throws Exception {
		
		/*
		 * 
		 * Modificado por mantenimiento.
		 * @author Brandon Ruiz.
		 * @date   07/02/2023.
		 * @cp MTC-FT-010 PR2_CO Validacion de Articulos a transferir, 
		 * Transferencias de Salida y Entrada a traves de la interface PR2_CO
		 * @projectname Actualizacion Tecnologica Webmethods
		 * 
		 */
		
		/*
		 * Paso 6, 7, 11 y 14 queries diferentes a evidencia manual, falta por saber de donde obtienen run_id y id
		 */
		
		/******Utilerias*********************************************************************/
		
		String FCWM6QA_HOST = GlobalVariables.DB_HOST_FCWMQA_NUEVA;
		SQLUtil FCWM6QA = new SQLUtil(FCWM6QA_HOST, 
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		String FCWMLQA_HOST = GlobalVariables.DB_HOST_FCWMLQA_WMLOG;
		SQLUtil FCWMLQA = new SQLUtil(FCWMLQA_HOST,GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		Util o = new Util(testCase);

		/******Variables*************************************************************************/
		//Data provider
		String host = data.get("host");
		String sender = data.get("sender");
		String receiver = data.get("receiver");
		String no_records = data.get("no_records");
		String sku = data.get("sku");
		String pv_doc_id = data.get("pv_doc_id");
		String ship_date = data.get("ship_date");
		String ext_ref_no = data.get("ext_ref_no");
		String ctrl_code = data.get("ctrl_code");
		String item = data.get("item");
		String ship_qty = data.get("ship_qty");
		String mov_type = data.get("mov_type");
		
		String succesfulConnection = "Consulta realizada con exito";
		String defaultError = "La consulta realizada no devolvio ningun registro";
		
		String queryPaso2 = "SELECT * FROM POSUSER.TSF_TRANSACTION_COL\r\n"
				+ "WHERE ROWNUM <=10";
		SQLResult resultPaso2;
		
		String queryPaso3 = "SELECT ID, START_DT, STATUS, CODE FROM POSUSER.TSF_TRANSACTION_COL \r\n"
				+ "WHERE START_DT >= TRUNC(SYSDATE) AND STATUS = 'S'\r\n"
				+ "AND OPERATION = 'PR2_CO_validSKU' AND CODE = '000'\r\n"
				+ "AND ROWNUM <=10 ORDER BY START_DT DESC";
		String errorPaso3 = "No se encontro ninguna ejecucion reciente de la PR2_CO, con code: 000 y estatus: S";
		SQLResult resultPaso3;
		
		String queryPaso5 = "SELECT ID, START_DT, STATUS, CODE FROM POSUSER.TSF_TRANSACTION_COL \r\n"
				+ "WHERE START_DT >= TRUNC(SYSDATE) AND STATUS = 'S' \r\n"
				+ "AND OPERATION = 'PR2_CO_Inbound' AND CODE = '000'\r\n"
				+ "AND ROWNUM <=10 ORDER BY START_DT DESC";
		String errorPaso5 = "No se registro ninguna transaccion reciente ejecutada, con code: 000 y estatus: S";
		SQLResult resultPaso5;
		
		//falta conseguir el run_id para utilizarlo en este query
		String queryPaso6 = "SELECT ID, STATUS, RECEIVED_DATE, RUN_ID, PARTITION_DATE FROM POSUSER.POS_TSF_ONLINE_HEAD\r\n"
				+ "WHERE RECEIVED_DATE >= TRUNC(SYSDATE)\r\n"
				+ "AND STATUS = 'R' AND ROWNUM <=10\r\n"
				+ "ORDER BY PARTITION_DATE DESC";
		String errorPaso6 = "No se encontro ningun registro reciente con status: R";
		SQLResult resultPaso6;
		
		//falta conseguir el run_id para utilizarlo en este query
		String queryPaso7 = "SELECT ID, ITEM, SHIP_QTY, PARTITION_DATE FROM POSUSER.POS_TSF_ONLINE_DETL  \r\n"
				+ "WHERE PARTITION_DATE >= TRUNC(SYSDATE) ORDER BY PARTITION_DATE DESC";
		String errorPaso7 = "No se encontro ningun registro reciente";
		SQLResult resultPaso7;
		
		String queryPaso9 = "SELECT ID, OPERATION, START_DT, STATUS, CODE FROM POSUSER.TSF_TRANSACTION_COL\r\n"
				+ "WHERE START_DT >= TRUNC(SYSDATE) AND STATUS = 'S' \r\n"
				+ "AND OPERATION = 'PR2_CO_Outbound' AND CODE = '000' AND ROWNUM <= 10 \r\n"
				+ "ORDER BY START_DT DESC";
		String errorPaso9 = "No se encontro ninguna transaccion ejecutada recientemente con status: S y code: 000";
		SQLResult resultPaso9;
		
		//falta conseguir el id para utilizarlo en este query
		String queryPaso11 = "SELECT ID, STATUS, PARTITION_DATE, AFFECTED_DATE FROM POSUSER.POS_TSF_ONLINE_HEAD\r\n"
				+ "WHERE STATUS = 'I' AND PARTITION_DATE >= TRUNC(SYSDATE) \r\n"
				+ "AND ROWNUM <=10 ORDER BY AFFECTED_DATE";
		String errorPaso11 = "No se encontro ningun registro con status: I";
		SQLResult resultPaso11;
		
		String queryPaso12 = "SELECT ID, OPERATION, START_DT, STATUS, CODE FROM POSUSER.TSF_TRANSACTION_COL\r\n"
				+ "WHERE OPERATION = 'PR2_CO_Affect' AND START_DT >= TRUNC(SYSDATE) \r\n"
				+ "AND STATUS = 'S' AND CODE = '000' AND ROWNUM <=10 ORDER BY START_DT DESC";
		String errorPaso12 = "No se encontro registro de la transaccion ejecutada con status: S y code: 000";
		SQLResult resultPaso12;
		
		//falta conseguir el id para utilizarlo en este query
		String queryPaso14 = "SELECT ID, PHYSICAL_INV_DT, STATUS, ACK_DATE, PARTITION_DATE FROM POSUSER.POS_TSF_ONLINE_HEAD\r\n"
				+ "WHERE PARTITION_DATE >= TRUNC(SYSDATE) AND STATUS = 'E'\r\n"
				+ "AND ROWNUM <=10 ORDER BY PARTITION_DATE DESC";
		String errorPaso14 = "No se encontraron registros de items transferidos con status: E";
		SQLResult resultPaso14;
		
		String queryPaso15 = "SELECT ID, OPERATION, START_DT, STATUS, CODE FROM POSUSER.TSF_TRANSACTION_COL \r\n"
				+ "WHERE OPERATION = 'PR2_CO_ACK' AND START_DT >= TRUNC(SYSDATE) AND STATUS = 'S'\r\n"
				+ "AND ROWNUM <=10 ORDER BY START_DT DESC";
		String errorPaso15 = "No se encontro registro de la transaccion ejecutada con status: S y code: 000";
		SQLResult resultPaso15;
		
		String queryPaso16 = "SELECT * FROM WMLOG.WM_LOG_ERROR_TPE WHERE ROWNUM <=5";
		SQLResult resultPaso16;
		
		String queryPaso17 = "SELECT ERROR_ID, ERROR_DATE, DESCRIPTION FROM WMLOG.WM_LOG_ERROR_TPE\r\n"
				+ "WHERE ERROR_DATE >= TRUNC(SYSDATE)\r\n"
				+ "AND ROWNUM <= 5 ORDER BY ERROR_DATE DESC";
		String errorPaso17 = "Se encontraron registros de errores para los servicios de la interfaz PR2_CO";
		SQLResult resultPaso17;
		
		/*******Paso 1*****************************************************************************/
		step("Ejecutar desde un navegador web el siguiente request para el servicio runValidSku "
				+ "para solicitar una validacion de articulos");
		String runValidSkuXML = "/PR2_CO/runValidSku.txt";
		HashMap<String, String> datos = new HashMap<>();
		datos.put("host", host);
		datos.put("DESTINATIONAPP", "POS");
		datos.put("SENDER_ID", sender);
		datos.put("RECEIVER_ID", receiver);
		datos.put("NO_RECORDS", no_records);
		datos.put("SKU", sku);
		o.logRequestAndResponse(runValidSkuXML, datos);
		datos.clear();
		
		/*******Paso 2*****************************************************************************/
		step("Establecer conexión a la BD FCWM6QA");
		o.logHost(FCWM6QA_HOST);
		resultPaso2 = ejecutaQuery(queryPaso2, FCWM6QA);
		if (resultPaso2.isEmpty()) {
			o.mensajeError(defaultError);
		}else {
			o.log(succesfulConnection);
		}
		
		/*******Paso 3*****************************************************************************/
		step("Consultar la tabla TSF_TRANSACTION_COL para validar la ejecución del servicio PR2_CO_validSKU"
				+ " el cual finalizó con el code 000 y en estatus S exitosa, en la BD FCWM6QA");
		resultPaso3 = ejecutaQuery(queryPaso3, FCWM6QA);
		o.validaRespuestaQuery(resultPaso3);
		if (resultPaso3.isEmpty()) {
			o.muestraError(queryPaso3, errorPaso3);
		}else {
			o.log("Ejecucion correcta de la PR2_CO con estatus: S y code: 000");
		}
		
		/*******Paso 4*****************************************************************************/
		step("Desde un navegador web ejecutar el siguiente request para el servicio runInbound "
				+ "para solicitar de transferencias de Salida (Inbound)");
		String runInboundXML = "/PR2_CO/runInbound.txt";
		datos.put("host", host);
		datos.put("PV_DOC_ID", pv_doc_id);
		datos.put("SHIP_DATE", ship_date);
		datos.put("SENDER_ID", sender);
		datos.put("RECEIVER_ID", receiver);
		datos.put("EXT_REF_NO", ext_ref_no);
		datos.put("NO_RECORDS", no_records);
		datos.put("CTRL_CODE", ctrl_code);
		datos.put("ITEM", item);
		datos.put("SHIP_QTY", ship_qty);
		o.logRequestAndResponse(runInboundXML, datos);
		datos.clear();
		
		/*******Paso 5*****************************************************************************/
		step("Comprobar que se registre la transaccion en la tabla TSF_TRANSACTION_COL "
				+ "de la BD FCWM6QA para la operación PR2_CO_Inbound");
		resultPaso5 = ejecutaQuery(queryPaso5, FCWM6QA);
		o.validaRespuestaQuery(resultPaso5);
		if (resultPaso5.isEmpty()) {
			o.muestraError(queryPaso5, errorPaso5);
		}else {
			o.log("Se registró la transacción ejecutada correctamente con STATUS: S y CODE: 000");
		}
		
		/*******Paso 6*****************************************************************************/
		step("Validar que se insertó correctamente la información en la tabla POS_TSF_ONLINE_HEAD "
				+ "de la BD FCWM6QA con status igual a 'R'");
		resultPaso6 = ejecutaQuery(queryPaso6, FCWM6QA);
		o.validaRespuestaQuery(resultPaso6);
		if (resultPaso6.isEmpty()) {
			o.muestraError(queryPaso6, errorPaso6);
		}else {
			o.log("Se muestra correctamente la informacion con status: R");
		}
		
		/*******Paso 7*****************************************************************************/
		step("Validar que se insertó el detalle de la información en la tabla"
				+ " POS_TSF_ONLINE_DETL de la BD FCWM6QA con status igual a 'R'");
		resultPaso7 = ejecutaQuery(queryPaso7, FCWM6QA);
		o.validaRespuestaQuery(resultPaso7);
		if (resultPaso7.isEmpty()) {
			o.muestraError(queryPaso7, errorPaso7);
		}else {
			o.log("Muestra el detalle de la información de forma correcta");
		}
		
		/*******Paso 8*****************************************************************************/
		step("Desde un navegador web ejecutar el siguiente request para el servicio runOutbound "
				+ "desde el navegador para validar las de transferencias (Outbound)");
		String runOutbound = "/PR2_CO/runOutbound.txt";
		datos.put("host", host);
		datos.put("SENDER_ID", receiver);
		o.logRequestAndResponse(runOutbound, datos);
		datos.clear();
		
		/*******Paso 9*****************************************************************************/
		step("Comprobar que se registre la transacción en la tabla "
				+ "TSF_TRANSACTION_COL de la BD FCWM6QA para la operación PR2_CO_Outbound");
		resultPaso9 = ejecutaQuery(queryPaso9, FCWM6QA);
		o.validaRespuestaQuery(resultPaso9);
		if (resultPaso9.isEmpty()) {
			o.muestraError(queryPaso9, errorPaso9);
		}else {
			o.log("Se registro la transaccion ejecutada correctamente con CODE: 000 y status: S");
		}
		
		/*******Paso 10*****************************************************************************/
		step("Desde un navegador web ejecutar el siguiente request para el servicio runAffect "
				+ "desde el navegador para marcar las transferencias recibidas");
		String runAffect = "/PR2_CO/runAffect.txt";
		datos.put("host", host);
		datos.put("SENDER_ID", sender);
		datos.put("NO_RECORDS", no_records);
		datos.put("MOV_TYPE", mov_type);
		datos.put("PV_DOC_ID", pv_doc_id);
		datos.put("SHIP_DATE", ship_date);
		datos.put("RECEIVER_ID", receiver);
		datos.put("EXT_REF_NO", ext_ref_no);
		datos.put("CTRL_CODE", ctrl_code);
		o.logRequestAndResponse(runAffect, datos);
		datos.clear();
		
		/*******Paso 11*****************************************************************************/
		step("Validar que tambien se muestre la informacion actualizada "
				+ "en la tabla POS_TSF_ONLINE_DETL de la BD FCWM6QA, con los items transferidos");
		resultPaso11 = ejecutaQuery(queryPaso11, FCWM6QA);
		o.validaRespuestaQuery(resultPaso11);
		if (resultPaso11.isEmpty()) {
			o.muestraError(queryPaso11, errorPaso11);
		}else {
			o.log("Se muestra la informacion de los items que se transfirieron, con STATUS: I");
		}
		
		/*******Paso 12*****************************************************************************/
		step("Comprobar que se registre la transaccion en la tabla TSF_TRANSACTION_COL "
				+ "de la BD FCWM6QA para la operación PR2_CO_Affect");
		resultPaso12 = ejecutaQuery(queryPaso12, FCWM6QA);
		o.validaRespuestaQuery(resultPaso12);
		if (resultPaso12.isEmpty()) {
			o.muestraError(queryPaso12, errorPaso12);
		}else {
			o.log("Se registro la transaccion ejecutada correctamente");
		}
		
		/*******Paso 13*****************************************************************************/
		step("Desde un navegador web ejecutar el siguiente request para el servicio "
				+ "runACK desde el navegador para confirmar las transferencias fueron recibidas");
		String runACK = "/PR2_CO/runACK.txt";
		datos.put("host", host);
		datos.put("SENDER_ID", receiver);
		datos.put("IF_DATE", ship_date);
		datos.put("NO_RECORDS", no_records);
		datos.put("RECEIVER_ID", sender);
		datos.put("NO_RECORDS", no_records);
		datos.put("PV_DOC_ID", pv_doc_id);
		//evidencia manual indica que la ejecucion del request no genera respuesta
		String request = GetRequestFile.getRequestFile(runACK, datos);
		o.log("Request: \r\n"+request);
		datos.clear();
		
		/*******Paso 14*****************************************************************************/
		step("Validar que tambien se muestre la informacion actualizada en la tabla "
				+ "POS_TSF_ONLINE_DETL de la BD FCWM6QA, con los items transferidos");
		resultPaso14 = ejecutaQuery(queryPaso14, FCWM6QA);
		o.validaRespuestaQuery(resultPaso14);
		if (resultPaso14.isEmpty()) {
			o.muestraError(queryPaso14, errorPaso14);
		}else {
			o.log("Se muestra la informacion de los items que se transfirieron "
					+ "con STATUS: E, PHYSICAL_INV_DT y ACK_DATE: "+o.generaFecha());
		}
		
		/*******Paso 15*****************************************************************************/
		step("Comprobar que se registre la transaccion en la tabla TSF_TRANSACTION_COL "
				+ "de la BD FCWM6QA para la operacion PR2_CO_ACK");
		resultPaso15 = ejecutaQuery(queryPaso15, FCWM6QA);
		o.validaRespuestaQuery(resultPaso15);
		if (resultPaso15.isEmpty()) {
			o.muestraError(queryPaso15, errorPaso15);
		}else {
			o.log("Se registro la transaccion ejecutada correctamente con status: S y code: 000");
		}
		
		
		/*******Paso 16*****************************************************************************/
		step("Establecer conexion a la BD FCWMLQA.FEMCOM.NET del server oxfwm6q00.femcom.net.");
		o.logHost(FCWMLQA_HOST);
		resultPaso16 = ejecutaQuery(queryPaso16, FCWMLQA);
		if (resultPaso16.isEmpty()) {
			o.mensajeError(defaultError);
		}else {
			o.log(succesfulConnection);
		}
		
		
		/*******Paso 17*****************************************************************************/
		step("Ejecutar la siguiente consulta en la base de datos FCWMLQA para validar "
				+ "que no se encuentren registros de error de la PR2_CO");
		resultPaso17 = ejecutaQuery(queryPaso17, FCWMLQA);
		o.validaRespuestaQuery(resultPaso17);
		if (resultPaso17.isEmpty()) {
			o.log("No se encontro ningun registro de error para la interfaz PR2_CO");
		}else {
			o.mensajeError(errorPaso17);
		}
		
		
		
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
	
	public void printQuery(String query) {
		System.out.println("\r\n#----- Query Ejecutado -----#\r\n");
		System.out.println(query+"\r\n");
		System.out.println("#---------------------------#\r\n");
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Prueba de regresion para comprobar la no afectacion en la funcionalidad principal "
				+ "de la interface FEMSA_PR02_CO para validar articulos a transferir, "
				+ "transferir articulos de salida y transferir articulos de entrada "
				+ "al ser migrada de WM9.9 a WM10.5";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_003_pr2_co_TransferenciaVariosArticulos_";
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
