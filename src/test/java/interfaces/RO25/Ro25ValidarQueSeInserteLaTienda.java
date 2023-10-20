package interfaces.RO25;

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

public class Ro25ValidarQueSeInserteLaTienda extends BaseExecution {
	public String tipo;

	/*
	 * comentario
	 */
	@Test(dataProvider = "data-provider")
	public void ATC_FT_RO25_002_Validar_Que_Se_Inserte_la_Tienda(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbFCIASQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCIASQA,
				GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		/*
		 * Variables
		 *************************************************************************/

		String tdcQueryPaso1 = " SELECT COUNT(ID_TIENDA) FROM T_TIENDAS " + " WHERE CR_SUPERIOR = '" + data.get("Plaza")
				+ "' AND " + " CR_TIENDA = '51PUN1'";

		String tdcQueryPaso2 = " SELECT * FROM T_PLAZAS WHERE CR_PLAZA ='11SYG' ";

		String tdcQueryPaso4 = " SELECT EMAIL, A.GROUP_ID, GROUP_NAME, INTERFACE_NAME, NAME FROM WMLOG.WM_LOG_GROUP A, "
				+ " WMLOG.WM_LOG_USER_GROUP B, WMLOG.WM_LOG_USER C" + " WHERE A.GROUP_ID = B.GROUP_ID "
				+ " AND B.USER_ID = C.USER_ID " + " AND A.GROUP_NAME = 'ORAFIN' " + " AND A.INTERFACE_NAME = 'RO25' "
				+ " AND C.PLAZA IS NULL ";

		String tdcQueryPaso5 = " SELECT *FROM T_TIENDAS WHERE CR_SUPERIOR = '" + data.get("Plaza") + "' AND "
				+ "CR_TIENDA = '" + data.get("Tienda") + "'";

		String tdcQueryPaso6 = " SELECT * FROM WMLOG.WM_LOG_RUN " + " WHERE INTERFACE = 'RO25'" + " AND STATUS = 'S'"
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE)" + " ORDER BY RUN_ID DESC";

		String tdcQueryPaso7 = "SELECT * FROM T_TIENDAS" + " WHERE CR_SUPERIOR = '" + data.get("Plaza") + "' AND "
				+ " CR_TIENDA = '" + data.get("Tienda") + "'";

		/*
		 * //En el paso 5 de ALM, se meciona un Insert, el cual lo cambie por un Select.
		 */

		SeleniumUtil u;
		PakageManagment pok;
		String user = data.get("User");
		String ps = PasswordUtil.decryptPassword(data.get("Ps"));
		String server = data.get("Server");

		/*
		 * Pasos
		 *****************************************************************************/

		/// Paso 1 ***************************************************

		addStep("Que NO exista la Tienda y Plaza en la tabla T_TIENDAS en la BD CNT");

		
		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		
		System.out.println(tdcQueryPaso1);

		SQLResult Paso1 = dbFCIASQA.executeQuery(tdcQueryPaso1);

		boolean Paso1Empty = Paso1.isEmpty();

		System.out.println(Paso1Empty);

		testCase.addQueryEvidenceCurrentStep(Paso1);

		assertFalse(Paso1Empty, "No se tiene informacion en la base datos");

		/// Paso 2 ***************************************************

		addStep("Que exista la Plaza  en la tabla T_PLAZAS en la BD CNT.");

		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		
		System.out.println(tdcQueryPaso2);

		SQLResult paso2 = dbFCIASQA.executeQuery(tdcQueryPaso2);

		boolean Paso2Empty = paso2.isEmpty();

		System.out.println(Paso2Empty);

		testCase.addQueryEvidenceCurrentStep(paso2);

		assertFalse(Paso2Empty, "No se tiene informacion en la base de datos");

		/// Paso 3 ***************************************************

		addStep("Comprobar que el adapter notification RO25.DB.ORAFIN:adpNotInsXxffcCentroResponsabilidad este habilitado.");

		/// Paso 4 *****************************************************

		addStep(" Comprobar la dirección de email para recibir las natificaciones en las tablas WM_LOG_GROUP, WM_LOG_USER_GROUP y "
				+ "WM_LOG_USER para el GROUP_NAME igual a ORAFIN y INTERFACE_NAME igual a RO25 en la BD WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		System.out.println(tdcQueryPaso4);

		SQLResult Paso4 = executeQuery(dbLog, tdcQueryPaso4);

		boolean Paso4Empty = Paso4.isEmpty();

		System.out.println(Paso4Empty);

		testCase.addQueryEvidenceCurrentStep(Paso4);

		assertFalse(Paso4Empty, "No se tiene informacion en la base datos");

		// Paso 5 ******************************************************

		addStep(" Se ejecuta el servicio de RO25.pub:runNewCr");

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);

		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";

		u.get(contra);

		u.hardWait(4);

		pok.runIntefaceWmOneButton(data.get("Interface"), data.get("Servicio"));

		// Paso 5 ******************************************************

		addStep("Se Valida un nuevo registro en la tabla XXFC_CENTROS_RESPONSABILIDAD en la BD ORAFIN.");


		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		
		System.out.println(tdcQueryPaso5);

		SQLResult Paso5 = executeQuery(dbFCIASQA, tdcQueryPaso5);

		Boolean Paso5Empty = Paso5.isEmpty();

		System.out.print(Paso5Empty);

		assertFalse(Paso5Empty, "No se tiene informacion en la base datos");

		/// Paso 6 ***************************************************

		addStep("Se valida en la tabla WM_LOG_RUN de la BD WMLOG que la interface RO25 haya finalizado con status igual a S.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		
		System.out.print(tdcQueryPaso6);
		System.out.print("\n");
		SQLResult Paso6 = executeQuery(dbLog, tdcQueryPaso6);

		boolean Paso6Empty = Paso6.isEmpty();

		System.out.print(Paso6Empty);

		testCase.addQueryEvidenceCurrentStep(Paso6);

		assertFalse(Paso6Empty, "No se tiene informacion en la base datos");

		/// Paso 7 ***************************************************


		addStep("Se valida la inserción en la tabla T_TIENDAS de la BD CNT donde CR_SUPERIOR es igual a 11SYG y CR_TIENDA es igual a 51PUN.");
		
		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		
		System.out.println(tdcQueryPaso7);

		SQLResult Paso7 = executeQuery(dbFCIASQA, tdcQueryPaso7);

		boolean Paso7Empty = Paso7.isEmpty();

		System.out.println(Paso7Empty);

		testCase.addQueryEvidenceCurrentStep(Paso7);

		assertFalse(Paso7Empty, "No se tiene informacion en la base datos");

		/// Paso 8 ***************************************************

	
		addStep("Validar que se haya recibido el correo.");

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
	}

	@Override
	public String setTestDescription() {
		return "Terminado.El script es para validar que se inserte la Plaza y Tienda.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatización";
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_RO25_002_Validar_Que_Se_Inserte_la_Tienda";
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
