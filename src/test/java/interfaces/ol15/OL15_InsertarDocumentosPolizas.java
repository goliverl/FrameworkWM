package interfaces.ol15;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class OL15_InsertarDocumentosPolizas extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_OL15_002_InsertarDocumentosPolizas(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Ebs,
				GlobalVariables.DB_USER_Ebs,GlobalVariables.DB_PASSWORD_Ebs);
		utils.sql.SQLUtil dbPuser = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, 
				GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);

		/*
		 * Variables
		 *********************************************************************/
		String noExisteEnLegacyBound = "SELECT COUNT(*)"
				+ " FROM WMUSER.LEGACY_INBOUND_DOCS"
				+ " WHERE LE_ID = '%s'";
		
		String insertarDocumentodePoliza1 = "INSERT INTO WMUSER.LEGACY_INBOUND_DOCS"
				+ " (LE_ID, ID, STATUS, DOC_TYPE, TARGET_ID, SENT_DATE, RUN_ID)"
				+ " VALUES('%s', '%s','L','POL', '%s', '%s', '%s')";
		
		String insertarDocumentodePoliza2 = "INSERT INTO WMUSER.LEGACY_POL_DETL"
				+ " (LID_ID, CONTROL, CONSECUTIVO, CR, CTA_CONTABLE, "
				+ " SUB_CTA, CVE_MVTO, ANALISIS, CONCEPTO, STATUS, "
				+ " VALOR, SEGMENT3, SEGMENT4, SEGMENT5)"
				+ " VALUES('%s', '%s', '%s', '%s', '%s',"
				+ " '%s', '%s', '%s', '%s', '%s', '%s', "
				+ " '%s', '%s', '%s')";
		
		String insertarDocumentodePoliza3 = "INSERT INTO WMUSER.LEGACY_POL_HEADER"
				+ " (LID_ID, CONTROL, CAPTURA, FECHA_AFECTACION, FOLIO, TOTAL_POLIZA)"
				+ " VALUES('%s', '%s', '%s', '%s', '%s', '%s')";
		
		String AlmacenaRegistrodeInterfaceWmLog = "SELECT * FROM WM_LOG_RUN"
				+ " WHERE INTERFACE LIKE 'OL15send'"
				+ " AND STATUS = 'S'"
				+ " AND TRUNC(START_DT) >= TRUNC(SYSDATE)"
				+ " ORDER BY RUN_ID DESC";
		
		String VerificaqueExisteRegistrosPoliza = "SELECT * FROM GL.GL_INTERFACE"
				+ " WHERE TRUNC(DATE_CREATED) = TRUNC(SYSDATE)"
				+ " AND SEGMENT3 = '%s'";
		
		String VerificaActualizaronDocProcesados = "SELECT * FROM WMUSER.LEGACY_INBOUND_DOCS"
				+ " WHERE STATUS = 'E'"
				+ " AND TRUNC(SENT_DATE) = TRUNC(SYSDATE)"
				+ " AND RUN_ID = '%s'"
				+ " AND LE_ID = '%s'";

		SeleniumUtil u1,u2; 
		PakageManagment pok1,pok2; 
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String statusToValidate = "S";
		String run_id, status, thread_status, thread_id;
	
		
		//testCase.setProject_Name("Interfaces Web Methods");
		//testCase.setFullTestName("OL15_"+data.get("origen"));
		//testCase.setTest_Description("Insertar documentos de Polizas para la Plaza "+data.get("origen"));
		

		/*
		 * *****************************************************************************
		 * Pasos
		 ***********************************************************************************************/
		/*
		 * Paso 1
		 *****************************************************************************************/
		addStep("Verificar que NO existan datos para procesar en la tabla LEGACY_INBOUND_DOCS de la BD ORAFINIMMEX");

		System.out.println(GlobalVariables.DB_USER_AVEBQA);
		
		noExisteEnLegacyBound = String.format(noExisteEnLegacyBound, data.get("LE_ID"));
		
		System.out.println(noExisteEnLegacyBound);

		SQLResult resultNoExisteenLegacy = executeQuery(dbEbs, noExisteEnLegacyBound);
				
		testCase.addQueryEvidenceCurrentStep(resultNoExisteenLegacy);

		boolean launcherEmpty = false;
		if(Integer.parseInt(resultNoExisteenLegacy.getFirstData()) == 0)
			launcherEmpty = true;

		assertTrue(launcherEmpty, "Si existe informacion para ser procesada.");
		
		/*
		 * Paso 2
		 *****************************************************************************************/
		addStep("Insertar el documento de las polizas en las siguientes tablas de la BD ORAFINIMMEX");

		System.out.println(GlobalVariables.DB_USER_AVEBQA);
		
		insertarDocumentodePoliza1 = String.format(insertarDocumentodePoliza1, data.get("LE_ID"), data.get("ID"), data.get("TARGET_ID"), data.get("SENT_DATO"), data.get("RUN_ID"));
		
		System.out.println(insertarDocumentodePoliza1);

		SQLResult resultinsertarDocumentodePoliza1 = executeQuery(dbEbs, insertarDocumentodePoliza1);
				
		testCase.addQueryEvidenceCurrentStep(resultinsertarDocumentodePoliza1);
		
		insertarDocumentodePoliza2 = String.format(insertarDocumentodePoliza2, data.get("LID_ID"), data.get("CONTROL"), data.get("CONSECUTIVO"), data.get("CR"), data.get("CTA_CONTABLE"), data.get("SUB_CTA"), data.get("CVE_MVTO"), data.get("ANALISIS"), data.get("CONCEPTO"), data.get("STATUS"), data.get("VALOR"), data.get("SEGMENT3"), data.get("SEGMENT4"), data.get("SEGMENT5"));
		
		System.out.println(insertarDocumentodePoliza2);

		SQLResult resultinsertarDocumentodePoliza2 = executeQuery(dbEbs, insertarDocumentodePoliza2);
				
		testCase.addQueryEvidenceCurrentStep(resultinsertarDocumentodePoliza2); //
		
		insertarDocumentodePoliza3 = String.format(insertarDocumentodePoliza3, data.get("LID_ID"), data.get("CONTROL"), data.get("CAPTURA"), data.get("FECHA_AFECTACION"), data.get("FOLIO"), data.get("TOTAL_POLIZA"));
		
		System.out.println(insertarDocumentodePoliza3);

		SQLResult resultinsertarDocumentodePoliza3 = executeQuery(dbEbs, insertarDocumentodePoliza3);
				
		testCase.addQueryEvidenceCurrentStep(resultinsertarDocumentodePoliza3);

		launcherEmpty = true;
		if(resultinsertarDocumentodePoliza1.isEmpty() || resultinsertarDocumentodePoliza2.isEmpty() || resultinsertarDocumentodePoliza3.isEmpty())
			launcherEmpty = false;

		assertTrue(launcherEmpty, "No se insertaron los registros correctamente.");
		
		/*
		 * Paso 3
		 *****************************************************************************************/
		addStep("Verificar que se almacene el registro de la ejecucion de la interface en WMLOG");

		System.out.println(GlobalVariables.DB_USER_AVEBQA);
		
		System.out.println(AlmacenaRegistrodeInterfaceWmLog);

		SQLResult resultAlmacenaRegistrodeInterfaceWmLog = executeQuery(dbEbs, AlmacenaRegistrodeInterfaceWmLog);
		
		String RUN_ID = resultAlmacenaRegistrodeInterfaceWmLog.getData(0, "RUN_ID");
				
		testCase.addQueryEvidenceCurrentStep(resultAlmacenaRegistrodeInterfaceWmLog);

		launcherEmpty = false;
		if(resultAlmacenaRegistrodeInterfaceWmLog.getRowCount() > 0)
			launcherEmpty = true;

		assertTrue(launcherEmpty, "No existe el regitro de la ejecucion de la interface con status igual a 'S'.");
		
		/*
		 * Paso 4
		 *****************************************************************************************/
		addStep("Verificar que existan registro de las polizas procesadas en la tabla GL_INTERFACE en la BD ORAFINIMMEX.");

		System.out.println(GlobalVariables.DB_USER_AVEBQA);
		
		System.out.println(VerificaqueExisteRegistrosPoliza);

		SQLResult resultVerificaqueExisteRegistrosPoliza = executeQuery(dbEbs, VerificaqueExisteRegistrosPoliza);
				
		testCase.addQueryEvidenceCurrentStep(resultVerificaqueExisteRegistrosPoliza);

		launcherEmpty = false;
		if(resultVerificaqueExisteRegistrosPoliza.getRowCount() > 0)
			launcherEmpty = true;

		assertTrue(launcherEmpty, "No existe el registro de las polizas procesadas.");
		
		/*
		 * Paso 5
		 *****************************************************************************************/
		addStep("Verificar que se actualizaron los documentos procesados correctamente en la tabla LEGACY_INBOUND_DOCS de la BD ORAFINIMMEX.");

		System.out.println(GlobalVariables.DB_USER_AVEBQA);
		
		VerificaActualizaronDocProcesados = String.format(VerificaActualizaronDocProcesados, data.get("LE_ID"), RUN_ID);
		
		System.out.println(VerificaActualizaronDocProcesados);

		SQLResult resultVerificaActualizaronDocProcesados = executeQuery(dbEbs, VerificaActualizaronDocProcesados);
				
		testCase.addQueryEvidenceCurrentStep(resultVerificaActualizaronDocProcesados);

		launcherEmpty = false;
		if(resultVerificaActualizaronDocProcesados.getRowCount() > 0)
			launcherEmpty = true;

		assertTrue(launcherEmpty, "No se actualizan correctamente los documentos procesados.");
		
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "El objetivo de la interface es recibir la nómina diaria e importarla a Oracle GL";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatización";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_OL15_002_InsertarDocumentosPolizas";
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
