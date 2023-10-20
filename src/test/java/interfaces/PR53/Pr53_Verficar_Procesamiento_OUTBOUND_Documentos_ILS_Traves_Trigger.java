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

public class Pr53_Verficar_Procesamiento_OUTBOUND_Documentos_ILS_Traves_Trigger extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR53_015_Verificar_OUTBOUND_Docs_ILS_TRIGGER(HashMap<String, String> data) throws Exception {


/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		
/** VARIABLES *********************************************************************/	

		String tdcPaso2 =	" SELECT * FROM gas_header_prov\r\n"
				+ "WHERE GOD_ID = '%s' \r\n";
				
		String tdcPaso2_1 =	"SELECT * FROM gas_detl_prov\r\n"
				+ "WHERE GOD_ID = %s \r\n";
		
		String tdcPaso3 = "SELECT * FROM WM_LOG_RUN \r\n"
				+ "WHERE INTERFACE = 'PR53OUTBOUNDan'\r\n"
				+ "AND STATUS='S'\r\n"
				+ "AND START_DT>=TRUNC(SYSDATE)\r\n"
				+ "ORDER BY START_DT DESC;\r\n";
			
		String tdcPaso3_1 = "SELECT * from WM_LOG_THREAD\r\n"
				+ "where PARENT_ID = %s \r\n";
				
		String tdcPaso4= "SELECT * FROM GAS_OUTBOUND_DOCS_OG\r\n"
				+ "WHERE POE_ID = %s\r\n"
				+ "AND STATUS = 'I'\r\n"
				+ "AND DOC_TYPE = %s \r\n"
				+ "AND TARGET_TYPE = %s \r\n"
				+ "AND SOURCE_ID= %S\r\n";
				
		String tdcPaso5 = "	SELECT * FROM GAS_HEADER_PROV_OG \r\n"
				+ "WHERE GOD_ID = %s \r\n";
		
		String tdcPaso5_1 = " SELECT * FROM GAS_DETL_PROV_OG\r\n"
				+ "WHERE GOD_ID = %S\r\n";
				
		String tdcPaso6 = " SELECT * FROM Gas_outBound_Docs\r\n"
				+ "where Status = 'P'\r\n"
				+ "AND DOC_TYPE = 'ILS'\r\n"
				+ "AND poe_id = %s\r\n"
				+ "AND date_created >= TRUNC(sysdate)\r\n"
				+ "AND sourceApp = 'PR53';\r\n";
		
		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		System.out.println("PAso1");

		addStep("Insertar un registro en la tabla Gas_OutBound_Docs del tipo de documento ILS con status L.\r\n");

		/* PASO 2 *********************************************************************/

		addStep("Tener el detalle del documento gas_header_prov y gas_detl_prov\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(tdcPaso2);
		SQLResult Paso2 = dbPos.executeQuery(tdcPaso2);
		boolean ValPaso2 = Paso2.isEmpty();

		if (!ValPaso2) {

			testCase.addQueryEvidenceCurrentStep(Paso2);
		}

		assertFalse(ValPaso2, "No se obtiene informacion de la consulta");

		/*
		 * PASO 2_1
		 *********************************************************************/

		addStep("Tener el detalle del documento gas_header_prov y gas_detl_prov\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		System.out.println(tdcPaso2_1);
		SQLResult Paso2_1 = dbPos.executeQuery(tdcPaso2_1);
		boolean ValPaso2_1 = Paso2_1.isEmpty();

		if (!ValPaso2_1) {

			testCase.addQueryEvidenceCurrentStep(Paso2_1);
		}

		assertFalse(ValPaso2_1, "No se obtiene informacion de la consulta");

		// PAso 3
		// ************************************************************************************************************************************

		addStep("Verificar que se haya insertado la información en la tabla POS_ENVELOPE");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String runid = "";

		System.out.println(tdcPaso3);
		SQLResult Paso3 = dbLog.executeQuery(tdcPaso3);
		boolean ValPaso3 = Paso3.isEmpty();

		if (!ValPaso3) {

			runid = Paso3.getData(0, "run_id");
			testCase.addQueryEvidenceCurrentStep(Paso3);
		}

		assertFalse(ValPaso3, "No se obtiene informacion de la consulta");

		// PAso 3_1
		// ************************************************************************************************************************************

		addStep("Verificar que se haya insertado la información en la tabla POS_ENVELOPE");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso3_1 = String.format(tdcPaso3_1, runid);

		System.out.println(FormatoPaso3_1);
		SQLResult Paso3_1 = dbLog.executeQuery(FormatoPaso3_1);
		boolean ValPaso3_1 = Paso3_1.isEmpty();

		if (!ValPaso3_1) {

			testCase.addQueryEvidenceCurrentStep(Paso3_1);
		}

		assertFalse(ValPaso3_1, "No se obtiene informacion de la consulta");
		
		/* PASO 4 *********************************************************************/

		addStep("Verificar que se hayan insertado registros en la tabla GAS_OUTBOUND_DOCS_OG\r\n");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		String id = "";
		System.out.println(tdcPaso4);
		SQLResult Paso4 = dbPos.executeQuery(tdcPaso4);

		boolean ValPaso4 = Paso4.isEmpty();

		if (!ValPaso4) {

			id = Paso4.getData(0, "ID");
			testCase.addQueryEvidenceCurrentStep(Paso4);

		}

		assertFalse(ValPaso4, "No se tiene informacion en la base de datos");

		/* PASO 5 *********************************************************************/

		addStep("Verificar que se inserten registros en la tabla GAS_HEADER_PROV_OG y GAS_DETL_PROV_OG\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso5 = String.format(tdcPaso5, id);
		System.out.println(FormatoPaso5);
		SQLResult Paso5 = dbPos.executeQuery(FormatoPaso5);

		boolean ValPaso5 = Paso5.isEmpty();

		if (!ValPaso5) {
			testCase.addQueryEvidenceCurrentStep(Paso5);

		}

		System.out.println(ValPaso5);
		assertFalse(ValPaso5, "No se obtiene informacion de la consulta");

		/*
		 * PASO 5_1
		 *********************************************************************/

		addStep("Verificar que se inserten registros en la tabla GAS_HEADER_PROV_OG y GAS_DETL_PROV_OG\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso5_1 = String.format(tdcPaso5_1, id);
		System.out.println(FormatoPaso5_1);
		SQLResult Paso5_1 = dbPos.executeQuery(FormatoPaso5_1);

		boolean ValPaso5_1 = Paso5_1.isEmpty();

		if (!ValPaso5_1) {
			testCase.addQueryEvidenceCurrentStep(Paso5_1);

		}

		System.out.println(ValPaso5_1);
		assertFalse(ValPaso5_1, "No se obtiene informacion de la consulta");

		/* PASO 6 *********************************************************************/

		addStep("Verificar que los registros procesados de la tabla Gas_OutBound_Docs sean actualizados a P.\r\n");

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

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Que la interface termine satisfactoriamente y que la información sea procesada y que se inserte la información en las tablas GAS_HEADER_PROV_OG y GAS_DETL_PROV_OG. "
				+ "Así como la actualización en la tabla GAS_OUTBOUNDS_DOCS";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAautomation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR53_015_Verificar_OUTBOUND_Docs_ILS_TRIGGER";
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
