package interfaces.ol9;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import org.json.JSONArray;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLResultExcel;
import utils.sql.SQLUtil;

/**
 * 004-FT-BACK OFFICE AVANTE: MTC_FT_003-1 OL9 Generación de archivo CEN de reporte diario de pago de servicios en línea ST-SFTP a través de la interface FEMSA_OL9 - Depósito en SFTP
 * Desc:
 * Prueba de regresión para comprobar la no afectación en la funcionalidad principal de la interface FEMSA_OL9 de avante 
 * para generar el archivos CEN (Reporte Diario de Pagos de Servicios en Línea) y ser enviados (de EBS XXFC_PAGO_SERVICIOS_PRE a Repositorios FTP/SFTP o Mail), 
 * al ser migrada la interface de WM9.9 a WM10.5 El objetivo de la Interface OL9 es extraer la información de los pagos nacionales de ORACLE Applications, 
 * realizar la conversión al formato Legacy y enviar al proveedor la información de los pagos vía Email, FTP o SFTP. 
 * Origen: Oracle Applications XXFC_PAGO_SERVICIOS_PRE. Contiene la tabla de pagos de servicios. 
 * XXFC_SERVICES_VENDOR_COMM_DATA. Cuenta con la configuración de envió de proveedor. 
 * Destino: Proveedor Se envía a directorios en servidor ftp, sftp o email del proveedor.
 * @author Roberto Flores
 * @date   2022/06/29
 */
public class ATC_FT_003_OL9_WMx86_GenArchivoCENPagoServiciosSFTP extends BaseExecution{
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_PO2_WMx86_ProcArchivoHEF(HashMap<String, String> data) throws Exception {
		
		testCase.setPrerequisites("*Contar con acceso a las bases de datos de FCWM6QA, FCWMLQA y AVEBQA (EBS) de Avante.\r\n"
				+ "*Contar con el nombre y grupo del nuevo Job de Control M para la ejecución de la interface OL9.\r\n"
				+ "*Contar con acceso a repositorio QA de buzón de archivos CEN de avante.\r\n"
				+ "*Contar con las credenciales de WM10.5 de los nuevos servers del Integration Server de QA de Back Office.\r\n"
				+ "*Haber ejecutado el caso de prueba <MTC_FT_002 PO3 Procesar archivo SYB de cobro de servicios a través de la interface FEMSA_PO3>.\r\n"
				+ "*Contar con datos de un servicio que tenga configurado la generación del archivo en formato STANDAR y sea enviado por FTP:\r\n"
				+ "SELECT VENDOR_ID, ORACLE_EF, SERVICE_ID, LEGACY_ID, SERVICE_TYPE, OUTPUT_FORMAT_ID, PROTOCOL, PROTOCOL1,ATTRIBUTE1, ATTRIBUTE2, ATTRIBUTE3, ATTRIBUTE4, ATTRIBUTE5, ATTRIBUTE6,\r\n"
				+ "ATTRIBUTE7, HORARIO_DE_ENVIO, HORARIO_DE_ENVIO2, ENVIAR_TRANS_CANCEL, ESTATUS_ENVIO_PROGRAMADO, ENVIAR_SERVICIO, NOTIFICACION, FRECUENCIA\r\n"
				+ "FROM XXFC_SERVICES_VENDOR_COMM_DATA WHERE SERVICE_ID in ('<Service_ID>'); \r\n"
				+ " \r\n"
				+ "OUTPUT_FORMAT_ID = \"ST\"\r\n"
				+ "PROTOCOL = \"FTP\"\r\n"
				+ "ATTRIBUTE6 = (Email de quien va a recibir el correo con el archivo CEN.)\r\n"
				+ "ATTRIBUTE7 = (Email de quien va a recibir el correo con el archivo CEN.)\r\n"
				+ "ESTATUS_ENVIO_PROGRAMADO = \"A\"\r\n"
				+ "HORARIO_DE_ENVIO: \"10:00 AM\"\r\n"
				+ "ENVIAR_SERVICIO = \"S\"\r\n"
				+ "HORARIO_DE_ENVIO2: (null)\r\n"
				+ "ENVIAR_TRANS_CANCEL = (null)\r\n"
				+ "");
		
		/*
		 * Utilerías
		 *********************************************************************/
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		formatter.setTimeZone(TimeZone.getTimeZone("America/Mexico_City"));
		
		//SQLUtil FCWM6QA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
		SQLUtil AVEBQA = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil FCWMLQA_WMLOG = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA_WMLOG, GlobalVariables.DB_USER_FCWMLQA_WMLOG,GlobalVariables.DB_PASSWORD_FCWMLQA_WMLOG);
		
		
		
