package interfaces.OE2_PER;

/*
 * Prueba de regresi贸n para comprobar la no afectaci贸n en la funcionalidad principal de la interface 
 * FEMSA_OE2_PER para generar la forma electr贸nica de comprobante Factura(01) con la informaci贸n 
 * de Oracle Per煤 y  la respuesta Exitosa por parte del proveedor ACEPTA, al ser migrada la 
 * interface de WM9.9 a WM10.5.
 * @autho Ultimo mantenimiento Mariana Vives
 * @date 22/02/2023
 * */

import static org.junit.Assert.assertFalse;
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
import utils.sql.SQLUtil;

public class  ATC_FT_004_OE2_PER_GeneraciondeformaelectronicadecomprobantedeFactura extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_OE2_PER_GeneraciondeformaelectronicadecomprobantedeFactura_test(HashMap<String, String> data)
			throws Exception {
		/**
		 * Proyecto : Actualizacion tecnologica
		 * CP: MTC-FT-012 OE2_PER Generacion de forma electronica exitosa de comprobante 
		 * Descripcion: Prueba de regresion para comprobar la no afectaci髇 en la funcionalidad principal 
		 * de la interface FEMSA_OE2_PER para generar la forma electr髇ica de comprobante Factura(01) con la 
		 * informaci髇 de Oracle Peru y  la respuesta Exitosa por parte del proveedor ACEPTA, al ser migrada la 
		 * interface de WM9.9 a WM10.5.
		 */
		
		/*
		 * 
		 * 
		 * Utilerias
		 *********************************************************************/

		SQLUtil dbPuserPeru = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,
				GlobalVariables.DB_PASSWORD_Oiwmqa);
		SQLUtil dbEbsPeru = new SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ, GlobalVariables.DB_USER_OIEBSBDQ,
				GlobalVariables.DB_PASSWORD_OIEBSBDQ);
		SQLUtil dbLogPeru = new SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,
				GlobalVariables.DB_PASSWORD_Oiwmqa);

		/*  ***********************************************************************/

		// Precondicion

		String Pre = " SELECT operacion, valor1, valor2 \r\n" + "  FROM WMUSER.wm_interfase_config \r\n"
				+ "  WHERE interfase = 'OE2_PER' \r\n";

		// Paso 1
		String tdcPaso1 = "  SELECT ID_FACTURA_DIGITAL, ORIGEN, ANIO, MES, CR_PLAZA, CR_TIENDA,WM_STATUS, VERSION_CFDI, \r\n"
				+ "  WM_RUN_ID, DOCTO_XML \r\n" + "  FROM APPS.XXPE_CFD_FACTURA_DIGITAL \r\n"
				+ "  WHERE ORIGEN = 'IMXAR' \r\n" + "  AND CR_PLAZA = '" + data.get("Plaza") + "' \r\n"
				+ "  AND CR_TIENDA = '" + data.get("Cr_tienda") + "'  \r\n" + "  AND ANIO = '" + data.get("Anio")
				+ "' \r\n" + "  AND MES = '" + data.get("Mes") + "' \r\n" + "  AND WM_STATUS = 'L' \r\n";

		// Paso 3
		String tdcPaso3 = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server \r\n"
				+ " FROM WMLOG.WM_LOG_RUN Tbl \r\n" + " WHERE interface = 'OE2_PER'  \r\n"
				+ " and  start_dt >= TRUNC(SYSDATE) \r\n" + " ORDER BY START_DT DESC) where rownum <=1 \r\n";// Consulta
																												// para
																												// estatus
																												// de la
																												// ejecucion

		// Paso 3
		String tdcPaso3_1 = " Select * from WMLOG.WM_LOG_THREAD \r\n" + " WHERE THREAD_ID = '%s' \r\n";

		// Paso 4
		String tdcPaso4 = "SELECT * FROM WMLOG.WM_LOG_ERROR WHERE RUN_ID='%s'";

		// Paso 5
		String tdcPaso5 = " SELECT * FROM APPS.XXPE_CFD_FACTURA_DIGITAL \r\n" + " WHERE ORIGEN = 'POSMES' \r\n"
				+ " AND CR_PLAZA =  '" + data.get("cr_Plaza") + "'\r\n" + " AND CR_TIENDA = '" + data.get("Cr_tienda")
				+ "' \r\n" + " AND ANIO = '" + data.get("Anio") + "'  \r\n" + " AND MES = '" + data.get("Mes")
				+ "' \r\n" + " AND WM_STATUS = 'E'\r\n" + " AND WM_RUN_ID = %s \r\n";

