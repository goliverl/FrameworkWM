package interfaces.pr1;

import static org.testng.Assert.assertTrue;

import java.util.HashMap;

import org.testng.annotations.Test;

import integrationServer.om.AdaptersPoolConection;
import modelo.BaseExecution;
import om.PE1;
import util.GlobalVariables;
import utils.password.PasswordUtil;
import utils.selenium.ChromeTest;
import utils.selenium.SeleniumUtil;
import utils.sql.SQLResult;
import integrationServer.om.PakageManagment;


public class PR1ConfiguracionDeAmbientesServiciosElectronicos extends BaseExecution{
	@Test(dataProvider = "data-provider")
	public void ATC_FT_006_PR1_ConfigAmbientesSE_test(HashMap<String, String> data) throws Exception {
		
		utils.sql.SQLUtil dbPos = new utils.sql.SQLUtil( GlobalVariables.DB_HOST_Puser, GlobalVariables.DB_USER_Puser,GlobalVariables.DB_PASSWORD_Puser);	
		
		// Utileria
	      SeleniumUtil u = new SeleniumUtil(new ChromeTest(), true);
	      PakageManagment pok = new PakageManagment(u, testCase);
	      boolean resumen = true;
		
		//Utileria
		String queryDocs = "SELECT ID, STATUS, DOC_TYPE, PV_DOC_NAME, RECEIVED_DATE " + 
				" FROM posuser.POS_INBOUND_DOCS" + 
				" where DOC_TYPE = 'TSF'" + 
				" AND PARTITION_DATE >= TRUNC(SYSDATE-13)";
		
		String queryNoRecords = " SELECT pid_id,NO_RECORDS" + 
				" FROM POSUSER.POS_TSF" + 
				" WHERE PID_ID ='%s' ";
		
		String queryItem = "SELECT pid_id,ITEM FROM POSUSER.POS_TSF_DETL" + 
				" WHERE PID_ID ='%s'"; 	
		
		String Status = "I";
		
		
//paso 1 ***************************************************************************************************************************************************************************
		  addStep("Resumen");
		testCase.addBoldTextEvidenceCurrentStep("Revisar que exista un documento tipo \"tsf\" en status \"I\" en la base de datos posuser en la tabla POS_INBOUND_DOCS. cumplir con un rango de fechas no menor a 13 dias");
		
		SQLResult resultDocs = executeQuery(dbPos, queryDocs);
		String id = resultDocs.getData(0, "ID");//Se guarda el ID 
		String statusQuery = resultDocs.getData(0, "STATUS");
		System.out.println(queryDocs);
		
		boolean validacion = statusQuery.equals(Status);
		if(!validacion) {
			pok.runIntefaceVdate();
			//testCase.addQueryEvidenceCurrentStep(resultDocs);
			testCase.addTextEvidenceCurrentStep("-Documento encontrado."); 
			testCase.addTextEvidenceCurrentStep("-El documento se encuentra en estatus I."); 
		}else {
		//testCase.addQueryEvidenceCurrentStep(resultDocs);
		testCase.addTextEvidenceCurrentStep("-Documento encontrado."); 
		testCase.addTextEvidenceCurrentStep("-El documento se encuentra en estatus I."); 
		}
		
//paso 2 ***************************************************************************************************************************************************************************
		testCase.addBoldTextEvidenceCurrentStep("Con el id conseguido del documento tsf verificar que se encuentra en la tabla POSUSER.POS_TSF y mostrar la cantidad de registros ques se procesaran en el campo 'NO_RECORDS'");
		
		
		String queryR = String.format(queryNoRecords, id);//se utiliza el ID para 
		SQLResult resultRecords = executeQuery(dbPos, queryR);
		System.out.println(queryR);
		
		boolean av2 = resultRecords.isEmpty();
		if(!av2) {
		//testCase.addQueryEvidenceCurrentStep(resultRecords);
		testCase.addTextEvidenceCurrentStep("-Se procesaran tantos documentos."); 
		}
		
		assertTrue(!av2);
		
		
//paso 3 ***************************************************************************************************************************************************************************

		testCase.addBoldTextEvidenceCurrentStep("Consultar el 'ITEM' en la tabla POSUSER.POS_TSF_DETL");
		String queryI = String.format(queryItem, id);
		SQLResult resultItem = executeQuery(dbPos, queryI);
		System.out.println(queryItem);
		boolean av3 = resultRecords.isEmpty();
		if(!av3) {
		//testCase.addQueryEvidenceCurrentStep(resultItem);
		testCase.addTextEvidenceCurrentStep("-Item encontrado."); 
		}
		
//paso 4 ***************************************************************************************************************************************************************************
		
		testCase.addBoldTextEvidenceCurrentStep("Se comprueba cuantos adapters estan encendidos");
		
       // SeleniumUtil u = new SeleniumUtil(new ChromeTest(),true);
            AdaptersPoolConection adaptPool = new AdaptersPoolConection(u, testCase);
             String user = data.get("user");
            String ps = PasswordUtil.decryptPassword( data.get("ps"));
            String server = data.get("server");      
           
            String contra =   "http://"+user+":"+ps+"@"+server+":5555";       
            u.get(contra);
            adaptPool.poolSwitchAdapter(resumen,data.get("adapter1"),data.get("adapter2"),data.get("adapter3"));
           // u.close();
            
            //****
            addStep("Evidencia:");
         addStep("Revisar que exista un documento tipo \"tsf\" en status \"I\" en la base de datos posuser en la tabla POS_INBOUND_DOCS. cumplir con un rango de fechas no menor a 13 dias");
    		
       		if(!validacion) {
    			pok.runIntefaceVdate();
    			testCase.addQueryEvidenceCurrentStep(resultDocs);
    			testCase.addTextEvidenceCurrentStep("-Documento encontrado."); 
    			testCase.addTextEvidenceCurrentStep("-El documento se encuentra en estatus I."); 
    		}else {
    		testCase.addQueryEvidenceCurrentStep(resultDocs);
    		testCase.addTextEvidenceCurrentStep("-Documento encontrado."); 
    		testCase.addTextEvidenceCurrentStep("-El documento se encuentra en estatus I."); 
    		}
    		
    //paso 2 ***************************************************************************************************************************************************************************
    		addStep("Con el id conseguido del documento tsf verificar que se encuentra en la tabla POSUSER.POS_TSF y mostrar la cantidad de registros ques se procesaran en el campo 'NO_RECORDS'");
    		
    		if(!av2) {
    		testCase.addQueryEvidenceCurrentStep(resultRecords);
    		testCase.addTextEvidenceCurrentStep("-Se procesaran tantos documentos."); 
    		}
    		
    		assertTrue(!av2);
    		
    		
    //paso 3 ***************************************************************************************************************************************************************************

    		
    		if(!av3) {
    		testCase.addQueryEvidenceCurrentStep(resultItem);
    		testCase.addTextEvidenceCurrentStep("-Item encontrado."); 
    		}
    		
    //paso 4 ***************************************************************************************************************************************************************************
    		resumen = false;
    		addStep("Se comprueba cuantos adapters estan encendidos");
    		
    		 //SeleniumUtil prueba = new SeleniumUtil(new ChromeTest(), true);
   	        	      
   	   u.hardWait(60);
   
   	   adaptPool = new AdaptersPoolConection(u, testCase);
                  user = data.get("user");
                 ps = PasswordUtil.decryptPassword( data.get("ps"));
                 server = data.get("server");      
               
                 contra =   "http://"+user+":"+ps+"@"+server+":5555";       
                u.get(contra);
                adaptPool.poolSwitchAdapter(resumen,data.get("adapter1"),data.get("adapter2"),data.get("adapter3"));
               // u.close();
       
         

		
	}

	@Override
	public void beforeTest() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "Qaautomation";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_006_PR1_ConfigAmbientesSE_test";
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
