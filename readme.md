# <img src="https://github.com/Xertis/LeftLang/blob/main/leftlang_logo.png?raw=true" width="30%" alt="Neutron Logo">

**Left** — Язык, который транслируется в **C++**. Добавляет некоторые свои новвоведения и изменяет старый синтаксис

### Сравнение кода

- Left
```kotlin
#include <stdio.h>

fun main() -> i32 {
  var x: u32 = 0
  printf("%d", x)
  return 0
}
```

- C++
```C
#include <stdio.h>

int main(void) {
  unsigned int x = 0;
  printf("%d", x);
  return 0;
}
```
