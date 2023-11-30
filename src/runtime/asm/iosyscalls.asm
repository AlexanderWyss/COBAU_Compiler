;; Copyright (c) 2020-2022 HSLU Informatik. All rights reserved.
;; This code and any derivative work thereof must remain private.
;; Public distribution is prohibited.

DEFAULT REL

global _read
global _write
global _exit

%ifdef WINDOWS_X64

extern GetStdHandle ; https://docs.microsoft.com/en-us/windows/console/getstdhandle
extern ReadFile     ; https://docs.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-readfile
extern WriteFile    ; https://docs.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-writefile
extern ExitProcess  ; https://docs.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-exitprocess

section .data
    STD_INPUT_HANDLE    EQU     -10
    STD_OUTPUT_HANDLE   EQU     -11

section .bss
alignb 8
    READ            resq 1
    WRITTEN         resq 1

section .text
; systemcall: read bytes from STDIN
_read:      push    rcx                                     ; preserve clobbered registers
            push    rdx
            push    r8
            push    r9
            push    r10
            push    r11

; get read handle
            sub     rsp, 8 + 32                              ; alignment (8 bytes) + shadow space (32)
            mov     rcx, STD_INPUT_HANDLE
            call    GetStdHandle
            add     rsp, 8 + 32

            cmp     rax, -1
            jne     .do_read
            mov     rdi, 99
            call    _exit

            ; read from input
.do_read:   sub     rsp, 8 + 32 + 16                         ; alignment (8 bytes) + shadow space (32) + parameter (8) + padding (8)
            mov     rcx, rax                                 ; rax contains input handle from call to GetStdHandle
            mov     rdx, rdi                                 ; buffer pointer
            mov     r8,  rsi                                 ; buffer length
            lea     r9, [READ]                               ; pointer to memory location that stores number of bytes written
            mov     qword [rsp + 32], 0
            call    ReadFile

            cmp     rax, 0
            jne     .success
            mov     rdi, 99
            call    _exit

.success:   mov     rax, qword [READ]                        ; return number of bytes writen in rax
            add     rsp, 8 + 32 + 16                         ; restore stack pointer (function call)

; restored clobbered registers
            pop     r11
            pop     r10
            pop     r9
            pop     r8
            pop     rdx
            pop     rcx

            ret

;system call: write bytes to STDOUT
_write:     push    rcx                                     ; preserve clobbered registers
            push    rdx
            push    r8
            push    r9
            push    r10
            push    r11

; get write handle
            sub     rsp, 8 + 32                              ; alignment (8 bytes) + shadow space (32)
            mov     rcx, STD_OUTPUT_HANDLE
            call    GetStdHandle
            add     rsp, 8 + 32

            cmp     rax, -1
            jne     .do_write
            mov     rdi, 99
            call    _exit

; output the value
.do_write:  sub     rsp, 8 + 32 + 16                         ; alignment (8 bytes) + shadow space (32) + parameter (8) + padding (8)
            mov     rcx, rax                                 ; rax contains output handle from call to GetStdHandle
            mov     rdx, rdi
            mov     r8,  rsi
            lea     r9,  [WRITTEN]
            mov     qword [RSP + 32], 0
            call    WriteFile

            cmp     rax, 0
            jne     .success
            mov     rdi, 99
            call    _exit

.success:   mov     rax, [WRITTEN]
            add     rsp, 8 + 32 + 16

; restored clobbered registers
            pop     r11
            pop     r10
            pop     r9
            pop     r8
            pop     rdx
            pop     rcx

            ret

_exit:      mov     rcx, rdi
            call    ExitProcess
%endif

%ifdef LINUX_X64

STDIN       EQU     0
STDOUT      EQU     1

READ        EQU     0
WRITE       EQU     1
EXIT        EQU     60

section .text
_read:      push    rcx                     ; preserve clobbered registers
            push    rdx
            push    rsi
            push    r11

            mov     rax, READ               ; save syscall number in rax
            mov     rdx, rsi                ; save second parameter: length of input buffer
            mov     rsi, rdi                ; save first parameter: pointer to input buffer
            mov     rdi, STDIN
            syscall

            pop     r11                     ; restored clobbered registers
            pop     rsi
            pop     rdx
            pop     rcx

            cmp     rax, 0                  ; check return value
            jge     .success
            mov     rsi, 99                 ; on error exit with error code 99 (internal error)
            call    _exit

.success:   ret

_write:     push    rcx                     ; preserve clobbered registers
            push    rdx
            push    rsi
            push    r11

            mov     rax, WRITE              ; save syscall number in rax
            mov     rdx, rsi                ; second parameter: length of input buffer
            mov     rsi, rdi                ; first parameter: pointer to input buffer
            mov     rdi, STDOUT
            syscall

            pop     r11                     ; restored clobbered registers
            pop     rsi
            pop     rdx
            pop     rcx

            cmp     rax, 0                  ; check return value
            jge     .success
            mov     rsi, 99                 ; on error exit with error code 99 (internal error)
            call    _exit

.success:   ret

    _exit:
        mov rax, EXIT
        syscall
%endif

%ifdef MACOS_X64

STDIN   EQU     0
STDOUT  EQU     1

READ    EQU     0x2000000 + 3
WRITE   EQU     0x2000000 + 4
EXIT    EQU     0x2000000 + 1

section .text
_read:      push    rcx
            push    rdx
            push    rsi
            push    r11

            mov     rax, READ
            mov     rdx, rsi                ; save second parameter: length of input buffer
            mov     rsi, rdi                ; save first parameter: pointer to input buffer
            mov     rdi, STDIN
            syscall                         ; execute system call

            jnc     .success                ; check return value
            mov     rsi, 99                 ; on error exit with error code 99 (internal error)
            call    _exit

.success:   pop     r11                     ; restored clobbered registers
            pop     rsi
            pop     rdx
            pop     rcx

            ret

_write:     push    rcx                     ; preserve clobbered registers
            push    rdx
            push    rsi
            push    r11

            mov     rax, WRITE
            mov     rdx, rsi                ; save second parameter: length of input buffer
            mov     rsi, rdi                ; save first parameter: pointer to input buffer
            mov     rdi, STDOUT             ; third parameter: set to STDOUT
            syscall                         ; execute system call

            jnc     .success                ; check return value
            mov     rsi, 99                 ; on error exit with error code 99 (internal error)
            call    _exit

.success:   pop     r11                     ; restored clobbered registers
            pop     rsi
            pop     rdx
            pop     rcx

            ret

_exit:      mov rax, EXIT                   ; first parameter: syscall number (other parameters already set by caller)
            syscall
%endif