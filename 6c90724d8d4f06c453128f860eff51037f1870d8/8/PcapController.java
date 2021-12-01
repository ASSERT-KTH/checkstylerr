/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.metron.rest.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.IOUtils;
import org.apache.metron.job.JobStatus;
import org.apache.metron.rest.RestException;
import org.apache.metron.rest.model.pcap.FixedPcapRequest;
import org.apache.metron.rest.model.pcap.PcapStatus;
import org.apache.metron.rest.model.pcap.Pdml;
import org.apache.metron.rest.model.pcap.QueryPcapRequest;
import org.apache.metron.rest.security.SecurityUtils;
import org.apache.metron.rest.service.PcapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pcap")
public class PcapController {

  private static final String PCAP_FILENAME_FORMAT = "pcap_%s_%s.pcap";

  @Autowired
  private PcapService pcapQueryService;

  @ApiOperation(value = "Executes a Fixed Filter Pcap Query.")
  @ApiResponses(value = { @ApiResponse(message = "Returns a job status with job ID.", code = 200)})
  @RequestMapping(value = "/fixed", method = RequestMethod.POST)
  ResponseEntity<PcapStatus> fixed(@ApiParam(name="fixedPcapRequest", value="A Fixed Pcap Request"
          + " which includes fixed filter fields like ip source address and protocol", required=true)@RequestBody FixedPcapRequest fixedPcapRequest) throws RestException {
    PcapStatus pcapStatus = pcapQueryService.submit(SecurityUtils.getCurrentUser(), fixedPcapRequest);
    return new ResponseEntity<>(pcapStatus, HttpStatus.OK);
  }

  @ApiOperation(value = "Executes a Query Filter Pcap Query.")
  @ApiResponses(value = { @ApiResponse(message = "Returns a job status with job ID.", code = 200)})
  @RequestMapping(value = "/query", method = RequestMethod.POST)
  ResponseEntity<PcapStatus> query(@ApiParam(name="queryPcapRequest", value="A Query Pcap Request"
          + " which includes Stellar query field", required=true)@RequestBody QueryPcapRequest queryPcapRequest) throws RestException {
    PcapStatus pcapStatus = pcapQueryService.submit(SecurityUtils.getCurrentUser(), queryPcapRequest);
    return new ResponseEntity<>(pcapStatus, HttpStatus.OK);
  }

  @ApiOperation(value = "Gets job status for Pcap query job.")
  @ApiResponses(value = {
          @ApiResponse(message = "Returns a job status for the Job ID.", code = 200),
          @ApiResponse(message = "Job is missing.", code = 404)
  })
  @RequestMapping(value = "/{jobId}", method = RequestMethod.GET)
  ResponseEntity<PcapStatus> getStatus(@ApiParam(name="jobId", value="Job ID of submitted job", required=true)@PathVariable String jobId) throws RestException {
    PcapStatus jobStatus = pcapQueryService.getJobStatus(SecurityUtils.getCurrentUser(), jobId);
    if (jobStatus != null) {
      return new ResponseEntity<>(jobStatus, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @ApiOperation(value = "Gets a list of job statuses for Pcap query jobs that match the requested state.")
  @ApiResponses(value = {
          @ApiResponse(message = "Returns a list of job statuses for jobs that match the requested state.", code = 200)
  })
  @RequestMapping(method = RequestMethod.GET)
  ResponseEntity<List<PcapStatus>> getStatuses(@ApiParam(name="state", value="Job state", required=true)@RequestParam String state) throws RestException {

    List<PcapStatus> jobs = pcapQueryService.getJobStatus(SecurityUtils.getCurrentUser(), JobStatus.State.valueOf(state));
    return new ResponseEntity<>(jobs, HttpStatus.OK);
  }

  @ApiOperation(value = "Gets Pcap Results for a page in PDML format.")
  @ApiResponses(value = {
          @ApiResponse(message = "Returns PDML in json format.", code = 200),
          @ApiResponse(message = "Job or page is missing.", code = 404)
  })
  @RequestMapping(value = "/{jobId}/pdml", method = RequestMethod.GET)
  ResponseEntity<Pdml> pdml(@ApiParam(name="jobId", value="Job ID of submitted job", required=true)@PathVariable String jobId,
                            @ApiParam(name="page", value="Page number", required=true)@RequestParam Integer page) throws RestException {
    Pdml pdml = pcapQueryService.getPdml(SecurityUtils.getCurrentUser(), jobId, page);
    if (pdml != null) {
      return new ResponseEntity<>(pdml, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }


  @ApiOperation(value = "Kills running job.")
  @ApiResponses(value = { @ApiResponse(message = "Kills passed job.", code = 200)})
  @RequestMapping(value = "/kill/{jobId}", method = RequestMethod.DELETE)
  ResponseEntity<PcapStatus> killJob(
      @ApiParam(name = "jobId", value = "Job ID of submitted job", required = true) @PathVariable String jobId)
      throws RestException {
    PcapStatus jobStatus = pcapQueryService.killJob(SecurityUtils.getCurrentUser(), jobId);
    if (jobStatus != null) {
      return new ResponseEntity<>(jobStatus, HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
  }

  @ApiOperation(value = "Download Pcap Results for a page.")
  @ApiResponses(value = {
          @ApiResponse(message = "Returns Pcap as a file download.", code = 200),
          @ApiResponse(message = "Job or page is missing.", code = 404)
  })
  @RequestMapping(value = "/{jobId}/raw", method = RequestMethod.GET)
  void raw(@ApiParam(name="jobId", value="Job ID of submitted job", required=true)@PathVariable String jobId,
           @ApiParam(name="page", value="Page number", required=true)@RequestParam Integer page,
           @RequestParam(defaultValue = "", required = false) String fileName,
           final HttpServletRequest request, final HttpServletResponse response) throws RestException {
    try (InputStream inputStream = pcapQueryService.getRawPcap(SecurityUtils.getCurrentUser(), jobId, page);
         OutputStream output = response.getOutputStream()) {
      response.reset();
      if (inputStream == null) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
      } else {
        response.setContentType("application/octet-stream");
        if (fileName.isEmpty()) {
          fileName = String.format(PCAP_FILENAME_FORMAT, jobId, page);
        }
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        int size = IOUtils.copy(inputStream, output);
        response.setContentLength(size);
        output.flush();
      }
    } catch (IOException e) {
      throw new RestException(e);
    }
  }

}
