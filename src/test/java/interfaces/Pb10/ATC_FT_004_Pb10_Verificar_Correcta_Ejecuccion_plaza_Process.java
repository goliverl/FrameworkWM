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

public class ATC_FT_004_Pb10_Verificar_Correcta_Ejecuccion_plaza_Process extends BaseExecution {	
	
	/*
	 * 
	 * @cp1 Verificar la correcta ejecucion para la plaza 10BGA y tienda 50RBT (Process)
	 * @cp2 Verificar la correcta ejecucion para la plaza 10BGA y tienda 50UCF (Process)
	 * @cp3 Verificar la correcta ejecucion para la plaza 10MON y tienda 50EDI (Process)
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_Pb10_Verificar_Correcta_Ejecuccion_plaza_Process_test(HashMap<String, String> data) throws Exception {

/** UTILERIA *********************************************************************/	

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDAQA, GlobalVariables.DB_USER_FCTDAQA, GlobalVariables.DB_PASSWORD_FCTDAQA);		
		 
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		
		testCase.setTest_Description("Verificar la correcta ejecucion para la plaza "+data.get("plaza")+
				" y tienda "+data.get("tienda")+" (Process)");
				
/** VARIABLES *********************************************************************/	

		String tdcQry1 = " SELECT * \r\n"
				+ " FROM XXRFCO.XXRFCO_POS_FVT_TEMP \r\n"
				+ " WHERE PV_CR_PLAZA = '"+ data.get("plaza") + "' \r\n"
				+ " AND PV_CR_TIENDA = '"+ data.get("tienda") + "' "
				+ " AND WM_STATUS = 'I' \r\n"
				+ " AND CREATION_DATE < SYSDATE-30 \r\n";
				

		String tdcQry2 =  "SELECT * FROM XXRFCO.XXRFCO_POS_FFC \r\n"
				+ " WHERE PV_CR_PLAZA = '%s' \r\n"
				+ " AND PV_CR_TIENDA = '%s' \r\n"
				+ " AND FOL_FISC = '%s' \r\n"
				+ " AND NO_RES = %s \r\n"
				+ " AND PV_FOLIO = %s \r\n"
				+ " AND TICKET_FILE IS NULL \r\n";
				

		String tdcQry4 = "SELECT * FROM WMLOG.WM_LOG_RUN \r\n"
				+ " WHERE INTERFACE = 'PB10Process'  \r\n"
				+ " AND STATUS = 'S' \r\n"
				+ " AND TRUNC(START_DT) = TRUNC(SYSDATE) \r\n"
				+ " ORDER BY RUN_ID DESC \r\n";
				
	    String tdcQry5 =  "SELECT * FROM WMLOG.WM_LOG_THREAD \r\n"
	    		+ "WHERE ATT1 = '"+ data.get("plaza") + "' \r\n"
	    		+ "AND ATT2 = '"+ data.get("tienda") + "' \r\n"
	    		+ "AND TRUNC(START_DT) = TRUNC(SYSDATE) \r\n"
	    		+ "AND PARENT_ID = %s\r\n"
	    		+ "AND STATUS = 'S'\r\n";
	    
	    String tdcQry6 =  " SELECT PID_ID, PV_CR_PLAZA, PV_CR_TIENDA, WM_STATUS, RUN_ID, LAST_UPDATE_DATE \r\n"
	    		+ " FROM XXRFCO.XXRFCO_POS_FVT_TEMP \r\n"
	    		+ " WHERE PV_CR_PLAZA= '"+ data.get("plaza") + "'  \r\n"
	    		+ " AND PV_CR_TIENDA = '"+ data.get("tienda") + "'\r\n"
	    		+ " AND TRUNC(LAST_UPDATE_DATE) = TRUNC(SYSDATE) \r\n"
	    		+ " AND WM_STATUS = 'P'  \r\n"
	    		+ " AND RUN_ID = %s \r\n";
	    		
	    String tdcQry7 =" SELECT PV_CR_PLAZA, PV_CR_TIENDA, TICKET_FILE, WM_RUN_ID, WM_STATUS, WM_LAST_UPDATE_DATE \r\n"
	    		+ " FROM XXRFCO.XXRFCO_POS_FFC \r\n"
	    		+ " WHERE PV_CR_PLAZA = '"+ data.get("plaza") + "'  \r\n"
	    		+ " AND PV_CR_TIENDA = '"+ data.get("tienda") + "' \r\n"
	    		+ " AND WM_RUN_ID  = %s \r\n"
	    		+ " AND WM_STATUS = 'P' \r\n"
	    		+ " AND TRUNC(WM_LAST_UPDATE_DATE) = TRUNC(SYSDATE) \r\n"
	    		+ " AND TICKET_FILE IS NOT NULL \r\n";
	    		

		//Se tiene que hacer un update
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
						
		/* PASO 1 *********************************************************************/	

		addStep("Validar que exista informacion en  la tabla POS_FVT_TEMP de POSREP para la plaza y tienda con status I.");
				
		System.out.println(GlobalVariables.DB_HOST_FCTDAQA);
		System.out.println(tdcQry1);
		SQLResult paso1_qry1_Result = dbPos.executeQuery(tdcQry1);	
		
		String Plaza = "";
		String Tienda = "";
		String FOL_FISC = "";
		String NO_RES = "";
		String PV_FOLIO = "";
		

		boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); // checa que el string contenga datos

		if (!paso1_qry1_valida) {
			
			 Plaza = paso1_qry1_Result.getData(0, "PV_CR_PLAZA");
			 Tienda = paso1_qry1_Result.getData(0, "PV_CR_TIENDA");
			 FOL_FISC =paso1_qry1_Result.getData(0, "FOL_FISC");
			 NO_RES = paso1_qry1_Result.getData(0, "NO_RES");
			 PV_FOLIO =paso1_qry1_Result.getData(0, "PV_FOLIO");
			testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso1_qry1_valida);
		
		assertFalse("No se encontro informacion en la base de datos.", paso1_qry1_valida); // Si esta vacio, imprime mensaje		
		
		/* PASO 2 *********************************************************************/
		
		addStep("Validar que existan los tickets en la tabla POS_FFC para la plaza y tienda.");
		
		System.out.println(GlobalVariables.DB_HOST_FCTDAQA);
		String FormatotdcQry2 = String.format(tdcQry2, Plaza,Tienda,FOL_FISC,NO_RES,PV_FOLIO);
		System.out.println(FormatotdcQry2);
		
		SQLResult paso2_qry2_Result = dbPos.executeQuery(FormatotdcQry2);		

		boolean paso2_qry2_valida = paso2_qry2_Result.isEmpty(); // checa que el string contenga datos

		if (!paso2_qry2_valida) {
			testCase.addQueryEvidenceCurrentStep(paso2_qry2_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso2_qry2_valida);
		assertFalse("No se encontro informacion en la base de datos.", paso2_qry2_valida); // Si esta vacio, imprime mensaje	
		
	
		
		/* PASO 3 *********************************************************************/	

		addStep("Invocar el servicio PB10.Pub:runDepura mediante la ejecucion del JOB PB10Depura.");

		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555"; 
		System.out.println(contra);
		u.get(contra);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
	
		
		/* PASO 4 *********************************************************************/	

		addStep("Verificar que el estatus sea igual a 'S' para la interface PB10Process en la tabla WM_LOG_RUN de la BD del WMLOG.");

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
		
		assertFalse("No se encontro informacion en la base de datos.", paso4_qry4_valida); // Si esta vacio, imprime mensaje
		
		
		/* PASO 5 *********************************************************************/	

		addStep("Verificar que existan registro en la tabla WM_LOG_THREAD para la plaza y la tienda con status S.");
		
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
		
		assertFalse("No se encontro informacion en la base de datos.", paso5_qry5_valida); // Si esta vacio, imprime mensaje
		
		/* PASO 6 *********************************************************************/	
		
		addStep("Verificar que el STATUS sea \"P\" en los registron con Plaza y Tienda en la tabla POS_FVT_TEMP en POSREP");

		System.out.println(GlobalVariables.DB_HOST_FCTDAQA);
		String FormatotdcQry6 = String.format(tdcQry6, threadid);
		System.out.println(FormatotdcQry6);
		SQLResult paso6_qry6_Result = dbPos.executeQuery(FormatotdcQry6);		

		boolean paso6_qry6_valida = paso6_qry6_Result.isEmpty(); // checa que el string contenga datos

		if (!paso6_qry6_valida) {
			testCase.addQueryEvidenceCurrentStep(paso6_qry6_Result); // Si no esta vacio, lo agrega a la evidencia
		}

		System.out.println(paso6_qry6_valida);
		
		assertFalse("No se encontro informacion en la base de datos.", paso6_qry6_valida); // Si esta vacio, imprime mensaje
		
		/* PASO 7 *********************************************************************/	
		
		addStep("Validar los registros sean actualizados correctamente en la tabla POS_FFC con Plaza y Tienda, y el campo TICKET_FILE no es nulo");

		System.out.println(GlobalVariables.DB_HOST_FCTDAQA);
		String formatotdcQry7 = String.format(tdcQry7, threadid);
		
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
		return "ATC_FT_004_Pb10_Verificar_Correcta_Ejecuccion_plaza_Process_test";
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
