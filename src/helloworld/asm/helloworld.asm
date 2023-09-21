DEFAULT REL		; defines relative addresses are used; is a NASM command
; import the required external symbols / system calls, you find these function in iosyscalls.asm
extern _write
extern _exit

global _start; exports public label _start; NASM command

Section .data
  TEXTHW: db 'hello world', 13, 10	; defines the string constant TEXTHW with ‘hello world’&cr/lf)
                                    ; don’t add CR (10) in Linux
  TXTLEN: equ $-TEXTHW		        ; Reads the lenght of TEXTHW ino TXTLN

section .text	        ; defines start of code section
_start:		            ; start label
  mov rdi, TEXTHW     	; copy address of constant TEXTHW into register rdi
  mov rsi, TXTLEN       ; writes the length of TEXTHW into register rsi
  call _write         	; now calls function _write to write to console (stdout)
  mov   rdi, 0        	; terminate program with exit 0
  call  _exit
