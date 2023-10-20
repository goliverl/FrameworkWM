package interfaces.tpebus;

import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import org.testng.annotations.Test;

import com.jcraft.jsch.JSchException;

import integrationServer.om.AdaptersPoolConection;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.PE6;
import util.GlobalVariables;
import util.SSHConnector;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class TPEBUSPreRequisitos extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void test(HashMap<String, String> data) throws Exception {
		utils.sql.SQLUtil db = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE,
				GlobalVariables.DB_PASSWORD_FCTPE);
		PE6 pe6Util = new PE6(data, testCase, null);

		// Consultas
		String Query = "SELECT APPLICATION, ENTITY, OPERATION, CODE, CATEGORY, VALUE1, DESCRIPTION "
				+ "FROM TPEUSER.TPE_FR_CONFIG "
				+ "WHERE APPLICATION='BUS' "
				+ "AND ENTITY='TICKET' "
				+ "AND CODE LIKE ('%SENDA%') "
				+ "AND OPERATION IN ('TRN02','QRY01','QRY02','QRY03','REVTRN02')";

		String Query2 = "SELECT ID, CATEGORY, KEY, NAME, DESCRIPTION, PARENT, ATTR1, ATTR2"
				+ " FROM TPEUSER.TPE_BUS_CATALOG" + " WHERE PARENT = '1'" + " AND UPPER(CATEGORY) = UPPER('seguridad')";
		
		String Query3 = "select * from TPEUSER.TPE_BUS_REF" + 
				" where origen = '%s'" + 
				" and creation_date >= TRUNC(SYSDATE - 15)" + 
				" ORDER BY CREATION_DATE DESC";
		
		String origen = data.get("Origen");
		String QRY01 = data.get("QRY01");
		String QRY02 = data.get("QRY02");
		String QRY03 = data.get("QRY03");
		String TRN02 = data.get("TRN02");
		String REVTRN02 = data.get("REVTRN02");
		

		testCase.setProject_Name("Configuracion de ambientes.");

		// ************************************ Pasos
		// ******************************************************************************************

		// ********************* PASO 1
		// **********************************************************************************************************
		addStep("RESUMEN"); 
		testCase.addBoldTextEvidenceCurrentStep("Conectarse a la base de datos FCTPE.");
		testCase.addTextEvidenceCurrentStep("Conexion a bd correcta: " + GlobalVariables.DB_HOST_FCTPE + " usuario: "+ GlobalVariables.DB_USER_FCTPE);
		testCase.addTextEvidenceCurrentStep("Conexion exitosa: " + db.getConn());

		// ********************* PASO 2
		// **********************************************************************************************************
		testCase.addBoldTextEvidenceCurrentStep("Verificar que el servidor este apuntando a DevTest");

		SQLResult step1 = executeQuery(db, Query);
		String devtest1 = step1.getData(0, "VALUE1");
		String devtest2 = step1.getData(1, "VALUE1");
		String devtest3 = step1.getData(2, "VALUE1");
		String devtest4 = step1.getData(3, "VALUE1");
		String devtest5 = step1.getData(4, "VALUE1");
		
		System.out.print(Query);

		boolean validationQuery = step1.isEmpty();
		if (!validationQuery) {
			testCase.addTextEvidenceCurrentStep("-La configuración se encuentra correctamente");
		}
		
		if (QRY01.equals(devtest1)) {
			
			testCase.addTextEvidenceCurrentStep("-QRY01 Esta apuntando a Dev Test SENDA");
		}
		
		if (QRY02.equals(devtest2)) {
			
			testCase.addTextEvidenceCurrentStep("-QRY02 Esta apuntando a Dev Test SENDA");
		}
		
		if (QRY03.equals(devtest3)) {
			
			testCase.addTextEvidenceCurrentStep("-QRY03 Esta apuntando a Dev Test SENDA");
		}
		
		if (TRN02.equals(devtest4)) {
			
			testCase.addTextEvidenceCurrentStep("-TRN02 Esta apuntando a Dev Test SENDA");
		}
 
		if (REVTRN02.equals(devtest5)) {
			
			testCase.addTextEvidenceCurrentStep("-REVTRN02 Esta apuntando a Dev Test SENDA");
		}



		//assertTrue(!validationQuery);

		// ********************* PASO 3
		// **********************************************************************************************************

		testCase.addBoldTextEvidenceCurrentStep("Mostrar el usuario y contraseña en la tabla TPE_BUS_CATALOG para los WS de proveedor que requieren.");
		SQLResult step2 = executeQuery(db, Query2);
		System.out.print(Query2);

		boolean validationQuery2 = step2.isEmpty();
		if (!validationQuery2) {
			//testCase.addQueryEvidenceCurrentStep(step2);
			testCase.addTextEvidenceCurrentStep("-Se encontró el usuario y contraseña en la tabla TPE_BUS_CATALOG.");

		}

		assertTrue(!validationQuery2);
		
		// ********************* PASO 4
				// **********************************************************************************************************
		
		testCase.addBoldTextEvidenceCurrentStep("Verificar que el origen "+origen+" este en el rango de 15 días.");
		
		
		String VerificaOrigen = String.format(Query3, origen);
		SQLResult step3 = executeQuery(db, VerificaOrigen);
		System.out.print(VerificaOrigen);
		
		boolean validationQuery3 = step3.isEmpty();
		
		if(!validationQuery3) {
			testCase.addTextEvidenceCurrentStep("-El origen "+origen+" esta dentro del rango de 15 días.");
		}
		
		testCase.addBoldTextEvidenceCurrentStep("EVIDENCIAS");// *********************************************************************************************************************************

		// ********************* PASO 1 ---------------------------------------------------------------------------
		// *************************************************************************************************************************************************************************************************************

		addStep("Conectarse a la base de datos FCTPE.");
		// testCase.addBoldTextEvidenceCurrentStep("Conectarse a la base de datos
		// FCTPE.");
		testCase.addTextEvidenceCurrentStep("Conexion a bd correcta: " + GlobalVariables.DB_HOST_FCTPE + " usuario: "
				+ GlobalVariables.DB_USER_FCTPE);
		testCase.addTextEvidenceCurrentStep("Conexion exitosa: " + db.getConn());

		// ********************* PASO 2
		// **********************************************************************************************************
		addStep("Consulta para verificar que la configuración se encuentre dada de alta.");

		if (!validationQuery) {
			testCase.addQueryEvidenceCurrentStep(step1);
			testCase.addTextEvidenceCurrentStep("-La configuración se encuentra correctamente");
		}
		if (QRY01.equals(devtest1)) {
			
			testCase.addTextEvidenceCurrentStep("-QRY01 Esta apuntando a Dev Test SENDA");
		}
		
		if (QRY02.equals(devtest2)) {
			
			testCase.addTextEvidenceCurrentStep("-QRY02 Esta apuntando a Dev Test SENDA");
		}
		
		if (QRY03.equals(devtest3)) {
			
			testCase.addTextEvidenceCurrentStep("-QRY03 Esta apuntando a Dev Test SENDA");
		}
		
		if (TRN02.equals(devtest4)) {
			
			testCase.addTextEvidenceCurrentStep("-TRN02 Esta apuntando a Dev Test SENDA");
		}
 
		if (REVTRN02.equals(devtest5)) {
			
			testCase.addTextEvidenceCurrentStep("-REVTRN02 Esta apuntando a Dev Test SENDA");
		}

		//assertTrue(!validationQuery);

		// ********************* PASO 3
		// **********************************************************************************************************

		addStep("Mostrar el usuario y contraseña en la tabla TPE_BUS_CATALOG para los WS de proveedor que requieren.");
		
		if (!validationQuery2) {
			testCase.addQueryEvidenceCurrentStep(step2);
			testCase.addTextEvidenceCurrentStep("-Se encontró el usuario y contraseña en la tabla TPE_BUS_CATALOG.");

		}

		assertTrue(!validationQuery2);
		
		// ********************* PASO 4
		// **********************************************************************************************************

		addStep("Verificar que el origen "+origen+" este en el rango de 15 días.");

		if(!validationQuery3) {
			testCase.addQueryEvidenceCurrentStep(step3);
			testCase.addTextEvidenceCurrentStep("-El origen "+origen+" esta dentro del rango de 15 días.");
		}

	}

	@Override
	public String setTestFullName() {
		return "Validacion de configuracion TPE_BUS";
	}

	@Override
	public String setTestDescription() {
		return "Se verifica que la configuracion sea correcta y el origen se genere cada 15 dias.";
	}

	@Override
	public String setTestDesigner() {
		return "tbd";
	}

	@Override
	public String setTestInstanceID() {
		return "-1";
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