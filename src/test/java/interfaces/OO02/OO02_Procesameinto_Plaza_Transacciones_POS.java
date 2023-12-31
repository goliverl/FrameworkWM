package interfaces.OO02;

import static org.junit.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import integrationServer.om.PakageManagment;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;

public class OO02_Procesameinto_Plaza_Transacciones_POS extends BaseExecution {	

	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_OO02_OO02_Procesameinto_Plaza_Transacciones_POS(HashMap<String, String> data) throws Exception {

/** UTILERIA *********************************************************************/

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbFic = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCIASQA, GlobalVariables.DB_USER_FCIASQA, GlobalVariables.DB_PASSWORD_FCIASQA);
		utils.sql.SQLUtil dbFceqa = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMREQA, GlobalVariables.DB_USER_FCWMREQA, GlobalVariables.DB_PASSWORD_FCWMREQA);

		SeleniumUtil u;
		PakageManagment pok;
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");


/** VARIABLES *********************************************************************/

		
		String tdcQry1 = " SELECT item, folio_wm, cr_plaza, cr_tienda,to_char(fecha_transaccion,'DD.MM.YYYY HH24:MI:SS'), \r\n"
				+ " num_autorizacion_prov,wm_code,wm_descripcion,wm_accion,estatus,created_by,last_update_date,last_updated_by \r\n"
				+ " FROM xxfc.xxfc_servicios_linea \r\n"
				+ " WHERE estatus = 'N' \r\n"
				+ " and cr_plaza = '"+data.get("plaza")+"' \r\n";
		
		String tdcQry2 = " SELECT item, item_desc, nacional_local, tipo_de_servicio, clave_integracion \r\n"
				+ " FROM tperep.catalogo_servicios \r\n"
				+ " WHERE item = %s \r\n";
				
		String tdcQry3 = " SELECT ENTITY WM_CODE FROM tperep.gif_transaction WHERE folio = %s \r\n"
				+ " AND creation_date >=To_date(%s,'DD.MM.YYYY HH24:MI:SS')-1 \r\n"
				+ " AND creation_date <To_date([%s,'DD.MM.YYYY HH24:MI:SS')+1 \r\n"
				+ " AND plaza = "+data.get("plaza")+"' \r\n"
				+ " AND tienda = '%s' \r\n";
						
		
		String tdcQry5 = " SELECT * FROM wm_log_run \r\n"
				+ " WHERE interface='OO02'\r\n"
				+ " AND start_dt>=trunc(sysdate) \r\n"
				+ " ORDER BY start_dt DESC \r\n";
				
		String tdcQry5_1 = " SELECT * FROM wm_log_thread \r\n"
				+ " WHERE PARENT_ID = %s \r\n"
				+ " ORDER BY start_dt DESC \r\n";
				
		
		String tdcQry6 = " SELECT * FROM xxfc.xxfc_servicios_linea \r\n"
				+ " WHERE CR_PLAZA = '"+ data.get("plaza") +"' \r\n"
				+ " AND estatus = 'L' \r\n"
				+ " AND cr_tienda = %s \r\n"
				+ " AND folio_WM = %s \r\n"
				+ " AND item = %s \r\n"
				+ " AND wm_action = 'ACK'\r\n";
				
				

