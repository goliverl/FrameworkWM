package integrationServer.om;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.docx4j.wml.U;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ByXPath;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;
import com.graphbuilder.curve.Point;

import modelo.TestCase;
import utils.selenium.SeleniumUtil;

public class PakageManagment {

	// utilerias
	SeleniumUtil u;
	TestCase testCase;
	
	/** software
	 * Product webMethods Integration Server Version 9.9.0.0 Updates TNS_9.9_Fix5
	 * IS_9.9_SPM_Fix4 IS_9.9_Core_Fix13
	 */
	
	By packageOption = By.xpath("//*[@id=\"Packages_twistie\"]");
	By settingsOption = By.xpath("//*[@class=\"menusection-Settings\"]");
	By managment = By.xpath("//*[@id=\"ipackage-list.dsp\"]");
	By messaging = By.xpath("//*[@id=\"asettings-messaging.dsp\"]");
	By jmsTriggerManagement = By.xpath("//a[text()='JMS Trigger Management']");
	By browseServices = By.xpath("/html/body/table/tbody/tr[2]/td/ul/li[2]/a");
	By runService = By.xpath("/html/body/table/tbody/tr[2]/td/ul/li[2]/a");
	By whInpunts = By.xpath("/html/body/table/tbody/tr[3]/td[2]/table/tbody/tr[2]/td/input[1]");
	By whOutInputs = By.xpath("/html/body/table/tbody/tr[3]/td[2]/table/tbody/tr[2]/td/input[2]");
	By whOutInputs2 = By.xpath("//input[@class='button2'][@value='Test (without inputs)']"); //po2 
	By globalVariables = By.xpath("//a[@id=\"asettings-global-variables.dsp\"]");
	
	/** software
	 * Product webMethods Integration Server Version 10.5.0.0 Updates
	 * IS_10.5_Core_Fix6 Trading Networks Server 10.5 Fix 6 IS_10.5_SPM_Fix6
	 */
	
	By packageOption10 = By.xpath("//*[@id=\"elmt_Packages_subMenu\"]/a");
	By management10 = By.xpath("//*[@id=\"apackage-list.dsp\"]");
	By browseServices10 = By.xpath("/html/body/table/tbody/tr[2]/td/ul/li[2]/a");
	By runService10 = By.xpath("/html/body/table/tbody/tr[2]/td/ul/li[2]/a");
	By whOutInputs10 = By.xpath("//input[@class='button2'][@value='Test (without inputs)']");

	// -----Adapters

	By adapterOption = By.xpath("//*[@id='Adapters_twistie']");
	By nextpage = By.xpath("//*[@id='connection_page_no_next']");
	By forJDBC = By.xpath("//*[text()=' webMethods Adapter for JDBC  ...  ']");
	String adapterName = "//tr/td[text()='AdapterConnections:%s']";
	String adapterEnable = "//tr/td[text()='AdapterConnections:%s']/../td[4]/a";
	By x = By.xpath("//*[@id='view_connection10']");


	

	By adapterConnectionBy = By.xpath("/html/body/form/table[1]/tbody/tr[3]/td");
	By connectionTypeBy = By.xpath("/html/body/form/table[1]/tbody/tr[4]/td[2]");
	By conPackageBy = By.xpath("/html/body/form/table[1]/tbody/tr[5]/td[2]");

	//By xmlInput = By.xpath("//input[@name='%s']");
	By withInputButton = By.xpath("//input[@value='Test (with inputs)']");
	By withoutInputButton = By.xpath("//input[@value='Test (without inputs)']");
	
	By withoutInputButton10 = By.xpath("//input[@class =\"button2\"][@value = \"Test (without inputs)\"]");
	
	

	// vaiable que recibira el parametro
	String interfaceNameBy = "//a[text()='%s']";
	String selectServices = "//a[text()='%s']";
	String input = "//input[@name='%s']";
	String UUID = "//input[@name='UUID']";
	String Folio = "//input[@name='Folio']";
	String Serie = "//input[@name='Serie']";
	String StringXML = "//input[@name='StringXML']";
	String Reintento = "//input[@name='Reintento']";
	String RFCEmisor = "//input[@name='RFCEmisor']";
	String RFCReceptor = "//input[@name='RFCReceptor']";
	String adapterElement = "//td[contains(text(),'AdapterConnections:%s')]/following-sibling::td[3]/a";
	String nextPage = "//*[@id='paginationContainer']/a[5]";
	String triggerEnabled = "//table[@id=\"JMS_TABLE\"]/tbody/tr[td//text()[contains(., '%s')]]";
	

	// constructor
	public PakageManagment(SeleniumUtil u, TestCase testCase) {
		super();
		this.u = u;
		this.testCase = testCase;

	}
	
	public void clickpackage() {
		
		
		u.getDriver().switchTo().frame(0);
//		u.highLight(By.xpath("//html"));
		u.getDriver().switchTo().frame(0);
//		u.highLight(By.xpath("//html"));
		u.getDriver().switchTo().frame(1);
//		u.highLight(By.xpath("//html"));
				
		u.click(packageOption10);
//		u.highLight(packageOption10);
		u.click(management10);
		u.getDriver().switchTo().parentFrame();
		// te swicheas al deseado
		u.getDriver().switchTo().frame(2);
		testCase.addScreenShotCurrentStep(u, "package list");
	}
	
public void clickpackage2() {
		u.getDriver().switchTo().frame(0);
		u.getDriver().switchTo().frame(1);
		u.click(packageOption10);
		u.click(management10);
		u.getDriver().switchTo().parentFrame();
		// te swicheas al deseado
		u.getDriver().switchTo().frame(2);
		testCase.addScreenShotCurrentStep(u, "package list");
	}
	
	
	public String runIntefaceWmOneButton10(String interfaceWM, String service) {

		clickpackage();
		u.hardWait(2);
		selectInterface(interfaceWM);
		u.hardWait(2);
		clickBrowserService();
		u.hardWait(2);
		selectService(service);
		u.hardWait(2);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);

