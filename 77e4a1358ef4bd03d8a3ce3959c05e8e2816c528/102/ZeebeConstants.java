/*
 * Copyright © 2017 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.model.bpmn.impl;

public class ZeebeConstants {

  public static final String ATTRIBUTE_RETRIES = "retries";
  public static final String ATTRIBUTE_TYPE = "type";

  public static final String ATTRIBUTE_KEY = "key";
  public static final String ATTRIBUTE_VALUE = "value";

  public static final String ATTRIBUTE_SOURCE = "source";
  public static final String ATTRIBUTE_TARGET = "target";

  public static final String ATTRIBUTE_CORRELATION_KEY = "correlationKey";

  public static final String ATTRIBUTE_INPUT_COLLECTION = "inputCollection";
  public static final String ATTRIBUTE_INPUT_ELEMENT = "inputElement";
  public static final String ATTRIBUTE_OUTPUT_COLLECTION = "outputCollection";
  public static final String ATTRIBUTE_OUTPUT_ELEMENT = "outputElement";

  public static final String ATTRIBUTE_PROCESS_ID = "processId";
  public static final String ATTRIBUTE_PROPAGATE_ALL_CHILD_VARIABLES = "propagateAllChildVariables";

  public static final String ATTRIBUTE_FORM_KEY = "formKey";

  public static final String ELEMENT_HEADER = "header";
  public static final String ELEMENT_INPUT = "input";
  public static final String ELEMENT_IO_MAPPING = "ioMapping";
  public static final String ELEMENT_OUTPUT = "output";

  public static final String ELEMENT_SUBSCRIPTION = "subscription";

  public static final String ELEMENT_TASK_DEFINITION = "taskDefinition";
  public static final String ELEMENT_TASK_HEADERS = "taskHeaders";

  public static final String ELEMENT_FORM_DEFINITION = "formDefinition";
  public static final String ELEMENT_USER_TASK_FORM = "userTaskForm";

  public static final String ELEMENT_LOOP_CHARACTERISTICS = "loopCharacteristics";

  public static final String ELEMENT_CALLED_ELEMENT = "calledElement";

  /** Form key format used for camunda-forms format */
  public static final String USER_TASK_FORM_KEY_CAMUNDA_FORMS_FORMAT = "camunda-forms";
  /** Form key location used for forms embedded in the same BPMN file, i.e. zeebeUserTaskForm */
  public static final String USER_TASK_FORM_KEY_BPMN_LOCATION = "bpmn";
}
