package interfaces.pe6;

import java.lang.Override;
import java.lang.String;
import java.util.HashMap;
import modelo.BaseExecution;
import om.PE6;
import util.GlobalVariables;
import utils.webmethods.ReadRequest;
import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.GlobalVariables.DB_PASSWORD_FCTDCQA;

import org.testng.annotations.Test;
import utils.sql.SQLResult;
import org.w3c.dom.Document;

public class ATC_FT_008_PE6_Transaccion_Caso_Critico_Servicios_Electronicos extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_008_PE6_Transaccion_Caso_Critico_Servicios_Electronicos_test(HashMap<String, String> data) throws Exception {
		
		utils.sql.SQLUtil dbFCTDCQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA,GlobalVariables.DB_USER_FCTDCQA, DB_PASSWORD_FCTDCQA);
		utils.sql.SQLUtil dbFCWMLTAEQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA,GlobalVariables.DB_USER_FCWMLTAEQA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA);
		utils.sql.SQLUtil dbFCSWQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO,GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		
		//BASE DE DATOS ANTIGUA
        utils.sql.SQLUtil dbFCSWQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA,GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		
		//BASE DE DATOS NUEVA PARA PRUEBAS
		//utils.sql.SQLUtil dbFCSWQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWPRD_CRECI, GlobalVariables.DB_USER_FCSWPRD_CRECI, GlobalVariables.DB_PASSWORD_FCSWPRD_CRECI);

		PE6 pe6Util = new PE6(data, testCase, null);

		// consulta para verificar la transaccion registrada
		String transaccionRegistrada_part1 = "select Folio,Plaza, Tienda, Caja, Creation_date from TPEUSER.TDC_TRANSACTION WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCTDCQA.
		String transaccionRegistrada_part2 = "select Wm_code, Track1,Card_Type, Bin,Issuer from TPEUSER.TDC_TRANSACTION WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCTDCQA.
		String transaccionRegistrada_part3 = "select Bank,Sw_auth_code,Amount,Switch from TPEUSER.TDC_TRANSACTION WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCTDCQA.
		String transaccionRegistrada_part4 = "select IS_Name,Site,Prom_Type,Reversed from TPEUSER.TDC_TRANSACTION WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCTDCQA.
		
		String transRegistradaFCSWQA_part1 = "select MTI, Creation_Date,Folio,Amount,PAN,AUTH_ID_RES FROM SWUSER.TPE_SW_TLOG WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCSWQA
		String transRegistradaFCSWQA_part2 = "select RESP_CODE,SW_CODE,COUNTER,IS_NAME,APPLICATION FROM SWUSER.TPE_SW_TLOG WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCSWQA
		String transRegistradaFCSWQA_part3 = "select ACQUIRER,ENTITY,Plaza,Tienda,Caja FROM SWUSER.TPE_SW_TLOG WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCSWQA
		String transRegistradaFCSWQA_part4 = "select POS_ENTRY_MODE,PROC_CODE FROM SWUSER.TPE_SW_TLOG WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCSWQA

		// consulta para verificar que no se registraron errores en la transaccion
		String errorTransaccion = "SELECT ERROR_ID,FOLIO,ERROR_DATE,SEVERITY,ERROR_TYPE,ERROR_CODE FROM WMLOG.WM_LOG_ERROR_TPE WHERE TPE_TYPE='PE6' AND FOLIO='%s' AND ERROR_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY') ORDER BY ERROR_DATE DESC";// FCWMLTAEQA.
		String tipo=data.get("tipoDePrueba");
		//Configuracion de ambiente
	
		// Valores WMcode esperados
		final String expectedWMCodeGetFolio = data.get("WMCodeGetFolio");
		final String expectedWMCodeGetAuth = data.get("WMCodeGetAuth");
		final String expectedWMCodeGetAuthAck = data.get("WMCodeGetAuthAck");
		final String expectedWMCodeGetFolioC = data.get("WMCodeGetFolioC");

		//testCase.setFullTestName(data.get("casoDePrueba"));
		//testCase.setProject_Name("Crecimiento Servicios Electr�nicos (SF)");
		//testCase.setTest_Description(data.get("description"));
		
		String pre = data.get("pre");
		testCase.setPrerequisites("Contar con acceso a la BD FCTDCQA_MTY.                                                                                       "
				+ "Contar con acceso a la BD FCWMLTAEQA_MTY.                                                                                  "
				+ "Contar con acceso a la BD FCSWPRD.                                                                                           "
				+ "Contar con un simulador bancario para que responda las solicitudes de autorizaci�n.                "
				+ "El bin relacionado a la tarjeta de "+pre+" que se va a utilizar deber� tener el valor del campo APPLY_DEP='Y' en la tabla TDC_BIN de la BD FCTDCQA_MTY.                                                                  "
				+ "El bin relacionado a la tarjeta de "+pre+" que se va a utilizar deber� tener el valor PROC_CODE='PAY' en la tabla TDC_ROUTING de la BD FCTDCQA_MTY.");
	
		// Paso 1 ****************************************************************************
		
		addStep("Solicitar un folio desde el navegador, invocando el servicio runGetFolio:");
		// ejecutar request
		String responseRunGetFolio = pe6Util.runGetFolio_request();
		String folio;
		String creationDate;
		// Obtener variables de la respuesta
		Document runGetFolioRequestDoc = ReadRequest.convertStringToXMLDocument(responseRunGetFolio);
		folio = runGetFolioRequestDoc.getElementsByTagName("folio").item(0).getTextContent();
		creationDate = runGetFolioRequestDoc.getElementsByTagName("creationDate").item(0).getTextContent();

		System.out.println("Folio: " + folio);
		System.out.println("creationDate: " + creationDate);
		String wmCodeFolio = runGetFolioRequestDoc.getElementsByTagName("wmCode").item(0).getTextContent();
		boolean validateRequest = wmCodeFolio.equals(expectedWMCodeGetFolio);
		System.out.println("wmCodeFolio: " + wmCodeFolio);
		
		assertTrue(validateRequest, "el wmCodeFolio no es" + responseRunGetFolio);

		/**************************************************************************************************
		 * Solicitud de autoriazaci�n
		 *************************************************************************************************/

		// Paso 2 *****************************************************************************
		
		String tarjeta = data.get("Name");
		addStep("Solicitar autorizaci�n del "+tarjeta+", invocando el servicio runGetAuth:");

		// Ejecutar el request
		String responseRunGetAuth = pe6Util.runGetAuth_request();

		// Obtener variables de la respuesta
		Document runGetAuthRequestDoc = ReadRequest.convertStringToXMLDocument(responseRunGetAuth);
		String wmCodeAuth = runGetAuthRequestDoc.getElementsByTagName("wmCode").item(0).getTextContent();
        boolean validationAuth = wmCodeAuth.equals(expectedWMCodeGetAuth);
        System.out.println("wmCodeAuth: " + wmCodeAuth);
		assertTrue(validationAuth, "el wmCodeAuth no es" + expectedWMCodeGetAuth); 

		// Paso 3 **************************************************************************
		
		addStep("Solicitar confirmaci�n ACK, invocando el servicio runGetAck:");
		// Ejecutar el request
		String responseRunGetAuthAck = pe6Util.runGetAuthAck_request();
		// Obtener variables de la respuesta
		Document runGetAuthAckRequestDoc = ReadRequest.convertStringToXMLDocument(responseRunGetAuthAck);
		String wmCodeAuthAck = runGetAuthAckRequestDoc.getElementsByTagName("wmCode").item(0).getTextContent();
		boolean responseAck = wmCodeAuthAck.equals(expectedWMCodeGetAuthAck);
		System.out.println("wmCodeAuthAck: " + wmCodeAuthAck);
