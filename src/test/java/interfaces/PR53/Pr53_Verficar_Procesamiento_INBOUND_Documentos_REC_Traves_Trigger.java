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

public class Pr53_Verficar_Procesamiento_INBOUND_Documentos_REC_Traves_Trigger extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR53_006_Verificar_INBOUND_Docs_REC_TRIGGER(HashMap<String, String> data) throws Exception {


/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		
/** VARIABLES *********************************************************************/	

		String tdcPaso2 =	" SELECT * FROM WM_LOG_RUN \r\n"
				+ " WHERE INTERFACE = 'PR53INBOUNDan' \r\n"
				+ " AND STATUS='S' \r\n"
				+ " AND START_DT>=TRUNC(SYSDATE) \r\n"
				+ " ORDER BY START_DT DESC \r\n";
		
		String tdcPaso2_1 =	"SELECT * FROM WM_LOG_THREAD\r\n"
				+ "WHERE PARENT_ID = %s";
			
		String tdcPaso3 = "SELECT * FROM GAS_INBOUND_DOCS\r\n"
				+ "AND status = 'I' \r\n"
				+ "AND doc_type ='REC' \r\n"
				+ "AND sourceapp = 'OG' \r\n";
		
		String tdcPaso4= " SELECT * FROM GAS_REC \r\n"
				+ " WHERE GD_ID=[GAS_INBOUND_DOCS.ID]  \r\n";
		
		String tdcPaso4_1 =" SELECT * FROM GAS_REC_DETL \r\n"
				+ " WHERE GID_ID=[GAS_INBOUND_DOCS.ID] \r\n";
				
		String tdcPaso5 = "SELECT * FROM GAS_INBOUND_DOCS_OG \r\n"
				+ " WHERE STATUS = 'P' \r\n"
				+ " AND LAST_UPDATE_DATE >= CONVERT (datetime, CONVERT(VARCHAR(10), GETDATE(), 103)) \r\n"
				+ " AND LAST_UPDATE_BY = 'OXXO' \r\n"
				+ " AND TARGET_ID = [WM_LOG_RUN.RUN_ID] \r\n"
				+ " AND ID = [GAS_INBOUND_DOCS_OG.ID] \r\n";
				
		

		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep("Insertar información en la tabla GAS_INBOUND_DOCS_OG del tipo de documento REC con status L.\r\n");

		addStep("Insertar información del header y detalle del tipo de documento REC en las tablas GAS_REC_OG y GAS_REC_DETL_OG.");

		/* PASO 2 *********************************************************************/

		addStep("Debe de haber datos en la tabla  GAS_BFC_OG\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String runid = "";

		System.out.println(tdcPaso2);
		SQLResult Paso2 = dbPos.executeQuery(tdcPaso2);
		boolean ValPaso2 = Paso2.isEmpty();

		if (!ValPaso2) {

			runid = Paso2.getData(0, "Run_id");
			testCase.addQueryEvidenceCurrentStep(Paso2);
		}

		assertFalse(ValPaso2, "No se obtiene informacion de la consulta");

		/*
		 * PASO 2_1
		 *********************************************************************/

		addStep("Debe de haber datos en la tabla  GAS_BFC_OG\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso2_1 = String.format(tdcPaso2_1, runid);

		System.out.println(FormatoPaso2_1);
		SQLResult Paso2_1 = dbPos.executeQuery(FormatoPaso2_1);
		boolean ValPaso2_1 = Paso2_1.isEmpty();

		if (!ValPaso2_1) {

			testCase.addQueryEvidenceCurrentStep(Paso2_1);
		}

		assertFalse(ValPaso2_1, "No se obtiene informacion de la consulta");

//PAso 3 ************************************************************************************************************************************					

		addStep("Verificar que se haya insertado la información en la tabla GAS_INBOUND_DOCS\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String id = "";

		System.out.println(tdcPaso3);
		SQLResult Paso3 = dbPos.executeQuery(tdcPaso3);
		boolean ValPaso3 = Paso3.isEmpty();

		if (!ValPaso3) {

			id = Paso3.getData(0, "ID");
			testCase.addQueryEvidenceCurrentStep(Paso3);
		}

		assertFalse(ValPaso2, "No se obtiene informacion de la consulta");

		/* PASO 4 *********************************************************************/

		addStep("Verificar que se haya insertado la información en la tabla GAS_REC y GAS_REC_DETL\r\n" + "");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		String FormatoPaso4 = String.format(tdcPaso4, id);

		System.out.println(FormatoPaso4);
		SQLResult Paso4 = dbLog.executeQuery(FormatoPaso4);

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

		addStep("Verificar que se haya insertado la información en la tabla GAS_REC y GAS_REC_DETL\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		String FormatoPaso4_1 = String.format(tdcPaso4_1, id);

		System.out.println(FormatoPaso4_1);
		SQLResult Paso4_1 = dbLog.executeQuery(FormatoPaso4_1);

		boolean ValPaso4_1 = Paso4.isEmpty();

		if (!ValPaso4_1) {

			testCase.addQueryEvidenceCurrentStep(Paso4_1);

		}

		assertFalse(ValPaso4_1, "No se econtro la informacion en la tabla");

		/* PASO 5 *********************************************************************/

		addStep("Verificar que se haya actualizado los registros procesados a P en GAS_INBOUND_DOCS_OG\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso5 = String.format(tdcPaso5, runid, id);
		System.out.println(FormatoPaso5);
		SQLResult Paso5 = dbPos.executeQuery(FormatoPaso5);

		boolean ValPaso5 = Paso5.isEmpty();

		if (!ValPaso5) {
			testCase.addQueryEvidenceCurrentStep(Paso5);

		}

		System.out.println(ValPaso5);
		assertFalse(ValPaso5, "No se obtiene informacion de la consulta");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Que la interface termine satisfactoriamente y que la información sea procesada y que se inserte la información en las tablas GAS_INBOUND_DOCS, GAS_REC y GAS_REC_DETL. "
				+ "Así como la actualización en la tabla GAS_INBOUND_DOCS_OG.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QAautomation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_PR53_006_Verificar_INBOUND_Docs_REC_TRIGGER";
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
