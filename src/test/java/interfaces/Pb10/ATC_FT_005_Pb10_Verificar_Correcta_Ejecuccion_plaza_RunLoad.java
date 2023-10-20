package interfaces.Pb10;

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

public class ATC_FT_005_Pb10_Verificar_Correcta_Ejecuccion_plaza_RunLoad extends BaseExecution {	
	
	
	/*
	 * 
	 * @cp1 Verificar la correcta ejecucion para la plaza 10BGA y tienda 50RBT (runLoad)
	 * @cp2 Verificar la correcta ejecucion para la plaza 10MON y tienda 50EDI (runLoad)
	 * @cp3 Verificar la ejecucion de la interfaz PB10 (runLoad) para la plaza 10BGA y tienda 50UCF
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_005_Pb10_Verificar_Correcta_Ejecuccion_plaza_RunLoad_test(HashMap<String, String> data) throws Exception {

/** UTILERIA *********************************************************************/	
		
		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDAQA, GlobalVariables.DB_USER_FCTDAQA, GlobalVariables.DB_PASSWORD_FCTDAQA);

		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		
		testCase.setTest_Description("Verificar la correcta ejecucion para la plaza "+data.get("plaza")+
				" y tienda "+data.get("tienda")+" (runLoad)");
				
/** VARIABLES *********************************************************************/	

		String tdcQry1 = " SELECT *\r\n"
				+ "FROM  XXRFCO.XXRFCO_POS_FVT_DETL B,  XXRFCO.XXRFCO_POS_FVT A \r\n"
				+ "WHERE A.PID_ID = B.PID_ID \r\n"
				+ "AND PV_CR_PLAZA = '" + data.get("plaza")+ "' \r\n"
				+ "AND PV_CR_TIENDA = '" + data.get("tienda") + "' \r\n"
				+ "AND STATUS = 'I'\r\n";
				
				

		String tdcQry2 =  " SELECT COUNT(PID_ID) FROM XXRFCO.XXRFCO_POS_FVT_TEMP \r\n"
				+ " WHERE PV_CR_PLAZA = '%s' \r\n"
				+ " AND PV_CR_TIENDA = '%s' \r\n"
				+ " AND pid_id = %s \r\n";
				

		String tdcQry4 = "SELECT * FROM WMLOG.WM_LOG_RUN \r\n"
				+ " WHERE INTERFACE = 'PB10Load' \r\n"
				+ " AND STATUS = 'S' \r\n"
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE) \r\n"
				+ " ORDER BY RUN_ID DESC \r\n";
				
				
	    String tdcQry5 =  "SELECT * FROM WMLOG.WM_LOG_THREAD \r\n"
	    		+ " WHERE TRUNC(START_DT) = TRUNC(SYSDATE) \r\n"
	    		+ " AND PARENT_ID = %s \r\n"
	    		+ " AND STATUS = 'S' \r\n";
	
	    
	    String tdcQry6 =  "SELECT *\r\n"
	    		+ " FROM XXRFCO.XXRFCO_POS_FVT \r\n"
	    		+ " WHERE PV_CR_PLAZA = %s \r\n"
	    		+ " AND PV_CR_TIENDA = %s \r\n"
	    		+ " AND STATUS = 'P' \r\n"
	    		+ " AND PID_ID = %s "
	    		+ " AND RUN_ID = %s \r\n"
	    		+ " AND TRUNC(WM_LAST_UPDATE_DATE) = TRUNC(SYSDATE) \r\n";
	    		
	    		
	    String tdcQry7 =" SELECT PID_ID, PV_CR_PLAZA, PV_CR_TIENDA, CREATION_DATE \r\n"
	    		+ " FROM XXRFCO.XXRFCO_POS_FVT_TEMP \r\n"
	    		+ " WHERE pv_cr_plaza = %s \r\n"
	    		+ " AND pv_cr_tienda = %S \r\n"
	    		+ " AND pid_id = %s \r\n"
	    		+ " AND TRUNC(CREATION_DATE) = TRUNC(SYSDATE) ";
	    		

		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
						
		/* PASO 1 *********************************************************************/	

		addStep(" Validar que exista informacion en las tablas POS_FVT y POS_FVT_DETL de POSREP para la plaza y tienda con status I. ");
				
		System.out.println(GlobalVariables.DB_HOST_FCTDAQA);
		System.out.println(tdcQry1);
		SQLResult paso1_qry1_Result = dbPos.executeQuery(tdcQry1);	
		
		String Plaza = "";
		String Tienda = "";
		String pid_id = "";
		

		boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); // checa que el string contenga datos

		if (!paso1_qry1_valida) {
			
			Plaza = paso1_qry1_Result.getData(0, "PV_CR_PLAZA");
			Tienda = paso1_qry1_Result.getData(0, "PV_CR_TIENDA");
			pid_id =paso1_qry1_Result.getData(0, "PID_ID");
			
			testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso1_qry1_valida);
		
		assertFalse(" No se encontro informacion en la base de datos. ", paso1_qry1_valida); // Si esta vacio, imprime mensaje		
		
		/* PASO 2 *********************************************************************/
		
		addStep(" Validar que no existan los documentos que se van a procesar  en la tabla POS_FVT_TEMP en POSREP para la plaza y tienda con status I. ");
				
		System.out.println(GlobalVariables.DB_HOST_FCTDAQA);
		String FormatotdcQry2 = String.format(tdcQry2, Plaza,Tienda,pid_id);
		System.out.println(FormatotdcQry2);
		
		SQLResult paso2_qry2_Result = dbPos.executeQuery(FormatotdcQry2);		

		boolean paso2_qry2_valida = paso2_qry2_Result.isEmpty(); // checa que el string contenga datos

		if (!paso2_qry2_valida) {
			testCase.addQueryEvidenceCurrentStep(paso2_qry2_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso2_qry2_valida);
		assertFalse(" No se encontro informacion en la base de datos. ", paso2_qry2_valida); // Si esta vacio, imprime mensaje	
		
		
		/* PASO 3 *********************************************************************/	

		addStep(" Se invoca el servicio PB10.Pub:runLoad ejecutando el JOB PB10Load ");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555"; 
		System.out.println(contra);
		u.get(contra);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
	
		
		/* PASO 4 *********************************************************************/	

		addStep(" Verificar que el estatus sea igual a 'S' para la interface PB10Load en la tabla WM_LOG_RUN de la BD del WMLOG. ");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(tdcQry4);
		SQLResult paso4_qry4_Result = dbLog.executeQuery(tdcQry4);	
		
		String runid = "";

		boolean paso4_qry4_valida = paso4_qry4_Result.isEmpty(); // checa que el string contenga datos

		if (!paso4_qry4_valida) {
			
			runid = paso4_qry4_Result.getData(0, "RUN_ID");
			testCase.addQueryEvidenceCurrentStep(paso4_qry4_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso4_qry4_valida);
		
		assertFalse(" No se encontro informacion en la base de datos. ", paso4_qry4_valida); // Si esta vacio, imprime mensaje
		
		
		/* PASO 5 *********************************************************************/	

		addStep(" Verificar que existan registro en la tabla WM_LOG_THREAD para la plaza y la tienda con status S.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		String formatotdcQry5 = String.format(tdcQry5, runid);
		System.out.println(formatotdcQry5);
		SQLResult paso5_qry5_Result = dbPos.executeQuery(formatotdcQry5);	
		
		String threadid = "";

		boolean paso5_qry5_valida = paso5_qry5_Result.isEmpty(); // checa que el string contenga datos

		if (!paso5_qry5_valida) {
			
			threadid = paso5_qry5_Result.getData(0, "THREAD_ID");
			testCase.addQueryEvidenceCurrentStep(paso5_qry5_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso5_qry5_valida);
		
		assertFalse(" No se encontro informacion en la base de datos. ", paso5_qry5_valida); // Si esta vacio, imprime mensaje
		
		/* PASO 6 *********************************************************************/	
		
		addStep("Validar que el estatus en la tabla POS_FVT sea igual a 'P' en la plaza y la tienda en POSREP.");
		System.out.println(GlobalVariables.DB_HOST_FCTDAQA);		
		String FormatotdcQry6 = String.format(tdcQry6,Plaza,Tienda,pid_id,threadid);
		System.out.println(FormatotdcQry6);
		SQLResult paso6_qry6_Result = dbPos.executeQuery(FormatotdcQry6);		

		boolean paso6_qry6_valida = paso6_qry6_Result.isEmpty(); // checa que el string contenga datos

		if (!paso6_qry6_valida) {
			testCase.addQueryEvidenceCurrentStep(paso6_qry6_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso6_qry6_valida);
		
		assertFalse("No se encontro informacion en la base de datos.", paso6_qry6_valida); // Si esta vacio, imprime mensaje
		
		/* PASO 7 *********************************************************************/	
		
		addStep(" Verificar que la informacion de la plaza y la tienda sea insertada en la tabla POS_FVT_TEMP en POSREP ");
		System.out.println(GlobalVariables.DB_HOST_FCTDAQA);
		String formatotdcQry7 = String.format(tdcQry7, Plaza,Tienda,pid_id);
		
		System.out.println(formatotdcQry7);
		SQLResult paso7_qry7_Result = dbPos.executeQuery(formatotdcQry7);		

		boolean paso7_qry7_valida = paso7_qry7_Result.isEmpty(); // checa que el string contenga datos

		if (!paso7_qry7_valida) {
			testCase.addQueryEvidenceCurrentStep(paso7_qry7_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso7_qry7_valida);
		
		assertFalse("No se encontro informacion en la base de datos.", paso7_qry7_valida); // Si esta vacio, imprime mensaje
	}
	

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_005_Pb10_Verificar_Correcta_Ejecuccion_plaza_RunLoad_test";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
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
