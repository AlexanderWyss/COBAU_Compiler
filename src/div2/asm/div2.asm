DEFAULT REL

; import the required external symbols for the system call functions
extern _read
extern _write
extern _exit

; export entry point
global _start
    LENGTH EQU 22
section .bss ; uninitialized data
    alignb 8 ; align to 8 bytes (for 64-bit machine)
    BUFFER resb LENGTH ; buffer (22 Byte, max number of chars used to represent 64bit int with - and \n\r)

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
            mov rdi, TEXTINPUT ; write input request
            mov rsi, TEXTINPUTLEN
            call _write
            mov rdi, BUFFER ; read input into buffer and actual input length in rax
            mov rsi, LENGTH
            call _read

bufferToNumber: ; expects buffer filled with input, length of input in rax
            mov r9, rax ; mover buffer length to r9
            mov r8, 0 ; init number with zero in r8
            mov rcx, 0 ; init loop index to rcx

bufferToNumberLoop: ; r8=number, rcx=index, r9=buffer_length
            mov rax, BUFFER ; set rax to start of buffer
            add rax, r9 ; add length to position of buffer (start and end of buffer)
            sub rax, 1 ; subtract 1 from current position (length - 1 -> end of buffer)
            sub rax, rcx ; subtract current index (from end of buffer minus current index)
            movzx rdx, byte [rax] ; read value of buffer
            cmp rdx, '-' ; if value is '-' negate number and goto divide
            je negateNumber
            cmp rdx, 48 ; filter non ascii
            jl ignoreCharacter
            cmp rdx, 57 ; filter non ascii
            jg ignoreCharacter
            sub rdx, 48 ; convert ascii to number
            cmp rcx, 0 ; if index is 0 do not multiply by 10
            je bufferToNumberOfTenthPowerLoopEnd
            mov rax, 0 ; init index of tenth power loop
bufferToNumberTenthPowerLoopStart:
            imul rdx, 10 ; multiply number by 10
            add rax, 1 ; add 1 to inner loop counter
            cmp rax, rcx ; if inner loop lower then outerloop multiply by ten again (eg. 10^1, 10^2, ...)
            jl bufferToNumberTenthPowerLoopStart
bufferToNumberOfTenthPowerLoopEnd:
            add r8, rdx ; add digit to number
            add rcx, 1; increment index
            cmp rcx, r9 ; if current index is lower then length of buffer, read next digit
            jl bufferToNumberLoop
            jmp divide ; jump to divide, ignoreCharacter and negateNumber are helpers
ignoreCharacter:
    sub r9, 1 ; subract 1 from max length, combined with index this ignores the current character
    jmp bufferToNumberLoop ; next character
negateNumber:
    neg r8 ; negate number

divide: ; number saved in r8
            mov rdx, 0 ; idiv works with 128 bit (rdx:rax), we work with 64bit ints, rdx always zero
            mov rax, r8
            cqo ; sign extend, when the number is negative the sign bit is rax, idiv works wih rdx:rax, cqo fixes the sign value
            mov r10, 2; init r10 with 2
            idiv r10 ; divide by 2
            mov r8, rax ; write divided number back to r8

numberToBuffer: ; r8=number, rbx=buffer begin address
            mov rcx, 0 ; init loop index
            mov rbx, BUFFER ; point to end of buffer with rbx
            add rbx, LENGTH
            mov r12, 0 ; remember if number is positive or negative: 0 = positive
            cmp r8, 0
            jge numberToBufferLoopStart
            mov r12, 1 ; 1 = negative

numberToBufferLoopStart:
            sub rbx, 1 ; buffer index -1
            mov rdx, 0 ; same as divide
            mov rax, r8
            cqo ; sign extend
            mov r10, 10
            idiv r10
            cmp r12, 0 ; when negative, negate current digit for printing
            je numberToBufferAdd
            neg rdx
numberToBufferAdd:
            add rdx, 48; convert to ascii
            mov [rbx], dl ; write remainder to buffer
            mov r8, rax ; write result to current number
            add rcx, 1 ; increment index
            cmp rax, 0 ; if result is zero, end loop
            jne numberToBufferLoopStart
            cmp r12, 0 ; if number is negative append '-'
            je writeBuffer
            sub rbx, 1
            add rcx, 1
            mov [rbx], byte '-'

writeBuffer:
            mov rdi, TEXTOUTPUT ; wirte output text
            mov rsi, TEXTOUTPUTLEN
            call _write
            mov rdi, rbx ; write result
            mov rsi, rcx
            call _write

; exit program with exit code 0
exit:       mov   rdi, 0                ; first parameter: set exit code
            call  _exit                 ; call function
