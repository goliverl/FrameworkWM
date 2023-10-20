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

public class ATC_FT_003_Pb10_Verificar_Correcta_Ejecuccion_plaza_Depura extends BaseExecution {	
	
	/*
	 * 
	 * @cp1 Verificar la correcta ejecucion para la plaza 10BGA y tienda 50RBT (Depura)
	 * @cp2 Verificar la correcta ejecucion para la plaza 10BGA y tienda 50UCF (Depura)
	 * @cp3 Verificar la correcta ejecucion para la plaza 10MON y tienda 50EDI (Depura)
	 * 
	 */
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_003_Pb10_Verificar_Correcta_Ejecuccion_plaza_Depura_test(HashMap<String, String> data) throws Exception {

/** UTILERIA *********************************************************************/	
		

		utils.sql.SQLUtil dbLog = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCWMLQA, GlobalVariables.DB_USER_FCWMLQA, GlobalVariables.DB_PASSWORD_FCWMLQA);
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTDAQA, GlobalVariables.DB_USER_FCTDAQA, GlobalVariables.DB_PASSWORD_FCTDAQA);	
			 
		String user = data.get("user");
		String ps = PasswordUtil.decryptPassword(data.get("ps"));
		String server = data.get("server");
		
		testCase.setTest_Description("Verificar la correcta ejecucion para la plaza "+data.get("plaza")+
				" y tienda "+data.get("tienda")+" (Depura)");
				
/** VARIABLES *********************************************************************/	

		String tdcQry1 = " SELECT PV_CR_PLAZA, PV_CR_TIENDA, CREATION_DATE \r\n"
				+ " FROM XXRFCO.XXRFCO_POS_FVT_TEMP \r\n"
				+ " WHERE PV_CR_PLAZA = '"+ data.get("plaza") + "' \r\n"
				+ " AND PV_CR_TIENDA = '"+ data.get("tienda") + "'\r\n"
				+ " AND CREATION_DATE < SYSDATE-30 \r\n";
				

		String tdcQry3 =  " SELECT *  FROM WMLOG.WM_LOG_RUN \r\n"
				+ " WHERE INTERFACE = 'PB10Depura'  \r\n"
				+ " AND STATUS = 'S' \r\n"
				+ " AND TRUNC(END_DT) = TRUNC(SYSDATE) \r\n"
				+ " ORDER BY RUN_ID DESC \r\n";

		String tdcQry4 = " SELECT COUNT(PID_ID) as PID_ID \r\n"
				+ " FROM XXRFCO.XXRFCO_POS_FVT_TEMP \r\n"
				+ " WHERE PV_CR_PLAZA = '"+ data.get("plaza") + "' \r\n"
				+ " AND PV_CR_TIENDA = '"+ data.get("tienda") + "' \r\n"
				+ " AND CREATION_DATE < SYSDATE-30 \r\n" ;
				


		
		
/** PASOS DEL CASO DE PRUEBA *********************************************************************/	
						
		/* PASO 1 *********************************************************************/	

		
		addStep("Validar que exista informacion con fecha de creacion mayor a 30 dias en  la tabla POS_FVT_TEMP de POSREP para la plaza y tienda \r\n");	
		System.out.println(GlobalVariables.DB_HOST_FCTDAQA);
		System.out.println(tdcQry1);
		SQLResult paso1_qry1_Result = dbPos.executeQuery(tdcQry1);		

		boolean paso1_qry1_valida = paso1_qry1_Result.isEmpty(); 

		if (!paso1_qry1_valida) {
			testCase.addQueryEvidenceCurrentStep(paso1_qry1_Result); 
		}

		System.out.println(paso1_qry1_valida);
		assertFalse("No se encontro informacion en la base de datos.", paso1_qry1_valida); 
		
	
		
		/* PASO 2 *********************************************************************/	


		addStep("Invocar el servicio PB10.Pub:runDepura mediante la ejecucion del JOB PB10Depura.");


		SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
		PakageManagment pok = new PakageManagment(u, testCase);
		
		String contra = "http://" + user + ":" + ps + "@" + server + ":5555"; 
		System.out.println(contra);
		u.get(contra);
		pok.runIntefaceWmOneButton(data.get("interfase"), data.get("servicio"));
	
		
		/* PASO 3 *********************************************************************/	


		addStep("Verificar que el estatus sea igual a 'S' para la interface PB10Depura en la tabla WM_LOG_RUN de la BD del WMLOG.");

		System.out.println(GlobalVariables.DB_HOST_FCWMLQA);
		System.out.println(tdcQry3);
		SQLResult paso3_qry3_Result = dbLog.executeQuery(tdcQry3);		

		boolean paso3_qry3_valida = paso3_qry3_Result.isEmpty(); 

		if (!paso3_qry3_valida) {
			testCase.addQueryEvidenceCurrentStep(paso3_qry3_Result); 
		}

		System.out.println(paso3_qry3_valida);
		assertFalse("No se encontro informacion en la base de datos.", paso3_qry3_valida); 
		
		
		/* PASO 4 *********************************************************************/	

		
		addStep("Se valida el STATUS = E en la tabla POS_INBOUND_DOCS con Tipo de documento RPA en la BD POSUSER.");

		System.out.println(GlobalVariables.DB_HOST_FCTDAQA);
		System.out.println(tdcQry4);
		SQLResult paso4_qry4_Result = dbPos.executeQuery(tdcQry4);		
  
		String Count =  paso4_qry4_Result.getData(0, "PID_ID");
		
		int count1 = Integer.parseInt(Count);
		
		System.out.println(Count);
		
		boolean paso4_qry4_valida = paso4_qry4_Result.isEmpty();
		
		if (count1==0) {
			System.out.println("Entro al IF");
			testCase.addQueryEvidenceCurrentStep(paso4_qry4_Result); 
		}

		System.out.println(paso4_qry4_valida);
		assertFalse("Se encontro informacion en la base de datos.", paso4_qry4_valida); 
		

	}
	

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_003_Pb10_Verificar_Correcta_Ejecuccion_plaza_Depura_test";
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