		/*
		 * Querys
		 *********************************************************************/
		String qryRangoFechas = "SELECT TO_CHAR(SYSDATE-30,'DDMMRRRR') BEGIN_DATE, \r\n"
				+ "TO_CHAR(TRUNC(SYSDATE),'DDMMRRRR') END_DATE     \r\n"
				+ "FROM DUAL";
		
		String qryPagosNacionales = "SELECT DISTINCT  A.TRANS_ID ,A.SERVICIO  ,A.TICKET ,A.ESTATUS ,A.FECHA_TRANSACCION ,A.FOLIO_TRANSACCION ,A.XXFC_FECHA_ADMINISTRATIVA ,A.PLAZA ,A.TIENDA \r\n"
				+ ",A.NO_AUTO ,A.REF1 ,A.VALOR ,A.COMISION ,A.HORA_TRANSACCION ,A.LOTE ,A.FECHA_TRANSMISION ,A.FECHA_RECEPCION ,A.LAST_UPDATE_DATE,\r\n"
				+ " --A.CONESECUTIVO ,\r\n"
				+ "A.REF2 ,A.ORACLE_CR ,A.CORTE ,A.CAJA ,A.TICKET ,A.VALIDATED ,A.ORG_ID ,A.FACTURA ,A.GL ,A.ID_SERVICIO\r\n"
				+ ",A.ORACLE_CIA ,A.ATRIBUTO3 ,A.POLIZA_OS ,A.CARTA_FEMSA ,B.SERVICE_ID ,B.ATTRIBUTE8,B.ENVIAR_TRANS_CANCEL ,B.OUTPUT_FORMAT_ID ,\r\n"
				+ "B.HORARIO_DE_ENVIO, B.SERVICE_TYPE \r\n"
				+ "FROM XXFC.XXFC_PAGO_SERVICIOS_PRE a INNER JOIN XXFC.XXFC_SERVICES_VENDOR_COMM_DATA b ON a.SERVICIO=b.SERVICE_ID\r\n"
				+ "WHERE (a.ESTATUS IS NULL or a.ESTATUS='F' OR (a.ESTATUS= 'C' AND a.FECHA_TRANSMISION is null and b.ENVIAR_TRANS_CANCEL = 'N'))\r\n"
				+ "AND a.FECHA_TRANSACCION >= TO_DATE(<<BeginDate>> ,'DDMMYYYY') AND a.FECHA_TRANSACCION <= TO_DATE(<<EndDate>>,'DDMMYYYY')\r\n"
				+ "AND a.XXFC_FECHA_ADMINISTRATIVA >= TO_DATE(<<BeginDate>>,'DDMMYYYY') AND a.XXFC_FECHA_ADMINISTRATIVA <= TO_DATE(<<EndDate>>,'DDMMYYYY')\r\n"
				+ "AND b.SERVICE_TYPE='N'\r\n"
				+ "AND B.OUTPUT_FORMAT_ID <>'TXO'\r\n"
				+ "AND A.SERVICIO = B.SERVICE_ID    \r\n"
				+ "AND A.SERVICIO NOT IN (SELECT SERVICE_ID FROM wmuser.WM_OL9_OUTBOUND_DOCS WHERE TRUNC(CREATED_DATE) = TRUNC(SYSDATE) AND (STATUS='L' OR STATUS='E')) \r\n"
				+ "ORDER BY TO_NUMBER(B.ATTRIBUTE8)";
		
		String qryServiciosPagosNacionales = "select distinct servicio from (\r\n"
				+ "SELECT SERVICIO\r\n"
				+ "FROM XXFC.XXFC_PAGO_SERVICIOS_PRE a INNER JOIN XXFC.XXFC_SERVICES_VENDOR_COMM_DATA b ON a.SERVICIO=b.SERVICE_ID\r\n"
				+ "WHERE (a.ESTATUS IS NULL or a.ESTATUS='F' OR (a.ESTATUS= 'C' AND a.FECHA_TRANSMISION is null and b.ENVIAR_TRANS_CANCEL = 'N'))\r\n"
				+ "AND a.FECHA_TRANSACCION >= TO_DATE(<<BeginDate>> ,'DDMMYYYY') AND a.FECHA_TRANSACCION <= TO_DATE(<<EndDate>>,'DDMMYYYY')\r\n"
				+ "AND a.XXFC_FECHA_ADMINISTRATIVA >= TO_DATE(<<BeginDate>>,'DDMMYYYY') AND a.XXFC_FECHA_ADMINISTRATIVA <= TO_DATE(<<EndDate>>,'DDMMYYYY')\r\n"
				+ "AND b.SERVICE_TYPE='N'\r\n"
				+ "AND B.OUTPUT_FORMAT_ID <>'TXO'\r\n"
				+ "AND A.SERVICIO = B.SERVICE_ID    \r\n"
				+ "AND A.SERVICIO NOT IN (SELECT SERVICE_ID FROM wmuser.WM_OL9_OUTBOUND_DOCS WHERE TRUNC(CREATED_DATE) = TRUNC(SYSDATE) AND (STATUS='L' OR STATUS='E')) \r\n"
				+ "ORDER BY TO_NUMBER(B.ATTRIBUTE8)\r\n"
				+ ")";

