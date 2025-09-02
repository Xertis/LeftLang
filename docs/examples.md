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


