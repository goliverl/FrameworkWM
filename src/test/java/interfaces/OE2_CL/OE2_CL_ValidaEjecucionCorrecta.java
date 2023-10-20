package interfaces.OE2_CL;


import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class OE2_CL_ValidaEjecucionCorrecta extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_OE2_CL_ValidaEjecucionCorrecta(HashMap<String, String> data) throws Exception {
		/*
	
		 * 
		 * Utilerías
		 *********************************************************************/
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_LogChile,GlobalVariables.DB_USER_LogChile, GlobalVariables.DB_PASSWORD_LogChile);
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_OIEBSBDQ,GlobalVariables.DB_USER_OIEBSBDQ, GlobalVariables.DB_PASSWORD_OIEBSBDQ);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_PosUserChile,GlobalVariables.DB_USER_PosUserChile, GlobalVariables.DB_PASSWORD_PosUserChile);
		
		
		//Paso 1
		
		String interfaceConfig = "SELECT valor1,\r\n" + 
				"valor2,\r\n" + 
				"valor3,\r\n" + 
				"valor4,\r\n" + 
				"valor5\r\n" + 
				"FROM WMUSER.WM_INTERFASE_CONFIG\r\n" + 
				"WHERE interfase = 'OE2_CL'";
		//Paso 2 
		String registrosPorProcesar = "SELECT ID_FACTURA_DIGITAL, TIPO_COMPROBANTE, FECHA, METODO_DE_PAGO, CONDICIONES_PAGO, FECHA_PAGO, EMI_RUT, EMI_GIRO "
				+ "FROM XXFC.XXFC_FACTURA_DIGITAL\r\n" + 
				"WHERE wm_retries < '%s' AND\r\n" + 
				"wm_estado_resp IS NULL AND\r\n" + 
				"( wm_estatus = 'L' OR\r\n" + 
				"( wm_estatus = 'E' AND\r\n" + 
				"creation_date >= SYSDATE - '%s' ) )";
		
		//Paso 3
		String detalleFactura= "SELECT ID_FACTURA_DIGITAL, ID_FACTURA_DIGITAL_LINEA, LINE_NUM, DESCRIPCION, CLAVE, CREATION_DATE\r\n" + 
				"FROM XXFC.XXFC_FACTURA_DIGITAL_LINES\r\n" + 
				"WHERE id_factura_digital = '%s'";
		
		//Paso 4 y 5
		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server "
				+ "FROM WMLOG.WM_LOG_RUN Tbl " + " WHERE interface = 'OE2_CL'  "
				+ "and  start_dt >= TRUNC(SYSDATE)"
				+ "ORDER BY START_DT DESC) where rownum <=1";// Consulta para estatus de la ejecucion
		String consultaERROR = " select * from (Select ERROR_ID, RUN_ID, ERROR_DATE, SEVERITY,ERROR_TYPE "
				+ "from  wmlog.WM_LOG_ERROR " + "where RUN_ID='%s') where rownum <=1";// Consulta para los errores
		String consultaERROR2 = " select * from (select description,MESSAGE " + "from wmlog.WM_LOG_ERROR "
				+ "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores
		String consultaERROR3 = " select * from (select PROC_ROLE, PROC_STATUS,CR_PLAZA, THREAD_ID,ATT1 " 
				+ "from wmlog.WM_LOG_ERROR " + "where RUN_ID='%s')WHERE rownum <= 1";// Consulta para los errores
		
		
	//Paso 6
		String actualizacionFactura1 = "SELECT wm_fecha,wm_estatus,wm_estado_resp,wm_estado_doc,wm_estado_acep\r\n" + 
				"FROM XXFC.XXFC_FACTURA_DIGITAL\r\n" + 
				"WHERE id_factura_digital = '%s'\r\n" + 
				"and WM_ESTADO_RESP in ('-11', 'SOK' ,  'CRT', 'FOK', 'PDR')\r\n" + 
				"and WM_ESTADO_DOC = null\r\n" + 
				"and WM_ESTATUS_CODE = null\r\n" + 
				"and WM_RUN_ID = '%s'\r\n" + 
				"and WM_ESTATUS = L\r\n" + 
				"and WM_ERROR= 'Envio Procesado'";
		
		String actualizacionFactura2 = "SELECT wm_track_id,wm_estatus_code,wm_error\r\n" + 
				"FROM XXFC.XXFC_FACTURA_DIGITAL\r\n" + 
				"WHERE id_factura_digital = '%s'\r\n" + 
				"and WM_ESTADO_RESP in ('-11', 'SOK' ,  'CRT', 'FOK', 'PDR')\r\n" + 
				"and WM_ESTADO_DOC = null\r\n" + 
				"and WM_ESTATUS_CODE = null\r\n" + 
				"and WM_RUN_ID = '%s'\r\n" + 
				"and WM_ESTATUS = L\r\n" + 
				"and WM_ERROR= 'Envio Procesado''";

		String segActFactura1 = "SELECT wm_fecha,wm_estatus,wm_estado_resp,wm_estado_doc,wm_estado_acep\r\n" + 
				"FROM XXFC.XXFC_FACTURA_DIGITAL\r\n" + 
				"WHERE id_factura_digital = '%s'\r\n" + 
				"and WM_ESTADO_DOC = null\r\n" + 
				"and WM_ESTADO_ACEP = 'EPR'\r\n" + 
				"and WM_ESTATUS_CODE = null\r\n" + 
				"and WM_RUN_ID = '%s'\r\n" + 
				"and WM_ESTATUS = S\r\n" + 
				"and WM_ERROR= 'Envio Procesado'";
		
		String segActFactura2 = "SELECT wm_track_id,wm_estatus_code,wm_error\r\n" + 
				"FROM XXFC.XXFC_FACTURA_DIGITAL\r\n" + 
				"WHERE id_factura_digital = '%s'\r\n" + 
				"and WM_ESTADO_DOC = null\r\n" + 
				"and WM_ESTADO_ACEP = 'EPR'\r\n" + 
				"and WM_ESTATUS_CODE = null\r\n" + 
				"and WM_RUN_ID = '%s'\r\n" + 
				"and WM_ESTATUS = 'S'\r\n" + 
				"and WM_ERROR= 'Envio Procesado'";
	
		
		
		/*
		 * Paso 1******************************************************************************************************************/
		
		addStep("Verificar que la interface OE2 tenga la configuración necesaria para operar correctamente con la siguiente consulta:");
		
		System.out.println(interfaceConfig);
		
		SQLResult interfaceConfig_R = dbPos.executeQuery(interfaceConfig);
		
		String VALOR3 =interfaceConfig_R.getData(0, "VALOR3");
		
		System.out.println("VALOR 3: " + VALOR3);
		
        String VALOR5 =interfaceConfig_R.getData(0, "VALOR5");
		
		System.out.println("VALOR 5: " + VALOR5);
		
		
		boolean ValidaInterfaceConfig = interfaceConfig_R.isEmpty();
		
		if (!ValidaInterfaceConfig) {
			
			testCase.addQueryEvidenceCurrentStep(interfaceConfig_R);
		}
		assertFalse("No se encontro la información de configuración ",ValidaInterfaceConfig);

		/*
		 * Paso 2
		 ***************************************************************************************************************************/
		addStep("Comprobar que existan registros validos para ser procesados por la interfaz con la siguienta consulta:(Los campos se obtienen de la consulta anterior)");
		
		String registrosPorProcesar_f = String.format(registrosPorProcesar, VALOR3, VALOR5);
		
		System.out.println( registrosPorProcesar_f);
		
		SQLResult registrosPorProcesar_r = dbEbs.executeQuery(registrosPorProcesar_f);
		
        String ID_FACTURA_DIGITAL =registrosPorProcesar_r.getData(0, "ID_FACTURA_DIGITAL");
		
		System.out.println("ID_FACTURA_DIGITAL " + ID_FACTURA_DIGITAL);
					
		boolean validaRegistrosPorProcesar = registrosPorProcesar_r.isEmpty();
		
		if (!validaRegistrosPorProcesar) {
			
			testCase.addQueryEvidenceCurrentStep(registrosPorProcesar_r);
		}
		
		System.out.println(validaRegistrosPorProcesar);

		assertFalse("La consulta no regresa información indicando que no existe información valida a procesar.", validaRegistrosPorProcesar);

		/*
		 * paso 3 
		 **************************************************************************************************************************/
		addStep("Comprobar que la factura tenga detalle de la factura ");
		
		String detalleFactura_f = String.format(detalleFactura, ID_FACTURA_DIGITAL);
		
		System.out.println( detalleFactura_f );
		
		SQLResult detalleFactura_r = dbEbs.executeQuery(detalleFactura_f);

		boolean validaDetalleFactura = detalleFactura_r.isEmpty();
		
		if (!validaDetalleFactura) {
			
			testCase.addQueryEvidenceCurrentStep(detalleFactura_r);
		}

		System.out.println(validaDetalleFactura);	
		
		assertFalse("La consulta regresa información.", validaDetalleFactura);

		/*
		 * Paso 4 ****************************************************************************************************************/
		addStep("Ejecutar la interface OE2_CL ");
		
		String status = "S";
		// utileria
		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		
		PakageManagment pok = new PakageManagment(u, testCase);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_LOG);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);
		
		String dateExecution = pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = executeQuery(dbLog, tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");
		String run_id = is.getData(0, "RUN_ID");

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			
			is = executeQuery(dbLog, tdcIntegrationServerFormat);

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}

		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(consultaERROR, run_id);
			String error1 = String.format(consultaERROR2, run_id);
			String error2 = String.format(consultaERROR3, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontr? un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorr);

			}

			SQLResult errorIS = dbLog.executeQuery(error1);
			boolean emptyError1 = errorIS.isEmpty();
			if (!emptyError1) {
				testCase.addQueryEvidenceCurrentStep(errorIS);
			}

			SQLResult errorIS2 = dbLog.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();

			if (!emptyError2) {

				testCase.addQueryEvidenceCurrentStep(errorIS2);

			}

		}

