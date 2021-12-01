/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.common.slider;

import org.roda.core.data.v2.index.IsIndexed;
import org.roda.core.data.v2.ip.DIPFile;
import org.roda.core.data.v2.ip.IndexedAIP;
import org.roda.core.data.v2.ip.IndexedDIP;
import org.roda.core.data.v2.ip.IndexedFile;
import org.roda.core.data.v2.ip.IndexedRepresentation;
import org.roda.wui.client.common.actions.ActionableObject;
import org.roda.wui.client.common.actions.ActionableWidgetBuilder;
import org.roda.wui.client.common.actions.AipActions;
import org.roda.wui.client.common.actions.DisseminationActions;
import org.roda.wui.client.common.actions.DisseminationFileActions;
import org.roda.wui.client.common.actions.FileActions;
import org.roda.wui.client.common.actions.RepresentationActions;

public class OptionsSliderHelper {

  private OptionsSliderHelper() {
    // do nothing
  }

  static <T extends IsIndexed> void updateOptionsObjectSliderPanel(T object, SliderPanel slider) {
    if (object instanceof IndexedFile) {
      updateOptionsSliderPanel((IndexedFile) object, slider);
    } else if (object instanceof IndexedRepresentation) {
      updateOptionsSliderPanel((IndexedRepresentation) object, slider);
    } else if (object instanceof IndexedAIP) {
      updateOptionsSliderPanel((IndexedAIP) object, slider);
    } else if (object instanceof IndexedDIP) {
      updateOptionsSliderPanel((IndexedDIP) object, slider);
    } else if (object instanceof DIPFile) {
      updateOptionsSliderPanel((DIPFile) object, slider);
    } else {
      // do nothing
    }
  }

  private static void updateOptionsSliderPanel(IndexedAIP aip, SliderPanel slider) {
    slider.clear();
    slider.addContent(new ActionableWidgetBuilder<>(AipActions.get()).buildWithObjects(new ActionableObject<>(aip)));
  }

  private static void updateOptionsSliderPanel(IndexedRepresentation representation, SliderPanel slider) {
    slider.clear();
    slider.addContent(new ActionableWidgetBuilder<>(RepresentationActions.get())
      .buildWithObjects(new ActionableObject<>(representation)));
  }

  private static void updateOptionsSliderPanel(final IndexedFile file, final SliderPanel slider) {
    slider.clear();
    slider
      .addContent(new ActionableWidgetBuilder<>(FileActions.get()).buildWithObjects(new ActionableObject<>((file))));
  }

  private static void updateOptionsSliderPanel(final IndexedDIP dip, final SliderPanel slider) {
    slider.clear();
    slider.addContent(
      new ActionableWidgetBuilder<>(DisseminationActions.get()).buildWithObjects(new ActionableObject<>((dip))));
  }

  private static void updateOptionsSliderPanel(final DIPFile file, final SliderPanel slider) {
    slider.clear();
    slider.addContent(
      new ActionableWidgetBuilder<>(DisseminationFileActions.get()).buildWithObjects(new ActionableObject<>((file))));
  }

}