//		testCase.addTextEvidenceCurrentStep(
//				"wmCodeAuthAck esperado: " + expectedWMCodeGetAuthAck + "  wmCodeAuthAck conseguido: " + wmCodeAuthAck);

		assertTrue(responseAck, "el wmCodeAuthAck no es" + expectedWMCodeGetAuthAck);

		// Paso 4 **************************************************************************
		
		addStep("Solicitar caso cr�tico de corresponsal�a, desde un navegador invocando el servicio:");
		// Ejecutar el request
		String responseRunGetFolioCorresponsalia = pe6Util.runGetFolioCorresponsalia_request();
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("El web service responde wm_code=101 a la solicitud de reversa exitosa por caso cr�tico de la siguiente manera:");
		testCase.addTextEvidenceCurrentStep(responseRunGetFolioCorresponsalia);	
		

		// Obtener variables de la respuesta
		Document runGetFolioCorresponsaliaRequestDoc = ReadRequest.convertStringToXMLDocument(responseRunGetFolioCorresponsalia);

		String wmCodeAuthFolioC = runGetFolioCorresponsaliaRequestDoc.getElementsByTagName("wmCode").item(0).getTextContent();

		boolean responseFolioC = wmCodeAuthFolioC.equals(expectedWMCodeGetFolioC);

		System.out.println("wmCodeAuthAck: " + wmCodeAuthFolioC);
		
		assertTrue(responseFolioC, "el wmCodeCrorresponsalia no es" + expectedWMCodeGetFolioC);
		
        // Paso 5 ****************************************************************************************************************************
		
		addStep("Conectarse a la BD FCTDCQA_MTY.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTDCQA.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexi�n con �xito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCTDCQA);
		
        // Paso 6  ****************************************************************************************************************************
		
		addStep("Ejecutar la siguiente consulta para validar que la transacci�n se registr� correctamente en la tabla TDC_TRANSACTION:");

		// Parte 1*****************************************************************************************************************************
		
		System.out.println(GlobalVariables.DB_HOST_FCTDCQA);

		String formatTransaccionRegistrada_part1 = String.format(transaccionRegistrada_part1, folio);
		System.out.println(formatTransaccionRegistrada_part1);

		SQLResult ExecutetransaccionRegistrada_part1 = dbFCTDCQA.executeQuery(formatTransaccionRegistrada_part1);

		boolean ValidaRegistroBoolean_1 = ExecutetransaccionRegistrada_part1.isEmpty();

		System.out.println(ValidaRegistroBoolean_1);

		if (!ValidaRegistroBoolean_1) {
			testCase.addTextEvidenceCurrentStep(formatTransaccionRegistrada_part1);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transacci�n con los siguientes valores:");       
			testCase.addQueryEvidenceCurrentStep(ExecutetransaccionRegistrada_part1,false);

		}
		// Parte2*****************************************************************************************************
		
		String formatTransaccionRegistrada_part2 = String.format(transaccionRegistrada_part2, folio);
		System.out.println(formatTransaccionRegistrada_part2);
		SQLResult ExecutetransaccionRegistrada_part2 = dbFCTDCQA.executeQuery(formatTransaccionRegistrada_part2);

		boolean ValidaRegistroBoolean_2 = ExecutetransaccionRegistrada_part1.isEmpty();
		
		String WmCodeFCTDCQA = ExecutetransaccionRegistrada_part2.getData(0, "WM_CODE");
		System.out.println("WmCodeFCTDCQA= " + WmCodeFCTDCQA);
		System.out.println(ValidaRegistroBoolean_2);

		if (!ValidaRegistroBoolean_2) {

			testCase.addQueryEvidenceCurrentStep(ExecutetransaccionRegistrada_part2,false);

		}
		// Parte 3
		String formatTransaccionRegistrada_part3 = String.format(transaccionRegistrada_part3, folio);
		System.out.println(formatTransaccionRegistrada_part3);

		SQLResult ExecutetransaccionRegistrada_part3 = dbFCTDCQA.executeQuery(formatTransaccionRegistrada_part3);

		boolean ValidaRegistroBoolean_3 = ExecutetransaccionRegistrada_part3.isEmpty();

		System.out.println(ValidaRegistroBoolean_3);

		if (!ValidaRegistroBoolean_3) {

			testCase.addQueryEvidenceCurrentStep(ExecutetransaccionRegistrada_part3,false);

		}
		
	
		// Parte 4************************************************************************************
		String formatTransaccionRegistrada_part4 = String.format(transaccionRegistrada_part4, folio);
		System.out.println(formatTransaccionRegistrada_part4);

		SQLResult ExecutetransaccionRegistrada_part4 = dbFCTDCQA.executeQuery(formatTransaccionRegistrada_part4);

		boolean ValidaRegistroBoolean_4 = ExecutetransaccionRegistrada_part4.isEmpty();

		System.out.println(ValidaRegistroBoolean_4);

		if (!ValidaRegistroBoolean_4) {

			testCase.addQueryEvidenceCurrentStep(ExecutetransaccionRegistrada_part4,false);

		}
		
		assertFalse("No se muestran registros que cumplan con el folio y creation_date en TDC_TRANSACTION.",ValidaRegistroBoolean_4);
		
		// Paso 7 *********************************************************************************************
	// ****************************************************Validar conexion FCSWPRD*************************************************************************
		
		addStep("Conectarse a la BD FCSWPRD.");
        testCase.addTextEvidenceCurrentStep("Base de Datos: FCSWPRD ");
        testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
        testCase.addBoldTextEvidenceCurrentStep("Se establece la conexi�n con �xito a la BD.");
        testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCSWPRD_CRECI);
		    
		  Thread.sleep(6000);  
      // paso 8 **********************************************************************************************************************************************		
		
		  addStep("Ejecutar la siguiente consulta para validar que la transacci�n se registr� correctamente en la tabla TPE_SW_TLOG:");
		System.out.println(GlobalVariables.DB_HOST_FCSWPRD_CRECI);
		
		//parte 1****************************************************************************************************************************************
		
				String formatTransRegistradaFCSWQA_part1 = String.format(transRegistradaFCSWQA_part1, folio);
				System.out.println(formatTransRegistradaFCSWQA_part1);
				
				SQLResult ExecuteTransRegistradaFCSWQA_part1 = executeQuery(dbFCSWQA, formatTransRegistradaFCSWQA_part1);
				
				boolean ValidaTransRegistroBoolean_1 = ExecuteTransRegistradaFCSWQA_part1.isEmpty();

				System.out.println(ValidaTransRegistroBoolean_1);
				
				if (!ValidaTransRegistroBoolean_1) {
					testCase.addTextEvidenceCurrentStep(formatTransRegistradaFCSWQA_part1);
					testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
					testCase.addBoldTextEvidenceCurrentStep("Se muestran 2 registros para la transacci�n:");
					testCase.addBoldTextEvidenceCurrentStep("1 para la transacci�n del caso cr�tico con MTI=0200");
					testCase.addBoldTextEvidenceCurrentStep("1 para la reversa con MTI=0420");
					testCase.addBoldTextEvidenceCurrentStep("Adicionalmente los siguientes campos:");
					testCase.addQueryEvidenceCurrentStep(ExecuteTransRegistradaFCSWQA_part1,false);
				}else {
					
					SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, formatTransRegistradaFCSWQA_part1);
					
					ValidaTransRegistroBoolean_1 = resultQRO.isEmpty();
					
					if(!ValidaTransRegistroBoolean_1)
					{
						testCase.addTextEvidenceCurrentStep(formatTransRegistradaFCSWQA_part1);
						testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
						testCase.addBoldTextEvidenceCurrentStep("Se muestran 2 registros para la transacci�n:");
						testCase.addBoldTextEvidenceCurrentStep("1 para la transacci�n del caso cr�tico con MTI=0200");
						testCase.addBoldTextEvidenceCurrentStep("1 para la reversa con MTI=0420");
						testCase.addBoldTextEvidenceCurrentStep("Adicionalmente los siguientes campos:");
					testCase.addQueryEvidenceCurrentStep(resultQRO,false);
					}
						}
				
					
				// Parte 2*************************************************************************************
				
				String formatTransRegistradaFCSWQA_part2 = String.format(transRegistradaFCSWQA_part2, folio);
				System.out.println(formatTransRegistradaFCSWQA_part2);
				
				SQLResult ExecuteTransRegistradaFCSWQA_part2 = executeQuery(dbFCSWQA, formatTransRegistradaFCSWQA_part2);
				
				boolean ValidaTransRegistroBoolean_2 = ExecuteTransRegistradaFCSWQA_part2.isEmpty();

				System.out.println(ValidaTransRegistroBoolean_2);
				
				if (!ValidaTransRegistroBoolean_2) {
					testCase.addQueryEvidenceCurrentStep(ExecuteTransRegistradaFCSWQA_part2,false);
				}else {
					
					SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, formatTransRegistradaFCSWQA_part2);
					
					ValidaTransRegistroBoolean_2 = resultQRO.isEmpty();
					
					if(!ValidaTransRegistroBoolean_2)
					{
					testCase.addQueryEvidenceCurrentStep(resultQRO,false);
					}
						}
				
				
				//parte 3 *****************************************************************************
				
				String formatTransRegistradaFCSWQA_part3 = String.format(transRegistradaFCSWQA_part3, folio);
				System.out.println(formatTransRegistradaFCSWQA_part3);
				
				SQLResult ExecuteTransRegistradaFCSWQA_part3 = executeQuery(dbFCSWQA, formatTransRegistradaFCSWQA_part3);
				
				boolean ValidaTransRegistroBoolean_3 = ExecuteTransRegistradaFCSWQA_part3.isEmpty();

				System.out.println(ValidaTransRegistroBoolean_3);
				
				if (!ValidaTransRegistroBoolean_3) {
					testCase.addQueryEvidenceCurrentStep(ExecuteTransRegistradaFCSWQA_part3,false);
				}else {
					
					SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, formatTransRegistradaFCSWQA_part3);
					
					ValidaTransRegistroBoolean_3 = resultQRO.isEmpty();
					
					if(!ValidaTransRegistroBoolean_3)
					{
					testCase.addQueryEvidenceCurrentStep(resultQRO,false);
					}
						}
				
				// Parte 4 *****************************************************************************************
				
				String formatTransRegistradaFCSWQA_part4 = String.format(transRegistradaFCSWQA_part4, folio);
				System.out.println(formatTransRegistradaFCSWQA_part4);
				
				SQLResult ExecuteTransRegistradaFCSWQA_part4 = executeQuery(dbFCSWQA, formatTransRegistradaFCSWQA_part4);
				
				boolean ValidaTransRegistroBoolean_4 = ExecuteTransRegistradaFCSWQA_part4.isEmpty();

				System.out.println(ValidaTransRegistroBoolean_4);
				
				if (!ValidaTransRegistroBoolean_4) {
					testCase.addQueryEvidenceCurrentStep(ExecuteTransRegistradaFCSWQA_part4,false);
					testCase.addTextEvidenceCurrentStep("Se encontr� el registro en la BD de FCSWPRD.");
				}else {
					
					SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, formatTransRegistradaFCSWQA_part4);
					
					ValidaTransRegistroBoolean_4 = resultQRO.isEmpty();
					
					if(!ValidaTransRegistroBoolean_4)
					{
					testCase.addQueryEvidenceCurrentStep(resultQRO,false);
					testCase.addTextEvidenceCurrentStep("Se encontr� el registro en la BD de FCSWQA_QUERETARO_S2.");

					}
						}

				System.out.println(ValidaTransRegistroBoolean_4);
		assertTrue(!ValidaTransRegistroBoolean_4, "No se encontr� registro");

		// Paso 9***************************************************************************************************************
		
		//****************************************************Validar conexion FCWMLTAEQA *************************************************************************
				addStep("Conectarse a la BD FCWMLTAEQA_MTY.");
				 testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAQ.FEMCOM.NET");
					testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
					testCase.addBoldTextEvidenceCurrentStep("Se establece la conexi�n con �xito a la BD.");
				    testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCWMLTAEQA);
				    
		// paso 10 **********************************************************************************************************
				    
		addStep("Ejecutar la siguiente consulta para validar que no se registr� error en la transacci�n:");
		System.out.println(GlobalVariables.DB_HOST_FCWMLTAEQA);

		String formatErrorTransaccion = String.format(errorTransaccion, folio);
		System.out.println(formatErrorTransaccion);

		SQLResult ExecuteErrorTransaccion = dbFCWMLTAEQA.executeQuery(formatErrorTransaccion);

		boolean ValidaRegistroErrorBoolean = ExecuteErrorTransaccion.isEmpty();

		System.out.println(ValidaRegistroErrorBoolean);

		if (ValidaRegistroErrorBoolean) {
			testCase.addTextEvidenceCurrentStep(formatErrorTransaccion);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("No se muestra ning�n registro de error relacionado con la transacci�n.");
			testCase.addQueryEvidenceCurrentStep(ExecuteErrorTransaccion,false);

		}
		assertTrue(ValidaRegistroErrorBoolean, "No hay registros de error en la tabla WMLOG.WM_LOG_ERROR_TPE ");
		
	}

	@Override
	public String setTestFullName() {
		return null;
	}
	@Override
	public String setTestDescription() {
		return "Realizar transaccion a tarjeta de forma exitosa para despues aplicar el caso critico.";
	}
	@Override
	public String setTestDesigner() {
		return "QA Automation";
	}
	@Override
	public String setTestInstanceID() {
		return "-1";                               
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