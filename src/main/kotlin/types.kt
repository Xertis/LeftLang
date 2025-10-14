enum class TokenTypes {
    IDENT, NUMBER, STRING, CHAR, VAL, VAR, CONST, LINK,
    KW_U8, KW_U16, KW_U32, KW_U64, KW_USIZE,
    KW_I8, KW_I16, KW_I32, KW_I64, KW_ISIZE,
    KW_F32, KW_F64,
    KW_BOOL, KW_STRING, KW_VOID,
    KW_IF, KW_ELSE, KW_ELIF, KW_WHEN,
    KW_FUN, KW_RETURN,
    KW_WHILE, KW_FOR, KW_BREAK, KW_CONTINUE,
    KW_TRUE, KW_FALSE,
    KW_IN,
    PLUS, MINUS, MUL, DIV, MOD, QMARK,
    PLUSEQ, MINUSEQ, MULEQ, DIVEQ, MODEQ, // +=, -=, *=, /= %=
    INC, DEC,
    EQ, EQEQ, BANGEQ, LT, GT, LTE, GTE, //=, ==, !=, <, >, <=, >=
    AND, OR, NOT,
    LPAREN, RPAREN, LBRACE, RBRACE, COMMA, SEMI, COL, DOT, RANGE, //(){},;:. ..
    ARROW,
    NEW_LINE, PP_INCLUDE, EOF

}

val VALID_VARTYPE_GROUP_TOKEN_TYPES = arrayOf(
    TokenTypes.KW_I8,
    TokenTypes.KW_I16,
    TokenTypes.KW_I32,
    TokenTypes.KW_I64,
    TokenTypes.KW_ISIZE,
    TokenTypes.KW_U8,
    TokenTypes.KW_U16,
    TokenTypes.KW_U32,
    TokenTypes.KW_U64,
    TokenTypes.KW_USIZE,
    TokenTypes.KW_F32,
    TokenTypes.KW_F64,
    TokenTypes.KW_STRING,
    TokenTypes.KW_BOOL,
    TokenTypes.KW_VOID
)

val VALUD_PREPROC_GROUP_TOKEN_TYPES = arrayOf(
    TokenTypes.PP_INCLUDE
)

enum class TokenGroups {
    VARTYPE, OPERATOR, IDENT, DELIMETER, PREPROC
}

enum class TokenizerStates {
    DEFAULT, IN_IDENT, IN_NUMBER, IN_STRING, IN_OPERATOR, IN_DELIMETER, IN_PREPROC
}