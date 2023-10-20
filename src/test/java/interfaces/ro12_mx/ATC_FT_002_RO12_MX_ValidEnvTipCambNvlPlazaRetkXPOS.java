package interfaces.ro12_mx;


import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.json.JSONArray;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.controlm.JobManagement;
import utils.controlm.pageObject.Control_mInicio;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_002_RO12_MX_ValidEnvTipCambNvlPlazaRetkXPOS extends BaseExecution {
	public String tipo;

	/*
 * MTC-FT-004-C1 RO12_MX Validar el envio de tipo de cambio de nivel Plaza a XPOS a traves de la interface RO12_MX 
 * Prueba de regresion para comprobar la no afectación en la funcionalidad
 *  principal de la interface FEMSA_RO12_MX para generar archivos EXR (ExchangeRate) de bajada 
 *  (de EBS Avante a WM OUTBOUND) con la informacion del nuevo tipo de cambio a nivel plaza, al ser migrada 
 *  la interface de WM9.9 a WM10.5
 * @author luis jasso
 * @date 2023/02/21
 * @proyecto Actualizacion tecnologica webmethods
	 */
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_RO12_MX_ValidEnvTipCambNvlPlazaRetkXPOS_test(HashMap<String, String> data)
			throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		SQLUtil dbAVEBQAEBS = new SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA,
				GlobalVariables.DB_PASSWORD_AVEBQA);

		SQLUtil dbFCWMLQA = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_AVEBQA,
				GlobalVariables.DB_PASSWORD_AVEBQA);

		SQLUtil dbFWM6QA = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_UPD, GlobalVariables.DB_USER_FCWMQA_UPD,
				GlobalVariables.DB_PASSWORD_FCWMQA_UPD);
		
		/*
		 * Variables
		 *************************************************************************/

		String InsertarTipoCambio = "INSERT INTO GL.GL_DAILY_RATES"
				+ "(FROM_CURRENCY, TO_CURRENCY, CONVERSION_DATE, CONVERSION_TYPE, CONVERSION_RATE,"
				+ " STATUS_CODE, CREATION_DATE, CREATED_BY, LAST_UPDATE_DATE, LAST_UPDATED_BY, "
				+ "LAST_UPDATE_LOGIN, CONTEXT, ATTRIBUTE1, ATTRIBUTE2, ATTRIBUTE3, ATTRIBUTE4, "
				+ "ATTRIBUTE5, ATTRIBUTE6, ATTRIBUTE7, ATTRIBUTE8, ATTRIBUTE9, ATTRIBUTE10, ATTRIBUTE11,"
				+ " ATTRIBUTE12, ATTRIBUTE13, ATTRIBUTE14, ATTRIBUTE15, RATE_SOURCE_CODE) VALUES " + " ('"
				+ data.get("FROM") + "','" + data.get("TO") + "',TO_DATE('" + data.get("CONVERSION_DATE")
				+ "','DD/MM/YYYY'),'" + data.get("CONVERSION_TYPE") + "','" + data.get("CONVERSION_RATE") + "','"
				+ data.get("STATUS_CODE") + "'," + "TO_DATE('" + data.get("CREATION_DATE") + "','DD/MM/YYYY'), '"
				+ data.get("CREATED_BY") + "'," + "TO_DATE('" + data.get("LAST_UPDATE_DATE") + "','DD/MM/YYYY'),'"
				+ data.get("LAST_UPDATED_BY") + "'," + "'" + data.get("LAST_UPDATE_LOGIN")
				+ "',NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,NULL,"
				+ " NULL, NULL, NULL, NULL, NULL, NULL," +"'" + data.get("USER") + "')";

		String consultaRegistro = "select * from " 
				+ "GL.GL_DAILY_RATES ORDER BY CREATION_DATE DESC";

		String WmLOG1 = "SELECT * FROM " 
				+ "WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE in '%RO12_MX%' "
				+ "ORDER BY END_DT DESC";

		String WmLOG2 = "SELECT * FROM WMLOG.WM_LOG_THREAD " 
				+ "WHERE RUN_ID IN ('%s','%s'";
		
		String WmLOGError = "SELECT * FROM WMLOG.WM_LOG_ERROR "
				+ "WHERE RUN_ID IN ('%s','%s')";

		String WmSyncExc = "SELECT * FROM  WM_SYNC_RMS_EXC_RATES " 
				+ "WHERE CONVERSION_TYPE='1734' "
				+ "ORDER BY CREATION_DATE DESC";

		String consultaPOSOUTBOND = "select * from " 
				+ "wmuser.POS_OUTBOUND_DOCS "
				+ "WHERE DOC_TYPE='EXR' " 
				+ "ORDER BY SENT_DATE DESC";

		
		/*
		 * Pasos
		 *****************************************************************************/

		/// Paso 1 ***************************************************

		addStep("Establecer conexion con la BD 'AVEBQA EBS' ");

		boolean conexiondbAVEBQAEBS = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbAVEBQAEBS ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_AVEBQA);

		assertTrue(conexiondbAVEBQAEBS, "La conexion no fue exitosa");

		/// Paso 2 ***************************************************

		addStep("Insertar un Tipo de Cambio en la tabla");

		SQLResult InsertarTipoCambio_r = executeQuery(dbAVEBQAEBS, InsertarTipoCambio);

		boolean validaInsercionTipoCambio = InsertarTipoCambio.isEmpty();

		if (!validaInsercionTipoCambio) {

			testCase.addQueryEvidenceCurrentStep(InsertarTipoCambio_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se inserto correctamente");
			testCase.addQueryEvidenceCurrentStep(InsertarTipoCambio_r);

		}

		/// Paso 3 ***************************************************

		addStep("Validar que se encuentre el nuevo registro insertado " + "en la tabla 'GL.GL_DAILY_RATES' ");

		SQLResult consultaRefRegistroInsertado_r = executeQuery(dbAVEBQAEBS, consultaRegistro);

		boolean validaConsultaRegistroInsertado = consultaRegistro.isEmpty();

		if (!validaConsultaRegistroInsertado) {

			testCase.addQueryEvidenceCurrentStep(consultaRefRegistroInsertado_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se muestra la consulta");
			testCase.addQueryEvidenceCurrentStep(consultaRefRegistroInsertado_r);

		}

		/// Paso 4 ***************************************************

		addStep("Establecer conexion con la BD 'FCWMLQA' ");

		boolean conexiondbFCWMLQA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFCWMLQA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLQA);

		assertTrue(conexiondbFCWMLQA, "La conexion no fue exitosa");

		/// Paso 5 ***************************************************

		addStep("Validar correcta ejecucion de la interface " + " 'WM_LOG_RUN' ");

		SQLResult consultaWmLOG1_r = executeQuery(dbFCWMLQA, WmLOG1);

		boolean validaConsultaWmLOG1_r = WmLOG1.isEmpty();

		if (!validaConsultaWmLOG1_r) {

			testCase.addQueryEvidenceCurrentStep(consultaWmLOG1_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se muestra la consulta");
			testCase.addQueryEvidenceCurrentStep(consultaWmLOG1_r);

		}

		/// Paso 6 ***************************************************

		addStep("Consultar tabla 'WMLOG.WM_LOG_THREAD' para ver " + " el THREAD creado al ejecutar la interfaz");

		String run_id1 = "";
		String run_id2 = "";

		String StatusThread = String.format(WmLOG2, run_id1,run_id2);

		SQLResult consultaWmLOGThread_r = executeQuery(dbAVEBQAEBS, StatusThread);

		boolean validaConsultaWmLOGThread_r = StatusThread.isEmpty();

		if (!validaConsultaWmLOGThread_r) {

			testCase.addQueryEvidenceCurrentStep(consultaWmLOGThread_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se muestra la consulta");
			testCase.addQueryEvidenceCurrentStep(consultaWmLOGThread_r);

		}

		/// Paso 7 ***************************************************

		addStep("Consultar tabla 'WMLOG.WM_LOG_ERROR' para verificar "
				+ " que no se encuentre ningun registro de error ");

		String StatusWMError = String.format(WmLOGError, run_id1,run_id2);

		SQLResult consultaWmLOGError_r = executeQuery(dbAVEBQAEBS, StatusWMError);

		boolean validaConsultaWmLOGError_r = StatusWMError.isEmpty();

		if (!validaConsultaWmLOGError_r) {

			testCase.addQueryEvidenceCurrentStep(consultaWmLOGError_r);

		} else {

			testCase.addTextEvidenceCurrentStep("Se muestra mensaje de error");
			testCase.addQueryEvidenceCurrentStep(consultaWmLOGError_r);

		}

		/// Paso 8 ***************************************************

		addStep("Consultar tabla 'WM_SYNC_RMS_EXC_RATES' para validar "
				+ " que se haya registrado el tipo de cambio en EBS ");

		String wm_creationDate = "";

		String StatusWmSyncExc = String.format(WmSyncExc, wm_creationDate);

		SQLResult consultaWmSyncExc_r = executeQuery(dbAVEBQAEBS, StatusWmSyncExc);

		boolean validaConsultaWmSyncExc_r = StatusWmSyncExc.isEmpty();

		if (!validaConsultaWmSyncExc_r) {

			testCase.addQueryEvidenceCurrentStep(consultaWmSyncExc_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se encontro ninguna consulta");
			testCase.addQueryEvidenceCurrentStep(consultaWmSyncExc_r);

		}

		
		/// Paso 9 ***************************************************

		addStep("Establecer conexion con la BD 'FCWM6QA' ");

		boolean conexiondbFWM6QA = true;

		testCase.addTextEvidenceCurrentStep("Conexion: dbFWM6QA ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMQA_UPD);

		assertTrue(conexiondbFWM6QA, "La conexion no fue exitosa");

		/// Paso 10 ***************************************************

		addStep("Validar correcta insercion de los documentos enviados a la plaza"
				+ " en la tabla 'POS_OUTBOUND_DOCS' ");

		String DOC_NAME = "";
		
		SQLResult consultaPOSOUTBOND_r = executeQuery(dbFWM6QA, consultaPOSOUTBOND);

		boolean validaconsultaPOSOUTBOND_r = consultaPOSOUTBOND.isEmpty();

		if (!validaconsultaPOSOUTBOND_r) {

			testCase.addQueryEvidenceCurrentStep(consultaPOSOUTBOND_r);

		} else {

			testCase.addTextEvidenceCurrentStep("No se encontro ninguna consulta");
			testCase.addQueryEvidenceCurrentStep(consultaPOSOUTBOND_r);

		}

		/// Paso 11 ***************************************************

		addStep("Ingresar al FTP (Filezilla) con usuario y contrasena " + "para validar los buzones por plaza");
		
		
	       FTPUtil ftp = new FTPUtil("10.182.92.13",21,"posuser","posuser");
	       
	       Thread.sleep(20000);
	       String ruta = "/FEMSA_OXXO/POS/"+ data.get("plaza") +"/"+ data.get("tienda") +"/working/" + DOC_NAME;
	                                 //host, puerto, usuario, contraseña
	     
		
		/// Paso 12 ***************************************************

		addStep("Validar que se realice el envio del archivo EXR generado por "
				+ "la interfaz RO12_MX en el directorio configurado para la tienda procesada");
		
		  ///u01/posuser
	       boolean validaFTP;
	       
	        if (ftp.fileExists(ruta) ) {
	        	
	        	validaFTP = true;
	            testCase.addFileEvidenceCurrentStep(ruta);
	            System.out.println("Existe");
	            testCase.addBoldTextEvidenceCurrentStep("El archivo si existe ");
	            testCase.addBoldTextEvidenceCurrentStep(ruta);
	            
	        }else {
	        	testCase.addFileEvidenceCurrentStep(ruta);
	        	testCase.addBoldTextEvidenceCurrentStep("El archivo no existe ");
	            System.out.println("No Existe");
	            validaFTP = false;
	        }
	        
	        assertTrue("No se encontro el archivo xml en POSUSER ", validaFTP);
		
		
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		return " Validar que el Tpo Cambio Corp. sea distrib. y aplic. a todas las tdas y pzas. " + tipo;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestFullName() {
		return "ATC_FT_001_RO12_MX_ValidaEnvioCambioCorpOperatRetek_test";
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