// Pre ******************************************************************************************************************/

		addStep(" Validar que se tenga la informaci锟絥 en la tabla de configuraci锟絥 \r\n");

		System.out.println(Pre);

		SQLResult Pasopre = dbPuserPeru.executeQuery(Pre);

		boolean ValidaPre = Pasopre.isEmpty();

		if (!ValidaPre) {

			testCase.addQueryEvidenceCurrentStep(Pasopre);
		}

		assertFalse("No se encontro la informacion de configuracion ", ValidaPre);

// Paso1 ******************************************************************************************************************/

		addStep(" Validar la tabla APPS.XXPE_CFD_FACTURA_DIGITAL \r\n");

		System.out.println(tdcPaso1);

		SQLResult Paso1 = dbEbsPeru.executeQuery(tdcPaso1);

		boolean ValidaPaso1 = Paso1.isEmpty();

		if (!ValidaPaso1) {

			testCase.addQueryEvidenceCurrentStep(Paso1);
		}

		assertFalse("No se encontro la informacion en la tabla ", !ValidaPaso1);

//Paso 2 *******************************************************************************************************

		addStep("Ejecutar la interface OE2_PER ");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		System.out.println(contra);
//Paso 3 *******************************************************************************************************

		addStep(" Validar el Log\r\n ");

		System.out.println(tdcPaso3);

		SQLResult Paso3 = dbLogPeru.executeQuery(tdcPaso3);

		String runid = "";

		boolean ValidaPaso3 = Paso3.isEmpty();

		if (!ValidaPaso3) {

			runid = Paso3.getData(0, "Run_id");
			testCase.addQueryEvidenceCurrentStep(Paso3);

		}

		assertFalse("No se encontro la informacion de configuracion ", ValidaPaso3);

//Paso 3_1  *******************************************************************************************************

		addStep(" Validar el Log\r\n ");

		String FormatoPaso3_1 = String.format(tdcPaso3_1, runid);

		System.out.println(FormatoPaso3_1);

		SQLResult Paso3_1 = dbLogPeru.executeQuery(FormatoPaso3_1);

		String threaid = "";
		boolean ValidaPaso3_1 = Paso3.isEmpty();
		System.out.println(ValidaPaso3_1);

		if (!ValidaPaso3_1) {

			threaid = Paso3_1.getData(0, "THREAD_ID");
			testCase.addQueryEvidenceCurrentStep(Paso3_1);
		}

		assertFalse("No se encontro la informacion de configuracion ", ValidaPaso3_1);

//Paso 4 *******************************************************************************************************   

		addStep("Validar que la interface finalice con error en las tablas del  WMLOG.\r\n");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String tdcQueryPaso4 = null;
		System.out.println(tdcQueryPaso4);
		SQLResult paso4 = executeQuery(dbLogPeru, tdcPaso4);
		String thread = "";
		boolean ValidaPaso4 = paso4.isEmpty();
		if (!ValidaPaso4) {
			thread = paso4.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(paso4);

		}

		System.out.println(ValidaPaso4);

		assertFalse(ValidaPaso4, "No se obtiene informacion de la consulta");

//Paso 5 *******************************************************************************************************

		addStep(" Validar la actualizacion del estatus tabla APPS.XXPE_CFD_FACTURA_DIGITAL \r\n ");

		String FormatoPaso5 = String.format(tdcPaso5, threaid);

		System.out.println(FormatoPaso5);

		SQLResult Paso5 = dbPuserPeru.executeQuery(FormatoPaso5);

		boolean ValidaPaso5 = Paso5.isEmpty();

		if (!ValidaPaso5) {

			testCase.addQueryEvidenceCurrentStep(Paso5);
		}

		assertFalse("No se encontro la informacion de configuracion ", ValidaPaso5);

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
		return "MTC-FT-012 OE2_PER Generacion de forma";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO DE AUTOMATIZACION";
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
