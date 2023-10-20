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

public class OE05_Reenviar_Reportes_Locales_Servicio_Finalizar_Estatus_F_Protocolo_MAIL extends BaseExecution{
	 

		@Test(dataProvider = "data-provider")
		public void ATC_FT_OE05_006_Reenviar_Reportes_Locales_Servicio_Finalizar_Estatus_F_Protocolo_MAIL(HashMap<String, String> data) throws Exception {

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
					
					
					
			String TdcQueryPaso2 = " SELECT d.*, s.protocol FROM WM_OE05_OUTBOUND_DOCS d, XXFC_SERV_VENDOR_COMM_DATA_V s \r\n"
					+ " WHERE d.item = s.service_id  \r\n"
					+ " AND d.estatus = 'F' \r\n"
					+ " AND d.service_type = 'N' \r\n"
					+ " AND s.protocol = 'MAIL'\r\n";
					
					
			
			String TdcQueryPaso4 = " SELECT * FROM wm_log_run \r\n"
					+ " WHERE interface = 'OE05 - Reenvio' \r\n"
					+ " AND status= 'S' \r\n"
					+ " AND start_dt >= trunc(sysdate) \r\n"
					+ " ORDER BY start_dt DESC \r\n";
					
					
					
			String TdcQueryPaso5 = " SELECT b.ATTRIBUTE6 emailSupplier FROM XXFC_SERV_VENDOR_COMM_DATA_V b, ap_suppliers pav, hz_parties hp \r\n"
					+ " WHERE pav.party_id = hp.party_id \r\n"
					+ " AND b.vendor_id = pav.vendor_id(+) \r\n"
					+ " AND SERVICE_ID = %s \r\n";
					
					
					
			String TdcQueryPaso7 = " SELECT * FROM WM_OE05_OUTBOUND_DOCS \r\n"
					+ " WHERE service_type= 'L' \r\n"
					+ " AND item = %s \r\n"
					+ " AND ESTATUS ='E' \r\n"
					+ " AND FECHA_ENVIO >= TRUNC(SYSDATE) \r\n";
					
					
					
					
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

			addStep("Validar que el exista al menos un servicio y lote de tipo Nacional enviado con estatos fallido (F) y protocolo MAIL.");
					
			System.out.println(GlobalVariables.DB_HOST_AVEBQA);
			System.out.println(TdcQueryPaso2);

			SQLResult paso2 = executeQuery(dbAvebqa, TdcQueryPaso1);
			
			String id_del_servicio = "";
			
			boolean BolPaso2 = estadoFacturas.isEmpty();

			if (!BolPaso2) {
				
				paso2.getData(0,"id_del_servicio");
				testCase.addQueryEvidenceCurrentStep(paso2);

			}

			assertFalse(BolPaso2, "No se cuenta con facturas emitidas");

			// Paso 3 ************************

			addStep("Ejecuctar el servicio OE05.Pub:runReSend con los parámetros del servicio y lote .\r\n");
					
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

			addStep("Ejecuctar el servicio OE05.Pub:runReSend con los parámetros del servicio y lote .\r\n");
					
			System.out.println(GlobalVariables.DB_HOST_AVEBQA);
			System.out.println(TdcQueryPaso4);

			SQLResult paso4 = executeQuery(dbLog, TdcQueryPaso4);
			boolean BolPaso4 = estadoFacturas.isEmpty();

			if (!BolPaso4) {

				testCase.addQueryEvidenceCurrentStep(paso4);

			}

			assertFalse(BolPaso4, "No se cuenta con facturas emitidas");

			// Paso 5 ************************

			addStep("Validar que el archivo fue enviado al correo del proveedor.\r\n");
			
			System.out.println(GlobalVariables.DB_HOST_AVEBQA);
			
			String FormatoPaso5 = String.format(id_del_servicio, TdcQueryPaso5);
			
			System.out.println(FormatoPaso5);

			SQLResult paso5 = executeQuery(dbAvebqa, FormatoPaso5);
			
			boolean BolPaso5 = estadoFacturas.isEmpty();

			if (!BolPaso5) {
		
				testCase.addQueryEvidenceCurrentStep(paso5);

			}

			assertFalse(BolPaso5, "No se cuenta con facturas emitidas");

			// Paso 6 ************************

			addStep("Validar la eliminación del archivo WM_OE05_OUTBOUND_DOCS.NOMBRE_ARCHIVO en el fileSystem.\r\n");
					
				
			// Paso 7 ************************

			addStep("Validar que el registro en la tabla WM_OE05_OUTBOUND_DOCS del servicio sea actualizado a estatus E.\r\n");
					
			System.out.println(GlobalVariables.DB_HOST_AVEBQA);
			
			String FormatoPaso7 = String.format(id_del_servicio, TdcQueryPaso7);
			
			System.out.println(FormatoPaso7);

			SQLResult paso7 = executeQuery(dbAvebqa, FormatoPaso7);
			boolean BolPaso7 = estadoFacturas.isEmpty();

			if (!BolPaso7) {

				testCase.addQueryEvidenceCurrentStep(paso7);

			}

			assertFalse(BolPaso7, "No se cuenta con facturas emitidas");

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
			return "Construido. Reenviar reportes Nacionales de los servicios que finalizar con estatus F con protocolo MAIL";
		}

		@Override
		public String setTestDesigner() {
			// TODO Auto-generated method stub
			return "QA Automation";
		}

		@Override
		public String setTestFullName() {
			// TODO Auto-generated method stub
			return "ATC_FT_OE05_006_Reenviar_Reportes_Locales_Servicio_Finalizar_Estatus_F_Protocolo_MAIL";
		}

		@Override
		public String setTestInstanceID() {
			// TODO Auto-generated method stub
			return null;
		}

}
