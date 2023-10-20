package interfaces.tpe.ols;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_OLS;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/**
 * Servicios Electronicos: MTC-FT-033 Transaccion exitosa de pago de servicio generico MERCADO LIBRE o LINIO o AMAZON.
 * Prueba de regresión  para comprobar la no afectacion en la funcionalidad principal de transaccionalidad de la interface TPE_OLS  (generico de Agiles) y TPE_FR al ser migradas de WM9.9 a WM10.5
 * @author Roberto Flores
 * @date   2022/05/26
 */
public class ATC_FT_004_TPE_OLS_TransaccionExitosaPagoMercadoLibre extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_TPE_OLS_TransaccionExitosaPagoMercadoLibre_test(HashMap<String, String> data) throws Exception {

		testCase.setPrerequisites("*Contar con un simulador de proveedor de MERCADO LIBRE o LINIO o AMAZON que responda las transacciones de forma exitosa.\r\n" + 
				"*Contar con acceso a las base de datos de FCTPEQA_MTY, FCTPEQA_QRO, FCWMLTAEQA_MTY y FCWMLTAEQA_QRO.\r\n" + 
				"*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA.");
		
		/**
		 * Proyecto: Servicios Electronicos (Regresion Enero 2023)
		 * Caso de prueba: Transaccion Exitosa Pago MercadoLibre
		 * @author Roberto
		 * @date 
		 */
		
		/*
		 * Utilerias
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		SQLUtil dbMtyFCTPE = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE,GlobalVariables.DB_PASSWORD_FCTPE);
		SQLUtil dbMtyFCTPE2 = new SQLUtil(GlobalVariables.DB_HOST_TPE_LOT,GlobalVariables.DB_USER_TPE_LOT, GlobalVariables.DB_PASSWORD_TPE_LOT);
		
		SQLUtil dbMtyFCWMLTAQ = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAQ_MTY,GlobalVariables.DB_USER_FCWMLTAQ_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAQ_MTY);
		TPE_OLS rhUtil = new TPE_OLS(data, testCase, dbMtyFCTPE);

		/*
		 * Variables
		 *********************************************************************/
		String wmCodeToValidateQRY01 = "101";
		String wmCodeToValidateTRN01 = "100";
		String wmCodeToValidateTRN02 = "000";
		String wmCodeToValidateTRN03 = "101";
		
		Date fechaEjecucionQRY01;
		Date fechaEjecucionTRN03;

		String consultaQRY1= "select operation, entity, folio,wm_code, application, creation_date from TPEUSER.TPE_FR_TRANSACTION \r\n"
				+ "where OPERATION = 'QRY01' \r\n"
				+ "and ENTITY = '"+ data.get("entityQRY01") +"'\r\n"
				+ "and FOLIO = '%s'\r\n"
				+ "and WM_CODE= '"+ wmCodeToValidateQRY01 +"'\r\n"
				+ "and APPLICATION= 'OLS'\r\n"
				+ "and CREATION_DATE >= (trunc(sysdate)) \r\n"
				+ "ORDER BY CREATION_DATE DESC ";
		
		String consultaTRN03 = "select operation, entity, folio,wm_code, application, creation_date, ack \r\n"
				+ "from TPEUSER.TPE_FR_TRANSACTION \r\n"
				+ "where OPERATION= 'TRN03' \r\n"
				+ "and ENTITY = '"+data.get("entity")+"'\r\n"
				+ "and FOLIO = '%s'\r\n"
				+ "and WM_CODE= '"+ wmCodeToValidateTRN03 +"'\r\n"
				+ "and APPLICATION= 'OLS'\r\n"
				+ "and ACK = '"+ data.get("ack") +"'\r\n"
				+ "and CREATION_DATE >= (trunc(sysdate)) \r\n"
				+ "ORDER BY CREATION_DATE DESC";
		
		String consultaError = "SELECT error_id, folio, error_date,description tpe_type \r\n" + 
				"FROM WMLOG.WM_LOG_ERROR_TPE \r\n" + 
				"WHERE   TPE_TYPE LIKE '%s' \r\n" + 
				"AND ERROR_DATE BETWEEN TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"AND TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"ORDER BY ERROR_DATE DESC";
		
