grammar MiniJ;

@header {
package ch.hslu.cobau.minij;
}

// milestone 2: parser

unit : (function | statement)+ EOF; // empty rule to make project compile
statement: (assignement | declaration | function_call) DELIMITER;
declaration: datatype IDENTIFIER;
assignement: IDENTIFIER '=' (value | operation);
function: return_type IDENTIFIER '(' (parameter_declaration? |  (parameter_declaration (',' parameter_declaration)+))')' function_body;
return_type: datatype | 'void';
parameter_declaration: datatype '&'? IDENTIFIER;
function_body: '{' statement* (RETURN (IDENTIFIER | value)? DELIMITER)?'}';
function_call: IDENTIFIER '(' (parameter? |  (parameter (',' parameter)+)) ')';
parameter: IDENTIFIER | value ;
value: NUMBER | STRING | BOOL_VALUE;
datatype: SIMPLE_DATATYPE | ARRAY_DATATYPE;
operation: math_operation | bool_operation;
math_operation: (NUMBER | IDENTIFIER) MATH_SYMBOL (NUMBER | IDENTIFIER);
bool_operation: (BOOL_VALUE | IDENTIFIER) BOOL_OPERATION_SYMBOL (BOOL_VALUE | IDENTIFIER);

RETURN: 'return';
DELIMITER: ';';
MATH_SYMBOL: '*' | '+' | '-' | '/' | '%';
BOOL_OPERATION_SYMBOL: '<' | '>' | '<=' | '>=' | '==' | '!=';
ARRAY_DATATYPE: SIMPLE_DATATYPE '[]';
SIMPLE_DATATYPE: 'int' | 'bool' | 'text';
BOOL_VALUE: 'true' | 'false';
STRING: '"' .*? '"';
NUMBER: ('-' | '+')? DIGIT+;
IDENTIFIER: LETTER (LETTER | DIGIT)*;
DIGIT: [0-9];
LETTER: [a-zA-Z_$];
WHITESPACE: [ \n\r\t]+ -> skip;
