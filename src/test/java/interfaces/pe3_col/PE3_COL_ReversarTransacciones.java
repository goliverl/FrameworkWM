package interfaces.pe3_col;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static util.RequestUtil.getSimpleDataXml;

import java.util.HashMap;

import org.testng.annotations.Test;

import modelo.BaseExecution;
import om.PE3_COL;
import util.GlobalVariables;
import utils.sql.SQLResult;
import utils.sql.SQLUtil;

public class PE3_COL_ReversarTransacciones  extends BaseExecution{

	@Test(dataProvider = "data-provider")
	public void ATC_FT_001_PE3_COL_Reservar_Transacciones(HashMap<String, String> data) throws Exception {

		/*
		 * Utilerías
		 ********************************************************************************************************************************************/
		/*
		 * LA CONEXION A LA BD NO SE PUEDE HACER POR QUE FALTA SABER CONTRASEÑA PARA EL USUARIO TPECOUSER
		 * SI SE USA USUARIO DIFERENTE EN LA CONEXION, NO SE PUEDE VER LA TABLA UTILIZADA PARA ESTA PRUEBA
		 * LA CONEXION NECESARIA ES:
		 * USER: TPECOUSER
		 * PASS: ???? DSCONOCIDO
		 * HOST: 10.184.80.120:1521/FCTPEQA.FEMCOM.NET
		 */
		
		SQLUtil dbFCTPE_COL = new utils.sql.SQLUtil(GlobalVariables.DB_HOST_FCTPE_CO, GlobalVariables.DB_USER_FCTPE_CO,
				GlobalVariables.DB_PASSWORD_FCTPE_CO);

		
		PE3_COL PE3_COLUtil = new PE3_COL(data, testCase, null);

		/*
		 * Variables
		 ******************************************************************************************************************************************/

		String ValidRev="SELECT FOLIO,CREATION_DATE,PLAZA,TIENDA,STATUS "
				+ "FROM TPEUSER.gif_col_reverse "
				+ "WHERE TRUNC (creation_date) = TRUNC (SYSDATE)";


//********************************************************************************************************************************************************************************		

		/* Pasos */

//		Paso 1	************************	
	
			addStep("Ejecutar el servicio runReverseManager ");

			String respuesta = PE3_COLUtil.RunReverse();
			System.out.print("Doc: " + respuesta);

			boolean validationResponse = true;

			if (respuesta != null) {
				validationResponse = false;
				testCase.addTextEvidenceCurrentStep("Response: \n" + respuesta);
				
			}

			assertFalse(validationResponse,"No se obtuvo el resultado esperado");
//		Paso 2	************************		
			/*
			 * LA CONEXION A LA BD NO SE PUEDE HACER POR QUE FALTA SABER CONTRASEÑA PARA EL USUARIO TPECOUSER
			 * SI SE USA USUARIO DIFERENTE EN LA CONEXION, NO SE PUEDE VER LA TABLA UTILIZADA PARA ESTA PRUEBA
			 * LA CONEXION NECESARIA ES:
			 * USER: TPECOUSER
			 * PASS: ???? DSCONOCIDO
			 * HOST: 10.184.80.120:1521/FCTPEQA.FEMCOM.NET
			 */
			
				addStep("Verificar las transacciones reversadas");
				System.out.print(GlobalVariables.DB_HOST_FCTPE_CO);
				System.out.println(ValidRev);
				SQLResult resultQuery = dbFCTPE_COL.executeQuery(ValidRev);

				boolean resultQueryRes = resultQuery.isEmpty();

				if (!resultQueryRes) {


					testCase.addQueryEvidenceCurrentStep(resultQuery);
					
				}
				
				assertFalse(resultQueryRes, "No se encontraron datos en la consulta");


		
	}

	@Override
	public String setTestFullName() {
		// TODO Auto-generated method stub
		return "ATC_FT_001_PE3_COL_Reservar_Transacciones";
	}

	@Override
	public String setTestDescription() {
		// TODO Auto-generated method stub
		return "PE3_COL_ReversarTransacciones";
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
