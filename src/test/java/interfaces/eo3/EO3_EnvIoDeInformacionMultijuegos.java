package interfaces.eo3;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class EO3_EnvIoDeInformacionMultijuegos extends BaseExecution {
	@Test(dataProvider = "data-provider")
	public void test(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerias
		 ********************************************************************************************************************************************/
		// Tabla ORAFIN/EBS
		SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA, GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		SQLUtil dbLog = new SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA,GlobalVariables.DB_PASSWORD_FCWMLQA);

		/**
		 * ALM
		 * Validar el envio de la informacion de multijuegos de la plaza 10AGC a finanzas
		 * NOTA:El ultimo paso quedo fuera de alcance
		 */
		
		/*
		 * Variables
		 ******************************************************************************************************************************************/
		// Paso 1
		String ValidInfo = "SELECT d.* FROM WMUSER.wm_televisa_inbound_docs d    "
				+ "INNER JOIN WMUSER.wm_televisa_mul m ON (d.ID = m.ti_id)    "
				+ "INNER JOIN (SELECT DISTINCT ID, SUBSTR(cr_tienda, '0', '5')   "
				+ "FROM WMUSER.wm_televisa_mul_detl   " + "WHERE SUBSTR(cr_tienda, '0', '5') = '" + data.get("tienda")
				+ "' md ON (m.id = md.ID)   " + "WHERE status = 'L'";

		// Paso 3

		String ValidLog = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER " + "FROM WMLOG.WM_LOG_RUN "
				+ "WHERE INTERFACE = 'EO3'  " + "AND START_DT >= TRUNC(SYSDATE) "
				+ "AND STATUS = 'S'  ORDER BY START_DT DESC";

		// Paso 4
		String ValidInsrtMultTel = "SELECT id_registro, id_multijuegos, cr_plaza, cr_tienda,creation_date,estatus "
				+ " FROM xxfc.xxfc_multijuegos_televisa " + "WHERE id_multijuegos='%s' " + "AND cr_plaza = '"
				+ data.get("tienda") + "'	" + "AND TRUNC(creation_date) = TRUNC(SYSDATE)  " + "AND estatus = 'R'";
		// Paso 5
		String validUpdtTelInb = "SELECT id,status,doc_type,doc_name,run_id_send,sent_date "
				+ "FROM WMUSER.wm_televisa_inbound_docs  	" + "WHERE id ='%s'  " + "AND run_id_send = '%s'  	"
				+ "AND sent_date >=TRUNC(SYSDATE)" + "  AND status = 'E'";

		String tdcIntegrationServerFormat = "select * from (SELECT Tbl.run_id,interface, start_dt, end_dt, status, server, (END_DT - START_DT)*24*60 "
				+ " FROM WMLOG.WM_LOG_RUN Tbl " + "WHERE INTERFACE='EO3' "
				+ " ORDER BY START_DT DESC) where rownum <=1 ";

// *****************************************PASO 1**************************************************************************		

		addStep("Validar que exista informaci�n pendiente de procesar para la plaza.");
		String ID = "";
		System.out.println(GlobalVariables.DB_HOST_Ebs);
		System.out.println(ValidInfo);
		SQLResult ExecValidInfo = dbEbs.executeQuery(ValidInfo);

		boolean ValidInfoRes = ExecValidInfo.isEmpty();

		if (!ValidInfoRes) {
			ID = ExecValidInfo.getData(0, "ID");
			System.out.println("ID: " + ID);
			testCase.addQueryEvidenceCurrentStep(ExecValidInfo);
		}

		System.out.println(ValidInfoRes);

		assertFalse("No se encontraron registros a procesar ", ValidInfoRes);

		// *******************************PASO 2********************************************************************************

		addStep("Ejecutar el servicio EO3.Pub:runTransform. El servicio ser� invocado por el job execEO3Transform.");

		// Utileria

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String searchedStatus = "R";

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		String dateExecution = pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
		System.out.println("Respuesta dateExecution" + dateExecution);

		SQLResult is = dbLog.executeQuery(tdcIntegrationServerFormat);

		String status1 = is.getData(0, "STATUS");// guarda el run id de la
													// ejecuci�n

		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se
																// encuentra en
																// estatus R

		while (valuesStatus) {

			status1 = is.getData(0, "STATUS");
			valuesStatus = status1.equals(searchedStatus);

			u.hardWait(3);
		}

		// *******************************PASO 3********************************************************************************

		addStep("Verificar que la interfaz termino con exito en WMLOG. ");

		String RUN_ID = "";
		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(ValidLog);
		SQLResult ExecLog = dbLog.executeQuery(ValidLog);

		boolean LogRequest = ExecLog.isEmpty();

		if (!LogRequest) {
			RUN_ID = ExecLog.getData(0, "RUN_ID");
			System.out.println("RUN_ID: " + RUN_ID);
			testCase.addQueryEvidenceCurrentStep(ExecLog);
		}

		System.out.println(LogRequest);
		assertFalse(LogRequest, "No se muestra  la informacion.");

//		********************************Paso 4 ******************************************************************************

		addStep(" Validar que los datos fueron insertados en la tabla xxfc_multijuegos_televisa.");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		String ValidInsrtMultTelFormat = String.format(ValidInsrtMultTel, ID);
		System.out.println(ValidInsrtMultTelFormat);

		SQLResult ValidInsrtMultTelExec = dbEbs.executeQuery(ValidInsrtMultTelFormat);

		boolean ValidInsrtMultTelRes = ValidInsrtMultTelExec.isEmpty();

		if (!ValidInsrtMultTelRes) {

			testCase.addQueryEvidenceCurrentStep(ValidInsrtMultTelExec);
		}

		System.out.println(ValidInsrtMultTelRes);

		assertFalse("No hay informacion insertada ", ValidInsrtMultTelRes);

		// ******************************************** Paso 5******************************************************************

		addStep("Validar la actualizacion de la tabla wm_televisa_inbound_docs, el campo status debe contener el valor 'E'.");

		System.out.println(GlobalVariables.DB_HOST_Ebs);
		String validUpdtTelInbFormat = String.format(validUpdtTelInb, ID, RUN_ID);
		System.out.println(validUpdtTelInbFormat);

		SQLResult validUpdtTelInbExec = dbEbs.executeQuery(validUpdtTelInbFormat);

		boolean validUpdtTelInbRes = validUpdtTelInbExec.isEmpty();

		if (!validUpdtTelInbRes) {

			testCase.addQueryEvidenceCurrentStep(validUpdtTelInbExec);
		}

		System.out.println(validUpdtTelInbRes);

		assertFalse("No se encontraron registros actualizados", validUpdtTelInbRes);

		// ************************************** Paso 6
		// ************************************************************

		// Validar que los usuarios registrados con el rol ORAFIN reciban un correo con
		// el reporte de los archivos procesados
		// Este paso no se puede realizar

	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "EO3_EnvIoDeInformacionMultijuegos";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATIZACION";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
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
}
