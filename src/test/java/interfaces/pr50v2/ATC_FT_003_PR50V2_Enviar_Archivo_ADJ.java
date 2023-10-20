package interfaces.pr50v2;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_003_PR50V2_Enviar_Archivo_ADJ extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PR50V2_Enviar_Archivo_ADJ_test (HashMap<String, String> data) throws Exception{
		
		/**
		 * Proyecto: RD Y BO Internacional (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-004-PR50_IN Enviar archivo ADJ a través de la DS50-PR50 a WM
		 * @author 
		 * @date 
		 */
		
		
		/*
		 * Utileria*************************************************/
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		/*
		 * Variables************************************************/
		String queryDS50Execution = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,PV_ENVELOPE_ID,RECEIVED_DATE  \r\n"
				+ "FROM POSUSER.POS_ENVELOPE \r\n"
				+ "WHERE PV_CR_PLAZA='10MON' \r\n"
				+ "AND PV_CR_TIENDA ='50MCZ' \r\n"
				+ "AND RECEIVED_DATE >= TRUNC(SYSDATE) \r\n"
				+ "AND PV_ENVELOPE_ID = '%s' ";
		
		String queryADJSending = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,BACKUP_DATE \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ "WHERE DOC_TYPE='ADJ' \r\n"
				+ "AND RECEIVED_DATE >= TRUNC(SYSDATE) \r\n"
				+ "AND PV_DOC_NAME LIKE '%%s%' \r\n"
				+ "ORDER BY RECEIVED_DATE DESC";
		
		String queryADJFile = "SELECT PID_ID, NO_RECORDS,ADJ_DATE,PARTITION_DATE \r\n"
				+ "FROM  POSUSER.POS_ADJ \r\n"
				+ "WHERE PID_ID = '%s'";
		
		String queryADJContent = "SELECT PID_ID,ITEM,ADJ_QTY,REASON,PARTITION_DATE "
				+ "FROM POSUSER.POS_ADJ_DETL "
				+ "WHERE  PID_ID = '%s'";
		
		/***************************Paso 1**************************/
		addStep("Solicitar al equipo de Xpos realizar los movimientos o \r\n"
				+ "transacciones de Ajuste de Inventario/merma y solicitar la ejecución del proceso Fin Dia  para generar el archivo ADJ.TXT. ");
		
		
		/***************************Paso 2**************************/
		addStep("Ejecutar el programa de DS50 para el envío del archivo ADJ.TXT.");
		
		/***************************Paso 3**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_ENVELOPE de la BD FCWM6QA para validar la ejecución de "
				+ "la DS50 con el ID del Envelope, donde se envía el archivo ADJ a WM.");
		String formatDS50Execution = String.format(queryDS50Execution, data.get("EnvelopeID"));
		SQLResult ds50Execution = executeQuery(dbLog, queryDS50Execution);
		System.out.println(formatDS50Execution);
		boolean validateds50Execution = ds50Execution.isEmpty();
		if(!validateds50Execution) {
			testCase.addQueryEvidenceCurrentStep(ds50Execution);
		}
		assertFalse(validateds50Execution, "No se obtuvieron registros");
		/***************************Paso 4**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_INBOUND_DOCS de la BD FCWM6QA para validar que el "
				+ "archivo <ARCHIVO.TXT > fue enviado a WM y cuente con el estatus=I (Insertado).");
		String formatADJSending = String.format(queryADJSending, data.get("FileName"));
		SQLResult adjSending = executeQuery(dbLog, formatADJSending);
		System.out.println(formatADJSending);
		boolean validateADJSending = adjSending.isEmpty();
		String posInboundDocsID = "";
		String posInboundDocsStatus = "";
		if(!validateADJSending) {
			posInboundDocsID = adjSending.getData(0, "ID");
			posInboundDocsStatus = adjSending.getData(0, "STATUS");
			testCase.addQueryEvidenceCurrentStep(adjSending);
		}
		assertEquals(posInboundDocsStatus, "I");
		/***************************Paso 5**************************/
		addStep(" Ejecutar la siguiente consulta a la tabla POSUSER.POS_ADJ de la BD FCWM6QA.");
		String formatADJFile = String.format(queryADJFile, posInboundDocsID);
		SQLResult adjFile = executeQuery(dbLog, formatADJFile);
		System.out.println(formatADJFile);
		boolean validateADJfile = adjFile.isEmpty();
		if(!validateADJfile) {
			testCase.addQueryEvidenceCurrentStep(adjFile);
		}
		assertFalse(validateADJfile, "No se obtuvieron registros");
		/***************************Paso 6**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_ADJ_DETL de la BD FCWM6QA para validar la "
				+ "información contenida en el archivo <ARCHIVO.TXT>.");
		String formatADJContent = String.format(queryADJContent, posInboundDocsID);
		SQLResult ADJContent = executeQuery(dbLog, formatADJContent);
		System.out.println(formatADJContent);
		boolean validateADJContent = ADJContent.isEmpty();
		if(!validateADJContent) {
			testCase.addQueryEvidenceCurrentStep(ADJContent);
		}
		assertFalse(validateADJContent, "No se obtuvo informacion de archivo ADJ.");
		
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
		return "MTC-FT-004-PR50_IN Enviar archivo ADJ a través de la DS50-PR50 a WM";
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
