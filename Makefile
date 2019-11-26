CC=javac
JAVA = java
SQL_DIR=studentClient/simpledb/
TEST_DIR=simpledb_test/

COMP_DIR=simpledb/tx/

FILES = simpledb/server/Startup.java studentClient/simpledb/CreateStudentDB.java studentClient/simpledb/StudentMajor.java

.PHONY: run-server sqlInterpreter run-interpreter wait-die-test nq-checkpoint-test

compile:
	$(CC) $(FILES)

recompile:
	$(MAKE) -C $(COMP_DIR) compile-all

run-server: compile
	$(JAVA) simpledb.server.Startup student

sqlInterpreter:
	$(MAKE) -C $(SQL_DIR) compile-interpreter

run-interpreter:
	$(MAKE) -C $(SQL_DIR) interpreter-cmd

wait-die-test:
	$(MAKE) -C $(TEST_DIR) test-wait-die

nq-checkpoint-test:
	$(MAKE) -C $(TEST_DIR) test-nq-checkpoint