		//----------------------------------------------------------------------------------------------------
		String consultaCanalNoSeguroQRY01 = "SELECT *\r\n" + 
				"FROM WMLOG.SECURITY_SESSION_LOG \r\n" + 
				"WHERE APPLICATION  like '%s'\r\n" + 
				"AND OPERATION = 'QRY01'\r\n" + 
				"AND ENTITY = '"+ data.get("entityQRY01") +"'\r\n" + 
				"AND PLAZA = '"+ data.get("plazaQRY01") +"'\r\n" + 
				"AND TIENDA = '"+ data.get("tiendaQRY01") +"'\r\n" + 
				"AND SOURCE = '"+ data.get("source") +"'\r\n" + 
				"AND CAJA = "+ data.get("cajaQRY01") +"\r\n" + 
				"AND CREATION_DATE BETWEEN TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"                  AND TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"ORDER BY CREATION_DATE DESC ";
		
		String consultaCanalNoSeguroTRN01 = "SELECT *\r\n" + 
				"FROM WMLOG.SECURITY_SESSION_LOG \r\n" + 
				"WHERE APPLICATION  like '%s'\r\n" + 
				"AND OPERATION = 'TRN01'\r\n" + 
				"AND ENTITY = '"+ data.get("entity") +"'\r\n" + 
				"AND PLAZA = '"+ data.get("plazaTRN01") +"'\r\n" + 
				"AND TIENDA = '"+ data.get("tiendaTRN01") +"'\r\n" + 
				"AND SOURCE = '"+ data.get("source") +"'\r\n" + 
				"AND CAJA = "+ data.get("cajaTRN01") +"\r\n" + 
				"AND CREATION_DATE BETWEEN TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"                  AND TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"ORDER BY CREATION_DATE DESC";
		
		String consultaCanalNoSeguroTRN02 = "SELECT *\r\n" + 
				"FROM WMLOG.SECURITY_SESSION_LOG \r\n" + 
				"WHERE APPLICATION  like '%s'\r\n" + 
				"AND OPERATION = 'TRN02'\r\n" + 
				"AND FOLIO = %s\r\n" + 
				"ORDER BY CREATION_DATE DESC";
		
		String consultaCanalNoSeguroTRN03 = "SELECT *\r\n" + 
				"FROM WMLOG.SECURITY_SESSION_LOG \r\n" + 
				"WHERE APPLICATION  like '%s'\r\n" + 
				"AND OPERATION = 'TRN03'\r\n" + 
				"AND FOLIO = %s\r\n" + 
				"ORDER BY CREATION_DATE DESC";
		//----------------------------------------------------------------------------------------------------

		String folioQRY01;
		String folioTRN01;
		
		
		String wmCodeRequestQRY01;
		String wmCodeRequestTRN01;
		String wmCodeRequestTRN02;
		String wmCodeRequestTRN03;
		
		String respuestaQRY01;
		String respuestaTRN01;
		String respuestaTRN02;
		String respuestaTRN03;
	

		testCase.setProject_Name("POC WMx86");

		/**************************************************************************************************
		 * Consulta QRY01
		 * 
		 *************************************************************************************************/
		
		 
		// Paso 1 ****************************************************
		addStep("Ejecutar desde el navegador el  servicio de solicitud Consulta de referencia de TPE.OLS.Pub.request");
		System.out.println("Paso 1: ");
		System.out.println("Consulta QRY01");

		//Se soicita fecha de inicio del request		
		fechaEjecucionQRY01 = new Date();
		
		//Se solicita el request del QRY01
		 respuestaQRY01 = rhUtil.ejecutarQRY01Generico();

		System.out.println("Respuesta QRY01 \r\n: " + respuestaQRY01);		

		folioQRY01 = RequestUtil.getFolioXml(respuestaQRY01);
		wmCodeRequestQRY01 = RequestUtil.getWmCodeXml(respuestaQRY01);

		
		boolean validaRespuestaQRY01 = true;
		if (respuestaQRY01 != null) {
			validaRespuestaQRY01 = false;
		}
		assertFalse(validaRespuestaQRY01); 

		/*
		 *
		 *****************************************************************************************/
		//Valida el status esperado del QRY01
		addStep("Verificar que el XML de respuesta tenga como wmCode: " + wmCodeToValidateQRY01);

