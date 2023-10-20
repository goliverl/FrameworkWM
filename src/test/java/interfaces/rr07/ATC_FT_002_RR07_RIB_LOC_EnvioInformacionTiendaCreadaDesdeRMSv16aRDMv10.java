package interfaces.rr07;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;
import interfaces.CR01.ATC_FT_001_CR01_EnvioDeInfoNuevasTiendas;



/**
 * Interfaces NUCLEO GAS: MTC-FT-002 RIB_LOC Comprobar el envio de la información de RMSv16 hacia RDMv10 a traves del 
 * framework RIB al crear una Tienda en RMSv16
 * Desc:
 * Prueba de regresión para comprobar la no afectación en la funcionalidad principal de la interfaz FEMSA_RIB_LOC para 
 * comprobar el envío de información de tiendas nuevas de RMSv16 a RDMv10 cuando se crea la TIENDA en RMS16, por medio 
 * de las interfaces RR07, FR_RIB, RIB_LOC al ser migradas estas interfaces de WM9.9 a WM10.5:
 * 
 * Origen: RMSv16
 * Destino: RDMv10
 * @author Oliver Martinez
 * @date   02/17/2023
 */

public class ATC_FT_002_RR07_RIB_LOC_EnvioInformacionTiendaCreadaDesdeRMSv16aRDMv10 extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_RR07_RIB_LOC_EnvioInformacionTiendaCreadaDesdeRMSv16aRDMv10_test (HashMap <String, String> data) throws Exception{
		/*
		 * Utileria***************************************************/
		SQLUtil dbFCWMFSIT = new SQLUtil(GlobalVariables.DB_HOST_FCWMFSIT, GlobalVariables.DB_USER_FCWMFSIT, GlobalVariables.DB_PASSWORD_FCWMFSIT);
		SQLUtil dbFCWMESIT = new SQLUtil(GlobalVariables.DB_HOST_FCWMESIT, GlobalVariables.DB_USER_FCWMESIT, GlobalVariables.DB_PASSWORD_FCWMESIT);
		SQLUtil dbFCMOMUAT = new SQLUtil(GlobalVariables.DB_HOST_FCMOMUAT,GlobalVariables.DB_USER_FCMOMUAT, GlobalVariables.DB_PASSWORD_FCMOMUAT);
		SQLUtil dbFCRDMSIT = new SQLUtil(GlobalVariables.DB_HOST_FCRDMSIT_RDM_MTY_RDMVIEW, GlobalVariables.DB_USER_FCRDMSIT_RDM_MTY_RDMVIEW, GlobalVariables.DB_PASSWORD_FCRDMSIT_RDM_MTY_RDMVIEW);
		
		ATC_FT_001_CR01_EnvioDeInfoNuevasTiendas sendingNewStoreInfo = new ATC_FT_001_CR01_EnvioDeInfoNuevasTiendas();
		/*
		 * Variables**************************************************/
		String queryFRRIBConfig = "SELECT MSG_TYPE, SOURCE, CONFIG_VALUE, DATA1, DESCRIPTION \r\n"
				+ "FROM WMUSER.RIB_FR_CONFIG \r\n"
				+ "WHERE FAMILY = 'Locations'";
		
		String queryRR07Config = "SELECT INTERFASE, ENTIDAD, OPERACION, CATEGORIA, VALOR1, DESCRIPCION \r\n"
				+ "FROM WMUSER.WM_INTERFASE_CONFIG \r\n"
				+ "WHERE INTERFASE LIKE '%RR07%' \r\n"
				+ "AND ENTIDAD = 'LOCATIONS'";
		
		String queryCedisMigrated = "SELECT ID_TIENDA, ID_CEDIS, CR_TIENDA, CR_PLAZA, LOC_TYPE, V16, V10, LOC_DESC\r\n"
				+ "FROM WMUSER.WM_OXXO_ROLLOUT_CONFIG_MV \r\n"
				+ "WHERE ID_CEDIS= '%s' \r\n"
				+ "AND V16 = '%s'\r\n"
				+ "AND LOC_TYPE = '%s'";
		
