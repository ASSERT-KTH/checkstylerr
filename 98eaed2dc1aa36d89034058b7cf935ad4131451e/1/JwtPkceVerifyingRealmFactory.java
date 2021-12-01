package com.bakdata.conquery.models.auth.oidc;

import com.bakdata.conquery.commands.ManagerNode;
import com.bakdata.conquery.io.cps.CPSType;
import com.bakdata.conquery.io.jackson.Jackson;
import com.bakdata.conquery.models.auth.AuthenticationConfig;
import com.bakdata.conquery.models.auth.ConqueryAuthenticationRealm;
import com.bakdata.conquery.models.preproc.GroovyPredicate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import lombok.*;
import org.apache.shiro.realm.Realm;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.keycloak.TokenVerifier;
import org.keycloak.authorization.client.Configuration;
import org.keycloak.common.VerificationException;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKParser;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.JsonWebToken;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A realm that verifies oauth tokens using PKCE.
 */
@CPSType(id = "JWT_PKCE_REALM", base = AuthenticationConfig.class)
@NoArgsConstructor
@Data
public class JwtPkceVerifyingRealmFactory implements AuthenticationConfig {

    /**
     * The public key information that is used to validate signed JWT.
     * It can be retrieved from the IDP.
     */
    @NotNull
    private JWK jwk;
    @NonNull
    private String[] allowedAudiences = {};
    @NotEmpty
    private String issuer;
    @NotEmpty
    private List<String> additionalTokenChecks = Collections.emptyList();

    public ConqueryAuthenticationRealm createRealm(ManagerNode manager) {
        List<TokenVerifier.Predicate<AccessToken>> additionalVerifiers = new ArrayList<>();

        for (String additionalTokenCheck : additionalTokenChecks) {
            additionalVerifiers.add(ScriptedTokenChecker.create(additionalTokenCheck));
        }

        return new JwtPkceVerifyingRealm(getPublicKey(jwk), allowedAudiences, additionalVerifiers, issuer);
    }


    @JsonIgnore
    @SneakyThrows(JsonProcessingException.class)
    private static PublicKey getPublicKey(JWK jwk) {
        // We have to re-serdes the object because it might be a sub class which can not be handled correctly by the JWKParser
        String jwkString = Jackson.MAPPER.writeValueAsString(jwk);
        return JWKParser.create().parse(jwkString).toPublicKey();
    }

    public static abstract class ScriptedTokenChecker extends Script implements TokenVerifier.Predicate<AccessToken> {

        private final static GroovyShell SHELL;

        static {
            CompilerConfiguration config = new CompilerConfiguration();
            config.addCompilationCustomizers(new ImportCustomizer().addImports(AccessToken.class.getName()));
            config.setScriptBaseClass(ScriptedTokenChecker.class.getName());

            SHELL = new GroovyShell(config);
        }

        public static ScriptedTokenChecker create(String checkScript) {


            return (ScriptedTokenChecker) SHELL.parse(checkScript);
        }

        @Override
        public abstract Boolean run();

        @Override
        public boolean test(AccessToken token) throws VerificationException {
            Binding binding = new Binding();
            binding.setVariable("t", token);
            setBinding(binding);

            return run();
        }
    }
}
