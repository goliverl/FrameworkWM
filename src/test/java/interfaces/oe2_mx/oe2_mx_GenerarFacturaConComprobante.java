package interfaces.oe2_mx;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;

public class oe2_mx_GenerarFacturaConComprobante extends BaseExecution{
	
	/*
	 * 
	 * @cp MTC-FT-004 -Generar factura con comprobante de pago
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_OE2_MX_GenerarFacturaConComprobante_test(HashMap<String, String> data) throws Exception{
		/*
		 * Utileria****************************************************/
		utils.sql.SQLUtil dbEBS = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbFCTPEQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE,GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		/*
		 * Variables***************************************************/
		String queryPorProcesarRegistros = "SELECT ID_FACTURA_DIGITAL, ORIGEN, CR_PLAZA, CR_TIENDA, FOLIO, FECHA, WM_STATUS, TIPO_COMPROBANTE\r\n"
				+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL\r\n"
				+ "WHERE VERSION_CFDI = '4.0'\r\n"
				+ "AND WM_STATUS = 'L'\r\n"
				+ "AND ORIGEN = 'CP'\r\n"
				+ "AND TIPO_COMPROBANTE = 'P'\r\n"
				+ "\r\n";
		
		String queryProcesarLines = "SELECT ID_FACTURA_DIGITAL, TIPO, DESCRIPCION, CANTIDAD, UNIDAD, CLAVE\r\n"
				+ "FROM XXFC. XXFC_CFD_FACTURA_DIGITAL_LINES\r\n"
				+ "WHERE ID_FACTURA_DIGITAL = '%s'";
		
		String queryValidarRegistros = "SELECT * \r\n"
				+ "FROM XXFC. XXFC_CFD_FACTURA_DIGITAL_PAY \r\n"
				+ "WHERE ID_FACTURA_DIGITAL = %s";
		
		String queryValidarPorProcesar = "SELECT ID_FACTURA_DIGITAL, ID_FACTURA_DIGITAL_PAY, SERIE, FOLIO, METODO_DE_PAGO\r\n"
				+ "FROM XXFC. XXFC_CFD_FACTURA_DIGITAL_PAY\r\n"
				+ "WHERE ID_FACTURA_DIGITAL = '%s'";
		
		String queryEstatusEjecucion = "SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS\r\n"
				+ "FROM WMLOG.WM_LOG_RUN\r\n"
				+ "WHERE INTERFACE = 'OE2_MX'\r\n"
				+ "AND START_DT >= TRUNC(SYSDATE)\r\n"
				+ "ORDER BY START_DT DESC";
		
		String queryEstatusThreads = "SELECT THREAD_ID, PARENT_ID, NAME, START_DT, END_DT, STATUS\r\n"
				+ "FROM WMLOG.WM_LOG_THREAD\r\n"
				+ "WHERE PARENT_ID = '%s'\r\n"
				+ "ORDER BY START_DT DESC";
		
		String queryRegistrosProcesados = "SELECT ID_FACTURA_DIGITAL, ORIGEN, CR_PLAZA, VERSION, SERIE, DOCTO_XML\r\n"
				+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL\r\n"
				+ "WHERE VERSION_CFDI ='4.0'\r\n"
				+ "AND WM_STATUS ='E'\r\n"
				+ "AND ID_FACTURA_DIGITAL = '%s'";
		
