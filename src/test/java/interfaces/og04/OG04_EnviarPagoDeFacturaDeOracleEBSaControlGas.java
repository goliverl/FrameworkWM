package interfaces.og04;

import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;

public class OG04_EnviarPagoDeFacturaDeOracleEBSaControlGas extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_OG04_001_EnviarPagoDeFacturaDeOracleEBSaControlGas(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbFCFINQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_EBS, GlobalVariables.DB_USER_EBS,
				GlobalVariables.DB_PASSWORD_EBS);
		utils.sql.SQLUtil dbFCIASQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCIASQA,
				GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		/*
		 * Variables
		 *************************************************************************/

		String tdcQueryPaso1 = " SELECT * FROM xxog.xxog_ar_recibos_h " 
		        +" WHERE id_site = 136417 AND "
				+" wm_status = 'L' ";

		String tdcQueryPaso2 = " SELECT * FROM xxog.xxog_ar_recibos_l "
				+ " WHERE id_recibo = %s AND " 
				+ " wm_status = 'L' ";

		String tdcQueryPaso4 = " SELECT * FROM wmlog.wm_log_run " 
		        + " WHERE interface = 'OG04' "
				+ " AND TRUNC (start_dt) = TRUNC (SYSDATE) " 
		        + " AND status = 'S' " 
				+ " ORDER BY 3 DESC ";

		String tdcQueryPaso5 = " SELECT * FROM wmlog.wm_log_thread" 
		        + " WHERE parent_id = [wm_log_run.run_id] AND "
				+ " name like 'OG04 - ' || [xxog_ar_recibidos_h.id_recibo]";

		String tdcQueryPaso6 = " SELECT * FROM gas_ar_recibos_h " 
		        + " WHERE id_site = '136417' AND "
				+ " CONVERT(varchar(10), wm_fecha, 103) = CONVERT(varchar(10), GETDATE(), 103) AND"
				+ " wm_status = 'L'";

		String tdcQueryPaso7 = " SELECT * FROM gas_ar_recibos_l "
				+ " WHERE id_recibo = [gas_ar_recibidos_h id_recibo] AND "
				+ " CONVERT(varchar(10), wm_fecha, 103) = CONVERT(varchar(10), GETDATE(), 103) AND "
				+ "wm_status = 'L'";

		String tdcQueryPaso8 = " SELECT * FROM xxog.xxog_ar_recibos_h " 
		        + " WHERE id_site = 136417 AND "
				+ " wm_status = 'E' AND " 
		        + " TRUNC(wm_fecha) = TRUNC(SYSDATE) ";

		String tdcQueryPaso9 = " SELECT * FROM xxog.xxog_ar_recibos_l "
				+ " WHERE id_recibo = [xxog_ar_recibos_h.idrecibo] AND " 
				+ " wm_status = 'E' AND "
				+ " TRUNC(wm_fecha) = TRUNC(SYSDATE) ";

		SeleniumUtil u;
		PakageManagment pok;
		String user = data.get("User");
		String ps = PasswordUtil.decryptPassword(data.get("Ps"));
		String server = data.get("Server");


		/*
		 * Pasos
		 *****************************************************************************/

		/// Paso 1 ***************************************************

		addStep("Tener informacion pendiente de enviar en la tabla xxog_ar_recibos_h de ORAFIN para el id_site");

		System.out.println(GlobalVariables.DB_HOST_EBS);

		System.out.println(tdcQueryPaso1);

		SQLResult Paso1 = dbFCFINQA.executeQuery(tdcQueryPaso1);
		
		String id_recibo = Paso1.getData(0, "id_recibo");

		boolean Paso1Empty = Paso1.isEmpty();

		System.out.println(Paso1Empty);
		
		if (!Paso1Empty) {
			
			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

		assertFalse(Paso1Empty, "No se tiene informacion en la base datos");

		/// Paso 2 ***************************************************

		addStep("Validar que exista detalle del pago de facturas correspondientes al site 136417 en la tabla xxog_ar_recibos_l de ORAFIN");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		String Paso2Format = String.format(tdcQueryPaso2, id_recibo);
		SQLResult paso2 = dbFCFINQA.executeQuery(Paso2Format);

		System.out.println(Paso2Format);
		
		boolean Paso2Empty = paso2.isEmpty();

		System.out.println(Paso2Empty);

		if (!Paso2Empty) {

			testCase.addQueryEvidenceCurrentStep(paso2);
		}

		assertFalse(Paso2Empty, "No se tiene informacion en la base de datos");

		/// Paso 3 ***************************************************

		addStep("Ejecutar el servicio OG04.Pub:runManual. El servicio será ejecutado con el job runOG04 desde Ctrl-M.");

		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		u.hardWait(4);
		pok.runIntefaceWmOneButton(data.get("Interface"), data.get("Servicio"));

		/// Paso 4 *****************************************************

		addStep(" Validar la correcta ejecución de la interface OG04 en la tabla wm_log_run de WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);

		System.out.println(tdcQueryPaso4);

		SQLResult Paso4 = executeQuery(dbLog, tdcQueryPaso4);

		boolean Paso4Empty = Paso4.isEmpty();

		System.out.println(Paso4Empty);

		if (!Paso4Empty) {

			testCase.addQueryEvidenceCurrentStep(Paso4);
		}

		assertFalse(Paso4Empty, "No se tiene informacion en la base datos");

		// Paso 5 ******************************************************

		addStep("Revisar en la tabla wm_log_thread que exista un registro por cada pago de factura procesada.");

		System.out.println(GlobalVariables.DB_HOST_Ebs);


		System.out.println(tdcQueryPaso5);

		SQLResult Paso5 = executeQuery(dbFCFINQA, tdcQueryPaso5);

		String Descripcion = Paso5.getData(0, "DESCRIPTION");

		Boolean Paso5Empty = Paso5.isEmpty();
		System.out.print(Paso5Empty);

		if (!Paso5Empty) {

			testCase.addQueryEvidenceCurrentStep(Paso5);
		}

		assertFalse(Paso5Empty, "No se tiene informacion en la base datos");

		/// Paso 6 ***************************************************

		addStep("Validar que el encabezado del pago de factura ha sido insertado en la tabla gas_ar_recibos_h de "
				+ "OXXOGAS para el id_site 136417");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.print(tdcQueryPaso6);
		System.out.print("\n");
		SQLResult Paso6 = executeQuery(dbLog, tdcQueryPaso6);

		boolean Paso6Empty = Paso6.isEmpty();

		System.out.print(Paso6Empty);

		testCase.addQueryEvidenceCurrentStep(Paso6);

		assertFalse(Paso6Empty, "No se tiene informacion en la base datos");

		/// Paso 7 ***************************************************

		addStep("Validar que se insertó el detalle de las facturas del id_site 136417 en gas_ar_recibos_l de la "
				+ "BD de OXXOGAS.");

		String Formato7 = String.format(tdcQueryPaso7, Descripcion);

		System.out.println(Formato7);

		SQLResult Paso7 = executeQuery(dbFCIASQA, Formato7);

		boolean Paso7Empty = Paso7.isEmpty();

		System.out.println(Paso7Empty);

		if (!Paso7Empty) {

			testCase.addQueryEvidenceCurrentStep(Paso7);
		}

		assertFalse(Paso7Empty, "No se tiene informacion en la base datos");

		/// Paso 8 ***************************************************

		addStep("Validar que en el sistema origen, el estatus del pago de factura halla sido actualizado a 'E', "
				+ "así como los campos wm_fecha, last_update_datey fecha_actualizacion con la fecha actual en "
				+ "la tabla xxog_ar_recibos_h en la BD de ORAFIN.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.print(tdcQueryPaso8);
		System.out.print("\n");
		SQLResult Paso8 = executeQuery(dbLog, tdcQueryPaso8);

		boolean Paso8Empty = Paso8.isEmpty();

		System.out.println(Paso7Empty);

		if (!Paso8Empty) {

			testCase.addQueryEvidenceCurrentStep(Paso8);
		}

		assertFalse(Paso8Empty, "No se tiene informacion en la base datos");

		/// Paso 9 ***************************************************

		addStep("Por último validar que se actualice también los campos wm_status='E', wm_fecha, last_update_date,"
				+ "fecha_actualizacion con la fecha actual en la tabla xxog_ar_recibos_l de ORAFIN para las líneas "
				+ "de los pagos de factura procesados.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.print(tdcQueryPaso6);
		System.out.print("\n");
		SQLResult Paso9 = executeQuery(dbLog, tdcQueryPaso9);

		boolean Paso9Empty = Paso9.isEmpty();

		System.out.println(Paso9Empty);

		if (!Paso9Empty) {

			testCase.addQueryEvidenceCurrentStep(Paso9);
		}

		assertFalse(Paso9Empty, "No se tiene informacion en la base datos");

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
		return "Construido. Enviar la información de pagos de facturas con respecto al id_site ID_NUM del sistema Oracle EBS AR hacia el sistema ControlGas.\n"
				+ "";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_OG04_001_EnviarPagoDeFacturaDeOracleEBSaControlGas";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}
}
