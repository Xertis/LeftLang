enum class TokenTypes {
    IDENT, NUMBER, STRING, CHAR, VAL, VAR, CONST, LINK,

    KW_U8, KW_U16, KW_U32, KW_U64, KW_UMAX,
    KW_I8, KW_I16, KW_I32, KW_I64, KW_IMAX,

    KW_U8_FAST, KW_U16_FAST, KW_U32_FAST, KW_U64_FAST,
    KW_I8_FAST, KW_I16_FAST, KW_I32_FAST, KW_I64_FAST,

    KW_F32, KW_F64,

    KW_BYTE, KW_SHORT, KW_INT, KW_LONG,  KW_HEAVY,
    KW_BYTE_UNSIGNED, KW_SHORT_UNSIGNED, KW_INT_UNSIGNED, KW_LONG_UNSIGNED, KW_HEAVY_UNSIGNED,

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

val STDINT_VARTYPE_GROUP = arrayOf(
    TokenTypes.KW_I8,
    TokenTypes.KW_I16,
    TokenTypes.KW_I32,
    TokenTypes.KW_I64,
    TokenTypes.KW_IMAX,
    TokenTypes.KW_U8,
    TokenTypes.KW_U16,
    TokenTypes.KW_U32,
    TokenTypes.KW_U64,
    TokenTypes.KW_UMAX,
    TokenTypes.KW_I8_FAST,
    TokenTypes.KW_I16_FAST,
    TokenTypes.KW_I32_FAST,
    TokenTypes.KW_I64_FAST,
    TokenTypes.KW_U8_FAST,
    TokenTypes.KW_U16_FAST,
    TokenTypes.KW_U32_FAST,
    TokenTypes.KW_U64_FAST,
)
val VALID_VARTYPE_GROUP_TOKEN_TYPES = arrayOf(
    TokenTypes.KW_I8,
    TokenTypes.KW_I16,
    TokenTypes.KW_I32,
    TokenTypes.KW_I64,
    TokenTypes.KW_IMAX,
    TokenTypes.KW_U8,
    TokenTypes.KW_U16,
    TokenTypes.KW_U32,
    TokenTypes.KW_U64,
    TokenTypes.KW_UMAX,
    TokenTypes.KW_F32,
    TokenTypes.KW_F64,
    TokenTypes.KW_STRING,
    TokenTypes.KW_BOOL,
    TokenTypes.KW_VOID,
    TokenTypes.KW_BYTE,
    TokenTypes.KW_SHORT,
    TokenTypes.KW_INT,
    TokenTypes.KW_LONG,
    TokenTypes.KW_HEAVY,
    TokenTypes.KW_BYTE_UNSIGNED,
    TokenTypes.KW_SHORT_UNSIGNED,
    TokenTypes.KW_INT_UNSIGNED,
    TokenTypes.KW_LONG_UNSIGNED,
    TokenTypes.KW_HEAVY_UNSIGNED,
    TokenTypes.KW_I8_FAST,
    TokenTypes.KW_I16_FAST,
    TokenTypes.KW_I32_FAST,
    TokenTypes.KW_I64_FAST,
    TokenTypes.KW_U8_FAST,
    TokenTypes.KW_U16_FAST,
    TokenTypes.KW_U32_FAST,
    TokenTypes.KW_U64_FAST,
)

val VALID_PREPROC_GROUP_TOKEN_TYPES = arrayOf(
    TokenTypes.PP_INCLUDE
)

enum class TokenGroups {
    VARTYPE, OPERATOR, IDENT, DELIMETER, PREPROC
}

enum class TokenizerStates {
    DEFAULT, IN_IDENT, IN_NUMBER, IN_STRING, IN_OPERATOR, IN_DELIMETER, IN_PREPROC
}