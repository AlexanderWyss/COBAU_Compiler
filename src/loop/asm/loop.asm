; loop.asm - Example of a loop implementation in assembler
; This program outputs the numbers 0 to 9 in a loop.

DEFAULT REL		; defines relative addresses (required for MacOS)

; import the required external symbols of the system calls abstractions
extern _read
extern _write
extern _exit

; export entry point
global _start		        ; exports public label _start

section .data               ; defines start of section of initialized data
    alignb 8                ; align to 8 bytes (for 64-bit machine)
    CRLF        db  10, 13  ; carriage return (CR) / line feed (LF)
    CRLF_LEN    equ $-CRLF  ; current position ($) minus address of CRLF => 2 bytes

section .bss	            ; defines start of section of uninitialized data
    alignb 8                ; align to 8 bytes (for 64-bit machine)
    BUFFER resb 2           ; output buffer (2 bytes)

section .text		        ; defines start of code section

_start:                     ; program entry point
; loop initialization
        mov r12, 0;         ; initialize loop register (r12)
        mov byte [BUFFER + 1], ' '; set second byte of buffer to space character
        jmp cond            ; check condition before first iteration

; loop iteration: compute ascii character match the value of the loop counter
loop:   mov r13, r12        ; copy r12 to r13
        add r13, '0'        ; add the ascii value of '0' to r13
        mov [BUFFER], r13b  ; copy lowest byte of r13 to first byte in buffer
        mov rdi, BUFFER     ; copy address of variable BUFFER into register rdi
        mov rsi, 2          ; length of output: 2 (number + ' ')
        call _write         ; now call function _write to write to console (stdin)
        add r12, 1          ; increment loop counter

; loop condition check
cond:   cmp r12, 10         ; compare loop register (r12) to 10
        jl  loop            ; perform loop iteration if r12 <= 10

; output a newline
        mov rdi, CRLF       ; copy address of variable BUFFER into register rdi
        mov rsi, CRLF_LEN   ; length of output: 3 (number + CRLF)
        call _write         ; now calls function _write to write to console (stdin)

; terminate program with exit code 0
exit:   mov   rdi, 0        ; terminate program with exit code 0
        call  _exit
