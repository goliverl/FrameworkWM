package interfaces.tpe.prom;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_PROM;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_008_TPE_PROM_SolicitRedencPromoEmplSolicitSaldos extends BaseExecution{
	
	/**
	 * TPE_PROM: MTC-FT-060 Solicitud de redencion de promocion a empleado y Solicitud de Saldos
	 *
	 * Desc: Prueba de regresión para comprobar la no afectación en la funcionalidad principal 
	 * de transacciones de la interface TPE_PROM y TPE_FR al ser migradas de WM9.9 a WM10.5 
	 * para una transacción de Solicitud de redención de promoción a empleado y Solicitud de Saldos. 
	 * 
	 * @author Jose Onofre
	 * @date 02/16/2023
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_008_TPE_PROM_SolicitRedencPromoEmplSolicitSaldos_test(HashMap<String, String> data) throws Exception {
		
/*
 * Utilerías *********************************************************************/

		//SQLUtil dbFCM = new SQLUtil(GlobalVariables.DB_HOST_FCMFS, GlobalVariables.DB_USER_FCMFS, GlobalVariables.DB_PASSWORD_FCMFS);
		SQLUtil dbFCTPEQA = new SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE, GlobalVariables.DB_PASSWORD_FCTPE);
		
		SQLUtil dbFCTPEQA_QRO = new SQLUtil(GlobalVariables.DB_HOST_FCTPE_QRO, GlobalVariables.DB_USER_FCTPE_QRO, GlobalVariables.DB_PASSWORD_FCTPE_QRO);
		
		SQLUtil dbFCWMLTAQ = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAQ_MTY, GlobalVariables.DB_USER_FCWMLTAQ_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAQ_MTY);
		
		SQLUtil dbFCWMLTAQ_QRO = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO, GlobalVariables.DB_USER_FCWMLTAEQA_QA, GlobalVariables.DB_PASSWORD_FCWMLTAEQA_QRO_QA);
		
		
		TPE_PROM configUtil = new TPE_PROM(data, testCase, dbFCTPEQA);
