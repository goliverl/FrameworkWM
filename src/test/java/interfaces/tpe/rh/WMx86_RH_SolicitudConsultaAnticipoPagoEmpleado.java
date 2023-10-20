package interfaces.tpe.rh;

import static org.testng.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_RH;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/**
 * Servicios Electrónicos: MTC-FT-061 Solicitud de consulta de anticipo del pago del empleado y pago del anticipo del empleado.
 * Prueba de regresión  para comprobar la no afectación en la funcionalidad principal de transaccionalidad de la interface TPE_RH y TPE_FR al ser migradas de WM9.9 a WM10.5
 * @author Roberto Flores
 * @date   2022/05/24
 * reviewer Gilberto Martinez
 * 2023/02/20
 * 
 * Mtto:
 * @author Jose Onofre
 * @date 02/23/2023
 */
public class WMx86_RH_SolicitudConsultaAnticipoPagoEmpleado extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_TPE_RH_001_SolicitudConsultaAnticipoPagoEmpleado_test(HashMap<String, String> data) throws Exception {

		testCase.setPrerequisites("*Contar con acceso a las base de datos de FCTPEQA_MTY, FCTPEQA_QRO, FCWMLTAEQA_MTY y FCWMLTAEQA_QRO.\r\n" + 
				"*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA.");
		
		/*
		 * Utilerías
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		SQLUtil dbMtyFCTPE = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE,GlobalVariables.DB_PASSWORD_FCTPE);
		SQLUtil dbMtyFCTPE2 = new SQLUtil(GlobalVariables.DB_HOST_TPE_LOT,GlobalVariables.DB_USER_TPE_LOT, GlobalVariables.DB_PASSWORD_TPE_LOT);
		
		SQLUtil dbMtyFCWMLTAQ = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAQ_MTY,GlobalVariables.DB_USER_FCWMLTAQ_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAQ_MTY);
		SQLUtil dbMtyFCWMLTAQ2 = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO,GlobalVariables.DB_USER_FCWMLTAEQA_QA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		
		TPE_RH rhUtil = new TPE_RH(data, testCase, dbMtyFCTPE);

		
		
		/*
		 * Variables
		 *********************************************************************/
		String wmCodeToValidateQRY01 = "101";
		String wmCodeToValidateTRN01 = "100";
		String wmCodeToValidateTRN02 = "000";
		String wmCodeToValidateTRN03 = "101";
		
		Date fechaEjecucionQRY01;
		Date fechaEjecucionTRN03;

		String ConsultaReferencias= "select operation, entity, folio,wm_code, application, creation_date from TPEUSER.TPE_FR_TRANSACTION \r\n"
				+ "where OPERATION = 'QRY01' \r\n"
				+ "and ENTITY = 'PA'\r\n"
				+ "and FOLIO = '%s'\r\n"
				+ "and WM_CODE= '101'\r\n"
				+ "and APPLICATION= 'RH'\r\n"
				+ "and CREATION_DATE >= (trunc(SYSDATE)) \r\n"
				+ "ORDER BY CREATION_DATE DESC ";
		
		String TransaccionPagoServicio = "select operation, entity, folio,wm_code, application, creation_date, ack from TPEUSER.TPE_FR_TRANSACTION \r\n"
				+ "where OPERATION= 'TRN03' \r\n"
				+ "and ENTITY = 'PA'\r\n"
				+ "and FOLIO = '%s'\r\n"
				+ "and WM_CODE= '101'\r\n"
				+ "and APPLICATION= 'RH'\r\n"
				+ "and CREATION_DATE >= (trunc(SYSDATE)) \r\n"
				+ "ORDER BY CREATION_DATE DESC";
		
		String WM_LOG_ERROR_TPE = "SELECT to_char(error_date, 'dd-mm-yyyy hh24:mi:ss') \r\n" + 
				"FROM WMLOG.WM_LOG_ERROR_TPE \r\n" + 
				"WHERE   TPE_TYPE LIKE '%s' \r\n" + 
				"AND ERROR_DATE BETWEEN TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"AND TO_DATE('%s','dd/mm/yyyy hh24:mi:ss')\r\n" + 
				"ORDER BY ERROR_DATE DESC";

		String folioQRY01 ;
		String folioTRN01 ;
		
		String creationDateTRN01;
		
		String noEmpleadoQRY01Response;
		String cveSeguridadQRY01Response;
		
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
		addStep("Ejecutar la siguiente servicio de la interface TPE_RH de consulta para validación del empleado en pago anticipado en fcwmqa8");
		
