# spring-examples

PropertySourcesPlaceholderEncrypter is an easiest example I could write to add support for using encryted values as properties and resolve and decrypt those on the fly.

Anywhere I looked to add support for keeping encrypted values are using PropertyPlaceholderConfigurer but rarely are when using PropertySourcesPlaceholderConfigurer which is the new recommanded version in new spring projects. Most alternative out there ask for using jasypt which I found to be more baggage to do the simple thing and more or less use to do the same way which is done in its predecessor PropertyPlaceholderConfigurer but not in PropertySourcesPlaceholderConfigurer way. So looked into the PropertySourcesPlaceholderConfigurer code and fond this wasy way to add support for encypted proeprties.
