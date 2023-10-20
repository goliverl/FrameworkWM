package interfaces.PR53;

import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class Pr53_Verficar_Procesamiento_INBOUND_Documentos_BFC_OxxoGAS extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR53_002_Verificar_INBOUND_Docs_BFC_OxxoGAS(HashMap<String, String> data) throws Exception {


/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		
/** VARIABLES *********************************************************************/	

		String tdcPaso1 =	" SELECT * FROM GAS_INBOUND_DOCS_OG \r\n"
				+ " WHERE STATUS IN ('L', 'R', 'W') \r\n"
				+ " AND DOC_TYPE='BFC' \r\n";
		
		String tdcPaso2 =	"SELECT * FROM GAS_BFC_OG \r\n"
				+ " WHERE gid_id = %s \r\n";
			
		String tdcPaso4 = "SELECT * FROM WM_LOG_RUN \r\n"
				+ "WHERE INTERFACE = 'PR53INBOUND' \r\n"
				+ "AND STATUS='S' \r\n"
				+ "AND START_DT>=TRUNC(SYSDATE) \r\n"
				+ "ORDER BY START_DT DESC \r\n";
		
		String tdcPaso4_1= "select * from WM_LOG_THREAD \r\n"
				+ "where PARENT_ID = %s \r\n";
				
		String tdcPaso5 = "	SELECT * FROM GAS_INBOUND_DOCS \r\n"
				+ "WHERE pv_doc_id = %S \r\n"
				+ "AND doc_type ='BFC' \r\n"
				+ "AND status = 'I' \r\n"
				+ "AND target_id = %s \r\n"
				+ "AND sourceapp = 'OG'";
		
		String tdcPaso6 = "SELECT * FROM GAS_BFC \r\n"
				+ "WHERE GID_ID = %s \r\n";

		String tdcPaso7 = "SELECT * FROM GAS_INBOUND_DOCS_OG \r\n"
				+ "WHERE STATUS = 'P' \r\n"
				+ "AND LAST_UPDATE_DATE >= CONVERT (datetime, CONVERT(VARCHAR(10), GETDATE(), 103)) \r\n"
				+ "AND LAST_UPDATE_BY = 'OXXO' \r\n"
				+ "AND TARGET_ID = %s \r\n"
				+ "AND ID = %s \r\n";

		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep("Tener registros en la tabla GAS_INBOUND_DOCS_OG de tipo BFC en status L, R o W.\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String id = "";

		System.out.println(tdcPaso1);
		SQLResult Paso1 = dbPos.executeQuery(tdcPaso1);
		boolean ValPaso1 = Paso1.isEmpty();
		if (!ValPaso1) {

			id = Paso1.getData(0, "id");
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}
		System.out.println(ValPaso1);
		assertFalse(ValPaso1, "No se obtiene informacion de la consulta");

		/* PASO 2 *********************************************************************/

		addStep("Debe de haber datos en la tabla  GAS_BFC_OG\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso2 = String.format(tdcPaso2, id);

		System.out.println(FormatoPaso2);
		SQLResult Paso2 = dbPos.executeQuery(FormatoPaso2);
		boolean ValPaso2 = Paso2.isEmpty();

		if (!ValPaso2) {

			testCase.addQueryEvidenceCurrentStep(Paso2);
		}

		assertFalse(ValPaso2, "No se obtiene informacion de la consulta");

//PAso 3 ************************************************************************************************************************************					

		addStep("Correr el servicio PR53V1.pub:runInBound.");

		SeleniumUtil u;
		PakageManagment pok;

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution " + dateExecution);

		/* PASO 4 *********************************************************************/

		addStep("Verificar el status de la Interface en WMLOG.\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		String runid = "";
		System.out.println(tdcPaso4);
		SQLResult Paso4 = dbLog.executeQuery(tdcPaso4);

		boolean ValPaso4 = Paso4.isEmpty();

		if (!ValPaso4) {

			testCase.addQueryEvidenceCurrentStep(Paso4);
			runid = Paso4.getData(0, "RUN_ID");
			System.out.println("Run_id " + runid);

		}

		assertFalse(ValPaso4, "No se tiene informacion en la base de datos");

		/*
		 * PASO 4_1
		 *********************************************************************/

		addStep("Verificar el status de la Interface en WMLOG.\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		String FormatoPaso4_1 = String.format(tdcPaso4_1, runid);

		System.out.println(FormatoPaso4_1);
		SQLResult Paso4_1 = dbLog.executeQuery(FormatoPaso4_1);

		boolean ValPaso4_1 = Paso4.isEmpty();

		if (!ValPaso4_1) {

			testCase.addQueryEvidenceCurrentStep(Paso4_1);

		}

		assertFalse(ValPaso4_1, "No se econtro la informacion en la tabla");

		/* PASO 5 *********************************************************************/

		addStep("Verificar que se inserten los datos en GAS_INBOUND_DOCS\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso5 = String.format(tdcPaso5, id, runid);
		System.out.println(FormatoPaso5);
		SQLResult Paso5 = dbPos.executeQuery(FormatoPaso5);

		boolean ValPaso5 = Paso5.isEmpty();

		if (!ValPaso5) {
			testCase.addQueryEvidenceCurrentStep(Paso5);

		}

		System.out.println(ValPaso5);
		assertFalse(ValPaso5, "No se obtiene informacion de la consulta");

		/* PASO 6 *********************************************************************/

		addStep("Verificar que se inserten los datos en GAS_INBOUND_DOCS\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso6 = String.format(tdcPaso6, id);
		System.out.println(FormatoPaso6);
		SQLResult Paso6 = dbPos.executeQuery(FormatoPaso6);

		boolean ValPaso6 = Paso6.isEmpty();

		if (!ValPaso6) {
			testCase.addQueryEvidenceCurrentStep(Paso6);

		}

		System.out.println(ValPaso6);
		assertFalse(ValPaso6, "No se obtiene informacion de la consulta");

		/* PASO 7 *********************************************************************/

		addStep("Verificar que se inserten los datos en GAS_INBOUND_DOCS\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso7 = String.format(tdcPaso7, id, runid);
		System.out.println(FormatoPaso7);
		SQLResult Paso7 = dbPos.executeQuery(FormatoPaso7);

		boolean ValPaso7 = Paso7.isEmpty();

		if (!ValPaso7) {
			testCase.addQueryEvidenceCurrentStep(Paso7);

		}

		System.out.println(ValPaso7);
		assertFalse(ValPaso7, "No se obtiene informacion de la consulta");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Que la interface termine satisfactoriamente y que la información sea procesada "
				+ "y que se inserte la información en las tablas GAS_INBOUND_DOCS, GAS_BFC. "
				+ "Así como la actualización en la tabla GAS_INBOUND_DOCS_OG";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAautomation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR53_002_Verificar_INBOUND_Docs_BFC_OxxoGAS";
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
