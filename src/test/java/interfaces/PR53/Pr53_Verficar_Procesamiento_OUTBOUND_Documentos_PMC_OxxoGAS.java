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

public class Pr53_Verficar_Procesamiento_OUTBOUND_Documentos_PMC_OxxoGAS extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR53_017_Verificar_OUTBOUND_Docs_PMC_OxxoGAS(HashMap<String, String> data) throws Exception {


/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		
/** VARIABLES *********************************************************************/	

		String tdcPaso1 =	" Select Id,Doc_Type, Target_Type, Doc_Name,\r\n"
				+ " Pv_Cr_Plaza, Pv_Cr_Tienda, Status,\r\n"
				+ " Source_Id,nb_sent \r\n"
				+ " from POSREP.Gas_OutBound_Docs \r\n"
				+ " where (Status='L' or Status='W' \r\n"
				+ " or Status='R' or Status='X') \r\n"
				+ " and Doc_Type='PMC'";
		
		String tdcPaso2 = "SELECT * FROM GAS_PMC_HEADER\r\n"
				+ "WHERE GOD_ID = %s";
			
		String tdcPaso2_1 =	"SELECT * FROM GAS_PMC_HEADER\r\n"
				+ "WHERE GOD_ID = %s";
		
		String tdcPaso4 = "SELECT * FROM WM_LOG_RUN \r\n"
				+ "WHERE INTERFACE = 'PR53OUTBOUND'\r\n"
				+ "AND STATUS='S'\r\n"
				+ "AND START_DT>=TRUNC(SYSDATE)\r\n"
				+ "ORDER BY START_DT DESC;\r\n";
				
		String tdcPaso4_1= "SELECT * from WM_LOG_THREAD\r\n"
				+ "where PARENT_ID = %s\r\n"
				+ "and NAME LIKE 'PR53-O-PMC-%';\r\n";
						
		String tdcPaso5 = "	SELECT * FROM GAS_OUTBOUND_DOCS_OG\r\n"
				+ "WHERE POE_ID = %s \r\n"
				+ "AND STATUS = 'I' \r\n"
				+ "AND DOC_TYPE = %s \r\n"
				+ "AND TARGET_TYPE = %s \r\n"
				+ "AND SOURCE_ID = %s\r\n";
		
		String tdcPaso6 = " SELECT * FROM GAS_PMC_HEADER_OG \r\n"
				+ " WHERE GOD_ID = %s \r\n";
		
		String tdcPaso6_1 = " SELECT * FROM GAS_PMC_DETALLE_OG \r\n"
				+ " WHERE GOD_ID = %s \r\n";

		String tdcPaso7 = " SELECT id, POE_ID, DOC_TYPE, PV_CR_PLAZA, PV_CR_TIENDA, STATUS, DATE_CREATED, DATE_SENT, SOURCEAPP \r\n"
				+ " FROM Gas_outBound_Docs \r\n"
				+ " where POE_I D= %s \r\n"
				+ " AND Status='P' \r\n"
				+ " AND doc_Type = 'PMC' \r\n"
				+ " AND date_created >=TRUNC(sysdate) \r\n"
				+ " AND sourceApp = 'PR53' \r\n";

		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep("Tener registros en la tabla Gas_OutBound_Docs del tipo de documento PMC con status L,W,R o X\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String id = "";
		String DocType = "";
		String TagertType = "";

		System.out.println(tdcPaso1);
		SQLResult Paso1 = dbPos.executeQuery(tdcPaso1);
		boolean ValPaso1 = Paso1.isEmpty();
		if (!ValPaso1) {

			id = Paso1.getData(0, "id");
			DocType = Paso1.getData(0, "DOC_TYPE");
			TagertType = Paso1.getData(0,"Target_Type");
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}
		System.out.println(ValPaso1);
		assertFalse(ValPaso1, "No se obtiene informacion de la consulta");

		/* PASO 2 *********************************************************************/

		addStep("Tener el detalle del documento GAS_PMC_HEADER y GAS_PMC_DETALLE");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso2 = String.format(tdcPaso2, id);

		System.out.println(FormatoPaso2);
		SQLResult Paso2 = dbPos.executeQuery(FormatoPaso2);
		boolean ValPaso2 = Paso2.isEmpty();

		if (!ValPaso2) {

			testCase.addQueryEvidenceCurrentStep(Paso2);
		}

		assertFalse(ValPaso2, "No se obtiene informacion de la consulta");
		
		/* PASO 2_1 *********************************************************************/

		addStep("Tener el detalle del documento GAS_PMC_HEADER y GAS_PMC_DETALLE");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso2_1 = String.format(tdcPaso2_1, id);

		System.out.println(FormatoPaso2_1);
		SQLResult Paso2_1 = dbPos.executeQuery(FormatoPaso2_1);
		boolean ValPaso2_1 = Paso2_1.isEmpty();

		if (!ValPaso2_1) {

			testCase.addQueryEvidenceCurrentStep(Paso2_1);
		}

		assertFalse(ValPaso2_1, "No se obtiene informacion de la consulta");

//PAso 3 ************************************************************************************************************************************					

		addStep("Ejecutar el servicio PR53V1.pub:runOutBound\r\n");

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

		String FormatoPaso5 = String.format(tdcPaso5, id, DocType,TagertType,runid);
		System.out.println(FormatoPaso5);
		SQLResult Paso5 = dbPos.executeQuery(FormatoPaso5);

		boolean ValPaso5 = Paso5.isEmpty();

		if (!ValPaso5) {
			testCase.addQueryEvidenceCurrentStep(Paso5);

		}

		System.out.println(ValPaso5);
		assertFalse(ValPaso5, "No se obtiene informacion de la consulta");

		/* PASO 6 *********************************************************************/

		addStep("Verificar que se inserten registros en la tabla GAS_PMC_HEADER_OG y GAS_PMC_DETALLE_OG\r\n");

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

		
		/* PASO 6_1 *********************************************************************/

		addStep("Verificar que se inserten registros en la tabla GAS_PMC_HEADER_OG y GAS_PMC_DETALLE_OG\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso6_1 = String.format(tdcPaso6_1, id);
		System.out.println(FormatoPaso6_1);
		SQLResult Paso6_1 = dbPos.executeQuery(FormatoPaso6_1);

		boolean ValPaso6_1 = Paso6_1.isEmpty();

		if (!ValPaso6_1) {
			testCase.addQueryEvidenceCurrentStep(Paso6_1);

		}

		System.out.println(ValPaso6_1);
		assertFalse(ValPaso6_1, "No se obtiene informacion de la consulta");
		
		/* PASO 7 *********************************************************************/

		addStep("Verificar que los registros procesados de la tabla Gas_OutBound_Docs sean actualizados a P.\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso7 = String.format(tdcPaso7, id);
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
		return "Construido. Que la interface termine satisfactoriamente y que la información sea procesada y que se inserte la información en las tablas GAS_PMC_HEADER_OG y GAS_PMC_DETALLE_OG."
				+ "Así como la actualización en la tabla GAS_OUTBOUNDS_DOCS.";
			
				
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAautomation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR53_017_Verificar_OUTBOUND_Docs_PMC_OxxoGAS";
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
