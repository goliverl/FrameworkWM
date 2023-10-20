package interfaces.RO1_MX;

import static org.testng.Assert.assertFalse;
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

public class ATC_FT_004_RO1_MX_EnvioInfoDeProveedores extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_RO1_MX_EnvioInfoDeProveedores_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbEbs = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil dbMuat = new SQLUtil(GlobalVariables.DB_HOST_FCMOMUAT, GlobalVariables.DB_USER_FCMOMUAT,GlobalVariables.DB_PASSWORD_FCMOMUAT);
		SQLUtil dbSuat = new SQLUtil(GlobalVariables.DB_HOST_FCDASUAT, GlobalVariables.DB_USER_FCDASUAT,GlobalVariables.DB_PASSWORD_FCDASUAT);

		String run_id = "";

		testCase.setProject_Name("AutomationQA");
		/**
		 * Proyecto: Actualizacion tecnologica
		 * CP: MTC-FT-002 RO1_MX Envio de informacion de proveedores a traves de la interface RO1_MX
		 * Desc: Prueba de regresion  para comprobar la no afectacion en la funcionalidad principal de la interface FEMSA_RO1_MX enviar 
		 * informacion de proveedores de EBS avante a RMSv16, al ser migrada la interface de WM9.9 a WM10.5
		 * 
		 */
		
		/**
		 * Variables
		 * ******************************************************************************************
		 * 
		 * 
		 */
//		Paso 1
		String tdcQueryContactName = "SELECT ID, VENDORID, CONTACTNAME, TERMSNAME, WM_STATUS, CREATE_DATE, ADDR_TYPE,RFC "
				+ " FROM WMUSER.WM_VENDORS_SYNC " + " WHERE WM_STATUS='L' ";
//		Paso 3
		String ValidLog = "SELECT run_id,start_dt,status " + "FROM WMLOG.WM_LOG_RUN  "
				+ "WHERE INTERFACE LIKE '%RO1_MX%' " + "AND STATUS = 'S' " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND rownum = 1  " + "ORDER BY START_DT DESC";
//		Paso 4
		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " + " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA
//		Paso 5
		String ValidVendor = " SELECT ID, VENDORID, CONTACTNAME, TERMSNAME, WM_STATUS, CREATE_DATE, ADDR_TYPE, RFC "
				+ "FROM WMUSER.WM_VENDORS_SYNC " + "WHERE ID = '%s' " + "AND WM_STATUS = 'E'";
//		Paso 6
		String Ret1 = "Select ADDR_KEY,MODULE,KEY_VALUE_1,PRIMARY_ADDR_IND " + "from RMS.ADDR "
				+ "where KEY_VALUE_1 ='%s' " + "and addr_type ='%s'";
		String Ret2 = "Select SUPPLIER,SUP_NAME,SUP_NAME_SECONDARY,SUPPLIER_PARENT " + "from RMS.SUPS "
				+ "where SUP_NAME_SECONDARY ='%s' " + "and supplier ='%s'";
//		 Paso 7
		String Req1 = "Select SUPPLIER,SUP_NAME,SUP_NAME_SECONDARY " + "from RMS.SUPS "
				+ "where SUP_NAM_SECONDARY = '%s' " + "and sup_name = '%s'";
		String Req2 = "Select ID_SEQ_DISTRICT,SUPPLIER,SUP_NAME,SUP_NAME_SECONDARY " + "from RMS_DT.DT_SUPS "
				+ "where SUP_NAM_SECONDARY = '%s' " + "and supplier ='%s' " + "and sup_name ='%s'";
		String Req3 = "Select ADDR_KEY,MODULE,KEY_VALUE_1,ADDR_TYPE " + "from RMS.ADDR " + "where KEY_VALUE_1 = '%s' "
				+ "and addr_type= '%s'";
		String Req4 = "Select ID_SEQ_DISTRICT,ADDR_KEY,KEY_VALUE_1,ADDR_TYPE " + "from RMS_DT.DT_ADDR "
				+ "where KEY_VALUE_1 = <ID del proveedor> " + "and addr_type = <ADDR_TYPE>";

		/**
		 * 
		 * **********************************Pasos del caso de Prueba
		 * *****************************************
		 * 
		 */

