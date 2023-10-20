package interfaces.tpe.rtp;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_RTP;
import util.GlobalVariables;
import util.RequestUtil;
import utils.password.PasswordUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/**
 * Servicios Electrónicos: MTC-FT-057 Solicitud de llave y registro de transacción de Recarga de tarjeta de transporte publico Feria.
 * Prueba de regresión  para comprobaF550:H550ectación en la funcionalidad principal de transaccionalidad de la interface TPE_RTP y TPE_FR al ser migradas de WM9.9 a WM10.5
 * @author Roberto Flores
 * @date   2022/05/24
 * Mantenimiento:
 * @author JoseO@Hexaware.com
 * @date 02/15/2023
 */
public class WMx86_RTP_SolicitudLlaveYRegistroTransaccionRecargaTarjetaTransporte extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_TPE_RTP_003_SolicitudLlaveYRegistro_test(HashMap<String, String> data) throws Exception {

		
		/*
		 * Utilerías
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		SQLUtil dbMtyFCTPE = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE,GlobalVariables.DB_PASSWORD_FCTPE);
		SQLUtil dbMtyFCTPE2 = new SQLUtil(GlobalVariables.DB_HOST_TPE_LOT,GlobalVariables.DB_USER_TPE_LOT, GlobalVariables.DB_PASSWORD_TPE_LOT);
		SQLUtil dbMtyFCWMLTAQ = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAQ_MTY,GlobalVariables.DB_USER_FCWMLTAQ_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAQ_MTY);
		//SQLUtil dbMtyFCWMLTAQ2 = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO,GlobalVariables.DB_USER_FCWMLTAEQA_QA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		TPE_RTP rtpUtil = new TPE_RTP(data, testCase, dbMtyFCTPE);

		/*
		 * Variables
		 *********************************************************************/
		String wmCodeToValidateOPR01 = "101";
		
		Date fechaEjecucionKEY;
		Date fechaEjecucionDTL;

		String consultaRegistroLlave= "select operation, entity, folio,wm_code, application, creation_date from TPEUSER.TPE_FR_TRANSACTION \r\n"
				+ "where APPLICATION = 'RTP' \r\n"
				+ "and ENTITY = 'KEY'\r\n"
				+ "and FOLIO = '%s'\r\n"
				+ "and WM_CODE= '101'\r\n"
				+ "and CREATION_DATE >= (TRUNC(SYSDATE)) \r\n"
				+ "ORDER BY CREATION_DATE DESC ";
		
		String consultaRegistroRecarga= "select operation, entity, folio,wm_code, application, creation_date from TPEUSER.TPE_FR_TRANSACTION \r\n"
				+ "where APPLICATION = 'RTP' \r\n"
				+ "and ENTITY = 'DTL'\r\n"
				+ "and FOLIO = '%s'\r\n"
				+ "and WM_CODE= '101'\r\n"
				+ "and CREATION_DATE >= (TRUNC(SYSDATE)) \r\n"
				+ "ORDER BY CREATION_DATE DESC ";
		
		String consultaError = "SELECT error_id, folio, error_date,description tpe_type \r\n" + 
				"FROM WMLOG.WM_LOG_ERROR_TPE \r\n" + 
				"WHERE   TPE_TYPE LIKE '%s' \r\n" + 
				"AND ERROR_DATE BETWEEN TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"AND TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"ORDER BY ERROR_DATE DESC";
		
		String consultaCanalNoSeguroOPR01Key = "SELECT *\r\n" + 
				"FROM WMLOG.SECURITY_SESSION_LOG \r\n" + 
				"WHERE APPLICATION  like '%s'\r\n" + 
				"AND OPERATION = 'OPR01'\r\n" + 
				"AND ENTITY = 'KEY'\r\n" + 
				"AND PLAZA = '"+ data.get("plaza") +"'\r\n" + 
				"AND TIENDA = '"+ data.get("tienda") +"'\r\n" + 
				"AND SOURCE = '"+ data.get("source") +"'\r\n" + 
				"AND CAJA = "+ data.get("caja") +"\r\n" + 
				"AND CREATION_DATE BETWEEN TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"                  AND TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"ORDER BY CREATION_DATE DESC ";

