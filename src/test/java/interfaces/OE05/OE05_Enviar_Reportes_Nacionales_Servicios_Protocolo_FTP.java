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

public class OE05_Enviar_Reportes_Nacionales_Servicios_Protocolo_FTP extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_OE05_004_Enviar_Reportes_Nacionales_Servicios_Protocolo_FTP(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
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

		String TdcQueryPaso1 = " SELECT trim(VALOR1),trim(VALOR2),trim(VALOR5), trim(VALOR6) \r\n"
				+ " FROM wm_interfase_config \r\n"
				+ " WHERE INTERFASE = 'OE05' \r\n"
				+ " AND OPERACION IN ('FS') \r\n"
				+ " AND CATEGORIA = 'CONFIG' \r\n";
				
				
		String TdcQueryPaso2 = " SELECT DISTINCT A.ps_forma_pago_id, A.fecha_administrativa,  A.servicio seviceId, b.service_type servicetype, \r\n"
				+ " b.cr_plaza plaza, A.estatus FROM XXFC_PAGO_SERVICIO_FORMAS_STG A, XXFC_SERV_VENDOR_COMM_DATA_V b \r\n"
				+ " WHERE A.ESTATUS ='I' \r\n"
				+ " AND b.ENVIAR_SERVICIO='S' \r\n"
				+ " AND UPPER(b.SERVICIO_ACEPTA_MPE)='SI' \r\n"
				+ " AND upper(b.servicio_factura_mpe)='SI' \r\n"
				+ " AND a.fecha_administrativa >= trunc(sysdate - 30) \r\n"
				+ " AND A.FECHA_ADMINISTRATIVA <= TRUNC(sysdate) \r\n"
				+ " AND b.SERVICE_TYPE='N' \r\n"
				+ " AND A.SERVICIO=b.SERVICE_ID \r\n"
				+ " AND a.servicio not in (select item from wm_oe05_outbound_docs where trunc(fecha_creacion) = trunc(sysdate)) AND b.protocol = 'FTP' \r\n";
				

		String TdcQueryPaso4 = " SELECT * FROM wm_log_run \r\n"
				+ " WHERE interface = 'OE05 - Nacional' \r\n"
				+ " AND status= 'S' \r\n"
				+ " AND start_dt >= trunc(sysdate) \r\n "
				+ " ORDER BY start_dt DESC \r\n";
				
				
		String TdcQueryPaso5 = " SELECT * FROM xxfc_lotes WHERE servicio = %s AND fecha = TRUNC(SYSDATE);";
				
		String TdcQueryPaso6 = "SELECT * FROM xxfc_pago_servicio_formas_stg \r\n"
				+ " WHERE PS_FORMA_PAGO_ID = %s \r\n"
				+ " AND FECHA_ADMINISTRATIVA = %s\r\n"
				+ " AND LOTE= %s \r\n"
				+ " AND FECHA_TRANSMISION = TRUNC(SYSDATE) \r\n"
				+ " AND ESTATUS='E' \r\n";
				
				
		String TdcQueryPaso7 = " SELECT * FROM xxfc_pago_servicio_formas "
				+ " WHERE PS_FORMA_PAGO_ID = %s "
				+ " AND FECHA_ADMINISTRATIVA = %s "
				+ " AND LOTE= %s "
				+ " AND FECHA_TRANSMISION = TRUNC(SYSDATE) "
				+ " AND ESTATUS='E' \r\n";
				

		String TdcQueryPaso8 = " SELECT * FROM wm_oe05_outbound_docs \r\n"
				+ " WHERE FECHA_CREACION >= TRUNC(SYSDATE) \r\n"
				+ " AND LOTE= %s \r\n"
				+ " AND ITEM = %s \r\n"
				+ " AND ESTATUS = 'E' \r\n";
				
		String TdcQueryPaso9 = " SELECT b.SERVICE_TYPE serviceType, b.OUTPUT_FORMAT_ID outputFormatId, b.PROTOCOL protocol, \r\n"
				+ " b.ATTRIBUTE1 ipFTP, b.ATTRIBUTE2 portFTP, b.ATTRIBUTE3 userFTP, b.ATTRIBUTE4 passFTP, b.ATTRIBUTE5 pathFTP, \r\n"
				+ " b.ATTRIBUTE6 emailSupplier, b.ATTRIBUTE7 emailUser, hp.PARTY_NAME vendorDesc, b.VENDOR_ID vendorId, b.LEGACY_ID \r\n"
				+ " legacyId, b.SERVICE_ID serviceId FROM XXFC_SERV_VENDOR_COMM_DATA_V b, ap_suppliers pav, hz_parties hp \r\n"
				+ " WHERE pav.party_id = hp.party_id \r\n"
				+ " AND b.vendor_id = pav.vendor_id(+) \r\n"
				+ " AND SERVICE_ID = %s \r\n";
				
				
		/**
		 * 
		 * **********************************Pasos del caso de Prueba *
		 * *****************************************
		 * 
		 * 
		 */

		// Paso 1 ************************

		addStep("Validar la configuración FTP y path de archivos temporales.\r\n");
				
		System.out.println(GlobalVariables.DB_HOST_AVEBQA);
		System.out.println(TdcQueryPaso1);

		SQLResult estadoFacturas = executeQuery(dbFCWM, TdcQueryPaso1);
		boolean bolFacturas = estadoFacturas.isEmpty();

		if (!bolFacturas) {

			testCase.addQueryEvidenceCurrentStep(estadoFacturas);

		}

		assertFalse(bolFacturas, "No se cuenta con facturas emitidas");

		// Paso 2 ************************

		addStep("Validar que existan reportes de servicios pendientes de enviar para la plaza 10MON mayor o igual a 30 días de antigüedad.\r\n");

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

		addStep("Ejecutar el servicio OE05.Pub:runNacional.\r\n");
				
		
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
		
		String FormatoPaso5 = String.format(servicio, TdcQueryPaso5);
		
		System.out.println(FormatoPaso5);

		SQLResult paso5 = executeQuery(dbAvebqa, FormatoPaso5);
		
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

		addStep("Validar la eliminación del archivo WM_OE05_OUTBOUND_DOCS.NOMBRE_ARCHIVO en el fileSystem.\r\n");

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
		return "Construido. Enviar los reportes Nacionales de los servicios con protocolo FTP";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "QA Automation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_OE05_004_Enviar_Reportes_Nacionales_Servicios_Protocolo_FTP";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
