// configuration for plugin testing - will not be included in the plugin zip

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
}

// Add the LDAP Auth Provider
grails.plugin.springsecurity.providerNames = [
	"daoAuthenticationProvider",
	"ldapAuthProvider",
	"rememberMeAuthenticationProvider"
]

// Enable the event listener
grails.plugin.springsecurity.useSecurityEventListener = true

// AD User Details Defaults
org.commonsemantics.grails.software.ldap.ad.default.firstName = "Unknown"
org.commonsemantics.grails.software.ldap.ad.default.lastName = "User"
org.commonsemantics.grails.software.ldap.ad.default.displayName = "Unknown User"
org.commonsemantics.grails.software.ldap.ad.default.email = "no@email.com"
org.commonsemantics.grails.software.ldap.ad.default.country = "Unknown"
org.commonsemantics.grails.software.ldap.ad.default.affiliation = "Unknown"
org.commonsemantics.grails.software.ldap.default.profileprivacy = "PROFILE_PRIVACY_ANONYMOUS"

// AD User Details Names
org.commonsemantics.grails.software.ldap.ad.firstName = "givenName"
org.commonsemantics.grails.software.ldap.ad.lastName = "sn"
org.commonsemantics.grails.software.ldap.ad.displayName = null
org.commonsemantics.grails.software.ldap.ad.username = "sAMAccountName"
org.commonsemantics.grails.software.ldap.ad.email = "mail"
org.commonsemantics.grails.software.ldap.ad.country = null
org.commonsemantics.grails.software.ldap.ad.affiliation = null
org.commonsemantics.grails.software.ldap.profileprivacy = "PROFILE_PRIVACY_ANONYMOUS"
