package lexer.tokens

enum class Types {
    IDENT, NUMBER, STRING,
    KW_U8, KW_U16, KW_U32, KW_U64, KW_USIZE,
    KW_I8, KW_I16, KW_I32, KW_I64, KW_ISIZE,
    KW_IF, KW_ELSE, KW_ELIF,
    KW_FUN, KW_RETURN,
    KW_WHILE, KW_FOR,
    KW_TRUE, KW_FALSE,
    PLUS, MINUS, MUL, DIV,
    EQ, EQEQ, BANGEQ, LT, GT, LTE, GTE, //=, ==, !=, <, >, <=, >=
    AND, OR, NOT,
    LPAREN, RPAREN, LBRACE, RBRACE, COMMA, SEMI, DOT, QUOT, //(){},;."
    NEW_LINE, PREPROC, EOF

}