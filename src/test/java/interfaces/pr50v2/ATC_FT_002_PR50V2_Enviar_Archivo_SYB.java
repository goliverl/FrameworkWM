package interfaces.pr50v2;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_002_PR50V2_Enviar_Archivo_SYB extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PR50V2_Enviar_Archivo_SYB_test (HashMap<String, String> data) throws Exception{
		
		/**
		 * Proyecto: RD Y BO Internacional (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-003-C1 PR50_IN Enviar archivo SYB a traves de la DS50-TN-PR50 a WM_MEX
		 * @author 
		 * @date 
		 */
		
		/*
		 * Utileria*************************************************/
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		/*
		 * Variables************************************************/
//Paso 3
		String querySYBSending = "SELECT ID,PE_ID,PV_DOC_ID,STATUS,DOC_TYPE,PV_DOC_NAME,RECEIVED_DATE \r\n"
				+ "FROM POSUSER.POS_INBOUND_DOCS \r\n"
				+ "WHERE DOC_TYPE='SYB'\r\n"
				+ "AND DOC_TYPE= 'SYB' "
				+ "AND PV_DOC_NAME LIKE '%"+data.get("FileName")+"%' AND RECEIVED_DATE = SYSDATE\r\n"
				+ "AND ROWNUM <= 10";
//Paso 4	
		String queryRecordNumber = "SELECT PID_ID,NO_RECORDS,PARTITION_DATE \r\n"
				+ "FROM POSUSER.POS_SYB \r\n"
				+ "WHERE PID_ID = '%s'";
		
		String queryFileInfo = "SELECT PID_ID,FECHA_TRANSACCION,HORA_TRANSACCION,VALOR,FOLIO_TRANSACCION "
				+ "FROM  POSUSER.POS_SYB_DETL "
				+ "WHERE PID_ID = '%s'";
		
		/***************************Paso 1**************************/
		addStep("Solicitar al equipo de Xpos realizar los movimientos o transacciones de Cobro de Servicios y la "
				+ "ejecución del proceso de Fin Día para generar el archivo SYB.XML.");
		
		
		/***************************Paso 2**************************/
		addStep("Ejecutar el programa de DS50 para el envío del archivo SYB.XML.");
		
		
		/***************************Paso 3**************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_INBOUND_DOCS de la BD FCWM6QA para validar "
				+ "que el archivo '<ARCHIVO.XML>' fue enviado a WM y cuente con el estatus=I (Insertado).");
		System.out.println(querySYBSending);
		
		SQLResult SYBSending = executeQuery(dbLog, querySYBSending);
		String posInboundDocsID = "";
		String posInboundDocsStatus = "";
		
		boolean validateSYBSending = SYBSending.isEmpty();
		if(!validateSYBSending) {
			posInboundDocsID = SYBSending.getData(0, "ID");
			posInboundDocsStatus = SYBSending.getData(0, "STATUS");
			testCase.addQueryEvidenceCurrentStep(SYBSending);
		}
		assertEquals(posInboundDocsStatus, "I");
		/***************************Paso 4**************************/
		
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_SYB de la BD FCWM6QA:");
		String formatRecordNumber = String.format(queryRecordNumber, posInboundDocsID);
		SQLResult recordNumber = executeQuery(dbLog, formatRecordNumber);
		System.out.println(formatRecordNumber);
		boolean validateRecordNumber = recordNumber.isEmpty();
		if(!validateRecordNumber) {
			testCase.addQueryEvidenceCurrentStep(recordNumber);
		}
		assertFalse(validateRecordNumber, "No se muestran registros.");
		/***************************Paso 5**************************/
		
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_SYB_DETL de la BD FCWM6QA "
				+ "para validar que el resultado de la consulta coincida con la información contenida en el archivo <ARCHIVO.XML> ");
		String formatFileInfo = String.format(queryFileInfo, posInboundDocsID);
		SQLResult fileInfo = executeQuery(dbLog, formatFileInfo);
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
		return "MTC-FT-003-PR50_IN Enviar archivo SYB a traves de la DS50-TN-PR50 a WM";
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