		clickSelectOneButton();
		return dateExecution;

	}
	
	public String runIntefaceWmTwoButtonsWihtoutInputs10(String interfaceWM, String service) {

		clickpackage();
		u.hardWait(2);
		selectInterface(interfaceWM);
		u.hardWait(2);
		clickBrowserService();
		u.hardWait(2);
		selectService(service);
		u.hardWait(2);
		clickTestRun();
		u.hardWait(2);
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);
		clickSelectTwoButton10();
		
		return dateExecution;

	}
	
	public String runIntefaceWmTwoButtonsWihtoutInputs10_2(String interfaceWM, String service) {

		clickpackage2();
		selectInterface(interfaceWM);
		clickBrowserService();
		selectService(service);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);

		clickSelectTwoButton10();
		return dateExecution;

	}
	
	public String GetText(String Xpath) {
		
		String text = u.getText(By.xpath(Xpath));
		testCase.addScreenShotCurrentStep(u, "interfaz");
		return text;
	}
	
	public String runIntefaceWmOneButtonPR6(String interfaceWM, String service) {

		clickpackage();
		selectInterface(interfaceWM);
		clickBrowserService();
		selectService(service);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);

		clickSelectOneButtonPR6();
		return dateExecution;

	}
	
	public String runIntefaceWmWithInput10(String interfaceWM, String service, String inputs,String name) {

		//El parametro Name  nos servidara como referencia al elemento del input
		
		clickpackage();
		selectInterface(interfaceWM);
		clickBrowserService();
		selectService(service);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);
		u.hardWait(2);
		clickSelectInput(inputs,name);

		return dateExecution;

	}
	
	
	public String runIntefaceWmWithTresInputs10(String interfaceWM, String service, String inputs,String name, String inputs2,String name2, String inputs3,String name3) { //3 campos esteeee

		//El parametro Name  nos servidara como referencia al elemento del input
		
		clickpackage();
		selectInterface(interfaceWM);
		clickBrowserService();
		selectService(service);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);
		u.hardWait(2);
		clickSelectTresInput(inputs,name, inputs2,name2, inputs3,name3);

		return dateExecution;

	}
	
	

	public void clickManagment() {
		u.getDriver().switchTo().frame(0);
		u.getDriver().switchTo().frame(0);
		u.getDriver().switchTo().frame(1);

		u.click(packageOption);
		u.click(managment);
		testCase.addScreenShotCurrentStep(u, "IS");

	}
	
	
	/*
	 * Nuevo metodo para ingresar a Managment actualmente
	 */
	
	public void clickManagment2() {
		u.getDriver().switchTo().frame(0);
		u.getDriver().switchTo().frame(1);

		u.click(packageOption);
		u.click(managment);
		testCase.addScreenShotCurrentStep(u, "IS");

	}
	
	public String getIpSwitch(String variable, boolean resumen ) {
		WebDriverWait wait = new WebDriverWait(u.getDriver(), 10);

		u.getDriver().switchTo().frame(0);
		u.getDriver().switchTo().frame(1);
		u.click(settingsOption);
		if (resumen) {
			testCase.addScreenShotCurrentStep(u, "IS");
		}
		u.click(globalVariables);
		u.getDriver().switchTo().parentFrame();
		u.getDriver().switchTo().frame(2);
		boolean exist;
		int row_num, col_num, verif;
		boolean getvalue = false;
		String value = "";
		try {
				WebElement encontrarSwitch = u.getDriver().findElement(By.xpath("//a[contains(text(),'"+variable+"')]"));
				List <WebElement> tabla=u.getDriver().findElements(By.xpath("//table[@class='tableView']/tbody/tr"));	
				System.out.print(tabla.toString());
				System.out.println("NUMBER OF ROWS IN THIS TABLE = "+tabla.size());
				exist=true;

		        verif=0;
		        row_num=1;
		        for(WebElement trElement : tabla)
		        {
		            List<WebElement> td_collection=trElement.findElements(By.xpath("td"));
		            System.out.println("NUMBER OF COLUMNS="+td_collection.size());
		            col_num=1;
		            for(WebElement tdElement : td_collection)
		            {
		                System.out.println("row # "+row_num+", col # "+col_num+ "text="+tdElement.getText());
		                if(tdElement.getText().equalsIgnoreCase(variable)) {
		                	getvalue=true;
		                }
		                if(getvalue==true&&col_num==2) {
		                	value=tdElement.getText();
		                	getvalue=false;
		                }
		                col_num++;
		            }
		            row_num++;
		        } 

		} catch (Exception e) {
			// TODO: handle exception
			exist=false;
		}
		System.out.println(exist);
		if (resumen) {
			testCase.addScreenShotCurrentStep(u, "IS");
		}
		return value;
	}
	

	public void selectInterface(String interfaceWM) {

		String interF = String.format(interfaceNameBy, interfaceWM);
//		u.highLight(By.xpath("/html/body"));
		// te regresas al frame padre para swichiarte
		WebDriverWait wait = new WebDriverWait(u.getDriver(), 5);

		u.getDriver().switchTo().parentFrame();
		// te swicheas al deseado
		u.getDriver().switchTo().frame(2);

//		WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(interF)));
//		u.highLight(By.xpath(interF));

		u.scrollToElement(By.xpath(interF));
		u.highLight(By.xpath(interF));
		testCase.addScreenShotCurrentStep(u, "selectionIter");
//        button.click();
		u.click(By.xpath(interF));

		//testCase.addScreenShotCurrentStep(u, "selectionIter");

	}
	
	public void CXP(String interfaceWM) {

		String interF = String.format(interfaceNameBy, interfaceWM);
//		u.highLight(By.xpath("/html/body"));
		// te regresas al frame padre para swichiarte
		WebDriverWait wait = new WebDriverWait(u.getDriver(), 5);

		u.getDriver().switchTo().parentFrame();
		// te swicheas al deseado
		u.getDriver().switchTo().frame(2);
		u.scroll(2000);
		u.highLight(By.xpath(interF));
		testCase.addScreenShotCurrentStep(u, "interfaz");
	}
	
	public void CXPclic(String interfaceWM) {
		String interF = String.format(interfaceNameBy, interfaceWM);
		u.click(By.xpath(interF));

		testCase.addScreenShotCurrentStep(u, "selectionIter");
	}
	

	public void clickBrowserService() {

		u.highLight(browseServices);
		testCase.addScreenShotCurrentStep(u, "buscarServicio");
		u.click(browseServices);

	}

	public void selectService(String nameService) {

		String browserService = String.format(selectServices, nameService);
		u.scrollToElement(By.xpath(browserService));
		u.highLight(By.xpath(browserService));
		testCase.addScreenShotCurrentStep(u, "servicio1");
		u.click(By.xpath(browserService));

		//testCase.addScreenShotCurrentStep(u, "servicio2");

	}

	public void clickTestRun() {

		u.highLight(runService);
		testCase.addScreenShotCurrentStep(u, "runServicio");
		u.click(runService);

	}

	public void clickSelectNoInput() {

		u.highLight(whOutInputs);
		u.click(whOutInputs);
		u.hardWait(8);
		testCase.addScreenShotCurrentStep(u, "fin");
		u.close();

	}
	
	public String clickSelectNoInputxml() {

		u.highLight(whOutInputs);
		u.click(whOutInputs);
		u.hardWait(8);
		String wmCode = u.getText(By.xpath("/html/body/table/tbody/tr/td[2]"));
		testCase.addScreenShotCurrentStep(u, "fin");
		u.close();
		
		return wmCode;

	}
	
	public void clickSelectNoInput2() {

		u.highLight(whOutInputs2);
		u.click(whOutInputs2);
		u.hardWait(8);
		testCase.addScreenShotCurrentStep(u, "fin");
		u.close();

	}

	public void clickSelectNoInputAdapter() {

		u.click(whOutInputs);
		u.hardWait(8);
		testCase.addScreenShotCurrentStep(u, "fin");

	}

	public void clickSelectOneButton() {
		//u.highLight(withoutInputButton);
		u.click(withoutInputButton);
		u.hardWait(8);
		testCase.addScreenShotCurrentStep(u, "fin2");
		u.close();
	}
	
	public void clickSelectTwoButton10() {
		
		//u.highLight(whOutInputs10);
		testCase.addScreenShotCurrentStep(u, "fin1");
		u.click(whOutInputs10);
		u.hardWait(8);
		testCase.addScreenShotCurrentStep(u, "fin2");
	//	u.close();
	}
	
	public void clickSelectOneButtonPR6() {
		u.highLight(withoutInputButton);
		//u.click(withoutInputButton);
		u.hardWait(8);
		testCase.addScreenShotCurrentStep(u, "fin2");
		u.close();
	}

	public void clickSelectOneButtonPe1() {
		u.highLight(withoutInputButton);
		u.click(withoutInputButton);
			}


	public void clickSelectInput(String dato,String name) {

		String formatInput = String.format(input, name);
		By xpathInput = By.xpath(formatInput);
		u.highLight(xpathInput);
		u.sendKeys(xpathInput,dato);
		u.highLight(withInputButton);
		testCase.addScreenShotCurrentStep(u, "inputs");
		u.click(withInputButton);
		u.hardWait(8);
		testCase.addScreenShotCurrentStep(u, "fin");
//		u.close();
	}
	
	public String clickSelectInputxml(String dato,String name) {

		String formatInput = String.format(input, name);
		By xpathInput = By.xpath(formatInput);
		u.highLight(xpathInput);
		u.sendKeys(xpathInput,dato);
		u.highLight(withInputButton);
		testCase.addScreenShotCurrentStep(u, "inputs");
		u.click(withInputButton);
		u.hardWait(8);
		
	    String wmCode = u.getText(By.xpath("/html/body/table/tbody/tr/td[2]"));
		testCase.addScreenShotCurrentStep(u, "fin");
//		u.close();
		
		return wmCode;
	}
	
	
	public void clickSelectDosInput(String dato,String name,String dato2,String name2) {

		String formatInput = String.format(input, name);
		String formatInput2 = String.format(input, name2);
		By xpathInput = By.xpath(formatInput);
		By xpathInput2 = By.xpath(formatInput2);
		u.highLight(xpathInput);
		u.sendKeys(xpathInput,dato);
		
		u.highLight(xpathInput2);
		u.sendKeys(xpathInput2,dato2);
		u.highLight(withInputButton);
		testCase.addScreenShotCurrentStep(u, "inputs");
		u.click(withInputButton);
		u.hardWait(8);
		testCase.addScreenShotCurrentStep(u, "fin");
		u.close();
	}
	
	public void clickSelectTresInput(String dato,String name,String dato2,String name2, String dato3,String name3) {

		String formatInput = String.format(input, name);
		String formatInput2 = String.format(input, name2);
		String formatInput3 = String.format(input, name3);
		By xpathInput = By.xpath(formatInput);
		By xpathInput2 = By.xpath(formatInput2);
		By xpathInput3 = By.xpath(formatInput3);
		u.highLight(xpathInput);
		u.sendKeys(xpathInput,dato);
		
		u.highLight(xpathInput2);
		u.sendKeys(xpathInput2,dato2);
		
		u.highLight(xpathInput3);
		u.sendKeys(xpathInput3,dato3);
		
		u.highLight(withInputButton);
		testCase.addScreenShotCurrentStep(u, "inputs");
		u.click(withInputButton);
		u.hardWait(8);
		testCase.addScreenShotCurrentStep(u, "fin");
		u.close();
	}
	
	public String clickSelectCuatroInputxml(String dato,String name,String dato2,String name2, String dato3,String name3,String dato4 , String name4) {

		String formatInput = String.format(input, name);
		String formatInput2 = String.format(input, name2);
		String formatInput3 = String.format(input, name3);
		String formatInput4 = String.format(input, name4);
		By xpathInput = By.xpath(formatInput);
		By xpathInput2 = By.xpath(formatInput2);
		By xpathInput3 = By.xpath(formatInput3);
		By xpathInput4 = By.xpath(formatInput4);
		u.highLight(xpathInput);
		u.sendKeys(xpathInput,dato);
		
		u.highLight(xpathInput2);
		u.sendKeys(xpathInput2,dato2);
		
		u.highLight(xpathInput3);
		u.sendKeys(xpathInput3,dato3);
		
		u.highLight(xpathInput4);
		u.sendKeys(xpathInput4,dato4);
		
		u.highLight(withInputButton);
		testCase.addScreenShotCurrentStep(u, "inputs");
		u.click(withInputButton);
		u.hardWait(8);
	    String wmCode = u.getText(By.xpath("/html/body/table/tbody/tr/td[2]"));
	   // System.out.println(wmCode);
		testCase.addScreenShotCurrentStep(u, "fin");
		u.close();
		
		return wmCode;
	}

	public void clickSelectInputs(String uuid,String folio,String serie,String stringxml,String reintento, String rfcemisor, String rfcreceptor) {

		u.highLight(By.xpath(UUID));
		u.sendKeys(By.xpath(UUID),uuid);
		u.hardWait(5);
		u.highLight(By.xpath(Folio));
		u.sendKeys(By.xpath(Folio),folio);
		u.highLight(By.xpath(Serie));
		u.sendKeys(By.xpath(Serie),serie);
		u.highLight(By.xpath(StringXML));
		u.sendKeys(By.xpath(StringXML),stringxml);
		u.highLight(By.xpath(Reintento));
		u.sendKeys(By.xpath(Reintento),reintento);
		u.highLight(By.xpath(RFCEmisor));
		u.sendKeys(By.xpath(RFCEmisor),rfcemisor);
		u.highLight(By.xpath(RFCReceptor));
		u.sendKeys(By.xpath(RFCReceptor),rfcreceptor);
		u.highLight(withInputButton);
		testCase.addScreenShotCurrentStep(u, "inputs");
		u.click(withInputButton);
		u.hardWait(8);
		testCase.addScreenShotCurrentStep(u, "fin");
		//u.close();
	
		
	}

	public void clickAdapter() {

		// u.highLight(By.xpath("//html"));
		u.getDriver().switchTo().frame(0);
		u.getDriver().switchTo().frame(1);
//		u.highLight(By.xpath("//html"));

		u.click(adapterOption);
		u.click(forJDBC);

		ArrayList<String> tabs = new ArrayList(u.getDriver().getWindowHandles());
		// System.out.println(tabs.size());
		u.getDriver().switchTo().window(tabs.get(1));

	}

