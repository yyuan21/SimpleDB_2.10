CC=javac
JAVA = java

.PHONY: compile-interpreter interpreter-cmd

compile-interpreter:
	$(CC) SQLInterpreter.java

interpreter-cmd: compile-interpreter
	$(JAVA) SQLInterpreter
