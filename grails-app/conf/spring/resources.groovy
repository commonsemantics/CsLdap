import org.commonsemantics.grails.security.LDAPAuthenticationApplicationListener;
import org.commonsemantics.grails.security.LDAPUserDetailsContextMapper;

beans = {
	// LDAP
	ldapUserDetailsMapper(LDAPUserDetailsContextMapper) {
		grailsApplication = ref("grailsApplication")
	}
	applicationListener(LDAPAuthenticationApplicationListener)
}
