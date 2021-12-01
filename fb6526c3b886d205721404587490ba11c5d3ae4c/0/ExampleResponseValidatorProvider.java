package com.griddynamics.jagger.engine.e1.collector;

import com.griddynamics.jagger.coordinator.NodeContext;
import com.griddynamics.jagger.invoker.v2.JHttpEndpoint;
import com.griddynamics.jagger.invoker.v2.JHttpQuery;
import com.griddynamics.jagger.invoker.v2.JHttpResponse;
import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * Example {@link ResponseValidatorProvider} implementation
 * aimed to demonstrate process of passing a value
 * to a dynamically created instances of {@link ResponseValidator}
 *
 * Created by Andrey Badaev
 * Date: 14/12/16
 */
public class ExampleResponseValidatorProvider implements ResponseValidatorProvider {
    
    private final String someValue;
    
    public ExampleResponseValidatorProvider(String someValue) {this.someValue = someValue;}
    
    @Override
    public ResponseValidator<JHttpQuery, JHttpEndpoint, JHttpResponse> provide(String sessionId,
                                                                               String taskId,
                                                                               NodeContext kernelContext) {

        return new ResponseValidator<JHttpQuery, JHttpEndpoint, JHttpResponse>(taskId, sessionId, kernelContext) {
            @Override
            public String getName() {
                return null;
            }
    
            @Override
            public boolean validate(JHttpQuery query, JHttpEndpoint endpoint, JHttpResponse result, long duration) {
                if (Objects.equals(someValue, "we are always good")) {
                    return true;
                }
                return result.getStatus() == HttpStatus.OK;
            }
        };
    }
}


