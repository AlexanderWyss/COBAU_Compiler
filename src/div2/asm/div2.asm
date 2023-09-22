DEFAULT REL

; import the required external symbols for the system call functions
extern _read
extern _write
extern _exit

; export entry point
global _start
    LENGTH EQU 20
section .bss ; uninitialized data
    alignb 8 ; align to 8 bytes (for 64-bit machine)
    BUFFER resb LENGTH ; buffer (64 bytes)

Section .data
  TEXTINPUT: db 'Please enter an integer value:', 13, 10
  TEXTINPUTLEN: equ $-TEXTINPUT
  TEXTOUTPUT: db 'The result of value / 2 is: '
  TEXTOUTPUTLEN: equ $-TEXTOUTPUT

section .text

_start:
            push  rbp                   ; store pointer to previous frame, and additionally
                                        ; ensure that the stack is aligned for the subsequent
                                        ; function calls. Required for Windows and MacOS.
readInput:
            mov rdi, TEXTINPUT
            mov rsi, TEXTINPUTLEN
            call _write
            mov rdi, BUFFER ; copy pointer to BUFFER into rdi
            mov rsi, LENGTH ; copy length of byte array into rsi
            call _read ; read input, length of input in rax

bufferToNumber: ; expects buffer initialized and length in rax
            mov r9, rax ; mover buffer length to r9
            mov r8, 0 ; init number with zero in r8
            mov rcx, 0 ; init loop counter to rcx

bufferToNumberLoop: ; r8=number, rcx=index, r9=buffer_length
            mov rax, BUFFER ; set rax to start of buffer
            add rax, r9 ; add length to position of buffer
            sub rax, rcx
            sub rax, 1
            movzx rdx, byte [rax] ; read value of buffer
            cmp rdx, '-'
            je negateNumber
            cmp rdx, 48 ; filter non ascii
            jl shortenLoop
            cmp rdx, 57 ; filter non ascii
            jg shortenLoop
            sub rdx, 48 ; convert ascii to number
            cmp rcx, 0 ; if index is 0 10^0
            je bufferToNumberOfTenthPowerLoopEnd
            mov rax, 0 ; init index of tenth power loop
bufferToNumberTenthPowerLoopStart:
            imul rdx, 10 ; multiply number by 10
            add rax, 1 ; add 1 to inner loop counter
            cmp rax, rcx ; if inner loop lower then outerloop multipl by ten again
            jl bufferToNumberTenthPowerLoopStart
bufferToNumberOfTenthPowerLoopEnd:
            add rdx, r8 ; add digit to number
            mov r8, rdx ; write back number
            add rcx, 1; increment index
            cmp rcx, r9
            jl bufferToNumberLoop
            jmp divide
shortenLoop:
    sub r9, 1
    jmp bufferToNumberLoop
negateNumber:
    sub r9, 1
    neg r8

divide:
            mov rdx, 0
            mov rax, r8
            cqo ; sign extend
            mov r10, 2
            idiv r10
            mov r8, rax

numberToBuffer: ; r8=number, rbx=buffer begin address
            mov rcx, 0 ; buffer length
            mov rbx, BUFFER
            add rbx, LENGTH
            mov r12, 0 ; 0 = positive
            cmp r8, 0
            jge numberToBufferLoopStart
            mov r12, 1 ; 1 = negative

numberToBufferLoopStart:
            sub rbx, 1 ; buffer index -1
            mov rdx, 0
            mov rax, r8
            cqo ; sign extend
            mov r10, 10
            idiv r10
            cmp r12, 0
            je numberToBufferAdd
            neg rdx
numberToBufferAdd:
            add rdx, 48
            mov [rbx], dl
            mov r8, rax
            add rcx, 1
            cmp rax, 0
            jne numberToBufferLoopStart
            cmp r12, 0
            je writeBuffer
            sub rbx, 1
            add rcx, 1
            mov [rbx], byte '-'



writeBuffer:
            mov rdi, TEXTOUTPUT
            mov rsi, TEXTOUTPUTLEN
            call _write
            mov rdi, rbx ; copy pointer to BUFFER into rdi
            mov rsi, rcx ; copy number of bytes to output to rsi
            call _write ; execute system call

; exit program with exit code 0
exit:       mov   rdi, 0                ; first parameter: set exit code
            call  _exit                 ; call function
