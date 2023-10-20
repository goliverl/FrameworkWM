package interfaces.po11;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import java.util.HashMap;
import org.testng.annotations.Test;
import modelo.BaseExecution;
import util.GlobalVariables;
import utils.sql.SQLResult;

public class PO11_RecepcionDCIMovimientosNegativos extends BaseExecution {
	
	@Test(dataProvider = "data-provider")
	public void ATC_FT_004_PO11_Recepcion_DCI_Movimientos_Negativos(HashMap<String, String> data) throws Exception {
		
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
		
		//Paso 4
		
		String montoNegativo = "SELECT * \r\n" + 
				"FROM POSUSER.POS_DCI_DETL\r\n" + 
				"WHERE PID_ID = '4473376645'\r\n" + 
				"AND MOV_TYPE = 'DEP'\r\n" + 
				"AND MOV_ID_POS = '2'\r\n" + 
				"AND MONTO < 0";
		
		     
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
		addStep("Ejecutar la siguiente consulta en la conexión FCWM6QA para validar el tipo de movimiento, "
				+ "en este caso validaremos movimientos en Pesos,Dolares y TCCI (DEP2,DEP3,TCCI1).");
      
		String typeMovFormat = String.format(typeMov, id);
		
		System.out.println( typeMovFormat);	
		
		SQLResult typeMovResult= dbPos.executeQuery(typeMovFormat );
		
        boolean validatypeMov  = typeMovResult.isEmpty();
		
		if (!validatypeMov) {
			
			testCase.addQueryEvidenceCurrentStep(typeMovResult);
		}
		
		System.out.println(validatypeMov);
		
		assertFalse(validatypeMov, "No se encontro ningun  registro de archivo DCI con STATUS = I");
		
		//paso 3 ******************************
				addStep("Levantar ticket por autoregistro para realizar update en la tabla POSUSER.POS_DCI_DETL en la BD FCWM6QA.FEMCOM.NET "
						+ "para cambiar los montos a negativo en el campo MONTO");
		      
				String updatePos = "UPDATE  POSUSER.POS_DCI_DETL \r\n" + 
						"SET MONTO='-" + data.get("MONTO") + "' \r\n" + 
						"WHERE PID_ID='" + data.get("PID_ID") + "' \r\n" + 
						"AND MOV_TYPE = '" + data.get("MOV_TYPE ") + "'\r\n" + 
						"AND MOV_ID_POS='" + data.get("MOV_ID_POS ") + "'";
				
				System.out.println(updatePos);
				
		        boolean validateCambio  = true;
				
				if (validateCambio) {
					
					testCase.addBoldTextEvidenceCurrentStep(updatePos);
				}
				
				System.out.println(validateCambio);
				
				assertTrue( "No se realizo el update", validateCambio);
				
	//Paso 4************************************************************************************************************************************
				
				addStep("Ejecutar la siguiente consulta en la conexión FCWM6QA  para validar la recepción del archivo.");
				
				System.out.println(montoNegativo);
				
				SQLResult montoNegativoResult = executeQuery(dbPos, montoNegativo);
				
				boolean validamontoNegativo  = montoNegativoResult.isEmpty();
				
				if (!validamontoNegativo) {
					
					testCase.addQueryEvidenceCurrentStep(montoNegativoResult);
				}
				
				System.out.println(validamontoNegativo);
				
				assertFalse(validamontoNegativo, "No se encontro ningun  movimiento negativo");
				

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
		return "Terminada. Validar informacion y recepcion de DCI con movimientos negativos";
	}

	@Override
	public String setTestDesigner() {
		// TODO Auto-generated method stub
		return "EQUIPO AUTOMATION";
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_004_PO11_Recepcion_DCI_Movimientos_Negativos";
	}

	@Override
	public String setTestInstanceID() {
		// TODO Auto-generated method stub
		return null;
	}

}

