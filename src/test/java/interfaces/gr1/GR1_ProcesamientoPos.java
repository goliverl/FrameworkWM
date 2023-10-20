package interfaces.gr1;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.Util;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class GR1_ProcesamientoPos extends BaseExecution {
	
	/*
	 * 
	 * Modificado por mantenimiento.
	 * @author Brandon Ruiz.
	 * @date   31/01/2023.
	 * @cp Verifcar el procesamiento de la interfaz POS plaza 13GDB, 
	 * Verifcar el procesamiento de la interfaz RETEK plaza 13GDB
	 * @projectname LOGV2
	 *
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_gr1_ProcesamientoPos(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/
		
		Util o = new Util(testCase);
		
// dbEbs no existe...
//		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Ebs,
//				GlobalVariables.DB_USER_Ebs,GlobalVariables.DB_PASSWORD_Ebs);

//dbPuser tiene esquema con la tabla GAS_INBOUND_DOCS, pero no las demás
//		utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Puser, 
//				GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
//dbRms no tiene el esquema POSUSER
		String dbRms_HOST = GlobalVariables.DB_HOST_RMS_MEX;
		utils.sql.SQLUtil dbRms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_MEX,
				GlobalVariables.DB_USER_RMS_MEX,GlobalVariables.DB_PASSWORD_RMS_MEX);

//dbLog no tiene el esquema POSUSER
		String dbLog_HOST = GlobalVariables.DB_HOST_FCWMLQA;
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		
//AVEBQA_QAVIEW tampoco tiene el esquema POSUSER
		
//FCWMQA tiene esquema con la tabla GAS_INBOUND_DOCS, pero no las demás
		
		/*
		 * Variables
		 *********************************************************************/
		String queryPaso1 ="SELECT a.ID, a.STATUS, a.DOC_TYPE, a.PV_DOC_NAME, b.NO_RECORDS, c.ITEM \r\n" + 
				"FROM POSUSER.GAS_INBOUND_DOCS a, POSUSER.GAS_TSF b, POSUSER.GAS_TSF_DETL c \r\n" + 
				"WHERE a.ID = b.GID_ID \r\n" + 
				"AND a.ID = c.GID_ID \r\n" + 
				"AND a.DOC_TYPE = 'TSF' \r\n" + 
				"AND a.STATUS = 'I' \r\n" + 
				"AND b.NO_RECORDS > 0 \r\n" + 
				"and SubStr(a.PV_DOC_NAME,4,5) = '" + data.get("plaza") + "'";
		SQLResult resultPaso1;
		
		String queryPaso3 = "SELECT * FROM wmlog.wm_log_run \r\n" + 
				"WHERE interface = 'GR1main' \r\n" + 
				"AND start_dt>=TRUNC(SYSDATE) \r\n" + 
				"ORDER BY start_dt DESC \r\n";
		SQLResult resultPaso3;
		
		String queryPaso3_1 = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER \r\n"
				+ "FROM WMLOG.WM_LOG_RUN \r\n"
				+ "WHERE RUN_ID =%s";
		SQLResult resultPaso3_1;
		
		
		String queryPaso4 = "SELECT parent_id,thread_id,name,wm_log_thread.status,att1,att2 \r\n"+
				"FROM WMLOG.wm_log_thread \r\n" + 
				"WHERE parent_id = %s"; // FCWMLQA
		SQLResult resultPaso4;
		

		
		String queryPaso5 = "SELECT a.id, a.pv_doc_id, a.target_id, a.received_date \r\n" + 
				"FROM posuser.gas_inbound_docs a, posuser.gas_tsf b \r\n" + 
				"WHERE a.id=b.gid_id \r\n" + 
				"AND a.doc_type='TSF' \r\n" + 
				"AND b.no_records>0  \r\n" + 
				"AND status='E' \r\n" + 
				"and SubStr(a.pv_doc_name,4,5) = '" + data.get("plaza") + "' \r\n"+
				"and a.target_id = '%s' \r\n";
		SQLResult resultPaso5;

		// utileria
		String status = "S";

		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String url = "http://" + user + ":" + ps + "@" + server;
		String run_id = "";
		
		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 */

//		Paso 1	************************		

		step("Validar que existan datos a procesar por la interface "
		+ "para la plaza correspondiente en las tablas GAS_INBOUND_DOCS, GAS_TSF y GAS_TSF_DETL "
		+ "de POSUSER con DOC_TYPE = 'TSF' y STATUS = 'I'." );
		o.logHost(dbRms_HOST);
		resultPaso1 = ejecutaQuery(queryPaso1, dbRms);
		o.validaRespuestaQuery(resultPaso1);
		if (resultPaso1.isEmpty()) {
			o.muestraError(queryPaso1,"No existen datos a procesar");
		}else {
			
		}

//		Paso 2	**************************************************************************************************************************************************

		step("Ejecutar el servicio GO01.Pub:run desde el Job runGO01 de Ctrl-M para procesar la informacion y de los movimientos de compras y ventas para dela plaza.");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		u.get(url);
		pok.runIntefaceWmTwoButtonsWihtoutInputs10(data.get("interfase"), data.get("servicio"));
		
		// Paso 3 ************************
		step("Validar la correcta ejecucion de la interface GO01 en la tabla WM_LOG_RUN de WMLOG.");
		o.logHost(dbLog_HOST);
		resultPaso3 = ejecutaQuery(queryPaso3, dbLog);
		o.validaRespuestaQuery(resultPaso3);
		if (!resultPaso3.isEmpty()) {
			String status1 = resultPaso3.getData(0, "STATUS");
			run_id = resultPaso3.getData(0, "RUN_ID");
			o.log("Recupera run_id: "+run_id+" y status: "+status1+" del primer registro");
		}else {
			o.muestraError(queryPaso3, "La consulta no devolvio ningun registro");
		}
		
		resultPaso3_1 = ejecutaQuery(queryPaso3_1, dbLog);
		o.validaRespuestaQuery(resultPaso3_1);
		if (resultPaso3_1.isEmpty()){
			o.muestraError(queryPaso3_1, "La consulta no devolvio ningun registro");
		}

//		Paso 4  *************************************************
		step("Validar la correcta ejecucion de los Threads lanzados por la interface GO01 en la tabla WM_LOG_THREAD de WMLOG.");
		String queryPaso4_1 = String.format(queryPaso4, run_id);
		resultPaso4 = ejecutaQuery(queryPaso4_1, dbLog);
		o.validaRespuestaQuery(resultPaso4);
		if (!resultPaso4.isEmpty()) {
			String estatusThread = resultPaso4.getData(0, "Status");
			o.log("Recupera el status: "+estatusThread+" del primer registro");
			boolean SR = estatusThread.equals(status);
			if (SR) {
				o.log("El estatus del thread: "+estatusThread+" es el esperado: "+status);
			}else {
				o.mensajeError("El estatus del thread: "+estatusThread+" no es el esperado: "+status);
			}
		}else {
			o.muestraError(queryPaso4_1, "La consulta no devolvio ningun registro");
		}
		
//		Paso 5	************************		

		step("Verificar si se actualizo el estatus a E (Enviado) en la tabla POSUSER.GAS_INBOUND_DOCS de la base de datos." );
		resultPaso5 = ejecutaQuery(queryPaso5, dbRms);
		o.validaRespuestaQuery(resultPaso5);
		if (resultPaso5.isEmpty()) {
			o.muestraError(queryPaso5, "La consulta no devolvio ningun registro");
		}
												
	}
			
	int contador = 0;
	private void step(String step) {
		contador++;
		System.out.println("\r\nStep "+contador+"-"+step);
		addStep(step);
	}
	
	private void printQuery(String query) {
		System.out.println("\r\n#----- Query Ejecutado -----#\r\n");
		System.out.println(query+"\r\n");
		System.out.println("#---------------------------#\r\n");
	}
	
	
	private SQLResult ejecutaQuery(String query, SQLUtil obj) throws Exception{
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
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Verifcar el procesamiento de la interfaz POS plaza 13GDB ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_gr1_ProcesamientoPos";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
}
