package interfaces.TpeServicio;


import static org.testng.Assert.assertFalse;

import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class ATC_FT_011_TpeServicio_Verificacion_KeyRing_Consulta_Informacion extends BaseExecution{
	
	/*
	 * 
	 * @cp Verificar KeyRing CONSULTA de informacion
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_011_TpeServicio_Verificacion_KeyRing_Consulta_Informacion_test(HashMap<String, String> data) throws Exception {
	
/** UTILERIA *********************************************************************/	
		
		
/** VARIABLES *********************************************************************/	
				
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep(" Ejecutar la interface con los parametros indicados TPE.TAE.Keyring.Pub:runSetKeyring ");

		SeleniumUtil u;
		PakageManagment pok;
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		System.out.println(contra);
		String WmCode  = pok.runIntefaceWmWithInputxml(data.get("interfase"), data.get("servicio"),
				data.get("folio"), data.get("Folio"));
		
		System.out.println(WmCode);

		/* PASO 2 *********************************************************************/

		addStep(" Verificar que la ejecucion haya terminado con exito.\r\n");

		int Code101 = 101;
		int CodeResult = 0;
		String subWmCode = WmCode.substring(71, 74);

		CodeResult = Integer.parseInt(subWmCode);

		boolean ValCode = Code101 == CodeResult;

		if (ValCode) {

			System.out.println("Son iguales");
			testCase.addFileEvidenceCurrentStep(subWmCode);

		}

		System.out.println(ValCode);
		System.out.println(CodeResult);

		assertFalse(!ValCode, " No son iguales");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Verificar KeyRing CONSULTA de informacion";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_011_TpeServicio_Verificacion_KeyRing_Consulta_Informacion_test";
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