//	public void fluentWait(WebDriver driver, final By locator) {
//		Wait<WebDriver> wait = new FluentWait<WebDriver>(driver).withTimeout(60, TimeUnit.SECONDS)
//				.pollingEvery(2, TimeUnit.SECONDS).ignoring(NoSuchElementException.class);
//
//		wait.until(new Function<WebDriver, WebElement>() {
//			public WebElement apply(WebDriver driver) {
//				return driver.findElement(locator);
//			}
//		});
//	}

	public static void scrollToElement(WebDriver driver, WebElement element) {
		JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
		javascriptExecutor.executeScript("arguments[0].scrollIntoView(true);", element);
	}

	public boolean switchAdapter(String adapter) {
		WebDriverWait wait = new WebDriverWait(u.getDriver(), 10);
		boolean segundaPagina = false;

		// clic apartado de adapters
		testCase.addScreenShotCurrentStep(u, "inicio");
		clickAdapter();
		// testCase.addScreenShotCurrentStep(u, "selectAdapter");
		u.waitForLoadPage();
		// busca el frame donde se encuentran los adapters
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("body"));
		String xPath = String.format(adapterElement, adapter);
		// busca el adapter indicado
		try {

			WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
			scrollToElement(u.getDriver(), button);
			u.highLight(By.xpath(xPath));
			testCase.addScreenShotCurrentStep(u, "click");
			button.click();

		} catch (TimeoutException e) {
			u.click(By.xpath(nextPage));
			WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
			scrollToElement(u.getDriver(), button);
			u.highLight(By.xpath(xPath));
			testCase.addScreenShotCurrentStep(u, "click");
			button.click();
			segundaPagina = true;
		}

		wait.until(ExpectedConditions.alertIsPresent());
		u.hardWait(2);
		u.getDriver().switchTo().alert().accept();

		testCase.addScreenShotCurrentStep(u, "ok");
		u.waitForLoadPage();
		u.getDriver().navigate().refresh();
		u.waitForLoadPage();
		u.hardWait(2);

		u.getDriver().switchTo().frame(2);

		if (segundaPagina) {
			u.click(By.xpath(nextPage));
			u.waitForLoadPage();
			u.hardWait(2);
		}

		WebElement button2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
		scrollToElement(u.getDriver(), button2);
		u.highLight(By.xpath(xPath));
		boolean isOff = button2.getText().equals("No");
		testCase.addScreenShotCurrentStep(u, "off");
		System.out.print("Adapter is off? " + isOff);
		return isOff;

	}

	public boolean  adapterOn(String adapter) {
		WebDriverWait wait = new WebDriverWait(u.getDriver(), 10);
		boolean segundaPagina = false;

		u.getDriver().switchTo().defaultContent();

		u.getDriver().switchTo().frame(0);
		u.getDriver().switchTo().frame(1);

		u.click(forJDBC);

		ArrayList<String> tabs = new ArrayList<String>(u.getDriver().getWindowHandles());
		u.getDriver().switchTo().window(tabs.get(1));

		u.waitForLoadPage();
		// busca el frame donde se encuentran los adapters
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("body"));
		String xPath = String.format(adapterElement, adapter);
		// busca el adapter indicado
		try {

			WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
			scrollToElement(u.getDriver(), button);
			u.highLight(By.xpath(xPath));

			button.click();

		} catch (TimeoutException e) {
			u.highLight(By.xpath("//html"));
			u.click(By.xpath(nextPage));
			WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
			scrollToElement(u.getDriver(), button);
			u.highLight(By.xpath(xPath));

			button.click();
			segundaPagina = true;
		}

		wait.until(ExpectedConditions.alertIsPresent());
		u.hardWait(2);
		u.getDriver().switchTo().alert().accept();

		u.waitForLoadPage();
		u.getDriver().navigate().refresh();
		u.waitForLoadPage();
		u.hardWait(2);

		u.getDriver().switchTo().frame(2);

		if (segundaPagina) {
			u.click(By.xpath(nextPage));
			u.waitForLoadPage();
			u.hardWait(2);
		}

		WebElement button2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
		scrollToElement(u.getDriver(), button2);
		u.highLight(By.xpath(xPath));
		boolean isOff = button2.getText().equals("No");

		System.out.print("Adapter is off? " + isOff);

		return isOff;
	}

	public String runIntefaceWM(String interfaceWM, String service, String inputs) {

		clickManagment();
		selectInterface(interfaceWM);
		clickBrowserService();
		selectService(service);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);
		u.hardWait(2);
		clickSelectNoInput();

		return dateExecution;

	}
	
	
	public String runIntefaceWM2(String interfaceWM, String service, String inputs) {

		clickManagment();
		selectInterface(interfaceWM);
		clickBrowserService();
		selectService(service);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);
		u.hardWait(2);
		clickSelectNoInput2();

		return dateExecution;

	}

	public String runIntefaceWmWithInput(String interfaceWM, String service, String inputs,String name) {

		//El parametro Name  nos servidara como referencia al elemento del input
		
		clickManagment();
		selectInterface(interfaceWM);
		clickBrowserService();
		selectService(service);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);
		u.hardWait(2);
		clickSelectInput(inputs,name);

		return dateExecution;

	}
	
	public String runIntefaceWmWithDosInput(String interfaceWM, String service, String inputs,String nameInput,
			String inputs2,String nameInput2) {

		//El parametro Name  nos servidara como referencia al elemento del input
		
		clickManagment();
		selectInterface(interfaceWM);
		clickBrowserService();
		selectService(service);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);
		u.hardWait(2);
		clickSelectDosInput(inputs,nameInput,inputs2,nameInput2);

		return dateExecution;

	}
	
	
	public String runIntefaceWmWithTresInput(String interfaceWM, String service, String inputs,String name,
			String inputs2,String name2, String inputs3, String name3) {

		//El parametro Name  nos servidara como referencia al elemento del input
		
		clickManagment();
		selectInterface(interfaceWM);
		clickBrowserService();
		selectService(service);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);
		u.hardWait(2);
		clickSelectTresInput(inputs,name, inputs2,name2, inputs3, name3 );

		return dateExecution;

	}

	
	
	public String runIntefaceWmWithInputsRFC(String interfaceWM, String service,String uuid,String folio,String Serie,String StringXML,String Reintento, String RFCEmisor, String RFCReceptor) {

		//El parametro Name  nos servidara como referencia al elemento del input
		
		clickManagment();
		selectInterface(interfaceWM);
		clickBrowserService();
		selectService(service);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);
		u.hardWait(2);
		clickSelectInputs(uuid,folio,Serie,StringXML,Reintento,RFCEmisor,RFCReceptor);

		return dateExecution;

	}
	
	// By adapterOption = By.xpath("//*[@id=Adapters_twistie]");

	public void writeInput(String input) {

		// String browserService = String.format(selectServices, nameService);
		// u.highLight(By.xpath(browserService));
		// u.click(By.xpath(browserService));

		testCase.addScreenShotCurrentStep(u, "servicio");

	}

	public String runIntefaceWmAdapter(String interfaceWM, String service, String inputs) {

		clickManagment();
		selectInterface(interfaceWM);
		clickBrowserService();
		selectService(service);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);
		u.hardWait(2);
		clickSelectNoInputAdapter();

		return dateExecution;

	}

	public String runIntefaceWmOneButton(String interfaceWM, String service) {

		clickManagment2();
		selectInterface(interfaceWM);
		clickBrowserService();
		selectService(service);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);
	/*	if(service.equals("PE1.Pub:runReveseManager")) {
			clickSelectOneButtonPe1();
		}else {
		clickSelectOneButton();
		}*/
		clickSelectOneButton();
		return dateExecution;

	}

	public boolean poolSwitchAdapter(String adapter1, String adapter2, String adapter3) {
		boolean adp1 = false, adp2 = false, adp3 = false;
		WebDriverWait wait = new WebDriverWait(u.getDriver(), 10);
		boolean segundaPagina = false;
		boolean isOff = false;

		String xPath;
		WebElement button2;
		// clic apartado de adapters
		testCase.addScreenShotCurrentStep(u, "inicio");
		clickAdapter();
		// testCase.addScreenShotCurrentStep(u, "selectAdapter");
		u.waitForLoadPage();

		// busca el frame donde se encuentran los adapters
		wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("body"));
		xPath = String.format(adapterElement, adapter1);
		// busca el adapter indicado
		try {

			WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
			scrollToElement(u.getDriver(), button);
			u.highLight(By.xpath(xPath));
			testCase.addScreenShotCurrentStep(u, "click");
			button.click();

		} catch (TimeoutException e) {
			u.click(By.xpath(nextPage));
			WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
			scrollToElement(u.getDriver(), button);
			u.highLight(By.xpath(xPath));
			testCase.addScreenShotCurrentStep(u, "click");
			button.click();
			segundaPagina = true;
		}

		wait.until(ExpectedConditions.alertIsPresent());
		u.hardWait(2);
		u.getDriver().switchTo().alert().accept();

		testCase.addScreenShotCurrentStep(u, "ok");
		u.waitForLoadPage();
		u.getDriver().navigate().refresh();
		u.waitForLoadPage();
		u.hardWait(2);

		u.getDriver().switchTo().frame(2);

		if (segundaPagina) {
			u.click(By.xpath(nextPage));
			u.waitForLoadPage();
			u.hardWait(2);
		}

		button2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
		scrollToElement(u.getDriver(), button2);
		u.highLight(By.xpath(xPath));
		adp1 = button2.getText().equals("No");
		testCase.addScreenShotCurrentStep(u, "off");
		System.out.print("Adapter is off? " + isOff);

		u.getDriver().navigate().refresh();
		u.hardWait(2);

