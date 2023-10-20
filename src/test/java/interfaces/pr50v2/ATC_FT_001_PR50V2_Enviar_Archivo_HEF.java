package interfaces.pr50v2;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_001_PR50V2_Enviar_Archivo_HEF extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PR50V2_Enviar_Archivo_HEF_test(HashMap<String, String> data) throws Exception{
		
		/**
		 * Proyecto: RD Y BO Internacional (Regresion Enero 2023)
		 * Caso de prueba:MTC-FT-002-PR50_IN Enviar archivo HEF a través de la DS50-TN-PR50 a WM
		 * Descripcion: Prueba de regresion  para comprobar la no afectacion en la funcionalidad principal de la interface 
		 * FEMSA_PR50V2 para archivos de subida (INBOUND) al ser migrada de WM9.9 a WM10.5
		 * @author 
		 * @date 
		 */
		
		/*
		 * Utileria*************************************************/
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		/*
		 * Variables************************************************/
		String queryDs50Execution = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,PV_ENVELOPE_ID,RECEIVED_DATE \r\n"
				+ "FROM POSUSER.POS_ENVELOPE \r\n"
				+ "WHERE RECEIVED_DATE >= TRUNC(SYSDATE) \r\n"
				+ "AND PV_ENVELOPE_ID= '%s' \r\n"
				+ "ORDER BY PV_ENVELOPE_ID DESC ";
		
		String queryHEFSending = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,RECEIVED_DATE \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ "WHERE PV_DOC_NAME = '%s' \r\n"
				+ "AND PE_ID = '%s'";
		
		String queryRecordNumber = "SELECT PID_ID,NO_RECORDS,TOTAL_AMOUNT,PV_DATE,TOTAL_SAL,PARTITION_DATE \r\n"
				+ "FROM POSUSER.POS_HEF \r\n"
				+ "WHERE PID_ID = '%s'";
		
		String queryRecordNumberMatch = "SELECT PID_ID,NO_RECORDS,TOTAL_AMOUNT,PV_DATE,TOTAL_SAL,PARTITION_DATE \r\n"
				+ "FROM POSUSER.POS_HEF_DETL \r\n"
				+ "WHERE PID_ID = '%s'";
		
		
		/***************************Paso 1**************************/
		addStep("Solicitar al equipo de XPOS realizar los movimientos o transacciones pertinentes para al realizar Fin de Día, la Hoja de Entrega en el archivo HEF.xml.");
		
		/***************************Paso 2**************************/
		addStep("Ejecutar el programa de DS50 para el envío del archivo HEF.xml.");
		
		
		/***************************Paso 3**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_ENVELOPE de la BD FCWM6QA para validar la ejecución de la DS50 con el ID del Envelope, donde se envía el archivo HEF a WM.");
		String formatEjecucionDs50 = String.format(queryDs50Execution, data.get("EnvelopeID"));
		SQLResult ejecucionDs50 = executeQuery(dbLog, formatEjecucionDs50);
		System.out.println(formatEjecucionDs50);
		boolean validateEjecucionDs50 = ejecucionDs50.isEmpty();
		String posEnvelopeID = "";
		if(!validateEjecucionDs50) {
			posEnvelopeID = ejecucionDs50.getData(0, "ID");
			testCase.addQueryEvidenceCurrentStep(ejecucionDs50);
		}
		assertFalse(validateEjecucionDs50, "No se obtuvieron registros de ejecucion de DS50.");
		/***************************Paso 4**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_INBOUND_DOCS de la BD FCWM6QA para validar que el archivo HEF fue enviado a WM y cuente con el estatus=I (Insertado).");
		String formatEnvioHEF = String.format(queryHEFSending, data.get("FileName"), posEnvelopeID);
		SQLResult envioHEF = executeQuery(dbLog, formatEnvioHEF);
		System.out.println(formatEnvioHEF);
		boolean validateEnvioHEF = envioHEF.isEmpty();
		String posInboundDocsID = "";
		if(!validateEnvioHEF) {
			posInboundDocsID = envioHEF.getData(0, "ID");
			testCase.addQueryEvidenceCurrentStep(envioHEF);
		}
		assertFalse(validateEnvioHEF, "No se encuentra registro del archivo HEF.");
		/***************************Paso 5**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_HEF de la BD FCWM6QA, para validar el número de registros.");
		String formatRecordNumber = String.format(queryRecordNumber, posInboundDocsID);
		SQLResult recordNumber = executeQuery(dbLog, formatRecordNumber);
		System.out.println(formatRecordNumber);
		boolean validateRecordNumber = recordNumber.isEmpty();
		if(!validateRecordNumber) {
			testCase.addQueryEvidenceCurrentStep(recordNumber);
		}
		assertFalse(validateRecordNumber, "No se obtuvieron registros");
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
		return "MTC-FT-002-PR50_IN Enviar archivo HEF a traves de la DS50-TN-PR50 a WM";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
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