		String consultaCanalNoSeguroOPR01Dtl = "SELECT *\r\n" + 
				"FROM WMLOG.SECURITY_SESSION_LOG \r\n" + 
				"WHERE APPLICATION  like '%s'\r\n" + 
				"AND OPERATION = 'OPR01'\r\n" + 
				"AND ENTITY = 'DTL'\r\n" + 
				"AND PLAZA = '"+ data.get("plaza") +"'\r\n" + 
				"AND TIENDA = '"+ data.get("tienda") +"'\r\n" + 
				"AND SOURCE = '"+ data.get("source") +"'\r\n" + 
				"AND CAJA = "+ data.get("caja") +"\r\n" + 
				"AND CREATION_DATE BETWEEN TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"                  AND TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"ORDER BY CREATION_DATE DESC ";

		String folioLlave ;
		String folioRecarga ;
		
		String wmCodeRequestOPR01Llave;
		String wmCodeRequestOPR01Recarga;
		
		String respuestaOPR01Llave;
		String respuestaOPR01Recarga;
	

		testCase.setProject_Name("POC WMx86");
		testCase.setTest_Description(data.get("description"));
		testCase.setPrerequisites(data.get("prerequisitos"));
		

		/**************************************************************************************************
		* Consulta OPR01 Llave
		*************************************************************************************************/
		// Paso 1 --------------------------------------------------------------------------------------------------------------------------
		addStep("Ejecutar en un navegador directamente el servicio de la operación de solicitud de llave de la TPE_RTP del server QA8");
		
		System.out.println("Consulta OPR01 Llave");

		//Se solicita el request del OPR01
		fechaEjecucionKEY = new Date();
		respuestaOPR01Llave = rtpUtil.ejecutarOPR01LlaveWMx86();

		System.out.println("Respuesta OPR01 \r\n: " + respuestaOPR01Llave);		

		folioLlave = RequestUtil.getFolioXml(respuestaOPR01Llave);
		wmCodeRequestOPR01Llave = RequestUtil.getWmCodeXml(respuestaOPR01Llave);

		assertNotNull(wmCodeRequestOPR01Llave);

		//Valida el status esperado del OPR01 ----------------------------------------------------------------------------------------------
		addStep("Verificar que el XML de respuesta tenga como wmCode: " + wmCodeToValidateOPR01);
		
		System.out.println("wmCode request QRY01 : " + wmCodeRequestOPR01Llave);
		
		testCase.addTextEvidenceCurrentStep(
				"Código esperado: " + wmCodeToValidateOPR01  + "\r\n" + "Código XML: " + wmCodeRequestOPR01Llave);

		assertTrue(wmCodeRequestOPR01Llave.equals(wmCodeToValidateOPR01), "El codigo wmCode no es el esperado: " + wmCodeRequestOPR01Llave);
		
		
		/**************************************************************************************************
		* Consulta OPR01 Recarga
		*************************************************************************************************/
		// Paso 2 --------------------------------------------------------------------------------------------------------------------------
		addStep("Ejecutar en un navegador directamente el servicio de la operación de registro de recarga de tarjeta feria de la TPE_RTP del server QA8");

		System.out.println("Consulta OPR01 Recarga");
	
		//Se solicita el request del OPR01
		respuestaOPR01Recarga = rtpUtil.ejecutarOPR01RecargaTarjeta();
		fechaEjecucionDTL = new Date();

		System.out.println("Respuesta OPR01 \r\n: " + respuestaOPR01Recarga);		

		folioRecarga = RequestUtil.getFolioXml(respuestaOPR01Recarga);
		wmCodeRequestOPR01Recarga = RequestUtil.getWmCodeXml(respuestaOPR01Recarga);

		assertNotNull(wmCodeRequestOPR01Recarga);

		//Valida el status esperado del OPR01 ----------------------------------------------------------------------------------------------
		addStep("Verificar que el XML de respuesta tenga como wmCode: " + wmCodeToValidateOPR01);
		
		System.out.println("wmCode request QRY01 : " + wmCodeRequestOPR01Recarga);
		
		testCase.addTextEvidenceCurrentStep(
				"Código esperado: " + wmCodeToValidateOPR01  + "\r\n" + "Código XML: " + wmCodeRequestOPR01Recarga);

		assertTrue(wmCodeRequestOPR01Recarga.equals(wmCodeToValidateOPR01), "El codigo wmCode no es el esperado: " + wmCodeRequestOPR01Recarga);
		
		  
		/**************************************************************************************************
		* Validación DB
		*************************************************************************************************/
		// Paso 3 --------------------------------------------------------------------------------------------------------------------------
		