/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
				
		
		/* PASO 1 *********************************************************************/	

		addStep("Validar que existen datos para procesar en la tabla XXFC_SERVICIOS_LINEA de ORAFIN con estatus 'N' para la plaza '10APO'.");
				
		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		System.out.println(tdcQry1);
		SQLResult paso1_qry1_Result = dbFic.executeQuery(tdcQry1);
		
		String item = paso1_qry1_Result.getData(0, "item");
        String folio = paso1_qry1_Result.getData(0, "folio");
        String Fecha = paso1_qry1_Result.getData(0, "fecha_transaccion");
        String tienda = paso1_qry1_Result.getData(0, "CR_TIENDA");
        
		boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); 

		if (!paso1_qry1_valida) {
			testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); 
		}

		System.out.println(paso1_qry1_valida);
		assertFalse("No se encontraron registros a procesar ", paso1_qry1_valida); 
		

		/* PASO 2 *********************************************************************/	
		
		addStep("Validar que existen datos disponibles para procesar en la tabla CATALOGO_SERVICIOS para los ITEMS procesados.");

		System.out.println(GlobalVariables.DB_HOST_FCWMREQA);
		String FormatdcQry2 = String.format(tdcQry2, item);
		System.out.println(FormatdcQry2);
		SQLResult paso2_qry2_Result = dbFceqa.executeQuery(FormatdcQry2);	


		boolean paso2_qry2_valida = paso2_qry2_Result.isEmpty(); 

		if (!paso2_qry2_valida) {
			testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); 
		}

		System.out.println(paso2_qry2_valida);
		assertFalse("No se encontraron registros a procesar ", paso2_qry2_valida); 
			
		/* PASO 3 *********************************************************************/	

		addStep("Validar datos disponibles para procesar en la tabla GIF_TRANSACCION con WM_CODE = 101.");
		
		System.out.println(GlobalVariables.DB_HOST_FCWMREQA);
		String FormatotdcQry3 = String.format(tdcQry3,folio,Fecha,Fecha,tienda);
		System.out.println(FormatotdcQry3);
		SQLResult paso3_qry3_Result = dbFceqa.executeQuery(FormatotdcQry3);	

		boolean paso3_qry3_valida = paso3_qry3_Result.isEmpty(); 

		if (!paso3_qry3_valida) {
			testCase.addQueryEvidenceCurrentStep(paso3_qry3_Result); 
		}

		System.out.println(paso3_qry3_valida);
		assertFalse("No se encontraron registros a procesar ", paso3_qry3_valida); 

		
		/* PASO 4 *********************************************************************/	

        addStep("Ejecutar la interfaz por medio del servicio ");
		
		u = new SeleniumUtil(new ChromeTest(), true);
		pok = new PakageManagment(u, testCase);

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555";
		u.get(contra);

		 pok.runIntefaceWmOneButton10(data.get("interface"), data.get("servicio"));


		/* PASO 5 *********************************************************************/	

		addStep("Validar que la interfaz haya finalizado correctamente en el WMLOG.\r\n");

		System.out.println(GlobalVariables.DB_HOST_Puser);
		System.out.println(tdcQry5);
		SQLResult paso5_qry5_Result = dbLog.executeQuery(tdcQry5);	
		
		String runid = paso5_qry5_Result.getData(0, "RUN_ID");

		boolean paso5_qry5_valida = paso5_qry5_Result.isEmpty(); 

		if (!paso5_qry5_valida) {
			testCase.addQueryEvidenceCurrentStep(paso5_qry5_Result); 
		}

		System.out.println(paso5_qry5_valida);
		assertFalse("No se encontraron registros a procesar ", paso5_qry5_valida); 
		
		
		/* PASO 5_1 *********************************************************************/	

		addStep("Validar que la interfaz haya finalizado correctamente en el Thread.\r\n");

		System.out.println(GlobalVariables.DB_HOST_Puser);
		
		String FormatotdcQry5_1 = String.format(tdcQry5_1, runid);
		System.out.println(FormatotdcQry5_1);
		SQLResult paso5_1_qry5_1_Result = dbLog.executeQuery(FormatotdcQry5_1);	
		
		boolean paso5_1_qry5_1_valida = paso5_1_qry5_1_Result.isEmpty(); 

		if (!paso5_1_qry5_1_valida) {
			testCase.addQueryEvidenceCurrentStep(paso5_1_qry5_1_Result); 
		}

		System.out.println(paso5_1_qry5_1_valida);
		assertFalse("No se encontraron registros a procesar ", paso5_1_qry5_1_valida); 
		
		
		/* PASO 6 *********************************************************************/	

		addStep("Verificar que el servicio este actualizado en la tabla XXFC_SERVICIOS_LINEA, estatus = 'L'");

		System.out.println(GlobalVariables.DB_HOST_FCIASQA);
		
		String FormatotdcQry6 = String.format(tdcQry6, tienda,folio,item);
		System.out.println(FormatotdcQry6);
		SQLResult paso6_qry6_Result = dbFic.executeQuery(FormatotdcQry6);		 

		boolean paso6_qry6_valida = paso6_qry6_Result.isEmpty(); 

		if (!paso6_qry6_valida) {
			testCase.addQueryEvidenceCurrentStep(paso6_qry6_Result); 
		}

		System.out.println(paso6_qry6_valida);
		assertFalse("No se encontraron registros a procesar ", paso6_qry6_valida); 

	}


	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_003_OO02_OO02_Procesameinto_Plaza_Transacciones_POS";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "Bloqueada. Verificar procesamiento de la interfaz para la plaza 10HER con transacciones POS, transacción ACEPTADA";
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
