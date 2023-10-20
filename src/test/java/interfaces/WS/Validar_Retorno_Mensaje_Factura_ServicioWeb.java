///////////////Pendiente//////////////

package interfaces.WS;

import static org.testng.Assert.assertFalse;
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

public class Validar_Retorno_Mensaje_Factura_ServicioWeb extends BaseExecution {

	@Test(dataProvider = "data-provider")

	public void ATC_FT_WS_007_Validar_Retorno_Mensaje_Factura_ServicioWeb(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbFCIASQA = new SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA,
				GlobalVariables.DB_PASSWORD_FCIASQA);

		/*
		 * Variables
		 *********************************************************************/

		String tdcQRY1 = "SELECT * FROM xxfc.xxfc_cfdavalidar \r\n"
				+ "WHERE valc_rfc = [rfc] \r\n"
				+ "and valn_folio = [folio] \r\n"
				+ "and valc_serie = [serie] \r\n"
				+ "and valn_tiporegistro = '0'";
		
		String tdcQRY3 = "SELECT * FROM xxfc.xxfc_cfdavalidar \r\n"
				+ "WHERE valc_rfc = [rfc] \r\n"
				+ "and valn_folio = [folio] \r\n"
				+ "and valc_serie = [serie] \r\n"
				+ "and valn_tiporegistro = '0'\r\n";
			

		SeleniumUtil u;
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("Server");

		/*
		 * Paso 1
		 *****************************************************************************************/

		addStep("Consultar la factura que debe estar registrada en la tabla xxfc_cfdavalidar.");

		System.out.println(GlobalVariables.DB_HOST_FCIASQA);

		System.out.println(tdcQRY1);

		SQLResult Paso1 = dbFCIASQA.executeQuery(tdcQRY1);

		String Countdb = Paso1.getData(0, "Count");

		System.out.println(Countdb);

		boolean Paso1Empty = Paso1.isEmpty();
		System.out.println(Paso1Empty);
		if (!Paso1Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

		assertFalse(Paso1Empty, "No se tiene informacion en la base datos");

		/* Paso 2 *********************************************************/

		addStep("Ejecutar el servicio oxxows.pub:wsMensajeFacturaelectronica con los parámetros indicados");

		u = new SeleniumUtil(new ChromeTest(), true);
		String contra = "http://" + user + ":" + ps + "@" + server
				+ ":5555/invoke/oxxows.wsMensajeFacturaelectronica";
		System.out.println(contra);
		u.get(contra);
		u.hardWait(8);
		String CountApi = u.getDriver()
				.findElement(By.cssSelector("#folder0 > div.opened > div.line > span:nth-child(2)")).getText();
		System.out.println(CountApi);
		u.close();

		/* Paso 3 *********************************************************/

		addStep("Validar la recepción del mensaje por parte del servicio.");

		System.out.println(GlobalVariables.DB_HOST_FCIASQA);

		System.out.println(tdcQRY3);

		SQLResult Paso3 = dbFCIASQA.executeQuery(tdcQRY3);

		boolean Paso3Empty = Paso3.isEmpty();
		System.out.println(Paso3Empty);
		if (!Paso3Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso3);
		}

		assertFalse(Paso3Empty, "No se tiene informacion en la base datos");

	}
	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Validar el retorno del mensaje para la factura por parte del servicio web. ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_WS_007_Validar_Retorno_Mensaje_Factura_ServicioWeb";
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