		boolean validationRequestQRY01 = wmCodeRequestQRY01.equals(wmCodeToValidateQRY01);
		
		System.out.println(validationRequestQRY01 + " - wmCode request QRY01 : " + wmCodeRequestQRY01);
		
		//testCase.addTextEvidenceCurrentStep("Respuesta QRY01: \r\n" + respuestaQRY01);
		
		testCase.addTextEvidenceCurrentStep(
				"Código esperado: " + wmCodeToValidateQRY01  + "\r\n" + "Código XML: " + wmCodeRequestQRY01);

		assertTrue(validationRequestQRY01, "El codigo wmCode no es el esperado: " + wmCodeToValidateQRY01);
		
		 
		// Paso 2 *****************************************************************************************
		addStep("Ejecutar desde el navegador el  servicio de solicitud de folio de TPE.OLS.Pub.request  usando el server QA8");
		System.out.println("Paso 2: ");
		System.out.println("TRN01");
	
		//Se solicita el request del TRN01
		respuestaTRN01 = rhUtil.ejecutarTRN01Generico();

		System.out.println("Respuesta TRN01 \r\n: " + respuestaTRN01);		

		folioTRN01 = RequestUtil.getFolioXml(respuestaTRN01);
		
		wmCodeRequestTRN01 = RequestUtil.getWmCodeXml(respuestaTRN01);

		boolean validaRespuestaTRN01 = true;
		if (respuestaTRN01 != null) {
			validaRespuestaTRN01 = false;
		}
		assertFalse(validaRespuestaTRN01);

		/*
		 *
		 *****************************************************************************************/
		//Valida el status esperado del TRN01
		addStep("Verificar que el XML de respuesta tenga como wmCode: " + wmCodeToValidateTRN01);
		
		boolean  validationRequestTRN01 = wmCodeRequestTRN01.equals(wmCodeToValidateTRN01);
		
		System.out.println(validationRequestTRN01 + " - wmCode request TRN01: " + wmCodeRequestTRN01);
		
	//	testCase.addTextEvidenceCurrentStep("\nRespuestaTRN01 : \r\n" + respuestaTRN01);
		
		testCase.addTextEvidenceCurrentStep(
				"Código esperado: " + wmCodeToValidateTRN01 + "\r\n" + "Código XML: " + wmCodeRequestTRN01);

		assertTrue(validationRequestTRN01, "El codigo wmCode no es el esperado: " + wmCodeRequestTRN01);
		
		  
		// Paso 3 *****************************************************************************************
		addStep("Ejecutar desde el navegador el servicio de solicitud de autorización  de TPE.OLS.Pub.request, Usando el server QA8");
		System.out.println("Paso 3: ");
		System.out.println("TRN02");
	
		//Se solicita el request del TRN02
		respuestaTRN02 = rhUtil.ejecutarTRN02Generico(folioTRN01); 

		System.out.println("Respuesta TRN02 \r\n: " + respuestaTRN02);		

		RequestUtil.getFolioXml(respuestaTRN02);
		//folio = "1";
		
		wmCodeRequestTRN02 = RequestUtil.getWmCodeXml(respuestaTRN02);

		boolean validaRespuestaTRN02 = true;
		if (respuestaTRN02 != null) {
			validaRespuestaTRN02 = false;
		}
		assertFalse(validaRespuestaTRN02);

		/*
		 *
		 *****************************************************************************************/
		//Valida el status esperado del TRN02
		addStep("Verificar que el XML de respuesta tenga como wmCode: " + wmCodeToValidateTRN02);

		boolean  validationRequestTRN02 = wmCodeRequestTRN02.equals(wmCodeToValidateTRN02);
		
		System.out.println(validationRequestTRN02 + " - wmCode request TRN02: " + wmCodeRequestTRN02);
		
	   //testCase.addTextEvidenceCurrentStep("\nRespuestaTRN02 : \r\n" + respuestaTRN02);
		
		testCase.addTextEvidenceCurrentStep(
				"Código esperado: " + wmCodeToValidateTRN02 + "\r\n" + "Código XML: " + wmCodeRequestTRN02);
 
		assertTrue(validationRequestTRN02, "El codigo wmCode no es el esperado: " + wmCodeRequestTRN02);
		
		
		 
