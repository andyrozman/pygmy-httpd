# pygmy-httpd

### Description
Pygmy is a 40KB portable http stack written in Java. It is ideal for PDA and resource limited devices, as well as desktop applications and server applications. Pygmy provides a plug-in architecture so features can be added and removed easily.

### Note
I just copied this project from sourceforge.net/pygmy-httpd. I have been using it for years now as part of my application. It does what I need, which is why I want to preserve it. Problem was that at the moment it "logs" everything to console, which is not ok. This is my intent to remedy that, by logging everything through Slf4j (Simple Logging Framework for Java), so that it can be controled through configuration.

## Old documentation (I will rewrite this, when I have time) 

How to Run pygmy
----------------
To run pygmy you must download Java 1.5 or greater.  After you have
installed Java open a command prompt and goto the <pygmy home>/build
directory.  Type the following command:

java -jar pygmy-core.jar

Now open your web browser and goto the following URL:

http://localhost

You should now see the documentation for pygmy.  Read and enjoy.

How to Build pygmy
------------------

You will need apache's ant and the JDK 1.5 or greater in order to compile
the source files.

1.  Download ant 1.5.1 or greater from http://ant.apache.org and install
    it on your system.

2.  Modify the PATH environment so that ant is within your PATH.

3.  Open up a command line prompt if you haven't already done that.

4.  Goto the directory where you unzipped pygmy.

5.  type: ant all

You should see ant building the source files.  It will produce the pgymy
jar files once it is done.  Now follow the instructions above on
How to Run pygmy.

charlie dot hubbard [don't want know spam> at gmail dot com