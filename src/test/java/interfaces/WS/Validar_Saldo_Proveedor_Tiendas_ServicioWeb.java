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

public class Validar_Saldo_Proveedor_Tiendas_ServicioWeb extends BaseExecution {

	@Test(dataProvider = "data-provider")

	public void ATC_FT_WS_012_Validar_Saldo_Proveedor_Tiendas_ServicioWeb(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		SQLUtil dbAveba = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,
				GlobalVariables.DB_PASSWORD_AVEBQA);

		/*
		 * Variables
		 *********************************************************************/

		String tdcQRY1 = " SELECT SUM( CASE WHEN INVOICE_CURRENCY_CODE = 'MXN' \r\n"
				+ " THEN INVOICE_AMOUNT ELSE BASE_AMOUNT END ) AS saldo \r\n"
				+ " FROM APPS.AP_INVOICES_ALL \r\n"
				+  "WHERE INVOICE_NUM LIKE '" + data.get("folio") + "' || '%' \r\n";
				
		
		String tdcQRY1_1 = " SELECT NVL(SUM(CASE WHEN INVOICE_CURRENCY_CODE = 'MXN' THEN \r\n"
				+ " INVOICE_AMOUNT ELSE BASE_AMOUNT END ),0) \r\n"
				+ " FROM APPS.AP_INVOICES_ALL WHERE DESCRIPTION LIKE '"+data.get("ordencompra")+"' \r\n";
						
		/*
		 * Paso 1
		 *****************************************************************************************/

		addStep(" Validar que exista el saldo en la BD de ORAFIN.--Para tienda \r\n");
				
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);

		System.out.println(tdcQRY1);

		SQLResult Paso1 = dbAveba.executeQuery(tdcQRY1);

		String Countdb = Paso1.getData(0, "SALDO");

		System.out.println(Countdb);

		boolean Paso1Empty = Paso1.isEmpty();
		System.out.println(Paso1Empty);
		if (!Paso1Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

		assertFalse(Paso1Empty, "No se tiene informacion en la base datos");
		
		/* Paso 1-1 *********************************************************/
		
		addStep(" Validar que exista el saldo en la BD de ORAFIN.--Para cedis \r\n");

		System.out.println(GlobalVariables.DB_HOST_AVEBQA);

		System.out.println(tdcQRY1_1);

		SQLResult Paso1_1 = dbAveba.executeQuery(tdcQRY1_1);

		String Countdb1 = Paso1.getData(0, "SALDO");

		System.out.println(Countdb1);

		boolean Paso1_1Empty = Paso1_1.isEmpty();
		System.out.println(Paso1_1Empty);
		if (!Paso1_1Empty) {
			testCase.addQueryEvidenceCurrentStep(Paso1_1);
		}

		assertFalse(Paso1_1Empty, "No se tiene informacion en la base datos");

		/* Paso 2 *********************************************************/

		addStep("Ejecutar el servicio oxxows.pub:wsObtenerSaldoProveedor con los parametros especificados.\r\n");
				
		SeleniumUtil u;
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("Server");

		u = new SeleniumUtil(new ChromeTest(), true);
		String contra = "http://" + user + ":" + ps + "@" + server
				+ ":5555/invoke/oxxows.pub:wsObtenerSaldoProveedor?folio=" +data.get("folio")+ "&ordenCmpra="+data.get("ordencompra")+"";
		System.out.println(contra);
		u.get(contra);
		u.hardWait(8);
		String CountApi = u.getDriver()
				.findElement(By.cssSelector("#folder0 > div.opened > div.line > span:nth-child(2)")).getText();
		System.out.println(CountApi);
		u.close();

		/* Paso 3 *********************************************************/

		addStep(" Validar que el saldo obtenido del WS coincida con el valor consultado en ORAFIN. ");

		boolean Paso3 = Countdb.equals(CountApi);

		if (Paso3) {
			testCase.addCodeEvidenceCurrentStep(Countdb);

			System.out.println("La informacion es corrrecta");
			System.out.println(Paso3);
		}

		assertFalse(!Paso3, "No concide la informacion");
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return " Validar la obtención del sdo de prov. para tdas X parte del serv. web. ";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_WS_012_Validar_Saldo_Proveedor_Tiendas_ServicioWeb";
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
