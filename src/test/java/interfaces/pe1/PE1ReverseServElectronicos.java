package interfaces.pe1;

import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.PE1;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

public class PE1ReverseServElectronicos extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_006_PE1_PE1ReverseServElectronicos(HashMap<String, String> data) throws Exception {
// Utilerías *********************************************************************
		utils.sql.SQLUtil dbFCTAEQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTAEQA,GlobalVariables.DB_USER_FCTAEQA, GlobalVariables.DB_PASSWORD_FCTAEQA);
		//utils.sql.SQLUtil dbFCSWQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA,	GlobalVariables.DB_USER_FCSWQA, GlobalVariables.DB_PASSWORD_FCSWQA);
		PE1 pe1Util = new PE1(data, testCase, dbFCTAEQA);
		utils.sql.SQLUtil dbFCSWQA_QRO = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCSWQA_QRO,GlobalVariables.DB_USER_FCSWQA_QRO, GlobalVariables.DB_PASSWORD_FCSWQA_QRO);
		SQLUtil dbFCSWQA = new SQLUtil(GlobalVariables.DB_HOST_FCSWPRD_CRECI, GlobalVariables.DB_USER_FCSWPRD_CRECI, GlobalVariables.DB_PASSWORD_FCSWPRD_CRECI);

// Variables *********************************************************************
//PTAE_TRANSACTION
		String query_TAE_TRANSACTION1 = "SELECT FOLIO, CREATION_DATE, PLAZA, TIENDA, WM_CODE "
				+ " FROM TPEUSER.TAE_TRANSACTION "
				+ " WHERE CREATION_DATE >= TO_CHAR(SYSDATE,'DD-MON-YY') " 
				+ " AND TIENDA= '"+ data.get("tienda")+ "'" 
				+ " AND folio = '%s' "
				+ " ORDER BY CREATION_DATE DESC ";
		
		String query_TAE_TRANSACTION2 = "SELECT PHONE, SW_AUTH, SWITCH, IS_NAME "
				+ " FROM TPEUSER.TAE_TRANSACTION "
				+ " WHERE CREATION_DATE >= TO_CHAR(SYSDATE,'DD-MON-YY') " 
				+ " AND TIENDA= '"+ data.get("tienda")+ "'"
				+ " AND folio = '%s' "
				+ " ORDER BY CREATION_DATE DESC ";
		
		
		String query_TAE_TRANSACTIONGeneral = "SELECT FOLIO, CREATION_DATE, PLAZA, TIENDA, WM_CODE, PHONE, SW_AUTH, SWITCH, IS_NAME  "
				+ " FROM TPEUSER.TAE_TRANSACTION "
				+ " WHERE CREATION_DATE >= TO_CHAR(SYSDATE,'DD-MON-YY') " 
				+ " AND TIENDA= '"+ data.get("tienda")+ "'"
				+ " AND folio = '%s' "
				+ " ORDER BY CREATION_DATE DESC ";
		
		
		
//TAE_REVERSE
		String query_TAE_REVERSE1 = "SELECT folio, creation_date, plaza, tienda, phone, carrier, sw_auth, wm_code, retek_cr "
				+ "FROM TPEUSER.TAE_REVERSE WHERE CREATION_DATE>=TRUNC(SYSDATE) "
				+ "AND FOLIO='%s' ORDER BY CREATION_DATE DESC ";
