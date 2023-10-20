package interfaces.po8;

import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;

import org.json.JSONObject;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.ControlM;
import utils.controlm.pageObject.Control_mInicio;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLResultExcel;

/**
 * Proyecto: "Actualizacion tecnologica Webmethods"
 * Este script cubre los siguientes casos de prueba: Prueba de regresión para
 * comprobar la no afectación en la funcionalidad principal de la interface
 * FEMSA_PO8 de avante para procesar archivos INV (Inventario diario) y ser
 * enviados de WM INBOUND a EBS, al ser migrada la interface de WM9.9 a WM10.5.
 * SALDO DIARIO La interface PO8 recibe la información de "saldo diario" de las
 * tiendas y la envía a la aplicación de Oracle financiero a diario.
 * La interfaz será disparada diariamente por un programador y recogerá toda la
 * información recibida de las tiendas.
 * 
 * @author Mariana Vives
 * @date 2023/02/02
 */

public class ATC_FT_004_PO8_ProcesarArchivoInventarioDiario extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_PO8_ProcesarArchivoInventarioDiario_test(HashMap<String, String> data) throws Exception {

	
		/* Utilerias ********************************************************************************************************************************************/
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbEBS = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);		
		utils.sql.SQLUtil FCWMLTQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG2, GlobalVariables.DB_USER_FCWMLQA_WMLOG2, GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG2);
		
		 /* Variables ******************************************************************************************************************************************/

		//Paso 2
		String ValidaStatusI = "SELECT id, PV_DOC_NAME, SUBSTR(PV_DOC_NAME,4,5) as PLAZA\r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS\r\n"
				+ "WHERE STATUS='I' \r\n"
				+ "AND DOC_TYPE='SDI'        \r\n"
				+ "AND ROWNUM<=5        \r\n"
				+ "AND SUBSTR(PV_DOC_NAME,4,5)='" + data.get("plaza") + "'";
		
		//Paso 3
		String ValidaPos_Sdi= "Select *\r\n"
				+ "FROM POSUSER.POS_SDI\r\n"
				+ "where PID_ID = '%s'"
				+ "AND ROWNUM<=5 \r\n";

		//Paso 4
		String ValidaPos_Sdi_Detl = "Select *\r\n"
				+ "FROM POSUSER.POS_SDI_DETL \r\n"
				+ "WHERE PID_ID in '%s' \r\n"
				+ "AND ROWNUM<=5        \r\n";
		
		//Paso 5
		String ValidaAplicacionPO8 = "SELECT * from WMUSER.WM_FW_BO_ENCENDIDO where APPLICATION = 'PO8'";
		
		//Paso 8
		String ValidaEBS = "SELECT ID_TIENDA_RETEK, CR_TIENDA, CR_PLAZA, TOTAL_PRECIO_COSTO, TOTAL_PRECIO_VENTA, INTERFACE_DATE, VAT_CODE \r\n"
				+ "FROM XXFC_SALDO_DIARIO_POS \r\n"
				+ "WHERE CR_PLAZA = '" + data.get("plaza") +"'"
				+ "AND  CR_TIENDA = '" + data.get("tienda") +"'"
				+ "AND ROWNUM<=5        \r\n"
				+ "ORDER BY INTERFACE_DATE desc";
		
		//Paso 9
		String ValidaStatusE = "SELECT * FROM POSUSER.POS_INBOUND_DOCS\r\n"
				+ "WHERE STATUS='E' AND DOC_TYPE='SDI'"
				+ "AND SUBSTR(PV_DOC_NAME,4,5) in('"+ data.get("plaza") +"')"
				+ "AND ROWNUM<=5        \r\n"
				+ "ORDER BY Received_Date asc";
				
		//Paso 11
		String ValidaEjeInterfPO8 = "Select RUN_ID, INTERFACE, START_DT, STATUS, SERVER FROM wmlog.wm_log_run\r\n"
				+ "WHERE interface = 'PO8'\r\n"
				+ "AND START_DT >= TRUNC(SYSDATE)\r\n"
				+ "ORDER BY RUN_ID DESC";
		
		//Paso 12
		String ValidaThreads = "SELECT THREAD_ID, PARENT_ID, NAME, START_DT, STATUS \r\n"
				+ "FROM wmlog.WM_LOG_THREAD \r\n"
				+ "WHERE PARENT_ID = '%s'"
				+ "AND ROWNUM<=5        \r\n";
		
		//Paso 13
		String ValidaError ="SELECT * FROM wmlog.WM_LOG_ERROR WHERE RUN_ID= '%s'"
				+ "AND ROWNUM<=5        \r\n";
		
//*******************************************************************Paso 1****************************************************************************************************		
		addStep("Establecer la conexion con la BD **FCWM6QA**.");
		testCase.addBoldTextEvidenceCurrentStep("Conexion: FCWM6QA");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_NUEVA);		
		
		
