PROGRAM=gcdmachine

run: $(PROGRAM)
	./$(PROGRAM)

$(PROGRAM).o: $(PROGRAM).asm
	nasm -f elf64 -g $(PROGRAM).asm

$(PROGRAM): $(PROGRAM).o
	ld -o $(PROGRAM) $(PROGRAM).o

# $(PROGRAM): $(PROGRAM).c
# 	gcc --std=gnu99 $(PROGRAM).c -o $(PROGRAM)


