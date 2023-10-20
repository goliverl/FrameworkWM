package interfaces.PR50V2_PER;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/*
 * I20016 Actualizacion tecnologica Webmethods 10 -  Enviar archivo DGT a traves de la DS50-TN-PR50 a XPOS
 * 
 * Prueba de regresion  para comprobar la no afectacion 
 * en la funcionalidad principal de la interface FEMSA_PR50V2 para 
 * archivos DGT (Datos Generales de Tienda) de bajada  (OUTBOUND) 
 * al ser migrada de WM9.9 a WM10.5
 * 
 * @author Edwin Ramirez
 * @date 23/02/2023
 * 
 */

public class ATC_FT_005_PR50V2_PER_Enviar_Archivo_DGT extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_PR50V2_PER_Enviar_Archivo_DGT_test (HashMap<String, String> data) throws Exception{
		/*
		 * Utileria*************************************************/
		SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,GlobalVariables.DB_PASSWORD_Oiwmqa);
		

		/*
		 * Variables************************************************/
		String queryPendingFiles = "SELECT * \r\n"
				+ "FROM POSUSER.POS_OUTBOUND_DOCS \r\n"
				+ "WHERE PV_CR_PLAZA = '10LIM' \r\n"
				+ "AND PV_CR_TIENDA = '50POL'\r\n"
				+ "AND DOC_TYPE = 'DGT'\r\n"
				+ "AND STATUS = 'L' \r\n"
				+ "ORDER BY SENT_DATE DESC";
		
		String queryLocateFile = "SELECT * "
				+ "FROM POS_OUTBOUND_ENV "
				+ "WHERE FILE_NAME = '%s'";
		
		String queryFileStatus = "SELECT * \r\n"
				+ "FROM POSUSER.POS_OUTBOUND_DOCS \r\n"
				+ "WHERE PV_CR_PLAZA = '10LIM' \r\n"
				+ "AND PV_CR_TIENDA = '50POL' \r\n"
				+ "AND DOC_TYPE = 'DGT' \r\n"
				+ "AND STATUS = 'P'  \r\n"
				+ "AND POE_ID = '%s'\r\n"
				+ "AND DOC_NAME = '%'\r\n"
				+ "ORDER BY SENT_DATE DESC";
		
		String path = "/u01/posuser/FEMSA_OXXO/POS/10LIM/50POL/backup";
		
		testCase.setTest_Description(data.get("id")+ data.get("descripcion"));
		
		/***************************Paso 1**************************/
		addStep("Validar que existan documentos DGT pendientes de procesar para la Plaza y Tienda en la tabla pos_outbound_docs de POSUSER.");
		SQLResult pendingFiles = executeQuery(dbPos, queryPendingFiles);
		System.out.println(queryPendingFiles);
		boolean validatePendingFiles = pendingFiles.isEmpty();
		if(!validatePendingFiles) {
			testCase.addQueryEvidenceCurrentStep(pendingFiles);
		}
		assertFalse(validatePendingFiles, "No se obtuvieron registros");
		/***************************Paso 2**************************/
		addStep("Ejecutar la DS50 (PR50) desde un punto de venta POS_PERU");
		
		/***************************Paso 3**************************/
		addStep("Validar que después de ejecutar  la DS50 (PR50) desde un punto de venta POS_PERU el archivo "
				+ "DGT se encuentre en la carpeta BACKUP");
		
		/***************************Paso 4**************************/
		addStep("Ejecutar la siguiente consulta en la siguiente tabla POS_OUTBOUND_ENV en la DB FCWMQA-PERU  para localizar el archivo .ZIP");
		String formatLocateFile = String.format(queryLocateFile, data.get("FileName"));
		SQLResult locateFile = executeQuery(dbPos, formatLocateFile);
		System.out.println(formatLocateFile);
		boolean validateLocateFile = locateFile.isEmpty();
		String posOutBoundID = "";
		if(!validateLocateFile) {
			posOutBoundID = locateFile.getData(0, "POE_ID");
			testCase.addQueryEvidenceCurrentStep(locateFile);
		}
		assertFalse(validateLocateFile, "No se obtuvieron registros");
		/***************************Paso 5**************************/
		addStep("Ejecutar la siguiente consulta en la DB FCWMQA-PERU para validar el cambio de status de L a P:");
		String formatFileStatus = String.format(queryFileStatus, posOutBoundID, data.get("FileName"));
		SQLResult fileStatus = executeQuery(dbPos, formatFileStatus);
		System.out.println(formatFileStatus);
		boolean validateFileStatus = fileStatus.isEmpty();
		if(!validateFileStatus) {
			testCase.addQueryEvidenceCurrentStep(fileStatus);
		}
		assertFalse(validateFileStatus, "No se obtiveron registros");
		/***************************Paso 6**************************/
		addStep("Establecer la conexión a Filezilla con el servidor FTP de Buzones de tienda\r\n"
				+ "IP: 10.80.8.181\r\n"
				+ "User: posuser\r\n"
				+ "Pass: <PASS>");
		
		/***************************Paso 7**************************/
		addStep("Validar que  el archivo generado se encuentre en el buzón de la tienda 50MCZ");
		
		
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
