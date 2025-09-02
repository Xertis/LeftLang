### Создание переменных
```kotlin
#include <stdio.h>

fun main() -> i32 {
  var x: u32 = 0
  printf("%d", x)
  return 0
}
```

### Логические выражения
```kotlin
#include <stdio.h>

fun main() -> i32 {
  if 1 != 2 {
    printf("Wow")
  } elif 2 == 2 {
    printf("WOOOW")
  } else {
    printf("You broke the math")
  }

  return 0
}
```

### Работа с функциями
```kotlin
#include <stdio.h>

fun print_num(x: i32=0) {
  printf("%d", x)
}

fun sum(a: i32, b: i32) -> i32 {
  return a+b
}

fun main() -> i32 {
  print_num() // Дефолтное значение
  print_num(sum(b=5, 10)) // Именованные аргументы
  return 0
}

```

