package integrationServer.om;

import java.util.ArrayList;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import modelo.TestCase;
import utils.selenium.SeleniumUtil;

public class AdaptersPoolConection {
	// utilerias
		SeleniumUtil u;
		TestCase testCase;
		String adapterElementStatus = "//td[contains(text(),'AdapterConnections:%s')]/following-sibling::td[3]/a";
		String adapterElement = "//td[contains(text(),'AdapterConnections:%s')]";
		String nextPage = "//*[@id='paginationContainer']/a[5]";
		By adapterOption = By.xpath("//*[@id=\"Adapters_twistie\"]");
		By forJDBC = By.xpath("//*[text()='webMethods Adapter for JDBC...']");
		public AdaptersPoolConection(SeleniumUtil u, TestCase testCase) {
			super();
			this.u = u;
			this.testCase = testCase;

		}
		public static void scrollToElement(WebDriver driver, WebElement element) {
			JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
			javascriptExecutor.executeScript("arguments[0].scrollIntoView(true);", element);
		}

		

		public void clickAdapter() {

			// u.highLight(By.xpath("//html"));
			u.getDriver().switchTo().frame(0);
			u.getDriver().switchTo().frame(1);
//			u.highLight(By.xpath("//html"));

			u.click(adapterOption);
			u.click(forJDBC);

			ArrayList<String> tabs = new ArrayList<String>(u.getDriver().getWindowHandles());
			// System.out.println(tabs.size());
			u.getDriver().switchTo().window(tabs.get(1));

		}
		
		public void poolSwitchAdapter(boolean resumen, String...adapters) {
			boolean adp1 = false, adp2 = false, adp3 = false;
			WebDriverWait wait = new WebDriverWait(u.getDriver(), 10);
			boolean segundaPagina = false;
			boolean isOff = false;

			String xPath;
			WebElement button2;
			// clic apartado de adapters
			if(!resumen) {
			testCase.addScreenShotCurrentStep(u, "inicio");
			}
			clickAdapter();
			u.waitForLoadPage();
			wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("body"));
		
	            for (int i = 0; i < adapters.length; i++) {   
	            	try {
	            	String adapter=adapters[i];
	            	// busca el frame donde se encuentran los adapters
	    			xPath = String.format(adapterElementStatus, adapter);
	    			// busca el adapter indicado
	    			testCase.addBoldTextEvidenceCurrentStep("Encontrar el adapter '"+adapter+"'.");
	    			try {

	    				WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
	    				scrollToElement(u.getDriver(), button);
	    				u.highLight(By.xpath(xPath));
	    				if(!resumen) {
	    				testCase.addScreenShotCurrentStep(u, "adapter");
	    				}
	    				u.unhighLight(By.xpath(xPath));
		    			u.hardWait(2);

	    				
	    			} catch (TimeoutException e) {
	    				u.click(By.xpath(nextPage));
	    				WebElement button = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
	    				scrollToElement(u.getDriver(), button);
	    				u.highLight(By.xpath(xPath));
	    				if(!resumen) {
	    				testCase.addScreenShotCurrentStep(u, "adapter");
	    				}
	    				u.unhighLight(By.xpath(xPath));
		    			u.hardWait(2);

	    				segundaPagina = true;
	    			}

	    			u.hardWait(2);
	    			u.waitForLoadPage();

	    			//u.getDriver().switchTo().frame(2);

	    			if (segundaPagina) {
	    				u.click(By.xpath(nextPage));
	    				u.waitForLoadPage();
	    				u.hardWait(2);
	    			}

	    			button2 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xPath)));
	    			scrollToElement(u.getDriver(), button2);
	    			adp1 = button2.getText().equals("No");
	    			System.out.println("Adapter is off? " + isOff);
	    			testCase.addTextEvidenceCurrentStep("-Adapter encontrado.");
	    			if(isOff) {
		    			testCase.addTextEvidenceCurrentStep("-El adapter '"+adapter+ "' esta apagado.");

	    			}else {
		    			testCase.addTextEvidenceCurrentStep("-El adapter '"+adapter+ "' esta encendido.");

	    			}
	    			//u.getDriver().navigate().refresh();
	            	}catch (Exception e) {
		    			testCase.addBoldTextEvidenceCurrentStep("-Adapter no encontrado.");
	    			}
	            }
			
		
		}
		
	}