//TPE_SW_TLOG
		String query_TPE_SW_TLOG1 = "SELECT CREATION_DATE, FOLIO, MTI, AMOUNT, PAN, AUTH_ID_RES "
				+ " from SWUSER.TPE_SW_TLOG "
				+ " where TIENDA='" + data.get("tienda")+ "'"
				+ " and APPLICATION='TAE' "
				+ " and CREATION_DATE>=TRUNC(SYSDATE) "
				+ " and folio ='%s' ";
		
		String query_TPE_SW_TLOG2 = "SELECT SW_CODE, COUNTER, IS_NAME, APPLICATION, ENTITY "
				+ " from SWUSER.TPE_SW_TLOG "
				+ " where TIENDA='" + data.get("tienda")+ "'"
				+ " and APPLICATION='TAE' "
				+ " and CREATION_DATE>=TRUNC(SYSDATE) "
				+ " and folio ='%s' ";
		
		String query_TPE_SW_TLOG3 = "SELECT PLAZA, TIENDA, CAJA, ACQUIRER, RESP_CODE, POS_ENTRY_MODE, PROC_CODE"
				+ " from SWUSER.TPE_SW_TLOG "
				+ " where TIENDA='" + data.get("tienda")+ "'"
				+ " and APPLICATION='TAE' "
				+ " and CREATION_DATE>=TRUNC(SYSDATE) "
				+ " and folio ='%s' ";
		
		String query_TPE_SW_TLOGGeneral = "SELECT CREATION_DATE, FOLIO, MTI, AMOUNT, PAN, AUTH_ID_RES, SW_CODE, COUNTER, IS_NAME, APPLICATION, ENTITY, PLAZA, TIENDA, CAJA, ACQUIRER, RESP_CODE, POS_ENTRY_MODE, PROC_CODE"
				+ " from SWUSER.TPE_SW_TLOG "
				+ " where TIENDA='" + data.get("tienda")+ "'"
				+ " and APPLICATION='TAE' "
				+ " and CREATION_DATE>=TRUNC(SYSDATE) "
				+ " and folio ='%s' ";
//Consultas de codigos
		String wmCodeToValidate = "100";
		String tdcTransactionQuery = "SELECT folio, wm_code FROM tpeuser.tae_transaction WHERE folio = '%s' ";
		String wmCodeToValidateAuth = "000";
		String tdcTransactionQueryAuth = "SELECT folio, wm_code FROM tpeuser.tae_transaction WHERE folio = '%s' ";
		String folio, wmCodeRequest, wmCodeDb;
		
		testCase.setFullTestName(data.get("casoDePrueba"));
		testCase.setProject_Name("Crecimiento de servicios electrónicos (SF)");
		testCase.setTest_Description(data.get("descripcion"));
		
		testCase.setPrerequisites("Contar con número de celular válido para realizar la recarga de tiempo aire.                     " + 
				"Contar con acceso a la BD FCTAEQA_MTY.                                                                                   " + 
				"Contar con acceso a la BD FCSWPRD.                                                                                                               " + 
				"Contar con la ejecución en automático del job PE1.reverseManager en Control-M.");
		
//***************************************************Llamar al servico PE1.Pub:runGetFolio****************************************************************************************************************************************
//Paso 1
		addStep("Ejecutar en un navegador directamente el servicio de solicitud de folio:");
		System.out.println("Paso 1: Llamar al servico PE1.Pub:runGetFolio");
		String respuesta = pe1Util.ejecutarRunGetFolio();// Ejecuta el servicio PE1.Pub:runGetFolio
		folio = getSimpleDataXml(respuesta, "folio");//Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la evidencia
		wmCodeRequest = getSimpleDataXml(respuesta, "wmCode");
		
		
		// Se valida que el wm_code del xml de respuesta sea igual a 100

		boolean validationRequest = wmCodeRequest.equals(wmCodeToValidate);
		System.out.println(validationRequest + " - wmCode request: " + wmCodeRequest);
	
		assertTrue(validationRequest);


//**********************************Llamar al servico PE1.Pub:runGetAuth********************************************************************************************************************

//Paso 2  
		addStep("Ejecutar en un navegador directamente el servicio de solicitud de Autorización:");

		// Ejecuta el servicio PE1.Pub:runGetAuth
		System.out.println("Paso 2: Llamar al servico PE1.Pub:runGetAuth ");
		String respuestaAuth = pe1Util.ejecutarRunGetAuth(folio);// Se obtienen los datos folio y wm_code del xml de respuesta y se agregan a la evidencia
		folio = getSimpleDataXml(respuestaAuth, "folio");
		wmCodeRequest = getSimpleDataXml(respuestaAuth, "wmCode");
		
		// Se valida que el wm_code del xml de respuesta sea igual a 000
		boolean validationRequestServAuth = wmCodeRequest.equals(wmCodeToValidateAuth);
		System.out.println(validationRequestServAuth + " - wmCode request: " + wmCodeRequest);
	
		assertTrue(validationRequestServAuth);

