package interfaces.oe2_mx;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;

public class oe2_mx_ValidarInfoDeFacturaRfcNacional extends BaseExecution{
	
	/*
	 * 
	 * @cp MTC-FT-009- Validar el envio de informacion de factura con RFC Generico Nacional
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_007_OE2_MX_ValidarInfoDeFacturaRfcNacional_test(HashMap<String, String> data) throws Exception{
		/*
		 * Utileria****************************************************/
		utils.sql.SQLUtil dbEBS = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA,GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbFCTPEQA = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE,GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		/*
		 * Variables***************************************************/
		String queryRegistrosPendientes = "SELECT * \r\n"
				+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL  \r\n"
				+ "WHERE VERSION_CFDI ='4.0' \r\n"
				+ "AND WM_STATUS ='L' \r\n"
				+ "AND ANIO='2022' \r\n"
				+ "AND MES='04' \r\n"
				+ "AND ORIGEN ='AR'";
		
		String queryStatusEjecucion = "SELECT * \r\n"
				+ "FROM wmlog.wm_log_run \r\n"
				+ "where interface LIKE 'OE2%' \r\n"
				+ "AND START_DT>='26/04/2022' \r\n"
				+ "ORDER BY START_DT DESC";
		
		String queryStatusThreads = "SELECT * \r\n"
				+ "FROM  WMLOG.WM_LOG_THREAD \r\n"
				+ "WHERE PARENT_ID = %s";
		
		String queryRegistrosFactura = "SELECT * \r\n"
				+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL_TAX \r\n"
				+ "WHERE ID_FACTURA_DIGITAL in (%s)";
		
		String queryRegistrosProcesados = "SELECT * \r\n"
				+ "FROM XXFC.XXFC_CFD_FACTURA_DIGITAL  \r\n"
				+ "WHERE VERSION_CFDI ='4.0' \r\n"
				+ "AND WM_STATUS ='E' \r\n"
				+ "AND ANIO='2022' \r\n"
				+ "AND MES='04' \r\n"
				+ "AND ORIGEN ='AR'";
		
		String queryDatosFactura = "SELECT * \r\n"
				+ "FROM TPEUSER.CFD_TRANSACTION WHERE PLAZA = '17ADM' \r\n"
				+ "AND CREATION_DATE >= TRUNC(SYSDATE) \r\n"
				+ "AND REC_RFC = 'FLI170630TX7' \r\n"
				+ "AND TICKET = '%s'"
				+ "ORDER BY CREATION_DATE DESC";
		/******************************Paso 1**************************/
		addStep("Ejecutar el siguiente query para validar si hay registros a procesar por la interfaz");
		SQLResult registrosPendientes = executeQuery(dbEBS, queryRegistrosPendientes);
		System.out.println(queryRegistrosPendientes);
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
		addStep("Ejecutar interfaz OE2_MX desde servidor FCWMINTQA3");
		
		/******************************Paso 3**************************/
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
		/******************************Paso 4**************************/
		addStep("Ejecutar el siguiente query para validar el estatus del threads, en la tabla WM_LOG_THREAD de la BD FCWMLQA");
		String formatEstatusThreads = String.format(queryStatusThreads, runID);
		SQLResult estatusThreads = executeQuery(dbLog, formatEstatusThreads);
		System.out.println(queryStatusThreads);
		boolean validateEstatusThreads = estatusThreads.isEmpty();
		if(!validateEstatusThreads) {
			testCase.addQueryEvidenceCurrentStep(estatusThreads);
		}
		assertFalse(validateEstatusThreads, "No se muestran registros");
		/******************************Paso 5**************************/
		addStep("Ejecutar el siguiente query validar si existe registros de la factura en la tabla XXFC_CFD_FACTURA_DIGITAL_TAX");
		String formatRegistrosFactura = String.format(queryRegistrosFactura, idFacturaDigital);
		SQLResult registrosFactura = executeQuery(dbEBS, formatRegistrosFactura);
		System.out.println(queryRegistrosFactura);
		boolean validateRegistrosFactura = registrosFactura.isEmpty();
		if(!validateRegistrosFactura) {
			testCase.addQueryEvidenceCurrentStep(registrosFactura);
		}
		assertFalse(validateRegistrosFactura, "No se muestra los registros de traslados para la factura");
		/******************************Paso 6**************************/
		addStep("Ejecutar el siguiente query en la BD AVEBQA para validar los registros procesados se muestre actualizado");
		SQLResult registrosProcesados = executeQuery(dbEBS, queryRegistrosProcesados);
		System.out.println(queryRegistrosProcesados);
		boolean validateRegistrosProcesados = registrosProcesados.isEmpty();
		String wmStatus = "";
		String doctoXML = "";
		if(!validateRegistrosProcesados) {
			wmStatus = registrosProcesados.getData(0, "WM_STATUS");
			doctoXML = registrosProcesados.getData(0, "DOCTO_XML");
			testCase.addQueryEvidenceCurrentStep(registrosProcesados);
			testCase.addTextEvidenceCurrentStep(doctoXML);
			assertEquals(wmStatus, data.get("WM_STATUS"), "El estatus esperado es diferente a E");
		}
		assertFalse(validateRegistrosProcesados, "No se muestran registros procesados");
		/******************************Paso 7**************************/
		addStep("Consultar la BD FCTPEQA_MTY_S1, la tabla CFD_TRANSACTION, para validar los datos de la factura");
		String formatDatosFactura = String.format(queryDatosFactura, idFacturaDigital);
		SQLResult datosFactura = executeQuery(dbFCTPEQA, formatDatosFactura);
		System.out.println(queryDatosFactura);
		boolean validateDatosFactura = datosFactura.isEmpty();
		if(!validateDatosFactura) {
			testCase.addQueryEvidenceCurrentStep(datosFactura);
		}
		assertFalse(validateDatosFactura, "No se muestran datos de factura");
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
		return "Realizar la ejecucion de la interfaz OE2_MX, provocando un error en la interfaz para que la factura "
				+ "se guarde y validemos que los datos enviados son los correctos";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO DE AUTOMATIZACION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_007_OE2_MX_ValidarInfoDeFacturaRfcNacional_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