		System.out.println("Consulta QRY01");

		//Se solicita el request del QRY01
		fechaEjecucionQRY01 = new Date();
		 respuestaQRY01 = rhUtil.ejecutarQRY01();

		System.out.println("Respuesta QRY01 \r\n: " + respuestaQRY01);		

		folioQRY01 = RequestUtil.getFolioXml(respuestaQRY01);
		wmCodeRequestQRY01 = RequestUtil.getWmCodeXml(respuestaQRY01);

		noEmpleadoQRY01Response = RequestUtil.getAttributeResponse(respuestaQRY01, "service", "noEmpleado");
		cveSeguridadQRY01Response = RequestUtil.getAttributeResponse(respuestaQRY01, "service", "cveSeguridad");
		
		boolean validaRespuestaQRY01 = true;
		if (respuestaQRY01 != null) {
			validaRespuestaQRY01 = false;
		}
//		assertFalse(validaRespuestaQRY01); 

		/*
		 *
		 *****************************************************************************************/
		//Valida el status esperado del QRY01
		addStep("Verificar que el XML de respuesta tenga como wmCode: " + wmCodeToValidateQRY01 + " y contenga noEmpleado y cveSeguridad en service");

		boolean validationRequestQRY01 = wmCodeRequestQRY01.equals(wmCodeToValidateQRY01);
		
		System.out.println(validationRequestQRY01 + " - wmCode request QRY01 : " + wmCodeRequestQRY01);
		
		//testCase.addTextEvidenceCurrentStep("Respuesta QRY01: \r\n" + respuestaQRY01);
		
		testCase.addTextEvidenceCurrentStep(
				"Código esperado: " + wmCodeToValidateQRY01  + "\r\n" + "Código XML: " + wmCodeRequestQRY01);

//		assertTrue(validationRequestQRY01, "El codigo wmCode no es el esperado: " + wmCodeToValidateQRY01);
		
		System.out.println("noEmpleado esperado: " + data.get("service:noEmpleado").toString() + " - reponse : " + noEmpleadoQRY01Response);
		testCase.addTextEvidenceCurrentStep("noEmpleado esperado: " + data.get("service:noEmpleado").toString()  + "\r\n" + "noEmpleado XML: " + noEmpleadoQRY01Response);
//		assertTrue(noEmpleadoQRY01Response.equals(data.get("service:noEmpleado").toString()), "NoEmpleado no coincide con la respuesta");
		
		System.out.println("cveSeguridad esperado: " + data.get("service:cveSeguridad").toString() + " - reponse : " + cveSeguridadQRY01Response);
		testCase.addTextEvidenceCurrentStep("cveSeguridad esperado: " + data.get("service:cveSeguridad").toString()  + "\r\n" + "cveSeguridad XML: " + cveSeguridadQRY01Response);
//		assertTrue(cveSeguridadQRY01Response.equals(data.get("service:cveSeguridad").toString()), "cveSeguridad no coincide con la respuesta");
		
		 
		// Paso 2 *****************************************************************************************
		addStep(" Ejecutar en el navegador el servicio  de solicitud de folio de la interface TPERH del server fcwmqa8 para el pago anticipado al empleado");

		System.out.println("TRN01");
	
		//Se solicita el request del TRN01
		respuestaTRN01 = rhUtil.ejecutarTRN01();

		System.out.println("Respuesta TRN01 \r\n: " + respuestaTRN01);		

		folioTRN01 = RequestUtil.getFolioXml(respuestaTRN01);
		
		wmCodeRequestTRN01 = RequestUtil.getWmCodeXml(respuestaTRN01);
		creationDateTRN01 = RequestUtil.getCreationDate(respuestaTRN01);
		
		boolean validaRespuestaTRN01 = true;
		if (respuestaTRN01 != null) {
			validaRespuestaTRN01 = false;
		}
		//assertFalse(validaRespuestaTRN01);

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

		//assertTrue(validationRequestTRN01, "El codigo wmCode no es el esperado: " + wmCodeRequestTRN01);
		
		  
		// Paso 3 *****************************************************************************************
		addStep("Ejecutar en el navegador el servicio para solicitud de autorización de la interface TPE_RH del server fcwmqa8 para el pago anticipado al empleado");

		System.out.println("TRN02");
	
		//Se solicita el request del TRN02
		respuestaTRN02 = rhUtil.ejecutarTRN02(folioTRN01, creationDateTRN01); 

		System.out.println("Respuesta TRN02 \r\n: " + respuestaTRN02);		

