package interfaces.pe6;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.GlobalVariables.DB_PASSWORD_FCTDCQA;
import static util.RequestUtil.getSimpleDataXml;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.PE6;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/**
 * PE6 Validar Switch_Transacción Reversa de pago a tarjeta de credito por ACK
 * Negativo Realizar un pago a tarjeta de crédito de forma no exitosa, reversa
 * por enviar ACK negativo, revisar que la transacción se registre correctamente
 * en la base de datos del switch FCSW.
 * 
 * @author 41335
 *
 */

public class PE6SwichReversaSinACKServElectronicos extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_007_PE6_Reversa_Sin_ACK_Serv_Electronicos(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 **************************************************************************/

		utils.sql.SQLUtil dbFCTDCQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDCQA,
				GlobalVariables.DB_USER_FCTDCQA, GlobalVariables.DB_PASSWORD_FCTDCQA);
		utils.sql.SQLUtil dbFCWMLTAEQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA,
				GlobalVariables.DB_USER_FCWMLTAEQA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA);
		utils.sql.SQLUtil dbFCSWQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO,
				GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);

		// BASE DE DATOS ANTIGUA
		 utils.sql.SQLUtil dbFCSWQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA,GlobalVariables.DB_USER_FCSWQA,
		 GlobalVariables.DB_PASSWORD_FCSWQA);
		
		// BASE DE DATOS NUEVA PARA PRUEBAS
		//utils.sql.SQLUtil dbFCSWQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWPRD_CRECI,
			//	GlobalVariables.DB_USER_FCSWPRD_CRECI, GlobalVariables.DB_PASSWORD_FCSWPRD_CRECI);

		PE6 pe6Util = new PE6(data, testCase, null);

		/**
		 * Variables
		 **/

		String wmCodeToValidate = "100";
		String tdcTransactionQuery = "select Folio,Plaza, Tienda, Caja, Creation_date from TPEUSER.TDC_TRANSACTION "
				+ "WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCTDCQA.
		String tdcTransactionQuery2 = "select Wm_code, Track1,Card_Type, Bin,Issuer from TPEUSER.TDC_TRANSACTION "
				+ "WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCTDCQA.
		String tdcTransactionQuery3 = "select Bank,Sw_auth_code,Amount,Switch from TPEUSER.TDC_TRANSACTION "
				+ "WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCTDCQA.
		String tdcTransactionQuery4 = "select IS_Name,Site,Prom_Type,Reversed from TPEUSER.TDC_TRANSACTION "
				+ "WHERE FOLIO='%s' AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";// FCTDCQA.
		String tdcTransactionQueryGeneral = "SELECT Folio,Plaza, Tienda, Caja, Creation_date,Wm_code, Track1,Card_Type,"
				+ " Bin,Issuer, Bank,Sw_auth_code,Amount,Switch, IS_Name,Site,Prom_Type,Reversed"
				+ " FROM tpeuser.tdc_transaction WHERE folio = %s";
		String wmCodeToValidateAuth = "000";
		String tdcTransactionQueryAuth = "SELECT folio, wm_code FROM tpeuser.tae_transaction WHERE folio = %s";
		String wmCodeToValidateAck = "101";
		String tdcTransactionQueryAck = "SELECT folio, wm_code FROM tpeuser.tae_transaction WHERE folio = %s";
		String tdcReverse = "SELECT Switch,Prom_Type,Site,Sw_reverse_code,Status,Plaza,Tienda,Caja "
				+ "FROM TPEUSER.TDC_REVERSE " + "WHERE FOLIO= '%s' "
				+ "AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";
		String tdcReverse2 = "SELECT Folio,Creation_date,Wm_code,Card_No,Card_Type,Amount "
				+ "FROM TPEUSER.TDC_REVERSE " + "WHERE FOLIO= '%s' "
				+ "AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";
		String tdcReverseGeneral = "SELECT Switch,Prom_Type,Site,Sw_reverse_code,Status,Plaza,Tienda,Caja,Folio,Creation_date,"
				+ "Wm_code,Card_No,Card_Type,Amount FROM TPEUSER.TDC_REVERSE " + "WHERE FOLIO= '%s'"
				+ "AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";
		String tdcTransactions = "SELECT Wm_code,Track1,Card_Type,BIN,Issuer " + " FROM TPEUSER.TDC_TRANSACTION "
				+ " WHERE FOLIO= '%s'" + " AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";
		String tdcTPESwLOG_1 = "SELECT Creation_Date,FOLIO,MTI, AMOUNT, PAN, AUTH_ID_RES, PROC_CODE, SW_CODE "
				+ " FROM SWUSER.TPE_SW_TLOG " + " WHERE FOLIO= '%s'"
				+ " AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";
		String tdcTPESwLOG_2 = "SELECT  COUNTER, IS_NAME,APPLICATION, ENTITY,PLAZA ,TIENDA , CAJA, POS_ENTRY_MODE"
				+ " FROM SWUSER.TPE_SW_TLOG " + " WHERE FOLIO= '%s'"
				+ " AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";
		String tdcTPESwLOG_3 = "SELECT Creation_Date,FOLIO,MTI, AMOUNT, PAN, AUTH_ID_RES, PROC_CODE, SW_CODE,COUNTER, "
				+ " IS_NAME,APPLICATION, ENTITY,PLAZA ,TIENDA , CAJA, POS_ENTRY_MODE " + " FROM SWUSER.TPE_SW_TLOG "
				+ " WHERE FOLIO= '%s'" + " AND CREATION_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY')";
		String tdcLog = "SELECT *  FROM WMLOG.WM_LOG_ERROR_TPE " + " WHERE TPE_TYPE='PE6' " + " AND FOLIO='%s'  "
				+ " AND ERROR_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY') " + " ORDER BY ERROR_DATE DESC";
		String errorTransaccion = "SELECT ERROR_ID,FOLIO,ERROR_DATE,SEVERITY,ERROR_TYPE,ERROR_CODE FROM WMLOG.WM_LOG_ERROR_TPE "
				+ "WHERE TPE_TYPE='PE6' AND " + "FOLIO='%s' AND " + "ERROR_DATE>=TO_CHAR(sysdate, 'DD/MON/YYYY') "
				+ "ORDER BY ERROR_DATE DESC";// FCWMLTAEQA.

		String folio, wmCodeRequest, wmCodeDb;

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 * 
		 */

		// erick
		//testCase.setFullTestName(data.get("casoDePrueba"));
		//testCase.setProject_Name("Crecimiento Servicios Electrónicos (SF)");
		//testCase.setTest_Description(data.get("Description"));

		String pre = data.get("pre");
		testCase.setPrerequisites(
				"Contar con acceso a la BD FCTDCQA_MTY.                                                                                        "
						+ "Contar con acceso a la BD FCWMLTAEQA_MTY.                                                                                  "
						+ "Contar con acceso a la BD FCSWPRD.                                                                                          "
						+ "Contar con un simulador bancario para que responda las solicitudes de autorización.                "
						+ "El bin relacionado a la tarjeta de " + pre
						+ " que se va a utilizar deberá tener el valor del campo APPLY_DEP='Y' en la tabla TDC_BIN de la BD FCTDCQA_MTY.                                                                 "
						+ "El bin relacionado a la tarjeta de " + pre
						+ " que se va a utilizar deberá tener el valor PROC_CODE='PAY' en la tabla TDC_ROUTING de la BD FCTDCQA_MTY.                                                                              "
						+ "Que el servicio/job PE6.Pub:runReverseManager.sh se encuentre en ejecución automática.  "
						+ "Solicitar apagar el tiempo de espera a la respuesta del proveedor threadTB desde la herramienta software AG Designer en la ruta: FEMSA_PE6 > PE6 > Mapping > mainGetAuth > TOP:SEQUENCE > TRY:SECUENCE > BRANCH > FALSE: SEQUENCE >FALSE: SEQUENCE > BRANCH >TLOG > BRANCH > FALSE: SEQUENCE >BRANCH > 000:SEQUENCE (OK )>  PE6.Utils:launch:Thread (//Lanza Thread TB).");

		// Paso 1
		// ***********************************************************************************************

		addStep("Solicitar un folio desde el navegador, invocando el servicio runGetFolio:");

		// Ejecuta el servicio PE1.Pub:runGetFolio
		String respuesta = pe6Util.runGetFolio_request();

		// Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la
		// evidencia
		folio = getSimpleDataXml(respuesta, "folio");
		wmCodeRequest = getSimpleDataXml(respuesta, "wmCode");

		boolean validateRequest = folio.equals(folio);

		System.out.println(validateRequest + " es igual ? :" + folio);

		assertTrue(validateRequest);

        // Paso 2 ***********************************************************************************************
        /**************************************************************************************************
		 * Solicitud de autoriazación
		 *************************************************************************************************/

