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

section .text

_start:
            push  rbp                   ; store pointer to previous frame, and additionally
                                        ; ensure that the stack is aligned for the subsequent
                                        ; function calls. Required for Windows and MacOS.

; implement divider (milestone 1)
            mov rdi, BUFFER ; copy pointer to BUFFER into rdi
            mov rsi, LENGTH ; copy length of byte array into rsi
            call _read ; execute system call
            mov r8, rax ; backup input length

            mov rax, [BUFFER] ; read first value from buffer
            sub rax, 48 ; convert to number
            sar rax, 1 ; divide by 2
            add rax, 48 ; convert to string
            mov [BUFFER], rax ;write back

            mov rax, r8
            sub rax, 1
            ; write to console from buffer
            mov rdi, BUFFER ; copy pointer to BUFFER into rdi
            mov rsi, rax ; copy number of bytes to output to rsi
            call _write ; execute system call

; exit program with exit code 0
exit:       mov   rdi, 0                ; first parameter: set exit code
            call  _exit                 ; call function
