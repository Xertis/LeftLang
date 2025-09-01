### Создание переменных
- **Left**
```kotlin
#include <stdio.h>

fun main() -> i32 {
  var x: u32 = 0
  printf("%d", x)
  return 0
}
```

- **C99**
```C
#include <stdio.h>

int main(void) {
  unsigned int x = 0;
  printf("%d", x);
  return 0;
}
```

### Логические выражения
- **Left**
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

- **C99**
```C
#include <stdio.h>

int main() {
  if (1 != 2) {
    printf("Wow");
  } else if (2 == 2) {
    printf("WOOOW");
  } else {
    printf("You broke the math");
  }
  return 0;
}
```