		//folio = RequestUtil.getFolioXml(respuestaTRN02);
		//folio = "1";
		
		wmCodeRequestTRN02 = RequestUtil.getWmCodeXml(respuestaTRN02);

		boolean validaRespuestaTRN02 = true;
		if (respuestaTRN02 != null) {
			validaRespuestaTRN02 = false;
		}
		//assertFalse(validaRespuestaTRN02);

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
 
	//	assertTrue(validationRequestTRN02, "El codigo wmCode no es el esperado: " + wmCodeRequestTRN02);
		
		
		 
		// Paso 4 *****************************************************************************************
		addStep(" Ejecutar en el navegador el servicio de la interface TPE_RH para confirmación de la transacción de pago anticipado al empleado");

		System.out.println("TRN03");
	
		//Se solicita el request del TRN03
		respuestaTRN03 = rhUtil.ejecutarTRN03(folioTRN01, creationDateTRN01); 
		fechaEjecucionTRN03 = new Date();

		System.out.println("Respuesta TRN03: " + respuestaTRN03);		

		//folio = RequestUtil.getFolioXml(respuestaTRN03);
		
		wmCodeRequestTRN03 = RequestUtil.getWmCodeXml(respuestaTRN03);

		boolean validaRespuestaTRN03 = true;
		if (respuestaTRN03 != null) {
			validaRespuestaTRN03 = false;
		}
		//assertFalse(validaRespuestaTRN03);

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
     
		//assertTrue(validationRequestTRN03, "El codigo wmCode no es el esperado: " + wmCodeRequestTRN03);

	//  Paso 5 *********************************************************************************************************
		
		addStep("Establecer la conexión con la BD * FCTPEQA*  en site 1 y site 2");
		
		boolean conexionFCTPEQA = true;
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCTPEQA site 1");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCTPE);
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCTPEQA site 2");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_TPE_LOT);
		
		//assertTrue(conexionFCTPEQA, "La conexion no fue exitosa");
		 
	 //Paso 6 *******************************************************************************************************
		
		addStep(" Ejecutar la siguiente consulta en la BD * FCTPEQA* en site 1 para validar el registro de transacción");
       
				//Consulta de transaccion
				String ConsultaReferencias_f = String.format(ConsultaReferencias, folioQRY01);
				
				System.out.println("Consulta de registro de transacción site 1: \r\n "+ ConsultaReferencias_f);
			
				SQLResult ConsultaReferencias_r = executeQuery(dbMtyFCTPE, ConsultaReferencias_f);
				
				//Transaccion pago servicio
		        String TransaccionPagoServicio_f = String.format(TransaccionPagoServicio, folioTRN01);
				
				System.out.println("Transacción exitosa de pago anticipado al empleados site 1: \r\n"+ TransaccionPagoServicio_f);
		
				SQLResult TransaccionPagoServicio_r = executeQuery(dbMtyFCTPE, TransaccionPagoServicio_f);
				
			    
				boolean validaConsultaReferencias = ConsultaReferencias_r.isEmpty();
				boolean validaTransaccionPagoServicio = TransaccionPagoServicio_r.isEmpty();
		        boolean querys = false;
		       // String creation_date = "";
		        
		        testCase.addQueryEvidenceCurrentStep(ConsultaReferencias_r, true);
		        
		        if(!validaConsultaReferencias) {
		        	testCase.addTextEvidenceCurrentStep("consulta de validación de empleados encontrada site 1");
		        } else {
		        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta de validación de empleados site 1");
		        }
		        
		        testCase.addQueryEvidenceCurrentStep(TransaccionPagoServicio_r, true);
		        if(!validaTransaccionPagoServicio) {
		        	testCase.addTextEvidenceCurrentStep("Registro en transaccion de pago anticipado al empleados encontrado site 1");
		        } else {
		        	testCase.addTextEvidenceCurrentStep("No se muestra la transaccion de pago anticipado al empleados site 1");
		        }
		        
		        if((!validaConsultaReferencias)&&(!validaTransaccionPagoServicio)) {
		        	querys = true;
		        }
				//assertTrue(querys, "No se insertaron datos en la tabla TPUSER.TPE_FR_TRANSACTION"); 
		       
        addStep(" Ejecutar la siguiente consulta en la BD * FCTPEQA* en site 2 para validar el registro de transacción");
				
        		Thread.sleep(10000);
        
				ConsultaReferencias_r = executeQuery(dbMtyFCTPE2, ConsultaReferencias_f);
				
				//Transaccion pago servicio
		
