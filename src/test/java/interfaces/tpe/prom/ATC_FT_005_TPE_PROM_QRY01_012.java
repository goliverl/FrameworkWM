package interfaces.tpe.prom;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_005_TPE_PROM_QRY01_012 extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_TPE_PROM_QRY01_012_test(HashMap<String, String> data) throws Exception {
		
/*
 * Utilerías *********************************************************************/

		SQLUtil dbFCM = new SQLUtil(GlobalVariables.DB_HOST_FCMFS, GlobalVariables.DB_USER_FCMFS, GlobalVariables.DB_PASSWORD_FCMFS);
		SQLUtil dbFCT = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		
/**
* Variables ******************************************************************************************
* 
* 
*/		
		String tdcQueryPromocion = "SELECT p.seniority_months, p.adviser_active, p.leader_active, p.cashier_active "
				+ "FROM xxaal.xxaal_promotion p INNER JOIN xxaal.xxaal_prom_head_sim_stg h ON p.id_promotion = h.id_promotion "
				+ "WHERE h.promotion = 1 "
				+ "AND p.status = 'W'";
		
		String tdcQueryTransaccion = "SELECT application, entity, operation, creation_date, plaza, tienda, wm_code "
				+ " FROM TPEUSER.tpe_fr_transaction "
				+ " WHERE application = 'PROM' "
				+ " AND entity = 'EMP'"
				+ " AND operation = 'QRY01' "
				+ " AND TRUNC(creation_date) = TRUNC(SYSDATE) "
				+ " AND plaza = '" + data.get("plaza") +"' "
				+ " AND tienda = '" + data.get("tienda") +"' "
				+ " AND wm_code = '012'";	
		
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TPEDoc version=\"1.0\">"
				+ "<header application=\"PROM\" entity=\"EMP\" operation=\"QRY01\" source=\"POS\" plaza=\"%s\" tienda=\"%s\" adDate=\"%s\" caja=\"1\" pvDate=\"%s\"  pvTicket=\"123452\" />"
				+ "<request><redencionProm userId=\"%s\" folioPromocional=\"1\" montoTotal=\"10000\"><articulo sku=\"123456789\" monto=\"5000\" /><articulo sku=\"987654321\" monto=\"5000\" /></redencionProm></request></TPEDoc>";

		
		
		
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************		
		addStep("Validar que la promocion no exista:");
	
		System.out.println(tdcQueryPromocion);
		
		SQLResult promocionResult = executeQuery(dbFCM, tdcQueryPromocion);

		
		boolean promocion = promocionResult.isEmpty();
		
		if (promocion) {
			
			testCase.addQueryEvidenceCurrentStep(promocionResult);
		}
		
		System.out.println(promocion);
		
		assertTrue(promocion, "No se obtiene información de la consulta");

//paso 2 ************************
		addStep("Ejecutar el servicio TPE.PROM.Pub:request agregando el XML indicado en el parametro xmlIn.");
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		System.out.println(xml);
		
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";

		u.get(contra);
		u.hardWait(3);

		String input = String.format(xml, data.get("plaza"), data.get("tienda"), data.get("adDate"), data.get("pvDate"), data.get("id_user"));

		System.out.println(input);

		pok.runIntefaceWmWithInput10(data.get("interface"),data.get("servicio"),input,"xmlIn");
		
		By xmlOut = By.xpath("/html/body/table/tbody/tr/td[2]");

		String respuesta = u.getText(xmlOut);

		System.out.println(respuesta);
		
//paso 3 ************************		
		addStep("Validar la inserción del registro de Transaccion en la tabla tpe_fr_transaction del esquema TPEUSER.");
		System.out.println(GlobalVariables.DB_HOST_FCTPE);
		System.out.println(tdcQueryTransaccion);
	
		SQLResult transaccionResult = executeQuery(dbFCT, tdcQueryTransaccion);
		String wmcode = transaccionResult.getData(0, "WM_CODE");

		
		boolean transaccion = transaccionResult.isEmpty();
		
		if (!transaccion) {
			
			testCase.addQueryEvidenceCurrentStep(transaccionResult);
		}
		
		System.out.println(transaccion);
		
		assertFalse(transaccion, "No se obtiene información de la consulta");		
		
//paso 4 ****************************
		addStep("Validar la respuesta exitosa de la solicitud TPEDoc[0].response[0].wmCode[0].value='012'.");
		
		String wmcodeToValidate = "012";
		
		boolean validationRequest = wmcode.equals(wmcodeToValidate);
		
		if(validationRequest) {
			
			testCase.addTextEvidenceCurrentStep(respuesta);
			testCase.addTextEvidenceCurrentStep("El wmCode es igual a '012'.");
			
		} 
		
		else
			{testCase.addTextEvidenceCurrentStep(respuesta);}

		
		assertTrue(validationRequest, "El wmCode no es el esperado.");		
				
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
		return "Validar el retorno del codigo de respuesta 012 de una Solicitud de Redencion de promocion QRY01";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
