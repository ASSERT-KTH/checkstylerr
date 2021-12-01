package com.bakdata.conquery.models.auth;

import com.bakdata.conquery.models.identifiable.ids.specific.UserId;
import lombok.Getter;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.SimplePrincipalCollection;

/**
 * Specialization class of the {@link AuthenticationInfo} that enforces the use
 * of a {@link UserId} as primary principal.
 */
@SuppressWarnings("serial")
@Getter
public class ConqueryAuthenticationInfo implements AuthenticationInfo {

	private final SimplePrincipalCollection principals = new SimplePrincipalCollection();
	
	/**
	 * The credential a realm used for authentication.
	 */
	private final Object credentials;
	
	
	/**
	 * A realm can indicate whether a logout button is shown for the user or not
	 */
	private final boolean hideUserLogout; 

	public ConqueryAuthenticationInfo(UserId userId, Object credentials, Realm realm, boolean hideUserLogout) {
		principals.add(userId, realm.getName());
		this.credentials = credentials;
		this.hideUserLogout = hideUserLogout;
	}
}
