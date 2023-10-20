package interfaces.PR50V2_PER;

import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

/*
 * I20016 Actualizacion tecnologica Webmethods 10 - Enviar archivo HEF a traves de la DS50-TN-PR50 a WM
 * Prueba de regresion  para comprobar la no afectación en la funcionalidad principal de la interface 
 * FEMSA_PR50V2 para archivos de subida (INBOUND) al ser migrada de WM9.9 a WM10.5
 * 
 * @author Edwin Ramirez
 * @date 23/02/2023
 * 
 */

public class ATC_FT_001_PR50V2PER_EnviarArchivoHEF extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PR50V2PER_EnviarArchivoHEF_test(HashMap<String, String> data) throws Exception {
		/*
		 * Utileria
		 *************************************************/
		SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_Oiwmqa, GlobalVariables.DB_USER_Oiwmqa,GlobalVariables.DB_PASSWORD_Oiwmqa);

		/*
		 * Variables
		 ************************************************/
		String queryFileSent = "SELECT * \r\n" + "FROM POSUSER.POS_INBOUND_DOCS \r\n" + "WHERE PV_DOC_NAME = '%s'";

		String queryRecordsNumber = "SELECT * \r\n" + "FROM POSUSER.POS_HEF \r\n" + "WHERE PID_ID = '%s'";

		String queryRecordsNumber2 = "SELECT * \r\n" + "FROM POSUSER.POS_HEF_DETL \r\n" + "WHERE PID_ID = '%s' ";
		
		testCase.setTest_Description(data.get("id")+ data.get("descripcion"));

		/*************************** Paso 1 **************************/
		addStep("Solicitar al equipo de POS realizar los movimientos o transacciones de Hoja de entrega Final para generar el archivo HEF.");

		/*************************** Paso 2 **************************/
		addStep("Ejecutar el Proceso de Fin Día");

		/*************************** Paso 3 **************************/
		addStep("Ejecutar el programa de DS50 para el envío del archivos a WM.");

		/*************************** Paso 4 **************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_INBOUND_DOCS de la BD FCWMQA_PERU para validar "
				+ "que el archivo HEF fue enviado a WM y cuente con el estatus=I.");
		String formatFileSent = String.format(queryFileSent, data.get("FileName"));
		SQLResult fileSent = executeQuery(dbPos, formatFileSent);
		System.out.println(formatFileSent);
		boolean validateFileSent = fileSent.isEmpty();
		String posInboundDocID = "";
		if (!validateFileSent) {
			posInboundDocID = fileSent.getData(0, "PID_ID");
			testCase.addQueryEvidenceCurrentStep(fileSent);
		}
		assertFalse(validateFileSent, "No se obtuvieron registros");

		/*************************** Paso 5 **************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_HEF de la BD FCWMQA_PERU para validad el número "
				+ "de registros y el saldo total de los registros.");
		String formatRecordsNumber = String.format(queryRecordsNumber, posInboundDocID);
		SQLResult recordsNumber = executeQuery(dbPos, formatRecordsNumber);
		System.out.println(formatRecordsNumber);
		boolean validateRecordsNumber = recordsNumber.isEmpty();
		if (!validateFileSent) {
			testCase.addQueryEvidenceCurrentStep(recordsNumber);
		}
		assertFalse(validateRecordsNumber, "No se obtuvieron registros");
		/*************************** Paso 6 **************************/
		addStep("Ejecutar la siguiente consulta a la tabla POSUSER.POS_HEF de la BD FCWMQA_PERU para validad el número "
				+ "de registros y el saldo total de los registros.");
		String formatRecordsNumber2 = String.format(queryRecordsNumber2, posInboundDocID);
		SQLResult recordsNumber2 = executeQuery(dbPos, formatRecordsNumber2);
		System.out.println(formatRecordsNumber2);
		boolean validateRecordsNumber2 = recordsNumber2.isEmpty();
		if (!validateRecordsNumber2) {
			testCase.addQueryEvidenceCurrentStep(recordsNumber2);
		}
		assertFalse(validateRecordsNumber2, "No se obtuvieron registros");
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
		return "Enviar archivo HEF a traves de la DS50-TN-PR50 a WM";
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
