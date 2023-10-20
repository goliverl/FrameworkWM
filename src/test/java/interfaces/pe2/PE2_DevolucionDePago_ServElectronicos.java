package interfaces.pe2;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PE2;
import util.GlobalVariables;
import utils.sql.SQLResult;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

public class PE2_DevolucionDePago_ServElectronicos extends BaseExecution {
	
	/*
	 * 
		@cp1 Validar Switch_Transaccion exitosa de devolucion de pago con tarjeta de debito
		@cp2 Validar Switch_Transaccion exitosa de devolucion de pago con tarjeta de credito
		@cp3 Validar Switch_Transaccion Rechazada de devolucion de pago con tarjeta de debito
		@cp4 Validar Switch_Transaccion Rechazada de devolucion de pago con tarjeta de credito
		@cp5 Validar Switch_Transaccion Reversa de devolucion de pago con tarjeta de debito por ACK Negativo
		@cp6 Validar Switch_Transaccion Reversa de devolucion de pago con tarjeta de credito por ACK Negativo
	 * 
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PE2_Devolucion_De_Pago_Servicios_Electronicos(HashMap<String, String> data) throws Exception {
		/* Utilerias ************************************************************************/
		utils.sql.SQLUtil BDFCTDCQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA,GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		utils.sql.SQLUtil BDFCWMLTAEQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA,GlobalVariables.DB_USER_FCWMLTAEQA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA);
		//utils.sql.SQLUtil BDFCSWQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA,GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		utils.sql.SQLUtil dbFCSWQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO,GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		utils.sql.SQLUtil dbFCWMLTAEQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO,GlobalVariables.DB_USER_FCWMLTAEQA_QRO, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO);
		//BD NUEVA
		utils.sql.SQLUtil BDFCSWQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWPRD_CRECI, GlobalVariables.DB_USER_FCSWPRD_CRECI, GlobalVariables.DB_PASSWORD_FCSWPRD_CRECI);
		PE2 pe2Util = new PE2(data, testCase, BDFCTDCQA);
		
		testCase.setTest_Description(data.get("Description"));
		
		/**
		 * Variables ******************************************************************************************
		*/
		
				
				// reverse ---
				String tdcTDC_REVERSE = "SELECT Creation_date,Folio,Plaza,Tienda,Caja,Wm_code,Card_Type "
						+ "FROM TPEUSER.TDC_REVERSE "
						+ "WHERE FOLIO= '%s' "
						+ "AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";
				
				String tdcTDC_REVERSE2 = "SELECT Amount,Switch,Site,Prom_Type,Status,Sw_reverse_code "
						+ " FROM TPEUSER.TDC_REVERSE WHERE FOLIO= '%s' "
						+ " AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";
				 
				String tdcTDC_REVERSEG = "SELECT Folio,Creation_date,Wm_code,Card_No,Card_Type,Amount,Switch,Prom_Type,Site,Sw_reverse_code,Status,Plaza,Tienda,Caja"
						+ " FROM TPEUSER.TDC_REVERSE "
						+ " WHERE FOLIO='%s'"
						+ " AND CREATION_DATE >= TRUNC(SYSDATE)";
				
		//String tdcTDC_REVERSE = "SELECT folio, PLAZA, TIENDA, CREATION_DATE FROM TPEUSER.TDC_REVERSE WHERE FOLIO= '%s' AND CREATION_DATE= TRUNC(SYSDATE)";
		
				String tdcDevolucionQuery = "SELECT Creation_date, Folio, Wm_code, Track1, Card_Type, Bin,o_folio FROM TPEUSER.TDC_DEVOLUTION WHERE folio = '%s'";
				String tdcDevolucionQuery2 = "SELECT Amount, Plaza, Tienda, Switch, is_name, site, sw_dev_code, Issuer, Bank FROM TPEUSER.TDC_DEVOLUTION WHERE folio = '%s'";

		String tdcDevolucionQueryG = "SELECT Creation_date, Folio, Wm_code, Track1, Card_Type, Bin, Issuer, Bank, Amount, Plaza, Tienda, Switch, is_name, site, sw_dev_code,o_folio FROM TPEUSER.TDC_DEVOLUTION WHERE folio = '%s'";
		
					
		String tdcTPE_SW_TLOGquery = "SELECT CREATION_DATE, FOLIO, MTI, AMOUNT, PAN, AUTH_ID_RES "
				+ "FROM SWUSER.TPE_SW_TLOG "
				+ "WHERE FOLIO= '%s' "
				+ "AND CREATION_DATE>= TRUNC(SYSDATE)";
		
		String tdcTPE_SW_TLOGquery2 = "SELECT SW_CODE, COUNTER, IS_NAME, APPLICATION, ENTITY  "
				+ " FROM SWUSER.TPE_SW_TLOG "
				+ " WHERE FOLIO='%s' "
				+ " AND CREATION_DATE>=TRUNC(SYSDATE)";
		
		String tdcTPE_SW_TLOGquery3 = "SELECT PLAZA, TIENDA, CAJA, ACQUIRER, RESP_CODE, POS_ENTRY_MODE, PROC_CODE"
				+ " FROM SWUSER.TPE_SW_TLOG "
				+ " WHERE FOLIO='%s' "
				+ " AND CREATION_DATE>=TRUNC(SYSDATE)";
		
		String tdcTPE_SW_TLOGqueryG = "SELECT CREATION_DATE, FOLIO, MTI, AMOUNT, PAN, AUTH_ID_RES, SW_CODE, COUNTER, IS_NAME, APPLICATION, ENTITY, PLAZA, TIENDA, CAJA, ACQUIRER, RESP_CODE, POS_ENTRY_MODE, PROC_CODE"
				+ " FROM SWUSER.TPE_SW_TLOG "
				+ " WHERE FOLIO='%s' "
				+ " AND CREATION_DATE>=TRUNC(SYSDATE)";
		
		String tdcWM_LOG_ERROR_TPEquery = "SELECT ERROR_ID, folio, ERROR_CODE FROM WMLOG.WM_LOG_ERROR_TPE WHERE TPE_TYPE='PE2' AND FOLIO= '%s' ORDER BY ERROR_DATE DESC";
		

	// Cambiar valores en data provider, Caso devolucion exitosa "codigo = 000" "codigoACK = 101"//	
	// Cambiar valores en data provider, Caso devolucion Fallida "codigo = 131" "codigoACK = 131"//	
	
		String wmCodeToValidateGetDev = data.get("codigo");
		String wmCodeToValidateAck = data.get("codigoACK");
		String wmCodeToValidateFolio = "100";
		String folio;
		String wmCodeRequest;
		String tipo = data.get("Name");
		String wm102 = "102";
		String Ofolio ="";
		String tarjeta = data.get("tarjeta");
		
		   /* Pasos *******************************************************************************************************/
		//testCase.setFullTestName(data.get("casoDePrueba"));
		//testCase.setProject_Name("Crecimiento Servicios Electronicos (SF)");
		//testCase.setTest_Description(data.get("Description"));
		
		if(wmCodeToValidateAck.equals("101")) {
		testCase.setPrerequisites("Contar con acceso a la BD FCTDCQA_MTY.                                                                                                   "
				+ "Contar con acceso a la BD FCWMLTAEQA_MTY.                                                                                            "
				+ "Contar con acceso a la BD FCSWPRD.                                                                                                  "
				+ "Previamente haber realizado un pago con tarjeta de "+tarjeta+" de forma exitosa.                          "
				+ "Contar con un simulador bancario para que responda las solicitudes de autorizacion. ");
		}
		
		if(wmCodeToValidateAck.equals("131")) {
			
			testCase.setPrerequisites("Contar con acceso a la BD FCTDCQA_MTY.                                                                                                     "
					+ "Contar con acceso a la BD FCWMLTAEQA_MTY.                                                                                                  "
					+ "Contar con acceso a la BD FCSWPRD.                                                                                                       "
					+ "Previamente haber realizado un pago con tarjeta de "+tarjeta+" de forma exitosa.                               "
					+ "Contar con un simulador bancario para que responda las solicitudes de autorizacion.        "
					+ "Introducir una fecha de autorizacion incorrecta en el campo adAuthDate al momento de solicitar la devolucion.");
			
		}
		
		if(wmCodeToValidateAck.equals("102")) {
			testCase.setPrerequisites("Contar con acceso a la BD FCTDCQA_MTY.                                                                                                   "
					+ "Contar con acceso a la BD FCWMLTAEQA_MTY.                                                                                            "
					+ "Contar con acceso a la BD FCSWPRD.                                                                                                  "
					+ "Previamente haber realizado un pago con tarjeta de "+tarjeta+" de forma exitosa.                          "
					+ "Contar con un simulador bancario para que responda las solicitudes de autorizacion.                                           "
					+ "Contar con la ejecucion automatica del job PE2.Pub:runReverseManager.sh");
		}
		
		if(wmCodeToValidateAck.equals("111")) {
			
			testCase.setPrerequisites(" Contar con acceso a la BD FCTDCQA_MTY.                                                                                         "
					+ "Contar con acceso a la BD FCWMLTAEQA_MTY.                                                                                                                    \r\n" 
					+ "Contar con acceso a la BD FCSWPRD.                                                                                             "
					+ "Previamente haber realizado un pago con tarjeta de "+tarjeta+" de forma exitosa.                                          "
					+ "Contar con un simulador bancario para que responda las solicitudes de autorizacion.                           "
					+ "Contar con la ejecucion automatica del job PE2.Pub:runReverseManager.sh\r\n" + 
					"");
		}
		
		
	//Paso 1
		addStep("Solicitar folio desde un navegador, invocando el servicio runGetFolio:");
		
	    //Ejecuta el servicio PE2.Pub:runGetFolio
		
		String responseRunGetFolio = pe2Util.ejecutarRunGetFolio();
		System.out.println(responseRunGetFolio); 
		
		//Se obtienen los datos folio, wm_code y creation_date del xml de respuesta y se agregan a la evidencia
    	folio = getSimpleDataXml(responseRunGetFolio, "folio");
    	wmCodeRequest = getSimpleDataXml(responseRunGetFolio, "wmCode");
    	boolean WMCODE100 = wmCodeToValidateFolio.equals(wmCodeRequest);
    	
        assertTrue(WMCODE100,"El wmCode 100 no es Correcto");
		System.out.println(folio); 
		
		
		
		boolean boton = wm102.equals(wmCodeToValidateAck);
    	//Paso 2
		if(boton) {
			addStep("Solicitar autorizacion de devolucion desde un navegador, invocando el servicio runGetDev:");
			}else {
		if(wmCodeToValidateAck.equals("131")) {
			
			addStep("Solicitar autorizacion de devolucion de "+tipo+" desde un navegador, invocando el servicio runGetDev:");

		}else {
			addStep("Solicitar autorizacion de devolucion exitosa de "+tipo+" desde un navegador, invocando el servicio runGetDev:");

		}
			
		}
    	  //Ejecuta el servicio PE2.Pub:runGetDev 
    	String responseRunGetDev = pe2Util.ejecutarRunGetDev(folio);
		System.out.println(responseRunGetDev);
		
		//Se obtienen los datos folio, wm_code
    	wmCodeRequest = getSimpleDataXml(responseRunGetDev, "wmCode");
    	
		System.out.println(wmCodeRequest);
		
    	
    	boolean WMCODE000 = wmCodeToValidateGetDev.equals(wmCodeRequest);
    	
    	assertTrue(WMCODE000,"El wmCode no es Correcto");
    	//Paso 3 *********************************************************************************************************************************************************
    	String wm111="111";
    	boolean pruebasinack = wm111.equals(wmCodeToValidateAck);
    	if(pruebasinack) {
    		
    	}else {
    	
		
    	if(boton) {
			addStep("Confirmar la autorizacion de la devolucion por ACK Negativo desde un navegador, invocando el servicio runGetDevAck:");
		}else {
			
			if(wmCodeToValidateAck.equals("131")) {
		    	addStep("Confirmar la autorizacion de la devolucion desde un navegador, invocando el servicio runGetDevAck:");
		    	}		
			else {	
    	addStep("Confirmar la autorizacion de la devolucion exitosa desde un navegador, invocando el servicio runGetDevAck:");}
		}
    	String responseRunDevAck = pe2Util.ejecutarRunGetDevAck(folio);
		System.out.println(responseRunDevAck); 
	
		//Se obtienen los datos folio, wm_code
    	wmCodeRequest = getSimpleDataXml(responseRunDevAck, "wmCode");
    	
		System.out.println(wmCodeRequest);

    	boolean WMCODE101 = wmCodeToValidateAck.equals(wmCodeRequest);   
    	assertTrue(WMCODE101,"El wmCode no es Correcto");
	}
    	

		//****************************************************Validar conexion FCTDCQA *************************************************************************
		addStep("Conectarse a la BD FCTDCQA_MTY.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTDCQA.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexion con exito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCTDCQA);

		//Paso 5 ******************************************************************************************************************************************
		if(pruebasinack) {
			addStep("Ejecutar la siguiente consulta para validar que la transaccion de devolucion se registro exitosamente en la tabla TDC_DEVOLUTION:");
	        System.out.println("\n"+ tdcDevolucionQuery); 
	       
	        String TDCDevolucion = String.format(tdcDevolucionQuery, folio);
	        String TDCDevolucion2 = String.format(tdcDevolucionQuery2, folio);
	        String TDCDevolucionG= String.format(tdcDevolucionQueryG, folio);


	        SQLResult StatusTDCDevolucion = executeQuery(BDFCTDCQA, TDCDevolucion);
	        Ofolio = StatusTDCDevolucion.getData(0, "O_FOLIO");    
	    	String QuerywmCode = StatusTDCDevolucion.getData(0, "WM_CODE");
	    	
	    	boolean valuesStatus = QuerywmCode.equals(wmCodeToValidateAck);// Valida si se encuentra en code 111
	    	while (!valuesStatus) {
	    	SQLResult resultC = executeQuery(BDFCTDCQA, TDCDevolucion);
	    	QuerywmCode = resultC.getData(0, "WM_CODE");
	    	valuesStatus = QuerywmCode.equals(wmCodeToValidateAck);
	    	Thread.sleep(2);
	    	}
	    	
	        StatusTDCDevolucion = executeQuery(BDFCTDCQA, TDCDevolucion);
	        SQLResult StatusTDCDevolucion2 = executeQuery(BDFCTDCQA, TDCDevolucion2);

	    	QuerywmCode = StatusTDCDevolucion.getData(0, "WM_CODE");

			System.out.println(QuerywmCode);
	    	
	    	boolean WMCODE111 = wmCodeToValidateAck.equals(QuerywmCode);
	    	 testCase.addTextEvidenceCurrentStep(TDCDevolucionG);
	  		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	  		testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transaccion con los siguientes valores:");
	  		testCase.addQueryEvidenceCurrentStep(StatusTDCDevolucion,false);
	  		testCase.addQueryEvidenceCurrentStep(StatusTDCDevolucion2,false);

	     	assertTrue(WMCODE111,"El wmCode 111 no es Correcto");
		}else {
    	addStep("Ejecutar la siguiente consulta para validar que la transaccion de devolucion se registro exitosamente en la tabla TDC_DEVOLUTION:");
        System.out.println("\n"+ tdcDevolucionQuery); 
		
        String TDCDevolucion = String.format(tdcDevolucionQuery, folio);
        //Guardamos el o_folio para cnsultarlo en tpe_log
     
        SQLResult StatusTDCDevolucion = executeQuery(BDFCTDCQA, TDCDevolucion);
        Ofolio = StatusTDCDevolucion.getData(0, "O_FOLIO");
    	String QuerywmCode = StatusTDCDevolucion.getData(0, "WM_CODE");

		System.out.println(QuerywmCode);
    	
    	 boolean WMCODE101 = wmCodeToValidateAck.equals(QuerywmCode);
    	 
    	 testCase.addTextEvidenceCurrentStep(TDCDevolucion);
 		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
 		testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transaccion con los siguientes valores:");
 		testCase.addQueryEvidenceCurrentStep(StatusTDCDevolucion,false);

     	assertTrue(WMCODE101,"El wmCode no es Correcto");}
     	
		//Paso 5 sin ack ******************************************************************************************************************************************
     	
     	if(pruebasinack) {
     	addStep("Ejecutar la siguiente consulta para validar que la transaccion  se registro exitosamente con la reversa en la tabla TDC_REVERSE");

		String TDCReverse = String.format(tdcTDC_REVERSE, folio);
		String TDCReverse2 = String.format(tdcTDC_REVERSE2, folio);
		String TDCReverseG= String.format(tdcTDC_REVERSEG, folio);


		SQLResult StatusTDCReverse = executeQuery(BDFCTDCQA, TDCReverse);
		SQLResult StatusTDCReverse2 = executeQuery(BDFCTDCQA, TDCReverse2);


		String QuerywmCode = StatusTDCReverse.getData(0, "WM_CODE");

		System.out.println(QuerywmCode);

		boolean WMCODE111 = wmCodeToValidateAck.equals(QuerywmCode);
		testCase.addTextEvidenceCurrentStep(TDCReverseG);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transaccion con los siguientes valores:");
		testCase.addQueryEvidenceCurrentStep(StatusTDCReverse, false);
		testCase.addQueryEvidenceCurrentStep(StatusTDCReverse2, false);


		assertTrue(WMCODE111, "El wmCode 111 no es Correcto");
     	}
     	
     	//****************************************************Validar conexion FCSWQA*************************************************************************
	    
     	addStep("Conectarse a la BD FCSWPRD.");
        testCase.addTextEvidenceCurrentStep("Base de Datos: FCSWPRD");
        testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
        testCase.addBoldTextEvidenceCurrentStep("Se establece la conexion con exito a la BD.");
        testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCSWPRD_CRECI);
   
		

		Thread.sleep(6000);
     	// Paso 7
    	addStep("Ejecutar la siguiente consulta para validar que la transaccion se registro correctamente en la tabla TPE_SW_TLOG usando el valor del campo O_FOLIO de la tabla TDC_DEVOLUTION:");
    	if(wmCodeToValidateAck.equals("131")) {
    		testCase.addBoldTextEvidenceCurrentStep("N/A");
    	}else {
    	String tempTPEQuery = String.format(tdcTPE_SW_TLOGquery, Ofolio);
    	String tempTPEQuery2 = String.format(tdcTPE_SW_TLOGquery2, Ofolio);
    	String tempTPEQuery3 = String.format(tdcTPE_SW_TLOGquery3, Ofolio);
    	String tempTPEQueryG = String.format(tdcTPE_SW_TLOGqueryG, Ofolio);
    	
		SQLResult result3 = executeQuery(BDFCSWQA, tempTPEQuery);
		SQLResult result3PT2 = executeQuery(BDFCSWQA, tempTPEQuery2);
		SQLResult result3PT3 = executeQuery(BDFCSWQA, tempTPEQuery3);
		
		System.out.print(result3);
		boolean validationTPE = result3.isEmpty();

		testCase.addTextEvidenceCurrentStep(tempTPEQueryG);
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
    	
		
		if (!validationTPE) {
			if(boton) {
				testCase.addBoldTextEvidenceCurrentStep("Para las transacciones de reversa se generan 2 registros, en los cuales el campo MTI tendra los siguientes valores:");
				testCase.addBoldTextEvidenceCurrentStep("MTI=0200  Para la solicitud.");
				testCase.addBoldTextEvidenceCurrentStep("MTI=0420 para la reversa.");
				testCase.addBoldTextEvidenceCurrentStep("Del mismo modo se muestran el resto de los campos con los siguientes valores para cada uno de los registros:");
		 		testCase.addQueryEvidenceCurrentStep(result3,false);
		 		testCase.addQueryEvidenceCurrentStep(result3PT2,false);
		 		testCase.addQueryEvidenceCurrentStep(result3PT3,false);
				testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWPRD.");
			}else {
	 		testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transaccion con los siguientes valores:");
	 		testCase.addQueryEvidenceCurrentStep(result3,false);
	 		testCase.addQueryEvidenceCurrentStep(result3PT2,false);
	 		testCase.addQueryEvidenceCurrentStep(result3PT3,false);
			testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWPRD.");
			}
		}else {
			
			SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, tempTPEQuery);
			SQLResult resultQRO2 = executeQuery(dbFCSWQA_QRO, tempTPEQuery2);
			SQLResult resultQRO3 = executeQuery(dbFCSWQA_QRO, tempTPEQuery3);
			
			validationTPE = resultQRO.isEmpty();
			
			if(!validationTPE)
			{
				if(boton) {
					testCase.addBoldTextEvidenceCurrentStep("Para las transacciones de reversa se generan 2 registros, en los cuales el campo MTI tendra los siguientes valores:");
					testCase.addBoldTextEvidenceCurrentStep("MTI=0200  Para la solicitud.");
					testCase.addBoldTextEvidenceCurrentStep("MTI=0420 para la reversa.");
					testCase.addBoldTextEvidenceCurrentStep("Del mismo modo se muestran el resto de los campos con los siguientes valores para cada uno de los registros:");
					testCase.addQueryEvidenceCurrentStep(resultQRO,false);
					testCase.addQueryEvidenceCurrentStep(resultQRO2,false);
					testCase.addQueryEvidenceCurrentStep(resultQRO3,false);
					testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWQA_QUERETARO_S2.");
				}else {
				testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transaccion con los siguientes valores:");
		 		testCase.addQueryEvidenceCurrentStep(resultQRO,false);
		 		testCase.addQueryEvidenceCurrentStep(resultQRO2,false);
				testCase.addQueryEvidenceCurrentStep(resultQRO3,false);
			testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWQA_QUERETARO_S2.");
				}
			}
				}
		
		assertTrue(!validationTPE);
		}
		
    	