		String qryInfoProveedoresFTP = "SELECT VENDOR_ID, ORACLE_EF, LEGACY_ID, SERVICE_ID, SERVICE_TYPE, OUTPUT_FORMAT_ID, PROTOCOL, PROTOCOL1, ATTRIBUTE1,ATTRIBUTE2,ATTRIBUTE3,ATTRIBUTE4,ATTRIBUTE6,ATTRIBUTE7,ATTRIBUTE9, HORARIO_DE_ENVIO \r\n"
				+ "FROM XXFC.XXFC_SERVICES_VENDOR_COMM_DATA \r\n"
				+ "WHERE SERVICE_ID IN (%s)\r\n"
				+ "AND PROTOCOL = 'FTP'";
		
		String qryActualizacionEstatus = "SELECT * FROM XXFC.XXFC_PAGO_SERVICIOS_PRE \r\n"
				+ "WHERE SERVICIO in (<<Servicios>>)\r\n"
				+ "AND ESTATUS = 'E' \r\n"
				+ "AND FECHA_TRANSACCION BETWEEN TO_DATE(<<BeginDate>>, 'DDMMRRRR') AND TO_DATE(<<EndDate>>,'DDMMRRRR') \r\n"
				+ "AND XXFC_FECHA_ADMINISTRATIVA BETWEEN TO_DATE(<<BeginDate>>,'DDMMRRRR') AND TO_DATE(<<EndDate>>,'DDMMRRRR')";
		
		testCase.setProject_Name("POC WMx86");
		
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		
		/****************************************************************************************************************************************
		 * Paso 1
		 * **************************************************************************************************************************************/
		addStep("Establecer la conexión con la BD **FCWM6QA**.");
		
