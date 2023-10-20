package interfaces.pr50v2;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_006_PR50V2_Enviar_Archivo_ITM extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_006_PR50V2_Enviar_Archivo_ITM_test(HashMap<String, String> data) throws Exception{
		
		/**
		 * Proyecto: RD Y BO Internacional (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-007-PR50_OUT Enviar archivo ITM a traves de la DS50-TN-PR50 a XPOS
		 * @author 
		 * @date 
		 */
		
		/*
		 * Utileria*************************************************/
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		/*
		 * Variables************************************************/
		String queryStatusChanging = "SELECT * FROM("
				+ "SELECT ID, POE_ID,TARGET_TYPE,PARTITION_DATE,DOC_TYPE,PV_CR_PLAZA,PV_CR_TIENDA,STATUS \r\n"
				+ "FROM posuser.POS_OUTBOUND_DOCS\r\n"
				+ "WHERE DOC_TYPE = 'ITM' \r\n"
				+ "AND PV_CR_PLAZA = '%s' \r\n"
				+ "AND PV_CR_TIENDA = '%s' \r\n"
				+ "AND STATUS='P' \r\n"
				+ "ORDER BY PARTITION_DATE DESC"
				+ ")WHERE ROWNUM <= 10";
		
		String filePath = "u01/posuser/FEMSA_OXXO/POS/%s/%s/backup";
		
		/***************************Paso 1**************************/
		addStep("Llamar a <MTC-FT-012-C1 PR26 Generacion de archivo ITM de Item Master a traves de la interface FEMSA_PR26>");
		
		
		/***************************Paso 2**************************/
		addStep("Ejecutar la DS50(PR50) desde un punto de venta XPOS ");
		
		
		/***************************Paso 3**************************/
		addStep("Ejecutar la siguiente consulta en la DB FCWM6QA para validar el cambio de status de L a P");
		String formatStatusChanging = String.format(queryStatusChanging, data.get("Plaza"), data.get("Tienda"));
		SQLResult statusChanging = executeQuery(dbLog, formatStatusChanging);
		System.out.println(formatStatusChanging);
		boolean validateStatusChanging = statusChanging.isEmpty();
		if(!validateStatusChanging) {
			testCase.addQueryEvidenceCurrentStep(statusChanging);
		}
		assertFalse(validateStatusChanging, "No se obtuvieron registros con Status P");
		/***************************Paso 4**************************/
		addStep("Ingresar a la herramienta FileZilla mediante los siguientes datos:\r\n"
				+ "IP: 10.182.92.120\r\n"
				+ "User: <posuser>\r\n"
				+ "Pass: <posuser>");
		
		Thread.sleep(20000);

		FTPUtil ftp = new FTPUtil("10.182.92.120", 21, "posuser", "posuser");

		Thread.sleep(20000);
		String ruta = "/u01/posuser/FEMSA_OXXO/POS/" + data.get("plaza") + "/" + data.get("tienda") + "/backup";
		// host, puerto, usuario, contraseña
		/// u01/posuser
		
		/***************************Paso 5**************************/
		addStep("Validar que  el archivo generado se encuentre en el buzón de la tienda <TIENDA>");
		
		boolean validaFTP;

		if (ftp.fileExists(ruta)) {

			validaFTP = true;
			testCase.addFileEvidenceCurrentStep(ruta);
			System.out.println("Existe");
			testCase.addBoldTextEvidenceCurrentStep("El archivo si existe ");
			testCase.addBoldTextEvidenceCurrentStep(ruta);

		} else {
			testCase.addFileEvidenceCurrentStep(ruta);
			testCase.addBoldTextEvidenceCurrentStep("El archivo no existe ");
			System.out.println("No Existe");
			validaFTP = false;
		}

		assertTrue("No se encontro el archivo xml en POSUSER ", validaFTP);
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
		return "MTC-FT-007-PR50_OUT Enviar archivo ITM a traves de la DS50-TN-PR50 a XPOS";
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
