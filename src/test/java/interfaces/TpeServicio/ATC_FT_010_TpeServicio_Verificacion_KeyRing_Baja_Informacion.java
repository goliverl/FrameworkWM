package interfaces.TpeServicio;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ATC_FT_010_TpeServicio_Verificacion_KeyRing_Baja_Informacion extends BaseExecution{
	
	/*
	 * 
	 * @cp Verificar KeyRing BAJA de informacion
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_010_TpeServicio_Verificacion_KeyRing_Baja_Informacion_test (HashMap<String, String> data) throws Exception {
	
/** UTILERIA *********************************************************************/	
		
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,
				GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);

		
/** VARIABLES *********************************************************************/	
				
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String tdcPaso3 = " SELECT * FROM POSREP.tpe_fcp_freq_data "
				+ " WHERE code = '"+data.get("Phone")+"' ";

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
		String WmCode = pok.runIntefaceWmWithcuatroInputxml(data.get("interfase"), data.get("servicio"), data.get("folio"),
				data.get("Folio"), data.get("Plaza"), data.get("plaza"), data.get("Tienda"), data.get("tienda"),
				data.get("Phone"), data.get("telefono"));
		
		
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

		/* PASO 3 *********************************************************************/

		addStep("Verificar la insercion del registro en la tabla TPEUSER.TPE_FCP_FREQ_DATA\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMQA_NUEVA);
		System.out.println(tdcPaso3);

		SQLResult Paso1 = executeQuery(dbPos, tdcPaso3);
		boolean ValPaso1 = Paso1.isEmpty();

		if (!ValPaso1) {

			testCase.addQueryEvidenceCurrentStep(Paso1);

		}

		System.out.println(ValPaso1);

		assertFalse(ValPaso1, " No se muestran registros a procesar ");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Verificar KeyRing BAJA de informacion";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_010_TpeServicio_Verificacion_KeyRing_Baja_Informacion_test";
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
