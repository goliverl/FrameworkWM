package interfaces.Tpe_Loy;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.Tpe_Loy;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_004_TPE_LOY_TransacAcumulNoExitosa extends BaseExecution {

	/**
	 * TPE_LOY: MTC-FT-004 TPE_LOY Transaccion de acumulacion no exitosa
	 * Desc:
	 * Prueba de regresion  para comprobar la no afectación en la funcionalidad de Transaccion
	 * de acumulación no exitosa de la interface FEMSA_TPE_LOY al ser migrada de webmethods
	 * v10.5 a webmethods 10.11 y del sistema operativo Solaris a Linux redhat 8.5 (X86).
	 * 
	 * 
	 * @author Jose Onofre
	 * @date 01/11/2023
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_TPE_LOY_TransacAcumulNoExitosa_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		SQLUtil dbOXTPEQA = new SQLUtil(GlobalVariables.DB_HOST_OXTPEQA, GlobalVariables.DB_USER_OXTPEQA, GlobalVariables.DB_PASSWORD_OXTPEQA);
		
		//SQLUtil dbOXWMLOGQA = new SQLUtil(GlobalVariables.DB_HOST_OXWMLOGQA, GlobalVariables.DB_USER_OXWMLOGQA, GlobalVariables.DB_PASSWORD_OXWMLOGQA);

		
		Tpe_Loy configUtil = new Tpe_Loy(data, testCase, dbOXTPEQA);
		
		
		/*
		 * Variables
		 *********************************************************************/
		
		String consulta_AcumulacionPts = "SELECT * FROM TPEUSER.TPE_FR_TRANSACTION "
				+ "WHERE CREATION_DATE >= '%s' AND "
				+ "APPLICATION = '%s' AND ENTITY = '%s' "
				+ "AND FOLIO IN ('%s', '%s') "
				+ "ORDER BY CREATION_DATE DESC";
		
		String consulta_WMLOGError = "SELECT * FROM WMLOG.WM_LOG_ERROR_TPE WHERE "
				+ "ERROR_DATE BETWEEN '%s' "
				+ "AND '%s' ORDER BY ERROR_DATE DESC";
		
		String consulta_SecurityLog = "SELECT * FROM WMLOG.SECURITY_SESSION_LOG WHERE "
				+ "CREATION_DATE BETWEEN '%s' AND "
				+ "'%s' AND APPLICATION = '%s' "
				+ "ORDER BY CREATION_DATE DESC;";
		
		String respuestaQRY01;
		String respuestaTRN01;
		String respuestaOPR01;
		
		String expect_operation = "QRY01";
		String expect_wm_code = "101";
		String expect_wm_code_no = "040";
		
		
		testCase.setProject_Name(data.get("name"));
		
		testCase.setPrerequisites(data.get("prerequisitos"));
	
		/********************************************************************************
		 * Ejecucion de la peticion del servico QRY01
		 * 
		 ********************************************************************************/
		
		// Paso 1 ****************************************************
		
		addStep("Ingresar a un navegador de Internet");
		
		
		// Paso 2 ****************************************************
	
		addStep("Ingresar al navegador para solicitar informacion de tarjeta "
				+ "premia (QRY01) mediante el siguiente request");
		
		respuestaQRY01 = configUtil.QRY01();
		
		System.out.println("Respuesta: " + respuestaQRY01);
		
		
		
		// Paso 3 ****************************************************
		
		addStep("Ingresar al navegador para solicitar un folio (TRN01) "
				+ "mediante el siguiente request");
		
		respuestaTRN01 = configUtil.TRN01();
				
		System.out.println("Respuesta: " + respuestaTRN01);
		
		String WmCodigoTRN01 = RequestUtil.getWmCodeXml(respuestaTRN01);
		System.out.println(WmCodigoTRN01);
		
		
		
		// Paso 4 ****************************************************
		
		addStep("Ingresar al navegador para solicitar una acumulación de "
				+ "puntos (COLLECT OPR01) mediante el siguiente request");
		
		
		respuestaOPR01 = configUtil.OPR01();
		
		System.out.println("Respuesta: " + respuestaOPR01);
		
		String WmCodigoOPR01 = RequestUtil.getWmCodeXml(respuestaOPR01);
		System.out.println(WmCodigoOPR01);
		
		
		// Paso 5 ****************************************************
		
		addStep("Establecer conexion con la BD 'OXTPEQA_PREM' ");
		
		boolean conexiondbOXTPEQA = true;
		
		testCase.addTextEvidenceCurrentStep("Conexion: dbOXTPEQA");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_OXTPEQA);
		
		assertTrue(conexiondbOXTPEQA, "La conexion no fue exitosa");
		
		
		// Paso 6 ****************************************************
			
		addStep("Validar que se registró la transacción exitosamente en la BD OXTPEQA_PREM");
		
		String consulta_AcumulacionPts_r = String.format(consulta_AcumulacionPts, data.get("creation_date") ,data.get("application") ,
				data.get("entity"), data.get("folio1"), data.get("folio2"));
		
		SQLResult consultaRefAcumulacionPts_r = executeQuery(dbOXTPEQA, consulta_AcumulacionPts_r);
		
		boolean validaConsultaAcumulacionPts = consultaRefAcumulacionPts_r.isEmpty();
		
		String operation = null;
		String wm_code_no = null;
		
		if(!validaConsultaAcumulacionPts) {
        	
			operation = consultaRefAcumulacionPts_r.getData(0, "OPERATION");
			wm_code_no = consultaRefAcumulacionPts_r.getData(0, "WM_CODE");
        	
        	testCase.addQueryEvidenceCurrentStep(consultaRefAcumulacionPts_r);
        	
        	
        } else {
        	
        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta");
        	testCase.addQueryEvidenceCurrentStep(consultaRefAcumulacionPts_r);
        	
        }
		
		
		assertEquals(expect_operation,operation);
		assertEquals(expect_wm_code_no,wm_code_no);
		
		
		// Paso 7 ****************************************************
	/*	
		addStep("Establecer conexion con la BD 'OXWMLOGQA_PREM' ");
		
		boolean conexiondbOXWMLOGQA = true;
		
		testCase.addTextEvidenceCurrentStep("Conexion: dbOXWMLOGQA");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_OXWMLOGQA);
		
		assertTrue(conexiondbOXWMLOGQA, "La conexion no fue exitosa");
		
		// Paso 8 ****************************************************
		
		addStep("Validar que no se registraron errores en la ejecución");
	
		String consulta_consulta_WMLOGError_r = String.format(consulta_WMLOGError,
				data.get("error_date1") ,data.get("error_date2"));
		
		SQLResult consultaRefWMLOGError_r = executeQuery(dbOXWMLOGQA, consulta_consulta_WMLOGError_r);
		
		boolean validaConsultaWMLOGError = consultaRefWMLOGError_r.isEmpty();
		
		if(!validaConsultaWMLOGError) {
        	
        	testCase.addQueryEvidenceCurrentStep(consultaRefWMLOGError_r);
        	
        	
        } else {
        	
        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta");
        	testCase.addQueryEvidenceCurrentStep(consultaRefWMLOGError_r);
        	
        }
		
		// Paso 9 ****************************************************
		
		addStep("validar el registro de las ejecuciones procesadas");
		
		String consulta_SecurityLogr_r = String.format(consulta_SecurityLog,
				data.get("creation_date") ,data.get("creation_date2"), 
				data.get("application"));
		
		SQLResult consultaRefSecurityLog_r = executeQuery(dbOXWMLOGQA, consulta_SecurityLogr_r);
		
		boolean validaConsultaSecurityLog = consulta_SecurityLogr_r.isEmpty();
		
		if(!validaConsultaSecurityLog) {
        	
        	testCase.addQueryEvidenceCurrentStep(consultaRefSecurityLog_r);
        	
        	
        } else {
        	
        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta");
        	testCase.addQueryEvidenceCurrentStep(consultaRefSecurityLog_r);
        	
        }
		
		assertFalse(validaConsultaSecurityLog, "No se muestran resultados");
		*/
	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_004_TPE_LOY_TransacAcumulNoExitosa_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "JoseOnofre@Hexaware.com";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
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

}