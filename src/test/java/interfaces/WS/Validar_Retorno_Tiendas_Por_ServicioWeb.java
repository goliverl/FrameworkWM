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

public class Validar_Retorno_Tiendas_Por_ServicioWeb extends BaseExecution {

	@Test(dataProvider = "data-provider")

	public void ATC_FT_WS_011_Validar_Retorno_Tiendas_Por_ServicioWeb(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbFCIASQA = new SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA,
				GlobalVariables.DB_PASSWORD_FCIASQA);

		/*
		 * Variables
		 *********************************************************************/

		String tdcQRY1 = " SELECT Count(tt.cr_tienda) as Count FROM t_tiendas tt, t_operacion top, t_formato_entorno tfe\r\n"
				+ "WHERE tt.id_tienda = top.id_tienda\r\n"
				+ "AND tt.id_tienda = tfe.id_tienda\r\n"
				+ "AND tt.status = 'S_OPERATIVA'\r\n"
				+ "AND top.LID_EMPRESA_PERTENECE LIKE '%'\r\n"
				+ "AND tfe.LID_ZONA_PAIS LIKE '%'\r\n"
				+ "AND tfe.LID_FORMATO_TIENDA LIKE '%'\r\n"
				+ "AND tfe.LID_CEDIS_QUE_SURTE LIKE '%'\r\n"
				+ "AND tt.CR_SUPERIOR LIKE '%'";

		SeleniumUtil u;
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("Server");

		/*
		 * Paso 1
		 *****************************************************************************************/

		addStep("Validar que existe información de tiendas en la BD de CNT.\r\n");

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

		addStep("Realizar una petición HTTP de tipo GET al servidor IS con la ruta:");

		u = new SeleniumUtil(new ChromeTest(), true);
		String contra = "http://" + user + ":" + ps + "@" + server
				+ ":5555/invoke/oxxows.pub:wsGetTiendasGroups?needDetails=true&GroupKey=TODAS";
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

		boolean Paso3 = Countdb.equals(CountApi);
		
		System.out.println(Paso3);

		if (Paso3) {
			testCase.addCodeEvidenceCurrentStep(Countdb);

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
		return " Validar el retorno de Tiendas por parte del servicio web. ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_WS_011_Validar_Retorno_Tiendas_Por_ServicioWeb";
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
