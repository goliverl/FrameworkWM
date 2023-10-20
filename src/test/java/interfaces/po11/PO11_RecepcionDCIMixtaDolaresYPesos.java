package interfaces.po11;


import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;

public class PO11_RecepcionDCIMixtaDolaresYPesos extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_002_PO11_Recepcion_DCI_Mixta_Dolares_Pesos(HashMap<String, String> data) throws Exception {
		
/* Utilerías *********************************************************************/		
		

		utils.sql.SQLUtil dbPos= new utils.sql.SQLUtil(GlobalVariables. DB_HOST_FCWMQA_NUEVA,GlobalVariables.DB_USER_FCWMQA_NUEVA ,GlobalVariables.DB_PASSWORD_FCWMQA_NUEVA);
	

		
/**
* Variables ******************************************************************************************
* 
* 
*/
		
		//Paso 1		
		String receptionFile = "SELECT ID, PE_ID, PV_DOC_ID, STATUS, DOC_TYPE, PV_DOC_NAME, TARGET_ID, PARTITION_DATE\r\n" + 
				"FROM POSUSER.POS_INBOUND_DOCS \r\n" + 
				"WHERE PV_DOC_NAME = '" + data.get("documento") + "'";
		
		//Paso 2
		
		String typeMov = "SELECT * \r\n" + 
				"FROM POSUSER.POS_DCI_DETL \r\n" + 
				"WHERE PID_ID = '%s'";
		
		     

		testCase.setProject_Name("Cajas inteligentes"); 
/**
* 	
* **********************************Pasos del caso de Prueba *****************************************
* 
*/
		
//Paso 1 *************************	
		addStep("Ejecutar la siguiente consulta en la conexión FCWM6QA  para validar la recepción del archivo.");
		
		System.out.println(receptionFile);
		
		SQLResult receptionFileResult = executeQuery(dbPos, receptionFile);
		
		String id = receptionFileResult.getData(0, "ID");
		
		System.out.println("ID: " + id);
		
		boolean validareceptionFile  = receptionFileResult.isEmpty();
		
		if (!validareceptionFile) {
			
			testCase.addQueryEvidenceCurrentStep(receptionFileResult);
		}
		
		System.out.println(validareceptionFile);
		
		assertFalse(validareceptionFile, "No se encontro ningun  registro de archivo DCI con STATUS = I");
		
//paso 2 ******************************
		addStep("Ejecutar la siguiente consulta en la conexión FCWM6QA  para validar el tipo de movimiento, "
				+ "en este caso validaremos movimientos en Pesos,Dolares y TCCI (DEP2,DEP3,TCCI1).");
      
		String typeMovFormat = String.format(typeMov, id);
		
		System.out.println(typeMovFormat);	
		
		SQLResult typeMovResult= dbPos.executeQuery(typeMovFormat );
		
        boolean validatypeMov  = typeMovResult.isEmpty();
		
		if (!validatypeMov) {
			
			testCase.addQueryEvidenceCurrentStep(typeMovResult);
		}
		
		System.out.println(validatypeMov);
		
		assertFalse(validatypeMov, "No se encontro ningun  registro de archivo DCI con STATUS = I");
		
	

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
		return "Terminada. Se validara que llegue el archivo a Integración y que se guarde la información en las tablas "
				+ "correspondientes verificando el tipo de movimiento sea Pesos (DEP2), dolares (DEP3) y el TCCI(TCCI1)";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_002_PO11_Recepcion_DCI_Mixta_Dolares_Pesos";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}

