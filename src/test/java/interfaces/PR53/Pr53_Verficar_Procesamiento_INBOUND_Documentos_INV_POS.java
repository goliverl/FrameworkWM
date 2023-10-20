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

public class Pr53_Verficar_Procesamiento_INBOUND_Documentos_INV_POS extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_PR53_005_Verificar_INBOUND_Docs_INV_POS(HashMap<String, String> data) throws Exception {


/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		
/** VARIABLES *********************************************************************/	

		String tdcPaso1 =	" SELECT * FROM GAS_INBOUND_DOCS_OG \r\n"
				+ " WHERE STATUS IN ('L', 'R', 'W') \r\n"
				+ " AND DOC_TYPE='HEF' \r\n";
		
		String tdcPaso2 ="SELECT * FROM GAS_INV_OG\r\n"
				+ "WHERE GID_ID = %s \r\n";
		
		String tdcPaso2_1 = "SELECT * FROM GAS_INV_DETL_OG \r\n"
				+ "WHERE GID_ID = %s \r\n";
		
			
		String tdcPaso4 = "SELECT * FROM WM_LOG_RUN \r\n"
				+ "WHERE INTERFACE = 'PR53INBOUND' \r\n"
				+ "AND STATUS='S' \r\n"
				+ "AND START_DT>=TRUNC(SYSDATE) \r\n"
				+ "ORDER BY START_DT DESC \r\n";
		
		String tdcPaso4_1= "select * from WM_LOG_THREAD\r\n"
				+ " where PARENT_ID = %s \r\n" ;
				
				
		String tdcPaso5 = "SELECT * FROM POS_ENVELOPE   \r\n"
				+ "WHERE PV_CR_PLAZA = [SUBSTR(%s,3,5)]\r\n"
				+ "AND PV_CR_TIENDA = [SUBSTR(%s,9,5)]\r\n"
				+ "AND PV_ENVELOPE_ID = %s \r\n"
				+ "AND RECEIVED_DATE >= TRUNC(SYSDATE)\r\n"
				+ "AND SOURCEAPP='PR53'";
		
		String tdcPaso6 = "SELECT * FROM pos_inbound_docs\r\n"
				+ "AND PE_ID = [POS_ENVELOPE.ID]\r\n"
				+ "AND STATUS = 'I'\r\n"
				+ "AND DOC_TYPE = 'INV'\r\n"
				+ "AND TARGET_ID = %S;";

		String tdcPaso7 = "SELECT * FROM POS_INV \r\n"
				+ "WHERE PID_ID = %s \r\n";
			   
		
		String tdcPaso7_1 =  "SELECT * FROM POS_INV_DETL\r\n"
				+ "WHERE PID_ID= %s \r\n";
		
		String tdcPaso8 = "SELECT * FROM GAS_INBOUND_DOCS_OG \r\n"
				+ "WHERE STATUS = 'P' \r\n"
				+ "AND LAST_UPDATE_DATE >= CONVERT (datetime, CONVERT(VARCHAR(10), GETDATE(), 103)) \r\n"
				+ "AND LAST_UPDATE_BY = 'OXXO' \r\n"
				+ "AND TARGET_ID = %s \r\n"
				+ "AND ID = %s";
			

		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep("Tener registros en la tabla GAS_INBOUND_DOCS_OG de tipo INV en status L, R o W.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String id = "";
		String name = "";

		System.out.println(tdcPaso1);
		SQLResult Paso1 = dbPos.executeQuery(tdcPaso1);
		boolean ValPaso1 = Paso1.isEmpty();
		if (!ValPaso1) {

			id = Paso1.getData(0, "id");
			name = Paso1.getData(0, "PV_DOC_NAME");
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}
		System.out.println(ValPaso1);
		assertFalse(ValPaso1, "No se obtiene informacion de la consulta");

		/* PASO 2 *********************************************************************/

		addStep("Tener el detalle del documento GAS_INV_OG y GAS_INV_DETL_OG\r\n");

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

		addStep("Tener el detalle del documento GAS_HEF_OG y GAS_HEF_DETL_OG\r\n");

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

		addStep("Verificar que se haya insertado la información en la tabla POS_ENVELOPE\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String ID = "";
		
		String FormatoPaso5 = String.format(tdcPaso5, name, name,id);
		System.out.println(FormatoPaso5);
		SQLResult Paso5 = dbPos.executeQuery(FormatoPaso5);

		boolean ValPaso5 = Paso5.isEmpty();

		if (!ValPaso5) {
			
			ID = Paso4.getData(0,"ID");
			testCase.addQueryEvidenceCurrentStep(Paso5);

		}

		System.out.println(ValPaso5);
		assertFalse(ValPaso5, "No se obtiene informacion de la consulta");

		/* PASO 6 *********************************************************************/

		addStep("Verificar que se haya insertado la información en la tabla POS_INBOUND_DOCS ");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		
		String FormatoPaso6 = String.format(tdcPaso6, ID,runid);
		System.out.println(FormatoPaso6);
		SQLResult Paso6 = dbPos.executeQuery(FormatoPaso6);

		boolean ValPaso6 = Paso6.isEmpty();

		if (!ValPaso6) {
			testCase.addQueryEvidenceCurrentStep(Paso6);

		}

		System.out.println(ValPaso6);
		assertFalse(ValPaso6, "No se obtiene informacion de la consulta");

		/* PASO 7 *********************************************************************/

		addStep("Verificar que se haya insertado la información en la tabla POS_INV y POS_INV_DETL\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso7 = String.format(tdcPaso7, ID);
		System.out.println(FormatoPaso7);
		SQLResult Paso7 = dbPos.executeQuery(FormatoPaso7);

		boolean ValPaso7 = Paso7.isEmpty();

		if (!ValPaso7) {
			testCase.addQueryEvidenceCurrentStep(Paso7);

		}

		System.out.println(ValPaso7);
		assertFalse(ValPaso7, "No se obtiene informacion de la consulta");
		
		/* PASO 7_1 *********************************************************************/

		addStep("Verificar que se haya insertado la información en la tabla POS_HEF y POS_HEF_DETL\r\n" );

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso7_1 = String.format(tdcPaso7_1, ID);
		System.out.println(FormatoPaso7_1);
		SQLResult Paso7_1 = dbPos.executeQuery(FormatoPaso7_1);

		boolean ValPaso7_1= Paso7_1.isEmpty();

		if (!ValPaso7_1) {
			testCase.addQueryEvidenceCurrentStep(Paso7_1);

		}

		System.out.println(ValPaso7_1);
		assertFalse(ValPaso7_1, "No se obtiene informacion de la consulta");
		
		/* PASO 8 *********************************************************************/

		addStep("Verificar que se haya actualizado los registros procesados a P en GAS_INBOUND_DOCS_OG \r\n");
				

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String FormatoPaso8 = String.format(tdcPaso8,runid, ID);
		System.out.println(FormatoPaso8);
		SQLResult Paso8 = dbPos.executeQuery(FormatoPaso8);

		boolean ValPaso8 = Paso8.isEmpty();

		if (!ValPaso8) {
			testCase.addQueryEvidenceCurrentStep(Paso8);

		}

		System.out.println(ValPaso8);
		assertFalse(ValPaso8, "No se obtiene informacion de la consulta");
		

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Que la interface termine satisfactoriamente y que la información sea procesada y que se inserte la información en las tablas POS_ENVELOPE, POS_INBOUND_DOCS, POS_INV y POS_INV_DETL. "
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
		return "ATC_FT_PR53_005_Verificar_INBOUND_Docs_INV_POS";
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
