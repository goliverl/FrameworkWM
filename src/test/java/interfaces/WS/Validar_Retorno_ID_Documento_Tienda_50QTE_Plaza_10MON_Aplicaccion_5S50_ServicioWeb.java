///////////////Funciona//////////////

package interfaces.WS;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import org.openqa.selenium.By;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class Validar_Retorno_ID_Documento_Tienda_50QTE_Plaza_10MON_Aplicaccion_5S50_ServicioWeb extends BaseExecution {

	@Test(dataProvider = "data-provider")

	public void ATC_FT_WS_005_Validar_Retorno_ID_Documento_Tienda_50QTE_Plaza_10MON_Aplicacion_5S50_ServicioWeb(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbFCWMLQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,
				GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		/*
		 * Variables
		 *********************************************************************/

		String tdcQRY1 = " SELECT 'DS50' AS appli, 'PV_DOC_ID' AS campo, 'N/A' AS clave, NVL (MAX (pv_doc_id), 1) AS value \r\n"
				+ " FROM  posuser.pos_envelope a, posuser.pos_inbound_docs b \r\n" 
				+ " WHERE a.ID = b.pe_id \r\n"
				+ " AND a.pv_cr_plaza =  '" + data.get("plaza") +  "' \r\n" 
				+ " AND a.pv_cr_tienda = '" + data.get("tienda") +  "' \r\n" 
				+ " AND a.received_date >= SYSDATE - 3\r\n";

		SeleniumUtil u;
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("Server");

		/*
		 * Paso 1
		 *****************************************************************************************/

		addStep("Validar que existe información de tiendas en la BD de CNT.\r\n");

		System.out.println("Paso 1");
		
		System.out.println(GlobalVariables.DB_HOST_FCIASQA);

		System.out.println(tdcQRY1);

		SQLResult Paso1 = dbFCWMLQA.executeQuery(tdcQRY1);

		String Appli = Paso1.getData(0, "APPLI");
		String Valuedb = Paso1.getData(0, "VALUE");

		System.out.println(Appli);
		System.out.println(Valuedb);

		boolean Paso1Empty = Paso1.isEmpty();
		System.out.println(Paso1Empty);
		if (!Paso1Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

		assertFalse(Paso1Empty, "No se tiene informacion en la base datos");

		/* Paso 2 *********************************************************/

		addStep("Realizar una petición HTTP de tipo GET al servidor IS con la ruta:");

		u = new SeleniumUtil(new ChromeTest(), true);
		String api = "http://" + user + ":" + ps + "@" + server + ":5555/invoke/oxxows.pub/wsGetMaxPvDocID?"
				+ "pvCrPlaza=" + data.get("plaza") + "&pvCrTienda=" + data.get("tienda") + "&appli=" + Appli + "";
		System.out.println(api);
		u.get(api);
		u.hardWait(8);
		String ValueWeb = u.getDriver().findElement(By.cssSelector("body > folios > field_value")).getText();
		System.out.println("Paso 2");
		System.out.println(ValueWeb);
		u.close();

		/* Paso 3 *********************************************************/

		addStep("Validar listado de tiendas retornado.");

		System.out.println("Paso3");

		boolean Paso3 = Valuedb.equals(ValueWeb);

		if (Paso3) {

			testCase.addCodeEvidenceCurrentStep(Valuedb);
			testCase.addCodeEvidenceCurrentStep(ValueWeb);

			System.out.println("La informacion es corrrecta");
			System.out.println(Paso3);
		}

		assertTrue(Paso3, "No concide la informacion");
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Validar el retorno del último ID de docto enviado X la tda 50QTE  para  plaza 10MON con aplic. DS50 X parte del serv. web ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_WS_005_Validar_Retorno_ID_Documento_Tienda_50QTE_Plaza_10MON_Aplicacion_5S50_ServicioWeb";
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
