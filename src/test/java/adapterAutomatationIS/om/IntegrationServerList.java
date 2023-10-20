package adapterAutomatationIS.om;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import config.Constants;
import modelo.TestCase;
import utils.selenium.SeleniumUtil;

public class IntegrationServerList {
	
	
	//utilerias
	
	SeleniumUtil u;
	TestCase testCase;
	// Creating Workbook instances 
    Workbook wb ;
    // An output stream accepts output bytes and sends them to sink. 
    OutputStream fileOut; 
    // Creating Sheets using sheet object 
    Sheet sheet ;
	
	
	//elementos de la pagina 
    
	By messageError = By.xpath("/html/body/form/table/tbody/tr[2]/td");
	By notEnableElementsBy =  By.xpath("//img[contains(@src,'/WmRoot/images/blank.gif')]/../../../td[1]");
	String notEnableElementByNameBy = "//td[text()='%s']/..//img[contains(@src,'/WmRoot/images/blank.gif')]";
	
	By editView    =  	By.xpath("//img[contains(@src,'/WmART/icons/config_edit.gif')]");
	
	By viewDetails =    By.xpath("//img[@src='/WmRoot/images/green_check.gif']/../../..//img[contains(@src,'/WmRoot/icons/file.gif')]/..");
	By errorPane   = 	By.xpath(".//td[text()='Error encountered ']/pre");
	
	//AdapterConnectios
	
	By adapterConnectionBy = By.xpath("/html/body/form/table[1]/tbody/tr[3]/td");
	By connectionTypeBy = By.xpath("/html/body/form/table[1]/tbody/tr[4]/td[2]");
	By conPackageBy =	By.xpath("/html/body/form/table[1]/tbody/tr[5]/td[2]");
	//ConnectionProperties
	
	By transactionTypeValueBy    = 	By.xpath("/html/body/form/table[1]/tbody/tr[7]/td[2]");
	By dataSourceValueBy    = 		By.xpath("/html/body/form/table[1]/tbody/tr[8]/td[2]");
	By serverNameValueBy    = 		By.xpath("/html/body/form/table[1]/tbody/tr[9]/td[2]");
	By userValueBy          = 		By.xpath("/html/body/form/table[1]/tbody/tr[10]/td[2]");
	By dataBaseValueBy    = 		By.xpath("/html/body/form/table[1]/tbody/tr[12]/td[2]");
	By portNumberValueBy    = 		By.xpath("/html/body/form/table[1]/tbody/tr[13]/td[2]");
	By networkProtocolValueBy    = 	By.xpath("/html/body/form/table[1]/tbody/tr[14]/td[2]");
	By otherProportiesValueBy    = 	By.xpath("/html/body/form/table[1]/tbody/tr[15]/td[2]");
	
	//Connection Management Properties
	
	By EnableConnectionValueBy    = 	By.xpath("/html/body/form/table[1]/tbody/tr[17]/td[2]");
	By minpoolSizeValueBy    = 			By.xpath("/html/body/form/table[1]/tbody/tr[18]/td[2]");
	By maxpoolSizeValueBy    = 			By.xpath("/html/body/form/table[1]/tbody/tr[19]/td[2]");
	By poolIncrementSizeValueBy    = 	By.xpath("/html/body/form/table[1]/tbody/tr[20]/td[2]");
	By blockTimeOutValueBy    = 		By.xpath("/html/body/form/table[1]/tbody/tr[21]/td[2]");
	By expireTimeValueBy    = 			By.xpath("/html/body/form/table[1]/tbody/tr[22]/td[2]");
	By startUpValueBy    = 				By.xpath("/html/body/form/table[1]/tbody/tr[23]/td[2]");
	By startUpBackValueBy    = 			By.xpath("/html/body/form/table[1]/tbody/tr[24]/td[2]");
	
	//generacion de constructor  /html/body/form/table[1]/tbody/tr[15]/td[1]/..
	public IntegrationServerList(SeleniumUtil u, TestCase testCase) {
		super();
		this.u = u;
		this.testCase = testCase;
		
		
		
		
		
	}

