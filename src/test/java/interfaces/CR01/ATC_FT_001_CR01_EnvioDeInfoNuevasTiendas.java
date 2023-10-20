package interfaces.CR01;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;
import org.json.JSONObject;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ATC_FT_001_CR01_EnvioDeInfoNuevasTiendas extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_CR01_EnvioDeInfoNuevasTiendas_test(HashMap<String, String> data) throws Exception {

		/**
		 * UTILERIA
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbCNT = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCIASQA,GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		utils.sql.SQLUtil dbWms = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_fcwmesit,GlobalVariables.DB_USER_fcwmesit, GlobalVariables.DB_PASSWORD_fcwmesit);
		utils.sql.SQLUtil dbMuat = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCMOMUAT,GlobalVariables.DB_USER_FCMOMUAT, GlobalVariables.DB_PASSWORD_FCMOMUAT);
		utils.sql.SQLUtil dbSuat = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCDASUAT,GlobalVariables.DB_USER_FCDASUAT, GlobalVariables.DB_PASSWORD_FCDASUAT);
/**
 * Proyecto: Actualizacion tecnologica
 * CP: MTC-FT-001 CR01 Envio de informacion de nuevas tiendas a traves de la interface CR01
 * Descripcion: Prueba de regresion  para comprobar la no afectacion en la funcionalidad principal de la interface FEMSA_CR01 
 * para envio de informacion de nuevas tiendas desde CNT hacia RMSv16, al ser migrada de WM9.9 a WM10.5
 * Autor: Oliver Martinez
 * 
 */
		
		/**
		 * VARIABLES
		 *********************************************************************/


//		Paso 1
		String ValidStore = "SELECT STORE, STORE_NAME, STORE_NAME10, WM_STATUS, SISTER_STORE \r\n"
				+ "FROM WMUSER.WM_STORE_ADD \r\n"
				+ "WHERE WM_STATUS = 'L' ";
//		 Paso 3
		String ValidStore2 = " SELECT STORE, STORE_NAME, STORE_NAME10, WM_STATUS, SISTER_STORE \r\n"
				+ "FROM WMUSER.WM_STORE_ADD \r\n"
				+ "WHERE WM_STATUS = 'E' ";
//			Paso 4 
		String ValidLog = "SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS \r\n"
				+ "FROM WMLOG.WM_LOG_RUN \r\n"
				+ "WHERE INTERFACE LIKE '%CR01%' \r\n"
				+ "AND STATUS = 'S' \r\n"
				+ "AND START_DT >= TRUNC(SYSDATE) \r\n"
				+ "ORDER BY END_DT DESC";
//    		Paso 5
		String ValidThread = "SELECT THREAD_ID, PARENT_ID, NAME, END_DT, STATUS \r\n"
				+ "FROM  WMLOG.WM_LOG_THREAD \r\n"
				+ "WHERE PARENT_ID = '%s' \r\n"
				+ "AND STATUS = 'S'";

//			Paso 6 
		String ValidError = "SELECT ERROR_ID, RUN_ID, ERROR_DATE, ERROR_CODE \r\n"
				+ "FROM WMLOG.WM_LOG_ERROR 	\r\n"
				+ "WHERE  RUN_ID = '%s' \r\n"
				+ "ORDER BY ERROR_DATE  DESC";
//			Paso 7 
		String ValidMSG = "SELECT CTRL_ID, MSG_TYPE, CREATION_DATE, WM_CODE \r\n"
				+ "FROM WMUSER.RIB_STORE_CONTROL \r\n"
				+ "WHERE MSG_TYPE = 'XStoreCre' \r\n"
				+ "AND CREATION_DATE >= TRUNC(SYSDATE)";
//			Paso 8 
		String ValidResp = "SELECT * \r\n"
				+ "FROM WMUSER.RIB_STORE_MSG_REP \r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE)";
//			Paso 9
		String ValidStag = "SELECT STORE, STORE_NAME, STORE_NAME10, STORE_CLASS, STORE_OPEN_DATE, RMS_ASYNC_ID \r\n"
				+ "FROM RMS.STORE_ADD \r\n"
				+ "WHERE STORE = '%s'";
//			Paso 10
		String ValidAsync = "SELECT RMS_ASYNC_ID, STATUS, JOB_TYPE, CREATE_DATETIME, LAST_UPDATE_DATETIME \r\n"
				+ "FROM RMS.RMS_ASYNC_STATUS  \r\n"
				+ "WHERE RMS_ASYNC_ID = '%s' ";
//			 Paso 11
		String NewStore = "SELECT STORE, STORE_NAME, STORE_NAME10, CREATE_DATETIME \r\n"
				+ "FROM RMS.STORE \r\n"
				+ "WHERE STORE = '%s'";
//			Paso 12
		String Query1 = " SELECT STORE, STORE_NAME, STORE_NAME10, STORE_OPEN_DATE \r\n"
				+ "FROM RMS16_V.V_STORE \r\n"
				+ "WHERE STORE='" + data.get("Store") + "'";
		
		String Query2 = "SELECT ADDR_KEY, MODULE, KEY_VALUE_1, SEQ_NO,ADDR_TYPE, PRIMARY_ADDR_IND \r\n"
				+ "FROM RMS16_V.V_ADDR \r\n"
				+ "WHERE ADDR_KEY ='" + data.get("Store") + "'";
		
		String Query3 = " SELECT STORE, GROUP_ID, VARCHAR2_1, VARCHAR2_2 \r\n"
				+ "FROM RMS16_V.V_STORE_CFA_EXT \r\n"
				+ "WHERE STORE='" + data.get("Store") + "'";
		
		String Query4 = "SELECT COMPANY, REGION, DISTRICT, STORE, CREATE_ID \r\n"
				+ "FROM RMS16_V.V_STORE_HIERARCHY \r\n"
				+ "WHERE STORE='" + data.get("Store") + "'";
		
		String Query5 = " SELECT ITEM, LOC,ITEM_PARENT, LOC_TYPE, UNIT_RETAIL, REGULAR_UNIT_RETAIL \r\n"
				+ "FROM RMS16_V.V_ITEM_LOC \r\n"
				+ "WHERE LOC ='" + data.get("Store") + "' \r\n"
				+ "AND CREATE_DATETIME >= TRUNC(SYSDATE)";
		
		String Query6 = "SELECT ITEM, SUPPLIER, ORIGIN_COUNTRY_ID, LOC,LOC_TYPE, PRIMARY_LOC_IND \r\n"
				+ "FROM RMS16_V.V_ITEM_SUPP_COUNTRY_LOC \r\n"
				+ "WHERE LOC='" + data.get("Store") + "' \r\n"
				+ "AND CREATE_DATETIME >= TRUNC(SYSDATE)";
		
		String Query7 = "SELECT ITEM, LOCATION, LOC_TYPE, ITEM_PARENT \r\n"
				+ "FROM RMS16_V.V_REPL_ITEM_LOC LOC  \r\n"
				+ "WHERE LOCATION='" + data.get("Store") + "'";
		
		String Query8 = "SELECT ITEM, LOC, LOC_TYPE, UDA_ID, CREATE_ID, CREATE_DATETIME \r\n"
				+ "FROM RMS16_V.V_NB_ITEM_LOC \r\n"
				+ "WHERE LOC='" + data.get("Store") + "' \r\n"
				+ "AND CREATE_DATETIME >= TRUNC(SYSDATE)";

//				 Paso 13
		String QueryDelta1 = "SELECT KEY_VALUE_1, ID_SEQ_DISTRICT, ADDR_KEY, MODULE \r\n"
				+ "FROM RMS_DT.DT_ADDR \r\n"
				+ "WHERE KEY_VALUE_1 = '" + data.get("Store") + "'";
		
		String QueryDelta2 = "SELECT SEQ_ID_DT_STORE_CFA, STORE, GROUP_ID \r\n"
				+ "FROM RMS_DT.DT_STORE_CFA_EXT "
				+ "WHERE STORE = '" + data.get("Store") + "'";
		
		String QueryDelta3 = "SELECT ITEM, LOC, LOC_TYPE, UDA_ID, LAST_UPDATE_DATETIME \r\n"
				+ "FROM RMS_DT.DT_NB_ITEM_LOC \r\n"
				+ "WHERE LOC = '" + data.get("Store") + "'"
				+ "AND CREATE_DATETIME >= TRUNC(SYSDATE)";

//					PAso 14 
		String Stag1 = "SELECT JOB_TYPE, JOB_DESCRIPTION, QUEUE_TABLE, QUEUE_NAME \r\n"
				+ "FROM RMS.RMS_ASYNC_JOB \r\n"
				+ "WHERE JOB_TYPE = 'STORE_ADD'";
		
		String Stag2 = "SELECT RMS_ASYNC_ID, JOB_TYPE, STATUS, CREATE_ID, CREATE_DATETIME \r\n"
				+ "FROM RMS.RMS_ASYNC_STATUS \r\n"
				+ "WHERE JOB_TYPE = 'STORE_ADD' \r\n"
				+ "AND CREATE_DATETIME >= TRUNC(SYSDATE) \r\n"
				+ "ORDER BY CREATE_DATETIME DESC";
		
		String Stag3 = "SELECT RMS_ASYNC_ID,RETRY_ATTEMPT_NUM,RETRY_USER_ID,LAST_UPDATE_DATETIME \r\n"
				+ "FROM RMS.RMS_ASYNC_RETRY \r\n"
				+ "WHERE RMS_ASYNC_ID = '%s'";


		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep(" Validar información de la tabla WM_STORE_ADD del esquema WMUSER de la BD XXCNT-WMUSER, filtrando la información por el campo WM_STATUS igual a ‘L’.");

		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		System.out.println(ValidStore);
		SQLResult ValidStoreRes = executeQuery(dbCNT, ValidStore);
		boolean ValidStoreResL = ValidStoreRes.isEmpty();
		if (!ValidStoreResL) {
			testCase.addTextEvidenceCurrentStep("Se encontro información de la tabla WM_STORE_ADD");
		}
		testCase.addQueryEvidenceCurrentStep(ValidStoreRes);
		assertFalse(ValidStoreResL, "No se encontro registro en la tabla WM_STORE_ADD");

		/* PASO 2 *********************************************************************/

		/*
		 * enviar un correo electrónico a los operadores USU
		 * UsuFEMCOMOperadoresSITE@oxxo.com Job name: runCR01 Comando: runInterfase.sh
		 * CR01 Host : tqa_clu_int_105_2 (FCWMINTQA3 , FCWMQA8D) Usuario: isuser Grupo:
		 * TQA_BO_105
		 */

		addStep("Solicitar el ordenamiento del Job runCR01, en Control M para su ejecución.");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);

		JSONObject obj = new JSONObject(data.get("job"));

		testCase.addBoldTextEvidenceCurrentStep("Jobs en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
		// testCase.addPaso("Paso con addPaso");
		testCase.addBoldTextEvidenceCurrentStep("Login");
		String url = "http://" + data.get("server") + ":18080"+ "/ControlM/";
		//http://10.184.56.194:18080/ControlM/
		u.get(url);
		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn();

		// Buscar del job
		testCase.addBoldTextEvidenceCurrentStep("Inicio de job ");
		ControlM control = new ControlM(u, testCase, obj);
		boolean flag = control.searchJob();
		assertTrue(flag);

		// Ejecucion
		String resultado = control.executeJob();
		System.out.println("Resultado de la ejecucion -> " + resultado);

		u.hardWait(30);

		// Valor del output

		String Res2 = control.getNewStatus();

		System.out.println("Valor de output getNewStatus:" + Res2);

		String output = control.getOutput();
		System.out.println("Valor de output control:" + output);

		testCase.addTextEvidenceCurrentStep("Status de ejecucion: " + Res2);
		testCase.addTextEvidenceCurrentStep("Output de ejecucion: " + output);
		// Validacion del caso
		Boolean casoPasado = false;
		if (Res2.equals("Ended OK")) {
			casoPasado = true;
		}

		control.closeViewpoint();
		u.close();
		assertTrue(casoPasado);
//		 assertNotEquals("Failure",resultado);

		/* PASO 3 *********************************************************************/

		addStep("Validar de nuevo información de la tabla WM_STORE_ADD del esquema WMUSER de la BD XXCNT-WMUSER, filtrando la información por el campo WM_STATUS = ‘E’");

		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		System.out.println(ValidStore2);
		SQLResult ValidStore2Res = executeQuery(dbCNT, ValidStore2);
		boolean ValidStore2ResL = ValidStore2Res.isEmpty();
		if (!ValidStore2ResL) {
			testCase.addTextEvidenceCurrentStep("Se encontro información de la tabla WM_STORE_ADD");
		}
		testCase.addQueryEvidenceCurrentStep(ValidStore2Res);
		System.out.println(ValidStore2ResL);
		assertFalse(ValidStore2ResL, "No se encontro registro en la tabla WM_STORE_ADD");

		/* PASO 4 *********************************************************************/

		addStep("Validar la correcta ejecución de la interface CR01 en la tabla WM_LOG_RUN de BD FCWMLQA.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ValidLogRes = executeQuery(dbLog, ValidLog);
		String Run_ID = "";
		boolean ValidLogResResL = ValidLogRes.isEmpty();

		if (!ValidLogResResL) {
			testCase.addTextEvidenceCurrentStep("Se encontro registro de ejecucion correcta");
			Run_ID = ValidLogRes.getData(0, "RUN_ID");
		}
		testCase.addQueryEvidenceCurrentStep(ValidLogRes);
		System.out.println(ValidLogResResL);
		assertFalse(ValidLogResResL, "No se encontro registro de ejecucion correcta");

		/* PASO 5 *********************************************************************/

		addStep("Validar la correcta ejecucion de los Threads lanzados por la interface CR01  en la tabla WM_LOG_THREAD  de BD FCWMLQA.");
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String ValidThreadFormat = String.format(ValidThread, Run_ID);
		System.out.println(ValidThreadFormat);
		SQLResult ValidThreadERes = executeQuery(dbLog, ValidThreadFormat);
		boolean ValidThreadE = ValidThreadERes.isEmpty();
		if (!ValidThreadE) {
			testCase.addTextEvidenceCurrentStep("Se encontro registro de ejecucion correcta en threads");
		}
		testCase.addQueryEvidenceCurrentStep(ValidThreadERes);
		System.out.println(ValidThreadE);
		assertFalse(ValidThreadE, "No se encontro registro de ejecucion correcta en threads");

		/* PASO 6 *********************************************************************/

		addStep("Validar que no se genro errores en la ejecución de la interface CR01, en la tabla WM_LOG_ERROR con la siguiente ejecución.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String ValidErrorFormat = String.format(ValidError, Run_ID);
		System.out.println(ValidErrorFormat);
		SQLResult paso7 = executeQuery(dbLog, ValidErrorFormat);
		boolean step7 = paso7.isEmpty();
		if (step7) {
			testCase.addTextEvidenceCurrentStep("OK - NO se encontro registro de ejecucion incorrecta ");
		}
		testCase.addQueryEvidenceCurrentStep(paso7);
		System.out.println(step7);
		assertTrue(step7, "Se encontro error en el log");

		/*
		 * PASO 7
		 ********************************************************************************/
		addStep("Validar envío de mensaje XStoreCre a RIB en la BD FCWMFSIT con la tabla WMUSER.RIB_STORE_CONTROL.");

		System.out.println(GlobalVariables.DB_HOST_fcwmesit);
		System.out.println(ValidMSG);
		SQLResult ValidMSGERes = executeQuery(dbWms, ValidMSG);
		boolean ValidMSGE = ValidMSGERes.isEmpty();
		if (!ValidMSGE) {
			testCase.addTextEvidenceCurrentStep("Se encontro registro de envío de mensaje XStoreCre a RIB en la BD FCWMFSIT");
		}
		testCase.addQueryEvidenceCurrentStep(ValidMSGERes);
		System.out.println(ValidMSGE);
		assertFalse(ValidMSGE, "No se encontro registro de ejecucion correcta en threads");

		/*
		 * PASO 8
		 ********************************************************************************/

		addStep("Validar que no exista un registro del  respaldo en la BD * FCWMFSIT-wmuser "
				+ "utilizando la tabla WMUSER.RIB_STORE_MSG, en la siguiente consulta.");

		System.out.println(GlobalVariables.DB_HOST_fcwmesit);
		System.out.println(ValidResp);
		SQLResult ValidRespRes = executeQuery(dbWms, ValidResp);
		boolean ValidRespVal = ValidRespRes.isEmpty();
		if (ValidRespVal) {
			testCase.addTextEvidenceCurrentStep("No Se encontro registro del  respaldo en la BD * FCWMFSIT-wmuser");
		}
		testCase.addQueryEvidenceCurrentStep(ValidRespRes);
		System.out.println(ValidRespVal);
		assertTrue(ValidRespVal, " se encontro registro del  respaldo en la BD * FCWMFSIT-wmuser");

		/*
		 * PASO 9
		 ********************************************************************************/

		addStep("Validar que la Tienda se registre en la Tabla Stagging de RMS16 de la BD MOMUAT-RMSV16 de manera correcta en la  BD MOMUAT-RMSV16 ");

		System.out.println(GlobalVariables.DB_HOST_FCMOMUAT);
		String FormatValidStag = String.format(ValidStag, data.get("Store"));
		System.out.println(FormatValidStag);
		String RMS_ASYNC_ID = "";
		SQLResult ValidStagRes = executeQuery(dbMuat, FormatValidStag);
		boolean ValidStagVal = ValidStagRes.isEmpty();
		if (!ValidStagVal) {
			testCase.addTextEvidenceCurrentStep("Se encontro registro de la Tienda en la Tabla Stagging de RMS16 de la BD MOMUAT-RMSV16");
			RMS_ASYNC_ID = ValidStagRes.getData(0, "RMS_ASYNC_ID");
		}
		testCase.addQueryEvidenceCurrentStep(ValidStagRes);
		System.out.println(ValidStagVal);
		assertFalse(ValidStagVal, "No se encontro que la Tienda se registre en la Tabla Stagging de RMS16 de la BD MOMUAT-RMSV16");

		/*
		 * PASO 10
		 ********************************************************************************/

		addStep("Validar que una Vez terminado el Proceso Asyncrono, se genere el Borrado de la Tienda en la Tabla STORE_ADD de RMS16 "
				+ "de la BD MOMUAT-RMSV16");

		System.out.println(GlobalVariables.DB_HOST_FCMOMUAT);
		String FormatValidAsync = String.format(ValidAsync, RMS_ASYNC_ID);
		System.out.println(FormatValidAsync);
		SQLResult ValidAsyncRes = executeQuery(dbMuat, FormatValidAsync);

		boolean ValidAsyncVal = ValidAsyncRes.isEmpty();

		if (!ValidAsyncVal) {
			testCase.addTextEvidenceCurrentStep("se genero el Borrado de la Tienda en la Tabla STORE_ADD de RMS16");

		}
		testCase.addQueryEvidenceCurrentStep(ValidAsyncRes);
		System.out.println(ValidAsyncVal);
		assertFalse(ValidAsyncVal, " No se genero el Borrado de la Tienda en la Tabla STORE_ADD de RMS16");

		/*
		 * PASO 11
		 ********************************************************************************/

		addStep("Validar que llegue la información de la Nueva tienda a la tabla store en la  BD MOMUAT-RMSV16");

		System.out.println(GlobalVariables.DB_HOST_FCMOMUAT);
		String FormatNewStore = String.format(NewStore, data.get("Store"));
		System.out.println(FormatNewStore);
		SQLResult NewStoreRes = executeQuery(dbMuat, FormatNewStore);

		boolean NewStoreVal = NewStoreRes.isEmpty();

		if (!NewStoreVal) {
			testCase.addTextEvidenceCurrentStep(
					"se Valido que llego la información de la Nueva tienda a la tabla store en la  BD MOMUAT-RMSV16");

		}
		testCase.addQueryEvidenceCurrentStep(NewStoreRes);
		System.out.println(NewStoreVal);
		assertFalse(NewStoreVal, "No se Valido que llego la información de la Nueva tienda a la tabla store en la  BD MOMUAT-RMSV16");

		/*
		 * PASO 12
		 ********************************************************************************/

		addStep("Validar que la información de la Tienda se replique al DAS por Golden Gate de manera correcta en la BD FCDASUAT ");

//							 Query 1************
		
		System.out.println(GlobalVariables.DB_HOST_FCDASUAT);
		System.out.println(Query1);
		SQLResult Query1Res = executeQuery(dbSuat, Query1);
		boolean Query1Val = Query1Res.isEmpty();
		if (!Query1Val) {
			testCase.addTextEvidenceCurrentStep("se valido que la información de la Tienda se replico al DAS por Golden Gate de manera correcta");
		}
		testCase.addQueryEvidenceCurrentStep(Query1Res);
		System.out.println(Query1Val);
		assertFalse(Query1Val, "No se valido que la información de la Tienda se replico al DAS por Golden Gate de manera correcta");

//								Query 2 ***********************************

		System.out.println(GlobalVariables.DB_HOST_FCDASUAT);
		System.out.println(Query2);
		SQLResult Query2Res = executeQuery(dbSuat, Query2);
		boolean Query2Val = Query2Res.isEmpty();
		if (!Query2Val) {
			testCase.addTextEvidenceCurrentStep("se valido que la información de la Tienda se replico al DAS por Golden Gate de manera correcta");
		}
		testCase.addQueryEvidenceCurrentStep(Query2Res);
		System.out.println(Query2Val);
		assertFalse(Query2Val, "No se valido que la información de la Tienda se replico al DAS por Golden Gate de manera correcta");

//								Query 3 ********************************************

		System.out.println(GlobalVariables.DB_HOST_FCDASUAT);
		System.out.println(Query3);
		SQLResult Query3Res = executeQuery(dbSuat, Query3);
		boolean Query3Val = Query3Res.isEmpty();
		if (!Query3Val) {
			testCase.addTextEvidenceCurrentStep("se valido que la información de la Tienda se replico al DAS por Golden Gate de manera correcta");

		}
		testCase.addQueryEvidenceCurrentStep(Query3Res);
		System.out.println(Query3Val);
		assertFalse(Query3Val, "No se valido que la información de la Tienda se replico al DAS por Golden Gate de manera correcta");

//								Query 4 ************************************************

		System.out.println(GlobalVariables.DB_HOST_FCDASUAT);
		System.out.println(Query4);
		SQLResult Query4Res = executeQuery(dbSuat, Query4);
		boolean Query4Val = Query4Res.isEmpty();
		if (!Query4Val) {
			testCase.addTextEvidenceCurrentStep("se valido que la información de la Tienda se replico al DAS por Golden Gate de manera correcta");
		}
		testCase.addQueryEvidenceCurrentStep(Query4Res);
		System.out.println(Query4Val);
		assertFalse(Query4Val, "No se valido que la información de la Tienda se replico al DAS por Golden Gate de manera correcta");

//								Query 5 ***********************************************************

		System.out.println(GlobalVariables.DB_HOST_FCDASUAT);
		System.out.println(Query5);
		SQLResult Query5Res = executeQuery(dbSuat, Query5);
		boolean Query5Val = Query5Res.isEmpty();
		if (!Query5Val) {
			testCase.addTextEvidenceCurrentStep("se valido que la información de la Tienda se replico al DAS por Golden Gate de manera correcta");
		}
		testCase.addQueryEvidenceCurrentStep(Query5Res);
		System.out.println(Query5Val);
		assertFalse(Query5Val, "No se valido que la información de la Tienda se replico al DAS por Golden Gate de manera correcta");

//								Query 6 ************************************************************

		System.out.println(GlobalVariables.DB_HOST_FCDASUAT);
		System.out.println(Query6);
		SQLResult Query6Res = executeQuery(dbSuat, Query6);
		boolean Query6Val = Query6Res.isEmpty();
		if (!Query6Val) {
			testCase.addTextEvidenceCurrentStep("se valido que la información de la Tienda se replico al DAS por Golden Gate de manera correcta");
		}
		testCase.addQueryEvidenceCurrentStep(Query6Res);
		System.out.println(Query6Val);
		assertFalse(Query6Val, "No se valido que la información de la Tienda se replico al DAS por Golden Gate de manera correcta");

//								Query 7 *************************************************************************

		System.out.println(GlobalVariables.DB_HOST_FCDASUAT);
		System.out.println(Query7);
		SQLResult Query7Res = executeQuery(dbSuat, Query7);
		boolean Query7Val = Query7Res.isEmpty();
		if (!Query7Val) {
			testCase.addTextEvidenceCurrentStep("se valido que la información de la Tienda se replico al DAS por Golden Gate de manera correcta");
		}
		testCase.addQueryEvidenceCurrentStep(Query7Res);
		System.out.println(Query7Val);
		assertFalse(Query7Val, "No se valido que la información de la Tienda se replico al DAS por Golden Gate de manera correcta");

//								Query 8 *********************************************************

		System.out.println(GlobalVariables.DB_HOST_FCDASUAT);
		System.out.println(Query8);
		SQLResult Query8Res = executeQuery(dbSuat, Query8);
		boolean Query8Val = Query8Res.isEmpty();
		if (!Query8Val) {
			testCase.addTextEvidenceCurrentStep("se valido que la información de la Tienda se replico al DAS por Golden Gate de manera correcta");

		}
		testCase.addQueryEvidenceCurrentStep(Query8Res);
		System.out.println(Query8Val);
		assertFalse(Query8Val, "No se valido que la información de la Tienda se replico al DAS por Golden Gate de manera correcta");

		/*
		 * PASO 13
		 ********************************************************************************/
		addStep("Validar que la informacion de la Tienda se replique a LAS TABLAS DELTA correctamente en la BD FCDASUAT :");

//								 Query 1************
		
		System.out.println(GlobalVariables.DB_HOST_FCDASUAT);
		System.out.println(QueryDelta1);
		SQLResult QueryDelta1Res = executeQuery(dbSuat, QueryDelta1);

		boolean QueryDelta1Val = QueryDelta1Res.isEmpty();

		if (!QueryDelta1Val) {
			testCase.addTextEvidenceCurrentStep(
					"se valido la informacion de la Tienda se replique a LAS TABLAS DELTA correctamente en la BD FCDASUAT ");

		}
		testCase.addQueryEvidenceCurrentStep(QueryDelta1Res);
		System.out.println(QueryDelta1Val);
		assertFalse(QueryDelta1Val, "No se valido la informacion de la Tienda se replique a LAS TABLAS DELTA correctamente en la BD FCDASUAT ");

//								 Query 2************
		
		System.out.println(GlobalVariables.DB_HOST_FCDASUAT);
		System.out.println(QueryDelta2);
		SQLResult QueryDelta2Res = executeQuery(dbSuat, QueryDelta2);

		boolean QueryDelta2Val = QueryDelta2Res.isEmpty();

		if (!QueryDelta2Val) {
			testCase.addTextEvidenceCurrentStep(
					"se valido la informacion de la Tienda se replique a LAS TABLAS DELTA correctamente en la BD FCDASUAT ");

		}
		testCase.addQueryEvidenceCurrentStep(QueryDelta2Res);
		System.out.println(QueryDelta2Val);
		assertFalse(QueryDelta2Val, "No se valido la informacion de la Tienda se replique a LAS TABLAS DELTA correctamente en la BD FCDASUAT ");

//								 Query 3************
		
		System.out.println(GlobalVariables.DB_HOST_FCDASUAT);
		System.out.println(QueryDelta3);
		SQLResult QueryDelta3Res = executeQuery(dbSuat, QueryDelta3);

		boolean QueryDelta3Val = QueryDelta3Res.isEmpty();

		if (!QueryDelta3Val) {
			testCase.addTextEvidenceCurrentStep(
					"se valido la informacion de la Tienda se replique a LAS TABLAS DELTA correctamente en la BD FCDASUAT ");

		}
		testCase.addQueryEvidenceCurrentStep(QueryDelta3Res);
		System.out.println(QueryDelta3Val);
		assertFalse(QueryDelta3Val, "No se valido la informacion de la Tienda se replique a LAS TABLAS DELTA correctamente en la BD FCDASUAT ");

		/*
		 * PASO 13
		 ********************************************************************************/

		addStep("Validar informacion en Tablas Staging de la Nueva Tienda Creada en la BD MOMUAT-RMSV16  con las siguientes tablas y siguientes consultas.");

//								 Query 1************
		
		System.out.println(GlobalVariables.DB_HOST_FCMOMUAT);
		System.out.println(Stag1);
		SQLResult Stag1Res = executeQuery(dbMuat, Stag1);

		boolean Stag1Val = Stag1Res.isEmpty();

		if (!Stag1Val) {
			testCase.addTextEvidenceCurrentStep(
					"se valido la informacion en Tablas Staging de la Nueva Tienda Creada en la BD MOMUAT-RMSV16");

		}
		testCase.addQueryEvidenceCurrentStep(Stag1Res);
		System.out.println(Stag1Val);
		assertFalse(Stag1Val, "No se valido la informacion en Tablas Staging de la Nueva Tienda Creada en la BD MOMUAT-RMSV16");

//								 Query 2 ************
		
		System.out.println(GlobalVariables.DB_HOST_FCMOMUAT);
		System.out.println(Stag2);
		SQLResult Stag2Res = executeQuery(dbMuat, Stag2);

		boolean Stag2Val = Stag2Res.isEmpty();

		if (!Stag2Val) {
			testCase.addTextEvidenceCurrentStep(
					"se valido la informacion en Tablas Staging de la Nueva Tienda Creada en la BD MOMUAT-RMSV16");

		}
		testCase.addQueryEvidenceCurrentStep(Stag2Res);
		System.out.println(Stag2Val);
		assertFalse(Stag2Val, "No se valido la informacion en Tablas Staging de la Nueva Tienda Creada en la BD MOMUAT-RMSV16");

//								 Query 3 ************
		
		System.out.println(GlobalVariables.DB_HOST_FCMOMUAT);
		String Stag3Format = String.format(Stag3, RMS_ASYNC_ID);
		System.out.println(Stag3Format);
		SQLResult Stag3Res = executeQuery(dbMuat, Stag3Format);

		boolean Stag3Val = Stag3Res.isEmpty();

		if (!Stag3Val) {
			testCase.addTextEvidenceCurrentStep(
					"se valido la informacion en Tablas Staging de la Nueva Tienda Creada en la BD MOMUAT-RMSV16");

		}
		testCase.addQueryEvidenceCurrentStep(Stag3Res);
		System.out.println(Stag3Val);
		assertFalse(Stag3Val, "No se valido la informacion en Tablas Staging de la Nueva Tienda Creada en la BD MOMUAT-RMSV16");

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
		return "MTC-FT-001 CR01 Envio de información de nuevas tiendas";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatización";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;

	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}