package lexer

enum class TokenTypes {
    IDENT, NUMBER, STRING, CHAR,
    KW_U8, KW_U16, KW_U32, KW_U64, KW_USIZE,
    KW_I8, KW_I16, KW_I32, KW_I64, KW_ISIZE,
    KW_IF, KW_ELSE, KW_ELIF,
    KW_FUN, KW_RETURN,
    KW_WHILE, KW_FOR,
    KW_TRUE, KW_FALSE,
    PLUS, MINUS, MUL, DIV,
    PLUSEQ, MINUSEQ, MULEQ, DIVEQ, // +=, -=, *=, /=
    INC, DEC,
    EQ, EQEQ, BANGEQ, LT, GT, LTE, GTE, //=, ==, !=, <, >, <=, >=
    AND, OR, NOT,
    LPAREN, RPAREN, LBRACE, RBRACE, COMMA, SEMI, COL, DOT, //(){},;:.
    NEW_LINE, PREPROC, EOF

}

enum class TokenizerStates {
    DEFAULT, IN_IDENT, IN_NUMBER, IN_STRING, IN_OPERATOR, IN_DELIMETER
}