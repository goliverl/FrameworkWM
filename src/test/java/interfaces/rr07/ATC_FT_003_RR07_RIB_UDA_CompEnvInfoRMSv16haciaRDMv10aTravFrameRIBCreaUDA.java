package interfaces.rr07;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_003_RR07_RIB_UDA_CompEnvInfoRMSv16haciaRDMv10aTravFrameRIBCreaUDA extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_RR07_RIB_UDA_CompEnvInfoRMSv16haciaRDMv10aTravFrameRIBCreaUDA_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/
		
		//AQUI
		
		SQLUtil dbFCWMFSIT = new SQLUtil(GlobalVariables.DB_HOST_FCWMFSIT, GlobalVariables.DB_USER_FCWMFSIT, GlobalVariables.DB_PASSWORD_FCWMFSIT);
		
		SQLUtil dbFCWMESIT = new SQLUtil(GlobalVariables.DB_HOST_FCWMESIT, GlobalVariables.DB_USER_FCWMESIT, GlobalVariables.DB_PASSWORD_FCWMESIT);
		
		//////
		
//		SQLUtil dbFCRDMSIT_RDM_MTY_RDMVIEW = new SQLUtil(GlobalVariables.DB_HOST_FCRDMSIT_RDM_MTY_RDMVIEW, GlobalVariables.DB_USER_FCRDMSIT_RDM_MTY_RDMVIEW, GlobalVariables.DB_PASSWORD_FCRDMSIT_RDM_MTY_RDMVIEW);
//		
		SQLUtil dbFCMOMSIT = new SQLUtil(GlobalVariables.DB_HOST_FCMOMSIT, GlobalVariables.DB_USER_FCMOMSIT, GlobalVariables.DB_PASSWORD_FCMOMSIT);