		// Paso 4 *****************************************************************************************
		addStep("Ejecutar desde el navegador el servicio  de solicitud de confirmación Ack de TPE.OLS.Pub.request, Usando el server QA8");
		System.out.println("Paso 4: ");
		System.out.println("TRN03");
	
		//Se solicita el request del TRN03
		respuestaTRN03 = rhUtil.ejecutarTRN03(folioTRN01); 
		fechaEjecucionTRN03 = new Date();
		
		System.out.println("Respuesta TRN03: " + respuestaTRN03);		

		//folio = RequestUtil.getFolioXml(respuestaTRN03);
		
		wmCodeRequestTRN03 = RequestUtil.getWmCodeXml(respuestaTRN03);

		boolean validaRespuestaTRN03 = true;
		if (respuestaTRN03 != null) {
			validaRespuestaTRN03 = false;
		}
		assertFalse(validaRespuestaTRN03);

		/*
		 *
		 *****************************************************************************************/
		//Valida el status esperado del TRN03
		addStep("Verificar que el XML de respuesta tenga como wmCode: " + wmCodeToValidateTRN03);

		boolean  validationRequestTRN03 = wmCodeRequestTRN03.equals(wmCodeToValidateTRN03);
		
		System.out.println(validationRequestTRN03 + " - wmCode request TRN03: " + wmCodeRequestTRN03);
		
	//	testCase.addTextEvidenceCurrentStep("\nRespuestaTRN03 : \r\n" + respuestaTRN03);
		
		testCase.addTextEvidenceCurrentStep(
				"Código esperado: " + wmCodeToValidateTRN03 + "\r\n" + "Código XML: " + wmCodeRequestTRN03);
     
		assertTrue(validationRequestTRN03, "El codigo wmCode no es el esperado: " + wmCodeRequestTRN03);

	//  Paso 5 *********************************************************************************************************
		
		addStep("Establecer la conexión con la BD * FCTPEQA*  en site 1 y site 2");
		System.out.println("Paso 5: ");
		boolean conexionFCTPEQA = true;
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCTPEQA site 1");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCTPE);
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCTPEQA site 2");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_TPE_LOT);
		
		assertTrue(conexionFCTPEQA, "La conexion no fue exitosa");
		 
	 //Paso 6 *******************************************************************************************************
		
		addStep(" Ejecutar la siguiente consulta en la BD * FCTPEQA* en site 1 para validar el registro de transacción");
		System.out.println("Paso 6: ");
				//Consulta de transaccion
				String consultaQRY1_f = String.format(consultaQRY1, folioQRY01);
				
				System.out.println("Consulta de registro de transacción site 1: \r\n "+ consultaQRY1_f);
			
				SQLResult consultaQRY1_r = executeQuery(dbMtyFCTPE, consultaQRY1_f);
				
				//Transaccion pago servicio
		        String consultaTRN03_f = String.format(consultaTRN03, folioTRN01);
				
				System.out.println("Transacción exitosa de pago anticipado al empleados site 1: \r\n"+ consultaTRN03_f);
		
				SQLResult consultaTRN03_r = executeQuery(dbMtyFCTPE, consultaTRN03_f);
				
			    
				boolean validaQRY1 = consultaQRY1_r.isEmpty();
				boolean validaTRN03 = consultaTRN03_r.isEmpty();
		        boolean querys = false;
		        
		        testCase.addQueryEvidenceCurrentStep(consultaQRY1_r, true);
		        if(!validaQRY1) {
		        	testCase.addTextEvidenceCurrentStep("consulta QRY1 encontrada en BD site 1");
		        } else {
		        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta QRY1 site 1");
		        }
		        
		        testCase.addQueryEvidenceCurrentStep(consultaTRN03_r, true);
		        if(!validaTRN03) {
		        	testCase.addTextEvidenceCurrentStep("consulta TRN03 encontrada en BD site 1");
		        } else {
		        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta TRN03 site 1");
		        }
		        
		        if((!validaQRY1)&&(!validaTRN03)) {
		        	querys = true;
		        }
				assertTrue(querys, "No se insertaron datos en la tabla TPUSER.TPE_FR_TRANSACTION site 1"); 
				