/**
* Variables ******************************************************************************************
* 
* 
*/	
		String consulta_TransAlimenticio = "SELECT APPLICATION, ENTITY, OPERATION,CREATION_DATE, "
				+ "PLAZA,TIENDA,PAN,WM_CODE,ORIG_DATA_ELEMS "
				+ "FROM TPEUSER.TPE_FR_TRANSACTION WHERE CREATION_DATE >= '%s' "
				+ "AND APPLICATION='%s' "
				+ "AND TIENDA='%s' ORDER BY CREATION_DATE DESC";
		
		String consulta_WMLOG_Error = "select ERROR_ID, FOLIO, ERROR_DATE, ERROR_CODE, DESCRIPTION, "
				+ "TPE_TYPE from WMLOG.WM_LOG_ERROR_TPE TPE "
				+ "WHERE TPE_TYPE='%s' AND ROWNUM <=5";
		
		String respuestaQRY01;
		String expec_respQRY01 = "101";
		String respuestaQRY02;
		String expec_respQRY02 = "101";
		
		testCase.setProject_Name(data.get("name"));
		testCase.setTest_Description(data.get("description"));
		testCase.setPrerequisites(data.get("prerequisitos"));
	
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
		// Paso 1 ****************************************************
		
		addStep("Ejecutar una transacción de consulta de rendición de saldos");
		
		respuestaQRY01 = configUtil.QRY01();
		
		System.out.println("Respuesta: " + respuestaQRY01);
		
		String WmCodigoQRY01 = RequestUtil.getWmCodeXml(respuestaQRY01);
		System.out.println("WmCode: " + WmCodigoQRY01);
		testCase.addBoldTextEvidenceCurrentStep("Expected WmCode: " + expec_respQRY01);
		testCase.addBoldTextEvidenceCurrentStep("Current WmCode: " + WmCodigoQRY01);
		
		assertEquals(expec_respQRY01,WmCodigoQRY01);
		
		
		// Paso 2 ****************************************************
		
		addStep("Ejecutar una transaccion de solicitud de saldos");
		
		respuestaQRY02 = configUtil.QRY02();
		
		System.out.println("Respuesta: " + respuestaQRY02);
		
		String WmCodigoQRY02 = RequestUtil.getWmCodeXml(respuestaQRY02);
		System.out.println("WmCode: " + WmCodigoQRY02);
		testCase.addBoldTextEvidenceCurrentStep("Expected WmCode: " + expec_respQRY02);
		testCase.addBoldTextEvidenceCurrentStep("Current WmCode: " + WmCodigoQRY02);
		
		assertEquals(expec_respQRY02,WmCodigoQRY02);
		
		// Paso 3 ****************************************************
		
		//*********** Paso 3.1 **************
		addStep("Conectarse a la Base de Datos **FCTPEQA** en esquema **TPEUSER** Monterrey y Queretaro ");
		
		testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
		
		boolean conexionFCTPEQA_MTY = true;
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCTPEQA_MTY ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCTPE);
		
		assertTrue(conexionFCTPEQA_MTY, "La conexion no fue exitosa");
		
		//*********** Paso 3.2 **************
		
		testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
		
		boolean conexionFCTPEQA_QRO = true;
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCTPEQA_QRO_S2 Server ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCTPE_QRO);
		
		assertTrue(conexionFCTPEQA_QRO, "La conexion no fue exitosa");
		
		// Paso 4 ****************************************************
		
		addStep("Validar la transaccion de Apoyo Alimentario en la tabla "
				+ "'TPE_FR_TRANSACTION' de la BD **FCTPEQA**");
		
		//*********** Paso 4.1 Site MTY**************
		
		String consulta_TransAlimenticio_s = String.format(consulta_TransAlimenticio,data.get("create_date"), data.get("application"), data.get("tienda")); 		
		SQLResult consulta_TransAlimenticio_r = executeQuery(dbFCTPEQA, consulta_TransAlimenticio_s);
		
		boolean validaConsultaTransAlimenticio = consulta_TransAlimenticio_r.isEmpty();
		
		testCase.addBoldTextEvidenceCurrentStep("Site FCTPEQA_MTY: \n");
		
		if(!validaConsultaTransAlimenticio) {
        	
        	testCase.addQueryEvidenceCurrentStep(consulta_TransAlimenticio_r);
   
        } else {
        	
        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta");
        	testCase.addQueryEvidenceCurrentStep(consulta_TransAlimenticio_r);
        	
        }
		

		//*********** Paso 4.2 Site QRO**************
		
		
		SQLResult consulta_TransAlimenticioQRO_r = executeQuery(dbFCTPEQA_QRO, consulta_TransAlimenticio_s);
		
		boolean validaConsultaTransAlimenticioQRO = consulta_TransAlimenticioQRO_r.isEmpty();
		
		testCase.addBoldTextEvidenceCurrentStep("Site FCTPEQA_QRO: \n");
		
		if(!validaConsultaTransAlimenticioQRO) {
        	
        	testCase.addQueryEvidenceCurrentStep(consulta_TransAlimenticioQRO_r);
     
        } else {
        	
        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta");
        	testCase.addQueryEvidenceCurrentStep(consulta_TransAlimenticioQRO_r);
        	
        }
		
		
		// Paso 5 ****************************************************
		
		//*********** Paso 5.1 **************
		addStep("Conectarse a la Base de Datos **FCTPEQA** en esquema **TPEUSER** Monterrey y Queretaro ");
				
		testCase.addBoldTextEvidenceCurrentStep("Site 1: ");
				
		boolean conexionFCWMLTAQ_MTY = true;
				
		testCase.addTextEvidenceCurrentStep("Conexion: FCWMLTAQ_MTY ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLTAQ_MTY);
				
		assertTrue(conexionFCWMLTAQ_MTY, "La conexion no fue exitosa");
				
		//*********** Paso 5.2 **************
				
		testCase.addBoldTextEvidenceCurrentStep("Site 2: ");
				
		boolean conexionFCWMLTAQ_QRO = true;
				
		testCase.addTextEvidenceCurrentStep("Conexion: FCWMLTAQ_QRO_S2 Server ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCWMLTAEQA_QRO);
				
		assertTrue(conexionFCWMLTAQ_QRO, "La conexion no fue exitosa");
		
		// Paso 6 ****************************************************
		
		addStep("Validar en la base de datos **FCWMLTAQ** que no se encuentren "
				+ "registros de error de la PROM");
		
		//*********** Paso 6.1 Site MTY**************
		
		String consulta_WMLOG_Error_s = String.format(consulta_WMLOG_Error,data.get("application")); 		
		SQLResult consulta_WMLOG_Error_r = executeQuery(dbFCWMLTAQ, consulta_WMLOG_Error_s);
				
		boolean validaConsultaWMLOG_Error = consulta_WMLOG_Error_r.isEmpty();
				
		testCase.addBoldTextEvidenceCurrentStep("Site FCWMLTAQ_MTY: \n");
				
		if(validaConsultaWMLOG_Error) {
		        	
        	testCase.addQueryEvidenceCurrentStep(consulta_WMLOG_Error_r);
		   
		} else {
		        	
        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta");
	       	testCase.addQueryEvidenceCurrentStep(consulta_WMLOG_Error_r);
		        	
		        }
				

		//*********** Paso 6.2 Site QRO**************
				
				
		SQLResult consulta_WMLOG_ErrorQRO_r = executeQuery(dbFCWMLTAQ_QRO, consulta_WMLOG_Error_s);
				
		boolean validaConsultaWMLOG_ErrorQROQRO = consulta_WMLOG_ErrorQRO_r.isEmpty();
				
		testCase.addBoldTextEvidenceCurrentStep("Site FCWMLTAQ_QRO: \n");
				
		if(validaConsultaWMLOG_ErrorQROQRO) {
		        	
	       	testCase.addQueryEvidenceCurrentStep(consulta_WMLOG_ErrorQRO_r);
		     
	       } else {
		        	
		   	testCase.addTextEvidenceCurrentStep("No se muestra la consulta");
		   	testCase.addQueryEvidenceCurrentStep(consulta_WMLOG_ErrorQRO_r);
		        	
	       }
		
		
		
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
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "JoseO@Hexaware.com";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_008_TPE_PROM_SolicitRedencPromoEmplSolicitSaldos_test";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}