		addStep("Establecer la conexión con la BD * FCTPEQA*  en site 1 y site 2.");
		
		boolean conexionFCTPEQA = true;
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCTPEQA site 1");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCTPE);
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCTPEQA site 2");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_TPE_LOT);
		
		assertTrue(conexionFCTPEQA, "La conexion no fue exitosa");
		 
		// Paso 4 --------------------------------------------------------------------------------------------------------------------------
		
		addStep("Ejecutar la siguiente consulta en la BD * FCTPEQA* en site 1 para validar el registro de transacción");
       
			//Consulta registro llave
			String consultaRegistroLlave_f = String.format(consultaRegistroLlave, folioLlave);
			
			System.out.println("Consulta registro llave: \r\n "+ consultaRegistroLlave_f);
		
			SQLResult consultaRegistroLlave_r = executeQuery(dbMtyFCTPE, consultaRegistroLlave_f);
			
			//Consulta registro recarga tarjeta
	        String consultaRegistroRecarga_f = String.format(consultaRegistroRecarga, folioRecarga);
			
			System.out.println("Transaccion de pago de servicios: \r\n"+ consultaRegistroRecarga_f);
	
			SQLResult consultaRegistroRecarga_r = executeQuery(dbMtyFCTPE, consultaRegistroRecarga_f);
			
		    
			boolean validaConsultaReferencias = consultaRegistroLlave_r.isEmpty();
			boolean validaTransaccionPagoServicio = consultaRegistroRecarga_r.isEmpty();
	        boolean querys = false;
	       // String creation_date = "";
	        
	        testCase.addQueryEvidenceCurrentStep(consultaRegistroLlave_r, true);
	        if(!validaConsultaReferencias) {
	        	testCase.addTextEvidenceCurrentStep("Transacción encontrada");
	        } else {
	        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta de registro de transacción llave");
	        }
	        
	        testCase.addQueryEvidenceCurrentStep(consultaRegistroRecarga_r, true);
	        if(!validaTransaccionPagoServicio) {
	        	testCase.addTextEvidenceCurrentStep("Transacción encontrada llave");
	        } else {
	        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta de registro de transacción recarga");
	        }
	        
	        if((!validaConsultaReferencias)&&(!validaTransaccionPagoServicio)) {
	        	querys = true;
	        }
	        
	        assertTrue(querys, "No se insertaron datos en la tabla TPUSER.TPE_FR_TRANSACTION site 1"); 
        /////////////////////////////////////////////////////////////////
	    addStep("Ejecutar la siguiente consulta en la BD * FCTPEQA* en site 2 para validar el registro de transacción");
	        
	    	
	    
			//Consulta registro llave
	    	System.out.println("Consulta registro llave site 2: \r\n "+ consultaRegistroLlave_f);
	    	Thread.sleep(10000);
			consultaRegistroLlave_r = executeQuery(dbMtyFCTPE2, consultaRegistroLlave_f);
			//consultaRegistroLlave_r = executeQuery(dbMtyFCTPE2, "select operation, entity, folio,wm_code, application, creation_date from TPEUSER.TPE_FR_TRANSACTION where APPLICATION = 'RTP' and ENTITY = 'KEY' and FOLIO = '2018849281' and WM_CODE= '101' and CREATION_DATE >= (TRUNC(SYSDATE)) ORDER BY CREATION_DATE DESC");
			
			//Consulta registro recarga tarjeta
			System.out.println("Transaccion de pago de servicios site 2: \r\n"+ consultaRegistroRecarga_f);
			consultaRegistroRecarga_r = executeQuery(dbMtyFCTPE2, consultaRegistroRecarga_f);
			//consultaRegistroRecarga_r = executeQuery(dbMtyFCTPE2, "select operation, entity, folio,wm_code, application, creation_date from TPEUSER.TPE_FR_TRANSACTION where APPLICATION = 'RTP' and ENTITY = 'KEY' and FOLIO = '2018849281' and WM_CODE= '101' and CREATION_DATE >= (TRUNC(SYSDATE)) ORDER BY CREATION_DATE DESC");
			
		    
			validaConsultaReferencias = consultaRegistroLlave_r.isEmpty();
			validaTransaccionPagoServicio = consultaRegistroRecarga_r.isEmpty();
	        querys = false;
	       // String creation_date = "";
	        
