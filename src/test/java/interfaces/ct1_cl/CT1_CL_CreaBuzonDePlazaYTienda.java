package interfaces.ct1_cl;

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

public class CT1_CL_CreaBuzonDePlazaYTienda extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_CT1_CL_Verificar_procesamiento_Crear_Buzon_Plaza_Tienda(HashMap<String, String> data) throws Exception {

		/* Utilerias
		 *********************************************************************/
		
		/*
		 * 
		 * Modificado por mantenimiento.
		 * @author Brandon Ruiz.
		 * @date   13/02/2023.
		 * @cp Verificar el procesamiento de la interfaz - Crear el Buzon de la Plaza y Tienda (runBuzon).
		 * @projectname LOGV2
		 *
		 */
		
		/**
		 * ALM (son los pasos de la de mexico pero con bases de datos de chile)
		 * Verificar el procesamiento de la interfaz - Crear el Buzon de la Plaza y Tienda (runBuzon).
		 */
		
		String dbLogCL_HOST = GlobalVariables.DB_HOST_LogChile;
		SQLUtil dbLogCL = new SQLUtil(GlobalVariables.DB_HOST_LogChile, GlobalVariables.DB_USER_LogChile,
				GlobalVariables.DB_PASSWORD_LogChile);
		String dbCNTCL_HOST = GlobalVariables.DB_HOST_CNTCHILE;
		SQLUtil dbCNTCL = new SQLUtil(GlobalVariables.DB_HOST_CNTCHILE, GlobalVariables.DB_USER_CNTCHILE,
				GlobalVariables.DB_PASSWORD_CNTCHILE);
		
		Util o = new Util(testCase);

		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 */
		
		//Variables Generales
		String cr_plaza = data.get("plaza");
		String cr_tienda = data.get("tienda");
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String run_id = "";
		
		// Paso 1
		//Comentado puesto que no hay plazas ni tiendas predefinidas
//		String ValidaSatusL = "SELECT ID, CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON\r\n"
//				+ "   FROM WM_BUZONES_T_TIENDAS \r\n" + " WHERE WM_STATUS_BUZON = 'L' \r\n" + "    AND CR_PLAZA = '"
//				+ data.get("plaza") + "' \r\n" + "    AND CR_TIENDA = '" + data.get("tienda") + "'\r\n"
//				+ "  ORDER BY CREATION_DATE";
		
		String queryPaso1 = "SELECT ID, CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON\r\n"
				+ "FROM WMUSER.WM_BUZONES_T_TIENDAS WHERE WM_STATUS_BUZON = 'L' \r\n"
				+ "AND ROWNUM <= 10 ORDER BY CREATION_DATE";
		
		// Paso 3
		String queryPaso3 = "SELECT RUN_ID, INTERFACE, START_DT, STATUS, SERVER FROM WMLOG.WM_LOG_RUN\r\n"
				+ "WHERE INTERFACE LIKE '%CT1_CL_BUZON%'\r\n"
				+ "AND START_DT >= TRUNC(SYSDATE)\r\n"
				+ "AND STATUS = 'S' AND ROWNUM <=10\r\n"
				+ "ORDER BY START_DT DESC";

		// Paso 4
		String queryPaso4 = "SELECT THREAD_ID, PARENT_ID,NAME, START_DT,END_DT,STATUS FROM WMLOG.WM_LOG_THREAD "
				+ "WHERE PARENT_ID = '%s'";

		// Paso 5

		//Comentado puesto que no hay plazas ni tiendas predefinidas
//		String ValidaSatusE = "SELECT ID, CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON, WM_FECHA_PROC\r\n"
//				+ "   FROM WM_BUZONES_T_TIENDAS \r\n" + "WHERE WM_STATUS_BUZON = 'E' \r\n" + "     AND CR_PLAZA = '"
//				+ data.get("plaza") + "'\r\n" + "     AND CR_TIENDA = '" + data.get("tienda") + "'\r\n"
//				+ "     AND TRUNC(WM_FECHA_PROC) = TRUNC(SYSDATE)\r\n" + "     AND WM_RUN_ID = '%s'\r\n"
//				+ " ORDER BY CREATION_DATE ";
		
		String queryPaso5;

		/***********************************Pasos del caso de Prueba*****************************************/
		

//**********************************************Paso 1	****************************************************************************