//Paso 2
		String tarjeta = data.get("Name");
		addStep("Solicitar autorización de " + tarjeta + " desde un navegador, invocando el servicio runGetAuth:");

		// Ejecuta el servicio PE1.Pub:runGetAuth
		String respuestaAuth = pe6Util.runGetAuth_request();

		// Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la
		// evidencia
		folio = getSimpleDataXml(respuestaAuth, "folio");
		wmCodeRequest = getSimpleDataXml(respuestaAuth, "wmCode");

		boolean validateRequestAuth = folio.equals(folio);
		System.out.println(validateRequestAuth + " es igual ? :" + folio);

		assertTrue(validateRequestAuth);

//Paso 3 ***********************************************************************************************

		addStep("Conectarse a la BD FCTDCQA_MTY.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTDCQA.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCTDCQA);

       //Paso 4 ***********************************************************************************************

		addStep("Ejecutar la siguiente consulta para validar que la transacción se registró correctamente en la tabla TDC_REVERSE:");

		String query = String.format(tdcReverse, folio);
		String query2 = String.format(tdcReverse2, folio);
		String queryGeneral = String.format(tdcReverseGeneral, folio);

		System.out.println(query2);
		SQLResult rs = dbFCTDCQA.executeQuery(query);
		SQLResult rsPt2 = dbFCTDCQA.executeQuery(query2);

		boolean validationQuery1 = rs.isEmpty();

		int contador = 0;

		while (validationQuery1) {
			System.out.println(query);
			rs = dbFCTDCQA.executeQuery(query);
			rsPt2 = dbFCTDCQA.executeQuery(query2);
			validationQuery1 = rs.isEmpty();
			Thread.sleep(10000);
			contador++;

			if (contador == 10) {

				SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
				PakageManagment pok = new PakageManagment(u, testCase);

				String user = data.get("user");
				String ps = PasswordUtil.decryptPassword(data.get("ps"));
				String server = data.get("server");
				String con = "http://" + user + ":" + ps + "@" + server;

				System.out.println(GlobalVariables.DB_HOST_LOG);
				String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
				u.get(contra);

				String dateExecution = pok.runIntefaceWmOneButtonNoScreenShots("FEMSA_PE6","PE6.Pub:runReverseManager");
				u.close();
				System.out.println("Respuesta dateExecution " + dateExecution);

			}
		}

		if (!validationQuery1) {
			testCase.addTextEvidenceCurrentStep(queryGeneral);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"Se muestra el registro de la transacción con los siguientes valores:");
			testCase.addQueryEvidenceCurrentStep(rs, false);
			testCase.addQueryEvidenceCurrentStep(rsPt2, false);

		}

		assertFalse(validationQuery1);

