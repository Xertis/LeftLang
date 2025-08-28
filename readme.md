# <img src="https://github.com/Xertis/LeftLang/blob/main/leftlang_logo.png?raw=true" width="30%" alt="Neutron Logo">

**Left** — Язык, который транслируется в **C99**. Добавляет некоторые свои новвоведения и изменяет старый синтаксис

**Left** старается быть простым и чистым транслятором, он не должен тянуть никаких своих зависимостей и должен максимально просто транслироваться в **C99**

> [!note]
> Проект находится на раннем этапе разработки, приведённые ниже примеры лишь показывают то, как язык должен будет выглядеть, а не то
> как он выглядит уже сейчас

### Сравнение кода

- C99
```C
#include <stdio.h>

int main(void) {
  unsigned int x = 0;
  printf("%d", x);
  return 0;
}
```

- Left
```kotlin
#include <stdio.h>

fun main() -> int {
  let x: u32 = 0
  printf("%d", x)
  return 0
}
```
