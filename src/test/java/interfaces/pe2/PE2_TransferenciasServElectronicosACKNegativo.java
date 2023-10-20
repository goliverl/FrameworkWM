package interfaces.pe2;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PE2;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

public class PE2_TransferenciasServElectronicosACKNegativo extends BaseExecution {
	
	/*
	 * 
		@cp1 Validar Switch_Transaccion Reversa de pago con tarjeta de credito por ACK Negativo
		@cp2 Validar Switch_Transaccion Reversa de pago con tarjeta de debito por ACK Negativo
		@cp3 Validar Switch_Transaccion Reversa de retiro de dinero por ACK Negativo
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PE2_Transferencias_Serv_Electronicos_AKC_Negativo(HashMap<String, String> data) throws Exception {
		/*
		 * Utilerias
		 ************************************************************************/

		utils.sql.SQLUtil dbFCTDC = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA,
				GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		utils.sql.SQLUtil dbFCWMLTAEQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA,
				GlobalVariables.DB_USER_FCWMLTAEQA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA);
		//utils.sql.SQLUtil dbFCSWQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA,
				//GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		utils.sql.SQLUtil dbFCSWQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO,
				GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		//BD NUEVA
		SQLUtil dbFCSWQA = new SQLUtil(GlobalVariables.DB_HOST_FCSWPRD_CRECI, GlobalVariables.DB_USER_FCSWPRD_CRECI, GlobalVariables.DB_PASSWORD_FCSWPRD_CRECI);


		PE2 pe2Util = new PE2(data, testCase, dbFCTDC);
		
		testCase.setTest_Description(data.get("Description"));

		/*
		 * Variables
		 ********************************************************************************/

		String wmCodeToValidateFolio =  data.get("wmCodeToValidateFolio");
		String wmCodeToValidateAuth = data.get("wmCodeToValidateAuth");
		String wmCodeToValidateAck = data.get("codigoACK");
		
		// TDC transaction --
				String Query= "SELECT Creation_date,Folio,Wm_code,Track1,Card_Type,Bin,Issuer "
						+ " FROM tpeuser.tdc_transaction "
						+ " WHERE folio = '%s' "
						+ " and CREATION_DATE >= TRUNC(SYSDATE)" ;
				
				String Query2= "SELECT Bank,Sw_auth_code,Reversed,Amount,Plaza,Tienda,Switch,IS_Name,Site"
						+ " FROM tpeuser.tdc_transaction "
						+ " WHERE folio = '%s' "
						+ " and CREATION_DATE >= TRUNC(SYSDATE)" ;
				
				String QueryG= "SELECT Creation_date,Folio,Wm_code,Track1,Card_Type,Bin,Issuer,Bank,Sw_auth_code,Reversed,Amount,Plaza,Tienda,Switch,IS_Name,Site "
						+ " FROM tpeuser.tdc_transaction "
						+ " WHERE folio = '%s' "
						+ " and CREATION_DATE >= TRUNC(SYSDATE)" ;
		
		String tdcLogErrorQuery = "SELECT ERROR_ID, FOLIO, ERROR_DATE, ERROR_CODE, DESCRIPTION  "
				+ " FROM WMLOG.WM_LOG_ERROR_TPE" + 
				" WHERE TPE_TYPE='PE2' " + 
				" AND FOLIO='%s'" + 
				" AND ERROR_DATE = TRUNC(SYSDATE)" + 
				" ORDER BY ERROR_DATE DESC";

		String tdcTPE = "SELECT CREATION_DATE, FOLIO, MTI, AMOUNT, PAN, AUTH_ID_RES "
				+ " FROM SWUSER.TPE_SW_TLOG "
				+ " WHERE FOLIO='%s' "
				+ " AND CREATION_DATE>=TRUNC(SYSDATE)";
		
		String tdcTPE2 = "SELECT SW_CODE, COUNTER, IS_NAME, APPLICATION, ENTITY  "
				+ " FROM SWUSER.TPE_SW_TLOG "
				+ " WHERE FOLIO='%s' "
				+ " AND CREATION_DATE>=TRUNC(SYSDATE)";
		
		String tdcTPE3 = "SELECT PLAZA, TIENDA, CAJA, ACQUIRER, RESP_CODE, POS_ENTRY_MODE, PROC_CODE"
				+ " FROM SWUSER.TPE_SW_TLOG "
				+ " WHERE FOLIO='%s' "
				+ " AND CREATION_DATE>=TRUNC(SYSDATE)";
		
		String tdcTPEG = "SELECT CREATION_DATE, FOLIO, MTI, AMOUNT, PAN, AUTH_ID_RES, SW_CODE, COUNTER, IS_NAME, APPLICATION, ENTITY, PLAZA, TIENDA, CAJA, ACQUIRER, RESP_CODE, POS_ENTRY_MODE, PROC_CODE"
				+ " FROM SWUSER.TPE_SW_TLOG "
				+ " WHERE FOLIO='%s' "
				+ " AND CREATION_DATE>=TRUNC(SYSDATE)";
		
		

		String folio;
		String creationDate;
		String wmCodeRequest;
		String wmCodeDb;

		/*
		 * Pasos
		 *******************************************************************************************************/
		String tarjeta= data.get("tarjeta");
		String promType= data.get("promType");
		
		
		if(promType.equals("CAB")) { 
			
			testCase.setPrerequisites("Contar con acceso a la BD FCTDCQA_MTY.                                                                                               "
					+ "Contar con acceso a la BD FCWMLTAEQA_MTY.                                                                                         "
					+ "Contar con acceso a la BD FCSWPRD.                                                                                                     "
					+ "Contar con una tarjeta de debito y que pueda realizar retiros de dinero.                                                          "
					+ "Contar con la correcta configuracion del BIN de la tarjeta de debito en las tablas TDC_BIN campo CASHBACK='Y' y TDC_ROUTING campo PROC_CODE='CAB' y ENTITY='BBVA' de la BD FCTDCQA_MTY.                                                                                                                                    Contar con un simulador bancario para que responda las solicitudes de autorizacion.                                "
					+ "Contar con la ejecucion automatica del job PE2.Pub:runReverseManager.sh");
		} else {
		
		
		testCase.setPrerequisites("Contar con acceso a la BD FCTDCQA_MTY.                                                                                                                              "
				+ "Contar con acceso a la BD FCWMLTAEQA_MTY.                                                                                       "
				+ "Contar con acceso a la BD FCSWPRD.                                                                                               "
				+ "Contar con una tarjeta de "+ tarjeta + " que pueda realizar compras.                                                        "
				+ "Contar con la correcta configuracion del BIN de la tarjeta de "+tarjeta+" en las tablas TDC_BIN y TDC_ROUTING de la BD FCTDCQA_MTY.                                                                                                "
				+ "Contar con un simulador bancario para que responda las solicitudes de autorizacion.                            "
				+ "Contar con la ejecucion automatica del job PE2.Pub:runReverseManager.sh");     }
		
		// Paso 1
		// ------------------------------------------------------------------------------------------------------------------------------------------------------
		
		String ackNegativo="01";
		testCase.setFullTestName(data.get("casoDePrueba"));
		testCase.setProject_Name("Crecimiento Servicios Electronicos (SF)");
		testCase.setTest_Description(data.get("Description"));
		addStep("Solicitar folio desde un navegador, invocando el servicio runGetFolio:"); //16, 17, 19
		// Ejecuta el servicio PE2.Pub:runGetFolio
		String responseRunGetFolio = pe2Util.ejecutarRunGetFolio();// obtener el folio del
		System.out.println(responseRunGetFolio);

		// Se obtienen los datos folio, wm_code y creation_date del xml de respuesta y
		// se agregan a la evidencia
		folio = getSimpleDataXml(responseRunGetFolio, "folio");
		wmCodeRequest = getSimpleDataXml(responseRunGetFolio, "wmCode");
		creationDate = getSimpleDataXml(responseRunGetFolio, "creationDate");

		// Se valida que el wm_code del xml de respuesta sea igual a 100
		boolean validationRequest = wmCodeRequest.equals(wmCodeToValidateFolio);
		System.out.println(validationRequest + " - wmCode request: " + wmCodeRequest);

		assertTrue(validationRequest, "El codigo wmCode no es el esperado, se esperaba "+ wmCodeToValidateFolio);

		// Paso 2--------------------------------------------------------------------------------------------------------------------------------------------------------------
		String tipo = data.get("Name");
		
		if(promType.equals("CAB")) { 
			addStep("Solicitar autorizacion de reversa de retiro de dinero desde un navegador, invocando el servicio runGetAuth:");

		}else {
			addStep("Solicitar autorizacion de "+ tipo +" desde un navegador, invocando el servicio runGetAuth:");

		}
	
		// Ejecuta el servicio PE2.Pub:runGetAuth
		String responseRunGetAuth = pe2Util.ejecutarRunGetAuth(folio, creationDate, 0);// retorna el response
		System.out.println(responseRunGetAuth);

		// Se obtienen los datos folio, wm_code y creation_date del xml de respuesta y
		// se agregan a la evidencia
		wmCodeRequest = getSimpleDataXml(responseRunGetAuth, "wmCode");

		// Se valida que el wm_code del xml de respuesta sea igual al esperado
		validationRequest = wmCodeRequest.equals(wmCodeToValidateAuth);
		System.out.println(validationRequest + " - wmCode request: " + wmCodeRequest);

		assertTrue(validationRequest, "El codigo wmCode no es el esperado, se esperaba "+wmCodeToValidateAuth);

		// Paso 3--------------------------------------------------------------------------------------------------------------------------------------------------------------
		
		if(data.get("ack").equals(ackNegativo)) {
			if(promType.equals("CAB")) { 
				addStep("Confirmar de forma negativa la reversa de retiro de dinero desde un navegador (con ack=01), invocando el servicio runGetAuthAck:");

			}else {
			addStep("Confirmar de forma negativa la autorizacion del "+tipo+" desde un navegador (con ack=01), invocando el servicio runGetAuthAck:");
			}
		}else {
		addStep("Solicitar autorizacion, invocando el servicio runGetAck");}
		// Ejecuta el servicio PE2.Pub:runGetAck
		String responseRunGetAck = pe2Util.ejecutarRunGetAck(folio,creationDate);// retorna el response
		System.out.println(responseRunGetAck);

		// Se obtienen los datos folio, wm_code y creation_date del xml de respuesta y
		// se agregan a la evidencia
		wmCodeRequest = getSimpleDataXml(responseRunGetAck, "wmCode");
		// Se valida que el wm_code del xml de respuesta sea igual al esperado
		validationRequest = wmCodeRequest.equals(wmCodeToValidateAck);
		System.out.println(validationRequest + " - wmCode request: " + wmCodeRequest);

		assertTrue(validationRequest, "El codigo wmCode no es el esperado, se esperaba "+wmCodeToValidateAck);
		
        //Paso 4
		//****************************************************Validar conexion FCTDCQA *************************************************************************

		addStep("Conectarse a la BD FCTDCQA_MTY.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTDCQA.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexion con exito con la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCTDCQA);

		// Paso 5 ------------------------------------------------------------------------------------------------------------------------------------------------------------
		addStep("Ejecutar la siguiente consulta para validar que la transaccion se registro correctamente en la tabla TDC_TRANSACTION");
		
	    //Consulta a BD, se forma el query a utilizar
	    String Tempquery = String.format(Query, folio);
	    String Tempquery2 = String.format(Query2, folio);
	    String TempqueryG = String.format(QueryG, folio);
			
	    //Se realiza la consulta a la BD y se obtiene el wm_code
	    SQLResult result1 = executeQuery(dbFCTDC, Tempquery);
	    SQLResult result1P2 = executeQuery(dbFCTDC, Tempquery2);
	    
	    
		wmCodeDb = result1.getData(0, "WM_CODE");
		System.out.print(result1);
		System.out.println("Code DB: " + wmCodeDb);

		// Se valida que sea igual al esperado
		boolean validationDb = wmCodeDb.equals(wmCodeToValidateAck);
		System.out.println(validationDb + " - wmCode db: " + wmCodeDb);
		
		testCase.addTextEvidenceCurrentStep(TempqueryG);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transaccion con los siguientes valores:");
		testCase.addQueryEvidenceCurrentStep(result1,false);
		testCase.addQueryEvidenceCurrentStep(result1P2,false);

		assertTrue(validationDb, "El codigo wmCode no es el esperado");

		
		
		//Paso 6
		// ****************************************************Validar conexion FCSWPRD*************************************************************************

		addStep("Conectarse a la BD FCSWPRD.");
		 testCase.addTextEvidenceCurrentStep("Base de Datos: FCSWPRD");
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("Se establece la conexion con exito con la BD.");
		    testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCSWQA);
		

		// Paso 7
		// ------------------------------------------------------------------------------------------------------------------------------------------------------------
		    Thread.sleep(6000);
		addStep("Ejecutar la siguiente consulta para validar que la transaccion se registro correctamente en la tabla TPE_SW_TLOG:");
		
		String tempTPEQuery = String.format(tdcTPE, folio);
		String tempTPEQuery2 = String.format(tdcTPE2, folio);
		String tempTPEQuery3 = String.format(tdcTPE3, folio);
		String tempTPEQueryG = String.format(tdcTPEG, folio);
		
		SQLResult result3 = executeQuery(dbFCSWQA, tempTPEQuery);
		SQLResult result3P2 = executeQuery(dbFCSWQA, tempTPEQuery2);
		SQLResult result3P3 = executeQuery(dbFCSWQA, tempTPEQuery3);
		
		
		boolean validationTPE = result3.isEmpty();

		if (!validationTPE) {
			testCase.addTextEvidenceCurrentStep(tempTPEQueryG);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	
				testCase.addBoldTextEvidenceCurrentStep("Para las transacciones de reversa se generan 2 registros, en los cuales el campo MTI tendra los siguientes valores:");
				testCase.addBoldTextEvidenceCurrentStep("MTI=0200  Para la solicitud.");
				testCase.addBoldTextEvidenceCurrentStep("MTI=0420  Para la solicitud.");
				testCase.addBoldTextEvidenceCurrentStep("Del mismo modo se muestran el resto de los campos con los siguientes valores para cada uno de los registros:");
			testCase.addQueryEvidenceCurrentStep(result3, false);
			testCase.addQueryEvidenceCurrentStep(result3P2, false);
			testCase.addQueryEvidenceCurrentStep(result3P3, false);
			testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWPRD.");
			
		} else {

			SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, tempTPEQuery);
			SQLResult resultQROP2 = executeQuery(dbFCSWQA_QRO, tempTPEQuery2);
			SQLResult resultQROP3 = executeQuery(dbFCSWQA_QRO, tempTPEQuery3);

			validationTPE = resultQRO.isEmpty();
			
			
			if (!validationTPE) {
				testCase.addTextEvidenceCurrentStep(tempTPEQueryG);
				testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
				testCase.addBoldTextEvidenceCurrentStep("Para las transacciones de reversa se generan 2 registros, en los cuales el campo MTI tendra los siguientes valores:");
					testCase.addBoldTextEvidenceCurrentStep("MTI=0200  Para la solicitud.");
					testCase.addBoldTextEvidenceCurrentStep("MTI=0420  Para la solicitud.");
					testCase.addBoldTextEvidenceCurrentStep("Del mismo modo se muestran el resto de los campos con los siguientes valores para cada uno de los registros:");
				testCase.addQueryEvidenceCurrentStep(resultQRO,false);
				testCase.addQueryEvidenceCurrentStep(resultQROP2,false);
				testCase.addQueryEvidenceCurrentStep(resultQROP3,false);
				testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWQA_QUERETARO_S2.");
			}
		}

		assertTrue(!validationTPE, "No se registro la transaccion");
		
		//Paso 8
		//****************************************************Validar conexion FCWMLTAEQA *************************************************************************
				addStep("Conectarse a la BD FCWMLTAEQA_MTY.");
				 testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAQ.FEMCOM.NET");
					testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
					testCase.addBoldTextEvidenceCurrentStep("Se establece la conexion con exito con la BD.");
				    testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCWMLTAEQA);

				
		
		
		// Paso 9
				// ------------------------------------------------------------------------------------------------------------------------------------------------------------
					if(promType.equals("CAB")) { 
						addStep("Ejecutar la siguiente consulta para validar que no se registro error de la transaccion:");

					}else {
						addStep("Ejecutar la siguiente consulta para validar que la transaccion no registro errores:");

					}
						
				String tempErrorQuery = String.format(tdcLogErrorQuery, folio);
				SQLResult result2 = executeQuery(dbFCWMLTAEQA, tempErrorQuery);
				System.out.println(result2);
				boolean validationError = result2.isEmpty();

				if (validationError) {
					testCase.addTextEvidenceCurrentStep(tempErrorQuery);
					testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
					if(promType.equals("CAB")) { 
						testCase.addBoldTextEvidenceCurrentStep("No se muestran errores relacionados con la transaccion de reversa de retiro de dinero con ACK negativo.");
					}else {
						testCase.addBoldTextEvidenceCurrentStep("No se muestran errores relacionados a la reversa de la transaccion con ACK negativo.");

					}

					
					
					
					testCase.addQueryEvidenceCurrentStep(result2,false);
				}

	}
	

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. Realizar un pago o retiro de forma no exitosa. Reversa de pago por ACK negativo."
				+ " Revisar que la transaccion se registre correctamente.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		
		return "ATC_FT_003_PE2_Transferencias_Serv_Electronicos_AKC_Negativo";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;
	}

}