		String queryCedisConnections = "SELECT RETEK_CR, RETEK_PHYSICAL_CR, ORACLE_CR, CONNECTION_NAME, CEDIS_DESC, TRANSACTION_TYPE, STATUS\r\n"
				+ "FROM WMUSER.WM_RDM_CONNECTIONS\r\n"
				+ "WHERE RETEK_CR = '%s' \r\n"
				+ "AND TRANSACTION_TYPE = '%s'";
		
		/*Queries para validar tienda creada*/
		String queryCreatedStore = "SELECT STORE, STORE_NAME, STORE_NAME10 \r\n"
				+ "FROM RMS.STORE \r\n"
				+ "WHERE STORE = '%s'";
		
		String queryCreatedStore1 = "SELECT ADDR_KEY, MODULE, ADD_1, ADD_2, ADD_3, CITY, STATE\r\n"
				+ "FROM RMS.ADDR\r\n"
				+ "WHERE ADDR_KEY = '%s'";
		
		String queryInterfaceErrors = "SELECT FAMILY_MSG, DESCRIPTION, MESSAGE\r\n"
				+ "FROM WMLOG.RIB_LOG_ERROR_INT \r\n"
				+ "WHERE INTERFACE = 'RR07'\r\n"
				+ "AND CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "ORDER BY CREATION_DATE DESC";
		
		String queryFrameworkErrors = "SELECT FAMILY, WM_CODE, DESCRIPTION, MESSAGE\r\n"
				+ "FROM WMLOG.RIB_LOG_ERROR \r\n"
				+ "wHERE ERROR_DATE >= TRUNC(SYSDATE)\r\n"
				+ "ORDER BY ERROR_DATE DESC";
		
		String queryTransactionMessage = "SELECT CUSTOM_DATA, WM_CODE, WM_DESC\r\n"
				+ "FROM WMUSER.RIB_LOCATION_CONTROL\r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "ORDER BY CREATION_DATE DESC";
		
		String queryMessageBackup = "SELECT MSG_TYPE, ROUTING_INFO, MESSAGE_DATA\r\n"
				+ "FROM WMUSER.RIB_LOCATION_MSG_REP\r\n"
				+ "WHERE CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "ORDER BY CREATION_DATE DESC";
		
		String queryLocationInfoCreated = "SELECT DESCRIPTION, ADDRESS1, ADDRESS2, CITY \r\n"
				+ "FROM RDM100.SHIP_DEST \r\n"
				+ "WHERE DEST_ID = '%s'";
		
		/***************************Paso 1****************************/
		addStep("Ejecutar la siguiente consulta para validar las configuraciones de la interfaz FR_RIB "
				+ "necesarias para el procesamiento de mensajes de la familia RIB_LOC:");
		
		SQLResult interfaceFRRIBConfig = executeQuery(dbFCWMFSIT, queryFRRIBConfig);
		System.out.println(queryFRRIBConfig);
		if(!interfaceFRRIBConfig.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(interfaceFRRIBConfig);
		}
		assertFalse(interfaceFRRIBConfig.isEmpty(), "No se encontraron registros de la configuraciones");
		/***************************Paso 2****************************/
		addStep("Ejecutar la siguiente consulta para validar la correcta configuración de la interfaz RR07 "
				+ "para la entidad correspondiente:");
		
		SQLResult interfaceRR07Config = executeQuery(dbFCWMFSIT, queryRR07Config);
		System.out.println(queryRR07Config);
		if(!interfaceRR07Config.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(interfaceRR07Config);
		}
		assertFalse(interfaceRR07Config.isEmpty(), "No se encontraron registros de la configuraciones");
		
