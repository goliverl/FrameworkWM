package interfaces.ol15;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import modelo.TestCase;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import java.sql.*;

public class OL15_ActualizarDocumentosPoliza extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_OL15_Actualizar_Documentos_Poliza(HashMap<String, String> data) throws Exception {

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
		String existDataToProcessInORAFINMMEX = "SELECT SUBSTR(DOC_NAME,5,2) LEGACY_EF, B.LE_ID, A.STATUS, A.DOC_NAME, B.DOC_TYPE"
				+ " FROM WMUSER.LEGACY_ENVELOPE A, WMUSER.LEGACY_INBOUND_DOCS B"
				+ " WHERE A.ID = B.LE_ID"
				+ " AND B.DOC_TYPE = 'POL'";
		
		String plazatoProcessORAFINMMEX = "SELECT DISTINCT ORACLE_CR_SUPERIOR PLAZA, ORACLE_EF, ORACLE_EF_DESC, LEGACY_EF"
				+ " FROM XXFC.XXFC_MAESTRO_DE_CRS_V" 
		        + " WHERE LEGACY_EF =  '%s'";
		
		String updateStatusRegisterLE_IDORAFINMMEX = "UPDATE WMUSER.LEGACY_INBOUND_DOCS"
				+ " SET STATUS = 'L'"
				+ " WHERE DOC_TYPE = 'POL'"
				+ " AND STATUS = 'F'"
				+ " AND LE_ID =  '%s'";
		
		String AlmacenaRegistroenWMLOG = "SELECT * FROM WM_LOG_RUN"
				+ " WHERE INTERFACE LIKE 'OL15send'"
				+ " AND STATUS = 'S'"
				+ " AND TRUNC(START_DT) >= TRUNC(SYSDATE)";
		
		String ExistenPolizasEnGL = "SELECT * FROM GL.GL_INTERFACE"
				+ " WHERE TRUNC(DATE_CREATED) = TRUNC(SYSDATE)"
				+ " AND SEGMENT3 = '%s'";
		
		String VerifyDocumentsActualizadosInLegacy = "SELECT * FROM WMUSER.LEGACY_INBOUND_DOCS"
				+ " WHERE STATUS = 'E'"
				+ " AND TRUNC(SENT_DATE) = TRUNC(SYSDATE)"
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
		//testCase.setTest_Description("Actualizar documentos de Polizas para la Plaza "+data.get("origen"));
		boolean launcherEmpty;
		String legacy_ef = "";
		String le_id = "";

		/*
		 * *****************************************************************************
		 * Pasos
		 ***********************************************************************************************/
		/*
		 * Paso 1
		 *****************************************************************************************/
		addStep("Verificar que existan datos para procesar en la BD ORAFINIMMEX");

		System.out.println(GlobalVariables.DB_HOST_AVEBQA);

		System.out.println(existDataToProcessInORAFINMMEX);

		SQLResult DataToProcessInORAFINMMEX = executeQuery(dbEbs, existDataToProcessInORAFINMMEX);
		
		legacy_ef = DataToProcessInORAFINMMEX.getData(0, "LEGACY_EF");
		
		le_id = DataToProcessInORAFINMMEX.getData(0, "LE_ID");
		
		testCase.addQueryEvidenceCurrentStep(DataToProcessInORAFINMMEX);

		launcherEmpty = false;
		if(DataToProcessInORAFINMMEX.getRowCount() > 0)
			launcherEmpty = true;

		assertTrue(launcherEmpty, "No existen datos para procesar en la base de datos ORAFINMMEX");
		
		/*
		 * Paso 2
		 *****************************************************************************************/
		addStep("Verificar cual es la Plaza que se procesara en la tabla XXFC_MAESTRO_DE_CRS_V de la BD ORAFINIMMEX.");

		System.out.println(GlobalVariables.DB_USER_AVEBQA);

		System.out.println(plazatoProcessORAFINMMEX);
		
		String plazatoProcessWithLegacyEfORAFINMMEX = String.format(plazatoProcessORAFINMMEX, legacy_ef);

