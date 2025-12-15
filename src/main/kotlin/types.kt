enum class TokenTypes {
    IDENT, NUMBER, STRING, CHAR, VAL, VAR, CONST, AMP,

    KW_U8, KW_U16, KW_U32, KW_U64, KW_UMAX,
    KW_I8, KW_I16, KW_I32, KW_I64, KW_IMAX,

    KW_U8_FAST, KW_U16_FAST, KW_U32_FAST, KW_U64_FAST,
    KW_I8_FAST, KW_I16_FAST, KW_I32_FAST, KW_I64_FAST,

    KW_F32, KW_F64,

    KW_CHAR, KW_SHORT, KW_INT, KW_LONG,  KW_HEAVY,
    KW_CHAR_UNSIGNED, KW_SHORT_UNSIGNED, KW_INT_UNSIGNED, KW_LONG_UNSIGNED, KW_HEAVY_UNSIGNED,

    KW_BOOL, KW_STRING, KW_VOID,
    KW_IF, KW_ELSE, KW_ELIF, KW_WHEN,
    KW_FUN, KW_RETURN,
    KW_WHILE, KW_FOR, KW_LOOP, KW_BREAK, KW_CONTINUE,
    KW_REPEAT, KW_UNTIL,
    KW_TRUE, KW_FALSE,
    KW_IN, KW_AS,

    PLUS, MINUS, MUL, DIV, MOD, QMARK,
    PLUSEQ, MINUSEQ, MULEQ, DIVEQ, MODEQ, // +=, -=, *=, /= %=

    INC, DEC,

    BITOR, BITXOR, BITNOT, SHL, SHR, // | ^ ~ << >>

    EQ, EQEQ, BANGEQ, LT, GT, LTE, GTE, //=, ==, !=, <, >, <=, >=
    AND, OR, NOT,
    LPAREN, RPAREN, LBRACE, RBRACE, LBRACK, RBRACK, COMMA, SEMI, COL, DOT, RANGE, //(){}[],;:. ..
    ARROW,
    NEW_LINE, INCLUDE, FROM, EOF

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
    TokenTypes.KW_CHAR,
    TokenTypes.KW_SHORT,
    TokenTypes.KW_INT,
    TokenTypes.KW_LONG,
    TokenTypes.KW_HEAVY,
    TokenTypes.KW_CHAR_UNSIGNED,
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
    TokenTypes.INCLUDE
)

val PREFIXED_UNARY_TOKEN_TYPES = arrayOf(
    TokenTypes.INC, TokenTypes.DEC, TokenTypes.NOT,
    TokenTypes.MINUS, TokenTypes.AMP,
    TokenTypes.BITNOT
)

val POSTFIXED_UNARY_TOKEN_TYPES = arrayOf(
    TokenTypes.INC, TokenTypes.DEC
)

val OPEQ_TOKEN_TYPES = arrayOf(
    TokenTypes.PLUSEQ, TokenTypes.MINUSEQ,
    TokenTypes.MULEQ, TokenTypes.DIVEQ, TokenTypes.MODEQ,
)

enum class TokenGroups {
    VARTYPE, OPERATOR, IDENT, DELIMETER, PREPROC
}

enum class TokenizerStates {
    DEFAULT, IN_IDENT, IN_NUMBER, IN_STRING, IN_OPERATOR, IN_DELIMETER, IN_PREPROC
}

val StrToType = hashMapOf(
    "char" to TokenTypes.KW_CHAR,
    "Char" to TokenTypes.KW_CHAR_UNSIGNED,
    "short" to TokenTypes.KW_SHORT,
    "Short" to TokenTypes.KW_SHORT_UNSIGNED,
    "int" to TokenTypes.KW_INT,
    "Int" to TokenTypes.KW_INT_UNSIGNED,
    "long" to TokenTypes.KW_LONG,
    "Long" to TokenTypes.KW_LONG_UNSIGNED,
    "heavy" to TokenTypes.KW_HEAVY,
    "Heavy" to TokenTypes.KW_HEAVY_UNSIGNED,

    "u8" to TokenTypes.KW_U8,
    "u16" to TokenTypes.KW_U16,
    "u32" to TokenTypes.KW_U32,
    "u64" to TokenTypes.KW_U64,
    "umax" to TokenTypes.KW_UMAX,

    "i8" to TokenTypes.KW_I8,
    "i16" to TokenTypes.KW_I16,
    "i32" to TokenTypes.KW_I32,
    "i64" to TokenTypes.KW_I64,
    "imax" to TokenTypes.KW_IMAX,

    "FastU8" to TokenTypes.KW_U8_FAST,
    "FastU16" to TokenTypes.KW_U16_FAST,
    "FastU32" to TokenTypes.KW_U32_FAST,
    "FastU64" to TokenTypes.KW_U64_FAST,

    "FastI8" to TokenTypes.KW_I8_FAST,
    "FastI16" to TokenTypes.KW_I16_FAST,
    "FastI32" to TokenTypes.KW_I32_FAST,
    "FastI64" to TokenTypes.KW_I64_FAST,

    "f32" to TokenTypes.KW_F32,
    "f64" to TokenTypes.KW_F64,

    "String" to TokenTypes.KW_STRING,
    "Bool" to TokenTypes.KW_BOOL,
    "Void" to TokenTypes.KW_VOID
)

val TypeToStr = hashMapOf(
    TokenTypes.KW_CHAR to "char",
    TokenTypes.KW_CHAR_UNSIGNED to "Char",
    TokenTypes.KW_SHORT to "short",
    TokenTypes.KW_SHORT_UNSIGNED to "Short",
    TokenTypes.KW_INT to "int",
    TokenTypes.KW_INT_UNSIGNED to "Int",
    TokenTypes.KW_LONG to "long",
    TokenTypes.KW_LONG_UNSIGNED to "Long",
    TokenTypes.KW_HEAVY to "heavy",
    TokenTypes.KW_HEAVY_UNSIGNED to "Heavy",

    TokenTypes.KW_U8 to "u8",
    TokenTypes.KW_U16 to "u16",
    TokenTypes.KW_U32 to "u32",
    TokenTypes.KW_U64 to "u64",
    TokenTypes.KW_UMAX to "umax",

    TokenTypes.KW_I8 to "i8",
    TokenTypes.KW_I16 to "i16",
    TokenTypes.KW_I32 to "i32",
    TokenTypes.KW_I64 to "i64",
    TokenTypes.KW_IMAX to "imax",

    TokenTypes.KW_U8_FAST to "FastU8",
    TokenTypes.KW_U16_FAST to "FastU16",
    TokenTypes.KW_U32_FAST to "FastU32",
    TokenTypes.KW_U64_FAST to "FastU64",

    TokenTypes.KW_I8_FAST to "FastI8",
    TokenTypes.KW_I16_FAST to "FastI16",
    TokenTypes.KW_I32_FAST to "FastI32",
    TokenTypes.KW_I64_FAST to "FastI64",

    TokenTypes.KW_F32 to "f32",
    TokenTypes.KW_F64 to "f64",

    TokenTypes.KW_STRING to "String",
    TokenTypes.KW_BOOL to "Bool",
    TokenTypes.KW_VOID to "Void"
)