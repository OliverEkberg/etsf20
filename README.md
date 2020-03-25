# Repository for group 2 in the PUSP-course

## Javadoc
Can be found [here](https://oliverekberg.github.io/etsf20/javadoc).

## Important notices
* **NEVER** commit passwords. If by accident commit a password, contact Oliver immediately.
* **NEVER** commit anything to `master`. `master` shall only get changed through pull requests that are approved by Oliver and SG. Instead, create a branch with a descriptive name and work on it. When you are ready for code review, submit a pull request.

## Getting started
### Prerequisites
1. Ask Oliver for access to this repository through the Slack-channel.
1. Get VPN access to LTH. This is crucial for developing at home.

### Importing the project to Eclipse
1. Download [Eclipse EE](https://www.eclipse.org/downloads/packages/release/2019-12/r/eclipse-ide-enterprise-java-developers).
1. Create a new empty directory that will contain your Eclipse project.
1. Open Eclipse and select the newly created directory.
1. In Eclipse, pick the option *Checkout projects from Git*.
1. Pick *Clone URI*.
1. Copy and paste the URI from this repository in the URI-field and provide your Github credentials.
1. Press *Next*, *Next* then *Finish*.

### Setting up Tomcat with Eclipse
1. Create a new directory where you want to keep your Tomcat server. Should **not** be in the same directory as you tracked files.
1. Download Tomcat from [here](http://fileadmin.cs.lth.se/cs/Education/etsf20/lab/apache-tomcat-9.0.30-windows-x64.zip) and extract its content into your newly created directory.
1. Download the MySQL connector from [here](http://fileadmin.cs.lth.se/cs/Education/etsf20/lab/mysql-connector-java-8.0.19.jar) and place it under `/lib` in the Tomcat folder.
1. In Eclipse, if not already present, add the view for *Servers*. This can be done by navigating to *Window* > *Show view* > *Other...* and then searching for "Servers".
1. In the servers view, add a new server by pressing the link. Pick server type *Tomcat v9.0 Server*.
1. You should now be able to run the system by right clicking on the root directory in Eclipse and picking *Run As* > *Run on Server*.
1. You should now be able to reach the login page [here](http://localhost:8080/BaseBlockSystem/LogIn).