//*******************************************************************Paso 2****************************************************************************************************		

		addStep("Verificar que exista información disponible para procesar en la tabla POS_INBOUD_DOCS y PLAZAS de la BD FCWM6QA.");
		SQLResult validaStatusI_res= executeQuery(dbPos,ValidaStatusI);
		System.out.println(ValidaStatusI);
		
		boolean validaStatusI = validaStatusI_res.isEmpty();
		String id="";
		
		if(!validaStatusI) {
			id = validaStatusI_res.getData(0, "ID");
			System.out.println("ID : " + id);
			testCase.addBoldTextEvidenceCurrentStep("Valida información disponible para procesar en la tabla POS_INBOUND_DOCS");
		}
		testCase.addQueryEvidenceCurrentStep(validaStatusI_res);
		assertFalse(validaStatusI);
		
//*******************************************************************Paso 3****************************************************************************************************		
		addStep("Verificar que exista información disponible para procesar en la tabla POS_SDI de la BD FCWM6QA.");
		String queryPOS_SDI = String.format(ValidaPos_Sdi, id);
		SQLResult validaPOS_SDI= dbPos.executeQuery(queryPOS_SDI);
		
		System.out.println(queryPOS_SDI);
		
		boolean ValidaPOS_SDI = validaPOS_SDI.isEmpty();
		
		if(!ValidaPOS_SDI) {
			testCase.addBoldTextEvidenceCurrentStep("Se verifica que exista información disponible para procesar en la tabla POS_SDI con el ID");
		}
		testCase.addQueryEvidenceCurrentStep(validaPOS_SDI);
		assertFalse(ValidaPOS_SDI);
		
//*******************************************************************Paso 4****************************************************************************************************		
		addStep("Verificar que exista el detalle de la información a procesar en la tabla POS_SDI_DETL de la BD FCWM6QA.");
		String  queryPos_Sdi_Detl = String.format(ValidaPos_Sdi_Detl, id);
		SQLResult validaQueryPos_Sdi_Det1 = dbPos.executeQuery(queryPos_Sdi_Detl);
		
		System.out.println(queryPos_Sdi_Detl);
		boolean ValidaQueryPos_SDI= validaQueryPos_Sdi_Det1.isEmpty();
		
		if(!ValidaQueryPos_SDI) {
			testCase.addBoldTextEvidenceCurrentStep("Se verifica que exista el detalle de la información a procesar en la tabla POS_SDI_DETL en la BD FCWM6QA.");
		}
		testCase.addQueryEvidenceCurrentStep(validaQueryPos_Sdi_Det1);
		assertFalse(ValidaQueryPos_SDI);

//*******************************************************************Paso 5****************************************************************************************************			
		addStep("Validar la configuración de la interfaz, se muestre para las plazas de la información a procesar, en la BD FCWM6QA.");
		SQLResult ValidaMuestraPlazas = executeQuery(dbPos,ValidaAplicacionPO8);
		System.out.println(ValidaAplicacionPO8);
		
		boolean validaMuestraPlazas = ValidaMuestraPlazas.isEmpty();
		if(!validaMuestraPlazas) {
			testCase.addBoldTextEvidenceCurrentStep("Validar la configuracion de la interfaz y que se myestra informacion a procesar en la BD FCWM6QA.");
		}
		testCase.addQueryEvidenceCurrentStep(ValidaMuestraPlazas);
		assertFalse(validaMuestraPlazas);
		
//*******************************************************************Paso 6****************************************************************************************************				
		addStep("Ejecutar la interfaz PO8 en Control-M.");
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		JSONObject obj = new JSONObject(data.get("job"));

		testCase.addBoldTextEvidenceCurrentStep("Jobs en  Control M ");
		Control_mInicio CM = new Control_mInicio(u, data.get("user"), data.get("ps"));
		//testCase.addPaso("Paso con addPaso");
		testCase.addBoldTextEvidenceCurrentStep("Login");
		u.get(data.get("server"));
		u.hardWait(40);
		u.waitForLoadPage();
		CM.logOn(); 

		//Buscar del job
		testCase.addBoldTextEvidenceCurrentStep("Inicio de job ");
		ControlM control = new ControlM(u, testCase, obj);
		boolean flag = control.searchJob();
		assertTrue(flag);
		
		//Ejecucion
		String resultado = control.executeJob();
		System.out.println("Resultado de la ejecucion -> " + resultado);
		u.hardWait(30);
		
		//Valor del output 
	
		String Res2 = control.getNewStatus();
		System.out.println ("Valor de output getNewStatus:" +Res2);
		//String output = control.getOutput();
		//System.out.println ("Valor de output control:" +output);
		
		testCase.addTextEvidenceCurrentStep("Status de ejecucion: "+Res2);
		//testCase.addTextEvidenceCurrentStep("Output de ejecucion: "+output);

		//Validacion del caso
		Boolean casoPasado = false;
		if(Res2.equals("Ended OK")) {
		casoPasado = true;
		}		
		
		control.closeViewpoint();
		u.close();
		assertTrue(casoPasado);
		//assertNotEquals("Failure",resultado); 
	