//adp2

		// busca el frame donde se encuentran los adapters
//  	wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("body"));
		u.getDriver().switchTo().frame(2);
		xPath = String.format(adapterElement, adapter2);
		// busca el adapter indicado
		try {

			WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
			scrollToElement(u.getDriver(), button);
			u.highLight(By.xpath(xPath));
			testCase.addScreenShotCurrentStep(u, "clickAdp2");
			button.click();

		} catch (TimeoutException e) {
			u.click(By.xpath(nextPage));
			WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
			scrollToElement(u.getDriver(), button);
			u.highLight(By.xpath(xPath));
			testCase.addScreenShotCurrentStep(u, "clickAdp2");
			button.click();
			segundaPagina = true;
		}

		wait.until(ExpectedConditions.alertIsPresent());
		u.hardWait(2);
		u.getDriver().switchTo().alert().accept();

		testCase.addScreenShotCurrentStep(u, "ok");
		u.waitForLoadPage();
		u.getDriver().navigate().refresh();
		u.waitForLoadPage();
		u.hardWait(2);

		u.getDriver().switchTo().frame(2);

		if (segundaPagina) {
			u.click(By.xpath(nextPage));
			u.waitForLoadPage();
			u.hardWait(2);
		}

		button2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
		scrollToElement(u.getDriver(), button2);
		u.highLight(By.xpath(xPath));
		adp2 = button2.getText().equals("No");
		testCase.addScreenShotCurrentStep(u, "off");
		System.out.print("Adapter is off? " + isOff);

		u.getDriver().navigate().refresh();
		u.hardWait(2);

