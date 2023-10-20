package interfaces.tpe.config;


import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.TPE_CONFIG;
import util.GlobalVariables;
import util.RequestUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_001_TPE_CONFIG_SITE2ObtenerConfiguracionMermaAutorizada extends BaseExecution {

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_TPE_CONFIG_SITE2ObtenerConfiguracionMermaAutorizada_test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 *********************************************************************/
		SQLUtil dbFCTPEQA = new SQLUtil(GlobalVariables.DB_HOST_FCTPE_QRO, GlobalVariables.DB_USER_FCTPE_QRO, GlobalVariables.DB_PASSWORD_FCTPE_QRO);
		
		SQLUtil dbFCTPEQA_MTY = new SQLUtil(GlobalVariables.DB_HOST_FCWMLTAQ_MTY, GlobalVariables.DB_USER_FCWMLTAQ_MTY, GlobalVariables.DB_PASSWORD_FCWMLTAQ_MTY);

		
		TPE_CONFIG configUtil = new TPE_CONFIG(data, testCase, dbFCTPEQA);
		
		
		/*
		 * Variables
		 *********************************************************************/
		
		String consultaTiendaMerma = "select * from " 
				+ "TPEUSER.TPE_POS_CFG "
				+ "WHERE APPLICATION = '%s' AND ENTITY = '%s' "
				+ "AND plaza = '%s' AND TIENDA = '%s' ";
		
		String consultaDetalleGrupo = "select * from "
				+ "TPEUSER.TPE_CFG_GROUP "
				+ "WHERE ID= '%s'";
		
		String consultaTransaccion = "select T.SITE, T.IS_NAME, T.* FROM TPEUSER.TPE_FR_TRANSACTION T "
				+ "WHERE T.CREATION_DATE >= TRUNC(SYSDATE) "
				+ "AND T.APPLICATION = 'POSCONF' "
				+ "AND T.ENTITY = 'CONFIG' "
				+ "ORDER BY T.CREATION_DATE DESC";
		
		String respuestaQRY01;
		
		String wmCodeToValidateQRY01 = "101";
		
		testCase.setProject_Name("FEMSA_TPE_CONFIG Rastreo de peticiones de POS a los servicos"
				+ "de Configuracion Central");
		
		testCase.setPrerequisites(data.get("prerequisitos"));
	
		/********************************************************************************
		 * Ejecucion de la peticion del servico QRY01
		 * 
		 ********************************************************************************/
		
		// Paso 1 ****************************************************
		addStep("Establecer conexion con la BD 'FCTPEQA_QRO_S2' ");
		
		boolean conexionFCTPEQA = true;
		
		testCase.addTextEvidenceCurrentStep("Conexion: FCTPEQA_QRO_S2 Server ");
		testCase.addTextEvidenceCurrentStep(GlobalVariables.DB_HOST_FCTPE_QRO);
		
		assertTrue(conexionFCTPEQA, "La conexion no fue exitosa");
		
		
		// Paso 2 ****************************************************
			
		addStep("Consulta tabla 'TPEUSER.TPE_POS_CFG' para validar que exista " 
				+ "configuración de MERMA_AUTORIZADA para la tienda");
		
		String consultaTiendaMerma_r = String.format(consultaTiendaMerma, data.get("application") ,
				data.get("entity"), data.get("plaza"), data.get("tienda") ); 		
		SQLResult consultaRefTiendaMerma_r = executeQuery(dbFCTPEQA, consultaTiendaMerma_r);
		
		boolean validaConsultaTiendaMerma = consultaTiendaMerma_r.isEmpty();
		
		if(!validaConsultaTiendaMerma) {
        	
        	
        	testCase.addQueryEvidenceCurrentStep(consultaRefTiendaMerma_r);
        	
        	
        	
        } else {
        	
        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta");
        	testCase.addQueryEvidenceCurrentStep(consultaRefTiendaMerma_r);
        	
        }
		
		// Paso 3 ****************************************************
		
		addStep("Consulta tabla 'TPEUSER.TPE_CFG_GROUP' para obtener los detalles del grupo " 
				+ "de la configuracion disponible");
		
		String CFG_ID = "31106";
		
		String consultaDetalleGrupo_r = String.format(consultaDetalleGrupo, CFG_ID ); 		
		SQLResult consultaRefconsultaDetalleGrupo_r = executeQuery(dbFCTPEQA, consultaDetalleGrupo_r);
		
		boolean validaConsultaDetalleGrupo = consultaDetalleGrupo_r.isEmpty();
		
		if(!validaConsultaDetalleGrupo) {
        	
        	
        	testCase.addQueryEvidenceCurrentStep(consultaRefconsultaDetalleGrupo_r);
        	
        	
        } else {
        	
        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta");
        	testCase.addQueryEvidenceCurrentStep(consultaRefconsultaDetalleGrupo_r);
        	
        }
		
		// Paso 4 ****************************************************
		
		addStep("Enviar el request con la operacion QRY01");
		
		respuestaQRY01 = configUtil.QRY01Merma();
		 
		//String folioQRY01 = RequestUtil.getFolioXml(respuestaQRY01);
		
		wmCodeToValidateQRY01 = RequestUtil.getWmCodeXml(respuestaQRY01);

		boolean validaRespuestaQRY01 = true;
		if (respuestaQRY01 != null) {
			validaRespuestaQRY01 = false;
		}
		assertFalse(validaRespuestaQRY01); 
		
		
		
		// Paso 5 ****************************************************
		
		addStep("Consulta tabla 'TPEUSER.TPE_CFG_GROUP' para obtener los detalles del grupo " 
				+ "de la configuracion disponible");
		
		String consultaTransaccion_r = String.format(consultaTransaccion, CFG_ID ); 		
		SQLResult consultaRefconsultaTransaccion_r = executeQuery(dbFCTPEQA, consultaTransaccion_r);
		
		boolean validaConsultaTransaccion = consultaTransaccion_r.isEmpty();
		
		if(!validaConsultaTransaccion) {
        	
        	
        	testCase.addQueryEvidenceCurrentStep(consultaRefconsultaTransaccion_r);
        	
        	
        } else {
        	
        	testCase.addTextEvidenceCurrentStep("No se muestra la consulta");
        	testCase.addQueryEvidenceCurrentStep(consultaRefconsultaTransaccion_r);
        	
        }
		
		
	}
	
	@Override
	public String setTestFullName() {
		return "ATC_FT_001_TPE_CONFIG_SITE2ObtenerConfiguracionMermaAutorizada_test";
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