//*******************************************************************Paso 7****************************************************************************************************		
		addStep("Establecer la conexion con la BD **AVEBQA**.");
		testCase.addBoldTextEvidenceCurrentStep("Conexion: AVEBQA");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_AVEBQA);	
		
//*******************************************************************Paso 8****************************************************************************************************			
		addStep("Verificar la información enviada a la EBS, en la BD AVEBQA.");
		SQLResult VerificaEBS = executeQuery(dbEBS, ValidaEBS);
		boolean verificaEBS = VerificaEBS.isEmpty();
		if(!verificaEBS) {
			testCase.addBoldTextEvidenceCurrentStep("Se verificó que la información fue enviada a EBS.");
		}
		testCase.addQueryEvidenceCurrentStep(VerificaEBS);
		assertFalse(verificaEBS);
		
//*******************************************************************Paso 9****************************************************************************************************		
		addStep("Validar que se actualizo el estado de los documentos en la pos_inbound_docs en la BD  FCWM6QA.");
		SQLResult validaStatusE = executeQuery(dbPos,ValidaStatusE);
		boolean ValidarStatusE = validaStatusE.isEmpty();
		if(!ValidarStatusE) {
			testCase.addBoldTextEvidenceCurrentStep("Se valida que se actualizo el estatus de los documentos en la pos_inbound_docs en la BD FCWM6QA");
		}
		testCase.addQueryEvidenceCurrentStep(validaStatusE);
		assertFalse(ValidarStatusE);
		
//*******************************************************************Paso 10****************************************************************************************************		
		addStep("Realizar conexión a la BD FCWMLTAQ.FEMCOM.NET del host oxfwm6q00.femcom.net.");
		testCase.addBoldTextEvidenceCurrentStep("Conexion: FCWMLQA_WMLOG");
		testCase.addBoldTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA_WMLOG);

//*******************************************************************Paso 11****************************************************************************************************		
		addStep("Validar la correcta ejecucion de la interface PO8 en la tabla WM WM_LOG_RUN de la BD FCWMLQA.");
		//Generar el script para threads
		SQLResult ValidaEjecucionInterface = executeQuery(FCWMLTQA, ValidaEjeInterfPO8);
		boolean validaEjecucionInterface = ValidaEjecucionInterface.isEmpty();
		String run_id="";
		if(!validaEjecucionInterface) {
			run_id = ValidaEjecucionInterface.getData(0, "RUN_ID");
			System.out.println("RUN_ID : " + run_id);
			testCase.addBoldTextEvidenceCurrentStep("Valida la correcta ejecucion de la interface PO8 en la tabla WM_LOG_RUN.");
		}
		testCase.addQueryEvidenceCurrentStep(ValidaEjecucionInterface);
		assertFalse(validaEjecucionInterface);
		
//*******************************************************************Paso 12***************************************************************************************************		
		addStep("Validar la correcta ejecución de los Threads lanzados por la interface PO8 en la tabla WM_LOG_THREAD de la BD FCWMLQA.");
		
		String valThreads = String.format(ValidaThreads, run_id);
		System.out.println(valThreads);
		
		SQLResult ValidacionThreads = FCWMLTQA.executeQuery(valThreads);
		boolean validacionThreads = ValidacionThreads.isEmpty();
		if(!validacionThreads) {
			testCase.addBoldTextEvidenceCurrentStep("Se valida la correcta ejecucion de los Threads generadios por la interface PO8 en la tabla WM_LOG_THREAD de la BD FCWMLQA con usuario wmlog.");
		}
		testCase.addQueryEvidenceCurrentStep(ValidacionThreads);
		assertFalse(validacionThreads);

//*******************************************************************Paso 13***************************************************************************************************		
		addStep("Valida que no se encuentre ningun error presente en la ejecucion de la interfaz PO8 en la tabla WM_LOG_ERROR de BD FCWMLQA.");
		
		String ValidarError = String.format(ValidaError, run_id); 
		System.out.println(ValidaError);
		SQLResult validaError = FCWMLTQA.executeQuery(ValidarError);
		boolean ValidaNoExisteError = validaError.isEmpty();
		if(ValidaNoExisteError) {
			testCase.addBoldTextEvidenceCurrentStep("Valida que no hay error presente en la ejecucion de la interface PO8 en la tabla WM_LOG_ERROR en la BD FCWMLQA.");
		}
		testCase.addQueryEvidenceCurrentStep(validaError);
		assertTrue(ValidaNoExisteError);
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

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_004_PO8_ProcesarArchivoInventarioDiario_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
