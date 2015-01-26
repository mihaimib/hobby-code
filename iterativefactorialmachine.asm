;;; assembly language version of:

;; (define-machine factorial
;;   (registers n product counter)
;;   (controller
;;    (assign product 1)
;;    (assign counter 1)
;;    :loop
;;    (branch (> counter n) :done)
;;    (assign product (* counter product))
;;    (assign counter (inc counter))
;;    (goto :loop)
;;    :done
;;    (assign n product)))

;;; On 64 bit linux assemble and run with

;;; nasm -f elf64 ${PROGRAM}.asm && ld -o ${PROGRAM} ${PROGRAM}.o && ./${PROGRAM} ; echo "--->" $?

        
        sys_exit equ 0x01
        write    equ 0x04
        stdout   equ 0x01
        
        section .bss
        
        ;; registers, all 32 bit 
        n       resb 4
        product resb 4
        counter resb 4

        ;; opening message
        section .data
        msg db "Iterative Factorial Machine",0x0a
        len equ $-msg

        section .text
        global _start

_start:


        ;; write message to stdout
        mov eax,write            ; system call number 4 (write)
        mov ebx,stdout            ; file descriptor (stdout)
        mov ecx,msg             ; pointer to buffer
        mov edx,len             ; length of buffer
        int 0x80                ; make system call

        ;; set up problem
        mov [n], dword 12

init:
        ;; (assign product 1)
        mov [product],dword 1

        ;; (assign counter 1)
        mov [counter],dword 1
        
loop:   
        ;; (branch (> counter n) :done)
        mov eax,[n]
        cmp [counter],eax
        jge done
        
        ;; (assign product (* counter product))
        mov eax, [counter]
        mov ebx, [product]
        mul ebx
        mov [product], eax
        
        ;;   (assign counter (inc counter))
        inc dword [counter]
        
        ;;   (goto :loop)
        jmp loop

done:   
        ;;   (assign n product)
        mov eax,[product]
        mov [n],eax

        

printn:
                                ;mov eax,[n]
        mov eax,[n]

printloop:      
        mov edx, 0
        mov ebx, 10
        div ebx
        add edx,0x30
        mov [msg], edx

        push rax
        
        mov eax,write            ; system call number 4 (write)
        mov ebx,stdout           ; file descriptor (stdout)
        mov ecx,msg              ; pointer to buffer
        mov edx,1              ; length of buffer
        int 0x80                 ; make system call

        pop rax
        cmp eax,0
        jnz printloop
    
        
exit:   
        mov ebx,[n]           ; return code
        mov eax,sys_exit       ; system call number 1 (exit)
        int 0x80               ; make system call
        
        
