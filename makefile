JAVA := java
JAVAC := javac
JAVACC:= javacc
JACC_SCANNER_FILE_NAME  := Scanner
JACC_SCANNER_CLASS_NAME := MiniJavaScanner
JACC_GENERATED_SRC :=\
	$(JACC_SCANNER_CLASS_NAME).java\
	$(JACC_SCANNER_CLASS_NAME)Constants.java\
	$(JACC_SCANNER_CLASS_NAME)TokenManager.java\
	Token.java\
	TokenMgrError.java\
	SimpleCharStream.java\
	ParseException.java

jacc_src := $(addprefix parser/, $(JACC_GENERATED_SRC))

default	:	compile

compile :	compile.jar

compile.jar	:	parser/*.class translate/*.class sparc/*.class
	echo "Main-Class: parser.Main\nClass-Path: http://www.cs.fit.edu/~ryan/cse4251/support.jar" > mc.mf
	echo
	jar cvmf mc.mf $@ parser/*.class translate/*.class sparc/*.class
	chmod u+x assembler

parser/*.class translate/*.class sparc/*.class:	$(jacc_src)
	$(JAVAC) -classpath .:$(SUPPORT) parser/*.java translate/*.java sparc/*.java

$(jacc_src)		:  parser/$(JACC_SCANNER_FILE_NAME).jj
	$(JAVACC) -OUTPUT_DIRECTORY=parser $^
