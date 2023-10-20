package interfaces.poinv_wo2;

import static org.junit.Assert.assertFalse;
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
import utils.sql.SQLUtil;

public class Poinv_WO2_ValidarInsercionNuevasRequisiciones extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_Poinv_WO2_003_ValidaInsercNuevasRequcisiones(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 ********************************************************************************************************************************************/
		SQLUtil dbPortal = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_IAS, GlobalVariables.DB_USER_IAS,
				GlobalVariables.DB_PASSWORD_IAS);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,
				GlobalVariables.DB_PASSWORD_FCWMLQA);
		SQLUtil dbAveb = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,
				GlobalVariables.DB_PASSWORD_AVEBQA);

		/*
		 * Variables
		 ******************************************************************************************************************************************/

		// Paso 1

		String validInf = "SELECT ID_LE,FOLIO_LE,PROPERTYNUM,STATUS_EPAPER,EPAPER_UPLOAD_DATE,WM_STATUS "
				+ "from XXEX.XXEX_SYNC_EPAPER_PO  " + "where SUBSTR(PROPERTYNUM, 1, 5) = '" + data.get("plaza") + "'  "
				+ "and WM_STATUS = 'L'  " + "AND STATUS_EPAPER = 'A' " + "AND ROWNUM < 2  "
				+ "ORDER BY EPAPER_UPLOAD_DATE DESC";

		// Paso 2
		String InfRequisic = "SELECT HED.ID_LE, HED.CREATED_BY, DET.ID_LE_DETAIL, DET.NUM_ARTICULO, DECODE( NVL(DET.ID_COMBO,-1),-1, "
				+ "	DET.CANTIDAD, DET.CANTIDAD*DET.CANTIDAD_COMBO), DET.PRECIO, TRIM(TO_CHAR(EMB.NUMERO,'09')), "
				+ " DM.FECHA_EMBARQUE_AUT, DET.CANTIDAD_COMBO, CASE WHEN DET.ID_COMBO IS NULL "
				+ "THEN 'I' ELSE 'K' END, COM.DID_COMBO " + "FROM XXEX.XXEX_LE_HEADER HED "
				+ "JOIN XXEX.XXEX_LE_DETAIL DET ON HED.ID_LE = DET.ID_LE " + "AND HED.CR_PLAZA = '" + data.get("plaza")
				+ "' " + "JOIN  XXEX.XXEX_CAT_ARTICULOS_ADIC ART ON DET.NUM_ARTICULO = ART.NUM_ARTICULO  "
				+ "JOIN WM_EMBARQUES EMB ON ART.TIPO_EMBARQUE = EMB.ID_EMBARQUE "
				+ "JOIN XXEX.XXEX_DETALLE_EMBARQUES DM ON EMB.ID_EMBARQUE = DM.ID_EMBARQUE  "
				+ "AND 2 = DM.estatus_autorizacion LEFT JOIN XXEX.XXEX_LE_COMBOS COM ON DET.ID_COMBO = COM.ID_COMBO  "
				+ "WHERE HED.ID_LE = '%s'  " + "AND DM.ID_LE = HED.ID_LE  " + "AND ART.CLASIF_COSTO <> 'CC_NF' "
				+ "AND ART.CLASIF_COSTO <> 'CC_NF'  " + "ORDER BY DET.ID_LE_DETAIL";

//		Paso 4
		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'POINV_WO2'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  ORDER BY START_DT DESC";
		// Paso 5
		String ValidInsertA = "SELECT HEADER_NUM,PREPARER_NAME,SI,WM_STATUS,WM_SENT_DATE,WM_RUN_ID,ORACLE_CR_SUPERIOR,ORACLE_CR "
				+ "FROM XXFC.XXPO_ORA_REQ_HEADERS_TMP  " + "WHERE WM_RUN_ID = '%s'  "
				+ "AND TRUNC(WM_SENT_DATE) = TRUNC(SYSDATE)";

		String ValidInsertB = "SELECT HEADER_NUM,LINE_NUM,ITEM_NUMBER,WM_STATUS,WM_SENT_DATE,WM_RUN_ID "
				+ "FROM XXFC.XXPO_ORA_REQ_LINES_TMP " + "WHERE HEADER_NUM = '%s'  " + "AND WM_RUN_ID = '%s' "
				+ "AND TRUNC(WM_SENT_DATE) = TRUNC(SYSDATE)";

//		Paso 6
		String ValidaUpdPortal = " SELECT ID_LE,FOLIO_LE,PROPERTYNUM,STATUS_EPAPER,EPAPER_UPLOAD_DATE,WM_STATUS "
				+ "FROM XXEX.XXEX_SYNC_EPAPER_PO  " + "WHERE PROPERTYNUM = '%s'  " + "AND WM_STATUS = 'P'  "
				+ "AND TRUNC(WM_PROCESS_DATE) = TRUNC(SYSDATE)";

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='POINV_WO2' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

//********************************************************************************************************************************************************************************		

		/* Pasos */

