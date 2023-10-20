package interfaces.pr50v2;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import util.GlobalVariables;
import utils.FTPUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class ATC_FT_005_PR50V2_Enviar_Archivo_DGT extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_PR50V2_Enviar_Archivo_DGT_test (HashMap<String, String> data) throws Exception{
		
		/**
		 * Proyecto: RD Y BO Internacional (Regresion Enero 2023)
		 * Caso de prueba: MTC-FT-006-PR50_OUT Enviar archivo DGT a traves de la DS50-TN-PR50 a XPOS
		 * @author 
		 * @date 
		 */
		
		/*
		 * Utileria*************************************************/
		SQLUtil dbPos = new SQLUtil(GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMQA_NUEVA, GlobalVariables.DB_USER_FCWMQA_NUEVA,GlobalVariables.DB_PASSWORD_FCWMLQA);
		
		/*
		 * Variables************************************************/
		String queryPendingDocs = "SELECT ID,POE_ID,TARGET_TYPE,DOC_NAME,DOC_TYPE,SENT_DATE,STATUS  \r\n"
				+ "FROM POSUSER.POS_OUTBOUND_DOCS\r\n"
				+ "WHERE PV_CR_PLAZA = '"+data.get("Plaza")+"'\r\n"
				+ "AND PV_CR_TIENDA = '"+data.get("Tienda")+"'\r\n"
				+ "AND DOC_TYPE = 'DGT'\r\n"
				+ "AND STATUS = 'L' \r\n"
				+ "ORDER BY SENT_DATE DESC";
		
		String queryStatusChanging = "SELECT ID,POE_ID,DOC_NAME,DOC_TYPE,PV_CR_PLAZA,PV_CR_TIENDA,SENT_DATE,STATUS  \r\n"
				+ "FROM POSUSER.POS_OUTBOUND_DOCS\r\n"
				+ "WHERE PV_CR_PLAZA = '"+data.get("Plaza")+"'\r\n"
				+ "AND PV_CR_TIENDA = '"+data.get("Tienda")+"'\r\n"
				+ "AND DOC_TYPE = 'DGT' "
				+ "AND DOC_NAME= '%s' \r\n"
				+ "ORDER BY SENT_DATE DESC";
		
		String filePath = "/u01/posuser/FEMSA_OXXO/POS/%s/%s/backup";
		
		/***************************Paso 1**************************/
		addStep("Llamar a <MTC-FT-011-C1 CP01 Generacion de archivo DGT a traves de la interface FEMSA_CP01>");
		
		
		/***************************Paso 2**************************/
		addStep("Validar que existan documentos pendientes de procesar para la Plaza y Tienda en la tabla POSUSER.POS_OUTBOUND_DOCS de POSUSER.");
		SQLResult pendingDocs = executeQuery(dbLog, queryPendingDocs);
		System.out.println(queryPendingDocs);
		boolean validatePendingDocs = pendingDocs.isEmpty();
		if(!validatePendingDocs) {
			testCase.addQueryEvidenceCurrentStep(pendingDocs);
		}
		assertFalse(validatePendingDocs, "No se encuentran archivos por procesar");
		/***************************Paso 3**************************/
		addStep("Ejecutar la DS50(PR50) desde un punto de venta XPOS ");
		
		
		/***************************Paso 4**************************/
		addStep("Validar que después de ejecutar  la DS50(PR50) desde un punto de venta XPOS el archivo DGT se encuentre en la carpeta Backup ");
		
		
		/***************************Paso 5**************************/
		addStep("Ejecutar la siguiente consulta en la DB FCWM6QA para validar el cambio de status de L a P:");
		String formatStatusChanging = String.format(queryStatusChanging, data.get("fileName"));
		SQLResult statusChanging = executeQuery(dbLog, formatStatusChanging);
		System.out.println(formatStatusChanging);
		boolean validateStatusChanging = statusChanging.isEmpty();
		String status = "";
		if(!validateStatusChanging) {
			status = statusChanging.getData(0, "STATUS");
			
			testCase.addQueryEvidenceCurrentStep(statusChanging);
		}
		assertEquals(status, "P");
		/***************************Paso 6**************************/
		
		addStep("Establecer la conexión a Filezilla con el servidor FTP de Buzones de tienda\r\n"
				+ "IP: 10.80.8.184\r\n"
				+ "User: posuser\r\n"
				+ "Pass: <Password>");

		Thread.sleep(20000);

		FTPUtil ftp = new FTPUtil("10.80.8.184", 21, "posuser", "posuser");

		Thread.sleep(20000);
		String ruta = "/u01/posuser/FEMSA_OXXO/POS/" + data.get("plaza") + "/" + data.get("tienda") + "/backup";
		// host, puerto, usuario, contraseña
		/// u01/posuser
		
		/***************************Paso 7**************************/
		addStep("Validar que  el archivo generado se encuentre en el buzón de la tienda");
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
		return "MTC-FT-006-PR50_OUT Enviar archivo DGT a través de la DS50-TN-PR50 a XPOS";
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