	public WebElement elementExist(By by) {
		
		
		try {
			
			WebElement webElement = u.getDriver().findElement(by);
		
		   return webElement;
		   
		   
		} catch (NoSuchElementException e) {
		   return null;
		}
		
		
	} 
	


public HashMap<String, String> getErrors() {

	 HashMap<String, String> errors = new HashMap<String, String>();
	

	 	u.getDriver().switchTo().frame(2);
//		u.highLight(By.xpath("//html"));
	

	 
	List<WebElement> notEnableElements = u.getDriver().findElements(notEnableElementsBy);
	
	
	
	//atributo del get Txt
List<String> adaptersName = notEnableElements.stream().map(WebElement::getText).collect(Collectors.toList());
	
	for (String adapterName : adaptersName) {
		
		String xPathAdaptersEnable = String.format(notEnableElementByNameBy, adapterName);
		
		u.click(By.xpath(xPathAdaptersEnable));
		
		
		u.getDriver().switchTo().alert().accept();
		u.hardWait(2);
//		u.highLight(By.xpath("//html"));
//		u.getDriver().switchTo().frame(2);
//		
		
		 
		WebElement errorPaneElement = elementExist(errorPane);
		 
		if (errorPaneElement!= null) {
			 
			String connectionName;
			String error;
			 
			error = errorPaneElement.getText();
			String textToFind	= "connection resource";				
			connectionName = error.substring(error.indexOf(textToFind) + textToFind.length()+1, error.indexOf("\n")-1);	 
			
			errors.put(connectionName, error);
			
		    testCase.addTextEvidenceCurrentStep(connectionName + " " + error);
		    //el nombre del adapter lo partimos 
		    testCase.addScreenShotCurrentStep(u, "Error " + connectionName.split(":")[1]);
			

			
		}
		
		
		
	}
	

	
	return errors;
}

public HashMap<String, String> clickView() throws Exception {

	
	
	 HashMap<String, String> details = new HashMap<String, String>();
	

	
		u.getDriver().switchTo().frame(2);
//		u.highLight(By.xpath("//html"));
	 
	List<WebElement> detailElements = u.getDriver().findElements(viewDetails);
	
	try {
		
		String rutaExcel = Constants.BASE_PATH+"\\Output\\email\\";
		File file = new File(rutaExcel);
		file.mkdir();
		  
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
		Date date = new Date();
		
		String nombreExcel = testCase.getTestNameId()+" - " +dateFormat.format(date)+".xlsx" ;
		
		  
		wb= new XSSFWorkbook(); 
		this.fileOut =  new FileOutputStream(rutaExcel+"\\"+nombreExcel);
		sheet= wb.createSheet("Reporte");
		
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		System.err.println("algo salio Mal :(");
	} 
	
	
	 String rgbS = "dff5f5";
     byte[] rgbB = Hex.decodeHex(rgbS); // get byte array from hex string
     XSSFColor color = new XSSFColor(rgbB, null); //IndexedColorMap has no usage until now. So it can be set null.

     XSSFCellStyle cellStyle = (XSSFCellStyle) wb.createCellStyle();
     cellStyle.setFillForegroundColor(color);
     cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

 //adapterConn
     
     Row row = sheet.createRow(0);
     Cell cell0 = row.createCell(0);
     cell0.setCellValue("AdapterConnections");
     cell0.setCellStyle(cellStyle);
     
     
     Cell cell = row.createCell(1);
     cell.setCellValue("Connection Type");
     cell.setCellStyle(cellStyle);
     
   
     Cell cell2 = row.createCell(2);
     cell2.setCellValue("Package Name");
     cell2.setCellStyle(cellStyle);
     
   //Properties 
     
     Cell cell3 = row.createCell(3);
     cell3.setCellValue("Transaction Type");
     cell3.setCellStyle(cellStyle);
     
     Cell cell4 = row.createCell(4);
     cell4.setCellValue("DataSource Class");
     cell4.setCellStyle(cellStyle);
     
     Cell cell5 = row.createCell(5);
     cell5.setCellValue("server Name");
     cell5.setCellStyle(cellStyle);

     Cell cell6 = row.createCell(6);
     cell6.setCellValue("user");
     cell6.setCellStyle(cellStyle);
     
    
     Cell cell7 = row.createCell(7);
     cell7.setCellValue("database Name");
     cell7.setCellStyle(cellStyle);
     
  
     Cell cell8 = row.createCell(8);
     cell8.setCellValue("portNumber");
     cell8.setCellStyle(cellStyle);
     
     Cell cell9 = row.createCell(9);
     cell9.setCellValue("NetworkProtocol");
     cell9.setCellStyle(cellStyle);
     
     Cell cell10 = row.createCell(10);
     cell10.setCellValue("Other Properties");
     cell10.setCellStyle(cellStyle);
     
// Managment Properties     
     Cell cell11 = row.createCell(11);
     cell11.setCellValue("Enable Connection Pooling");
     cell11.setCellStyle(cellStyle);
     
     Cell cell12 = row.createCell(12);
     cell12.setCellValue("Minimum Pool Size");
     cell12.setCellStyle(cellStyle);
     
     Cell cell13 = row.createCell(13);
     cell13.setCellValue("Maximum Pool Size");
     cell13.setCellStyle(cellStyle);
     
     Cell cell14 = row.createCell(14);
     cell14.setCellValue("Pool Increment Size");
     cell14.setCellStyle(cellStyle);
     
     Cell cell15 = row.createCell(15);
     cell15.setCellValue("Block Timeout (msec)");
     cell15.setCellStyle(cellStyle);
     
     Cell cell16 = row.createCell(16);
     cell16.setCellValue("Expire Timeout (msec)");
     cell16.setCellStyle(cellStyle);
     
     Cell cell17 = row.createCell(17);
     cell17.setCellValue("Startup Retry Count");
     cell17.setCellStyle(cellStyle);
     
     Cell cell18 = row.createCell(18);
     cell18.setCellValue("Startup Backoff Timeout (sec)");
     cell18.setCellStyle(cellStyle);
     
     for (int i = 1; i <= detailElements.size(); i++) {
		
    	 
    			
    			String urlDetails = detailElements.get(i-1).getAttribute("href");
    			
    			String selectLinkOpeninNewTab = Keys.chord(Keys.CONTROL,Keys.RETURN); 

    			detailElements.get(i-1).sendKeys(selectLinkOpeninNewTab);
    			
    			
    			ArrayList <String> tabs = new ArrayList <String>(u.getDriver().getWindowHandles());
    			
    		    //System.out.println(tabs.size());
    		   // u.highLight(viewDetails);
    			
    		    u.getDriver().switchTo().window(tabs.get(2));
    		    
    		    u.get(urlDetails);
    		    
    		    getDetails(i);
    		    
    		   //devolverte a la pestaña de la lista  

    		    
    		  //cerrar pestaña actual, antes del comentario y 
    		    
    		    u.getDriver().close();
    		    u.getDriver().switchTo().window(tabs.get(1));
    		    u.highLight(By.xpath("//html"));
    		    
    		  
//    		    if (i==3) {
//    		    	  break;
//				}
   		  
    			u.getDriver().switchTo().frame(2);
    		    
    		    
    		}
    		
     sheet.autoSizeColumn(0);
     sheet.autoSizeColumn(1);
     sheet.autoSizeColumn(2);
     sheet.autoSizeColumn(3);
     sheet.autoSizeColumn(4);
     sheet.autoSizeColumn(5);
     sheet.autoSizeColumn(6);
     sheet.autoSizeColumn(7);
     sheet.autoSizeColumn(8);
     sheet.autoSizeColumn(9);
     sheet.autoSizeColumn(10);
     sheet.autoSizeColumn(11);
     sheet.autoSizeColumn(12);
     sheet.autoSizeColumn(13);
     sheet.autoSizeColumn(14);
     sheet.autoSizeColumn(15);
     sheet.autoSizeColumn(16);
     sheet.autoSizeColumn(17);
     sheet.autoSizeColumn(18);
     
    wb.write(fileOut);
     System.out.println("Sheets Has been Created successfully"); 
    		
    		return details;
    	 
    	 
	}
	
	
	


public HashMap<String, String> getDetails(int rowNum) {

	HashMap<String, String> details = new HashMap<String, String>();
		
	
	//adapterconnection
	
		String adapterConn = u.getText(adapterConnectionBy).split(":")[1];
		String connectionType = u.getText(connectionTypeBy);
	    String conPackage    = u.getText(conPackageBy);
	    
	//ConnectionPropertie
	    
	    String transactionTypeValue = u.getText(transactionTypeValueBy);
	    String dataSourceValue      =u.getText(dataBaseValueBy);
	    String serverNameValue = u.getText(serverNameValueBy);
	    String userValue = u.getText(userValueBy);
	    String dataBaseValue = u.getText(dataBaseValueBy); 
	    String portNumberValue = u.getText(portNumberValueBy);
	    String networkProtocolValue = u.getText(networkProtocolValueBy);
	    String otherProportiesValue = u.getText(otherProportiesValueBy); 
	    
	//Connection Managment
	    
	    String EnableConnectionValue = u.getText(EnableConnectionValueBy);
	    String minpoolSizeValue  = u.getText(minpoolSizeValueBy);
	    String maxpoolSizeValue = u.getText(maxpoolSizeValueBy);
	    String poolIncrementSizeValue = u.getText(poolIncrementSizeValueBy);
	    String blockTimeOutValue = u.getText(blockTimeOutValueBy);
	    String expireTimeValue = u.getText(expireTimeValueBy);
	    String startUpValue = u.getText(startUpValueBy);
	    String startUpBackValue = u.getText(startUpBackValueBy);
	    
	    
//	    System.out.println(connectionType);
//	    System.out.println(conPackage);
//	    System.out.println(serverNameValue);
//	    System.out.println(userValue);
//	    System.out.println(dataBaseValue);
//	    System.out.println(otherProportiesValue);
	
	//adapter connections 
		
	    details.put("adapterConnections",adapterConn);
	    details.put("conectio", connectionType);
	    details.put("paquete", conPackage);
	//Properties
	    details.put("TransactionType", transactionTypeValue);
	    details.put("dataSource", dataSourceValue);
	    details.put("server", serverNameValue);
		details.put("user", userValue);
		details.put("dataBase", dataBaseValue);
		details.put("PortNumber", portNumberValue);
		details.put("networkProtocol", networkProtocolValue);
		details.put("other properties", otherProportiesValue);
		
	//ConnectionManagment 
		
		details.put("EnableConnection", EnableConnectionValue);
		details.put("minpoolSize", minpoolSizeValue);
		details.put("maxpoolSize", maxpoolSizeValue);
		details.put("poolIncrementSize", poolIncrementSizeValue);
		details.put("blockTimeOut", blockTimeOutValue);
		details.put("expireTime", expireTimeValue);
		details.put("startUpValue", startUpValue);
		details.put("startUpBack", startUpBackValue);
		
		
		testCase.addTextEvidenceCurrentStep(connectionType);
		testCase.addTextEvidenceCurrentStep(conPackage);
		testCase.addTextEvidenceCurrentStep(serverNameValue);
		testCase.addTextEvidenceCurrentStep(userValue);
		testCase.addTextEvidenceCurrentStep(dataBaseValue);
		testCase.addTextEvidenceCurrentStep(otherProportiesValue);
		
	//adapterconnection	
		
			 Row row = sheet.createRow(rowNum);
			 Cell cell = row.createCell(0);
			 cell.setCellValue(adapterConn);	
	     
		     Cell cell1 = row.createCell(1);
		     cell1.setCellValue(connectionType);
		
		     Cell cell2 = row.createCell(2);
		     cell2.setCellValue(conPackage);
	//Connection Properties
		     
		     
		     Cell cell3 = row.createCell(3);
		     cell3.setCellValue(transactionTypeValue);
		     
		     Cell cell4 = row.createCell(4);
		     cell4.setCellValue(dataSourceValue);
		     
		     Cell cell5 = row.createCell(5);
		     cell5.setCellValue(serverNameValue);
		     
		     Cell cell6 = row.createCell(6);
		     cell6.setCellValue(userValue);
		     
		     Cell cell7 = row.createCell(7);
		     cell7.setCellValue(dataBaseValue);
		     
		     Cell cell8 = row.createCell(8);
		     cell8.setCellValue(portNumberValue);
		     
		     Cell cell9 = row.createCell(9);
		     cell9.setCellValue(networkProtocolValue);
		     
		     Cell cell10 = row.createCell(10);
		     cell10.setCellValue(otherProportiesValue);
		     
	//ConnectionMangment	     
		     Cell cell11 = row.createCell(11);
		     cell11.setCellValue(EnableConnectionValue);
		     
		     Cell cell12 = row.createCell(12);
		     cell12.setCellValue(minpoolSizeValue);
		     
		     Cell cell13 = row.createCell(13);
		     cell13.setCellValue(maxpoolSizeValue);
		     
		     Cell cell14 = row.createCell(14);
		     cell14.setCellValue(poolIncrementSizeValue);
		     
		     Cell cell15 = row.createCell(15);
		     cell15.setCellValue(blockTimeOutValue);
		     
		     Cell cell16 = row.createCell(16);
		     cell16.setCellValue(expireTimeValue);
		     
		     Cell cell17 = row.createCell(17);
		     cell17.setCellValue(startUpBackValue);
		     
		     Cell cell18 = row.createCell(18);
		     cell18.setCellValue(startUpBackValue);
		     
		     
		return details;
	}
	




}
