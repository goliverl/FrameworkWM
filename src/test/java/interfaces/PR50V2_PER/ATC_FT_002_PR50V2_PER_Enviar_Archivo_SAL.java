package interfaces.PR50V2_PER;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/*
 * /*
 * I20016 Actualizacion tecnologica Webmethods 10 - Enviar archivo SAL a través de la DS50-TN-PR50 a WM
 * Prueba de regresion  para comprobar la no afectación en la funcionalidad principal de la interface 
 * FEMSA_PR50V2 para archivos de subida (INBOUND) al ser migrada de WM9.9 a WM10.5
 * 
 * @author Edwin Ramirez
 * @date 23/02/2023
 * 
 */

public class ATC_FT_002_PR50V2_PER_Enviar_Archivo_SAL extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PR50V2_PER_Enviar_Archivo_SAL_test (HashMap<String, String> data) throws Exception{
		/*
		 * Utileria*************************************************/
		SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,GlobalVariables.DB_PASSWORD_Oiwmqa);
		
		/*
		 * Variables************************************************/
		String queryFCWMQA = "SELECT * \r\n"
				+ "FROM POSUSER.POS_ENVELOPE \r\n"
				+ "WHERE PV_ENVELOPE_ID = '%s' \r\n"
				+ "AND PARTITION_DATE >= TRUNC(SYSDATE)";
		
		String querySendingStatus = "SELECT * \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ "WHERE PV_DOC_NAME = '%s'";
		
		String queryRecordNumber = "SELECT * \r\n"
				+ "FROM POSUSER.POS_SAL \r\n"
				+ "WHERE PID_ID = '%s'";
		
		String queryFileInfo = "SELECT * \r\n"
				+ "FROM  POSUSER.POS_SAL_DETL \r\n"
				+ "WHERE PID_ID = '%s'";
		
		testCase.setTest_Description(data.get("id")+ data.get("descripcion"));
		
		/***************************Paso 1**************************/
		addStep("Solicitar al equipo de POS realizar los movimientos o transacciones de Cobros de Servicios para generar el archivo SAL.");
		
		
		/***************************Paso 2**************************/
		addStep("Ejecutar el Proceso de Fin Día");
		
		/***************************Paso 3**************************/
		addStep("Ejecutar el programa de DS50 para el envío del archivo SAL.TXT.");
		
		
		/***************************Paso 4**************************/
		addStep("Realizar la consulta de la BD FCWMQA_PERU");
		String formatQuery = String.format(queryFCWMQA, data.get("EnvelopeID"));
		SQLResult querying = executeQuery(dbPos, formatQuery);
		System.out.println(formatQuery);
		boolean validateQuerying = querying.isEmpty();
		if(!validateQuerying) {
			testCase.addQueryEvidenceCurrentStep(querying);
		}
		assertFalse(validateQuerying, "No se obtuvieron registros.");
		/***************************Paso 4**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_INBOUND_DOCS de la BD FCWMQA_PERU para "
				+ "validar que el archivo SAL  fue enviado a WM y cuente con el estatus=I (Insertado).");
		String formatSendingStatus = String.format(querySendingStatus, data.get("FileName"));
		SQLResult sendingStatus = executeQuery(dbPos, formatSendingStatus);
		System.out.println(formatSendingStatus);
		String posInboundDocID = "";
		boolean validateSendingStatus = sendingStatus.isEmpty();
		if(!validateSendingStatus) {
			posInboundDocID = sendingStatus.getData(0, "PID_ID");
			testCase.addQueryEvidenceCurrentStep(sendingStatus);
		}
		assertFalse(validateSendingStatus, "No se obtuvieron registros");
		/***************************Paso 5**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_SAL de la BD FCWMQA_PERU para validar el número de registros.");
		String formatRecordNumber = String.format(queryRecordNumber, posInboundDocID);
		SQLResult recordNumber = executeQuery(dbPos, queryRecordNumber);
		System.out.println(formatRecordNumber);
		boolean validateRecordNumber = recordNumber.isEmpty();
		if(!validateRecordNumber) {
			testCase.addQueryEvidenceCurrentStep(recordNumber);
		}
		assertFalse(validateRecordNumber, "No se obtuvieron registros.");
		/***************************Paso 6**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_SAL_DETL de la BD FCWMQA_PERU para validar "
				+ "que el resultado de la consulta coincida con la información contenida en el archivo SAL.TXT");
		String formatFileInfo = String.format(queryFileInfo, posInboundDocID);
		SQLResult fileInfo = executeQuery(dbPos, formatFileInfo);
		System.out.println(formatFileInfo);
		boolean validateFileInfo = fileInfo.isEmpty();
		if(!validateFileInfo) {
			testCase.addQueryEvidenceCurrentStep(fileInfo);
		}
		assertFalse(validateFileInfo, "No se obtuvieron registros");
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