//Paso 5 ***********************************************************************************************
		addStep("Ejecutar la siguiente consulta para validar que la transacción se registró correctamente en la tabla TDC_TRANSACTION:");

		String queryTran = String.format(tdcTransactionQuery, folio);
		String queryTran2 = String.format(tdcTransactionQuery2, folio);
		String queryTran3 = String.format(tdcTransactionQuery3, folio);
		String queryTran4 = String.format(tdcTransactionQuery4, folio);
		String queryTranGeneral = String.format(tdcTransactionQueryGeneral, folio);

		System.out.println(queryTranGeneral);
		SQLResult rs1 = dbFCTDCQA.executeQuery(queryTran);
		SQLResult rs2 = dbFCTDCQA.executeQuery(queryTran2);
		SQLResult rs3 = dbFCTDCQA.executeQuery(queryTran3);
		SQLResult rs4 = dbFCTDCQA.executeQuery(queryTran4);
		// String wmCodedb = rs.getData(0, "wm_code");

		boolean validationQuery2 = rs1.isEmpty();

		if (!validationQuery2) {
			testCase.addTextEvidenceCurrentStep(queryTranGeneral);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transacción de forma exitosa:");
			testCase.addQueryEvidenceCurrentStep(rs1, false);
			testCase.addQueryEvidenceCurrentStep(rs2, false);
			testCase.addQueryEvidenceCurrentStep(rs3, false);
			testCase.addQueryEvidenceCurrentStep(rs4, false);

		}

		assertFalse(validationQuery2);

