package interfaces.tpe.prom;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.util.HashMap;

import org.openqa.selenium.By;
import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;

import utils.sql.SQLUtil;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;


public class ATC_FT_003_TPE_PROM_QRY01_009_010 extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_TPE_PROM_QRY01_009_010_test(HashMap<String, String> data) throws Exception {
		
/*
 * Utilerías *********************************************************************/

		SQLUtil dbFCM = new SQLUtil(GlobalVariables.DB_HOST_FCMFS, GlobalVariables.DB_USER_FCMFS, GlobalVariables.DB_PASSWORD_FCMFS);
		SQLUtil dbFCT = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		
/**
* Variables ******************************************************************************************
* 
* 
*/	
		String tdcQuerySaldo = "SELECT " + data.get("query1") +" "
				+ " FROM xxaal.xxaal_employee_balance "
				+ " WHERE id_user = '" + data.get("id_user") +"'";
		
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TPEDoc version=\"1.0\">"
				+ "<header application=\"PROM\" entity=\"EMP\" operation=\"QRY01\" source=\"POS\" plaza=\"%s\" tienda=\"%s\" adDate=\"%s\" caja=\"1\" pvDate=\"%s\"  pvTicket=\"%s\" />"
				+ "<request><redencionProm userId=\"%s\" folioPromocional=\"1544716\" montoTotal=\"%s\"><articulo sku=\"123456789\" monto=\"%s\" /><articulo sku=\"987654321\" monto=\"%s\" /></redencionProm></request></TPEDoc>";

		String tdcQueryTransaccion = "SELECT application, entity, operation, creation_date, plaza, tienda, wm_code "
				+ " FROM TPEUSER.tpe_fr_transaction "
				+ " WHERE application = 'PROM' "
				+ " AND entity = 'EMP'"
				+ " AND operation = 'QRY01' "
				+ " AND TRUNC(creation_date) = TRUNC(SYSDATE) "
				+ " AND plaza = '" + data.get("plaza") +"' "
				+ " AND tienda = '" + data.get("tienda") +"' "
				+ " AND wm_code = '" + data.get("wm_code") +"'";	
		
		
		
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************			
		addStep("Consultar el saldo " + data.get("saldo") +" del usuario:");
		System.out.println(GlobalVariables.DB_HOST_FCMFS);
		System.out.println(tdcQuerySaldo);
		
		SQLResult saldoResult = executeQuery(dbFCM, tdcQuerySaldo);

		
		boolean saldo = saldoResult.isEmpty();
		
		if (!saldo) {
			
			testCase.addQueryEvidenceCurrentStep(saldoResult);
		}
		
		System.out.println(saldo);
		
		assertFalse(saldo, "No se obtiene información de la consulta");
		
//paso2 **********************
		addStep("Validar que el total de la compra sea mayor al saldo " + data.get("saldo") +":");
		
//paso 3 *********************
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

		String input = String.format(xml, data.get("plaza"), data.get("tienda"), data.get("adDate"), data.get("pvDate"), data.get("pvTicket"), data.get("id_user"), data.get("montoTotal"), data.get("monto"), data.get("monto"));

		System.out.println(input);

		pok.runIntefaceWmWithInput10(data.get("interface"), data.get("servicio"), input, "xmlIn");
		
		By xmlOut = By.xpath("/html/body/table/tbody/tr/td[2]");

		String respuesta = u.getText(xmlOut);

		System.out.println(respuesta);
		
//paso 4 ************************		
		addStep("Validar la insercion del registro de Transaccion en la tabla tpe_fr_transaction del esquema TPEUSER.");
		System.out.println(GlobalVariables.DB_HOST_FCTPE);
		System.out.println(tdcQueryTransaccion);
	
		SQLResult transaccionResult = executeQuery(dbFCT, tdcQueryTransaccion);
		String wmcode = transaccionResult.getData(0, "WM_CODE");
		
		boolean transaccion = transaccionResult.isEmpty();
		
		if (!transaccion) {
			
			testCase.addQueryEvidenceCurrentStep(transaccionResult);
		}
		
		System.out.println(transaccion);
		
		assertFalse(transaccion, "No se obtiene informacion de la consulta");		
		
//paso 5****************************
		addStep("Validar la respuesta exitosa de la solicitud TPEDoc[0].response[0].wmCode[0].value='" + data.get("wm_code") +"'.");
		String wmcodeToValidate = data.get("wm_code");
		
		boolean validationRequest = wmcode.equals(wmcodeToValidate);
		
		if(validationRequest) {
			testCase.addTextEvidenceCurrentStep(respuesta);
			testCase.addTextEvidenceCurrentStep("El wmCode es igual a '" + data.get("wm_code") +"'.");
			
		} 
		
		else
			{testCase.addTextEvidenceCurrentStep(respuesta);}

		
		assertTrue(validationRequest, "El wmCode no es el esperado: " + wmcodeToValidate);							
		
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
		return "Construido. Validar el retorno del codigo de respuesta 009 y 010 de una Solicitud de Redencion de promoción QRY01";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
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