//		Paso 1	************************ 

		addStep("Consultar la tabla WMUSER.WM_VENDORS_SYNC de la DB FCAVEBQA para validar que los proveedores cuenten con el estatus L");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		System.out.println(tdcQueryContactName);
		String ID = "";

		SQLResult result1 = executeQuery(dbEbs, tdcQueryContactName);

		boolean contact = result1.isEmpty();

		if (!contact) {
			ID = result1.getData(0, "ID");

			testCase.addTextEvidenceCurrentStep("Los proveedores cuentan con el estatus L");

		}
		testCase.addQueryEvidenceCurrentStep(result1);
		System.out.println(contact);

		assertFalse(contact, "Los proveedores No cuentan con el estatus L");

//		Paso 2	************************

		/*
		 * Solicitar el ordenamiento del Job runRO1_MEX enviando un correo electrónico a
		 * los operadores USU UsuFEMCOMOperadoresSITE@oxxo.com , en Control M para su
		 * ejecución. Job name: runRO1_MEX Comando: runInterfase.sh RO1_MEX Host :
		 * tqa_clu_int_105_2 (FCWMINTQA3 , FCWMQA8D) Usuario: isuser Grupo: TQA_BO_105
		 */

		addStep("Solicitar el ordenamiento del Job runRO1_MEX, en Control M para su ejecución.");
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
		// assertNotEquals("Failure",resultado);

