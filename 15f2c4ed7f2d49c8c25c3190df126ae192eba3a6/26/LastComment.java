/**
 * Copyright (c) 2016-2017, Mihai Emil Andronache
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  1)Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  2)Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *  3)Neither the name of charles-rest nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.amihaiemil.charles.github;

import java.io.IOException;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;

import com.google.common.collect.Lists;
import com.jcabi.github.Comment;
import com.jcabi.github.Issue;

/**
 * Last comment where the agent was mentioned.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id: b924a617a503a0f3c36639253eab3d4b15c15ef0 $
 * @since 1.0.0
 * 
 */
public class LastComment extends Command {
    
    public LastComment(Issue issue) throws IOException {
        super(
            issue,
            Json.createObjectBuilder().add("id", "-1").add("body", "").build()
        );
        List<Comment> comments = Lists.newArrayList(issue.comments().iterate());
        boolean agentFound = false;
        for(int i=comments.size() - 1; !agentFound && i >=0; i--) {//we go backwards
            JsonObject currentJsonComment = comments.get(i).json();
            if(currentJsonComment.getJsonObject("user").getString("login").equals(agentLogin())) {
                agentFound = true; //we found a reply of the agent, so stop looking.
            } else {
                if(currentJsonComment.getString("body").contains("@" + agentLogin())) {
                    this.comment(currentJsonComment);
                    agentFound = true;
                }
            }
        }
    }

}
