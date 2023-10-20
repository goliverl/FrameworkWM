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

public class Pr53_Verficar_Procesamiento_INBOUND_Documentos_ADJ_Traves_Trigger extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR53_001_Verificar_INBOUND_Docs_ADJ_TRIGGER(HashMap<String, String> data) throws Exception {


/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		
/** VARIABLES *********************************************************************/	

		String tdcPaso2 =	" SELECT * FROM WMLOG.WM_LOG_RUN \r\n"
				+ "WHERE INTERFACE = 'PR53INBOUNDan' \r\n"
				+ "AND STATUS='S' \r\n"
				+ "AND START_DT>=TRUNC(SYSDATE)\r\n"
				+ "ORDER BY START_DT DESC \r\n";
		
		String tdcPaso2_1 =	"SELECT * FROM WM_LOG_THREAD\r\n"
				+ "WHERE PARENT_ID = %s\r\n";
		
		String tdcPaso3 = " SELECT * FROM POS_ENVELOPE   \r\n"
				+ " WHERE RECEIVED_DATE >= TRUNC(SYSDATE)\r\n"
				+ " AND SOURCEAPP = 'PR53";
		
		String tdcPaso4= " SELECT * FROM pos_inbound_docs\r\n"
				+ " AND PE_ID= %s\r\n"
				+ " AND STATUS='I'\r\n"
				+ " AND DOC_TYPE='ADJ'\r\n"
				+ " AND TARGET_ID = %s \r\n";
				
		String tdcPaso5 = "	SELECT * FROM pos_adj\r\n"
				+ " WHERE PID_ID = %s \r\n";
		
		String tdcPaso5_1 = " SELECT * FROM POS_ADJ_DETL \r\n"
				+ " WHERE PID_ID = %s \r\n";
		
		String tdcPaso6 = " SELECT * FROM GAS_INBOUND_DOCS_OG\r\n"
				+ " WHERE STATUS = 'P' \r\n"
				+ " AND LAST_UPDATE_DATE >= CONVERT (datetime, CONVERT(VARCHAR(10), GETDATE(), 103))\r\n"
				+ " AND LAST_UPDATE_BY = 'OXXO' \r\n"
				+ " AND TARGET_ID = %s \r\n"
				+ " AND ID = %s \r\n";


		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/
         
		System.out.println("PAso1");

		addStep("Insertar información en la tabla GAS_INBOUND_DOCS_OG del tipo de documento ADJ con status L");
		
		addStep("Insertar información del header y detalle del tipo de documento REC en las tablas GAS_ADJ_OG y GAS_ADJ_DETL_OG.\r\n");

		/* PASO 2 *********************************************************************/

		addStep("Verificar el status de la Interface en WMLOG.\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String runid = "";
		System.out.println(tdcPaso2);
		SQLResult Paso2 = dbLog.executeQuery(tdcPaso2);
		boolean ValPaso2 = Paso2.isEmpty();

		if (!ValPaso2) {

			runid = Paso2.getData(0,"RUN_ID");
			testCase.addQueryEvidenceCurrentStep(Paso2);
		}

		assertFalse(ValPaso2, "No se obtiene informacion de la consulta");

		
		/* PASO 2_1 *********************************************************************/

		addStep("Verificar el status de la Interface en WMLOG.\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso2_1 = String.format(tdcPaso2_1,runid);
		System.out.println(FormatoPaso2_1);
		SQLResult Paso2_1 = dbLog.executeQuery(FormatoPaso2_1);
		boolean ValPaso2_1 = Paso2_1.isEmpty();

		if (!ValPaso2_1) {

			testCase.addQueryEvidenceCurrentStep(Paso2_1);
		}

		assertFalse(ValPaso2_1, "No se obtiene informacion de la consulta");

//PAso 3 ************************************************************************************************************************************					

		addStep("Verificar que se haya insertado la información en la tabla POS_ENVELOPE");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String id = "";
		
		System.out.println(tdcPaso3);
		SQLResult Paso3 = dbLog.executeQuery(tdcPaso3);
		boolean ValPaso3 = Paso3.isEmpty();

		if (!ValPaso3) 
		{

			id = Paso3.getData(0, "ID");
			testCase.addQueryEvidenceCurrentStep(Paso3);
		}

		assertFalse(ValPaso3, "No se obtiene informacion de la consulta");

		/* PASO 4 *********************************************************************/

		addStep("Verificar que se haya insertado la información en la tabla POS_INBOUND_DOCS\r\n");
				
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		
		String ID = ""; 
		
		String FormatoPaso4 = String.format(tdcPaso4, id,runid);
		
		System.out.println(FormatoPaso4);
		SQLResult Paso4 = dbPos.executeQuery(FormatoPaso4);

		boolean ValPaso4 = Paso4.isEmpty();

		if (!ValPaso4) {

			ID = Paso4.getData(0,"ID");
			testCase.addQueryEvidenceCurrentStep(Paso4);

		}

		assertFalse(ValPaso4, "No se tiene informacion en la base de datos");



		/* PASO 5 *********************************************************************/

		addStep("Verificar que se inserten los datos en GAS_INBOUND_DOCS\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso5 = String.format(tdcPaso5, ID);
		System.out.println(FormatoPaso5);
		SQLResult Paso5 = dbPos.executeQuery(FormatoPaso5);

		boolean ValPaso5 = Paso5.isEmpty();

		if (!ValPaso5) {
			testCase.addQueryEvidenceCurrentStep(Paso5);

		}

		System.out.println(ValPaso5);
		assertFalse(ValPaso5, "No se obtiene informacion de la consulta");
		
		/* PASO 5_1 *********************************************************************/

		addStep("Verificar que se inserten los datos en GAS_INBOUND_DOCS\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso5_1 = String.format(tdcPaso5_1, ID);
		System.out.println(FormatoPaso5_1);
		SQLResult Paso5_1 = dbPos.executeQuery(FormatoPaso5_1);

		boolean ValPaso5_1 = Paso5_1.isEmpty();

		if (!ValPaso5_1) {
			testCase.addQueryEvidenceCurrentStep(Paso5_1);

		}

		System.out.println(ValPaso5_1);
		assertFalse(ValPaso5_1, "No se obtiene informacion de la consulta");

		/* PASO 6 *********************************************************************/

		addStep("Verificar que se haya actualizado los registros procesados a P en GAS_INBOUND_DOCS_OG");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso6 = String.format(tdcPaso6, runid,id);
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
		return "Construido. Que la interface termine satisfactoriamente y que la información sea procesada "
				+ "y que se inserte la información en las tablas "
				+ "POS_ENVELOPE, POS_INBOUND_DOCS, POS_ADJ y POS_ADJ_DETL. "
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
		return "ATC_FT_PR53_001_Verificar_INBOUND_Docs_ADJ_TRIGGER";
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
