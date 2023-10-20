package interfaces.PR50V2_PER;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.apache.commons.math3.analysis.function.Sqrt;
import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/*
 * I20016 Actualizacion tecnologica Webmethods 10 - Enviar archivo RTV a traves de la DS50-TN-PR50 a WM
 * 
 * Prueba de regresion  para comprobar la no afectacion en la funcionalidad 
 * principal de la interface FEMSA_PR50V2 para archivos RTV 
 * (Devoluciones de Compras a Proveedores) de subida (INBOUND) 
 * al ser migrada de WM9.9 a WM10.5
 * 
 * @author Edwin Ramirez
 * @date 23/02/2023
 * 
 */


public class ATC_FT_004_PR50V2_PER_Enviar_Archivo_RTV extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_PR50V2_PER_Enviar_Archivo_RTV_test (HashMap<String, String> data) throws Exception{
		/*
		 * Utileria*************************************************/
		SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,GlobalVariables.DB_PASSWORD_Oiwmqa);
		

		/*
		 * Variables************************************************/
		String queryFile = "SELECT * \r\n"
				+ "FROM POSUSER.POS_ENVELOPE \r\n"
				+ "WHERE PV_ENVELOPE_ID = '%s' \r\n"
				+ "AND PARTITION_DATE >= TRUNC(SYSDATE)";
		
		String queryFileStatus = "SELECT * \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ "WHERE  PV_DOC_NAME = '%s'";
		
		String queryRecordNumber = "SELECT * \r\n"
				+ "FROM  POSUSER.POS_RTV \r\n"
				+ "WHERE PID_ID = '%s'";
		
		String queryFileInfo = "SELECT * \r\n"
				+ "FROM POSUSER.POS_RTV_DETL \r\n"
				+ "WHERE PID_ID = '%s'";
		testCase.setTest_Description(data.get("id")+ data.get("descripcion"));
		
		/***************************Paso 1**************************/
		addStep("Solicitar al equipo de Xpos realizar los movimientos o transacciones de Devoluciones de Compras a "
				+ "Proveedores y solicitar la ejecución del proceso Fin Dia .para generar el archivo RTV.TXT.");
		
		
		/***************************Paso 2**************************/
		addStep("Ejecutar el programa de DS50 para el envío del archivo RTV.TXT.");
		
		
		/***************************Paso 3**************************/
		addStep("Realizar la consulta de la BD FCWMQA_PERU");
		String formatFile = String.format(queryFile, data.get("EnvelopeID"));
		SQLResult file = executeQuery(dbPos, formatFile);
		System.out.println(formatFile);
		boolean validateFile = file.isEmpty();
		if(!validateFile) {
			testCase.addQueryEvidenceCurrentStep(file);
		}
		assertFalse(validateFile, "No se obtuvieron registros");
		/***************************Paso 4**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_INBOUND_DOCS de la BD FCMWQA_PERU para "
				+ "validar que el archivo <ARCHIVO.TXT > fue enviado a WM y cuente con el estatus=I (Insertado).");
		String formatFileStatus = String.format(queryFileStatus, data.get("FileName"));
		SQLResult fileStatus = executeQuery(dbPos, formatFileStatus);
		System.out.println(formatFileStatus);
		boolean validateFileStatus = fileStatus.isEmpty();
		String posInboundDocID = "";
		if(!validateFileStatus) {
			posInboundDocID = fileStatus.getData(0, "PID_ID");
			testCase.addQueryEvidenceCurrentStep(fileStatus);
		}
		assertFalse(validateFileStatus, "No se obtuvieron registros.");
		/***************************Paso 5**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_RTV de la BD FCMWQA_PERU para validar  el número de registros.");
		String formatRecordNumber = String.format(queryRecordNumber, posInboundDocID);
		SQLResult recordNumber = executeQuery(dbPos, formatRecordNumber);
		System.out.println(formatRecordNumber);
		boolean validateRecordNumber = recordNumber.isEmpty();
		if(!validateRecordNumber) {
			testCase.addQueryEvidenceCurrentStep(recordNumber);
		}
		assertFalse(validateRecordNumber, "No se obtuvieron registros");
		/***************************Paso 6**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_RTV_DETL de la BD FCMWQA_PERU para validar "
				+ "la información contenida en el archivo <ARCHIVO.TXT>.");
		String formatFileInfo = String.format(queryFileInfo, posInboundDocID);
		SQLResult fileInfo = executeQuery(dbPos, formatFileInfo);
		System.out.println(formatFileInfo);
		boolean validateFileInfo = fileInfo.isEmpty();
		if(!validateFileInfo) {
			testCase.addQueryEvidenceCurrentStep(fileInfo);
		}
		assertFalse(validateFileInfo, "No se obtuvieron registros.");
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
		return null;
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}
