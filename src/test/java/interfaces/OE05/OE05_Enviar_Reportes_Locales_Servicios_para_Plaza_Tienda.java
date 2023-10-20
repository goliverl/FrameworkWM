package interfaces.OE05;

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

public class OE05_Enviar_Reportes_Locales_Servicios_para_Plaza_Tienda extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_OE05_001_Enviar_Reportes_Locales_Servicios_Plaza_Tienda(HashMap<String, String> data) throws Exception {

		/*
		 * Utiler�as
		 *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,
				GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);

		utils.sql.SQLUtil dbFCWM = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,
				GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		
		utils.sql.SQLUtil dbAvebqa = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,
				GlobalVariables.DB_PASSWORD_AVEBQA);

		/**
		 * Variables
		 * ******************************************************************************************
		 * *
		 * 
		 */

		String TdcQueryPaso1 = "SELECT trim(VALOR1),trim(VALOR2),trim(VALOR5), trim(VALOR6) \r\n"
				+ " FROM WMUSER.wm_interfase_config \r\n"
				+ " WHERE INTERFASE = 'OE05' \r\n"
				+ " AND OPERACION IN ('FS') \r\n"
				+ " AND CATEGORIA = 'CONFIG' \r\n";
				
		String TdcQueryPaso2 = " SELECT DISTINCT a.ps_forma_pago_id, a.fecha_administrativa,  a.servicio seviceId, b.service_type servicetype, b.cr_plaza plaza \r\n"
				+ " FROM wmuser.xxfc_pago_servicio_formas_stg a, wmuser.xxfc_serv_vendor_comm_data_v b \r\n"
				+ " WHERE a.estatus ='I' \r\n"
				+ " AND b.enviar_servicio='S' \r\n"
				+ " AND UPPER(b.servicio_acepta_mpe)='SI' \r\n"
				+ " AND UPPER(b.servicio_factura_mpe)='SI' \r\n"
				+ " AND a.fecha_administrativa >= TRUNC(SYSDATE - 30) \r\n"
				+ " AND A.fecha_administrativa <= TRUNC(SYSDATE + 1) \r\n"
				+ " AND a.plaza = '"+ data.get("") +"' \r\n"
				+ " AND b.service_type='L' \r\n"
				+ " AND a.servicio=b.service_id \r\n"
				+ " AND b.cr_plaza=A.plaza \r\n"
				+ " AND a.servicio NOT IN (SELECT item FROM wm_oe05_outbound_docs WHERE TRUNC(fecha_creacion) = TRUNC(SYSDATE)) \r\n";

		String TdcQueryPaso4 = "SELECT * FROM wmlog.wm_log_run \r\n"
				+ " WHERE interface = 'OE05 - Local' \r\n"
				+ " and status= 'S' \r\n"
				+ " and start_dt >= trunc(sysdate) \r\n"
				+ " ORDER BY start_dt DESC \r\n";
				
		String TdcQueryPaso5 = " SELECT * FROM wmuser.xxfc_lotes WHERE plaza = '" +data.get("") + "'"
				+ " AND fecha >= TRUNC(SYSDATE) \r\n";
				
		String TdcQueryPaso6 = "SELECT * FROM wmuser.xxfc_pago_servicio_formas_stg \r\n"
				+ " WHERE PS_FORMA_PAGO_ID = %s \r\n"
				+ " AND FECHA_ADMINISTRATIVA = %s \r\n"
				+ " AND LOTE= %s \r\n"
				+ " AND FECHA_TRANSMISION = TRUNC(SYSDATE) \r\n"
				+ " AND ESTATUS='E' \r\n"
				+ " AND plaza =  '" +data.get("")+ "' \r\n";
				
		String TdcQueryPaso7 = " SELECT *  FROM wmuser.xxfc_pago_servicio_formas \r\n"
				+ " WHERE PS_FORMA_PAGO_ID = %s \r\n"
				+ " AND FECHA_ADMINISTRATIVA = %s \r\n"
				+ " AND LOTE= %s \r\n"
				+ " AND FECHA_TRANSMISION = TRUNC(SYSDATE) \r\n"
				+ " AND ESTATUS='E' \r\n";

		String TdcQueryPaso8 = " SELECT *  FROM wmuser.xxfc_pago_servicio_formas \r\n"
				+ " WHERE PS_FORMA_PAGO_ID = %s \r\n"
				+ " AND FECHA_ADMINISTRATIVA = %s \r\n"
				+ " AND LOTE= %s \r\n"
				+ " AND FECHA_TRANSMISION = TRUNC(SYSDATE) \r\n"
				+ " AND ESTATUS='E' \r\n";
		
		String TdcQueryPaso9 = " SELECT b.ATTRIBUTE6 emailSupplier \r\n"
				+ " FROM wmuser.XXFC_SERV_VENDOR_COMM_DATA_V b, wmuser.ap_suppliers pav, wmuser.hz_parties hp \r\n"
				+ " WHERE pav.party_id = hp.party_id \r\n"
				+ " AND b.vendor_id = pav.vendor_id(+) \r\n"
				+ " AND b.CR_PLAZA = '"+data.get("")+"' \r\n"
				+ " AND SERVICE_ID = %s \r\n";
				
		/**
		 * 
		 * **********************************Pasos del caso de Prueba *
		 * *****************************************
		 * 
		 * 
		 */

		// Paso 1 ************************

		addStep("Validar la configuraci�n FTP y path de archivos temporales.\r\n");

		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(TdcQueryPaso1);

		SQLResult estadoFacturas = executeQuery(dbFCWM, TdcQueryPaso1);
		boolean bolFacturas = estadoFacturas.isEmpty();