//adp3

		// busca el frame donde se encuentran los adapters
//  	wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("body"));
		u.getDriver().switchTo().frame(2);
		xPath = String.format(adapterElement, adapter3);
		// busca el adapter indicado
		try {

			WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
			scrollToElement(u.getDriver(), button);
			u.highLight(By.xpath(xPath));
			testCase.addScreenShotCurrentStep(u, "click");
			button.click();

		} catch (TimeoutException e) {
			u.click(By.xpath(nextPage));
			WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
			scrollToElement(u.getDriver(), button);
			u.highLight(By.xpath(xPath));
			testCase.addScreenShotCurrentStep(u, "click");
			button.click();
			segundaPagina = true;
		}

		wait.until(ExpectedConditions.alertIsPresent());
		u.hardWait(2);
		u.getDriver().switchTo().alert().accept();

		testCase.addScreenShotCurrentStep(u, "ok");
		u.waitForLoadPage();
		u.getDriver().navigate().refresh();
		u.waitForLoadPage();
		u.hardWait(2);

		u.getDriver().switchTo().frame(2);

		if (segundaPagina) {
			u.click(By.xpath(nextPage));
			u.waitForLoadPage();
			u.hardWait(2);
		}

		button2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
		scrollToElement(u.getDriver(), button2);
		u.highLight(By.xpath(xPath));
		adp3 = button2.getText().equals("No");
		testCase.addScreenShotCurrentStep(u, "off");
		System.out.print("Adapter is off? " + isOff);

		if (adp1 == true && adp2 == true && adp3 == true) {
			isOff = true;
		}

		return isOff;

	}

	public String runIntefaceWmOneButtonNoScreenShots(String interfaceWM, String service) {
//		u.hardWait(60);
		//clickManagment();
		u.getDriver().switchTo().frame(0);
		u.getDriver().switchTo().frame(1);
		u.click(packageOption);
		u.click(managment);
		//selectInterface(interfaceWM);
		String interF = String.format(interfaceNameBy, interfaceWM);
		WebDriverWait wait = new WebDriverWait(u.getDriver(), 5);
		u.getDriver().switchTo().parentFrame();
		u.getDriver().switchTo().frame(2);
		u.highLight(By.xpath(interF));
		u.click(By.xpath(interF));
		clickBrowserService();


		//selectService(service);
		String browserService = String.format(selectServices, service);
		u.highLight(By.xpath(browserService));
		u.click(By.xpath(browserService));
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);
		//clickSelectOneButton();
		u.highLight(withoutInputButton);
		u.click(withoutInputButton);
		return dateExecution;

		 }

	public void clickSelectWithoutInput() {
		u.highLight(withoutInputButton);
		testCase.addScreenShotCurrentStep(u, "inputs");
		u.click(withInputButton);
		u.hardWait(8);
		testCase.addScreenShotCurrentStep(u, "fin");

	}

	public String runIntefaceWmTextBox(String interfaceWM, String service) {
		clickManagment();
		selectInterface(interfaceWM);
		clickBrowserService();
		selectService(service);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);
		u.hardWait(2);
		clickSelectWithoutInput();
		return dateExecution;

	}

	 //Metodo para ejecucion de Vdate
    public String runIntefaceVdate() {

 

        clickManagment();
        selectInterface("FEMSA_VDATE");
        clickBrowserService();
        selectService("WM_VDATE.Pub:run");
        clickTestRun();
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String dateExecution = ft.format(dNow);
        u.hardWait(2);
        clickSelectOneButton();

 

        return dateExecution;

 

    }
    
    
	public void adapter10(String adapter) throws InterruptedException, AWTException {

		//clickAdapter10();
		String adap = String.format(adapterName, adapter);
		System.out.println(adap);
		selectAdapter(adap);
//		clickBrowserService();
//		selectService(service);
//		clickTestRun();
//		Date dNow = new Date();
//		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
//		String dateExecution = ft.format(dNow);
//
//		clickSelectOneButton();
//		return dateExecution;

	}
	
	public void clickAdapter10() throws InterruptedException {				
		u.getDriver().switchTo().frame(0);
		u.getDriver().switchTo().frame(0);
		u.getDriver().switchTo().frame(1);
		
		u.click(adapterOption);
		
		u.click(forJDBC);
		Thread.sleep(15000);
	}
	
	public void selectAdapter() throws InterruptedException {
		
		//AQUI SE ABRE LA SECCION DE ADAPTERS, CARGA LA PAGINA Y TOMA CAPTURA PARA EL REPORTE
		Thread.sleep(15000);

		testCase.addScreenShotCurrentStep(u, "IS");

		u.getDriver().switchTo().parentFrame();
	    u.getDriver().switchTo().frame(0);
		
		WebElement searchbar = u.getDriver().findElement(By.xpath("/html/body"));
		
		//EN LO SIGUIENTE REALIZA PURO MOVIMIENTO DE TABS HASTA LLEGAR A LA POSICION DESEADA Y LE DA ENTER
		//(EN ESTE CASO QUEREMOS LLEGAR A LA SEGUNDA PAGINA ASI QUE SE POSICIONA EN EL "SIG. PAGINA" O EN EL NUMERO "2" PARA SEGUIR AVANZANDO HASTA EL ICONO DEL ADAPTER A APAGAR)
		
		for (int i=0;i<614;i++) {
			searchbar.sendKeys(Keys.TAB);      
		}
		
		searchbar.sendKeys(Keys.TAB);      
		searchbar.sendKeys(Keys.TAB); 
		searchbar.sendKeys(Keys.TAB);
		searchbar.sendKeys(Keys.TAB);      
		searchbar.sendKeys(Keys.TAB); 
		searchbar.sendKeys(Keys.TAB);
		searchbar.sendKeys(Keys.TAB);      
		searchbar.sendKeys(Keys.TAB); 
	//	searchbar.sendKeys(Keys.TAB);
		searchbar.sendKeys(Keys.ENTER);      

		//ESPERA A QUE CARGUE LA PAGINA #2 Y TOMA CAPTURA
		Thread.sleep(5000);		
		testCase.addScreenShotCurrentStep(u, "IS2");

		for (int i=0;i<35;i++) {
			searchbar.sendKeys(Keys.TAB);      
		}
		
		
		//UNA VEZ POSICIONADOS EN EL ICONO DEL ADAPTER A APAGAR SE LE DA ENTER
		//SALDRA UNA ALERTA DE JAVASCRIPT Y SE LE DA ACEPTAR PARA APAGARLO, ESPERA UN TIEMPO PARA TOMAR CAPTURA DEL ADAPTER EN OFF Y CIERRA EL BROWSER
		searchbar.sendKeys(Keys.ENTER);      		
		Thread.sleep(2000);
		u.getDriver().switchTo().alert().accept();
		Thread.sleep(2000);
		testCase.addScreenShotCurrentStep(u, "adapterOff");
		u.getDriver().close();
	}
	
	//Seleccionar adapter
	public void selectAdapterBTC() throws InterruptedException {
		
		By adapter = By.xpath("//tbody/tr/td[contains(text(),'AdapterConnections:DBS_TPEBTC_LT')]//parent::tr/td[4]/a");
		By adapterOption10 = By.xpath("//td[@id=\"elmt_Adapters_subMenu\"]");
		By forJDBC10 = By.xpath("//a[@id='aadapter-index.dsp?url=%2FWmART%2FListResources.dsp%3FadapterTypeName%3DJDBCAdapter%26dspName%3D.LISTRESOURCES&adapter=JDBCAdapter&text=webMethods+Adapter+for+JDBC&help=true']");



		u.waitForLoadPage();

		u.getDriver().switchTo().frame(0);
		u.getDriver().switchTo().frame(0);
		u.getDriver().switchTo().frame(1);

		u.click(adapterOption10);
		u.click(forJDBC10);

		u.waitForLoadPage();
		ArrayList <String> tabs = new ArrayList (u.getDriver().getWindowHandles());
	
		u.getDriver().switchTo().window(tabs.get(1));


		u.getDriver().manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		u.getDriver().switchTo().parentFrame();
		u.getDriver().switchTo().frame(2);		
		u.highLight(adapter);
		testCase.addScreenShotCurrentStep(u, "adapterOff");
		u.click(adapter);

		WebDriverWait wait = new WebDriverWait(u.getDriver(), 80);
		wait.until(ExpectedConditions.alertIsPresent());
		u.getDriver().switchTo().alert().accept();


		u.getDriver().close();
		}
	
	
	public void selectAdapter(String adapter) throws InterruptedException, AWTException {
		Thread.sleep(15000);

		testCase.addScreenShotCurrentStep(u, "IS");

		u.getDriver().switchTo().parentFrame();
	    u.getDriver().switchTo().frame(0);
		
		WebElement searchbar = u.getDriver().findElement(By.xpath("/html/body"));
		
		for (int i=0;i<614;i++) {
			searchbar.sendKeys(Keys.TAB);      
		}
		
		searchbar.sendKeys(Keys.TAB);      
		searchbar.sendKeys(Keys.TAB); 
		searchbar.sendKeys(Keys.TAB);
		searchbar.sendKeys(Keys.ENTER);      

		Thread.sleep(5000);
		
		testCase.addScreenShotCurrentStep(u, "IS2");


		for (int i=0;i<35;i++) {
			searchbar.sendKeys(Keys.TAB);      
		}
		
		searchbar.sendKeys(Keys.ENTER);      
		Thread.sleep(2000);
		testCase.addScreenShotCurrentStep(u, "alert");

		//u.getDriver().switchTo().alert().accept();
		
		Thread.sleep(2000);
		testCase.addScreenShotCurrentStep(u, "adapterOff");
		
		u.getDriver().close();

	}
 
	
	public void selectAdapter2(String adapter) throws InterruptedException {
	    System.out.println(u.getDriver().getCurrentUrl());

	  
	    
	    u.getDriver().switchTo().frame(0);
		u.getDriver().switchTo().frame(0);
		u.getDriver().switchTo().frame(2);
		u.getDriver().switchTo().frame(2);


		  
	//	   u.getDriver().close();
		   
			Thread.sleep(5000);
		    System.out.println(u.getDriver().getWindowHandle());			
	    System.out.println(u.getDriver().getCurrentUrl());
			
	//	u.click(By.xpath("/html/body"));
		
		  WebElement searchbar = u.getDriver().findElement(By.xpath("/html/body"));
          searchbar.sendKeys(Keys.DOWN);
          searchbar.sendKeys(Keys.DOWN);
          searchbar.sendKeys(Keys.DOWN);
          searchbar.sendKeys(Keys.DOWN);


		//u.getDriver().switchTo().frame(0);
		
	//	Thread.sleep(15000);

		//u.getDriver().switchTo().frame(1);
		
		//Thread.sleep(15000);

		u.getDriver().switchTo().frame(2);
		
		Thread.sleep(5000);

		//u.getDriver().switchTo().frame(3);
		 // considering that there is only one tab opened in that point.
	    String oldTab = u.getDriver().getWindowHandle();
	    ArrayList<String> newTab = new ArrayList<String>(u.getDriver().getWindowHandles());
	    newTab.remove(oldTab);
	    // change focus to new tab
	    u.getDriver().switchTo().window(newTab.get(0));

	    // Do what you want here, you are in the new tab

	    u.getDriver().close();
	    // change focus back to old tab
	    u.getDriver().switchTo().window(oldTab);
		
//		ArrayList<String> tabs = new ArrayList<String>(u.getDriver().getWindowHandles());
//		u.getDriver().switchTo().window(tabs.get(1));
//        
		Thread.sleep(15000);
		
	    u.getDriver().close();

		Thread.sleep(15000);

		//u.highLight(nextpage);
		u.click(nextpage);
		
		Thread.sleep(15000);
		//u.click(adapterName);
		//u.getDriver().switchTo().alert().accept();

		WebDriverWait wait = new WebDriverWait(u.getDriver(), 5);

		u.getDriver().switchTo().parentFrame();
		// te swicheas al deseado
		u.getDriver().switchTo().frame(2);
		testCase.addScreenShotCurrentStep(u, "selectionIter");

	}
 
	
	public String runIntefaceWmWithcuatroInputxml(String interfaceWM, String service, String inputs,String name,
			String inputs2,String name2, String inputs3, String name3 ,String inputs4,  String name4) {

		//El parametro Name  nos servidara como referencia al elemento del input
		
		clickManagment();
		selectInterface(interfaceWM);
		clickBrowserService();
		selectService(service);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);
		u.hardWait(2);
		String Wmcode = clickSelectCuatroInputxml(inputs,name, inputs2,name2, inputs3, name3,inputs4, name4);
		
	//	System.out.print(Wmcode);
		

		return Wmcode;

	}
	
	
	public String runIntefaceWmWithInputxml(String interfaceWM, String service, String inputs,String name) {

		//El parametro Name  nos servidara como referencia al elemento del input
		
		clickManagment();
		selectInterface(interfaceWM);
		clickBrowserService();
		selectService(service);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);
		u.hardWait(2);
		String wmcode = clickSelectInputxml(inputs,name);

		return wmcode;

	}
	
	public String runIntefaceWMXml(String interfaceWM, String service, String inputs) {

		clickManagment();
		selectInterface(interfaceWM);
		clickBrowserService();
		selectService(service);
		clickTestRun();
		Date dNow = new Date();
		SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		String dateExecution = ft.format(dNow);
		u.hardWait(2);
		String wmcode = clickSelectNoInputxml();

		return wmcode;

	}

	public String runInterfaceOneButton10(String string, String string2, Object object) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void validateStatusTrigger() {
		clickSettings();
		selectJmsTriggerManagement();
//		return;
	}
	
	public void clickSettings() {
//		u.getDriver().switchTo().frame(0);
		u.getDriver().switchTo().frame(0);
		u.getDriver().switchTo().frame(1);
		
		u.highLight(settingsOption).click();
		u.scrollToElement(messaging);
		u.highLight(messaging).click();
		testCase.addScreenShotCurrentStep(u, "IS");
	}
	
	public void selectJmsTriggerManagement() {
		u.getDriver().switchTo().parentFrame();
		u.getDriver().switchTo().frame(2);
		u.highLight(jmsTriggerManagement);
		testCase.addScreenShotCurrentStep(u, "TR");
	}
	
	public boolean validateTriggerEnabled (String triggerName) {
		String statusTriggerPath = String.format(triggerEnabled, triggerName);
		u.getDriver().switchTo().parentFrame();
		u.getDriver().switchTo().frame(2);
		u.click(jmsTriggerManagement);
		boolean isVisible = u.getDriver().findElements(By.xpath(statusTriggerPath)).isEmpty();
		if(!isVisible) {
			u.highLight(By.xpath(statusTriggerPath));
		}
		testCase.addScreenShotCurrentStep(u, "ST");
		System.out.println("Is Visible:" + isVisible);
		return isVisible;
	}
	


}
