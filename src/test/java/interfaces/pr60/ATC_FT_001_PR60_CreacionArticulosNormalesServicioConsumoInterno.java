package interfaces.pr60;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_001_PR60_CreacionArticulosNormalesServicioConsumoInterno extends BaseExecution{
	@Test(dataProvider = "data-provider")

	public void ATC_FT_001_PR60_CreacionArticulosNormalesServicioConsumoInterno_test(HashMap<String, String> data) throws Exception {

	
		/*
		 * Utileria
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbFcias = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCIASQA,GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		SQLUtil dbMuat = new SQLUtil(GlobalVariables.DB_HOST_FCMOMUAT, GlobalVariables.DB_USER_FCMOMUAT,GlobalVariables.DB_PASSWORD_FCMOMUAT);
		SQLUtil dbSuat = new SQLUtil(GlobalVariables.DB_HOST_FCDASUAT, GlobalVariables.DB_USER_FCDASUAT,GlobalVariables.DB_PASSWORD_FCDASUAT);
		SQLUtil dbFSIT = new SQLUtil(GlobalVariables.DB_HOST_FCWMFSIT, GlobalVariables.DB_USER_FCWMFSIT,GlobalVariables.DB_PASSWORD_FCWMFSIT);
		
		/**
		 * Proyecto: Actualizacion tecnologica Webmethods
		 * CP: MTC_FT_005 PR60 Creacion de articulos normales de servicio o de consumo interno en RMSv16 con registros de 
		 * las tablas stagin de SIV a traves de la interface PR60.
		 * 
		 */
		


//		Paso 2
		 String ValidProv="SELECT ITEM,SUPPLIER,ORIGIN_COUNTRY_ID,PRIMARY_SUPP_IND "
			 		+ "FROM WMUSER.WM_XXSERV_ITEM_SUPPLIER";
		 
//		 Paso 3
		 String ValidItem = "SELECT ITEM,ITEM_NUMBER_TYPE,ITEM_LEVEL,ITEM_DESC FROM WMUSER.WM_XXSERV_ITEM_MASTER";
//		 Paso 4
		 String ValidItemUDA= "SELECT ITEM,UDA_ID,UDA_VALUE,UDA_TYPE,USER_TRANSACTION FROM WMUSER.WM_XXSERV_ITEM_UDA";
//		 Paso 6
		 String ValidReg="SELECT ITEM,ITEM_NUMBER_TYPE,FORMAT_ID,PACK_IND,ITEM_LEVEL,TRAN_LEVEL FROM RMS.ITEM_MASTER WHERE ITEM='%s' ";
//		 Paso 7
		 String ValidIt=	" SELECT ITEM,ITEM_NUMBER_TYPE,FORMAT_ID,PREFIX,ITEM_PARENT FROM RMS.ITEM_MASTER WHERE ITEM='%s'" ;
//		 Paso 8
		 String ValidRegRib="SELECT CTRL_ID,MSG_TYPE,BUSINESS_ID,MSG_SEC,SOURCE,TARGET FROM WMUSER.RIB_ITEM_CONTROL ORDER BY CREATION_DATE DESC";
		
		String tdcIntegrationServerFormat = "	select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE LIKE '%PR5%' "
				+ "ORDER BY START_DT DESC) where rownum <=1";
		
	
		String consultaerror = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE from  wmlog.WM_LOG_ERROR where RUN_ID='%s') where rownum <=1";
		
	

        testCase.setProject_Name("I20016 Actualización tecnológica Webmethods 10");
		
		testCase.setPrerequisites(data.get("prerequisitos"));
	
		
		/*
		 Paso 1 **********************************************************************************************
		 
		  Solicitar el apoyo para crear un proveedor con sus items de servicio, desde el Portal de Automatización de Nuevos Proveedores (PANP).
		 
		 
		 **********************/
		
		// Paso 2 **************************************************************************************************
		
				addStep("Establecer conexion con la BD 'FCIASQA.FEMCOM.NET' ");
				
				testCase.addTextEvidenceCurrentStep("Conexion: FCIASQA ");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCIASQA);		
				
		
//		Paso 3 ******************************************************************************************************
		
		addStep("Validar que se encuentre el proveedor creado con los item de servicio, en la tabla WM_XXSERV_ITEM_SUPPLIER");
		 
		System.out.println(ValidProv);
		String ITEM ="";
		SQLResult ValidProvQuery = dbFcias.executeQuery(ValidProv);

		boolean ValidProvRTV=ValidProvQuery.isEmpty();
		if (ValidProvRTV == false) {
			ITEM = ValidProvQuery.getData(0, "ITEM");
			testCase.addTextEvidenceCurrentStep("se encuentro el proveedor creado con los item de servicio, en la tabla WM_XXSERV_ITEM_SUPPLIER");

		} 
		testCase.addQueryEvidenceCurrentStep(ValidProvQuery);
		System.out.println(ValidProvRTV);
		assertFalse(ValidProvRTV, "No se encuentro el proveedor creado con los item de servicio, en la tabla WM_XXSERV_ITEM_SUPPLIER");