		if (!bolFacturas) {

			testCase.addQueryEvidenceCurrentStep(estadoFacturas);

		}

		assertFalse(bolFacturas, "No se cuenta con facturas emitidas");

		// Paso 2 ************************

		addStep("Validar que existan reportes de servicios pendientes de enviar para la plaza 10MON mayor o igual a 30 d�as de antig�edad.\r\n");

		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(TdcQueryPaso2);

		SQLResult paso2 = executeQuery(dbAvebqa, TdcQueryPaso1);
		
		String PS_FORMA_PAGO_ID = "";
		String FECHA_ADMINISTRATIVA = "";
		String servicio = "";
		
		boolean BolPaso2 = estadoFacturas.isEmpty();

		if (!BolPaso2) {
			paso2.getData(0, "PS_FORMA_PAGO_ID");
			paso2.getData(0, "FECHA_ADMINISTRATIVA");
			paso2.getData(0,servicio);
			testCase.addQueryEvidenceCurrentStep(paso2);

		}

		assertFalse(BolPaso2, "No se cuenta con facturas emitidas");

		// Paso 3 ************************

		addStep("Ejecutar el servicio OE05.Pub:runLocal.\r\n");
		
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555"; 
		System.out.println(contra);
		u.get(contra);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
	

		// Paso 4 ************************

		addStep("Validar que el registro de la tabla wm_log_run termine en estatus 'S'.\r\n");

		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(TdcQueryPaso4);

		SQLResult paso4 = executeQuery(dbLog, TdcQueryPaso4);
		boolean BolPaso4 = estadoFacturas.isEmpty();

		if (!BolPaso4) {

			testCase.addQueryEvidenceCurrentStep(paso4);

		}

		assertFalse(BolPaso4, "No se cuenta con facturas emitidas");

		// Paso 5 ************************

		addStep("Validar que se registren los lotes de los servicios enviados en la tabla XXFC_LOTES.\r\n");
		
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(TdcQueryPaso5);

		SQLResult paso5 = executeQuery(dbAvebqa, TdcQueryPaso5);
		
		String LOTE = "";
		
		boolean BolPaso5 = estadoFacturas.isEmpty();

		if (!BolPaso5) {
			 paso5.getData(0, "LOTE");
			testCase.addQueryEvidenceCurrentStep(paso5);

		}

		assertFalse(BolPaso5, "No se cuenta con facturas emitidas");

		// Paso 6 ************************

		addStep("Validar que el campo ESTATUS de la tabla XXFC_PAGO_SERVICIO_FORMAS_STG sea actualizado a E.\r\n");
		
		
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		
		String FormatoPaso6 = String.format(PS_FORMA_PAGO_ID,FECHA_ADMINISTRATIVA,LOTE, TdcQueryPaso6);
		System.out.println(FormatoPaso6);

		SQLResult paso6 = executeQuery(dbAvebqa, FormatoPaso6);
		boolean BolPaso6 = estadoFacturas.isEmpty();

		if (!BolPaso6) {

			testCase.addQueryEvidenceCurrentStep(paso6);

		}

		assertFalse(BolPaso6, "No se cuenta con facturas emitidas");

		// Paso 7 ************************

		addStep("Validar que el campo ESTATUS de la tabla XXFC_PAGO_SERVICIO_FORMAS sea actualizado a E.");
		
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		
		String FormatoPaso7 = String.format(PS_FORMA_PAGO_ID,FECHA_ADMINISTRATIVA,LOTE, TdcQueryPaso7);
		
		System.out.println(FormatoPaso7);

		SQLResult paso7 = executeQuery(dbAvebqa, FormatoPaso7);
		boolean BolPaso7 = estadoFacturas.isEmpty();

		if (!BolPaso7) {

			testCase.addQueryEvidenceCurrentStep(paso7);

		}

		assertFalse(BolPaso7, "No se cuenta con facturas emitidas");

		// Paso 8 ************************

		addStep("Validar el registro en bitacora de los lotes enviados en la tabla WM_OE05_OUTBOUND_DOCS.\r\n");
		
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		
		String FormatoPaso8 = String.format(LOTE,servicio, TdcQueryPaso8);
		
		System.out.println(FormatoPaso8);

		SQLResult paso8 = executeQuery(dbAvebqa, FormatoPaso8);
		boolean BolPaso8 = estadoFacturas.isEmpty();

		if (!BolPaso8) {

			testCase.addQueryEvidenceCurrentStep(paso8);

		}

		assertFalse(BolPaso8, "No se cuenta con facturas emitidas");


		// Paso 9 ************************

		addStep("Validar el registro en bitacora de los lotes enviados en la tabla WM_OE05_OUTBOUND_DOCS.\r\n");
		
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		
		String FormatoPaso9 = String.format( servicio,TdcQueryPaso9);
		System.out.println(FormatoPaso9);

		SQLResult paso9 = executeQuery(dbAvebqa, FormatoPaso9);
		boolean BolPaso9 = estadoFacturas.isEmpty();

		if (!BolPaso9) {

			testCase.addQueryEvidenceCurrentStep(paso9);

		}

		assertFalse(BolPaso9, "No se cuenta con facturas emitidas");

		// Paso 10 ************************

		addStep("Validar la eliminaci�n del archivo WM_OE05_OUTBOUND_DOCS.NOMBRE_ARCHIVO en el fileSystem.\r\n");

//Paso 6

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
		return "Construido. Enviar los reportes Locales de los servicios para la plaza asignada";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_OE05_001_Enviar_Reportes_Locales_Servicios_Plaza_Tienda";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