//****************************************Validar conexion a FCTAEQA*************************************************************************************************
		addStep("Establecer conexión con la base de datos: FCTAEQA_MTY ");
		testCase.addTextEvidenceCurrentStep("Base de Datos: FCTAEQA.FEMCOM.NET ");
		testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		testCase.addBoldTextEvidenceCurrentStep("La conexión a la base de datos FCTAEQA_MTY fue exitosa.");
		testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCTAEQA);
		
//********************************************Consulta en la tabla TAE_TRANSACTION:***********************************************************************************
		addStep("Ejecutar la siguiente consulta de la tabla TAE_TRANSACTION ");
		System.out.println("Paso 4: Ejecutar  consulta en  la tabla TAE_TRANSACTION:\n ");
		String TAE_TRANSACTION1 = String.format(query_TAE_TRANSACTION1, folio);
		String TAE_TRANSACTION2 = String.format(query_TAE_TRANSACTION2, folio);
		String TAE_TRANSACTIONG = String.format(query_TAE_TRANSACTIONGeneral, folio);
		
		SQLResult TAE_TRANSACTIONRes1 = dbFCTAEQA.executeQuery(TAE_TRANSACTION1);
		SQLResult TAE_TRANSACTIONRes2 = dbFCTAEQA.executeQuery(TAE_TRANSACTION2);

		System.out.println(TAE_TRANSACTION1);
		boolean TAE_TRANSACTIONBool = TAE_TRANSACTIONRes1.isEmpty(); // checa que el string contenga datos

		System.out.println(TAE_TRANSACTIONBool);

		if (!TAE_TRANSACTIONBool) {
			testCase.addTextEvidenceCurrentStep(TAE_TRANSACTIONG);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transacción de TAE de forma exitosa.");
			testCase.addQueryEvidenceCurrentStep(TAE_TRANSACTIONRes1,false); // Si no esta vacio, lo agrega a la evidencia
			testCase.addQueryEvidenceCurrentStep(TAE_TRANSACTIONRes2,false);
		}

		assertFalse("El registro de la transacción de TAE no fue exitoso.", TAE_TRANSACTIONBool);

		System.out.println(TAE_TRANSACTIONBool);
		
		
		
		Thread.sleep(60000);

//***************************************************Ejecutar el job runPE1ReverseManager.sh*********************************************************************

	      addStep("Ejecutar servicio runPE1ReverseManager.sh");
	       
	       System.out.println("Paso 5: Ejecutar servicio runPE1ReverseManager.sh ");
		     
		     // Utileria
		      SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		      PakageManagment pok = new PakageManagment(u, testCase);
		      

		      String user = data.get("user");
		      String ps = PasswordUtil.decryptPassword(data.get("ps"));
		      String server = data.get("server");
		      String con = "http://" + user + ":" + ps + "@" + server;
		     
		  
		      System.out.println(GlobalVariables.DB_HOST_LOG);
		      String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		      u.get(contra);

		   
		      String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		      System.out.println("Respuesta dateExecution " + dateExecution);
		      			
		     
		      	
		        testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
		    	testCase.addBoldTextEvidenceCurrentStep("Se ejecuta el job de forma exitosa sin mostrar errores.");
		    	 testCase.addScreenShotCurrentStep(u, "fin1");
 	u.close();
 	
 	
		