		/***************************Paso 3****************************/
		addStep("Consultar que el Cedis (a probar) esté migrado a RMS v16 en la tabla WM_OXXO_ROLLOUT_CONFIG "
				+ "en el esquema WMUSER en la Base de Datos FCWMFSIT de Web Methods  mediante la siguiente consulta:");
		
		String formatCedisMigrated = String.format(queryCedisMigrated, data.get("CEDIS"), data.get("V16"), data.get("LOC_TYPE"));
		SQLResult cedisMigrated = executeQuery(dbFCWMFSIT, formatCedisMigrated);
		System.out.println(formatCedisMigrated);
		if(!cedisMigrated.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(cedisMigrated);
		}
//		assertFalse(cedisMigrated.isEmpty(), "No se encuentra registro de CEDIS");
		
		/***************************Paso 4****************************/
		addStep("Consultar que las conexiones del Cedis (a probar) exista en la tabla WM_RDM_CONNECTIONS en el esquema WMUSER en "
				+ "la base de datos NGA_FCWMESIT_NREADER mediante la siguiente consulta:");
		
		String formatCedisConnections = String.format(queryCedisConnections, data.get("CEDIS"), data.get("TRANSACTION_TYPE"));
		SQLResult cedisConnections = executeQuery(dbFCWMESIT, formatCedisConnections);
		System.out.println(formatCedisConnections);
		if(!cedisConnections.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(cedisConnections);
		}
		assertFalse(cedisConnections.isEmpty(), "No se obtuvieron registros de conexiones");
		
		/***************************Paso 5****************************/
		addStep("Validar que se encuentre encendido el trigger RR07.Triggers:trgConsumeMsgLocation que ejecuta automáticamente "
				+ "la interfaz RR07 una vez detecte una nueva Tienda, el cual se encuentra ubicado "
				+ "en la ruta:Integration Server> Settings > Messaging > JMS Trigger Management.");
		
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
		
		assertFalse(triggerOnscreen, "No se encontro trigger encendido");
		
		/*Xpath de Status:
		 *		//a[contains(@href, 'Nombre de trigger')]
		 **/
		
		/***************************Paso 6****************************/
		addStep("Ejecutar el caso de prueba de la interface CR01 que crea la tienda hacía RMSv16, con nombre: MTC_001 CR01 "
				+ "Envío de información de nuevas tiendas a través de la interface CR01.");
		
		sendingNewStoreInfo.setTestCase(this.testCase);
		sendingNewStoreInfo.ATC_FT_001_CR01_EnvioDeInfoNuevasTiendas_test(data);
		
		/***************************Paso 7****************************/
		addStep("Consultar en las tablas de la BD RMS16QAMOMUAT la creación de la tienda mediante las "
				+ "siguientes consultas:");
		/*Consulta 1*/
		String formatCreatedStore = String.format(queryCreatedStore, data.get("store"));
		SQLResult createdStore = executeQuery(dbFCMOMUAT, formatCreatedStore);
		System.out.println(formatCreatedStore);
		if(!createdStore.isEmpty()) {
			testCase.addBoldTextEvidenceCurrentStep("Consulta 1:");
			testCase.addQueryEvidenceCurrentStep(createdStore);
		}
		testCase.addBoldTextEvidenceCurrentStep("Consulta 1:");
		testCase.addQueryEvidenceCurrentStep(createdStore);
		assertFalse(createdStore.isEmpty(), "No se obtuvieron registros de tienda");
		
		/*Consulta 2*/
		String formatCreatedStore1 = String.format(queryCreatedStore1, data.get("store"));
		SQLResult createdStore1 = executeQuery(dbFCMOMUAT, formatCreatedStore1);
		System.out.println(formatCreatedStore1);
		if(!createdStore1.isEmpty()) {
			testCase.addBoldTextEvidenceCurrentStep("Consulta 2:");
			testCase.addQueryEvidenceCurrentStep(createdStore1);
		}
		testCase.addBoldTextEvidenceCurrentStep("Consulta 2:");
		testCase.addQueryEvidenceCurrentStep(createdStore1);
		
