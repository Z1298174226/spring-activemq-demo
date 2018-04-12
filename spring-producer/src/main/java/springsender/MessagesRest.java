/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package springsender;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class MessagesRest {

    @Autowired
    private MessagesService messages;

    @RequestMapping(value = "/api/log/{key}", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    public void postMessage(
            @PathVariable("key") String key,

            // messages parameters
            @RequestParam(value = "delay_message_process", defaultValue = "0") int delayMessageProcess,
            @RequestParam(value = "message_throws_exception", defaultValue = "false") boolean messageThrowsException,
            @RequestParam(value = "number_of_messages", defaultValue = "1000") int numberOfMessages,
            @RequestParam(value = "size_of_message", defaultValue = "1") int sizeOfMessage,
            @RequestParam(value = "persistent_message", defaultValue = "false") boolean persistentMessage,

            // forward messages parameters (used by the consumer of the messages above)
            @RequestParam(value = "number_of_forward_messages", defaultValue = "1000") int numberOfForwardMessages,
            @RequestParam(value = "size_of_forward_message", defaultValue = "1000") int sizeOfForwardMessage,
            @RequestParam(value = "persistent_forward_message", defaultValue = "false") boolean persistentForwardMessage
    ) {
        messages.postMessage(
                key,
                numberOfMessages,
                sizeOfMessage,
                delayMessageProcess,
                messageThrowsException,
                persistentMessage,
                numberOfForwardMessages,
                sizeOfForwardMessage,
                persistentForwardMessage
        );
    }

}