//***************************************************Validar conexion FCWMLTAEQA *************************************************************************
	    
		addStep("Conectarse a la BD FCWMLTAEQA_MTY.");
		 testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAQ.FEMCOM.NET");
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("Se establece la conexion con exito a la BD.");
		    testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCWMLTAEQA);
		    
		    
		    String wm101 = "101";
		    String TDCError = String.format(tdcWM_LOG_ERROR_TPEquery, folio);
	        SQLResult result2 = executeQuery(BDFCWMLTAEQA, TDCError);
	         
	         boolean validationError = result2.isEmpty();
	         boolean validacionwm101 = wm101.contentEquals(wmCodeToValidateAck);
     	//Paso 9
			if(validacionwm101) {
		    	addStep("Ejecutar la siguiente consulta para validar que no se registro algun error relacionado al folio de la transaccion");

		             if(validationError) {
		     		testCase.addTextEvidenceCurrentStep(TDCError);
		     		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	     			testCase.addBoldTextEvidenceCurrentStep("Se valida que no se muestre ningun registro de error relacionado con la devolucion de "+tipo+".");
		     		testCase.addQueryEvidenceCurrentStep(result2,false);
		     		testCase.addBoldTextEvidenceCurrentStep("Se encontro el registro en la BD de FCWMLTAEQA_MONTERREY_S1.");
		     		}else {
		     			 SQLResult result2Qro = executeQuery(dbFCWMLTAEQA_QRO, TDCError);
				         validationError = result2Qro.isEmpty();
				         if(validationError) {
				        testCase.addTextEvidenceCurrentStep(TDCError);
				     	testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		     			testCase.addBoldTextEvidenceCurrentStep("Se valida que no se muestre ningun registro de error relacionado con la devolucion de "+tipo+"");
				     	testCase.addQueryEvidenceCurrentStep(result2Qro,false);
				     	testCase.addBoldTextEvidenceCurrentStep("Se encontro el registro en la BD de FCWMLTAEQA_QUERETARO_S2.");
				         }
		     		}
		     		assertTrue(validationError);
			}else {
		    addStep("Ejecutar la siguiente consulta para validar que se registro error en la transaccion:");

		    String wm131 = "131";
       	 boolean validacionwm131 = wm131.contentEquals(wmCodeToValidateAck);
         
         if(!validationError) {
        	
     		if(validacionwm131) {
     			testCase.addTextEvidenceCurrentStep(TDCError);
         		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	     		testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de error relacionado con la transaccion de devolucion de "+tipo+".");
         		testCase.addQueryEvidenceCurrentStep(result2,false);
         		testCase.addBoldTextEvidenceCurrentStep("Se encontro el registro en la BD de FCWMLTAEQA_MONTERREY_S1.");
		
     		}else {
     			testCase.addTextEvidenceCurrentStep(TDCError);
         		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
     		testCase.addBoldTextEvidenceCurrentStep("Se muestran errores relacionados a la reversa con ACK negativo.");
     		testCase.addQueryEvidenceCurrentStep(result2,false);
     		testCase.addBoldTextEvidenceCurrentStep("Se encontro el registro en la BD de FCWMLTAEQA_MONTERREY_S1.");
     		}
     		}else {
     			SQLResult result2Qro = executeQuery(dbFCWMLTAEQA_QRO, TDCError);
		         validationError = result2Qro.isEmpty();
		         
		         if(!validationError) {
		        	 
     		if(validacionwm131) {
     			testCase.addTextEvidenceCurrentStep(TDCError);
         		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	     		testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de error relacionado con la transaccion de devolucion de pago con "+tipo+".");
         		testCase.addQueryEvidenceCurrentStep(result2Qro,false);   		
         		testCase.addBoldTextEvidenceCurrentStep("Se encontro el registro en la BD de FCWMLTAEQA_QUERETARO_S2.");
     		}else {
     			testCase.addTextEvidenceCurrentStep(TDCError);
         		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
     		testCase.addBoldTextEvidenceCurrentStep("Se muestran errores relacionados a la reversa con ACK negativo.");
     		testCase.addQueryEvidenceCurrentStep(result2Qro,false);
     		testCase.addBoldTextEvidenceCurrentStep("Se encontro el registro en la BD de FCWMLTAEQA_QUERETARO_S2.");}
		         					}
		        
     		}
     		
         assertTrue(!validationError);
			
			} 
       
		
        
	}
	
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. Realizar devoluciones con tarjeta y revisar que la transaccion se registre"
				+ " correctamente.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {		
		return "ATC_FT_001_PE2_Devolucion_De_Pago_Servicios_Electronicos";
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