		/***************************Paso 8****************************/
		addStep("Comprobar que no existan mensajes de error referentes a la interface FEMSA_RR07 en "
				+ "la tabla RIB_LOG_ERROR_INT de la BD FCWMFSIT,  mediante la siguiente consulta:");
		
		SQLResult interfaceErrors = executeQuery(dbFCWMFSIT, queryInterfaceErrors);
		System.out.println(queryInterfaceErrors);
		if(!interfaceErrors.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(interfaceErrors);
		}
		testCase.addQueryEvidenceCurrentStep(interfaceErrors);
		assertTrue(interfaceErrors.isEmpty(), "Se obtuvieron registros de error de la interface FEMSA_RR07");
		
		/***************************Paso 9****************************/
		addStep("Comprobar que no existan mensajes de error referentes al Framework FEMSA_FR_RIB en la tabla"
				+ " RIB_LOG_ERROR de la BD FCWMFSIT,  mediante la siguiente consulta:");
		
		SQLResult frameworkErrors = executeQuery(dbFCWMFSIT, queryFrameworkErrors);
		System.out.println(queryFrameworkErrors);
		if(!frameworkErrors.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(frameworkErrors);
		}
		testCase.addQueryEvidenceCurrentStep(frameworkErrors);
		assertTrue(frameworkErrors.isEmpty(), "Se obtuvieron registros de error del framework FEMSA_FR_RIB");
		
		/***************************Paso 10****************************/
		addStep("Comprobar que se genero el registros de la transacción de la publicación del mensaje en la "
				+ "tabla de control RIB_LOCATION_CONTROL de la BD FCWMFSIT,  mediante la siguiente consulta:");
		
		SQLResult transactionMessage = executeQuery(dbFCWMFSIT, queryTransactionMessage);
		System.out.println(queryTransactionMessage);
		if(!transactionMessage.isEmpty()) {
			String wmCode = transactionMessage.getData(0, "WM_CODE");
			testCase.addQueryEvidenceCurrentStep(transactionMessage);
			
			assertEquals(wmCode, data.get("wmCode"));
		}
		assertFalse(transactionMessage.isEmpty(), "No se generaron registros de la transaccion");
		
		/***************************Paso 11****************************/
		addStep("Comprobar que se haya creado un registro de respaldo del mensaje enviado en la tabla "
				+ "RIB_LOCATION_MSG_REP, al crear una tienda en RMS16, mediante la siguiente consulta:");
		
		SQLResult messageBackup = executeQuery(dbFCWMFSIT, queryMessageBackup);
		System.out.println(queryMessageBackup);
		if(!messageBackup.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(messageBackup);
		}
		assertFalse(messageBackup.isEmpty(), "No se creo respaldo del mensaje enviado en la tabla RIB_LOCATION_MSG_REP");
		
		/***************************Paso 12****************************/
		addStep("Comprobar que la informacion del Location/tienda creada en RMS v16  esté en las tablas de "
				+ "RDM v10 (FCRDMSIT), mediante la siguiente consulta:");
		String formatLocationInfoCreated = String.format(queryLocationInfoCreated, data.get("store"));
		SQLResult locationInfoCreated = executeQuery(dbFCRDMSIT, formatLocationInfoCreated); // Pendiente credenciales de DB
		System.out.println(formatLocationInfoCreated);
		if(!locationInfoCreated.isEmpty()) {
			testCase.addQueryEvidenceCurrentStep(locationInfoCreated);
		}
		assertFalse(locationInfoCreated.isEmpty(), "No se encontro inforacion del Location/tienda");
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
		return "Construido. MTC-FT-002 RIB_LOC Comprobar el envío de la información de RMSv16 hacia RDMv10 a través del framework RIB al crear una Tienda en RMSv16";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_RR07_RIB_LOC_EnvioInformacionTiendaCreadaDesdeRMSv16aRDMv10_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