//*********************************************************Consulta en TAE_REVERSE********************************************************************************

		addStep("Ejecutar el siguiente query para la tabla TAE_REVERSE de la BD FCTAEQA_MTY para validar que se reversó exitosamente la transacción:");
		System.out.println("Paso 6: Ejecutar  query en la tabla TAE_REVERSE para validar que se reversó exitosamente la transacción:");

		//folio = "5596596529";
		String TAE_REVERSE1 = String.format(query_TAE_REVERSE1, folio);
		SQLResult TAE_REVERSERes1 = dbFCTAEQA.executeQuery(TAE_REVERSE1);

		System.out.println(TAE_REVERSE1);
		boolean TAE_REVERSE1Bool1 = TAE_REVERSERes1.isEmpty(); // checa que el string contenga datos

		System.out.println(TAE_REVERSE1Bool1);

		if (!TAE_REVERSE1Bool1) {
			testCase.addTextEvidenceCurrentStep(TAE_REVERSE1);
			testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
			testCase.addBoldTextEvidenceCurrentStep("Se ejecuta la consulta y se muestra el reverso de la transacción de forma exitosa.");
			testCase.addQueryEvidenceCurrentStep(TAE_REVERSERes1,false); // Si no esta vacio, lo agrega a la evidencia
		}

		assertFalse("La reversion en la transacción no fue exitosa.", TAE_REVERSE1Bool1);

		System.out.println(TAE_REVERSE1Bool1);

//***********************************************Validar conexion con FCSWQA*************************************************************************    

		   addStep("Establecer conexión con la base de datos: FCSWPRD");
	        testCase.addTextEvidenceCurrentStep("Base de Datos: FCSWPRD");
	        testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
	        testCase.addBoldTextEvidenceCurrentStep("La conexión a la base de datos FCSWPRD fue exitosa.");
	        testCase.addTextEvidenceCurrentStep("Host: "+ GlobalVariables.DB_HOST_FCSWPRD_CRECI);
//********************************************************Consulta query_TPE_SW_TLOG1*******************************************************************
		    addStep("Ejecutar la siguiente consulta de la tabla TPE_SW_TLOG:");
	
		    String tempTPEQuery = String.format(query_TPE_SW_TLOG1, folio);
		    String tempTPEQuery2 = String.format(query_TPE_SW_TLOG2, folio);
		    String tempTPEQuery3 = String.format(query_TPE_SW_TLOG3, folio);
		    String tempTPEQueryG = String.format(query_TPE_SW_TLOGGeneral, folio);
		    
			SQLResult result3 = executeQuery(dbFCSWQA, tempTPEQuery);
			SQLResult result3P2 = executeQuery(dbFCSWQA, tempTPEQuery2);
			SQLResult result3P3 = executeQuery(dbFCSWQA, tempTPEQuery3);
			
			boolean validationTPE = result3.isEmpty();

			
			
			if (!validationTPE) {			
				testCase.addTextEvidenceCurrentStep(tempTPEQueryG);
				testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
				testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transacción de TAE de forma exitosa.");
				testCase.addQueryEvidenceCurrentStep(result3,false);
				testCase.addQueryEvidenceCurrentStep(result3P2,false);
				testCase.addQueryEvidenceCurrentStep(result3P3,false);
				testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWPRD.");
			}else {
				
				SQLResult resultQRO = executeQuery(dbFCSWQA_QRO, tempTPEQuery);
				SQLResult resultQRO2 = executeQuery(dbFCSWQA_QRO, tempTPEQuery2);
				SQLResult resultQRO3 = executeQuery(dbFCSWQA_QRO, tempTPEQuery3);
				
				validationTPE = resultQRO.isEmpty();
				
				if(!validationTPE)
				{	testCase.addTextEvidenceCurrentStep(tempTPEQueryG);
					testCase.addBoldTextEvidenceCurrentStep("Resultado Esperado:");
					testCase.addBoldTextEvidenceCurrentStep("Se muestra el registro de la transacción de TAE de forma exitosa.");
				testCase.addQueryEvidenceCurrentStep(resultQRO,false);
				testCase.addQueryEvidenceCurrentStep(resultQRO2,false);
				testCase.addQueryEvidenceCurrentStep(resultQRO3,false);
				testCase.addTextEvidenceCurrentStep("Se encontro el registro en la BD de FCSWQA_QUERETARO_S2.");
				}
					}
			
			assertTrue(!validationTPE);
		     
		    
		
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. FEMSA_PE1_PE1ReverseServElectronicos";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_006_PE1_PE1ReverseServElectronicos";
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