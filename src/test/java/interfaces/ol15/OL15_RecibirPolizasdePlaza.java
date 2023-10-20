package interfaces.ol15;

import modelo.BaseExecution;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class OL15_RecibirPolizasdePlaza extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_OL15_003_RecibirPolizasdePlaza(HashMap<String, String> data) throws Exception {

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
		String ValidaEjecucionEnTablaWMLOG = "SELECT * FROM WM_LOG_RUN"
				+ " WHERE INTERFACE LIKE 'OL15'"
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE)"
				+ " AND STATUS = 'S'"
				+ " ORDER BY RUN_ID DESC";
		
		String VerificaDocumentoRecibidoLegacy = "SELECT * FROM WMUSER.LEGACY_ENVELOPE"
				+ " WHERE DOC_NAME = '%s'"
				+ " AND STATUS = 'L'"
				+ " AND TRUNC(RECEIVED_DATE) = TRUNC(SYSDATE)"
				+ " AND RUN_ID = '%s'";
		
		String RevisaenLegacyquetengaPOL = "SELECT * FROM WMUSER.LEGACY_INBOUND_DOCS"
				+ " WHERE DOC_TYPE = 'POL'"
				+ " AND STATUS = 'L'"
				+ " AND LE_ID = '%s'";
		
		String VerificarRegistroEnLegacyPOL = "SELECT * FROM LEGACY_POL_HEADER"
				+ " WHERE LID_ID = '%s'";
		
		String ValidaInsertadoLineasLegacyOrafin = "SELECT * FROM LEGACY_POL_DETL"
				+ " WHERE LID_ID = '%s'";

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
		//testCase.setTest_Description("Recibir pólizas correctamente para la Plaza");
		

		/*
		 * *****************************************************************************
		 * Pasos
		 ***********************************************************************************************/
		
		/*
		 * Paso 1
		 *****************************************************************************************/
		
		// No se puede realizar ya que el framework no esta adecuado a subir archivos por ftp y no se tiene tampoco acceso al ftp
		
		/*
		 * Paso 2
		 *****************************************************************************************/
		
		// No se puede realizar ya que el framework no esta adecuado a subir archivos por ftp y no se tiene tampoco acceso al ftp
		
		/*
		 * Paso 3
		 *****************************************************************************************/
		addStep("Validar la ejecución de la interface OL15 en la tabla wm_log_run de WMLOG.");

		System.out.println(GlobalVariables.DB_USER_AVEBQA);
		
		System.out.println(ValidaEjecucionEnTablaWMLOG);

		SQLResult resultValidaEjecucionEnTablaWMLOG = executeQuery(dbEbs, ValidaEjecucionEnTablaWMLOG);
		
		run_id = resultValidaEjecucionEnTablaWMLOG.getData(0, "RUN_ID");
				
		testCase.addQueryEvidenceCurrentStep(resultValidaEjecucionEnTablaWMLOG);

		boolean launcherEmpty = false;
		if(resultValidaEjecucionEnTablaWMLOG.getRowCount() > 0)
			launcherEmpty = true;

		assertTrue(launcherEmpty, "No existe el registro de la ejecución de la interface");
		
		/*
		 * Paso 4
		 *****************************************************************************************/
		addStep("Verificar que se ha registrado el documento recibido en la tabla legacy_envelope de ORAFIN y que a sido procesado de forma correcta y completa, status = 'L'.");

		System.out.println(GlobalVariables.DB_USER_AVEBQA);
		
		System.out.println(ValidaEjecucionEnTablaWMLOG);
		
		VerificaDocumentoRecibidoLegacy = String.format(VerificaDocumentoRecibidoLegacy,  data.get("NOMBRE_DEL_ARCHIVO"), run_id);

		SQLResult resultVerificaDocumentoRecibidoLegacy = executeQuery(dbEbs, VerificaDocumentoRecibidoLegacy);
		
		String id = resultValidaEjecucionEnTablaWMLOG.getData(0, "ID");
				
		testCase.addQueryEvidenceCurrentStep(resultVerificaDocumentoRecibidoLegacy);

		launcherEmpty = false;
		if(resultVerificaDocumentoRecibidoLegacy.getRowCount() > 0)
			launcherEmpty = true;

		assertTrue(launcherEmpty, "No existe el registro del documento en legacy_envelope.");
		
		/*
		 * Paso 5
		 *****************************************************************************************/
		addStep("Revisar en la tabla legacy_inbound_docs de ORAFIN que se haya insertado como tipo de documento POL.");

		System.out.println(GlobalVariables.DB_USER_AVEBQA);
		
		System.out.println(ValidaEjecucionEnTablaWMLOG);
		
		RevisaenLegacyquetengaPOL = String.format(RevisaenLegacyquetengaPOL,  id);

		SQLResult resultRevisaenLegacyquetengaPOL = executeQuery(dbEbs, RevisaenLegacyquetengaPOL);
				
		testCase.addQueryEvidenceCurrentStep(resultRevisaenLegacyquetengaPOL);

		launcherEmpty = false;
		if(resultRevisaenLegacyquetengaPOL.getRowCount() > 0)
			launcherEmpty = true;

		assertTrue(launcherEmpty, "No existe el registro del documento POL.");
		
		/*
		 * Paso 6
		 *****************************************************************************************/
		addStep("Verificar que se ha insertado un registro en la tabla legacy_pol_header de ORAFIN.");

		System.out.println(GlobalVariables.DB_USER_AVEBQA);
		
		System.out.println(VerificarRegistroEnLegacyPOL);
		
		VerificarRegistroEnLegacyPOL = String.format(VerificarRegistroEnLegacyPOL,  id);

		SQLResult resultVerificarRegistroEnLegacyPOL = executeQuery(dbEbs, VerificarRegistroEnLegacyPOL);
				
		testCase.addQueryEvidenceCurrentStep(resultVerificarRegistroEnLegacyPOL);

		launcherEmpty = false;
		if(resultVerificarRegistroEnLegacyPOL.getRowCount() > 0)
			launcherEmpty = true;

		assertTrue(launcherEmpty, "No existe el encabezado del documento.");
		
		/*
		 * Paso 7
		 *****************************************************************************************/
		addStep("Validar que se han insertado las líneas del documento en la tabla LEGACY_POL_DETL de ORAFIN.");

		System.out.println(GlobalVariables.DB_USER_AVEBQA);
		
		System.out.println(ValidaInsertadoLineasLegacyOrafin);
		
		ValidaInsertadoLineasLegacyOrafin = String.format(ValidaInsertadoLineasLegacyOrafin,  id);

		SQLResult resultValidaInsertadoLineasLegacyOrafin = executeQuery(dbEbs, ValidaInsertadoLineasLegacyOrafin);
				
		testCase.addQueryEvidenceCurrentStep(resultValidaInsertadoLineasLegacyOrafin);

		launcherEmpty = false;
		if(resultValidaInsertadoLineasLegacyOrafin.getRowCount() > 0)
			launcherEmpty = true;

		assertTrue(launcherEmpty, "No se Insertarán las líneas del documento.");
		
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
		return "ATC_FT_OL15_003_RecibirPolizasdePlaza";
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