//		Paso 3 ************************************************************************************

		addStep("Validar que se inserte el detalle de la ejecución de la interface en la tabla WM_LOG_RUN de la DB  FCWMLQA.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);

		SQLResult ValidLogRes = executeQuery(dbLog, ValidLog);

		boolean ValidLogVal = ValidLogRes.isEmpty();

		if (!ValidLogVal) {
			run_id = ValidLogRes.getData(0, "RUN_ID");
			testCase.addTextEvidenceCurrentStep("Se encontro detalle de la ejecuion de servicio en estatus S");

		}
		testCase.addQueryEvidenceCurrentStep(ValidLogRes);
		System.out.println(ValidLogVal);

		assertFalse(ValidLogVal, "No se encontro detalle de la ejecuion de servicio en estatus S");

// Paso 4************************************************************************************		

		addStep(" Realizar la siguiente consulta para verificar que no se encuentre ningún error presente en la ejecución de la interfaz  dentro de la tabla WM_LOG_ERROR ");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String FormatError = String.format(tdcQueryErrorId, run_id);
		System.out.println(FormatError);

		SQLResult FormatErrorRes = executeQuery(dbLog, FormatError);

		boolean FormatErrorVal = FormatErrorRes.isEmpty();

		if (FormatErrorVal) {

			testCase.addTextEvidenceCurrentStep(
					"No se encontro ningún error presente en la ejecución de la interfaz  dentro de la tabla WM_LOG_ERROR");

		}
		testCase.addQueryEvidenceCurrentStep(FormatErrorRes);
		System.out.println(FormatErrorVal);

		assertTrue(FormatErrorVal,
				"se encontro error presente en la ejecución de la interfaz  dentro de la tabla WM_LOG_ERROR");

//		Paso 5*********************************************************************************************************

		addStep(" Consultar la tabla WMUSER.WM_VENDORS_SYNC de la DB FCAVEBQA para validar que se actualizo el campo WM_STATUS = 'E'  ");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		String ValidVendorFormat = String.format(ValidVendor, ID);
		System.out.println(ValidVendorFormat);
		SQLResult ValidVendorresult1 = executeQuery(dbEbs, ValidVendorFormat);
		String ADDR_TYPE = "";
		String VENDORID = "";
		String RFC = "";
		boolean ValidVendorVal = ValidVendorresult1.isEmpty();

		if (!ValidVendorVal) {
			VENDORID = ValidVendorresult1.getData(0, "VENDORID");
			ADDR_TYPE = ValidVendorresult1.getData(0, "ADDR_TYPE");
			RFC = ValidVendorresult1.getData(0, RFC);
			System.out.println("VENDORID: " + VENDORID + "\nADDR_TYPE: " + ADDR_TYPE + "\nRFC: " + RFC);

			testCase.addTextEvidenceCurrentStep(
					"Se valida que se actualizo el campo WM_STATUS = 'E' de la tabla WMUSER.WM_VENDORS_SYNC de la DB FCAVEBQA");

		}
		testCase.addQueryEvidenceCurrentStep(ValidVendorresult1);
		System.out.println(ValidVendorVal);

		assertFalse(ValidVendorVal,
				"No se actualizo el campo WM_STATUS = 'E' de la tabla WMUSER.WM_VENDORS_SYNC de la DB FCAVEBQA");

//	Paso 6	*****************************************************************************************************************

		addStep("Ejecutar las siguientes consultas para validar el envío de la informacin de proveedores a Retek.");

		System.out.println(GlobalVariables.DB_HOST_FCMOMUAT);
		String Ret1Format = String.format(Ret1, VENDORID, ADDR_TYPE);
		System.out.println(Ret1Format);
		String SUP_NAME = "";
		SQLResult Ret1Res = executeQuery(dbMuat, Ret1Format);

		boolean Ret1Val = Ret1Res.isEmpty();

		if (!Ret1Val) {

			testCase.addTextEvidenceCurrentStep("Se valida el envío de la informacin de proveedores a Retek.");

		}
		testCase.addQueryEvidenceCurrentStep(Ret1Res);
		System.out.println(Ret1Val);

		assertFalse(Ret1Val, "No se valida el envío de la informacin de proveedores a Retek.");

//			 Query 2

		System.out.println(GlobalVariables.DB_HOST_FCMOMUAT);
		String Ret2Format = String.format(Ret2, RFC, VENDORID);
		System.out.println(Ret1Format);
		SQLResult Ret2Res = executeQuery(dbMuat, Ret2Format);

		boolean Ret2Val = Ret1Res.isEmpty();

		if (!Ret2Val) {
			testCase.addTextEvidenceCurrentStep("Se valida el envío de la informacin de proveedores a Retek.");
			SUP_NAME = Ret1Res.getData(0, "SUP_NAME");
			System.out.println("SUP_NAME: " + SUP_NAME);
		}
		testCase.addQueryEvidenceCurrentStep(Ret2Res);
		System.out.println(Ret2Val);

		assertFalse(Ret2Val, "No se valida el envío de la informacin de proveedores a Retek.");

//		Paso 7	************************

		addStep("Ejecutar las siguientes consultas para validar el envío de la información de proveedores a Núcleo:");

		String Req1Format = String.format(Req1, RFC, SUP_NAME);
		System.out.println(Req1Format);
		SQLResult Req1Res = executeQuery(dbSuat, Req1Format);

		boolean Req1Val = Req1Res.isEmpty();

		if (!Req1Val) {

			testCase.addTextEvidenceCurrentStep("Se valida el envío de la informacin de proveedores ");

		}
		testCase.addQueryEvidenceCurrentStep(Req1Res);
		System.out.println(Req1Val);

		assertFalse(Req1Val, "No se valida el envío de la informacin de proveedores ");

//		 Query 2

		String Req2Format = String.format(Req2, RFC, VENDORID, SUP_NAME);
		System.out.println(Req2Format);
		SQLResult Req2Res = executeQuery(dbSuat, Req2Format);

		boolean Req2Val = Req2Res.isEmpty();

		if (!Req2Val) {

			testCase.addTextEvidenceCurrentStep("Se valida el envío de la informacin de proveedores ");

		}
		testCase.addQueryEvidenceCurrentStep(Req2Res);
		System.out.println(Req2Val);

		assertFalse(Req2Val, "No se valida el envío de la informacin de proveedores ");

//		 Query 3

		String Req3Format = String.format(Req3, ID, ADDR_TYPE);
		System.out.println(Req3Format);
		SQLResult Req3Res = executeQuery(dbSuat, Req3Format);

		boolean Req3Val = Req3Res.isEmpty();

		if (!Req3Val) {

			testCase.addTextEvidenceCurrentStep("Se valida el envío de la informacin de proveedores ");

		}
		testCase.addQueryEvidenceCurrentStep(Req3Res);
		System.out.println(Req3Val);

		assertFalse(Req3Val, "No se valida el envío de la informacin de proveedores ");

//		 Query 4

		String Req4Format = String.format(Req4, ID, ADDR_TYPE);
		System.out.println(Req4Format);
		SQLResult Req4Res = executeQuery(dbSuat, Req4Format);

		boolean Req4Val = Req4Res.isEmpty();

		if (!Req4Val) {

			testCase.addTextEvidenceCurrentStep("Se valida el envío de la informacin de proveedores ");

		}
		testCase.addQueryEvidenceCurrentStep(Req4Res);
		System.out.println(Req4Val);

		assertFalse(Req4Val, "No se valida el envío de la informacin de proveedores ");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "MTC-FT-002 RO1_MX Envío de informacion de proveedores a través de la interface RO1_MX";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_004_RO1_MX_EnvioInfoDeProveedores_test";
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
