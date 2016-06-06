NAME = "./*.java"
all:
	@echo "C..."
	javac ./*.java

run: all
	@echo "R..."
	java Breakout

clean:
	rm -rf *.class ./*.class 
