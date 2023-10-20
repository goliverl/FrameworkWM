package interfaces.pe2;

import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import om.PE2;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;


public class PE2_DevolucionDePagoSinACk_ServElectronicos extends BaseExecution {
	
	/*
	 * 
		@cp1 Validar Switch_Transaccion Reversa de devolución de pago con tarjeta de debito por no enviar ACK
		@cp2 Validar Switch_Transaccion Reversa de devolución de pago con tarjeta de credito por no enviar ACK
	 * 
	 */

	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PE2_Devolucion_De_Pago_Sin_ACK(HashMap<String, String> data) throws Exception {
		/* Utilerias ************************************************************************/
		utils.sql.SQLUtil BDFCTDCQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA,GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		utils.sql.SQLUtil BDFCWMLTAEQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA,GlobalVariables.DB_USER_FCWMLTAEQA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA);
		utils.sql.SQLUtil BDFCSWQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA,GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		utils.sql.SQLUtil dbFCSWQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO,GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		utils.sql.SQLUtil dbFCWMLTAEQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO,GlobalVariables.DB_USER_FCWMLTAEQA_QRO, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO);
		//BD NUEVA
		//		SQLUtil dbFCSWQA = new SQLUtil(GlobalVariables.DB_HOST_FCSWPRD_CRECI, GlobalVariables.DB_USER_FCSWPRD_CRECI, GlobalVariables.DB_PASSWORD_FCSWPRD_CRECI);

		PE2 pe2Util = new PE2(data, testCase, BDFCTDCQA);
		testCase.setTest_Description(data.get("Description"));
		/**
		 * Variables ******************************************************************************************
		*/

		String tdcDevolucionQuery = "SELECT Creation_date, Folio, Wm_code, Track1, Card_Type, Bin, Issuer, o_folio FROM TPEUSER.TDC_DEVOLUTION WHERE folio = '%s'";
			
		String tdcTDC_REVERSE = "SELECT folio, PLAZA, TIENDA, CREATION_DATE FROM TPEUSER.TDC_REVERSE WHERE FOLIO= '%s' AND CREATION_DATE= TRUNC(SYSDATE)";
		
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

		String tdcWM_LOG_ERROR_TPEquery = "SELECT ERROR_ID, folio, ERROR_CODE FROM WMLOG.WM_LOG_ERROR_TPE WHERE TPE_TYPE='PE2' AND FOLIO= '%s' ORDER BY ERROR_DATE DESC;";
		
		String wmCodeToValidateGetDev = "000";
		String wmCodeToValidateAck = "111";
		String wmCodeToValidateFolio = "100";
		String MTI = "0420";
		String folio;
		String wmCodeRequest;
	
		
		   /* Pasos *******************************************************************************************************/
		testCase.setFullTestName(data.get("casoDePrueba"));
		testCase.setProject_Name("Crecimiento Servicios Electronicos (SF)");
		testCase.setTest_Description(data.get("Description"));
		//Paso 1
			addStep("Solicitar folio desde un navegador, invocando el servicio runGetFolio");
			
			//Ejecuta el servicio PE2.Pub:runGetFolio
			
			String responseRunGetFolio = pe2Util.ejecutarRunGetFolio();
			System.out.println(responseRunGetFolio); 
			
			//Se obtienen los datos folio, wm_code y creation_date del xml de respuesta y se agregan a la evidencia
	    	folio = getSimpleDataXml(responseRunGetFolio, "folio");
	    	wmCodeRequest = getSimpleDataXml(responseRunGetFolio, "wmCode");
	    	boolean WMCODE100 = wmCodeToValidateFolio.equals(wmCodeRequest);
	    	
	        assertTrue(WMCODE100,"El wmCode 100 no es Correcto");
			System.out.println(folio); 
			
	//Paso 2 ----------------------------------------------------------------------------------------------------------------------------
			String tipo = data.get("Name");
			addStep("Solicitar autorizacion de devolucion exitosa de "+tipo+" desde un navegador, invocando el servicio runGetDev:");
	    	  //Ejecuta el servicio PE2.Pub:runGetDev 
			 //Ejecuta el servicio PE2.Pub:runGetDev 
	    	String responseRunGetDev = pe2Util.ejecutarRunGetDev(folio);
			System.out.println(responseRunGetDev);
			
			//Se obtienen los datos folio, wm_code
	    	wmCodeRequest = getSimpleDataXml(responseRunGetDev, "wmCode");
	    	
			System.out.println(wmCodeRequest);
	    	
	    	boolean WMCODE000 = wmCodeToValidateGetDev.equals(wmCodeRequest);
	    	
	    	assertTrue(WMCODE000,"El wmCode no es Correcto");

			//****************************************************Validar conexion FCTDCQA *************************************************************************
	    	addStep("Conectarse a la BD FCTDCQA.");
			testCase.addTextEvidenceCurrentStep("Base de Datos: FCTDCQA.FEMCOM.NET");
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("La conexion a la base de datos FCTDCQA fue exitosa.");
			testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCTDCQA);

			
	    //Paso 4
	    	addStep("Ejecutar la siguiente consulta para validar que la transaccion de devolucion se registro exitosamente en la tabla TDC_DEVOLUTION");
	        System.out.println("\n"+ tdcDevolucionQuery); 
	       
	        String TDCDevolucion = String.format(tdcDevolucionQuery, folio);

	        SQLResult StatusTDCDevolucion = executeQuery(BDFCTDCQA, TDCDevolucion);
	                
	    	String QuerywmCode = StatusTDCDevolucion.getData(0, "WM_CODE");
	    	
	    	boolean valuesStatus = QuerywmCode.equals(wmCodeToValidateAck);// Valida si se encuentra en code 111
	    	while (!valuesStatus) {
	    	SQLResult resultC = executeQuery(BDFCTDCQA, TDCDevolucion);
	    	QuerywmCode = resultC.getData(0, "WM_CODE");
	    	valuesStatus = QuerywmCode.equals(wmCodeToValidateAck);
	    	Thread.sleep(2);
	    	}
	    	
	        StatusTDCDevolucion = executeQuery(BDFCTDCQA, TDCDevolucion);

	    	QuerywmCode = StatusTDCDevolucion.getData(0, "WM_CODE");

			System.out.println(QuerywmCode);
	    	
	    	boolean WMCODE111 = wmCodeToValidateAck.equals(QuerywmCode);
	    	 testCase.addTextEvidenceCurrentStep(TDCDevolucion);
	  		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	  		testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transaccion con los siguientes valores:");
	  		testCase.addQueryEvidenceCurrentStep(StatusTDCDevolucion,false);

	     	assertTrue(WMCODE111,"El wmCode 111 no es Correcto");
			
	     //Paso 5
	    	addStep("Ejecutar la siguiente consulta para validar que la transaccion  se registro exitosamente con la reversa en la tabla TDC_REVERSE");

			String TDCReverse = String.format(tdcTDC_REVERSE, folio);

			SQLResult StatusTDCReverse = executeQuery(BDFCTDCQA, TDCReverse);

			QuerywmCode = StatusTDCReverse.getData(0, "WM_CODE");

			System.out.println(QuerywmCode);

			WMCODE111 = wmCodeToValidateAck.equals(QuerywmCode);
			testCase.addTextEvidenceCurrentStep(TDCReverse);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"Se muestra el registro de la transaccion con los siguientes valores:");
			testCase.addQueryEvidenceCurrentStep(StatusTDCReverse, false);

			assertTrue(WMCODE111, "El wmCode 111 no es Correcto");

			// ****************************************************Validar conexion FCSWQA*************************************************************************

			addStep("Conectarse a la BD FCSWQA.");
			testCase.addTextEvidenceCurrentStep("Base de Datos: FCSWQA.FEMCOM.NET");
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("La conexion a la base de datos FCSWQA fue exitosa.");
			testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCSWQA);

			// Paso 7
			addStep("Ejecutar la siguiente consulta para validar que la transaccion  se registro exitosamente en la tabla TPE_SW_TLOG");

			String tempTPEQuery = String.format(tdcTPE_SW_TLOGquery, folio);
			SQLResult result3 = executeQuery(BDFCSWQA, tempTPEQuery);
			System.out.print(result3);
			boolean validationTPE = result3.isEmpty();

			if (!validationTPE) {
				testCase.addTextEvidenceCurrentStep(tempTPEQuery);
				testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
				testCase.addBoldTextEvidenceCurrentStep("Para las transacciones de reversa se generan 2 registros, en los cuales el campo MTI tendra los siguientes valores:");
				testCase.addBoldTextEvidenceCurrentStep("MTI=0200  Para la solicitud.");
				testCase.addBoldTextEvidenceCurrentStep("MTI=0420  Para la solicitud.");
				testCase.addBoldTextEvidenceCurrentStep("Del mismo modo se muestran el resto de los campos con los siguientes valores para cada uno de los registros:");
				testCase.addQueryEvidenceCurrentStep(result3, false);
				testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWQA_MONTERREY_S1.");
			} else {

				SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, tempTPEQuery);

				validationTPE = resultQRO.isEmpty();

				if (!validationTPE) {
					testCase.addTextEvidenceCurrentStep(tempTPEQuery);
					testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
					testCase.addBoldTextEvidenceCurrentStep("Para las transacciones de reversa se generan 2 registros, en los cuales el campo MTI tendra los siguientes valores:");
					testCase.addBoldTextEvidenceCurrentStep("MTI=0200  Para la solicitud.");
					testCase.addBoldTextEvidenceCurrentStep("MTI=0420  Para la solicitud.");
					testCase.addBoldTextEvidenceCurrentStep("Del mismo modo se muestran el resto de los campos con los siguientes valores para cada uno de los registros:");
					testCase.addQueryEvidenceCurrentStep(resultQRO, false);
					testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWQA_QUERETARO_S2.");
				}
			}

			assertTrue(!validationTPE);

			// ****************************************************Validar conexion		// FCWMLTAEQA	// *************************************************************************

			addStep("Conectarse a la BD FCWMLTAEQA.");
			 testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAQ.FEMCOM.NET");
				testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
				testCase.addBoldTextEvidenceCurrentStep("La conexion a la base de datos FCWMLTAEQA_MTY fue exitosa.");
			    testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCWMLTAEQA);

			
			// Paso 9 ------------------------------------------------------------------------------------------------------------------------------------------------------------
		    
			Thread.sleep(30000);
			  
			addStep("Ejecutar la siguiente consulta para validar que se registro el error de la transaccion:");
			String tempErrorQuery = String.format(tdcWM_LOG_ERROR_TPEquery, folio);
			SQLResult result2 = executeQuery(BDFCWMLTAEQA, tempErrorQuery);
			System.out.print(result2);
			boolean validationError = result2.isEmpty();
			testCase.addTextEvidenceCurrentStep(tempErrorQuery);
			if(!validationError) {
				testCase.addTextEvidenceCurrentStep(tempErrorQuery);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de error relacionado con reversa por timeout (TB) para la transaccion.");
			testCase.addQueryEvidenceCurrentStep(result2,false);
			testCase.addBoldTextEvidenceCurrentStep("Se encontro el registro en la BD de FCWMLTAEQA_MONTERREY_S1.");
			}else {
				
				SQLResult result2QRO = executeQuery(dbFCWMLTAEQA_QRO, tempErrorQuery);
				validationError = result2QRO.isEmpty();
				if(!validationError) {
					testCase.addTextEvidenceCurrentStep(tempErrorQuery);
					testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
					testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de error relacionado con reversa por timeout (TB) para la transaccion.");
					testCase.addQueryEvidenceCurrentStep(result2QRO,false);
					testCase.addBoldTextEvidenceCurrentStep("Se encontro el registro en la BD de FCWMLTAEQA_QUERETARO_S2.");

				}
				
			}
			assertTrue(!validationError); 
		
	}


	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. Realizar una devolucion de pago con tarjeta de manera no exitosa."
				+ "reversa de devolucion por no enviar ACK. Registrar la transaccion correctamente.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {		
		return "ATC_FT_002_PE2_Devolucion_De_Pago_Sin_ACK";
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
