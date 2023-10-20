package interfaces.pi1;

import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class PI1_ValidarEnvioDeArticulosParaSurtirDePosAImmex extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PI1_PI1_ValidarEnvioDeArticulosParaSurtirDePosAImmex(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbFCRMSSIT = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Rms,	GlobalVariables.DB_HOST_Rms, GlobalVariables.DB_HOST_Rms);
		utils.sql.SQLUtil dbFCWMQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		/*
		 * Variables
		 *************************************************************************/

		String tdcQueryPaso1 = " SELECT DISTINCT CEDIS, PLAZA FROM XXFC.XXFC_PEDIDOS_EXT_PLAZAS " 
		        + " WHERE PLAZA = '"+ data.get("plaza") + "'" 
				+ " AND CEDIS = '" + data.get("CEDIS") + "'" 
		        + " ORDER BY CEDIS";

		String tdcQueryPaso2 = "SELECT DISTINCT C.CEDIS_ID, A.PV_CR_PLAZA, A.PV_CR_TIENDA, C.EXT_REF_NO, "
				+ " TO_CHAR(C.SHIP_DATE, 'YYYYMMDD') AS SHIP_DATE, B.ID, M.CR_PLAZA_DESC, T.DESCRIPTION, RPAD(D.ITEM,25) AS ITEM, D.SHIP_QTY " 
				+ " FROM " 
				+ " POSUSER.POS_ENVELOPE A, "
				+ " POSUSER.POS_INBOUND_DOCS B, " 
				+ " POSUSER.POS_TSF C, " 
				+ " POSUSER.POS_TSF_DETL D, "
				+ " POSUSER.PEDIDOS_EXT_PLAZAS_ITEMS_V P, " 
				+ " POSUSER.PLAZAS M , " 
				+ " POSUSER.TIENDAS T "
				+ " WHERE B.DOC_TYPE = 'TSF' " 
				+ " AND B.STATUS = 'C' " 
				+ " AND B.PARTITION_DATE < SYSDATE -20 "
				+ " AND C.PID_ID = B.ID " 
				+ " AND C.CEDIS_ID = %s "
				+ " AND C.SHIP_DATE BETWEEN P.PLAZA_VIG_INI AND P.PLAZA_VIG_FIN "
				+ " AND C.SHIP_DATE BETWEEN P.ITEM_VIG_INI AND P.ITEM_VIG_FIN " 
				+ " AND D.PID_ID = B.ID "
				+ " AND P.ITEM = TRIM( D.ITEM ) " 
				+ " AND A.ID = B.PE_ID " 
				+ " AND P.PLAZA = A.PV_CR_PLAZA "
				+ " AND M.CR_PLAZA = A.PV_CR_PLAZA " 
				+ " AND T.PV_CR_PLAZA = A.PV_CR_PLAZA "
				+ " AND T.PV_CR_TIENDA = A.PV_CR_TIENDA " 
				+ " AND p.plaza = substr(pv_doc_name, 4, 5) "
				+ " AND m.cr_plaza = substr(pv_doc_name, 4, 5) " 
				+ " AND t.pv_cr_plaza = substr(pv_doc_name, 4, 5) "
				+ " AND t.pv_cr_tienda = substr(pv_doc_name, 9, 5) " 
				+ " AND P.PLAZA =  '%s' ";

		String tdcQueryPaso3 = " SELECT DISTINCT C.CEDIS_ID, B.PV_CR_PLAZA, B.PV_CR_TIENDA, C.EXT_REF_NO, "
				+ " TO_CHAR( C.SHIP_DATE, 'YYYYMMDD') AS SHIP_DATE, RPAD( A.ITEM, 25 ) AS ITEM, A.RD_QTY,B.PC_ID, B.STORE "
				+ " FROM XXFC.XXFC_PC_TSF_DETAIL A"
				+ " JOIN XXFC.XXFC_PC_TSF B ON A.PC_ID = B.PC_ID AND A.STORE = B.STORE "
				+ " JOIN XXFC.XXFC_PC_MASTER C ON B.PC_ID = C.PC_ID "
				+ " JOIN XXFC.XXFC_PEDIDOS_EXT_PLAZAS D ON B.PV_CR_PLAZA = D.PLAZA "
				+ " JOIN XXFC.XXFC_PEDIDOS_EXT_ITEMS E ON D.PLAZA = E.PLAZA AND A.ITEM = E.ITEM "
				+ " WHERE C.SHIP_DATE BETWEEN D.FCH_VIG_INI AND D.FCH_VIG_FIN "
				+ " AND C.SHIP_DATE BETWEEN E.FCH_VIG_INI AND E.FCH_VIG_FIN " 
				+ " AND B.STATUS = 'C' "
				+ " AND E.TIPO_MOV = 2 " 
				+ " AND C.CEDIS_ID = %s" 
				+ " AND D.PLAZA = ' %s ' ";

		String tdcQueryPaso4 = " SELECT A.TSF_NO, RPAD(B.DEFAULT_ROUTE,10) "
				+ " FROM RMS100.TSFHEAD A, XXFC.SHIP_DEST B" 
				+ " WHERE A.FROM_LOC_TYPE = 'W' "
				+ " AND A.TO_LOC_TYPE = 'S'" 
				+ " AND A.EXT_REF_NO = ' %s '  " 
				+ " AND A.TO_LOC = 10429 " // No se tiene en para este campo en la tablas.																																																				
				+ " AND B.DEST_ID = to_char(A.TO_LOC) AND B.FACILITY_ID = 'PR' ";

		String tdcQueryPaso5 = " SELECT * FROM WMUSER.WM_FTP_CONNECTIONS " 
		        + " WHERE FTP_CONN_ID = 'IMMEX_%s' ";

		String tdcQueryPaso7 = " SELECT * FROM WMLOG.WM_LOG_RUN " 
		        + " WHERE INTERFACE = 'PI1' " 
				+ " AND STATUS='S' "
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE) " 
				+ " ORDER BY START_DT DESC";

		String tdcQueryPaso8 = " SELECT * FROM SAPUSER.LEGACY_OUTBOUND_DOCS" 
		        + " WHERE DOC_TYPE = 'PI'"
				+ " AND LEGACY_USER = 'IMMEX'" 
		        + " AND RUN_ID = %s";

		String tdcQueryPaso10 = " SELECT * FROM POSUSER.POS_INBOUND_DOCS" 
		        + " WHERE DOC_TYPE = 'TSF'"
				+ " AND STATUS = 'E'" 
		        + " AND ID = %S";

		String tdcQueryPaso11 = " SELECT * from XXFC.XXFC_PC_TSF" 
		        + " where PC_ID = %s  " 
				+ " AND  STORE = %s"
				+ " STATUS = 'E'";

		SeleniumUtil u;
		PakageManagment pok;
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String interface1 = data.get("interfase");
		String Servicio = "PI1.Pub:run";

		/*
		 * Pasos
		 *****************************************************************************/

		/// Paso 1 ***************************************************

		System.out.print("Paso 1");

		addStep("Validar que exista información de la PLAZA y CEDIS en la tabla XXFC.XXFC_PEDIDOS_EXT_PLAZAS de la BD RETEK");

		System.out.println(GlobalVariables.DB_HOST_Rms);

		System.out.println(tdcQueryPaso1);

		SQLResult Paso1 = dbFCRMSSIT.executeQuery(tdcQueryPaso1);

		String plaza = Paso1.getData(0, "plaza");
		String cedis = Paso1.getData(0, "cedis");

		System.out.println(plaza);
		System.out.println(cedis);

		boolean Paso1Empty = Paso1.isEmpty();

		System.out.println(Paso1Empty);

		if (!Paso1Empty) {

			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

		assertFalse(Paso1Empty, "No se tiene informacion en la base datos");

		/// Paso 2 ***************************************************

		System.out.print("Paso 2");

		addStep("Validar que existan registros a procesar en las tablas de de BD POSUSER");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		String Paso2Format = String.format(tdcQueryPaso2, cedis, plaza);
		System.out.println(Paso2Format);
		SQLResult paso2 = dbFCWMQA.executeQuery(Paso2Format);

		String EXTREFNO = paso2.getData(0, "EXT_REF_NO");
		String INBOID = paso2.getData(0, "ID");

		boolean Paso2Empty = paso2.isEmpty();

		System.out.println(Paso2Empty);

		if (!Paso2Empty) {

			testCase.addQueryEvidenceCurrentStep(paso2);
		}
		assertFalse(Paso2Empty, "No se tiene informacion en la base de datos");

		/// Paso 3 ***************************************************

		System.out.print("Paso 3");

		addStep("Validar que existan pedidos centralizados en RETEK.");

		System.out.println(GlobalVariables.DB_HOST_Rms);
		String Paso3Format = String.format(tdcQueryPaso3, cedis, plaza);
		System.out.println(Paso3Format);
		SQLResult paso3 = dbFCRMSSIT.executeQuery(Paso3Format);

		String PC_ID = paso3.getData(0, "PC_ID");
		String STORE = paso3.getData(0, "STORE");

		boolean Paso3Empty = paso3.isEmpty();

		System.out.println(Paso3Empty);

		if (!Paso3Empty) {

			testCase.addQueryEvidenceCurrentStep(paso2);
		}

		assertFalse(Paso3Empty, "No se tiene informacion en la base de datos");

		/// Paso 4 *****************************************************

		System.out.print("Paso 4");

		addStep(" Validar que la tienda aprocesar exista en las tablas TSFHEAD, SHIP_DEST de BD RETEK");

		System.out.println(GlobalVariables.DB_HOST_Rms);

		String Formato4 = String.format(tdcQueryPaso4, EXTREFNO); // Se necesita agregar este campo ,EXTREFNO para el
																	// formato del query
		System.out.println(Formato4);

		SQLResult Paso4 = executeQuery(dbFCRMSSIT, Formato4);

		boolean Paso4Empty = Paso4.isEmpty();

		System.out.println(Paso4Empty);

		if (!Paso4Empty) {

			testCase.addQueryEvidenceCurrentStep(Paso4);
		}

		assertFalse(Paso4Empty, "No se tiene informacion en la base datos");

		// Paso 5 ******************************************************

		addStep("Validar la configación del servidor FTP para el CEDIS a procesar en la BD WMINT.");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		String Formato5 = String.format(tdcQueryPaso5, cedis);

		SQLResult Paso5 = executeQuery(dbFCWMQA, tdcQueryPaso5);
		System.out.println(Formato5);

		Boolean Paso5Empty = Paso5.isEmpty();
		System.out.print(Paso5Empty);

		if (!Paso5Empty) {

			testCase.addQueryEvidenceCurrentStep(Paso5);
		}

		assertFalse(Paso5Empty, "No se tiene informacion en la base datos");

		/// Paso 6 ***************************************************

		addStep("Ejecutar el servicio PI1.Pub:run con el job PI1 de Ctrl-M ");

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		System.out.print(contra);
		System.out.print(interface1);
		System.out.print(Servicio);
		u.get(contra);
		u.hardWait(4);
		pok.runIntefaceWmOneButton(interface1, Servicio);

		/// Paso 7 ***************************************************

		addStep("Validar la correcta ejecución de la interface PI1 en la tabla WM WM_LOG_RUN de WMLOG..");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);

		SQLResult Paso7 = executeQuery(dbLog, tdcQueryPaso7);

		String Runid = Paso7.getData(0, "RUN_ID");

		System.out.println(tdcQueryPaso7);

		boolean Paso7Empty = Paso7.isEmpty();

		System.out.println(Paso7Empty);

		if (!Paso7Empty) {

			testCase.addQueryEvidenceCurrentStep(Paso7);
		}

		assertFalse(Paso7Empty, "No se tiene informacion en la base datos");

		/// Paso 8 ***************************************************

		System.out.print("Paso 8");

		addStep("Validar que se inserte la información del archivo generado en la tabla LEGACY_OUTBOUND_DOCS de la BD LEGUSER con STATUS = 'L'.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.print("\n");
		String Formato8 = String.format(tdcQueryPaso8, Runid);
		System.out.print(Formato8);
		SQLResult Paso8 = executeQuery(dbFCWMQA, Formato8);
		boolean Paso8Empty = Paso8.isEmpty();

		System.out.println(Paso8Empty);

		if (!Paso8Empty) {

			testCase.addQueryEvidenceCurrentStep(Paso8);
		}

		assertFalse(Paso8Empty, "No se tiene informacion en la base datos");

		/// Paso 9 ***************************************************

		System.out.print("Paso 9");
		addStep("Validar el envío del archivo generado por la interface hacia el buzón configurado para el cedis.");

		/// Paso 10 ***************************************************

		System.out.print("Paso 10");
		addStep("Validar que se actualice el estatus de la información procesada de la tabla POS_INBOUND_DOCS de la BD POSUSER a STATUS = 'E'.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String Formato10 = String.format(tdcQueryPaso10, INBOID);
		System.out.print(Formato10);
		System.out.print("\n");
		SQLResult Paso10 = executeQuery(dbFCWMQA, Formato10);

		boolean Paso10Empty = Paso10.isEmpty();

		System.out.println(Paso10Empty);

		if (!Paso10Empty) {

			testCase.addQueryEvidenceCurrentStep(Paso10);
		}

		assertFalse(Paso10Empty, "No se tiene informacion en la base datos");

		/// Paso 11 ***************************************************

		System.out.print("Paso 11");
		addStep("Validar que se actualice el estatus de la información procesada en la XXFC_PC_TSF de la BD RETEK a STATUS = 'E'.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		String Formato11 = String.format(tdcQueryPaso11, PC_ID, STORE);
		System.out.print(Formato11);
		System.out.print("\n");
		SQLResult Paso11 = executeQuery(dbFCRMSSIT, Formato11);

		boolean Paso11Empty = Paso11.isEmpty();

		System.out.println(Paso11Empty);

		if (!Paso10Empty) {

			testCase.addQueryEvidenceCurrentStep(Paso11);
		}

		assertFalse(Paso10Empty, "No se tiene informacion en la base datos");
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

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. Validar el envío de artículos para surtido de POS Immex para la plaza ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatización";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PI1_PI1_ValidarEnvioDeArticulosParaSurtirDePosAImmex";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
}