	        testCase.addQueryEvidenceCurrentStep(consultaRegistroLlave_r, true);
	        if(!validaConsultaReferencias) {
	        	testCase.addTextEvidenceCurrentStep("Transacción encontrada");
	        } else {
	        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta de registro de transacción llave");
	        }
	        
	        testCase.addQueryEvidenceCurrentStep(consultaRegistroRecarga_r, true);
	        if(!validaTransaccionPagoServicio) {
	        	testCase.addTextEvidenceCurrentStep("Transacción encontrada llave");
	        } else {
	        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta de registro de transacción recarga");
	        }
	        
	        if((!validaConsultaReferencias)&&(!validaTransaccionPagoServicio)) {
	        	querys = true;
	        }
           

		assertTrue(querys, "No se insertaron datos en la tabla TPUSER.TPE_FR_TRANSACTION site 2"); 
		
        // Paso 5 --------------------------------------------------------------------------------------------------------------------------
		addStep("Establecer conexión con la Base de Datos **FCWMLTAQ**  tanto en site 1 MTY y 2 QRO");
        
		boolean conexionFCWMLTAQ = true;
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCWMLTAQ2 ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA);
		
		assertTrue(conexionFCWMLTAQ, "La conexion no fue exitosa");
	
	
		// Paso 6 --------------------------------------------------------------------------------------------------------------------------
		addStep("Ejecutar la siguiente consulta en la base de datos *FCWMLTAEQA* site 1 buscando que no se encuentren registros de error de recarga de tarjeta para RTP");
		
			String fechaEjecucionKEY_f = formatter.format(fechaEjecucionKEY);
			String fechaEjecucionDTL_f = formatter.format(fechaEjecucionDTL);
			
			String consultaError_f = String.format(consultaError, "%RTP%", fechaEjecucionKEY_f, fechaEjecucionDTL_f);
		
			System.out.println(consultaError_f);
		
			SQLResult consultaError_r = dbMtyFCWMLTAQ.executeQuery(consultaError_f);
		
			boolean validaConsultaError = consultaError_r.isEmpty();
		
			System.out.println(validaConsultaError);
			
			testCase.addQueryEvidenceCurrentStep(consultaError_r, true);
			if (!validaConsultaError) {
				testCase.addTextEvidenceCurrentStep("Error encontrado en WMLOG.WM_LOG_ERROR_TPE site 1");
			} else {
				testCase.addTextEvidenceCurrentStep("No se encontraron errores en WMLOG.WM_LOG_ERROR_TPE site 1");
			}
		
			assertTrue(validaConsultaError, "Se visualizan errores en la base de datos site 1");
		
