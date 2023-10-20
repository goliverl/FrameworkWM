package interfaces.pr2_co;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import om.PR2;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class PR2_CO_SkuNoConfigurado_ extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_pr2_co_SkuNoConfigurado_(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		utils.sql.SQLUtil dbRmsCol = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_RMS_COL,GlobalVariables.DB_USER_RMS_COL, GlobalVariables.DB_PASSWORD_RMS_COL);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA,
				GlobalVariables.DB_USER_FCWMQA_NUEVA, GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		utils.sql.SQLUtil dbEbsCol = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS_COL,
				GlobalVariables.DB_USER_EBS_COL, GlobalVariables.DB_PASSWORD_EBS_COL);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		PR2 pr2Util = new PR2(data, testCase, dbLog);

		/*
		 * Variables
		 *************************************************************************/

		String tdcQueryOrafin = " SELECT ORACLE_CR, ORACLE_CR_DESC, ORACLE_CR_SUPERIOR, ESTADO, RETEK_CR  FROM  XXFC_MAESTRO_DE_CRS_V "
				+ " WHERE ESTADO = 'A'" + " AND ORACLE_CR_SUPERIOR = '" + data.get("PLAZA") + "'" + " AND ORACLE_CR = '"
				+ data.get("TIENDA") + "'" + " AND ORACLE_CR_TYPE = 'T'";

		String tdcQueryItem = "SELECT * " + "FROM XXFC_ITEM_LOC_VIEW " + "WHERE ITEM =" + data.get("SKU")
				+ " AND LOC = %s " + "AND STATUS IN ('A','I')";

		String tdcQueryWmLogValidSku = "SELECT *  FROM (SELECT ID, OPERATION, START_DT,END_DT,STATUS,CODE "
				+ "FROM POSUSER.TSF_TRANSACTION_COL " + "WHERE OPERATION = 'PR2_CO_validSKU' ORDER BY START_DT DESC)"
				+ " WHERE ROWNUM = 1";

		String tdcQueryIntegrationServer = "SELECT *  FROM (SELECT ID, OPERATION, START_DT,END_DT,STATUS,CODE "
				+ "FROM POSUSER.TSF_TRANSACTION_COL " + "WHERE OPERATION = 'PR2_CO_validSKU' ORDER BY START_DT DESC)"
				+ " WHERE ROWNUM = 1";

		String tdcQueryError = "SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION,MESSAGE FROM" + " WMLOG.WM_LOG_ERROR"
				+ " WHERE RUN_ID =  %s";

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><VALIDSKUS><HEADER><DESTINATIONAPP>POS</DESTINATIONAPP>"
				+ "<SENDER_ID>%s</SENDER_ID><RECEIVER_ID>%s</RECEIVER_ID><NO_RECORDS>%s</NO_RECORDS>"
				+ "</HEADER><DETAIL><SKU>%s</SKU></DETAIL></VALIDSKUS>";

		String sku, pv_doc_id, query, receiver, inboundStatusPuser = "R", statusWmlog = "S", statusAffect = "I",
				statusAck = "E", statusPuser, wmcode, wmcodeVal = "101";

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";
		String ftp_conn_id = "RTKRMS";
		String status = "E";
		String run_id;
		testCase.setProject_Name("Autonomía Sistema OXXO Colombia");

		/*
		 * Pasos
		 *****************************************************************************/

		/*
		 * Valid Sku
		 **************************************************************************/

		// Paso 1
		addStep("Validar el CR que se utiliza para realizar la ejecución en la tabla XXFC_MAESTRO_DE_CRS_V de ORAFIN.");
		System.out.println(tdcQueryOrafin);
		SQLResult orafin = dbEbsCol.executeQuery(tdcQueryOrafin);
		String retek_cr = orafin.getData(0, "RETEK_CR");
		System.out.println("Fecha minima= " + retek_cr);
		boolean paso1 = orafin.isEmpty();
		if (!paso1) {
			testCase.addQueryEvidenceCurrentStep(orafin);
		}
		assertFalse(paso1, "No se encontro el CR de la plaza y/o tienda a utilizar");
		// Paso 2
		addStep("Comprobar que no existan datos de los items que se estan procesando en la tabla  XXFC_ITEM_LOC_VIEW de RETEK.");

		query = String.format(tdcQueryItem, retek_cr);
		System.out.println("\n" + query);
		SQLResult rms = dbRmsCol.executeQuery(query);

		boolean paso2 = rms.isEmpty();
		if (!paso2) {
			testCase.addQueryEvidenceCurrentStep(rms);
		}
		// assertFalse("No existen datos de los items que se estan procesando", paso2);
		// Paso 3
		addStep("Ejecutar el servico PR02.Pub:runValidSku para realizar la validación de articulos.");

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";

		u.get(contra);

		// testCase.addScreenShotCurrentStep(u, "homepage");

		u.hardWait(3);

		String input = String.format(xml, data.get("SENDER"), data.get("RECEIVER"), data.get("NO_RECORDS"),
				data.get("SKU"));

		System.out.println(input);

		String dateExecution = pok.runIntefaceWmWithInput(data.get("interface"), data.get("servicio"), input, null);

		By xmlOut = By.xpath("/html/body/table/tbody/tr/td[2]");

		String respuesta = u.getText(xmlOut);

		System.out.println(respuesta);

		String tdcIntegrationServerFormat = String.format(tdcQueryIntegrationServer);

		System.out.println(tdcIntegrationServerFormat);
		System.out.println(tdcIntegrationServerFormat);
		SQLResult serv = dbLog.executeQuery(tdcIntegrationServerFormat);
		String STATUS = serv.getData(0, "STATUS");
		String ID = serv.getData(0, "ID");
		boolean valuesStatus = STATUS.equals(searchedStatus);// Valida si se encuentra en estatus R

		while (valuesStatus) {
			serv = dbLog.executeQuery(tdcIntegrationServerFormat);
			STATUS = serv.getData(0, "STATUS");
			ID = serv.getData(0, "ID");
			valuesStatus = STATUS.equals(searchedStatus);
			u.hardWait(2);
		}
		boolean successRun = STATUS.equals(status);// Valida si se encuentra en estatus S

		if (!successRun) {
			String error = String.format(tdcQueryError, ID);
			SQLResult E = dbLog.executeQuery(error);
			boolean emptyError = E.isEmpty();
			if (!emptyError) {
				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR_TPE ");
				testCase.addQueryEvidenceCurrentStep(E);

			}
		}

		// Paso 4
		addStep("Verificar la respuesta generada por el servicio.");
		testCase.addTextEvidenceCurrentStep(respuesta);

		// Paso 5
		addStep("Validar que la ejecución de la interface se registre correctamente en la tabla TSF_TRANSACTION_COL de la BD POSUSER.");
		SQLResult sk = dbLog.executeQuery(tdcQueryWmLogValidSku);
		String statusSku = sk.getData(0, "STATUS");
		boolean skuRun = statusSku.equals(status);
		assertTrue(skuRun, "La ejecución de la interfaz termino en un estatus diferente");
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Terminada. No tener en existencia dentro del catálogo el SKU a transferir.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_pr2_co_SkuNoConfigurado_";
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