		/******************************Paso 1**************************/
		addStep("Ejecutar el siguiente query para validar si hay registros a procesar por la interfaz.");
		SQLResult porProcesarRegistros = executeQuery(dbEBS, queryPorProcesarRegistros);
		System.out.println(queryPorProcesarRegistros);
		boolean validatePorProcesarRegistros = porProcesarRegistros.isEmpty();
		String idFacturaDigital = "";
		if(!validatePorProcesarRegistros) {
			idFacturaDigital = porProcesarRegistros.getData(0, "ID_FACTURA_DIGITAL");
			testCase.addQueryEvidenceCurrentStep(porProcesarRegistros);
		}
		assertFalse(validatePorProcesarRegistros, "No se encontraron registros por procesar.");
		/******************************Paso 2**************************/
		addStep("Ejecutar el siguiente query para validar si hay registros a procesar por la interfaz.");
		String formatProcesarLines = String.format(queryProcesarLines, idFacturaDigital);
		SQLResult procesarLines = executeQuery(dbEBS, formatProcesarLines);
		System.out.println(formatProcesarLines);
		boolean validateProcesarLines = procesarLines.isEmpty();
		if(!validateProcesarLines) {
			testCase.addQueryEvidenceCurrentStep(procesarLines);
		}
		assertFalse(validateProcesarLines, "No se encontraron registros por procesar.");
		/******************************Paso 3**************************/
		addStep("Ejecutar el siguiente query para validar si hay registros a procesar por la interfaz.");
		String formatValidarRegistros = String.format(queryValidarRegistros, idFacturaDigital);
		SQLResult validarRegistros = executeQuery(dbEBS, formatValidarRegistros);
		System.out.println(formatValidarRegistros);
		boolean validateValidarRegistros = validarRegistros.isEmpty();
		if(!validateValidarRegistros) {
			testCase.addQueryEvidenceCurrentStep(validarRegistros);
		}
		assertFalse(validateValidarRegistros, "No se encontraron registros por procesar");
		/******************************Paso 4**************************/
		addStep("Ejecutar el siguiente query para validar si hay registros a procesar por la interfaz.");
		String formatPorProcesar = String.format(queryValidarPorProcesar, idFacturaDigital);
		SQLResult porProcesar = executeQuery(dbEBS, formatPorProcesar);
		System.out.println(formatPorProcesar);
		boolean validatePorProcesar = porProcesar.isEmpty();
		if(!validatePorProcesar) {
			testCase.addQueryEvidenceCurrentStep(porProcesar);
		}
		assertFalse(validatePorProcesar, "No se encontraron registros por procesar.");
		/******************************Paso 5**************************/
		addStep(" Ejecutamos interfaz desde servidor FCWMINTQA3");
		
		
		/******************************Paso 6**************************/
		addStep("Ejecutamos el siguiente query en la BD FCWMLQA.FEMCOM.NET para validar el estatus de la ejecucion.");
		SQLResult estatusEjecucion = executeQuery(dbLog, queryEstatusEjecucion);
		System.out.println(queryEstatusEjecucion);
		boolean validateEstatusEjecucion = estatusEjecucion.isEmpty();
		String runID = "";
		if(!validateEstatusEjecucion) {
			runID = estatusEjecucion.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(estatusEjecucion);
		}
		assertFalse(validateEstatusEjecucion, "No se muetran ultimas ejecuciones");
		/******************************Paso 7**************************/
		addStep("Ejecutamos el siguiente query para validar estatus de los threads.");
		String formatEstatusThreads = String.format(queryEstatusThreads, runID);
		SQLResult estatusThreads = executeQuery(dbLog, formatEstatusThreads);
		System.out.println(formatEstatusThreads);
		boolean validateEstatusThreads = estatusThreads.isEmpty();
		if(!validateEstatusThreads) {
			testCase.addQueryEvidenceCurrentStep(estatusThreads);
		}
		assertFalse(validateEstatusThreads, "No se muestran registros de threads");
		/******************************Paso 8**************************/
		addStep("Ejecutamos el siguiente query en la BD AVEBQA para validar los registros procesados.");
		String formatRegistrosProcesados = String.format(queryRegistrosProcesados, idFacturaDigital);
		SQLResult registrosProcesados = executeQuery(dbEBS, formatRegistrosProcesados);
		System.out.println(formatRegistrosProcesados);
		boolean validateRegistrosProcesados = registrosProcesados.isEmpty();
		String doctoXML = "";
		if(!validateRegistrosProcesados) {
			doctoXML = registrosProcesados.getData(0, "DOCTO_XML");
			testCase.addQueryEvidenceCurrentStep(registrosProcesados);
		}
		assertFalse(validateRegistrosProcesados, "No se muestran valores de registros timbrados");
		/******************************Paso 8**************************/
		addStep("Validamos el XML en  el campo DOCTO_XML de la conultsa anterior.");
		testCase.addTextEvidenceCurrentStep(doctoXML);
		
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
		return "Envio de informacion para generacion y timbrado de factura con comprobante de pago";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_004_OE2_MX_GenerarFacturaConComprobante_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
