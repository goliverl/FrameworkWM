package interfaces.rr08;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_001_RR08_CompEnvInfoRutDistrPMOdeRDMv10aRMSv16 extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_RR08_CompEnvInfoRutDistrPMOdeRDMv10aRMSv16_test(HashMap<String, String> data) throws Exception {

		/**
		 * Proyecto: Nucleo Gas (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-001 RR08 Comprobar el envio de informacion de Rutas de Distribucion o PMO de RDMv10 a RMSv16
		 * @author 
		 * @date
		 */
		
		
		/*
		 * Utilerias
		 *********************************************************************/
		
		SQLUtil dbFCWMFSIT = new SQLUtil(GlobalVariables.DB_HOST_FCWMFSIT, GlobalVariables.DB_USER_FCWMFSIT, GlobalVariables.DB_PASSWORD_FCWMFSIT);
		
		SQLUtil dbFCWMESIT = new SQLUtil(GlobalVariables.DB_HOST_FCWMESIT, GlobalVariables.DB_USER_FCWMESIT, GlobalVariables.DB_PASSWORD_FCWMESIT);
		
		SQLUtil dbFCRDMSIT_RDM_MTY_RDMVIEW = new SQLUtil(GlobalVariables.DB_HOST_FCRDMSIT_RDM_MTY_RDMVIEW, GlobalVariables.DB_USER_FCRDMSIT_RDM_MTY_RDMVIEW, GlobalVariables.DB_PASSWORD_FCRDMSIT_RDM_MTY_RDMVIEW);
		
		SQLUtil dbFCMOMPFR = new SQLUtil(GlobalVariables.DB_HOST_FCMOMPFR_RMS, GlobalVariables.DB_USER_FCMOMPFR_RMS, GlobalVariables.DB_PASSWORD_FCMOMPFR_RMS);
		
		//SQLUtil dbFCWMLTAQ_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		
		
		/*
		 * Variables
		 *********************************************************************/
		
		//Paso 2
		String consulta_interf_config  = "SELECT INTERFASE,ENTIDAD,OPERACION,CATEGORIA,VALOR1,DESCRIPCION \r\n"
				+ "FROM WMUSER.WM_INTERFASE_CONFIG WHERE INTERFASE ='RR08'";
		//Paso 4
		String consulta_conexRDM = "SELECT RETEK_CR,RETEK_PHYSICAL_CR,ORACLE_CR,CONNECTION_NAME,CEDIS_DESC,TRANSACTION_TYPE,STATUS \r\n"
				+ "FROM WMUSER.WM_RDM_CONNECTIONS WHERE STATUS='A'";
		//Paso 7
		String consulta_PMO_Tienda = "SELECT * FROM(\r\n"
				+ "SELECT ROW_NUMBER () OVER (ORDER BY INSERT_DATE)\r\n"
				+ "AS CANTIDAD_DE_REGISTROS, A.ROUTE,A.DIA_GEN_PED,DEST_ID,LOAD_SEQUENCE,H_INI_VENT_ENTREGA\r\n"
				+ "FROM RDM100.XXFC_PMO_TIENDA A WHERE STATUS='A' ORDER BY CANTIDAD_DE_REGISTROS DESC\r\n"
				+ ")WHERE ROWNUM <= 10";
		//Paso 8
		String consulta_PMO_ROUTE = "SELECT * FROM(\r\n"
				+ "SELECT ROW_NUMBER () OVER (ORDER BY FECHA_INSERT) \r\n"
				+ "AS CANTIDAD_DE_REGISTROS, A.FACILITY_ID,CEDIS,TIPO_DISTRO,CARRIER_CODE,SERVICE_CODE,ROUTE \r\n"
				+ "FROM RDM100.XXFC_PMO_ROUTE A \r\n"
				+ "WHERE STATUS='A' ORDER BY CANTIDAD_DE_REGISTROS DESC\r\n"
				+ ")WHERE ROWNUM <= 10";
		
		String consulta_Reg_PMO_Tienda = "SELECT * FROM(\r\n"
				+ "SELECT ROW_NUMBER () OVER (ORDER BY INSERT_DATE)\r\n"
				+ "AS CANTIDAD_DE_REGISTROS, A.ROUTE,A.DIA_GEN_PED,A.DEST_ID,A.LOAD_SEQUENCE,A.DIA_INI_VENT_ENTREGA\r\n"
				+ "FROM XXFC_GA.XXFC_PMO_TIENDA A WHERE STATUS='A' AND CEDIS='%s' \r\n"
				+ "ORDER BY CANTIDAD_DE_REGISTROS DESC\r\n"
				+ ")WHERE ROWNUM <=10";
		
		String consulta_Reg_PMO_ROUTE = "SELECT * FROM(\r\n"
				+ "SELECT ROW_NUMBER () OVER (ORDER BY FECHA_INSERT) \r\n"
				+ "AS CANTIDAD_DE_REGISTROS, A.FACILITY_ID,CEDIS,TIPO_DISTRO,CARRIER_CODE,SERVICE_CODE,ROUTE \r\n"
				+ "FROM XXFC_GA.XXFC_PMO_ROUTE A \r\n"
				+ "WHERE STATUS='A' AND CEDIS='%s' \r\n"
				+ "ORDER BY CANTIDAD_DE_REGISTROS DESC\r\n"
				+ ")WHERE ROWNUM <= 10";
		
		String consultaWM_LOG_RUN= "SELECT * FROM WMLOG.WM_LOG_RUN WHERE INTERFACE "
				+ "LIKE'%RR08%' ORDER BY START_DT DESC";
		
		String consultaWM_LOG_ERROR  = "SELECT * FROM WMLOG.WM_LOG_ERROR WHERE RUN_ID='RUN_ID'";
		
		
		String expec_WM_LOG_RUN_stat = "S";
		
		
		Date fechaEjecucionInicio;
		
		testCase.setTest_Description(data.get("id") + data.get("description"));
		
		testCase.setPrerequisites("prerequisites");
	
			
		
		// Paso 1 ****************************************************
		addStep("Realizar conexión a la BD WM FCWMFSIT con usuario y contraseña correctos");
		System.out.println("Paso 1: \n" + GlobalVariables.DB_HOST_FCWMFSIT );
		
		
		boolean conexiondbFCWMFSIT = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCWMFSIT ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMFSIT);

		assertTrue(conexiondbFCWMFSIT, "La conexion no fue exitosa");
	
		
		// Paso 2 ****************************************************
		
		addStep("Validar con la siguiente consulta en la base de datos de WM FCWMFSIT "
				+ "los datos de configuración  (Operación, Valor1) del cedis a procesar");
		System.out.println("Paso 2: \n" + consulta_interf_config );
		
		
		SQLResult consulta_interf_config_r = executeQuery(dbFCWMFSIT, consulta_interf_config);
		

		boolean validainterf_config = consulta_interf_config_r.isEmpty();

		if (!validainterf_config) {

			
			testCase.addQueryEvidenceCurrentStep(consulta_interf_config_r);
		}

		System.out.println(validainterf_config);

		assertFalse(validainterf_config, "No se encontraron registros en la consulta");
		
		
		// Paso 3 ****************************************************
		
		addStep("Establecer  conexión con la BD de WM FCWMESIT");
		System.out.println("Paso 3: \n" + GlobalVariables.DB_HOST_FCWMESIT );
		
		
		boolean conexiondbFCWMESIT = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCWMESIT ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMESIT);

		assertTrue(conexiondbFCWMESIT, "La conexion no fue exitosa");
		
		
		// Paso 4 ****************************************************
		
		addStep(" Validar con la siguiente consulta las conexiones de RDM registradas "
				+ "en la tabla WM_RDM_CONNECTIONS con estatus A, la columna RETEk_CR "
				+ "son los id de los cedis virtuales configurados");
		System.out.println("Paso 4: \n" + consulta_conexRDM );
		
		SQLResult consulta_conexRDM_r = executeQuery(dbFCWMESIT, consulta_conexRDM);
		

		boolean validaconsulta_conexRDM = consulta_conexRDM_r.isEmpty();

		if (!validaconsulta_conexRDM) {

			
			testCase.addQueryEvidenceCurrentStep(consulta_conexRDM_r);
		}

		System.out.println(validaconsulta_conexRDM);

		assertFalse(validaconsulta_conexRDM, "No se encontraron registros los registros en la tabla WM_RDM_CONNECTIONS");
		
		
		
		// Paso 5 ****************************************************
		
		addStep("Solicitar el apoyo del equipo de soporte de Núcleo para la creación de "
				+ "una Ruta de distribución desde RDMv10, por medio de inserts y updates "
				+ "a las tablas RDM100.XXFC_PMO_TIENDA y RDM100.XXFC_PMO_ROUTE");
		
		/*
		 * Se obtiene la respuesta de soporte RDM confirmando la creación y 
		 * actualizacion de rutas de distribución exitosamente en las tablas
			RDM100.XXFC_PMO_TIENDA y RDM100.XXFC_PMO_ROUTE
		 */
		
		
		
		// Paso 6 ****************************************************
		
		addStep("Establecer conexión a la BD FCRDMSIT_RDM_MTY_RDMVIEW de RDMv10");
		System.out.println("Paso 6: \n" + GlobalVariables.DB_HOST_FCRDMSIT_RDM_MTY_RDMVIEW );
		
		boolean conexiondbFCRDMSIT_RDM_MTY_RDMVIEW = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCRDMSIT_RDM_MTY_RDMVIEW ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCRDMSIT_RDM_MTY_RDMVIEW);

		assertTrue(conexiondbFCRDMSIT_RDM_MTY_RDMVIEW, "La conexion no fue exitosa");
		
		// Paso 7 ****************************************************
		
		addStep(" Comprobar la existencia información en la tabla XXFC_PMO_TIENDA de la base de datos "
				+ "de FCRD01CU_RDM_MTY_RDMVIEW mediante la siguiente consulta");
		System.out.println("Paso 7: \n" + consulta_PMO_Tienda );
		
		SQLResult consulta_PMO_Tienda_r = executeQuery(dbFCRDMSIT_RDM_MTY_RDMVIEW, consulta_PMO_Tienda);
		

		boolean validaconsulta_PMO_Tienda = consulta_PMO_Tienda_r.isEmpty();

		if (!validaconsulta_PMO_Tienda) {

			
			testCase.addQueryEvidenceCurrentStep(consulta_PMO_Tienda_r);
		}

		System.out.println(validaconsulta_PMO_Tienda);

		assertFalse(validaconsulta_PMO_Tienda, "No se mostraron los registros con status 'A' ");
		
		
		// Paso 8 ****************************************************
		
		addStep("Consultar la información y cantidad de registros de la tabla XXFC_PMO_ROUTE"
				+ " con estatus A en la base de datos de FCRDMSIT_MTY_RDMVIEW");
		System.out.println("Paso 8: \n" + consulta_PMO_ROUTE );
		
		SQLResult consulta_PMO_ROUTE_r = executeQuery(dbFCRDMSIT_RDM_MTY_RDMVIEW, consulta_PMO_ROUTE);
		

		boolean validaconsulta_PMO_ROUTE = consulta_PMO_ROUTE_r.isEmpty();

		if (!validaconsulta_PMO_ROUTE) {

			
			testCase.addQueryEvidenceCurrentStep(consulta_PMO_ROUTE_r);
		}

		System.out.println(validaconsulta_PMO_ROUTE);

		assertFalse(validaconsulta_PMO_ROUTE, "No se mostraron los registros con status 'A' ");
		
		
		
		// Paso 9 ****************************************************
		
		addStep("Establecer conexion a la BD NGA_FCMOMPRF_RMS16 de RMSv16");
		System.out.println("Paso 9: \n" + GlobalVariables.DB_HOST_FCMOMPFR_RMS );
		
		boolean conexiondbFCMOMPFR = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCMOMPFR ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCMOMPFR_RMS);

		assertTrue(conexiondbFCMOMPFR, "La conexion no fue exitosa");
		
		
		// Paso 10 ****************************************************
		
		addStep("Consultar la información y cantidad de registros en la tabla "
				+ "XXFC_PMO_TIENDA de la base de datos de FCMOMSIT");
		
		String consulta_Registro_PMO_Tienda = String.format(consulta_Reg_PMO_Tienda, data.get("cedis"));
		System.out.println("Paso 10: \n" + consulta_Registro_PMO_Tienda );
		
		SQLResult consulta_Reg_PMO_Tienda_r = executeQuery(dbFCMOMPFR, consulta_Registro_PMO_Tienda);

		boolean validaReg_PMO_Tienda = consulta_Reg_PMO_Tienda_r.isEmpty();

		if (!validaReg_PMO_Tienda) {

			
			testCase.addQueryEvidenceCurrentStep(consulta_Reg_PMO_Tienda_r);
		}

		System.out.println(validaReg_PMO_Tienda);

		assertFalse(validaReg_PMO_Tienda, "No se mostraron los registros con status 'A'");
		
		
		// Paso 11 ****************************************************
		
		addStep("Consultar la información y cantidad de registros de la tabla XXFC_PMO_ROUTE"
				+ " con estatus A en la base de datos de FCMOMSIT");
		
		String consulta_Registro_PMO_ROUTE = String.format(consulta_Reg_PMO_ROUTE, data.get("cedis"));
		System.out.println("Paso 11: \n" + consulta_Registro_PMO_ROUTE );
		
		SQLResult consulta_Reg_PMO_ROUTE_r = executeQuery(dbFCMOMPFR, consulta_Registro_PMO_ROUTE);

		boolean validaReg_PMO_ROUTE = consulta_Reg_PMO_ROUTE_r.isEmpty();

		if (!validaReg_PMO_ROUTE) {
			testCase.addQueryEvidenceCurrentStep(consulta_Reg_PMO_ROUTE_r);
		}

		System.out.println(validaReg_PMO_ROUTE);

		assertFalse(validaReg_PMO_ROUTE, "No se mostraron los registros con status 'A'");
		
		
		// Paso 12 ****************************************************
		
		addStep("Solicitar el ordenamiento del Job runRR08_MTY en Control-M "
				+ "para su ejecución, enviando un correo electrónico a los "
				+ "operadores USU: UsuFEMCOMOperadoresSITE@oxxo.com");
		

		fechaEjecucionInicio = new Date();
		
		// Se obtiene la cadena de texto del data provider en la columna "jobs"
		// Se asigna a un array para poder manejarlo
		JSONArray array = new JSONArray(data.get("cm_jobs"));

		testCase.addTextEvidenceCurrentStep("Ejecución Job: " + data.get("cm_jobs"));
		SeleniumUtil u = new SeleniumUtil(new ChromeTest());
		Control_mInicio cm = new Control_mInicio(u, data.get("cm_user"), data.get("cm_ps"));

		testCase.addTextEvidenceCurrentStep("Login");
		addStep("Login");
		u.get(data.get("cm_server"));
		u.hardWait(40);
		u.waitForLoadPage();
		cm.logOn();
		
		testCase.addTextEvidenceCurrentStep("Inicio de job");
		JobManagement j = new JobManagement(u, testCase, array);
		String resultadoEjecucion = j.jobRunner();
		
		
		// Paso 13 ****************************************************
		/*
		addStep("Consultar la ejecución del Job en Control M");
		
		
		testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
		System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);
		
		assertEquals(resultadoEjecucion, "Ended OK");
		u.close();
		
		
		// Paso 14 ****************************************************
		
		addStep("Realizar conexión a la BD FCWMLQA_ WMLOG");
		
		
		boolean conexiondbFCWMLTAQ_WMLOG = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCWMLTAQ_WMLOG ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA_WMLOG);

		assertTrue(conexiondbFCWMLTAQ_WMLOG, "La conexion no fue exitosa");
		
		// Paso 15 ****************************************************
		
		addStep("Validar la tabla WMLOG.WM_LOG_RUN con la siguiente consulta "
				+ "que la interface  haya terminado correctamente con estatus S");
		
		String WM_LOG_RUN_stat = null;
		
		SQLResult consultaWM_LOG_RUN_Result = executeQuery(dbFCWMLTAQ_WMLOG, consultaWM_LOG_RUN);	

		boolean validaWM_LOG_RUN = consultaWM_LOG_RUN_Result.isEmpty();

		if (!validaWM_LOG_RUN) {

			WM_LOG_RUN_stat = consultaWM_LOG_RUN_Result.getData(0, "STATUS");
			
			testCase.addQueryEvidenceCurrentStep(consultaWM_LOG_RUN_Result);
		}

		System.out.println(validaWM_LOG_RUN);

		assertEquals(expec_WM_LOG_RUN_stat, WM_LOG_RUN_stat);
		
		// Paso 16 ****************************************************
		
		addStep("Validar con la siguiente consulta que no se haya registrado "
				+ "ningún error en la tabla WMLOG.WM_LOG_ERROR");
		
		SQLResult consultaWM_LOG_ERROR_Result = executeQuery(dbFCWMLTAQ_WMLOG, consultaWM_LOG_ERROR);	

		boolean validaWM_LOG_ERROR = consultaWM_LOG_ERROR_Result.isEmpty();

		if (!validaWM_LOG_ERROR) {

			
			testCase.addQueryEvidenceCurrentStep(consultaWM_LOG_ERROR_Result);
		}

		System.out.println(validaWM_LOG_ERROR);

		assertTrue(validaWM_LOG_ERROR, "Se muestran errores en la ejecucion");
		
		// Paso 17 ****************************************************
		
		addStep("Consultar la Información y cantidad de registros en la tabla "
				+ "XXFC_PMO_TIENDA de la base de datos de FCMOMSIT");
		
		String consulta_Registro_PMO_Tienda_2 = String.format(consulta_Reg_PMO_Tienda, data.get("cedis"));
		
		SQLResult consulta_Reg_PMO_Tienda_2_r = executeQuery(dbFCMOMSIT, consulta_Registro_PMO_Tienda_2);

		boolean validaReg_PMO_Tienda_2 = consulta_Reg_PMO_Tienda_2_r.isEmpty();

		if (!validaReg_PMO_Tienda_2) {

			
			testCase.addQueryEvidenceCurrentStep(consulta_Reg_PMO_Tienda_2_r);
		}

		System.out.println(validaReg_PMO_Tienda_2);

		assertFalse(validaReg_PMO_Tienda_2, "No se mostraron los registros con status 'A'");
		
		
		// Paso 18 ****************************************************
		
		addStep("Consultar la información y cantidad de registros de la tabla XXFC_PMO_ROUTE "
				+ "con estatus A en la base de datos de FCMOMSIT");
		
		
		String consulta_Registro_PMO_ROUTE_2 = String.format(consulta_Reg_PMO_ROUTE, data.get("cedis"));
		
		SQLResult consulta_Reg_PMO_ROUTE_2_r = executeQuery(dbFCMOMSIT, consulta_Registro_PMO_ROUTE_2);

		boolean validaReg_PMO_ROUTE_2 = consulta_Reg_PMO_ROUTE_2_r.isEmpty();

		if (!validaReg_PMO_ROUTE_2) {

			
			testCase.addQueryEvidenceCurrentStep(consulta_Reg_PMO_ROUTE_2_r);
		}

		System.out.println(validaReg_PMO_ROUTE_2);

		assertFalse(validaReg_PMO_ROUTE_2, "No se mostraron los registros con status 'A'");
		
		*/
	}
	
	

	@Override
	public String setTestFullName() {
		return "ATC_FT_001_RR08_CompEnvInfoRutDistrPMOdeRDMv10aRMSv16_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "JoseOnofre@Hexaware.com";
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
		return null;
	}

}