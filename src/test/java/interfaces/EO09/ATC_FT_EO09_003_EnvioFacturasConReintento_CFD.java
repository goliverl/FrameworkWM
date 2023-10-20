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

public class ATC_FT_EO09_003_EnvioFacturasConReintento_CFD extends BaseExecution {

	/**
	 * ALM
	 * Desc:
	 * Validar el envio de facturas con reintento hacia el Portal de Consultas CFD (Codigo 101)
	 * NOTA: Entra al webService wsdRFCRequest, no se tiene infromacion sobre esta plataforma.
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_EO09_003_EnvioFacturasConReintento_CFD_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 *********************************************************************/

		utils.sql.SQLUtil dbEBS = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		
		/**
		 *
		 * 
		 */
		
		/*
		 * Variables
		 ******************************************************************************************/

		String tdcQueryxxfc_cfd_transactionOrafin = "SELECT UUID, SERIE, FOLIO, CODIGO, CREATION_DATE "
				+ " FROM WMUSER.XXFC_CFD_TRANSACTION" 
				+ " WHERE UUID = '" + data.get("UUID") + "'" 
				+ " AND FOLIO = '"+ data.get("Folio") + "'" 
				+ " AND CODIGO = '101'" 
				+ " AND TRUNC(CREATION_DATE) >= TRUNC(SYSDATE)";

		String tdcQueryxxfc_cfd_comprobanteOrafin = "SELECT UUID, FOLIO, SERIE, CREATION_DATE "
				+ " FROM XXFC.XXFC_CFD_COMPROBANTE" 
				+ " WHERE UUID = '" + data.get("UUID") + "'" 
				+ " AND FOLIO = '"+ data.get("Folio") + "'" 
				+ " AND SERIE = '" + data.get("Serie") + "'"
				+ "	AND TRUNC(CREATION_DATE) >= TRUNC(SYSDATE)";

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String codigo101 = "101";

		/*
		 * Pasos
		 *********************************************************************************/

		//									PASO 1

		addStep("Validar que la factura no tiene informaci�n con c�digo 101 en la tabla XXFC_CFD_TRANSACTION de ORAFIN");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		System.out.println("\n" + tdcQueryxxfc_cfd_transactionOrafin);
		
		SQLResult ExecuteQuery6 = executeQuery(dbEBS, tdcQueryxxfc_cfd_transactionOrafin);
		
		boolean StatusQuery0 = ExecuteQuery6.isEmpty();
		System.out.println(StatusQuery0);
		if (!StatusQuery0) {
			testCase.addTextEvidenceCurrentStep("La Factura contiene informacion con codigo 101. Tabla XXFC_CFD_TRANSACTION");
		}
		
		testCase.addQueryEvidenceCurrentStep(ExecuteQuery6);
		
		assertTrue(StatusQuery0, "La Factura contiene informacion con codigo 101. Tabla XXFC_CFD_TRANSACTION");

		
		//									Paso 2 		
		/// La informacion de la Factura se Checa en el Dataprovider /

		//addStep("Validar que la factura tenga los campos UUID, folio, serie, RFCEmisor y RFCReceptor.");
		

		//									PASO 3

		// addStep("Ejecutar el webService wsdRFCRequest.\r\n");

		
		//									PASO 4

		addStep("Validar la ejecuci�n del servicio EO09.Pub:request en el servidor");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		u.hardWait(4);
		pok.runIntefaceWmWithInputsRFC(data.get("interfase"), data.get("servicio"), data.get("UUID"), data.get("Folio"),
				data.get("Serie"), data.get("StringXML"), data.get("Reintento"), data.get("RFCEmisor"),
				data.get("RFCReceptor"));
		By xmlOut = By.xpath("/html/body/table/tbody/tr/td[2]");
		String CodigoXML = u.getText(xmlOut);
		System.out.println(CodigoXML);
		
		u.close();
				
		//									PASO 5

		addStep("Validar la inserci�n de la Factura en la tabla XXFC_CFD_COMPROBANTE");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		System.out.println("\n" + tdcQueryxxfc_cfd_comprobanteOrafin);
		
		SQLResult ExecuteQuery1 = executeQuery(dbEBS, tdcQueryxxfc_cfd_comprobanteOrafin);
		
		boolean StatusQuery = ExecuteQuery1.isEmpty();
		System.out.println(StatusQuery);
		if (!StatusQuery) {
			testCase.addQueryEvidenceCurrentStep(ExecuteQuery1);			
		} else {			
			testCase.addTextEvidenceCurrentStep("No se inserto la Factura correctamente. Tabla XXFC_CFD_COMPROBANTE");
			testCase.addQueryEvidenceCurrentStep(ExecuteQuery1);
		}
				
		assertFalse(StatusQuery, "No se inserto la Factura correctamente. Tabla XXFC_CFD_COMPROBANTE");

		
		//									PASO 6 
		
		addStep("Validar la inserci�n de la Factura en la tabla XXFC_CFD_TRANSACTION");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		System.out.println("\n" + tdcQueryxxfc_cfd_transactionOrafin);
		
		SQLResult ExecuteQuery2 = executeQuery(dbEBS, tdcQueryxxfc_cfd_transactionOrafin);
		
		boolean StatusQuery2 = ExecuteQuery2.isEmpty();
		System.out.println(StatusQuery2);
		if (!StatusQuery2) {
			testCase.addQueryEvidenceCurrentStep(ExecuteQuery1);			
		} else {			
			testCase.addTextEvidenceCurrentStep("No se inserto la factura correctamente. Tabla XXFC_CFD_TRANSACTION");
			testCase.addQueryEvidenceCurrentStep(ExecuteQuery1);
		}
		
		assertFalse(StatusQuery2, "No se inserto la factura correctamente. Tabla XXFC_CFD_TRANSACTION");

		
		//									PASO 7 

		addStep("Validar que el codigo retornado por el webService sea 101");

		boolean validacion = codigo101.equals(CodigoXML);
		System.out.println(validacion);
		if (validacion) {
			testCase.addTextEvidenceCurrentStep("El codigo es correcto (" + CodigoXML + ").");			
			System.out.println("El codigo es correcto");
		} else {
			testCase.addTextEvidenceCurrentStep("El codigo es incorrecto codigo buscado '101'"
					+ " codigo encontrado: '" + CodigoXML + "'.");
		}
		
		//testCase.addQueryEvidenceCurrentStep(ExecuteQuery2);
		
		assertTrue(validacion, "El codigo retornado no es 101");

	}

	@Override
	public void beforeTest() {
		
	}

	@Override
	public String setTestDescription() {
		return "Construido. Validar el env�o de facturas con reintento hacia el Portal de Consultas CFD (C�digo 101)";
	}

	@Override
	public String setTestDesigner() {
		return "AutomationQA";
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_EO09_003_EnvioFacturasConReintento_CFD_test";
	}

	@Override
	public String setTestInstanceID() {
		return null;
	}

	@Override
	public String setPrerequisites() {
		return null;
	}
}
