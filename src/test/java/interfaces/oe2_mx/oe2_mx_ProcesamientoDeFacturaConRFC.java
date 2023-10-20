package interfaces.oe2_mx;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;

public class oe2_mx_ProcesamientoDeFacturaConRFC extends BaseExecution{
	
	/*
	 * 
	 * @cp MTC-FT-001- Procesamiento de Factura con RFC generico Nacional
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_OE2_MX_ProcesamientoDeFacturaConRFC_test(HashMap <String, String> data) throws Exception{
		/*
		 * Utileria****************************************************/
		utils.sql.SQLUtil dbEBS = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbFCTPEQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE,GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		/*
		 * Variables***************************************************/
		String queryRegistrosPendientes = "SELECT ID_FACTURA_DIGITAL, CR_PLAZA, CR_TIENDA, REC_RFC, REC_REGIMEN_FISCAL, USO_CFDI\r\n"
				+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL\r\n"
				+ "WHERE VERSION_CFDI ='4.0'\r\n"
				+ "AND WM_STATUS = 'L'\r\n"
				+ "AND ANIO= '%s'\r\n"
				+ "AND MES= '%s'\r\n"
				+ "AND ORIGEN ='AR'";
		
		String queryRegistrosLines = "SELECT TIPO, TIPO_IMPUESTO, DESCRIPCION, CANTIDAD, UNIDAD, PRECIO_UNITARIO, IMPORTE\r\n"
				+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL_LINES\r\n"
				+ "WHERE ID_FACTURA_DIGITAL IN ('%s')\r\n"
				+ "ORDER BY CREATION_DATE DESC\r\n";
		
		String queryRegistrosFactura = "SELECT IMPUESTO, BASE, TIPO_FACTOR, IMPORTE, TIPO_IMPUESTO\r\n"
				+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL_TAX\r\n"
				+ "WHERE ID_FACTURA_DIGITAL in (%s)";
		
		String queryStatusEjecucion = "SELECT RUN_ID, INTERFACE, START_DT, END_DT, STATUS\r\n"
				+ "FROM wmlog.wm_log_run\r\n"
				+ "WHERE INTERFACE LIKE 'OE2%'\r\n"
				+ "AND START_DT >= TRUNC(SYSDATE)\r\n"
				+ "ORDER BY START_DT DESC";
		
		String queryStatusThreads = "SELECT PARENT_ID, NAME, START_DT, END_DT, STATUS\r\n"
				+ "FROM WMLOG.WM_LOG_THREAD\r\n"
				+ "WHERE PARENT_ID = '%s'\r\n";
		
		String queryRegistrosProcesados = "SELECT ORIGEN, ANIO, MES, CR_PLAZA, FOLIO, FECHA, SELLO\r\n"
				+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL\r\n"
				+ "WHERE VERSION_CFDI ='4.0'\r\n"
				+ "AND WM_STATUS ='E'\r\n"
				+ "AND ANIO='%s'\r\n"
				+ "AND MES='%s'\r\n"
				+ "AND ORIGEN ='AR'";
		
		String queryDatosFactura = "SELECT ADMIN_DATE, EMI_RFC, EMI_NOMBRE, EMI_CALLE, EMI_NOEXT, EMI_NOINT, EMI_COLONIA, EMI_PAIS\r\n"
				+ "FROM TPEUSER.cfd_transaction\r\n"
				+ "WHERE TICKET IN ( '%s')\r\n"
				+ "AND SOURCE = 'AR'\r\n"
				+ "AND WM_CODE = '101'\r\n"
				+ "AND CREATION_DATE >= TRUNC(SYSDATE)\r\n"
				+ "ORDER BY CREATION_DATE DESC";
		
		/******************************Paso 1**************************/
		addStep("Ejecutar el siguiente query para validar si hay registros a procesar por la interfaz");
		
		String formatRegistrosPendientes = String.format(queryRegistrosPendientes, data.get("ANIO"), data.get("MES"));
		SQLResult registrosPendientes = executeQuery(dbEBS, formatRegistrosPendientes);
		System.out.println(formatRegistrosPendientes);
		boolean validateRegistrosPendientes = registrosPendientes.isEmpty();
		String idFacturaDigital = "";
		String rec_rfc = "";
		String rec_regimen_fiscal = "";
		String uso_cfdi = "";
		if(!validateRegistrosPendientes) {
			idFacturaDigital = registrosPendientes.getData(0, "ID_FACTURA_DIGITAL");
			rec_rfc = registrosPendientes.getData(0, "REC_RFC");
			rec_regimen_fiscal = registrosPendientes.getData(0, "REC_REGIMEN_FISCAL");
			uso_cfdi = registrosPendientes.getData(0, "USO_CFDI");
			testCase.addQueryEvidenceCurrentStep(registrosPendientes);
			
			assertEquals(rec_rfc, data.get("REC_RFC"), "El REC_RFC obtenido no es el esperado");
			assertEquals(rec_regimen_fiscal, data.get("REC_REGIMEN_FISCAL"), "El REC_REGIMEN_FISCAL obtenido no es el esperado");
			assertEquals(uso_cfdi, data.get("USO_CFDI"), "El USO_CFDI obtenido no es el esperado");
		}
		assertFalse(validateRegistrosPendientes, "No se encontraron registros por procesar");
		