//		Paso 1	************************	

		addStep("Validar que existe información con estatus L en la tabla XXEX_SYNC_EPAPER_PO de "
				+ "PORTAL de acuerdo al parámetro propertyNum proporcionado.");
		String ID_LE = "";
		String PROPERTYNUM = "";
		System.out.println(GlobalVariables.DB_HOST_IAS);
		System.out.println(validInf);
		SQLResult validInfExc = dbPortal.executeQuery(validInf);

		boolean validInfRes = validInfExc.isEmpty();

		if (!validInfRes) {

			testCase.addQueryEvidenceCurrentStep(validInfExc);
			ID_LE = validInfExc.getData(0, "ID_LE");
			System.out.println("LE_ID= " + ID_LE);
			PROPERTYNUM = validInfExc.getData(0, "PROPERTYNUM");
			System.out.println("PROPERTYNUM= " + PROPERTYNUM);
		}

		System.out.println(validInfRes);

		assertFalse("No existe información", validInfRes);

//		Paso 2	************************

		addStep("Validar que existe información de la requisición para la plaza y el campo LE_ID obtenido del query anterior.");
		System.out.println(GlobalVariables.DB_HOST_IAS);
		String InfRequisicFormat = String.format(InfRequisic, ID_LE);
		System.out.println(InfRequisicFormat);
		SQLResult ExecInfRequisic = dbPortal.executeQuery(InfRequisicFormat);
		boolean InfRequisicRes = ExecInfRequisic.isEmpty();
		if (!InfRequisicRes) {

			testCase.addQueryEvidenceCurrentStep(ExecInfRequisic);

		}

		System.out.println(InfRequisicRes);
		assertFalse("No existe información de la requisición para la plaza", InfRequisicRes);

//		Paso 3	************************

		// Utileria
		addStep("Ejecutar el servicio: POINV_WO2.Pub:wsInsertRequisicion.");
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecución

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(3);

		}

//				Paso 4 *********************************

		addStep("Validar que la interfaz se ejecutó correctamente en WMLOG con estatus S.");

		String RUN_ID = "";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RUN_ID = ExecLog.getData(0, "RUN_ID");
			System.out.println("RUN_ID: " + RUN_ID);
			testCase.addQueryEvidenceCurrentStep(ExecLog);
		}

		System.out.println(LogRequest);
		assertFalse(LogRequest, "No se muestra la información.");

//				Paso 5	************************

		addStep("	Validar que se insertó la información en las tablas: XXPO_ORA_REQ_HEADERS_TMP y XXPO_ORA_REQ_LINES_TMP de ORAFIN.");

		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		String ValidInsertAForm = String.format(ValidInsertA, RUN_ID);
		System.out.println(ValidInsertAForm);
		SQLResult ExecValidInsertA = dbAveb.executeQuery(ValidInsertAForm);
		String HEADER_NUM = "";

		boolean ValidInsertARes = ExecValidInsertA.isEmpty();

		if (!ValidInsertARes) {

			testCase.addQueryEvidenceCurrentStep(ExecValidInsertA);
			HEADER_NUM = ExecValidInsertA.getData(0, "HEADER_NUM");
			System.out.println("HEADER_NUM= " + HEADER_NUM);

		}
		System.out.println(ValidInsertARes);
		assertFalse("No se encontraron registros insertados en XXPO_ORA_REQ_HEADERS_TMP", ValidInsertARes);

		String ValidInsertBForm = String.format(ValidInsertB, HEADER_NUM, RUN_ID);
		System.out.println(ValidInsertBForm);
		SQLResult ExecValidInsertB = dbAveb.executeQuery(ValidInsertBForm);

		boolean ValidInsertBRes = ExecValidInsertB.isEmpty();

		if (!ValidInsertBRes) {

			testCase.addQueryEvidenceCurrentStep(ExecValidInsertB);

			System.out.println(ValidInsertBRes);

			assertFalse("No se encontraron registros insertados en XXPO_ORA_REQ_LINES_TMP", ValidInsertBRes);

//		Paso 6	************************

			addStep("Validar que se modificaron los campos FOLIOSI, WM_STATUS a P y WM_PROCESS_DATE en la tabla XXEX_SYNC_EPAPER_PO de PORTAL.");
			System.out.println(GlobalVariables.DB_HOST_IAS);
			String ValidaUpdPortalFormat = String.format(ValidaUpdPortal, PROPERTYNUM);
			System.out.println(ValidaUpdPortalFormat);
			SQLResult ExecValidaUpdPortal = dbPortal.executeQuery(ValidaUpdPortalFormat);
			boolean ValidaUpdPortalRes = ExecValidaUpdPortal.isEmpty();
			if (!ValidaUpdPortalRes) {

				testCase.addQueryEvidenceCurrentStep(ExecValidaUpdPortal);

			}

			System.out.println(ValidaUpdPortalRes);
			assertFalse("No existe información de la requisición para la plaza", ValidaUpdPortalRes);

		}
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_Poinv_WO2_003_ValidaInsercNuevasRequcisiones";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar que se inserta correctamente la información de nuevas requisiciones en las tablas destino de ORAFIN";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
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
