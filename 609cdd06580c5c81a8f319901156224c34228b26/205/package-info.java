/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * A client to Phishing Protection API.
 *
 * <p>The interfaces provided are listed below, along with usage samples.
 *
 * <p>====================================== PhishingProtectionServiceV1Beta1Client
 * ======================================
 *
 * <p>Service Description: Service to report phishing URIs.
 *
 * <p>Sample for PhishingProtectionServiceV1Beta1Client:
 *
 * <pre>
 * <code>
 * try (PhishingProtectionServiceV1Beta1Client phishingProtectionServiceV1Beta1Client = PhishingProtectionServiceV1Beta1Client.create()) {
 *   ProjectName parent = ProjectName.of("[PROJECT]");
 *   String uri = "";
 *   ReportPhishingResponse response = phishingProtectionServiceV1Beta1Client.reportPhishing(parent, uri);
 * }
 * </code>
 * </pre>
 */
@Generated("by gapic-generator")
package com.google.cloud.phishingprotection.v1beta1;

import javax.annotation.Generated;
