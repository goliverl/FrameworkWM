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
 * I20016 Actualizacion tecnologica Webmethods 10 - Enviar archivo ADJ a traves de la DS50-PR50 a WM
 * Prueba de regresion  para comprobar la no afectacion en la funcionalidad 
 * principal de la interface FEMSA_PR50V2 para 
 * archivos  ADJ (Ajuste de Inventario/merma) de subida 
 * (INBOUND) al ser migrada de WM9.9 a WM10.5
 * 
 * @author Edwin Ramirez
 * @date 23/02/2023
 * 
 */

public class ATC_FT_003_PR50V2_PER_Enviar_Archivo_ADJ extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_PR50V2_PER_Enviar_Archivo_ADJ_test (HashMap<String, String> data) throws Exception{
		/*
		 * Utileria*************************************************/
		SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,GlobalVariables.DB_PASSWORD_Oiwmqa);
		

		/*
		 * Variables************************************************/
		String queryDS50Execution = "SELECT * \r\n"
				+ "FROM POSUSER.POS_ENVELOPE \r\n"
				+ "WHERE RECEIVED_DATE >= TRUNC(SYSDATE) \r\n"
				+ "AND PV_ENVELOPE_ID='%s'";
		
		String queryFileStatus = "SELECT * \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ "WHERE  PV_DOC_NAME = '%s'";
		
		String queryRecordNumber = "SELECT * \r\n"
				+ "FROM  POSUSER.POS_ADJ \r\n"
				+ "WHERE PID_ID = '%s'";
		
		String queryFileInfo = "SELECT * \r\n"
				+ "FROM POSUSER.POS_ADJ_DETL \r\n"
				+ "WERE  PID_ID = '%s'";
		
		testCase.setTest_Description(data.get("id")+ data.get("descripcion"));
		
		/***************************Paso 1**************************/
		addStep("Solicitar al equipo de Pos realizar los movimientos o transacciones de Ajuste de "
				+ "Inventario/merma y solicitar la ejecución del proceso Fin Dia  para generar el archivo ADJ.TXT. ");
		
		/***************************Paso 2**************************/
		addStep("Ejecutar el programa de DS50 para el envío del archivo ADJ.TXT.");
		
		/***************************Paso 3**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_ENVELOPE de la BD OIWMQA.FEMCOM.NET para validar"
				+ " la ejecución de la DS50 con el ID del Envelope, donde se envía el archivo A a WM.");
		String formatDS50Execution = String.format(queryDS50Execution, data.get("EnvelopeID"));
		SQLResult ds50Execution = executeQuery(dbPos, formatDS50Execution);
		System.out.println(formatDS50Execution);
		boolean validateDS50Execution = ds50Execution.isEmpty();
		if(!validateDS50Execution) {
			testCase.addQueryEvidenceCurrentStep(ds50Execution);
		}
		assertFalse(validateDS50Execution, "No se obtuvieron registros.");
		/***************************Paso 4**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_INBOUND_DOCS de la BD FCMWQA_PERU para validar "
				+ "que el archivo <ARCHIVO.TXT > fue enviado a WM y cuente con el estatus=I (Insertado).");
		String formatFileStatus = String.format(queryFileStatus, data.get("FileName"));
		SQLResult fileStatus = executeQuery(dbPos, formatFileStatus);
		System.out.println(formatFileStatus);
		boolean validateFileStatus = fileStatus.isEmpty();
		String posInboundDocsID = ""; 
		if(!validateDS50Execution) {
			posInboundDocsID = fileStatus.getData(0, "PID_ID");
			testCase.addQueryEvidenceCurrentStep(fileStatus);
		}
		assertFalse(validateFileStatus, "No se obtuvieron registros");
		/***************************Paso 4**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_ADJ de la BD FCMWQA_PERU para validar  el número "
				+ "de registros.");
		String formatRecordNumber = String.format(queryRecordNumber, posInboundDocsID);
		SQLResult recordNumber = executeQuery(dbPos, formatRecordNumber);
		System.out.println(formatRecordNumber);
		boolean validateRecordNumber = recordNumber.isEmpty();
		if(!validateRecordNumber) {
			testCase.addQueryEvidenceCurrentStep(recordNumber);
		}
		assertFalse(validateRecordNumber, "No se obtuvieron registros");
		/***************************Paso 5**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_ADJ_DETL de la BD FCMWQA_PERU para validar la información contenida en el archivo <ARCHIVO.TXT> coincida don la información registrada en la tabla.");
		String formatFileInfo = String.format(queryFileInfo, posInboundDocsID);
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
