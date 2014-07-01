package org.commonsemantics.grails.security;

import java.util.Collection;
import org.commonsemantics.grails.agents.model.AgentUri
import org.commonsemantics.grails.agents.model.Person;
import org.commonsemantics.grails.users.model.ProfilePrivacy
import org.commonsemantics.grails.users.model.Role;
import org.commonsemantics.grails.users.model.User;
import org.commonsemantics.grails.users.model.UserRole;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.transaction.annotation.*;

/** Map the details from LDAP to a user and add that user to the database.
 * Also map the roles from any appropriate AD groups to roles in the database
 * on each login.
 * @author Tom Wilkin */
class LDAPUserDetailsContextMapper implements UserDetailsContextMapper {
	
	def grailsApplication;
	
	@Override
	@Transactional
	public UserDetails mapUserFromContext(final DirContextOperations context,
			final String username, final Collection<GrantedAuthority> authorities)
	{
		// extract the user details from LDAP
		String email = getUserDetail(context, "show",
			grailsApplication.config.org.commonsemantics.grails.software.ldap.ad.email,
			grailsApplication.config.org.commonsemantics.grails.software.ldap.ad.default.email
		);
		String displayName = getUserDetail(context, "show",
			grailsApplication.config.org.commonsemantics.grails.software.ldap.ad.displayName,
			grailsApplication.config.org.commonsemantics.grails.software.ldap.ad.default.displayName
		);
		
		String firstName = getUserDetail(context,
			grailsApplication.config.org.commonsemantics.grails.persons.model.field.firstName,
			grailsApplication.config.org.commonsemantics.grails.software.ldap.ad.firstName,
			grailsApplication.config.org.commonsemantics.grails.software.ldap.ad.default.firstName
		);
		String lastName = getUserDetail(context,
			grailsApplication.config.org.commonsemantics.grails.persons.model.field.lastName,
			grailsApplication.config.org.commonsemantics.grails.software.ldap.ad.lastName,
			grailsApplication.config.org.commonsemantics.grails.software.ldap.ad.default.lastName
		);
		String country = getUserDetail(context,
			grailsApplication.config.org.commonsemantics.grails.persons.model.field.country,
			grailsApplication.config.org.commonsemantics.grails.software.ldap.ad.country,
			grailsApplication.config.org.commonsemantics.grails.software.ldap.ad.default.country
		);
		String affiliation = getUserDetail(context,
			grailsApplication.config.org.commonsemantics.grails.persons.model.field.affiliation,
			grailsApplication.config.org.commonsemantics.grails.software.ldap.ad.affiliation,
			grailsApplication.config.org.commonsemantics.grails.software.ldap.ad.default.affiliation
		);
		
		// check if the user already exists
		User user = User.findByUsername(username);
		if(!user) {
			// create the person instance
			Person person = new Person(
				firstName: firstName,
				lastName: lastName,
				displayName: displayName,
				email: email,
				country: country,
				affiliation: affiliation
			);
		
			// save the person
			if(!person.save(flush: true)) {
				System.out.println(person.errors.allErrors);
			}
			
			// create the URI
			AgentUri agentURI = new AgentUri(
				uri: "username:" + username,
				agent: person,
				type: "ldap"
			);
		
			// save the URI
			if(!agentURI.save(flush: true)) {
				System.out.println(agentURI.errors.allErrors);
			}
			
			// set the privacy level
			String strProfilePrivacy;
			if(grailsApplication.config.org.commonsemantics.grails.software.ldap.profileprivacy != null
				&& !grailsApplication.config.org.commonsemantics.grails.software.ldap.profileprivacy.equals(""))
			{
				strProfilePrivacy = grailsApplication.config.org.commonsemantics.grails.software.ldap.profileprivacy;
			} else {
				strProfilePrivacy = grailsApplication.config.org.commonsemantics.grails.software.ldap.default.profileprivacy;
			}
			ProfilePrivacy privacy = ProfilePrivacy.findByValue(strProfilePrivacy);
			
			// create the user instance
			user = new User(
				username: username,
				password: "ldap",
				enabled: "true",
				person: person,
				profilePrivacy: privacy
			);
		
			// save the user
			if(!user.save(flush: true)) {
				System.out.println(user.errors.allErrors);
			}
		} else {
			// update the user details from AD
			Person person = user.person;
			person.setFirstName(firstName);
			person.setLastName(lastName);
			person.setDisplayName(displayName);
			person.setEmail(email);
			person.setCountry(country);
			person.setAffiliation(affiliation);
			
			// save the person
			if(!person.save(flush: true)) {
				System.out.println(person.errors.allErrors);
			}
		}
		
		// update the access roles from AD
		updateRoles(user, authorities);
		
		// create the user details for this user
		UserDetails ud = new LDAPUserDetails(user, authorities);
		return ud;
	}

	@Override
	public void mapUserToContext(final UserDetails user, 
		final DirContextAdapter context)
	{
		// not required		
	}
	
	/** Update the roles for the given user to the given authorities from AD.
	 * @param user The user to update the roles for.
	 * @param authorities The authorities from AD to create roles from. */
	@Transactional
	public static void updateRoles(final User user, final Collection<GrantedAuthority> authorities)
	{
		// remove all this user's roles
		UserRole.removeAll(user);
		
		// iterate through the AD roles and add matching roles to the database
		Role[ ] roles = Role.findAll( );
		for(GrantedAuthority authority : authorities) {
			for(Role role : roles) {
				if(authority.getAuthority( ).equals(role.authority)) {
					UserRole userRole = new UserRole(user: user, role: role);
					userRole.save(flush: true);
				}
			}
		}
	}
	
	/** Allow updating of the roles for the given user.
	 * @param ud The LDAPUserDetail instance to update the roles for. */
	@Transactional
	public static void updateRoles(final LDAPUserDetails ud) {
		User user = User.findByUsername(ud.getUsername( ));
		updateRoles(user, ud.getAuthorities( ));
	}
	
	/** Extract the user details from AD if it is enabled.
	 * @param context The context to extract the LDAP details from.
	 * @param hideConfig Whether this value is enabled.
	 * @param ldapConfig The field in AD to extract the value form.
	 * @param defaultConfig The default value if the AD field is undefined.
	 * @return The value of this user detail. */
	private String getUserDetail(final DirContextOperations context, final String hideConfig, final String ldapConfig, final String defaultConfig) {
		String result = null;
		if(!hideConfig.equals("hide")) {
			if(ldapConfig != null && !ldapConfig.equals("")) {
				result = context.getStringAttribute(ldapConfig);
			} else {
				result = defaultConfig;
			}
		}
		
		if(result == null || result.equals("")) {
			result = defaultConfig;
		}
		
		return result;
	}

};
