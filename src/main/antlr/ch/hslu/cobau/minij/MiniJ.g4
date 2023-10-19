grammar MiniJ;

@header {
package ch.hslu.cobau.minij;
}

// milestone 2: parser

// TODO rename shit, so the parse tree is easyer to understand

unit : (function | statement | declaration | record_declaration)* EOF;
statement: (assignement | function_call) DELIMITER;
declaration: datatype IDENTIFIER DELIMITER;
assignement: variable_accessor '=' usable_value;
function: return_type IDENTIFIER '(' (parameter_declaration? |  (parameter_declaration (',' parameter_declaration)+))')' function_program_block;
return_type: datatype | 'void';
parameter_declaration: datatype PARAMETER_BY_REFERENCE? IDENTIFIER;
if_statement: (IF '(' operation ')' (program_block | single_line_program_block)) else_if_statement* else_statement?;
else_if_statement: ELSE IF '(' operation ')' (program_block | single_line_program_block);
else_statement: ELSE (program_block | single_line_program_block);
while_loop: WHILE '(' operation ')' (program_block | single_line_program_block);
function_program_block: '{' ( statement | declaration | if_statement | while_loop | program_block)* return_statement?'}' DELIMITER?;
program_block: ('{' ( statement | if_statement | while_loop | program_block)* return_statement?'}' DELIMITER?);
single_line_program_block: statement | return_statement;
return_statement: RETURN (usable_value)? DELIMITER;
record_declaration: RECORD IDENTIFIER record_block;
record_block: '{' declaration* '}' DELIMITER?;
variable_accessor: variable_accessor '.' variable_accessor | variable_accessor '[' usable_value ']' | IDENTIFIER;
function_call: IDENTIFIER '(' (usable_value? |  (usable_value (',' usable_value)+)) ')';
usable_value: variable_accessor | value | operation | function_call;
value: NUMBER | STRING | BOOL_VALUE;
datatype: SIMPLE_DATATYPE | ARRAY_DATATYPE | IDENTIFIER;
operation: '(' operation ')'
            | (MINUS | BOOL_NEGATE | INCREMENT | DECREMENT) operation
            | variable_accessor (INCREMENT | DECREMENT)
            | operation (TIMES | DIVIDE | MODULO) operation
            | operation (MINUS | PLUS) operation
            | operation (LESS_THAN | LEQ_THAN | GREATER_THEN | GEQ_THAN) operation
            | operation (EQUAL_TO | NEQ_TO) operation
            | operation BOOL_AND operation
            | operation BOOL_OR operation
            | (BOOL_VALUE | NUMBER | variable_accessor | function_call);

RETURN: 'return';
RECORD: 'record';
IF: 'if';
ELSE: 'else';
WHILE: 'while';
DELIMITER: ';';
TIMES: '*';
DIVIDE: '/';
MODULO: '%';
INCREMENT: '++';
DECREMENT: '--';
PLUS: '+';
MINUS: '-';
BOOL_NEGATE: '!';
BOOL_AND: '&&';
BOOL_OR: '||';
LESS_THAN: '<';
LEQ_THAN: '<=';
GREATER_THEN: '>';
GEQ_THAN: '>=';
EQUAL_TO: '==';
NEQ_TO: '!=';
PARAMETER_BY_REFERENCE: '&';
ARRAY_DATATYPE: SIMPLE_DATATYPE '[]'+;
SIMPLE_DATATYPE: 'int' | 'bool' | 'text';
BOOL_VALUE: 'true' | 'false';
STRING: '"' .*? '"';
NUMBER: ('-' | '+')? DIGIT+;
IDENTIFIER: LETTER (LETTER | DIGIT)*;
DIGIT: [0-9];
LETTER: [a-zA-Z_$];
COMMENT: ('//' .*? NL) -> skip;
MULTI_LINE_COMMENT: ('/*' .*? '*/') -> skip;
WHITESPACE: [ \n\r\t]+ -> skip;
NL: '\r'? '\n' | '\r' | EOF;
