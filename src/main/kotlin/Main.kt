import console.Console

fun main(_args: Array<String>) {
    val args = arrayOf(
        "translate", """
            
            include "stdio.h"
            
            fun sum(a: int=0, b: int=0) -> int {
                return a+b
            }
            
            fun main() -> int {
                var x: int = 0
                   
                for x, y in [1; 2) + (-100; 1) {
                    printf("%d %d", 
                        sum(
                            y=y,
                            x=x
                        )
                    )
                }
            }
        """.trimIndent(), "-itContent"
    )
    val status = Console().process(args)

    if (!status) throw RuntimeException("Abort")
}