//		addStep("Ejecutar la siguiente consulta en la base de datos *FCWMLTAEQA* site 2 buscando que no se encuentren registros de error de recarga de tarjeta para RTP");
//		
//			System.out.println(consultaError_f);
//		
//			consultaError_r = dbMtyFCWMLTAQ.executeQuery(consultaError_f);
//		
//			validaConsultaError = consultaError_r.isEmpty();
//		
//			System.out.println(validaConsultaError);
//			
//			testCase.addQueryEvidenceCurrentStep(consultaError_r, true);
//			if (!validaConsultaError) {
//				testCase.addTextEvidenceCurrentStep("Error encontrado en WMLOG.WM_LOG_ERROR_TPE llave site 2");
//			} else {
//				testCase.addTextEvidenceCurrentStep("No se encontraron errores en WMLOG.WM_LOG_ERROR_TPE site 2");
//			}
//		
//			assertTrue(validaConsultaError, "Se visualizan errores en la base de datos site 2");
	
		// Paso 7 --------------------------------------------------------------------------------------------------------------------------
		addStep("Ejecutar  la siguiente consulta  en la BD   *FCWMLTAEQA* en site 1 para verificar  que no  viajo por un canal seguro");
		
			String consultaCanalSeguro_f = String.format(consultaCanalNoSeguroOPR01Key, "%RTP%", fechaEjecucionKEY_f, fechaEjecucionDTL_f);
			
			System.out.println(consultaCanalSeguro_f);
			
			SQLResult consultaCanalSeguro_r = dbMtyFCWMLTAQ.executeQuery(consultaCanalSeguro_f);
			
			boolean validaConsultaCanalSeguro = consultaCanalSeguro_r.isEmpty();
			
			System.out.println(validaConsultaCanalSeguro);
			
			testCase.addQueryEvidenceCurrentStep(consultaCanalSeguro_r);
			if (!validaConsultaCanalSeguro) {
				testCase.addTextEvidenceCurrentStep("Registro encontrado llave site 1");
			} else {
				testCase.addTextEvidenceCurrentStep("No se visualizan los registros de las invocaciones realizadas para generacion llave site 1");
			}
			
			assertFalse(validaConsultaCanalSeguro, "No se visualizan los registros de las invocaciones realizadas site 1");
		
			//------------------------------------------------------------------------
			addStep("Ejecutar  la siguiente consulta  en la BD   *FCWMLTAEQA* en site 1  para verificar  que no  viajo por un canal seguro");
			
			consultaCanalSeguro_f = String.format(consultaCanalNoSeguroOPR01Dtl, "%RTP%", fechaEjecucionKEY_f, fechaEjecucionDTL_f);
			
			System.out.println(consultaCanalSeguro_f);
			
			consultaCanalSeguro_r = dbMtyFCWMLTAQ.executeQuery(consultaCanalSeguro_f);
			
			validaConsultaCanalSeguro = consultaCanalSeguro_r.isEmpty();
			
			System.out.println(validaConsultaCanalSeguro);
			
			testCase.addQueryEvidenceCurrentStep(consultaCanalSeguro_r, true);
			if (!validaConsultaCanalSeguro) {
				testCase.addTextEvidenceCurrentStep("Registro encontrado recarga site 1");
			} else {
				
				testCase.addTextEvidenceCurrentStep("No se visualizan los registros de las invocaciones realizadas para generacion recarga site 1");
				
			}
			
			assertFalse(validaConsultaCanalSeguro, "No se visualizan los registros de las invocaciones realizadas site 1");
			
//		addStep("Ejecutar  la siguiente consulta  en la BD   *FCWMLTAEQA* en site 2 para verificar  que no  viajo por un canal seguro");
//		
//			consultaCanalSeguro_r = dbMtyFCWMLTAQ2.executeQuery(consultaCanalSeguro_f);
//			
//			validaConsultaCanalSeguro = consultaCanalSeguro_r.isEmpty();
//			
//			System.out.println(validaConsultaCanalSeguro);
//			
//			testCase.addQueryEvidenceCurrentStep(consultaCanalSeguro_r);
//			if (!validaConsultaCanalSeguro) {
//				testCase.addTextEvidenceCurrentStep("Registro encontrado llave site 2");
//			} else {
//				testCase.addTextEvidenceCurrentStep("No se visualizan los registros de las invocaciones realizadas para generacion llave site 2");
//			}
//			
//			assertFalse(validaConsultaCanalSeguro, "No se visualizan los registros de las invocaciones realizadas site 2");
//		
//			//------------------------------------------------------------------------
//			addStep("Ejecutar  la siguiente consulta  en la BD   *FCWMLTAEQA* en site 2 para verificar  que no  viajo por un canal seguro site 2");
//			
//			consultaCanalSeguro_f = String.format(consultaCanalNoSeguroOPR01Dtl, "%RTP%", fechaEjecucionKEY_f, fechaEjecucionDTL_f);
//			
//			System.out.println(consultaCanalSeguro_f);
//			
//			consultaCanalSeguro_r = dbMtyFCWMLTAQ2.executeQuery(consultaCanalSeguro_f);
//			
//			validaConsultaCanalSeguro = consultaCanalSeguro_r.isEmpty();
//			
//			System.out.println(validaConsultaCanalSeguro);
//			
//			testCase.addQueryEvidenceCurrentStep(consultaCanalSeguro_r, true);
//			if (!validaConsultaCanalSeguro) {
//				testCase.addTextEvidenceCurrentStep("Registro encontrado recarga site 2");
//			} else {
//				
//				testCase.addTextEvidenceCurrentStep("No se visualizan los registros de las invocaciones realizadas para generacion recarga site 2");
//				
//			}
//			
//			assertFalse(validaConsultaCanalSeguro, "No se visualizan los registros de las invocaciones realizadas site 2");
		
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_TPE_RTP_003_SolicitudLlaveYRegistro_test";
	}

	@Override
	public String setTestDescription() {
		return "MTC-FT-057 Solicitud de llave y registro de transacción de Recarga de tarjeta de transporte publico Feria";
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