		addStep(" Ejecutar la siguiente consulta en la BD * FCTPEQA* en site 2 para validar el registro de transacción");
			
				Thread.sleep(10000);
		
				consultaQRY1_r = executeQuery(dbMtyFCTPE2, consultaQRY1_f);
				
				//Transaccion pago servicio
		
				consultaTRN03_r = executeQuery(dbMtyFCTPE2, consultaTRN03_f);
				
			    
				validaQRY1 = consultaQRY1_r.isEmpty();
				validaTRN03 = consultaTRN03_r.isEmpty();
		        querys = false;
		        
		        testCase.addQueryEvidenceCurrentStep(consultaQRY1_r, true);
		        if(!validaQRY1) {
		        	testCase.addTextEvidenceCurrentStep("consulta QRY1 encontrada en BD site 2");
		        } else {
		        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta QRY1 site 2");
		        }
		        
		        testCase.addQueryEvidenceCurrentStep(consultaTRN03_r, true);
		        if(!validaTRN03) {
		        	testCase.addTextEvidenceCurrentStep("consulta TRN03 encontrada en BD site 2");
		        } else {
		        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta TRN03 site 2");
		        }
		        
		        if((!validaQRY1)&&(!validaTRN03)) {
		        	querys = true;
		        }
				assertTrue(querys, "No se insertaron datos en la tabla TPUSER.TPE_FR_TRANSACTION site 2"); 
		
		//Paso 7 ***************************************************************************************************

		addStep("Conectarse a la Base de Datos *FCWMLTAEQA* en site1.");
        
