package org.commonsemantics.grails.security;

import java.util.Collection;
import grails.plugin.springsecurity.userdetails.GrailsUser;
import org.commonsemantics.grails.users.model.User;
import org.springframework.security.core.GrantedAuthority;

/** Wrapper for the Spring Security UserDetails object. This allows storage of
 * the user object when it has been created by the LDAPUserDetailsContextWrapper class.
 * @author Tom Wilkin */
public class LDAPUserDetails extends GrailsUser {
	
	/** Construct a new LDAPUserDetails object with the given role authorities
	 * and the specified user.
	 * @param user The user to wrap.
	 * @param authorities The role authorities this user has. */
	public LDAPUserDetails(final User user, final Collection<GrantedAuthority> authorities)	{
		super(user.getUsername( ), user.getPassword( ), user.isEnabled( ),
				!user.isAccountExpired( ), !user.isPasswordExpired( ),
				!user.getAccountLocked( ), authorities,
				user.getId( ));
	}
	
	@Override
	public Object writeReplace( ) throws ObjectStreamException {
		return new GrailsUser(getUsername( ), "ldap", isEnabled( ),
			isAccountNonExpired( ), isCredentialsNonExpired( ),
			isAccountNonLocked( ), getAuthorities( ), getId( ));
	}

};
