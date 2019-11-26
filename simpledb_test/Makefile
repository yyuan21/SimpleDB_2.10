CC=javac
JAVA = java

.PHONY: test-wait-die test-nq-checkpoint

compile:
	$(CC) *.java

test-wait-die: compile
	$(JAVA) WaitDieTest

test-nq-checkpoint: compile
	$(JAVA) NQCheckpointTest