//Paso 5    ************************		
		addStep("Comprobar que se ejecutó correctamente la interfaz OE2_CL con la siguiente consulta: ");
		SQLResult is1 = executeQuery(dbLog, tdcIntegrationServerFormat);

		String fcwS = is1.getData(0, "STATUS");
        boolean validateStatus = fcwS.equals(status);
        
        if (validateStatus) {
			testCase.addQueryEvidenceCurrentStep(is1);
		}
	
		System.out.println("STATUS = S: "+ validateStatus);
		assertTrue(validateStatus, "La ejecucion de la interfaz no fue exitosa");
		
//  Paso 6  *********************************
		
		addStep("Verificar que en la tabla XXFC_FACTURA_DIGITAL hayan cambiado los siguientes campos"
				+ "FOLIO\r\n" + 
				"WM_FECHA\r\n" + 
				"WM_STATUS(S)\r\n" + 
				"WM_ESTADO_RESP EL ESTADO DEBE ESTAR ENTRE LOS SIGUIENTES(-11,SOK,CRT,FOK,PDR)\r\n" + 
				"WM_ESTADO_DOC(null)\r\n" + 
				"WM_ESTADO_ACEP\r\n" + 
				"WM_TRACK_ID\r\n" + 
				"WM_ESTATUS_CODE(NULL)\r\n" + 
				"WM_ERROR\r\n" + 
				"WM_RUN_ID(ID de la corrida)\r\n" + 
				"WM_ERROR(Envio procesado) ");
		
	//Primera parte
		String actualizacionFactura1_F = String.format(actualizacionFactura1, ID_FACTURA_DIGITAL, run_id);
		
		System.out.println(actualizacionFactura1_F);
		
		SQLResult actualizacionFactura1_r = dbEbs.executeQuery(actualizacionFactura1_F);
				
		boolean validaActualizacionFactura1 = actualizacionFactura1_r.isEmpty();
		
		if (!validaActualizacionFactura1) {
			testCase.addQueryEvidenceCurrentStep(actualizacionFactura1_r);
		}
		
		System.out.println(validaActualizacionFactura1);
	
	//Segunda parte
		
        String actualizacionFactura2_F = String.format(actualizacionFactura2, ID_FACTURA_DIGITAL, run_id);
		
		System.out.println(actualizacionFactura2_F);
		
		SQLResult actualizacionFactura2_r = dbEbs.executeQuery(actualizacionFactura2_F);
				
		boolean validaActualizacionFactura2 = actualizacionFactura2_r.isEmpty();
		
		if (!validaActualizacionFactura2) {
			testCase.addQueryEvidenceCurrentStep(actualizacionFactura2_r);
		}
		
		System.out.println(validaActualizacionFactura2);
		
		assertFalse("Los campos fueron actualizados y el campo WM_STATUS contiene L ", validaActualizacionFactura2);
		
