///////////////Pendiente////////////// Si funciona el Api

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

public class Validar_Retorno_Plazas_Por_ServicioWeb extends BaseExecution {

	@Test(dataProvider = "data-provider")

	public void ATC_FT_WS_008_Validar_Retorno_Plazas_Por_ServicioWeb(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbPouser = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,
				GlobalVariables.DB_PASSWORD_Puser);

		/*
		 * Variables
		 *********************************************************************/

		String tdcQRY1 = " SELECT 'Plaza' as tipo, cr_plaza, plaza_type, cr_plaza_desc, fch_arranque\r\n"
				+ " FROM posuser.plazas \r\n"
				+ " WHERE fch_arranque <= SYSDATE AND fch_arranque IS NOT NULL\r\n"
				+ " UNION \r\n"
				+ " SELECT 'Cedis' AS tipo, cedis, NULL, cedis_desc, fch_arranque\r\n"
				+ " FROM posuser.cedis\r\n"
				+ " WHERE fch_arranque <= SYSDATE AND fch_arranque IS NOT NULL\r\n"
				+ " ORDER BY 1 \r\n";
				

		SeleniumUtil u;
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("Server");

		/*
		 * Paso 1
		 *****************************************************************************************/

		addStep("Validar que existe información de tiendas en la BD de CNT.\r\n");

		System.out.println(GlobalVariables.DB_HOST_Puser);

		System.out.println(tdcQRY1);

		SQLResult Paso1 = dbPouser.executeQuery(tdcQRY1);

		boolean Paso1Empty = Paso1.isEmpty();
		System.out.println(Paso1Empty);
		if (!Paso1Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

		assertFalse(Paso1Empty, "No se tiene informacion en la base datos");

		/* Paso 2 *********************************************************/

		addStep("Realizar una petición HTTP de tipo GET al servidor IS con la ruta:");

		u = new SeleniumUtil(new ChromeTest(), true);
		String contra = "http://" + user + ":" + ps + "@" + server
				+ ":5555/invoke/oxxows.pub:wsGetAllPlazas";
		System.out.println(contra);
		u.get(contra);
		u.hardWait(8);
		String CountApi = u.getDriver()
				.findElement(By.cssSelector("#folder0 > div.opened > div.line > span:nth-child(2)")).getText();
		System.out.println(CountApi);
		u.close();

		/* Paso 3 *********************************************************/

		addStep("Validar listado de tiendas retornado.");

		System.out.println("Paso3");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Validar el retorno de todas las Plazas por parte del servicio web. ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_WS_008_Validar_Retorno_Plazas_Por_ServicioWeb";
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