//Paso 6 ***********************************************************************************************

// ****************************************************Validar conexion FCSWQA*************************************************************************
		addStep("Conectarse a la BD FCSWPRD.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCSWPRD ");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCSWPRD_CRECI);

//Paso 7 ***********************************************************************************************
		addStep("Ejecutar la siguiente consulta para validar que la transacción se registró correctamente en la tabla TPE_SW_TLOG:");

		String queryTPE = String.format(tdcTPESwLOG_1, folio);
		String queryTPEG = String.format(tdcTPESwLOG_3, folio);
		System.out.println(queryTPEG);
		SQLResult rsTPE = dbFCSWQA.executeQuery(queryTPE);

		boolean validationQuery3 = rsTPE.isEmpty();

		if (!validationQuery3) {

			testCase.addTextEvidenceCurrentStep(queryTPEG);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"Para las transacciones de reversa se generan 2 registros, en los cuáles el campo MTI tendrá los siguientes valores:");
			testCase.addBoldTextEvidenceCurrentStep("MTI=0200 -> PARA LA SOLICITUD");
			testCase.addBoldTextEvidenceCurrentStep("MTI=0420 -> PARA LA REVERSA");
			testCase.addQueryEvidenceCurrentStep(rsTPE, false);

		} else {
			SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, queryTPE);

			validationQuery3 = resultQRO.isEmpty();

			if (!validationQuery3) {
				testCase.addTextEvidenceCurrentStep(queryTPEG);
				testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
				testCase.addBoldTextEvidenceCurrentStep(
						"Para las transacciones de reversa se generan 2 registros, en los cuáles el campo MTI tendrá los siguientes valores:");
				testCase.addBoldTextEvidenceCurrentStep("MTI=0200 -> PARA LA SOLICITUD");
				testCase.addBoldTextEvidenceCurrentStep("MTI=0420 -> PARA LA REVERSA");
				testCase.addQueryEvidenceCurrentStep(resultQRO, false);
			}
		}
		String queryTPE_2 = String.format(tdcTPESwLOG_2, folio);
		System.out.println(queryTPE_2);
		SQLResult rsTPE_2 = dbFCSWQA.executeQuery(queryTPE_2);

		boolean validationQuery3_2 = rsTPE_2.isEmpty();

		if (!validationQuery3_2) {

			testCase.addQueryEvidenceCurrentStep(rsTPE_2, false);
			testCase.addTextEvidenceCurrentStep("Se encontró el registro en la BD FCSWPRD.");
		} else {
			SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, queryTPE_2);

			validationQuery3 = resultQRO.isEmpty();

			if (!validationQuery3) {
				testCase.addQueryEvidenceCurrentStep(resultQRO, false);

			}
		}

		assertFalse(validationQuery3);

//****************************************************Validar conexion FCWMLTAEQA *************************************************************************
		addStep("Conectarse a la BD FCWMLTAEQA_MTY.");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCWMLTAQ.FEMCOM.NET");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("Se establece la conexión con éxito a la BD.");
		testCase.addTextEvidenceCurrentStep("Host: " + GlobalVariables.DB_HOST_FCWMLTAEQA);

//Paso 8 ***********************************************************************************************
		addStep("Ejecutar la siguiente consulta para validar que no se registró error en la transacción:");

		String queryLog = String.format(errorTransaccion, folio);
		System.out.println(queryLog);
		SQLResult rsLog = dbFCWMLTAEQA.executeQuery(queryLog);

		boolean validationQuery4 = queryLog.isEmpty();

		if (!validationQuery4) {
			testCase.addTextEvidenceCurrentStep(queryTPE);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep(
					"No se muestra el registro de algún error relacionado con la transacción.");
			testCase.addQueryEvidenceCurrentStep(rsLog, false);

		} 

		assertFalse(validationQuery4); 

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminado. Realziar transaccion con tarjeta de forma no exitosa, reversa por no ACK "
				+ "y revisar que la trasnsaccion se registre correctamente.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAautomation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_007_PE6_Reversa_Sin_ACK_Serv_Electronicos";
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