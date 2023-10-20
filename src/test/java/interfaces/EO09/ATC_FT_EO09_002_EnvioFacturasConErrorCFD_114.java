package interfaces.EO09;

import modelo.BaseExecution;
import util.GlobalVariables;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.openqa.selenium.By;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_EO09_002_EnvioFacturasConErrorCFD_114 extends BaseExecution {

	/**
	 * Des: Validar la notificacion de error por parametros inv�lidos (Codigo 114)
	 * @author Ultima modificacion Mariana Vives
	 * @throws 02/27/2023
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_EO09_002_EnvioFacturasConErrorCFD_114_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utiler�as
		 *********************************************************************/

		SQLUtil dbEBS = new SQLUtil(GlobalVariables.DB_HOST_Ebs, GlobalVariables.DB_USER_Ebs,
				GlobalVariables.DB_PASSWORD_Ebs);		
			
		/*
		 * Variables
		 ******************************************************************************************/

		String tdcQueryxxfc_cfd_transactionOrafin = "SELECT UUID, FOLIO, SERIE, CREATION_DATE, CODIGO"
				+ " FROM WMUSER.XXFC_CFD_TRANSACTION" 
				+ " WHERE UUID = '" + data.get("UUID") + "'" 
				+ " AND FOLIO = '"+ data.get("Folio") + "'"
				+ " AND SERIE = '" + data.get("Serie") + "'" 
				+ " AND CODIGO = '"+ data.get("Codigo") + "'" // Se debe cambiar el valor de "Codigo" en el data provider por 112 o 114
				+ "	AND TRUNC(CREATION_DATE) >= TRUNC(SYSDATE-1)";
		
		// utileria
		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		//String codigo112 = "112";
		String codigo114 = "114";

		
		//									PASO  1
		/* NA La informacion de la Factura se Checa en el Dataprovider */
		
		//addStep("Validar que la factura tenga los campos UUID, folio, serie, RFCEmisor y RFCReceptor.");
					
		//									PASO 2

		// addStep("Ejecutar el webService wsdRFCRequest.");

		
		//									PASO 3
		
		/*
		 * Para conseguir un error 112 se debe borrar algun dato del dataprovider como
		 * por ejemplo el StringXML
		
		 * Para Conseguir un error 114 se debe poner mal los datos del RFCEmisor o
		 * receptor ya sea quitando o a�adiendo caracteres
		 */

		addStep("Validar la ejecuci�n del servicio EO09.Pub:request en el servidor");

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		u.get(contra);
		u.hardWait(4);
		pok.runIntefaceWmWithInputsRFC(data.get("interfase"), data.get("servicio"), data.get("UUID"), data.get("Folio"),
				data.get("Serie"), data.get("StringXML"), data.get("Reintento"), data.get("RFCEmisor"),
				data.get("RFCReceptor"));

		By xmlOut = By.xpath("/html/body/table/tbody/tr/td[2]");
		String CodigoXML = u.getText(xmlOut);
		System.out.println(CodigoXML);
		u.close();
		

		//									PASO 4 

		addStep("Validar la inserci�n de la factura en la tabla XXFC_CFD_TRANSACTION");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		System.out.println("\n" + tdcQueryxxfc_cfd_transactionOrafin);
		u.hardWait(6);
		
		SQLResult ExecuteQuery = executeQuery(dbEBS, tdcQueryxxfc_cfd_transactionOrafin);
		
		boolean StatusQuery2 = ExecuteQuery.isEmpty();
		System.out.println(StatusQuery2);
		if (!StatusQuery2) {
			testCase.addQueryEvidenceCurrentStep(ExecuteQuery);
		} else {			
			testCase.addTextEvidenceCurrentStep("No se inserto la factura correctamente. Tabla XXFC_CFD_COMPROBANTE");
			testCase.addQueryEvidenceCurrentStep(ExecuteQuery);
		}
		
		//assertFalse(StatusQuery2, "No se inserto la factura correctamente. Tabla XXFC_CFD_COMPROBANTE");
		
		
		//									PASO 5
		addStep("Validar la recepci�n de un correo electr�nico con la notificaci�n del error.");
		
		
		//									PASO 6
		
		addStep("Validar que el c�digo retornado por el webService sea 114");

		boolean validacion = codigo114.equals(CodigoXML);
		System.out.println(validacion);
		if (validacion) {
			testCase.addTextEvidenceCurrentStep("El codigo es correcto (" + CodigoXML + ").");			
			System.out.println("El codigo es correcto");
		} else {
			testCase.addTextEvidenceCurrentStep("El codigo es incorrecto codigo buscado '114'"
					+ " codigo encontrado: '" + CodigoXML + "'.");
		}
		
		//testCase.addQueryEvidenceCurrentStep(ExecuteQuery);
		
		assertTrue(validacion, "El codigo retornado no es 114");
		
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Construido. Validar la notificaci�n de error por formato de Factura incorrecto (C�digo 114)";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_EO09_002_EnvioFacturasConErrorCFD_114_test";
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