//		
//		SQLUtil dbFCWMLTAQ_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, GlobalVariables.DB_USER_FCWMLQA_WMLOG, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		
		
		/*
		 * Variables
		 *********************************************************************/
		//New
		
		String consulta_config_UDA = "SELECT FAMILY, SOURCE, CONFIG_VALUE, DATA1, DESCRIPTION, CREATION_DATE\n"
				+ "FROM WMUSER.RIB_FR_CONFIG \n"
				+ "WHERE FAMILY = 'UDAS' \n";
		
		String consulta_interf_config  = "SELECT OPERACION, CATEGORIA, VALOR1, DESCRIPCION\n"
				+ "FROM WMUSER.WM_INTERFASE_CONFIG \n"
				+ "WHERE INTERFASE LIKE '%RR07%' \n"
				+ "AND ENTIDAD = 'UDAS'";
		
		String consulta_ROLLOUT_conf = "SELECT ID_CEDIS, CR_TIENDA, CR_PLAZA, LOC_TYPE, LOC_DESC\n"
				+ "FROM WMUSER.WM_OXXO_ROLLOUT_CONFIG_MV \n"
				+ "WHERE ID_CEDIS = '%s' \n"
				+ "AND V16 = '%s' \n"
				+ "AND LOC_TYPE = '%s'";
		
		String consulta_WM_RDM_conex = "SELECT * \r\n"
				+ "FROM WMUSER.WM_RDM_CONNECTIONS \r\n"
				+ "WHERE RETEK_CR='%s' \r\n"
				+ "AND TRANSACTION_TYPE = '%s'";
		
		String consulta_RMSv16_UDA  = "SELECT * \r\n"
				+ "FROM RMS.UDA \r\n"
				+ "WHERE UDA_ID = '59'";
		
		String consulta_RMSv16_Val = "SELECT * \r\n"
				+ "FROM RMS.UDA_VALUES \r\n"
				+ "WHERE UDA_ID = '59'";
		
		////
		
		String consulta_conexRDM = "SELECT * \r\n"
				+ "FROM WM_RDM_CONNECTIONS \r\n"
				+ "WHERE STATUS = 'A'";
		
		String consulta_PMO_Tienda = "SELECT ROW_NUMBER () OVER (ORDER BY INSERT_DATE)AS CANTIDAD_DE_REGISTROS, A.* \r\n"
				+ "FROM RDM100.XXFC_PMO_TIENDA A \r\n"
				+ "WHERE STATUS='A' \r\n"
				+ "ORDER BY CANTIDAD_DE_REGISTROS DESC";
		
		String consulta_PMO_ROUTE = "SELECT ROW_NUMBER () OVER (ORDER BY FECHA_INSERT)AS "
				+ "CANTIDAD_DE_REGISTROS, A.* FROM RDM100.XXFC_PMO_ROUTE A WHERE STATUS='A' "
				+ "ORDER BY CANTIDAD_DE_REGISTROS DESC";
		
		String consulta_Reg_PMO_Tienda = "SELECT ROW_NUMBER () OVER (ORDER BY INSERT_DATE)"
				+ "AS CANTIDAD_DE_REGISTROS, A.* FROM XXFC_GA.XXFC_PMO_TIENDA A "
				+ "WHERE STATUS='A' AND CEDIS='%s'"
				+ " ORDER BY CANTIDAD_DE_REGISTROS DESC";
		
		String consulta_Reg_PMO_ROUTE = "SELECT ROW_NUMBER () OVER (ORDER BY FECHA_INSERT)"
				+ "AS CANTIDAD_DE_REGISTROS, A.* FROM XXFC_GA.XXFC_PMO_ROUTE A"
				+ " WHERE STATUS='A' AND CEDIS='%s' "
				+ "ORDER BY CANTIDAD_DE_REGISTROS DESC";
		
		String consultaWM_LOG_RUN= "SELECT * FROM WMLOG.WM_LOG_RUN WHERE INTERFACE "
				+ "LIKE'%RR08%' ORDER BY START_DT DESC";
		
		String consultaWM_LOG_ERROR  = "SELECT * FROM WMLOG.WM_LOG_ERROR WHERE RUN_ID='RUN_ID'";
		
		
		String expec_WM_LOG_RUN_stat = "S";
		
		
		Date fechaEjecucionInicio;
		
		testCase.setTest_Description(data.get("id") + data.get("description"));
		
		testCase.setPrerequisites("prerequisites");
	
			
		
		// Paso 1 ****************************************************
		addStep("Realizar conexion a la BD FCWMFSIT");
		
		testCase.addTextEvidenceCurrentStep("Conexion: dbFCWMFSIT ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMFSIT);
	
		// Paso 2 ****************************************************
		
		addStep("Validar con la siguiente consulta en la base de datos de WM FCWMFSIT "
				+ "los datos de configuración  (Operación, Valor1) del cedis a procesar");
		
		SQLResult consulta_UDA_config_r = executeQuery(dbFCWMFSIT, consulta_config_UDA);
		System.out.println(consulta_config_UDA);
		boolean validaUDA_config = consulta_UDA_config_r.isEmpty();
		if (!validaUDA_config) {	
			testCase.addQueryEvidenceCurrentStep(consulta_UDA_config_r);
		}

		assertFalse(validaUDA_config, "No se encontraron registros en la consulta");
		
		
		// Paso 3 ****************************************************
		
		
		addStep("validar la correcta configuración de la interfaz RR07 "
				+ "para la entidad correspondiente en el esquema WMUSER en la BD FCWMFSIT");
		
		SQLResult consulta_interf_config_r = executeQuery(dbFCWMFSIT, consulta_interf_config);
		System.out.println(consulta_interf_config);
		boolean validainterf_config = consulta_interf_config_r.isEmpty();
		if (!validainterf_config) {
			testCase.addQueryEvidenceCurrentStep(consulta_interf_config_r);
		}
		assertFalse(validainterf_config, "No se encontraron registros en la consulta");
		
		// Paso 4 ****************************************************
		
		addStep("Consultar que el Cedis (a probar) esté migrado a RMS v16 en la "
				+ "tabla WM_OXXO_ROLLOUT_CONFIG en el esquema WMUSER en la Base de Datos FCWMFSIT de Web Methods");
		
		String consulta_ROLLOUT_config = String.format(consulta_ROLLOUT_conf, data.get("cedis"), data.get("V16"), data.get("LOC_TYPE"));
		SQLResult consulta_ROLLOUT_conf_r = executeQuery(dbFCWMFSIT, consulta_ROLLOUT_config);
		System.out.println(consulta_ROLLOUT_config);
		boolean validaconsulta_ROLLOUT = consulta_ROLLOUT_conf_r.isEmpty();
		if (!validaconsulta_ROLLOUT) {
			testCase.addQueryEvidenceCurrentStep(consulta_ROLLOUT_conf_r);
		}
//		assertFalse(validaconsulta_ROLLOUT, "No se muestra el Cedis (A probar) migrado en la tabla WM_OXXO_ROLLOUT_CONFIG");
		
		// Paso 5 ****************************************************
		
		addStep("Ingresar a la base de datos de (QA) FCWMESIT");
		
		testCase.addTextEvidenceCurrentStep("Conexion: dbFCWMESIT ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMESIT);
		
		// Paso 6 ****************************************************
		
		addStep(" Consultar que las conexiones del Cedis (a probar) exista en "
				+ "la tabla WM_RDM_CONNECTIONS en el esquema WMUSER en la base de datos FCWMESIT");
		
		String consulta_WM_RDM_connect = String.format(consulta_WM_RDM_conex, data.get("cedis"), data.get("transaction_type"));
		
		SQLResult consulta_WM_RDM_connect_r = executeQuery(dbFCWMESIT, consulta_WM_RDM_connect);
		System.out.println(consulta_WM_RDM_connect);
		boolean validaWM_RDM_connect_rT = consulta_WM_RDM_connect_r.isEmpty();
		if (!validaWM_RDM_connect_rT) {
			testCase.addQueryEvidenceCurrentStep(consulta_WM_RDM_connect_r);
		}

		assertFalse(validaWM_RDM_connect_rT, "No se muestra el Cedis (A probar) en la tabla WM_RDM_CONNECTIONS con TRANSACTION_TYPE=LT");
		
		
		// Paso 7 ****************************************************
		
		addStep("Validar que se encuentre encendido el trigger RR07.Triggers:trgConsumeMsgUDA "
				+ "que ejecuta automáticamente la interfaz RR07 una vez detecte el nuevo UDA, "
				+ "el cual se encuentra ubicado en la ruta:"
				+ "Integration Server> Settings > Messaging > JMS Trigger Management.");
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user_IS");
		String ps = PasswordUtil.decryptPassword(data.get("ps_IS"));
		String server = data.get("server_IS");
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		
		pok.validateStatusTrigger();
		boolean triggerOnscreen = pok.validateTriggerEnabled(data.get("triggerName"));
		if(!triggerOnscreen) {
			testCase.addBoldTextEvidenceCurrentStep("Se encuentra trigger encendido.");	
			}else {
				testCase.addBoldTextEvidenceCurrentStep("No se encuentra trigger en ejecucion.");
			}
		u.close();
		
		// Paso 8 ****************************************************
		
		addStep("Conectarse a la BD de FCMOMSIT");
		
		testCase.addTextEvidenceCurrentStep("Conexion: dbFCMOMSIT ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCMOMSIT);
		
		// Paso 9 ****************************************************
		
		addStep("Consultar  la tabla de RMSv16 que no exista el UDA a crear en el esquema RMS en la BD  FCMOMSIT");
		
		SQLResult consulta_RMSv16_UDA__r = executeQuery(dbFCMOMSIT, consulta_RMSv16_UDA);
		System.out.println(consulta_RMSv16_UDA);
		boolean validaRMSv16_UDA = consulta_RMSv16_UDA__r.isEmpty();
		if (!validaRMSv16_UDA) {
			testCase.addQueryEvidenceCurrentStep(consulta_RMSv16_UDA__r);
		}

		assertFalse(validaRMSv16_UDA, "Se muestra el registro de UDA consultado");
		
		// Paso 10 ****************************************************
		
		addStep("Consultar  la tabla de RMSv16 de los valores de UDA en el esquema RMS en la BD  FCMOMSIT");
		
		SQLResult consulta_RMSv16_Val__r = executeQuery(dbFCMOMSIT, consulta_RMSv16_Val);
		System.out.println(consulta_RMSv16_Val);
		boolean validaRMSv16_Val = consulta_RMSv16_Val__r.isEmpty();
		if (!validaRMSv16_Val) {
			testCase.addQueryEvidenceCurrentStep(consulta_RMSv16_Val__r);
		}
		assertFalse(validaRMSv16_Val, "No se muestra el registro de UDA creado");
	}
	

	@Override
	public String setTestFullName() {
		return "ATC_FT_003_RR07_RIB_UDA_CompEnvInfoRMSv16haciaRDMv10aTravFrameRIBCreaUDA_test";
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
