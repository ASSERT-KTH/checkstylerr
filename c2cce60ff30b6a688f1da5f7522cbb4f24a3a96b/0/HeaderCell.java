/*
 * Copyright 2011-2020 PrimeFaces Extensions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.primefaces.extensions.selenium.component.model.datatable;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.primefaces.extensions.selenium.PrimeExpectedConditions;
import org.primefaces.extensions.selenium.PrimeSelenium;
import org.primefaces.extensions.selenium.component.base.ComponentUtils;

public class HeaderCell extends Cell {

    public HeaderCell(WebElement webElement) {
        super(webElement);
    }

    public WebElement getColumnFilter() {
        if (getWebElement() != null) {
            return getWebElement().findElement(By.className("ui-column-filter"));
        }

        return null;
    }

    public void setFilterValue(String filterValue, boolean unfocusFilterField) {
        WebElement columnFilterElt;

        try {
            // default-filter
            columnFilterElt = getColumnFilter();
        }
        catch (NoSuchElementException ex) {
            // for <f:facet name="filter">
            columnFilterElt = getWebElement().findElement(By.tagName("input"));
        }

        columnFilterElt.clear();
        if (filterValue != null) {
            ComponentUtils.sendKeys(columnFilterElt, filterValue);
        }
        
        if (unfocusFilterField) {
            columnFilterElt.sendKeys(Keys.TAB);
        }
        else {
            try {
                // default-filter runs delayed - so wait...
                Thread.sleep(500);
            }
            catch (InterruptedException ex) {
            }
        }
        PrimeSelenium.waitGui().until(PrimeExpectedConditions.jQueryNotActive());
    }
}