				TransaccionPagoServicio_r = executeQuery(dbMtyFCTPE2, TransaccionPagoServicio_f);
				
			    
				validaConsultaReferencias = ConsultaReferencias_r.isEmpty();
				validaTransaccionPagoServicio = TransaccionPagoServicio_r.isEmpty();
		        querys = false;
		        
		        testCase.addQueryEvidenceCurrentStep(ConsultaReferencias_r, true);
		        
		        if(!validaConsultaReferencias) {
		        	testCase.addTextEvidenceCurrentStep("consulta de validación de empleados encontrada site 2 ");
		        } else {
		        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta de validación de empleados site 2 ");
		        }
		        
		        testCase.addQueryEvidenceCurrentStep(TransaccionPagoServicio_r, true);
		        if(!validaTransaccionPagoServicio) {
		        	testCase.addTextEvidenceCurrentStep("Registro en transaccion de pago anticipado al empleados encontrado site 2 ");
		        } else {
		        	testCase.addTextEvidenceCurrentStep("No se muestra la transaccion de pago anticipado al empleados site 2 ");
		        }
		        
		        if((!validaConsultaReferencias)&&(!validaTransaccionPagoServicio)) {
		        	querys = true;
		        }
				//assertTrue(querys, "No se insertaron datos en la tabla TPUSER.TPE_FR_TRANSACTION"); 
		
		//Paso 7 ***************************************************************************************************

		addStep("Conectarse a la Base de Datos *FCWMLTAEQA* en site1.");
        
		boolean conexionFCWMLTAQ = true;
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCWMLTAEQA site 1");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLTAQ_MTY);
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCWMLTAEQA site 2");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO);
		
		//assertTrue(conexionFCWMLTAQ, "La conexion no fue exitosa");
	
	
	
	//Paso 8 *****************************************************************************************************************
		addStep("Ejecutar la consulta en la base de datos *FCWMLTAEQA* site 1 buscando que no se encuentren registros de error del pago anticipado al empleado");
		
				String fechaEjecucionQRY01_f = formatter.format(fechaEjecucionQRY01);
				String fechaEjecucionTRN03_f = formatter.format(fechaEjecucionTRN03);
				
				String WM_LOG_ERROR_TPE_F = String.format(WM_LOG_ERROR_TPE, "%RH%", fechaEjecucionQRY01_f, fechaEjecucionTRN03_f);
				
				System.out.println(WM_LOG_ERROR_TPE_F);
				
				SQLResult WM_LOG_ERROR_TPE_R = dbMtyFCWMLTAQ.executeQuery(WM_LOG_ERROR_TPE_F);
				
				boolean valida_WM_LOG_ERROR_TPE = WM_LOG_ERROR_TPE_R.isEmpty();
				
				System.out.println(valida_WM_LOG_ERROR_TPE);
				
				testCase.addQueryEvidenceCurrentStep(WM_LOG_ERROR_TPE_R, true);
				if (!valida_WM_LOG_ERROR_TPE) {
					testCase.addTextEvidenceCurrentStep("Errores encontrados site 1");
				} else {
					testCase.addTextEvidenceCurrentStep("No se presentaron errores site 1");
					
				}
				//assertTrue(valida_WM_LOG_ERROR_TPE, "Se registro un error en WM_LOG_ERROR_TPE site 1");
				
		addStep("Ejecutar la consulta en la base de datos *FCWMLTAEQA* site 2 buscando que no se encuentren registros de error del pago anticipado al empleado");
				
				WM_LOG_ERROR_TPE_R = dbMtyFCWMLTAQ2.executeQuery(WM_LOG_ERROR_TPE_F);
				
				valida_WM_LOG_ERROR_TPE = WM_LOG_ERROR_TPE_R.isEmpty();
				
				System.out.println(valida_WM_LOG_ERROR_TPE);
				
				testCase.addQueryEvidenceCurrentStep(WM_LOG_ERROR_TPE_R, true);
				if (!valida_WM_LOG_ERROR_TPE) {
					testCase.addTextEvidenceCurrentStep("Errores encontrados site 2");
				} else {
					testCase.addTextEvidenceCurrentStep("No se presentaron errores site 2");
					
				}
				assertTrue(valida_WM_LOG_ERROR_TPE, "Se registro un error en WM_LOG_ERROR_TPE site 2");
	
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_TPE_RH_001_SolicitudConsultaAnticipoPagoEmpleado_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. MTC-FT-061 Solicitud de consulta de anticipo del pago del empleado y pago del anticipo del empleado.";
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