/*
 * Copyright (c) 2011-2021 PrimeFaces Extensions
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package org.primefaces.extensions.selenium.internal.junit;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.primefaces.extensions.selenium.spi.WebDriverProvider;

public class WebDriverExtension implements BeforeAllCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        WebDriver webDriver = WebDriverProvider.get(true);
        System.out.println("WebDriverExtension#beforeAll - webDriver.getWindowHandle: " + webDriver.getWindowHandle());
        System.out.println("WebDriverExtension#beforeAll - webDriver.getWindowHandles: " + String.join(",", webDriver.getWindowHandles()));

        if (webDriver instanceof EventFiringWebDriver) {
            EventFiringWebDriver eventFiringWebDriver = (EventFiringWebDriver) webDriver;
            WebDriver wrappedWebDriver = eventFiringWebDriver.getWrappedDriver();
            if (wrappedWebDriver instanceof RemoteWebDriver) {
                RemoteWebDriver remoteWebDriver = (RemoteWebDriver) wrappedWebDriver;
                System.out.println("WebDriverExtension#beforeAll - remoteWebDriver.getSessionId: " + remoteWebDriver.getSessionId());
            }
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        WebDriver webDriver = WebDriverProvider.get();
        if (webDriver != null) {
            webDriver.quit();
        }
        WebDriverProvider.set(null);
    }
}
