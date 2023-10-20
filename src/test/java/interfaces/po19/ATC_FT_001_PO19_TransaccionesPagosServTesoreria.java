package interfaces.po19;

import static org.junit.Assert.assertFalse;
import static org.testng.Assert.assertFalse;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class ATC_FT_001_PO19_TransaccionesPagosServTesoreria extends BaseExecution {
	public WebDriver webDriver;
	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PO19_TransaccionesPagosServTesoreria_test(HashMap<String, String> data) throws Exception {
		// *********************************************************Utilerías***************************************************************************************

		
		utils.sql.SQLUtil dbEbs = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_AVEBQA ,GlobalVariables.DB_USER_AVEBQA, GlobalVariables.DB_PASSWORD_AVEBQA);
		utils.sql.SQLUtil dbTPE = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE, GlobalVariables.DB_USER_FCTPE,GlobalVariables.DB_PASSWORD_FCTPE);
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA,GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbUpdate = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE_UPD, GlobalVariables.DB_USER_FCTPE_UPD, GlobalVariables.DB_PASSWORD_FCTPE_UPD);
		
		
	/**
	 * ALM
	 * Verificar ejecucion de la interfaz con la plaza 
	 */

		// **********************************************************
		// Variables**************************************************************************************
		// Paso 1

		
		String queryUpd="Update TPEUSER.TPE_SERVICE_TRAN "
				+ " set LV_TPE_STATUS='I'" + 
				" Where LV_REF1='2200000000010V61QU1M'"+ 
				" AND LV_ENTITY = 'GDF'" + 
				" AND LV_PLAZA='10MON'"
				+ " AND LN_FOLIO_TRANSACCION = 2018325017";
		
		String tdcQueryIntegrationServer = "select * from ( SELECT run_id,start_dt,status" 
				+ " FROM WMLOG.wm_log_run"
				+ " WHERE interface = 'PO19'" 
				+ " and  start_dt >= TRUNC(SYSDATE)"
				+ " order by start_dt desc)" + " where rownum = 1";
		
		String consulta5 = "SELECT RUN_ID,INTERFACE,START_DT,STATUS,SERVER "
				+ " FROM WMLOG.WM_LOG_RUN "
				+ " WHERE RUN_ID =%s";

		String tdcQueryErrorId = " SELECT ERROR_ID,RUN_ID,ERROR_DATE,DESCRIPTION " 
				+ " FROM WMLOG.WM_LOG_ERROR "
				+ " where RUN_ID=%s"; // FCWMLQA
		
		String ValidarInsumo_1 = "select * from (SELECT LV_ATRIBUTO3,LV_ENTITY,LV_PLAZA,LV_TIENDA,LN_FOLIO_TRANSACCION,LV_REF1,to_char(LD_FECHA_TRANSACCION,'DD/MON/RRRR') "
				+ "FROM TPEUSER.TPE_SERVICE_TRAN "
				+ "WHERE LV_TPE_STATUS = 'I' "
				+ "AND LV_ENTITY = 'GDF' "
				+ "AND LV_PLAZA='"
				+ data.get("plaza") + "')where rownum <=1";// wmview

		String inf_PAGO_SERVICIOS1 = "select * from (SELECT trans_id, plaza, tienda, folio_transaccion, servicio, consecutivo, FECHA_RECEPCION" + 
				" FROM XXFC.XXFC_PAGO_SERVICIOS_PRE" + 
				" WHERE PLAZA ='10MON'" + 
				" AND TRUNC(FECHA_RECEPCION) = TRUNC(SYSDATE)" + 
				" order by FECHA_RECEPCION desc) where rownum = 1";
		

		String ValidarInsumoProcesado_1 = "SELECT LV_APPLICATION,LV_ENTITY,LV_PLAZA,LV_TIENDA,LN_FOLIO_TRANSACCION,LV_TPE_STATUS" + 
				" FROM TPEUSER.TPE_SERVICE_TRAN" + 
				" WHERE  LV_ENTITY = 'GDF'" + 
				" AND LV_TPE_STATUS = 'E'" + 
				" AND LV_PLAZA='10MON'" + 
				" AND LN_FOLIO_TRANSACCION = 2018325017";// wmview

		
		// *****************************************************************Paso
		// 1**************************************************************************************

		addStep("Validar datos a procesar en la tabla TPE_SERVICE_TRAN.");
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/YYYY");
		
	    Calendar cal = Calendar.getInstance();
		String actualDate = formatter.format(cal.getTime());

		String updDatesFormat = String.format(queryUpd, actualDate,actualDate,actualDate);
		System.out.println(updDatesFormat);
		dbUpdate.executeUpdate(updDatesFormat);


		// Primera parte

		System.out.println(ValidarInsumo_1);

		SQLResult ValidaInsumoResult_1 = dbTPE.executeQuery(ValidarInsumo_1);

		String LV_REF1 = ValidaInsumoResult_1.getData(0, "LV_REF1");

		String LD_FECHA_TRANSACCION = ValidaInsumoResult_1.getData(0, "to_char(LD_FECHA_TRANSACCION,'DD/MON/RRRR')");
		
		String LV_ATRIBUTO3 = ValidaInsumoResult_1.getData(0, "LV_ATRIBUTO3");
		
		boolean ValidaInsumoBoolean_1 = ValidaInsumoResult_1.isEmpty();

		if (!ValidaInsumoBoolean_1) {

			testCase.addQueryEvidenceCurrentStep(ValidaInsumoResult_1);

		}

	
		assertFalse("No hay registros por procesar en la tabla TPE_SERVICE_TRAN ", ValidaInsumoBoolean_1);

		// ********************************************************Paso
		// 2*************************** *******************************************

		addStep(" Ejecutar el servicio PO19.Pub:run.");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
		PakageManagment pok = new PakageManagment(u, testCase);
		/*
		 * Variables
		 *********************************************************************/
	   
	    String status = "S";
	    String searchedStatus = "R";
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);	
		
		pok.runIntefaceWmOneButton10(data.get("interfase"), data.get("servicio"));
		
		SQLResult result5 = executeQuery(dbLog, tdcQueryIntegrationServer);
		String status1 = result5.getData(0, "STATUS");
		String run_id = result5.getData(0, "RUN_ID");
		
		boolean valuesStatus = status1.equals(searchedStatus);// Valida si se encuentra en estatus R
		while (valuesStatus) {
			result5 = executeQuery(dbLog, tdcQueryIntegrationServer);
			status1 = result5.getData(0, "STATUS");
			run_id = result5.getData(0, "RUN_ID");
			
	
			Thread.sleep(2);
	
		}
		boolean successRun = status1.equals(status);// Valida si se encuentra en estatus S
		if (!successRun) {
	
			String error = String.format(tdcQueryErrorId, run_id);
			SQLResult result3 = executeQuery(dbLog, error);
	
			boolean emptyError = result3.isEmpty();
			
	
			if (!emptyError) {
	
				testCase.addTextEvidenceCurrentStep(
						"Se encontró un error en la ejecución de la interfaz en la tabla WM_LOG_ERROR");
	
				testCase.addQueryEvidenceCurrentStep(result3);
	
			}
		}else {
			testCase.addTextEvidenceCurrentStep("El servicio PO19.Pub:run. fue ejecutado exitosamente");
		}
		
		// *************************************Paso 3*******************************************

		 addStep("Verificar el estatus con el cual fue terminada la ejecucion de la interface en la tabla WM_LOG_RUN del usuario WMLOG.");
			
			String verificacionInterface = String.format(consulta5, run_id);
			SQLResult paso4 = executeQuery(dbLog, verificacionInterface);
			System.out.println(verificacionInterface);

			boolean av5 = paso4.isEmpty();
			
			if (!av5) {

				testCase.addQueryEvidenceCurrentStep(paso4);
				
			} 
	
			assertFalse(av5, "No se obtiene informacion de la consulta");

		// ****************************************Paso 4***********************************************
	
		addStep("Verificar la informacion procesada en la tabla XXFC_PAGO_SERVICIOS de Finanzas.");

		// Parte 1
		String inf_PAGO_SERVICIOS1_FECHA = String.format(inf_PAGO_SERVICIOS1, LD_FECHA_TRANSACCION);

		SQLResult inf_PAGO_SERVICIOS1_FECHA_R = dbEbs.executeQuery(inf_PAGO_SERVICIOS1_FECHA);

		boolean inf_PAGO_SERVICIOS1_b = inf_PAGO_SERVICIOS1_FECHA_R.isEmpty();

		if (!inf_PAGO_SERVICIOS1_b) {

			testCase.addQueryEvidenceCurrentStep(inf_PAGO_SERVICIOS1_FECHA_R);

		}

		assertFalse("Error en el envio de datos de transacciones de pagos de servicios", inf_PAGO_SERVICIOS1_b);

		// ************************************************** Paso 5
		// *********************************************************************************************

		addStep("Verificar que se actualizaron los estatus a enviado \"E\" en la tabla TPE_SERVICE_TRAN");
		// Primera parte

		Thread.sleep(10000);
		String ConsultaValidarInsumoProcesado_1 = String.format(ValidarInsumoProcesado_1, LV_REF1, LV_ATRIBUTO3);

		SQLResult ConsultaValidarInsumoProcesado_1_R = dbTPE.executeQuery(ConsultaValidarInsumoProcesado_1);

		boolean ConsultaValidarInsumoProcesado_1_b = ConsultaValidarInsumoProcesado_1_R.isEmpty();

		if (!ConsultaValidarInsumoProcesado_1_b) {

			testCase.addQueryEvidenceCurrentStep(ConsultaValidarInsumoProcesado_1_R);

		}

		
		assertFalse("No se actualizo el estatus a enviado", ConsultaValidarInsumoProcesado_1_b);
		

	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub

	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Validar la operacion para envio de datos de transacciones de tipo GDF a la DB de Finanzas.";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Equipo de Automatizacion";
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

	@Override
	public String setPrerequisites() {
		// TODO Auto-generated method stub
		return null;	
	}
	

}