		SQLResult resultPlazaProcess = executeQuery(dbEbs, plazatoProcessWithLegacyEfORAFINMMEX);
		
		String plaza = resultPlazaProcess.getData(0, "PLAZA");
        
		testCase.addQueryEvidenceCurrentStep(resultPlazaProcess);

		launcherEmpty = false;
		if(!resultPlazaProcess.isEmpty())
			launcherEmpty = true;

		assertTrue(launcherEmpty, "No existe una plaza para el LEGACY_EF que se procesa");
		
		/*
		 * Paso 3
		 *****************************************************************************************/
		addStep("Actualizar el status del registro para el LE_ID que se esta procesando en la tabla LEGACY_INBOUND_DOCS de la BD ORAFINIMMEX");

		System.out.println(GlobalVariables.DB_USER_AVEBQA);

		System.out.println(updateStatusRegisterLE_IDORAFINMMEX);
		
		String updateStatusRegisterLE = String.format(updateStatusRegisterLE_IDORAFINMMEX, le_id);
		
		int resultUpdateRegister = dbEbs.executeUpdate(updateStatusRegisterLE);

		launcherEmpty = true;
		if(resultUpdateRegister == 0)
			launcherEmpty = false;

		assertTrue(launcherEmpty, "No existe el registro de la ejecución de la interface.");
		
		/*
		 * Paso 4
		 *****************************************************************************************/
		addStep("Verificar que se almacene el registro de la ejecucion de la interface en WMLOG");

		System.out.println(GlobalVariables.DB_USER_AVEBQA);

		System.out.println(AlmacenaRegistroenWMLOG);

		SQLResult resultAlmacenaRegistro = executeQuery(dbEbs, AlmacenaRegistroenWMLOG);
		        
		testCase.addQueryEvidenceCurrentStep(resultAlmacenaRegistro);

		launcherEmpty = false;
		if(!resultAlmacenaRegistro.isEmpty())
			launcherEmpty = true;

		assertTrue(launcherEmpty, "No existe el regitro de la ejecucion de la interface con status igual a 'S'.");
	
		/*
		 * Paso 5
		 *****************************************************************************************/
		addStep("Verificar que existan registro de las polizas procesadas en la tabla GL_INTERFACE en la BD ORAFINIMMEX");

		System.out.println(GlobalVariables.DB_USER_AVEBQA);		
		
		String updateExistenPolizasEnGL = String.format(ExistenPolizasEnGL, plaza);
		
		System.out.println(updateExistenPolizasEnGL);

		SQLResult resultExistenPolizasEnGL = executeQuery(dbEbs, updateExistenPolizasEnGL);
		        
		testCase.addQueryEvidenceCurrentStep(resultExistenPolizasEnGL);

		launcherEmpty = false;
		if(resultExistenPolizasEnGL.getRowCount() > 0)
			launcherEmpty = true;

		assertTrue(launcherEmpty, "No existe el registro de las polizas procesadas.");
		
		/*
		 * Paso 6
		 *****************************************************************************************/
		addStep("Verificar que se actualizaron los documentos procesados correctamente en la tabla LEGACY_INBOUND_DOCS de la BD ORAFINIMMEX");

		System.out.println(GlobalVariables.DB_USER_AVEBQA);		
		
		String updateActualizadosInLegacy = String.format(VerifyDocumentsActualizadosInLegacy, le_id);
		
		System.out.println(updateActualizadosInLegacy);

		SQLResult resultupdateActualizadosInLegacy = executeQuery(dbEbs, updateActualizadosInLegacy);
		        
		testCase.addQueryEvidenceCurrentStep(resultupdateActualizadosInLegacy);

		launcherEmpty = false;
		if(resultupdateActualizadosInLegacy.getRowCount() > 0)
			launcherEmpty = true;

		assertTrue(launcherEmpty, "No se actualizo correctamente los documentos procesados.");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Actualizar documentos de Polizas para las plazas.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatización";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_OL15_Actualizar_Documentos_Poliza";
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