//		Paso 4 ******************************************************************************************************
	
		addStep("Validar que se encuentre los items de servicio creados en la tabla WM_XXSERV_ITEM_MASTER");
		
		System.out.println(ValidItem);
		SQLResult execValidItem = dbFcias.executeQuery(ValidItem);

		boolean ValidItemres=execValidItem.isEmpty();
		if (!ValidItemres) {

			testCase.addTextEvidenceCurrentStep("se encuentro items de servicio creados en la tabla WM_XXSERV_ITEM_MASTER");

		} 
		testCase.addQueryEvidenceCurrentStep(execValidItem);
		System.out.println(ValidItemres);
		assertFalse(ValidItemres, "No se ecnuentran items de servicio creados en la tabla WM_XXSERV_ITEM_MASTER");

//		Paso 5 ******************************************************************************************************

		addStep("Validar que se encuentre los items de servicio creados con su UDA en la tabla WM_XXSERV_ITEM_UDA");
		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		System.out.println(ValidItemUDA);
		SQLResult execValidItemUDA = dbFcias.executeQuery(ValidItemUDA);

		boolean ValidItemUDAres=execValidItemUDA.isEmpty();
		if (!ValidItemUDAres) {

			testCase.addTextEvidenceCurrentStep("se encuentro items de servicio creados con su UDA en la tabla WM_XXSERV_ITEM_UDA");

		} 
		testCase.addQueryEvidenceCurrentStep(execValidItemUDA);
		System.out.println(ValidItemUDAres);
		assertFalse(ValidItemUDAres, "No se ecnuentran items de servicio creados con su UDA en la tabla WM_XXSERV_ITEM_UDA");
		
		
//		Paso 6 ******************************************************************************************************

		/*
		 * Solicitar el ordenamiento del Job runPR60_ITM enviando un correo electrónico a los operadores USU UsuFEMCOMOperadoresSITE@oxxo.com , en Control M para su ejecución.
			Job name: runPR60_ITMServicio
			Comando:runInterfase.sh PR60_ITMServicio
			Host : tqa_clu_int_105_2 (FCWMINTQA3 , FCWMQA8D)
			Usuario: isuser
			Grupo: TQA_BO_105
		 */

		addStep("Solicitar el ordenamiento del Job runPR60_ITM , en Control M para su ejecucion.");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);

		JSONObject obj = new JSONObject(data.get("job"));

		testCase.addBoldTextEvidenceCurrentStep("Jobs en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
		// testCase.addPaso("Paso con addPaso");
		testCase.addBoldTextEvidenceCurrentStep("Login");
		u.get(data.get("server"));
		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn();

		// Buscar del job
		testCase.addBoldTextEvidenceCurrentStep("Inicio de job ");
		ControlM control = new ControlM(u, testCase, obj);
		boolean flag = control.searchJob();
		assertTrue(flag);

		// Ejecucion
		String resultado = control.executeJob();
		System.out.println("Resultado de la ejecucion -> " + resultado);

		u.hardWait(30);

		// Valor del output

		String Res2 = control.getNewStatus();

		System.out.println("Valor de output getNewStatus:" + Res2);

		String output = control.getOutput();
		System.out.println("Valor de output control:" + output);

		testCase.addTextEvidenceCurrentStep("Status de ejecucion: " + Res2);
		testCase.addTextEvidenceCurrentStep("Output de ejecucion: " + output);
		
		// Validacion del caso
		Boolean casoPasado = false;
		if (Res2.equals("Ended OK")) {
			casoPasado = true;
		}

		control.closeViewpoint();
		u.close();
		assertTrue(casoPasado);
		 assertNotEquals("Failure",resultado);
		 
		 
		// Paso 7 ****************************************************
			
			addStep("Abrir la herramienta de control M para validar que la ejecución del job haya sido exitosa");
			
			testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + output);
			System.out.println("Resultado de la ejecucion -> " + output);
			
			assertEquals(output, "Ended OK");
			u.close();
			
	// Paso 8 ****************************************************
			
			addStep("Establecer conexión a la BD FCRMS16MOMUAT ");
			
			testCase.addTextEvidenceCurrentStep("Conexion exitosa: FCRMS16MOMUAT ");
			testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCMOMUAT);
		
			
		
