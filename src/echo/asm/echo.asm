DEFAULT REL		; defines relative addresses

; import the required external symbols / system calls
extern _read
extern _write
extern _exit

; export entry point
global _start		; exports public label _start

    LENGTH EQU 128      ; definition constant LENGTH (buffer length)

section .bss		; defines start of section of unitialized data
    alignb 8            ; align to 8 bytes (for 64-bit machine)
    BUFFER resb LENGTH  ; buffer (128 bytes)

section .text		; defines start of code section

_start:			; start label
    mov rdi, BUFFER     ; copy address of variable BUFFER into register rdi
    mov rsi, LENGTH     ; copy value of LENGHT into register rsi
    call _read		; now calls function _read to read from console (stdin)

    mov rdi, BUFFER     ; copy address of variable BUFFER into register rdi
    mov rsi, rax        ; register rax contians the number of typed char, copy value of rax into register rsi
    call _write         ; now calls function _write to write to console (stdin)

    mov   rdi, 0        ; terminate program with exit 0
    call  _exit