		step("Verificar que existan la plaza y la tienda en la tabla WM_BUZONES_T_TIENDAS con WM_STATUS_BUZON = 'L' ");
		o.logHost(dbCNTCL_HOST);
		SQLResult resultPaso1 = ejecutaQuery(queryPaso1,dbCNTCL);
		o.validaRespuestaQuery(resultPaso1);
		if (resultPaso1.isEmpty()) {
			o.muestraError(queryPaso1, "No se encontro registro de la plaza y tienda");
		}else {
			//en caso de que se tengan definidas la plaza y tienda, quitar este else y modificar la consulta de este paso
			cr_plaza = resultPaso1.getData(0, "CR_PLAZA");
			cr_tienda = resultPaso1.getData(0, "CR_TIENDA");
			o.log("recupera cr_plaza: "+cr_plaza+" y cr_tienda: "+cr_tienda+" del primer registro" );
		}

//**********************************************Paso 2	****************************************************************************** 

		step("Ejecutar el JOB runCT1Buzon desde Control M para invocar la interface por medio del servicio CT1.Pub:runBuzon ");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String url = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(url);
		String dateExecution = pok.runIntefaceWmTwoButtonsWihtoutInputs10(data.get("interface"), data.get("servicio"));
		o.log("Respuesta dateExecution" + dateExecution);
		
//*******************************************************Paso 3************************************************************************

		step("Validar que la interface haya finalizado correctamente en el WMLOG");
		o.logHost(dbLogCL_HOST);
		SQLResult resultPaso3 = ejecutaQuery(queryPaso3, dbLogCL);
		o.validaRespuestaQuery(resultPaso3);
		if (resultPaso3.isEmpty()) {
			o.muestraError(queryPaso3, "No se encontro ningun registro de ejecucion de la interfaz");
		}else{
			run_id = resultPaso3.getData(0, "RUN_ID");
			o.log("recupera run_id: "+run_id+" del primer registro");
		}

//**************************************************Paso 4 **************************************************************************
		step("Verificar que los threads de la interfaz finalizaron correctamente.");
		o.logHost(dbLogCL_HOST);
		String queryPaso4_1 = String.format(queryPaso4, run_id);
		SQLResult resultPaso4 = ejecutaQuery(queryPaso4_1, dbLogCL);
		o.validaRespuestaQuery(resultPaso4);
		if (resultPaso4.isEmpty()) {
			o.muestraError(queryPaso4_1, "No se encontro ningun thread con parent_id: "+run_id);
		}

//*******************************************Script 1************************************************************************************

		/*
		 * Verificar que el directorio fue creado en el filesystem,
		 * 
		 * PATH=/u01/posuser/FEMSA_OXXO/POS/[PLAZA]/[TIENDA]/
		 * DIRECTORIO=backup,working,recovery,outbox,duplicate.
		 */

//**************************************************Paso 5 ***************************************************************************		
		step("Verificar que se actualice el registro de la tabla WM_BUZONES_T_TIENDAS, WM_STATUS_BUZON = E,WM_RUN_ID = [WM_LOG_RUN.RUN_ID], WM_FECHA_PROC = SYSDATE.");
		o.logHost(dbCNTCL_HOST);
		//modificar consulta en caso de que se tenga la plaza y la tienda predefinidas
		queryPaso5 = "SELECT ID, CR_PLAZA, CR_TIENDA, WM_STATUS_BUZON, WM_FECHA_PROC \r\n"
				+ "FROM WM_BUZONES_T_TIENDAS WHERE WM_STATUS_BUZON = 'E' \r\n" 
				+ "AND CR_PLAZA = '"+ cr_plaza + "' AND CR_TIENDA = '" + cr_tienda + "'\r\n"
				+ "AND TRUNC(WM_FECHA_PROC) = TRUNC(SYSDATE) AND WM_RUN_ID = '%s'\r\n"
				+ "ORDER BY CREATION_DATE ";
		String queryPaso5_1 = String.format(queryPaso5, run_id);
		SQLResult resultPaso5 = ejecutaQuery(queryPaso5_1, dbCNTCL);
		o.validaRespuestaQuery(resultPaso5);
		if (resultPaso5.isEmpty()) {
			o.muestraError(queryPaso5_1, "No se actualizo correctamente el registro");
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
		return "Construida. Verificar el procesamiento de la interfaz crear buzon plaza y tienda";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_005_CT1_CL_Verificar_procesamiento_Crear_Buzon_Plaza_Tienda";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
