/* Software License Agreement (BSD License)
 * 
 * Copyright (c) 2010-2013, Rustici Software, LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Rustici Software, LLC BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.rusticisoftware.hostedengine.client;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class StatementForwardService {

    private Configuration configuration = null;
    private ScormEngineService manager = null;
	
    /// <summary>
    /// Main constructor that provides necessary configuration information
    /// </summary>
    /// <param name="configuration">Application Configuration Data</param>
    public StatementForwardService(Configuration configuration, ScormEngineService manager)
    {
        this.configuration = configuration;
        this.manager = manager;
    }

	public String AddPath(String sURL, String sUsername, String sPassword, String dURL, 
		String dUsername, String dPassword) throws Exception {

		ServiceRequest request = new ServiceRequest(configuration);
		request.getParameters().add("sourceURL", sURL);
		request.getParameters().add("sourceUsername", sUsername);
		request.getParameters().add("sourcePassword", sPassword);
		request.getParameters().add("destinationURL", dURL);
		request.getParameters().add("destinationUsername", dUsername);
		request.getParameters().add("destinationPassword", dPassword);
		return request.getStringFromService("rustici.statementForward.AddForwardingPath");
	}

	public void UpdatePath(String id, String sURL, String sUsername, String sPassword, String dURL, 
		String dUsername, String dPassword) throws Exception {

		ServiceRequest request = new ServiceRequest(configuration);
		request.getParameters().add("id", id);
		request.getParameters().add("sourceURL", sURL);
		request.getParameters().add("sourceUsername", sUsername);
		request.getParameters().add("sourcePassword", sPassword);
		request.getParameters().add("destinationURL", dURL);
		request.getParameters().add("destinationUsername", dUsername);
		request.getParameters().add("destinationPassword", dPassword);

	    Document response = request.callService("rustici.statementForward.UpdateForwardingPath");
	}

	public void DeletePath(String id) throws Exception {
		ServiceRequest request = new ServiceRequest(configuration);
		request.getParameters().add("id", id);

	    Document response = request.callService("rustici.statementForward.DeleteForwardingPath");
	}
}