				testCase.addTextEvidenceCurrentStep("Conexion: AVEBQA");
				testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_AVEBQA);
		
		
		/****************************************************************************************************************************************
		 * Paso 2
		 * **************************************************************************************************************************************/
		addStep("Obtener el rango de fechas para el envío de pagos nacionales a procesar consultando en la BD AVEBQA (EBS)");
				
				System.out.println("qryRangoFechas: \r\n "+ qryRangoFechas);
				SQLResult qryRangoFechas_r = executeQuery(AVEBQA, qryRangoFechas);
				testCase.addQueryEvidenceCurrentStep(qryRangoFechas_r, true);
				
				assertFalse(qryRangoFechas_r.isEmpty());
				
				String beginDate = qryRangoFechas_r.getData(0, "BEGIN_DATE");
				String endDate = qryRangoFechas_r.getData(0, "END_DATE");
						
				
		/****************************************************************************************************************************************
		 * Paso 3
		 * **************************************************************************************************************************************/
		addStep("Verificar que existan pagos nacionales a enviar a los proveedores en la BD AVEBQA (EBS).");
				
				String qryPagosNacionales_f = qryPagosNacionales.replace("<<BeginDate>>", beginDate).replace("<<EndDate>>", endDate);
				System.out.println("qryPagosNacionales_f: \r\n "+ qryPagosNacionales_f);
				
				SQLResult qryPagosNacionales_r = executeQuery(AVEBQA, qryPagosNacionales_f);
				testCase.addQueryEvidenceCurrentStep(qryPagosNacionales_r, true);
				
				assertFalse(qryPagosNacionales_r.isEmpty());
				
				String qryServiciosPagosNacionales_f = qryServiciosPagosNacionales.replace("<<BeginDate>>", beginDate).replace("<<EndDate>>", endDate);
				System.out.println("qryServiciosPagosNacionales_f: \r\n "+ qryServiciosPagosNacionales_f);
				
				SQLResult qryServiciosPagosNacionales_r = executeQuery(AVEBQA, qryServiciosPagosNacionales_f);
				
				String servicios = "";
				for (int i = 0; i < qryServiciosPagosNacionales_r.getRowCount(); i++) {
					servicios += "'" + qryServiciosPagosNacionales_r.getData(i, "SERVICIO") + "'";
					if (i < qryServiciosPagosNacionales_r.getRowCount() - 1) {
						servicios += ",";
					}
				}
				
		
		/****************************************************************************************************************************************
		 * Paso 4
		 * **************************************************************************************************************************************/
		addStep("Comprobar que exista información de los proveedores, y que el método de envío sea por FTP, en la BD AVEBQA");
				
				String qryInfoProveedoresFTP_f = String.format(qryInfoProveedoresFTP, servicios);
				System.out.println("qryInfoProveedoresFTP_f: \r\n "+ qryInfoProveedoresFTP_f);
				
				
				SQLResult qryInfoProveedoresFTP_r = executeQuery(AVEBQA, qryInfoProveedoresFTP_f);
				testCase.addQueryEvidenceCurrentStep(qryInfoProveedoresFTP_r, true);
				
				assertFalse(qryInfoProveedoresFTP_r.isEmpty());
				
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//		/****************************************************************************************************************************************
//		 * Paso 5
//		 * **************************************************************************************************************************************/
//		addStep("Ejecución control-M");
//		
//				fechaEjecucionInicio = new Date();
//		
//				// Se obtiene la cadena de texto del data provider en la columna "jobs"
//				// Se asigna a un array para poder manejarlo
//				JSONArray array = new JSONArray(data.get("cm_jobs"));
//
//				testCase.addTextEvidenceCurrentStep("Ejecución Job: " + data.get("cm_jobs"));
//				SeleniumUtil u = new SeleniumUtil(new ChromeTest());
//				Control_mInicio cm = new Control_mInicio(u, data.get("cm_user"), data.get("cm_ps"));
//		
//				testCase.addTextEvidenceCurrentStep("Login");
//				addStep("Login");
//				u.get(data.get("cm_server"));
//				u.hardWait(40);
//				u.waitForLoadPage();
//				cm.logOn();
//				
//				testCase.addTextEvidenceCurrentStep("Inicio de job");
//				JobManagement j = new JobManagement(u, testCase, array);
//				String resultadoEjecucion = j.jobRunner();
//			
//		
//		/****************************************************************************************************************************************
//		 * Paso 6
//		 * **************************************************************************************************************************************/
//		addStep("Abrir la herramienta de control M para validar que la ejecución del job haya sido exitosa");
//				
//				testCase.addTextEvidenceCurrentStep("Resultado de la ejecucion: " + resultadoEjecucion);
//				System.out.println("Resultado de la ejecucion -> " + resultadoEjecucion);
//				
//				assertEquals(resultadoEjecucion, "Ended OK");
//				u.close();
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
		/****************************************************************************************************************************************
		 * Paso 7
		 * **************************************************************************************************************************************/
		addStep("Validar la actualización del ESTATUS = 'E' y FECHA_TRANSMISION = Fecha de procesamiento de los pagos de servicios nacionales enviados al Proveedor, en la BD AVEBQA (EBS).");
				
				String qryActualizacionEstatus_f = qryActualizacionEstatus
						.replace("<<BeginDate>>", beginDate)
						.replace("<<EndDate>>", endDate)
						.replace("<<Servicios>>", servicios);
				
				System.out.println("qryActualizacionEstatus_f: \r\n "+ qryActualizacionEstatus_f);
				
				SQLResult qryActualizacionEstatus_r = executeQuery(AVEBQA, qryActualizacionEstatus_f);
				testCase.addQueryEvidenceCurrentStep(qryActualizacionEstatus_r, true);
				
				//assertFalse(qryActualizacionEstatus_r.isEmpty());
				
				
		/****************************************************************************************************************************************
		 * Paso 8
		 * **************************************************************************************************************************************/
		addStep("Validar la inserción de los documentos enviados al Proveedor en la tabla WM_OL9_OUTBOUND_DOCS de BD AVEDQA.");
				
				testCase.addTextEvidenceCurrentStep("Conexion: AVEBQA");
				//testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_AVEBQA_12.2.4);
				
				

	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_003_OL9_WMx86_GenArchivoCENPagoServiciosSFTP";
	}

	@Override
	public String setTestDescription() {
		return "MTC_FT_003-1 OL9 Generación de archivo CEN de reporte diario de pago de servicios en línea ST-SFTP a través de la interface FEMSA_OL9 - Depósito en SFTP";
	}

	@Override
	public String setTestDesigner() {
		return "AutomationQA";
	}

	@Override
	public String setTestInstanceID() {
		return null;
	}

	@Override
	public void beforeTest() {
	}

	@Override
	public String setPrerequisites() {
		return null;
	}

}