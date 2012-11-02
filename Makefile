JFLAGS = -cp "commons-codec-1.7.jar:json-simple-1.1.1.jar"
JC = javac
.SUFFIXES: .java .class
.java.class:

CLASSES = \
        BingTest.java \
	KeyFinder.java \
	Tree.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

run:
	java -cp "json-simple-1.1.1.jar:commons-codec-1.7.jar:." BingTest $(keyword) $(precision)