//		Paso 9 ******************************************************************************************************
		
		 
		addStep("Validar que se haya creado el registro de los items de servicio en la tabla ITEM_MASTER de la BD FCRMS16MOMUAT de RMSv16, mediante la siguiente consulta:");
		System.out.println(GlobalVariables.DB_HOST_FCMOMUAT);
		String ValidRegForm = String.format(ValidReg, ITEM);
		System.out.println(ValidRegForm);
		SQLResult execValidRegForm = dbMuat.executeQuery(ValidRegForm);

		boolean ValidRegFormres=execValidRegForm.isEmpty();
		if (!ValidRegFormres) {

			testCase.addTextEvidenceCurrentStep("se valida que se haya creado el registro de los items de servicio en la tabla ITEM_MASTER");

		} 
		testCase.addQueryEvidenceCurrentStep(execValidRegForm);
		System.out.println(ValidRegFormres);
		assertFalse(ValidRegFormres, "No se valida que se haya creado el registro de los items de servicio en la tabla ITEM_MASTER");	
		
		

	// Paso 10 ****************************************************
		
		addStep("Establecer conexion a la BD FCDASUAT ");
	
		testCase.addTextEvidenceCurrentStep("Conexion: FCDASUAT ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCDASUAT);
		
	
//		Paso 11 ******************************************************************************************************
		
	
		addStep("Validar que la informacion del item de servicio se replique a la BD FCDASUAT por medio del Golden Gate de manera correcta, en la tabla ITEM_MASTER");
	
		String ValidItForm = String.format(ValidIt, ITEM);
		System.out.println(ValidItForm);
		SQLResult execValidItForm = dbSuat.executeQuery(ValidItForm);

		boolean ValidItFormres = execValidItForm.isEmpty();
		if (!ValidItFormres) {

			testCase.addTextEvidenceCurrentStep("se valida que la información del item de servicio se replique a la BD FCDASUAT");

		} 
		testCase.addQueryEvidenceCurrentStep(execValidItForm);
		System.out.println(ValidItFormres);
		assertFalse(ValidItFormres, "No se valida que que la información del item de servicio se replique a la BD FCDASUAT");	
		
		
		// Paso 12 ****************************************************
		
				addStep("Establecer conexión a la BD  FCWMFSIT  ");
						
				testCase.addTextEvidenceCurrentStep("Conexion: FCWMESIT ");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_fcwmesit);
				
		
		
//			Paso 13 ******************************************************************************************************
			
			
			addStep("Validar que se haya registrado en la tabla WMUSER.RIB_ITEM_CONTROL de la BD FCWMFSIT del Framework");
			System.out.println(GlobalVariables.DB_HOST_FCWMFSIT);
			System.out.println(ValidRegRib);
			SQLResult execValidRegRib = dbFSIT.executeQuery(ValidRegRib);

			boolean ValidRegRibres = execValidRegRib.isEmpty();
			if (!ValidRegRibres) {

				testCase.addTextEvidenceCurrentStep("Se valida que se haya registrado en la tabla WMUSER.RIB_ITEM_CONTROL de la BD FCWMFSIT del Framework");

			} 
			testCase.addQueryEvidenceCurrentStep(execValidRegRib);
			System.out.println(ValidRegRibres);
			assertFalse(ValidRegRibres, "No se valida que se haya registrado en la tabla WMUSER.RIB_ITEM_CONTROL de la BD FCWMFSIT del Framework");	
			
			
			// Paso 14 ****************************************************
			
			addStep("Establecer conexion a la BD FCWMLTAQ.FEMCOM.NET ");
					
			testCase.addTextEvidenceCurrentStep("Conexion: FCWMLTAQ ");
			testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLTAEQA);
		
			
//			Paso 15 ******************************************************************************************************
			
			addStep("Validar la correcta ejecucion de la interface PR60 en la tabla WM_LOG_RUN de BD FCWMLQA.");
			System.out.println(GlobalVariables.DB_HOST_LOG);
			System.out.println(tdcIntegrationServerFormat);
			String run_id="";
			SQLResult execLog = dbLog.executeQuery(tdcIntegrationServerFormat);

			boolean Logres = execLog.isEmpty();
			if (!Logres) {
				run_id = execLog.getData(0, "run_id");
				testCase.addTextEvidenceCurrentStep("Se valida la correcta ejecucion de la interface PR60 en la tabla WM_LOG_RUN");

			} 
			testCase.addQueryEvidenceCurrentStep(execLog);
			System.out.println(Logres);
			assertFalse(Logres, "No se valida la correcta ejecucion de la interface PR60 en la tabla WM_LOG_RUN");	
			
//			Paso 16 ******************************************************************************************************
			
			addStep("Realizar la siguiente consulta para verificar que no se encuentre ningun error presente en la ejecucion ");
			System.out.println(GlobalVariables.DB_HOST_LOG);
			String consultaerrorForm= String.format(consultaerror, run_id);
			System.out.println(consultaerrorForm);
			SQLResult execLogError = dbLog.executeQuery(consultaerrorForm);

			boolean LogErrorres = execLogError.isEmpty();
			if (LogErrorres) {

				testCase.addTextEvidenceCurrentStep("Se encontro error en la ejecucion");

			} 
			testCase.addQueryEvidenceCurrentStep(execLogError);
			System.out.println(LogErrorres);
			assertTrue(LogErrorres, "No se encontro error en la ejecucion");	
		 
		

	}

	@Override
	public String setTestFullName() {

		return null;

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub

		return "Creacion de articulos normales de servicio o de consumo interno en RMSv16 con registros de las tablas stagin de SIV a traves de la interface PR60.";

	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
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
