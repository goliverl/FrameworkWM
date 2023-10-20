package interfaces.TpeServicio;

import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;


public class ATC_FT_001_TpeServicio_Obtener_Servicio_Echo_Nivel_0 extends BaseExecution{
	
	/*
	 * 
	 * @cp Obtener servicio de echo nivel 0
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_TpeServicio_Obtener_Servicio_Echo_Nivel_0_test(HashMap<String, String> data) throws Exception {
	
/** UTILERIA *********************************************************************/	
		
/** VARIABLES *********************************************************************/	
				
		
		/**
		 * PASOS DEL CASO DE PRUEBA
		 *********************************************************************/

		/* PASO 1 *********************************************************************/

		addStep("Ejecutar la interface con los parametros indicados ");

		
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");

		SeleniumUtil u;
		PakageManagment pok;
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		System.out.println(contra);
	
		String WmCode = pok.runIntefaceWMXml(data.get("interfase"), data.get("servicio"),null);
		
		System.out.println(WmCode);

		/* PASO 2 *********************************************************************/

		addStep(" Verificar que la ejecucion haya terminado con exito.");
		
		int Code101 = 0;
		int CodeResult = 0;
		String SubWmCode = WmCode.substring(39,40);

		System.out.println(SubWmCode);
		
		CodeResult = Integer.parseInt(SubWmCode);

		boolean ValCode = Code101 == CodeResult;

		if (ValCode) {

			System.out.println("Son iguales");
			System.out.println(CodeResult);
			testCase.addFileEvidenceCurrentStep(SubWmCode);

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
		return "Obtener servicio de echo nivel 0";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_TpeServicio_Obtener_Servicio_Echo_Nivel_0_test";
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
