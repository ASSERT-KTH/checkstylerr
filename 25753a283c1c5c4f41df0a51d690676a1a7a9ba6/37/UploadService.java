/* Software License Agreement (BSD License)
 * 
 * Copyright (c) 2010-2011, Rustici Software, LLC
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.rusticisoftware.hostedengine.client.datatypes.FileData;
import com.rusticisoftware.hostedengine.client.datatypes.UploadProgress;
import com.rusticisoftware.hostedengine.client.datatypes.UploadToken;

public class UploadService
{
	private static final Logger log = Logger.getLogger(UploadService.class.getName());
	
    private Configuration configuration = null;
    private ScormEngineService manager = null;

    /// <summary>
    /// Main constructor that provides necessary configuration information
    /// </summary>
    /// <param name="configuration">Application Configuration Data</param>
    public UploadService(Configuration configuration, ScormEngineService manager)
    {
        this.configuration = configuration;
        this.manager = manager;
    }

    /// <summary>
    /// Retrieve upload token, which must be acquired and pass to a subsequent call to UploadFile
    /// </summary>
    /// <returns>An upload token which can be used in a call to UploadFile</returns>
    public UploadToken GetUploadToken() throws Exception
    {
        ServiceRequest request = new ServiceRequest(configuration);
        Document response = request.callService("rustici.upload.getUploadToken");
        return new UploadToken(response);
    }

    /// <summary>
    /// Upload a file to your Scorm Cloud upload space
    /// </summary>
    /// <param name="absoluteFilePathToZip">Absolute local path to file to be uploaded</param>
    /// <param name="permissionDomain">The upload "permission domain" under which to upload the file</param>
    /// <returns>A relative path to the file uploaded, which should be used in a subsequent call to ImportCourse or VersionCourse</returns>
    public String UploadFile(String absoluteFilePathToZip, String permissionDomain) throws Exception
    {
        UploadToken token = GetUploadToken();
        return UploadFile(absoluteFilePathToZip, permissionDomain, token);
    }

    /// <summary>
    /// Upload a file to your Scorm Cloud upload space
    /// </summary>
    /// <param name="absoluteFilePathToZip">Absolute local path to file to be uploaded</param>
    /// <param name="permissionDomain">The upload "permission domain" under which to upload the file</param>
    /// <param name="token">A previously acquired upload token to be used for this upload request</param>
    /// <returns>A relative path to the file uploaded, which should be used in a subsequent call to ImportCourse or VersionCourse</returns>
    public String UploadFile(String absoluteFilePathToZip, String permissionDomain, UploadToken token) throws Exception
    {
        ServiceRequest request = new ServiceRequest(configuration);
        request.setFileToPost(absoluteFilePathToZip);
        //request.setServer(token.getServer());
        request.getParameters().add("token", token.getTokenId());

        if (!Utils.isNullOrEmpty(permissionDomain)) {
            request.getParameters().add("pd", permissionDomain);
        }

        Document response = request.callService("rustici.upload.uploadFile");
        Element location = (Element)response.getElementsByTagName("location").item(0);
        return location.getTextContent();
    }

    /// <summary>
    /// Return a url that can be embedded in the action attribute of a form element
    /// </summary>
    /// <param name="redirectUrl">The url to redirect to after the upload is complete</param>
    public String GetUploadUrl(String redirectUrl) throws Exception
    {
        return GetUploadUrl(redirectUrl, null);
    }

    /// <summary>
    /// Return a url that can be embedded in the action attribute of a form element
    /// </summary>
    /// <param name="redirectUrl">The url to redirect to after the upload is complete</param>
    /// <param name="permissionDomain">The permission domain in which the upload should be placed</param>
    public String GetUploadUrl(String redirectUrl, String permissionDomain) throws Exception
    {
        UploadToken token = GetUploadToken();
        
        ServiceRequest request = new ServiceRequest(configuration);
        request.getParameters().add("token", token.getTokenId());
        //request.setServer(token.getServer());
        
        if (!Utils.isNullOrEmpty(permissionDomain)) {
            request.getParameters().add("pd", permissionDomain);
        }
        if (!Utils.isNullOrEmpty(redirectUrl)) {
            request.getParameters().add("redirecturl", redirectUrl);
        }
        return request.constructUrl("rustici.upload.uploadFile");
    }
    
    public UploadProgress GetUploadProgress(String tokenId) throws Exception
    {
    	ServiceRequest request = new ServiceRequest(configuration);
    	request.getParameters().add("token", tokenId);
    	Document response = request.callService("rustici.upload.getUploadProgress");
    	return new UploadProgress(response);
    }

    /// <summary>
    /// Retrieve a list of high-level data about all files owned by the 
    /// configured appId.
    /// </summary>
    /// <returns>List of Course Data objects</returns>
    public List<FileData> GetFileList() throws Exception
    {
        return GetFileList(null);
    }

    /// <summary>
    /// Retrieve a list of high-level data about all files owned by the 
    /// configured appId.
    /// </summary>
    /// <returns>List of Course Data objects</returns>
    public List<FileData> GetFileList(String permissionDomain) throws Exception
    {
        ServiceRequest request = new ServiceRequest(configuration);
        if (!Utils.isNullOrEmpty(permissionDomain))
            request.getParameters().add("pd", permissionDomain);
        Document response = request.callService("rustici.upload.listFiles");
        return FileData.ConvertToFileDataList(response);
    }

    /// <summary>
    /// Delete the specified files given filenames only (not full path)
    /// </summary>
    public void DeleteFileList(List<String> fileNames, String permissionDomain) throws Exception
    {
        ServiceRequest request = new ServiceRequest(configuration);
        if (!Utils.isNullOrEmpty(permissionDomain))
            request.getParameters().add("pd", permissionDomain);

        for (String file : fileNames)
        {
            request.getParameters().add("file", file);
        }

        request.callService("rustici.upload.deleteFiles");
    }

    /// <summary>
    /// Delete the specified files given filenames only (not full path)
    /// from the default domain
    /// </summary>
    public void DeleteFileList(List<String> fileNames) throws Exception
    {
        DeleteFileList(fileNames, null);
    }

    /// <summary>
    /// Delete the specified file from the permission domain
    /// </summary>
    public void DeleteFile(String fileName, String permissionDomain) throws Exception
    {
        List<String> fileNames = new ArrayList<String>();
        fileNames.add(fileName);
        DeleteFileList(fileNames, permissionDomain);
    }

    /// <summary>
    /// Delete the specified file from the default domain
    /// </summary>
    public void DeleteFile(String filePath) throws Exception
    {
        String domain = null;
        String fileName = filePath;

        String[] parts = filePath.split("/");
        if (parts.length > 1) {
            domain = parts[0];
            fileName = parts[1];
        }

        DeleteFile(fileName, domain);
    }
}
