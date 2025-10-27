package tokens

import TokenTypes
import TokenGroups
import VALID_VARTYPE_GROUP_TOKEN_TYPES
import VALID_PREPROC_GROUP_TOKEN_TYPES

data class Token(val type: TokenTypes, val value: String, val ln: Int, val col: Int) {
    val group: TokenGroups = when (type) {
        in VALID_VARTYPE_GROUP_TOKEN_TYPES -> TokenGroups.VARTYPE
        in VALID_PREPROC_GROUP_TOKEN_TYPES -> TokenGroups.PREPROC
        else -> TokenGroups.IDENT
    }
}