//Paso 7 **************************************************************************
		
addStep("Ejecutar nuevamente la interfaz ");
		
		
		// utileria
		
		u.get(contra);
		
		 pok.runIntefaceWM(data.get("interfase"), data.get("servicio"), null);
		System.out.println("Respuesta dateExecution" + dateExecution);

		 is = executeQuery(dbLog, tdcIntegrationServerFormat);

		 status1 = is.getData(0, "STATUS");
		 run_id = is.getData(0, "RUN_ID");

		 valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			
			is = executeQuery(dbLog, tdcIntegrationServerFormat);

			status1 = is.getData(0, "STATUS");
			run_id = is.getData(0, "RUN_ID");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(2);

		}

		 successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {

			String error = String.format(consultaERROR, run_id);
			String error1 = String.format(consultaERROR2, run_id);
			String error2 = String.format(consultaERROR3, run_id);

			SQLResult errorr = dbLog.executeQuery(error);
			boolean emptyError = errorr.isEmpty();

			if (!emptyError) {

				testCase.addTextEvidenceCurrentStep(
						"Se encontro un error en la ejecucion de la interfaz en la tabla WM_LOG_ERROR");

				testCase.addQueryEvidenceCurrentStep(errorr);

			}

			SQLResult errorIS = dbLog.executeQuery(error1);
			boolean emptyError1 = errorIS.isEmpty();
			if (!emptyError1) {
				testCase.addQueryEvidenceCurrentStep(errorIS);
			}

			SQLResult errorIS2 = dbLog.executeQuery(error2);
			boolean emptyError2 = errorIS2.isEmpty();

			if (!emptyError2) {

				testCase.addQueryEvidenceCurrentStep(errorIS2);

			}

		}
