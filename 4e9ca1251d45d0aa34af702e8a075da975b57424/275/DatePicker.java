/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
/**
 * 
 */
package org.roda.wui.common.client.widgets;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.SourcesChangeEvents;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
@SuppressWarnings("deprecation")
public class DatePicker extends HorizontalPanel implements SourcesChangeEvents {

  private YearPicker year;
  private MonthPicker month;
  private DayPicker day;
  private boolean initial;
  private boolean firstEdit;
  private final List<ChangeListener> listeners;

  /**
   * Create a new date picker
   */
  public DatePicker() {
    this(true);
  }

  /**
   * Create a new date picker
   * 
   * @param initial
   *          is an initial or final date
   */
  public DatePicker(boolean initial) {

    this.initial = initial;
    this.firstEdit = true;
    year = new YearPicker();
    month = new MonthPicker();
    day = new DayPicker(initial);
    listeners = new Vector<>();

    add(year);
    add(month);
    add(day);

    year.addKeyboardListener(new KeyboardListener() {
      @Override
      public void onKeyDown(Widget sender, char keyCode, int modifiers) {
        // do nothing
      }

      @Override
      public void onKeyPress(Widget sender, char keyCode, int modifiers) {
        // do nothing
      }

      @Override
      public void onKeyUp(Widget sender, char keyCode, int modifiers) {
        if (year.isValid()) {
          day.update(year.getYearType(), month.getSelectedMonthType());
          if (firstEdit) {
            firstEdit = false;
            if (!DatePicker.this.initial) {
              month.setSelectedIndex(month.getItemCount() - 1);
            }
          }
        }
        DatePicker.this.onChange(year);
      }
    });

    month.addClickListener(new ClickListener() {

      @Override
      public void onClick(Widget sender) {
        day.update(year.getYearType(), month.getSelectedMonthType());
      }

    });

    ChangeListener changeListener = new ChangeListener() {

      @Override
      public void onChange(Widget sender) {
        DatePicker.this.onChange(sender);
      }

    };

    month.addChangeListener(changeListener);
    day.addChangeListener(changeListener);
  }

  /**
   * Get the date according to ISO 8601
   * 
   * @return
   */
  public String getISODate() {
    StringBuilder year = new StringBuilder(this.year.getString());
    String month = null;
    String day = null;

    StringBuilder ret = new StringBuilder();

    if (year.length() > 0) {
      try {
        month = this.month.getSelected();
        day = this.day.getSelected();
      } catch (NoSuchElementException e) {
        // do nothing
      }

      while (year.length() < 4) {
        year.append("0").append(year);
      }

      ret.append(year.toString());
      if (month != null) {
        ret.append("-").append(month);
      }
      if (day != null) {
        ret.append("-").append(day);
      }
    }

    return ret.toString();
  }

  /**
   * Set date with ISO 8601 defined date
   * 
   * @param isodate
   */
  public void setISODate(String isodate) {
    int year = -1;
    int month = -1;
    int day = -1;
    if (isodate.matches("\\d{1,4}-\\d\\d\\-\\d\\d")) {
      String[] splits = isodate.split("-");
      year = (new Integer(splits[0])).intValue();
      month = (new Integer(splits[1])).intValue();
      day = (new Integer(splits[2])).intValue();

    } else if (isodate.matches("\\d{1,4}-\\d\\d")) {
      String[] splits = isodate.split("-");
      year = (new Integer(splits[0])).intValue();
      month = (new Integer(splits[1])).intValue();

    } else if (isodate.matches("\\d{1,4}")) {
      year = (new Integer(isodate)).intValue();
    }
    if (year > 0) {
      this.year.set(year);
      this.month.setEnabled(this.year.isValid());
      if (month > 0) {
        this.month.setSelected(month);
        this.day.update(this.year.getYearType(), this.month.getSelectedMonthType());
        if (day > 0) {
          this.day.setSelected(day);
        }
      }

    }
  }

  /**
   * Get picked date
   * 
   * @return
   */
  public Date getDate() {
    return new Date(year.getInt() - 1900, month.getSelectedInt() - 1, day.getSelectedInt());
  }

  /**
   * Set date
   * 
   * @param date
   */

  public void setDate(Date date) {
    if (date != null) {
      int year = date.getYear() + 1900;
      int month = date.getMonth() + 1;
      int day = date.getDate();

      if (year > 0) {
        this.year.set(year);
        this.month.setEnabled(this.year.isValid());
        if (month > 0) {
          this.month.setSelected(month);
          this.day.update(this.year.getYearType(), this.month.getSelectedMonthType());
          if (day > 0) {
            this.day.setSelected(day);
          }
        }

      }
    } else {
      this.year.setText("");
      this.month.setSelected(1);
      this.day.setSelected(1);
    }
  }

  /**
   * Is date valid
   * 
   * @return true if valid
   */
  public boolean isValid() {
    return year.isValid();
  }

  /**
   * If not valid, add style from sub-components
   * 
   * @param valid
   * @param style
   */
  public void setValidStyle(boolean valid, String style) {
    if (valid) {
      year.removeStyleName(style);
      month.removeStyleName(style);
      day.removeStyleName(style);
    } else {
      year.addStyleName(style);
      month.addStyleName(style);
      day.addStyleName(style);
    }

  }

  @Override
  public void addChangeListener(ChangeListener listener) {
    listeners.add(listener);
  }

  @Override
  public void removeChangeListener(ChangeListener listener) {
    listeners.remove(listener);
  }

  protected void onChange(Widget sender) {
    for (ChangeListener listener : listeners) {
      listener.onChange(sender);
    }
  }

}
