package interfaces.pr50v2;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_004_PR50V2_Enviar_Archivo_RTV extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_PR50V2_Enviar_Archivo_RTV_test (HashMap<String, String> data) throws Exception{
		
		/**
		 * Proyecto: RD Y BO Internacional (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-005-C1 PR50_IN Enviar archivo RTV a traves de la DS50-TN-PR50 a WM_MEX
		 * @author 
		 * @date 
		 */
		
		/*
		 * Utileria*************************************************/
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		/*
		 * Variables************************************************/
		String queryDS50Execution = "SELECT ID,PV_CR_PLAZA,PV_CR_TIENDA,PV_ENVELOPE_ID,RECEIVED_DATE,PARTITION_DATE \r\n"
				+ "FROM POSUSER.POS_ENVELOPE \r\n"
				+ "WHERE PV_CR_PLAZA='10MON' \r\n"
				+ "AND PV_CR_TIENDA ='50MCZ' \r\n"
				+ "AND RECEIVED_DATE >= TRUNC(SYSDATE) \r\n"
				+ "AND PV_ENVELOPE_ID= '%s'";
		
		String queryRTVSending = "SELECT ID, PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,BACKUP_DATE \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ "WHERE RECEIVED_DATE >= TRUNC(SYSDATE) \r\n"
				+ "AND PE_ID ='%s' \r\n"
				+ "AND DOC_TYPE='RTV' \r\n"
				+ "ORDER BY RECEIVED_DATE DESC";
		
		String queryRecordNumber = "SELECT PID_ID,NO_RECORDS,CREATED_DATE,PV_CR_FROM_LOC,EXT_REF_NO,SUPPLIER  \r\n"
				+ "FROM  POSUSER.POS_RTV \r\n"
				+ "WHERE PID_ID = '%s'";
		
		String queryRTVFile = "SELECT * \r\n"
				+ "FROM POSUSER.POS_RTV_DETL \r\n"
				+ "WHERE  PID_ID = '%s'";
		
		/***************************Paso 1**************************/
		addStep("Solicitar al equipo de Xpos realizar los movimientos o transacciones de Devoluciones de Compras "
				+ "a Proveedores y solicitar la ejecución del proceso Fin Dia para generar el archivo RTV.TXT.");
		
		
		/***************************Paso 2**************************/
		addStep("Ejecutar el programa de DS50 para el envío del archivo RTV.TXT.");
		
		
		/***************************Paso 3**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_ENVELOPE de la BD FCWM6QA para validar la ejecución "
				+ "de la DS50 con el ID del Envelope, donde se envía el archivo RTV a WM.");
		String formatDS50Execution = String.format(queryDS50Execution, data.get("EnvelopeID"));
		SQLResult ds50Execution = executeQuery(dbLog, formatDS50Execution);
		System.out.println(formatDS50Execution);
		boolean validateds50Execution = ds50Execution.isEmpty();
		String posEnvelopeID = "";
		if(!validateds50Execution) {
			posEnvelopeID = ds50Execution.getData(0, "PE_ID");
			testCase.addQueryEvidenceCurrentStep(ds50Execution);
		}
		assertFalse(validateds50Execution, "No se obtuvieron registros");
		/***************************Paso 4**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_INBOUND_DOCS de la BD FCWM6QA para validar que "
				+ "el archivo <ARCHIVO.TXT > fue enviado a WM y cuente con el estatus=I (Insertado).");
		String formatRTVSending = String.format(queryRTVSending, posEnvelopeID);
		SQLResult rtvSending = executeQuery(dbLog, formatRTVSending);
		System.out.println(formatRTVSending);
		boolean validateRTVSending = rtvSending.isEmpty();
		String posInboundID = "";
		String posInboundStatus = "";
		if(!validateRTVSending) {
			posInboundID = rtvSending.getData(0, "ID");
			posInboundStatus = rtvSending.getData(0, "STATUS");
			testCase.addQueryEvidenceCurrentStep(rtvSending);
		}
		assertEquals(posInboundStatus, "I");
		/***************************Paso 5**************************/
		addStep(" Ejecutar la siguiente consulta a la tabla POSUSER.POS_RTV de la BD FCWM6QA para validar  el número de registros.");
		String formatRecordNumber = String.format(queryRecordNumber, posInboundID);
		SQLResult recordNumber = executeQuery(dbLog, formatRecordNumber);
		System.out.println(formatRecordNumber);
		boolean validateRecordNumber = recordNumber.isEmpty();
		if(!validateRecordNumber) {
			testCase.addQueryEvidenceCurrentStep(recordNumber);
		}
		assertFalse(validateRecordNumber, "No se obtuvieron registros");
		/***************************Paso 6**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_RTV_DETL de la BD FCWM6QA");
		String formatRTVFile = String.format(queryRTVFile, posInboundID);
		SQLResult rtvFile = executeQuery(dbLog, formatRTVFile);
		System.out.println(formatRTVFile);
		boolean validateRTVFile = rtvFile.isEmpty();
		if(!validateRTVFile) {
			testCase.addQueryEvidenceCurrentStep(rtvFile);
		}
		assertFalse(validateRTVFile, "No se obtuvieron registros");
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
		return "MTC-FT-005-PR50_IN Enviar archivo RTV a través de la DS50-TN-PR50 a WM";
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
