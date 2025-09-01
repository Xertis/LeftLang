# <img src="https://github.com/Xertis/LeftLang/blob/main/leftlang_logo.png?raw=true" width="30%" alt="Neutron Logo">

**Left** — Язык, который транслируется в **C99**. Добавляет некоторые свои новвоведения и изменяет старый синтаксис

**Left** старается быть простым и чистым транслятором, он не должен тянуть никаких своих зависимостей и должен максимально просто транслироваться в **C99**

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

- C99
```C
#include <stdio.h>

int main(void) {
  unsigned int x = 0;
  printf("%d", x);
  return 0;
}
```
