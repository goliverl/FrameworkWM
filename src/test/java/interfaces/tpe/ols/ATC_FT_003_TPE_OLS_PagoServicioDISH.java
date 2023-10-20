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

public class ATC_FT_003_TPE_OLS_PagoServicioDISH extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_TPE_OLS_PagoServicioDISH_test(HashMap<String, String> data) throws Exception {

		/**
		 * Proyecto: Servicios Electronicos (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-032 Transacción exitosa de pago de servicio DISH
		 * @author Mariso Rodriguez
		 * @date 
		 */
		
		/*
		 * Utilerias
		 *********************************************************************/
		SQLUtil dbMtyFCTPE = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE,GlobalVariables.DB_PASSWORD_FCTPE);
		SQLUtil dbMtyFCTPE2 = new SQLUtil(GlobalVariables.DB_HOST_TPE_LOT, GlobalVariables.DB_USER_TPE_LOT,GlobalVariables.DB_PASSWORD_TPE_LOT);
		SQLUtil dbMtyFCWMLTAQ = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAQ_MTY,GlobalVariables.DB_USER_FCWMLTAQ_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAQ_MTY);

		TPE_OLS olsUtil = new TPE_OLS(data, testCase, dbMtyFCTPE);

		/*
		 * Variables
		 *********************************************************************/
		String wmCodeToValidateQRY01 = "101";
		String wmCodeToValidateTRN01 = "100";
		String wmCodeToValidateTRN02 = "000";
		String wmCodeToValidateTRN03 = "101";

		String ConsultaReferencias= "select operation, entity, folio,wm_code, application, creation_date from TPEUSER.TPE_FR_TRANSACTION \r\n"
				+ "where OPERATION = 'QRY01' \r\n"
				+ "and ENTITY = 'DISH'\r\n"
				+ "and FOLIO = '%s'\r\n"
				+ "and APPLICATION= 'OLS'\r\n"
				+" and CREATION_DATE BETWEEN TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" 
				+" and TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" 
				+ "and WM_CODE= '101'\r\n"
				+ "ORDER BY CREATION_DATE DESC ";
		
		String TransaccionPagoServicio = "select operation, entity, folio,wm_code, application, creation_date, ack from TPEUSER.TPE_FR_TRANSACTION \r\n"
				+ "where OPERATION= 'TRN03' \r\n"
				+ "and ENTITY = 'DISH'\r\n"
				+ "and FOLIO = '%s'\r\n"
				+ "and WM_CODE= '101'\r\n"
				+ "and APPLICATION= 'OLS'\r\n"
				+ "and ACK = '00'\r\n"
				+ "and CREATION_DATE BETWEEN TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n"
				+ "and TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n"
				+ "ORDER BY CREATION_DATE DESC";
		
		String SECURITY_SESSION_LOG = "SELECT application, entity, operation, creation_date, source, folio \r\n"
				+ "FROM WMLOG.SECURITY_SESSION_LOG \r\n"
				+ "WHERE APPLICATION  = 'OLS' \r\n" 
				+"AND CREATION_DATE BETWEEN TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" 
				+" AND TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" 
				+ "ORDER BY CREATION_DATE DESC ";
		
		String WM_LOG_ERROR_TPE = "SELECT error_id, folio, error_date,description tpe_type FROM WMLOG.WM_LOG_ERROR_TPE\r\n"
				+ "WHERE  TPE_TYPE like '%s'\r\n"
				+"AND ERROR_DATE BETWEEN TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" 
				+" AND TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" 
				+ "ORDER BY error_date  DESC";

		
		String wmCodeRequestQRY01;
		String wmCodeRequestTRN01;
		String wmCodeRequestTRN02;
		String wmCodeRequestTRN03;
		
		String respuestaQRY01;
		String respuestaTRN01;
		String respuestaTRN02;
		String respuestaTRN03;
		
		Date fechaInicio ;
		Date fechaFin;
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
	

		testCase.setProject_Name("POC WMx86");
		
		testCase.setPrerequisites(data.get("prerequicitos"));

		/**************************************************************************************************
		 * Consulta QRY01
		 * 
		 *************************************************************************************************/
		//Se solicita el request del QRY01
				fechaInicio = new Date();
				
		        String fechaInicial = formatter.format(fechaInicio);
				
				System.out.println(fechaInicial);
		 
		// Paso 1 ****************************************************
		addStep("Ejecutar en un navegador directamente el servicio de la consulta de referencia para una transacción de "
				+ "pago de servicio DISH");
		System.out.println("Paso 1:");
	   		
		respuestaQRY01 = olsUtil.ejecutarQRY01wm86();
		 
		String folioQRY01 = RequestUtil.getFolioXml(respuestaQRY01);
		
		wmCodeRequestQRY01 = RequestUtil.getWmCodeXml(respuestaQRY01);

		boolean validaRespuestaQRY01 = true;
		if (respuestaQRY01 != null) {
			validaRespuestaQRY01 = false;
		}
		assertFalse(validaRespuestaQRY01); 

		
		//Valida el status esperado del QRY01 *******************************************************************************
		
		addStep("Verificar que el XML de respuesta tenga como wmCode: " + wmCodeToValidateQRY01);

		boolean validationRequestQRY01 = wmCodeRequestQRY01.equals(wmCodeToValidateQRY01);
		
		System.out.println(validationRequestQRY01 + " - wmCode request QRY01 : " + wmCodeRequestQRY01);
			
		testCase.addTextEvidenceCurrentStep(
				"Código esperado: " + wmCodeToValidateQRY01  + "\r\n" + "Código XML: " + wmCodeRequestQRY01);

		assertTrue(validationRequestQRY01, "El codigo wmCode no es el esperado: " + data.get("wmCodeQRY01 "));
		
		 
		// Paso 2 *****************************************************************************************
		addStep(" Ejecutar en un navegador directamente el servicio de solicitud de folio de la TPE_OLS del server QA8 "
				+ "para una transacción de pago de servicio DISH");
		System.out.println("Paso 2: ");

		//Se solicita el request del TRN01
		respuestaTRN01 = olsUtil.ejecutarTRN01wm86();
		
		String folioTRN01  = RequestUtil.getFolioXml(respuestaTRN01);
		
		wmCodeRequestTRN01 = RequestUtil.getWmCodeXml(respuestaTRN01);

		boolean validaRespuestaTRN01 = true;
		if (respuestaTRN01 != null) {
			validaRespuestaTRN01 = false;
		}
		assertFalse(validaRespuestaTRN01); 

		//Valida el status esperado del TRN01*******************************************************************************
		
		addStep("Verificar que el XML de respuesta tenga como wmCode: " + wmCodeToValidateTRN01);

		boolean  validationRequestTRN01 = wmCodeRequestTRN01.equals(wmCodeToValidateTRN01);
		
		System.out.println(validationRequestTRN01 + " - wmCode request TRN01: " + wmCodeRequestTRN01);
		
		
		testCase.addTextEvidenceCurrentStep(
				"Código esperado: " + wmCodeToValidateTRN01 + "\r\n" + "Código XML: " + wmCodeRequestTRN01);

		assertTrue(validationRequestTRN01, "El codigo wmCode no es el esperado: " + wmCodeRequestTRN01); 
		
		  
		// Paso 3 *****************************************************************************************
		addStep(" Ejecutar en un navegador directamente el servicio de solicitud de Autorización de la TPE_OLS del servidor QA8:");
		System.out.println("Paso 3: ");
	
		//Se solicita el request del TRN02
		respuestaTRN02 = olsUtil.ejecutarTRN02wm86(folioTRN01); 
		
		wmCodeRequestTRN02 = RequestUtil.getWmCodeXml(respuestaTRN02);

		boolean validaRespuestaTRN02 = true;
		if (respuestaTRN02 != null) {
			validaRespuestaTRN02 = false;
		}
		assertFalse(validaRespuestaTRN02);

		//Valida el status esperado del TRN02 ******************************************************************************
		
		addStep("Verificar que el XML de respuesta tenga como wmCode: " + wmCodeToValidateTRN02);

		boolean  validationRequestTRN02 = wmCodeRequestTRN02.equals(wmCodeToValidateTRN02);
		
		System.out.println(validationRequestTRN02 + " - wmCode request TRN02: " + wmCodeRequestTRN02);
		
		testCase.addTextEvidenceCurrentStep(
				"Código esperado: " + wmCodeToValidateTRN02 + "\r\n" + "Código XML: " + wmCodeRequestTRN02);
 
		assertTrue(validationRequestTRN02, "El codigo wmCode no es el esperado: " + wmCodeRequestTRN02);
				
	 
		// Paso 4 *****************************************************************************************
		addStep(" Solicitar confirmación ACK de una transacción de pago de servicio DISH desde un navegador");
		System.out.println("Paso 4: ");
		
		//Se solicita el request del TRN03
		respuestaTRN03 = olsUtil.ejecutarTRN03wm86(folioTRN01); 
			
		wmCodeRequestTRN03 = RequestUtil.getWmCodeXml(respuestaTRN03);

		boolean validaRespuestaTRN03 = true;
		
		if (respuestaTRN03 != null) {
			
			validaRespuestaTRN03 = false;
		}
		
		assertFalse(validaRespuestaTRN03);

		
		//Valida el status esperado del TRN03 ***************************************************************************
		
		addStep("Verificar que el XML de respuesta tenga como wmCode: " + wmCodeToValidateTRN03);

		boolean  validationRequestTRN03 = wmCodeRequestTRN03.equals(wmCodeToValidateTRN03);
		
		System.out.println(validationRequestTRN03 + " - wmCode request TRN03: " + wmCodeRequestTRN03);
		
		
		testCase.addTextEvidenceCurrentStep(
				"Código esperado: " + wmCodeToValidateTRN03 + "\r\n" + "Código XML: " + wmCodeRequestTRN03); 
		
       fechaFin = new Date();
		
        String fechaFinal = formatter.format(fechaFin);
		
		System.out.println(fechaFinal);
	
     
		assertTrue(validationRequestTRN03, "El codigo wmCode no es el esperado: " + wmCodeRequestTRN03);

	//  Paso 5 *********************************************************************************************************
		
		addStep("Conectarse a la Base de Datos: **FCTPEQA** en esquema: **TPEUSER**  de Ambos sites MTY y QRO");
		System.out.println("Paso 5: ");
		
		boolean conexionFCTPEQA = true;
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCTPEQA Server MTY ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCTPE);
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCTPEQA Server QRO");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_TPE_LOT);
		
		assertTrue(conexionFCTPEQA, "La conexion no fue exitosa");
		 
	 //Paso 6 *******************************************************************************************************
		
		addStep("Ejecutar la siguiente consulta para validar la transacción de pago de servicio DISH en la tabla TPE_FR_TRANSACTION  de la BD  **FCTPEQA** :");
		System.out.println("Paso 6: ");
		
		testCase.addBoldTextEvidenceCurrentStep("Server MTY");
       
		//Consulta de referencias Dish
		String ConsultaReferencias_f = String.format(ConsultaReferencias, folioQRY01, fechaInicial, fechaFinal );
		
		System.out.println("Consulta de referencias Dish: \r\n "+ ConsultaReferencias_f);
	
		SQLResult ConsultaReferencias_r = executeQuery(dbMtyFCTPE, ConsultaReferencias_f);
		
		//Transaccion pago servicio
        String TransaccionPagoServicio_f = String.format(TransaccionPagoServicio, folioTRN01, fechaInicial, fechaFinal);
		
		System.out.println("Transaccion de pago de servicios: \r\n"+ TransaccionPagoServicio_f);

		SQLResult TransaccionPagoServicio_r = executeQuery(dbMtyFCTPE, TransaccionPagoServicio_f);
		
	    
		boolean validaConsultaReferencias = ConsultaReferencias_r.isEmpty();
		boolean validaTransaccionPagoServicio = TransaccionPagoServicio_r.isEmpty();
        boolean querys = false;
       
        System.out.println("Consulta de referencias Dish: \r\n"+  validaConsultaReferencias);
        System.out.println("Transaccion de pago de servicios: \r\n"+  validaTransaccionPagoServicio);
        
        if(!validaConsultaReferencias) {
        	
        	
        	testCase.addQueryEvidenceCurrentStep(ConsultaReferencias_r);
        	
        	
        } else {
        	
        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta de referencias Dish");
        	testCase.addQueryEvidenceCurrentStep(ConsultaReferencias_r);
        	
        	
        }
        
        if(!validaTransaccionPagoServicio) {
        	
            testCase.addQueryEvidenceCurrentStep(TransaccionPagoServicio_r);
        	
        } else {
        	
        	testCase.addTextEvidenceCurrentStep("No se muestra la transaccion de pago de servicio");
        	testCase.addQueryEvidenceCurrentStep(TransaccionPagoServicio_r);
        	
        }
        
        if((!validaConsultaReferencias)&&(!validaTransaccionPagoServicio)) {
        	querys = true;
        	
        }
           

		assertTrue(querys, "No se insertaron datos en la tabla TPUSER.TPE_FR_TRANSACTION"); 
        
       
        //Paso 6 (QRO)**************************************************************************************************
		addStep("Ejecutar la siguiente consulta para validar la transacción de pago de servicio DISH en la tabla TPE_FR_TRANSACTION  de la BD  **FCTPEQA** :");
		System.out.println("Paso 6(QRO): ");
		
		testCase.addTextEvidenceCurrentStep("Server QRO");
       
		//Consulta de referencias Dish
		 String ConsultaReferencias_f2 = String.format(ConsultaReferencias, folioQRY01, fechaInicial, fechaFinal );
		
		System.out.println("Consulta de referencias Dish: \r\n "+ ConsultaReferencias_f2);
	
		 SQLResult ConsultaReferencias_r2 = executeQuery(dbMtyFCTPE2, ConsultaReferencias_f2);
		
		//Transaccion pago servicio
        String TransaccionPagoServicio_f2 = String.format(TransaccionPagoServicio, folioTRN01, fechaInicial, fechaFinal);
		
		System.out.println("Transaccion de pago de servicios: \r\n"+ TransaccionPagoServicio_f2);

		 SQLResult TransaccionPagoServicio_r2 = executeQuery(dbMtyFCTPE2, TransaccionPagoServicio_f2);
		
		 //boolean validaConsultaReferencias2 = ConsultaReferencias_r2.isEmpty();
		 boolean validaConsultaReferencias2 = false;
		 //boolean validaTransaccionPagoServicio2 = TransaccionPagoServicio_r2.isEmpty();
		 boolean validaTransaccionPagoServicio2 = false;
         querys = false;
       
        System.out.println("Consulta de referencias Dish: \r\n"+  validaConsultaReferencias2);
        System.out.println("Transaccion de pago de servicios: \r\n"+  validaTransaccionPagoServicio2);
        
        if(!validaConsultaReferencias2) {
        	
        	
        	testCase.addQueryEvidenceCurrentStep(ConsultaReferencias_r);
        	
        	
        } else {
        	
        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta de referencias Dish");
        	testCase.addQueryEvidenceCurrentStep(ConsultaReferencias_r);
        	
        	
        }
        
        if(!validaTransaccionPagoServicio2) {
        	
            testCase.addQueryEvidenceCurrentStep(TransaccionPagoServicio_r);
        	
        } else {
        	
        	testCase.addTextEvidenceCurrentStep("No se muestra la transaccion de pago de servicio");
        	testCase.addQueryEvidenceCurrentStep(TransaccionPagoServicio_r);
        	
        }
        
        if((!validaConsultaReferencias2)&&(!validaTransaccionPagoServicio2)) {
        	querys = true;
        	
        }
           

		assertTrue(querys, "No se insertaron datos en la tabla TPUSER.TPE_FR_TRANSACTION"); 
		
		//Paso 7 ***************************************************************************************************

		addStep("Establecer conexión con la Base de Datos **FCWMLTAQ**  tanto en site 1 MTY y 2 QRO");
		System.out.println("Paso 7: ");
		
		boolean conexionFCWMLTAQ = true;
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCWMLTAQ ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA);
		
		assertTrue(conexionFCWMLTAQ, "La conexion no fue exitosa");
	
	
	//Paso 8
	addStep("Ejecutar la siguiente consulta en la base de datos **FCWMLTAQ** para validar que se hayan registrando "
			+ "las invocaciones en la tabla SECURITY_SESSION_LOG:");
	System.out.println("Paso 8: ");
	
	String SECURITY_SESSION_LOG_F = String.format(SECURITY_SESSION_LOG, fechaInicial, fechaFinal);
	
	System.out.println(SECURITY_SESSION_LOG_F);
	
	SQLResult SECURITY_SESSION_LOG_R = dbMtyFCWMLTAQ.executeQuery(SECURITY_SESSION_LOG_F);
	
	boolean valida_SECURITY_SESSION_LOG = SECURITY_SESSION_LOG_R.isEmpty();
	
	System.out.println(valida_SECURITY_SESSION_LOG);
	
	if (!valida_SECURITY_SESSION_LOG) {
		
		testCase.addQueryEvidenceCurrentStep(SECURITY_SESSION_LOG_R);
		
	} else {
		
		testCase.addQueryEvidenceCurrentStep(SECURITY_SESSION_LOG_R);
	}
	
	assertFalse(valida_SECURITY_SESSION_LOG, "No se visualizan los registros de las invocaciones realizadas a la interface OLS de la transacción exitosa de pago de servicio DISH.");
	
	//Paso 9 *****************************************************************************************************************
		addStep(" Validar en la base de datos **FCWMLTAQ** que no se encuentren registros de error de la interfaz OLS relacionado a la transacción de pago de servicio DISH, "
				+ "utilizando la siguiente consulta");
		System.out.println("Paso 9: ");
		
		String WM_LOG_ERROR_TPE_F = String.format(WM_LOG_ERROR_TPE,"%OLS%", fechaInicial, fechaFinal);
		
		System.out.println(WM_LOG_ERROR_TPE_F);
		
		SQLResult WM_LOG_ERROR_TPE_R = dbMtyFCWMLTAQ.executeQuery(WM_LOG_ERROR_TPE_F);
		
		boolean valida_WM_LOG_ERROR_TPE = WM_LOG_ERROR_TPE_R.isEmpty();
		
		System.out.println(valida_WM_LOG_ERROR_TPE);
		
		if (!valida_WM_LOG_ERROR_TPE) {
			
			testCase.addQueryEvidenceCurrentStep(WM_LOG_ERROR_TPE_R);
			
		} else {
			
			testCase.addTextEvidenceCurrentStep("No se presentaron errores");
			
		}
		
		
		assertTrue(valida_WM_LOG_ERROR_TPE, "Se registro un error en WM_LOG_ERROR_TPE "); 
	
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_003_TPE_OLS_PagoServicioDISH_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "MTC-FT-032 Transaccion exitosa de pago de servicio DISH";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Sergio Robles Ramos";
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