		/******************************Paso 2**************************/
		addStep("Ejecutar el siguiente query para validar si hay registros en la tabla XXFC_CFD_FACTURA_DIGITAL_LINES");
		String formatRegistroslineas = String.format(queryRegistrosLines, idFacturaDigital);
		SQLResult registrosLines = executeQuery(dbEBS, formatRegistroslineas);
		System.out.println(formatRegistroslineas);
		boolean validateRegistrosLines = registrosLines.isEmpty();
		if(!validateRegistrosLines) {
			testCase.addQueryEvidenceCurrentStep(registrosLines);
		}
		assertFalse(validateRegistrosLines, "No se muestra los registros de impuestos para la factura");
		
		/******************************Paso 3**************************/
		addStep("Ejecutar el siguiente query validar si existe registros de la factura en la tabla XXFC_CFD_FACTURA_DIGITAL_TAX");
		String formatRegistrosFactura = String.format(queryRegistrosFactura, idFacturaDigital);
		SQLResult registrosFactura = executeQuery(dbEBS, formatRegistrosFactura);
		System.out.println(queryRegistrosFactura);
		boolean validateRegistrosFactura = registrosFactura.isEmpty();
		if(!validateRegistrosFactura) {
			testCase.addQueryEvidenceCurrentStep(registrosFactura);
		}
		assertFalse(validateRegistrosFactura, "No se muestra los registros de traslados para la factura");
		/******************************Paso 4**************************/
		addStep("Ejecutar interfaz OE2_MX desde servidor FCWMINTQA3");
		
		
		/******************************Paso 5**************************/
		addStep("Ejecutar el siguiente query en la BD FCWMLQA.FEMCOM.NET para validar el estatus de la ejecucion");
		SQLResult estatusEjecucion = executeQuery(dbLog, queryStatusEjecucion);
		System.out.println(queryStatusEjecucion);
		boolean validateEstatusEjecucion = estatusEjecucion.isEmpty();
		String runID = "";
		if(!validateEstatusEjecucion) {
			runID = estatusEjecucion.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(estatusEjecucion);
		}
		assertFalse(validateEstatusEjecucion, "No se muestran registros de ejecucion");
		/******************************Paso 6**************************/
		addStep("Ejecutar el siguiente query para validar el estatus del threads, en la tabla WM_LOG_THREAD de la BD FCWMLQA");
		String formatEstatusThreads = String.format(queryStatusThreads, runID);
		SQLResult estatusThreads = executeQuery(dbLog, formatEstatusThreads);
		System.out.println(queryStatusThreads);
		boolean validateEstatusThreads = estatusThreads.isEmpty();
		if(!validateEstatusThreads) {
			testCase.addQueryEvidenceCurrentStep(estatusThreads);
		}
		assertFalse(validateEstatusThreads, "No se muestran registros");
		/******************************Paso 7**************************/
		String formatRegistrosProcesados = String.format(queryRegistrosProcesados, data.get("ANIO"), data.get("MES"));
		addStep("Ejecutar el siguiente query en la BD AVEBQA para validar los registros procesados se muestre actualizado");
		SQLResult registrosProcesados = executeQuery(dbEBS, formatRegistrosProcesados);
		System.out.println(formatRegistrosProcesados);
		boolean validateRegistrosProcesados = registrosProcesados.isEmpty();
		String wmStatus = "";
//		String doctoXML = "";
		if(!validateRegistrosProcesados) {
			wmStatus = registrosProcesados.getData(0, "WM_STATUS");
//			doctoXML = registrosProcesados.getData(0, "DOCTO_XML");
			testCase.addQueryEvidenceCurrentStep(registrosProcesados);
//			testCase.addTextEvidenceCurrentStep(doctoXML);
			assertEquals(wmStatus, data.get("WM_STATUS"), "El estatus esperado es diferente a E");
		}
		assertFalse(validateRegistrosProcesados, "No se muestran registros procesados");
		
		/******************************Paso 8**************************/
		addStep("Consultar la tabla CFD_TRANSACTION en la BD FCTPEQA, para validar los datos de la factura");
		String formatDatosFactura = String.format(queryDatosFactura, idFacturaDigital);
		SQLResult datosFactura = executeQuery(dbFCTPEQA, formatDatosFactura);
		System.out.println(queryDatosFactura);
		boolean validateDatosFactura = datosFactura.isEmpty();
		if(!validateDatosFactura) {
			testCase.addQueryEvidenceCurrentStep(datosFactura);
		};
		assertFalse(validateDatosFactura, "No se muestran datos de la factura");
		
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
		return "Se realizara la ejecucion de la interfaz OE2 a traves de HTTP REQUEST realizado por el modulo "
				+ "de facturacion electronica. La interfaz generara un XML de entrada y sera recibido por la "
				+ "interfaz PE4 y recibira un XML de salida con la informacion del CFDI.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO DE AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_005_OE2_MX_ProcesamientoDeFacturaConRFC_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}