//Paso 8 *******************************************************************************************************
		
		addStep("Comprobar que se ejecutó correctamente la interfaz OE2_CL con la siguiente consulta: ");
		SQLResult is2 = executeQuery(dbLog, tdcIntegrationServerFormat);

		String fcwS2 = is2.getData(0, "STATUS");
        boolean validateStatus2 = fcwS2.equals(status);
        
        if (validateStatus2) {
			testCase.addQueryEvidenceCurrentStep(is2);
		}
	
		System.out.println("STATUS = S: "+ validateStatus2);
		assertTrue(validateStatus2, "La ejecucion de la interfaz no fue exitosa");
		
//**************************************************************************************************************
		
		addStep(" Verificar nuevamente en la tabla XXFC_FACTURA_DIGITAL que hayan cambiado los siguientes campos:"
				+ "FOLIO\r\n" + 
				"WM_FECHA\r\n" + 
				"WM_STATUS(S)\r\n" + 
				"WM_ESTADO_RESP \r\n" + 
				"WM_ESTADO_DOC(null)\r\n" + 
				"WM_ESTADO_ACEP(EPR)\r\n" + 
				"WM_TRACK_ID\r\n" + 
				"WM_ESTATUS_CODE(NULL)\r\n" + 
				"WM_ERROR\r\n" + 
				"WM_RUN_ID(ID de la corrida)\r\n" + 
				"WM_ERROR(Envio procesado) ");
		
		//Primera parte
		
		String segActFactura1_f = String.format(segActFactura1, ID_FACTURA_DIGITAL, run_id);
		
		System.out.println(segActFactura1_f);
		
		SQLResult segActFactura1_r = dbEbs.executeQuery(segActFactura1_f);
		
		boolean validaSegActFactura = segActFactura1_r.isEmpty();
		
		if (!validaSegActFactura) {
			
			testCase.addQueryEvidenceCurrentStep(segActFactura1_r);
		}
		System.out.println(validaSegActFactura);
		
		// Segunda parte

        String segActFactura2_f = String.format(segActFactura2, ID_FACTURA_DIGITAL, run_id );
		
		System.out.println(segActFactura2_f);
		
		SQLResult segActFactura2_r = dbEbs.executeQuery(segActFactura2_f);
		
		boolean validaSegActFactura2 = segActFactura2_r.isEmpty();
		
		if (!validaSegActFactura2) {
			
			testCase.addQueryEvidenceCurrentStep(segActFactura2_r);
		}
		System.out.println(validaSegActFactura2);
		
		assertFalse("Los campos no fueron actualizados y el  campo WM_STATUS no contiene S", validaSegActFactura2 );

	

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
		return "";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO DE AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_OE2_CL_ValidaEjecucionCorrecta";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
