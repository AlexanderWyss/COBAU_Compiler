;; Copyright (c) 2020-2022 HSLU Informatik. All rights reserved.
;; This code and any derivative work thereof must remain private.
;; Public distribution is prohibited.

DEFAULT REL

; import the required external symbols for the system call functions
extern _read
extern _write
extern _exit

; export entry point
global readInt
global writeInt
global readChar
global writeChar

; constants
BUFFER_SIZE     equ   128                   ; size of buffer

section .bss
alignb 8
    BUFFER          resb BUFFER_SIZE    ; buffer to store input and output

section .text

; writes a single character to standard output
writeChar:   push rdi
             mov  rdi, rsp
             mov  rsi, 1
             call _write
             pop  rdi
             ret

; reads a single character from console
readChar:
; read from console (standard input into buffer) using system call _read
            mov   rdi, BUFFER           ; set first parameter: pointer to READ_BUFFER
            mov   rsi, 1                ; set second parameter: size of buffer
            call  _read                 ; call function (returns length of input in rax)
            mov   rsi, BUFFER
            mov   rax, 0
            mov   al, byte [rsi]        ; load current byte
            ret

; read a 64-bit value from console
; store the
readInt:
; read from console (standard input into buffer) using system call _read
read_input: mov   rdi, BUFFER           ; set first parameter: pointer to READ_BUFFER
            mov   rsi, BUFFER_SIZE      ; set second parameter: size of buffer
            call  _read                 ; call function (returns length of input in rax)

; convert the value from ascii to 64-bit signed integer
; requires rax to contain length of input stored in READ_BUFFER

; check the sign of the input, expects:
.get_sign:  mov   rsi, BUFFER           ; rsi contains pointer to the READ_BUFFER
            mov   r9, rsi               ; copy rsi to r9
            add   r9, rax               ; and add rax (input length), r9 now contains pointer to the end of the buffer
            mov   r11, 0                ; r11 contains sign flag (either 1 if minus, otherwise 0)
            cmp   rsi, r9               ; check if rsi == r9 (empty input)
            je    .loop_init            ; exit the check if that is the case
            cmp   byte [rsi], '-'       ; otherwise check if byte at first position is '-'
            jne   .loop_init            ; if not do nothing (retain sign of 1)
            add   rsi, 1                ; advance pointer in the input buffer
            mov   r11, 1                ; set minus flag

.loop_init: mov   rax, 0                ; set rax to 0 (rax will contains converted number)
            jmp   .loop_cond

.loop_iter: mov   r10, 0                ; clear r10
            mov   r10b, byte [rsi]      ; load current byte
            sub   r10b, '0'             ; subtract ASCII code of 0 to obtain integer

; check for non-numeric character (== done parsing)
            cmp   r10, 0                ; done if value < 0
            jl    .done
            cmp   r10, 9                ; done if value > 9
            jg    .done

; add new digit: rax = rax * 10 + r10
            mov   rcx, 10               ; set rcx to 10
            imul  rcx                   ; rdx:rax = rax * 10 (ignore rdx, only need lower 64 bits)
            add   rax, r10              ; add result to number
            add   rsi, 1                ; increment pointer in buffer

.loop_cond: cmp   rsi, r9               ; stop when reaching end of buffer (rsi == r9)
            jne   .loop_iter
.done:

; set the sign of the number
; (r11 must contain sign, rax must contain value without sign)
set_sign:   cmp   r11, 1
            jne   .done
            neg   rax

.done:      ret

; outputs a 64-bit integer (signed) on standard output
writeInt:   push  rbp                   ; align stack to 16 bytes
            mov   rax, rdi              ; load first parameter in rax

; initialize loop
            mov   rsi, BUFFER           ; load pointer to buffer into rsi
            add   rsi, BUFFER_SIZE      ; add BUFFER_SIZE so rsi points to the end of the buffer
            mov   r11, 0                ; clear sign flag
            cmp   rax, 0                ; check if the number is negative (check sign in r11 == -1)
            jge   .loop_iter            ; non-negative: directly enter the loop
            neg   rax                   ; negative: eliminate sign
            mov   r11, 1                ; remember sign in r11

; extract each digit (backwards): rax = rax / 10, rdx = rax % 10 (% is remainder == desired digit)
.loop_iter: mov   rdx, 0                ; clear rdx (upper 64-bits of first operand)
            mov   rcx, 10               ; set second operand to 10
            idiv  rcx                   ; rdx:rax / rcx = rax with remainder in rdx

            sub   rsi, 1                ; advance pointer of the write buffer
            add   rdx, '0'              ; convert division remainder to ascii
            mov   byte [rsi], dl        ; and store in write buffer (lowest byte of rdx)
.loop_cond: cmp   rax, 0                ; check if remainder is zero (== we are done)
            jne   .loop_iter

.addsign:   cmp   r11, 1                ; check if sign is set
            jne   .done                 ; if not we are done
            sub   rsi, 1                ; and advance pointer
            mov   byte [rsi], '-'       ; otherwise output sign

; calculate buffer positions
; BUFFER_END = BUFFER + BUFFER_SIZE
; length = BUFFER_END - rsi (current position)
.done:      mov   rax, BUFFER           ; copy pointer to BUFFER TO rax
            add   rax, BUFFER_SIZE      ; add BUFFER_SIZE to rax
            sub   rax, rsi              ; subtract current position (rsi) from rax

; output the value with the output message
; (expect rax: length of message, rsi: pointer to start of message)
output:     mov   rdi, rsi              ; first parameter start of message in buffer
            mov   rsi, rax              ; second parameter length of message in buffer
            call  _write                ; call system function

            pop   rbp
            ret