		boolean conexionFCWMLTAQ = true;
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCWMLTAQ ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLTAQ_MTY);
		
		assertTrue(conexionFCWMLTAQ, "La conexion no fue exitosa");
	
	
	
		//Paso 8 *****************************************************************************************************************
		addStep("Ejecutar la siguiente consulta   en la base de datos *FCWMLTAEQA* buscando que no se encuentren registros de error de pago de servicio de Mercado Libre");
		
		
		
		String fechaEjecucionQRY01_f = formatter.format(fechaEjecucionQRY01);
		String fechaEjecucionTRN03_f = formatter.format(fechaEjecucionTRN03);
		
		String consultaError_f = String.format(consultaError, "%OLS%", fechaEjecucionQRY01_f, fechaEjecucionTRN03_f);
		
		System.out.println(consultaError_f);
	
		SQLResult consultaError_r = dbMtyFCWMLTAQ.executeQuery(consultaError_f);
	
		boolean validaConsultaError = consultaError_r.isEmpty();
	
		System.out.println(validaConsultaError);
		
		testCase.addQueryEvidenceCurrentStep(consultaError_r, true);
		if (!validaConsultaError) {
			testCase.addTextEvidenceCurrentStep("Error encontrado en WMLOG.WM_LOG_ERROR_TPE");
		} else {
			testCase.addTextEvidenceCurrentStep("No se encontraron errores en WMLOG.WM_LOG_ERROR_TPE");
		}
	
		assertTrue(validaConsultaError, "Se visualizan errores en la base de datos");
		
		//Paso 9 *****************************************************************************************************************
		addStep("Ejecutar  la siguiente consulta  en la BD   *FCWMLTAEQA* en site 1 y site 2 para verificar  que no  viajo por un canal seguro.");
		
		
		//QRY01-----------------------------------------------------------------------------------------------------------------------------------------------
				String consultaCanalSeguro_f = String.format(consultaCanalNoSeguroQRY01, "%OLS%", fechaEjecucionQRY01_f, fechaEjecucionTRN03_f);
				
				System.out.println(consultaCanalSeguro_f);
				
				SQLResult consultaCanalSeguro_r = dbMtyFCWMLTAQ.executeQuery(consultaCanalSeguro_f);
				
				boolean validaConsultaCanalSeguro = consultaCanalSeguro_r.isEmpty();
				
				System.out.println(validaConsultaCanalSeguro);
				
				testCase.addQueryEvidenceCurrentStep(consultaCanalSeguro_r);
				if (!validaConsultaCanalSeguro) {
					testCase.addTextEvidenceCurrentStep("Registro encontrado en WMLOG.SECURITY_SESSION_LOG QRY01");
				} else {
					testCase.addTextEvidenceCurrentStep("No se visualizan los registros de las invocaciones realizadas en WMLOG.SECURITY_SESSION_LOG QRY01");
				}
				
				assertFalse(validaConsultaCanalSeguro, "No se visualizan los registros de las invocaciones realizadas en WMLOG.SECURITY_SESSION_LOG QRY01");
	
		//TRN01-----------------------------------------------------------------------------------------------------------------------------------------------
				consultaCanalSeguro_f = String.format(consultaCanalNoSeguroTRN01, "%OLS%", fechaEjecucionQRY01_f, fechaEjecucionTRN03_f);
				
				System.out.println(consultaCanalSeguro_f);
				
				consultaCanalSeguro_r = dbMtyFCWMLTAQ.executeQuery(consultaCanalSeguro_f);
				
				validaConsultaCanalSeguro = consultaCanalSeguro_r.isEmpty();
				
				System.out.println(validaConsultaCanalSeguro);
				
				testCase.addQueryEvidenceCurrentStep(consultaCanalSeguro_r);
				if (!validaConsultaCanalSeguro) {
					testCase.addTextEvidenceCurrentStep("Registro encontrado en WMLOG.SECURITY_SESSION_LOG TRN01");
				} else {
					testCase.addTextEvidenceCurrentStep("No se visualizan los registros de las invocaciones realizadas en WMLOG.SECURITY_SESSION_LOG TRN01");
				}
				
				assertFalse(validaConsultaCanalSeguro, "No se visualizan los registros de las invocaciones realizadas en WMLOG.SECURITY_SESSION_LOG TRN01");
		
		//TRN02-----------------------------------------------------------------------------------------------------------------------------------------------
				consultaCanalSeguro_f = String.format(consultaCanalNoSeguroTRN02, "%OLS%", folioTRN01);
				
				System.out.println(consultaCanalSeguro_f);
				
				consultaCanalSeguro_r = dbMtyFCWMLTAQ.executeQuery(consultaCanalSeguro_f);
				
				validaConsultaCanalSeguro = consultaCanalSeguro_r.isEmpty();
				
				System.out.println(validaConsultaCanalSeguro);
				
				testCase.addQueryEvidenceCurrentStep(consultaCanalSeguro_r);
				if (!validaConsultaCanalSeguro) {
					testCase.addTextEvidenceCurrentStep("Registro encontrado en WMLOG.SECURITY_SESSION_LOG TRN02");
				} else {
					testCase.addTextEvidenceCurrentStep("No se visualizan los registros de las invocaciones realizadas en WMLOG.SECURITY_SESSION_LOG TRN02");
				}
				
				assertFalse(validaConsultaCanalSeguro, "No se visualizan los registros de las invocaciones realizadas en WMLOG.SECURITY_SESSION_LOG TRN02");

		//TRN03-----------------------------------------------------------------------------------------------------------------------------------------------
				consultaCanalSeguro_f = String.format(consultaCanalNoSeguroTRN03, "%OLS%", folioTRN01);
				
				System.out.println(consultaCanalSeguro_f);
				
				consultaCanalSeguro_r = dbMtyFCWMLTAQ.executeQuery(consultaCanalSeguro_f);
				
				validaConsultaCanalSeguro = consultaCanalSeguro_r.isEmpty();
				
				System.out.println(validaConsultaCanalSeguro);
				
				testCase.addQueryEvidenceCurrentStep(consultaCanalSeguro_r);
				if (!validaConsultaCanalSeguro) {
					testCase.addTextEvidenceCurrentStep("Registro encontrado en WMLOG.SECURITY_SESSION_LOG TRN03");
				} else {
					testCase.addTextEvidenceCurrentStep("No se visualizan los registros de las invocaciones realizadas en WMLOG.SECURITY_SESSION_LOG TRN03");
				}
				
				assertFalse(validaConsultaCanalSeguro, "No se visualizan los registros de las invocaciones realizadas en WMLOG.SECURITY_SESSION_LOG TRN03");
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_004_TPE_OLS_TransaccionExitosaPagoMercadoLibre_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "ATC-FT-033 Transacción exitosa de pago de servicio generico MERCADO LIBRE o LINIO o AMAZON.";
	}

	@Override
	public String setTestDesigner() {
		return "AutomationQA";
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